package com.contextable.a2ui4k.example.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Interests
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Data class representing a widget in the catalog.
 */
data class WidgetInfo(
    val name: String,
    val category: String,
    val description: String,
    val icon: ImageVector
)

/**
 * All available widgets organized by category.
 */
val allWidgets = listOf(
    // Custom - empty editor for user experimentation
    WidgetInfo("Custom", "Custom", "Start from scratch", Icons.Default.Edit),

    // Layout widgets
    WidgetInfo("Column", "Layout", "Vertical layout container", Icons.Default.ViewColumn),
    WidgetInfo("Row", "Layout", "Horizontal layout container", Icons.Default.TableRows),
    WidgetInfo("List", "Layout", "Scrollable list with templates", Icons.Default.ViewList),
    WidgetInfo("Card", "Layout", "Material card container", Icons.Default.CreditCard),
    WidgetInfo("Tabs", "Layout", "Tabbed navigation container", Icons.Default.Tab),
    WidgetInfo("Modal", "Layout", "Dialog overlay", Icons.Default.OpenInNew),

    // Display widgets
    WidgetInfo("Text", "Display", "Text with markdown support", Icons.Default.TextFields),
    WidgetInfo("Image", "Display", "Remote image display", Icons.Default.Image),
    WidgetInfo("Icon", "Display", "Material Design icon", Icons.Default.Interests),
    WidgetInfo("Divider", "Display", "Horizontal/vertical line", Icons.Default.HorizontalRule),
    WidgetInfo("Video", "Display", "Video player", Icons.Default.VideoLibrary),
    WidgetInfo("AudioPlayer", "Display", "Audio player", Icons.Default.Headphones),

    // Input widgets
    WidgetInfo("Button", "Input", "Clickable button with actions", Icons.Default.TouchApp),
    WidgetInfo("TextField", "Input", "Text input field", Icons.Default.ShortText),
    WidgetInfo("CheckBox", "Input", "Boolean toggle", Icons.Default.CheckBox),
    WidgetInfo("Slider", "Input", "Numeric range slider", Icons.Default.Tune),
    WidgetInfo("DateTimeInput", "Input", "Date/time picker", Icons.Default.CalendarToday)
)

/**
 * Grid page showing all available widgets.
 * Uses Row with IntrinsicSize.Min to ensure equal card heights per row.
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val minCardWidth = 160.dp
            val spacing = 12.dp
            val padding = 16.dp
            val availableWidth = maxWidth - (padding * 2)
            val columns = maxOf(1, ((availableWidth + spacing) / (minCardWidth + spacing)).toInt())

            val rows = allWidgets.chunked(columns)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                rows.forEach { rowWidgets ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        rowWidgets.forEach { widget ->
                            WidgetCard(
                                widget = widget,
                                onClick = { onWidgetSelected(widget.name) },
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                        // Fill remaining space if row is not full
                        repeat(columns - rowWidgets.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetCard(
    widget: WidgetInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = widget.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
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
