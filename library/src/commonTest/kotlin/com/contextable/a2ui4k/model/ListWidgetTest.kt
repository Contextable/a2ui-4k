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
 * Tests for List widget JSON parsing.
 *
 * A2UI Spec properties (v0.9):
 * - children (required): Either plain array or template object
 * - direction (optional): vertical, horizontal
 * - alignment (optional): start, center, end
 */
class ListWidgetTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parseChildren extracts plain array`() {
        val jsonStr = """
            {
                "children": ["item1", "item2", "item3"]
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val childrenRef = DataReferenceParser.parseChildren(data["children"])

        assertNotNull(childrenRef)
        assertTrue(childrenRef is ChildrenReference.ExplicitList)
        assertEquals(listOf("item1", "item2", "item3"), (childrenRef as ChildrenReference.ExplicitList).componentIds)
    }

    @Test
    fun `parseChildren extracts template with path`() {
        val jsonStr = """
            {
                "children": {
                    "componentId": "item-template",
                    "path": "/items"
                }
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val childrenRef = DataReferenceParser.parseChildren(data["children"])

        assertNotNull(childrenRef)
        assertTrue(childrenRef is ChildrenReference.Template)
        val template = childrenRef as ChildrenReference.Template
        assertEquals("item-template", template.componentId)
        assertEquals("/items", template.path)
    }

    @Test
    fun `parseString extracts direction vertical`() {
        val jsonStr = """
            {
                "direction": "vertical"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val dirRef = DataReferenceParser.parseString(data["direction"])

        assertNotNull(dirRef)
        assertEquals("vertical", (dirRef as LiteralString).value)
    }

    @Test
    fun `parseString extracts direction horizontal`() {
        val jsonStr = """
            {
                "direction": "horizontal"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val dirRef = DataReferenceParser.parseString(data["direction"])

        assertNotNull(dirRef)
        assertEquals("horizontal", (dirRef as LiteralString).value)
    }

    @Test
    fun `all alignment values are valid`() {
        val alignments = listOf("start", "center", "end")

        alignments.forEach { align ->
            val jsonStr = """{"alignment": "$align"}"""
            val data = json.decodeFromString<JsonObject>(jsonStr)
            val ref = DataReferenceParser.parseString(data["alignment"])

            assertNotNull(ref, "alignment '$align' should parse")
            assertEquals(align, (ref as LiteralString).value)
        }
    }

    @Test
    fun `complete List with template and direction`() {
        val jsonStr = """
            {
                "children": {
                    "componentId": "card-template",
                    "path": "/products"
                },
                "direction": "vertical",
                "alignment": "center"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val childrenRef = DataReferenceParser.parseChildren(data["children"])
        assertTrue(childrenRef is ChildrenReference.Template)

        val dirRef = DataReferenceParser.parseString(data["direction"])
        assertEquals("vertical", (dirRef as LiteralString).value)

        val alignRef = DataReferenceParser.parseString(data["alignment"])
        assertEquals("center", (alignRef as LiteralString).value)
    }

    @Test
    fun `missing optional properties return null`() {
        val jsonStr = """
            {
                "children": ["a", "b"]
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        assertNull(DataReferenceParser.parseString(data["direction"]))
        assertNull(DataReferenceParser.parseString(data["alignment"]))
    }

    private fun assertTrue(condition: Boolean) {
        kotlin.test.assertTrue(condition)
    }
}
