package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.ContentBundle
import de.sprachgefuehl.app.data.Lesson
import de.sprachgefuehl.app.data.Persona
import de.sprachgefuehl.app.data.Profile
import de.sprachgefuehl.app.data.Progress

/**
 * One answer with its consequence. [paths] moves situations forward, [storyTags] decides which
 * story a situation tells, [effect] says in plain words what the choice does – a question without
 * a visible consequence does not belong in the app.
 */
data class ProfileChoice(
    val id: String,
    val label: String,
    val paths: Set<String> = emptySet(),
    val storyTags: Set<String> = emptySet(),
    val effect: String = "",
    /** Was diese Antwort beim Üben besonders häufig macht. */
    val focusSkills: Set<Skill> = emptySet()
)

/** The labels live in one place so profile screen, home and tests agree. */
object ProfileCatalog {
    val reasons = listOf(
        ProfileChoice(
            id = "familie",
            label = "Familie & Enkel",
            paths = setOf("leute", "feste"),
            effect = "Begegnungen mit Familie, Nachbarn und Festen kommen zuerst.",
            focusSkills = setOf(Skill.SPRECHEN, Skill.BAUSTEINE)
        ),
        ProfileChoice(
            id = "reisen",
            label = "Reisen",
            paths = setOf("unterwegs"),
            storyTags = setOf("reisen"),
            effect = "Begegnungen unterwegs – Bus, Bahn, Weg fragen – kommen zuerst.",
            focusSkills = setOf(Skill.HOEREN, Skill.SPRECHEN)
        ),
        ProfileChoice(
            id = "alltag",
            label = "Alltag & Nachbarn",
            paths = setOf("alltag", "einkaufen"),
            effect = "Alltag, Nachbarschaft und Einkaufen kommen zuerst.",
            focusSkills = setOf(Skill.SPRECHEN, Skill.BAUSTEINE)
        ),
        ProfileChoice(
            id = "gesundheit",
            label = "Gesundheit",
            paths = setOf("gesundheit"),
            storyTags = setOf("gesundheit"),
            effect = "Arzt, Apotheke und Gesundheit kommen zuerst.",
            focusSkills = setOf(Skill.HOEREN, Skill.BAUSTEINE)
        ),
        ProfileChoice(
            id = "kopf",
            label = "Kopf fit halten",
            paths = setOf("werkstatt", "leute"),
            effect = "Muster und Grammatik-Werkstatt kommen zuerst – Gespräche bleiben dabei.",
            focusSkills = setOf(Skill.GRAMMATIK, Skill.VERKNUEPFEN)
        )
    )

    /**
     * Only interests with a real effect: every one of them moves situations forward and changes
     * the story. "Nachrichten" was dropped for that reason – it changed nothing.
     */
    val interests = listOf(
        ProfileChoice(
            id = "kochen",
            label = "Kochen & Essen",
            paths = setOf("einkaufen"),
            storyTags = setOf("kochen"),
            effect = "Geschichten spielen öfter im Café, beim Essen und beim Einkaufen.",
            focusSkills = setOf(Skill.BAUSTEINE, Skill.GRAMMATIK)
        ),
        ProfileChoice(
            id = "reisen",
            label = "Reisen",
            paths = setOf("unterwegs"),
            storyTags = setOf("reisen"),
            effect = "Geschichten spielen öfter unterwegs – Bus, Bahnhof, Hotel.",
            focusSkills = setOf(Skill.HOEREN, Skill.SPRECHEN)
        ),
        ProfileChoice(
            id = "garten",
            label = "Garten",
            paths = setOf("alltag"),
            storyTags = setOf("garten"),
            effect = "Geschichten spielen öfter im Haus, im Hof und im Garten.",
            focusSkills = setOf(Skill.BAUSTEINE, Skill.GRAMMATIK)
        ),
        ProfileChoice(
            id = "musik",
            label = "Musik",
            paths = setOf("leute", "feste"),
            storyTags = setOf("musik"),
            effect = "Geschichten spielen öfter bei Festen und im Gespräch.",
            focusSkills = setOf(Skill.SPRECHEN, Skill.BAUSTEINE)
        ),
        ProfileChoice(
            id = "sport",
            label = "Bewegung & Gesundheit",
            paths = setOf("gesundheit"),
            storyTags = setOf("gesundheit"),
            effect = "Geschichten spielen öfter bei Gesundheit, Apotheke und Bewegung.",
            focusSkills = setOf(Skill.HOEREN, Skill.BAUSTEINE)
        )
    )

    val goals = listOf(5, 10, 15, 20)
    val weekdayLabels = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")

