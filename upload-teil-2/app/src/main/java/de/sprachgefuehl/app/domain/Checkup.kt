package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.CheckupItem
import de.sprachgefuehl.app.data.CheckupState
import de.sprachgefuehl.app.data.SkillProfile
import de.sprachgefuehl.app.data.SkillState
import kotlin.math.abs

/**
 * The standort check at the start. Five blocks, one per skill, each with an adaptive staircase:
 * two right answers in a row raise the difficulty, one wrong answer lowers it. A block ends after
 * six tasks, so nobody is asked the same thing forever.
 *
 * The rule is deliberately simple and explainable – an older learner should never feel tested by
 * a machine she cannot understand.
 */
object Checkup {

    const val BLOCK_SIZE = 5

    val order: List<Skill> = Skill.entries

    fun start(now: Long, selfRating: Int): CheckupState = CheckupState(
        startedAt = now,
        selfRating = selfRating.coerceIn(0, 2)
    )

    fun isFinished(state: CheckupState): Boolean = state.finished || state.blockIndex >= order.size

    fun skillOf(state: CheckupState): Skill? = order.getOrNull(state.blockIndex)

    fun blockNumber(state: CheckupState): Int = (state.blockIndex + 1).coerceIn(1, order.size)

    fun blockCount(): Int = order.size

    /** The next task: same skill, nearest to the current difficulty, never asked twice. */
    fun nextItem(state: CheckupState, items: List<CheckupItem>): CheckupItem? {
        if (isFinished(state)) return null
        val skill = skillOf(state) ?: return null
        val pool = items.filter { it.skill == skill.id && it.id !in state.answeredIds }
        if (pool.isEmpty()) return null
        return pool.minWithOrNull(
            compareBy({ abs(it.level - state.level) }, { it.level }, { it.id })
        )
    }

    fun answer(
        state: CheckupState,
        item: CheckupItem,
        correct: Boolean,
        items: List<CheckupItem>
    ): CheckupState {
        if (isFinished(state)) return state
        val results = state.results + (item.id to correct)
        val answeredIds = state.answeredIds + item.id
        var level = state.level
        var streak = state.streak
        if (correct) {
            streak += 1
            if (streak >= 2) {
                level = (level + 1).coerceAtMost(3)
                streak = 0
            }
        } else {
            level = (level - 1).coerceAtLeast(0)
            streak = 0
        }
        val answeredInBlock = state.answeredInBlock + 1
        val moved = state.copy(
            level = level,
            streak = streak,
            answeredInBlock = answeredInBlock,
            answeredIds = answeredIds,
            results = results
        )
        // Wer oben angekommen ist, muss nicht mehr fünfmal antworten: drei sichere Antworten
        // auf Stufe 3 genügen. Das hält den Start bei etwa 20 bis 25 Aufgaben.
        val settled = level >= 3 && answeredInBlock >= 3
        val blockDone = answeredInBlock >= BLOCK_SIZE || settled || nextItem(moved, items) == null
        if (!blockDone) return moved
        val nextBlock = moved.blockIndex + 1
        return moved.copy(
            blockIndex = nextBlock,
            level = 1,
            streak = 0,
            answeredInBlock = 0,
            finished = nextBlock >= order.size
        )
    }

    /**
     * Turns the answers of one block into a rating. Correct answers on a hard level count most;
     * the self rating is a light anchor, so a nervous first minute cannot decide the whole level.
     */
    fun ratingFor(
        profile: SkillProfile,
        state: CheckupState,
        items: List<CheckupItem>,
        now: Long
    ): SkillProfile {
        val byId = items.associateBy { it.id }
        var next = profile
        order.forEach { skill ->
            val answered = state.results.keys.mapNotNull { byId[it] }.filter { it.skill == skill.id }
            if (answered.isEmpty()) return@forEach
            val correctItems = answered.filter { state.results[it.id] == true }
            val accuracy = correctItems.size.toDouble() / answered.size
            val topLevel = correctItems.maxOfOrNull { it.level } ?: 0
            val test = (22.0 + 20.0 * topLevel + 22.0 * accuracy).coerceIn(0.0, 100.0)
            val anchor = 18.0 + 22.0 * state.selfRating.coerceIn(0, 2)
            val rating = (0.78 * test + 0.22 * anchor).coerceIn(0.0, 100.0)
            next = next.with(
                skill,
                SkillState(rating = rating, observations = answered.size.coerceAtLeast(2))
            )
        }
        return next.copy(measuredAt = now)
    }

    /** The old three-step level, kept for the profile label and for older code paths. */
    fun overallLevel(profile: SkillProfile): Int {
        val average = Skills.averageRating(profile)
        return when {
            average < 40.0 -> 0
            average < 65.0 -> 1
            else -> 2
        }
    }

    /** Without the check (skipped): a cautious start from the self rating alone. */
    fun fromSelfRating(profile: SkillProfile, selfRating: Int, now: Long): SkillProfile {
        val rating = 18.0 + 22.0 * selfRating.coerceIn(0, 2)
        var next = profile
        order.forEach { skill ->
            next = next.with(skill, SkillState(rating = rating, observations = 2))
        }
        return next.copy(measuredAt = now)
    }

    fun label(skill: Skill, state: CheckupState): String =
        "Teil ${blockNumber(state)} von ${blockCount()} · ${skill.tab}"
}
