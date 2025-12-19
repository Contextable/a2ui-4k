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
import com.contextable.a2ui4k.model.LiteralBoolean
import com.contextable.a2ui4k.model.LiteralString
import com.contextable.a2ui4k.model.PathBoolean
import com.contextable.a2ui4k.model.PathString
import com.contextable.a2ui4k.model.UserActionEvent
import com.contextable.a2ui4k.render.LocalUiDefinition
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Button widget for user actions.
 *
 * A2UI Protocol Button properties:
 * - `child`: Component ID reference (e.g., to a Text widget)
 * - `action`: Action object with "name" and "context"
 * - `usageHint`: "primary", "secondary", or "text" (v0.9)
 *
 * See A2UI protocol: standard_catalog_definition.json - Button component
 *
 * JSON Schema (v0.9):
 * ```json
 * {
 *   "component": "Button",
 *   "child": "button-text-id",
 *   "action": {"name": "submit", "context": {...}},
 *   "usageHint": "primary"
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

@Composable
private fun ButtonWidgetContent(
    componentId: String,
    data: JsonObject,
    buildChild: ChildBuilder,
    dataContext: DataContext,
    onEvent: EventDispatcher
) {
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

    // Primary button styling
    val primaryRef = DataReferenceParser.parseBoolean(data["primary"])
    val isPrimary = when (primaryRef) {
        is LiteralBoolean -> primaryRef.value
        is PathBoolean -> dataContext.getBoolean(primaryRef.path) ?: false
        else -> false
    }

    val actionData = data["action"]?.jsonObject

    // Get surfaceId from UiDefinition (not from component data)
    val uiDefinition = LocalUiDefinition.current
    val surfaceId = uiDefinition?.surfaceId ?: "default"

    // Get template item key for sourceComponentId suffix
    val templateItemKey = LocalTemplateItemKey.current

    val onClick: () -> Unit = {
        val actionName = actionData?.get("name")?.jsonPrimitive?.content ?: "click"

        // Process dataUpdates for internal data binding (A2UI v0.9)
        val dataUpdates = actionData?.get("dataUpdates")?.jsonArray
        dataUpdates?.forEach { update ->
            val updateObj = update as? JsonObject ?: return@forEach
            val path = updateObj["path"]?.jsonPrimitive?.content ?: return@forEach
            val value = updateObj["value"]
            when {
                value is JsonPrimitive && value.booleanOrNull != null ->
                    dataContext.update(path, value.booleanOrNull!!)
                value is JsonPrimitive && value.doubleOrNull != null ->
                    dataContext.update(path, value.doubleOrNull!!)
                value is JsonPrimitive && value.contentOrNull != null ->
                    dataContext.update(path, value.contentOrNull!!)
            }
        }

        // Resolve action.context (A2UI Button.action.context property)
        val contextArray = actionData?.get("context")?.jsonArray
        val resolvedContext = resolveContext(contextArray, dataContext)

        // Build sourceComponentId with item suffix (e.g., "template-book-button:item1")
        val sourceComponentId = if (templateItemKey != null) {
            "$componentId:item$templateItemKey"
        } else {
            componentId
        }

        // Generate ISO8601 timestamp
        val timestamp = getCurrentIso8601Timestamp()

        onEvent(
            UserActionEvent(
                name = actionName,
                surfaceId = surfaceId,
                sourceComponentId = sourceComponentId,
                timestamp = timestamp,
                context = resolvedContext
            )
        )
    }

    // Button styling based on usageHint/primary
    // primary/usageHint="primary": colorScheme.primary background
    // secondary/usageHint="secondary": colorScheme.surface background
    val colors = if (isPrimary) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
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
 * A2UI Button.action.context is resolved at event time:
 * - v0.8: Array of {key, value} pairs where value contains path/literal bindings
 * - v0.9: Standard JSON object with path bindings
 *
 * @param contextArray The action.context JsonArray from the button definition
 * @param dataContext The current DataContext for resolving path bindings
 * @return JsonObject with resolved key-value pairs, or null if no context
 */
private fun resolveContext(contextArray: JsonArray?, dataContext: DataContext): JsonObject? {
    if (contextArray == null || contextArray.isEmpty()) return null

    val resolved = mutableMapOf<String, JsonElement>()

    for (entry in contextArray) {
        val entryObj = entry as? JsonObject ?: continue
        val key = entryObj["key"]?.jsonPrimitive?.content ?: continue
        val value = entryObj["value"]?.jsonObject ?: continue

        val resolvedValue: JsonElement? = when {
            // Path binding - resolve from DataContext
            value.containsKey("path") -> {
                val path = value["path"]?.jsonPrimitive?.content ?: ""
                // Try to get value from data context
                dataContext.getString(path)?.let { JsonPrimitive(it) }
                    ?: dataContext.getNumber(path)?.let { JsonPrimitive(it) }
                    ?: dataContext.getBoolean(path)?.let { JsonPrimitive(it) }
            }
            // Literal string
            value.containsKey("literalString") -> {
                value["literalString"]?.jsonPrimitive?.content?.let { JsonPrimitive(it) }
            }
            // Literal number
            value.containsKey("literalNumber") -> {
                value["literalNumber"]?.jsonPrimitive?.doubleOrNull?.let { JsonPrimitive(it) }
            }
            // Literal boolean
            value.containsKey("literalBoolean") -> {
                value["literalBoolean"]?.jsonPrimitive?.booleanOrNull?.let { JsonPrimitive(it) }
            }
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