    fun reason(id: String): ProfileChoice? = reasons.firstOrNull { it.id == id }
    fun reasonLabel(id: String): String = reason(id)?.label ?: ""
    fun reasonLabels(ids: Set<String>): List<String> = reasons.filter { it.id in ids }.map { it.label }

    /** Was alle gewählten Gründe zusammen ändern – ein Satz pro Grund. */
    fun reasonEffects(ids: Set<String>): List<String> = reasons.filter { it.id in ids }.map { it.effect }
    fun interestLabels(ids: Set<String>): List<String> = interests.filter { it.id in ids }.map { it.label }

    /**
     * Die gewählten Antworten übersetzt in das, was sie beim Üben häufiger machen. Gründe und
     * Interessen teilen sich dieselben IDs (z. B. „reisen“), deshalb eine Menge.
     */
    fun focusSkills(ids: Set<String>): Set<Skill> =
        (reasons + interests).filter { it.id in ids }.flatMap { it.focusSkills }.toSet()

    /** In der Reihenfolge der fünf Bereiche – für „Übt besonders: Hören · Sprechen“. */
    fun focusLabels(ids: Set<String>): List<String> {
        val focus = focusSkills(ids)
        return Skill.entries.filter { it in focus }.map { it.tab }
    }

    /** What the chosen reason actually changes, in one sentence. */
    fun reasonEffect(id: String): String = reason(id)?.effect.orEmpty()

    /** What the chosen interests actually change – one sentence each. */
    fun interestEffects(ids: Set<String>): List<String> = interests.filter { it.id in ids }.map { it.effect }

    /**
     * The interests translated into the tags the stories carry. "Bewegung & Gesundheit" should
     * open the health stories, even though the story itself is tagged "gesundheit".
     */
    fun storyTags(ids: Set<String>): Set<String> =
        (interests.filter { it.id in ids }.flatMap { it.storyTags } +
            reasons.filter { it.id in ids }.flatMap { it.storyTags }).toSet()

    fun weekdaysLabel(days: Set<Int>): String = when {
        days.isEmpty() -> "keine festen Tage"
        days.size == 7 -> "täglich"
        days == setOf(1, 2, 3, 4, 5) -> "Mo–Fr"
        days == setOf(6, 7) -> "am Wochenende"
        else -> weekdayLabels.filterIndexed { index, _ -> index + 1 in days }.joinToString(", ")
    }

    /** One line for the profile overview; the questions themselves have their own page. */
    fun summary(profile: Profile): String = listOf(
        reasonLabels(profile.reasons()).joinToString(", "),
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

    /** The story tags a profile asks for – reason and interests, both translated. */
    fun storyTagsFor(profile: Profile): Set<String> =
        ProfileCatalog.storyTags(profile.interests + profile.reasons())

    fun preferredPaths(profile: Profile): Set<String> {
        val paths = mutableSetOf<String>()
        profile.reasons().forEach { id -> ProfileCatalog.reason(id)?.let { paths += it.paths } }
        ProfileCatalog.interests.filter { it.id in profile.interests }.forEach { paths += it.paths }
        return paths
    }

    fun suggestion(content: ContentBundle, progress: Progress): LessonSuggestion? {
        val remaining = SessionBuilder.orderedLessons(content).filter { it.id !in progress.completedLessons }
        if (remaining.isEmpty()) return null
        // Eine angefangene Situation gewinnt: sie ist der Ort, an dem die Lernende steht. Sonst
        // gäbe es zwei verschiedene Antworten auf "was kommt als Nächstes" – eine hier, eine in
        // der Bereichsübersicht.
        val started = PathProgress.nextUp(content, progress)?.nextLesson
            ?.let { next -> remaining.firstOrNull { it.id == next.id } }
        val paths = preferredPaths(progress.profile)
        val preferred = if (paths.isEmpty()) null else remaining.firstOrNull { lesson ->
            content.situations.firstOrNull { it.id == lesson.situationId }?.pathId in paths
        }
        val lesson = started ?: preferred ?: remaining.first()
        return LessonSuggestion(
            lesson = lesson,
            why = whyFor(progress.profile, lesson, content),
            encounter = encounterFor(content, lesson)
        )
    }

    private fun whyFor(profile: Profile, lesson: Lesson, content: ContentBundle): String {
        val path = content.situations.firstOrNull { it.id == lesson.situationId }?.pathId ?: return ""
        val reason = profile.reasons().mapNotNull { ProfileCatalog.reason(it) }
            .firstOrNull { path in it.paths }
        if (reason != null) return "Weil du für ${reason.label} übst"
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
