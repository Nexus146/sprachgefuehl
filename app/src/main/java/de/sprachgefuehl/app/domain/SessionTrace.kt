package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Lesson
import de.sprachgefuehl.app.data.Step

/** Local diagnosis only: no upload, no account, no background service. */
data class SessionTrace(
    val contentVersion: Int,
    val startedDay: Long,
    val seed: Long,
    val lessonId: String,
    val lessonTitle: String,
    val steps: List<Step>,
    val index: Int
) {
    fun render(): String = buildString {
        appendLine("Sprachgefühl diagnostics")
        appendLine("contentVersion=$contentVersion")
        appendLine("startedDay=$startedDay")
        appendLine("seed=$seed")
        appendLine("lesson=$lessonId \"$lessonTitle\"")
        appendLine("index=$index/${steps.size}")
        steps.forEachIndexed { position, step ->
            appendLine(
                "${position + 1}. ${step.type} | ${step.id} | chunk=${step.chunkId.ifBlank { "-" }} | " +
                    "why=${step.why.ifBlank { "-" }}"
            )
        }
    }
}

object Diagnostics {
    fun trace(
        contentVersion: Int,
        startedDay: Long,
        seed: Long,
        lesson: Lesson,
        steps: List<Step>,
        index: Int
    ): SessionTrace = SessionTrace(
        contentVersion = contentVersion,
        startedDay = startedDay,
        seed = seed,
        lessonId = lesson.id,
        lessonTitle = lesson.title,
        steps = steps,
        index = index
    )
}
