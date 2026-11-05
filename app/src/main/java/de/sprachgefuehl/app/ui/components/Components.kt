package de.sprachgefuehl.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalPharmacy
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.sprachgefuehl.app.data.Profile
import de.sprachgefuehl.app.ui.theme.LocalMotion
import de.sprachgefuehl.app.ui.theme.LocalSprachColors
import de.sprachgefuehl.app.ui.theme.Sizes
import de.sprachgefuehl.app.ui.theme.Spacing
import kotlin.math.PI
import kotlin.math.sin

fun appIcon(name: String): ImageVector = when (name) {
    "mic" -> Icons.Rounded.Mic
    "home" -> Icons.Rounded.Home
    "health", "stethoscope" -> Icons.Rounded.MedicalServices
    "pharmacy" -> Icons.Rounded.LocalPharmacy
    "people", "family" -> Icons.Rounded.FamilyRestroom
    "cafe" -> Icons.Rounded.LocalCafe
    "basket" -> Icons.Rounded.ShoppingBasket
    "map" -> Icons.Rounded.Map
    "bus" -> Icons.Rounded.DirectionsBus
    "phone" -> Icons.Rounded.Phone
    "help" -> Icons.Rounded.People
    "slow" -> Icons.Rounded.Hearing
    "repair" -> Icons.Rounded.RecordVoiceOver
    "grammar" -> Icons.Rounded.MenuBook
    "link" -> Icons.Rounded.Link
    "spark" -> Icons.Rounded.AutoAwesome
    "leaf", "garden" -> Icons.Rounded.Spa
    "chat" -> Icons.Rounded.Chat
    "star" -> Icons.Rounded.Star
    "person" -> Icons.Rounded.Person
    else -> Icons.Rounded.Chat
}

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    // Der Pfeil sitzt oben neben der Überschrift. Mittig ausgerichtet schwebte er bei
    // mehrzeiligem Untertitel mitten im Text und war nicht mehr als „zurück“ zu erkennen.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.m, vertical = Spacing.s),
        verticalAlignment = Alignment.Top
    ) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Zurück",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.width(Spacing.xs))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    secondary: Boolean = false
) {
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(20.dp)
    val background = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant
        secondary -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.primary
    }
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant
        secondary -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onPrimary
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        color = background,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.button)
            .then(
                if (secondary) Modifier.border(Sizes.border, MaterialTheme.colorScheme.outline, shape)
                else Modifier
            ),
        interactionSource = interaction
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(Spacing.s))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(Sizes.corner)
    val base = modifier
        .fillMaxWidth()
        .border(Sizes.border, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), shape)
        .then(
            if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick)
            else Modifier
        )
    Surface(
        modifier = base,
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(Spacing.l), content = content)
    }
}

@Composable
fun Tag(text: String, active: Boolean = false, onClick: (() -> Unit)? = null) {
    val shape = RoundedCornerShape(100)
    val bg = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        shape = shape,
        color = bg,
        modifier = Modifier
            .heightIn(min = 36.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s)
        )
    }
}

@Composable
fun ProgressChain(total: Int, current: Int, modifier: Modifier = Modifier) {
    if (isLargeFont()) {
        // Bei großer Schrift passen fünfzehn Punkte nicht mehr neben die Schrittzahl; ein
        // schmaler Balken zeigt denselben Fortschritt und bleibt ruhig.
        val share = ((current + 1).toFloat() / total.coerceAtLeast(1)).coerceIn(0f, 1f)
        Box(
            modifier = modifier
                .height(10.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(100))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(share)
                    .height(10.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(100))
            )
        }
        return
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val active = index < current
            Box(
                modifier = Modifier
                    .size(width = if (index == current) 22.dp else 10.dp, height = 10.dp)
                    .background(
                        color = if (active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(100)
                    )
            )
        }
    }
}

/**
 * Ab dieser Schriftgröße passen drei Kennzahlen nicht mehr nebeneinander: „Minuten“ und
 * „gesprochen“ brachen mitten im Wort um. Dann stehen sie untereinander, je Zahl ein Wort.
 */
private const val STACKED_FONT_SCALE = 1.3f

/** Ob die Schrift so groß ist, dass zwei Dinge nebeneinander nicht mehr passen. */
@Composable
fun isLargeFont(): Boolean = LocalDensity.current.fontScale >= STACKED_FONT_SCALE

