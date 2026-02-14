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
 * Tests for Divider widget JSON parsing.
 *
 * A2UI Spec properties (v0.9):
 * - axis (optional): "horizontal" or "vertical" (default: horizontal)
 */
class DividerWidgetTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parseString extracts axis horizontal`() {
        val jsonStr = """
            {
                "axis": "horizontal"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val axisRef = DataReferenceParser.parseString(data["axis"])

        assertNotNull(axisRef)
        assertEquals("horizontal", (axisRef as LiteralString).value)
    }

    @Test
    fun `parseString extracts axis vertical`() {
        val jsonStr = """
            {
                "axis": "vertical"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val axisRef = DataReferenceParser.parseString(data["axis"])

        assertNotNull(axisRef)
        assertEquals("vertical", (axisRef as LiteralString).value)
    }

    @Test
    fun `parseString extracts axis path binding`() {
        val jsonStr = """
            {
                "axis": {"path": "/layout/dividerDirection"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val axisRef = DataReferenceParser.parseString(data["axis"])

        assertNotNull(axisRef)
        assertEquals("/layout/dividerDirection", (axisRef as PathString).path)
    }

    @Test
    fun `missing axis returns null for default behavior`() {
        val jsonStr = """{}"""
        val data = json.decodeFromString<JsonObject>(jsonStr)

        assertNull(DataReferenceParser.parseString(data["axis"]))
    }

    @Test
    fun `empty Divider with no properties`() {
        val jsonStr = """{}"""
        val data = json.decodeFromString<JsonObject>(jsonStr)

        // Divider works with no properties (defaults to horizontal)
        assertNull(DataReferenceParser.parseString(data["axis"]))
    }
}
