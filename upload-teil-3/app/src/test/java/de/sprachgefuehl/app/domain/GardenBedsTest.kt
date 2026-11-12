package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.SessionLog
import de.sprachgefuehl.app.data.SkillProfile
import de.sprachgefuehl.app.data.SkillState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GardenBedsTest {

    private fun bed(progress: Progress, skill: Skill) = Garden.beds(progress).first { it.skill == skill }

    @Test
    fun `every skill gets its own bed and its own plant`() {
        val beds = Garden.beds(Progress())
        assertEquals(Skill.entries, beds.map { it.skill })
        assertEquals(Skill.entries.size, beds.map { it.plant.id }.toSet().size)
        assertEquals("Sonnenblume", bed(Progress(), Skill.HOEREN).plant.name)
        assertEquals("Rose", bed(Progress(), Skill.GRAMMATIK).plant.name)
        assertEquals("Lavendel", bed(Progress(), Skill.BAUSTEINE).plant.name)
        assertEquals("Tulpe", bed(Progress(), Skill.SPRECHEN).plant.name)
        assertEquals("Efeu", bed(Progress(), Skill.VERKNUEPFEN).plant.name)
    }

    @Test
    fun `a bed without observations stays empty and says so`() {
        val b = bed(Progress(), Skill.HOEREN)
        assertFalse(b.observed)
        assertEquals(0, b.stage)
        assertEquals(0f, b.growth, 0.001f)
        assertEquals("Wartet auf dich", b.note)
        assertEquals("Bis „Trieb“: eine erste Aufgabe in diesem Bereich", b.nextLine)
    }

    @Test
    fun `the refinement follows ability and experience together`() {
        fun stage(rating: Double, observations: Int) = bed(
            Progress(skills = SkillProfile(hoeren = SkillState(rating = rating, observations = observations))),
            Skill.HOEREN
        ).stage
        assertEquals(1, stage(30.0, 3))
        assertEquals(2, stage(50.0, 3))
        assertEquals(3, stage(70.0, 3))
        assertEquals(3, stage(90.0, 3))
        assertEquals(4, stage(90.0, 8))
    }

    @Test
    fun `a bloom needs six observations and then has no next step`() {
        val bloom = bed(Progress(skills = SkillProfile(hoeren = SkillState(90.0, 8))), Skill.HOEREN)
        assertEquals(1f, bloom.growth, 0.001f)
        assertEquals("Blüte", bloom.note)
        assertNull(bloom.nextLine)
        val bud = bed(Progress(skills = SkillProfile(hoeren = SkillState(90.0, 3))), Skill.HOEREN)
        assertEquals("Knospe", bud.note)
        assertEquals("Bis „Blüte“: noch etwas Übung, bis es ohne Nachdenken geht", bud.nextLine)
    }

    @Test
    fun `only the observed skill grows its bed`() {
        val progress = Progress(skills = SkillProfile(grammatik = SkillState(rating = 80.0, observations = 2)))
        assertTrue(bed(progress, Skill.GRAMMATIK).stage > 0)
        assertEquals(0, bed(progress, Skill.SPRECHEN).stage)
    }

    @Test
    fun `residents come with a blooming skill or a standing feature`() {
        val empty = Garden.residents(Progress())
        assertTrue(empty.none { it.present })
        val blooming = Garden.residents(Progress(skills = SkillProfile(sprechen = SkillState(90.0, 8))))
        assertTrue(blooming.first { it.id == "schmetterling" }.present)
        assertFalse(blooming.first { it.id == "amsel" }.present)
        val fiveRounds = Garden.residents(Progress(gardenRounds = 5))
        assertTrue(fiveRounds.first { it.id == "libelle" }.present)
        assertFalse(fiveRounds.first { it.id == "igel" }.present)
        val tenRounds = Garden.residents(Progress(gardenRounds = 10))
        assertTrue(tenRounds.first { it.id == "igel" }.present)
    }

    @Test
    fun `the chronicle shows the latest rounds without dates`() {
        assertTrue(Garden.chronicle(Progress()).isEmpty())
        val progress = Progress(sessions = listOf(SessionLog(1, 12, 4, 2), SessionLog(2, 1, 1, 0)))
        assertEquals(
            listOf("1 Minute · 1 Satz gesprochen · 0 neu", "12 Minuten · 4 Sätze gesprochen · 2 neu"),
            Garden.chronicle(progress)
        )
        val many = Progress(sessions = (1L..6L).map { SessionLog(it, 5, 2, 1) })
        assertEquals(4, Garden.chronicle(many).size)
    }

    @Test
    fun `garden features arrive with rounds and only add up`() {
        assertTrue(Garden.features(Progress()).none { it.reached })
        val ten = Garden.features(Progress(gardenRounds = 10))
        assertEquals(listOf("bank", "teich", "baum"), ten.filter { it.reached }.map { it.id })
        assertTrue(Garden.features(Progress(gardenRounds = 30)).all { it.reached })
    }

    @Test
    fun `the next feature names the missing rounds`() {
        assertEquals("Noch 4 Runden bis „Der Teich“", Garden.nextFeature(Progress(gardenRounds = 1)))
        assertNull(Garden.nextFeature(Progress(gardenRounds = 40)))
    }
}
