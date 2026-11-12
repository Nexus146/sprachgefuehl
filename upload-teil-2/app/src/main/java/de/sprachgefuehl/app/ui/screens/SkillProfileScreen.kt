package de.sprachgefuehl.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.sprachgefuehl.app.AppUiState
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.Screen
import de.sprachgefuehl.app.domain.SkillCatalog
import de.sprachgefuehl.app.domain.SkillView
import de.sprachgefuehl.app.ui.components.BigButton
import de.sprachgefuehl.app.ui.components.ScreenHeader
import de.sprachgefuehl.app.ui.components.SoftCard
import de.sprachgefuehl.app.ui.components.isLargeFont
import de.sprachgefuehl.app.ui.theme.Spacing

@Composable
fun SkillProfileScreen(vm: AppViewModel, state: AppUiState) {
    var confirm by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = "Dein Sprachprofil",
            subtitle = "Fünf Bereiche – nicht eine Zahl",
            onBack = { vm.back() }
        )
        Column(modifier = Modifier.padding(horizontal = Spacing.l)) {
            Text(
                "Sprache ist kein einzelner Wert. Wer gut versteht, muss noch nicht sicher sprechen – " +
                    "und wer viel weiß, findet im Gespräch nicht immer die Verbindung. Jeder Bereich " +
                    "wächst mit dem, was du wirklich tust.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.m))
            SkillCards(vm.skillViews())
            Spacer(Modifier.height(Spacing.m))
            Text(
                "Die Stufen sind bewusst grob: „Wir fangen an“, „Du baust auf“, „Du bist sicher“, " +
                    "„Du kannst es frei“. Ein einzelner Fehler verschiebt nichts – erst mehrere Belege " +
                    "in dieselbe Richtung.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.m))
            BigButton(
                text = "Standort neu bestimmen",
                onClick = { confirm = true },
                secondary = true
            )
            Spacer(Modifier.height(Spacing.s))
            Text(
                "Rund 30 Aufgaben, danach schätzt die App die fünf Bereiche neu. Lektionen und Sätze bleiben.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.xl))
        }
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Neu bestimmen?") },
            text = {
                Text(
                    "Der bisherige Stand der fünf Bereiche wird ersetzt. Deine Lektionen, Sätze und " +
                        "Aufnahmen bleiben."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirm = false
                    vm.startCheckup(state.progress.selfRating)
                    vm.navigate(Screen.Checkup)
                }) { Text("Starten") }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }) { Text("Später") }
            }
        )
    }
}

/** Used in the profile and at the end of the standort check, so both always say the same. */
@Composable
fun SkillCards(views: List<SkillView>) {
    views.forEach { view ->
        SoftCard(modifier = Modifier.padding(bottom = Spacing.s)) {
            // Bereich und Stufe standen nebeneinander; bei „Bausteine & Wortschatz“ und „Du
            // kannst es frei“ reichte die Zeile nicht und die Stufe schrieb sich über den Titel.
            // Jetzt steht die Stufe immer darunter – ruhiger und unabhängig von der Schriftgröße.
            Text(view.skill.label, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Spacing.xs))
            Text(
                SkillCatalog.stageLabel(view.stage),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(view.meaning, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(Spacing.xs))
            Text(
                if (view.observed) "Nächster Schritt: ${view.next}" else "Noch kein Beleg – wir schätzen vorsichtig.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
