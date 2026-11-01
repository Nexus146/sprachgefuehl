package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.ContentBundle
import de.sprachgefuehl.app.data.ErrorCard
import de.sprachgefuehl.app.data.Lesson
import de.sprachgefuehl.app.data.Memory
import de.sprachgefuehl.app.data.Progress

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
        return (due + shaky + met + upcoming + everything).distinct().take(limit)
    }

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
        return progress.chunkMemory.entries
            .filter { it.key in byId }
            .sortedWith(
                compareBy<Map.Entry<String, Memory>>(
                    { if (Srs.isDue(it.value, now)) 0 else 1 },
                    { it.value.stage },
                    { it.value.stability }
                )
            )
            .map { it.key }
            .take(limit)
    }
}
