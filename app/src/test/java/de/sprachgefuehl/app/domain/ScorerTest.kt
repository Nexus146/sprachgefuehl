package de.sprachgefuehl.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScorerTest {

    @Test
    fun `exact sentence is clear`() {
        val result = Scorer.score("I have a bad headache.", "I have a bad headache")
        assertEquals(3, result.grade)
    }

    @Test
    fun `missing article still counts when the meaning is intact`() {
        val result = Scorer.score("Could I have a coffee, please?", "could i have coffee please")
        assertEquals(3, result.grade)
    }

    @Test
    fun `a fragment of a short formula is not perfect`() {
        // „How are you?“ besteht fast nur aus Funktionswörtern. Ein einzelnes „how“ ist der
        // Anfang, nicht der Satz – also höchstens „fast“.
        val result = Scorer.score("How are you?", "how")
        assertEquals(2, result.grade)
        assertTrue(result.missing.containsAll(listOf("are", "you")))
    }

    @Test
    fun `short formula without one small word stays perfect`() {
        val result = Scorer.score("How are you?", "how are you doing")
        assertEquals(3, result.grade)
    }

    @Test
    fun `wrong content word is only almost`() {
        val result = Scorer.score("I have a bad headache.", "I have a bad stomach")
        assertTrue(result.grade <= 2)
        assertTrue(result.missing.isNotEmpty())
    }

    @Test
    fun `nothing heard is grade zero`() {
        val result = Scorer.score("I have a headache", "   ")
        assertEquals(0, result.grade)
    }

    @Test
    fun `required form caps the grade`() {
        val result = Scorer.score(
            target = "Could I have a coffee, please?",
            heard = "can i have a coffee please",
            requiredForm = listOf("could")
        )
        assertEquals(2, result.grade)
    }

    @Test
    fun `contractions and numbers normalise`() {
        assertEquals(listOf("i", "do", "not", "understand"), Scorer.normalize("I don’t understand!"))
        assertEquals(listOf("2", "days"), Scorer.normalize("two days"))
    }

    @Test
    fun `alternative phrasing is accepted`() {
        val result = Scorer.score(
            target = "Yes, see you at six!",
            heard = "yes see you at 6",
            accept = listOf("Yes, see you at six!")
        )
        assertEquals(3, result.grade)
    }

    @Test
    fun `strict dictation rejects half a sentence`() {
        val result = Scorer.score(
            target = "I have a bad headache tomorrow morning",
            heard = "I have a bad headache",
            strict = true
        )
        assertTrue("Halber Satz galt als bestanden: ${result.grade}", result.grade < 2)
    }

    @Test
    fun `strict dictation rewards the full sentence`() {
        val result = Scorer.score(
            target = "I have a bad headache tomorrow morning",
            heard = "I have a bad headache tomorrow morning",
            strict = true
        )
        assertEquals(3, result.grade)
    }

    @Test
    fun `strict dictation still allows one small slip`() {
        val result = Scorer.score(
            target = "I have a bad headache tomorrow morning",
            heard = "I have bad headache tomorrow morning",
            strict = true
        )
        assertEquals(2, result.grade)
    }
}
