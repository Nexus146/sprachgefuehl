package de.sprachgefuehl.app.data

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Backups are one file the learner owns. No server, no account: the file goes wherever the
 * learner saves it (Downloads, an SD card, a USB stick) and can be read back on another phone.
 */
object Backup {

    const val MIME = "application/zip"
    const val FILE_NAME = "sprachgefuehl-sicherung.zip"

    fun recordingsDir(context: Context): File = File(context.filesDir, "recordings")

    fun recordings(context: Context): List<File> = recordingsDir(context)
        .listFiles()
        ?.filter { it.isFile && it.length() > 0 }
        ?.sortedBy { it.name }
        .orEmpty()

    /** Writes the bundle into the file the learner picked in the system dialog. */
    fun write(context: Context, uri: Uri, progressJson: String): Boolean = runCatching {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            BackupBundle.write(stream, progressJson, recordings(context))
            true
        } ?: false
    }.getOrDefault(false)

    /** Reads the picked file. Null means: not a backup of this app, nothing is changed. */
    fun read(context: Context, uri: Uri): BackupContent? = runCatching {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return null
        BackupBundle.read(bytes)
    }.getOrNull()

    /** Puts the recordings from the file back where the app looks for them. */
    fun restoreRecordings(context: Context, content: BackupContent): Int {
        val directory = recordingsDir(context).apply { mkdirs() }
        var restored = 0
        content.recordings.forEach { (name, data) ->
            runCatching {
                File(directory, name).writeBytes(data)
                restored++
            }
        }
        return restored
    }
}
