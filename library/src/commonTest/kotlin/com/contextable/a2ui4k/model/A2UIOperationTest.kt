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
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for A2UI operations - BeginRendering, SurfaceUpdate, DataModelUpdate, DeleteSurface.
 *
 * These operations modify surface state per the A2UI protocol.
 */
class A2UIOperationTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ========== BeginRendering tests ==========

    @Test
    fun `BeginRendering has required properties`() {
        val op = BeginRendering(
            surfaceId = "main",
            root = "root-column"
        )

        assertEquals("main", op.surfaceId)
        assertEquals("root-column", op.root)
        assertNull(op.styles)
    }

    @Test
    fun `BeginRendering with styles`() {
        val styles = JsonObject(mapOf(
            "theme" to JsonPrimitive("dark"),
            "fontSize" to JsonPrimitive(16)
        ))

        val op = BeginRendering(
            surfaceId = "styled-surface",
            root = "root",
            styles = styles
        )

        assertNotNull(op.styles)
        assertEquals("dark", op.styles?.get("theme")?.toString()?.trim('"'))
    }

    @Test
    fun `BeginRendering deserializes from JSON`() {
        val jsonStr = """
            {
                "surfaceId": "test-surface",
                "root": "main-column",
                "styles": {"background": "white"}
            }
        """.trimIndent()

        val op = json.decodeFromString<BeginRendering>(jsonStr)

        assertEquals("test-surface", op.surfaceId)
        assertEquals("main-column", op.root)
        assertNotNull(op.styles)
    }

    // ========== SurfaceUpdate tests ==========

    @Test
    fun `SurfaceUpdate with empty components`() {
        val op = SurfaceUpdate(
            surfaceId = "main",
            components = emptyList()
        )

        assertEquals("main", op.surfaceId)
        assertTrue(op.components.isEmpty())
    }

    @Test
    fun `SurfaceUpdate with components`() {
        val compDef = ComponentDef(
            id = "text1",
            component = "Text",
            properties = JsonObject(mapOf(
                "text" to JsonObject(mapOf("literalString" to JsonPrimitive("Hello")))
            ))
        )

        val op = SurfaceUpdate(
            surfaceId = "main",
            components = listOf(compDef)
        )

        assertEquals(1, op.components.size)
        assertEquals("text1", op.components[0].id)
        assertEquals("Text", op.components[0].component)
    }

    // ========== DataModelUpdate tests ==========

    @Test
    fun `DataModelUpdate with string entry`() {
        val entry = DataEntry(key = "name", valueString = "John")

        val op = DataModelUpdate(
            surfaceId = "form",
            path = "/user",
            contents = listOf(entry)
        )

        assertEquals("form", op.surfaceId)
        assertEquals("/user", op.path)
        assertEquals(1, op.contents.size)
        assertEquals("name", op.contents[0].key)
        assertEquals("John", op.contents[0].valueString)
    }

    @Test
    fun `DataModelUpdate with multiple entries`() {
        val entries = listOf(
            DataEntry(key = "firstName", valueString = "John"),
            DataEntry(key = "age", valueNumber = 30.0),
            DataEntry(key = "active", valueBoolean = true)
        )

        val op = DataModelUpdate(
            surfaceId = "profile",
            path = "/user",
            contents = entries
        )

        assertEquals(3, op.contents.size)
    }

    // ========== DeleteSurface tests ==========

    @Test
    fun `DeleteSurface has surfaceId`() {
        val op = DeleteSurface(surfaceId = "temp-surface")

        assertEquals("temp-surface", op.surfaceId)
    }

    // ========== DataEntry tests ==========

    @Test
    fun `DataEntry toJsonElement with string`() {
        val entry = DataEntry(key = "name", valueString = "Alice")
        val element = entry.toJsonElement()

        assertTrue(element is JsonPrimitive)
        assertEquals("Alice", (element as JsonPrimitive).content)
    }

    @Test
    fun `DataEntry toJsonElement with number`() {
        val entry = DataEntry(key = "count", valueNumber = 42.0)
        val element = entry.toJsonElement()

        assertTrue(element is JsonPrimitive)
        assertEquals(42.0, (element as JsonPrimitive).doubleOrNull)
    }

    @Test
    fun `DataEntry toJsonElement with boolean`() {
        val entry = DataEntry(key = "enabled", valueBoolean = true)
        val element = entry.toJsonElement()

        assertTrue(element is JsonPrimitive)
        assertEquals(true, (element as JsonPrimitive).booleanOrNull)
    }

    @Test
    fun `DataEntry toJsonElement with nested map`() {
        val nestedEntries = listOf(
            DataEntry(key = "city", valueString = "NYC"),
            DataEntry(key = "zip", valueString = "10001")
        )
        val entry = DataEntry(key = "address", valueMap = nestedEntries)
        val element = entry.toJsonElement()

        assertTrue(element is JsonObject)
        val obj = element as JsonObject
        assertEquals("NYC", (obj["city"] as JsonPrimitive).content)
        assertEquals("10001", (obj["zip"] as JsonPrimitive).content)
    }

    @Test
    fun `DataEntry toJsonElement with raw JSON`() {
        val rawJson = JsonObject(mapOf("custom" to JsonPrimitive("data")))
        val entry = DataEntry(key = "raw", valueJson = rawJson)
        val element = entry.toJsonElement()

        assertTrue(element is JsonObject)
    }

    @Test
    fun `DataEntry toJsonElement with no value returns null`() {
        val entry = DataEntry(key = "empty")
        val element = entry.toJsonElement()

        assertEquals(JsonNull, element)
    }

    // ========== A2UIActivityContent tests ==========

    @Test
    fun `A2UIActivityContent with empty operations`() {
        val content = A2UIActivityContent()

        assertTrue(content.operations.isEmpty())
    }

    @Test
    fun `A2UIActivityContent with operations`() {
        val op = JsonObject(mapOf(
            "type" to JsonPrimitive("beginRendering"),
            "surfaceId" to JsonPrimitive("main"),
            "root" to JsonPrimitive("root")
        ))

        val content = A2UIActivityContent(operations = listOf(op))

        assertEquals(1, content.operations.size)
    }
}
