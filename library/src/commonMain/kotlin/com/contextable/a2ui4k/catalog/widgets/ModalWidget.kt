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

package com.contextable.a2ui4k.catalog.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.contextable.a2ui4k.model.CatalogItem
import com.contextable.a2ui4k.model.ChildBuilder
import com.contextable.a2ui4k.model.DataContext
import com.contextable.a2ui4k.model.DataReferenceParser
import com.contextable.a2ui4k.util.PropertyValidation
import kotlinx.serialization.json.JsonObject

/**
 * Modal widget for displaying dialog overlays.
 *
 * A2UI Protocol Properties (v0.8):
 * - entryPointChild (required): Component ID of the trigger element (e.g., a Button)
 * - contentChild (required): Component ID of the content to display in the modal
 *
 * The modal opens when the entryPointChild is clicked.
 *
 * JSON Schema:
 * ```json
 * {
 *   "component": "Modal",
 *   "entryPointChild": "open-modal-button",
 *   "contentChild": "modal-content"
 * }
 * ```
 */
val ModalWidget = CatalogItem(
    name = "Modal"
) { componentId, data, buildChild, dataContext, onEvent ->
    ModalWidgetContent(
        componentId = componentId,
        data = data,
        buildChild = buildChild,
        dataContext = dataContext
    )
}

private val EXPECTED_PROPERTIES = setOf("entryPointChild", "contentChild")

@Composable
private fun ModalWidgetContent(
    componentId: String,
    data: JsonObject,
    buildChild: ChildBuilder,
    dataContext: DataContext
) {
    PropertyValidation.warnUnexpectedProperties("Modal", data, EXPECTED_PROPERTIES)

    val entryPointRef = DataReferenceParser.parseComponentRef(data["entryPointChild"])
    val contentRef = DataReferenceParser.parseComponentRef(data["contentChild"])

    val entryPointChildId = entryPointRef?.componentId
    val contentChildId = contentRef?.componentId

    var showDialog by remember { mutableStateOf(false) }

    // Render the entry point (trigger element)
    if (entryPointChildId != null) {
        Box(
            modifier = Modifier.clickable { showDialog = true }
        ) {
            buildChild(entryPointChildId)
        }
    }

    // Show the modal when triggered
    if (showDialog && contentChildId != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    buildChild(contentChildId)
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
