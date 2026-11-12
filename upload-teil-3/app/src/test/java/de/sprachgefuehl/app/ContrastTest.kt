package de.sprachgefuehl.app

import androidx.compose.ui.graphics.Color
import de.sprachgefuehl.app.ui.theme.Cream
import de.sprachgefuehl.app.ui.theme.DarkExtra
import de.sprachgefuehl.app.ui.theme.Ink
import de.sprachgefuehl.app.ui.theme.InkSoft
import de.sprachgefuehl.app.ui.theme.LightExtra
import de.sprachgefuehl.app.ui.theme.Line
import de.sprachgefuehl.app.ui.theme.NightBackground
import de.sprachgefuehl.app.ui.theme.NightInk
import de.sprachgefuehl.app.ui.theme.NightInkSoft
import de.sprachgefuehl.app.ui.theme.NightOutline
import de.sprachgefuehl.app.ui.theme.NightSage
import de.sprachgefuehl.app.ui.theme.NightSurface
import de.sprachgefuehl.app.ui.theme.NightTerracotta
import de.sprachgefuehl.app.ui.theme.Paper
import de.sprachgefuehl.app.ui.theme.Sage
import de.sprachgefuehl.app.ui.theme.SageDark
import de.sprachgefuehl.app.ui.theme.SageSoft
import de.sprachgefuehl.app.ui.theme.Terracotta
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

/**
 * Lesbarkeit ist keine Geschmacksfrage: Der Test rechnet die Kontraste der Paare nach, die die
 * Oberfläche wirklich benutzt – für das helle und das dunkle Thema.
 *
 * Grenzen nach WCAG: 4,5:1 für Fließtext, 3:1 für große Schrift, Symbole und die Kanten von
 * Bedienelementen. Reine Zierlinien und Kartenkanten im hellen Thema sind absichtlich leise und
 * stehen darum nicht in der Liste; im dunklen Thema trägt die Kante die Form und muss 3:1 halten.
 */
class ContrastTest {

    private fun channel(value: Float): Double {
        val v = value.toDouble()
        return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
    }

    private fun luminance(color: Color): Double =
        0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)

    private fun ratio(first: Color, second: Color): Double {
        val a = luminance(first)
        val b = luminance(second)
        return (maxOf(a, b) + 0.05) / (minOf(a, b) + 0.05)
    }

    private data class Pair2(val label: String, val foreground: Color, val background: Color, val minimum: Double)

    private val lightPairs = listOf(
        Pair2("Fließtext auf Grund", Ink, Cream, 4.5),
        Pair2("Fließtext auf Karte", Ink, Paper, 4.5),
        Pair2("Sekundärtext auf Karte", InkSoft, Paper, 4.5),
        Pair2("Sekundärtext auf Grund", InkSoft, Cream, 4.5),
        Pair2("Knopftext auf Knopf", Color.White, Sage, 4.5),
        Pair2("Pillentext", SageDark, SageSoft, 4.5),
        Pair2("Akzent groß auf Karte", Sage, Paper, 3.0),
        Pair2("Warnakzent groß auf Karte", Terracotta, Paper, 3.0),
        Pair2("Fehlertext auf Karte", Color(0xFF9C3B2E), Paper, 4.5),
        Pair2("Blatt im Garten", LightExtra.leaf, Paper, 3.0)
    )

    private val darkPairs = listOf(
        Pair2("Fließtext auf Grund", NightInk, NightBackground, 4.5),
        Pair2("Fließtext auf Karte", NightInk, NightSurface, 4.5),
        Pair2("Sekundärtext auf Karte", NightInkSoft, NightSurface, 4.5),
        Pair2("Sekundärtext auf Grund", NightInkSoft, NightBackground, 4.5),
        Pair2("Knopftext auf Knopf", Color(0xFF14251A), NightSage, 4.5),
        Pair2("Pillentext", Color(0xFFD6EBDC), Color(0xFF2F4635), 4.5),
        Pair2("Akzent groß auf Karte", NightSage, NightSurface, 3.0),
        Pair2("Warnakzent groß auf Karte", NightTerracotta, NightSurface, 3.0),
        Pair2("Fehlertext auf Karte", Color(0xFFE6A79B), NightSurface, 4.5),
        Pair2("Kartenkante auf Karte", NightOutline, NightSurface, 3.0),
        Pair2("Blatt im Garten", DarkExtra.leaf, NightSurface, 3.0),
        Pair2("Pflanze im Garten", DarkExtra.pot, NightSurface, 3.0)
    )

    private fun check(pairs: List<Pair2>, theme: String) {
        val report = StringBuilder("\nKontraste $theme\n")
        val failures = mutableListOf<String>()
        pairs.forEach { pair ->
            val value = ratio(pair.foreground, pair.background)
            val ok = value >= pair.minimum
            report.append(
                "  %-28s %5.2f : 1  (min %.1f) %s%n".format(pair.label, value, pair.minimum, if (ok) "ok" else "ZU NIEDRIG")
            )
            if (!ok) failures += "${pair.label}: %.2f von %.1f".format(value, pair.minimum)
        }
        println(report)
        assertTrue("$theme zu kontrastarm -> $failures$report", failures.isEmpty())
    }

    @Test
    fun `the light theme keeps its text readable`() {
        check(lightPairs, "hell")
    }

    @Test
    fun `the dark theme keeps its text readable`() {
        check(darkPairs, "dunkel")
    }

    @Test
    fun `the card edge stays visible in the dark theme`() {
        assertTrue(
            "Kante auf Karte: %.2f : 1".format(ratio(NightOutline, NightSurface)),
            ratio(NightOutline, NightSurface) >= 3.0
        )
        assertTrue(
            "Kante auf Grund: %.2f : 1".format(ratio(NightOutline, NightBackground)),
            ratio(NightOutline, NightBackground) >= 3.0
        )
    }

    @Test
    fun `the light theme border stays quiet but visible`() {
        // Der helle Rand ist bewusst leise (Zierde, die Karte trägt sich über Weiß auf Creme).
        // Geprüft wird nur, dass er überhaupt eine Kante zeichnet.
        assertTrue("Rand zu blass: %.2f".format(ratio(Line, Paper)), ratio(Line, Paper) >= 1.2)
    }
}
