package de.sprachgefuehl.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.sprachgefuehl.app.AppUiState
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.Screen
import de.sprachgefuehl.app.domain.Checkup
import de.sprachgefuehl.app.domain.DailyPlan
import de.sprachgefuehl.app.domain.DayMood
import de.sprachgefuehl.app.domain.FocusTarget
import de.sprachgefuehl.app.domain.Garden
import de.sprachgefuehl.app.domain.PathProgress
import de.sprachgefuehl.app.domain.Personalization
import de.sprachgefuehl.app.domain.Phrasing
import de.sprachgefuehl.app.domain.Srs
import de.sprachgefuehl.app.domain.VariantPicker
import de.sprachgefuehl.app.ui.components.BigButton
import de.sprachgefuehl.app.ui.components.DailyRing
import de.sprachgefuehl.app.ui.components.GardenSummary
import de.sprachgefuehl.app.ui.components.LinkCard
import de.sprachgefuehl.app.ui.components.ProfileAvatar
import de.sprachgefuehl.app.ui.components.SoftCard
import de.sprachgefuehl.app.ui.components.RingSummary
import de.sprachgefuehl.app.ui.components.StatRow
import de.sprachgefuehl.app.ui.components.appIcon
import de.sprachgefuehl.app.ui.components.plural
import de.sprachgefuehl.app.ui.theme.LocalSprachColors
import de.sprachgefuehl.app.ui.theme.Spacing
import java.util.Calendar

