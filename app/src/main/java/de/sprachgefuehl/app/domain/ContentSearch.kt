package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.ContentBundle

enum class SearchKind(val label: String) {
    SENTENCE("Satz"),
    PATTERN("Muster"),
    SITUATION("Situation"),
    LESSON("Lektion")
}

data class SearchHit(
    val kind: SearchKind,
    val title: String,
    val subtitle: String,
    val lessonId: String = ""
)

/**
 * Plain substring search over the authored content, German and English. Deliberately not fuzzy:
 * a learner who types "headache" wants the sentences with that word, not a guess.
 */
object ContentSearch {

    private fun score(haystacks: List<String>, query: String): Int? {
        var best: Int? = null
        haystacks.forEach { text ->
            val lower = text.lowercase()
            val hit = when {
                lower == query -> 3
                lower.startsWith(query) -> 2
                lower.contains(" $query") -> 1
                lower.contains(query) -> 1
                else -> 0
            }
            if (hit > 0 && (best == null || hit > best!!)) best = hit
        }
        return best
    }

    fun search(content: ContentBundle, rawQuery: String, limit: Int = 25): List<SearchHit> {
        val query = rawQuery.trim().lowercase()
        if (query.length < 2) return emptyList()

        val hits = mutableListOf<Pair<Int, SearchHit>>()

        content.chunks.forEach { chunk ->
            val rank = score(listOf(chunk.en, chunk.de), query) ?: return@forEach
            val lesson = content.lessons.firstOrNull { chunk.id in it.chunkIds }
            hits += rank to SearchHit(
                kind = SearchKind.SENTENCE,
                title = chunk.en,
                subtitle = chunk.de,
                lessonId = lesson?.id.orEmpty()
            )
        }

        content.patterns.forEach { pattern ->
            val rank = score(listOf(pattern.template, pattern.de), query) ?: return@forEach
            val lesson = content.lessons.firstOrNull { lesson -> lesson.steps.any { it.patternId == pattern.id } }
            hits += rank to SearchHit(
                kind = SearchKind.PATTERN,
                title = pattern.template,
                subtitle = pattern.de,
                lessonId = lesson?.id.orEmpty()
            )
        }

        content.situations.forEach { situation ->
            val rank = score(listOf(situation.title, situation.subtitle, situation.scene), query) ?: return@forEach
            val lesson = content.lessons
                .filter { it.situationId == situation.id }
                .minByOrNull { it.order }
            hits += rank to SearchHit(
                kind = SearchKind.SITUATION,
                title = situation.title,
                subtitle = situation.subtitle.ifBlank { situation.scene },
                lessonId = lesson?.id.orEmpty()
            )
        }

        content.lessons.forEach { lesson ->
            val rank = score(listOf(lesson.title, lesson.goal), query) ?: return@forEach
            hits += rank to SearchHit(
                kind = SearchKind.LESSON,
                title = lesson.title,
                subtitle = lesson.goal,
                lessonId = lesson.id
            )
        }

        return hits
            .sortedWith(compareByDescending<Pair<Int, SearchHit>> { it.first }.thenBy { it.second.title })
            .map { it.second }
            .distinctBy { it.kind to it.title }
            .take(limit)
    }
}
