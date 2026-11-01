package de.sprachgefuehl.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import de.sprachgefuehl.app.AppUiState
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.speech.AudioPlayer
import de.sprachgefuehl.app.speech.AudioRecorder
import de.sprachgefuehl.app.ui.components.BigButton
import de.sprachgefuehl.app.ui.components.DividerLine
import de.sprachgefuehl.app.ui.components.ScreenHeader
import de.sprachgefuehl.app.ui.components.SoftCard
import de.sprachgefuehl.app.ui.components.Tag
import de.sprachgefuehl.app.ui.components.plural
import de.sprachgefuehl.app.ui.speech.rememberSpeechController
import de.sprachgefuehl.app.ui.theme.Spacing
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CollectionScreen(vm: AppViewModel, state: AppUiState) {
    val content = state.content ?: return
    val progress = state.progress
    val speech = rememberSpeechController()
    val context = LocalContext.current
    val recorder = remember { AudioRecorder(context) }
    val player = remember { AudioPlayer() }
    var recording by remember { mutableStateOf(false) }
    var permissionAsked by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("all") }
    var pendingDelete by remember { mutableStateOf("") }
    var playingAll by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            recorder.start()
            recording = true
        }
    }

    DisposableEffect(Unit) {
        speech.output.onQueueDone = { playingAll = false }
        onDispose {
            recorder.cancel()
            player.stop()
            speech.stopSpeaking()
            speech.output.onQueueDone = null
        }
    }

    // Alles, was in der Sammlung liegt – auch Sätze aus einer Lektion, die noch nicht
    // gesprochen wurden. Sonst widerspricht die Liste der Zahl im Profil.
    val allChunks = progress.chunkMemory.entries
        .sortedWith(compareByDescending<Map.Entry<String, de.sprachgefuehl.app.data.Memory>> { it.value.stage }.thenBy { it.key })
        .mapNotNull { entry -> content.chunks.firstOrNull { it.id == entry.key }?.let { it to entry.value } }
    val chunks = allChunks.filter { (chunk, memory) ->
        val matchesQuery = query.isBlank() ||
            chunk.en.contains(query.trim(), ignoreCase = true) ||
            chunk.de.contains(query.trim(), ignoreCase = true)
        val matchesFilter = when (filter) {
            "solid" -> memory.stage >= 4
            "shaky" -> memory.stage <= 2
            else -> true
        }
        matchesQuery && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = "Meine Sätze",
            subtitle = "${plural(allChunks.size, "Satz", "Sätze")} in deiner Sammlung",
            onBack = { vm.back() }
        )
        Column(modifier = Modifier.padding(horizontal = Spacing.l)) {
            if (allChunks.isNotEmpty()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Wie sagt man …?") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(Spacing.s))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    Tag("Alle", active = filter == "all", onClick = { filter = "all" })
                    Tag("Sicher", active = filter == "solid", onClick = { filter = "solid" })
                    Tag("Wackelig", active = filter == "shaky", onClick = { filter = "shaky" })
                }
                Spacer(Modifier.height(Spacing.s))
                BigButton(
                    text = if (playingAll) "Wiedergabe stoppen" else "Alle Sätze anhören",
                    onClick = {
                        if (playingAll) {
                            speech.stopSpeaking()
                            playingAll = false
                        } else {
                            speech.speakQueue(chunks.map { it.first.en })
                            playingAll = true
                        }
                    },
                    secondary = true
                )
                Spacer(Modifier.height(Spacing.m))
            }
            if (chunks.isEmpty() && allChunks.isNotEmpty()) {
                Text(
                    "Kein Satz passt zu „$query“. Versuche ein Wort aus dem Deutschen oder Englischen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.m))
            }
            if (allChunks.isEmpty()) {
                SoftCard {
                    Text("Hier wächst deine Sammlung", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(Spacing.s))
                    Text(
                        "Jeder gesprochene Satz landet hier – mit Modellstimme und deiner Aufnahme.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                chunks.forEach { (chunk, memory) ->
                    SoftCard(modifier = Modifier.padding(bottom = Spacing.s)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(chunk.en, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text(
                                    chunk.de,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(Spacing.xs))
                                Row {
                                    Tag(text = "Stufe ${memory.stage} von 5", active = memory.stage >= 4)
                                }
                            }
                            IconButton(onClick = { speech.speak(chunk.en) }, modifier = Modifier.size(56.dp)) {
                                Icon(Icons.Rounded.VolumeUp, contentDescription = "Satz anhören", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.l))
            DividerLine()
            Spacer(Modifier.height(Spacing.l))
            Text("Deine Stimme", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.s))
            Text(
                "Nimm dich auf und höre dir in ein paar Wochen zu.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.m))
            BigButton(
                text = if (recording) "Aufnahme stoppen" else "Jetzt aufnehmen",
                onClick = {
                    if (recording) {
                        val file = recorder.stop()
                        recording = false
                        if (file != null) vm.addRecording("Meine Stimme", file.absolutePath)
                    } else {
                        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                            PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            recorder.start()
                            recording = true
                        } else {
                            permissionAsked = true
                            launcher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                icon = if (recording) Icons.Rounded.Stop else Icons.Rounded.Mic
            )
            if (permissionAsked && !recording) {
                Spacer(Modifier.height(Spacing.s))
                Text(
                    "Mit Mikrofon-Erlaubnis kann die App zuhören. Abschalten kannst du sie jederzeit in den Einstellungen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (progress.recordings.isNotEmpty()) {
                Spacer(Modifier.height(Spacing.l))
                Text("Aufnahmen", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.s))
                progress.recordings.reversed().forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.label, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Aufgenommen am ${dayLabel(item.day)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { player.play(item.path) }, modifier = Modifier.size(56.dp)) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = "Aufnahme abspielen", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { pendingDelete = item.id }, modifier = Modifier.size(56.dp)) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Aufnahme löschen", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                if (pendingDelete.isNotBlank()) {
                    AlertDialog(
                        onDismissRequest = { pendingDelete = "" },
                        title = { Text("Aufnahme löschen?") },
                        text = { Text("Die Aufnahme wird von diesem Gerät entfernt. Dein Fortschritt bleibt.") },
                        confirmButton = {
                            TextButton(onClick = {
                                vm.removeRecording(pendingDelete)
                                pendingDelete = ""
                            }) { Text("Löschen") }
                        },
                        dismissButton = {
                            TextButton(onClick = { pendingDelete = "" }) { Text("Behalten") }
                        }
                    )
                }
            }
            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

private val dayFormat = DateTimeFormatter.ofPattern("d. MMMM", Locale.GERMAN)

private fun dayLabel(day: Long): String = LocalDate.ofEpochDay(day).format(dayFormat)
