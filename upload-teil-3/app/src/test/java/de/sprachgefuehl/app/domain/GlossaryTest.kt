package de.sprachgefuehl.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GlossaryTest {

    @Test
    fun `punctuation and case do not matter`() {
        assertEquals("Kaffee", Glossary.of("Coffee,"))
        assertEquals("Rechnung", Glossary.of("BILL?"))
        assertEquals("Termin", Glossary.of("appointment"))
    }

    @Test
    fun `unknown words stay unknown instead of guessing`() {
        assertNull(Glossary.of("serendipity"))
        assertNull(Glossary.of(""))
    }

    @Test
    fun `the dictionary is big enough for the content`() {
        assertTrue("Zu wenige Einträge: ${Glossary.size}", Glossary.size >= 180)
        assertEquals("Kopfschmerzen", Glossary.of("headache"))
        assertEquals("Krankenhaus", Glossary.of("hospital"))
        assertEquals("nicht", Glossary.of("don't"))
    }
}
