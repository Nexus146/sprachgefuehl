package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Mistake
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.SkillProfile
import de.sprachgefuehl.app.data.SkillState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptivePlanTest {

    private val day = 20_000L

    @Test
    fun `the measured rating sets one level per skill`() {
        val progress = Progress(
            skills = SkillProfile(
                hoeren = SkillState(rating = 90.0, observations = 4),
                grammatik = SkillState(rating = 70.0, observations = 4),
                bausteine = SkillState(rating = 50.0, observations = 4),
                sprechen = SkillState(rating = 30.0, observations = 4)
            )
        )
        val target = AdaptivePlan.forProgress(progress, day)
        assertEquals(3, target.of(Skill.HOEREN))
        assertEquals(3, target.of(Skill.GRAMMATIK))
        assertEquals(1, target.of(Skill.BAUSTEINE))
        assertEquals(0, target.of(Skill.SPRECHEN))
    }

    @Test
    fun `a mistake lowers only its own skill`() {
        val progress = Progress(
            skills = SkillProfile(
                hoeren = SkillState(rating = 70.0, observations = 4),
                grammatik = SkillState(rating = 70.0, observations = 4)
            ),
            mistakes = listOf(Mistake(chunkId = "c1", skill = "grammatik", day = day - 1))
        )
        val target = AdaptivePlan.forProgress(progress, day)
        assertEquals("Hören darf nicht mit sinken", 3, target.of(Skill.HOEREN))
        assertEquals(1, target.of(Skill.GRAMMATIK))
    }

    @Test
    fun `three mistakes cost two levels`() {
        val progress = Progress(
            skills = SkillProfile(grammatik = SkillState(rating = 70.0, observations = 4)),
            mistakes = (0..2).map { Mistake(chunkId = "c$it", skill = "grammatik", day = day - it) }
        )
        assertEquals(0, AdaptivePlan.forProgress(progress, day).of(Skill.GRAMMATIK))
    }

    @Test
    fun `an old mistake no longer counts`() {
        val progress = Progress(
            skills = SkillProfile(grammatik = SkillState(rating = 50.0, observations = 4)),
            mistakes = listOf(
                Mistake(chunkId = "old", skill = "grammatik", day = day - AdaptivePlan.MISTAKE_WINDOW_DAYS - 1)
            )
        )
        assertEquals(1, AdaptivePlan.forProgress(progress, day).of(Skill.GRAMMATIK))
    }

    @Test
    fun `a mistake from this very lesson weighs extra`() {
        val base = Progress(skills = SkillProfile(grammatik = SkillState(rating = 50.0, observations = 4)))
        val lesson = de.sprachgefuehl.app.data.Lesson(
            id = "l1", situationId = "s1", title = "Test", goal = ""
        )
        val withOwn = base.copy(
            mistakes = listOf(
                Mistake(chunkId = "c1", lessonId = "l1", skill = "grammatik", day = day)
            )
        )
        assertEquals(1, AdaptivePlan.forProgress(base, day, lesson).of(Skill.GRAMMATIK))
        assertEquals(0, AdaptivePlan.forProgress(withOwn, day, lesson).of(Skill.GRAMMATIK))
    }

    @Test
    fun `the level never leaves the range`() {
        val strong = Progress(
            skills = SkillProfile(hoeren = SkillState(rating = 99.0, observations = 6))
        )
        val weak = Progress(
            skills = SkillProfile(hoeren = SkillState(rating = 5.0, observations = 6)),
            mistakes = (0..5).map { Mistake(chunkId = "c$it", skill = "hoeren", day = day) }
        )
        assertEquals(3, AdaptivePlan.forProgress(strong, day).of(Skill.HOEREN))
        assertEquals(0, AdaptivePlan.forProgress(weak, day).of(Skill.HOEREN))
    }

    @Test
    fun `without measurement the placement decides`() {
        assertEquals(0, AdaptivePlan.forProgress(Progress(placementLevel = 0, selfRating = 0), day).of(Skill.HOEREN))
        assertEquals(1, AdaptivePlan.forProgress(Progress(placementLevel = 1, selfRating = 1), day).of(Skill.HOEREN))
        assertEquals(2, AdaptivePlan.forProgress(Progress(placementLevel = 2, selfRating = 2), day).of(Skill.HOEREN))
    }

    @Test
    fun `the reason is said in plain words`() {
        val progress = Progress(
            skills = SkillProfile(grammatik = SkillState(rating = 50.0, observations = 4)),
            mistakes = listOf(Mistake(chunkId = "c1", skill = "grammatik", day = day))
        )
        val target = AdaptivePlan.forProgress(progress, day)
        assertTrue(target.noteFor(Skill.GRAMMATIK).contains("ruhiger"))
        assertTrue(target.noteFor(Skill.HOEREN).isEmpty())
    }
}
