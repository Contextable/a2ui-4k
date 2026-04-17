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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.contextable.a2ui4k.function.FunctionEvaluator
import com.contextable.a2ui4k.model.Accessibility
import com.contextable.a2ui4k.model.ActionEvent
import com.contextable.a2ui4k.model.CatalogItem
import com.contextable.a2ui4k.model.CheckRule
import com.contextable.a2ui4k.model.ChildBuilder
import com.contextable.a2ui4k.model.DataContext
import com.contextable.a2ui4k.model.DataReferenceParser
import com.contextable.a2ui4k.model.EventDispatcher
import com.contextable.a2ui4k.model.LiteralString
import com.contextable.a2ui4k.model.PathString
import com.contextable.a2ui4k.render.LocalUiDefinition
import com.contextable.a2ui4k.util.PropertyValidation
import kotlin.time.Clock
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/**
 * Button widget for user actions.
 *
 * A2UI v0.9 Button properties:
 * - `child`: ComponentId of the child (typically Text or Icon)
 * - `variant`: `"default"` | `"primary"` | `"borderless"`
 * - `action`: Action object — `{"event":{"name","context"}}` or `{"functionCall":{...}}`
 * - `checks`: Array of [CheckRule]s; when any check fails, the button is disabled
 * - `accessibility`: Optional accessibility metadata
 *
 * ```json
 * {
 *   "component": "Button",
 *   "child": "button-text",
 *   "variant": "primary",
 *   "action": {"event": {"name": "submit", "context": {"k": "v"}}}
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

private val EXPECTED_PROPERTIES = setOf("child", "variant", "action", "checks", "accessibility")

@Composable
private fun ButtonWidgetContent(
    componentId: String,
    data: JsonObject,
    buildChild: ChildBuilder,
    dataContext: DataContext,
    onEvent: EventDispatcher
) {
    PropertyValidation.warnUnexpectedProperties("Button", data, EXPECTED_PROPERTIES)

    val childRef = DataReferenceParser.parseString(data["child"])
    val childId = when (childRef) {
        is LiteralString -> childRef.value
        is PathString -> dataContext.getString(childRef.path)
        else -> null
    }

    val variantRef = DataReferenceParser.parseString(data["variant"])
    val variant = when (variantRef) {
        is LiteralString -> variantRef.value
        is PathString -> dataContext.getString(variantRef.path)
        else -> null
    }

    // Button.action variants:
    //   { "event": { "name": "...", "context": {...} } }
    //   { "functionCall": { "call": "...", "args": {...} } }
    val actionElement = data["action"] as? JsonObject
    val eventData = actionElement?.get("event") as? JsonObject
    val functionCall = actionElement?.get("functionCall") as? JsonObject

    val rules = CheckRule.fromJsonArray(data["checks"])
    val enabled = CheckRule.evaluateAll(rules, dataContext).isEmpty()

    val uiDefinition = LocalUiDefinition.current
    val surfaceId = uiDefinition?.surfaceId ?: "default"
    val templateItemKey = LocalTemplateItemKey.current

    val onClick: () -> Unit = handler@{
        if (functionCall != null) {
            val call = functionCall["call"]?.jsonPrimitive?.content ?: return@handler
            val args = functionCall["args"] as? JsonObject
            FunctionEvaluator.evaluate(call, args, dataContext)
            return@handler
        }

        val actionName = eventData?.get("name")?.jsonPrimitive?.content ?: "click"
        val contextObject = eventData?.get("context") as? JsonObject
        val resolvedContext = resolveContext(contextObject, dataContext)

        val sourceComponentId = if (templateItemKey != null) {
            "$componentId:item$templateItemKey"
        } else {
            componentId
        }

        onEvent(
            ActionEvent(
                name = actionName,
                surfaceId = surfaceId,
                sourceComponentId = sourceComponentId,
                timestamp = getCurrentIso8601Timestamp(),
                context = resolvedContext
            )
        )
    }

    val accessibility = Accessibility.fromJson(data["accessibility"])
    val a11yLabel = accessibility?.resolveLabel(dataContext)

    when (variant?.lowercase()) {
        "borderless" -> TextButton(
            onClick = onClick,
            enabled = enabled
        ) { renderButtonChild(childId, buildChild, a11yLabel) }
        "primary" -> Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) { renderButtonChild(childId, buildChild, a11yLabel) }
        else -> Button(
            onClick = onClick,
            enabled = enabled
        ) { renderButtonChild(childId, buildChild, a11yLabel) }
    }
}

@Composable
private fun renderButtonChild(
    childId: String?,
    buildChild: ChildBuilder,
    accessibilityLabel: String?
) {
    when {
        childId != null -> buildChild(childId)
        accessibilityLabel != null -> Text(accessibilityLabel)
        else -> Text("Button")
    }
}

private fun resolveContext(contextObject: JsonObject?, dataContext: DataContext): JsonObject? {
    if (contextObject == null || contextObject.isEmpty()) return null

    val resolved = mutableMapOf<String, JsonElement>()
    for ((key, value) in contextObject) {
        val resolvedValue: JsonElement? = when {
            value is JsonObject && value.containsKey("path") -> {
                val path = value["path"]?.jsonPrimitive?.content ?: ""
                dataContext.getString(path)?.let { JsonPrimitive(it) }
                    ?: dataContext.getNumber(path)?.let { JsonPrimitive(it) }
                    ?: dataContext.getBoolean(path)?.let { JsonPrimitive(it) }
            }
            value is JsonObject && value.containsKey("call") -> {
                val call = value["call"]?.jsonPrimitive?.content
                val args = value["args"] as? JsonObject
                if (call != null) FunctionEvaluator.evaluate(call, args, dataContext) else null
            }
            value is JsonPrimitive -> value
            else -> null
        }
        if (resolvedValue != null) {
            resolved[key] = resolvedValue
        }
    }
    return if (resolved.isNotEmpty()) JsonObject(resolved) else null
}

@OptIn(kotlin.time.ExperimentalTime::class)
private fun getCurrentIso8601Timestamp(): String = Clock.System.now().toString()
