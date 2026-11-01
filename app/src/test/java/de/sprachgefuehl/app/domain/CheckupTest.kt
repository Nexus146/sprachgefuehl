package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.TestContent
import de.sprachgefuehl.app.data.CheckupItem
import de.sprachgefuehl.app.data.CheckupState
import de.sprachgefuehl.app.data.SkillProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckupTest {

    private val items: List<CheckupItem> = TestContent.bundle.checkup

    private fun firstItem(state: CheckupState) = Checkup.nextItem(state, items)!!

    private fun answerN(state: CheckupState, correct: Boolean, n: Int): CheckupState {
        var current = state
        repeat(n) {
            val item = Checkup.nextItem(current, items) ?: return current
            current = Checkup.answer(current, item, correct, items)
        }
        return current
    }

    @Test
    fun `the item bank covers every skill and level`() {
        assertTrue("mindestens 40 Aufgaben", items.size >= 40)
        Skill.entries.forEach { skill ->
            listOf(0, 1, 2).forEach { level ->
                val count = items.count { it.skill == skill.id && it.level == level }
                assertTrue("$skill Stufe $level hat $count Aufgaben", count >= 3)
            }
        }
    }

    @Test
    fun `two right answers raise the level and one wrong answer lowers it`() {
        var state = Checkup.start(0L, 1)
        val startLevel = state.level
        state = Checkup.answer(state, firstItem(state), true, items)
        assertEquals("nach einer richtigen Aufgabe bleibt die Stufe", startLevel, state.level)
        state = Checkup.answer(state, firstItem(state), true, items)
        assertEquals(startLevel + 1, state.level)
        state = Checkup.answer(state, firstItem(state), false, items)
        assertEquals(startLevel, state.level)
    }

    @Test
    fun `a block ends after six tasks and never repeats one`() {
        var state = Checkup.start(0L, 1)
        state = answerN(state, false, Checkup.BLOCK_SIZE)
        assertEquals(1, state.blockIndex)
        assertEquals(Checkup.BLOCK_SIZE, state.answeredIds.distinct().size)
        assertEquals(Skill.GRAMMATIK, Checkup.skillOf(state))
    }

    @Test
    fun `the check ends after one block per skill`() {
        var state = Checkup.start(0L, 2)
        state = answerN(state, true, Checkup.BLOCK_SIZE * Skill.entries.size)
        assertTrue(Checkup.isFinished(state))
        assertNull(Checkup.nextItem(state, items))
        assertEquals(Skill.entries.size, state.blockIndex)
    }

    @Test
    fun `a paused check continues where it stopped`() {
        var state = Checkup.start(0L, 1)
        state = answerN(state, true, 3)
        val answered = state.answeredIds
        val next = Checkup.nextItem(state, items)!!
        assertTrue("bereits beantwortete Aufgabe kommt nicht wieder", next.id !in answered)
    }

    @Test
    fun `more right answers on a harder level give a higher rating`() {
        var strong = Checkup.start(0L, 1)
        var weak = Checkup.start(0L, 1)
        strong = answerN(strong, true, Checkup.BLOCK_SIZE * Skill.entries.size)
        weak = answerN(weak, false, Checkup.BLOCK_SIZE * Skill.entries.size)
        val strongProfile = Checkup.ratingFor(SkillProfile(), strong, items, 1L)
        val weakProfile = Checkup.ratingFor(SkillProfile(), weak, items, 1L)
        assertTrue(strongProfile.hoeren.rating > weakProfile.hoeren.rating + 20)
        assertTrue(Checkup.overallLevel(strongProfile) > Checkup.overallLevel(weakProfile))
    }

    @Test
    fun `skipping the check starts cautiously from the self rating`() {
        val low = Checkup.fromSelfRating(SkillProfile(), 0, 1L)
        val high = Checkup.fromSelfRating(SkillProfile(), 2, 1L)
        assertTrue(high.hoeren.rating > low.hoeren.rating)
        assertEquals(5, listOf(low.hoeren, low.grammatik, low.bausteine, low.sprechen, low.verknuepfen).size)
        assertEquals(1L, low.measuredAt)
        assertEquals(0, Checkup.overallLevel(low))
    }

    @Test
    fun `the label names the block`() {
        val state = Checkup.start(0L, 1)
        assertTrue(Checkup.label(Skill.HOEREN, state).contains("Teil 1"))
        assertEquals(5, Checkup.blockCount())
        assertNotNull(Checkup.nextItem(state, items))
    }
}
