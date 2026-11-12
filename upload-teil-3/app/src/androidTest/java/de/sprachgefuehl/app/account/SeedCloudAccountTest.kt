package de.sprachgefuehl.app.account

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.sprachgefuehl.app.data.ContentRepository
import de.sprachgefuehl.app.data.Memory
import de.sprachgefuehl.app.data.Profile
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.SessionLog
import de.sprachgefuehl.app.data.SkillProfile
import de.sprachgefuehl.app.data.SkillState
import de.sprachgefuehl.app.data.account.CloudAccountRepository
import de.sprachgefuehl.app.data.account.SupabaseConfig
import de.sprachgefuehl.app.data.account.SyncPhase
import de.sprachgefuehl.app.domain.ProfileCatalog
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

/**
 * Legt einen realistischen Lernstand in einem bestehenden Konto ab, damit der Weg
 * „Cloud hat schon Fortschritt“ auf einem frischen Gerät prüfbar ist. Der Stand entsteht aus dem
 * echten Inhaltskatalog und geht über denselben RPC wie in der App hoch – kein handgeschriebenes
 * JSON, das später nicht mehr zum Modell passt.
 *
 * Die Zugangsdaten stehen bewusst nirgends im Code, sondern kommen als Instrumentation-Argument:
 * ```
 * ./gradlew :app:connectedDebugAndroidTest \
 *   -Pandroid.testInstrumentationRunnerArguments.class=de.sprachgefuehl.app.account.SeedCloudAccountTest \
 *   -Pandroid.testInstrumentationRunnerArguments.email=… \
 *   -Pandroid.testInstrumentationRunnerArguments.password=…
 * ```
 * Ohne Argumente tut der Test nichts.
 */
@RunWith(AndroidJUnit4::class)
class SeedCloudAccountTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val prefs by lazy {
        context.getSharedPreferences("cloud_account", android.content.Context.MODE_PRIVATE)
    }

    @Test(timeout = 240_000)
    fun seed() {
        val args = InstrumentationRegistry.getArguments()
        val email = args.getString("email")
        val password = args.getString("password")
        assumeTrue(
            "Ohne email und password als Instrumentation-Argument wird nichts geschrieben.",
            !email.isNullOrBlank() && !password.isNullOrBlank()
        )
        assertTrue("Supabase ist nicht konfiguriert.", SupabaseConfig.configured)

        prefs.edit().clear().commit()
        val today = java.time.LocalDate.now().toEpochDay()
        val now = System.currentTimeMillis()

        val bundle = runBlocking { ContentRepository(context).load() }
        val lessonIds = bundle.lessons.take(7).map { it.id }
        val cards = bundle.chunks.take(28).mapIndexed { index, chunk ->
            chunk.id to Memory(
                stability = 1.5 + index * 0.4,
                difficulty = 4.6,
                due = (today + (index % 6) - 2) * DAY,
                reps = 1 + index % 4,
                lapses = index % 2,
                stage = 1 + index % 3,
                lastGrade = 3,
                contextCount = 1 + index % 3
            )
        }.toMap()
        val days = (1..6).map { today - it }.sorted()
        val sessions = days.mapIndexed { index, day ->
            SessionLog(
                day = day,
                minutes = 7 + index,
                speakingTasks = 4 + index % 4,
                newChunks = 2 + index % 3,
                errorCardsSeen = index % 2
            )
        }
        val secondsPerDay = days.associateWith { day -> (9 + (day % 4).toInt()) * 60 }

        val seeded = Progress(
            onboarded = true,
            contentVersion = bundle.version,
            placementLevel = 1,
            placementScore = 11,
            selfRating = 1,
            completedLessons = lessonIds.toSet(),
            chunkMemory = cards,
            canDo = bundle.canDo.map { it.id }.toSet(),
            sessions = sessions,
            gardenGrowth = 0.62f,
            gardenRounds = 9,
            lastSessionDay = today - 1,
            profile = Profile(
                name = "Tobi",
                reasonId = ProfileCatalog.reasons.first().id,
                reasonIds = setOf(ProfileCatalog.reasons.first().id),
                interests = setOf(ProfileCatalog.interests.first().id),
                dailyMinutes = 10,
                createdAt = now - 7 * DAY,
                reviewedDay = today - 1
            ),
            skills = SkillProfile(
                hoeren = SkillState(72.0, 14),
                grammatik = SkillState(58.0, 11),
                bausteine = SkillState(64.0, 9),
                sprechen = SkillState(51.0, 7),
                verknuepfen = SkillState(60.0, 6),
                measuredAt = now - 3 * DAY
            ),
            seed = 1_234_567L,
            homeIntroSeen = true,
            studySeconds = secondsPerDay.values.sum(),
            timeByDay = secondsPerDay
        )

        var local = seeded
        val imports = AtomicInteger(0)
        val device = CloudAccountRepository(
            context,
            progressProvider = { local },
            importProgress = { incoming ->
                local = incoming
                imports.incrementAndGet()
                true
            },
            autoSync = false
        )

        try {
            device.login(email!!, password!!)
            assertTrue(
                "Anmeldung fehlgeschlagen: ${describe(device)}",
                waitUntil { device.state.value.signedIn }
            )
            runBlocking { device.syncNowAndWait() }
            assertEquals(
                "Der Stand ging nicht in die Cloud: ${describe(device)}",
                SyncPhase.SYNCED,
                device.state.value.phase
            )
            assertEquals("Es hätte nichts heruntergeladen werden dürfen.", 0, imports.get())
            println("SEED_OK ${device.state.value.email} revision=${device.state.value.lastSyncAt}")
        } finally {
            if (device.state.value.signedIn) {
                device.logout()
                waitUntil { !device.state.value.signedIn }
            }
            device.close()
            prefs.edit().clear().commit()
        }
    }

    private fun describe(device: CloudAccountRepository): String {
        val state = device.state.value
        return "phase=${state.phase} signedIn=${state.signedIn} verified=${state.emailVerified} " +
            "busy=${state.busy} error=${state.error} message=${state.message}"
    }

    private fun waitUntil(timeoutMs: Long = 30_000, check: () -> Boolean): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (check()) return true
            Thread.sleep(150)
        }
        return check()
    }

    private companion object {
        const val DAY = 86_400_000L
    }
}
