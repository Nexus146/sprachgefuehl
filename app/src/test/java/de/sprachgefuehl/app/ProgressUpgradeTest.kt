package de.sprachgefuehl.app

import de.sprachgefuehl.app.data.CheckupState
import de.sprachgefuehl.app.data.LearningRepository
import de.sprachgefuehl.app.data.LessonSession
import de.sprachgefuehl.app.data.Memory
import de.sprachgefuehl.app.data.Mistake
import de.sprachgefuehl.app.data.Profile
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.ProgressStore
import de.sprachgefuehl.app.data.RecordingRef
import de.sprachgefuehl.app.data.SessionLog
import de.sprachgefuehl.app.data.Settings
import de.sprachgefuehl.app.data.SkillProfile
import de.sprachgefuehl.app.data.SkillSnapshot
import de.sprachgefuehl.app.data.SkillState
import de.sprachgefuehl.app.data.TaskStat
import de.sprachgefuehl.app.domain.DailyPlan
import de.sprachgefuehl.app.domain.Garden
import de.sprachgefuehl.app.domain.LessonMixer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.time.ZoneId

/**
 * Die Regel für jedes Update: ein neuer Speicherstand darf einen alten nie unlesbar machen, und
 * ein alter Stand muss nach dem Update weiter mit den adaptiven Teilen zusammenarbeiten.
 *
 * Der erste Test ist der Wächter: Wer ein Feld ohne Default in eine gespeicherte Klasse schreibt,
 * bricht diesen Test – und damit den Build – bevor eine alte Datei in der Werkstatt liegt.
 */
class ProgressUpgradeTest {

    private val content = TestContent.bundle
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /** Felder, die schon vor dieser Regel ohne Default auskamen. Sie bleiben, damit alte Dateien laden. */
    private val legacyRequired = mapOf(
        "SessionLog" to setOf("day", "minutes", "speakingTasks", "newChunks"),
        "RecordingRef" to setOf("id", "label", "path", "day")
    )

    private val savedClasses: List<SerialDescriptor> = listOf(
        Progress.serializer().descriptor,
        Profile.serializer().descriptor,
        Settings.serializer().descriptor,
        SkillProfile.serializer().descriptor,
        SkillState.serializer().descriptor,
        CheckupState.serializer().descriptor,
        LessonSession.serializer().descriptor,
        Mistake.serializer().descriptor,
        Memory.serializer().descriptor,
        TaskStat.serializer().descriptor,
        SkillSnapshot.serializer().descriptor,
        SessionLog.serializer().descriptor,
        RecordingRef.serializer().descriptor
    )

    private fun tempFile(): File =
        Files.createTempFile("sg-upgrade", ".json").toFile().also { it.delete() }

    private fun repoFor(file: File) =
        LearningRepository(content, ProgressStore(file), { 0L }, ZoneId.of("UTC"))

    // ---- Der Wächter ------------------------------------------------------------------------

    @Test
    fun `every saved field has a default so a future version can read old files`() {
        val problems = mutableListOf<String>()
        savedClasses.forEach { descriptor ->
            val className = descriptor.serialName.substringAfterLast('.')
            val allowed = legacyRequired[className].orEmpty()
            descriptor.elementNames.forEachIndexed { index, name ->
                if (!descriptor.isElementOptional(index) && name !in allowed) {
                    problems += "$className.$name"
                }
            }
        }
        assertTrue(
            "Diese Felder im Speicherstand haben keinen Default. Eine Datei aus einer älteren " +
                "Fassung könnte dann nicht mehr gelesen werden – bitte `= <Wert>` ergänzen:\n" +
                problems.joinToString("\n"),
            problems.isEmpty()
        )
    }

    /** Ohne diesen Test könnte der Wächter oben auch einfach alles durchwinken. */
    @Test
    fun `the guard notices a field without a default`() {
        val descriptor = SessionLog.serializer().descriptor
        val required = descriptor.elementNames.filterIndexed { index, _ -> !descriptor.isElementOptional(index) }
        assertTrue("Der Wächter muss Pflichtfelder erkennen: $required", "day" in required)
    }

    @Test
    fun `the oldest possible document still opens`() {
        val decoded = json.decodeFromString(Progress.serializer(), """{"onboarded":true}""")
        assertEquals(Progress(onboarded = true), decoded)
    }

    @Test
    fun `a save from before a new setting opens with the new default`() {
        val decoded = json.decodeFromString(
            Progress.serializer(),
            """{"onboarded":true,"settings":{"themeMode":"dark","fontScale":1.3}}"""
        )
        assertEquals("dark", decoded.settings.themeMode)
        assertEquals(1.3f, decoded.settings.fontScale, 0.001f)
        // Alles, was erst später dazukam, steht auf seinem Default.
        assertTrue(decoded.settings.soundEnabled)
        assertTrue(decoded.settings.germanSupport)
        assertEquals(10, decoded.profile.dailyMinutes)
    }

    @Test
    fun `unknown fields from a newer version are ignored`() {
        val decoded = json.decodeFromString(
            Progress.serializer(),
            """{"onboarded":true,"gardenMood":"sonnig","settings":{"stimme":"neu"}}"""
        )
        assertTrue(decoded.onboarded)
    }

