package com.contextable.a2ui4k.catalog.widgets

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
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
import com.contextable.a2ui4k.model.LiteralBoolean
import com.contextable.a2ui4k.model.LiteralString
import com.contextable.a2ui4k.model.PathBoolean
import com.contextable.a2ui4k.model.PathString
import com.contextable.a2ui4k.render.LocalUiDefinition
import com.contextable.a2ui4k.util.PropertyValidation
import kotlinx.serialization.json.JsonObject

/**
 * CheckBox widget for boolean input.
 *
 * A2UI Protocol Properties (v0.8):
 * - label (required): Display label for the checkbox
 * - value (optional): Boolean value, supports path binding for two-way data binding
 *
 * JSON Schema:
 * ```json
 * {
 *   "label": {"literalString": "Accept terms"},
 *   "value": {"path": "/form/accepted"}
 * }
 * ```
 */
val CheckBoxWidget = CatalogItem(
    name = "CheckBox"
) { componentId, data, buildChild, dataContext, onEvent ->
    CheckBoxWidgetContent(
        componentId = componentId,
        data = data,
        dataContext = dataContext,
        onEvent = onEvent
    )
}

private val EXPECTED_PROPERTIES = setOf("label", "value")

@Composable
private fun CheckBoxWidgetContent(
    componentId: String,
    data: JsonObject,
    dataContext: DataContext,
    onEvent: EventDispatcher
) {
    PropertyValidation.warnUnexpectedProperties("CheckBox", data, EXPECTED_PROPERTIES)

    val labelRef = DataReferenceParser.parseString(data["label"])
    val valueRef = DataReferenceParser.parseBoolean(data["value"])

    // Get surfaceId from UiDefinition
    val uiDefinition = LocalUiDefinition.current
    val surfaceId = uiDefinition?.surfaceId ?: "default"

    val label = when (labelRef) {
        is LiteralString -> labelRef.value
        is PathString -> dataContext.getString(labelRef.path) ?: ""
        else -> ""
    }

    val initialValue = when (valueRef) {
        is PathBoolean -> dataContext.getBoolean(valueRef.path) ?: false
        is LiteralBoolean -> valueRef.value
        else -> false
    }

    var checked by remember(initialValue) { mutableStateOf(initialValue) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = { newValue ->
                checked = newValue
                if (valueRef is PathBoolean) {
                    dataContext.update(valueRef.path, newValue)
                    onEvent(
                        DataChangeEvent(
                            surfaceId = surfaceId,
                            path = valueRef.path,
                            value = newValue.toString()
                        )
                    )
                }
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}
