package de.sprachgefuehl.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.ui.components.DividerLine
import de.sprachgefuehl.app.ui.components.ScreenHeader
import de.sprachgefuehl.app.ui.components.SoftCard
import de.sprachgefuehl.app.ui.theme.Spacing

@Composable
fun GuideScreen(vm: AppViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = "Für Angehörige",
            subtitle = "Einrichtung und die erste gemeinsame Runde",
            onBack = { vm.back() }
        )
        Column(modifier = Modifier.padding(horizontal = Spacing.l)) {
            SoftCard {
                Text("In drei Schritten startklar", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(Spacing.m))
                Step("1", "App installieren", "Die APK-Datei auf dem Handy öffnen und „Installieren“ erlauben. Danach liegt Sprachgefühl im App-Menü.")
                Step("2", "Gemeinsam starten", "Beim ersten Öffnen fragt die App nach einer Selbsteinschätzung und spielt sechs Sätze vor. Setzt euch zehn Minuten zusammen – das nimmt die Unsicherheit.")
                Step("3", "Mikrofon erlauben", "Beim ersten Sprechen fragt Android nach dem Mikrofon. Einmal erlauben reicht. Ohne Mikrofon funktioniert alles außer dem automatischen Zuhören.")
            }
            Spacer(Modifier.height(Spacing.m))
            SoftCard {
                Text("Was die App anders macht", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.s))
                Text(
                    "Sie fragt nicht nur Vokabeln ab. Jede Lektion verbindet Hören, Verstehen, eine Grammatikform, ein Verbindungswort und eigenes Sprechen. " +
                        "Fehler werden nicht nur markiert: Du siehst, was passiert ist, warum es anders ist als im Deutschen, wie es richtig geht und was du beim nächsten Mal tust.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(Spacing.m))
            SoftCard {
                Text("Was die App nicht kann", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.s))
                Text(
                    "Sie ersetzt kein echtes Gespräch und keinen Kurs. Die Spracherkennung ist ein Übungspartner, kein Prüfer – sie hört manchmal anders als ein Mensch. " +
                        "Der Garten und die Abzeichen sollen ermutigen, nicht unter Druck setzen. Wenn etwas nicht klappt, ist die Technik schuld, nicht der Mensch.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(Spacing.m))
            SoftCard {
                Text("Gute erste Woche", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.s))
                Text(
                    "Drei Runden an drei Tagen sind besser als eine lange Sitzung. Sprecht die Sätze laut mit, auch wenn es sich komisch anfühlt. " +
                        "Fragt nach der Runde: „Welchen Satz würdest du im Café sagen?“ – nicht: „Was war die Regel?“",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(Spacing.l))
            DividerLine()
            Spacer(Modifier.height(Spacing.l))
            Text(
                "Alle Daten bleiben auf dem Gerät. Aufnahmen liegen im privaten App-Ordner und werden nicht hochgeladen.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun Step(number: String, title: String, text: String) {
    Column(modifier = Modifier.padding(vertical = Spacing.s)) {
        Text("$number · $title", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