@Composable
fun StatRow(stats: List<Pair<String, String>>) {
    if (isLargeFont()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            stats.forEach { (value, label) ->
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(Spacing.s))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = Spacing.xs)
                    )
                }
            }
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            stats.forEachIndexed { index, (value, label) ->
                StatTile(value, label, Modifier.weight(if (index == 1) 1.4f else 1f))
            }
        }
    }
}

/**
 * Garten mit Text daneben. Bei großer Schrift bleibt neben der Pflanze zu wenig Platz, und jede
 * Zeile zerfällt in Wortfetzen – dann steht die Pflanze oben und der Text darunter.
 */
@Composable
fun GardenSummary(
    stage: Int,
    title: String,
    stageLabel: String,
    line: String,
    nextLine: String? = null
) {
    val stacked = isLargeFont()
    val plant = @Composable { modifier: Modifier ->
        GardenPlant(stage = stage, modifier = modifier.width(110.dp).height(120.dp))
    }
    val labels = @Composable { modifier: Modifier ->
        Column(modifier = modifier) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Spacing.xs))
            Text(stageLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Text(line, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (nextLine != null) {
                Text(nextLine, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
    if (stacked) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            plant(Modifier)
            Spacer(Modifier.height(Spacing.s))
            labels(Modifier.fillMaxWidth())
        }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            plant(Modifier)
            Spacer(Modifier.width(Spacing.m))
            labels(Modifier.weight(1f))
        }
    }
}

/**
 * Tageskarte: Ring und Text. Bei großer Schrift steht der Ring oben, sonst bliebe für den Text
 * nur ein schmaler Streifen und jeder Satz bräche in Stücke.
 */
@Composable
fun RingSummary(
    progress: Float,
    top: String,
    bottom: String,
    title: String,
    line: String,
    hint: String
) {
    val stacked = isLargeFont()
    val texts = @Composable { modifier: Modifier ->
        Column(modifier = modifier) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Spacing.xs))
            Text(line, style = MaterialTheme.typography.bodyMedium)
            Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (stacked) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DailyRing(progress = progress, top = top, bottom = bottom)
            Spacer(Modifier.height(Spacing.s))
            texts(Modifier.fillMaxWidth())
        }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DailyRing(progress = progress, top = top, bottom = bottom)
            Spacer(Modifier.width(Spacing.m))
            texts(Modifier.weight(1f))
        }
    }
}

/**
 * Karte mit Text und einem kleinen Link. Bei großer Schrift rutscht der Link unter den Text,
 * statt ihm die halbe Breite zu nehmen.
 */
