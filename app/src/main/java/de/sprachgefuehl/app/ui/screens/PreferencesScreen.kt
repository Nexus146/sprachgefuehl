package de.sprachgefuehl.app.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.sprachgefuehl.app.AppUiState
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.domain.Personalization
import de.sprachgefuehl.app.domain.Placement
import de.sprachgefuehl.app.domain.ProfileCatalog
import de.sprachgefuehl.app.ui.components.ProfileAvatar
import de.sprachgefuehl.app.ui.components.ScreenHeader
import de.sprachgefuehl.app.ui.components.SoftCard
import de.sprachgefuehl.app.ui.components.Tag
import de.sprachgefuehl.app.ui.theme.Spacing
import kotlinx.coroutines.delay

/**
 * Everything the learner told us at the start in one place. The profile overview only summarises
 * it; the questions themselves live here, apart from the technical settings and the achievements.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreferencesScreen(vm: AppViewModel, state: AppUiState) {
    val progress = state.progress
    val profile = progress.profile
    var name by rememberSaveable { mutableStateOf(profile.name) }

    LaunchedEffect(name) {
        if (name != profile.name) {
            delay(350)
            vm.updateProfile { it.copy(name = name.trim().take(24)) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = "Deine Angaben",
            subtitle = "Name, Ziel und Lerntage",
            onBack = { vm.back() }
        )
        Column(modifier = Modifier.padding(horizontal = Spacing.l)) {
            Text(
                "Das sind die Fragen vom ersten Start. Sie bestimmen nur, was dir zuerst " +
                    "vorgeschlagen wird – gesperrt ist nichts. Du kannst alles jederzeit ändern.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(Spacing.l))
            SoftCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar(profile = profile.copy(name = name))
                    Spacer(Modifier.width(Spacing.m))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.take(24) },
                        label = { Text("Dein Name") },
                        placeholder = { Text("z. B. Ingrid") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(Spacing.s))
                Text(
                    if (name.isBlank()) {
                        "Ohne Namen duzt dich die App einfach – das ist völlig in Ordnung."
                    } else {
                        "So begrüßt dich die App: „${Personalization.greeting(name, 9)}“"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(Spacing.l))
            Text("Warum lernst du Englisch?", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "Das bestimmt, welche Begegnungen zuerst vorgeschlagen werden – der Weg bleibt offen.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.s))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                verticalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                ProfileCatalog.reasons.forEach { choice ->
                    Tag(
                        text = choice.label,
                        active = profile.reasonId == choice.id,
                        onClick = {
                            vm.updateProfile {
                                it.copy(reasonId = if (it.reasonId == choice.id) "" else choice.id)
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(Spacing.l))
            Text("Was interessiert dich?", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.xs))
            Text(
                if (profile.interests.isEmpty()) {
                    "Ohne Auswahl kommst du der Reihe nach durch alle Situationen."
                } else {
                    "Diese Themen bekommen in den Geschichten den Vorrang."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.s))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                verticalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                ProfileCatalog.interests.forEach { choice ->
                    Tag(
                        text = choice.label,
                        active = choice.id in profile.interests,
                        onClick = {
                            vm.updateProfile {
                                val next = if (choice.id in it.interests) it.interests - choice.id else it.interests + choice.id
                                it.copy(interests = next)
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(Spacing.l))
            Text("Dein Tagesziel", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.s))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                ProfileCatalog.goals.forEach { minutes ->
                    Tag(
                        text = "$minutes Minuten",
                        active = profile.dailyMinutes == minutes,
                        onClick = { vm.updateProfile { it.copy(dailyMinutes = minutes) } }
                    )
                }
            }
            Spacer(Modifier.height(Spacing.s))
            Text(
                "Zehn Minuten an vier Tagen bringen mehr als eine lange Sitzung am Sonntag.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(Spacing.l))
            Text("Deine Lerntage", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.s))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                ProfileCatalog.weekdayLabels.forEachIndexed { index, label ->
                    val day = index + 1
                    Tag(
                        text = label,
                        active = day in profile.weekdays,
                        onClick = {
                            vm.updateProfile {
                                val next = if (day in it.weekdays) it.weekdays - day else it.weekdays + day
                                if (next.isEmpty()) it else it.copy(weekdays = next)
                            }
                        }
                    )
                }
            }
            Spacer(Modifier.height(Spacing.s))
            Text(
                "Ein Vorschlag, keine Pflicht. Die App erinnert nicht und zählt keine Serien.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(Spacing.l))
            Text("Dein Startpunkt", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "Aus der Standortbestimmung. Du kannst ihn hier auch von Hand verschieben.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.s))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                verticalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                (0..2).forEach { level ->
                    Tag(
                        text = Placement.levelLabel(level),
                        active = progress.placementLevel == level,
                        onClick = { vm.updatePlacementLevel(level) }
                    )
                }
            }
            Spacer(Modifier.height(Spacing.s))
            Text(
                "Das ändert Tempo, Hinweise und Stützung – nicht die Inhalte. Jede Lektion bleibt offen.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(Spacing.xl))
            Text(
                "Änderungen gelten sofort. Der Startpunkt stammt aus der Standortbestimmung beim " +
                    "ersten Start; wie sich die fünf Bereiche seither entwickelt haben, steht im Sprachprofil.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.xl))
        }
    }
}
