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
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Comprehensive tests for DataReferenceParser.
 *
 * These tests cover all parsing methods and edge cases for A2UI v0.9 protocol compliance.
 */
class DataReferenceParserTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ========== parseString tests ==========

    @Test
    fun `parseString with plain string primitive`() {
        val element = JsonPrimitive("Hello World")
        val ref = DataReferenceParser.parseString(element)

        assertNotNull(ref)
        assertTrue(ref is LiteralString)
        assertEquals("Hello World", (ref as LiteralString).value)
    }

    @Test
    fun `parseString with path object`() {
        val element = json.parseToJsonElement("""{"path": "/user/name"}""")
        val ref = DataReferenceParser.parseString(element)

        assertNotNull(ref)
        assertTrue(ref is PathString)
        assertEquals("/user/name", (ref as PathString).path)
    }

    @Test
    fun `parseString with another plain string`() {
        val element = JsonPrimitive("plain text")
        val ref = DataReferenceParser.parseString(element)

        assertNotNull(ref)
        assertTrue(ref is LiteralString)
        assertEquals("plain text", (ref as LiteralString).value)
    }

    @Test
    fun `parseString with null returns null`() {
        assertNull(DataReferenceParser.parseString(null))
    }

    @Test
    fun `parseString with empty string`() {
        val element = JsonPrimitive("")
        val ref = DataReferenceParser.parseString(element)

        assertNotNull(ref)
        assertEquals("", (ref as LiteralString).value)
    }

    @Test
    fun `parseString with JsonArray returns null`() {
        val element = JsonArray(listOf(JsonPrimitive("a"), JsonPrimitive("b")))
        assertNull(DataReferenceParser.parseString(element))
    }

    // ========== parseNumber tests ==========

    @Test
    fun `parseNumber with plain number primitive`() {
        val element = JsonPrimitive(42.5)
        val ref = DataReferenceParser.parseNumber(element)

        assertNotNull(ref)
        assertTrue(ref is LiteralNumber)
        assertEquals(42.5, (ref as LiteralNumber).value)
    }

    @Test
    fun `parseNumber with path object`() {
        val element = json.parseToJsonElement("""{"path": "/settings/volume"}""")
        val ref = DataReferenceParser.parseNumber(element)

        assertNotNull(ref)
        assertTrue(ref is PathNumber)
        assertEquals("/settings/volume", (ref as PathNumber).path)
    }

    @Test
    fun `parseNumber with integer primitive`() {
        val element = JsonPrimitive(100)
        val ref = DataReferenceParser.parseNumber(element)

        assertNotNull(ref)
        assertTrue(ref is LiteralNumber)
        assertEquals(100.0, (ref as LiteralNumber).value)
    }

    @Test
    fun `parseNumber with negative number`() {
        val element = JsonPrimitive(-50)
        val ref = DataReferenceParser.parseNumber(element)

        assertNotNull(ref)
        assertEquals(-50.0, (ref as LiteralNumber).value)
    }

    @Test
    fun `parseNumber with decimal`() {
        val element = JsonPrimitive(3.14159)
        val ref = DataReferenceParser.parseNumber(element)

        assertNotNull(ref)
        assertEquals(3.14159, (ref as LiteralNumber).value)
    }

    @Test
    fun `parseNumber with null returns null`() {
        assertNull(DataReferenceParser.parseNumber(null))
    }

    @Test
    fun `parseNumber with string primitive returns null`() {
        val element = JsonPrimitive("not a number")
        assertNull(DataReferenceParser.parseNumber(element))
    }

    // ========== parseBoolean tests ==========

    @Test
    fun `parseBoolean with plain boolean true`() {
        val element = JsonPrimitive(true)
        val ref = DataReferenceParser.parseBoolean(element)

        assertNotNull(ref)
        assertTrue(ref is LiteralBoolean)
        assertEquals(true, (ref as LiteralBoolean).value)
    }

    @Test
    fun `parseBoolean with plain boolean false`() {
        val element = JsonPrimitive(false)
        val ref = DataReferenceParser.parseBoolean(element)

        assertNotNull(ref)
        assertEquals(false, (ref as LiteralBoolean).value)
    }

    @Test
    fun `parseBoolean with path object`() {
        val element = json.parseToJsonElement("""{"path": "/settings/darkMode"}""")
        val ref = DataReferenceParser.parseBoolean(element)

        assertNotNull(ref)
        assertTrue(ref is PathBoolean)
        assertEquals("/settings/darkMode", (ref as PathBoolean).path)
    }

    @Test
    fun `parseBoolean with another plain boolean`() {
        val element = JsonPrimitive(true)
        val ref = DataReferenceParser.parseBoolean(element)

        assertNotNull(ref)
        assertEquals(true, (ref as LiteralBoolean).value)
    }

    @Test
    fun `parseBoolean with null returns null`() {
        assertNull(DataReferenceParser.parseBoolean(null))
    }

    @Test
    fun `parseBoolean with non-boolean string primitive returns null`() {
        val element = JsonPrimitive("hello")
        assertNull(DataReferenceParser.parseBoolean(element))
    }

    // ========== parseComponentRef tests ==========

    @Test
    fun `parseComponentRef with string primitive`() {
        val element = JsonPrimitive("button-text")
        val ref = DataReferenceParser.parseComponentRef(element)

        assertNotNull(ref)
        assertEquals("button-text", ref.componentId)
    }

    @Test
    fun `parseComponentRef with hyphenated ID`() {
        val element = JsonPrimitive("my-complex-component-id")
        val ref = DataReferenceParser.parseComponentRef(element)

        assertNotNull(ref)
        assertEquals("my-complex-component-id", ref.componentId)
    }

    @Test
    fun `parseComponentRef with null returns null`() {
        assertNull(DataReferenceParser.parseComponentRef(null))
    }

    @Test
    fun `parseComponentRef with object returns null`() {
        val element = json.parseToJsonElement("""{"id": "test"}""")
        assertNull(DataReferenceParser.parseComponentRef(element))
    }

    // ========== parseChildren tests ==========

    @Test
    fun `parseChildren with plain array`() {
        val element = json.parseToJsonElement("""["child1", "child2", "child3"]""")
        val ref = DataReferenceParser.parseChildren(element)

        assertNotNull(ref)
        assertTrue(ref is ChildrenReference.ExplicitList)
        assertEquals(listOf("child1", "child2", "child3"), (ref as ChildrenReference.ExplicitList).componentIds)
    }

    @Test
    fun `parseChildren with empty array`() {
        val element = json.parseToJsonElement("""[]""")
        val ref = DataReferenceParser.parseChildren(element)

        assertNotNull(ref)
        assertTrue(ref is ChildrenReference.ExplicitList)
        assertTrue((ref as ChildrenReference.ExplicitList).componentIds.isEmpty())
    }

    @Test
    fun `parseChildren with template`() {
        val element = json.parseToJsonElement("""
            {"componentId": "item-template", "path": "/items"}
        """.trimIndent())
        val ref = DataReferenceParser.parseChildren(element)

        assertNotNull(ref)
        assertTrue(ref is ChildrenReference.Template)
        val template = ref as ChildrenReference.Template
        assertEquals("item-template", template.componentId)
        assertEquals("/items", template.path)
    }

    @Test
    fun `parseChildren with null returns null`() {
        assertNull(DataReferenceParser.parseChildren(null))
    }

    @Test
    fun `parseChildren with primitive returns null`() {
        val element = JsonPrimitive("child-id")
        assertNull(DataReferenceParser.parseChildren(element))
    }

    @Test
    fun `parseChildren with incomplete template returns null`() {
        // Missing path
        val element = json.parseToJsonElement("""{"componentId": "item"}""")
        assertNull(DataReferenceParser.parseChildren(element))
    }

    @Test
    fun `parseChildren with empty object returns null`() {
        val element = json.parseToJsonElement("""{}""")
        assertNull(DataReferenceParser.parseChildren(element))
    }

    // ========== DataReference resolve tests ==========

    @Test
    fun `LiteralString resolve returns value`() {
        val ref = LiteralString("Hello")
        assertEquals("Hello", ref.resolve { null })
    }

    @Test
    fun `PathString resolve uses resolver`() {
        val ref = PathString("/user/name")
        val result = ref.resolve { path ->
            if (path == "/user/name") "John" else null
        }
        assertEquals("John", result)
    }

    @Test
    fun `PathString resolve returns null when path not found`() {
        val ref = PathString("/missing/path")
        assertNull(ref.resolve { null })
    }

    @Test
    fun `LiteralNumber resolve returns value`() {
        val ref = LiteralNumber(42.0)
        assertEquals(42.0, ref.resolve { null })
    }

    @Test
    fun `PathNumber resolve uses resolver`() {
        val ref = PathNumber("/settings/volume")
        val result = ref.resolve { path ->
            if (path == "/settings/volume") 75.0 else null
        }
        assertEquals(75.0, result)
    }

    @Test
    fun `LiteralBoolean resolve returns value`() {
        val ref = LiteralBoolean(true)
        assertEquals(true, ref.resolve { null })
    }

    @Test
    fun `PathBoolean resolve uses resolver`() {
        val ref = PathBoolean("/enabled")
        val result = ref.resolve { path ->
            if (path == "/enabled") false else null
        }
        assertEquals(false, result)
    }
}
