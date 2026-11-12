package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.ContentBundle
import de.sprachgefuehl.app.data.Progress

/**
 * The IDs that still exist in the shipped content. Progress from an older content version can
 * contain removed chunks, cards or lessons; every count and queue must ignore those IDs.
 */
data class ContentIndex(
    val lessonIds: Set<String>,
    val chunkIds: Set<String>,
    val patternIds: Set<String>,
    val situationIds: Set<String>,
    val errorCardIds: Set<String>,
    val stepIds: Set<String>,
    val canDoIds: Set<String>
) {
    fun knowsChunk(id: String): Boolean = id in chunkIds

    /**
     * Removes entries that no longer have a counterpart in the content. This happens on load
     * and on import, so an old backup cannot make “collected 40” claim 40 known sentences.
     */
    fun migrate(progress: Progress, version: Int): Progress {
        // Eine angefangene Runde bleibt erhalten, solange ihre Lektion und die Sätze darin noch
        // existieren. Früher fiel sie bei jeder Inhaltsänderung weg – wer mitten in einer Lektion
        // war, stand nach dem Update wieder am Anfang. Die Schritte tragen ihren Text selbst,
        // deshalb stört eine neue Inhaltsversion sie nicht.
        val session = progress.lessonSession
            ?.takeIf { it.lessonId in lessonIds && it.steps.isNotEmpty() }
            ?.takeIf { it.steps.none { step -> step.chunkId.isNotBlank() && step.chunkId !in chunkIds } }
            ?.let { it.copy(index = it.index.coerceIn(0, it.steps.lastIndex)) }
        // Auch erzeugte Schritte einer offenen Runde tragen bereits Erledigtes.
        val liveStepIds = stepIds + session?.steps?.map { it.id }.orEmpty()
        return progress.copy(
            contentVersion = version,
            completedLessons = progress.completedLessons.filterTo(mutableSetOf()) { it in lessonIds },
            completedSteps = progress.completedSteps.filterTo(mutableSetOf()) { it in liveStepIds },
            lessonProgress = progress.lessonProgress.filterKeys { it in lessonIds },
            lessonRuns = progress.lessonRuns.filterKeys { it in lessonIds },
            chunkMemory = progress.chunkMemory.filterKeys { it in chunkIds },
            errorCounts = progress.errorCounts.filterKeys { it in errorCardIds },
            canDo = progress.canDo.filterTo(mutableSetOf()) { it in canDoIds },
            // Ein Fehler ohne Satzbezug (Grammatik, Muster) bleibt – nur verschwundene Sätze gehen.
            mistakes = progress.mistakes.filter { it.chunkId.isBlank() || it.chunkId in chunkIds },
            lessonSession = session,
            lastPracticeChunkIds = progress.lastPracticeChunkIds.filter { it in chunkIds },
            variantIds = progress.variantIds.filterKeys { it in situationIds }
        )
    }

    companion object {
        fun of(content: ContentBundle): ContentIndex = ContentIndex(
            lessonIds = content.lessons.map { it.id }.toSet(),
            chunkIds = content.chunks.map { it.id }.toSet(),
            patternIds = content.patterns.map { it.id }.toSet(),
            situationIds = content.situations.map { it.id }.toSet(),
            errorCardIds = content.errorCards.map { it.id }.toSet(),
            stepIds = content.lessons.flatMap { lesson -> lesson.steps.map { it.id } }.toSet(),
            canDoIds = content.canDo.map { it.id }.toSet()
        )
    }
}
