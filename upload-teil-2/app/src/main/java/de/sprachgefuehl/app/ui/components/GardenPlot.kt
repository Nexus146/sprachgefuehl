package de.sprachgefuehl.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import de.sprachgefuehl.app.domain.GardenBed
import de.sprachgefuehl.app.ui.theme.LocalSprachColors
import de.sprachgefuehl.app.ui.theme.SprachColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ein einzelnes Beet. Es zeigt nur, was in diesem Bereich schon gewachsen ist: kein Prozentwert,
 * keine Bewertung. Ein Beet ohne Beobachtung bleibt bewusst leer.
 */
@Composable
fun GardenPlot(bed: GardenBed, modifier: Modifier = Modifier) {
    val palette = LocalSprachColors.current
    val leaf = palette.leaf
    val soil = palette.warning
    val petal = petalColor(bed.plant.id, palette)
    val growth by animateFloatAsState(
        targetValue = bed.growth.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 50f),
        label = "bed"
    )
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRoundRect(
            color = soil.copy(alpha = 0.30f),
            topLeft = Offset(0f, h * 0.84f),
            size = Size(w, h * 0.16f),
            cornerRadius = CornerRadius(h * 0.06f)
        )
        if (bed.stage <= 0) {
            drawCircle(
                color = leaf.copy(alpha = 0.18f),
                radius = h * 0.04f,
                center = Offset(w / 2, h * 0.84f)
            )
            return@Canvas
        }
        val baseY = h * 0.85f
        val stem = h * (0.20f + 0.58f * growth)
        val topY = baseY - stem
        drawLine(
            color = leaf,
            start = Offset(w / 2, baseY),
            end = Offset(w / 2, topY),
            strokeWidth = w * 0.035f,
            cap = StrokeCap.Round
        )
        val leaves = if (bed.stage >= 2) 2 else 1
        for (i in 0 until leaves) {
            val y = baseY - stem * (0.32f + 0.26f * i)
            val side = if (i % 2 == 0) 1f else -1f
            val length = w * 0.20f
            rotate(degrees = -30f * side, pivot = Offset(w / 2, y)) {
                drawOval(
                    color = leaf.copy(alpha = 0.85f),
                    topLeft = Offset(
                        if (side > 0) w / 2 else w / 2 - length,
                        y - length * 0.22f
                    ),
                    size = Size(length, length * 0.44f)
                )
            }
        }
        if (bed.stage >= 4) {
            drawBloom(bed.plant.id, topY, w, h, petal, palette.pot)
        } else if (bed.stage == 3) {
            drawCircle(
                color = petal.copy(alpha = 0.75f),
                radius = h * 0.042f,
                center = Offset(w / 2, topY)
            )
        }
    }
}

/** Die Blüte je Art – gleiche Erde, gleiche Blätter, aber jede Pflanze hat ihre eigene Form. */
private fun DrawScope.drawBloom(
    plantId: String,
    topY: Float,
    w: Float,
    h: Float,
    petal: Color,
    core: Color
) {
    val cx = w / 2
    // Deutlich größer als der Knospenkopf (h * 0.042), damit Blüte und Knospe unterscheidbar bleiben.
    val r = h * 0.10f
    when (plantId) {
        "sonnenblume" -> {
            drawCircle(color = petal, radius = r * 1.25f, center = Offset(cx, topY))
            drawCircle(color = core, radius = r * 0.55f, center = Offset(cx, topY))
            for (i in 0 until 6) {
                val angle = (Math.PI * 2 * i / 6).toFloat()
                drawCircle(
                    color = core,
                    radius = r * 0.14f,
                    center = Offset(cx + cos(angle) * r * 0.32f, topY + sin(angle) * r * 0.32f)
                )
            }
        }
        "rose" -> {
            drawCircle(color = petal, radius = r * 1.2f, center = Offset(cx, topY))
            drawCircle(color = petal.copy(alpha = 0.55f), radius = r * 0.85f, center = Offset(cx, topY))
            drawCircle(color = petal.copy(alpha = 0.35f), radius = r * 0.45f, center = Offset(cx, topY))
        }
        "lavendel" -> {
            for (i in 0 until 5) {
                val y = topY - i * r * 0.45f
                drawCircle(
                    color = petal.copy(alpha = 0.9f - i * 0.08f),
                    radius = r * 0.42f,
                    center = Offset(cx - r * 0.12f, y)
                )
                drawCircle(
                    color = petal.copy(alpha = 0.85f - i * 0.08f),
                    radius = r * 0.42f,
                    center = Offset(cx + r * 0.12f, y - r * 0.18f)
                )
            }
        }
        "tulpe" -> {
            drawOval(
                color = petal,
                topLeft = Offset(cx - r * 0.82f, topY - r * 1.15f),
                size = Size(r * 1.64f, r * 2.1f)
            )
            drawLine(
                color = petal.copy(alpha = 0.40f),
                start = Offset(cx, topY - r * 1.0f),
                end = Offset(cx, topY + r * 0.9f),
                strokeWidth = w * 0.014f
            )
        }
        else -> {
            // Efeu: eine kurze Ranke mit drei Beeren statt einer Blüte.
            for (i in 0 until 3) {
                val y = topY - r * (0.4f + i * 0.55f)
                val side = if (i % 2 == 0) 1f else -1f
                drawLine(
                    color = petal.copy(alpha = 0.5f),
                    start = Offset(cx, y + r * 0.5f),
                    end = Offset(cx + side * r * 0.5f, y),
                    strokeWidth = w * 0.014f,
                    cap = StrokeCap.Round
                )
                drawCircle(color = petal, radius = r * 0.24f, center = Offset(cx + side * r * 0.5f, y))
            }
        }
    }
}

/** Jede Art blüht in ihrer eigenen Farbe – so sind die fünf Beete auch ohne Text zu unterscheiden. */
private fun petalColor(plantId: String, palette: SprachColors): Color = when (plantId) {
    "sonnenblume" -> palette.bloomSun
    "rose" -> palette.bloomRose
    "lavendel" -> palette.bloomLavender
    "tulpe" -> palette.bloomTulip
    else -> palette.bloomIvy
}
