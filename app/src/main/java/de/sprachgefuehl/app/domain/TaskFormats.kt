package de.sprachgefuehl.app.domain

/**
 * Die neun Übungsformate, die der Aufgabenpool aus dem vorhandenen Inhalt baut.
 *
 * Ein Format beschreibt, was die Lernende tut – nicht, welcher Satz dran ist. Dieselbe Wendung
 * kann nachsprechen, übersetzen, hören, ergänzen oder aus dem Kopf abgerufen werden. Darum steht
 * die Liste hier einmal zentral: Unterricht, Übersicht und Tests benutzen dieselben Wörter.
 */
enum class TaskFormat(
    val type: String,
    val label: String,
    val skill: Skill,
    val instruction: String
) {
    SPEAK_REPEAT("SpeakRepeat", "Nachsprechen", Skill.SPRECHEN, "Hör zu und sprich nach."),
    TRANSLATE("TranslateToEnglish", "Übersetzen", Skill.SPRECHEN, "Sag den Satz auf Englisch."),
    RECALL("Recall", "Aus dem Kopf", Skill.SPRECHEN, "Sag den Satz ohne Modell."),
    BUILD("BuildSentence", "Wortstellung", Skill.GRAMMATIK, "Ordne die Wörter zum Satz."),
    MEANING("MeaningChoice", "Bedeutung", Skill.BAUSTEINE, "Wähle die deutsche Bedeutung."),
    LISTEN("ListenMeaning", "Hörverstehen", Skill.HOEREN, "Hör zu und wähle die Bedeutung."),
    DICTATION("Dictation", "Diktat", Skill.HOEREN, "Hör zu und schreibe den Satz."),
    GAP("GapFill", "Lücke", Skill.GRAMMATIK, "Wähle das Wort für die Lücke."),
    CONNECTOR("ChooseConnector", "Verbinden", Skill.VERKNUEPFEN, "Verbinde die beiden Sätze.")
}

object TaskFormats {

    /** Alle Formate, aus denen eine Runde schöpfen kann. */
    val all: List<TaskFormat> = TaskFormat.entries

    fun of(type: String): TaskFormat? = TaskFormat.entries.firstOrNull { it.type == type }

    /**
     * Welcher Bereich durch eine Aufgabe wirklich trainiert wird. Die Kernschritte einer Lektion
     * (Dialog, Baustein, Abschluss) trainieren keinen einzelnen Bereich und liefern null.
     */
    fun skillFor(type: String): Skill? = when (type) {
        "SpeakRepeat", "TranslateToEnglish", "Recall", "SpeakFree", "TransferTask",
        "Shadowing", "Paraphrase", "WhatsAppReply" -> Skill.SPRECHEN
        "BuildSentence", "GapFill", "GrammarSpot" -> Skill.GRAMMATIK
        "MeaningChoice", "SpeakFillSlot" -> Skill.BAUSTEINE
        "ListenMeaning", "Dictation", "ComprehensionDeep", "UnderstandFastSpeech",
        "ListenNumber", "MinimalPair" -> Skill.HOEREN
        "ChooseConnector", "ConnectClauses" -> Skill.VERKNUEPFEN
        else -> null
    }

    /** Ein kurzes Wort für den Kopf einer Aufgabe. */
    fun labelFor(type: String): String = of(type)?.label ?: when (type) {
        "DialogInput" -> "Hören"
        "ComprehensionDeep" -> "Verstehen"
        "Noticing" -> "Die Form"
        "ChunkIntro" -> "Baustein"
        "SpeakFree" -> "Sprechen"
        "GrammarSpot" -> "Grammatik"
        "SelfCheck" -> "Abschluss"
        else -> "Aufgabe"
    }
}
