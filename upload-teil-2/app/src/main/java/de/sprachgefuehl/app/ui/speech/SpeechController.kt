package de.sprachgefuehl.app.ui.speech

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import de.sprachgefuehl.app.speech.SpeechAttempt
import de.sprachgefuehl.app.speech.SpeechDefaults
import de.sprachgefuehl.app.speech.SpeechInput
import de.sprachgefuehl.app.speech.SpeechOutput

class SpeechController(
    val output: SpeechOutput,
    val input: SpeechInput,
    val permissionGranted: () -> Boolean,
    /**
     * Fragt das Mikrofon an und meldet die Antwort der Nutzerin. Erlaubt sie es, hört die App von
     * selbst zu: Sonst liest sie direkt nach dem Erlauben noch „Bitte erlaube das Mikrofon“ und
     * muss ein zweites Mal auf „Sprechen“ tippen, obwohl sie gerade genau das getan hat.
     */
    val requestPermission: (onGranted: () -> Unit, onDenied: () -> Unit) -> Unit
) {
    /** Ohne eigene Angabe gilt die Einstellung der Lernenden - gelesen beim Aufruf, nicht beim Bauen. */
    fun speak(text: String, rate: Float = SpeechDefaults.rate) {
        output.speak(text, rate)
    }

    fun speakQueue(texts: List<String>, rate: Float = SpeechDefaults.rate) {
        output.speakQueue(texts, rate)
    }

    fun stopSpeaking() = output.stop()

    fun listen(onPartial: (String) -> Unit, onResult: (SpeechAttempt) -> Unit) {
        if (permissionGranted()) {
            input.start(onPartial, onResult)
            return
        }
        requestPermission(
            // Erlaubt: sofort zuhören. Nicht erlaubt: ehrlicher Hinweis statt Schweigen.
            { input.start(onPartial, onResult) },
            {
                onResult(
                    SpeechAttempt(
                        "",
                        error = "Ohne Mikrofon kann ich nicht zuhören. Schätze selbst ein – das zählt genauso."
                    )
                )
            }
        )
    }

    fun stopListening() = input.stop()
}

@Composable
fun rememberSpeechController(): SpeechController {
    val context = LocalContext.current
    val tts = remember { SpeechOutput(context) }
    val input = remember { SpeechInput(context) }
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    // Die Antwort des Systems kommt erst nach dem Dialog zurück. Bis dahin merkt sich die
    // App, wer über „erlaubt“ oder „nicht erlaubt“ Bescheid bekommen soll.
    val waiting = remember { mutableStateOf<Pair<() -> Unit, () -> Unit>?>(null) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        granted = result
        // „Jetzt erlaubt“ heißt: sofort zuhören, ohne dass sie ein zweites Mal tippen muss.
        waiting.value?.let { (onGranted, onDenied) -> if (result) onGranted() else onDenied() }
        waiting.value = null
    }

    LaunchedEffect(Unit) { tts.initialize() }
    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
            input.destroy()
        }
    }

    val latestGranted by rememberUpdatedState(granted)
    return remember(tts, input, latestGranted) {
        SpeechController(
            output = tts,
            input = input,
            permissionGranted = { latestGranted },
            requestPermission = { onGranted, onDenied ->
                waiting.value = onGranted to onDenied
                launcher.launch(Manifest.permission.RECORD_AUDIO)
            }
        )
    }
}
