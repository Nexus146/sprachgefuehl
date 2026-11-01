package de.sprachgefuehl.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.data.ErrorCard
import de.sprachgefuehl.app.data.Lesson
import de.sprachgefuehl.app.domain.Milestone
import de.sprachgefuehl.app.data.Step
import de.sprachgefuehl.app.domain.ErrorDiagnosis
import de.sprachgefuehl.app.domain.Skill
import de.sprachgefuehl.app.domain.Garden
import de.sprachgefuehl.app.domain.Scorer
import de.sprachgefuehl.app.speech.SpeechAttempt
import de.sprachgefuehl.app.ui.components.BigButton
import de.sprachgefuehl.app.ui.components.DividerLine
import de.sprachgefuehl.app.ui.components.ErrorCardContent
import de.sprachgefuehl.app.ui.components.FeedbackPanel
import de.sprachgefuehl.app.ui.components.FeedbackTone
import de.sprachgefuehl.app.ui.components.GardenPlant
import de.sprachgefuehl.app.ui.components.HintLine
import de.sprachgefuehl.app.ui.components.ProgressChain
import de.sprachgefuehl.app.ui.components.appIcon
import de.sprachgefuehl.app.ui.components.SoftCard
import de.sprachgefuehl.app.ui.components.SpeakerBadge
import de.sprachgefuehl.app.ui.components.Waveform
import de.sprachgefuehl.app.ui.components.plural
import de.sprachgefuehl.app.ui.speech.SpeechController
import de.sprachgefuehl.app.ui.speech.rememberSpeechController
import de.sprachgefuehl.app.ui.theme.LocalSprachColors
import de.sprachgefuehl.app.ui.theme.Sizes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import de.sprachgefuehl.app.ui.theme.Spacing
import kotlin.math.max
import kotlin.random.Random

private data class Outcome(
    val grade: Int,
    val heard: String = "",
    val usedHint: Boolean = false,
    val hint: String = ""
)

private data class FeedbackState(
    val tone: FeedbackTone,
    val title: String,
    val detail: String,
    val model: String,
    val card: ErrorCard? = null
)

