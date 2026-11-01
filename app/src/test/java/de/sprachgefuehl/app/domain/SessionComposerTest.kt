package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.TestContent
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.SkillProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionComposerTest {

    private val content = TestContent.bundle

    private fun profileWith(vararg values: Pair<Skill, Int>): SkillProfile {
        var profile = SkillProfile()
        values.forEach { (skill, signal) ->
            repeat(3) { profile = Skills.observe(profile, skill, signal) }
        }
        return profile
    }

    @Test
    fun `no evidence means no focus`() {
        assertNull(SessionComposer.focus(content, Progress(onboarded = true), 0L))
    }

    @Test
    fun `a weak speaking skill leads to a speaking round`() {
        val progress = Progress(onboarded = true, skills = profileWith(Skill.SPRECHEN to 20, Skill.HOEREN to 88))
        val focus = SessionComposer.focus(content, progress, 0L)
        assertEquals(Skill.SPRECHEN, focus!!.skill)
        assertEquals(FocusTarget.PRACTICE, focus.target)
        assertTrue(focus.title.isNotBlank())
        assertTrue(focus.detail.isNotBlank())
    }

    @Test
    fun `a weak grammar skill names the pillar behind the errors`() {
        val card = content.errorCards.first { it.pillarId.isNotBlank() }
        val progress = Progress(
            onboarded = true,
            skills = profileWith(Skill.GRAMMATIK to 20, Skill.SPRECHEN to 88),
            errorCounts = mapOf(card.id to 3)
        )
        val focus = SessionComposer.focus(content, progress, 0L)!!
        assertEquals(Skill.GRAMMATIK, focus.skill)
        assertEquals(FocusTarget.WORKSHOP, focus.target)
        val pillar = content.pillars.first { it.id == card.pillarId }
        assertTrue("Fokus nennt die Säule", focus.title.contains(pillar.title) || focus.detail.contains(pillar.metaphor))
    }

    @Test
    fun `chunk and connector weaknesses lead to the right rooms`() {
        val chunks = Progress(onboarded = true, skills = profileWith(Skill.BAUSTEINE to 20, Skill.HOEREN to 88))
        assertEquals(FocusTarget.REVIEW, SessionComposer.focus(content, chunks, 0L)!!.target)

        val connect = Progress(onboarded = true, skills = profileWith(Skill.VERKNUEPFEN to 20, Skill.HOEREN to 88))
        assertEquals(FocusTarget.WORKSHOP, SessionComposer.focus(content, connect, 0L)!!.target)
    }
}