@Composable
fun HomeScreen(vm: AppViewModel, state: AppUiState) {
    val content = state.content ?: return
    val progress = state.progress
    val profile = progress.profile
    val now = vm.repository?.now() ?: System.currentTimeMillis()
    val today = now / Srs.DAY_MS
    val minutesToday = progress.minutesOn(today)
    val goal = if (profile.dailyMinutes > 0) profile.dailyMinutes else 10
    val suggestion = Personalization.suggestion(content, progress)
    val next = suggestion?.lesson
    val situation = next?.let { lesson -> content.situations.firstOrNull { it.id == lesson.situationId } }
    val variant = situation?.let { current ->
        VariantPicker.resolve(
            situation = current,
            seed = progress.seed,
            interests = progress.profile.interests,
            chosenId = progress.variantIds[current.id]
        )
    }
    val encounter = variant?.personaId?.takeIf { it.isNotBlank() }
        ?.let { id -> content.personas.firstOrNull { it.id == id } }
        ?: suggestion?.encounter
    val story = variant?.story?.takeIf { it.isNotBlank() } ?: situation?.story.orEmpty()
    val due = progress.chunkMemory.count { Srs.isDue(it.value, now) }
    val gardenStage = Garden.stage(progress)
    val day = DailyPlan.summary(progress, today)
    // The position comes from the lesson that is actually offered, not from a second opinion.
    val nextPath = situation?.let { current -> content.paths.firstOrNull { it.id == current.pathId } }
    val nextPosition = if (situation != null && nextPath != null) {
        val index = content.situations
            .filter { it.pathId == nextPath.id }
            .sortedBy { it.order }
            .indexOfFirst { it.id == situation.id }
        if (index < 0) "" else "Situation ${index + 1} von ${content.situations.count { it.pathId == nextPath.id }}"
    } else ""
    val afterNext = situation?.let { PathProgress.nextInPath(content, progress, it.id) }
    val resume = PathProgress.resumeLesson(content, progress)
        ?.takeIf { (lesson, _) -> lesson.id != next?.id }
    // Ist genau diese Begegnung schon angefangen, sagt die Karte das auch. Sonst liest sie sich
    // wie etwas Neues mit 13 Schritten, obwohl es bei Schritt 7 weitergeht.
    val nextStep = next?.let { progress.lessonProgress[it.id] ?: 0 } ?: 0
    val nextIsStarted = next != null && nextStep in 1 until next.steps.size &&
        next.id !in progress.completedLessons
    val canDoItems = content.canDo.filter { it.id in progress.canDo }.takeLast(3)
    val focus = vm.focusBlock()
    val checkupOpen = progress.onboarded && progress.checkup.startedAt > 0L && !Checkup.isFinished(progress.checkup)
    val hour = remember {
        Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    }
    val greeting = Personalization.greeting(profile.greetingName, hour)

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

        SoftCard {
            RingSummary(
                progress = if (goal > 0) minutesToday.toFloat() / goal.toFloat() else 0f,
                top = "$minutesToday",
                bottom = "von $goal",
                title = when (day.mood) {
                    DayMood.DONE -> "Für heute reicht es"
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
            if (day.mood == DayMood.DONE) {
                Spacer(Modifier.height(Spacing.s))
                BigButton(
                    text = "Noch fünf Minuten",
                    onClick = { vm.navigate(Screen.Practice) },
                    secondary = true
                )
            }
        }

        Spacer(Modifier.height(Spacing.m))

        SoftCard {
            GardenSummary(
                stage = gardenStage,
                title = "Dein Sprachgarten",
                stageLabel = Garden.label(gardenStage),
                line = "${plural(progress.sessions.size, "Runde", "Runden")} · ${Phrasing.minutes(progress.totalMinutes)}"
            )
        }

        if (checkupOpen) {
            Spacer(Modifier.height(Spacing.m))
            SoftCard(onClick = { vm.navigate(Screen.Checkup) }) {
                LinkCard(
                    title = "Standort bestimmen",
                    description = "Du warst bei Teil ${progress.checkup.blockIndex + 1} von ${Checkup.blockCount()} – mach dort weiter, wo du aufgehört hast.",
                    action = "Weiter"
                )
            }
        }

        Spacer(Modifier.height(Spacing.l))

        if (next != null && situation != null) {
            SoftCard {
                Text("Nächste Begegnung", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "${situation.title}: ${next.title}",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(next.goal, style = MaterialTheme.typography.bodyMedium)
                if (story.isNotBlank()) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        story,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (nextPath != null && nextPosition.isNotBlank()) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        "${nextPath.title} · $nextPosition",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (encounter != null) {
                    Spacer(Modifier.height(Spacing.s))
                    Text(
                        "Mit ${encounter.name} · ${encounter.role}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (!suggestion.why.isBlank()) {
                    Text(
                        suggestion.why,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(Spacing.s))
                Text(
                    if (nextIsStarted) {
                        "Weiter bei Schritt ${nextStep + 1} von ${next.steps.size} · ca. ${next.xpMinutes} Minuten"
                    } else {
                        "ca. ${next.xpMinutes} Minuten · ${next.steps.size} Schritte"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (afterNext != null) {
                    Text(
                        "Danach: ${afterNext.situation.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(Spacing.m))
                BigButton(
                    text = if (nextIsStarted) "Weitermachen" else "Weiterlernen",
                    onClick = { vm.navigate(Screen.Lesson(next.id)) },
                    icon = appIcon(situation.icon)
                )
            }
        } else {
            SoftCard {
                Text("Alle Begegnungen geschafft", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(Spacing.s))
                Text(
                    "Wiederhole deine Sätze, bis sie sicher sitzen.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(Spacing.m))
                BigButton("Wiederholen", { vm.navigate(Screen.Review) })
            }
        }

        if (focus != null) {
            Spacer(Modifier.height(Spacing.m))
            SoftCard {
                Text(
                    "Heute passt zu dir",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(focus.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    focus.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.s))
                BigButton(
                    text = "Jetzt üben",
                    onClick = {
                        when (focus.target) {
                            FocusTarget.PRACTICE -> vm.navigate(Screen.Practice)
                            FocusTarget.REVIEW -> vm.navigate(Screen.Review)
                            FocusTarget.WORKSHOP -> vm.navigate(Screen.Workshop)
                            FocusTarget.LESSON -> {
                                val target = vm.nextLesson()
                                if (target != null) vm.navigate(Screen.Lesson(target.id)) else vm.navigate(Screen.Practice)
                            }
                        }
                    },
                    secondary = true
                )
            }
        }

        if (resume != null) {
            val (resumeLesson, step) = resume
            Spacer(Modifier.height(Spacing.m))
            SoftCard(onClick = { vm.navigate(Screen.Lesson(resumeLesson.id)) }) {
                Text("Du warst hier", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "${resumeLesson.title} · weiter bei Schritt ${step + 1} von ${resumeLesson.steps.size}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "Weitermachen, wo du aufgehört hast.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (due > 0) {
            Spacer(Modifier.height(Spacing.m))
            SoftCard(onClick = { vm.navigate(Screen.Review) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Wiederholen", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (due == 1) "1 Satz wartet auf dich" else "$due Sätze warten auf dich",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = LocalSprachColors.current.leafSoft,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Text("$due", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        val week = progress.sessions.filter { it.day >= today - 6 }
        if (week.isNotEmpty()) {
            Spacer(Modifier.height(Spacing.m))
            SoftCard {
                Text("Diese Woche", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.s))
                StatRow(
                    listOf(
                        week.sumOf { it.minutes }.toString() to "Minuten",
                        week.sumOf { it.speakingTasks }.toString() to "Sätze gesprochen",
                        week.size.toString() to "Runden"
                    )
                )
                Spacer(Modifier.height(Spacing.s))
                BigButton(
                    text = "Wochenrückblick",
                    onClick = { vm.navigate(Screen.Recap) },
                    secondary = true
                )
            }
        }

        Spacer(Modifier.height(Spacing.l))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            HomeTile(
                title = "Pfade",
                subtitle = "${content.paths.size} Bereiche",
                icon = "map",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = { vm.navigate(Screen.Paths) }
            )
            HomeTile(
                title = "Üben",
                subtitle = "Diktat & Sprechen",
                icon = "chat",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = { vm.navigate(Screen.Practice) }
            )
        }
        Spacer(Modifier.height(Spacing.m))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            HomeTile(
                title = "Sprachkarte",
                subtitle = "Muster & Menschen",
                icon = "link",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = { vm.navigate(Screen.Map) }
            )
            HomeTile(
                title = "Meine Sätze",
                subtitle = "${progress.chunkMemory.size} gelernt",
                icon = "star",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = { vm.navigate(Screen.Collection) }
            )
        }
        Spacer(Modifier.height(Spacing.m))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            HomeTile(
                title = "Fehler-Werkstatt",
                subtitle = if (progress.errorCounts.isEmpty()) "Noch keine Fehler" else "${progress.errorCounts.size} Themen",
                icon = "grammar",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = { vm.navigate(Screen.Workshop) }
            )
            HomeTile(
                title = "Rückblick",
                subtitle = "Deine Woche",
                icon = "leaf",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = { vm.navigate(Screen.Recap) }
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
}

@Composable
private fun HomeTile(
    title: String,
    subtitle: String,
    icon: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SoftCard(modifier = modifier, onClick = onClick) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(44.dp)
        ) {
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(appIcon(icon), contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(Spacing.s))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
