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
 * Tests for Icon widget JSON parsing.
 *
 * A2UI Spec properties (v0.9):
 * - name (required): Icon name as plain string or path binding
 */
class IconWidgetTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parseString extracts name literal`() {
        val jsonStr = """
            {
                "name": "home"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val nameRef = DataReferenceParser.parseString(data["name"])

        assertNotNull(nameRef)
        assertEquals("home", (nameRef as LiteralString).value)
    }

    @Test
    fun `parseString extracts name path binding`() {
        val jsonStr = """
            {
                "name": {"path": "/ui/selectedIcon"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val nameRef = DataReferenceParser.parseString(data["name"])

        assertNotNull(nameRef)
        assertEquals("/ui/selectedIcon", (nameRef as PathString).path)
    }

    @Test
    fun `common icon names parse correctly`() {
        val iconNames = listOf(
            "home", "menu", "arrow_back", "close", "add",
            "search", "settings", "check", "favorite", "star"
        )

        iconNames.forEach { iconName ->
            val jsonStr = """{"name": "$iconName"}"""
            val data = json.decodeFromString<JsonObject>(jsonStr)
            val nameRef = DataReferenceParser.parseString(data["name"])

            assertNotNull(nameRef, "Icon '$iconName' should parse")
            assertEquals(iconName, (nameRef as LiteralString).value)
        }
    }

    @Test
    fun `icon with camelCase name`() {
        val jsonStr = """
            {
                "name": "playArrow"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val nameRef = DataReferenceParser.parseString(data["name"])

        assertNotNull(nameRef)
        assertEquals("playArrow", (nameRef as LiteralString).value)
    }

    @Test
    fun `missing name returns null`() {
        val jsonStr = """{}"""
        val data = json.decodeFromString<JsonObject>(jsonStr)

        assertNull(DataReferenceParser.parseString(data["name"]))
    }
}
