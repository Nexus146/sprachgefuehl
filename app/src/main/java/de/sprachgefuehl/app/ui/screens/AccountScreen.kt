package de.sprachgefuehl.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.sprachgefuehl.app.AppUiState
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.data.account.AccountUiState
import de.sprachgefuehl.app.data.account.SyncPhase
import de.sprachgefuehl.app.ui.components.BigButton
import de.sprachgefuehl.app.ui.components.DividerLine
import de.sprachgefuehl.app.ui.components.ScreenHeader
import de.sprachgefuehl.app.ui.components.SoftCard
import de.sprachgefuehl.app.ui.theme.Spacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Optional account and cloud backup. Everything here can be ignored: the learning app never
 * requires a session and the local document stays the primary copy.
 */
@Composable
fun AccountScreen(vm: AppViewModel, state: AppUiState) {
    val account by vm.account.collectAsStateWithLifecycle()
    var registerMode by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf(account.email.orEmpty()) }
    var password by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var showPasswordChange by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var confirmLogout by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = "Konto und Cloud",
            subtitle = "Optional. Ohne Konto bleibt alles auf diesem Gerät.",
            onBack = { vm.back() }
        )
        Column(modifier = Modifier.padding(horizontal = Spacing.l)) {
            if (!account.configured) {
                SetupCard()
            } else if (!account.signedIn) {
                AuthCard(
                    account = account,
                    registerMode = registerMode,
                    email = email,
                    password = password,
                    onEmail = { email = it },
                    onPassword = { password = it },
                    onMode = {
                        registerMode = !registerMode
                        password = ""
                    },
                    onRegister = { vm.registerAccount(email, password) },
                    onLogin = { vm.loginAccount(email, password) },
                    onForgot = { vm.sendPasswordReset(email) },
                    onResend = { vm.resendConfirmation() }
                )
            } else {
                SignedInCard(
                    account = account,
                    localOnly = state.progress,
                    onResend = { vm.resendConfirmation() },
                    onSync = { vm.syncCloudNow() },
                    onToggleSync = { vm.setCloudSyncEnabled(it) },
                    onConflictCloud = { vm.resolveCloudConflict(true) },
                    onConflictLocal = { vm.resolveCloudConflict(false) },
                    onLogout = { confirmLogout = true },
                    onDelete = { confirmDelete = true }
                )
            }

            if (account.emailVerified) {
                Spacer(Modifier.height(Spacing.l))
                DividerLine()
                Spacer(Modifier.height(Spacing.l))
                PasswordCard(
                    account = account,
                    show = showPasswordChange || account.passwordRecovery,
                    password = newPassword,
                    onPassword = { newPassword = it },
                    onToggle = { showPasswordChange = !showPasswordChange },
                    onSubmit = {
                        vm.updatePassword(newPassword)
                        newPassword = ""
                    }
                )
            }

            Spacer(Modifier.height(Spacing.l))
            Text(
                "Cloud-Sync ist eine Sicherung, kein Zwang. Wenn du offline bist, lernt die App " +
                    "weiter und überträgt später. Aufnahmen werden nur bei aktivem Sync übertragen.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.xl))
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Abmelden?") },
            text = {
                Text(
                    "Der Fortschritt auf diesem Gerät wird dabei entfernt. In der Cloud bleibt " +
                        "er erhalten – beim nächsten Anmelden ist er wieder da. Was noch nicht " +
                        "gesichert ist, geht ohne Verbindung verloren."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmLogout = false
                    vm.logoutAccount()
                }) { Text("Abmelden") }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) { Text("Abbrechen") }
            }
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Konto wirklich löschen?") },
            text = {
                Text(
                    "Das Konto, der Cloud-Fortschritt und die Cloud-Aufnahmen werden gelöscht. " +
                        "Der lokale Fortschritt auf diesem Gerät bleibt erhalten."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.deleteAccount()
                }) { Text("Konto löschen") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Abbrechen") }
            }
        )
    }
}

