package de.sprachgefuehl.app.data.account

import de.sprachgefuehl.app.data.Progress
import java.io.File

/**
 * Recordings keep their original id, but the path is always rebuilt for this device. That is
 * what makes a cloud restore on a new phone work: the old absolute path is meaningless there.
 */
object RecordingPaths {
    fun fileName(recordingId: String): String {
        if (recordingId.contains('/') || recordingId.contains('\\') || recordingId.contains("..")) {
            return "recording.m4a"
        }
        val safe = recordingId.filter { it.isLetterOrDigit() || it == '_' || it == '-' }
        return (safe.ifBlank { "recording" }) + ".m4a"
    }

    fun localFile(directory: File, recordingId: String): File =
        File(directory, fileName(recordingId))

    fun objectName(userId: String, recordingId: String): String =
        "$userId/${fileName(recordingId)}"

    fun recordingIdFromObjectName(name: String): String? =
        name.substringAfterLast('/').removeSuffix(".m4a").takeIf { it.isNotBlank() }

    fun remap(progress: Progress, directory: File): Progress =
        progress.copy(recordings = progress.recordings.map { item ->
            item.copy(path = localFile(directory, item.id).absolutePath)
        })
}
