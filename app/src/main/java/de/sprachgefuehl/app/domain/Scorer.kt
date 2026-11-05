package de.sprachgefuehl.app.domain

data class ScoreResult(
    val grade: Int,
    val headline: String,
    val detail: String,
    val recognized: String,
    val missing: List<String>,
    val usedHint: Boolean = false
) {
    val isSuccess: Boolean get() = grade >= 2
}

/**
 * Compares what the learner said with the target. Content words carry the meaning, so a
 * missing "the" should not count like a missing "headache". No percentages are shown to the
 * learner: three honest levels are enough and they prevent false precision.
 */
object Scorer {

    private val stopWords = setOf(
        "a", "an", "the", "to", "of", "in", "on", "at", "for", "and", "or", "is", "am", "are",
        "was", "were", "be", "been", "do", "does", "did", "i", "you", "he", "she", "it", "we",
        "they", "my", "your", "her", "his", "their", "this", "that", "these", "those", "please",
        "with", "from", "as", "me", "him", "them", "us", "there", "have", "has", "had", "would",
        "could", "can", "will", "should"
    )

    private val contractions = mapOf(
        "don't" to "do not", "doesn't" to "does not", "didn't" to "did not",
        "can't" to "can not", "cannot" to "can not", "won't" to "will not",
        "i'm" to "i am", "i've" to "i have", "i'd" to "i would", "i'll" to "i will",
        "you're" to "you are", "you've" to "you have", "we're" to "we are",
        "they're" to "they are", "it's" to "it is", "that's" to "that is",
        "she's" to "she is", "he's" to "he is",
        "isn't" to "is not", "aren't" to "are not", "wasn't" to "was not",
        "weren't" to "were not", "haven't" to "have not", "hasn't" to "has not",
        "wouldn't" to "would not", "couldn't" to "could not", "shouldn't" to "should not",
        "let's" to "let us"
    )

    private val numbers = mapOf(
        "zero" to "0", "one" to "1", "two" to "2", "three" to "3", "four" to "4",
        "five" to "5", "six" to "6", "seven" to "7", "eight" to "8", "nine" to "9",
        "ten" to "10", "eleven" to "11", "twelve" to "12", "thirteen" to "13",
        "fourteen" to "14", "fifteen" to "15", "sixteen" to "16", "seventeen" to "17",
        "eighteen" to "18", "nineteen" to "19", "twenty" to "20", "thirty" to "30",
        "forty" to "40", "fifty" to "50", "sixty" to "60", "seventy" to "70",
        "eighty" to "80", "ninety" to "90", "hundred" to "100"
    )

    fun normalize(text: String): List<String> {
        val cleaned = text.lowercase()
            .replace('’', '\'')
            .replace(Regex("[^a-z0-9' ]"), " ")
            .trim()
        if (cleaned.isEmpty()) return emptyList()
        return cleaned.split(Regex("\\s+"))
            .flatMap { token -> (contractions[token] ?: numbers[token] ?: token).split(" ") }
            .filter { it.isNotBlank() }
    }

    fun score(
        target: String,
        heard: String,
        requiredForm: List<String> = emptyList(),
        accept: List<String> = emptyList(),
        usedHint: Boolean = false,
        /** Diktat und Tippen: halbe Sätze gelten hier nicht als „fast geschafft“. */
        strict: Boolean = false
    ): ScoreResult {
        val heardTokens = normalize(heard)
        val spoken = heard.trim()
        if (heardTokens.isEmpty()) {
            return ScoreResult(
                grade = 0,
                headline = "Ich habe nichts gehört",
                detail = "Tippe auf das Mikrofon und sprich direkt danach.",
                recognized = "",
                missing = normalize(target),
                usedHint = usedHint
            )
        }

        val variants = (listOf(target) + accept).distinct()
        var best: VariantScore? = null
        for (variant in variants) {
            val candidate = evaluate(variant, heardTokens, requiredForm, strict)
            if (best == null || candidate.grade > best.grade ||
                (candidate.grade == best.grade && candidate.ratio > best.ratio)
            ) {
                best = candidate
            }
        }
        val chosen = best ?: evaluate(target, heardTokens, requiredForm, strict)
        val formMissing = requiredForm.filter { req ->
            val reqTokens = normalize(req)
            reqTokens.isNotEmpty() && reqTokens.none { it in heardTokens }
        }
        var grade = chosen.grade
        if (formMissing.isNotEmpty() && grade > 2) grade = 2

        val headline = when {
            grade == 3 -> "Klar verstanden"
            grade == 2 -> "Fast geschafft"
            else -> "Nochmal zusammen"
        }
        val detail = when {
            grade == 3 -> "Die wichtigen Wörter waren da."
            grade == 2 && formMissing.isNotEmpty() ->
                "Der Sinn war da – achte noch auf: ${formMissing.joinToString(", ")}."
            grade == 2 ->
                "Der Sinn war da – es fehlte: ${chosen.missing.joinToString(", ").ifBlank { "ein kleines Wort" }}."
            else ->
                "Ich habe „${spoken.take(60)}“ verstanden. Höre das Modell einmal und sprich dann nach."
        }

        return ScoreResult(
            grade = grade,
            headline = headline,
            detail = detail,
            recognized = spoken,
            missing = chosen.missing,
            usedHint = usedHint
        )
    }

    private fun evaluate(
        target: String,
        heard: List<String>,
        requiredForm: List<String>,
        strict: Boolean
    ): VariantScore {
        val targetTokens = normalize(target).distinct()
        val heardSet = heard.toSet()
        if (targetTokens.isEmpty()) return VariantScore(3, 1.0, emptyList())
        val matched = targetTokens.filter { it in heardSet }
        val missing = targetTokens.filter { it !in heardSet }
        val contentTokens = targetTokens.filter { it !in stopWords }
        val contentMatched = contentTokens.filter { it in heardSet }
        val ratio = matched.size.toDouble() / targetTokens.size.toDouble()
        val contentRatio = if (contentTokens.isEmpty()) ratio
        else contentMatched.size.toDouble() / contentTokens.size.toDouble()

        // „Klar verstanden“ verdient nur, wer den Satz wirklich gesagt hat. In kurzen Formeln
        // („How are you?“) sind fast alle Wörter Funktionswörter – dann reichte früher das eine
        // Inhaltswort, und ein halber Satz galt als perfekt. Darum braucht die beste Stufe auch
        // eine Deckung über den ganzen Satz.
        val grade = if (strict) {
            when {
                ratio >= 0.95 && contentRatio >= 0.9 -> 3
                ratio >= 0.8 && contentRatio >= 0.75 -> 2
                else -> 1
            }
        } else {
            when {
                ratio >= 0.9 || (contentRatio >= 0.85 && ratio >= 0.6) -> 3
                ratio >= 0.55 || contentRatio >= 0.6 -> 2
                else -> 1
            }
        }
        return VariantScore(grade, maxOf(ratio, contentRatio), missing)
    }

    private data class VariantScore(val grade: Int, val ratio: Double, val missing: List<String>)
}
