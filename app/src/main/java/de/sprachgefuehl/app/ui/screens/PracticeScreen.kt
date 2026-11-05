package de.sprachgefuehl.app.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.sprachgefuehl.app.AppUiState
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.data.Chunk
import de.sprachgefuehl.app.data.ContentBundle
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.domain.Milestone
import de.sprachgefuehl.app.domain.Milestones
import de.sprachgefuehl.app.domain.PracticeCatalog
import de.sprachgefuehl.app.domain.PracticeMode
import de.sprachgefuehl.app.domain.Scorer
import de.sprachgefuehl.app.domain.SessionBuilder
import de.sprachgefuehl.app.speech.SpeechAttempt
import de.sprachgefuehl.app.ui.components.BigButton
import de.sprachgefuehl.app.ui.components.FeedbackPanel
import de.sprachgefuehl.app.ui.components.FeedbackTone
import de.sprachgefuehl.app.ui.components.HintLine
import de.sprachgefuehl.app.ui.components.ScreenHeader
import de.sprachgefuehl.app.ui.components.SoftCard
import de.sprachgefuehl.app.ui.components.Waveform
import de.sprachgefuehl.app.ui.components.appIcon
import de.sprachgefuehl.app.ui.navigation.LocalTabRoot
import de.sprachgefuehl.app.ui.speech.SpeechController
import de.sprachgefuehl.app.ui.speech.rememberSpeechController
import de.sprachgefuehl.app.ui.theme.Spacing
import kotlin.random.Random

@Composable
fun PracticeScreen(vm: AppViewModel, state: AppUiState) {
    val content = state.content ?: return
    val speech = rememberSpeechController()
    var mode by rememberSaveable { mutableStateOf("") }
    var round by rememberSaveable { mutableIntStateOf(0) }

    BackHandler(enabled = mode.isNotBlank()) { mode = "" }

    if (mode.isBlank()) {
        PracticeMenu(
            progress = state.progress,
            now = vm.repository?.now() ?: System.currentTimeMillis(),
            onPick = { mode = it; round++ },
            onBack = if (LocalTabRoot.current) null else ({ vm.back() })
        )
        return
    }

    val info = PracticeCatalog.mode(mode)
    PracticeSession(
        key = round,
        mode = info,
        content = content,
        progress = state.progress,
        speech = speech,
        vm = vm,
        onExit = { mode = "" },
        onRestart = { round++; mode = info.id }
    )
}

