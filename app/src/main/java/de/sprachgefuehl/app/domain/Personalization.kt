package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.ContentBundle
import de.sprachgefuehl.app.data.Lesson
import de.sprachgefuehl.app.data.Persona
import de.sprachgefuehl.app.data.Profile
import de.sprachgefuehl.app.data.Progress

data class ProfileChoice(val id: String, val label: String, val paths: Set<String> = emptySet())

/** The labels live in one place so profile screen, home and tests agree. */
object ProfileCatalog {
    val reasons = listOf(
        ProfileChoice("familie", "Familie & Enkel", setOf("leute")),
        ProfileChoice("reisen", "Reisen", setOf("unterwegs")),
        ProfileChoice("alltag", "Alltag & Nachbarn", setOf("alltag", "einkaufen")),
        ProfileChoice("gesundheit", "Gesundheit", setOf("gesundheit")),
        ProfileChoice("kopf", "Kopf fit halten")
    )

    val interests = listOf(
        ProfileChoice("kochen", "Kochen & Essen", setOf("einkaufen")),
        ProfileChoice("reisen", "Reisen", setOf("unterwegs")),
        ProfileChoice("garten", "Garten", setOf("alltag")),
        ProfileChoice("musik", "Musik", setOf("leute")),
        ProfileChoice("nachrichten", "Nachrichten"),
        ProfileChoice("sport", "Sport", setOf("gesundheit"))
    )

    val goals = listOf(5, 10, 15, 20)
    val weekdayLabels = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")

    fun reason(id: String): ProfileChoice? = reasons.firstOrNull { it.id == id }
    fun reasonLabel(id: String): String = reason(id)?.label ?: ""
    fun interestLabels(ids: Set<String>): List<String> = interests.filter { it.id in ids }.map { it.label }

    fun weekdaysLabel(days: Set<Int>): String = when {
        days.isEmpty() -> "keine festen Tage"
        days.size == 7 -> "täglich"
        days == setOf(1, 2, 3, 4, 5) -> "Mo–Fr"
        days == setOf(6, 7) -> "am Wochenende"
        else -> weekdayLabels.filterIndexed { index, _ -> index + 1 in days }.joinToString(", ")
    }

    /** One line for the profile overview; the questions themselves have their own page. */
    fun summary(profile: Profile): String = listOf(
        reasonLabel(profile.reasonId),
        "${profile.dailyMinutes} Minuten am Tag",
        weekdaysLabel(profile.weekdays)
    ).filter { it.isNotBlank() }.joinToString(" · ")
}

data class LessonSuggestion(val lesson: Lesson, val why: String, val encounter: Persona?)

/**
 * The next lesson follows the path order, but a situation that matches the learner's reason
 * or interests is moved forward. The reason is shown so the recommendation never feels random.
 */
object Personalization {

    fun preferredPaths(profile: Profile): Set<String> {
        val paths = mutableSetOf<String>()
        ProfileCatalog.reason(profile.reasonId)?.let { paths += it.paths }
        ProfileCatalog.interests.filter { it.id in profile.interests }.forEach { paths += it.paths }
        return paths
    }

    fun suggestion(content: ContentBundle, progress: Progress): LessonSuggestion? {
        val remaining = SessionBuilder.orderedLessons(content).filter { it.id !in progress.completedLessons }
        if (remaining.isEmpty()) return null
        val paths = preferredPaths(progress.profile)
        val preferred = if (paths.isEmpty()) null else remaining.firstOrNull { lesson ->
            content.situations.firstOrNull { it.id == lesson.situationId }?.pathId in paths
        }
        val lesson = preferred ?: remaining.first()
        return LessonSuggestion(
            lesson = lesson,
            why = whyFor(progress.profile, lesson, content),
            encounter = encounterFor(content, lesson)
        )
    }

    private fun whyFor(profile: Profile, lesson: Lesson, content: ContentBundle): String {
        val path = content.situations.firstOrNull { it.id == lesson.situationId }?.pathId ?: return ""
        val reason = ProfileCatalog.reason(profile.reasonId)
        if (reason != null && path in reason.paths) return "Weil du für ${reason.label} übst"
        val interest = ProfileCatalog.interests.firstOrNull { it.id in profile.interests && path in it.paths }
        if (interest != null) return "Passt zu deinem Interesse: ${interest.label}"
        return ""
    }

    fun encounterFor(content: ContentBundle, lesson: Lesson): Persona? =
        content.personas.firstOrNull { lesson.situationId in it.situations }
            ?: content.personas.firstOrNull()

    fun greeting(name: String, hour: Int): String {
        val base = when (hour) {
            in 5..10 -> "Guten Morgen"
            in 11..17 -> "Guten Tag"
            else -> "Guten Abend"
        }
        return if (name.isBlank()) base else "$base, $name"
    }

    fun goalLine(minutes: Int, goal: Int): String = when {
        goal <= 0 -> "${Phrasing.minutes(minutes)} geübt"
        minutes >= goal -> "Tagesziel geschafft – für heute reicht es."
        minutes == 0 -> "$goal Minuten warten auf dich."
        else -> "$minutes von $goal Minuten"
    }
}
