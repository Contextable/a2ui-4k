package com.contextable.a2ui4k.example.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.contextable.a2ui4k.example.editor.EditorState
import com.contextable.a2ui4k.example.editor.JsonEditorPanel
import com.contextable.a2ui4k.example.editor.RenderPanel
import com.contextable.a2ui4k.example.editor.rememberEditorState
import com.contextable.a2ui4k.example.widgets.WidgetSamples

/**
 * Editor page for a specific widget with adaptive layout.
 *
 * - Compact (< 840dp): Preview on top, JSON editor at bottom
 * - Expanded (>= 840dp): Side-by-side (JSON left, preview right)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetEditorPage(
    widgetName: String,
    onBack: () -> Unit
) {
    val state = rememberEditorState(initialJson = WidgetSamples.getJson(widgetName))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(widgetName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
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
            if (maxWidth < 840.dp) {
                CompactEditorLayout(state)
            } else {
                ExpandedEditorLayout(state)
            }
        }
    }
}

/**
 * Compact layout: Preview on top, JSON editor at bottom (2-3 lines minimum).
 */
@Composable
private fun CompactEditorLayout(state: EditorState) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Preview panel (takes most space)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            RenderPanel(parseResult = state.parseResult)
        }

        HorizontalDivider()

        // JSON editor (2-3 lines minimum height, can expand)
        JsonEditorPanel(
            json = state.jsonInput,
            onJsonChange = state::updateJson,
            error = state.errorMessage,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 240.dp)
        )
    }
}

/**
 * Expanded layout: JSON editor on left (40%), preview on right (60%).
 */
@Composable
private fun ExpandedEditorLayout(state: EditorState) {
    Row(modifier = Modifier.fillMaxSize()) {
        // JSON editor (40%)
        JsonEditorPanel(
            json = state.jsonInput,
            onJsonChange = state::updateJson,
            error = state.errorMessage,
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
        )

        VerticalDivider()

        // Preview panel (60%)
        Box(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            RenderPanel(parseResult = state.parseResult)
        }
    }
}
