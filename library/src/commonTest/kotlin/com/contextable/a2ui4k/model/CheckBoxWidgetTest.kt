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
 * Tests for CheckBox widget JSON parsing.
 *
 * A2UI Spec properties (v0.9):
 * - value (required): Path binding for boolean state
 * - label (optional): Label text as plain string or path binding
 */
class CheckBoxWidgetTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parseString extracts value path binding`() {
        val jsonStr = """
            {
                "value": {"path": "/settings/notifications"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val valueRef = DataReferenceParser.parseString(data["value"])

        assertNotNull(valueRef)
        assertEquals("/settings/notifications", (valueRef as PathString).path)
    }

    @Test
    fun `parseString extracts label literal`() {
        val jsonStr = """
            {
                "value": {"path": "/agreed"},
                "label": "I agree to the terms"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val labelRef = DataReferenceParser.parseString(data["label"])

        assertNotNull(labelRef)
        assertEquals("I agree to the terms", (labelRef as LiteralString).value)
    }

    @Test
    fun `parseString extracts label path binding`() {
        val jsonStr = """
            {
                "value": {"path": "/selected"},
                "label": {"path": "/options/0/name"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val labelRef = DataReferenceParser.parseString(data["label"])

        assertNotNull(labelRef)
        assertEquals("/options/0/name", (labelRef as PathString).path)
    }

    @Test
    fun `complete CheckBox with value and label`() {
        val jsonStr = """
            {
                "value": {"path": "/form/newsletter"},
                "label": "Subscribe to newsletter"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val valueRef = DataReferenceParser.parseString(data["value"])
        assertEquals("/form/newsletter", (valueRef as PathString).path)

        val labelRef = DataReferenceParser.parseString(data["label"])
        assertEquals("Subscribe to newsletter", (labelRef as LiteralString).value)
    }
}
