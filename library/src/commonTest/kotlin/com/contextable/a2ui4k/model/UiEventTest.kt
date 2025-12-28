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

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for UiEvent types - UserActionEvent and DataChangeEvent.
 *
 * These tests verify A2UI ClientEvent format compliance.
 */
class UiEventTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ========== UserActionEvent tests ==========

    @Test
    fun `UserActionEvent has correct properties`() {
        val event = UserActionEvent(
            name = "submit",
            surfaceId = "main",
            sourceComponentId = "submit-button",
            timestamp = "2025-12-28T10:00:00.000Z"
        )

        assertEquals("submit", event.name)
        assertEquals("main", event.surfaceId)
        assertEquals("submit-button", event.sourceComponentId)
        assertEquals("2025-12-28T10:00:00.000Z", event.timestamp)
        assertNull(event.context)
    }

    @Test
    fun `UserActionEvent with context`() {
        val context = JsonObject(mapOf(
            "itemId" to JsonPrimitive("item-123"),
            "quantity" to JsonPrimitive(2)
        ))

        val event = UserActionEvent(
            name = "add-to-cart",
            surfaceId = "product-page",
            sourceComponentId = "add-button:item-123",
            timestamp = "2025-12-28T10:00:00.000Z",
            context = context
        )

        assertNotNull(event.context)
        assertEquals("item-123", event.context?.get("itemId")?.toString()?.trim('"'))
    }

    @Test
    fun `UserActionEvent serializes to JSON`() {
        val event = UserActionEvent(
            name = "click",
            surfaceId = "default",
            sourceComponentId = "btn1",
            timestamp = "2025-12-28T10:00:00.000Z"
        )

        val jsonStr = json.encodeToString(event)

        // Verify key fields are present
        assert(jsonStr.contains("\"name\":\"click\""))
        assert(jsonStr.contains("\"surfaceId\":\"default\""))
        assert(jsonStr.contains("\"sourceComponentId\":\"btn1\""))
        assert(jsonStr.contains("\"timestamp\":\"2025-12-28T10:00:00.000Z\""))
    }

    @Test
    fun `UserActionEvent deserializes from JSON`() {
        val jsonStr = """
            {
                "name": "action_name",
                "surfaceId": "default",
                "sourceComponentId": "component-id:item1",
                "timestamp": "2025-12-17T02:00:23.936Z",
                "context": {"key": "value"}
            }
        """.trimIndent()

        val event = json.decodeFromString<UserActionEvent>(jsonStr)

        assertEquals("action_name", event.name)
        assertEquals("default", event.surfaceId)
        assertEquals("component-id:item1", event.sourceComponentId)
        assertEquals("2025-12-17T02:00:23.936Z", event.timestamp)
        assertNotNull(event.context)
    }

    @Test
    fun `UserActionEvent with template item suffix`() {
        val event = UserActionEvent(
            name = "select",
            surfaceId = "list",
            sourceComponentId = "item-template:3",
            timestamp = "2025-12-28T10:00:00.000Z"
        )

        assertEquals("item-template:3", event.sourceComponentId)
    }

    // ========== DataChangeEvent tests ==========

    @Test
    fun `DataChangeEvent has correct properties`() {
        val event = DataChangeEvent(
            surfaceId = "form",
            path = "/user/email",
            value = "test@example.com"
        )

        assertEquals("form", event.surfaceId)
        assertEquals("/user/email", event.path)
        assertEquals("test@example.com", event.value)
    }

    @Test
    fun `DataChangeEvent serializes to JSON`() {
        val event = DataChangeEvent(
            surfaceId = "settings",
            path = "/preferences/theme",
            value = "dark"
        )

        val jsonStr = json.encodeToString(event)

        assert(jsonStr.contains("\"surfaceId\":\"settings\""))
        assert(jsonStr.contains("\"path\":\"/preferences/theme\""))
        assert(jsonStr.contains("\"value\":\"dark\""))
    }

    @Test
    fun `DataChangeEvent deserializes from JSON`() {
        val jsonStr = """
            {
                "surfaceId": "editor",
                "path": "/content/title",
                "value": "New Title"
            }
        """.trimIndent()

        val event = json.decodeFromString<DataChangeEvent>(jsonStr)

        assertEquals("editor", event.surfaceId)
        assertEquals("/content/title", event.path)
        assertEquals("New Title", event.value)
    }

    @Test
    fun `DataChangeEvent with nested path`() {
        val event = DataChangeEvent(
            surfaceId = "form",
            path = "/address/city/name",
            value = "New York"
        )

        assertEquals("/address/city/name", event.path)
    }

    @Test
    fun `DataChangeEvent with array index path`() {
        val event = DataChangeEvent(
            surfaceId = "list",
            path = "/items/0/name",
            value = "First Item"
        )

        assertEquals("/items/0/name", event.path)
    }
}
