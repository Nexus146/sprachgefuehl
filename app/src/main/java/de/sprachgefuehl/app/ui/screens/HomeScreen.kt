package de.sprachgefuehl.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.sprachgefuehl.app.AppUiState
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.Screen
import de.sprachgefuehl.app.domain.DailyPlan
import de.sprachgefuehl.app.domain.DayMood
import de.sprachgefuehl.app.domain.Garden
import de.sprachgefuehl.app.domain.Journey
import de.sprachgefuehl.app.domain.JourneyPlanner
import de.sprachgefuehl.app.domain.LessonMixer
import de.sprachgefuehl.app.domain.NextAction
import de.sprachgefuehl.app.domain.NextKind
import de.sprachgefuehl.app.domain.NextPlanner
import de.sprachgefuehl.app.domain.NextTarget
import de.sprachgefuehl.app.domain.PathProgress
import de.sprachgefuehl.app.domain.Personalization
import de.sprachgefuehl.app.domain.Phrasing
import de.sprachgefuehl.app.domain.ProfileCatalog
import de.sprachgefuehl.app.domain.Srs
import de.sprachgefuehl.app.domain.Streak
import de.sprachgefuehl.app.domain.VariantPicker
import de.sprachgefuehl.app.ui.components.BigButton
import de.sprachgefuehl.app.ui.components.GardenFullscreen
import de.sprachgefuehl.app.ui.components.GardenSummary
import de.sprachgefuehl.app.ui.components.ProfileAvatar
import de.sprachgefuehl.app.ui.components.RingSummary
import de.sprachgefuehl.app.ui.components.SoftCard
import de.sprachgefuehl.app.ui.components.appIcon
import de.sprachgefuehl.app.ui.theme.Spacing
import java.util.Calendar

/**
 * The home screen has one job: say what is next and how the path continues. So it carries four
 * things – the recommendation, the path with two steps of foresight, today's goal and the garden –
 * and one quiet list of rooms. The full course does not appear here; 104 lessons are a shelf, not
 * an answer.
 */
