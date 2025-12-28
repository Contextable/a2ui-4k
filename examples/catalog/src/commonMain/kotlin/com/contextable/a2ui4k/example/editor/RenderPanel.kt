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

package com.contextable.a2ui4k.example.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.contextable.a2ui4k.catalog.CoreCatalog
import com.contextable.a2ui4k.data.rememberDataModel
import com.contextable.a2ui4k.render.A2UISurface

/**
 * Panel for displaying the rendered A2UI surface or error/empty states.
 */
@Composable
fun RenderPanel(
    parseResult: ParseResult,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxSize()
    ) {
        when (parseResult) {
            is ParseResult.Success -> {
                // Create DataModel with initial data for path bindings
                val dataModel = rememberDataModel(parseResult.initialData)

                A2UISurface(
                    definition = parseResult.definition,
                    dataModel = dataModel,
                    catalog = CoreCatalog,
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    onEvent = { event ->
                        println("A2UI Event: $event")
                    }
                )
            }
            is ParseResult.Error -> {
                PlaceholderContent(
                    icon = Icons.Default.Error,
                    message = "Fix JSON errors to see preview"
                )
            }
            is ParseResult.Empty -> {
                PlaceholderContent(
                    icon = Icons.Default.Preview,
                    message = "Enter JSON to see preview"
                )
            }
        }
    }
}

@Composable
private fun PlaceholderContent(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
