package de.sprachgefuehl.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import de.sprachgefuehl.app.domain.Glossary

/**
 * Ein englischer Satz, in dem unbekannte Wörter antippbar sind. Nur Wörter mit einem Eintrag im
 * Glossar bekommen die gepunktete Linie – lieber nichts markieren als eine falsche Bedeutung
 * anbieten. Das Fenster nennt Wort, Übersetzung und spricht das Wort auf Wunsch vor.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GlossedSentence(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    onSpeak: ((String) -> Unit)? = null
) {
    val words = remember(text) { text.trim().split(Regex("\\s+")).filter { it.isNotBlank() } }
    var shown by remember(text) { mutableStateOf<Pair<String, String>?>(null) }
    val resolved = remember(text) { words.map { it to Glossary.of(it) } }
    val textColor = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color
    val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        resolved.forEach { (word, gloss) ->
            if (gloss == null) {
                Text(word, style = style, color = textColor)
            } else {
                Text(
                    word,
                    style = style,
                    color = textColor,
                    modifier = Modifier
                        .clickable { shown = word to gloss }
                        .drawBehind {
                            val y = size.height - 2.dp.toPx()
                            drawLine(
                                color = lineColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.6.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx()))
                            )
                        }
                )
            }
        }
    }

    val entry = shown
    if (entry != null) {
        AlertDialog(
            onDismissRequest = { shown = null },
            title = { Text(entry.first, style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    entry.second,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            confirmButton = {
                TextButton(onClick = { shown = null }) { Text("Schließen") }
            },
            dismissButton = if (onSpeak != null) {
                {
                    TextButton(onClick = {
                        onSpeak(entry.first)
                        shown = null
                    }) { Text("Anhören") }
                }
            } else null
        )
    }
}
