package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.SkillProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SkillsTest {

    @Test
    fun `the first observation moves the estimate most`() {
        val start = SkillProfile()
        val afterOne = Skills.observe(start, Skill.SPRECHEN, 90)
        val firstMove = afterOne.sprechen.rating - start.sprechen.rating
        val afterTwo = Skills.observe(afterOne, Skill.SPRECHEN, 90)
        val secondMove = afterTwo.sprechen.rating - afterOne.sprechen.rating
        assertTrue("erste Beobachtung bewegt stark", firstMove > 20)
        assertTrue("zweite bewegt weniger", secondMove < firstMove)
    }

    @Test
    fun `one bad day does not undo weeks of work`() {
        var profile = SkillProfile()
        repeat(12) { profile = Skills.observe(profile, Skill.GRAMMATIK, 88) }
        val before = profile.grammatik.rating
        profile = Skills.observe(profile, Skill.GRAMMATIK, 20)
        assertTrue("kaum Bewegung nach vielen Belegen", before - profile.grammatik.rating < 10)
    }

    @Test
    fun `signals and grades stay inside the scale`() {
        assertEquals(91, Skills.signal(true, 10))
        assertTrue(Skills.signal(false, 0) < Skills.signal(true, 0))
        assertTrue(Skills.gradeSignal(3, false) > Skills.gradeSignal(2, false))
        assertTrue(Skills.gradeSignal(3, true) < Skills.gradeSignal(3, false))
    }

    @Test
    fun `stages follow the rating`() {
        assertEquals(0, SkillCatalog.stage(10.0))
        assertEquals(1, SkillCatalog.stage(50.0))
        assertEquals(2, SkillCatalog.stage(70.0))
        assertEquals(3, SkillCatalog.stage(90.0))
        assertEquals(4, SkillCatalog.stageLabels.size)
    }

    @Test
    fun `an unmeasured profile has no weak skill`() {
        assertNull(Skills.weakest(SkillProfile()))
        assertEquals(5, Skills.views(SkillProfile()).size)
        assertTrue(Skills.views(SkillProfile()).none { it.observed })
    }

    @Test
    fun `the weakest skill wins over a strong one`() {
        var profile = SkillProfile()
        repeat(3) { profile = Skills.observe(profile, Skill.SPRECHEN, 90) }
        repeat(3) { profile = Skills.observe(profile, Skill.GRAMMATIK, 25) }
        assertEquals(Skill.GRAMMATIK, Skills.weakest(profile))
    }
}