@Composable
fun HomeScreen(vm: AppViewModel, state: AppUiState) {
    val content = state.content ?: return
    val progress = state.progress
    val profile = progress.profile
    val now = vm.repository?.now() ?: System.currentTimeMillis()
    val today = vm.today()
    val minutesToday = progress.measuredMinutesOn(today)
    val goal = if (profile.dailyMinutes > 0) profile.dailyMinutes else 10
    val day = DailyPlan.summary(progress, today)
    val step = NextPlanner.plan(content, progress, now, today)
    val journey = JourneyPlanner.of(content, progress, step?.lessonId)

    val lesson = step?.lessonId?.let { id -> content.lessons.firstOrNull { it.id == id } }
    val situation = lesson?.let { current -> content.situations.firstOrNull { it.id == current.situationId } }
    val variant = situation?.let { current ->
        VariantPicker.resolve(
            situation = current,
            seed = progress.seed,
            tags = Personalization.storyTagsFor(profile),
            chosenId = progress.variantIds[current.id]
        )
    }
    val encounter = variant?.personaId?.takeIf { it.isNotBlank() }
        ?.let { id -> content.personas.firstOrNull { it.id == id } }
    val story = variant?.story?.takeIf { it.isNotBlank() } ?: situation?.story.orEmpty()
    val activeSession = progress.lessonSession?.takeIf { it.lessonId == lesson?.id }
    val stepIndex = activeSession?.index ?: lesson?.let { progress.lessonProgress[it.id] ?: 0 } ?: 0
    // Eine gespeicherte Sitzung kennt ihre echte Länge. Der Fallback für alte Daten erzeugt
    // weiterhin dieselbe Mischung wie der Unterricht.
    val sessionSize = activeSession?.steps?.size ?: remember(lesson?.id, progress.seed, progress.lessonRuns[lesson?.id]) {
        lesson?.let { LessonMixer.steps(it, content, progress, today).size } ?: 0
    }
    val afterNext = situation?.let { current -> PathProgress.nextInPath(content, progress, current.id) }
    val due = vm.dueCount()
    val gardenStage = Garden.stage(progress)
    val streak = Streak.of(progress, today)
    val canDoItems = content.canDo.filter { it.id in progress.canDo }.takeLast(2)
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = Personalization.greeting(profile.greetingName, hour)

    val reviewedDay = if (profile.reviewedDay > 0) profile.reviewedDay else vm.dayOf(profile.createdAt)
    val reviewDue = profile.createdAt > 0L && today - reviewedDay >= 21 &&
        (profile.reasons().isNotEmpty() || profile.interests.isNotEmpty())

    fun go(action: NextAction) {
        when (action.target) {
            NextTarget.LESSON -> {
                val id = action.lessonId
                if (id != null) vm.navigate(Screen.Lesson(id)) else vm.navigate(Screen.Practice)
            }
            NextTarget.REVIEW -> vm.navigate(Screen.Review)
            NextTarget.PRACTICE -> vm.navigate(Screen.Practice)
            NextTarget.WORKSHOP -> vm.navigate(Screen.Workshop)
            NextTarget.CHECKUP -> vm.navigate(Screen.Checkup)
        }
    }

    var showIntro by rememberSaveable { mutableStateOf(!progress.homeIntroSeen) }
    var showGarden by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.l)
    ) {
        Spacer(Modifier.height(Spacing.s))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProfileAvatar(profile = profile, onClick = { vm.navigate(Screen.Profile) })
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { vm.navigate(Screen.Search) }, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Rounded.Search, contentDescription = "Suche", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(Spacing.s))
        Text(greeting, style = MaterialTheme.typography.displaySmall)
        Text(
            content.world.name.ifBlank { "Dein Viertel" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.m))

        // ---- 1. What is next -------------------------------------------------------------------
        if (step != null) {
            SoftCard {
                Text(
                    step.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(step.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(Spacing.xs))
                Text(step.detail, style = MaterialTheme.typography.bodyMedium)
                if (lesson != null && story.isNotBlank()) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        story,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (lesson != null && encounter != null) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        "Mit ${encounter.name} · ${encounter.role}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    step.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (lesson != null) {
                    Text(
                        if (stepIndex in 1 until sessionSize) {
                            "Weiter bei Schritt ${stepIndex + 1} von $sessionSize · ca. ${step.primary.minutes} Minuten"
                        } else {
                            "ca. ${step.primary.minutes} Minuten"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(Spacing.m))
                BigButton(
                    text = step.primary.label,
                    onClick = { go(step.primary) },
                    icon = if (step.kind == NextKind.DONE) null else situation?.let { appIcon(it.icon) }
                )
                val alternative = step.alternative
                if (alternative != null) {
                    Spacer(Modifier.height(Spacing.s))
                    BigButton(
                        text = alternative.label,
                        onClick = { go(alternative) },
                        secondary = true
                    )
                }
            }
            Spacer(Modifier.height(Spacing.m))
        }

        // ---- 2. How it continues ---------------------------------------------------------------
        if (journey != null) {
            SoftCard {
                Text(
                    "Dein Weg",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(journey.chapterLine, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.s))
                LinearProgressIndicator(
                    progress = { journey.chapterProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    drawStopIndicator = {}
                )
                Spacer(Modifier.height(Spacing.s))
                Text(
                    "${journey.chapterDone} von ${journey.chapterTotal} Lektionen in diesem Kapitel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Nur der nächste wirklich andere Ort wird genannt – zweimal „Beim Arzt“
                // hintereinander erklärt nichts.
                val nextDifferent = journey.ahead.firstOrNull { it.situationTitle != journey.now?.situationTitle }
                val nextPlace = nextDifferent?.situationTitle
                    ?: afterNext?.situation?.title
                if (!nextPlace.isNullOrBlank()) {
                    Text(
                        "Danach: $nextPlace",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (journey.afterChapter.isNotBlank()) {
                    Text(
                        "Nächstes Kapitel: ${journey.afterChapter}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "${journey.doneLessons} von ${journey.totalLessons} Lektionen geschafft",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.s))
                BigButton(
                    text = "Alle Bereiche",
                    onClick = { vm.navigate(Screen.Paths) },
                    secondary = true
                )
            }
            Spacer(Modifier.height(Spacing.m))
        }

        // ---- 3. Today --------------------------------------------------------------------------
        SoftCard {
            RingSummary(
                progress = if (goal > 0) minutesToday.toFloat() / goal.toFloat() else 0f,
                top = "$minutesToday",
                bottom = "von $goal",
                title = when (day.mood) {
                    DayMood.DONE -> "Tagesziel erreicht"
                    DayMood.REST -> "Heute ist Pausentag"
                    DayMood.OPEN -> "Dein Tagesziel"
                },
                line = when (day.mood) {
                    DayMood.DONE ->
                        "${Phrasing.minutes(day.minutes)} geübt · ${Phrasing.sentences(day.speakingTasks)} gesprochen."
                    DayMood.REST -> "Heute ist keiner deiner Lerntage. Nichts verfällt."
                    DayMood.OPEN -> Personalization.goalLine(minutesToday, goal)
                },
                hint = when (day.mood) {
                    DayMood.DONE -> "Der Garten hat heute genug Wasser."
                    DayMood.REST -> "Wenn du magst, übe trotzdem – oder lass es einfach."
                    DayMood.OPEN -> "Kein Zeitdruck – jeder Satz zählt."
                }
            )
        }

        Spacer(Modifier.height(Spacing.m))

        SoftCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Tagesstreak",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${streak.current} ${if (streak.current == 1) "Tag" else "Tage"}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(Spacing.s))
            Text(Streak.line(streak), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(Spacing.s))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                ProfileCatalog.weekdayLabels.forEachIndexed { index, label ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (streak.week.getOrElse(index) { false }) "●" else "○",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (streak.week.getOrElse(index) { false }) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (streak.best > 1) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "Bester Lauf: ${streak.best} Tage. Eine Pause nimmt nichts weg.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(Spacing.m))

        SoftCard {
            GardenSummary(
                stage = gardenStage,
                title = "Dein Sprachgarten",
                stageLabel = Garden.label(gardenStage),
                line = "${Phrasing.minutes(progress.measuredMinutes.takeIf { it > 0 } ?: progress.totalMinutes)} insgesamt"
            )
            Spacer(Modifier.height(Spacing.s))
            BigButton(
                text = "Garten groß ansehen",
                onClick = { showGarden = true },
                secondary = true
            )
        }

        if (reviewDue) {
            Spacer(Modifier.height(Spacing.m))
            SoftCard {
                Text("Passt das noch?", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "Deine Angaben aus dem ersten Start sind ein paar Wochen alt. Sie bestimmen, welche Begegnungen zuerst kommen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.s))
                BigButton(
                    text = "Angaben ansehen",
                    onClick = { vm.navigate(Screen.Preferences) },
                    secondary = true
                )
                TextButton(onClick = { vm.markProfileReviewed() }) {
                    Text("Passt noch so", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // ---- 4. The other rooms, deliberately quiet --------------------------------------------
        Spacer(Modifier.height(Spacing.m))
        SoftCard {
            Text(
                "Auch möglich",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            RoomRow(
                icon = "spark",
                title = "Dein Fortschritt",
                detail = buildString {
                    append("${progress.chunkMemory.values.count { it.stage >= 4 }} Bausteine sicher")
                    if (progress.mistakes.isNotEmpty()) {
                        append(" · ${progress.mistakes.size} Sätze kommen zurück")
                    } else {
                        append(" · Garten, Woche und nächster Schritt")
                    }
                },
                onClick = { vm.navigate(Screen.Recap) }
            )
            // Kein zweiter Weg in dieselbe Übung: „Üben“ ist der eigene Tab unten. Die
            // Wiederholung erscheint hier nur, wenn die Empfehlung nicht schon dorthin führt.
            if (step?.primary?.target != NextTarget.REVIEW) {
                RoomRow(
                    icon = "leaf",
                    title = "Wiederholen",
                    detail = when {
                        due == 1 -> "1 Satz ist fällig"
                        due > 1 -> "$due Sätze sind fällig"
                        else -> "Nichts fällig"
                    },
                    onClick = { vm.navigate(Screen.Review) }
                )
            }
            RoomRow(
                icon = "star",
                title = "Meine Sätze",
                detail = "${progress.chunkMemory.size} gelernt",
                onClick = { vm.navigate(Screen.Collection) }
            )
            RoomRow(
                icon = "grammar",
                title = "Fehler-Werkstatt",
                detail = if (progress.errorCounts.isEmpty()) "Noch keine Fehler" else "${progress.errorCounts.size} Themen",
                onClick = { vm.navigate(Screen.Workshop) }
            )
            RoomRow(
                icon = "link",
                title = "Sprachkarte",
                detail = "Muster & Menschen",
                onClick = { vm.navigate(Screen.Map) }
            )
        }

        if (canDoItems.isNotEmpty()) {
            Spacer(Modifier.height(Spacing.l))
            Text("Was du schon kannst", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.s))
            canDoItems.reversed().forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("✓", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.width(Spacing.s))
                    Text(item.text, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.height(Spacing.xl))
        Text(
            "Für Angehörige",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(Spacing.s))
        BigButton(
            text = "So richtest du die App ein",
            onClick = { vm.navigate(Screen.Guide) },
            secondary = true
        )
        Spacer(Modifier.height(Spacing.xl))
        Text(
            "Alle Daten bleiben auf diesem Gerät. Keine Anmeldung, keine Werbung.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = Spacing.xl)
        )
    }

    if (showGarden) {
        GardenFullscreen(progress = progress, onDismiss = { showGarden = false })
    }

    if (showIntro) {
        AlertDialog(
            onDismissRequest = {
                showIntro = false
                vm.markHomeIntroSeen()
            },
            title = { Text("Das Wichtigste zuerst") },
            text = {
                Text(
                    "Ganz oben steht, was jetzt dran ist – mit einem Knopf. „Dein Weg“ zeigt, wo du " +
                        "gerade bist und was als Nächstes kommt. Was du außerdem machen kannst, steht " +
                        "weiter unten. Verpassen kannst du nichts."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showIntro = false
                    vm.markHomeIntroSeen()
                }) { Text("Verstanden") }
            }
        )
    }
}

/** A quiet line instead of a tile: the rooms are alternatives, not the main road. */
@Composable
private fun RoomRow(
    icon: String,
    title: String,
    detail: String,
    onClick: () -> Unit
) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { contentDescription = "$title. $detail" }
            .padding(vertical = Spacing.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            appIcon(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(Spacing.m))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}
