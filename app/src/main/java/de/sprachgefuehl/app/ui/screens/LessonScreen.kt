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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import de.sprachgefuehl.app.domain.LessonMixer
import de.sprachgefuehl.app.domain.Skill
import de.sprachgefuehl.app.domain.Streak
import de.sprachgefuehl.app.domain.Garden
import de.sprachgefuehl.app.domain.Scorer
import de.sprachgefuehl.app.domain.TaskFormats
import de.sprachgefuehl.app.speech.SpeechAttempt
import de.sprachgefuehl.app.ui.components.BigButton
import de.sprachgefuehl.app.ui.components.DividerLine
import de.sprachgefuehl.app.ui.components.ErrorCardContent
import de.sprachgefuehl.app.ui.components.FeedbackPanel
import de.sprachgefuehl.app.ui.components.FeedbackTone
import de.sprachgefuehl.app.ui.components.GardenPlant
import de.sprachgefuehl.app.ui.components.GlossedSentence
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
    val hint: String = "",
    /** Getippt zählt nicht als gesprochene Aufgabe. */
    val spoken: Boolean = true
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
    // Der Aufgabenmix bleibt für diesen Tag und Lektionslauf stabil: ein Neustart mitten in
    // der Stunde setzt an derselben Stelle fort, eine spätere Runde sieht anders aus.
    val today = remember { (vm.repository?.now() ?: System.currentTimeMillis()) / 86_400_000L }
    val sessionSteps = remember(lesson.id, state.progress.lessonRuns[lesson.id], state.progress.seed) {
        LessonMixer.steps(lesson, content, state.progress, today)
    }
    val startIndex = (state.progress.lessonProgress[lesson.id] ?: 0).coerceIn(0, sessionSteps.lastIndex)
    var index by rememberSaveable(lesson.id) { mutableIntStateOf(startIndex) }
    var feedback by remember { mutableStateOf<FeedbackState?>(null) }
    var attempts by remember { mutableIntStateOf(0) }
    var hintLevel by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    var newMilestones by remember { mutableStateOf<List<Milestone>>(emptyList()) }
    // Nur wirklich gesprochene Aufgaben zählen – nicht die, die getippt wurden.
    var spokenSteps by remember(lesson.id) { mutableIntStateOf(0) }
    val deferredCards = remember { mutableStateListOf<ErrorCard>() }

    val step = sessionSteps.getOrNull(index) ?: sessionSteps.last()
    val total = sessionSteps.size
    // Gemessene Zeit: läuft im Hintergrund, wird bei jedem Schritt und beim Verlassen gebucht.
    val startedAt = remember(lesson.id) { System.currentTimeMillis() }
    var reportedSeconds by remember(lesson.id) { mutableIntStateOf(0) }
    fun elapsedSeconds(): Int = ((System.currentTimeMillis() - startedAt) / 1000L).toInt()
    fun reportTime() {
        val elapsed = elapsedSeconds()
        val delta = elapsed - reportedSeconds
        if (delta > 0) {
            reportedSeconds = elapsed
            vm.recordLearningTime(delta)
        }
    }
    DisposableEffect(lesson.id) { onDispose { reportTime() } }
    var stepDone by remember(step.id) { mutableStateOf(!stepNeedsAnswer(step)) }

    fun advance() {
        if (!stepDone) return
        reportTime()
        vm.recordStep(lesson.id, index)
        if (index >= sessionSteps.lastIndex) {
            vm.completeLesson(lesson.id, spokenSteps, elapsedSeconds())
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
        // Der Abschluss ist eine Einschätzung, keine Prüfung: keine Note, kein Rot.
        if (step.type == "SelfCheck") {
            stepDone = true
            feedback = FeedbackState(
                tone = FeedbackTone.GOOD,
                title = if (outcome.grade >= 3) "Danke – dann geht es so weiter" else "Danke, das merke ich mir",
                detail = if (outcome.grade >= 3) {
                    "Die nächste Lektion baut darauf auf."
                } else {
                    "Dieser Satz kommt in die Wiederholung. Nachholen musst du nichts."
                },
                model = ""
            )
            return
        }
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
        val stepSkill = skillForStep(step.type)
        // Nur echte Sprechaufgaben zählen als Sprechen. Diktat ist Hören, Lücke ist Grammatik.
        if (step.chunkId.isNotBlank()) {
            vm.recordSpeaking(
                step.chunkId,
                outcome.grade,
                outcome.usedHint,
                skills = if (step.type in speakingTypes) listOf(Skill.BAUSTEINE, Skill.SPRECHEN)
                else listOf(stepSkill ?: Skill.BAUSTEINE)
            )
        }
        // Ein Fehler wird mit Satz und Gehörtem gemerkt und kommt in einer späteren Lektion
        // zurück. Eine richtige Antwort auf einen eingestreuten Satz löst den Eintrag auf.
        if (step.type != "SelfCheck" && step.chunkId.isNotBlank()) {
            if (outcome.grade < 2) {
                vm.recordMistake(
                    chunkId = step.chunkId,
                    lessonId = lesson.id,
                    lessonTitle = lesson.title,
                    target = target,
                    heard = outcome.heard,
                    skill = stepSkill,
                    cardId = card?.id.orEmpty()
                )
            } else {
                // Eine richtige Antwort löst den Eintrag – auch wenn die Herkunft fehlt.
                vm.resolveMistake(step.chunkId)
            }
        }
        if (step.type in speakingTypes && outcome.heard.isNotBlank() && outcome.spoken) spokenSteps += 1
        // The same answer also feeds the skill profile: one check, two uses.
        val checkSkill = stepSkill
        if (checkSkill != null) {
            val level = when {
                lesson.level.startsWith("B1") -> 2
                lesson.level.startsWith("A2") -> 1
                else -> 0
            }
            vm.recordCheck(checkSkill, outcome.grade >= 2, level)
        }
        if (outcome.grade >= 2) {
            stepDone = true
            val origin = step.originTitle
            feedback = FeedbackState(
                tone = if (outcome.grade == 3) FeedbackTone.GOOD else FeedbackTone.ALMOST,
                title = when {
                    origin.isNotBlank() -> "Jetzt sitzt er"
                    outcome.grade == 3 -> "Klar verstanden"
                    else -> "Fast geschafft"
                },
                detail = when {
                    origin.isNotBlank() -> step.originNote.ifBlank {
                        "In „$origin“ war dieser Satz noch wackelig – jetzt hast du ihn gesagt."
                    }
                    outcome.grade == 3 -> "Die wichtigen Wörter waren da."
                    else -> "Der Sinn war da – achte auf die Form."
                },
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
            seconds = elapsedSeconds(),
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
                    stepKind(step).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = Spacing.m)
                )
                Text(
                    step.title.ifBlank { stepKind(step) },
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.s)
                )
                val shownSkill = TaskFormats.skillFor(step.type)
                if (shownSkill != null) {
                    Text(
                        "Übt: ${shownSkill.label}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = Spacing.s)
                    )
                }
                if (step.why.isNotBlank()) {
                    Text(
                        "Warum diese Aufgabe?",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = Spacing.xs)
                    )
                    Text(
                        step.why,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = Spacing.s)
                    )
                }
                if (step.originNote.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(Sizes.cornerSmall),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Spacing.m)
                    ) {
                        Text(
                            step.originNote,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(Spacing.m)
                        )
                    }
                }
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
                    if (!stepDone) {
                        Text(
                            "Erst die Aufgabe lösen – dann geht es weiter.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = Spacing.xs)
                        )
                    }
                    BigButton(
                        text = if (index >= total - 1) "Lektion abschließen" else "Weiter",
                        onClick = { advance() },
                        enabled = stepDone
                    )
                }
            }
        }
    }
}

