package de.sprachgefuehl.app.domain

import de.sprachgefuehl.app.data.Progress

/**
 * Die Tagesstreak. Sie zeigt nur, wie viele Tage in Folge etwas passiert ist – sie droht nicht,
 * sie sperrt nichts und ein Pausentag nimmt nichts weg. Der laufende Wert zählt bis gestern
 * weiter, solange der heutige Tag noch offen ist; so entsteht kein Druck am Vormittag.
 */
data class StreakInfo(
    val current: Int,
    val best: Int,
    val todayDone: Boolean,
    /** Montag bis Sonntag der laufenden Woche. */
    val week: List<Boolean>,
    val activeDays: Int
) {
    val weekDone: Int get() = week.count { it }
}

object Streak {

    fun of(progress: Progress, today: Long): StreakInfo {
        val days = progress.sessions.map { it.day }.toSet()
        val todayDone = today in days

        var cursor = if (todayDone) today else today - 1
        var current = 0
        while (cursor in days) {
            current += 1
            cursor -= 1
        }

        var best = 0
        var run = 0
        var previous: Long? = null
        days.sorted().forEach { day ->
            run = if (previous != null && day == previous + 1) run + 1 else 1
            if (run > best) best = run
            previous = day
        }

        val weekStart = today - ((today + 3) % 7)
        val week = (0L..6L).map { weekStart + it in days }

        return StreakInfo(
            current = current,
            best = best,
            todayDone = todayDone,
            week = week,
            activeDays = days.size
        )
    }

    /** Ein Satz, der die Zahl einordnet, ohne zu mahnen. */
    fun line(info: StreakInfo): String = when {
        info.current == 0 && !info.todayDone ->
            "Noch kein Tag in Folge. Eine Runde heute beginnt die Serie."
        info.current == 0 -> "Heute ist ein guter Anfang."
        info.current == 1 && info.todayDone -> "Ein Tag in Folge. Der Anfang ist gemacht."
        info.current == 1 -> "Ein Tag in Folge. Heute ist noch offen."
        info.todayDone -> "${info.current} Tage in Folge – heute zählt mit."
        else -> "${info.current} Tage in Folge. Heute ist noch offen."
    }
}
