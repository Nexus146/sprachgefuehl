package de.sprachgefuehl.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.sprachgefuehl.app.AppUiState
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.Screen
import de.sprachgefuehl.app.data.Backup
import de.sprachgefuehl.app.data.BackupContent
import de.sprachgefuehl.app.domain.Checkup
import de.sprachgefuehl.app.domain.Personalization
import de.sprachgefuehl.app.domain.ProfileCatalog
import de.sprachgefuehl.app.ui.components.BigButton
import de.sprachgefuehl.app.ui.components.ProgressChain
import de.sprachgefuehl.app.ui.components.SoftCard
import de.sprachgefuehl.app.ui.components.Tag
import de.sprachgefuehl.app.ui.theme.Spacing

private const val STEPS = 6
private const val STEP_WELCOME = 0
private const val STEP_NAME = 1
private const val STEP_TASTE = 2
private const val STEP_RATING = 3
private const val STEP_CHECKUP = 4
private const val STEP_RESULT = 5

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(vm: AppViewModel, state: AppUiState) {
    state.content ?: return
    val profile = state.progress.profile
    // Nach einem Absturz oder Neustart mitten in der Standortbestimmung beginnt der Erststart
    // nicht wieder beim Begrüßungstext, sondern dort, wo die Prüfung aufgehört hat.
    var step by rememberSaveable {
        mutableIntStateOf(
            when {
                state.progress.checkup.startedAt > 0L && !Checkup.isFinished(state.progress.checkup) -> STEP_CHECKUP
                Checkup.isFinished(state.progress.checkup) -> STEP_RESULT
                else -> STEP_WELCOME
            }
        )
    }
    var name by rememberSaveable { mutableStateOf(profile.name) }
    var reasonId by rememberSaveable { mutableStateOf(profile.reasonId) }
    var interests by rememberSaveable { mutableStateOf(profile.interests) }
    var minutes by rememberSaveable { mutableIntStateOf(profile.dailyMinutes) }
    var weekdays by rememberSaveable { mutableStateOf(profile.weekdays) }
    var selfRating by rememberSaveable { mutableIntStateOf(1) }
    // Auf einem neuen Handy ist die Sicherung der erste Schritt – nicht der letzte. Sonst müsste
    // sie erst die ganze Prüfung durchlaufen, bevor sie ihren Fortschritt zurückholen kann.
    val context = LocalContext.current
    var pendingBackup by remember { mutableStateOf<BackupContent?>(null) }
    var backupMessage by rememberSaveable { mutableStateOf("") }
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val content = Backup.read(context, uri)
        if (content == null) {
            backupMessage = "Diese Datei ist keine Sicherung von Sprachgefühl."
        } else {
            pendingBackup = content
        }
    }

    fun saveProfile() {
        vm.updateProfile {
            it.copy(
                name = name.trim().take(24),
                reasonId = reasonId,
                interests = interests,
                dailyMinutes = minutes,
                weekdays = weekdays.ifEmpty { it.weekdays }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.l)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(56.dp)) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Spa, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.weight(1f))
            ProgressChain(total = STEPS, current = step)
        }
        Spacer(Modifier.height(Spacing.xl))

        when (step) {
            STEP_WELCOME -> {
                Text("Sprachgefühl", style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(Spacing.s))
                Text(
                    "Englisch verstehen. Selbst sprechen. Ohne Druck.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(Spacing.l))
                SoftCard {
                    Bullet("10 Minuten am Tag reichen.")
                    Bullet("Du sprichst ab der ersten Lektion – mit Modellstimme und ruhigem Feedback.")
                    Bullet("Grammatik, Verbindungswörter und Aussprache greifen ineinander.")
                    Bullet("Keine Anmeldung, keine Werbung. Alles bleibt auf diesem Gerät.")
                }
                Spacer(Modifier.height(Spacing.m))
                Text(
                    "Du kannst nichts verlieren. Es gibt keine Herzen, keine Serien, keinen Zeitdruck.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.xl))
                BigButton("Los geht’s", { step = STEP_NAME })
                Spacer(Modifier.height(Spacing.s))
                BigButton(
                    text = "Ich habe schon eine Sicherung",
                    onClick = { backupLauncher.launch(arrayOf(Backup.MIME)) },
                    secondary = true
                )
                if (backupMessage.isNotBlank()) {
                    Spacer(Modifier.height(Spacing.s))
                    Text(
                        backupMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            STEP_NAME -> {
                Text("Wie dürfen wir dich nennen?", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(Spacing.s))
                Text(
                    "Nur für die Anrede. Du kannst das Feld leer lassen – die App duzt dich dann einfach.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.l))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(24) },
                    label = { Text("Dein Name") },
                    placeholder = { Text("z. B. Ingrid") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                if (name.isNotBlank()) {
                    Spacer(Modifier.height(Spacing.s))
                    Text(
                        "So begrüßt dich die App: „${Personalization.greeting(name, 9)}“",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(Spacing.l))
                Text("Warum möchtest du Englisch lernen?", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "Das entscheidet nur, welche Begegnungen zuerst kommen. Jede Lektion bleibt offen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.s))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    ProfileCatalog.reasons.forEach { choice ->
                        Tag(
                            text = choice.label,
                            active = reasonId == choice.id,
                            onClick = { reasonId = if (reasonId == choice.id) "" else choice.id }
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.xl))
                BigButton("Weiter", {
                    saveProfile()
                    step = STEP_TASTE
                })
                Spacer(Modifier.height(Spacing.s))
                BigButton("Später ausfüllen", {
                    step = STEP_TASTE
                }, secondary = true)
            }

            STEP_TASTE -> {
                Text("Was passt zu deinem Alltag?", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(Spacing.s))
                Text(
                    "Alles ist ein Vorschlag. Unter „Deine Angaben“ im Profil änderst du es jederzeit.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(Spacing.l))
                Text("Interessen", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(Spacing.s))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    ProfileCatalog.interests.forEach { choice ->
                        Tag(
                            text = choice.label,
                            active = choice.id in interests,
                            onClick = {
                                interests = if (choice.id in interests) interests - choice.id else interests + choice.id
                            }
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.l))
                Text("Tagesziel", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "Zehn Minuten an vier Tagen bringen mehr als eine lange Sitzung am Sonntag.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.s))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                    ProfileCatalog.goals.forEach { goal ->
                        Tag(text = "$goal Minuten", active = minutes == goal, onClick = { minutes = goal })
                    }
                }

                Spacer(Modifier.height(Spacing.l))
                Text("Deine Lerntage", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(Spacing.s))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                    ProfileCatalog.weekdayLabels.forEachIndexed { index, label ->
                        val day = index + 1
                        Tag(
                            text = label,
                            active = day in weekdays,
                            onClick = {
                                val next = if (day in weekdays) weekdays - day else weekdays + day
                                weekdays = if (next.isEmpty()) weekdays else next
                            }
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "Keine Erinnerungen, keine Serien. Ein Pausentag ist in Ordnung.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(Spacing.xl))
                BigButton("Weiter", {
                    saveProfile()
                    step = STEP_RATING
                })
                Spacer(Modifier.height(Spacing.s))
                BigButton("Überspringen", { step = STEP_RATING }, secondary = true)
            }

            STEP_RATING -> {
                Text("Wie fühlst du dich mit Englisch?", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(Spacing.s))
                Text(
                    "Es gibt keine falsche Antwort. Wir starten da, wo es sich gut anfühlt.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.l))
                RatingCard("Ich verstehe kaum Englisch.", "Wir beginnen ganz langsam.", 0, selfRating) { selfRating = it }
                RatingCard("Ich verstehe einfache Sätze.", "Wir starten mit Alltagssätzen.", 1, selfRating) { selfRating = it }
                RatingCard("Ich spreche schon etwas.", "Wir starten mit mehr Tempo.", 2, selfRating) { selfRating = it }
                Spacer(Modifier.height(Spacing.xl))
                BigButton("Weiter", { step = STEP_CHECKUP })
                Spacer(Modifier.height(Spacing.s))
                BigButton("Ohne Prüfung starten", {
                    vm.finishCheckup(selfRating)
                    step = STEP_RESULT
                }, secondary = true)
            }

            STEP_CHECKUP -> {
                if (state.progress.checkup.startedAt == 0L) {
                    LaunchedEffect(Unit) { vm.startCheckup(selfRating) }
                } else {
                    CheckupContent(vm = vm, state = state, onFinished = { step = STEP_RESULT }, onPause = { step = STEP_RATING })
                }
            }

            else -> {
                Text("Das ist dein Startpunkt", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(Spacing.s))
                Text(
                    "Nicht eine Zahl, sondern fünf Bereiche. Die App beginnt bei dem, was noch wackelt, " +
                        "und hält alles andere frisch.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.m))
                SkillCards(vm.skillViews())
                Spacer(Modifier.height(Spacing.s))
                SoftCard {
                    Text("So arbeitet die App", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(Spacing.s))
                    Text(
                        "Jede Lektion: hören, verstehen, eine Form erkennen, selbst sprechen, automatisieren. " +
                            "Bei Fehlern bekommst du eine Erklärung mit Ursache, Kontrast zum Deutschen und dem nächsten Schritt.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(Spacing.xl))
                BigButton(
                    text = "Erste Runde starten",
                    onClick = {
                        // Look the lesson up first: setPlacement flips `onboarded` and therefore
                        // rebuilds the whole shell. Afterwards the placement is saved and only
                        // the navigation is left, so one tap always ends in exactly one screen.
                        val next = vm.nextLesson()
                        saveProfile()
                        vm.setPlacement(vm.checkupLevel(), vm.checkupCorrectCount(), selfRating)
                        if (next != null) vm.navigate(Screen.Lesson(next.id))
                    }
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "Später auf ein neues Handy? Der Fortschritt lässt sich in den Einstellungen als " +
                        "Datei sichern und dort wieder laden.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(Spacing.xl))
        Text(
            "Tipp: Lege das Handy auf den Tisch und sprich in normaler Lautstärke.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    val backup = pendingBackup
    if (backup != null) {
        AlertDialog(
            onDismissRequest = { pendingBackup = null },
            title = { Text("Sicherung laden?") },
            text = { Text("Deine Angaben und dein Fortschritt kommen aus der Datei. Danach geht es direkt weiter.") },
            confirmButton = {
                TextButton(onClick = {
                    pendingBackup = null
                    val restored = vm.importProgress(backup.progressJson)
                    if (restored) {
                        Backup.restoreRecordings(context, backup)
                        vm.openTab(de.sprachgefuehl.app.ui.navigation.AppTab.START)
                    } else {
                        backupMessage = "Diese Sicherung konnte ich nicht lesen. Es bleibt alles, wie es war."
                    }
                }) { Text("Laden") }
            },
            dismissButton = {
                TextButton(onClick = { pendingBackup = null }) { Text("Abbrechen") }
            }
        )
    }
}

@Composable
private fun Bullet(text: String) {
    Row(modifier = Modifier.padding(vertical = Spacing.xs)) {
        Text("·", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.size(Spacing.s))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun RatingCard(
    title: String,
    subtitle: String,
    value: Int,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    val active = value == selected
    SoftCard(
        modifier = Modifier.padding(bottom = Spacing.s),
        onClick = { onSelect(value) }
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (active) {
            Spacer(Modifier.height(Spacing.xs))
            Text("Ausgewählt", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}
