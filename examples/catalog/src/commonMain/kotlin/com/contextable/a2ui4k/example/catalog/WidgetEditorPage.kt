/*
 * Copyright 2025 Contextable LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * Features two separate editors:
 * - Components: Array of component definitions
 * - Data: JSON object for data binding
 *
 * Layout:
 * - Compact (< 840dp): Preview on top, editors stacked at bottom
 * - Expanded (>= 840dp): Editors on left, preview on right
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetEditorPage(
    widgetName: String,
    onBack: () -> Unit
) {
    val sample = WidgetSamples.getSample(widgetName)
    val state = rememberEditorState(
        initialComponents = sample.components,
        initialData = sample.data
    )

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
 * Compact layout: Preview on top, editors stacked at bottom.
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

        // Components editor
        JsonEditorPanel(
            label = "Components",
            value = state.componentsJson,
            onValueChange = state::updateComponents,
            error = state.errorMessage,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp, max = 180.dp)
        )

        HorizontalDivider()

        // Data editor
        JsonEditorPanel(
            label = "Data",
            value = state.dataJson,
            onValueChange = state::updateData,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp, max = 100.dp)
        )
    }
}

/**
 * Expanded layout: Editors stacked on left (40%), preview on right (60%).
 */
@Composable
private fun ExpandedEditorLayout(state: EditorState) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left side: Two stacked editors
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
        ) {
            // Components editor (larger, top)
            JsonEditorPanel(
                label = "Components",
                value = state.componentsJson,
                onValueChange = state::updateComponents,
                error = state.errorMessage,
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxWidth()
            )

            HorizontalDivider()

            // Data editor (smaller, bottom)
            JsonEditorPanel(
                label = "Data",
                value = state.dataJson,
                onValueChange = state::updateData,
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxWidth()
            )
        }

        VerticalDivider()

        // Right side: Preview panel
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