/** Welcher Bereich durch diese Aufgabe wirklich trainiert wird. */
private fun skillForStep(type: String): Skill? = when (type) {
    "ComprehensionDeep", "UnderstandFastSpeech", "MinimalPair", "ListenNumber",
    "ListenMeaning", "Dictation" -> Skill.HOEREN
    "GrammarSpot", "BuildSentence", "GapFill" -> Skill.GRAMMATIK
    "ChooseConnector", "ConnectClauses" -> Skill.VERKNUEPFEN
    "SpeakFree", "TransferTask", "Paraphrase", "WhatsAppReply", "Recall" -> Skill.SPRECHEN
    "MeaningChoice", "SpeakFillSlot" -> Skill.BAUSTEINE
    else -> null
}

private fun stepNeedsAnswer(step: Step): Boolean = step.type in setOf(
    "ComprehensionDeep", "GrammarSpot", "ChooseConnector", "ConnectClauses",
    "UnderstandFastSpeech", "ListenNumber", "MinimalPair", "BuildSentence",
    "SpeakRepeat", "SpeakFillSlot", "SpeakFree", "Shadowing", "Paraphrase",
    "TranslateToEnglish", "Recall", "TransferTask", "WhatsAppReply", "MeaningChoice",
    "ListenMeaning", "GapFill", "Dictation", "SelfCheck"
)

