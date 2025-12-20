package com.contextable.a2ui4k.util

import kotlinx.serialization.json.JsonObject

/**
 * Utility for validating A2UI component properties against the v0.8 specification.
 *
 * Logs info-level warnings for any unexpected properties encountered during parsing.
 * This helps identify JSON that doesn't conform to the official A2UI protocol.
 */
object PropertyValidation {

    /**
     * Checks for unexpected properties in a widget's data object.
     * Logs an info-level warning for any properties not in the expected set.
     *
     * @param widgetName The name of the widget being validated (e.g., "Button", "Text")
     * @param data The JsonObject containing the widget's properties
     * @param expectedProperties Set of property names that are valid per A2UI spec
     */
    fun warnUnexpectedProperties(
        widgetName: String,
        data: JsonObject,
        expectedProperties: Set<String>
    ) {
        val unexpected = data.keys - expectedProperties
        if (unexpected.isNotEmpty()) {
            println("Info: $widgetName has unexpected properties: $unexpected")
        }
    }
}
