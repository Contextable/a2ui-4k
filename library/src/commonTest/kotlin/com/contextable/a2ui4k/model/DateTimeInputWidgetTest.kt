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
import kotlin.test.assertTrue

/**
 * Tests for DateTimeInput widget JSON parsing.
 *
 * A2UI Spec properties (v0.8):
 * - value (required): Path binding for the date/time value
 * - enableDate (optional): Boolean to enable date picker
 * - enableTime (optional): Boolean to enable time picker
 */
class DateTimeInputWidgetTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parseString extracts value path binding`() {
        val jsonStr = """
            {
                "value": {"path": "/appointment/dateTime"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val valueRef = DataReferenceParser.parseString(data["value"])

        assertNotNull(valueRef)
        assertEquals("/appointment/dateTime", (valueRef as PathString).path)
    }

    @Test
    fun `parseBoolean extracts enableDate true`() {
        val jsonStr = """
            {
                "value": {"path": "/date"},
                "enableDate": true
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val enableDateRef = DataReferenceParser.parseBoolean(data["enableDate"])

        assertNotNull(enableDateRef)
        assertTrue((enableDateRef as LiteralBoolean).value)
    }

    @Test
    fun `parseBoolean extracts enableTime true`() {
        val jsonStr = """
            {
                "value": {"path": "/time"},
                "enableTime": true
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val enableTimeRef = DataReferenceParser.parseBoolean(data["enableTime"])

        assertNotNull(enableTimeRef)
        assertTrue((enableTimeRef as LiteralBoolean).value)
    }

    @Test
    fun `parseBoolean extracts false values`() {
        val jsonStr = """
            {
                "value": {"path": "/date"},
                "enableDate": true,
                "enableTime": false
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val enableDateRef = DataReferenceParser.parseBoolean(data["enableDate"])
        assertTrue((enableDateRef as LiteralBoolean).value)

        val enableTimeRef = DataReferenceParser.parseBoolean(data["enableTime"])
        assertEquals(false, (enableTimeRef as LiteralBoolean).value)
    }

    @Test
    fun `complete DateTimeInput with all properties`() {
        val jsonStr = """
            {
                "value": {"path": "/meeting/startTime"},
                "enableDate": true,
                "enableTime": true
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val valueRef = DataReferenceParser.parseString(data["value"])
        assertEquals("/meeting/startTime", (valueRef as PathString).path)

        val enableDateRef = DataReferenceParser.parseBoolean(data["enableDate"])
        assertTrue((enableDateRef as LiteralBoolean).value)

        val enableTimeRef = DataReferenceParser.parseBoolean(data["enableTime"])
        assertTrue((enableTimeRef as LiteralBoolean).value)
    }

    @Test
    fun `missing optional properties return null`() {
        val jsonStr = """
            {
                "value": {"path": "/date"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        assertNull(DataReferenceParser.parseBoolean(data["enableDate"]))
        assertNull(DataReferenceParser.parseBoolean(data["enableTime"]))
    }

    @Test
    fun `date only configuration`() {
        val jsonStr = """
            {
                "value": {"path": "/birthDate"},
                "enableDate": true,
                "enableTime": false
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val enableDateRef = DataReferenceParser.parseBoolean(data["enableDate"])
        assertTrue((enableDateRef as LiteralBoolean).value)

        val enableTimeRef = DataReferenceParser.parseBoolean(data["enableTime"])
        assertEquals(false, (enableTimeRef as LiteralBoolean).value)
    }

    @Test
    fun `time only configuration`() {
        val jsonStr = """
            {
                "value": {"path": "/alarmTime"},
                "enableDate": false,
                "enableTime": true
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val enableDateRef = DataReferenceParser.parseBoolean(data["enableDate"])
        assertEquals(false, (enableDateRef as LiteralBoolean).value)

        val enableTimeRef = DataReferenceParser.parseBoolean(data["enableTime"])
        assertTrue((enableTimeRef as LiteralBoolean).value)
    }
}
