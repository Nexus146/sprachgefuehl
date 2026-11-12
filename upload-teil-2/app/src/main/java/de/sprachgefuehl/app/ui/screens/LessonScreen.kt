package de.sprachgefuehl.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import de.sprachgefuehl.app.speech.AudioPlayer
import de.sprachgefuehl.app.speech.AudioRecorder

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
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.data.ErrorCard
import de.sprachgefuehl.app.data.Lesson
import de.sprachgefuehl.app.domain.AnswerKey
import de.sprachgefuehl.app.domain.Milestone
import de.sprachgefuehl.app.data.Step
import de.sprachgefuehl.app.domain.ErrorDiagnosis
import de.sprachgefuehl.app.domain.LessonMixer
import de.sprachgefuehl.app.domain.Skill
import de.sprachgefuehl.app.domain.Streak
import de.sprachgefuehl.app.domain.Garden
import de.sprachgefuehl.app.domain.Scorer
import de.sprachgefuehl.app.domain.TaskRegistry
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
import de.sprachgefuehl.app.ui.components.pressScale
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import de.sprachgefuehl.app.ui.speech.SpeechController
import de.sprachgefuehl.app.ui.speech.rememberSpeechController
import de.sprachgefuehl.app.ui.theme.LocalSprachColors
import de.sprachgefuehl.app.ui.theme.Sizes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import de.sprachgefuehl.app.ui.theme.Spacing
import de.sprachgefuehl.app.ui.components.TapSurface
import de.sprachgefuehl.app.ui.components.cardBorder
import de.sprachgefuehl.app.ui.components.cardElevation
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
    // A focus round is built for one error card and lives outside the path content.
    val lesson = state.content?.lessons?.firstOrNull { it.id == lessonId } ?: vm.focusLesson(lessonId)
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
    var finished by remember { mutableStateOf(false) }
    var completedSeconds by remember { mutableIntStateOf(0) }
    var newMilestones by remember { mutableStateOf<List<Milestone>>(emptyList()) }
    val deferredCards = remember { mutableStateListOf<ErrorCard>() }

    // The round is created once and stored with its concrete steps. A new day or a new task
    // mix can therefore never move the saved index onto a different task.
    val activeSession = state.progress.lessonSession?.takeIf { it.lessonId == lesson.id }
    LaunchedEffect(lesson.id) { vm.startLesson(lesson.id) }

    if (finished) {
        LessonComplete(
            lesson = lesson,
            state = state.progress,
            speech = speech,
            deferredCards = deferredCards.toList(),
            milestones = newMilestones,
            seconds = completedSeconds,
            today = vm.today(),
            onExit = onExit
        )
        return
    }

    if (activeSession == null || activeSession.steps.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Lektion wird vorbereitet …", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val sessionSteps = activeSession.steps
    val startIndex = activeSession.index.coerceIn(0, sessionSteps.lastIndex)
    var index by rememberSaveable(lesson.id) { mutableIntStateOf(startIndex) }
    var feedback by remember { mutableStateOf<FeedbackState?>(null) }
    var attempts by remember { mutableIntStateOf(0) }
    var hintLevel by remember { mutableIntStateOf(0) }
    // Nur wirklich gesprochene Aufgaben zählen – nicht die, die getippt wurden.
    var spokenSteps by remember(lesson.id) { mutableIntStateOf(0) }

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
    val scrollState = rememberScrollState()
    // Jeder Schritt beginnt oben – der Scrollstand des vorigen Schritts gehört nicht hierher.
    LaunchedEffect(step.id) { scrollState.scrollTo(0) }
    // Nach dem Lösen erscheint die Leiste und nimmt Platz weg. Statt ans Ende zu springen,
    // rückt der Inhalt nur so weit nach, dass die Rückmeldung ganz im Bild ist – die eigene
    // Antwort (bei Auswahlaufgaben die markierte Zeile) bleibt dabei so weit wie möglich stehen.
    var viewportTop by remember { mutableFloatStateOf(0f) }
    var viewportBottom by remember { mutableFloatStateOf(0f) }
    var feedbackBottom by remember { mutableFloatStateOf(0f) }
    var feedbackPlaced by remember(step.id) { mutableStateOf(false) }
    // Eigene Antwort, die sichtbar bleiben soll (bei Wortbau der zusammengesetzte Satz).
    var answerTop by remember(step.id) { mutableFloatStateOf(0f) }
    var answerPlaced by remember(step.id) { mutableStateOf(false) }
    LaunchedEffect(feedback, feedbackPlaced) {
        if (feedback == null || !feedbackPlaced) return@LaunchedEffect
        // Ein Frame warten: Leiste und Rückmeldung messen sich erst in diesem Durchgang.
        withFrameNanos { }
        val delta = revealDelta(feedbackBottom, viewportBottom, answerTop, viewportTop, answerPlaced)
        if (delta > 1f) {
            val target = (scrollState.value + delta.toInt()).coerceIn(0, scrollState.maxValue)
            scrollState.animateScrollTo(target)
        }
    }

    fun advance() {
        if (!stepDone) return
        reportTime()
        vm.recordStep(lesson.id, index)
        if (index >= sessionSteps.lastIndex) {
            completedSeconds = elapsedSeconds()
            vm.completeLesson(lesson.id, spokenSteps, completedSeconds)
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
        // Verständnisfragen tragen ihren Satz jetzt selbst; ohne ihn gäbe es nichts zu wiederholen.
        val target = step.target.ifBlank { step.model }.ifBlank {
            content.chunks.firstOrNull { it.id == step.chunkId }?.en.orEmpty()
        }
        val speaking = TaskRegistry.isSpeaking(step.type)
        val diagnosisId = if (outcome.heard.isNotBlank() && target.isNotBlank() && speaking) {
            ErrorDiagnosis.diagnose(target, outcome.heard, step.pillarId)
        } else ""
        val card = content.errorCards.firstOrNull { it.id == diagnosisId }
        if (card != null && !settings.explainImmediately && card !in deferredCards) deferredCards.add(card)
        val visibleCard = if (settings.explainImmediately) card else null
        val stepSkill = skillForStep(step.type)
        val level = when {
            lesson.level.startsWith("B1") -> 2
            lesson.level.startsWith("A2") -> 1
            else -> 0
        }
        // One answer, one update: card, memory, mistake and skill profile see the same snapshot.
        vm.recordAnswer(
            chunkId = step.chunkId,
            grade = outcome.grade,
            usedHint = outcome.usedHint,
            skills = if (speaking) listOf(Skill.BAUSTEINE, Skill.SPRECHEN)
            else listOf(stepSkill ?: Skill.BAUSTEINE),
            cardId = card?.id.orEmpty(),
            lessonId = lesson.id,
            lessonTitle = lesson.title,
            target = target,
            heard = outcome.heard,
            mistakeSkill = stepSkill?.id.orEmpty(),
            trackMistake = step.type != "SelfCheck",
            checkSkill = stepSkill,
            checkCorrect = outcome.grade >= 2,
            level = level,
            taskType = step.type
        )
        // Ein richtig gelöster Transfer löst auch den ursprünglichen Fehler ein.
        if (outcome.grade >= 2 && step.resolvesMistake.isNotBlank()) {
            vm.resolveMistake(step.resolvesMistake)
        }
        if (speaking && outcome.heard.isNotBlank() && outcome.spoken) spokenSteps += 1
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
                    .onGloballyPositioned {
                        viewportTop = it.positionInRoot().y
                        viewportBottom = viewportTop + it.size.height
                    }
                    .verticalScroll(scrollState)
                    .padding(horizontal = Spacing.l)
            ) {
                // Bei „Nachsprechen“ stand dasselbe Wort zweimal untereinander – klein und groß.
                // Die kleine Zeile bleibt nur, wenn sie mehr sagt als der Titel.
                val kind = stepKind(step)
                val stepTitle = step.title.ifBlank { kind }
                val titleIsKind = stepTitle.equals(kind, ignoreCase = true)
                if (!titleIsKind) {
                    Text(
                        kind.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = Spacing.m)
                    )
                }
                Text(
                    stepTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = if (titleIsKind) Spacing.m else Spacing.xs, bottom = Spacing.s)
                )
                val shownSkill = TaskRegistry.skillFor(step.type)
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
                        onInfo = { advance() },
                        onAnswerTop = {
                            answerTop = it
                            answerPlaced = true
                        }
                    )
                }
                Spacer(Modifier.height(Spacing.l))
                val currentFeedback = feedback
                if (currentFeedback != null) {
                    Box(
                        modifier = Modifier.onGloballyPositioned {
                            feedbackBottom = it.positionInRoot().y + it.size.height
                            feedbackPlaced = true
                        }
                    ) {
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
                }
                Spacer(Modifier.height(120.dp))
            }
            // Solange die Aufgabe offen ist, wäre „Weiter“ ohnehin gesperrt – die Leiste nähme nur
            // der Sprech- und Prüffläche den Platz weg und schnitte sie am unteren Rand ab.
            // Sie erscheint deshalb erst, wenn es tatsächlich weitergeht.
            if (stepDone) {
                Surface(color = MaterialTheme.colorScheme.background, shadowElevation = 8.dp) {
                    Column(modifier = Modifier.padding(Spacing.m)) {
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
}

/** Welcher Bereich durch diese Aufgabe wirklich trainiert wird. */
private fun skillForStep(type: String): Skill? = TaskRegistry.skillFor(type)

private fun stepNeedsAnswer(step: Step): Boolean = TaskRegistry.needsAnswer(step.type)

/**
 * Wie weit der Inhalt nachrücken darf, damit die Rückmeldung ganz sichtbar wird, ohne die eigene
 * Antwort aus dem Bild zu schieben (bei Wortbau der zusammengesetzte Satz).
 */
internal fun revealDelta(
    feedbackBottom: Float,
    viewportBottom: Float,
    answerTop: Float,
    viewportTop: Float,
    answerPlaced: Boolean
): Float {
    val missing = feedbackBottom - viewportBottom
    val allowed = if (answerPlaced) (answerTop - viewportTop).coerceAtLeast(0f) else Float.MAX_VALUE
    return minOf(missing, allowed).coerceAtLeast(0f)
}

/** Ein Wort, das sagt, wo im Ablauf man gerade ist – vor dem Titel, nicht statt seiner. */
private fun stepKind(step: Step): String = TaskRegistry.labelFor(step.type)

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
    onInfo: () -> Unit,
    onAnswerTop: (Float) -> Unit
) {
    when (step.type) {
        "DialogInput" -> DialogBody(step, speech)
        "Noticing", "ChunkIntro" -> InfoBody(step, speech)
        "SelfCheck" -> SelfCheckBody(step, onOutcome)
        "BuildSentence" -> BuildBody(step, enabled, onOutcome, onAnswerTop)
        "Dictation" -> DictationBody(step, speech, enabled, onOutcome)
        "ComprehensionDeep" -> ComprehensionBody(step, speech, enabled, onOutcome)
        "OrderDialogue" -> OrderDialogueBody(step, speech, enabled, onOutcome)
        "GrammarSpot", "ChooseConnector", "ConnectClauses", "UnderstandFastSpeech", "ListenNumber", "MinimalPair", "MeaningChoice", "ListenMeaning", "GapFill", "OddOneOut", "CorrectTheSentence", "TrueFalse", "TranslateToGerman" ->
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
        TapSurface(
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
                shadowElevation = cardElevation(),
                border = cardBorder(),
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
    TapSurface(
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
            val press = remember { MutableInteractionSource() }
            Surface(
                shape = RoundedCornerShape(Sizes.cornerSmall),
                color = if (isSelected || isAnswer) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.s)
                    .pressScale(press)
                    // Echte Auswahl statt bloßer Farbe: Rolle und Zustand landen so auch in der
                    // Accessibility-Info, aus der Screenreader ihre Ansage bauen.
                    .selectable(
                        selected = isSelected,
                        enabled = enabled && !checked,
                        role = Role.RadioButton,
                        interactionSource = press,
                        indication = LocalIndication.current,
                        // Erneuter Tipp auf die gewählte Zeile hebt die Auswahl wieder auf.
                        onClick = { if (!checked) selected = if (selected == index) -1 else index }
                    )
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
            val press = remember { MutableInteractionSource() }
            Surface(
                shape = RoundedCornerShape(Sizes.cornerSmall),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.s)
                    .pressScale(press)
                    // Echte Auswahl statt bloßer Farbe: Rolle und Zustand landen so auch in der
                    // Accessibility-Info, aus der Screenreader ihre Ansage bauen.
                    .selectable(
                        selected = isSelected,
                        enabled = enabled && !checked,
                        role = Role.RadioButton,
                        interactionSource = press,
                        indication = LocalIndication.current,
                        // Erneuter Tipp auf die gewählte Zeile hebt die Auswahl wieder auf.
                        onClick = { if (!checked) selected = if (selected == index) -1 else index }
                    )
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
                val correct = AnswerKey.correctIndex(step)
                // Ohne eindeutige Lösung wird nichts als falsch bewertet: Eine Aufgabe, deren
                // Antwort nicht feststeht, darf dem Lernenden nicht schaden.
                val isCorrect = correct < 0 || selected == correct
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

/** Nur für instrumentierte Tests: die Verständnisaufgabe ohne den umgebenden Bildschirm. */
@Composable
internal fun ComprehensionStepForTest(
    step: Step,
    speech: SpeechController,
    enabled: Boolean,
    onSolved: () -> Unit
) {
    ComprehensionBody(step, speech, enabled, onOutcome = { onSolved() })
}

/** Nur für instrumentierte Tests: die Auswahlaufgabe ohne den umgebenden Bildschirm. */
@Composable
internal fun ChoiceStepForTest(
    step: Step,
    speech: SpeechController,
    enabled: Boolean,
    onSolved: () -> Unit
) {
    ChoiceBody(step, speech, enabled, onOutcome = { onSolved() })
}

/** Nur für instrumentierte Tests: die Wortbau-Aufgabe ohne den umgebenden Bildschirm. */
@Composable
internal fun BuildStepForTest(
    step: Step,
    enabled: Boolean,
    onSolved: () -> Unit,
    onAnswerTop: (Float) -> Unit
) {
    BuildBody(step, enabled, onOutcome = { onSolved() }, onAnswerTop = onAnswerTop)
}

@Composable
private fun BuildBody(
    step: Step,
    enabled: Boolean,
    onOutcome: (Outcome) -> Unit,
    onAnswerTop: (Float) -> Unit
) {
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
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { onAnswerTop(it.positionInRoot().y) }
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
                    // Recognition often returns several hypotheses. Score all of them and keep
                    // the best one instead of letting the first guess decide the grade.
                    val alternatives = (listOf(attempt.transcript) + attempt.alternatives)
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .distinct()
                    val best = alternatives.maxByOrNull { candidate ->
                        Scorer.score(
                            target = target,
                            heard = candidate,
                            requiredForm = step.requiredForm,
                            accept = step.accept,
                            usedHint = hintLevel > 0
                        ).grade
                    } ?: attempt.transcript
                    spoken = best
                    val result = Scorer.score(
                        target = target,
                        heard = best,
                        requiredForm = step.requiredForm,
                        accept = step.accept,
                        usedHint = hintLevel > 0
                    )
                    onOutcome(Outcome(result.grade, best, hintLevel > 0))
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
                shadowElevation = cardElevation(),
                border = cardBorder(),
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
                        TapSurface(
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
            VoiceCompare(target = target, speech = speech, enabled = enabled && !listening)
            Spacer(Modifier.height(Spacing.m))
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
            TapSurface(
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
    today: Long,
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
        val streak = Streak.of(state, today)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OrderDialogueBody(
    step: Step,
    speech: SpeechController,
    enabled: Boolean,
    onOutcome: (Outcome) -> Unit
) {
    val lines = step.lines
    // Feste Mischung je Aufgabe: Beim Neuzeichnen bleiben die Kärtchen liegen, sonst würde die
    // halb getippte Reihenfolge unter der Hand springen.
    val shuffled = remember(step.id) { lines.indices.shuffled(Random(step.id.hashCode())) }
    var chosen by remember(step.id) { mutableStateOf(listOf<Int>()) }
    val scope = rememberCoroutineScope()
    Column {
        SectionLabel("Aufgabe")
        Text(
            step.promptDe.ifBlank { "Tippe die Zeilen in der Reihenfolge an, in der sie gesprochen werden." },
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(Spacing.m))
        BigButton(
            text = "Dialog anhören",
            onClick = {
                scope.launch {
                    for (line in lines) {
                        speech.speak(line.en, 0.8f)
                        delay(900L + line.en.split(" ").size * 380L)
                    }
                }
            },
            icon = Icons.Rounded.VolumeUp,
            secondary = true
        )
        Spacer(Modifier.height(Spacing.m))
        SectionLabel("Deine Reihenfolge")
        shuffled.forEach { lineIndex ->
            val line = lines[lineIndex]
            val number = chosen.indexOf(lineIndex)
            val used = number >= 0
            Surface(
                onClick = {
                    if (!enabled) return@Surface
                    chosen = if (used) chosen - lineIndex else chosen + lineIndex
                },
                shape = RoundedCornerShape(Sizes.cornerSmall),
                color = if (used) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.s)
            ) {
                Row(modifier = Modifier.padding(Spacing.m), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (used) "${number + 1}." else "–",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(Spacing.s))
                    Column(Modifier.weight(1f)) {
                        SpeakerBadge(line.speaker)
                        Spacer(Modifier.height(Spacing.xs))
                        Text(line.en, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        BigButton(
            text = "Prüfen",
            enabled = enabled && chosen.size == lines.size,
            onClick = {
                val attempt = chosen.map { lines[it].en }
                val correct = attempt == lines.map { it.en }
                onOutcome(
                    Outcome(
                        grade = if (correct) 3 else 1,
                        heard = attempt.joinToString(" "),
                        hint = if (correct) "" else "Richtig ist: " + lines.joinToString(" → ") { it.en }
                    )
                )
            }
        )
        Spacer(Modifier.height(Spacing.s))
        BigButton("Reihenfolge zurücksetzen", { chosen = emptyList() }, secondary = true)
    }
}

/**
 * Eigene Stimme aufnehmen und mit dem Modell vergleichen.
 *
 * Die Aufnahme läuft bewusst erst nach der Spracherkennung: Beide brauchen das Mikrofon, und
 * gleichzeitig würde die Erkennung abbrechen. Aufgenommen wird nur zum Anhören – nichts wird
 * hochgeladen, die Datei bleibt im privaten Ordner der App.
 */
@Composable
private fun VoiceCompare(target: String, speech: SpeechController, enabled: Boolean) {
    val context = LocalContext.current
    val recorder = remember { AudioRecorder(context) }
    val player = remember { AudioPlayer() }
    var recording by remember { mutableStateOf(false) }
    var clip by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            recorder.start()
            recording = true
        }
    }
    // Wird gesprochen, hält die Erkennung das Mikrofon – dann hört die Aufnahme auf, statt
    // stumm weiterzulaufen.
    LaunchedEffect(enabled) {
        if (!enabled && recording) {
            clip = recorder.stop()?.absolutePath ?: clip
            recording = false
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            recorder.cancel()
            player.stop()
        }
    }

    fun toggleRecording() {
        if (recording) {
            clip = recorder.stop()?.absolutePath ?: clip
            recording = false
            return
        }
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) {
            player.stop()
            speech.stopSpeaking()
            recorder.start()
            recording = true
        } else {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    SectionLabel("So klingst du")
    Text(
        "Nimm dich auf und höre beides hintereinander: erst dich, dann das Modell.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(Spacing.s))
    BigButton(
        text = when {
            recording -> "Aufnahme stoppen"
            clip == null -> "Eigene Stimme aufnehmen"
            else -> "Noch einmal aufnehmen"
        },
        onClick = { toggleRecording() },
        icon = if (recording) Icons.Rounded.Stop else Icons.Rounded.Mic,
        enabled = enabled
    )
    val path = clip
    if (path != null) {
        Spacer(Modifier.height(Spacing.s))
        BigButton(
            text = "Mich anhören",
            onClick = {
                speech.stopSpeaking()
                player.play(path)
            },
            icon = Icons.Rounded.PlayArrow
        )
        Spacer(Modifier.height(Spacing.xs))
        BigButton(
            text = "Modell noch einmal",
            onClick = {
                player.stop()
                speech.speak(target)
            },
            secondary = true
        )
    }
}
