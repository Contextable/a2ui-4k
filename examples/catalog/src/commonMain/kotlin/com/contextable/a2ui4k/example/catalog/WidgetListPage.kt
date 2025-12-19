package com.contextable.a2ui4k.example.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Data class representing a widget in the catalog.
 */
data class WidgetInfo(
    val name: String,
    val category: String,
    val description: String
)

/**
 * All available widgets organized by category.
 */
val allWidgets = listOf(
    // Layout widgets
    WidgetInfo("Column", "Layout", "Vertical layout container"),
    WidgetInfo("Row", "Layout", "Horizontal layout container"),
    WidgetInfo("List", "Layout", "Scrollable list with templates"),
    WidgetInfo("Card", "Layout", "Material card container"),
    WidgetInfo("Tabs", "Layout", "Tabbed navigation container"),
    WidgetInfo("Modal", "Layout", "Dialog overlay"),

    // Display widgets
    WidgetInfo("Text", "Display", "Text with markdown support"),
    WidgetInfo("Image", "Display", "Remote image display"),
    WidgetInfo("Icon", "Display", "Material Design icon"),
    WidgetInfo("Divider", "Display", "Horizontal/vertical line"),
    WidgetInfo("Video", "Display", "Video player"),
    WidgetInfo("AudioPlayer", "Display", "Audio player"),

    // Input widgets
    WidgetInfo("Button", "Input", "Clickable button with actions"),
    WidgetInfo("TextField", "Input", "Text input field"),
    WidgetInfo("CheckBox", "Input", "Boolean toggle"),
    WidgetInfo("Slider", "Input", "Numeric range slider"),
    WidgetInfo("ChoicePicker", "Input", "Selection dropdown"),
    WidgetInfo("DateTimeInput", "Input", "Date/time picker")
)

/**
 * Grid page showing all available widgets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetListPage(onWidgetSelected: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("A2UI Widget Catalog") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(allWidgets) { widget ->
                WidgetCard(
                    widget = widget,
                    onClick = { onWidgetSelected(widget.name) }
                )
            }
        }
    }
}

@Composable
private fun WidgetCard(
    widget: WidgetInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = widget.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = widget.category,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = widget.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
