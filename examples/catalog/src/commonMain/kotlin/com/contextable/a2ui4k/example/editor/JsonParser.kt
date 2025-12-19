package com.contextable.a2ui4k.example.editor

import com.contextable.a2ui4k.model.Component
import com.contextable.a2ui4k.model.UiDefinition
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Parses user-entered JSON into UiDefinition.
 */
object JsonParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Parses a JSON string into a UiDefinition.
     *
     * Expected format:
     * ```json
     * {
     *   "surfaceId": "demo",
     *   "root": "root-component",
     *   "components": {
     *     "root-component": {
     *       "id": "root-component",
     *       "component": "Text",
     *       "text": "Hello"
     *     }
     *   }
     * }
     * ```
     */
    fun parseToUiDefinition(jsonString: String): ParseResult {
        if (jsonString.isBlank()) {
            return ParseResult.Empty
        }

        return try {
            val jsonElement = json.parseToJsonElement(jsonString)
            val jsonObj = jsonElement.jsonObject

            val surfaceId = jsonObj["surfaceId"]?.jsonPrimitive?.content
                ?: return ParseResult.Error("Missing 'surfaceId' field")

            val root = jsonObj["root"]?.jsonPrimitive?.content

            val componentsJson = jsonObj["components"]?.jsonObject
                ?: return ParseResult.Error("Missing 'components' field")

            val components = parseComponents(componentsJson)

            // Validate root exists in components
            if (root != null && !components.containsKey(root)) {
                return ParseResult.Error("Root component '$root' not found in components")
            }

            val catalogId = jsonObj["catalogId"]?.jsonPrimitive?.content

            // Parse initial data for data binding
            val initialData = jsonObj["data"]?.jsonObject ?: JsonObject(emptyMap())

            ParseResult.Success(
                definition = UiDefinition(
                    surfaceId = surfaceId,
                    components = components,
                    root = root,
                    catalogId = catalogId
                ),
                initialData = initialData
            )
        } catch (e: kotlinx.serialization.SerializationException) {
            ParseResult.Error("JSON syntax error: ${e.message?.take(100)}")
        } catch (e: IllegalArgumentException) {
            ParseResult.Error("Invalid JSON structure: ${e.message?.take(100)}")
        } catch (e: Exception) {
            ParseResult.Error("Parse error: ${e.message?.take(100)}")
        }
    }

    private fun parseComponents(componentsJson: JsonObject): Map<String, Component> {
        return componentsJson.mapValues { (id, componentJson) ->
            val obj = componentJson.jsonObject
            val componentId = obj["id"]?.jsonPrimitive?.content ?: id
            val componentType = obj["component"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("Component '$id' missing 'component' field")
            val weight = obj["weight"]?.jsonPrimitive?.intOrNull

            // Extract properties: all fields except id, component, weight
            val properties = buildJsonObject {
                obj.forEach { (key, value) ->
                    if (key !in listOf("id", "component", "weight")) {
                        put(key, value)
                    }
                }
            }

            Component(
                id = componentId,
                component = componentType,
                properties = properties,
                weight = weight
            )
        }
    }
}
