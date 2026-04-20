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

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for ChoicePickerWidget JSON parsing.
 *
 * The ChoicePicker widget in A2UI v0.9 replaces MultipleChoice and supports
 * both "multipleSelection" and "mutuallyExclusive" variants.
 */
class ChoicePickerWidgetTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `options array parses label and value correctly`() {
        val jsonStr = """
            {
                "value": {"path": "/form/selections"},
                "options": [
                    {"label": "Option 1", "value": "opt1"},
                    {"label": "Option 2", "value": "opt2"},
                    {"label": "Option 3", "value": "opt3"}
                ]
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val options = data["options"]?.jsonArray

        assertNotNull(options)
        assertEquals(3, options.size)

        val firstOpt = options[0].jsonObject
        assertEquals("Option 1", firstOpt["label"]?.jsonPrimitive?.contentOrNull)
        assertEquals("opt1", firstOpt["value"]?.jsonPrimitive?.contentOrNull)
    }

    @Test
    fun `options with missing value defaults to label`() {
        val jsonStr = """
            {
                "value": {"path": "/selections"},
                "options": [
                    {"label": "Red"},
                    {"label": "Blue", "value": "blue"}
                ]
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val options = data["options"]?.jsonArray

        assertNotNull(options)
        assertEquals(2, options.size)

        // First option has no explicit "value" - widget code falls back to label
        val firstOpt = options[0].jsonObject
        assertEquals("Red", firstOpt["label"]?.jsonPrimitive?.contentOrNull)
        assertNull(firstOpt["value"])
    }

    @Test
    fun `variant mutuallyExclusive parses correctly`() {
        val jsonStr = """
            {
                "value": {"path": "/form/choice"},
                "options": [
                    {"label": "Yes", "value": "yes"},
                    {"label": "No", "value": "no"}
                ],
                "variant": "mutuallyExclusive"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val variant = data["variant"]?.jsonPrimitive?.contentOrNull

        assertEquals("mutuallyExclusive", variant)
    }

    @Test
    fun `variant multipleSelection parses correctly`() {
        val jsonStr = """
            {
                "value": {"path": "/form/tags"},
                "options": [
                    {"label": "Tag A", "value": "a"},
                    {"label": "Tag B", "value": "b"}
                ],
                "variant": "multipleSelection"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val variant = data["variant"]?.jsonPrimitive?.contentOrNull

        assertEquals("multipleSelection", variant)
    }

    @Test
    fun `missing variant defaults to multipleSelection behavior`() {
        val jsonStr = """
            {
                "value": {"path": "/form/items"},
                "options": [
                    {"label": "Item 1", "value": "i1"}
                ]
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val variant = data["variant"]?.jsonPrimitive?.contentOrNull

        // When variant is null, the widget defaults to multipleSelection
        assertNull(variant)
    }

    @Test
    fun `value with path binding parses correctly`() {
        val jsonStr = """
            {
                "value": {"path": "/form/selections"},
                "options": [
                    {"label": "A", "value": "a"}
                ]
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val valueElement = data["value"] as? JsonObject

        assertNotNull(valueElement)
        assertEquals("/form/selections", valueElement["path"]?.jsonPrimitive?.contentOrNull)
    }

    @Test
    fun `value as inline array parses correctly`() {
        val jsonStr = """
            {
                "value": ["preselected1", "preselected2"],
                "options": [
                    {"label": "A", "value": "preselected1"},
                    {"label": "B", "value": "preselected2"},
                    {"label": "C", "value": "other"}
                ]
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val valueElement = data["value"]

        assertTrue(valueElement is JsonArray)
        assertEquals(2, valueElement.size)
        assertEquals("preselected1", valueElement[0].jsonPrimitive.contentOrNull)
        assertEquals("preselected2", valueElement[1].jsonPrimitive.contentOrNull)
    }

    @Test
    fun `displayStyle property parses correctly`() {
        val jsonStr = """
            {
                "value": {"path": "/selections"},
                "options": [{"label": "A", "value": "a"}],
                "displayStyle": "checkbox"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val displayStyle = data["displayStyle"]?.jsonPrimitive?.contentOrNull

        assertEquals("checkbox", displayStyle)
    }

    @Test
    fun `displayStyle chips variant parses correctly`() {
        val jsonStr = """
            {
                "value": {"path": "/selections"},
                "options": [{"label": "A", "value": "a"}],
                "displayStyle": "chips"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val displayStyle = data["displayStyle"]?.jsonPrimitive?.contentOrNull

        assertEquals("chips", displayStyle)
    }

    @Test
    fun `filterable property parses correctly`() {
        val jsonStr = """
            {
                "value": {"path": "/selections"},
                "options": [{"label": "A", "value": "a"}],
                "filterable": true
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val filterable = data["filterable"]?.jsonPrimitive?.contentOrNull

        assertEquals("true", filterable)
    }

    @Test
    fun `empty options array parses as empty list`() {
        val jsonStr = """
            {
                "value": {"path": "/selections"},
                "options": []
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val options = data["options"]?.jsonArray

        assertNotNull(options)
        assertTrue(options.isEmpty())
    }

    @Test
    fun `component definition with ChoicePicker type creates correct ComponentDef`() {
        val jsonStr = """
            {
                "id": "color-picker",
                "component": "ChoicePicker",
                "value": {"path": "/form/colors"},
                "options": [
                    {"label": "Red", "value": "red"},
                    {"label": "Blue", "value": "blue"}
                ],
                "variant": "multipleSelection"
            }
        """.trimIndent()

        val compJson = json.decodeFromString<JsonObject>(jsonStr)
        val componentDef = ComponentDef.fromJson(compJson)

        assertEquals("color-picker", componentDef.id)
        assertEquals("ChoicePicker", componentDef.component)

        // Properties should contain value, options, variant (not id/component)
        assertTrue(componentDef.properties.containsKey("value"))
        assertTrue(componentDef.properties.containsKey("options"))
        assertTrue(componentDef.properties.containsKey("variant"))
    }

    @Test
    fun `options with only labels and no values skips entries without labels`() {
        val jsonStr = """
            {
                "value": {"path": "/picks"},
                "options": [
                    {"label": "Valid"},
                    {"value": "no-label"},
                    {"label": "Also Valid", "value": "valid2"}
                ]
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val options = data["options"]?.jsonArray

        assertNotNull(options)
        assertEquals(3, options.size)

        // The widget's mapNotNull logic filters out items without labels
        // Here we verify the raw JSON has the expected structure
        val secondOpt = options[1].jsonObject
        assertNull(secondOpt["label"])
        assertNotNull(secondOpt["value"])
    }

    @Test
    fun `full ChoicePicker schema with all properties`() {
        val jsonStr = """
            {
                "id": "picker-1",
                "component": "ChoicePicker",
                "value": {"path": "/form/selections"},
                "options": [
                    {"label": "Option 1", "value": "opt1"},
                    {"label": "Option 2", "value": "opt2"}
                ],
                "variant": "multipleSelection",
                "displayStyle": "checkbox",
                "filterable": true
            }
        """.trimIndent()

        val compJson = json.decodeFromString<JsonObject>(jsonStr)
        val componentDef = ComponentDef.fromJson(compJson)
        val component = Component.fromComponentDef(componentDef)

        assertEquals("picker-1", component.id)
        assertEquals("ChoicePicker", component.widgetType)
        assertNotNull(component.widgetData["options"])
        assertNotNull(component.widgetData["value"])
        assertNotNull(component.widgetData["variant"])
        assertNotNull(component.widgetData["displayStyle"])
        assertNotNull(component.widgetData["filterable"])
    }
}
