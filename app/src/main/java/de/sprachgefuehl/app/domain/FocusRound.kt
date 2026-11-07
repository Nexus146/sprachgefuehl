package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Chunk
import de.sprachgefuehl.app.data.ContentBundle
import de.sprachgefuehl.app.data.ErrorCard
import de.sprachgefuehl.app.data.Lesson
import de.sprachgefuehl.app.data.Option
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.Step
import kotlin.random.Random

/**
 * Eine kurze Runde zu genau einem Fehler. Die Fehlerkarte ist der Anker: erst die richtige
 * Fassung hören, dann die betroffenen Sätze selbst sprechen und bauen. Die Runde ist bewusst
 * kürzer als eine Lektion – sie soll den Fehler schließen, nicht ein Thema neu aufrollen.
 */
object FocusRound {

    const val MIN_TASKS = 7

    fun lessonId(cardId: String): String = "focus_$cardId"

    fun lesson(card: ErrorCard, content: ContentBundle, progress: Progress, day: Long): Lesson {
        val random = Random(progress.seed xor card.id.hashCode().toLong() xor (day * 7_919L))
        val anchors = anchors(card, content, random)
        val steps = mutableListOf<Step>()
        var number = 1
        fun nextId(): String = "${lessonId(card.id)}_s${number++}"

        val model = card.correctEn.ifBlank { card.title }
        steps += Step(
            id = nextId(),
            type = "Noticing",
            title = "Dein Fehler",
            promptEn = model,
            promptDe = card.anchor,
            explanationDe = card.whyDe,
            note = card.checklist.joinToString(" · "),
            target = model,
            model = model,
            pillarId = card.pillarId
        )
        anchors.take(3).forEach { chunk ->
            steps += speak(chunk, card, nextId())
            steps += build(chunk, card, nextId())
        }
        minimalPair(card, content, random)?.let { pair -> steps += pair.copy(id = nextId()) }
        // Jede Runde hat eine feste Mindestlänge, auch wenn nur ein Satz zur Karte passt.
        while (steps.size < MIN_TASKS && anchors.isNotEmpty()) {
            val chunk = anchors[steps.size % anchors.size]
            steps += speak(chunk, card, nextId())
        }
        steps += Step(
            id = nextId(),
            type = "SelfCheck",
            title = "Geschafft",
            promptDe = "Hat die Runde geholfen? Sei ehrlich – das steuert die Wiederholung.",
            model = model
        )
        return Lesson(
            id = lessonId(card.id),
            situationId = "",
            title = "Fokus: ${card.title}",
            goal = card.anchor,
            level = "A2",
            order = 0,
            pillarId = card.pillarId,
            steps = steps,
            chunkIds = anchors.take(3).map { it.id },
            xpMinutes = 5
        )
    }

    private fun speak(chunk: Chunk, card: ErrorCard, id: String): Step = Step(
        id = id,
        type = "SpeakRepeat",
        title = "Richtig sagen",
        promptDe = "Sprich die richtige Fassung nach.",
        target = chunk.en,
        accept = listOf(chunk.en),
        model = chunk.en,
        chunkId = chunk.id,
        tip = chunk.tip,
        why = "Diese Wendung gehört zu „${card.title}“."
    )

    private fun build(chunk: Chunk, card: ErrorCard, id: String): Step = Step(
        id = id,
        type = "BuildSentence",
        title = "Selbst bauen",
        promptDe = "Baue den Satz aus den Wörtern – ohne Vorlage.",
        target = chunk.en,
        words = chunk.en.trim().split(Regex(" +")).filter { it.isNotBlank() },
        model = chunk.en,
        chunkId = chunk.id,
        why = "Derselbe Fehler, diesmal selbst gebaut: ${card.anchor}."
    )

    /** Nur ein Lautpaar, dessen Wort wirklich in der Karte vorkommt – sonst keins. */
    private fun minimalPair(card: ErrorCard, content: ContentBundle, random: Random): Step? {
        val words = Scorer.normalize("${card.correctEn} ${card.examples.joinToString(" ")}").toSet()
        val pair = content.minimalPairs
            .filter { it.a.lowercase() in words || it.b.lowercase() in words }
            .randomOrNull(random) ?: return null
        return Step(
            id = "focus_${card.id}_mp",
            type = "MinimalPair",
            title = "Welches Wort hörst du?",
            promptDe = "Zwei Wörter klingen ähnlich. Hör genau hin.",
            target = pair.a,
            options = listOf(Option(text = pair.a, correct = true), Option(text = pair.b, correct = false)),
            model = pair.a,
            tip = pair.tipDe,
            why = "Der Lautunterschied steckt in dieser Karte."
        )
    }

    /**
     * Sätze zur Karte: zuerst die Beispiele der Karte selbst, dann Sätze aus derselben Säule.
     * So bleibt die Runde beim Fehler, ohne einen Satz zu erfinden.
     */
    private fun anchors(card: ErrorCard, content: ContentBundle, random: Random): List<Chunk> {
        val examples = card.examples.map { Scorer.normalize(it).joinToString(" ") }
        val byExample = content.chunks.filter { chunk ->
            val normalized = Scorer.normalize(chunk.en).joinToString(" ")
            normalized.isNotBlank() && examples.any { it.contains(normalized) }
        }
        if (byExample.isNotEmpty()) return byExample.distinctBy { it.id }
        val pillarChunks = content.chunks.filter { chunk ->
            content.patterns.firstOrNull { it.id == chunk.patternId }?.pillarId == card.pillarId
        }
        val pool = if (pillarChunks.isNotEmpty()) pillarChunks else content.chunks
        return pool.shuffled(random).distinctBy { it.id }.take(3)
    }
}
