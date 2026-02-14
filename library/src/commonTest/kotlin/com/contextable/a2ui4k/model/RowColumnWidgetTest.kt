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

/**
 * Tests for Row and Column widget property parsing.
 *
 * A2UI Spec v0.9 supported values:
 * - Row justify: "start", "center", "end", "spaceBetween", "spaceAround", "spaceEvenly", "stretch"
 * - Row align: "start", "center", "end", "stretch"
 * - Column justify: "start", "center", "end", "spaceBetween", "spaceAround", "spaceEvenly", "stretch"
 * - Column align: "start", "center", "end", "stretch"
 * - children: plain array of component IDs (v0.9 format)
 */
class RowColumnWidgetTest {

    private val json = Json { ignoreUnknownKeys = true }

    // Tests for children parsing
    @Test
    fun `parseChildren extracts plain array of children`() {
        val jsonStr = """
            {
                "children": ["child1", "child2", "child3"]
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val childrenRef = DataReferenceParser.parseChildren(data["children"])

        assertNotNull(childrenRef)
        assertEquals(listOf("child1", "child2", "child3"), (childrenRef as ChildrenReference.ExplicitList).componentIds)
    }

    // Tests for justify parsing (renamed from distribution)
    @Test
    fun `parseString extracts justify value`() {
        val justifyValues = listOf(
            "start", "center", "end",
            "spaceBetween", "spaceAround", "spaceEvenly",
            "stretch"
        )

        justifyValues.forEach { justify ->
            val jsonStr = """{"justify": "$justify"}"""
            val data = json.decodeFromString<JsonObject>(jsonStr)
            val ref = DataReferenceParser.parseString(data["justify"])

            assertNotNull(ref, "justify '$justify' should parse")
            assertEquals(justify, (ref as LiteralString).value)
        }
    }

    // Tests for align parsing (renamed from alignment)
    @Test
    fun `parseString extracts align value`() {
        val alignValues = listOf("start", "center", "end", "stretch")

        alignValues.forEach { align ->
            val jsonStr = """{"align": "$align"}"""
            val data = json.decodeFromString<JsonObject>(jsonStr)
            val ref = DataReferenceParser.parseString(data["align"])

            assertNotNull(ref, "align '$align' should parse")
            assertEquals(align, (ref as LiteralString).value)
        }
    }

    // Tests for Row with all properties
    @Test
    fun `Row with justify and align`() {
        val jsonStr = """
            {
                "children": ["left", "center", "right"],
                "justify": "spaceBetween",
                "align": "center"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val childrenRef = DataReferenceParser.parseChildren(data["children"])
        assertNotNull(childrenRef)
        assertEquals(3, (childrenRef as ChildrenReference.ExplicitList).componentIds.size)

        val justifyRef = DataReferenceParser.parseString(data["justify"])
        assertEquals("spaceBetween", (justifyRef as LiteralString).value)

        val alignRef = DataReferenceParser.parseString(data["align"])
        assertEquals("center", (alignRef as LiteralString).value)
    }

    // Tests for Column with all properties
    @Test
    fun `Column with justify and align`() {
        val jsonStr = """
            {
                "children": ["header", "content", "footer"],
                "justify": "spaceEvenly",
                "align": "center"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val childrenRef = DataReferenceParser.parseChildren(data["children"])
        assertNotNull(childrenRef)
        assertEquals(3, (childrenRef as ChildrenReference.ExplicitList).componentIds.size)

        val justifyRef = DataReferenceParser.parseString(data["justify"])
        assertEquals("spaceEvenly", (justifyRef as LiteralString).value)

        val alignRef = DataReferenceParser.parseString(data["align"])
        assertEquals("center", (alignRef as LiteralString).value)
    }

    // Test path-based values for dynamic binding
    @Test
    fun `justify from path binding`() {
        val jsonStr = """
            {
                "justify": {"path": "/ui/rowDistribution"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val ref = DataReferenceParser.parseString(data["justify"])

        assertNotNull(ref)
        assertEquals("/ui/rowDistribution", (ref as PathString).path)
    }
}
