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

package com.contextable.a2ui4k.example.editor

import com.contextable.a2ui4k.model.Component
import com.contextable.a2ui4k.model.ComponentDef
import com.contextable.a2ui4k.model.UiDefinition
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Parses user-entered JSON into UiDefinition.
 *
 * Expects two separate inputs:
 * - Components: Array of component definitions (v0.9 flat format)
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
     * Components format (v0.9 flat array):
     * ```json
     * [
     *   {"id": "root", "component": "Column", "children": ["text1"]},
     *   {"id": "text1", "component": "Text", "text": "Hello"}
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
                    components = components
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

            // v0.9: flat format where "component" is a string type discriminator
            // and all properties are at the top level
            val def = ComponentDef.fromJson(obj)
            val component = Component.fromComponentDef(def)

            component.id to component
        }
    }
}
