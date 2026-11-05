package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.ContentBundle
import de.sprachgefuehl.app.data.Lesson
import de.sprachgefuehl.app.data.Progress

/** One stop on the path: a lesson, the situation it belongs to, and how long it takes. */
data class JourneyStop(
    val lessonId: String,
    val lessonTitle: String,
    val situationTitle: String,
    val minutes: Int
)

/**
 * The answer to "where am I and what comes next", in words a person can repeat: one chapter, the
 * current step, the two steps behind it, and the chapter after this one. Everything else in the
 * content pool stays out of sight – 104 lessons are not an overview, they are a shelf.
 */
data class Journey(
    val chapterTitle: String,
    val chapterIndex: Int,
    val chapterCount: Int,
    val chapterDone: Int,
    val chapterTotal: Int,
    val now: JourneyStop?,
    val ahead: List<JourneyStop>,
    val afterChapter: String,
    val doneLessons: Int,
    val totalLessons: Int
) {
    val chapterProgress: Float
        get() = if (chapterTotal <= 0) 0f else chapterDone.toFloat() / chapterTotal.toFloat()

    /** "Kapitel 2 von 9 · Gesundheit" */
    val chapterLine: String
        get() = if (chapterTitle.isBlank()) "" else "Kapitel $chapterIndex von $chapterCount · $chapterTitle"

    val chapterLineShort: String
        get() = if (chapterTitle.isBlank()) "" else "$chapterTitle · $chapterDone von $chapterTotal"
}

object JourneyPlanner {

    /** Two steps of foresight are enough to know how it continues. */
    const val AHEAD = 2

    fun of(content: ContentBundle, progress: Progress, lessonId: String? = null): Journey? {
        val ordered = SessionBuilder.orderedLessons(content)
        if (ordered.isEmpty()) return null
        val chapters = PathProgress.paths(content, progress)
        val standing = PathProgress.nextUp(content, progress)?.nextLesson
        val current = ordered.firstOrNull { it.id == lessonId }
            ?: standing?.let { next -> ordered.firstOrNull { it.id == next.id } }
            ?: ordered.firstOrNull { it.id !in progress.completedLessons }
            ?: ordered.last()
        val situation = content.situations.firstOrNull { it.id == current.situationId }
        val pathId = situation?.pathId.orEmpty()
        val chapterStatus = chapters.firstOrNull { it.path.id == pathId }
        val chapterIndex = chapters.indexOfFirst { it.path.id == pathId }
        val start = ordered.indexOfFirst { it.id == current.id }
        val after = if (start >= 0) ordered.drop(start + 1) else emptyList()
        val nextChapter = chapters.drop(chapterIndex + 1)
            .firstOrNull { !it.finished }
            ?: chapters.drop(chapterIndex + 1).firstOrNull()

        return Journey(
            chapterTitle = chapterStatus?.path?.title.orEmpty(),
            chapterIndex = if (chapterIndex >= 0) chapterIndex + 1 else 0,
            chapterCount = chapters.size,
            chapterDone = chapterStatus?.done ?: 0,
            chapterTotal = chapterStatus?.total ?: 0,
            now = stop(content, current),
            ahead = after.take(AHEAD).mapNotNull { stop(content, it) },
            afterChapter = nextChapter?.path?.title.orEmpty(),
            doneLessons = progress.completedLessons.count { id -> ordered.any { it.id == id } },
            totalLessons = ordered.size
        )
    }

    private fun stop(content: ContentBundle, lesson: Lesson): JourneyStop? {
        val situation = content.situations.firstOrNull { it.id == lesson.situationId } ?: return null
        return JourneyStop(
            lessonId = lesson.id,
            lessonTitle = lesson.title,
            situationTitle = situation.title,
            minutes = lesson.xpMinutes
        )
    }
}
