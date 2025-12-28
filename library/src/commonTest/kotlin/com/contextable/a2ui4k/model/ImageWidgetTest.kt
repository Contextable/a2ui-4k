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
 * Tests for Image widget JSON parsing.
 *
 * A2UI Spec properties:
 * - url (required): Image URL as literalString or path
 * - fit (optional): contain, cover, fill, none, scale-down
 * - usageHint (optional): icon, avatar, smallFeature, mediumFeature, largeFeature, header
 */
class ImageWidgetTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parseString extracts url literalString`() {
        val jsonStr = """
            {
                "url": {"literalString": "https://example.com/image.jpg"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val urlRef = DataReferenceParser.parseString(data["url"])

        assertNotNull(urlRef)
        assertEquals("https://example.com/image.jpg", (urlRef as LiteralString).value)
    }

    @Test
    fun `parseString extracts url path binding`() {
        val jsonStr = """
            {
                "url": {"path": "/product/imageUrl"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val urlRef = DataReferenceParser.parseString(data["url"])

        assertNotNull(urlRef)
        assertEquals("/product/imageUrl", (urlRef as PathString).path)
    }

    @Test
    fun `all fit values are valid`() {
        val fitValues = listOf("contain", "cover", "fill", "none", "scale-down")

        fitValues.forEach { fit ->
            val jsonStr = """{"fit": "$fit"}"""
            val data = json.decodeFromString<JsonObject>(jsonStr)
            val ref = DataReferenceParser.parseString(data["fit"])

            assertNotNull(ref, "fit '$fit' should parse")
            assertEquals(fit, (ref as LiteralString).value)
        }
    }

    @Test
    fun `all usageHint values are valid`() {
        val hints = listOf("icon", "avatar", "smallFeature", "mediumFeature", "largeFeature", "header")

        hints.forEach { hint ->
            val jsonStr = """{"usageHint": "$hint"}"""
            val data = json.decodeFromString<JsonObject>(jsonStr)
            val ref = DataReferenceParser.parseString(data["usageHint"])

            assertNotNull(ref, "usageHint '$hint' should parse")
            assertEquals(hint, (ref as LiteralString).value)
        }
    }

    @Test
    fun `complete Image with all properties`() {
        val jsonStr = """
            {
                "url": {"path": "/user/avatar"},
                "fit": "cover",
                "usageHint": "avatar"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val urlRef = DataReferenceParser.parseString(data["url"])
        assertEquals("/user/avatar", (urlRef as PathString).path)

        val fitRef = DataReferenceParser.parseString(data["fit"])
        assertEquals("cover", (fitRef as LiteralString).value)

        val hintRef = DataReferenceParser.parseString(data["usageHint"])
        assertEquals("avatar", (hintRef as LiteralString).value)
    }

    @Test
    fun `missing optional properties return null`() {
        val jsonStr = """
            {
                "url": {"literalString": "https://example.com/img.png"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        assertNull(DataReferenceParser.parseString(data["fit"]))
        assertNull(DataReferenceParser.parseString(data["usageHint"]))
    }
}
