package com.contextable.a2ui4k.example.editor

import com.contextable.a2ui4k.model.Component
import com.contextable.a2ui4k.model.UiDefinition
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Parses user-entered JSON into UiDefinition.
 *
 * Expects two separate inputs:
 * - Components: Array of component definitions
 * - Data: Object with data for path bindings
 */
object JsonParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Parses components array and data object into a UiDefinition.
     *
     * Components format (array):
     * ```json
     * [
     *   {"id": "root", "component": {"Column": {"children": {...}}}},
     *   {"id": "text1", "component": {"Text": {"text": "Hello"}}}
     * ]
     * ```
     *
     * Data format (object):
     * ```json
     * {"location": "Austin, TX", "temp": "72°"}
     * ```
     */
    fun parse(componentsJson: String, dataJson: String): ParseResult {
        if (componentsJson.isBlank()) {
            return ParseResult.Empty
        }

        return try {
            // Parse components array
            val componentsArray = json.parseToJsonElement(componentsJson).jsonArray
            val components = parseComponentsArray(componentsArray)

            // Parse data object (defaults to empty)
            val data = if (dataJson.isBlank() || dataJson.trim() == "{}") {
                JsonObject(emptyMap())
            } else {
                json.parseToJsonElement(dataJson).jsonObject
            }

            // Validate root exists
            if (!components.containsKey("root")) {
                return ParseResult.Error("Missing component with id='root'")
            }

            ParseResult.Success(
                definition = UiDefinition(
                    surfaceId = "editor",
                    components = components,
                    root = "root"
                ),
                initialData = data
            )
        } catch (e: kotlinx.serialization.SerializationException) {
            ParseResult.Error("JSON syntax error: ${e.message?.take(100)}")
        } catch (e: IllegalArgumentException) {
            ParseResult.Error("Invalid JSON structure: ${e.message?.take(100)}")
        } catch (e: Exception) {
            ParseResult.Error("Parse error: ${e.message?.take(100)}")
        }
    }

    private fun parseComponentsArray(array: JsonArray): Map<String, Component> {
        return array.associate { element ->
            val obj = element.jsonObject
            val id = obj["id"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("Component missing 'id' field")

            val componentObj = obj["component"]?.jsonObject
                ?: throw IllegalArgumentException("Component '$id' missing 'component' field")

            // v0.8: component is { "WidgetType": { ...properties } }
            val (widgetType, propertiesElement) = componentObj.entries.firstOrNull()
                ?: throw IllegalArgumentException("Component '$id' has empty 'component' object")
            val properties = propertiesElement.jsonObject

            val weight = obj["weight"]?.jsonPrimitive?.intOrNull

            id to Component.create(
                id = id,
                widgetType = widgetType,
                data = properties,
                weight = weight
            )
        }
    }
}
