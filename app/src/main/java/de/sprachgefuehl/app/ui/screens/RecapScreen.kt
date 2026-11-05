package de.sprachgefuehl.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.sprachgefuehl.app.AppUiState
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.Screen
import de.sprachgefuehl.app.domain.Garden
import de.sprachgefuehl.app.domain.LearningOverview
import de.sprachgefuehl.app.domain.Milestones
import de.sprachgefuehl.app.domain.NextAction
import de.sprachgefuehl.app.domain.NextTarget
import de.sprachgefuehl.app.domain.Phrasing
import de.sprachgefuehl.app.domain.Srs
import de.sprachgefuehl.app.ui.components.BigButton
import de.sprachgefuehl.app.ui.components.GardenFullscreen
import de.sprachgefuehl.app.ui.components.GardenPlant
import de.sprachgefuehl.app.ui.components.ScreenHeader
import de.sprachgefuehl.app.ui.components.SoftCard
import de.sprachgefuehl.app.ui.components.StatRow
import de.sprachgefuehl.app.ui.components.plural
import de.sprachgefuehl.app.ui.navigation.LocalTabRoot
import de.sprachgefuehl.app.ui.theme.Spacing
import java.util.Calendar

@Composable
fun RecapScreen(vm: AppViewModel, state: AppUiState) {
    val content = state.content ?: return
    val progress = state.progress
    val now = vm.repository?.now() ?: System.currentTimeMillis()
    val today = now / Srs.DAY_MS
    val days = (0..6).map { offset -> today - 6 + offset }
    val week = progress.sessions.filter { it.day in days }
    val overview = LearningOverview.of(content, progress, now)
    val minutes = overview.week.minutes
    val speaking = overview.week.speakingTasks
    val newChunks = overview.week.newChunks
    val maxMinutes = days.maxOfOrNull { progress.measuredMinutesOn(it) }?.coerceAtLeast(1) ?: 1
    val canDoItems = content.canDo.filter { it.id in progress.canDo }.takeLast(4)
    val milestones = Milestones.earned(progress)
    val goal = overview.week.goal
    val previousMinutes = overview.week.previousMinutes
    val gardenStage = Garden.stage(progress)
    val gardenHeight = when (gardenStage) {
        0 -> 170.dp
        1 -> 210.dp
        2 -> 235.dp
        3 -> 260.dp
        4 -> 285.dp
        else -> 300.dp
    }
    var showGarden by rememberSaveable { mutableStateOf(false) }

    fun go(action: NextAction) {
        when (action.target) {
            NextTarget.LESSON -> {
                val id = action.lessonId
                if (id != null) vm.navigate(Screen.Lesson(id)) else vm.navigate(Screen.Home)
            }
            NextTarget.REVIEW -> vm.navigate(Screen.Review)
            NextTarget.PRACTICE -> vm.navigate(Screen.Practice)
            NextTarget.WORKSHOP -> vm.navigate(Screen.Workshop)
            NextTarget.CHECKUP -> vm.navigate(Screen.Checkup)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = "Dein Sprachgarten",
            subtitle = "Dein Garten, deine Woche und was als Nächstes kommt",
            onBack = if (LocalTabRoot.current) null else ({ vm.back() })
        )
        Column(modifier = Modifier.padding(horizontal = Spacing.l)) {
            SoftCard {
                GardenPlant(
                    stage = gardenStage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gardenHeight)
                )
                Spacer(Modifier.height(Spacing.m))
                Text(
                    Garden.label(gardenStage),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "${plural(progress.sessions.size, "Runde", "Runden")} · " +
                        "${Phrasing.minutes(progress.measuredMinutes.takeIf { it > 0 } ?: progress.totalMinutes)} · " +
                        Phrasing.activeDays(progress.activeDays),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Garden.nextStep(progress)?.let { nextLine ->
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        nextLine,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(Spacing.m))
                BigButton(
                    text = "Garten groß ansehen",
                    onClick = { showGarden = true },
                    secondary = true
                )
            }
            Spacer(Modifier.height(Spacing.m))

            val next = overview.next
            if (next != null) {
                SoftCard {
                    Text(
                        "Als Nächstes",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(next.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(Spacing.xs))
                    Text(next.detail, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        next.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(Spacing.m))
                    BigButton(next.primary.label, { go(next.primary) })
                }
                Spacer(Modifier.height(Spacing.m))
            }

            if (overview.mistakes.isNotEmpty()) {
                SoftCard {
                    Text("Diese Sätze kommen zurück", style = MaterialTheme.typography.titleMedium)
                    overview.mistakes.forEach { mistake ->
                        Spacer(Modifier.height(Spacing.s))
                        Text("„${mistake.target}“", style = MaterialTheme.typography.bodyMedium)
                        val detail = listOfNotNull(
                            mistake.origin.takeIf { it.isNotBlank() }?.let { "Aus „$it“" },
                            mistake.heard.takeIf { it.isNotBlank() }?.let { "damals: „${it.take(40)}“" },
                            mistake.skill?.tab
                        ).joinToString(" · ")
                        if (detail.isNotBlank()) {
                            Text(
                                detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(Spacing.s))
                    Text(
                        "Eine richtige Antwort löst den Eintrag auf.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(Spacing.m))
            }

            if (week.isEmpty() && overview.week.minutes == 0) {
                SoftCard {
                    Text("Diese Woche ist noch ruhig", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(Spacing.s))
                    Text(
                        "Eine Runde reicht. Es gibt nichts nachzuholen und keine Serie, die reißt.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(Spacing.m))
                BigButton("Eine Lektion beginnen", { vm.navigate(Screen.Home) })
            } else {
                SoftCard {
                    StatRow(
                        listOf(
                            "$minutes" to "Minuten",
                            "$speaking" to "Sätze gesprochen",
                            "${week.size}" to if (week.size == 1) "Runde" else "Runden"
                        )
                    )
                    Spacer(Modifier.height(Spacing.s))
                    Text(
                        if (newChunks > 0) {
                            "${plural(newChunks, "Baustein", "Bausteine")} kamen diese Woche dazu."
                        } else {
                            "Du hast vor allem wiederholt – auch das ist Fortschritt."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        "Dein Ziel: $goal Minuten pro Tag.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        plural(overview.week.activeDays, "aktiver Tag", "aktive Tage") +
                            " in dieser Woche.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (previousMinutes > 0) {
                        Text(
                            "Die Woche davor: $previousMinutes Minuten.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.m))
                SoftCard {
                    Text("Deine Tage · Minuten", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(Spacing.m))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                    ) {
                        days.forEach { day ->
                            val dayMinutes = progress.measuredMinutesOn(day)
                            val isToday = day == today
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    if (dayMinutes > 0) "$dayMinutes" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(Spacing.xs))
                                // Fixed height, bottom aligned: all seven bars share one baseline.
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(
                                                if (dayMinutes == 0) 8.dp
                                                else (12 + 68 * (dayMinutes.toFloat() / maxMinutes)).dp
                                            )
                                            .background(
                                                color = when {
                                                    dayMinutes == 0 -> MaterialTheme.colorScheme.surfaceVariant
                                                    else -> MaterialTheme.colorScheme.primary
                                                },
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                    )
                                }
                                Spacer(Modifier.height(Spacing.xs))
                                Text(
                                    weekdayLabel(day),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isToday) FontWeight.Bold else null,
                                    color = if (isToday) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(Spacing.s))
                    Text(
                        "Kein Tag ist Pflicht. Die Balken zeigen nur, was war.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (canDoItems.isNotEmpty()) {
                    Spacer(Modifier.height(Spacing.m))
                    SoftCard {
                        Text("Was du jetzt kannst", style = MaterialTheme.typography.titleMedium)
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
                }

                Spacer(Modifier.height(Spacing.m))
                SoftCard {
                    Text("Deine Stimme", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(Spacing.s))
                    Text(
                        if (progress.recordings.isEmpty()) {
                            "Noch keine Aufnahme. In „Meine Sätze“ nimmst du dich auf und vergleichst später."
                        } else {
                            "Du hast ${plural(progress.recordings.size, "Aufnahme", "Aufnahmen")}. Höre die erste und die letzte nebeneinander."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(Spacing.s))
                    BigButton(
                        text = "Zu meinen Sätzen",
                        onClick = { vm.navigate(Screen.Collection) },
                        secondary = true
                    )
                }

                if (milestones.isNotEmpty()) {
                    Spacer(Modifier.height(Spacing.m))
                    SoftCard {
                        Text("Meilensteine", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(Spacing.s))
                        milestones.takeLast(3).reversed().forEach { milestone ->
                            Text(
                                "· ${milestone.title}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.height(Spacing.xs))
                        Text(
                            "${milestones.size} von ${Milestones.all.size} – keiner geht verloren.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.m))
                SoftCard {
                    Text("Deine letzten Runden", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(Spacing.s))
                    week.takeLast(5).reversed().forEach { session ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                weekdayLabel(session.day),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(52.dp)
                            )
                            Text(
                                "${Phrasing.minutes(session.minutes)} · ${Phrasing.spokenTasks(session.speakingTasks)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.s))
            Text(
                "Der Rückblick zählt nur, was du getan hast. Er vergleicht dich mit niemandem.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(Spacing.m)
            )
            Spacer(Modifier.height(Spacing.xl))
        }
    }

    if (showGarden) {
        GardenFullscreen(progress = progress, onDismiss = { showGarden = false })
    }
}

private fun weekdayLabel(day: Long): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = day * Srs.DAY_MS }
    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Mo"
        Calendar.TUESDAY -> "Di"
        Calendar.WEDNESDAY -> "Mi"
        Calendar.THURSDAY -> "Do"
        Calendar.FRIDAY -> "Fr"
        Calendar.SATURDAY -> "Sa"
        else -> "So"
    }
}
