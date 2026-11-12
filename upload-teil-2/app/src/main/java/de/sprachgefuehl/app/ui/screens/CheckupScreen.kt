package de.sprachgefuehl.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.sprachgefuehl.app.AppUiState
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.domain.Checkup
import de.sprachgefuehl.app.domain.Skill
import de.sprachgefuehl.app.ui.components.BigButton
import de.sprachgefuehl.app.ui.components.SoftCard
import de.sprachgefuehl.app.ui.speech.rememberSpeechController
import de.sprachgefuehl.app.ui.theme.Spacing

/**
 * The standort check inside onboarding. One task at a time, no time limit, no correction after
 * each answer – the point is to find the right starting place, not to teach here.
 */
@Composable
fun CheckupContent(
    vm: AppViewModel,
    state: AppUiState,
    onFinished: () -> Unit,
    onPause: (() -> Unit)? = null
) {
    val content = state.content ?: return
    val checkup = state.progress.checkup
    val speech = rememberSpeechController()
    var voiceMissing by remember { mutableStateOf(false) }

    val finished = Checkup.isFinished(checkup)
    val item = remember(checkup.answeredIds.size, checkup.blockIndex, content.checkup) { vm.checkupItem() }

    LaunchedEffect(finished) { if (finished) onFinished() }

    if (finished) return

    if (item == null) {
        LaunchedEffect(checkup.answeredIds.size) {
            vm.finishCheckup(checkup.selfRating)
            onFinished()
        }
        return
    }

    val skill = Skill.fromId(item.skill)
    val total = Checkup.order.size * Checkup.BLOCK_SIZE
    val answered = checkup.answered.coerceAtMost(total)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Standort bestimmen", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.weight(1f))
            Text(
                "${answered.coerceAtMost(total)}/$total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(Spacing.s))
        Text(
            "Kein Test, kein Bestehen. Wenn du etwas nicht weißt, tippst du einfach weiter. " +
                "Du kannst jederzeit pausieren – der Stand bleibt gespeichert.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.s))
        // Ohne eigene Spurfarbe nahm Material im Dunkeln einen Braunton, der mit 1,5:1 kaum
        // vom Grund zu unterscheiden war – bei 0/25 sah die Zeile wie ein Strich aus.
        LinearProgressIndicator(
            progress = { answered.toFloat() / total.toFloat() },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline,
            drawStopIndicator = {}
        )
        Spacer(Modifier.height(Spacing.m))
        Text(
            skill?.let { Checkup.label(it, checkup) } ?: "Teil",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        if (onPause != null) {
            Spacer(Modifier.height(Spacing.s))
            BigButton(text = "Pause", onClick = onPause, secondary = true)
            Text(
                "Dein Stand ist gespeichert. Du kannst jederzeit hier weitermachen.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(Spacing.m))
        SoftCard {
            Text(item.promptDe, style = MaterialTheme.typography.titleMedium)
            if (item.textEn.isNotBlank()) {
                Spacer(Modifier.height(Spacing.s))
                Text(
                    item.textEn,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal
                )
            }
            if (item.audioEn.isNotBlank()) {
                Spacer(Modifier.height(Spacing.m))
                BigButton(
                    text = "Anhören",
                    onClick = {
                        speech.speak(item.audioEn, 0.8f)
                        val output = speech.output
                        voiceMissing = output.isReady && output.availableEnglishVoices().isEmpty()
                    },
                    icon = Icons.Rounded.VolumeUp
                )
                Spacer(Modifier.height(Spacing.xs))
                BigButton(
                    text = "Langsamer",
                    onClick = { speech.speak(item.audioEn, 0.6f) },
                    secondary = true
                )
                if (voiceMissing) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        "Keine englische Stimme gefunden. Du kannst die Aufgabe auch lesen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.m))

        if (item.type == "speak") {
            if (item.model.isNotBlank()) {
                Text("So klingt es:", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(Spacing.xs))
                Text(item.model, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(Spacing.s))
                BigButton(
                    text = "Modell anhören",
                    onClick = { speech.speak(item.model, 0.8f) },
                    icon = Icons.Rounded.VolumeUp,
                    secondary = true
                )
            }
            Spacer(Modifier.height(Spacing.m))
            Text("Sag es einmal laut. Wie ist es dir gegangen?", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Spacing.s))
            RatingChoice("Leicht gefallen", onClick = { vm.answerCheckup(item, true) })
            RatingChoice("Mit Mühe geschafft", onClick = { vm.answerCheckup(item, true) })
            RatingChoice("Noch nicht", onClick = { vm.answerCheckup(item, false) })
        } else {
            item.options.forEachIndexed { index, option ->
                SoftCard(
                    modifier = Modifier.padding(bottom = Spacing.s),
                    onClick = { vm.answerCheckup(item, index == item.answer) }
                ) {
                    Text(option, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Spacer(Modifier.height(Spacing.m))
        Text(
            "Die Auswertung kommt erst am Ende – hier wird nicht korrigiert.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RatingChoice(text: String, onClick: () -> Unit) {
    SoftCard(
        modifier = Modifier.padding(bottom = Spacing.s),
        onClick = onClick
    ) {
        Row(horizontalArrangement = Arrangement.Start) {
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}


/**
 * The standort check outside onboarding. It reuses the same state, so pausing costs nothing and
 * the home screen can offer to continue.
 */
@Composable
fun CheckupScreen(vm: AppViewModel, state: AppUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.l)
    ) {
        CheckupContent(
            vm = vm,
            state = state,
            onFinished = { vm.back() },
            onPause = { vm.back() }
        )
        Spacer(Modifier.height(Spacing.xl))
    }
}
