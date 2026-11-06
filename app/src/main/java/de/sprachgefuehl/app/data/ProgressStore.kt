package de.sprachgefuehl.app.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Progress lives in one small JSON document next to the app's private files. No account, no
 * server. Writes go to a temporary file first so a crash cannot leave a half-written state.
 */
class ProgressStore(private val file: File) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Synchronized
    fun load(): Progress = runCatching {
        if (!file.exists()) Progress() else json.decodeFromString(Progress.serializer(), file.readText())
    }.getOrElse { Progress() }

    /** Returns false when the write could not be completed. The caller must surface that. */
    @Synchronized
    fun save(progress: Progress): Boolean = runCatching {
        val text = json.encodeToString(progress)
        file.parentFile?.mkdirs()
        val temp = File(file.parentFile, file.name + ".tmp")
        temp.writeText(text)
        runCatching {
            Files.move(
                temp.toPath(), file.toPath(),
                StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE
            )
        }.onFailure {
            file.writeText(text)
            temp.delete()
        }
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
