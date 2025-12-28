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

package com.contextable.a2ui4k.util

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test

/**
 * Tests for PropertyValidation utility.
 *
 * PropertyValidation helps ensure A2UI protocol compliance by warning about
 * unexpected properties in widget configurations.
 */
class PropertyValidationTest {

    @Test
    fun `warnUnexpectedProperties with all expected properties`() {
        val data = JsonObject(mapOf(
            "text" to JsonPrimitive("Hello"),
            "usageHint" to JsonPrimitive("h1")
        ))
        val expected = setOf("text", "usageHint")

        // Should not warn - all properties are expected
        PropertyValidation.warnUnexpectedProperties("Text", data, expected)
    }

    @Test
    fun `warnUnexpectedProperties with unexpected property`() {
        val data = JsonObject(mapOf(
            "text" to JsonPrimitive("Hello"),
            "unknownProp" to JsonPrimitive("value")
        ))
        val expected = setOf("text", "usageHint")

        // Should warn about "unknownProp"
        PropertyValidation.warnUnexpectedProperties("Text", data, expected)
    }

    @Test
    fun `warnUnexpectedProperties with multiple unexpected properties`() {
        val data = JsonObject(mapOf(
            "text" to JsonPrimitive("Hello"),
            "foo" to JsonPrimitive("bar"),
            "baz" to JsonPrimitive("qux")
        ))
        val expected = setOf("text")

        // Should warn about "foo" and "baz"
        PropertyValidation.warnUnexpectedProperties("Text", data, expected)
    }

    @Test
    fun `warnUnexpectedProperties with empty data`() {
        val data = JsonObject(emptyMap())
        val expected = setOf("text", "usageHint")

        // Should not warn - no unexpected properties
        PropertyValidation.warnUnexpectedProperties("Text", data, expected)
    }

    @Test
    fun `warnUnexpectedProperties with empty expected set`() {
        val data = JsonObject(mapOf(
            "anyProp" to JsonPrimitive("value")
        ))
        val expected = emptySet<String>()

        // Should warn about all properties
        PropertyValidation.warnUnexpectedProperties("Custom", data, expected)
    }

    @Test
    fun `warnUnexpectedProperties with Button widget`() {
        val data = JsonObject(mapOf(
            "child" to JsonPrimitive("button-text"),
            "action" to JsonObject(emptyMap()),
            "invalidProp" to JsonPrimitive("should warn")
        ))
        val expected = setOf("child", "action")

        PropertyValidation.warnUnexpectedProperties("Button", data, expected)
    }

    @Test
    fun `warnUnexpectedProperties with Image widget`() {
        val data = JsonObject(mapOf(
            "url" to JsonObject(mapOf("literalString" to JsonPrimitive("https://example.com/img.jpg"))),
            "fit" to JsonPrimitive("cover"),
            "usageHint" to JsonPrimitive("avatar"),
            "altText" to JsonPrimitive("not in spec")  // Should warn
        ))
        val expected = setOf("url", "fit", "usageHint")

        PropertyValidation.warnUnexpectedProperties("Image", data, expected)
    }

    @Test
    fun `warnUnexpectedProperties with Row widget`() {
        val data = JsonObject(mapOf(
            "children" to JsonObject(emptyMap()),
            "alignment" to JsonPrimitive("center"),
            "gap" to JsonPrimitive(8)  // Not in A2UI spec - should warn
        ))
        val expected = setOf("children", "alignment")

        PropertyValidation.warnUnexpectedProperties("Row", data, expected)
    }

    @Test
    fun `warnUnexpectedProperties case sensitivity`() {
        val data = JsonObject(mapOf(
            "Text" to JsonPrimitive("uppercase"),  // Wrong case
            "text" to JsonPrimitive("correct")
        ))
        val expected = setOf("text")

        // Should warn about "Text" (uppercase) as it's different from expected "text"
        PropertyValidation.warnUnexpectedProperties("Widget", data, expected)
    }
}
