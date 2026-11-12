package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Progress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GardenAndPlacementTest {

    @Test
    fun `a session grows the garden`() {
        val progress = Garden.recordSession(Progress(), day = 1, minutes = 10, speakingTasks = 3, newChunks = 2)
        assertEquals(1, progress.sessions.size)
        assertEquals(1, Garden.stage(progress))
        assertTrue(progress.gardenGrowth > 0f)
    }

    @Test
    fun `a missed day never takes progress away`() {
        var progress = Garden.recordSession(Progress(), 1, 10, 3, 2)
        val before = Garden.growth(progress)
        progress = Garden.recordSession(progress, 30, 10, 3, 1)
        assertEquals(2, progress.sessions.size)
        assertTrue(progress.gardenGrowth >= before)
    }

    @Test
    fun `garden reaches the next stage after three rounds`() {
        var progress = Progress()
        progress = Garden.recordSession(progress, 1, 10, 3, 2)
        progress = Garden.recordSession(progress, 2, 10, 3, 2)
        progress = Garden.recordSession(progress, 3, 10, 3, 2)
        assertEquals(2, Garden.stage(progress))
    }

    @Test
    fun `two sessions on the same day are merged`() {
        var progress = Garden.recordSession(Progress(), 1, 10, 3, 2)
        progress = Garden.recordSession(progress, 1, 5, 2, 1)
        assertEquals(1, progress.sessions.size)
        assertEquals(15, progress.sessions.first().minutes)
        assertEquals(5, progress.sessions.first().speakingTasks)
    }

    @Test
    fun `placement uses the test and the self rating`() {
        assertEquals(2, Placement.level(correct = 6, total = 6, selfRating = 2))
        assertEquals(1, Placement.level(correct = 6, total = 6, selfRating = 0))
        assertEquals(0, Placement.level(correct = 0, total = 6, selfRating = 0))
        assertEquals(1, Placement.level(correct = 3, total = 6, selfRating = 1))
    }

    @Test
    fun `placement is safe for empty tests`() {
        assertEquals(1, Placement.level(correct = 0, total = 0, selfRating = 1))
    }
}
