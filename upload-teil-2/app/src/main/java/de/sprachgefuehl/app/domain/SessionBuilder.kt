package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.ContentBundle
import de.sprachgefuehl.app.data.ErrorCard
import de.sprachgefuehl.app.data.Lesson
import de.sprachgefuehl.app.data.Memory
import de.sprachgefuehl.app.data.Progress
import kotlin.random.Random

data class DailyRound(
    val reviewChunkIds: List<String>,
    val lesson: Lesson?,
    val remediationCardIds: List<String>,
    val connectorLessonId: String?
)

object SessionBuilder {

    fun orderedLessons(content: ContentBundle): List<Lesson> {
        val situationOrder = content.situations.sortedWith(compareBy({ s -> pathOrder(content, s.pathId) }, { it.order }))
            .mapIndexed { index, situation -> situation.id to index }
            .toMap()
        return content.lessons.sortedWith(
            compareBy({ situationOrder[it.situationId] ?: Int.MAX_VALUE }, { it.order })
        )
    }

    private fun pathOrder(content: ContentBundle, pathId: String): Int =
        content.paths.firstOrNull { it.id == pathId }?.order ?: Int.MAX_VALUE

    fun nextLesson(content: ContentBundle, progress: Progress): Lesson? =
        orderedLessons(content).firstOrNull { it.id !in progress.completedLessons }

    fun nextLessonOf(content: ContentBundle, progress: Progress, situationId: String): Lesson? {
        val situationLessons = content.lessons.filter { it.situationId == situationId }.sortedBy { it.order }
        return situationLessons.firstOrNull { it.id !in progress.completedLessons }
            ?: situationLessons.lastOrNull()
    }

    fun dueChunks(content: ContentBundle, progress: Progress, now: Long, limit: Int = 6): List<String> {
        val known = content.chunks.associateBy { it.id }
        return progress.chunkMemory.entries
            .filter { (id, memory) -> id in known && Srs.isDue(memory, now) }
            .sortedWith(compareBy({ it.value.stage }, { it.value.due }))
            .map { it.key }
            .take(limit)
    }

    /** Chunks that were learned but are not yet fluent: used for extra speaking practice. */
    fun shakyChunks(content: ContentBundle, progress: Progress, limit: Int = 5): List<String> =
        progress.chunkMemory.entries
            .filter { (id, memory) -> id in content.chunks.associateBy { it.id } && memory.stage < 4 && memory.reps > 0 }
            .sortedBy { it.value.stage }
            .map { it.key }
            .take(limit)

    /**
     * The practice hub works on what is due, then on what is still shaky, then on sentences from
     * lessons the learner has already finished. Only when all of that is used up does it borrow
     * from the next lesson – practice never jumps into a situation she has not met yet.
     */
    fun practiceChunks(content: ContentBundle, progress: Progress, now: Long, limit: Int = 5): List<String> {
        val due = reviewQueue(content, progress, now, limit = limit * 2)
        val shaky = shakyChunks(content, progress, limit = limit * 2)
        val met = content.lessons
            .filter { it.id in progress.completedLessons }
            .flatMap { it.chunkIds }
            .filter { it !in progress.chunkMemory }
        val upcoming = nextLesson(content, progress)?.chunkIds.orEmpty()
        val everything = orderedLessons(content).flatMap { it.chunkIds }
        val recent = progress.lastPracticeChunkIds.toSet()
        val candidates = (due + shaky + met + upcoming + everything)
            .distinct()
            .sortedBy { if (it in recent) 1 else 0 }

        // At most two sentences per lesson in one round; afterwards fill any remaining slot so
        // a small content pool never leaves the round short.
        val picked = mutableListOf<String>()
        val perLesson = mutableMapOf<String, Int>()
        for (id in candidates) {
            val lesson = lessonForChunk(content, id) ?: continue
            if ((perLesson[lesson] ?: 0) >= 2) continue
            picked += id
            perLesson[lesson] = (perLesson[lesson] ?: 0) + 1
            if (picked.size >= limit) return picked
        }
        for (id in candidates) {
            if (id in picked) continue
            picked += id
            if (picked.size >= limit) break
        }
        return picked
    }

    private fun lessonForChunk(content: ContentBundle, chunkId: String): String? =
        content.lessons.firstOrNull { chunkId in it.chunkIds }?.id
            ?: chunkId.substringBeforeLast('_').takeIf { it.isNotBlank() }

    fun remediation(content: ContentBundle, progress: Progress, limit: Int = 3): List<ErrorCard> {
        val cards = content.errorCards.associateBy { it.id }
        return progress.errorCounts.entries
            .filter { it.value > 0 && it.key in cards }
            .sortedByDescending { it.value }
            .mapNotNull { cards[it.key] }
            .take(limit)
    }

    fun dailyRound(content: ContentBundle, progress: Progress, now: Long): DailyRound = DailyRound(
        reviewChunkIds = dueChunks(content, progress, now),
        lesson = nextLesson(content, progress),
        remediationCardIds = remediation(content, progress, 2).map { it.id },
        connectorLessonId = orderedLessons(content).firstOrNull {
            it.id.startsWith("ws_conn") && it.id !in progress.completedLessons
        }?.id
    )

    /**
     * Review order puts the least stable memory first and mixes lessons, so the learner
     * retrieves the same chunk in a different context instead of the same sentence again.
     */
    fun reviewQueue(content: ContentBundle, progress: Progress, now: Long, limit: Int = 10): List<String> {
        val byId = content.chunks.associateBy { it.id }
        val entries = progress.chunkMemory.entries
            .filter { it.key in byId }
            .sortedWith(
                compareBy<Map.Entry<String, Memory>>(
                    { it.value.stage },
                    { it.value.stability },
                    { it.key }
                )
            )
        val due = entries.filter { Srs.isDue(it.value, now) }
        val rest = entries.filterNot { Srs.isDue(it.value, now) }
        val random = Random(progress.seed xor now)
        return (mixByLesson(content, due, random) + mixByLesson(content, rest, random))
            .distinct()
            .take(limit)
    }

    /** Round-robin over lessons: no two entries from one lesson in a row while others wait. */
    private fun mixByLesson(
        content: ContentBundle,
        entries: List<Map.Entry<String, Memory>>,
        random: Random
    ): List<String> {
        val groups = LinkedHashMap<String, ArrayDeque<String>>()
        entries.shuffled(random).forEach { entry ->
            val lesson = lessonForChunk(content, entry.key) ?: "?"
            groups.getOrPut(lesson) { ArrayDeque() }.addLast(entry.key)
        }
        val result = mutableListOf<String>()
        while (groups.values.any { it.isNotEmpty() }) {
            groups.values.forEach { queue ->
                if (queue.isNotEmpty()) result += queue.removeFirst()
            }
        }
        return result
    }
}
