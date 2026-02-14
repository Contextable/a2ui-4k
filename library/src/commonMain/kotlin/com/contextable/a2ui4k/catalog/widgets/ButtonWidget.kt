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

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.contextable.a2ui4k.model.CatalogItem
import com.contextable.a2ui4k.model.ChildBuilder
import com.contextable.a2ui4k.model.DataContext
import com.contextable.a2ui4k.model.DataReferenceParser
import com.contextable.a2ui4k.model.EventDispatcher
import com.contextable.a2ui4k.model.ActionEvent
import com.contextable.a2ui4k.model.LiteralString
import com.contextable.a2ui4k.model.PathString
import com.contextable.a2ui4k.render.LocalUiDefinition
import com.contextable.a2ui4k.util.PropertyValidation
import kotlin.time.Clock
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Button widget for user actions.
 *
 * A2UI Protocol v0.9 Button properties:
 * - `child`: Component ID reference (e.g., to a Text widget)
 * - `label`: Fallback text label (backwards compatibility)
 * - `variant`: "filled", "outlined", "text", "elevated", "tonal" (default: filled)
 * - `action`: Action object with `event` or `functionCall`
 *
 * See A2UI protocol: standard_catalog_definition.json - Button component
 *
 * JSON Schema (v0.9):
 * ```json
 * {
 *   "component": "Button",
 *   "child": "button-text-id",
 *   "action": {"event": {"name": "submit", "context": {"key": "value"}}},
 *   "variant": "filled"
 * }
 * ```
 */
val ButtonWidget = CatalogItem(
    name = "Button"
) { componentId, data, buildChild, dataContext, onEvent ->
    ButtonWidgetContent(
        componentId = componentId,
        data = data,
        buildChild = buildChild,
        dataContext = dataContext,
        onEvent = onEvent
    )
}

private val EXPECTED_PROPERTIES = setOf("child", "action", "label", "variant")

@Composable
private fun ButtonWidgetContent(
    componentId: String,
    data: JsonObject,
    buildChild: ChildBuilder,
    dataContext: DataContext,
    onEvent: EventDispatcher
) {
    PropertyValidation.warnUnexpectedProperties("Button", data, EXPECTED_PROPERTIES)

    // Child component reference (A2UI Button.child property)
    val childRef = DataReferenceParser.parseString(data["child"])
    val childId = when (childRef) {
        is LiteralString -> childRef.value
        is PathString -> dataContext.getString(childRef.path)
        else -> null
    }

    // Fallback to label for backwards compatibility
    val labelRef = DataReferenceParser.parseString(data["label"])
    val label = when (labelRef) {
        is LiteralString -> labelRef.value
        is PathString -> dataContext.getString(labelRef.path)
        else -> null
    }

    // Button variant: "primary", "borderless", or default
    val variantRef = DataReferenceParser.parseString(data["variant"])
    val variant = when (variantRef) {
        is LiteralString -> variantRef.value
        is PathString -> dataContext.getString(variantRef.path)
        else -> null
    }

    // v0.9 action format: {"event": {"name": "submit", "context": {...}}} or {"functionCall": {...}}
    val actionElement = data["action"]
    val actionData = when {
        actionElement is JsonObject -> actionElement
        else -> null
    }
    // v0.9: action.event contains name and context
    val eventData = actionData?.get("event")?.let { it as? JsonObject }
    val actionNameDirect = when {
        actionElement is JsonPrimitive -> actionElement.contentOrNull
        else -> null
    }

    // Get surfaceId from UiDefinition (not from component data)
    val uiDefinition = LocalUiDefinition.current
    val surfaceId = uiDefinition?.surfaceId ?: "default"

    // Get template item key for sourceComponentId suffix
    val templateItemKey = LocalTemplateItemKey.current

    val onClick: () -> Unit = {
        val actionName = actionNameDirect
            ?: eventData?.get("name")?.jsonPrimitive?.content
            ?: "click"

        // Resolve action.event.context (v0.9: flat JSON object with dynamic values)
        val contextObject = eventData?.get("context")?.let { it as? JsonObject }
        val resolvedContext = resolveContext(contextObject, dataContext)

        // Build sourceComponentId with item suffix (e.g., "template-book-button:item1")
        val sourceComponentId = if (templateItemKey != null) {
            "$componentId:item$templateItemKey"
        } else {
            componentId
        }

        // Generate ISO8601 timestamp
        val timestamp = getCurrentIso8601Timestamp()

        onEvent(
            ActionEvent(
                name = actionName,
                surfaceId = surfaceId,
                sourceComponentId = sourceComponentId,
                timestamp = timestamp,
                context = resolvedContext
            )
        )
    }

    // Button styling based on variant (v0.9 values)
    // "filled": colorScheme.primary background (default)
    // "outlined": surface background, primary content, outline border
    // "text": transparent background, primary content
    // "elevated": surface background, elevated
    // "tonal": secondaryContainer background
    val colors = when (variant) {
        "filled" -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
        "outlined" -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
        "text" -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        )
        "elevated" -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
        "tonal" -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        else -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    }

    Button(
        onClick = onClick,
        colors = colors
    ) {
        when {
            // Prefer child component reference (A2UI Button.child)
            childId != null -> buildChild(childId)
            // Fallback to label text
            label != null -> Text(label)
            // Default fallback
            else -> Text("Button")
        }
    }
}

/**
 * Resolves action context by evaluating path bindings against the DataContext.
 *
 * A2UI v0.9 Button.action.event.context is a flat JSON object where each value
 * is either a plain primitive or a `{"path": "..."}` binding that is resolved
 * at event time from the DataContext.
 *
 * @param contextObject The action.event.context JsonObject from the button definition
 * @param dataContext The current DataContext for resolving path bindings
 * @return JsonObject with resolved key-value pairs, or null if no context
 */
private fun resolveContext(contextObject: JsonObject?, dataContext: DataContext): JsonObject? {
    if (contextObject == null || contextObject.isEmpty()) return null

    val resolved = mutableMapOf<String, JsonElement>()

    for ((key, value) in contextObject) {
        val resolvedValue: JsonElement? = when {
            // Path binding object: {"path": "..."}
            value is JsonObject && value.containsKey("path") -> {
                val path = value["path"]?.jsonPrimitive?.content ?: ""
                dataContext.getString(path)?.let { JsonPrimitive(it) }
                    ?: dataContext.getNumber(path)?.let { JsonPrimitive(it) }
                    ?: dataContext.getBoolean(path)?.let { JsonPrimitive(it) }
            }
            // Plain primitive value (string, number, boolean)
            value is JsonPrimitive -> value
            else -> null
        }

        if (resolvedValue != null) {
            resolved[key] = resolvedValue
        }
    }

    return if (resolved.isNotEmpty()) JsonObject(resolved) else null
}

/**
 * Gets the current timestamp in ISO8601 format.
 * Example: "2025-12-17T02:00:23.936Z"
 */
@OptIn(kotlin.time.ExperimentalTime::class)
private fun getCurrentIso8601Timestamp(): String {
    val now = Clock.System.now()
    return now.toString() // Instant.toString() produces ISO8601 format
}
