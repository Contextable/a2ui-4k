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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for A2UI operations - CreateSurface, UpdateComponents, UpdateDataModel, DeleteSurface.
 *
 * These operations modify surface state per the A2UI v0.9 protocol.
 */
class A2UIOperationTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ========== CreateSurface tests ==========

    @Test
    fun `CreateSurface has required properties`() {
        val op = CreateSurface(
            surfaceId = "main",
            catalogId = "standard"
        )

        assertEquals("main", op.surfaceId)
        assertEquals("standard", op.catalogId)
        assertNull(op.theme)
    }

    @Test
    fun `CreateSurface with theme`() {
        val theme = JsonObject(mapOf(
            "theme" to JsonPrimitive("dark"),
            "fontSize" to JsonPrimitive(16)
        ))

        val op = CreateSurface(
            surfaceId = "styled-surface",
            catalogId = "standard",
            theme = theme
        )

        assertNotNull(op.theme)
        assertEquals("dark", op.theme?.get("theme")?.toString()?.trim('"'))
    }

    @Test
    fun `CreateSurface deserializes from JSON`() {
        val jsonStr = """
            {
                "surfaceId": "test-surface",
                "catalogId": "standard",
                "theme": {"background": "white"}
            }
        """.trimIndent()

        val op = json.decodeFromString<CreateSurface>(jsonStr)

        assertEquals("test-surface", op.surfaceId)
        assertEquals("standard", op.catalogId)
        assertNotNull(op.theme)
    }

    // ========== UpdateComponents tests ==========

    @Test
    fun `UpdateComponents with empty components`() {
        val op = UpdateComponents(
            surfaceId = "main",
            components = emptyList()
        )

        assertEquals("main", op.surfaceId)
        assertTrue(op.components.isEmpty())
    }

    @Test
    fun `UpdateComponents with components`() {
        val compDef = ComponentDef(
            id = "text1",
            component = "Text",
            properties = JsonObject(mapOf(
                "text" to JsonPrimitive("Hello")
            ))
        )

        val op = UpdateComponents(
            surfaceId = "main",
            components = listOf(compDef)
        )

        assertEquals(1, op.components.size)
        assertEquals("text1", op.components[0].id)
        assertEquals("Text", op.components[0].component)
    }

    // ========== UpdateDataModel tests ==========

    @Test
    fun `UpdateDataModel with string value`() {
        val op = UpdateDataModel(
            surfaceId = "form",
            path = "/user/name",
            value = JsonPrimitive("John")
        )

        assertEquals("form", op.surfaceId)
        assertEquals("/user/name", op.path)
        assertEquals(JsonPrimitive("John"), op.value)
    }

    @Test
    fun `UpdateDataModel with object value`() {
        val value = JsonObject(mapOf(
            "firstName" to JsonPrimitive("John"),
            "age" to JsonPrimitive(30),
            "active" to JsonPrimitive(true)
        ))

        val op = UpdateDataModel(
            surfaceId = "profile",
            path = "/user",
            value = value
        )

        assertEquals("profile", op.surfaceId)
        assertEquals("/user", op.path)
        assertNotNull(op.value)
        assertTrue(op.value is JsonObject)
    }

    @Test
    fun `UpdateDataModel with null value deletes key`() {
        val op = UpdateDataModel(
            surfaceId = "form",
            path = "/user/name"
        )

        assertEquals("form", op.surfaceId)
        assertEquals("/user/name", op.path)
        assertNull(op.value)
    }

    // ========== DeleteSurface tests ==========

    @Test
    fun `DeleteSurface has surfaceId`() {
        val op = DeleteSurface(surfaceId = "temp-surface")

        assertEquals("temp-surface", op.surfaceId)
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
            "type" to JsonPrimitive("createSurface"),
            "surfaceId" to JsonPrimitive("main"),
            "catalogId" to JsonPrimitive("standard")
        ))

        val content = A2UIActivityContent(operations = listOf(op))

        assertEquals(1, content.operations.size)
    }
}
