package de.sprachgefuehl.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.sprachgefuehl.app.AppUiState
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.BuildConfig
import de.sprachgefuehl.app.Screen
import de.sprachgefuehl.app.data.Backup
import de.sprachgefuehl.app.data.BackupContent
import de.sprachgefuehl.app.data.account.SyncPhase
import de.sprachgefuehl.app.domain.LearningOverview
import de.sprachgefuehl.app.domain.LessonMixer
import de.sprachgefuehl.app.domain.SkillCatalog
import de.sprachgefuehl.app.domain.TaskFormats
import de.sprachgefuehl.app.ui.components.BigButton
import de.sprachgefuehl.app.ui.components.DividerLine
import de.sprachgefuehl.app.ui.components.ScreenHeader
import de.sprachgefuehl.app.ui.components.SoftCard
import de.sprachgefuehl.app.ui.components.StatRow
import de.sprachgefuehl.app.ui.speech.rememberSpeechController
import de.sprachgefuehl.app.ui.theme.Spacing

/**
 * Deliberately small: everything that describes the learner lives in the profile. What is left
 * here changes how the app looks and sounds, or touches the saved data.
 */
@Composable
fun SettingsScreen(vm: AppViewModel, state: AppUiState) {
    val account by vm.account.collectAsStateWithLifecycle()
    val settings = state.progress.settings
    val overview = state.content?.let { content ->
        LearningOverview.of(
            content,
            state.progress,
            vm.repository?.now() ?: System.currentTimeMillis(),
            vm.today()
        )
    }
    val speech = rememberSpeechController()
    val context = LocalContext.current
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    var poolOpen by rememberSaveable { mutableStateOf(false) }
    var skillsOpen by rememberSaveable { mutableStateOf(false) }
    var status by rememberSaveable { mutableStateOf("") }
    // The picked file is only read into memory here; nothing changes until the learner confirms.
    var pendingImport by remember { mutableStateOf<BackupContent?>(null) }
    var confirmImport by rememberSaveable { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(Backup.MIME)) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        status = if (Backup.write(context, uri, vm.exportProgress())) {
            "Sicherung gespeichert. Für ein neues Handy kopierst du diese Datei einfach dorthin."
        } else {
            "Die Sicherung hat nicht geklappt. Versuche einen anderen Speicherort."
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val content = Backup.read(context, uri)
        if (content == null) {
            status = "Diese Datei ist keine Sicherung von Sprachgefühl. Es bleibt alles, wie es war."
        } else {
            pendingImport = content
            confirmImport = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = "Einstellungen",
            subtitle = "Darstellung, Stimme und deine Daten",
            onBack = { vm.back() }
        )
        Column(modifier = Modifier.padding(horizontal = Spacing.l)) {

            SoftCard(onClick = { vm.navigate(Screen.Account) }) {
                Text("Konto und Cloud", style = MaterialTheme.typography.titleMedium)
                Text(
                    when {
                        !account.configured -> "Noch nicht eingerichtet · die App bleibt vollständig lokal"
                        !account.signedIn -> account.localOwner?.let {
                            "Nicht angemeldet · Fortschritt von $it bleibt auf diesem Gerät"
                        } ?: "Nicht angemeldet · optional für Cloud-Sicherung"
                        !account.emailVerified -> "E-Mail noch nicht bestätigt"
                        account.phase == SyncPhase.SYNCED -> "Gesichert als ${account.email}"
                        else -> "Angemeldet als ${account.email}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "Öffnen",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(Spacing.m))

            Text("Darstellung", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.s))
            Text(
                "Hell ist am Tag gut lesbar, Dunkel ist abends angenehmer.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.s))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                ChoiceButton("Wie Handy", settings.themeMode == "system", Modifier.weight(1f)) {
                    vm.updateSettings { it.copy(themeMode = "system") }
                }
                ChoiceButton("Hell", settings.themeMode == "light", Modifier.weight(1f)) {
                    vm.updateSettings { it.copy(themeMode = "light") }
                }
                ChoiceButton("Dunkel", settings.themeMode == "dark", Modifier.weight(1f)) {
                    vm.updateSettings { it.copy(themeMode = "dark") }
                }
            }

            Spacer(Modifier.height(Spacing.l))
            Text("Schriftgröße", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Spacing.s))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                ChoiceButton("Normal", settings.fontScale < 1.15f, Modifier.weight(1f)) {
                    vm.updateSettings { it.copy(fontScale = 1.0f) }
                }
                ChoiceButton("Groß", settings.fontScale in 1.15f..1.45f, Modifier.weight(1f)) {
                    vm.updateSettings { it.copy(fontScale = 1.3f) }
                }
                ChoiceButton("Sehr groß", settings.fontScale > 1.45f, Modifier.weight(1f)) {
                    vm.updateSettings { it.copy(fontScale = 1.55f) }
                }
            }
            Spacer(Modifier.height(Spacing.s))
            SoftCard {
                Text("So sieht es aus", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Could I have a coffee, please?", style = MaterialTheme.typography.bodyLarge)
                Text("Könnte ich bitte einen Kaffee bekommen?", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(Spacing.m))
            SettingRow(
                title = "Ruhige Darstellung",
                subtitle = "Weniger Bewegung, kürzere Übergänge.",
                checked = settings.reducedMotion
            ) { value -> vm.updateSettings { it.copy(reducedMotion = value) } }

            SettingRow(
                title = "Leise Töne",
                subtitle = "Ein kurzer Klang, wenn etwas geklappt hat.",
                checked = settings.soundEnabled
            ) { value -> vm.updateSettings { it.copy(soundEnabled = value) } }

            Spacer(Modifier.height(Spacing.l))
            DividerLine()
            Spacer(Modifier.height(Spacing.l))

            Text("Stimme", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.s))
            Text(
                "Sprechtempo für Modellsätze und Erklärungen.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.s))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                ChoiceButton("Langsam", settings.speechRate < 0.8f, Modifier.weight(1f)) {
                    vm.updateSettings { it.copy(speechRate = 0.7f) }
                }
                ChoiceButton("Normal", settings.speechRate in 0.8f..0.95f, Modifier.weight(1f)) {
                    vm.updateSettings { it.copy(speechRate = 0.85f) }
                }
                ChoiceButton("Zügig", settings.speechRate > 0.95f, Modifier.weight(1f)) {
                    vm.updateSettings { it.copy(speechRate = 1.0f) }
                }
            }
            Spacer(Modifier.height(Spacing.s))
            BigButton(
                text = "Stimme testen",
                onClick = { speech.speak("Could I have a coffee, please?", settings.speechRate) },
                secondary = true
            )

            Spacer(Modifier.height(Spacing.m))
            SettingRow(
                title = "Fehler sofort erklären",
                subtitle = if (settings.explainImmediately) {
                    "An: Die Erklärung kommt direkt nach der Aufgabe."
                } else {
                    "Aus: Erklärungen sammeln sich am Ende der Lektion."
                },
                checked = settings.explainImmediately
            ) { value -> vm.updateSettings { it.copy(explainImmediately = value) } }

            Spacer(Modifier.height(Spacing.l))
            DividerLine()
            Spacer(Modifier.height(Spacing.l))

            Text("Deine Daten", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.s))
            Text(
                "Alles liegt auf diesem Gerät. Ein Konto ist optional und überträgt nur, was du " +
                    "freigibst. Für ein neues Handy kannst du weiterhin eine Sicherungsdatei erstellen.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.m))
            BigButton(
                text = "Sicherung erstellen",
                onClick = { exportLauncher.launch(Backup.FILE_NAME) },
                icon = Icons.Rounded.FileUpload,
                secondary = true
            )
            Spacer(Modifier.height(Spacing.s))
            BigButton(
                text = "Sicherung laden",
                onClick = { importLauncher.launch(arrayOf("*/*")) },
                icon = Icons.Rounded.FileDownload,
                secondary = true
            )
            if (status.isNotBlank()) {
                Spacer(Modifier.height(Spacing.s))
                Text(status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "Die Datei enthält Fortschritt, Sätze und Aufnahmen. Sie ist nicht verschlüsselt – " +
                    "bewahre sie an einem Ort auf, den du wiederfindest.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(Spacing.m))
            BigButton(
                text = "Fortschritt zurücksetzen",
                onClick = { confirmReset = true },
                secondary = true
            )

            Spacer(Modifier.height(Spacing.l))
            DividerLine()
            Spacer(Modifier.height(Spacing.l))

            Text("Dein Lernweg", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.s))
            Text(
                "Nur zum Nachlesen. Zugeklappt, damit der Stand ruhig bleibt.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.s))
            if (overview != null) {
                CollapsibleInfo(
                    title = "Dein Aufgabenpool",
                    summary = "${overview.pool.chunks} Bausteine · ${overview.pool.lessons} Lektionen · ${overview.pool.formats} Formate",
                    expanded = poolOpen,
                    onToggle = { poolOpen = !poolOpen }
                ) {
                    Text(
                        "${overview.pool.patterns} Muster · ${LessonMixer.MIN_TASKS}–${LessonMixer.MAX_TASKS} Aufgaben pro Runde",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(Spacing.s))
                    StatRow(
                        listOf(
                            "${overview.pool.collected}" to "in deiner Sammlung",
                            "${overview.pool.stable}" to "sicher",
                            "${overview.pool.due}" to "fällig"
                        )
                    )
                    if (overview.pool.learning > 0) {
                        Spacer(Modifier.height(Spacing.xs))
                        Text(
                            "${overview.pool.learning} Bausteine sind noch im Aufbau.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(Spacing.s))
                    Text(
                        TaskFormats.all.joinToString(" · ") { it.label },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (overview.focus.isNotEmpty()) {
                        Spacer(Modifier.height(Spacing.xs))
                        Text(
                            "Übt besonders: " + overview.focus.joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        "Jede Runde wird neu gemischt: Deine Startantworten gewichten, Fehler kommen zurück, sichere Sätze werden abgefragt.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(Spacing.s))
                CollapsibleInfo(
                    title = "Dein Können",
                    summary = "Fünf Bereiche – Stufen und Bewegung",
                    expanded = skillsOpen,
                    onToggle = { skillsOpen = !skillsOpen }
                ) {
                    overview.skills.forEach { view ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                view.skill.tab,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                SkillCatalog.stageLabel(view.stage),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        val movement = overview.movements.firstOrNull { it.skill == view.skill }
                        Text(
                            movement?.sentence() ?: view.meaning,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(Spacing.xs))
                    }
                    Text(
                        "Die Stufen wachsen mit dem, was du wirklich tust – nicht mit einer einzelnen Aufgabe.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(Spacing.m))
                    BigButton(
                        "Sprachprofil im Detail",
                        { vm.navigate(Screen.SkillProfile) },
                        secondary = true
                    )
                }
            }

            Spacer(Modifier.height(Spacing.l))
            DividerLine()
            Spacer(Modifier.height(Spacing.l))
            Text("Über", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.s))
            Text(
                "Sprachgefühl ${BuildConfig.VERSION_NAME} · offline nutzbar · Konto optional · keine Werbung",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.s))
            BigButton(
                text = "So richtest du die App ein",
                onClick = { vm.navigate(Screen.Guide) },
                secondary = true
            )
            Spacer(Modifier.height(Spacing.xl))
        }
    }

    if (confirmImport) {
        AlertDialog(
            onDismissRequest = {
                confirmImport = false
                pendingImport = null
            },
            title = { Text("Sicherung laden?") },
            text = { Text("Der Fortschritt auf diesem Gerät wird durch die Sicherung ersetzt.") },
            confirmButton = {
                TextButton(onClick = {
                    val content = pendingImport
                    confirmImport = false
                    pendingImport = null
                    if (content != null) {
                        status = if (vm.importProgress(content.progressJson)) {
                            when (val count = Backup.restoreRecordings(context, content)) {
                                0 -> "Sicherung geladen. Dein Fortschritt ist wieder da."
                                1 -> "Sicherung geladen. Dein Fortschritt und eine Aufnahme sind wieder da."
                                else -> "Sicherung geladen. Dein Fortschritt und $count Aufnahmen sind wieder da."
                            }
                        } else {
                            "Diese Sicherung konnte ich nicht lesen. Es bleibt alles, wie es war."
                        }
                    }
                }) { Text("Laden") }
            },
            dismissButton = {
                TextButton(onClick = {
                    confirmImport = false
                    pendingImport = null
                }) { Text("Abbrechen") }
            }
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Wirklich zurücksetzen?") },
            text = { Text("Fortschritt, Sätze und Aufnahmen werden gelöscht. Das lässt sich nicht rückgängig machen.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    vm.resetProgress()
                }) { Text("Zurücksetzen") }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("Abbrechen") }
            }
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun CollapsibleInfo(
    title: String,
    summary: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    SoftCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onToggle)
                .padding(vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = if (expanded) "Zuklappen" else "Aufklappen",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        if (expanded) {
            Spacer(Modifier.height(Spacing.s))
            content()
        }
    }
}

@Composable
private fun ChoiceButton(text: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    androidx.compose.material3.Surface(
        onClick = onClick,
        shape = shape,
        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        modifier = modifier.height(64.dp)
    ) {
        // Bei „Sehr groß“ braucht „Wie Handy“ zwei Zeilen. Ohne Polster klebte der Text dann
        // am Rand der Auswahl, als wäre er abgeschnitten.
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Spacing.s)
        ) {
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
