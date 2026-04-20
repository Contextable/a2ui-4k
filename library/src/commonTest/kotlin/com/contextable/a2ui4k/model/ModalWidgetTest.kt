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
 * A2UI Spec properties (v0.9):
 * - trigger (required): Component ID of trigger element (renamed from entryPointChild)
 * - content (required): Component ID of modal content (renamed from contentChild)
 */
class ModalWidgetTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parseComponentRef extracts trigger`() {
        val jsonStr = """
            {
                "trigger": "open-button",
                "content": "modal-content"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val triggerRef = DataReferenceParser.parseComponentRef(data["trigger"])

        assertNotNull(triggerRef)
        assertEquals("open-button", triggerRef.componentId)
    }

    @Test
    fun `parseComponentRef extracts content`() {
        val jsonStr = """
            {
                "trigger": "trigger",
                "content": "dialog-content"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val contentRef = DataReferenceParser.parseComponentRef(data["content"])

        assertNotNull(contentRef)
        assertEquals("dialog-content", contentRef.componentId)
    }

    @Test
    fun `complete Modal with both required properties`() {
        val jsonStr = """
            {
                "trigger": "settings-button",
                "content": "settings-panel"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val triggerRef = DataReferenceParser.parseComponentRef(data["trigger"])
        assertEquals("settings-button", triggerRef?.componentId)

        val contentRef = DataReferenceParser.parseComponentRef(data["content"])
        assertEquals("settings-panel", contentRef?.componentId)
    }

    @Test
    fun `Modal with nested component IDs containing hyphens`() {
        val jsonStr = """
            {
                "trigger": "my-custom-trigger-button",
                "content": "my-complex-modal-content-area"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val triggerRef = DataReferenceParser.parseComponentRef(data["trigger"])
        assertEquals("my-custom-trigger-button", triggerRef?.componentId)

        val contentRef = DataReferenceParser.parseComponentRef(data["content"])
        assertEquals("my-complex-modal-content-area", contentRef?.componentId)
    }

    @Test
    fun `Modal typical usage pattern`() {
        // Matches the pattern used in WidgetSamples
        val jsonStr = """
            {
                "trigger": "trigger",
                "content": "modal-content"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val triggerRef = DataReferenceParser.parseComponentRef(data["trigger"])
        assertNotNull(triggerRef)
        assertEquals("trigger", triggerRef.componentId)

        val contentRef = DataReferenceParser.parseComponentRef(data["content"])
        assertNotNull(contentRef)
        assertEquals("modal-content", contentRef.componentId)
    }
}
