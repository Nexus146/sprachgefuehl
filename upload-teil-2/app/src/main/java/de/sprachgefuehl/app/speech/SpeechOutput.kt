package de.sprachgefuehl.app.speech

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import java.util.Locale

/**
 * Wraps Android TTS. Only voices that work without a network connection are preferred, because
 * the app promises to work offline. If the device has no English voice installed, the UI shows
 * a friendly setup hint instead of an error.
 */
object SpeechDefaults {
    @Volatile var enabled: Boolean = true
    @Volatile var rate: Float = 0.85f
}

class SpeechOutput(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var ready = false
    private var pending: (() -> Unit)? = null
    private val main = Handler(Looper.getMainLooper())

    var onReady: ((Boolean) -> Unit)? = null
    var onDone: (() -> Unit)? = null

    /** Called when the last line of a queue has been read. */
    var onQueueDone: (() -> Unit)? = null
    private var queueLeft = 0

    val isReady: Boolean get() = ready

    fun initialize() {
        if (tts != null) return
        tts = TextToSpeech(context.applicationContext) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) {
                configure()
            }
            onReady?.invoke(ready)
            pending?.invoke()
            pending = null
        }
    }

    private fun configure() {
        val engine = tts ?: return
        val result = engine.setLanguage(Locale.US)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            engine.setLanguage(Locale.UK)
        }
        val voice = pickVoice()
        if (voice != null) {
            engine.voice = voice
        }
        engine.setSpeechRate(0.85f)
        engine.setPitch(1.0f)
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit
            override fun onDone(utteranceId: String?) {
                val fromQueue = utteranceId?.startsWith("sq_") == true
                main.post {
                    if (fromQueue) {
                        queueLeft -= 1
                        if (queueLeft <= 0) {
                            queueLeft = 0
                            onQueueDone?.invoke()
                        }
                    }
                    onDone?.invoke()
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) = Unit
        })
    }

    private fun pickVoice(): Voice? {
        val engine = tts ?: return null
        val voices = runCatching { engine.voices }.getOrNull() ?: return null
        val english = voices.filter { it.locale.language == "en" }
        if (english.isEmpty()) return null
        val offline = english.filter { !it.isNetworkConnectionRequired }
        val pool = if (offline.isNotEmpty()) offline else english
        return pool.sortedWith(
            compareByDescending<Voice> { it.quality }.thenBy { it.name }
        ).firstOrNull()
    }

    fun availableEnglishVoices(): List<Voice> {
        val engine = tts ?: return emptyList()
        return runCatching { engine.voices?.filter { it.locale.language == "en" } }.getOrNull() ?: emptyList()
    }

    fun setVoice(name: String) {
        val engine = tts ?: return
        val voice = runCatching { engine.voices?.firstOrNull { it.name == name } }.getOrNull() ?: return
        engine.voice = voice
    }

    fun speak(text: String, rate: Float = SpeechDefaults.rate) {
        if (text.isBlank() || !SpeechDefaults.enabled) return
        val engine = tts
        if (engine == null || !ready) {
            initialize()
            pending = { speak(text, rate) }
            return
        }
        engine.setSpeechRate(rate.coerceIn(0.6f, 1.2f))
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), "sg_${System.nanoTime()}")
    }

    /** Reads a whole list in one go – hands free, without tapping every line. */
    fun speakQueue(texts: List<String>, rate: Float = SpeechDefaults.rate) {
        val lines = texts.filter { it.isNotBlank() }
        if (lines.isEmpty() || !SpeechDefaults.enabled) return
        val engine = tts
        if (engine == null || !ready) {
            initialize()
            pending = { speakQueue(lines, rate) }
            return
        }
        queueLeft = lines.size
        engine.setSpeechRate(rate.coerceIn(0.6f, 1.2f))
        engine.speak(lines.first(), TextToSpeech.QUEUE_FLUSH, Bundle(), "sq_" + System.nanoTime())
        lines.drop(1).forEach { line ->
            engine.speak(line, TextToSpeech.QUEUE_ADD, Bundle(), "sq_" + System.nanoTime())
        }
    }

    fun stop() {
        queueLeft = 0
        runCatching { tts?.stop() }
    }

    fun shutdown() {
        runCatching { tts?.shutdown() }
        tts = null
        ready = false
    }
}