    @Test
    fun `a full save survives a round trip`() {
        val full = Progress(
            onboarded = true,
            profile = Profile(name = "Tobi", interests = setOf("kochen"), dailyMinutes = 15),
            settings = Settings(fontScale = 1.3f, themeMode = "dark"),
            skills = SkillProfile(hoeren = SkillState(70.0, 5), measuredAt = 42L),
            sessions = listOf(SessionLog(day = 20_000, minutes = 10, speakingTasks = 3, newChunks = 2)),
            mistakes = listOf(Mistake(chunkId = "c1", lessonId = "l1", day = 20_000)),
            taskStats = mapOf("SpeakRepeat" to TaskStat(attempts = 4, correct = 3)),
            lessonSession = LessonSession(lessonId = "l1", steps = emptyList(), index = 0),
            timeByDay = mapOf(20_000L to 600)
        )
        val text = json.encodeToString(Progress.serializer(), full)
        assertEquals(full, json.decodeFromString(Progress.serializer(), text))
    }

    // ---- Ein Update mit neuem Inhalt --------------------------------------------------------

    @Test
    fun `an update keeps a running lesson as long as its sentences exist`() {
        val file = tempFile()
        try {
            val lesson = content.lessons.first { it.chunkIds.size >= 2 }
            val old = Progress(
                onboarded = true,
                contentVersion = (content.version - 1).coerceAtLeast(0),
                lessonSession = LessonSession(lessonId = lesson.id, steps = lesson.steps, index = 1),
                lessonProgress = mapOf(lesson.id to 1),
                mistakes = listOf(
                    Mistake(chunkId = "", lessonId = lesson.id, lessonTitle = "Grammatik"),
                    Mistake(chunkId = "removed_chunk", lessonId = lesson.id)
                ),
                lastPracticeChunkIds = listOf(content.chunks.first().id, "removed_chunk")
            )
            ProgressStore(file).save(old)

            val p = repoFor(file).progress.value

            assertEquals("Die angefangene Lektion darf ein Update nicht verlieren", lesson.id, p.lessonSession?.lessonId)
            assertEquals(1, p.lessonSession?.index)
            assertEquals(content.version, p.contentVersion)
            assertEquals("Fehler mit Satzbezug auf gelöschten Inhalt gehen", 1, p.mistakes.size)
            assertEquals("", p.mistakes.first().chunkId)
            assertEquals(listOf(content.chunks.first().id), p.lastPracticeChunkIds)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `adaptive parts keep working on a migrated old save`() {
        val file = tempFile()
        try {
            val lesson = content.lessons.first { it.chunkIds.size >= 2 }
            ProgressStore(file).save(
                Progress(
                    onboarded = true,
                    contentVersion = 0,
                    lessonSession = LessonSession(lessonId = lesson.id, steps = lesson.steps, index = 0),
                    // Ein Stand von vor dem Sprachprofil: keine Beobachtungen, keine Aufgabenzahlen.
                    skills = SkillProfile(),
                    taskStats = emptyMap()
                )
            )
            val p = repoFor(file).progress.value

            val steps = LessonMixer.steps(lesson, content, p, 20_000)
            assertTrue("Eine Runde muss auch ohne Vorgeschichte entstehen", steps.size >= 6)
            assertTrue(steps.map { it.type }.toSet().size >= 4)
            assertTrue(DailyPlan.summary(p, 20_000).goal > 0)
            assertEquals(5, Garden.beds(p).size)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `a running lesson that vanished is dropped and the rest survives`() {
        val file = tempFile()
        try {
            val lesson = content.lessons.first()
            val knownChunk = content.chunks.first().id
            ProgressStore(file).save(
                Progress(
                    onboarded = true,
                    contentVersion = 0,
                    lessonSession = LessonSession(lessonId = "removed_lesson", steps = lesson.steps),
                    lessonProgress = mapOf("removed_lesson" to 3, lesson.id to 1),
                    chunkMemory = mapOf("removed_chunk" to Memory(), knownChunk to Memory(stability = 3.0)),
                    completedLessons = setOf("removed_lesson")
                )
            )
            val p = repoFor(file).progress.value

            assertNull(p.lessonSession)
            assertEquals(mapOf(lesson.id to 1), p.lessonProgress)
            assertEquals(setOf(knownChunk), p.chunkMemory.keys)
            assertTrue(p.completedLessons.isEmpty())
        } finally {
            file.delete()
        }
    }

    @Test
    fun `new content does not look already learned`() {
        val file = tempFile()
        try {
            // Der Stand kennt nur die ersten drei Lektionen – der Rest ist für ihn neu.
            val known = content.lessons.take(3).map { it.id }.toSet()
            ProgressStore(file).save(
                Progress(
                    onboarded = true,
                    contentVersion = content.version,
                    completedLessons = known,
                    lessonProgress = known.associateWith { 0 }
                )
            )
            val p = repoFor(file).progress.value

            assertEquals(known, p.completedLessons)
            val newLessons = content.lessons.filter { it.id !in known }
            assertTrue(newLessons.isNotEmpty())
            assertTrue(newLessons.none { it.id in p.lessonProgress })
            assertNotNull(content.lessons.firstOrNull { it.id !in p.completedLessons })
        } finally {
            file.delete()
        }
    }
}