@Composable
fun LinkCard(title: String, description: String, action: String) {
    val stacked = isLargeFont()
    val body = @Composable { modifier: Modifier ->
        Column(modifier = modifier) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    if (stacked) {
        Column {
            body(Modifier.fillMaxWidth())
            Spacer(Modifier.height(Spacing.xs))
            Text(action, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            body(Modifier.weight(1f))
            Text(action, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** A calm garden: it grows with sessions and never wilts. */
@Composable
fun GardenPlant(
    stage: Int,
    modifier: Modifier = Modifier,
    animate: Boolean = true
) {
    val target = (stage / 5f).coerceIn(0f, 1f)
    val progress by animateFloatAsState(
        targetValue = target,
        animationSpec = if (animate) spring(dampingRatio = 0.7f, stiffness = 40f) else tween(0),
        label = "garden"
    )
    val leaf = LocalSprachColors.current.leaf
    val soil = LocalSprachColors.current.warning
    val accent = LocalSprachColors.current.warning
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // pot
        val potWidth = w * 0.42f
        val potHeight = h * 0.16f
        val potTop = h * 0.80f
        drawRoundRect(
            color = soil.copy(alpha = 0.35f),
            topLeft = Offset((w - potWidth) / 2, potTop),
            size = Size(potWidth, potHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(potWidth * 0.12f)
        )
        if (progress <= 0.02f) {
            drawCircle(
                color = leaf.copy(alpha = 0.25f),
                radius = w * 0.03f,
                center = Offset(w / 2, potTop)
            )
            return@Canvas
        }
        // Auch der erste Trieb bekommt sichtbare Höhe; sonst bleibt die große Gartenfläche
        // oben leer und wirkt wie eine leere Karte.
        val stemHeight = h * (0.24f + 0.48f * progress)
        val baseY = potTop
        val topY = baseY - stemHeight
        drawLine(
            color = leaf,
            start = Offset(w / 2, baseY),
            end = Offset(w / 2, topY),
            strokeWidth = w * 0.022f,
            cap = StrokeCap.Round
        )
        val leafCount = (1 + (progress * 4).toInt()).coerceAtMost(5)
        for (i in 0 until leafCount) {
            val fraction = 0.25f + i * 0.17f
            val y = baseY - stemHeight * fraction
            val side = if (i % 2 == 0) 1f else -1f
            val leafLength = w * (0.10f + 0.03f * progress)
            rotate(degrees = -28f * side, pivot = Offset(w / 2, y)) {
                drawOval(
                    color = leaf.copy(alpha = 0.85f),
                    topLeft = Offset(
                        if (side > 0) w / 2 else w / 2 - leafLength,
                        y - leafLength * 0.22f
                    ),
                    size = Size(leafLength, leafLength * 0.44f)
                )
            }
        }
        if (progress > 0.85f) {
            drawCircle(color = accent, radius = w * 0.035f, center = Offset(w / 2, topY))
        }
    }
}

@Composable
fun Waveform(amplitude: Float, active: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val bars = 24
        val gap = size.width / bars
        for (i in 0 until bars) {
            val base = if (active) amplitude.coerceIn(0.05f, 1f) else 0.12f
            val wobble = if (active) (sin(phase + i * 0.6f).toFloat() * 0.25f + 0.75f) else 0.5f
            val barHeight = size.height * base * wobble
            drawLine(
                color = color.copy(alpha = 0.75f),
                start = Offset(i * gap + gap / 2, size.height / 2 - barHeight / 2),
                end = Offset(i * gap + gap / 2, size.height / 2 + barHeight / 2),
                strokeWidth = gap * 0.45f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun DividerLine(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    )
}

@Composable
fun SpeakerBadge(name: String) {
    val isYou = name == "you"
    Surface(
        shape = RoundedCornerShape(100),
        color = if (isYou) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = if (isYou) "Du" else "Gegenüber",
            style = MaterialTheme.typography.labelMedium,
            color = if (isYou) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.m, vertical = 6.dp)
        )
    }
}

@Composable
fun ListeningHint(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Rounded.VolumeUp,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(Spacing.s))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MicrophoneBadge(listening: Boolean) {
    val color = if (listening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val tint = if (listening) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        shape = RoundedCornerShape(100),
        color = color,
        modifier = Modifier
            .size(48.dp)
            .semantics { contentDescription = if (listening) "Mikrofon hört zu" else "Mikrofon bereit" }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Mic, contentDescription = null, tint = tint)
        }
    }
}

@Composable
fun HintLine(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalSprachColors.current.highlight, RoundedCornerShape(16.dp))
            .padding(Spacing.m),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Rounded.Favorite,
            contentDescription = null,
            tint = LocalSprachColors.current.warning,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(Spacing.s))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun LeafBadge(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Rounded.Spa,
            contentDescription = null,
            tint = LocalSprachColors.current.leaf,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(Spacing.xs))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** A calm goal ring: today's minutes against the learner's own goal. */
@Composable
fun DailyRing(
    progress: Float,
    top: String,
    bottom: String,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(LocalMotion.current.garden),
        label = "dailyGoal"
    )
    val track = MaterialTheme.colorScheme.surfaceVariant
    val leaf = LocalSprachColors.current.leaf
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.toPx() * 0.09f
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            if (animated > 0.001f) {
                drawArc(
                    color = leaf,
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(top, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text(bottom, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ProfileAvatar(profile: Profile, onClick: (() -> Unit)? = null, size: Dp = 54.dp) {
    val initial = profile.greetingName.trim().take(1).uppercase()
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .size(size)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (initial.isBlank()) {
                Icon(
                    Icons.Rounded.Spa,
                    contentDescription = "Profil öffnen",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            } else {
                Text(initial, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/** German counts: "1 Runde", "2 Runden" – so the copy stays honest for a single item. */
fun plural(count: Int, singular: String, plural: String): String =
    if (count == 1) "$count $singular" else "$count $plural"

@Composable
fun BigNumber(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
