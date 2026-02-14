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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.contextable.a2ui4k.model.CatalogItem
import com.contextable.a2ui4k.model.DataChangeEvent
import com.contextable.a2ui4k.model.DataContext
import com.contextable.a2ui4k.model.EventDispatcher
import com.contextable.a2ui4k.render.LocalUiDefinition
import com.contextable.a2ui4k.util.PropertyValidation
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * ChoicePicker widget for selecting one or more options.
 *
 * A2UI Protocol Properties (v0.9):
 * - value (required): Path for data binding to selected values (DynamicStringList)
 * - options (required): Array of {label, value} objects
 * - variant (optional): "multipleSelection" | "mutuallyExclusive"
 * - displayStyle (optional): "checkbox" | "chips"
 * - filterable (optional): Boolean indicating whether options can be filtered
 *
 * JSON Schema:
 * ```json
 * {
 *   "component": "ChoicePicker",
 *   "value": {"path": "/form/selections"},
 *   "options": [
 *     {"label": "Option 1", "value": "opt1"},
 *     {"label": "Option 2", "value": "opt2"}
 *   ],
 *   "variant": "multipleSelection",
 *   "displayStyle": "checkbox",
 *   "filterable": true
 * }
 * ```
 */
val ChoicePickerWidget = CatalogItem(
    name = "ChoicePicker"
) { componentId, data, buildChild, dataContext, onEvent ->
    ChoicePickerWidgetContent(
        componentId = componentId,
        data = data,
        dataContext = dataContext,
        onEvent = onEvent
    )
}

private val EXPECTED_PROPERTIES = setOf("value", "options", "variant", "displayStyle", "filterable")

@Composable
private fun ChoicePickerWidgetContent(
    componentId: String,
    data: JsonObject,
    dataContext: DataContext,
    onEvent: EventDispatcher
) {
    PropertyValidation.warnUnexpectedProperties("ChoicePicker", data, EXPECTED_PROPERTIES)

    // Get surfaceId from UiDefinition
    val uiDefinition = LocalUiDefinition.current
    val surfaceId = uiDefinition?.surfaceId ?: "default"

    val variant = data["variant"]?.jsonPrimitive?.contentOrNull
    val multipleSelection = variant != "mutuallyExclusive"

    val options = (data["options"]?.jsonArray ?: JsonArray(emptyList())).mapNotNull { optElement ->
        val optObj = optElement.jsonObject
        val optLabel = optObj["label"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
        val optValue = optObj["value"]?.jsonPrimitive?.contentOrNull ?: optLabel
        optLabel to optValue
    }

    val valueElement = data["value"]
    val valuePath = (valueElement as? JsonObject)?.get("path")?.jsonPrimitive?.contentOrNull
    val selections = when {
        valuePath != null -> dataContext.getStringList(valuePath) ?: emptyList()
        valueElement is JsonArray -> valueElement.mapNotNull { it.jsonPrimitive.contentOrNull }
        else -> emptyList()
    }

    var selectedValues by remember(selections) { mutableStateOf(selections.toSet()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        options.forEach { (optLabel, optValue) ->
            val isSelected = optValue in selectedValues

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedValues = if (multipleSelection) {
                            if (isSelected) selectedValues - optValue else selectedValues + optValue
                        } else {
                            setOf(optValue)
                        }

                        if (valuePath != null) {
                            dataContext.update(valuePath, selectedValues.toList())
                            onEvent(
                                DataChangeEvent(
                                    surfaceId = surfaceId,
                                    path = valuePath,
                                    value = selectedValues.joinToString(",")
                                )
                            )
                        }
                    }
                    .padding(vertical = 4.dp)
            ) {
                if (multipleSelection) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null // Handled by row click
                    )
                } else {
                    RadioButton(
                        selected = isSelected,
                        onClick = null // Handled by row click
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = optLabel)
            }
        }
    }
}
