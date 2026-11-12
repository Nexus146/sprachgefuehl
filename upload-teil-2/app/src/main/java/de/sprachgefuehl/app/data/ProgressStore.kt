package de.sprachgefuehl.app.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Progress lives in one small JSON document next to the app's private files. No account, no
 * server. Writes go to a temporary file first so a crash cannot leave a half-written state.
 *
 * Neben der Hauptdatei liegt die letzte gute Fassung als `.bak`. Wird die Hauptdatei einmal
 * unlesbar, kommt der Fortschritt daraus zurück – und eine beschädigte Datei kann die gute
 * Sicherung nie überschreiben.
 */
class ProgressStore(private val file: File) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val backup = File(file.parentFile, file.name + ".bak")

    /** Ob die Hauptdatei beim letzten Lesen verstanden wurde. */
    private var mainReadable = true

    @Synchronized
    fun load(): Progress {
        val fromMain = read(file)
        if (fromMain != null) {
            mainReadable = true
            return fromMain
        }
        // Eine vorhandene, aber unlesbare Datei darf nie zur Sicherung werden.
        if (file.exists() && file.length() > 0L) mainReadable = false
        return read(backup) ?: Progress()
    }

    private fun read(source: File): Progress? = runCatching {
        if (!source.exists() || source.length() == 0L) null
        else json.decodeFromString(Progress.serializer(), source.readText())
    }.getOrNull()

    /** Returns false when the write could not be completed. The caller must surface that. */
    @Synchronized
    fun save(progress: Progress): Boolean = runCatching {
        val text = json.encodeToString(progress)
        file.parentFile?.mkdirs()
        val temp = File(file.parentFile, file.name + ".tmp")
        temp.writeText(text)
        if (mainReadable && file.exists()) {
            runCatching { file.copyTo(backup, overwrite = true) }
        }
        runCatching {
            Files.move(
                temp.toPath(), file.toPath(),
                StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE
            )
        }.onFailure {
            file.writeText(text)
            temp.delete()
        }
        mainReadable = true
        true
    }.getOrDefault(false)

    /** The same document, ready to be shared as a backup file. */
    fun encode(progress: Progress): String = json.encodeToString(progress)

    /**
     * A backup is only accepted if it is a readable progress document of this app. Anything
     * else returns null and leaves the current progress untouched.
     */
    fun decode(text: String): Progress? = runCatching {
        if (!text.trimStart().startsWith("{")) return null
        val parsed = json.decodeFromString(Progress.serializer(), text)
        if (!parsed.onboarded) null else parsed
    }.getOrNull()
}
