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
 * Tests for Modal widget JSON parsing.
 *
 * A2UI Spec properties (v0.8):
 * - entryPointChild (required): Component ID of trigger element
 * - contentChild (required): Component ID of modal content
 */
class ModalWidgetTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parseComponentRef extracts entryPointChild`() {
        val jsonStr = """
            {
                "entryPointChild": "open-button",
                "contentChild": "modal-content"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val entryRef = DataReferenceParser.parseComponentRef(data["entryPointChild"])

        assertNotNull(entryRef)
        assertEquals("open-button", entryRef.componentId)
    }

    @Test
    fun `parseComponentRef extracts contentChild`() {
        val jsonStr = """
            {
                "entryPointChild": "trigger",
                "contentChild": "dialog-content"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val contentRef = DataReferenceParser.parseComponentRef(data["contentChild"])

        assertNotNull(contentRef)
        assertEquals("dialog-content", contentRef.componentId)
    }

    @Test
    fun `complete Modal with both required properties`() {
        val jsonStr = """
            {
                "entryPointChild": "settings-button",
                "contentChild": "settings-panel"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val entryRef = DataReferenceParser.parseComponentRef(data["entryPointChild"])
        assertEquals("settings-button", entryRef?.componentId)

        val contentRef = DataReferenceParser.parseComponentRef(data["contentChild"])
        assertEquals("settings-panel", contentRef?.componentId)
    }

    @Test
    fun `Modal with nested component IDs containing hyphens`() {
        val jsonStr = """
            {
                "entryPointChild": "my-custom-trigger-button",
                "contentChild": "my-complex-modal-content-area"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val entryRef = DataReferenceParser.parseComponentRef(data["entryPointChild"])
        assertEquals("my-custom-trigger-button", entryRef?.componentId)

        val contentRef = DataReferenceParser.parseComponentRef(data["contentChild"])
        assertEquals("my-complex-modal-content-area", contentRef?.componentId)
    }

    @Test
    fun `Modal typical usage pattern`() {
        // Matches the pattern used in WidgetSamples
        val jsonStr = """
            {
                "entryPointChild": "trigger",
                "contentChild": "modal-content"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val entryRef = DataReferenceParser.parseComponentRef(data["entryPointChild"])
        assertNotNull(entryRef)
        assertEquals("trigger", entryRef.componentId)

        val contentRef = DataReferenceParser.parseComponentRef(data["contentChild"])
        assertNotNull(contentRef)
        assertEquals("modal-content", contentRef.componentId)
    }
}
