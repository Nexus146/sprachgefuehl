package de.sprachgefuehl.app.account

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.RecordingRef
import de.sprachgefuehl.app.data.account.AccountUiState
import de.sprachgefuehl.app.data.account.CloudAccountRepository
import de.sprachgefuehl.app.data.account.CloudLearningState
import de.sprachgefuehl.app.data.account.PushStateParams
import de.sprachgefuehl.app.data.account.PushStateResult
import de.sprachgefuehl.app.data.account.SupabaseConfig
import de.sprachgefuehl.app.data.account.SyncPhase
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.Instant

/**
 * The account layer against the real Supabase project, not a mock. It runs on an emulator as
 * `./gradlew :app:connectedDebugAndroidTest`.
 *
 * The test provisions a throwaway account with a unique address, exercises upload, a second
 * device, audio transfer, a real revision conflict and the deletion function, and leaves no
 * account behind. It needs a project where "Confirm email" is off for the duration of the run,
 * because there is no mailbox to click.
 */
@RunWith(AndroidJUnit4::class)
class CloudAccountLiveTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val prefs by lazy {
        context.getSharedPreferences("cloud_account", android.content.Context.MODE_PRIVATE)
    }

    private val password = "Testpasswort-2026"
    private val email = "sprachgefuehl.test.${System.currentTimeMillis()}@example.com"
    private val payload = ByteArray(4096) { (it % 251).toByte() }
    private val recordingFile by lazy {
        File(File(context.filesDir, "recordings"), "rec_live_test.m4a")
    }

    @Test(timeout = 240_000)
    fun fullAccountLifecycle() {
        assertTrue("Supabase ist nicht konfiguriert.", SupabaseConfig.configured)
        prefs.edit().clear().commit()

        recordingFile.parentFile?.mkdirs()
        recordingFile.writeBytes(payload)

        var local = Progress(
            onboarded = true,
            contentVersion = 1,
            completedLessons = setOf("live-a"),
            studySeconds = 90,
            recordings = listOf(
                RecordingRef(
                    id = "rec_live_test",
                    label = "Testaufnahme",
                    path = recordingFile.absolutePath,
                    day = 0L
                )
            )
        )
        var imports = 0

        val device = CloudAccountRepository(
            context,
            progressProvider = { local },
            importProgress = { progress ->
                local = progress
                imports++
                true
            },
            autoSync = false
        )

        val external = createSupabaseClient(SupabaseConfig.url, SupabaseConfig.anonKey) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }

        try {
            // 1. Registration signs in and counts as confirmed.
            device.register(email, password)
            assertTrue(
                "Registrierung hat keine Sitzung ergeben: ${describe(device.state.value)}",
                waitUntil { device.state.value.signedIn }
            )
            assertTrue(
                "E-Mail gilt nach der Registrierung als unbestätigt.",
                device.state.value.emailVerified
            )

            // 2. The first sync writes revision 1 including the recording.
            runBlocking { device.syncNowAndWait() }
            assertEquals(
                "Erste Sicherung fehlgeschlagen: ${describe(device.state.value)}",
                SyncPhase.SYNCED,
                device.state.value.phase
            )

            runBlocking {
                external.auth.signInWith(Email) {
                    this.email = this@CloudAccountLiveTest.email
                    this.password = this@CloudAccountLiveTest.password
                }
            }
            val userId = requireNotNull(
                runBlocking { external.auth.currentUserOrNull() }
            ).id

            val cloudAfterFirstSync = runBlocking { readCloud(external, userId) }
            assertNotNull("Die Cloud-Zeile fehlt nach dem ersten Sync.", cloudAfterFirstSync)
            assertEquals(1L, cloudAfterFirstSync!!.revision)
            assertEquals(setOf("live-a"), decoded(cloudAfterFirstSync).completedLessons)
            assertEquals(90, decoded(cloudAfterFirstSync).studySeconds)

            val remoteFiles = runBlocking { external.storage.from("recordings").list(userId) }
            assertTrue(
                "Die Aufnahme wurde nicht hochgeladen: ${remoteFiles.map { it.name }}",
                remoteFiles.any { it.name == "rec_live_test.m4a" }
            )

            // 3. A fresh device pulls the cloud document instead of asking a question.
            device.close()
            prefs.edit().clear().commit()
            recordingFile.delete()
            local = Progress()
            imports = 0

            val fresh = CloudAccountRepository(
                context,
                progressProvider = { local },
                importProgress = { progress ->
                    local = progress
                    imports++
                    true
                },
                autoSync = false
            )
            try {
                fresh.login(email, password)
                assertTrue(
                    "Anmeldung auf dem zweiten Gerät fehlgeschlagen: ${describe(fresh.state.value)}",
                    waitUntil { fresh.state.value.signedIn }
                )

                runBlocking { fresh.syncNowAndWait() }
                assertEquals(
                    "Zweites Gerät hat nicht geladen: ${describe(fresh.state.value)}",
                    SyncPhase.SYNCED,
                    fresh.state.value.phase
                )
                assertEquals("Der Cloud-Stand wurde nicht übernommen.", 1, imports)
                assertTrue("Lokaler Stand blieb leer.", local.onboarded)
                assertEquals(setOf("live-a"), local.completedLessons)

                assertTrue(
                    "Die Aufnahme wurde nicht zurückgeholt.",
                    recordingFile.exists() && recordingFile.readBytes().contentEquals(payload)
                )
                assertEquals(
                    "Der Aufnahmepfad zeigt nicht auf das Gerät.",
                    recordingFile.absolutePath,
                    local.recordings.single().path
                )

                // 4. Another device pushes a newer revision while this one has local changes.
                local = local.copy(
                    completedLessons = local.completedLessons + "live-b",
                    studySeconds = local.studySeconds + 30
                )
                val newerDocument = Progress(
                    onboarded = true,
                    contentVersion = 1,
                    completedLessons = setOf("live-a", "cloud-b"),
                    studySeconds = 400
                )
                val pushed = runBlocking { pushForced(external, newerDocument) }
                assertFalse("Der erzwungene Upload meldete einen Konflikt.", pushed.conflict)
                assertEquals(2L, pushed.revision)

                runBlocking { fresh.syncNowAndWait() }
                assertEquals(
                    "Der Konflikt wurde nicht erkannt: ${describe(fresh.state.value)}",
                    SyncPhase.CONFLICT,
                    fresh.state.value.phase
                )

                // 5. Choosing the cloud side replaces the local document and keeps it saved.
                fresh.resolveConflict(useCloud = true)
                assertTrue(
                    "Konfliktlösung endete nicht im gesicherten Zustand: ${describe(fresh.state.value)}",
                    waitUntil { fresh.state.value.phase == SyncPhase.SYNCED }
                )
                assertEquals(setOf("live-a", "cloud-b"), local.completedLessons)
                assertEquals(400, local.studySeconds)

                // 6. Deleting the account removes the user, its row and its recording.
                fresh.deleteAccount()
                assertTrue(
                    "Konto wurde nicht abgemeldet: ${describe(fresh.state.value)}",
                    waitUntil { !fresh.state.value.signedIn }
                )
                fresh.login(email, password)
                assertTrue(
                    "Ein gelöschtes Konto lässt sich noch anmelden.",
                    waitUntil { fresh.state.value.error != null }
                )
                assertFalse(fresh.state.value.signedIn)
            } finally {
                fresh.close()
            }
        } finally {
            device.close()
            runCatching { runBlocking { external.auth.signOut() } }
        }
    }

    private suspend fun readCloud(
        client: io.github.jan.supabase.SupabaseClient,
        userId: String
    ): CloudLearningState? = client.from("learning_states")
        .select { filter { eq("user_id", userId) } }
        .decodeSingleOrNull<CloudLearningState>()

    private suspend fun pushForced(
        client: io.github.jan.supabase.SupabaseClient,
        document: Progress
    ): PushStateResult = client.postgrest
        .rpc(
            "push_learning_state",
            PushStateParams(
                progress = json.encodeToJsonElement(Progress.serializer(), document).jsonObject,
                expectedRevision = 0L,
                clientUpdatedAt = Instant.now().toString(),
                force = true
            )
        )
        .decodeSingle<PushStateResult>()

    private fun decoded(state: CloudLearningState): Progress =
        json.decodeFromJsonElement(Progress.serializer(), state.progress)

    private fun waitUntil(timeoutMs: Long = 30_000, check: () -> Boolean): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (check()) return true
            Thread.sleep(150)
        }
        return check()
    }

    private fun describe(state: AccountUiState): String =
        "phase=${state.phase} signedIn=${state.signedIn} verified=${state.emailVerified} " +
            "busy=${state.busy} error=${state.error} message=${state.message}"
}
