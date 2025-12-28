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

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for Component class - represents a UI component in A2UI v0.8 format.
 */
class ComponentTest {

    @Test
    fun `widgetType returns first key from componentProperties`() {
        val component = Component(
            id = "test",
            componentProperties = mapOf("Text" to JsonObject(emptyMap()))
        )

        assertEquals("Text", component.widgetType)
    }

    @Test
    fun `widgetType returns null when componentProperties empty`() {
        val component = Component(
            id = "test",
            componentProperties = emptyMap()
        )

        assertNull(component.widgetType)
    }

    @Test
    fun `widgetData returns first value from componentProperties`() {
        val data = JsonObject(mapOf("text" to JsonPrimitive("Hello")))
        val component = Component(
            id = "test",
            componentProperties = mapOf("Text" to data)
        )

        assertNotNull(component.widgetData)
        assertEquals(data, component.widgetData)
    }

    @Test
    fun `widgetData returns null when componentProperties empty`() {
        val component = Component(
            id = "test",
            componentProperties = emptyMap()
        )

        assertNull(component.widgetData)
    }

    @Test
    fun `create builds component correctly`() {
        val data = JsonObject(mapOf("child" to JsonPrimitive("text1")))
        val component = Component.create("btn1", "Button", data)

        assertEquals("btn1", component.id)
        assertEquals("Button", component.widgetType)
        assertEquals(data, component.widgetData)
        assertNull(component.weight)
    }

    @Test
    fun `create with weight`() {
        val data = JsonObject(emptyMap())
        val component = Component.create("col", "Column", data, weight = 2)

        assertEquals("col", component.id)
        assertEquals("Column", component.widgetType)
        assertEquals(2, component.weight)
    }

    @Test
    fun `fromComponentDef creates component`() {
        val def = ComponentDef(
            id = "img1",
            component = "Image",
            properties = JsonObject(mapOf(
                "url" to JsonObject(mapOf("literalString" to JsonPrimitive("https://example.com/img.jpg")))
            )),
            weight = 1
        )

        val component = Component.fromComponentDef(def)

        assertEquals("img1", component.id)
        assertEquals("Image", component.widgetType)
        assertEquals(1, component.weight)
        assertNotNull(component.widgetData)
    }

    @Test
    fun `fromComponentDef preserves all properties`() {
        val props = JsonObject(mapOf(
            "text" to JsonObject(mapOf("literalString" to JsonPrimitive("Hello"))),
            "usageHint" to JsonObject(mapOf("literalString" to JsonPrimitive("h1")))
        ))

        val def = ComponentDef(
            id = "heading",
            component = "Text",
            properties = props
        )

        val component = Component.fromComponentDef(def)

        assertEquals(props, component.widgetData)
    }

    @Test
    fun `Component with various widget types`() {
        val widgetTypes = listOf(
            "Text", "Button", "Card", "Row", "Column",
            "Image", "TextField", "CheckBox", "Slider",
            "Modal", "Tabs", "List", "Icon", "Divider"
        )

        widgetTypes.forEach { type ->
            val component = Component.create("test-$type", type, JsonObject(emptyMap()))
            assertEquals(type, component.widgetType, "Widget type should be $type")
        }
    }

    @Test
    fun `Component preserves complex widget data`() {
        val complexData = JsonObject(mapOf(
            "children" to JsonObject(mapOf(
                "template" to JsonObject(mapOf(
                    "componentId" to JsonPrimitive("item-template"),
                    "dataBinding" to JsonPrimitive("/items")
                ))
            )),
            "direction" to JsonPrimitive("vertical")
        ))

        val component = Component.create("list", "List", complexData)

        assertEquals(complexData, component.widgetData)
    }
}
