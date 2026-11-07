package de.sprachgefuehl.app.account

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.account.CloudAccountRepository
import de.sprachgefuehl.app.data.account.SupabaseConfig
import de.sprachgefuehl.app.data.account.SyncPhase
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Abmelden heißt: Das Gerät ist wieder frei. Die Sitzung ist weg, der lokale Lernstand auch –
 * und beim nächsten Anmelden kommt er aus der Cloud zurück. Geprüft gegen das echte Projekt, weil
 * Sitzung und Dokument im Gerätespeicher liegen und nicht in der App.
 *
 * Die Zugangsdaten stehen nicht im Code:
 * ```
 * ./gradlew :app:connectedDebugAndroidTest \
 *   -Pandroid.testInstrumentationRunnerArguments.class=de.sprachgefuehl.app.account.SessionLogoutTest \
 *   -Pandroid.testInstrumentationRunnerArguments.email=… \
 *   -Pandroid.testInstrumentationRunnerArguments.password=…
 * ```
 * Der Test liest nur; er schreibt nichts in die Cloud (lokal gilt der Stand nach dem Laden als
 * unverändert, deshalb gibt es beim Abmelden nichts hochzuladen).
 */
@RunWith(AndroidJUnit4::class)
class SessionLogoutTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val prefs by lazy {
        context.getSharedPreferences("cloud_account", android.content.Context.MODE_PRIVATE)
    }

    @Test(timeout = 240_000)
    fun logoutClearsTheDeviceAndLoginRestoresIt() {
        val args = InstrumentationRegistry.getArguments()
        val email = args.getString("email")
        val password = args.getString("password")
        assumeTrue(!email.isNullOrBlank() && !password.isNullOrBlank())
        assertTrue(SupabaseConfig.configured)

        prefs.edit().clear().commit()

        var local = Progress()
        var imports = 0
        var cleared = 0
        val device = CloudAccountRepository(
            context,
            progressProvider = { local },
            importProgress = { incoming -> local = incoming; imports++; true },
            clearLocalProgress = { local = Progress(); cleared++ },
            autoSync = false
        )

        try {
            // 1. Anmelden lädt den Cloud-Stand; der lokale Stand gehört ab jetzt zu diesem Konto.
            device.login(email!!, password!!)
            assertTrue("Anmeldung fehlgeschlagen: ${describe(device)}", waitUntil { device.state.value.signedIn })
            runBlocking { device.syncNowAndWait() }
            assertEquals("Konto hat nicht geladen: ${describe(device)}", SyncPhase.SYNCED, device.state.value.phase)
            assertEquals(1, imports)
            assertTrue("Der Cloud-Stand ist leer – Test nicht aussagekräftig.", local.onboarded)
            assertEquals("Die Herkunft des Standes fehlt.", email, device.state.value.localOwner)

            // 2. Abmelden: Sitzung weg, lokaler Fortschritt weg, nichts bleibt zurück.
            device.logout()
            assertTrue("Abmelden blieb ohne Wirkung: ${describe(device)}", waitUntil { !device.state.value.signedIn })
            assertTrue("Der lokale Fortschritt wurde nicht entfernt.", waitUntil { cleared == 1 })
            assertFalse("Der lokale Fortschritt steht noch.", local.onboarded)
            assertTrue("Es wurde kein Fortschritt entfernt.", local.completedLessons.isEmpty())
            assertNull("Die Adresse steht nach dem Abmelden noch im Zustand.", device.state.value.email)
            assertNull("Die Herkunft des Standes steht noch im Zustand.", device.state.value.localOwner)
            val storedKeys = prefs.all.keys.filter { it.startsWith("revision_") || it.startsWith("hash_") || it.startsWith("owner_") }
            assertTrue("Im Gerätespeicher blieb Sync-Zustand zurück: $storedKeys", storedKeys.isEmpty())
            device.close()

            // 3. Ein neues Repository auf demselben Gerät ist der nächste App-Start: abgemeldet.
            val afterRestart = CloudAccountRepository(
                context,
                progressProvider = { local },
                importProgress = { incoming -> local = incoming; imports++; true },
                clearLocalProgress = { local = Progress(); cleared++ },
                autoSync = false
            )
            try {
                Thread.sleep(3_000)
                assertFalse(
                    "Nach dem Abmelden ist beim nächsten Start noch eine Sitzung da: ${describe(afterRestart)}",
                    afterRestart.state.value.signedIn
                )
                val external = createSupabaseClient(SupabaseConfig.url, SupabaseConfig.anonKey) {
                    install(Auth)
                }
                runCatching { runBlocking { external.auth.awaitInitialization() } }
                val stored = runCatching { runBlocking { external.auth.currentSessionOrNull() } }.getOrNull()
                assertNull("Im Gerätespeicher liegt noch eine Supabase-Sitzung.", stored)

                // 4. Wieder anmelden holt den Stand zurück – als Download, ohne Konfliktfrage.
                afterRestart.login(email, password)
                assertTrue("Erneute Anmeldung fehlgeschlagen: ${describe(afterRestart)}", waitUntil { afterRestart.state.value.signedIn })
                runBlocking { afterRestart.syncNowAndWait() }
                assertEquals(
                    "Der Stand kam nicht zurück: ${describe(afterRestart)}",
                    SyncPhase.SYNCED,
                    afterRestart.state.value.phase
                )
                assertEquals("Es wurde nicht nachgeladen.", 2, imports)
                assertTrue("Der geladene Stand ist leer.", local.onboarded)

                afterRestart.logout()
                assertTrue("Zweites Abmelden blieb ohne Wirkung: ${describe(afterRestart)}", waitUntil { !afterRestart.state.value.signedIn })
            } finally {
                afterRestart.close()
            }
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
}