/** Ein Wort, das sagt, wo im Ablauf man gerade ist – vor dem Titel, nicht statt seiner. */
private fun stepKind(step: Step): String = when (step.type) {
    "DialogInput", "UnderstandFastSpeech" -> "Hören"
    "ComprehensionDeep", "ListenNumber", "MinimalPair" -> "Verstehen"
    "Noticing" -> "Die Form"
    "ChunkIntro" -> "Baustein"
    "BuildSentence" -> "Wortstellung"
    "MeaningChoice" -> "Bedeutung"
    "ListenMeaning" -> "Hören"
    "GapFill" -> "Lücke"
    "Dictation" -> "Diktat"
    "Recall" -> "Abrufen"
    "GrammarSpot", "ChooseConnector", "ConnectClauses" -> "Grammatik"
    "TransferTask" -> "Übertragen"
    "SelfCheck" -> "Abschluss"
    else -> "Sprechen"
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = Spacing.m, bottom = Spacing.xs)
    )
}
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
        "Dictation" -> DictationBody(step, speech, enabled, onOutcome)
        "ComprehensionDeep" -> ComprehensionBody(step, speech, enabled, onOutcome)
        "GrammarSpot", "ChooseConnector", "ConnectClauses", "UnderstandFastSpeech", "ListenNumber", "MinimalPair", "MeaningChoice", "ListenMeaning", "GapFill" ->
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
        SectionLabel("Szene")
        if (step.promptDe.isNotBlank()) {
            Text(step.promptDe, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        SectionLabel("Dialog")
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
            SectionLabel(if (step.type == "ChunkIntro") "Baustein" else "Beispiel")
            Surface(
                shape = RoundedCornerShape(Sizes.corner),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Spacing.l)) {
                    GlossedSentence(
                        sentence,
                        style = MaterialTheme.typography.headlineSmall,
                        onSpeak = { speech.speak(it, 0.85f) }
                    )
                    if (step.promptDe.isNotBlank()) {
                        Text(
                            "Deutsch",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = Spacing.m)
                        )
                        Text(
                            step.promptDe,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
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
            SectionLabel("Erklärung")
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
        SectionLabel("Deine Einschätzung")
        Text(
            step.promptDe.ifBlank { "Wie ist es dir mit dieser Lektion ergangen?" },
            style = MaterialTheme.typography.bodyLarge
        )
        if (step.model.isNotBlank()) {
            Spacer(Modifier.height(Spacing.m))
            SoftCard {
                SectionLabel("Dein Satz")
                GlossedSentence(step.model, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Spacer(Modifier.height(Spacing.m))
        Text("Was trifft am ehesten zu?", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(Spacing.s))
        BigButton("Gut geklappt – ich fühle mich sicher", { onOutcome(Outcome(3)) })
        Spacer(Modifier.height(Spacing.s))
        BigButton("Fast – es fehlte noch etwas", { onOutcome(Outcome(2)) }, secondary = true)
        Spacer(Modifier.height(Spacing.s))
        BigButton("Noch nicht – bitte wiederholen", { onOutcome(Outcome(1)) }, secondary = true)
        Spacer(Modifier.height(Spacing.s))
        Text(
            "Deine Antwort ändert nur, was als Nächstes wiederholt wird. Bewertet wird hier nichts.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ComprehensionBody(
    step: Step,
    speech: SpeechController,
    enabled: Boolean,
    onOutcome: (Outcome) -> Unit
) {
    var questionIndex by remember(step.id) { mutableIntStateOf(0) }
    var selected by remember(step.id) { mutableIntStateOf(-1) }
    var checked by remember(step.id) { mutableStateOf(false) }
    var confirmed by remember(step.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val question = step.questions.getOrNull(questionIndex) ?: step.questions.firstOrNull()
    if (question == null) {
        Text(step.promptDe, style = MaterialTheme.typography.bodyLarge)
        return
    }
    LaunchedEffect(enabled) {
        if (enabled && checked && !confirmed) {
            checked = false
            selected = -1
        }
    }
    Column {
        SectionLabel("Aufgabe")
        if (step.promptDe.isNotBlank()) {
            Text(step.promptDe, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(Spacing.s))
        }
        if (step.lines.isNotEmpty()) {
            BigButton(
                text = "Dialog noch einmal anhören",
                onClick = {
                    scope.launch {
                        for (line in step.lines) {
                            speech.speak(line.en, 0.8f)
                            delay(900L + line.en.split(" ").size * 380L)
                        }
                    }
                },
                icon = Icons.Rounded.VolumeUp,
                secondary = true
            )
            Spacer(Modifier.height(Spacing.m))
        }
        if (step.questions.size > 1) {
            Text(
                "Frage ${questionIndex + 1} von ${step.questions.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(Spacing.xs))
        }
        Text(question.prompt, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(Spacing.m))
        SectionLabel("Deine Antwort")
        question.options.forEachIndexed { index, option ->
            val isSelected = selected == index
            val isAnswer = checked && index == question.answer
            Surface(
                onClick = { if (!checked) selected = index },
                enabled = enabled && !checked,
                shape = RoundedCornerShape(Sizes.cornerSmall),
                color = if (isSelected || isAnswer) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.s)
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.m),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        option,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    if (isAnswer) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = "richtig",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        BigButton(
            text = "Prüfen",
            enabled = selected >= 0 && enabled && !checked,
            onClick = {
                checked = true
                val correct = selected == question.answer
                if (!correct) {
                    onOutcome(
                        Outcome(
                            grade = 1,
                            heard = question.options.getOrNull(selected).orEmpty(),
                            hint = "Richtig ist: „${question.options[question.answer]}“. ${question.why}"
                        )
                    )
                } else if (questionIndex < step.questions.lastIndex) {
                    // Erst die Bestätigung, dann die nächste Frage – kein stiller Sprung.
                    confirmed = true
                    scope.launch {
                        delay(1000)
                        questionIndex += 1
                        selected = -1
                        checked = false
                        confirmed = false
                    }
                } else {
                    onOutcome(Outcome(grade = 3, heard = question.options.getOrNull(selected).orEmpty()))
                }
            }
        )
        if (confirmed) {
            Spacer(Modifier.height(Spacing.s))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(Spacing.xs))
                Text(
                    if (questionIndex < step.questions.lastIndex) "Richtig – weiter mit Frage ${questionIndex + 2}"
                    else "Richtig",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
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
        if (step.type == "MinimalPair" || step.type == "ListenNumber" || step.type == "ListenMeaning") {
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
        if (step.type == "MinimalPair" || step.type == "ListenNumber" || step.type == "ListenMeaning") {
            BigButton("Nochmal hören", { speech.speak(audio) }, icon = Icons.Rounded.VolumeUp, secondary = true)
            Spacer(Modifier.height(Spacing.m))
        }
        SectionLabel("Deine Antwort")
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
        SectionLabel("Aufgabe")
        Text(step.promptDe, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(Spacing.m))
        SectionLabel("Deine Antwort")
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

@Composable
private fun DictationBody(
    step: Step,
    speech: SpeechController,
    enabled: Boolean,
    onOutcome: (Outcome) -> Unit
) {
    var input by remember(step.id) { mutableStateOf("") }
    var checked by remember(step.id) { mutableStateOf(false) }
    val target = step.target
    LaunchedEffect(step.id) {
        if (target.isNotBlank()) speech.speak(target)
    }
    LaunchedEffect(enabled) {
        if (enabled && checked) checked = false
    }
    Column {
        SectionLabel("Aufgabe")
        Text(
            step.promptDe.ifBlank { "Hör zu und schreibe den Satz auf." },
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(Spacing.m))
        BigButton("Satz anhören", { speech.speak(target) }, icon = Icons.Rounded.VolumeUp)
        Spacer(Modifier.height(Spacing.xs))
        BigButton("Langsam", { speech.speak(target, 0.6f) }, secondary = true)
        Spacer(Modifier.height(Spacing.m))
        OutlinedTextField(
            value = input,
            onValueChange = { input = it; checked = false },
            label = { Text("Was hörst du?") },
            minLines = 2,
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )
        Spacer(Modifier.height(Spacing.s))
        BigButton(
            text = "Prüfen",
            enabled = input.isNotBlank() && enabled && !checked,
            onClick = {
                val result = Scorer.score(target = target, heard = input, strict = true)
                checked = true
                onOutcome(Outcome(result.grade, input, spoken = false))
            }
        )
        Spacer(Modifier.height(Spacing.s))
        Text(
            "Tippe den Satz vollständig. Beim Diktat zählt auch die Schreibweise.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
    var typing by remember(step.id) { mutableStateOf(false) }
    var typed by remember(step.id) { mutableStateOf("") }
    // Verstehen vor Sprechen: nur bei langen Sätzen, einmal pro Lektion.
    var gatePassed by remember(step.id) { mutableStateOf(step.understandingOptions.isEmpty()) }
    var gateWrong by remember(step.id) { mutableStateOf(false) }

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
                    val result = Scorer.score(
                        target = target,
                        heard = attempt.transcript,
                        requiredForm = step.requiredForm,
                        accept = step.accept,
                        usedHint = hintLevel > 0
                    )
                    onOutcome(Outcome(result.grade, attempt.transcript, hintLevel > 0))
                }
            }
        )
    }

    fun checkTyped() {
        if (typed.isBlank()) return
        val result = Scorer.score(
            target = target,
            heard = typed,
            requiredForm = step.requiredForm,
            accept = step.accept,
            usedHint = hintLevel > 0,
            strict = true
        )
        spoken = typed
        onOutcome(Outcome(result.grade, typed, hintLevel > 0, spoken = false))
    }

    Column {
        SectionLabel("Aufgabe")
        if (step.promptDe.isNotBlank()) {
            Text(step.promptDe, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(Spacing.s))
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

        if (!gatePassed) {
            Surface(
                shape = RoundedCornerShape(Sizes.corner),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(Spacing.l)) {
                    SectionLabel("Erst verstehen")
                    Text(
                        "Was heißt dieser Satz auf Deutsch?",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(Spacing.s))
                    step.understandingOptions.forEachIndexed { index, option ->
                        val correct = index == step.understandingAnswer
                        Surface(
                            onClick = {
                                gatePassed = true
                                gateWrong = !correct
                                if (!correct) speech.speak(target, 0.8f)
                            },
                            shape = RoundedCornerShape(Sizes.cornerSmall),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = Spacing.s)
                        ) {
                            Text(
                                option,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(Spacing.m)
                            )
                        }
                    }
                    Text(
                        "Wenn du unsicher bist: einfach tippen. Danach kommt der Satz zum Nachsprechen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(Spacing.m))
        }

        if (gatePassed) {
            if (gateWrong) {
                Text(
                    "Richtig heißt es: „${step.understandingDe}“",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Höre den Satz einmal und sprich ihn dann nach.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.m))
            }

            if (!step.modelHidden) {
                SoftCard {
                    SectionLabel("So klingt es")
                    GlossedSentence(
                        target,
                        style = MaterialTheme.typography.headlineSmall,
                        onSpeak = { speech.speak(it, 0.8f) }
                    )
                    Spacer(Modifier.height(Spacing.m))
                    // Zwei halbe Knöpfe reichen auf schmalen Geräten nicht für „Anhören“ –
                    // das Wort brach mitten im Buchstaben. Untereinander bleibt es ganz.
                    Column {
                        BigButton(
                            "Anhören",
                            { speech.speak(target) },
                            icon = Icons.Rounded.VolumeUp
                        )
                        Spacer(Modifier.height(Spacing.s))
                        BigButton(
                            "Langsam",
                            { speech.speak(target, 0.6f) },
                            secondary = true
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.m))
            }
            SectionLabel("Deine Antwort")
            BigButton(
                text = if (listening) "Ich höre zu …" else "Jetzt sprechen",
                onClick = { if (listening) speech.stopListening() else startListening() },
                icon = if (listening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                enabled = enabled
            )
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
            }
            if (message.isNotBlank() && !typing) {
                Spacer(Modifier.height(Spacing.s))
                BigButton(
                    "Mikrofon geht nicht? Lieber tippen",
                    onClick = { typing = true },
                    secondary = true
                )
            }
            if (typing) {
                Spacer(Modifier.height(Spacing.m))
                OutlinedTextField(
                    value = typed,
                    onValueChange = { typed = it },
                    label = { Text("Satz auf Englisch") },
                    minLines = 2,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled
                )
                Spacer(Modifier.height(Spacing.s))
                BigButton(
                    "Prüfen",
                    onClick = { checkTyped() },
                    enabled = typed.isNotBlank() && enabled
                )
            }
            if (spoken.isNotBlank()) {
                Spacer(Modifier.height(Spacing.s))
                Text("Ich habe gehört: „$spoken“", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(Spacing.m))
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
}

@Composable
private fun LessonComplete(
    lesson: Lesson,
    state: de.sprachgefuehl.app.data.Progress,
    speech: SpeechController,
    deferredCards: List<ErrorCard>,
    milestones: List<Milestone>,
    seconds: Int,
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
        Spacer(Modifier.height(Spacing.s))
        Text(
            "${((seconds + 59) / 60).coerceAtLeast(1)} Minuten – gemessen, nicht geschätzt",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        val streak = Streak.of(state, System.currentTimeMillis() / 86_400_000L)
        if (streak.current > 0) {
            Text(
                Streak.line(streak),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
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
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "${state.chunkMemory.size} Bausteine in deiner Sammlung" + when (state.mistakes.size) {
                    0 -> "."
                    1 -> " · 1 Satz kommt in einer späteren Lektion zurück."
                    else -> " · ${state.mistakes.size} Sätze kommen in einer späteren Lektion zurück."
                },
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
