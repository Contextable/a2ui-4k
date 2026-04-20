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
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for ComponentDef parsing from JSON.
 *
 * These tests ensure that component definitions are correctly parsed
 * in v0.9 flat format where 'component' is a string discriminator
 * and properties are at the top level.
 */
class ComponentDefTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `fromJson extracts id and component type`() {
        val jsonStr = """
            {
                "id": "my-button",
                "component": "Button",
                "child": "button-text"
            }
        """.trimIndent()

        val jsonObj = json.decodeFromString<JsonObject>(jsonStr)
        val def = ComponentDef.fromJson(jsonObj)

        assertEquals("my-button", def.id)
        assertEquals("Button", def.component)
    }

    @Test
    fun `fromJson extracts weight from top level`() {
        val jsonStr = """
            {
                "id": "template-image",
                "weight": 1,
                "component": "Image",
                "url": {"path": "imageUrl"}
            }
        """.trimIndent()

        val jsonObj = json.decodeFromString<JsonObject>(jsonStr)
        val def = ComponentDef.fromJson(jsonObj)

        assertEquals("template-image", def.id)
        assertEquals("Image", def.component)
        assertEquals(1, def.weight)
    }

    @Test
    fun `fromJson extracts larger weight values`() {
        val jsonStr = """
            {
                "id": "card-details",
                "weight": 2,
                "component": "Column",
                "children": ["title", "subtitle"]
            }
        """.trimIndent()

        val jsonObj = json.decodeFromString<JsonObject>(jsonStr)
        val def = ComponentDef.fromJson(jsonObj)

        assertEquals("card-details", def.id)
        assertEquals("Column", def.component)
        assertEquals(2, def.weight)
    }

    @Test
    fun `fromJson handles missing weight as null`() {
        val jsonStr = """
            {
                "id": "simple-text",
                "component": "Text",
                "text": "Hello"
            }
        """.trimIndent()

        val jsonObj = json.decodeFromString<JsonObject>(jsonStr)
        val def = ComponentDef.fromJson(jsonObj)

        assertEquals("simple-text", def.id)
        assertEquals("Text", def.component)
        assertNull(def.weight)
    }

    @Test
    fun `fromJson extracts widget properties`() {
        val jsonStr = """
            {
                "id": "my-text",
                "component": "Text",
                "text": "Hello World",
                "variant": "h1"
            }
        """.trimIndent()

        val jsonObj = json.decodeFromString<JsonObject>(jsonStr)
        val def = ComponentDef.fromJson(jsonObj)

        assertEquals("my-text", def.id)
        assertEquals("Text", def.component)
        assertNotNull(def.properties["text"])
        assertNotNull(def.properties["variant"])
    }

    @Test
    fun `Component fromComponentDef preserves weight`() {
        val jsonStr = """
            {
                "id": "weighted-image",
                "weight": 3,
                "component": "Image",
                "url": "https://example.com/img.jpg"
            }
        """.trimIndent()

        val jsonObj = json.decodeFromString<JsonObject>(jsonStr)
        val def = ComponentDef.fromJson(jsonObj)
        val component = Component.fromComponentDef(def)

        assertEquals("weighted-image", component.id)
        assertEquals("Image", component.widgetType)
        assertEquals(3, component.weight)
    }

    @Test
    fun `Component fromComponentDef handles null weight`() {
        val jsonStr = """
            {
                "id": "no-weight",
                "component": "Text",
                "text": "No weight"
            }
        """.trimIndent()

        val jsonObj = json.decodeFromString<JsonObject>(jsonStr)
        val def = ComponentDef.fromJson(jsonObj)
        val component = Component.fromComponentDef(def)

        assertEquals("no-weight", component.id)
        assertEquals("Text", component.widgetType)
        assertNull(component.weight)
    }
}
