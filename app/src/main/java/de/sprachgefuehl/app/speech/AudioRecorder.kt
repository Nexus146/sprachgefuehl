package de.sprachgefuehl.app.speech

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * Recordings stay in the app's private folder and are deleted after 30 days by the store.
 * They exist so the learner can hear her own progress, not for assessment.
 */
class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var current: File? = null

    val isRecording: Boolean get() = recorder != null

    fun start(): File? {
        if (recorder != null) return current
        val dir = File(context.filesDir, "recordings").apply { mkdirs() }
        val file = File(dir, "rec_${System.currentTimeMillis()}.m4a")
        val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        return runCatching {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder.setAudioEncodingBitRate(96_000)
            mediaRecorder.setAudioSamplingRate(44_100)
            mediaRecorder.setOutputFile(file.absolutePath)
            mediaRecorder.prepare()
            mediaRecorder.start()
            recorder = mediaRecorder
            current = file
            file
        }.getOrElse {
            runCatching { mediaRecorder.release() }
            null
        }
    }

    fun amplitude(): Float {
        val max = runCatching { recorder?.maxAmplitude ?: 0 }.getOrDefault(0)
        return (max / 32767f).coerceIn(0f, 1f)
    }

    fun stop(): File? {
        val file = current
        runCatching { recorder?.stop() }
        runCatching { recorder?.release() }
        recorder = null
        current = null
        return file?.takeIf { it.exists() && it.length() > 0 }
    }

    fun cancel() {
        runCatching { recorder?.stop() }
        runCatching { recorder?.release() }
        recorder = null
        current?.delete()
        current = null
    }
}

class AudioPlayer {
    private var player: MediaPlayer? = null

    fun play(path: String, onDone: () -> Unit = {}) {
        stop()
        val mediaPlayer = MediaPlayer()
        runCatching {
            mediaPlayer.setDataSource(path)
            mediaPlayer.setOnCompletionListener {
                onDone()
                stop()
            }
            mediaPlayer.prepare()
            mediaPlayer.start()
            player = mediaPlayer
        }.onFailure {
            runCatching { mediaPlayer.release() }
        }
    }

    fun stop() {
        runCatching { player?.stop() }
        runCatching { player?.release() }
        player = null
    }
}
