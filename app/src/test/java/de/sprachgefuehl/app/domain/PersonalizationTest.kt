package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.TestContent
import de.sprachgefuehl.app.data.Profile
import de.sprachgefuehl.app.data.Progress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalizationTest {

    private val content = TestContent.bundle

    @Test
    fun `reason and interests choose the preferred paths`() {
        assertTrue("unterwegs" in Personalization.preferredPaths(Profile(reasonId = "reisen")))
        assertTrue("einkaufen" in Personalization.preferredPaths(Profile(interests = setOf("kochen"))))
        assertTrue(Personalization.preferredPaths(Profile()).isEmpty())
    }

    @Test
    fun `the suggestion follows the reason and explains itself`() {
        val found = requireNotNull(Personalization.suggestion(content, Progress(profile = Profile(reasonId = "reisen"))))
        val situation = content.situations.first { it.id == found.lesson.situationId }
        assertEquals("unterwegs", situation.pathId)
        assertTrue("Begründung fehlt", found.why.contains("Reisen"))
        assertNotNull("Begegnung fehlt", found.encounter)
    }

    @Test
    fun `without a profile the normal path order wins`() {
        val found = requireNotNull(Personalization.suggestion(content, Progress()))
        val situation = content.situations.first { it.id == found.lesson.situationId }
        assertEquals("gesundheit", situation.pathId)
        assertEquals("", found.why)
    }

    @Test
    fun `greeting and goal line stay calm and honest`() {
        assertEquals("Guten Morgen, Anna", Personalization.greeting("Anna", 8))
        assertEquals("Guten Abend", Personalization.greeting("", 21))
        assertEquals("4 von 10 Minuten", Personalization.goalLine(4, 10))
        assertEquals("Tagesziel geschafft – für heute reicht es.", Personalization.goalLine(12, 10))
        assertEquals("10 Minuten warten auf dich.", Personalization.goalLine(0, 10))
    }
}
