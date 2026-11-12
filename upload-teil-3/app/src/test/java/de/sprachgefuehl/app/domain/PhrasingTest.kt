package de.sprachgefuehl.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PhrasingTest {

    @Test
    fun minutesStaySingularForOne() {
        assertEquals("0 Minuten", Phrasing.minutes(0))
        assertEquals("1 Minute", Phrasing.minutes(1))
        assertEquals("5 Minuten", Phrasing.minutes(5))
    }

    @Test
    fun sentencesStaySingularForOne() {
        assertEquals("1 Satz", Phrasing.sentences(1))
        assertEquals("3 Sätze", Phrasing.sentences(3))
    }

    @Test
    fun activeDaysAgreeWithTheNumber() {
        assertEquals("noch kein Tag", Phrasing.activeDays(0))
        assertEquals("1 aktiver Tag", Phrasing.activeDays(1))
        assertEquals("4 aktive Tage", Phrasing.activeDays(4))
    }

    @Test
    fun spokenTasksNameTheQuietCase() {
        assertEquals("ohne Sprechen", Phrasing.spokenTasks(0))
        assertEquals("1 Satz gesprochen", Phrasing.spokenTasks(1))
        assertEquals("3 Sätze gesprochen", Phrasing.spokenTasks(3))
    }
}
