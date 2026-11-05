package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.ContentBundle
import de.sprachgefuehl.app.data.Progress

/** Where a recommendation sends the learner. The screen mapping stays in the UI. */
enum class NextTarget { LESSON, REVIEW, PRACTICE, WORKSHOP, CHECKUP }

data class NextAction(
    val label: String,
    val target: NextTarget,
    val lessonId: String? = null,
    val minutes: Int = 0
)

enum class NextKind {
    /** The standort check is unfinished – it outranks everything, because it measures. */
    CHECKUP,

    /** A lesson was started and left; the app keeps the place. */
    CONTINUE,

    /** A new lesson in the learner's order. */
    NEW,

    /** Due sentences, because they are due or because little time is left. */
    REVIEW,

    /** A short round without new material. */
    PRACTICE,

    /** The daily goal is reached. Nothing is expected any more. */
    DONE
}

/**
 * Exactly one recommendation, with the reason why it is this one and not another. The home screen
 * shows this card first and asks nothing else – the learner should never have to weigh four
 * options against each other.
 */
data class NextStep(
    val kind: NextKind,
    /** "Heute dran", "Für heute geschafft", "Heute ist Pausentag". */
    val label: String,
    val title: String,
    val detail: String,
    val reason: String,
    val primary: NextAction,
    val alternative: NextAction?,
    val lessonId: String? = null,
    val situationId: String? = null
)

object NextPlanner {

    /** Below this many minutes left, a lesson no longer fits the goal. */
    const val SHORT_LEFTOVER_MINUTES = 5

    /** A backlog this size outweighs a new lesson. */
    const val REVIEW_BACKLOG = 8

    const val REVIEW_MINUTES = 5
    const val PRACTICE_MINUTES = 5
    const val CHECKUP_MINUTES = 4

    /**
     * The order follows what the learner would ask: "Wo war ich?" before "Was ist neu?", and
     * "Was ist fällig?" before "Noch ein Thema". The daily goal decides how long the answer may
     * be – with two minutes left, a nine-minute lesson is the wrong suggestion.
     */
    fun plan(content: ContentBundle, progress: Progress, now: Long): NextStep? {
        val today = now / Srs.DAY_MS
        val day = DailyPlan.summary(progress, today)
        val due = progress.chunkMemory.count { Srs.isDue(it.value, now) }
        val suggestion = Personalization.suggestion(content, progress)
        val resume = PathProgress.resumeLesson(content, progress)
        val restLabel = if (day.mood == DayMood.REST) "Heute ist Pausentag" else "Heute dran"

        // 1. An open standort check blocks every other recommendation: it decides what comes first.
        if (progress.onboarded && progress.checkup.startedAt > 0L && !Checkup.isFinished(progress.checkup)) {
            val block = progress.checkup.blockIndex + 1
            return NextStep(
                kind = NextKind.CHECKUP,
                label = restLabel,
                title = "Standortbestimmung fortsetzen",
                detail = "Du warst bei Teil $block von ${Checkup.blockCount()}.",
                reason = "Die Messung entscheidet, welche Bereiche zuerst drankommen.",
                primary = NextAction("Weitermessen", NextTarget.CHECKUP, minutes = CHECKUP_MINUTES),
                alternative = null
            )
        }

        // 2. The goal is reached. From here on the app stops nudging – both doors stay open.
        if (day.reachedGoal) {
            val continueAction = lessonAction(resume?.first, resume?.second, suggestion)
            return NextStep(
                kind = NextKind.DONE,
                label = "Für heute geschafft",
                title = "Für heute reicht es",
                detail = "${Phrasing.minutes(day.minutes)} geübt – dein Ziel waren ${Phrasing.minutes(day.goal)}.",
                reason = "Alles Weitere ist freiwillig. Der Garten hat heute genug Wasser.",
                primary = NextAction(
                    label = "Noch fünf Minuten üben",
                    target = NextTarget.PRACTICE,
                    minutes = PRACTICE_MINUTES
                ),
                alternative = continueAction,
                lessonId = null,
                situationId = null
            )
        }

        // 3. A half-finished lesson: the place is kept, so the app offers it back.
        if (resume != null) {
            val (lesson, step) = resume
            return lessonStep(
                content = content,
                lesson = lesson,
                step = step,
                totalSteps = LessonMixer.stepCount(lesson, content, progress, today),
                label = restLabel,
                due = due,
                // Wer mitten in einer Lektion steht, bekommt keine zweite Lektion angeboten.
                alternativeLesson = null
            )
        }

        // 4. A real backlog is worth more than one more story.
        if (due >= REVIEW_BACKLOG) {
            return NextStep(
                kind = NextKind.REVIEW,
                label = restLabel,
                title = "${Phrasing.sentences(due)} wiederholen",
                detail = "Sie sind heute fällig.",
                reason = "So viele sind es selten – eine Runde hält sie frisch.",
                primary = NextAction("Wiederholen", NextTarget.REVIEW, minutes = REVIEW_MINUTES),
                alternative = lessonAction(null, null, suggestion)
            )
        }

        // 5. Little of the goal is left: a short round fits, a lesson would not.
        val lastSuggestion = suggestion
        if (lastSuggestion != null && day.minutes > 0) {
            val left = day.goal - day.minutes
            if (left in 1..SHORT_LEFTOVER_MINUTES) {
                return if (due > 0) {
                    NextStep(
                        kind = NextKind.REVIEW,
                        label = restLabel,
                        title = "${Phrasing.sentences(due)} wiederholen",
                        detail = "Passt in die letzten Minuten bis zum Ziel.",
                        reason = "Für eine ganze Lektion ist heute nicht mehr viel Platz.",
                        primary = NextAction("Wiederholen", NextTarget.REVIEW, minutes = REVIEW_MINUTES),
                        alternative = lessonAction(null, null, lastSuggestion)
                    )
                } else {
                    NextStep(
                        kind = NextKind.PRACTICE,
                        label = restLabel,
                        title = "Fünf Minuten üben",
                        detail = "Eine kurze Runde, ohne neues Material.",
                        reason = "Für eine ganze Lektion ist heute nicht mehr viel Platz.",
                        primary = NextAction("Jetzt üben", NextTarget.PRACTICE, minutes = PRACTICE_MINUTES),
                        alternative = lessonAction(null, null, lastSuggestion)
                    )
                }
            }
        }

        // 6. The normal case: the next lesson in the learner's order.
        if (lastSuggestion != null) {
            return lessonStep(
                content = content,
                lesson = lastSuggestion.lesson,
                step = 0,
                totalSteps = LessonMixer.stepCount(lastSuggestion.lesson, content, progress, today),
                label = restLabel,
                due = due,
                alternativeLesson = null,
                why = lastSuggestion.why
            )
        }

        // 7. Everything is done. Only repetition is left, and that is a good place to be.
        return NextStep(
            kind = NextKind.REVIEW,
            label = restLabel,
            title = if (due > 0) "${Phrasing.sentences(due)} wiederholen" else "Sätze warm halten",
            detail = "Alle Begegnungen sind geschafft.",
            reason = "Wiederholen hält die Sätze im Zugriff – neue kommen später dazu.",
            primary = if (due > 0) {
                NextAction("Wiederholen", NextTarget.REVIEW, minutes = REVIEW_MINUTES)
            } else {
                NextAction("Jetzt üben", NextTarget.PRACTICE, minutes = PRACTICE_MINUTES)
            },
            alternative = NextAction("Fehler-Werkstatt", NextTarget.WORKSHOP)
        )
    }

