package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Memory
import de.sprachgefuehl.app.data.Progress
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * FSRS-inspired scheduler. Stability is measured in days, difficulty on a 1..10 scale.
 * The implementation is deliberately small and deterministic so it can be unit tested.
 */
object Srs {
    const val DAY_MS = 86_400_000L
    const val MAX_INTERVAL_DAYS = 180.0

    /** grade: 0 = nichts gehört, 1 = nicht verstanden, 2 = fast, 3 = klar. */
    fun review(memory: Memory, grade: Int, now: Long, usedHint: Boolean = false): Memory {
        val elapsedDays = if (memory.reps == 0) 0.0 else (now - memory.due).coerceAtLeast(0L) / DAY_MS.toDouble()
        val retrievability = retrievability(memory, now)
        val g = grade.coerceIn(0, 3)

        if (g <= 1) {
            val stability = max(0.25, memory.stability * 0.35)
            return memory.copy(
                stability = stability,
                difficulty = (memory.difficulty + 0.8).coerceIn(1.0, 10.0),
                due = now + (stability * DAY_MS).toLong(),
                reps = memory.reps + 1,
                lapses = memory.lapses + 1,
                stage = max(1, memory.stage - 1),
                lastGrade = g,
                contextCount = 0
            )
        }

        val base = if (memory.reps == 0) {
            if (g == 3) 2.5 else 1.0
        } else {
            memory.stability
        }
        val growth = 1.0 + 1.1 * (1.0 - retrievability) + 0.35 * (g - 2)
        var stability = (base * growth).coerceIn(0.5, MAX_INTERVAL_DAYS)
        if (usedHint) stability *= 0.7
        if (memory.reps > 0 && elapsedDays > stability * 2) stability *= 1.1

        val difficulty = (memory.difficulty - 0.25 * (g - 2) + if (usedHint) 0.2 else 0.0)
            .coerceIn(1.0, 10.0)

        val stageGain = if (usedHint) 0 else 1
        val newStage = (memory.stage + stageGain).coerceIn(1, 5)
        val contexts = if (usedHint) memory.contextCount else memory.contextCount + 1
        val finalStage = if (newStage >= 4 && contexts >= 3) 5 else newStage.coerceAtMost(4)

        return memory.copy(
            stability = stability,
            difficulty = difficulty,
            due = now + (stability * DAY_MS).toLong(),
            reps = memory.reps + 1,
            stage = finalStage,
            lastGrade = g,
            contextCount = contexts
        )
    }

    fun retrievability(memory: Memory, now: Long): Double {
        if (memory.reps == 0 || memory.stability <= 0.0) return 0.0
        val elapsed = max(0.0, (now - (memory.due - (memory.stability * DAY_MS).toLong())) / DAY_MS.toDouble())
        return (1.0 + elapsed / (9.0 * memory.stability)).pow(-1.0)
    }

    fun intervalDays(memory: Memory): Double = max(0.25, memory.stability)

    fun isDue(memory: Memory, now: Long): Boolean = memory.reps == 0 || memory.due <= now

    fun dueChunks(progress: Progress, now: Long, limit: Int = 8): List<String> =
        progress.chunkMemory.entries
            .filter { isDue(it.value, now) }
            .sortedBy { it.value.due }
            .take(limit)
            .map { it.key }

    /** Stability grows sublinearly, so long-term retention stays realistic. */
    fun projectInterval(days: Double): Double = min(MAX_INTERVAL_DAYS, max(0.25, days))

    /** Simple forgetting curve used by the UI to explain why review matters. */
    fun retentionAfter(memory: Memory, days: Double): Double =
        exp(-days / (9.0 * max(0.25, memory.stability)))
}
