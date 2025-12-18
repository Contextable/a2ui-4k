package com.contextable.a2ui4k.catalog.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
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
import com.contextable.a2ui4k.model.DataReferenceParser
import com.contextable.a2ui4k.model.EventDispatcher
import com.contextable.a2ui4k.model.LiteralString
import com.contextable.a2ui4k.model.PathString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * ChoicePicker widget for selecting one or more options.
 *
 * JSON Schema (v0.9):
 * ```json
 * {
 *   "component": "ChoicePicker",
 *   "properties": {
 *     "label": "Select options",
 *     "usageHint": "multipleSelection" | "mutuallyExclusive",
 *     "options": [
 *       {"label": "Option 1", "value": "opt1"},
 *       {"label": "Option 2", "value": "opt2"}
 *     ],
 *     "value": {"path": "/form/selections"}
 *   }
 * }
 * ```
 *
 * Note: In v0.8, this component is called "MultipleChoice" with different properties:
 * - "selections" instead of "value"
 * - "maxAllowedSelections" instead of "usageHint"
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

/**
 * MultipleChoice widget (v0.8 name for ChoicePicker).
 */
val MultipleChoiceWidget = CatalogItem(
    name = "MultipleChoice"
) { componentId, data, buildChild, dataContext, onEvent ->
    ChoicePickerWidgetContent(
        componentId = componentId,
        data = data,
        dataContext = dataContext,
        onEvent = onEvent
    )
}

@Composable
private fun ChoicePickerWidgetContent(
    componentId: String,
    data: JsonObject,
    dataContext: DataContext,
    onEvent: EventDispatcher
) {
    val labelRef = DataReferenceParser.parseString(data["label"])
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

    // v0.9: usageHint, v0.8: maxAllowedSelections
    val usageHint = data["usageHint"]?.jsonPrimitive?.contentOrNull
    val maxAllowed = data["maxAllowedSelections"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()
    val isMultiSelect = usageHint == "multipleSelection" || (maxAllowed != null && maxAllowed != 1)

    // Parse options
    val optionsArray = data["options"]?.jsonArray ?: JsonArray(emptyList())
    val options = optionsArray.mapNotNull { optElement ->
        val optObj = optElement.jsonObject
        val optLabel = optObj["label"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
        val optValue = optObj["value"]?.jsonPrimitive?.contentOrNull ?: optLabel
        optLabel to optValue
    }

    // v0.9: value, v0.8: selections
    // Value can be a path to a string array or a JSON array of strings
    val valueElement = data["value"] ?: data["selections"]
    val valuePath = valueElement?.let { elem ->
        when (elem) {
            is JsonObject -> elem["path"]?.jsonPrimitive?.contentOrNull
            else -> null
        }
    }
    val initialValue = when {
        valuePath != null -> dataContext.getStringList(valuePath) ?: emptyList()
        valueElement is JsonArray -> valueElement.mapNotNull { it.jsonPrimitive.contentOrNull }
        else -> emptyList()
    }

    var selectedValues by remember(initialValue) { mutableStateOf(initialValue.toSet()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        options.forEach { (optLabel, optValue) ->
            val isSelected = optValue in selectedValues

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedValues = if (isMultiSelect) {
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
                if (isMultiSelect) {
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
