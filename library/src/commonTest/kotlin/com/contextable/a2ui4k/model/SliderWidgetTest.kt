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
 * Tests for Slider widget JSON parsing.
 *
 * A2UI Spec properties (v0.9):
 * - value (required): Path binding for numeric value
 * - min (optional): Minimum value (renamed from minValue)
 * - max (optional): Maximum value (renamed from maxValue)
 */
class SliderWidgetTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parseString extracts value path binding`() {
        val jsonStr = """
            {
                "value": {"path": "/audio/volume"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val valueRef = DataReferenceParser.parseString(data["value"])

        assertNotNull(valueRef)
        assertEquals("/audio/volume", (valueRef as PathString).path)
    }

    @Test
    fun `parseNumber extracts min`() {
        val jsonStr = """
            {
                "value": {"path": "/volume"},
                "min": 0
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val minRef = DataReferenceParser.parseNumber(data["min"])

        assertNotNull(minRef)
        assertEquals(0.0, (minRef as LiteralNumber).value)
    }

    @Test
    fun `parseNumber extracts max`() {
        val jsonStr = """
            {
                "value": {"path": "/volume"},
                "max": 100
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val maxRef = DataReferenceParser.parseNumber(data["max"])

        assertNotNull(maxRef)
        assertEquals(100.0, (maxRef as LiteralNumber).value)
    }

    @Test
    fun `parseNumber handles decimal values`() {
        val jsonStr = """
            {
                "min": 0.5,
                "max": 1.5
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val minRef = DataReferenceParser.parseNumber(data["min"])
        assertEquals(0.5, (minRef as LiteralNumber).value)

        val maxRef = DataReferenceParser.parseNumber(data["max"])
        assertEquals(1.5, (maxRef as LiteralNumber).value)
    }

    @Test
    fun `complete Slider with all properties`() {
        val jsonStr = """
            {
                "value": {"path": "/brightness"},
                "min": 0,
                "max": 100
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val valueRef = DataReferenceParser.parseString(data["value"])
        assertEquals("/brightness", (valueRef as PathString).path)

        val minRef = DataReferenceParser.parseNumber(data["min"])
        assertEquals(0.0, (minRef as LiteralNumber).value)

        val maxRef = DataReferenceParser.parseNumber(data["max"])
        assertEquals(100.0, (maxRef as LiteralNumber).value)
    }

    @Test
    fun `missing optional properties return null`() {
        val jsonStr = """
            {
                "value": {"path": "/value"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        assertNull(DataReferenceParser.parseNumber(data["min"]))
        assertNull(DataReferenceParser.parseNumber(data["max"]))
    }

    @Test
    fun `negative min is valid`() {
        val jsonStr = """
            {
                "value": {"path": "/temperature"},
                "min": -50,
                "max": 50
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val minRef = DataReferenceParser.parseNumber(data["min"])
        assertEquals(-50.0, (minRef as LiteralNumber).value)
    }
}
