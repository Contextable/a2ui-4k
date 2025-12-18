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
 * MultipleChoice widget for selecting one or more options.
 *
 * A2UI Protocol MultipleChoice properties:
 * - `label`: Display label
 * - `maxAllowedSelections`: Number of selections allowed (1 = single, >1 = multiple)
 * - `options`: Array of {label, value} objects
 * - `selections`: Path for data binding to selected values
 *
 * See A2UI protocol: standard_catalog_definition.json - MultipleChoice component
 *
 * JSON Schema (v0.8):
 * ```json
 * {
 *   "id": "choice_1",
 *   "component": {
 *     "MultipleChoice": {
 *       "label": {"literalString": "Select options"},
 *       "maxAllowedSelections": 3,
 *       "options": [
 *         {"label": "Option 1", "value": "opt1"},
 *         {"label": "Option 2", "value": "opt2"}
 *       ],
 *       "selections": {"path": "/form/selections"}
 *     }
 *   }
 * }
 * ```
 */
val MultipleChoiceWidget = CatalogItem(
    name = "MultipleChoice"
) { componentId, data, buildChild, dataContext, onEvent ->
    MultipleChoiceWidgetContent(
        componentId = componentId,
        data = data,
        dataContext = dataContext,
        onEvent = onEvent
    )
}

@Composable
private fun MultipleChoiceWidgetContent(
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

    val maxAllowedSelections = data["maxAllowedSelections"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()
    val multipleSelection = maxAllowedSelections != 1

    val options = (data["options"]?.jsonArray ?: JsonArray(emptyList())).mapNotNull { optElement ->
        val optObj = optElement.jsonObject
        val optLabel = optObj["label"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
        val optValue = optObj["value"]?.jsonPrimitive?.contentOrNull ?: optLabel
        optLabel to optValue
    }

    val selectionsElement = data["selections"]
    val selectionsPath = (selectionsElement as? JsonObject)?.get("path")?.jsonPrimitive?.contentOrNull
    val selections = when {
        selectionsPath != null -> dataContext.getStringList(selectionsPath) ?: emptyList()
        selectionsElement is JsonArray -> selectionsElement.mapNotNull { it.jsonPrimitive.contentOrNull }
        else -> emptyList()
    }

    var selectedValues by remember(selections) { mutableStateOf(selections.toSet()) }

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
                        selectedValues = if (multipleSelection) {
                            if (isSelected) selectedValues - optValue else selectedValues + optValue
                        } else {
                            setOf(optValue)
                        }

                        if (selectionsPath != null) {
                            dataContext.update(selectionsPath, selectedValues.toList())
                            onEvent(
                                DataChangeEvent(
                                    surfaceId = surfaceId,
                                    path = selectionsPath,
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
