package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.SkillSnapshot

/** How one area moved in the last seven days – told in stages, never in percentages. */
data class SkillMovement(val skill: Skill, val from: Int, val to: Int) {
    val better: Boolean get() = to > from

    fun sentence(): String = if (better) {
        "${skill.tab}: von „${SkillCatalog.stageLabel(from)}“ zu „${SkillCatalog.stageLabel(to)}“"
    } else {
        "${skill.tab} ist gerade wieder bei „${SkillCatalog.stageLabel(to)}“ – das pendelt sich ein"
    }
}

/**
 * A daily snapshot of the five areas. Without it the app can only say where the learner stands,
 * never that something moved – and "diese Woche ist Sprechen gewachsen" is the sentence that
 * makes a week of small steps visible.
 */
object SkillHistory {

    const val MAX_ENTRIES = 60
    const val WINDOW_DAYS = 7L

    /** One entry per day, written with the first save of that day. */
    fun record(progress: Progress, now: Long): Progress {
        val day = now / Srs.DAY_MS
        if (progress.skillHistory.lastOrNull()?.day == day) return progress
        val snapshot = SkillSnapshot(day, Skill.entries.map { progress.skills.state(it).rating })
        return progress.copy(skillHistory = (progress.skillHistory + snapshot).takeLast(MAX_ENTRIES))
    }

    /** The state before the last seven days, or the oldest snapshot there is. */
    fun baseline(progress: Progress, now: Long): SkillSnapshot? {
        val cutoff = now / Srs.DAY_MS - WINDOW_DAYS
        return progress.skillHistory.lastOrNull { it.day <= cutoff }
            ?: progress.skillHistory.firstOrNull()
    }

    /** Only areas that really changed a stage; a wobble inside one stage is not a story. */
    fun movements(progress: Progress, now: Long): List<SkillMovement> {
        if (progress.skillHistory.size < 2) return emptyList()
        val base = baseline(progress, now) ?: return emptyList()
        if (base.day == now / Srs.DAY_MS) return emptyList()
        return Skill.entries.mapNotNull { skill ->
            val index = Skill.entries.indexOf(skill)
            val fromRating = base.ratings.getOrNull(index) ?: return@mapNotNull null
            val from = SkillCatalog.stage(fromRating)
            val to = SkillCatalog.stage(progress.skills.state(skill).rating)
            if (from == to) null else SkillMovement(skill, from, to)
        }.sortedByDescending { it.to - it.from }
    }
}
