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

package com.contextable.a2ui4k.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Represents a UI component in the A2UI protocol v0.8 format.
 *
 * In v0.8, components use a nested structure where the widget type is a key
 * containing the properties object:
 * ```json
 * {
 *   "id": "button_1",
 *   "component": {
 *     "Button": { "child": "text_1" }
 *   }
 * }
 * ```
 *
 * Components are typically created via [ComponentDef.fromJson] when processing
 * `surfaceUpdate` operations.
 *
 * @property id Unique identifier for this component within its surface
 * @property componentProperties Map with widget type as key, properties as value
 * @property weight Optional flex weight for layout containers (Row/Column)
 *
 * @see ComponentDef
 * @see UiDefinition
 * @see CatalogItem
 */
@Serializable
data class Component(
    val id: String,
    val componentProperties: Map<String, JsonObject> = emptyMap(),
    val weight: Int? = null
) {
    /**
     * Returns the widget type name (e.g., "Text", "Button", "Column").
     */
    val widgetType: String?
        get() = componentProperties.keys.firstOrNull()

    /**
     * Returns the widget configuration data.
     */
    val widgetData: JsonObject?
        get() = componentProperties.values.firstOrNull()

    companion object {
        /**
         * Creates a Component from a ComponentDef.
         */
        fun fromComponentDef(def: ComponentDef): Component {
            return Component(
                id = def.id,
                componentProperties = mapOf(def.component to def.properties),
                weight = def.weight
            )
        }

        /**
         * Creates a Component with the given properties.
         */
        fun create(id: String, widgetType: String, data: JsonObject, weight: Int? = null): Component {
            return Component(
                id = id,
                componentProperties = mapOf(widgetType to data),
                weight = weight
            )
        }
    }
}
