package de.sprachgefuehl.app.data

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.IOException

class ContentRepository(private val context: Context) {

    private var loaded: ContentBundle? = null

    val bundle: ContentBundle
        get() = loaded ?: throw IllegalStateException("Inhalte sind noch nicht geladen")

    fun load(): ContentBundle {
        loaded?.let { return it }
        val json = context.assets.open("content/content.json").bufferedReader().use { it.readText() }
        val parsed = Json {
            ignoreUnknownKeys = true
            isLenient = false
        }.decodeFromString(ContentBundle.serializer(), json)
        if (parsed.lessons.isEmpty()) throw IOException("Inhalte sind leer")
        loaded = parsed
        return parsed
    }

    fun lesson(id: String): Lesson? = loaded?.lessons?.firstOrNull { it.id == id }
    fun situation(id: String): Situation? = loaded?.situations?.firstOrNull { it.id == id }
    fun pillar(id: String): Pillar? = loaded?.pillars?.firstOrNull { it.id == id }
    fun errorCard(id: String): ErrorCard? = loaded?.errorCards?.firstOrNull { it.id == id }
    fun chunk(id: String): Chunk? = loaded?.chunks?.firstOrNull { it.id == id }
    fun pattern(id: String): PatternDef? = loaded?.patterns?.firstOrNull { it.id == id }
    fun connector(id: String): ConnectorLevel? = loaded?.connectorLevels?.firstOrNull { it.id == id }

    fun lessonsOf(situationId: String): List<Lesson> =
        loaded?.lessons?.filter { it.situationId == situationId }?.sortedBy { it.order } ?: emptyList()

    fun situationsOf(pathId: String): List<Situation> =
        loaded?.situations?.filter { it.pathId == pathId }?.sortedBy { it.order } ?: emptyList()

    fun chunksOf(patternId: String): List<Chunk> =
        loaded?.chunks?.filter { it.patternId == patternId } ?: emptyList()

    fun relatedPatterns(patternId: String): List<PatternDef> {
        val pattern = pattern(patternId) ?: return emptyList()
        return pattern.related.mapNotNull { pattern(it) }
    }
}
