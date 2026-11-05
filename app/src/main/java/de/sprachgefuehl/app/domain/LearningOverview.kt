package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.ContentBundle
import de.sprachgefuehl.app.data.Lesson
import de.sprachgefuehl.app.data.Progress

/** Wie groß der Pool ist und wie viel davon schon im Gedächtnis angekommen ist. */
data class PoolFacts(
    val chunks: Int,
    val lessons: Int,
    val patterns: Int,
    val formats: Int,
    val collected: Int,
    val stable: Int,
    val learning: Int,
    val due: Int,
    val openMistakes: Int
)

/** Ein Format und wie oft es in der nächsten Runde vorkommt. */
data class FormatShare(val label: String, val count: Int, val skill: Skill?)

data class WeekFacts(
    val activeDays: Int,
    val minutes: Int,
    val speakingTasks: Int,
    val sessions: Int,
    val newChunks: Int,
    val previousMinutes: Int,
    val goal: Int
)

data class MistakeView(
    val target: String,
    val origin: String,
    val heard: String,
    val skill: Skill?
)

/**
 * Alles, was die Übersicht braucht – an einer Stelle berechnet, damit Start, Stand und Tests
 * dieselben Zahlen sehen. Bewusst ohne Prozentwerte: Stufen und bewegte Bereiche erzählen mehr.
 */
data class LearningOverview(
    val pool: PoolFacts,
    val week: WeekFacts,
    val skills: List<SkillView>,
    val movements: List<SkillMovement>,
    val focus: List<String>,
    val next: NextStep?,
    val nextSteps: Int,
    val nextMix: List<FormatShare>,
    val mistakes: List<MistakeView>,
    val nextLessonTitle: String,
    val completedLessons: Int,
    val activeDaysTotal: Int
) {
    companion object {

        fun of(content: ContentBundle, progress: Progress, now: Long): LearningOverview {
            val today = now / Srs.DAY_MS
            val days = (0..6).map { today - 6 + it }
            val sessions = progress.sessions.filter { it.day in days }
            val memories = progress.chunkMemory.values
            val next = NextPlanner.plan(content, progress, now)
            val nextLesson = lessonFor(next, content)
            val steps = nextLesson
                ?.let { LessonMixer.steps(it, content, progress, today) }
                .orEmpty()
            val mix = steps
                .mapNotNull { TaskFormats.of(it.type) }
                .groupingBy { it }
                .eachCount()
                .map { (format, count) -> FormatShare(format.label, count, format.skill) }
                .sortedWith(compareByDescending<FormatShare> { it.count }.thenBy { it.label })

            return LearningOverview(
                pool = PoolFacts(
                    chunks = content.chunks.size,
                    lessons = content.lessons.size,
                    patterns = content.patterns.size,
                    formats = TaskFormats.all.size,
                    collected = progress.chunkMemory.size,
                    stable = memories.count { it.stage >= 4 },
                    learning = memories.count { it.reps > 0 && it.stage < 4 },
                    due = progress.chunkMemory.count { Srs.isDue(it.value, now) },
                    openMistakes = progress.mistakes.size
                ),
                week = WeekFacts(
                    activeDays = days.count { progress.measuredMinutesOn(it) > 0 },
                    minutes = days.sumOf { progress.measuredMinutesOn(it) },
                    speakingTasks = sessions.sumOf { it.speakingTasks },
                    sessions = sessions.size,
                    newChunks = sessions.sumOf { it.newChunks },
                    previousMinutes = days.sumOf { progress.measuredMinutesOn(it - 7) },
                    goal = progress.profile.dailyMinutes.takeIf { it > 0 } ?: 10
                ),
                skills = Skills.views(progress.skills),
                movements = SkillHistory.movements(progress, now),
                focus = ProfileCatalog.focusLabels(
                    progress.profile.reasons() + progress.profile.interests
                ),
                next = next,
                nextSteps = steps.size,
                nextMix = mix,
                mistakes = progress.mistakes
                    .sortedByDescending { it.day }
                    .take(3)
                    .map { mistake ->
                        MistakeView(
                            target = mistake.target.ifBlank {
                                content.chunks.firstOrNull { it.id == mistake.chunkId }?.en.orEmpty()
                            },
                            origin = mistake.lessonTitle,
                            heard = mistake.heard,
                            skill = Skill.fromId(mistake.skill)
                        )
                    },
                nextLessonTitle = nextLesson?.title.orEmpty(),
                completedLessons = progress.completedLessons.size,
                activeDaysTotal = progress.activeDays
            )
        }

        /** Nur eine echte Lektion bekommt eine Vorschau – Wiederholen hat keine Formate. */
        private fun lessonFor(next: NextStep?, content: ContentBundle): Lesson? {
            if (next == null || next.primary.target != NextTarget.LESSON) return null
            val id = next.primary.lessonId ?: next.lessonId
            return content.lessons.firstOrNull { it.id == id }
        }
    }
}
