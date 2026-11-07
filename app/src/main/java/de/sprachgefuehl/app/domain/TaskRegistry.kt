package de.sprachgefuehl.app.domain

/** One central description of a task type: label, trained skill and interaction shape. */
data class TaskSpec(
    val type: String,
    val label: String,
    val skill: Skill?,
    val speaking: Boolean = false,
    val needsAnswer: Boolean = false
)

/**
 * The single source of truth for task types. [TaskFormats] remains the public list of the nine
 * practice formats; this registry also covers the authored core steps and generated variants.
 */
object TaskRegistry {

    private val specs: Map<String, TaskSpec> = buildList {
        TaskFormats.all.forEach { format ->
            add(
                TaskSpec(
                    type = format.type,
                    label = format.label,
                    skill = format.skill,
                    speaking = format.skill == Skill.SPRECHEN,
                    needsAnswer = true
                )
            )
        }
        add(TaskSpec("SpeakFree", "Sprechen", Skill.SPRECHEN, speaking = true, needsAnswer = true))
        add(TaskSpec("TransferTask", "Übertragen", Skill.SPRECHEN, speaking = true, needsAnswer = true))
        add(TaskSpec("Shadowing", "Shadowing", Skill.SPRECHEN, speaking = true, needsAnswer = true))
        add(TaskSpec("AnswerQuestion", "Antworten", Skill.SPRECHEN, speaking = true, needsAnswer = true))
        add(TaskSpec("Paraphrase", "Umschreiben", Skill.SPRECHEN, speaking = true, needsAnswer = true))
        add(TaskSpec("WhatsAppReply", "Antworten", Skill.SPRECHEN, speaking = true, needsAnswer = true))
        add(TaskSpec("SpeakFillSlot", "Einsetzen", Skill.BAUSTEINE, speaking = true, needsAnswer = true))
        add(TaskSpec("ListenNumber", "Zahlen hören", Skill.HOEREN, needsAnswer = true))
        add(TaskSpec("MinimalPair", "Lautpaar", Skill.HOEREN, needsAnswer = true))
        add(TaskSpec("UnderstandFastSpeech", "Schnell hören", Skill.HOEREN, needsAnswer = true))
        add(TaskSpec("ComprehensionDeep", "Verstehen", Skill.HOEREN, needsAnswer = true))
        add(TaskSpec("GrammarSpot", "Grammatik", Skill.GRAMMATIK, needsAnswer = true))
        add(TaskSpec("CorrectTheSentence", "Richtig sagen", Skill.GRAMMATIK, needsAnswer = true))
        add(TaskSpec("OddOneOut", "Was passt nicht", Skill.BAUSTEINE, needsAnswer = true))
        add(TaskSpec("TranslateToGerman", "Zurückübersetzen", Skill.BAUSTEINE, needsAnswer = true))
        add(TaskSpec("TrueFalse", "Stimmt das", Skill.HOEREN, needsAnswer = true))
        add(TaskSpec("ConnectClauses", "Verbinden", Skill.VERKNUEPFEN, needsAnswer = true))
        add(TaskSpec("DialogInput", "Hören", null))
        add(TaskSpec("Noticing", "Die Form", null))
        add(TaskSpec("ChunkIntro", "Baustein", null))
        add(TaskSpec("SelfCheck", "Abschluss", null, needsAnswer = true))
    }.associateBy { it.type }

    fun spec(type: String): TaskSpec? = specs[type]

    fun skillFor(type: String): Skill? = specs[type]?.skill ?: TaskFormats.skillFor(type)

    fun isSpeaking(type: String): Boolean = specs[type]?.speaking == true

    fun needsAnswer(type: String): Boolean = specs[type]?.needsAnswer == true

    fun labelFor(type: String): String = specs[type]?.label ?: TaskFormats.labelFor(type)
}
