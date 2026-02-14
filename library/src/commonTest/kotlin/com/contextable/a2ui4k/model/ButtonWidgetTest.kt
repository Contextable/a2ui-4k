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
 * Tests for ButtonWidget JSON parsing.
 *
 * The Button widget in A2UI v0.9 uses a `child` property that references
 * another component (typically Text) rather than a direct label.
 * The `variant` property accepts values: "filled", "outlined", "text", "elevated", "tonal".
 * Action format uses `{"event": {"name": "...", "context": {...}}}`.
 */
class ButtonWidgetTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parseString extracts child component reference`() {
        val jsonStr = """
            {
                "child": "book-now-text",
                "variant": "filled",
                "action": {
                    "event": {"name": "book_restaurant"}
                }
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val childRef = DataReferenceParser.parseString(data["child"])

        assertNotNull(childRef)
        assertEquals("book-now-text", (childRef as LiteralString).value)
    }

    @Test
    fun `parseString extracts variant property`() {
        val jsonStr = """
            {
                "child": "button-text",
                "variant": "filled"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val variantRef = DataReferenceParser.parseString(data["variant"])

        assertNotNull(variantRef)
        assertEquals("filled", (variantRef as LiteralString).value)
    }

    @Test
    fun `parseString returns null when variant is missing`() {
        val jsonStr = """
            {
                "child": "button-text"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val variantRef = DataReferenceParser.parseString(data["variant"])

        assertNull(variantRef)
    }

    @Test
    fun `action event schema parses name correctly`() {
        val jsonStr = """
            {
                "child": "submit-text",
                "action": {
                    "event": {
                        "name": "submit_form",
                        "context": {"formId": "contact-form"}
                    }
                }
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val action = data["action"] as? JsonObject

        assertNotNull(action)
        val event = action["event"] as? JsonObject
        assertNotNull(event)
        assertEquals("submit_form", event["name"]?.let {
            (it as? kotlinx.serialization.json.JsonPrimitive)?.content
        })
    }

    @Test
    fun `action event context with literal values parses correctly`() {
        val jsonStr = """
            {
                "child": "book-text",
                "action": {
                    "event": {
                        "name": "book_restaurant",
                        "context": {
                            "restaurantId": "rest-123",
                            "guests": 4,
                            "confirmed": true
                        }
                    }
                }
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val action = data["action"] as? JsonObject
        val event = action?.get("event") as? JsonObject
        val context = event?.get("context") as? JsonObject

        assertNotNull(context)
        assertEquals(3, context.size)

        assertEquals("rest-123", context["restaurantId"]?.let {
            (it as? kotlinx.serialization.json.JsonPrimitive)?.content
        })
    }

    @Test
    fun `action event context with path bindings parses correctly`() {
        val jsonStr = """
            {
                "child": "book-now-text",
                "variant": "filled",
                "action": {
                    "event": {
                        "name": "book_restaurant",
                        "context": {
                            "restaurantName": {"path": "name"},
                            "imageUrl": {"path": "imageUrl"},
                            "address": {"path": "address"}
                        }
                    }
                }
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val action = data["action"] as? JsonObject
        val event = action?.get("event") as? JsonObject
        val context = event?.get("context") as? JsonObject

        assertNotNull(context)
        assertEquals(3, context.size)

        // Verify path binding structure
        val nameValue = context["restaurantName"] as? JsonObject
        assertNotNull(nameValue)
        assertEquals("name", nameValue["path"]?.let {
            (it as? kotlinx.serialization.json.JsonPrimitive)?.content
        })
    }

    @Test
    fun `variant outlined is supported`() {
        val jsonStr = """
            {
                "child": "button-text",
                "variant": "outlined"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val variantRef = DataReferenceParser.parseString(data["variant"])

        assertNotNull(variantRef)
        assertEquals("outlined", (variantRef as LiteralString).value)
    }

    @Test
    fun `variant text is supported`() {
        val jsonStr = """
            {
                "child": "button-text",
                "variant": "text"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val variantRef = DataReferenceParser.parseString(data["variant"])

        assertNotNull(variantRef)
        assertEquals("text", (variantRef as LiteralString).value)
    }
}
