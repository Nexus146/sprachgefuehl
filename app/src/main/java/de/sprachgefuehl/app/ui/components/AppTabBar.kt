package de.sprachgefuehl.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.sprachgefuehl.app.ui.navigation.AppTab

/**
 * Four equal targets, large icons and a label under each: the bar has to work for a thumb and for
 * eyes that need bigger type. The selected tab is marked by the pill, not only by colour.
 */
@Composable
fun AppTabBar(active: AppTab, onSelect: (AppTab) -> Unit) {
    // Der Grund läuft über die ganze Breite, die vier Ziele bleiben in der Spalte der Karten.
    // Sonst stünden die Tabs auf dem Tablet meilenweit von dem entfernt, was sie öffnen.
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                NavigationBar(
                    modifier = Modifier
                        .widthIn(max = MaxContentWidth)
                        .fillMaxWidth(),
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    AppTab.entries.forEach { tab ->
                        val selected = tab == active
                        NavigationBarItem(
                            selected = selected,
                            onClick = { onSelect(tab) },
                            alwaysShowLabel = true,
                            modifier = Modifier
                                .heightIn(min = 72.dp)
                                .semantics { contentDescription = "${tab.label}-Bereich" },
                            icon = {
                                Icon(
                                    appIcon(tab.icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp)
                                )
                            },
                            label = {
                                Text(
                                    tab.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}
