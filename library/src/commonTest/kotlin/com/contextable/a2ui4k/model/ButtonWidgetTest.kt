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
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for Button JSON parsing.
 *
 * A2UI v0.9 Button.variant values are `default` | `primary` | `borderless`.
 * Action format: `{"event": {"name", "context"}}` or `{"functionCall": {...}}`.
 */
class ButtonWidgetTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parseString extracts child component reference`() {
        val data = json.decodeFromString<JsonObject>(
            """
            {
                "child": "book-now-text",
                "variant": "primary",
                "action": {"event": {"name": "book_restaurant"}}
            }
            """.trimIndent()
        )
        val childRef = DataReferenceParser.parseString(data["child"])

        assertNotNull(childRef)
        assertEquals("book-now-text", (childRef as LiteralString).value)
    }

    @Test
    fun `parseString extracts variant primary`() {
        val data = json.decodeFromString<JsonObject>(
            """{"child":"t","variant":"primary"}"""
        )
        val variantRef = DataReferenceParser.parseString(data["variant"])
        assertEquals("primary", (variantRef as LiteralString).value)
    }

    @Test
    fun `parseString extracts variant default`() {
        val data = json.decodeFromString<JsonObject>(
            """{"child":"t","variant":"default"}"""
        )
        val variantRef = DataReferenceParser.parseString(data["variant"])
        assertEquals("default", (variantRef as LiteralString).value)
    }

    @Test
    fun `parseString extracts variant borderless`() {
        val data = json.decodeFromString<JsonObject>(
            """{"child":"t","variant":"borderless"}"""
        )
        val variantRef = DataReferenceParser.parseString(data["variant"])
        assertEquals("borderless", (variantRef as LiteralString).value)
    }

    @Test
    fun `parseString returns null when variant is missing`() {
        val data = json.decodeFromString<JsonObject>("""{"child":"t"}""")
        assertNull(DataReferenceParser.parseString(data["variant"]))
    }

    @Test
    fun `action event schema parses name correctly`() {
        val data = json.decodeFromString<JsonObject>(
            """
            {
                "child": "submit-text",
                "action": {"event": {"name": "submit_form", "context": {"formId": "contact-form"}}}
            }
            """.trimIndent()
        )
        val action = data["action"] as? JsonObject
        assertNotNull(action)
        val event = action["event"] as? JsonObject
        assertNotNull(event)
        assertEquals("submit_form", (event["name"] as JsonPrimitive).content)
    }

    @Test
    fun `action event context with literal values parses correctly`() {
        val data = json.decodeFromString<JsonObject>(
            """
            {
                "child": "book-text",
                "action": {
                    "event": {
                        "name": "book_restaurant",
                        "context": {"restaurantId": "rest-123", "guests": 4, "confirmed": true}
                    }
                }
            }
            """.trimIndent()
        )
        val context = ((data["action"] as JsonObject)["event"] as JsonObject)["context"] as JsonObject
        assertEquals(3, context.size)
        assertEquals("rest-123", (context["restaurantId"] as JsonPrimitive).content)
    }

    @Test
    fun `action event context with path bindings parses correctly`() {
        val data = json.decodeFromString<JsonObject>(
            """
            {
                "child": "book-text",
                "variant": "primary",
                "action": {
                    "event": {
                        "name": "book_restaurant",
                        "context": {
                            "restaurantName": {"path": "name"},
                            "imageUrl": {"path": "imageUrl"}
                        }
                    }
                }
            }
            """.trimIndent()
        )
        val context = ((data["action"] as JsonObject)["event"] as JsonObject)["context"] as JsonObject
        val nameValue = context["restaurantName"] as JsonObject
        assertEquals("name", (nameValue["path"] as JsonPrimitive).content)
    }

    @Test
    fun `action functionCall variant parses`() {
        val data = json.decodeFromString<JsonObject>(
            """
            {
                "child": "link-text",
                "action": {"functionCall": {"call": "openUrl", "args": {"url": "https://example.com"}}}
            }
            """.trimIndent()
        )
        val action = data["action"] as JsonObject
        val functionCall = action["functionCall"] as JsonObject
        assertEquals("openUrl", (functionCall["call"] as JsonPrimitive).content)
    }

    @Test
    fun `checks array parses into CheckRules`() {
        val data = json.decodeFromString<JsonObject>(
            """
            {
                "child": "submit-text",
                "checks": [
                    {"condition": {"call": "required", "args": {"value": {"path": "/email"}}}, "message": "Email required"}
                ]
            }
            """.trimIndent()
        )
        val rules = CheckRule.fromJsonArray(data["checks"])
        assertEquals(1, rules.size)
        assertEquals("Email required", rules[0].message)
    }
}