@Composable
private fun SetupCard() {
    SoftCard {
        Text("Noch nicht eingerichtet", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(Spacing.s))
        Text(
            "Die App läuft vollständig lokal. Für Konten fehlen nur zwei Werte in " +
                "local.properties: SUPABASE_URL und SUPABASE_ANON_KEY.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.s))
        Text(
            "Die Einrichtung steht Schritt für Schritt in docs/accounts.md. Ohne diese Werte " +
                "wird nichts übertragen und es gibt keine Anmeldung.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AuthCard(
    account: AccountUiState,
    registerMode: Boolean,
    email: String,
    password: String,
    onEmail: (String) -> Unit,
    onPassword: (String) -> Unit,
    onMode: () -> Unit,
    onRegister: () -> Unit,
    onLogin: () -> Unit,
    onForgot: () -> Unit,
    onResend: () -> Unit
) {
    SoftCard {
        Text(
            if (registerMode) "Konto erstellen" else "Anmelden",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            if (registerMode) {
                "Du bekommst eine Bestätigungsmail. Erst danach wird die Cloud-Sicherung frei."
            } else {
                "Mit deinem Konto kommt dein Fortschritt auf ein neues Gerät."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.m))
        OutlinedTextField(
            value = email,
            onValueChange = onEmail,
            label = { Text("E-Mail") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(Spacing.s))
        OutlinedTextField(
            value = password,
            onValueChange = onPassword,
            label = { Text("Passwort (mindestens 8 Zeichen)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(Spacing.m))
        BigButton(
            text = if (registerMode) "Konto erstellen" else "Anmelden",
            onClick = if (registerMode) onRegister else onLogin,
            enabled = !account.busy
        )
        Spacer(Modifier.height(Spacing.s))
        // Nebeneinander wurden die beiden Beschriftungen bei großer Schrift zu einem
        // Buchstabenstapel gequetscht. Untereinander bleibt jede Zeile lesbar.
        Column(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onMode, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (registerMode) "Ich habe schon ein Konto" else "Neu hier? Konto erstellen",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
            if (!registerMode) {
                TextButton(onClick = onForgot, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Passwort vergessen",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                }
                TextButton(onClick = onResend, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Bestätigungsmail erneut senden",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
        if (account.busy) {
            Spacer(Modifier.height(Spacing.s))
            Text(
                "Einen Moment …",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        // Nach dem Abmelden bleibt der lokale Fortschritt – er gehört aber weiterhin zu dem
        // Konto, aus dem er kam. Das gehört hierhin, sonst wirkt die Abmeldung wirkungslos.
        account.localOwner?.let { owner ->
            Spacer(Modifier.height(Spacing.s))
            Text(
                "Auf diesem Gerät liegt der Fortschritt von $owner. Melde dich mit diesem Konto " +
                    "an, um ihn wieder abzugleichen.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Feedback(account)
}

@Composable
private fun SignedInCard(
    account: AccountUiState,
    localOnly: de.sprachgefuehl.app.data.Progress,
    onResend: () -> Unit,
    onSync: () -> Unit,
    onToggleSync: (Boolean) -> Unit,
    onConflictCloud: () -> Unit,
    onConflictLocal: () -> Unit,
    onLogout: () -> Unit,
    onDelete: () -> Unit
) {
    SoftCard {
        Text("Angemeldet", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(Spacing.xs))
        Text(
            account.email.orEmpty(),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            if (account.emailVerified) "E-Mail bestätigt" else "E-Mail noch nicht bestätigt",
            style = MaterialTheme.typography.bodySmall,
            color = if (account.emailVerified) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
        )

        if (!account.emailVerified) {
            Spacer(Modifier.height(Spacing.m))
            Text(
                "Öffne den Link in der Bestätigungsmail. Ohne Bestätigung bleibt die Cloud gesperrt.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.s))
            BigButton("Bestätigungsmail erneut senden", onResend, secondary = true)
        } else {
            Spacer(Modifier.height(Spacing.m))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Automatisch sichern", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Nach Änderungen und beim Öffnen der App.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = account.syncEnabled, onCheckedChange = onToggleSync)
            }
            Spacer(Modifier.height(Spacing.s))
            Text(
                syncLine(account),
                style = MaterialTheme.typography.bodyMedium,
                color = when (account.phase) {
                    SyncPhase.ERROR, SyncPhase.OFFLINE -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            Spacer(Modifier.height(Spacing.s))
            BigButton(
                text = if (account.phase == SyncPhase.SYNCING) "Synchronisiert …" else "Jetzt synchronisieren",
                onClick = onSync,
                enabled = !account.busy && account.syncEnabled,
                secondary = true
            )
            if (account.phase == SyncPhase.CONFLICT) {
                Spacer(Modifier.height(Spacing.s))
                SoftCard {
                    Text("Zwei Stände, eine Entscheidung", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Auf diesem Gerät und in der Cloud wurde geändert. Wähle bewusst, " +
                            "welcher Stand bleiben soll. Nichts wird automatisch überschrieben.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(Spacing.s))
                    BigButton("Cloud-Stand laden", onConflictCloud, enabled = !account.busy)
                    Spacer(Modifier.height(Spacing.s))
                    BigButton(
                        "Dieses Gerät hochladen",
                        onConflictLocal,
                        enabled = !account.busy,
                        secondary = true
                    )
                }
            }
            Spacer(Modifier.height(Spacing.s))
            Text(
                "Lokal: ${localOnly.completedLessons.size} Lektionen · " +
                    "${localOnly.recordings.size} Aufnahmen",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(Spacing.m))
        Feedback(account)
        if (!account.emailVerified) {
            Spacer(Modifier.height(Spacing.m))
        }
        BigButton("Abmelden", onLogout, secondary = true)
        Spacer(Modifier.height(Spacing.s))
        BigButton("Konto löschen", onDelete, secondary = true)
    }
}

@Composable
private fun PasswordCard(
    account: AccountUiState,
    show: Boolean,
    password: String,
    onPassword: (String) -> Unit,
    onToggle: () -> Unit,
    onSubmit: () -> Unit
) {
    SoftCard {
        Text(
            if (account.passwordRecovery) "Neues Passwort setzen" else "Passwort ändern",
            style = MaterialTheme.typography.titleMedium
        )
        if (!show) {
            Spacer(Modifier.height(Spacing.s))
            BigButton("Passwort ändern", onToggle, secondary = true)
            return@SoftCard
        }
        Spacer(Modifier.height(Spacing.s))
        if (account.passwordRecovery) {
            Text(
                "Der Link aus der Mail ist angekommen. Vergib jetzt ein neues Passwort.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.s))
        }
        OutlinedTextField(
            value = password,
            onValueChange = onPassword,
            label = { Text("Neues Passwort") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(Spacing.s))
        BigButton("Passwort speichern", onSubmit, enabled = !account.busy)
        if (!account.passwordRecovery) {
            Spacer(Modifier.height(Spacing.s))
            TextButton(onClick = onToggle) { Text("Abbrechen") }
        }
    }
}

@Composable
private fun Feedback(account: AccountUiState) {
    val error = account.error
    val message = account.message
    if (error != null) {
        Spacer(Modifier.height(Spacing.s))
        Text(
            error,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
    if (message != null) {
        Spacer(Modifier.height(Spacing.s))
        Text(
            message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
    if (account.phase == SyncPhase.OFFLINE && error == null) {
        Spacer(Modifier.height(Spacing.s))
        Text(
            "Keine Verbindung. Der lokale Fortschritt bleibt sicher.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}

private fun syncLine(account: AccountUiState): String {
    val last = account.lastSyncAt?.let {
        Instant.ofEpochMilli(it)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm", Locale.GERMAN))
    } ?: "noch nicht"
    return when (account.phase) {
        SyncPhase.SYNCING -> "Wird synchronisiert …"
        SyncPhase.SYNCED -> "Zuletzt gesichert: $last"
        SyncPhase.CONFLICT -> "Bitte Konflikt auflösen"
        SyncPhase.OFFLINE -> "Offline · zuletzt gesichert: $last"
        SyncPhase.ERROR -> "Synchronisierung braucht Aufmerksamkeit"
        SyncPhase.READY -> if (account.lastSyncAt == null) "Bereit für die erste Sicherung" else "Zuletzt gesichert: $last"
        SyncPhase.LOCAL_ONLY -> "Nur lokal"
    }
}
