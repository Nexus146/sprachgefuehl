package de.sprachgefuehl.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.sprachgefuehl.app.AppUiState
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.data.account.AccountUiState
import de.sprachgefuehl.app.data.account.AdminUser
import de.sprachgefuehl.app.ui.components.BigButton
import de.sprachgefuehl.app.ui.components.DividerLine
import de.sprachgefuehl.app.ui.components.SoftCard
import de.sprachgefuehl.app.ui.theme.Spacing
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class AdminFilter(val label: String) {
    ALL("Alle"),
    CLOUD("Mit Cloud"),
    BANNED("Gesperrt")
}

/**
 * The admin console. Same palette and cards as the learning app, but a different job: accounts
 * and their cloud documents. Nothing here changes the learner's lessons, and the learning app is
 * never reachable from this screen – only "Abmelden" and back to the console.
 */
@Composable
fun AdminScreen(vm: AppViewModel, state: AppUiState, account: AccountUiState) {
    val live by vm.account.collectAsStateWithLifecycle()
    val current = if (live.isAdmin) live else account
    val context = LocalContext.current

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var query by rememberSaveable { mutableStateOf("") }
    var filterIndex by rememberSaveable { mutableStateOf(0) }
    var shareText by rememberSaveable { mutableStateOf("") }

    var manageFor by remember { mutableStateOf<AdminUser?>(null) }
    var detailFor by remember { mutableStateOf<AdminUser?>(null) }
    var emailFor by remember { mutableStateOf<AdminUser?>(null) }
    var passwordFor by remember { mutableStateOf<AdminUser?>(null) }
    var banFor by remember { mutableStateOf<AdminUser?>(null) }
    var resetFor by remember { mutableStateOf<AdminUser?>(null) }
    var toDelete by remember { mutableStateOf<AdminUser?>(null) }

    var newEmail by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(current.signedIn) {
        if (current.signedIn) vm.adminRefreshUsers()
    }

    val users = current.adminUsers
    val filter = enumValues<AdminFilter>().getOrElse(filterIndex) { AdminFilter.ALL }
    val needle = query.trim().lowercase()
    val shown = users.filter { user ->
        val matches = needle.isBlank() ||
            user.email.lowercase().contains(needle) ||
            user.name.lowercase().contains(needle)
        matches && when (filter) {
            AdminFilter.ALL -> true
            AdminFilter.CLOUD -> user.hasCloud
            AdminFilter.BANNED -> user.banned
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.l)
    ) {
        Text("Admin-Konsole", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(Spacing.xs))
        Text(
            "Sprachgefühl · Konten und Cloud",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(Spacing.l))

        SoftCard {
            Text("Angemeldet", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.xs))
            Text(current.email.orEmpty(), style = MaterialTheme.typography.bodyLarge)
            Text(
                "Admin-Zugang · nur Konsole, keine Lern-App",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.s))
            BigButton("Abmelden", { vm.logoutAccount() }, secondary = true, enabled = !current.adminBusy)
        }

        Spacer(Modifier.height(Spacing.m))
        SoftCard {
            Text("Überblick", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.s))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Stat("Konten", users.size.toString())
                Stat("Mit Cloud", users.count { it.hasCloud }.toString())
                Stat("Gesperrt", users.count { it.banned }.toString())
            }
            Spacer(Modifier.height(Spacing.s))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Stat("Lernminuten", users.sumOf { it.minutes }.toString())
                Stat("Aktiv (7 Tage)", users.count { recent(it.lastSignInAt, 7) }.toString())
            }
        }

        Spacer(Modifier.height(Spacing.m))
        SoftCard {
            Text("Neues Konto", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "Anlegen ohne Mail: Das Konto gilt sofort als bestätigt und kann sich in der App " +
                    "anmelden.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.m))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-Mail") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.s))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Startpasswort (mindestens 8 Zeichen)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.m))
            BigButton(
                text = "Konto anlegen",
                onClick = {
                    val clean = email.trim()
                    shareText = "Sprachgefühl\nAdresse: $clean\nStartpasswort: $password"
                    vm.adminCreateUser(clean, password)
                    password = ""
                },
                enabled = !current.adminBusy
            )
            if (shareText.isNotBlank()) {
                Spacer(Modifier.height(Spacing.s))
                Text(
                    "Zugangsdaten bereithalten – solange keine Mails rausgehen, ist Weitergeben " +
                        "der einzige Weg.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.xs))
                BigButton(
                    text = "Zugangsdaten teilen",
                    onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Zugang zu Sprachgefühl")
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(
                            Intent.createChooser(send, "Zugangsdaten teilen")
                        )
                    },
                    secondary = true
                )
            }
        }

        Spacer(Modifier.height(Spacing.m))
        SoftCard {
            Text("Konten (${users.size})", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.s))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Suchen (Adresse oder Name)") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.xs))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                enumValues<AdminFilter>().forEachIndexed { index, option ->
                    TextButton(onClick = { filterIndex = index }) {
                        Text(
                            option.label,
                            color = if (index == filterIndex) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            if (!current.adminLoaded && current.adminBusy) {
                Text(
                    "Wird geladen …",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (users.isEmpty()) {
                Text(
                    "Noch keine Konten sichtbar. Tippe auf „Aktualisieren“.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (shown.isEmpty()) {
                Text(
                    "Kein Konto passt zu „$query“.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            shown.forEach { user ->
                Spacer(Modifier.height(Spacing.s))
                val own = user.email == current.email
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        user.email.ifBlank { user.id },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        standLine(user),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val lastSeen = day(user.lastSignInAt)
                    if (lastSeen.isNotBlank()) {
                        Text(
                            "Zuletzt angemeldet: $lastSeen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val since = day(user.updatedAt)
                    if (since.isNotBlank()) {
                        Text(
                            "Zuletzt gesichert: $since",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Die Knöpfe stehen unter der Adresse: daneben blieb für lange Adressen zu
                    // wenig Platz, sie brach mitten im Wort um.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                detailFor = user
                                vm.adminLoadDetail(user.id)
                            },
                            enabled = !current.adminBusy
                        ) {
                            Text("Ansehen")
                        }
                        if (own) {
                            Text(
                                "dieses Konto",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            TextButton(
                                onClick = { manageFor = user },
                                enabled = !current.adminBusy
                            ) {
                                Text("Verwalten")
                            }
                            TextButton(
                                onClick = { toDelete = user },
                                enabled = !current.adminBusy
                            ) {
                                Text("Löschen")
                            }
                        }
                    }
                }
                DividerLine()
            }
            Spacer(Modifier.height(Spacing.s))
            BigButton(
                text = if (current.adminBusy) "Lädt …" else "Aktualisieren",
                onClick = { vm.adminRefreshUsers() },
                enabled = !current.adminBusy,
                secondary = true
            )
        }

        current.adminNotice?.let { notice ->
            Spacer(Modifier.height(Spacing.s))
            Text(
                notice,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        current.adminError?.let { error ->
            Spacer(Modifier.height(Spacing.s))
            Text(
                error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(Spacing.m))
        Text(
            "Konten anlegen und löschen läuft über eine Serverfunktion; der Service-Schlüssel " +
                "bleibt dort und ist nie in der App.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }

    manageFor?.let { target ->
        AlertDialog(
            onDismissRequest = { manageFor = null },
            title = { Text("Konto verwalten") },
            text = {
                Column {
                    Text(
                        target.email.ifBlank { target.id },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(Spacing.s))
                    TextButton(onClick = {
                        newEmail = target.email
                        emailFor = target
                        manageFor = null
                    }) { Text("E-Mail-Adresse ändern") }
                    TextButton(onClick = {
                        newPassword = ""
                        passwordFor = target
                        manageFor = null
                    }) { Text("Passwort neu setzen") }
                    TextButton(onClick = {
                        if (target.banned) {
                            vm.adminSetBanned(target.id, false)
                            manageFor = null
                        } else {
                            banFor = target
                            manageFor = null
                        }
                    }) {
                        Text(if (target.banned) "Konto entsperren" else "Konto sperren")
                    }
                    TextButton(onClick = {
                        resetFor = target
                        manageFor = null
                    }) { Text("Fortschritt zurücksetzen") }
                }
            },
            confirmButton = {
                TextButton(onClick = { manageFor = null }) { Text("Schließen") }
            }
        )
    }

    detailFor?.let { target ->
        val detail = if (current.adminDetailFor == target.id) current.adminDetail else null
        AlertDialog(
            onDismissRequest = { detailFor = null },
            title = { Text("Konto ansehen") },
            text = {
                Column {
                    Text(
                        target.email.ifBlank { target.id },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(Spacing.s))
                    when {
                        detail == null && current.adminDetailBusy -> Text("Wird geladen …")
                        detail == null -> Text("Kein Cloud-Stand für dieses Konto.")
                        !detail.hasCloud -> Text("Noch nichts in der Cloud: kein Lernstand gesichert.")
                        else -> {
                            if (detail.name.isNotBlank()) {
                                Text("Name: ${detail.name}")
                            }
                            if (detail.dailyMinutes > 0) {
                                Text("Tagesziel: ${detail.dailyMinutes} Minuten")
                            }
                            if (detail.interests.isNotEmpty()) {
                                Text("Interessen: ${detail.interests.joinToString(", ")}")
                            }
                            Text("Lektionen: ${detail.lessons} · Minuten: ${detail.minutes}")
                            Text(
                                "Aufnahmen: ${detail.recordingCount} · " +
                                    size(detail.recordingBytes)
                            )
                            Text("Revision ${detail.revision}")
                            val saved = day(detail.updatedAt)
                            if (saved.isNotBlank()) Text("Zuletzt gesichert: $saved")
                            if (!detail.onboarded) Text("Einrichtung noch nicht abgeschlossen.")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { detailFor = null }) { Text("Schließen") }
            }
        )
    }

    emailFor?.let { target ->
        AlertDialog(
            onDismissRequest = { emailFor = null },
            title = { Text("E-Mail-Adresse ändern") },
            text = {
                Column {
                    Text("${target.email.ifBlank { target.id }} bekommt eine neue Adresse.")
                    Spacer(Modifier.height(Spacing.s))
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        singleLine = true,
                        label = { Text("Neue Adresse") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = target.id
                        emailFor = null
                        vm.adminSetEmail(id, newEmail)
                    },
                    enabled = newEmail.contains("@") && newEmail.length >= 5
                ) { Text("Speichern") }
            },
            dismissButton = {
                TextButton(onClick = { emailFor = null }) { Text("Abbrechen") }
            }
        )
    }

    passwordFor?.let { target ->
        AlertDialog(
            onDismissRequest = { passwordFor = null; newPassword = "" },
            title = { Text("Neues Passwort") },
            text = {
                Column {
                    Text(
                        "${target.email.ifBlank { target.id }} bekommt ein neues Passwort. " +
                            "Das bisherige gilt sofort nicht mehr."
                    )
                    Spacer(Modifier.height(Spacing.s))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        singleLine = true,
                        label = { Text("Neues Passwort (mindestens 8 Zeichen)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = target.id
                        passwordFor = null
                        vm.adminSetPassword(id, newPassword)
                        newPassword = ""
                    },
                    enabled = newPassword.length >= 8
                ) { Text("Setzen") }
            },
            dismissButton = {
                TextButton(onClick = { passwordFor = null; newPassword = "" }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    banFor?.let { target ->
        AlertDialog(
            onDismissRequest = { banFor = null },
            title = { Text("Konto sperren?") },
            text = {
                Text(
                    "${target.email.ifBlank { target.id }} kann sich danach nicht mehr anmelden. " +
                        "Der Lernstand bleibt erhalten, das Entsperren ist jederzeit möglich."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    banFor = null
                    vm.adminSetBanned(target.id, true)
                }) { Text("Sperren") }
            },
            dismissButton = {
                TextButton(onClick = { banFor = null }) { Text("Abbrechen") }
            }
        )
    }

    resetFor?.let { target ->
        AlertDialog(
            onDismissRequest = { resetFor = null },
            title = { Text("Fortschritt zurücksetzen?") },
            text = {
                Text(
                    "Lernstand und Aufnahmen von ${target.email.ifBlank { target.id }} werden " +
                        "gelöscht. Das Konto bleibt und kann sich weiterhin anmelden. Das lässt " +
                        "sich nicht rückgängig machen."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    resetFor = null
                    vm.adminResetProgress(target.id)
                }) { Text("Zurücksetzen") }
            },
            dismissButton = {
                TextButton(onClick = { resetFor = null }) { Text("Abbrechen") }
            }
        )
    }

    toDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Konto löschen?") },
            text = {
                Text(
                    "${pending.email.ifBlank { pending.id }} wird mit Cloud-Fortschritt und " +
                        "Aufnahmen gelöscht. Das lässt sich nicht rückgängig machen."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    toDelete = null
                    vm.adminDeleteUser(pending.id)
                }) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Abbrechen") }
            }
        )
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun standLine(user: AdminUser): String {
    val status = when {
        user.banned -> "gesperrt"
        user.confirmed -> "bestätigt"
        else -> "nicht bestätigt"
    }
    return if (user.hasCloud) {
        "$status · ${user.lessons} Lektionen · ${user.minutes} Minuten · Revision ${user.revision}"
    } else {
        "$status · noch keine Cloud-Sicherung"
    }
}

private fun size(bytes: Long): String = when {
    bytes >= 1_048_576 -> String.format(Locale.GERMAN, "%.1f MB", bytes / 1_048_576.0)
    bytes >= 1024 -> "${bytes / 1024} kB"
    else -> "$bytes B"
}

private fun recent(iso: String?, days: Long): Boolean {
    if (iso.isNullOrBlank()) return false
    val when_ = runCatching { Instant.parse(iso) }
        .recoverCatching { OffsetDateTime.parse(iso).toInstant() }
        .getOrNull() ?: return false
    return Duration.between(when_, Instant.now()).toDays() <= days
}

private fun day(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm", Locale.GERMAN)
    return runCatching {
        OffsetDateTime.parse(iso).atZoneSameInstant(ZoneId.systemDefault()).format(formatter)
    }.recoverCatching {
        Instant.parse(iso).atZone(ZoneId.systemDefault()).format(formatter)
    }.getOrDefault("")
}
