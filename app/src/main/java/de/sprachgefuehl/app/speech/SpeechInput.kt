package de.sprachgefuehl.app.speech

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

data class SpeechAttempt(
    val transcript: String,
    val alternatives: List<String> = emptyList(),
    val error: String? = null
)

/**
 * Small wrapper around SpeechRecognizer. It prefers the platform's on-device recognizer when
 * available; the caller is told when recognition is unavailable so the UI can fall back to a
 * self check instead of blocking the lesson.
 */
class SpeechInput(private val context: Context) {

    private var recognizer: SpeechRecognizer? = null
    private val main = Handler(Looper.getMainLooper())
    private var result: ((SpeechAttempt) -> Unit)? = null
    private var partial: ((String) -> Unit)? = null
    private var listening = false

    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    val isOnDevice: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            runCatching { SpeechRecognizer.isOnDeviceRecognitionAvailable(context) }.getOrDefault(false)

    fun start(
        onPartial: (String) -> Unit,
        onResult: (SpeechAttempt) -> Unit
    ) {
        if (!isAvailable) {
            onResult(SpeechAttempt("", error = "Auf diesem Gerät ist keine Spracherkennung installiert."))
            return
        }
        partial = onPartial
        result = onResult
        main.post {
            ensureRecognizer()
            val intent = buildIntent()
            runCatching { recognizer?.startListening(intent) }
                .onFailure { onResult(SpeechAttempt("", error = "Die Spracherkennung konnte nicht starten.")) }
            listening = true
        }
    }

    private fun ensureRecognizer() {
        if (recognizer != null) return
        recognizer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isOnDevice) {
            runCatching { SpeechRecognizer.createOnDeviceSpeechRecognizer(context) }
                .getOrElse { SpeechRecognizer.createSpeechRecognizer(context) }
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() {
                listening = false
            }

            override fun onError(error: Int) {
                listening = false
                val message = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Ich habe nichts gehört. Versuch es noch einmal."
                    SpeechRecognizer.ERROR_NETWORK,
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Die Erkennung braucht gerade eine Verbindung. Du kannst auch selbst einschätzen."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Das Mikrofon ist noch nicht erlaubt."
                    // Die App hört absichtlich offline zu, damit keine Aufnahme das Gerät verlässt.
                    // Fehlt das englische Sprachpaket, hilft kein zweiter Versuch – das muss man wissen.
                    SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
                    SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> "Für Englisch fehlt auf diesem Gerät noch das Sprachpaket zum Zuhören. " +
                        "In den Android-Einstellungen unter „Spracherkennung“ lässt es sich mit WLAN nachladen. " +
                        "Bis dahin kannst du selbst einschätzen, ob es geklappt hat."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Die Erkennung war noch beschäftigt. Noch einmal?"
                    else -> "Das hat gerade nicht geklappt. Noch einmal?"
                }
                result?.invoke(SpeechAttempt("", error = message))
            }

            override fun onResults(results: Bundle?) {
                listening = false
                val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                result?.invoke(SpeechAttempt(list.firstOrNull().orEmpty(), list.drop(1)))
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val text = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (!text.isNullOrBlank()) partial?.invoke(text)
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
    }

    private fun buildIntent(): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toLanguageTag())
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.US.toLanguageTag())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            putExtra(RecognizerIntent.EXTRA_REQUEST_WORD_CONFIDENCE, true)
        }
    }

    fun stop() {
        main.post {
            runCatching { recognizer?.stopListening() }
            listening = false
        }
    }

    fun cancel() {
        main.post {
            runCatching { recognizer?.cancel() }
            listening = false
        }
    }

    fun destroy() {
        main.post {
            runCatching { recognizer?.destroy() }
            recognizer = null
        }
    }
}
