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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.sprachgefuehl.app.AppUiState
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.ui.components.ScreenHeader
import de.sprachgefuehl.app.ui.components.SoftCard
import de.sprachgefuehl.app.ui.components.Tag
import de.sprachgefuehl.app.ui.components.plural
import de.sprachgefuehl.app.ui.speech.rememberSpeechController
import de.sprachgefuehl.app.ui.theme.Spacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MapScreen(vm: AppViewModel, state: AppUiState) {
    val content = state.content ?: return
    val speech = rememberSpeechController()
    val memory = state.progress.chunkMemory
    var expanded by remember { mutableStateOf<String?>(null) }
    val patterns = content.patterns.filter { it.chunks.isNotEmpty() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = "Sprachkarte",
            subtitle = "${patterns.size} Muster · ${plural(memory.size, "Satz", "Sätze")} in Erinnerung",
            onBack = { vm.back() }
        )
        Column(modifier = Modifier.padding(horizontal = Spacing.l)) {
            Text(
                "Sprache ist ein Netz, kein Stapel Wörter. Tippe auf einen Baustein, um ihn zu hören.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (content.personas.isNotEmpty()) {
                Spacer(Modifier.height(Spacing.l))
                Text("Menschen im Viertel", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(Spacing.s))
                content.personas.forEach { persona ->
                    val places = content.situations.filter { it.id in persona.situations }
                    SoftCard(modifier = Modifier.padding(bottom = Spacing.s)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(persona.name, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.width(Spacing.s))
                            Text(
                                persona.role,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            persona.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(Spacing.xs))
                        Text(
                            "Zu hören in: " + places.take(3).joinToString(", ") { it.title },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(Spacing.m))
            content.pillars.sortedBy { it.order }.forEach { pillar ->
                val pillarPatterns = patterns.filter { it.pillarId == pillar.id }
                if (pillarPatterns.isEmpty()) return@forEach
                Text(
                    pillar.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = Spacing.m, bottom = Spacing.s)
                )
                pillarPatterns.forEach { pattern ->
                    val isOpen = expanded == pattern.id
                    SoftCard(
                        modifier = Modifier.padding(bottom = Spacing.s),
                        onClick = { expanded = if (isOpen) null else pattern.id }
                    ) {
                        Text(pattern.template, style = MaterialTheme.typography.titleMedium)
                        Text(
                            pattern.de,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(Spacing.s))
                        val chunks = pattern.chunks.mapNotNull { id -> content.chunks.firstOrNull { it.id == id } }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                            verticalArrangement = Arrangement.spacedBy(Spacing.s)
                        ) {
                            chunks.take(if (isOpen) chunks.size else 4).forEach { chunk ->
                                val learned = memory.containsKey(chunk.id)
                                Tag(
                                    text = (if (learned) "✓ " else "") + chunk.en,
                                    active = learned,
                                    onClick = { speech.speak(chunk.en) }
                                )
                            }
                        }
                        if (isOpen) {
                            Spacer(Modifier.height(Spacing.s))
                            chunks.forEach { chunk ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.VolumeUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = Spacing.s)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(chunk.en, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            chunk.de,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (memory.containsKey(chunk.id)) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = "gelernt", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            val related = content.patterns.filter { it.id in pattern.related }
                            if (related.isNotEmpty()) {
                                Spacer(Modifier.height(Spacing.s))
                                Text("Verwandt", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                related.forEach { rel ->
                                    Text(
                                        "· ${rel.template}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(Spacing.xl))
        }
    }
}
