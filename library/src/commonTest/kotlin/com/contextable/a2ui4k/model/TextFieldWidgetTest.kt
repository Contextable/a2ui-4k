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
 * Tests for TextField widget JSON parsing.
 *
 * A2UI Spec properties (v0.9):
 * - value (required): Path binding for the text value (renamed from text)
 * - label (optional): Label text as plain string or path binding
 * - variant (optional): date, longText, number, shortText, obscured (renamed from textFieldType)
 * - validationRegexp (optional): Regex pattern for validation
 */
class TextFieldWidgetTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parseString extracts value path binding`() {
        val jsonStr = """
            {
                "value": {"path": "/form/username"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val valueRef = DataReferenceParser.parseString(data["value"])

        assertNotNull(valueRef)
        assertEquals("/form/username", (valueRef as PathString).path)
    }

    @Test
    fun `parseString extracts label literal`() {
        val jsonStr = """
            {
                "value": {"path": "/name"},
                "label": "Enter your name"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val labelRef = DataReferenceParser.parseString(data["label"])

        assertNotNull(labelRef)
        assertEquals("Enter your name", (labelRef as LiteralString).value)
    }

    @Test
    fun `all variant values are valid`() {
        val types = listOf("date", "longText", "number", "shortText", "obscured")

        types.forEach { type ->
            val jsonStr = """{"variant": "$type"}"""
            val data = json.decodeFromString<JsonObject>(jsonStr)
            val ref = DataReferenceParser.parseString(data["variant"])

            assertNotNull(ref, "variant '$type' should parse")
            assertEquals(type, (ref as LiteralString).value)
        }
    }

    @Test
    fun `parseString extracts validationRegexp`() {
        val jsonStr = """
            {
                "value": {"path": "/email"},
                "validationRegexp": "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val regexRef = DataReferenceParser.parseString(data["validationRegexp"])

        assertNotNull(regexRef)
    }

    @Test
    fun `missing optional properties return null`() {
        val jsonStr = """
            {
                "value": {"path": "/value"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        assertNull(DataReferenceParser.parseString(data["label"]))
        assertNull(DataReferenceParser.parseString(data["variant"]))
        assertNull(DataReferenceParser.parseString(data["validationRegexp"]))
    }

    @Test
    fun `complete TextField with all properties`() {
        val jsonStr = """
            {
                "value": {"path": "/form/password"},
                "label": "Password",
                "variant": "obscured"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val valueRef = DataReferenceParser.parseString(data["value"])
        assertEquals("/form/password", (valueRef as PathString).path)

        val labelRef = DataReferenceParser.parseString(data["label"])
        assertEquals("Password", (labelRef as LiteralString).value)

        val variantRef = DataReferenceParser.parseString(data["variant"])
        assertEquals("obscured", (variantRef as LiteralString).value)
    }
}