@Composable
fun LessonScreen(vm: AppViewModel, lessonId: String, onExit: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val lesson = state.content?.lessons?.firstOrNull { it.id == lessonId }
    if (lesson == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Diese Lektion konnte nicht geladen werden.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }
    val content = state.content ?: return
    // The story for this situation is fixed the first time a lesson is opened.
    LaunchedEffect(lesson.situationId) {
        content.situations.firstOrNull { it.id == lesson.situationId }?.let { vm.variantFor(it.id) }
    }
    val speech = rememberSpeechController()
    val settings = state.progress.settings
    val startIndex = (state.progress.lessonProgress[lesson.id] ?: 0).coerceIn(0, lesson.steps.lastIndex)
    var index by rememberSaveable(lesson.id) { mutableIntStateOf(startIndex) }
    var feedback by remember { mutableStateOf<FeedbackState?>(null) }
    var attempts by remember { mutableIntStateOf(0) }
    var hintLevel by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    var newMilestones by remember { mutableStateOf<List<Milestone>>(emptyList()) }
    // Nur wirklich gesprochene Aufgaben zählen – nicht die, die mit "Weiter" übersprungen wurden.
    var spokenSteps by remember(lesson.id) { mutableIntStateOf(0) }
    val deferredCards = remember { mutableStateListOf<ErrorCard>() }

    val step = lesson.steps.getOrNull(index) ?: lesson.steps.last()
    val total = lesson.steps.size

    fun advance() {
        vm.recordStep(lesson.id, index)
        if (index >= lesson.steps.lastIndex) {
            vm.completeLesson(lesson.id, spokenSteps)
            newMilestones = vm.celebrateMilestones()
            finished = true
        } else {
            index += 1
            feedback = null
            attempts = 0
            hintLevel = 0
        }
    }

    fun handle(outcome: Outcome) {
        attempts += 1
        val target = step.target.ifBlank { step.model }
        val speakingTypes = setOf(
            "SpeakRepeat", "SpeakFree", "SpeakFillSlot", "TransferTask", "Shadowing",
            "TranslateToEnglish", "Recall", "Paraphrase", "WhatsAppReply"
        )
        val diagnosisId = if (outcome.heard.isNotBlank() && target.isNotBlank() && step.type in speakingTypes) {
            ErrorDiagnosis.diagnose(target, outcome.heard, step.pillarId)
        } else ""
        val card = content.errorCards.firstOrNull { it.id == diagnosisId }
        if (card != null) {
            vm.recordError(card.id)
            if (!settings.explainImmediately && card !in deferredCards) deferredCards.add(card)
        }
        val visibleCard = if (settings.explainImmediately) card else null
        vm.recordSpeaking(step.chunkId, outcome.grade, outcome.usedHint)
        if (step.type in speakingTypes && outcome.heard.isNotBlank()) spokenSteps += 1
        // The same answer also feeds the skill profile: one check, two uses.
        val checkSkill = when (step.type) {
            "ComprehensionDeep", "UnderstandFastSpeech", "MinimalPair", "ListenNumber" -> Skill.HOEREN
            "GrammarSpot", "BuildSentence" -> Skill.GRAMMATIK
            "ChooseConnector", "ConnectClauses" -> Skill.VERKNUEPFEN
            "SpeakFree", "TransferTask", "Paraphrase", "WhatsAppReply" -> Skill.SPRECHEN
            else -> null
        }
        if (checkSkill != null) {
            val level = when {
                lesson.level.startsWith("B1") -> 2
                lesson.level.startsWith("A2") -> 1
                else -> 0
            }
            vm.recordCheck(checkSkill, outcome.grade >= 2, level)
        }
        if (outcome.grade >= 2) {
            feedback = FeedbackState(
                tone = if (outcome.grade == 3) FeedbackTone.GOOD else FeedbackTone.ALMOST,
                title = if (outcome.grade == 3) "Klar verstanden" else "Fast geschafft",
                detail = if (outcome.grade == 3)
                    "Die wichtigen Wörter waren da."
                else
                    "Der Sinn war da – achte auf die Form.",
                model = target,
                card = visibleCard
            )
        } else {
            feedback = FeedbackState(
                tone = FeedbackTone.RETRY,
                title = "Das war noch nicht ganz",
                detail = buildString {
                    if (outcome.heard.isBlank()) {
                        append("Ich habe nichts gehört. Tippe auf das Mikrofon und sprich direkt danach.")
                    } else {
                        append("Ich habe „${outcome.heard.take(70)}“ verstanden. Höre das Modell und sag es noch einmal.")
                    }
                    if (outcome.hint.isNotBlank()) append("\n\n${outcome.hint}")
                },
                model = target,
                card = visibleCard
            )
        }
    }

    if (finished) {
        LessonComplete(
            lesson = lesson,
            state = state.progress,
            speech = speech,
            deferredCards = deferredCards.toList(),
            milestones = newMilestones,
            onExit = onExit
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.m, vertical = Spacing.s),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExit, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = "Lektion verlassen", tint = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(Modifier.width(Spacing.s))
                ProgressChain(total = total, current = index, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(Spacing.s))
                // Die Schrittzahl bleibt in einer Zeile – „4 /“ und „15“ untereinander half niemandem.
                Text(
                    "${index + 1} / $total",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.l)
            ) {
                Text(
                    step.title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = Spacing.s, bottom = Spacing.m)
                )
                AnimatedContent(
                    targetState = step.id,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "step"
                ) { _ ->
                    StepBody(
                        step = step,
                        speech = speech,
                        enabled = feedback == null,
                        hintLevel = hintLevel,
                        onHint = { hintLevel = (hintLevel + 1).coerceAtMost(2) },
                        onOutcome = { handle(it) },
                        onInfo = { advance() }
                    )
                }
                Spacer(Modifier.height(Spacing.l))
                val currentFeedback = feedback
                if (currentFeedback != null) {
                    FeedbackPanel(
                        tone = currentFeedback.tone,
                        title = currentFeedback.title,
                        detail = currentFeedback.detail,
                        model = currentFeedback.model,
                        onSpeakModel = { speech.speak(currentFeedback.model) },
                        onSpeakSlow = { speech.speak(currentFeedback.model, rate = 0.6f) },
                        errorCard = currentFeedback.card,
                        onRetry = if (currentFeedback.tone != FeedbackTone.GOOD && attempts < 3) {
                            {
                                feedback = null
                                hintLevel = 1
                            }
                        } else null
                    )
                }
                Spacer(Modifier.height(120.dp))
            }
            Surface(color = MaterialTheme.colorScheme.background, shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(Spacing.m)) {
                    BigButton(
                        text = if (feedback == null && !stepNeedsAnswer(step)) "Weiter" else "Weiter",
                        onClick = { advance() },
                        secondary = true
                    )
                }
            }
        }
    }
}

