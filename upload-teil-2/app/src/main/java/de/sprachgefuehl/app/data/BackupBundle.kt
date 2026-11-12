package de.sprachgefuehl.app.data

import java.io.ByteArrayInputStream
import java.io.File
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** What a backup file carries: the progress document and the learner's own recordings. */
data class BackupContent(
    val progressJson: String,
    val recordings: Map<String, ByteArray> = emptyMap()
)

/**
 * The file format of a backup, kept free of Android types so it can be tested on the JVM.
 *
 * A backup is one ZIP file with `fortschritt.json` and, if there are any, the recordings under
 * `aufnahmen/`. A plain JSON document from an older version is still accepted, so nothing a
 * learner saved earlier becomes unreadable.
 */
object BackupBundle {

    const val PROGRESS_ENTRY = "fortschritt.json"
    const val RECORDINGS_PREFIX = "aufnahmen/"

    // A file from elsewhere must not be able to fill the device: the caps are generous for a
    // learner's own recordings (forty at most) and small enough to stop a zip bomb.
    private const val MAX_RECORDINGS = 80
    private const val MAX_RECORDING_BYTES = 12 * 1024 * 1024

    fun write(out: OutputStream, progressJson: String, recordings: List<File>) {
        ZipOutputStream(out).use { zip ->
            zip.putNextEntry(ZipEntry(PROGRESS_ENTRY))
            zip.write(progressJson.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            recordings.forEach { file ->
                val data = runCatching { file.readBytes() }.getOrNull()
                if (data == null || data.isEmpty()) return@forEach
                zip.putNextEntry(ZipEntry(RECORDINGS_PREFIX + file.name))
                zip.write(data)
                zip.closeEntry()
            }
        }
    }

    /** Returns null for anything that is not a backup of this app – the caller then changes nothing. */
    fun read(bytes: ByteArray): BackupContent? {
        if (bytes.isEmpty()) return null
        return if (looksLikeZip(bytes)) readZip(bytes) else readPlain(bytes)
    }

    private fun looksLikeZip(bytes: ByteArray): Boolean =
        bytes.size > 4 && bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte()

    private fun readPlain(bytes: ByteArray): BackupContent? {
        val text = runCatching { bytes.toString(Charsets.UTF_8) }.getOrNull() ?: return null
        if (!text.trimStart().startsWith("{")) return null
        return BackupContent(text)
    }

    private fun readZip(bytes: ByteArray): BackupContent? {
        var json: String? = null
        val recordings = LinkedHashMap<String, ByteArray>()
        runCatching {
            ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    when {
                        entry.name == PROGRESS_ENTRY -> {
                            json = zip.readBytes().toString(Charsets.UTF_8)
                        }
                        entry.name.startsWith(RECORDINGS_PREFIX) -> {
                            val name = recordingName(entry.name)
                            if (name != null && recordings.size < MAX_RECORDINGS) {
                                val data = zip.readBytes()
                                if (data.isNotEmpty() && data.size <= MAX_RECORDING_BYTES) {
                                    recordings[name] = data
                                }
                            }
                        }
                    }
                    zip.closeEntry()
                }
            }
        }.getOrElse { return null }
        val progressJson = json?.takeIf { it.trimStart().startsWith("{") } ?: return null
        return BackupContent(progressJson, recordings)
    }

    /**
     * Only a bare file name is taken over. An entry that tries to reach into another folder of the
     * app (`../…`) is ignored instead of being written somewhere else.
     */
    private fun recordingName(entry: String): String? {
        val name = entry.removePrefix(RECORDINGS_PREFIX)
        if (name.isEmpty()) return null
        if (name.contains('/') || name.contains('\\') || name.contains("..")) return null
        return name
    }
}
