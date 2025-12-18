package com.contextable.a2ui4k.catalog.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.contextable.a2ui4k.model.CatalogItem
import com.contextable.a2ui4k.model.DataChangeEvent
import com.contextable.a2ui4k.model.DataContext
import com.contextable.a2ui4k.model.DataReferenceParser
import com.contextable.a2ui4k.model.EventDispatcher
import com.contextable.a2ui4k.model.LiteralString
import com.contextable.a2ui4k.model.PathString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * DateTimeInput widget for date and/or time input.
 *
 * JSON Schema (v0.9):
 * ```json
 * {
 *   "component": "DateTimeInput",
 *   "properties": {
 *     "label": "Select date",
 *     "value": {"path": "/form/date"} | "2025-01-15T10:30:00Z",
 *     "enableDate": true,
 *     "enableTime": true,
 *     "outputFormat": "yyyy-MM-dd'T'HH:mm:ss'Z'"
 *   }
 * }
 * ```
 *
 * Note: This is a simplified implementation using a text field.
 * A production implementation would use platform-specific date/time pickers.
 */
val DateTimeInputWidget = CatalogItem(
    name = "DateTimeInput"
) { componentId, data, buildChild, dataContext, onEvent ->
    DateTimeInputWidgetContent(
        componentId = componentId,
        data = data,
        dataContext = dataContext,
        onEvent = onEvent
    )
}

@Composable
private fun DateTimeInputWidgetContent(
    componentId: String,
    data: JsonObject,
    dataContext: DataContext,
    onEvent: EventDispatcher
) {
    val labelRef = DataReferenceParser.parseString(data["label"])
    val valueRef = DataReferenceParser.parseString(data["value"])
    val surfaceId = DataReferenceParser.parseString(data["surfaceId"])?.let {
        when (it) {
            is LiteralString -> it.value
            is PathString -> dataContext.getString(it.path)
            else -> null
        }
    } ?: ""

    val enableDate = data["enableDate"]?.jsonPrimitive?.booleanOrNull ?: true
    val enableTime = data["enableTime"]?.jsonPrimitive?.booleanOrNull ?: false

    val label = when (labelRef) {
        is LiteralString -> labelRef.value
        is PathString -> dataContext.getString(labelRef.path) ?: ""
        else -> ""
    }

    val initialValue = when (valueRef) {
        is PathString -> dataContext.getString(valueRef.path) ?: ""
        is LiteralString -> valueRef.value
        else -> ""
    }

    var dateTimeValue by remember(initialValue) { mutableStateOf(initialValue) }

    // Determine placeholder based on what's enabled
    val placeholder = when {
        enableDate && enableTime -> "YYYY-MM-DD HH:MM"
        enableDate -> "YYYY-MM-DD"
        enableTime -> "HH:MM"
        else -> ""
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = dateTimeValue,
            onValueChange = { newValue ->
                dateTimeValue = newValue
                if (valueRef is PathString) {
                    dataContext.update(valueRef.path, newValue)
                    onEvent(
                        DataChangeEvent(
                            surfaceId = surfaceId,
                            path = valueRef.path,
                            value = newValue
                        )
                    )
                }
            },
            label = if (label.isNotEmpty()) {
                { Text(label) }
            } else null,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}
