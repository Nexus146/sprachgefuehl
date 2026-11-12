package de.sprachgefuehl.app.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import de.sprachgefuehl.app.domain.Garden
import de.sprachgefuehl.app.domain.Milestones
import de.sprachgefuehl.app.domain.ProfileCatalog
import de.sprachgefuehl.app.domain.SkillCatalog
import de.sprachgefuehl.app.domain.Skills
import de.sprachgefuehl.app.domain.Srs
import de.sprachgefuehl.app.domain.state
import de.sprachgefuehl.app.ui.components.BigButton
import de.sprachgefuehl.app.ui.components.DividerLine
import de.sprachgefuehl.app.ui.components.GardenFullscreen
import de.sprachgefuehl.app.ui.components.GardenSummary
import de.sprachgefuehl.app.ui.components.ProfileAvatar
import de.sprachgefuehl.app.ui.components.ScreenHeader
import de.sprachgefuehl.app.ui.components.SoftCard
import de.sprachgefuehl.app.ui.components.appIcon
import de.sprachgefuehl.app.ui.theme.Spacing
import de.sprachgefuehl.app.ui.components.LinkCard

/**
 * The overview. It shows what has grown, and it links to the two places that own the details:
 * "Deine Angaben" for everything the learner said about themselves, settings for the app itself.
 */
@Composable
fun ProfileScreen(vm: AppViewModel, state: AppUiState) {
    val progress = state.progress
    val profile = progress.profile
    val dueReviews = progress.chunkMemory.count { Srs.isDue(it.value, System.currentTimeMillis()) }
    val weakest = Skills.weakest(progress.skills)
    val weakestStage = weakest?.let { SkillCatalog.stage(progress.skills.state(it).rating) }
    var showGarden by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = "Dein Profil",
            subtitle = "Was die App über dich wissen darf",
            trailing = {
                IconButton(
                    onClick = { vm.navigate(Screen.Settings) },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Rounded.Settings,
                        contentDescription = "Einstellungen",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
        Column(modifier = Modifier.padding(horizontal = Spacing.l)) {
            SoftCard(onClick = { vm.navigate(Screen.Preferences) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar(profile = profile)
                    Spacer(Modifier.width(Spacing.m))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            profile.greetingName.ifBlank { "Ohne Namen" },
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            ProfileCatalog.summary(profile),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val interests = ProfileCatalog.interestLabels(profile.interests)
                        if (interests.isNotEmpty()) {
                            Text(
                                "Interessen: " + interests.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "Name, Ziel und Lerntage anpassen",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(Spacing.m))
            SoftCard(onClick = { vm.navigate(Screen.SkillProfile) }) {
                LinkCard(
                    title = "Dein Sprachprofil",
                    description = if (weakest != null && weakestStage != null) {
                        "Am meisten wächst gerade: ${weakest.tab} – " +
                            SkillCatalog.stageLabel(weakestStage).lowercase()
                    } else {
                        "Fünf Bereiche: Hören, Grammatik, Bausteine, Sprechen, Verknüpfen"
                    },
                    action = "Ansehen"
                )
            }

            Spacer(Modifier.height(Spacing.m))
            SoftCard(onClick = { vm.navigate(Screen.Recap) }) {
                LinkCard(
                    title = "Dein Fortschritt",
                    description = "Garten, Woche und was als Nächstes kommt",
                    action = "Ansehen"
                )
            }

            Spacer(Modifier.height(Spacing.m))
            SoftCard(onClick = { vm.navigate(Screen.Settings) }) {
                LinkCard(
                    title = "Neues Handy?",
                    description = "Fortschritt als Datei sichern und mitnehmen",
                    action = "Sichern"
                )
            }

            Spacer(Modifier.height(Spacing.l))
            Text("Dein Stand", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.s))
            SoftCard {
                StatLine("Geschaffene Lektionen", "${progress.completedLessons.size} von ${state.content?.lessons?.size ?: 0}")
                StatLine("Sätze in deiner Sammlung", "${progress.chunkMemory.size}")
                StatLine("Sprechaufgaben", "${progress.totalSpeakingTasks}")
                StatLine(
                    "Gemessene Lernzeit",
                    "${progress.measuredMinutes.takeIf { it > 0 } ?: progress.totalMinutes} Minuten"
                )
                StatLine("Garten", Garden.label(Garden.stage(progress)))
                StatLine("Fällige Wiederholungen", "$dueReviews")
            }

            Spacer(Modifier.height(Spacing.l))
            SoftCard {
                GardenSummary(
                    stage = Garden.stage(progress),
                    title = "Dein Sprachgarten",
                    stageLabel = Garden.label(Garden.stage(progress)),
                    line = "${progress.sessions.size} Runden · ${Garden.nextStep(progress) ?: "voll gewachsen"}"
                )
                Spacer(Modifier.height(Spacing.s))
                BigButton(
                    text = "Garten groß ansehen",
                    onClick = { showGarden = true },
                    secondary = true
                )
            }

            Spacer(Modifier.height(Spacing.l))
            SoftCard {
                Text("Deine Meilensteine", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.s))
                val earned = Milestones.earned(progress)
                if (earned.isEmpty()) {
                    Text(
                        "Der erste Meilenstein wartet auf die erste Runde.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    earned.forEach { milestone ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                appIcon(milestone.icon),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(Spacing.s))
                            Column(modifier = Modifier.weight(1f)) {
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
                val missing = Milestones.all.size - earned.size
                if (missing > 0) {
                    Spacer(Modifier.height(Spacing.s))
                    Text(
                        "Noch ${if (missing == 1) "ein Meilenstein" else "$missing Meilensteine"} – keiner kann verloren gehen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(Spacing.l))
            DividerLine()
            Spacer(Modifier.height(Spacing.m))
            Text(
                "Alles hier bleibt auf diesem Gerät, solange du kein Konto verbindest. " +
                    "Ein Konto ist optional; Sichern und Zurücksetzen findest du in den Einstellungen.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.s))
            BigButton(
                text = "Einstellungen öffnen",
                onClick = { vm.navigate(Screen.Settings) },
                icon = Icons.Rounded.Settings,
                secondary = true
            )
            Spacer(Modifier.height(Spacing.xl))
        }
    }

    if (showGarden) {
        GardenFullscreen(progress = progress, onDismiss = { showGarden = false })
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    }
}
