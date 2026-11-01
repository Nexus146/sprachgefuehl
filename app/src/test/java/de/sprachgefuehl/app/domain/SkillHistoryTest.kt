package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.SkillProfile
import de.sprachgefuehl.app.data.SkillSnapshot
import de.sprachgefuehl.app.data.SkillState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SkillHistoryTest {

    private fun profile(sprechen: Double) = SkillProfile(
        hoeren = SkillState(60.0, 5),
        grammatik = SkillState(60.0, 5),
        bausteine = SkillState(60.0, 5),
        sprechen = SkillState(sprechen, 5),
        verknuepfen = SkillState(60.0, 5)
    )

    @Test
    fun `one snapshot per day`() {
        val first = SkillHistory.record(Progress(), now = Srs.DAY_MS * 10)
        assertEquals(1, first.skillHistory.size)
        val sameDay = SkillHistory.record(first, now = Srs.DAY_MS * 10 + 5_000)
        assertEquals(1, sameDay.skillHistory.size)
        val nextDay = SkillHistory.record(sameDay, now = Srs.DAY_MS * 11)
        assertEquals(2, nextDay.skillHistory.size)
    }

    @Test
    fun `movement compares the time before the week with today`() {
        val before = SkillSnapshot(day = 10, ratings = listOf(60.0, 60.0, 60.0, 30.0, 60.0))
        val progress = Progress(
            skills = profile(sprechen = 55.0),
            skillHistory = listOf(before, SkillSnapshot(day = 17, ratings = listOf(60.0, 60.0, 60.0, 55.0, 60.0)))
        )
        val movements = SkillHistory.movements(progress, now = Srs.DAY_MS * 17)
        assertEquals(1, movements.size)
        assertEquals(Skill.SPRECHEN, movements.first().skill)
        assertEquals(0, movements.first().from)
        assertEquals(1, movements.first().to)
        assertTrue(movements.first().better)
        assertTrue(movements.first().sentence().contains("Sprechen"))
    }

    @Test
    fun `a wobbly value inside one stage is no story`() {
        val before = SkillSnapshot(day = 10, ratings = listOf(60.0, 60.0, 60.0, 52.0, 60.0))
        val progress = Progress(
            skills = profile(sprechen = 61.0),
            skillHistory = listOf(before, SkillSnapshot(day = 17, ratings = listOf(60.0, 60.0, 60.0, 61.0, 60.0)))
        )
        assertTrue(SkillHistory.movements(progress, now = Srs.DAY_MS * 17).isEmpty())
    }

    @Test
    fun `without a second day nothing moved`() {
        val only = SkillSnapshot(day = 17, ratings = listOf(60.0, 60.0, 60.0, 30.0, 60.0))
        val progress = Progress(skills = profile(sprechen = 55.0), skillHistory = listOf(only))
        assertTrue(SkillHistory.movements(progress, now = Srs.DAY_MS * 17).isEmpty())
    }
}
