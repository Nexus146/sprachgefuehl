package de.sprachgefuehl.app.domain

object Placement {

    /** selfRating: 0 = kaum Englisch, 1 = versteht Einfaches, 2 = spricht schon etwas. */
    fun level(correct: Int, total: Int, selfRating: Int): Int {
        if (total <= 0) return selfRating.coerceIn(0, 2)
        val ratio = correct.toDouble() / total.toDouble()
        val fromTest = when {
            ratio >= 0.8 -> 2
            ratio >= 0.45 -> 1
            else -> 0
        }
        // The test counts double, the self rating adjusts it. Starting slightly easier is
        // deliberate: the first lesson should feel achievable, not like an exam.
        val weighted = (fromTest * 2 + selfRating.coerceIn(0, 2) + 1) / 3
        return weighted.coerceIn(0, 2)
    }

    fun levelLabel(level: Int): String = when (level) {
        0 -> "Wir starten ganz am Anfang"
        1 -> "Wir starten mit einfachen Sätzen"
        else -> "Wir starten mit etwas mehr Tempo"
    }
}
