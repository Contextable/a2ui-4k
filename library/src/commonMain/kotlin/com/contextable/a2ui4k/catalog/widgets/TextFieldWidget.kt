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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.contextable.a2ui4k.model.CatalogItem
import com.contextable.a2ui4k.model.DataChangeEvent
import com.contextable.a2ui4k.model.DataContext
import com.contextable.a2ui4k.model.DataReferenceParser
import com.contextable.a2ui4k.model.EventDispatcher
import com.contextable.a2ui4k.model.LiteralString
import com.contextable.a2ui4k.model.PathString
import com.contextable.a2ui4k.render.LocalUiDefinition
import com.contextable.a2ui4k.util.PropertyValidation
import kotlinx.serialization.json.JsonObject

/**
 * TextField widget for user text input.
 *
 * A2UI Protocol Properties (v0.9):
 * - label (required): Display label for the text field
 * - value (optional): Current text value, supports path binding for two-way data binding
 * - variant (optional): longText, number, shortText, obscured
 * - validationRegexp (optional): Regex pattern for input validation
 *
 * JSON Schema (v0.9):
 * ```json
 * {
 *   "label": "Name" | {"path": "/labels/name"},
 *   "value": {"path": "/form/name"} | "initial value",
 *   "variant": "shortText",
 *   "validationRegexp": "^[a-zA-Z]+$"
 * }
 * ```
 */
val TextFieldWidget = CatalogItem(
    name = "TextField"
) { componentId, data, buildChild, dataContext, onEvent ->
    TextFieldWidgetContent(
        componentId = componentId,
        data = data,
        dataContext = dataContext,
        onEvent = onEvent
    )
}

private val EXPECTED_PROPERTIES = setOf("label", "value", "variant", "validationRegexp")

@Composable
private fun TextFieldWidgetContent(
    componentId: String,
    data: JsonObject,
    dataContext: DataContext,
    onEvent: EventDispatcher
) {
    PropertyValidation.warnUnexpectedProperties("TextField", data, EXPECTED_PROPERTIES)

    val labelRef = DataReferenceParser.parseString(data["label"])
    val valueRef = DataReferenceParser.parseString(data["value"])
    val variantRef = DataReferenceParser.parseString(data["variant"])
    val validationRegexpRef = DataReferenceParser.parseString(data["validationRegexp"])

    val label = when (labelRef) {
        is LiteralString -> labelRef.value
        is PathString -> dataContext.getString(labelRef.path) ?: ""
        else -> ""
    }

    val variant = when (variantRef) {
        is LiteralString -> variantRef.value
        is PathString -> dataContext.getString(variantRef.path)
        else -> null
    }

    val validationRegexp = when (validationRegexpRef) {
        is LiteralString -> validationRegexpRef.value
        is PathString -> dataContext.getString(validationRegexpRef.path)
        else -> null
    }

    // Get initial value from data context if bound
    val initialValue = when (valueRef) {
        is PathString -> dataContext.getString(valueRef.path) ?: ""
        is LiteralString -> valueRef.value
        else -> ""
    }

    // Get surfaceId from UiDefinition
    val uiDefinition = LocalUiDefinition.current
    val surfaceId = uiDefinition?.surfaceId ?: "default"

    var textValue by remember(initialValue) { mutableStateOf(initialValue) }
    var isError by remember { mutableStateOf(false) }

    // Determine keyboard type, visual transformation, and layout based on variant
    val keyboardType: KeyboardType
    val visualTransformation: VisualTransformation
    val singleLine: Boolean
    val modifier: Modifier

    when (variant?.lowercase()) {
        "number" -> {
            keyboardType = KeyboardType.Number
            visualTransformation = VisualTransformation.None
            singleLine = true
            modifier = Modifier.fillMaxWidth()
        }
        "obscured" -> {
            keyboardType = KeyboardType.Password
            visualTransformation = PasswordVisualTransformation()
            singleLine = true
            modifier = Modifier.fillMaxWidth()
        }
        "longtext" -> {
            keyboardType = KeyboardType.Text
            visualTransformation = VisualTransformation.None
            singleLine = false
            modifier = Modifier.fillMaxWidth().height(120.dp)
        }
        else -> { // shorttext and default
            keyboardType = KeyboardType.Text
            visualTransformation = VisualTransformation.None
            singleLine = true
            modifier = Modifier.fillMaxWidth()
        }
    }

    OutlinedTextField(
        value = textValue,
        onValueChange = { newValue ->
            textValue = newValue

            // Validate against regex if provided
            isError = if (validationRegexp != null && newValue.isNotEmpty()) {
                !Regex(validationRegexp).matches(newValue)
            } else {
                false
            }

            // Update data context and fire event if bound
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
        modifier = modifier,
        singleLine = singleLine,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation
    )
}
