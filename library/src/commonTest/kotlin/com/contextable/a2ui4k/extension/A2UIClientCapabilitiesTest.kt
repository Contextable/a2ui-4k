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
import kotlin.test.assertTrue

/**
 * Tests for A2UIClientCapabilities and related helper functions.
 */
class A2UIClientCapabilitiesTest {

    private val json = Json { prettyPrint = true }

    @Test
    fun `A2UIClientCapabilities creates with catalog list`() {
        val capabilities = A2UIClientCapabilities(
            supportedCatalogIds = listOf("catalog1", "catalog2")
        )

        assertEquals(2, capabilities.supportedCatalogIds.size)
        assertEquals("catalog1", capabilities.supportedCatalogIds[0])
        assertEquals("catalog2", capabilities.supportedCatalogIds[1])
    }

    @Test
    fun `A2UIClientCapabilities serializes correctly`() {
        val capabilities = A2UIClientCapabilities(
            supportedCatalogIds = listOf(A2UIExtension.STANDARD_CATALOG_URI)
        )

        val serialized = json.encodeToString(capabilities)

        assertTrue(serialized.contains("supportedCatalogIds"))
        assertTrue(serialized.contains(A2UIExtension.STANDARD_CATALOG_URI))
    }

    @Test
    fun `A2UIClientCapabilities deserializes correctly`() {
        val jsonString = """
            {
                "supportedCatalogIds": [
                    "https://example.com/catalog1",
                    "https://example.com/catalog2"
                ]
            }
        """.trimIndent()

        val capabilities = Json.decodeFromString<A2UIClientCapabilities>(jsonString)

        assertEquals(2, capabilities.supportedCatalogIds.size)
        assertEquals("https://example.com/catalog1", capabilities.supportedCatalogIds[0])
        assertEquals("https://example.com/catalog2", capabilities.supportedCatalogIds[1])
    }

    @Test
    fun `a2uiStandardClientCapabilities creates with standard catalog`() {
        val capabilities = a2uiStandardClientCapabilities()

        assertEquals(1, capabilities.supportedCatalogIds.size)
        assertEquals(A2UIExtension.STANDARD_CATALOG_URI, capabilities.supportedCatalogIds[0])
    }

    @Test
    fun `a2uiClientCapabilities creates with vararg catalogs`() {
        val capabilities = a2uiClientCapabilities(
            A2UIExtension.STANDARD_CATALOG_URI,
            "https://my-company.com/custom_catalog.json"
        )

        assertEquals(2, capabilities.supportedCatalogIds.size)
        assertEquals(A2UIExtension.STANDARD_CATALOG_URI, capabilities.supportedCatalogIds[0])
        assertEquals("https://my-company.com/custom_catalog.json", capabilities.supportedCatalogIds[1])
    }

    @Test
    fun `a2uiClientCapabilities creates empty list with no args`() {
        val capabilities = a2uiClientCapabilities()

        assertTrue(capabilities.supportedCatalogIds.isEmpty())
    }

    @Test
    fun `A2UIExtension constants are correct`() {
        assertEquals(
            "https://a2ui.org/a2a-extension/a2ui/v0.8",
            A2UIExtension.URI_V08
        )
        assertEquals(
            "https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json",
            A2UIExtension.STANDARD_CATALOG_URI
        )
        assertEquals(
            "application/json+a2ui",
            A2UIExtension.MIME_TYPE
        )
    }

    @Test
    fun `serialized JSON matches expected structure`() {
        val capabilities = a2uiStandardClientCapabilities()
        val serialized = json.encodeToString(capabilities)

        // Should produce JSON that can be included in message metadata
        assertTrue(serialized.contains("\"supportedCatalogIds\""))
        assertTrue(serialized.contains("standard_catalog_definition.json"))
    }
}
