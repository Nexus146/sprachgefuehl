package de.sprachgefuehl.app.domain

/**
 * German counts, kept out of the screens so every place says the same thing – and so the
 * "1 Minuten" mistake cannot come back. The adjective in [activeDays] has to agree with the
 * number, which is why a plain plural helper is not enough.
 */
object Phrasing {

    /** "1 Minute", "5 Minuten", "0 Minuten". */
    fun minutes(count: Int): String = "$count " + if (count == 1) "Minute" else "Minuten"

    /** "1 Satz", "5 Sätze", "0 Sätze". */
    fun sentences(count: Int): String = "$count " + if (count == 1) "Satz" else "Sätze"

    /** Days with at least one round. */
    fun activeDays(count: Int): String = when (count) {
        0 -> "noch kein Tag"
        1 -> "1 aktiver Tag"
        else -> "$count aktive Tage"
    }

    /** What happened to the speaking part of a round. */
    fun spokenTasks(count: Int): String = when (count) {
        0 -> "ohne Sprechen"
        1 -> "1 Satz gesprochen"
        else -> "$count Sätze gesprochen"
    }
}
