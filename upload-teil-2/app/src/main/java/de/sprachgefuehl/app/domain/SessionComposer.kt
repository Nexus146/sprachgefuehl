package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.ContentBundle
import de.sprachgefuehl.app.data.Progress

enum class FocusTarget { PRACTICE, REVIEW, WORKSHOP, LESSON }

/**
 * The one extra block that fits today. It names the weakest skill in plain words and says where
 * to go – no hidden algorithm, and no second lesson if the learner only wants to repeat.
 */
data class FocusBlock(
    val skill: Skill,
    val title: String,
    val detail: String,
    val target: FocusTarget
)

object SessionComposer {

    /** Null while the app has no evidence yet: guessing would feel arbitrary. */
    fun focus(content: ContentBundle, progress: Progress, now: Long): FocusBlock? {
        val skill = Skills.weakest(progress.skills) ?: return null
        val stage = SkillCatalog.stage(progress.skills.state(skill).rating)
        return when (skill) {
            Skill.SPRECHEN -> FocusBlock(
                skill = skill,
                title = "Fünf Minuten sprechen",
                detail = SkillCatalog.nextStep(skill, stage),
                target = FocusTarget.PRACTICE
            )
            Skill.BAUSTEINE -> FocusBlock(
                skill = skill,
                title = "Bausteine wiederholen",
                detail = SkillCatalog.nextStep(skill, stage),
                target = FocusTarget.REVIEW
            )
            Skill.HOEREN -> FocusBlock(
                skill = skill,
                title = "Hören üben",
                detail = SkillCatalog.nextStep(skill, stage),
                target = FocusTarget.LESSON
            )
            Skill.VERKNUEPFEN -> FocusBlock(
                skill = skill,
                title = "Sätze verbinden",
                detail = SkillCatalog.nextStep(skill, stage),
                target = FocusTarget.WORKSHOP
            )
            Skill.GRAMMATIK -> grammarFocus(content, progress, stage)
        }
    }

    /**
     * The grammar block starts at the pillar behind the most frequent error. If nothing has gone
     * wrong yet, it takes the pillar of the next lesson instead of inventing one.
     */
    private fun grammarFocus(content: ContentBundle, progress: Progress, stage: Int): FocusBlock {
        val fromErrors = progress.errorCounts.entries
            .sortedByDescending { it.value }
            .mapNotNull { (id, _) -> content.errorCards.firstOrNull { it.id == id }?.pillarId }
            .firstOrNull { it.isNotBlank() }
        val fromLesson = SessionBuilder.nextLesson(content, progress)?.pillarId
        val pillar = content.pillars.firstOrNull { it.id == (fromErrors ?: fromLesson) }
        return FocusBlock(
            skill = Skill.GRAMMATIK,
            title = pillar?.let { "Grammatik: ${it.title}" } ?: "Grammatik festigen",
            detail = pillar?.metaphor?.takeIf { it.isNotBlank() }?.plus(" · ")?.plus(
                SkillCatalog.nextStep(Skill.GRAMMATIK, stage)
            ) ?: SkillCatalog.nextStep(Skill.GRAMMATIK, stage),
            target = FocusTarget.WORKSHOP
        )
    }
}
