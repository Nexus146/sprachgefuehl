package de.sprachgefuehl.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.sprachgefuehl.app.AppUiState
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.domain.SessionBuilder
import de.sprachgefuehl.app.ui.components.DividerLine
import de.sprachgefuehl.app.ui.components.ErrorCardContent
import de.sprachgefuehl.app.ui.components.ScreenHeader
import de.sprachgefuehl.app.ui.components.SoftCard
import de.sprachgefuehl.app.ui.speech.rememberSpeechController
import de.sprachgefuehl.app.ui.theme.Spacing

@Composable
fun WorkshopScreen(vm: AppViewModel, state: AppUiState) {
    val content = state.content ?: return
    val progress = state.progress
    val speech = rememberSpeechController()
    val recurring = SessionBuilder.remediation(content, progress, limit = 5)
    var expanded by remember { mutableStateOf(recurring.firstOrNull()?.id ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = "Fehler-Werkstatt",
            subtitle = "Fehler sind der Lernstoff",
            onBack = { vm.back() }
        )
        Column(modifier = Modifier.padding(horizontal = Spacing.l)) {
            if (recurring.isEmpty()) {
                SoftCard {
                    Text("Noch nichts gesammelt", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(Spacing.s))
                    Text(
                        "Sobald ein Fehler auftaucht, steht hier die Erklärung: was passiert ist und was du beim nächsten Mal tust.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Text("Diese Fehler kommen wieder", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(Spacing.s))
                recurring.forEach { card ->
                    val open = expanded == card.id
                    val count = progress.errorCounts[card.id] ?: 0
                    SoftCard(
                        modifier = Modifier.padding(bottom = Spacing.s),
                        onClick = { expanded = if (open) "" else card.id }
                    ) {
                        Row {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(card.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${card.type} · $count×",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                if (open) "schließen" else "erklären",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (open) {
                            Spacer(Modifier.height(Spacing.s))
                            ErrorCardContent(card) { speech.speak(card.correctEn) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.l))
            DividerLine()
            Spacer(Modifier.height(Spacing.l))
            Text("Alle Erklärungen", style = MaterialTheme.typography.titleLarge)
            Text(
                "Nachschlagen, wenn du willst – ohne Reihenfolge, ohne Druck.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.s))

            content.errorCards.groupBy { it.type }.forEach { (type, cards) ->
                Text(
                    type,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = Spacing.m, bottom = Spacing.xs)
                )
                cards.forEach { card ->
                    val open = expanded == card.id
                    SoftCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Spacing.s),
                        onClick = { expanded = if (open) "" else card.id }
                    ) {
                        Text(card.title, style = MaterialTheme.typography.bodyLarge)
                        if (open) {
                            Spacer(Modifier.height(Spacing.s))
                            ErrorCardContent(card) { speech.speak(card.correctEn) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(Spacing.xl))
        }
    }
}
