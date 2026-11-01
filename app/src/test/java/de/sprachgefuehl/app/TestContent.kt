package de.sprachgefuehl.app

import de.sprachgefuehl.app.data.ContentBundle
import kotlinx.serialization.json.Json
import java.io.File

object TestContent {
    val bundle: ContentBundle by lazy {
        val file = File("src/main/assets/content/content.json")
        require(file.exists()) { "content.json fehlt: ${file.absolutePath}" }
        Json { ignoreUnknownKeys = true }
            .decodeFromString(ContentBundle.serializer(), file.readText())
    }
}
