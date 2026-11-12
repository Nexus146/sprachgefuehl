package de.sprachgefuehl.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import de.sprachgefuehl.app.ui.theme.LocalMotion
import de.sprachgefuehl.app.ui.theme.Spacing
import kotlinx.coroutines.delay

/**
 * Der Hintergrund atmet: ein sehr sanfter Verlauf statt einer einzigen Fläche. Er liegt hinter
 * dem Inhalt und scrollt nicht mit, damit das Papier ruhig bleibt.
 */
@Composable
fun AppBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val background = MaterialTheme.colorScheme.background
    val wash = MaterialTheme.colorScheme.surfaceVariant
    // Im Dunkeln war die breite Aufhellung in der Mitte als grauer Schleier zu sehen, durch den
    // die Karten wie ausgeschnitten wirkten. Dort bleibt der Grund fast flächig und hellt nur
    // nach unten um eine Spur auf.
    val dark = background.luminance() < 0.5f
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val brush = if (dark) {
                    Brush.verticalGradient(
                        0f to background,
                        1f to lerp(background, wash, 0.22f)
                    )
                } else {
                    Brush.verticalGradient(
                        0f to background,
                        0.55f to wash.copy(alpha = 0.45f),
                        1f to background
                    )
                }
                onDrawBehind { drawRect(brush) }
            }
    ) { content() }
}

/**
 * Eine Breite für alles, was in einer Spalte steht: Karten, Knöpfe – und die Tab-Leiste unten,
 * die sonst über den ganzen Tablet-Schirm auseinanderliefe.
 */
val MaxContentWidth = 560.dp

/**
 * Begrenzt die Zeilenlänge auf breiten Bildschirmen und zentriert den Inhalt. Auf dem Telefon
 * ändert sich dadurch nichts, auf Tablet und im Querformat bleiben Sätze lesbar.
 */
@Composable
fun AdaptiveFrame(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    BoxWithConstraints(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val wide = maxWidth >= 600.dp
        Box(
            modifier = Modifier
                .widthIn(max = MaxContentWidth)
                .fillMaxHeight()
                .then(if (wide) Modifier.padding(horizontal = Spacing.m) else Modifier)
        ) { content() }
    }
}

/** Karten und Knöpfe sinken beim Drücken leicht ein – Tippen wird spürbar. */
@Composable
fun Modifier.pressScale(interaction: InteractionSource): Modifier {
    val pressed by interaction.collectIsPressedAsState()
    val motion = LocalMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && !motion.reduced) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 700f),
        label = "pressScale"
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** Lässt einen Block beim Öffnen kurz nachrücken – gestaffelt über [index]. */
@Composable
fun Modifier.reveal(index: Int): Modifier {
    val motion = LocalMotion.current
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!motion.reduced) delay(40L * index)
        shown = true
    }
    val progress by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = motion.base, easing = motion.easing),
        label = "reveal"
    )
    return graphicsLayer {
        alpha = progress
        translationY = (1f - progress) * 24f
    }
}

/** Eine Zahl, die beim Anzeigen hochzählt – ruhig, ohne zu blinken. */
@Composable
fun AnimatedCount(
    value: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified
) {
    val motion = LocalMotion.current
    val animated by animateIntAsState(
        targetValue = value,
        animationSpec = tween(
            durationMillis = if (motion.reduced) 0 else motion.slow,
            easing = motion.easing
        ),
        label = "count"
    )
    Text(text = "$animated", modifier = modifier, style = style, color = color)
}