private fun stepNeedsAnswer(step: Step): Boolean = step.type in setOf(
    "ComprehensionDeep", "GrammarSpot", "ChooseConnector", "ConnectClauses",
    "UnderstandFastSpeech", "ListenNumber", "MinimalPair", "BuildSentence",
    "SpeakRepeat", "SpeakFillSlot", "SpeakFree", "Shadowing", "Paraphrase",
    "TranslateToEnglish", "Recall", "TransferTask", "WhatsAppReply", "SelfCheck"
)
@Composable
private fun StepBody(
    step: Step,
    speech: SpeechController,
    enabled: Boolean,
    hintLevel: Int,
    onHint: () -> Unit,
    onOutcome: (Outcome) -> Unit,
    onInfo: () -> Unit
) {
    when (step.type) {
        "DialogInput" -> DialogBody(step, speech)
        "Noticing", "ChunkIntro" -> InfoBody(step, speech)
        "SelfCheck" -> SelfCheckBody(step, onOutcome)
        "BuildSentence" -> BuildBody(step, enabled, onOutcome)
        "ComprehensionDeep" -> ComprehensionBody(step, enabled, onOutcome)
        "GrammarSpot", "ChooseConnector", "ConnectClauses", "UnderstandFastSpeech", "ListenNumber", "MinimalPair" ->
            ChoiceBody(step, speech, enabled, onOutcome)
        else -> SpeakBody(step, speech, enabled, hintLevel, onHint, onOutcome)
    }
}

