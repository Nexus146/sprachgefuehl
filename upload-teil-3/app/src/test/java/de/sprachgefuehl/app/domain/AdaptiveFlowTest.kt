package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.TestContent
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.SkillProfile
import de.sprachgefuehl.app.data.SkillState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Die Kette, die die Lernende wirklich erlebt: Standort-Check → Einstiegslektion → erste Aufgabe.
 * Einzeltests können alle grün sein, während die Teile nicht zueinander passen; dieser Test prüft
 * den Übergang.
 */
class AdaptiveFlowTest {

    private val content = TestContent.bundle
    private val day = 22_000L

    @Test
    fun `the placement decides the entry and the first difficulty together`() {
        val low = Progress(onboarded = true, placementLevel = 0, selfRating = 0)
        val high = Progress(onboarded = true, placementLevel = 2, selfRating = 2)

        val lowLesson = EntryPoint.firstLesson(content, low)!!
        val highLesson = EntryPoint.firstLesson(content, high)!!
        assertTrue(
            "Einstieg: ${lowLesson.level} → ${highLesson.level}",
            EntryPoint.rankOf(highLesson) > EntryPoint.rankOf(lowLesson)
        )
        assertTrue(
            "Ziel: ${AdaptivePlan.forProgress(low, day, lowLesson).overall} → " +
                "${AdaptivePlan.forProgress(high, day, highLesson).overall}",
            AdaptivePlan.forProgress(high, day, highLesson).overall >
                AdaptivePlan.forProgress(low, day, lowLesson).overall
        )
    }

    @Test
    fun `a weak learner gets support where the strong one gets free recall`() {
        fun profile(rating: Double) = SkillProfile(
            hoeren = SkillState(rating, 4),
            grammatik = SkillState(rating, 4),
            bausteine = SkillState(rating, 4),
            sprechen = SkillState(rating, 4),
            verknuepfen = SkillState(rating, 4)
        )

        fun types(rating: Double): Set<String> {
            val progress = Progress(
                seed = 42,
                selfRating = if (rating > 50) 2 else 0,
                skills = profile(rating)
            )
            return content.lessons.take(12).flatMap { lesson ->
                LessonMixer.steps(lesson, content, progress, day).map { it.type }
            }.toSet()
        }

        val weak = types(20.0)
        val strong = types(90.0)
        val supportive = setOf("SpeakRepeat", "MeaningChoice", "ListenMeaning", "CorrectTheSentence")
        val free = setOf("Dictation", "Recall", "Paraphrase", "AnswerQuestion")

        assertTrue("Schwach ohne Stütze: $weak", weak.any { it in supportive })
        assertTrue("Stark ohne freien Abruf: $strong", strong.any { it in free })
        assertFalse("Schwach mit freiem Abruf: $weak", weak.any { it in free })
    }
}
