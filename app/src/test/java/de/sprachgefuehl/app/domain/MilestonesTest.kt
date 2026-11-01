package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.SessionLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MilestonesTest {

    @Test
    fun `the first round unlocks a milestone once`() {
        val progress = Progress(sessions = listOf(SessionLog(day = 1, minutes = 10, speakingTasks = 5, newChunks = 2)))
        assertEquals(listOf("first_round"), Milestones.newOnes(progress).map { it.id })

        val celebrated = progress.copy(milestones = setOf("first_round"))
        assertTrue(Milestones.newOnes(celebrated).isEmpty())
        assertEquals(listOf("first_round"), Milestones.earned(celebrated).map { it.id })
    }

    @Test
    fun `milestones never disappear`() {
        val progress = Progress(
            sessions = List(3) { SessionLog(day = it.toLong(), minutes = 10, speakingTasks = 5, newChunks = 1) },
            completedLessons = (1..10).map { "lesson_$it" }.toSet(),
            milestones = setOf("first_round", "three_rounds")
        )
        val earned = Milestones.earned(progress).map { it.id }.toSet()
        assertTrue("earned enthält nicht mehr alle gespeicherten", earned.containsAll(setOf("first_round", "three_rounds")))
        assertTrue("ten_lessons ist erreicht und wird beim nächsten Feiern vergeben", "ten_lessons" in Milestones.unlocked(progress))
        assertTrue("ein noch nicht gefeierter Meisterstein gilt nicht als vergeben", "ten_lessons" !in earned)
    }

    @Test
    fun `five active days are counted, not five sessions`() {
        val repeated = Progress(sessions = List(5) { SessionLog(day = 1, minutes = 10, speakingTasks = 1, newChunks = 0) })
        assertTrue("fünf Sitzungen an einem Tag sind kein Fünf-Tage-Meilenstein", "five_days" !in Milestones.unlocked(repeated))
        val spread = Progress(sessions = (1L..5L).map { SessionLog(day = it, minutes = 10, speakingTasks = 1, newChunks = 0) })
        assertTrue("five_days" in Milestones.unlocked(spread))
    }
}
