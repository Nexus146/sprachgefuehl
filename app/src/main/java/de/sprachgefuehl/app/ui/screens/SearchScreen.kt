package de.sprachgefuehl.app.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.ui.unit.dp
import de.sprachgefuehl.app.AppUiState
import de.sprachgefuehl.app.AppViewModel
import de.sprachgefuehl.app.Screen
import de.sprachgefuehl.app.domain.ContentSearch
import de.sprachgefuehl.app.domain.SearchKind
import de.sprachgefuehl.app.ui.components.ScreenHeader
import de.sprachgefuehl.app.ui.components.SoftCard
import de.sprachgefuehl.app.ui.components.Tag
import de.sprachgefuehl.app.ui.components.plural
import de.sprachgefuehl.app.ui.speech.rememberSpeechController
import de.sprachgefuehl.app.ui.theme.Spacing

private val examples = listOf("headache", "Kaffee", "Termin", "Weg", "danke")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(vm: AppViewModel, state: AppUiState) {
    val content = state.content ?: return
    val speech = rememberSpeechController()
    var query by rememberSaveable { mutableStateOf("") }
    // Ist die Tastatur offen, schließt „Zurück“ erst die Tastatur. Sonst verlor sie beim
    // Verstecken der Tastatur gleich die ganze Suche samt eingegebenem Wort. Ob die Tastatur
    // wirklich offen ist, sagt das Fenster – das Feld behält den Fokus auch danach.
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val view = LocalView.current
    BackHandler {
        val imeOpen = ViewCompat.getRootWindowInsets(view)
            ?.isVisible(WindowInsetsCompat.Type.ime()) == true
        if (imeOpen) {
            keyboard?.hide()
            focus.clearFocus()
        } else {
            vm.back()
        }
    }
    val hits = ContentSearch.search(content, query)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = "Suche",
            subtitle = "Sätze, Situationen und Muster wiederfinden",
            onBack = { vm.back() }
        )
        Column(modifier = Modifier.padding(horizontal = Spacing.l)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it.take(40) },
                label = { Text("Wonach suchst du?") },
                placeholder = { Text("Deutsch oder Englisch") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.s))
            Text(
                "Ein Wort genügt. Groß- und Kleinschreibung sind egal.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.m))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                verticalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                examples.forEach { word ->
                    Tag(text = word, active = query.equals(word, ignoreCase = true), onClick = { query = word })
                }
            }

            Spacer(Modifier.height(Spacing.l))
            when {
                query.trim().length < 2 -> SoftCard {
                    Text("Was du hier findest", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(Spacing.s))
                    Text(
                        "Alle Sätze aus deinen Lektionen, die Muster dahinter und die Situationen. " +
                            "Tippe ein Ergebnis an, um es in der passenden Lektion zu üben.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                hits.isEmpty() -> SoftCard {
                    Text("Nichts gefunden", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(Spacing.s))
                    Text(
                        "Für „$query“ gibt es noch keinen Eintrag. Versuche ein kürzeres Wort – " +
                            "zum Beispiel „Kaffee“ statt „Kaffeebestellung“.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                else -> {
                    Text(
                        plural(hits.size, "Treffer", "Treffer"),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(Spacing.s))
                    hits.forEach { hit ->
                        SoftCard(
                            modifier = Modifier.padding(bottom = Spacing.s),
                            onClick = {
                                if (hit.lessonId.isNotBlank()) vm.navigate(Screen.Lesson(hit.lessonId))
                            }
                        ) {
                            Row {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        hit.kind.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        hit.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        hit.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (hit.lessonId.isNotBlank()) {
                                        Spacer(Modifier.height(Spacing.xs))
                                        Text(
                                            "In der Lektion üben",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                if (hit.kind == SearchKind.SENTENCE) {
                                    IconButton(
                                        onClick = { speech.speak(hit.title) },
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.VolumeUp,
                                            contentDescription = "Satz anhören",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}
