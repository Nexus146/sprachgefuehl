package de.sprachgefuehl.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ErrorDiagnosisTest {

    @Test
    fun `since with a duration points to the for since card`() {
        assertEquals("err_since_for", ErrorDiagnosis.diagnose("I have lived here for three years.", "I live here since three years"))
    }

    @Test
    fun `missing do in a question is diagnosed`() {
        assertEquals("err_do_support", ErrorDiagnosis.diagnose("Do you have a fever?", "you have a fever"))
    }

    @Test
    fun `missing article is diagnosed`() {
        assertEquals("err_article", ErrorDiagnosis.diagnose("I have a headache.", "I have headache"))
    }

    @Test
    fun `third person s is diagnosed`() {
        assertEquals("err_third_person", ErrorDiagnosis.diagnose("She works in a hospital.", "she work in a hospital"))
    }

    @Test
    fun `direct request points to register`() {
        assertEquals("err_register", ErrorDiagnosis.diagnose("Could I have a coffee, please?", "Give me a coffee"))
    }

    @Test
    fun `german word order after because is diagnosed`() {
        assertEquals("err_because_order", ErrorDiagnosis.diagnose("I stayed inside because it was cold.", "I stayed inside because was it cold"))
    }

    @Test
    fun `plural child is diagnosed`() {
        assertEquals("err_plural_child", ErrorDiagnosis.diagnose("I have two grandchildren.", "I have two childs"))
    }

    @Test
    fun `unmatched answers fall back to the generic card`() {
        val id = ErrorDiagnosis.diagnose("Take one tablet twice daily.", "tablet")
        assertEquals("err_missing_word", id)
    }

    @Test
    fun `empty input produces no diagnosis`() {
        assertEquals("", ErrorDiagnosis.diagnose("I have a headache.", ""))
    }
}
