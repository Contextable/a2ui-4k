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
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for Tabs widget JSON parsing.
 *
 * A2UI Spec properties (v0.9):
 * - tabs (required): Array of tab definitions with title and child (renamed from tabItems)
 */
class TabsWidgetTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parseString extracts tab title literal`() {
        val jsonStr = """
            {
                "tabs": [
                    {"title": "Home", "child": "home-content"}
                ]
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val tabs = data["tabs"]?.jsonArray
        assertNotNull(tabs)
        assertEquals(1, tabs.size)

        val firstTab = tabs[0].jsonObject
        val titleRef = DataReferenceParser.parseString(firstTab["title"])

        assertNotNull(titleRef)
        assertEquals("Home", (titleRef as LiteralString).value)
    }

    @Test
    fun `parseString extracts tab title path binding`() {
        val jsonStr = """
            {
                "tabs": [
                    {"title": {"path": "/tabs/0/label"}, "child": "tab-content"}
                ]
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val tabs = data["tabs"]?.jsonArray
        assertNotNull(tabs)

        val firstTab = tabs[0].jsonObject
        val titleRef = DataReferenceParser.parseString(firstTab["title"])

        assertNotNull(titleRef)
        assertEquals("/tabs/0/label", (titleRef as PathString).path)
    }

    @Test
    fun `parseComponentRef extracts tab child`() {
        val jsonStr = """
            {
                "tabs": [
                    {"title": "Settings", "child": "settings-panel"}
                ]
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val tabs = data["tabs"]?.jsonArray
        assertNotNull(tabs)

        val firstTab = tabs[0].jsonObject
        val childRef = DataReferenceParser.parseComponentRef(firstTab["child"])

        assertNotNull(childRef)
        assertEquals("settings-panel", childRef.componentId)
    }

    @Test
    fun `multiple tabs with different titles`() {
        val jsonStr = """
            {
                "tabs": [
                    {"title": "Tab 1", "child": "content-1"},
                    {"title": "Tab 2", "child": "content-2"},
                    {"title": "Tab 3", "child": "content-3"}
                ]
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val tabs = data["tabs"]?.jsonArray
        assertNotNull(tabs)
        assertEquals(3, tabs.size)

        val titles = tabs.map { tab ->
            val titleRef = DataReferenceParser.parseString(tab.jsonObject["title"])
            (titleRef as? LiteralString)?.value
        }

        assertEquals(listOf("Tab 1", "Tab 2", "Tab 3"), titles)
    }

    @Test
    fun `tab with hyphenated child IDs`() {
        val jsonStr = """
            {
                "tabs": [
                    {"title": "Overview", "child": "product-overview-panel"},
                    {"title": "Details", "child": "product-details-section"}
                ]
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val tabs = data["tabs"]?.jsonArray
        assertNotNull(tabs)

        val firstTab = tabs[0].jsonObject
        val childRef = DataReferenceParser.parseComponentRef(firstTab["child"])
        assertEquals("product-overview-panel", childRef?.componentId)

        val secondTab = tabs[1].jsonObject
        val childRef2 = DataReferenceParser.parseComponentRef(secondTab["child"])
        assertEquals("product-details-section", childRef2?.componentId)
    }

    @Test
    fun `empty tabs array`() {
        val jsonStr = """
            {
                "tabs": []
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val tabs = data["tabs"]?.jsonArray

        assertNotNull(tabs)
        assertTrue(tabs.isEmpty())
    }

    @Test
    fun `missing tabs returns null`() {
        val jsonStr = """{}"""
        val data = json.decodeFromString<JsonObject>(jsonStr)

        assertNull(data["tabs"])
    }
}
