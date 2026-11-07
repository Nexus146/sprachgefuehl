package de.sprachgefuehl.app.account

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.account.CloudAccountRepository
import de.sprachgefuehl.app.data.account.SupabaseConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Die Admin-Konsole gegen das echte Projekt: kurzer Benutzername als Login, Kontenliste über die
 * Serverfunktion, Konto anlegen und wieder löschen. Der Test legt nur ein eigenes Wegwerf-Konto an
 * und entfernt es am Ende; bestehende Konten werden nicht angefasst.
 *
 * ```
 * ./gradlew :app:connectedDebugAndroidTest \
 *   -Pandroid.testInstrumentationRunnerArguments.class=de.sprachgefuehl.app.account.AdminConsoleTest \
 *   -Pandroid.testInstrumentationRunnerArguments.email=admin-11 \
 *   -Pandroid.testInstrumentationRunnerArguments.password=…
 * ```
 */
@RunWith(AndroidJUnit4::class)
class AdminConsoleTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val prefs by lazy {
        context.getSharedPreferences("cloud_account", android.content.Context.MODE_PRIVATE)
    }
    private val stamp = System.currentTimeMillis()
    private val newAccount = "admin.test.$stamp@sprachgefuehl.local"
    private val renamedAccount = "admin.test2.$stamp@sprachgefuehl.local"
    private val newPassword = "Testpasswort-2026"

    @Test(timeout = 240_000)
    fun consoleListsCreatesAndDeletesAccounts() {
        val args = InstrumentationRegistry.getArguments()
        val name = args.getString("email")
        val password = args.getString("password")
        assumeTrue(!name.isNullOrBlank() && !password.isNullOrBlank())
        assertTrue(SupabaseConfig.configured)

        prefs.edit().clear().commit()
        var local = Progress()
        val device = CloudAccountRepository(
            context,
            progressProvider = { local },
            importProgress = { incoming -> local = incoming; true },
            clearLocalProgress = { local = Progress() },
            autoSync = false
        )

        try {
            // Eine Sitzung aus einem früheren Lauf gehört nicht zu diesem Test.
            Thread.sleep(2_000)
            if (device.state.value.signedIn) {
                device.logout()
                waitUntil { !device.state.value.signedIn }
            }
            // 1. Kurzname ohne "@" wird zur internen Adresse und landet in der Konsole.
            device.login(name!!, password!!)
            assertTrue(
                "Anmeldung mit dem Kurznamen fehlgeschlagen: ${describe(device)}",
                waitUntil {
                    device.state.value.signedIn &&
                        device.state.value.email == "$name@${SupabaseConfig.ACCOUNT_DOMAIN}"
                }
            )
            assertEquals(
                "Der Kurzname wurde nicht auf die interne Adresse abgebildet.",
                "$name@${SupabaseConfig.ACCOUNT_DOMAIN}",
                device.state.value.email
            )
            assertTrue(
                "Das Konto wurde nicht als Admin erkannt: ${describe(device)}",
                waitUntil { device.state.value.isAdmin }
            )

            // 2. Liste kommt über die Serverfunktion.
            device.adminRefreshUsers()
            assertTrue(
                "Die Kontenliste kam nicht an: ${describe(device)}",
                waitUntil { device.state.value.adminLoaded && device.state.value.adminUsers.isNotEmpty() }
            )
            assertTrue(
                "Das eigene Admin-Konto fehlt in der Liste.",
                device.state.value.adminUsers.any { it.email == "$name@${SupabaseConfig.ACCOUNT_DOMAIN}" }
            )
            assertTrue(
                "Ein bekanntes Lernkonto fehlt in der Liste.",
                device.state.value.adminUsers.any { it.email == "opboxnull@gmail.com" }
            )
            val seeded = device.state.value.adminUsers.first { it.email == "opboxnull@gmail.com" }
            assertTrue("Der geladene Cloud-Stand wird nicht angezeigt.", seeded.hasCloud && seeded.lessons > 0)

            // 3. Neues Konto anlegen: erscheint in der Liste und gilt als bestätigt.
            device.adminCreateUser(newAccount, newPassword)
            assertTrue(
                "Das neue Konto erschien nicht: ${describe(device)}",
                waitUntil { device.state.value.adminUsers.any { it.email == newAccount } }
            )
            val created = device.state.value.adminUsers.first { it.email == newAccount }
            assertTrue("Das neue Konto gilt nicht als bestätigt.", created.confirmed)

            // 4. Passwort neu setzen: die Serverfunktion antwortet ohne Fehler.
            device.adminSetPassword(created.id, "NeuesPasswort-2026")
            assertTrue(
                "Passwort setzen meldete einen Fehler: ${describe(device)}",
                waitUntil {
                    !device.state.value.adminBusy && device.state.value.adminError == null
                }
            )

            // 5. Einblick in das frische Konto: noch kein Cloud-Stand.
            device.adminLoadDetail(created.id)
            assertTrue(
                "Der Einblick kam nicht an: ${describe(device)}",
                waitUntil {
                    !device.state.value.adminDetailBusy &&
                        device.state.value.adminDetailFor == created.id &&
                        device.state.value.adminDetail != null
                }
            )
            val detail = device.state.value.adminDetail!!
            assertFalse("Ein neues Konto hat schon einen Cloud-Stand.", detail.hasCloud)
            assertEquals("Ein neues Konto hat schon Lektionen.", 0, detail.lessons)

            // 6. Adresse ändern: die neue steht in der Liste, die alte ist weg.
            device.adminSetEmail(created.id, renamedAccount)
            assertTrue(
                "Die Adresse wurde nicht geändert: ${describe(device)}",
                waitUntil { device.state.value.adminUsers.any { it.email == renamedAccount } }
            )
            assertTrue(
                "Die alte Adresse steht noch in der Liste.",
                device.state.value.adminUsers.none { it.email == newAccount }
            )

            // 7. Sperren: das Konto kann sich nicht mehr anmelden.
            device.adminSetBanned(created.id, true)
            assertTrue(
                "Das Konto gilt nicht als gesperrt: ${describe(device)}",
                waitUntil { device.state.value.adminUsers.any { it.id == created.id && it.banned } }
            )
            device.logout()
            assertTrue("Abmelden blieb ohne Wirkung.", waitUntil { !device.state.value.signedIn })
            runCatching { device.login(renamedAccount, "NeuesPasswort-2026") }
            Thread.sleep(4_000)
            assertFalse(
                "Ein gesperrtes Konto konnte sich anmelden.",
                device.state.value.signedIn
            )

            // 8. Entsperren: die Anmeldung klappt wieder.
            device.login(name!!, password!!)
            assertTrue(
                "Admin-Anmeldung nach dem Sperren fehlgeschlagen: ${describe(device)}",
                waitUntil {
                    device.state.value.signedIn &&
                        device.state.value.email == "$name@${SupabaseConfig.ACCOUNT_DOMAIN}"
                }
            )
            device.adminSetBanned(created.id, false)
            assertTrue(
                "Das Konto gilt weiter als gesperrt.",
                waitUntil { device.state.value.adminUsers.any { it.id == created.id && !it.banned } }
            )
            device.logout()
            waitUntil { !device.state.value.signedIn }
            device.login(renamedAccount, "NeuesPasswort-2026")
            assertTrue(
                "Ein entsperrtes Konto kann sich nicht anmelden.",
                waitUntil { device.state.value.signedIn }
            )
            device.logout()
            waitUntil { !device.state.value.signedIn }

            // 9. Zurücksetzen räumt den Lernstand, das Konto bleibt.
            device.login(name!!, password!!)
            assertTrue(
                "Admin-Anmeldung fehlgeschlagen: ${describe(device)}",
                waitUntil {
                    device.state.value.signedIn &&
                        device.state.value.email == "$name@${SupabaseConfig.ACCOUNT_DOMAIN}"
                }
            )
            device.adminResetProgress(created.id)
            assertTrue(
                "Zurücksetzen meldete einen Fehler: ${describe(device)}",
                waitUntil {
                    !device.state.value.adminBusy && device.state.value.adminError == null
                }
            )
            assertTrue(
                "Das Konto fehlt nach dem Zurücksetzen.",
                waitUntil { device.state.value.adminUsers.any { it.id == created.id } }
            )

            // 10. Wieder löschen: verschwindet aus der Liste.
            device.adminDeleteUser(created.id)
            assertTrue(
                "Das Konto ließ sich nicht löschen: ${describe(device)}",
                waitUntil { device.state.value.adminUsers.none { it.email == newAccount } }
            )

            device.logout()
            assertTrue("Abmelden blieb ohne Wirkung.", waitUntil { !device.state.value.signedIn })
            assertFalse("Nach dem Abmelden bleibt der Admin-Status stehen.", device.state.value.isAdmin)
            assertTrue("Die Kontenliste bleibt nach dem Abmelden stehen.", device.state.value.adminUsers.isEmpty())
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
        return "signedIn=${state.signedIn} isAdmin=${state.isAdmin} adminBusy=${state.adminBusy} " +
            "users=${state.adminUsers.size} notice=${state.adminNotice} error=${state.adminError}"
    }

    private fun waitUntil(timeoutMs: Long = 30_000, check: () -> Boolean): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (check()) return true
            Thread.sleep(200)
        }
        return check()
    }
}
