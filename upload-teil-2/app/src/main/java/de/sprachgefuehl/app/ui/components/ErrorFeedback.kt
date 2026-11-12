package de.sprachgefuehl.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.sprachgefuehl.app.data.ErrorCard
import de.sprachgefuehl.app.ui.theme.LocalSprachColors
import de.sprachgefuehl.app.ui.theme.Sizes
import de.sprachgefuehl.app.ui.theme.Spacing
import de.sprachgefuehl.app.ui.components.cardBorder
import de.sprachgefuehl.app.ui.components.cardElevation

enum class FeedbackTone { GOOD, ALMOST, RETRY }

@Composable
fun FeedbackPanel(
    tone: FeedbackTone,
    title: String,
    detail: String,
    model: String,
    onSpeakModel: () -> Unit,
    onSpeakSlow: (() -> Unit)? = null,
    errorCard: ErrorCard? = null,
    onRetry: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val background = when (tone) {
        FeedbackTone.GOOD -> colors.primaryContainer
        // „Fast“ ist ein guter Zwischenstand, kein Fehler: warmes Gelb statt Terrakotta.
        FeedbackTone.ALMOST -> LocalSprachColors.current.highlight
        FeedbackTone.RETRY -> colors.surfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(Sizes.corner),
        color = background,
        shadowElevation = cardElevation(),
        border = cardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Spacing.l)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (tone == FeedbackTone.GOOD) Icons.Rounded.CheckCircle else Icons.Rounded.Lightbulb,
                    contentDescription = null,
                    tint = if (tone == FeedbackTone.RETRY) colors.onSurfaceVariant else colors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(Spacing.s))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(Spacing.s))
            Text(detail, style = MaterialTheme.typography.bodyMedium)
            if (model.isNotBlank()) {
                Spacer(Modifier.height(Spacing.m))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            model,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s)
                        )
                    }
                    Spacer(Modifier.width(Spacing.s))
                    TapSurface(
                        onClick = onSpeakModel,
                        shape = RoundedCornerShape(100),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.VolumeUp,
                                contentDescription = "Modell anhören",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            if (onSpeakSlow != null && model.isNotBlank()) {
                Spacer(Modifier.height(Spacing.s))
                BigButton(
                    text = "Langsam noch einmal",
                    onClick = onSpeakSlow,
                    secondary = true
                )
            }
            if (errorCard != null) {
                Spacer(Modifier.height(Spacing.m))
                ErrorCardContent(errorCard, onSpeakModel)
            }
            if (onRetry != null) {
                Spacer(Modifier.height(Spacing.m))
                BigButton(
                    text = "Nochmal versuchen",
                    onClick = onRetry,
                    icon = Icons.Rounded.Refresh,
                    secondary = true
                )
            }
        }
    }
}

@Composable
fun ErrorCardContent(card: ErrorCard, onSpeak: () -> Unit) {
    Column {
        Text(
            card.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(Spacing.s))
        ErrorSection("Was ist passiert?", card.observation)
        ErrorSection("Warum ist das anders?", card.whyDe)
        Text(
            "So ist es richtig",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                card.correctEn,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            TapSurface(
                onClick = onSpeak,
                shape = RoundedCornerShape(100),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(44.dp)
            ) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.VolumeUp, contentDescription = "Satz anhören", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Spacer(Modifier.height(Spacing.s))
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = LocalSprachColors.current.highlight,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Merkanker: ${card.anchor}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(Spacing.m)
            )
        }
        Spacer(Modifier.height(Spacing.s))
        Text("So machst du es künftig richtig", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        card.checklist.forEach { item ->
            Text("· $item", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 2.dp))
        }
        Spacer(Modifier.height(Spacing.s))
        Text(
            "Nächster Schritt: ${card.nextStep}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        if (card.examples.isNotEmpty()) {
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "Beispiele: ${card.examples.joinToString(" · ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorSection(label: String, text: String) {
    Column(modifier = Modifier.padding(bottom = Spacing.s)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
