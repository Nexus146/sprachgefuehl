package de.sprachgefuehl.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
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
