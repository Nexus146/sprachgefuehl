package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.ContentBundle
import de.sprachgefuehl.app.data.Lesson
import de.sprachgefuehl.app.data.PathDef
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.Situation

enum class SituationState {
    /** No lesson of this situation was finished and none was started. */
    NEW,

    /** At least one lesson is done or one is half way through. */
    STARTED,

    /** Every lesson of this situation is done. */
    DONE
}

data class SituationStatus(
    val situation: Situation,
    val done: Int,
    val total: Int,
    val nextLesson: Lesson?,
    val state: SituationState
) {
    val progress: Float get() = if (total <= 0) 0f else done.toFloat() / total.toFloat()
    val label: String get() = when {
        state == SituationState.DONE -> "Geschafft"
        state == SituationState.STARTED && done == 0 -> "Angefangen"
        state == SituationState.STARTED -> "$done von $total"
        else -> "Neu"
    }
}

data class PathStatus(
    val path: PathDef,
    val situations: List<SituationStatus>
) {
    val done: Int get() = situations.sumOf { it.done }
    val total: Int get() = situations.sumOf { it.total }
    val progress: Float get() = if (total <= 0) 0f else done.toFloat() / total.toFloat()
    val finished: Boolean get() = situations.isNotEmpty() && situations.all { it.state == SituationState.DONE }
    val next: SituationStatus? get() = situations.firstOrNull { it.state != SituationState.DONE }
}

/**
 * Turns the flat progress maps into the answers the learner actually asks for: where am I, what
 * comes next, and what is already behind me. Pure functions, so the answers can be tested.
 */
object PathProgress {

    fun situation(content: ContentBundle, progress: Progress, situation: Situation): SituationStatus {
        val lessons = content.lessons.filter { it.situationId == situation.id }.sortedBy { it.order }
        val done = lessons.count { it.id in progress.completedLessons }
        val next = lessons.firstOrNull { it.id !in progress.completedLessons }
        val started = lessons.any { (progress.lessonProgress[it.id] ?: 0) > 0 }
        val state = when {
            lessons.isNotEmpty() && done == lessons.size -> SituationState.DONE
            done > 0 || started -> SituationState.STARTED
            else -> SituationState.NEW
        }
        return SituationStatus(situation, done, lessons.size, next, state)
    }

    fun path(content: ContentBundle, progress: Progress, path: PathDef): PathStatus {
        val situations = content.situations
            .filter { it.pathId == path.id }
            .sortedBy { it.order }
            .map { situation(content, progress, it) }
        return PathStatus(path, situations)
    }

    fun paths(content: ContentBundle, progress: Progress): List<PathStatus> =
        content.paths.sortedBy { it.order }.map { path(content, progress, it) }.filter { it.situations.isNotEmpty() }

    fun of(content: ContentBundle, progress: Progress, situationId: String): SituationStatus? {
        val situation = content.situations.firstOrNull { it.id == situationId } ?: return null
        return situation(content, progress, situation)
    }

    /**
     * The situation the app recommends next. A half-finished situation wins over a new one,
     * so the learner never has to remember where they stopped. Otherwise the profile decides:
     * a reason or an interest that matches the path moves that situation forward.
     */
    fun nextUp(content: ContentBundle, progress: Progress): SituationStatus? {
        val all = paths(content, progress).flatMap { it.situations }
        if (all.isEmpty()) return null

        all.firstOrNull { it.state == SituationState.STARTED }?.let { return it }

        val open = all.filter { it.state == SituationState.NEW }
        if (open.isEmpty()) return null
        val preferred = Personalization.preferredPaths(progress.profile)
        return open.firstOrNull { it.situation.pathId in preferred } ?: open.first()
    }

    /** What follows this situation inside its own path, used for the "danach" line. */
    fun nextInPath(content: ContentBundle, progress: Progress, situationId: String): SituationStatus? {
        val situation = content.situations.firstOrNull { it.id == situationId } ?: return null
        val pathDef = content.paths.firstOrNull { it.id == situation.pathId } ?: return null
        val statuses = path(content, progress, pathDef).situations
        val index = statuses.indexOfFirst { it.situation.id == situationId }
        if (index < 0) return null
        return statuses.drop(index + 1).firstOrNull { it.state != SituationState.DONE }
    }

    /** The lesson that was started and not finished, so the app can offer to continue it. */
    fun resumeLesson(content: ContentBundle, progress: Progress, today: Long = 0L): Pair<Lesson, Int>? {
        // New rounds carry their exact steps. The index is therefore independent of the day
        // and of any later change to the task mix.
        val session = progress.lessonSession
        if (session != null &&
            session.lessonId !in progress.completedLessons &&
            session.steps.isNotEmpty() &&
            session.index in 1 until session.steps.size
        ) {
            content.lessons.firstOrNull { it.id == session.lessonId }?.let { lesson ->
                return lesson to session.index
            }
        }

        // Old progress has no session yet. Keep that path working and compare against the
        // actual mixed round, not against the shorter authored core.
        val ordered = SessionBuilder.orderedLessons(content)
        ordered.forEach { lesson ->
            val reach = progress.lessonProgress[lesson.id] ?: 0
            val total = if (today > 0L) LessonMixer.stepCount(lesson, content, progress, today) else lesson.steps.size
            if (reach in 1 until total && lesson.id !in progress.completedLessons) {
                return lesson to reach
            }
        }
        return null
    }
}
