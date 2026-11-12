package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Memory
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.SkillProfile
import de.sprachgefuehl.app.data.SkillState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeCatalogTest {

    private fun profileWithWeak(skill: Skill): SkillProfile {
        val strong = SkillState(rating = 72.0, observations = 6)
        val weak = SkillState(rating = 28.0, observations = 6)
        return SkillProfile(
            hoeren = strong,
            grammatik = strong,
            bausteine = strong,
            sprechen = strong,
            verknuepfen = strong
        ).with(skill, weak)
    }

    @Test
    fun `every mode has a title, a description and at least one skill`() {
        assertEquals(PracticeCatalog.modes.size, PracticeCatalog.modes.map { it.id }.distinct().size)
        PracticeCatalog.modes.forEach { mode ->
            assertTrue("${mode.id} ohne Bereich", mode.skills.isNotEmpty())
            assertTrue("${mode.id} ohne Titel", mode.title.isNotBlank())
            assertTrue("${mode.id} ohne Beschreibung", mode.subtitle.isNotBlank())
            assertTrue("${mode.id} ohne Zeile", mode.trainsLine.isNotBlank())
        }
    }

    @Test
    fun `recommendation follows the weakest skill`() {
        assertEquals("shadow", PracticeCatalog.recommended(Progress(skills = profileWithWeak(Skill.SPRECHEN))).id)
        assertEquals("free", PracticeCatalog.recommended(Progress(skills = profileWithWeak(Skill.VERKNUEPFEN))).id)
        assertEquals("dictation", PracticeCatalog.recommended(Progress(skills = profileWithWeak(Skill.HOEREN))).id)
        assertEquals("order", PracticeCatalog.recommended(Progress(skills = profileWithWeak(Skill.GRAMMATIK))).id)
    }

    @Test
    fun `without evidence the gentlest mode is offered`() {
        assertEquals("shadow", PracticeCatalog.recommended(Progress()).id)
    }

    @Test
    fun `only a mode that asks for the voice counts as speaking`() {
        assertTrue(PracticeCatalog.mode("shadow").speaks)
        assertTrue(PracticeCatalog.mode("free").speaks)
        assertFalse(PracticeCatalog.mode("dictation").speaks)
        assertFalse(PracticeCatalog.mode("order").speaks)
    }

    @Test
    fun `standing counts what is due and shaky`() {
        val progress = Progress(
            chunkMemory = mapOf(
                "a" to Memory(stage = 1, reps = 2, due = 0L),
                "b" to Memory(stage = 4, reps = 5, due = 10_000L, stability = 20.0)
            )
        )
        val standing = PracticeCatalog.standing(progress, now = 1_000L)
        assertEquals(1, standing.due)
        assertEquals(1, standing.shaky)
        assertEquals(2, standing.learned)
        assertTrue(standing.line.contains("fällig"))
    }

    @Test
    fun `an empty profile gets a friendly standing`() {
        val standing = PracticeCatalog.standing(Progress(), now = 1_000L)
        assertEquals(0, standing.due)
        assertEquals(0, standing.learned)
        assertTrue(standing.line.contains("Noch keine Sätze"))
        assertEquals("shadow", standing.mode.id)
    }

    @Test
    fun `an unknown mode falls back to the first entry`() {
        assertEquals(PracticeCatalog.modes.first(), PracticeCatalog.mode("gibtsnicht"))
    }
}
