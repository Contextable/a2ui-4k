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

package com.contextable.a2ui4k.extension

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [A2UIExtensionParams] and [A2UIExtension] constants.
 */
class A2UIExtensionParamsTest {

    private val json = Json { ignoreUnknownKeys = true }

    // --- A2UIExtensionParams defaults ---

    @Test
    fun `A2UIExtensionParams defaults have null catalogIds and false acceptsInlineCatalogs`() {
        val params = A2UIExtensionParams()

        assertNull(params.supportedCatalogIds)
        assertFalse(params.acceptsInlineCatalogs)
    }

    @Test
    fun `A2UIExtensionParams with catalogs and inline support`() {
        val params = A2UIExtensionParams(
            supportedCatalogIds = listOf(
                A2UIExtension.STANDARD_CATALOG_URI,
                "https://custom.example.com/catalog.json"
            ),
            acceptsInlineCatalogs = true
        )

        assertEquals(2, params.supportedCatalogIds!!.size)
        assertEquals(A2UIExtension.STANDARD_CATALOG_URI, params.supportedCatalogIds!![0])
        assertTrue(params.acceptsInlineCatalogs)
    }

    // --- Serialization ---

    @Test
    fun `A2UIExtensionParams serializes correctly`() {
        val params = A2UIExtensionParams(
            supportedCatalogIds = listOf(A2UIExtension.STANDARD_CATALOG_URI),
            acceptsInlineCatalogs = true
        )

        val serialized = json.encodeToString(params)

        assertTrue(serialized.contains("supportedCatalogIds"))
        assertTrue(serialized.contains("acceptsInlineCatalogs"))
        assertTrue(serialized.contains("true"))
        assertTrue(serialized.contains("standard_catalog.json"))
    }

    @Test
    fun `A2UIExtensionParams deserializes correctly`() {
        val jsonString = """
            {
                "supportedCatalogIds": [
                    "https://example.com/cat1.json"
                ],
                "acceptsInlineCatalogs": true
            }
        """.trimIndent()

        val params = json.decodeFromString<A2UIExtensionParams>(jsonString)

        assertEquals(1, params.supportedCatalogIds!!.size)
        assertEquals("https://example.com/cat1.json", params.supportedCatalogIds!![0])
        assertTrue(params.acceptsInlineCatalogs)
    }

    @Test
    fun `A2UIExtensionParams deserializes with defaults`() {
        val jsonString = """{}"""

        val params = json.decodeFromString<A2UIExtensionParams>(jsonString)

        assertNull(params.supportedCatalogIds)
        assertFalse(params.acceptsInlineCatalogs)
    }

    @Test
    fun `A2UIExtensionParams round-trips through serialization`() {
        val original = A2UIExtensionParams(
            supportedCatalogIds = listOf("catalog-a", "catalog-b"),
            acceptsInlineCatalogs = false
        )

        val serialized = json.encodeToString(original)
        val deserialized = json.decodeFromString<A2UIExtensionParams>(serialized)

        assertEquals(original, deserialized)
    }

    // --- A2UIExtension constants ---

    @Test
    fun `A2UIExtension URI_V09 is correct`() {
        assertEquals("https://a2ui.org/a2a-extension/a2ui/v0.9", A2UIExtension.URI_V09)
    }

    @Test
    fun `A2UIExtension PROTOCOL_VERSION is v0_9`() {
        assertEquals("v0.9", A2UIExtension.PROTOCOL_VERSION)
    }

    @Test
    fun `A2UIExtension MIME_TYPE is correct`() {
        assertEquals("application/json+a2ui", A2UIExtension.MIME_TYPE)
    }

    @Test
    fun `A2UIExtension STANDARD_CATALOG_URI contains expected path`() {
        assertTrue(A2UIExtension.STANDARD_CATALOG_URI.contains("standard_catalog.json"))
        assertTrue(A2UIExtension.STANDARD_CATALOG_URI.contains("v0_9"))
    }
}
