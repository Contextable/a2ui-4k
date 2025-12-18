package com.contextable.a2ui4k.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Represents a UI component in the A2UI protocol v0.8 format.
 *
 * v0.8 format uses a nested component object:
 * ```json
 * {
 *   "id": "button_1",
 *   "component": {
 *     "Button": { "child": "text_1" }
 *   }
 * }
 * ```
 *
 * @property id Unique identifier for this component within its surface
 * @property componentProperties Map with widget type as key, properties as value
 * @property weight Optional flex weight for layout containers
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
