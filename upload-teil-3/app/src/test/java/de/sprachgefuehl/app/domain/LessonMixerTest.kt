package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.TestContent
import de.sprachgefuehl.app.data.LearningRepository
import de.sprachgefuehl.app.data.Memory
import de.sprachgefuehl.app.data.Mistake
import de.sprachgefuehl.app.data.Profile
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.ProgressStore
import de.sprachgefuehl.app.data.SkillState
import de.sprachgefuehl.app.data.SkillProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class LessonMixerTest {

    private val content = TestContent.bundle
    private val lesson = content.lessons.first { it.chunkIds.size >= 2 }

    @Test
    fun `the pedagogical core survives every mix`() {
        val steps = LessonMixer.steps(lesson, content, Progress(seed = 42), day = 20000)
        val types = steps.map { it.type }.toSet()
        assertTrue(types.contains("DialogInput"))
        assertTrue(types.contains("ComprehensionDeep"))
        assertTrue(types.contains("Noticing"))
        assertTrue(types.contains("SpeakFree"))
        assertTrue(types.contains("GrammarSpot"))
        assertTrue(types.contains("SelfCheck"))
        assertTrue("Länge ${steps.size}", steps.size in 13..17)
    }

    @Test
    fun `the same lesson feels different on another day`() {
        val signatures = (0L..7L).map { day ->
            LessonMixer.steps(lesson, content, Progress(seed = 42), day).joinToString("|") {
                "${it.type}:${it.target}"
            }
        }.toSet()
        assertTrue("Der Pool rotiert nicht", signatures.size > 1)
    }

    @Test
    fun `the mix is stable within a day`() {
        val first = LessonMixer.steps(lesson, content, Progress(seed = 7), day = 20000).map { it.id }
        val second = LessonMixer.steps(lesson, content, Progress(seed = 7), day = 20000).map { it.id }
        assertEquals(first, second)
    }

    @Test
    fun `no step id appears twice`() {
        val ids = LessonMixer.steps(lesson, content, Progress(seed = 3), day = 20000).map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `onboarding focus moves the exercise pool`() {
        fun grammarCount(progress: Progress): Int = (0 until 25).sumOf { index ->
            val candidate = content.lessons[index % content.lessons.size]
            LessonMixer.steps(candidate, content, progress, day = 20000L + index)
                .count { it.type == "BuildSentence" || it.type == "GapFill" }
        }
        val head = Progress(seed = 42, selfRating = 1, profile = Profile(reasonIds = setOf("kopf")))
        val travel = Progress(seed = 42, selfRating = 1, profile = Profile(reasonIds = setOf("reisen")))
        val headGrammar = grammarCount(head)
        val travelGrammar = grammarCount(travel)
        assertTrue(
            "Kopf: $headGrammar, Reisen: $travelGrammar",
            headGrammar > travelGrammar * 1.3
        )
    }

    @Test
    fun `every onboarding answer trains something`() {
        (ProfileCatalog.reasons + ProfileCatalog.interests).forEach { choice ->
            assertTrue("${choice.id} hat keinen Schwerpunkt", choice.focusSkills.isNotEmpty())
        }
        assertEquals(
            setOf(Skill.GRAMMATIK, Skill.VERKNUEPFEN),
            ProfileCatalog.focusSkills(setOf("kopf"))
        )
        assertTrue(ProfileCatalog.focusLabels(setOf("reisen")).isNotEmpty())
    }

    @Test
    fun `a mistake from another lesson comes back with origin and what was heard`() {
        val foreign = foreignChunk()
        val origin = content.lessons.first { foreign.id.startsWith(it.id + "_") }
        val progress = Progress(
            seed = 11,
            selfRating = 1,
            mistakes = listOf(
                Mistake(
                    chunkId = foreign.id,
                    lessonId = origin.id,
                    lessonTitle = origin.title,
                    target = foreign.en,
                    heard = "I go yesterday",
                    day = 20000,
                    skill = "hoeren"
                )
            )
        )
        val steps = LessonMixer.steps(lesson, content, progress, day = 20000)
        val recall = steps.firstOrNull { it.title == "Wieder getroffen" }
        assertTrue("Keine Wiederholung eingestreut", recall != null)
        assertEquals(foreign.en, recall?.target)
        assertEquals(origin.title, recall?.originTitle)
        assertTrue(recall?.originNote.orEmpty().contains("I go yesterday"))
        assertTrue(recall?.why.orEmpty().isNotBlank())
    }

    @Test
    fun `a lesson never recycles its own chunks as mistakes`() {
        val own = lesson.chunkIds.first()
        val progress = Progress(
            seed = 11,
            selfRating = 1,
            mistakes = listOf(Mistake(chunkId = own, lessonId = lesson.id, skill = "hoeren", day = 20000))
        )
        val steps = LessonMixer.steps(lesson, content, progress, day = 20000)
        assertFalse(steps.any { it.title == "Wieder getroffen" })
    }

    @Test
    fun `a mistake is stored with what was heard and resolved by a right answer`() {
        val file = Files.createTempFile("sprachgefuehl-mistake", ".json").toFile()
        try {
            val repo = LearningRepository(content, ProgressStore(file), clock = { 20000L * 86_400_000L })
            val foreign = foreignChunk()
            repo.recordMistake(foreign.id, "lesson-a", "Beim Bäcker", foreign.en, "I go yesterday", "hoeren", "")
            assertEquals(1, repo.progress.value.mistakes.size)
            assertEquals("I go yesterday", repo.progress.value.mistakes.single().heard)
            repo.recordMistake(foreign.id, "lesson-b", "Am Markt", foreign.en, "I went yesterday", "hoeren", "")
            assertEquals(1, repo.progress.value.mistakes.size)
            assertEquals("I went yesterday", repo.progress.value.mistakes.single().heard)
            repo.resolveMistake(foreign.id)
            assertTrue(repo.progress.value.mistakes.isEmpty())
        } finally {
            file.delete()
        }
    }

    @Test
    fun `the pool reaches all nine exercise types`() {
        val learner = Progress(seed = 42, selfRating = 2)
        // Achtzig Runden statt dreißig: Der Zufall zieht aus einem größeren Inhalt, einzelne
        // Formen (Diktat) können dadurch in wenigen Runden ausbleiben, ohne zu fehlen.
        val types = (0 until 80).flatMap { index ->
            val candidate = content.lessons[index % content.lessons.size]
            LessonMixer.steps(candidate, content, learner, day = 21000L + index).map { it.type }
        }.toSet()
        val expected = setOf(
            "SpeakRepeat", "TranslateToEnglish", "BuildSentence", "MeaningChoice",
            "ListenMeaning", "GapFill", "Dictation", "Recall", "ChooseConnector"
        )
        assertTrue("Gefunden: $types", types.containsAll(expected))
    }

    @Test
    fun `dictation hides the model and gap fill has exactly one answer`() {
        val learner = Progress(seed = 42, selfRating = 2)
        val all = (0 until 80).flatMap { index ->
            val candidate = content.lessons[index % content.lessons.size]
            LessonMixer.steps(candidate, content, learner, day = 21000L + index)
        }
        val dictation = all.firstOrNull { it.type == "Dictation" }
        assertTrue("Kein Diktat erzeugt", dictation != null)
        assertTrue(dictation?.modelHidden == true)
        assertTrue(dictation?.model.orEmpty().isBlank())

        val gap = all.firstOrNull { it.type == "GapFill" }
        assertTrue("Keine Lücke erzeugt", gap != null)
        assertEquals(1, gap?.options?.count { it.correct })
        assertEquals(3, gap?.options?.size)
        assertTrue(gap?.promptEn.orEmpty().contains("_____"))

        val listen = all.firstOrNull { it.type == "ListenMeaning" }
        assertTrue("Kein Hörverstehen erzeugt", listen != null)
        assertEquals(1, listen?.options?.count { it.correct })
    }

    @Test
    fun `a shaky chunk gets support instead of recall`() {
        val memory = lesson.chunkIds.associateWith { Memory(lastGrade = 2, lapses = 1) }
        val progress = Progress(seed = 3, selfRating = 2, chunkMemory = memory)
        val steps = LessonMixer.steps(lesson, content, progress, day = 20000)
        assertFalse(steps.any { it.type in setOf("Dictation", "Recall", "GapFill") })
    }

    @Test
    fun `a secure chunk gets retrieval`() {
        val memory = lesson.chunkIds.associateWith { Memory(lastGrade = 3, lapses = 0) }
        val progress = Progress(seed = 3, selfRating = 2, chunkMemory = memory)
        val types = (0 until 20).flatMap { day ->
            LessonMixer.steps(lesson, content, progress, day = 20000L + day).map { it.type }
        }.toSet()
        assertTrue("Kein Abruf bei sicheren Bausteinen: $types", types.any {
            it in setOf("Dictation", "Recall", "GapFill")
        })
    }

    @Test
    fun `the weakest skill gets its own task and says why`() {
        val skills = SkillProfile(
            hoeren = SkillState(rating = 30.0, observations = 4),
            grammatik = SkillState(rating = 70.0, observations = 4)
        )
        val progress = Progress(seed = 5, selfRating = 1, skills = skills)
        val steps = LessonMixer.steps(lesson, content, progress, day = 20000)
        val extra = steps.firstOrNull { it.id.endsWith("_mix_weak") }
        assertTrue("Keine Schwächenaufgabe", extra != null)
        assertTrue(extra?.why.orEmpty().contains("schwächster"))
    }

    @Test
    fun `only one long sentence gets the understanding gate`() {
        val gated = content.lessons.mapNotNull { candidate ->
            LessonMixer.steps(candidate, content, Progress(seed = 5), day = 20000)
                .filter { it.understandingOptions.isNotEmpty() }
                .takeIf { it.isNotEmpty() }
        }.firstOrNull()
        assertTrue("Kein Verständnis-Gate erzeugt", gated != null)
        assertEquals(1, gated?.size)
        val step = gated!!.first()
        assertEquals(3, step.understandingOptions.size)
        assertTrue(step.understandingAnswer in 0..2)
        assertEquals(step.understandingDe, step.understandingOptions[step.understandingAnswer])
        assertTrue(step.target.trim().split(" ").filter { it.isNotBlank() }.size >= 6)
    }

    @Test
    fun `a fresh chunk is not treated as an error`() {
        val foreign = foreignChunk()
        val progress = Progress(seed = 11, chunkMemory = mapOf(foreign.id to Memory(due = 1)))
        val steps = LessonMixer.steps(lesson, content, progress, day = 20000)
        assertFalse(steps.any { it.id.endsWith("_mix_recall") })
    }

    private fun foreignChunk() = content.chunks.first { chunk ->
        lesson.chunkIds.none { it == chunk.id } &&
            content.lessons.any { chunk.id.startsWith(it.id + "_") }
    }
}
