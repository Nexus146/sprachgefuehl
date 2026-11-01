package de.sprachgefuehl.app.domain

/**
 * Rule based diagnosis. It maps a mismatch between target and what was heard to an authored
 * error card. Deliberately conservative: the generic card is used when nothing matches.
 */
object ErrorDiagnosis {

    private val durations = setOf(
        "day", "days", "week", "weeks", "month", "months", "year", "years",
        "hour", "hours", "minute", "minutes"
    )

    private val dayNames = setOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")

    private val questionStarters = setOf("do", "does", "did", "where", "what", "when", "which", "who", "how", "is", "are", "can", "could")

    fun diagnose(target: String, heard: String, fallbackPillar: String = ""): String {
        val t = Scorer.normalize(target)
        val h = Scorer.normalize(heard)
        if (h.isEmpty()) return ""
        val hs = h.toSet()
        val ts = t.toSet()

        val sinceIndex = h.indexOf("since")
        if (sinceIndex >= 0 && h.drop(sinceIndex + 1).any { it in durations }) return "err_since_for"

        if ((t.firstOrNull() == "could" || t.firstOrNull() == "would") &&
            (h.firstOrNull() == "give" || (h.firstOrNull() == "i" && h.getOrNull(1) == "want"))
        ) {
            return "err_register"
        }

        if (t.firstOrNull() in questionStarters) {
            val heardHasAux = h.firstOrNull() in setOf(
                "do", "does", "did", "where", "what", "when", "which", "how", "can", "could", "is", "are"
            )
            if (!heardHasAux) return "err_do_support"
        }

        if ((t.contains("a") || t.contains("an")) && !hs.contains("a") && !hs.contains("an") &&
            t.size >= 3 && !t.contains("the")
        ) {
            return "err_article"
        }

        if (t.contains("she") || t.contains("he") || t.contains("it")) {
            val thirdPerson = t.filter { it.endsWith("s") && it.length > 3 }
            if (thirdPerson.isNotEmpty() && thirdPerson.none { it in hs }) return "err_third_person"
        }

        if (h.contains("make") && (hs.contains("shopping") || hs.contains("homework"))) return "err_make_do"
        if (h.contains("do") && (hs.contains("mistake") || hs.contains("appointment"))) return "err_make_do"

        if (hs.contains("become") && (ts.contains("get") || ts.contains("got") || ts.contains("receive"))) return "err_become"
        if (hs.contains("handy") && !ts.contains("handy")) return "err_handy"
        if (hs.contains("gift") && ts.contains("present")) return "err_gift"

        if (target.trim().endsWith("?") && h.isNotEmpty() && hs.none { it in questionStarters }) {
            return "err_question_order"
        }

        if (h.zipWithNext().any { (a, b) -> a == "in" && b in dayNames }) return "err_prep_day"
        if (h.zipWithNext().any { (a, b) -> a == "on" && (b.toIntOrNull() != null || b == "ten" || b == "eight") }) return "err_prep_time"

        val becauseIndex = h.indexOf("because")
        if (becauseIndex >= 0) {
            val after = h.drop(becauseIndex + 1)
            if (after.isNotEmpty() && after.first() in setOf("was", "were", "is", "are", "has", "have", "can", "could", "will")) {
                return "err_because_order"
            }
        }

        if (hs.contains("childs") || hs.contains("grandchilds")) return "err_plural_child"
        if (hs.contains("as") && ts.contains("than")) return "err_comparative"
        if (h.contains("look") && h.contains("as")) return "err_look_like"
        if (ts.contains("many") && hs.contains("much")) return "err_much_many"
        if (ts.contains("much") && hs.contains("many") && (ts.contains("is") || ts.contains("water") || ts.contains("money"))) {
            return "err_much_many"
        }
        if (ts.contains("any") && hs.contains("some") && target.trim().endsWith("?")) return "err_some_any"
        if (hs.contains("yes") && h.drop(1).take(2).joinToString(" ") == "i have") return "err_short_answer"
        if (hs.contains("no") && h.drop(1).take(2).joinToString(" ") == "i have") return "err_short_answer"
        if (ts.contains("there") && hs.contains("it") && hs.contains("has")) return "err_there_is"
        if (ts.contains("lend") && hs.contains("borrow")) return "err_borrow_lend"
        if (ts.contains("borrow") && hs.contains("lend")) return "err_borrow_lend"
        if (hs.contains("said") && hs.contains("me")) return "err_say_tell"

        val pastMarkers = setOf("yesterday", "ago", "last")
        if (t.any { it in pastMarkers } || t.any { it.endsWith("ed") }) {
            if (h.none { it.endsWith("ed") } && hs.none { it in setOf("was", "were", "did", "went", "saw", "had", "got", "took", "made") }) {
                return "err_past_ed"
            }
        }

        if (h.size < (t.size * 0.55).toInt()) return "err_missing_word"
        return ""
    }

    fun isMeaningful(heard: String): Boolean = Scorer.normalize(heard).isNotEmpty()
}