    /** A lesson as the main step: either the place it was left at, or the start. */
    private fun lessonStep(
        content: ContentBundle,
        lesson: de.sprachgefuehl.app.data.Lesson,
        step: Int,
        totalSteps: Int,
        label: String,
        due: Int,
        alternativeLesson: de.sprachgefuehl.app.data.Lesson?,
        why: String = ""
    ): NextStep {
        val situation = content.situations.firstOrNull { it.id == lesson.situationId }
        val started = step in 1 until totalSteps
        val minutes = if (started) (lesson.xpMinutes - step).coerceAtLeast(2) else lesson.xpMinutes
        val reason = when {
            started -> "Du warst hier stehen geblieben – bei Schritt ${step + 1} von $totalSteps."
            why.isNotBlank() -> why
            else -> "Der Weg geht der Reihe nach – das ist die nächste Begegnung."
        }
        val alternative = when {
            due > 0 -> NextAction(
                label = "${Phrasing.sentences(due)} wiederholen",
                target = NextTarget.REVIEW,
                minutes = REVIEW_MINUTES
            )
            started -> NextAction("Fünf Minuten üben", NextTarget.PRACTICE, minutes = PRACTICE_MINUTES)
            alternativeLesson != null -> NextAction(
                label = "Andere Begegnung: ${alternativeLesson.title}",
                target = NextTarget.LESSON,
                lessonId = alternativeLesson.id,
                minutes = alternativeLesson.xpMinutes
            )
            else -> NextAction("Fünf Minuten üben", NextTarget.PRACTICE, minutes = PRACTICE_MINUTES)
        }
        return NextStep(
            kind = if (started) NextKind.CONTINUE else NextKind.NEW,
            label = label,
            title = if (situation != null) "${situation.title}: ${lesson.title}" else lesson.title,
            detail = lesson.goal,
            reason = reason,
            primary = NextAction(
                label = if (started) "Weitermachen" else "Weiterlernen",
                target = NextTarget.LESSON,
                lessonId = lesson.id,
                minutes = minutes
            ),
            alternative = alternative,
            lessonId = lesson.id,
            situationId = situation?.id
        )
    }

    private fun lessonAction(
        lesson: de.sprachgefuehl.app.data.Lesson?,
        step: Int?,
        suggestion: LessonSuggestion?
    ): NextAction? {
        val target = lesson ?: suggestion?.lesson ?: return null
        val started = (step ?: 0) in 1 until target.steps.size
        return NextAction(
            // Kurz halten: der Knopf trägt eine Handlung, nicht den Titel der Lektion.
            label = if (started) "Weitermachen bei Schritt ${(step ?: 0) + 1}" else "Weiterlernen",
            target = NextTarget.LESSON,
            lessonId = target.id,
            minutes = if (started) (target.xpMinutes - (step ?: 0)).coerceAtLeast(2) else target.xpMinutes
        )
    }
}
