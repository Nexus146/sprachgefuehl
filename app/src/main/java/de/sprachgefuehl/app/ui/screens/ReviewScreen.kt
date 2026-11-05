package de.sprachgefuehl.app.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.sprachgefuehl.app.AppUiState
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.domain.Garden
import de.sprachgefuehl.app.domain.Scorer
import de.sprachgefuehl.app.domain.Skill
import de.sprachgefuehl.app.domain.Srs
import de.sprachgefuehl.app.domain.SessionBuilder
import de.sprachgefuehl.app.speech.SpeechAttempt
import de.sprachgefuehl.app.ui.components.BigButton
import de.sprachgefuehl.app.ui.components.FeedbackPanel
import de.sprachgefuehl.app.ui.components.FeedbackTone
import de.sprachgefuehl.app.ui.components.GardenPlant
import de.sprachgefuehl.app.ui.components.HintLine
import de.sprachgefuehl.app.ui.components.ScreenHeader
import de.sprachgefuehl.app.ui.components.SoftCard
import de.sprachgefuehl.app.ui.components.Waveform
import de.sprachgefuehl.app.ui.speech.rememberSpeechController
import de.sprachgefuehl.app.ui.theme.Spacing

@Composable
fun ReviewScreen(vm: AppViewModel, state: AppUiState) {
    val content = state.content ?: return
    val speech = rememberSpeechController()
    val now = vm.repository?.now() ?: System.currentTimeMillis()
    val dueTotal = state.progress.chunkMemory.count { Srs.isDue(it.value, now) }
    var round by remember { mutableIntStateOf(0) }
    val queue = remember(round) {
        SessionBuilder.reviewQueue(content, state.progress, now, limit = 8)
    }
    var index by remember { mutableIntStateOf(0) }
    var done by remember { mutableStateOf(false) }
    var recorded by remember { mutableStateOf(false) }
    var speakingCount by remember { mutableIntStateOf(0) }
    var grade by remember { mutableIntStateOf(0) }
    // Was beim Satz noch fehlte. Damit sagt die Rückmeldung mehr als nur „fast“.
    var missing by remember { mutableStateOf<List<String>>(emptyList()) }
    var heard by remember { mutableStateOf("") }
    var revealed by remember { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }
    var partial by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var typed by remember { mutableStateOf("") }
    val chunk = queue.getOrNull(index)?.let { id -> content.chunks.firstOrNull { it.id == id } }

    fun finishAnswer(
        g: Int,
        transcript: String,
        usedHint: Boolean,
        spoken: Boolean = true,
        missingWords: List<String> = emptyList()
    ) {
        grade = g
        missing = missingWords
        heard = transcript
        if (spoken) speakingCount += 1
        val skills = if (spoken) listOf(Skill.BAUSTEINE, Skill.SPRECHEN) else listOf(Skill.BAUSTEINE, Skill.GRAMMATIK)
        chunk?.let { vm.recordSpeaking(it.id, g, usedHint, skills) }
    }

    /** Leaving counts the round as well: three spoken sentences stay three spoken sentences. */
    fun leave() {
        if (!recorded && speakingCount > 0) {
            vm.recordReviewSession(minutes = 5, speakingTasks = speakingCount)
            recorded = true
        }
        vm.back()
    }

    BackHandler { leave() }

    fun next() {
        if (index >= queue.lastIndex) {
            vm.recordReviewSession(minutes = 5, speakingTasks = speakingCount)
            recorded = true
            done = true
        } else {
            index += 1
            grade = 0
            missing = emptyList()
            heard = ""
            revealed = false
            partial = ""
            message = ""
            typed = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = "Wiederholen",
            subtitle = when {
                queue.isEmpty() -> "Nichts fällig"
                dueTotal > queue.size -> "Satz ${index + 1} von ${queue.size} · $dueTotal warten"
                else -> "Satz ${index + 1} von ${queue.size}"
            },
            onBack = { leave() }
        )
        Column(modifier = Modifier.padding(horizontal = Spacing.l)) {
            when {
                queue.isEmpty() -> {
                    Spacer(Modifier.height(Spacing.xl))
                    SoftCard {
                        Text("Gerade ist nichts fällig", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(Spacing.s))
                        Text(
                            "Für heute ist nichts fällig. Eine neue Lektion bringt die Sätze im Zusammenhang wieder.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.height(Spacing.m))
                    BigButton("Zurück", { leave() })
                }

                done -> {
                    Spacer(Modifier.height(Spacing.l))
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        GardenPlant(
                            stage = Garden.stage(state.progress),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                        Spacer(Modifier.height(Spacing.m))
                        Text("Wiederholung geschafft", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(Spacing.s))
                        Text(
                            if (speakingCount > 0) {
                                "Du hast $speakingCount Sätze selbst gesprochen – so werden sie im Gespräch abrufbar."
                            } else {
                                "Du hast ${queue.size} Sätze wiederholt. Beim nächsten Mal sagen wir sie laut."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(Spacing.l))
                        BigButton("Noch eine Runde", {
                            round++
                            index = 0
                            done = false
                            recorded = false
                            speakingCount = 0
                            grade = 0
                            heard = ""
                            revealed = false
                            partial = ""
                            message = ""
                            typed = ""
                        })
                        Spacer(Modifier.height(Spacing.s))
                        BigButton("Fertig", { leave() }, secondary = true)
                    }
                }

                chunk != null -> {
                    Spacer(Modifier.height(Spacing.s))
                    SoftCard {
                        Text("Sag es auf Englisch", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(Spacing.s))
                        Text(chunk.de, style = MaterialTheme.typography.headlineSmall)
                        if (revealed) {
                            Spacer(Modifier.height(Spacing.s))
                            Text(chunk.en, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                            ) {
                                BigButton(
                                    text = "Anhören",
                                    onClick = { speech.speak(chunk.en) },
                                    secondary = true,
                                    modifier = Modifier.weight(1f)
                                )
                                BigButton(
                                    text = "Langsam",
                                    onClick = { speech.speak(chunk.en, rate = 0.6f) },
                                    secondary = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(Spacing.m))
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                        BigButton(
                            text = if (listening) "Ich höre zu …" else "Sprechen",
                            onClick = {
                                if (listening) {
                                    speech.stopListening()
                                } else {
                                    listening = true
                                    partial = ""
                                    message = ""
                                    speech.listen(
                                        onPartial = { partial = it },
                                        onResult = { attempt: SpeechAttempt ->
                                            listening = false
                                            if (attempt.error != null) {
                                                message = attempt.error
                                            } else {
                                                val result = Scorer.score(chunk.en, attempt.transcript, usedHint = revealed)
                                                finishAnswer(result.grade, attempt.transcript, revealed, missingWords = result.missing)
                                            }
                                        }
                                    )
                                }
                            },
                            icon = if (listening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(Spacing.s))
                    Text(
                        "Oder tippen, wenn Sprechen gerade nicht geht:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = typed,
                        onValueChange = { typed = it },
                        label = { Text("Auf Englisch") },
                        singleLine = true,
                        enabled = grade == 0,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(Spacing.s))
                    BigButton(
                        text = "Antwort prüfen",
                        enabled = typed.isNotBlank() && grade == 0,
                        onClick = {
                            val result = Scorer.score(chunk.en, typed, usedHint = revealed, strict = true)
                            finishAnswer(result.grade, "", revealed, spoken = false, missingWords = result.missing)
                        },
                        secondary = true
                    )
                    if (listening) {
                        Spacer(Modifier.height(Spacing.s))
                        Waveform(
                            amplitude = 0.6f,
                            active = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        )
                        Text(partial, style = MaterialTheme.typography.bodySmall)
                    }
                    if (message.isNotBlank()) {
                        Spacer(Modifier.height(Spacing.s))
                        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(Spacing.s))
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                            BigButton("Hat geklappt", { finishAnswer(3, "", true) }, secondary = true, modifier = Modifier.weight(1f))
                            BigButton("Nicht ganz", { finishAnswer(1, "", true) }, secondary = true, modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(Spacing.m))
                    BigButton(
                        text = if (revealed) "Satz verbergen" else "Satz zeigen",
                        onClick = { revealed = !revealed },
                        secondary = true
                    )
                    if (grade > 0) {
                        Spacer(Modifier.height(Spacing.m))
                        FeedbackPanel(
                            tone = if (grade >= 2) FeedbackTone.GOOD else FeedbackTone.RETRY,
                            title = when {
                                grade >= 3 -> "Klar verstanden"
                                grade == 2 -> "Fast geschafft"
                                else -> "Noch nicht ganz"
                            },
                            detail = when {
                                grade >= 3 -> "Der Satz sitzt – in ein paar Tagen kommt er anders wieder."
                                grade == 2 && missing.isNotEmpty() ->
                                    "Der Sinn war da – es fehlte: ${missing.joinToString(", ")}."
                                grade == 2 -> "Der Sinn war da. In ein paar Tagen kommt er wieder."
                                else -> "Höre es einmal langsam und sprich dann nach."
                            },
                            model = chunk.en,
                            onSpeakModel = { speech.speak(chunk.en) },
                            onSpeakSlow = { speech.speak(chunk.en, rate = 0.6f) }
                        )
                        Spacer(Modifier.height(Spacing.s))
                        BigButton("Weiter", { next() })
                    }
                    if (heard.isNotBlank()) {
                        Spacer(Modifier.height(Spacing.s))
                        Text(
                            "Ich habe gehört: „$heard“",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(120.dp))
        }
    }
}
