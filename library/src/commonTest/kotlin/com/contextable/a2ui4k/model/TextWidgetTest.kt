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
 * Tests for Text widget JSON parsing.
 *
 * A2UI Spec properties:
 * - text (required): String content as literalString or path
 * - usageHint (optional): h1, h2, h3, h4, h5, body, caption
 */
class TextWidgetTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parseString extracts literalString text`() {
        val jsonStr = """
            {
                "text": {"literalString": "Hello World"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val textRef = DataReferenceParser.parseString(data["text"])

        assertNotNull(textRef)
        assertEquals("Hello World", (textRef as LiteralString).value)
    }

    @Test
    fun `parseString extracts path binding`() {
        val jsonStr = """
            {
                "text": {"path": "/user/name"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val textRef = DataReferenceParser.parseString(data["text"])

        assertNotNull(textRef)
        assertEquals("/user/name", (textRef as PathString).path)
    }

    @Test
    fun `parseString extracts usageHint h1`() {
        val jsonStr = """
            {
                "text": {"literalString": "Title"},
                "usageHint": "h1"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val usageRef = DataReferenceParser.parseString(data["usageHint"])

        assertNotNull(usageRef)
        assertEquals("h1", (usageRef as LiteralString).value)
    }

    @Test
    fun `all usageHint values are valid`() {
        val usageHints = listOf("h1", "h2", "h3", "h4", "h5", "body", "caption")

        usageHints.forEach { hint ->
            val jsonStr = """{"usageHint": "$hint"}"""
            val data = json.decodeFromString<JsonObject>(jsonStr)
            val ref = DataReferenceParser.parseString(data["usageHint"])

            assertNotNull(ref, "usageHint '$hint' should parse")
            assertEquals(hint, (ref as LiteralString).value)
        }
    }

    @Test
    fun `missing usageHint returns null`() {
        val jsonStr = """
            {
                "text": {"literalString": "No hint"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val usageRef = DataReferenceParser.parseString(data["usageHint"])

        assertNull(usageRef)
    }

    @Test
    fun `text with markdown content parses correctly`() {
        val jsonStr = """
            {
                "text": {"literalString": "This is **bold** and _italic_ text"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val textRef = DataReferenceParser.parseString(data["text"])

        assertNotNull(textRef)
        assertEquals("This is **bold** and _italic_ text", (textRef as LiteralString).value)
    }
}
