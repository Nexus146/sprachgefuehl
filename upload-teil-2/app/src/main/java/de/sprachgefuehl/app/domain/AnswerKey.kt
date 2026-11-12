package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Step

/**
 * Welche Zeile einer Auswahlaufgabe wirklich stimmt.
 *
 * Die meisten Schritte tragen ihr Häkchen in den Optionen. Die Verbindungs- und Hörformate aus
 * dem Katalog tun das nicht – dort steht die Lösung im Zielsatz: ein Verbindungswort, das im
 * fertigen Satz vorkommt, oder die ausgeschriebene Fassung, die genau dem Ziel entspricht.
 *
 * Findet sich keine eindeutige Lösung, gibt es keine: Dann wird die Antwort nicht als falsch
 * bewertet, statt eine beliebige Zeile zur richtigen zu erklären.
 */
object AnswerKey {

    /** Der Index der richtigen Option oder -1, wenn die Aufgabe ihn nicht hergibt. */
    fun correctIndex(step: Step): Int {
        val flagged = step.options.indexOfFirst { it.correct }
        if (flagged >= 0) return flagged
        val target = step.target.trim()
        if (target.isBlank()) return -1
        return when (step.type) {
            // Hier ist die Lösung eine Schreibweise des Ziels, nicht ein Wort daraus.
            "MinimalPair", "UnderstandFastSpeech" ->
                step.options.indexOfFirst { it.text.trim().equals(target, ignoreCase = true) }
            // Ein Verbindungswort steht im fertigen Satz – und nur eins davon.
            "ChooseConnector", "ConnectClauses" -> onlyOptionInTarget(step, target)
            else -> onlyOptionInTarget(step, target)
        }
    }

    private fun onlyOptionInTarget(step: Step, target: String): Int {
        val hits = step.options.indices.filter { index ->
            val text = step.options[index].text.trim()
            text.isNotBlank() && Regex("\\b${Regex.escape(text)}\\b", RegexOption.IGNORE_CASE)
                .containsMatchIn(target)
        }
        return if (hits.size == 1) hits.first() else -1
    }
}
