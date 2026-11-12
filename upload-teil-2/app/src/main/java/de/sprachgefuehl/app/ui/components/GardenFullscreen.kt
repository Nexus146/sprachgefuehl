package de.sprachgefuehl.app.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.domain.Garden
import de.sprachgefuehl.app.domain.Phrasing
import de.sprachgefuehl.app.ui.theme.Sizes
import de.sprachgefuehl.app.ui.theme.Spacing

/** Der Garten in voller Größe: genug Platz, um wirklich etwas wachsen zu sehen. */
@Composable
fun GardenFullscreen(progress: Progress, onDismiss: () -> Unit) {
    val stage = Garden.stage(progress)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.l)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Dein Sprachgarten", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(56.dp)) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Garten schließen",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.m))
                GardenPlant(
                    stage = stage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                )
                Spacer(Modifier.height(Spacing.m))
                Text(
                    Garden.label(stage),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "${Phrasing.minutes(progress.totalMinutes)} insgesamt · " +
                        "${Phrasing.activeDays(progress.activeDays)} · " +
                        "${progress.sessions.size} ${if (progress.sessions.size == 1) "Runde" else "Runden"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Garden.nextStep(progress)?.let { next ->
                    Spacer(Modifier.height(Spacing.s))
                    Text(next, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(Spacing.l))
                Text("Deine fünf Beete", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "Jedes Beet gehört zu einem Bereich und wächst, wenn du dort sicherer wirst.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.s))
                Garden.beds(progress).forEach { bed ->
                    Surface(
                        shape = RoundedCornerShape(Sizes.cornerSmall),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Spacing.s)
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.m),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GardenPlot(
                                bed = bed,
                                modifier = Modifier.size(width = 52.dp, height = 64.dp)
                            )
                            Spacer(Modifier.width(Spacing.m))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${bed.title} · ${bed.plant.name}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    bed.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (bed.observed) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                bed.nextLine?.let { line ->
                                    Text(
                                        line,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(Spacing.m))
                Text("Gartenbewohner", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "Sie kommen, wenn ein Bereich blüht oder ein Stück Garten steht.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.s))
                Garden.residents(progress).forEach { resident ->
                    Surface(
                        shape = RoundedCornerShape(Sizes.cornerSmall),
                        color = if (resident.present) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Spacing.s)
                    ) {
                        Column(Modifier.padding(Spacing.m)) {
                            Text(
                                resident.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (resident.present) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                if (resident.present) "ist da" else resident.line,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (resident.present) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(Spacing.m))
                Text("Im Garten", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "Mit jeder Runde kommt ein Stück dazu – auch nach einer Pause bleibt alles stehen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.s))
                Garden.features(progress).forEach { feature ->
                    Surface(
                        shape = RoundedCornerShape(Sizes.cornerSmall),
                        color = if (feature.reached) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Spacing.s)
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.m),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                feature.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (feature.reached) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                if (feature.reached) "steht"
                                else "ab ${feature.fromRounds} " +
                                    if (feature.fromRounds == 1) "Runde" else "Runden",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (feature.reached) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Garden.nextFeature(progress)?.let { next ->
                    Spacer(Modifier.height(Spacing.xs))
                    Text(next, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(Spacing.m))
                Text("Zuletzt im Garten", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.xs))
                val notes = Garden.chronicle(progress)
                if (notes.isEmpty()) {
                    Text(
                        "Sobald du eine Runde machst, steht hier, was gewachsen ist.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    notes.forEach { note ->
                        Text(note, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(Spacing.xs))
                    }
                }
                Spacer(Modifier.height(Spacing.l))
                Text("Die fünf Stufen", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.s))
                (0..5).forEach { step ->
                    val reached = stage >= step
                    Surface(
                        shape = RoundedCornerShape(Sizes.cornerSmall),
                        color = if (reached) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Spacing.s)
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.m),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                Garden.label(step),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (reached) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (reached) {
                                Text(
                                    "erreicht",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(Spacing.m))
                Text(
                    "Der Garten wächst mit jeder Runde und verwelkt nie. Auch nach einer Pause " +
                        "ist alles noch da.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.l))
                BigButton("Zurück", onDismiss, secondary = true)
                Spacer(Modifier.height(Spacing.xl))
            }
        }
    }
}
