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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.contextable.a2ui4k.model.CatalogItem
import com.contextable.a2ui4k.model.DataChangeEvent
import com.contextable.a2ui4k.model.DataContext
import com.contextable.a2ui4k.model.DataReferenceParser
import com.contextable.a2ui4k.model.EventDispatcher
import com.contextable.a2ui4k.model.LiteralNumber
import com.contextable.a2ui4k.model.PathNumber
import com.contextable.a2ui4k.render.LocalUiDefinition
import com.contextable.a2ui4k.util.PropertyValidation
import kotlinx.serialization.json.JsonObject

/**
 * Slider widget for numeric range input.
 *
 * A2UI Protocol Properties (v0.8):
 * - value (required): Current value, supports path binding for two-way data binding
 * - minValue (optional): Minimum value (default 0)
 * - maxValue (optional): Maximum value (default 100)
 *
 * JSON Schema:
 * ```json
 * {
 *   "value": {"path": "/settings/volume"},
 *   "minValue": {"literalNumber": 0},
 *   "maxValue": {"literalNumber": 100}
 * }
 * ```
 */
val SliderWidget = CatalogItem(
    name = "Slider"
) { componentId, data, buildChild, dataContext, onEvent ->
    SliderWidgetContent(
        componentId = componentId,
        data = data,
        dataContext = dataContext,
        onEvent = onEvent
    )
}

private val EXPECTED_PROPERTIES = setOf("value", "minValue", "maxValue")

@Composable
private fun SliderWidgetContent(
    componentId: String,
    data: JsonObject,
    dataContext: DataContext,
    onEvent: EventDispatcher
) {
    PropertyValidation.warnUnexpectedProperties("Slider", data, EXPECTED_PROPERTIES)

    val valueRef = DataReferenceParser.parseNumber(data["value"])
    val minValueRef = DataReferenceParser.parseNumber(data["minValue"])
    val maxValueRef = DataReferenceParser.parseNumber(data["maxValue"])

    // Get surfaceId from UiDefinition
    val uiDefinition = LocalUiDefinition.current
    val surfaceId = uiDefinition?.surfaceId ?: "default"

    val minValue = when (minValueRef) {
        is LiteralNumber -> minValueRef.value.toFloat()
        is PathNumber -> dataContext.getNumber(minValueRef.path)?.toFloat() ?: 0f
        else -> 0f
    }

    val maxValue = when (maxValueRef) {
        is LiteralNumber -> maxValueRef.value.toFloat()
        is PathNumber -> dataContext.getNumber(maxValueRef.path)?.toFloat() ?: 100f
        else -> 100f
    }

    val initialValue = when (valueRef) {
        is PathNumber -> dataContext.getNumber(valueRef.path)?.toFloat() ?: minValue
        is LiteralNumber -> valueRef.value.toFloat()
        else -> minValue
    }

    var sliderValue by remember(initialValue) { mutableFloatStateOf(initialValue) }

    Slider(
        value = sliderValue,
        onValueChange = { newValue ->
            sliderValue = newValue
        },
        onValueChangeFinished = {
            if (valueRef is PathNumber) {
                dataContext.update(valueRef.path, sliderValue.toDouble())
                onEvent(
                    DataChangeEvent(
                        surfaceId = surfaceId,
                        path = valueRef.path,
                        value = sliderValue.toString()
                    )
                )
            }
        },
        valueRange = minValue..maxValue,
        modifier = Modifier.fillMaxWidth()
    )
}