@Composable
private fun PracticeMenu(
    progress: Progress,
    now: Long,
    onPick: (String) -> Unit,
    onBack: (() -> Unit)?
) {
    val standing = remember(progress, now) { PracticeCatalog.standing(progress, now) }
    val recommended = standing.mode

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = "Üben",
            subtitle = "Fünf Minuten, die sitzen",
            onBack = onBack
        )
        Column(modifier = Modifier.padding(horizontal = Spacing.l)) {
            Text(
                "Hier wiederholst du Sätze aus deinem Viertel – ohne neue Lektion, ohne Bewertung.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.m))
            SoftCard {
                Text("Gerade dran", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    standing.line,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    standing.because,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.m))
                BigButton(
                    text = "Passt gerade: ${recommended.title}",
                    onClick = { onPick(recommended.id) }
                )
            }
            Spacer(Modifier.height(Spacing.l))
            Text("Alle Übungen", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.s))
            PracticeCatalog.modes.forEach { mode ->
                SoftCard(
                    modifier = Modifier.padding(bottom = Spacing.m),
                    onClick = { onPick(mode.id) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                Icon(appIcon(mode.icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(Modifier.width(Spacing.m))
                        Column(modifier = Modifier.weight(1f)) {
                            if (mode.id == recommended.id) {
                                Text(
                                    "Passt gerade",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(mode.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                mode.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(Spacing.xs))
                            Text(
                                mode.trainsLine,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            Text(
                "Eine Runde sind fünf Sätze: erst was fällig ist, dann was noch wackelt.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun PracticeSession(
    key: Int,
    mode: PracticeMode,
    content: ContentBundle,
    progress: Progress,
    speech: SpeechController,
    vm: AppViewModel,
    onExit: () -> Unit,
    onRestart: () -> Unit
) {
    val now = vm.repository?.now() ?: System.currentTimeMillis()
    val chunks = remember(key, mode.id) {
        SessionBuilder.practiceChunks(content, progress, now, limit = 5)
            .mapNotNull { id -> content.chunks.firstOrNull { it.id == id } }
    }
    var index by remember(key, mode.id) { mutableIntStateOf(0) }
    var tasks by remember(key, mode.id) { mutableIntStateOf(0) }
    var spoken by remember(key, mode.id) { mutableIntStateOf(0) }
    var finished by remember(key, mode.id) { mutableStateOf(false) }
    var milestones by remember(key, mode.id) { mutableStateOf<List<Milestone>>(emptyList()) }

    fun next() {
        if (index >= chunks.lastIndex) {
            vm.recordReviewSession(minutes = 5, speakingTasks = spoken)
            milestones = vm.celebrateMilestones()
            finished = true
        } else {
            index += 1
        }
    }

    if (chunks.isEmpty() || finished) {
        PracticeDone(
            mode = mode,
            tasks = tasks,
            milestones = milestones,
            onRestart = onRestart,
            onExit = onExit
        )
        return
    }

    val chunk = chunks[index]

    // One graded task: sentence memory, the skills this mode really trains, and the counters for
    // the session log. A dictation is not speaking, so only voice modes raise `spoken` – and
    // within a voice mode only when she really spoke. A self-check after a denied microphone is
    // no spoken sentence and must not inflate the week's count.
    fun graded(grade: Int, spoke: Boolean, hint: Boolean = false) {
        vm.recordSpeaking(chunk.id, grade, hint, mode.skills)
        tasks += 1
        if (mode.speaks && spoke) spoken += 1
    }

    /** Leaving counts the round as well – she worked, even if she stopped after three tasks. */
    fun quit() {
        if (tasks > 0) vm.recordReviewSession(minutes = 5, speakingTasks = spoken)
        onExit()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.l)
    ) {
        Spacer(Modifier.height(Spacing.s))
        // Die Überschrift steht in einer eigenen Zeile. Neben dem Knopf blieb bei großer Schrift
        // zu wenig Platz, und „Nachsprechen“ brach mitten im Wort um.
        Text(mode.title, style = MaterialTheme.typography.headlineSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Übung ${index + 1} von ${chunks.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            BigButton(
                text = "Beenden",
                onClick = { quit() },
                secondary = true,
                modifier = Modifier.width(150.dp)
            )
        }
        Spacer(Modifier.height(Spacing.m))
        when (mode.id) {
            "dictation" -> DictationBody(
                chunk = chunk,
                speech = speech,
                onGraded = { grade, spoke ->
                    graded(grade, spoke)
                },
                onNext = { next() }
            )
            "shadow" -> ShadowBody(
                chunk = chunk,
                speech = speech,
                onGraded = { grade, spoke ->
                    graded(grade, spoke)
                },
                onNext = { next() }
            )
            "order" -> OrderBody(
                chunk = chunk,
                speech = speech,
                onGraded = { grade, spoke ->
                    graded(grade, spoke)
                },
                onNext = { next() }
            )
            else -> FreeBody(
                chunk = chunk,
                content = content,
                index = index,
                speech = speech,
                onGraded = { grade, spoke ->
                    graded(grade, spoke, hint = true)
                },
                onNext = { next() }
            )
        }
        Spacer(Modifier.height(Spacing.xl))
    }
}

@Composable
private fun DictationBody(
    chunk: Chunk,
    speech: SpeechController,
    onGraded: (Int, Boolean) -> Unit,
    onNext: () -> Unit
) {
    var input by remember(chunk.id) { mutableStateOf("") }
    var checked by remember(chunk.id) { mutableStateOf(false) }
    var grade by remember(chunk.id) { mutableIntStateOf(0) }
    var showTranslation by remember(chunk.id) { mutableStateOf(false) }

    Column {
        Text("Höre den Satz und schreibe ihn auf.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(Spacing.m))
        BigButton("Satz anhören", { speech.speak(chunk.en) }, icon = Icons.Rounded.VolumeUp)
        Spacer(Modifier.height(Spacing.m))
        OutlinedTextField(
            value = input,
            onValueChange = { input = it; checked = false },
            label = { Text("Was hörst du?") },
            minLines = 2,
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
            enabled = !checked
        )
        Spacer(Modifier.height(Spacing.m))
        BigButton(
            text = "Prüfen",
            enabled = input.isNotBlank() && !checked,
            onClick = {
                val result = Scorer.score(chunk.en, input, strict = true)
                grade = result.grade
                checked = true
                // Diktat: getippt, nicht gesprochen.
                onGraded(result.grade, false)
            }
        )
        Spacer(Modifier.height(Spacing.m))
        if (checked) {
            FeedbackPanel(
                tone = when {
                    grade >= 3 -> FeedbackTone.GOOD
                    grade == 2 -> FeedbackTone.ALMOST
                    else -> FeedbackTone.RETRY
                },
                title = if (grade >= 3) "Genau so" else if (grade == 2) "Fast geschafft" else "Höre es noch einmal",
                detail = if (grade >= 3) {
                    "Du hast den Satz richtig gehört und geschrieben."
                } else {
                    "Höre den Satz noch einmal und vergleiche Wort für Wort."
                },
                model = chunk.en,
                onSpeakModel = { speech.speak(chunk.en) },
                onSpeakSlow = { speech.speak(chunk.en, rate = 0.6f) }
            )
            Spacer(Modifier.height(Spacing.s))
            if (showTranslation) {
                HintLine(chunk.de)
                Spacer(Modifier.height(Spacing.s))
            }
            BigButton(
                text = if (showTranslation) "Übersetzung verbergen" else "Übersetzung zeigen",
                onClick = { showTranslation = !showTranslation },
                secondary = true
            )
            Spacer(Modifier.height(Spacing.s))
            BigButton("Weiter", onNext)
        }
    }
}

@Composable
private fun ShadowBody(
    chunk: Chunk,
    speech: SpeechController,
    onGraded: (Int, Boolean) -> Unit,
    onNext: () -> Unit
) {
    var listening by remember(chunk.id) { mutableStateOf(false) }
    var heard by remember(chunk.id) { mutableStateOf("") }
    var message by remember(chunk.id) { mutableStateOf("") }
    var grade by remember(chunk.id) { mutableIntStateOf(0) }

    fun listen() {
        listening = true
        heard = ""
        message = ""
        speech.listen(
            onPartial = {},
            onResult = { attempt: SpeechAttempt ->
                listening = false
                if (attempt.error != null) {
                    message = attempt.error
                } else {
                    heard = attempt.transcript
                    val result = Scorer.score(chunk.en, attempt.transcript)
                    grade = result.grade
                    onGraded(result.grade, true)
                }
            }
        )
    }

    Column {
        Text(chunk.de, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(Spacing.s))
        Text(
            "Sprich den Satz auf Englisch. Du darfst ihn vorher hören – das ist keine Schummelei.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.m))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            BigButton("Langsam", { speech.speak(chunk.en, 0.6f) }, secondary = true, modifier = Modifier.weight(1f))
            BigButton("Normal", { speech.speak(chunk.en, 0.9f) }, secondary = true, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(Spacing.m))
        BigButton(
            text = if (listening) "Ich höre zu …" else "Sprechen",
            onClick = { if (listening) speech.stopListening() else listen() },
            icon = if (listening) Icons.Rounded.Stop else Icons.Rounded.Mic
        )
        if (listening) {
            Spacer(Modifier.height(Spacing.s))
            Waveform(amplitude = 0.6f, active = true, modifier = Modifier.fillMaxWidth().height(40.dp))
        }
        if (heard.isNotBlank()) {
            Spacer(Modifier.height(Spacing.s))
            Text(
                "Ich habe gehört: „$heard“",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (message.isNotBlank()) {
            Spacer(Modifier.height(Spacing.m))
            Text(message, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(Spacing.s))
            Text("Wie hat es sich angefühlt?", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                BigButton("Hat geklappt", { grade = 3; onGraded(3, false) }, secondary = true, modifier = Modifier.weight(1f))
                BigButton("Fast", { grade = 2; onGraded(2, false) }, secondary = true, modifier = Modifier.weight(1f))
            }
        }
        if (grade > 0) {
            Spacer(Modifier.height(Spacing.m))
            FeedbackPanel(
                tone = if (grade >= 2) FeedbackTone.GOOD else FeedbackTone.RETRY,
                title = if (grade >= 3) "Klar verstanden" else if (grade == 2) "Fast geschafft" else "Noch einmal zusammen",
                detail = if (grade >= 2) {
                    "Der Satz sitzt – in ein paar Tagen kommt er anders wieder."
                } else {
                    "Höre es einmal langsam und sprich dann nach."
                },
                model = chunk.en,
                onSpeakModel = { speech.speak(chunk.en) },
                onSpeakSlow = { speech.speak(chunk.en, rate = 0.6f) }
            )
            Spacer(Modifier.height(Spacing.s))
            BigButton("Weiter", onNext)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OrderBody(
    chunk: Chunk,
    speech: SpeechController,
    onGraded: (Int, Boolean) -> Unit,
    onNext: () -> Unit
) {
    val words = remember(chunk.id) {
        chunk.en.split(" ").filter { it.isNotBlank() }.shuffled(Random(chunk.id.hashCode()))
    }
    var chosen by remember(chunk.id) { mutableStateOf(listOf<Int>()) }
    var checked by remember(chunk.id) { mutableStateOf(false) }
    var correct by remember(chunk.id) { mutableStateOf(false) }

    Column {
        Text("Bringe die Wörter in die richtige Reihenfolge.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(Spacing.s))
        Text(
            chunk.de,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(Spacing.m))
        SoftCard {
            val attempt = chosen.joinToString(" ") { words[it] }
            Text(
                if (attempt.isBlank()) "Tippe die Wörter an …" else attempt,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(Spacing.s))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            verticalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            words.forEachIndexed { wordIndex, word ->
                val used = wordIndex in chosen
                Surface(
                    onClick = {
                        if (checked) return@Surface
                        chosen = if (used) chosen else chosen + wordIndex
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = if (used) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        word,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.m)
                    )
                }
            }
        }
        Spacer(Modifier.height(Spacing.m))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            BigButton(
                text = "Prüfen",
                enabled = chosen.isNotEmpty() && !checked,
                onClick = {
                    val attempt = chosen.joinToString(" ") { words[it] }
                    correct = Scorer.normalize(attempt) == Scorer.normalize(chunk.en)
                    checked = true
                    onGraded(if (correct) 3 else 1, false)
                },
                modifier = Modifier.weight(1f)
            )
            BigButton(
                text = "Zurücksetzen",
                onClick = { chosen = emptyList(); checked = false },
                secondary = true,
                modifier = Modifier.weight(1f)
            )
        }
        if (checked) {
            Spacer(Modifier.height(Spacing.m))
            FeedbackPanel(
                tone = if (correct) FeedbackTone.GOOD else FeedbackTone.RETRY,
                title = if (correct) "Richtig gebaut" else "Fast – hier ist die Reihenfolge",
                detail = if (correct) {
                    "Die Reihenfolge stimmt – so wird der Satz verstanden."
                } else {
                    "Lies den Satz einmal laut und vergleiche die Reihenfolge."
                },
                model = chunk.en,
                onSpeakModel = { speech.speak(chunk.en) },
                onSpeakSlow = { speech.speak(chunk.en, rate = 0.6f) }
            )
            Spacer(Modifier.height(Spacing.s))
            BigButton("Weiter", onNext)
        }
    }
}

@Composable
private fun FreeBody(
    chunk: Chunk,
    content: ContentBundle,
    index: Int,
    speech: SpeechController,
    onGraded: (Int, Boolean) -> Unit,
    onNext: () -> Unit
) {
    val situations = remember {
        content.situations.filter { it.scene.isNotBlank() }.shuffled(Random(System.currentTimeMillis().toInt()))
    }
    val situation = situations.getOrNull(index % situations.size.coerceAtLeast(1))
    val lesson = situation?.let { s -> content.lessons.firstOrNull { it.situationId == s.id } }
    val model = lesson?.chunkIds?.let { ids -> content.chunks.firstOrNull { it.id in ids }?.en } ?: chunk.en

    var listening by remember(chunk.id) { mutableStateOf(false) }
    var heard by remember(chunk.id) { mutableStateOf("") }
    var message by remember(chunk.id) { mutableStateOf("") }
    var grade by remember(chunk.id) { mutableIntStateOf(0) }

    fun listen() {
        listening = true
        heard = ""
        message = ""
        speech.listen(
            onPartial = {},
            onResult = { attempt: SpeechAttempt ->
                listening = false
                if (attempt.error != null) {
                    message = attempt.error
                } else {
                    heard = attempt.transcript
                    val words = Scorer.normalize(attempt.transcript)
                    if (words.size >= 3) {
                        grade = 2
                        onGraded(2, true)
                    } else {
                        message = "Ich habe nur wenige Wörter verstanden. Erzähl ruhig noch einmal."
                    }
                }
            }
        )
    }

    Column {
        Text("Frei sprechen", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(Spacing.s))
        SoftCard {
            Text(
                situation?.scene ?: "Erzähl drei Sätze über deinen Tag.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(Spacing.s))
            Text(
                "Sag drei Sätze. Falsch gibt es hier nicht.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.s))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Als Stütze:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(Spacing.s))
                Surface(
                    onClick = { speech.speak(model) },
                    shape = RoundedCornerShape(100),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(Spacing.xs))
                        Text("Modell", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        Spacer(Modifier.height(Spacing.m))
        BigButton(
            text = if (listening) "Ich höre zu …" else "Sprechen",
            onClick = { if (listening) speech.stopListening() else listen() },
            icon = if (listening) Icons.Rounded.Stop else Icons.Rounded.Mic
        )
        if (listening) {
            Spacer(Modifier.height(Spacing.s))
            Waveform(amplitude = 0.6f, active = true, modifier = Modifier.fillMaxWidth().height(40.dp))
        }
        if (heard.isNotBlank()) {
            Spacer(Modifier.height(Spacing.s))
            Text(
                "Ich habe gehört: „$heard“",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (message.isNotBlank()) {
            Spacer(Modifier.height(Spacing.s))
            Text(message, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(Spacing.s))
            Text("Hat es geklappt?", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                BigButton("Ja", { grade = 3; onGraded(3, false) }, secondary = true, modifier = Modifier.weight(1f))
                BigButton("Fast", { grade = 2; onGraded(2, false) }, secondary = true, modifier = Modifier.weight(1f))
            }
        }
        if (grade > 0) {
            Spacer(Modifier.height(Spacing.m))
            FeedbackPanel(
                tone = FeedbackTone.GOOD,
                title = "Du hast gesprochen",
                detail = "Sprich denselben Gedanken morgen noch einmal.",
                model = if (heard.isBlank()) "" else heard,
                onSpeakModel = { speech.speak(model) }
            )
            Spacer(Modifier.height(Spacing.s))
            BigButton("Weiter", onNext)
        }
    }
}

@Composable
private fun PracticeDone(
    mode: PracticeMode,
    tasks: Int,
    milestones: List<Milestone>,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(Spacing.xxl))
        Text("Geübt", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(Spacing.s))
        Text(
            if (tasks > 0) "$tasks Aufgaben in ${mode.title} – das zählt für deine Wiederholung."
            else "Heute ist nichts fällig. Eine neue Lektion bringt die Sätze zurück.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        if (milestones.isNotEmpty()) {
            Spacer(Modifier.height(Spacing.l))
            SoftCard {
                Text("Neu für dich", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.s))
                milestones.forEach { milestone ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(appIcon(milestone.icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(Spacing.s))
                        Column {
                            Text(milestone.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                milestone.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(Spacing.xl))
        BigButton("Noch eine Runde", onRestart)
        Spacer(Modifier.height(Spacing.s))
        BigButton("Fertig", onExit, secondary = true)
        Spacer(Modifier.height(Spacing.xl))
    }
}