@Composable
private fun DialogBody(step: Step, speech: SpeechController) {
    var showGerman by remember(step.id) { mutableStateOf(false) }
    var playingAll by remember(step.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun stopAll() {
        playingAll = false
        speech.output.stop()
    }

    fun playAll() {
        if (playingAll) {
            stopAll()
            return
        }
        playingAll = true
        scope.launch {
            for (line in step.lines) {
                if (!playingAll) break
                speech.speak(line.en, 0.8f)
                // No callback from the engine here: a calm estimate keeps the lines apart.
                val words = line.en.split(" ").size
                delay(900L + words * 380L)
            }
            playingAll = false
        }
    }

    Column {
        if (step.promptDe.isNotBlank()) {
            Text(step.promptDe, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.m))
        }
        Surface(
            onClick = { playAll() },
            shape = RoundedCornerShape(100),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                if (playingAll) "Stoppen" else "Alle Zeilen anhören",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s)
            )
        }
        Spacer(Modifier.height(Spacing.m))
        step.lines.forEach { line ->
            SoftCard(modifier = Modifier.padding(bottom = Spacing.s)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SpeakerBadge(line.speaker)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { stopAll(); speech.speak(line.en) }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Rounded.VolumeUp, contentDescription = "Zeile anhören", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { stopAll(); speech.speak(line.en, 0.65f) }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = "Zeile langsam anhören", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(Modifier.height(Spacing.s))
                Text(line.en, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (showGerman) {
                    Text(
                        line.de,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (showGerman) "Deutsch ausblenden" else "Deutsch anzeigen",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { showGerman = !showGerman }
                    .padding(vertical = Spacing.m)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InfoBody(step: Step, speech: SpeechController) {
    Column {
        val sentence = step.promptEn.ifBlank { step.target.ifBlank { step.model } }
        if (sentence.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(Sizes.corner),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Spacing.l)) {
                    Text(sentence, style = MaterialTheme.typography.headlineSmall)
                    if (step.promptDe.isNotBlank()) {
                        Text(
                            step.promptDe,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = Spacing.xs)
                        )
                    }
                    // FlowRow statt Row: bei großer Schrift rutschen die Knöpfe als Ganzes in
                    // die nächste Zeile, statt dass „Langsam“ mitten im Wort umbricht.
                    FlowRow(
                        modifier = Modifier.padding(top = Spacing.m),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                        verticalArrangement = Arrangement.spacedBy(Spacing.s)
                    ) {
                        SpeechButton("Anhören", 0.85f, speech, sentence)
                        SpeechButton("Langsam", 0.65f, speech, sentence)
                    }
                }
            }
            Spacer(Modifier.height(Spacing.m))
        }
        if (step.explanationDe.isNotBlank()) {
            Text(step.explanationDe, style = MaterialTheme.typography.bodyLarge)
        }
        if (step.note.isNotBlank()) {
            Spacer(Modifier.height(Spacing.s))
            Text(step.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (step.tip.isNotBlank()) {
            Spacer(Modifier.height(Spacing.m))
            HintLine(step.tip)
        }
    }
}

@Composable
private fun SpeechButton(label: String, rate: Float, speech: SpeechController, text: String) {
    Surface(
        onClick = { speech.speak(text, rate) },
        shape = RoundedCornerShape(100),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(Spacing.xs))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SelfCheckBody(step: Step, onOutcome: (Outcome) -> Unit) {
    Column {
        if (step.model.isNotBlank()) {
            Text(step.model, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = Spacing.m))
        }
        BigButton("Klar geklappt", { onOutcome(Outcome(3)) })
        Spacer(Modifier.height(Spacing.s))
        BigButton("Fast – es fehlte etwas", { onOutcome(Outcome(2)) }, secondary = true)
        Spacer(Modifier.height(Spacing.s))
        BigButton("Noch nicht", { onOutcome(Outcome(1)) }, secondary = true)
    }
}

@Composable
private fun ComprehensionBody(step: Step, enabled: Boolean, onOutcome: (Outcome) -> Unit) {
    var questionIndex by remember(step.id) { mutableIntStateOf(0) }
    var selected by remember(step.id) { mutableStateOf(-1) }
    var checked by remember(step.id) { mutableStateOf(false) }
    val question = step.questions.getOrNull(questionIndex) ?: step.questions.firstOrNull()
    if (question == null) {
        Text(step.promptDe, style = MaterialTheme.typography.bodyLarge)
        return
    }
    LaunchedEffect(enabled) {
        if (enabled && checked) {
            checked = false
            selected = -1
        }
    }
    Column {
        if (step.promptDe.isNotBlank()) {
            Text(step.promptDe, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.m))
        }
        Text(question.prompt, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(Spacing.m))
        question.options.forEachIndexed { index, option ->
            val isSelected = selected == index
            Surface(
                onClick = { if (!checked) selected = index },
                enabled = enabled && !checked,
                shape = RoundedCornerShape(Sizes.cornerSmall),
                color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.s)
            ) {
                Text(option, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(Spacing.m))
            }
        }
        BigButton(
            text = "Prüfen",
            enabled = selected >= 0 && enabled && !checked,
            onClick = {
                checked = true
                val correct = selected == question.answer
                if (correct && questionIndex < step.questions.lastIndex) {
                    questionIndex += 1
                    selected = -1
                    checked = false
                } else {
                    onOutcome(
                        Outcome(
                            grade = if (correct) 3 else 1,
                            heard = question.options.getOrNull(selected).orEmpty(),
                            hint = if (correct) "" else "Richtig ist: „${question.options[question.answer]}“. ${question.why}"
                        )
                    )
                }
            }
        )
        if (step.questions.size > 1) {
            Spacer(Modifier.height(Spacing.s))
            Text(
                "Frage ${questionIndex + 1} von ${step.questions.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChoiceBody(
    step: Step,
    speech: SpeechController,
    enabled: Boolean,
    onOutcome: (Outcome) -> Unit
) {
    var selected by remember(step.id) { mutableStateOf(-1) }
    var checked by remember(step.id) { mutableStateOf(false) }
    val audio = step.target.ifBlank { step.promptEn }
    LaunchedEffect(step.id) {
        if (step.type == "MinimalPair" || step.type == "ListenNumber") {
            speech.speak(step.target.ifBlank { step.promptEn })
        }
    }
    LaunchedEffect(enabled) {
        if (enabled && checked) {
            checked = false
            selected = -1
        }
    }
    Column {
        if (step.promptDe.isNotBlank()) {
            Text(step.promptDe, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(Spacing.s))
        }
        if (step.promptEn.isNotBlank() && step.type != "ComprehensionDeep") {
            Text(step.promptEn, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.s))
        }
        if (step.type == "MinimalPair" || step.type == "ListenNumber") {
            BigButton("Nochmal hören", { speech.speak(audio) }, icon = Icons.Rounded.VolumeUp, secondary = true)
            Spacer(Modifier.height(Spacing.m))
        }
        step.options.forEachIndexed { index, option ->
            val isSelected = selected == index
            Surface(
                onClick = { if (!checked) selected = index },
                enabled = enabled && !checked,
                shape = RoundedCornerShape(Sizes.cornerSmall),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.s)
            ) {
                Text(
                    option.text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(Spacing.m)
                )
            }
        }
        BigButton(
            text = "Prüfen",
            enabled = selected >= 0 && enabled && !checked,
            onClick = {
                checked = true
                val correct = correctIndex(step)
                val isCorrect = selected == correct
                onOutcome(
                    Outcome(
                        grade = if (isCorrect) 3 else 1,
                        heard = step.options.getOrNull(selected)?.text.orEmpty(),
                        hint = if (isCorrect) "" else "Richtig ist: „${step.options.getOrNull(correct)?.text.orEmpty()}“."
                    )
                )
            }
        )
        if (step.options.isEmpty()) {
            Text("Für diese Aufgabe gibt es keine Auswahl – höre zu und sprich nach.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun correctIndex(step: Step): Int {
    val flagged = step.options.indexOfFirst { it.correct }
    if (flagged >= 0) return flagged
    if (step.type == "MinimalPair") {
        return step.options.indexOfFirst { it.text.equals(step.target, ignoreCase = true) }
    }
    val target = step.target
    return step.options.indexOfFirst { option ->
        Regex("\\b${Regex.escape(option.text)}\\b", RegexOption.IGNORE_CASE).containsMatchIn(target)
    }.let { if (it >= 0) it else 0 }
}

@Composable
private fun BuildBody(step: Step, enabled: Boolean, onOutcome: (Outcome) -> Unit) {
    val shuffled = remember(step.id) { step.words.shuffled(Random(step.id.hashCode())) }
    var chosen by remember(step.id) { mutableStateOf(listOf<Int>()) }
    Column {
        Text(step.promptDe, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(Spacing.m))
        Surface(
            shape = RoundedCornerShape(Sizes.cornerSmall),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (chosen.isEmpty()) "Tippe unten auf die Wörter" else chosen.joinToString(" ") { shuffled[it] },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(Spacing.m)
                    .heightIn(min = 32.dp)
            )
        }
        Spacer(Modifier.height(Spacing.m))
        Column {
            shuffled.indices.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s), modifier = Modifier.padding(bottom = Spacing.s)) {
                    row.forEach { wordIndex ->
                        val used = wordIndex in chosen
                        Surface(
                            onClick = {
                                if (!enabled) return@Surface
                                chosen = if (used) chosen - wordIndex else chosen + wordIndex
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (used) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                shuffled[wordIndex],
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s)
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(Spacing.s))
        BigButton(
            text = "Prüfen",
            enabled = enabled && chosen.isNotEmpty(),
            onClick = {
                val attempt = chosen.joinToString(" ") { shuffled[it] }
                val correct = Scorer.normalize(attempt) == Scorer.normalize(step.target)
                onOutcome(Outcome(if (correct) 3 else 1, heard = attempt))
            }
        )
        Spacer(Modifier.height(Spacing.s))
        BigButton(
            text = "Wörter zurücksetzen",
            onClick = { chosen = emptyList() },
            secondary = true
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpeakBody(
    step: Step,
    speech: SpeechController,
    enabled: Boolean,
    hintLevel: Int,
    onHint: () -> Unit,
    onOutcome: (Outcome) -> Unit
) {
    val target = step.target.ifBlank { step.model }
    var listening by remember(step.id) { mutableStateOf(false) }
    var partial by remember(step.id) { mutableStateOf("") }
    var message by remember(step.id) { mutableStateOf("") }
    var spoken by remember(step.id) { mutableStateOf("") }

    fun startListening() {
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
                    message = ""
                    spoken = attempt.transcript
                    var result = Scorer.score(
                        target = target,
                        heard = attempt.transcript,
                        requiredForm = step.requiredForm,
                        accept = step.accept,
                        usedHint = hintLevel > 0
                    )
                    if (step.type in setOf("SpeakFree", "Paraphrase", "WhatsAppReply") &&
                        Scorer.normalize(attempt.transcript).size >= 3 && result.grade < 2
                    ) {
                        result = result.copy(grade = 2, headline = "Fast geschafft")
                    }
                    onOutcome(Outcome(result.grade, attempt.transcript, hintLevel > 0))
                }
            }
        )
    }

    Column {
        if (step.promptDe.isNotBlank()) {
            Text(step.promptDe, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(Spacing.m))
        }
        if (step.promptEn.isNotBlank()) {
            Text(step.promptEn, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.s))
        }
        // Die Marke zeigt, was eingesetzt wird – steht das schon in der Aufgabe
        // („Sag es mit: ‚two days‘“), wäre sie nur eine Wiederholung.
        val slotAlreadyNamed = step.slots.isNotEmpty() && step.slots.all { step.promptDe.contains(it) }
        if (step.slots.isNotEmpty() && !slotAlreadyNamed) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                step.slots.forEach { Text("„$it“", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }
            }
            Spacer(Modifier.height(Spacing.s))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            BigButton(
                text = if (listening) "Ich höre zu …" else "Sprechen",
                onClick = { if (listening) speech.stopListening() else startListening() },
                icon = if (listening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
        }
        if (listening) {
            Spacer(Modifier.height(Spacing.s))
            Waveform(amplitude = 0.6f, active = true, modifier = Modifier
                .fillMaxWidth()
                .height(48.dp))
            Text(partial, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (message.isNotBlank()) {
            Spacer(Modifier.height(Spacing.m))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.s))
            Text(
                "Du kannst auch selbst einschätzen: hat es geklappt?",
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                Surface(
                    onClick = { onOutcome(Outcome(3)) },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) { Text("Ja", Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m)) }
                Surface(
                    onClick = { onOutcome(Outcome(2)) },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) { Text("Fast", Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m)) }
            }
        }
        if (spoken.isNotBlank()) {
            Spacer(Modifier.height(Spacing.s))
            Text("Ich habe gehört: „$spoken“", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(Spacing.m))
        Row {
            Surface(
                onClick = { speech.speak(target.ifBlank { step.model }) },
                shape = RoundedCornerShape(100),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(Spacing.xs))
                    Text("Modell", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.width(Spacing.s))
            Surface(
                onClick = onHint,
                shape = RoundedCornerShape(100),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = LocalSprachColors.current.warning, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(Spacing.xs))
                    Text("Tipp", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        if (hintLevel > 0) {
            Spacer(Modifier.height(Spacing.s))
            HintLine(
                when (hintLevel) {
                    1 -> step.hint.ifBlank { "Anfang: ${target.take(12)} …" }
                    else -> "Ganzer Satz: $target"
                }
            )
        }
    }
}

@Composable
private fun LessonComplete(
    lesson: Lesson,
    state: de.sprachgefuehl.app.data.Progress,
    speech: SpeechController,
    deferredCards: List<ErrorCard>,
    milestones: List<Milestone>,
    onExit: () -> Unit
) {
    val stage = Garden.stage(state)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(Spacing.xl))
        GardenPlant(stage = stage, modifier = Modifier
            .fillMaxWidth()
            .height(180.dp))
        Spacer(Modifier.height(Spacing.m))
        Text("Lektion geschafft", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Spacing.s))
        Text(lesson.goal, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Spacing.l))
        SoftCard {
            Text("Was du jetzt kannst", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Spacing.s))
            Text(lesson.goal, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(Spacing.s))
            Text(
                "${Garden.label(stage)} · ${plural(state.sessions.size, "Runde", "Runden")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (milestones.isNotEmpty()) {
            Spacer(Modifier.height(Spacing.l))
            Text("Neu für dich", style = MaterialTheme.typography.titleLarge)
            milestones.forEach { milestone ->
                Spacer(Modifier.height(Spacing.s))
                SoftCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            appIcon(milestone.icon),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
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
        if (deferredCards.isNotEmpty()) {
            Spacer(Modifier.height(Spacing.l))
            Text("Diese Fehler schauen wir uns noch an", style = MaterialTheme.typography.titleLarge)
            deferredCards.forEach { card ->
                Spacer(Modifier.height(Spacing.m))
                SoftCard {
                    ErrorCardContent(card) { speech.speak(card.correctEn) }
                }
            }
        }
        Spacer(Modifier.height(Spacing.l))
        BigButton("Zurück zum Pfad", onExit)
        Spacer(Modifier.height(Spacing.xl))
    }
}
