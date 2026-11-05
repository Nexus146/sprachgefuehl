package de.sprachgefuehl.app.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.sprachgefuehl.app.AppUiState
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.Screen
import de.sprachgefuehl.app.data.ContentBundle
import de.sprachgefuehl.app.domain.JourneyPlanner
import de.sprachgefuehl.app.domain.PathProgress
import de.sprachgefuehl.app.domain.Personalization
import de.sprachgefuehl.app.domain.SessionBuilder
import de.sprachgefuehl.app.domain.VariantPicker
import de.sprachgefuehl.app.domain.SituationState
import de.sprachgefuehl.app.domain.SituationStatus
import de.sprachgefuehl.app.ui.components.ScreenHeader
import de.sprachgefuehl.app.ui.components.SoftCard
import de.sprachgefuehl.app.ui.components.appIcon
import de.sprachgefuehl.app.ui.theme.Sizes
import de.sprachgefuehl.app.ui.theme.Spacing

/**
 * The course as one line, not as a catalogue: the current chapter is open, every other chapter is
 * a single row with its progress. Nothing is hidden – it is only out of the way until asked for.
 */
@Composable
fun PathsScreen(vm: AppViewModel, state: AppUiState) {
    val content = state.content ?: return
    val progress = state.progress
    val paths = PathProgress.paths(content, progress)
    val nextUp = PathProgress.nextUp(content, progress)
    val recommended = nextUp?.situation?.id
    val journey = JourneyPlanner.of(content, progress)
    var expanded by rememberSaveable { mutableStateOf(nextUp?.situation?.pathId.orEmpty()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = "Dein Weg",
            subtitle = journey?.let { "${it.doneLessons} von ${it.totalLessons} Lektionen geschafft" }
                ?: content.world.intro,
            onBack = { vm.back() }
        )
        Column(modifier = Modifier.padding(horizontal = Spacing.l)) {
            Text(
                if (journey != null) {
                    "${journey.chapterLine} ist offen. Tippe ein anderes Kapitel an, um es zu öffnen."
                } else {
                    "Ein Kapitel nach dem anderen."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.m))
            paths.forEach { path ->
                val situations = path.situations
                if (situations.isEmpty()) return@forEach
                val open = path.path.id == expanded
                if (!open) {
                    SoftCard(
                        modifier = Modifier.padding(bottom = Spacing.s),
                        onClick = { expanded = path.path.id }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        appIcon(path.path.icon),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(Spacing.m))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(path.path.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${path.done} von ${path.total} Lektionen" + if (path.finished) " · geschafft" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (path.finished) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = "geschafft",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Icon(
                                    Icons.Rounded.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                } else {
                    SoftCard(modifier = Modifier.padding(bottom = Spacing.m)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        appIcon(path.path.icon),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(Spacing.m))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(path.path.title, style = MaterialTheme.typography.titleLarge)
                                Text(
                                    "${path.done} von ${path.total} Lektionen · ${path.path.subtitle}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(Spacing.s))
                        LinearProgressIndicator(
                            progress = { path.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            drawStopIndicator = {}
                        )
                        Spacer(Modifier.height(Spacing.m))
                        situations.forEach { status ->
                            SituationRow(
                                status = status,
                                season = content.world.seasons.firstOrNull { it.id == status.situation.season }?.title?.substringBefore(" –") ?: "",
                                recommended = status.situation.id == recommended,
                                onClick = { vm.navigate(Screen.Situation(status.situation.id)) }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SituationRow(
    status: SituationStatus,
    season: String,
    recommended: Boolean,
    onClick: () -> Unit
) {
    val situation = status.situation
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // FlowRow: bei großer Schrift rutscht die Marke unter den Titel, statt ihn zu
            // quetschen und selbst in zwei Zeilen zu brechen.
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(situation.title, style = MaterialTheme.typography.titleMedium)
                if (recommended) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            "Als Nächstes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                listOf(status.label, situation.place, season)
                    .filter { it.isNotBlank() }
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (status.nextLesson != null) {
                Text(
                    "Nächstes: ${status.nextLesson.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        when (status.state) {
            SituationState.DONE ->
                Icon(Icons.Rounded.CheckCircle, contentDescription = "abgeschlossen", tint = MaterialTheme.colorScheme.primary)
            else ->
                Icon(Icons.Rounded.PlayArrow, contentDescription = "öffnen", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SituationScreen(vm: AppViewModel, state: AppUiState, situationId: String) {
    val content = state.content ?: return
    val situation = content.situations.firstOrNull { it.id == situationId } ?: return
    val path = content.paths.firstOrNull { it.id == situation.pathId }
    val lessons = content.lessons.filter { it.situationId == situation.id }.sortedBy { it.order }
    val progress = state.progress
    val next = SessionBuilder.nextLessonOf(content, progress, situationId)
    val status = PathProgress.of(content, progress, situationId)
    val following = PathProgress.nextInPath(content, progress, situationId)
    // The story this learner meets. Pure read: the choice is stored when the lesson starts.
    val variant = VariantPicker.resolve(
        situation = situation,
        seed = progress.seed,
        tags = Personalization.storyTagsFor(progress.profile),
        chosenId = progress.variantIds[situation.id]
    )
    val story = variant?.story?.takeIf { it.isNotBlank() } ?: situation.story
    val scene = variant?.scene?.takeIf { it.isNotBlank() } ?: situation.scene
    val place = variant?.place?.takeIf { it.isNotBlank() } ?: situation.place
    val personaIds = if (variant != null && variant.personaId.isNotBlank()) listOf(variant.personaId) else situation.personaIds

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = situation.title,
            subtitle = path?.title,
            onBack = { vm.back() }
        )
        Column(modifier = Modifier.padding(horizontal = Spacing.l)) {
            SoftCard {
                Text(scene, style = MaterialTheme.typography.bodyLarge)
                if (story.isNotBlank()) {
                    Spacer(Modifier.height(Spacing.s))
                    Text(
                        story,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(Spacing.s))
                Text(
                    "Ziel: ${situation.subtitle}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val season = content.world.seasons.firstOrNull { it.id == situation.season }
                if (season != null || place.isNotBlank()) {
                    Spacer(Modifier.height(Spacing.s))
                    Text(
                        listOf(season?.title.orEmpty(), place).filter { it.isNotBlank() }.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                val people = content.personas.filter { it.id in personaIds }
                if (people.isNotEmpty()) {
                    Spacer(Modifier.height(Spacing.m))
                    Text("Du triffst", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    people.forEach { persona ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("· ${persona.name}", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.width(Spacing.s))
                            Text(
                                persona.role,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            if (status?.state == SituationState.DONE) {
                Spacer(Modifier.height(Spacing.m))
                SoftCard {
                    Text("Diese Situation ist geschafft", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        "${status.done} von ${status.total} Lektionen. Du kannst jede davon wiederholen – " +
                            "oder weitergehen, wenn du magst.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (following != null) {
                        Spacer(Modifier.height(Spacing.s))
                        de.sprachgefuehl.app.ui.components.BigButton(
                            text = "Weiter zu ${following.situation.title}",
                            onClick = { vm.navigate(Screen.Situation(following.situation.id)) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.l))
            Text("Lektionen", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.s))
            lessons.forEach { lesson ->
                val done = lesson.id in progress.completedLessons
                val step = progress.lessonProgress[lesson.id] ?: 0
                val started = step > 0 && !done
                SoftCard(
                    modifier = Modifier.padding(bottom = Spacing.s),
                    onClick = { vm.navigate(Screen.Lesson(lesson.id)) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (done) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(22.dp))
                                } else {
                                    Text("${lesson.order}", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                        Spacer(Modifier.width(Spacing.m))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(lesson.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                lesson.goal,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (started) {
                                Spacer(Modifier.height(Spacing.xs))
                                LinearProgressIndicator(
                                    progress = { step.toFloat() / lesson.steps.size.coerceAtLeast(1) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.width(Spacing.s))
                        Text(
                            when {
                                done -> "Fertig"
                                started -> "Weiter"
                                else -> "Neu"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (next != null) {
                Spacer(Modifier.height(Spacing.m))
                de.sprachgefuehl.app.ui.components.BigButton(
                    text = if (next.id in progress.completedLessons) "Lektion wiederholen" else "Weiterlernen",
                    onClick = { vm.navigate(Screen.Lesson(next.id)) }
                )
            }
            if (following != null) {
                Spacer(Modifier.height(Spacing.s))
                Text(
                    "Danach: ${following.situation.title} · ${following.situation.subtitle}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

@Suppress("unused")
private fun lessonCount(content: ContentBundle, situationId: String): Int =
    content.lessons.count { it.situationId == situationId }
