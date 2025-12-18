package com.contextable.a2ui4k.catalog.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
import com.contextable.a2ui4k.model.LiteralString
import com.contextable.a2ui4k.model.PathNumber
import com.contextable.a2ui4k.model.PathString
import kotlinx.serialization.json.JsonObject

/**
 * Slider widget for numeric range input.
 *
 * JSON Schema (v0.9):
 * ```json
 * {
 *   "component": "Slider",
 *   "properties": {
 *     "label": "Volume" | {"path": "/labels/volume"},
 *     "min": 0,
 *     "max": 100,
 *     "value": {"path": "/settings/volume"} | 50
 *   }
 * }
 * ```
 *
 * Note: v0.8 uses "minValue" and "maxValue" instead of "min" and "max"
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

@Composable
private fun SliderWidgetContent(
    componentId: String,
    data: JsonObject,
    dataContext: DataContext,
    onEvent: EventDispatcher
) {
    val labelRef = DataReferenceParser.parseString(data["label"])
    val valueRef = DataReferenceParser.parseNumber(data["value"])

    // v0.9 uses "min"/"max", v0.8 uses "minValue"/"maxValue"
    val minRef = DataReferenceParser.parseNumber(data["min"] ?: data["minValue"])
    val maxRef = DataReferenceParser.parseNumber(data["max"] ?: data["maxValue"])

    val surfaceId = DataReferenceParser.parseString(data["surfaceId"])?.let {
        when (it) {
            is LiteralString -> it.value
            is PathString -> dataContext.getString(it.path)
            else -> null
        }
    } ?: ""

    val label = when (labelRef) {
        is LiteralString -> labelRef.value
        is PathString -> dataContext.getString(labelRef.path) ?: ""
        else -> ""
    }

    val minValue = when (minRef) {
        is LiteralNumber -> minRef.value.toFloat()
        is PathNumber -> dataContext.getNumber(minRef.path)?.toFloat() ?: 0f
        else -> 0f
    }

    val maxValue = when (maxRef) {
        is LiteralNumber -> maxRef.value.toFloat()
        is PathNumber -> dataContext.getNumber(maxRef.path)?.toFloat() ?: 100f
        else -> 100f
    }

    val initialValue = when (valueRef) {
        is PathNumber -> dataContext.getNumber(valueRef.path)?.toFloat() ?: minValue
        is LiteralNumber -> valueRef.value.toFloat()
        else -> minValue
    }

    var sliderValue by remember(initialValue) { mutableFloatStateOf(initialValue) }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(text = "$label: ${sliderValue.toInt()}")
        }
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
}
