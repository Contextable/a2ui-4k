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
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for UiEvent types: [ActionEvent], [DataChangeEvent], [ValidationError], [ClientError].
 *
 * Verifies A2UI v0.9 ClientMessage format compliance.
 */
class UiEventTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ========== ActionEvent tests ==========

    @Test
    fun `ActionEvent has correct properties`() {
        val event = ActionEvent(
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
    fun `ActionEvent with context`() {
        val context = JsonObject(mapOf(
            "itemId" to JsonPrimitive("item-123"),
            "quantity" to JsonPrimitive(2)
        ))

        val event = ActionEvent(
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
    fun `ActionEvent serializes to JSON`() {
        val event = ActionEvent(
            name = "click",
            surfaceId = "default",
            sourceComponentId = "btn1",
            timestamp = "2025-12-28T10:00:00.000Z"
        )

        val jsonStr = json.encodeToString(event)

        // Verify key fields are present
        assertTrue(jsonStr.contains("\"name\":\"click\""))
        assertTrue(jsonStr.contains("\"surfaceId\":\"default\""))
        assertTrue(jsonStr.contains("\"sourceComponentId\":\"btn1\""))
        assertTrue(jsonStr.contains("\"timestamp\":\"2025-12-28T10:00:00.000Z\""))
    }

    @Test
    fun `ActionEvent deserializes from JSON`() {
        val jsonStr = """
            {
                "name": "action_name",
                "surfaceId": "default",
                "sourceComponentId": "component-id:item1",
                "timestamp": "2025-12-17T02:00:23.936Z",
                "context": {"key": "value"}
            }
        """.trimIndent()

        val event = json.decodeFromString<ActionEvent>(jsonStr)

        assertEquals("action_name", event.name)
        assertEquals("default", event.surfaceId)
        assertEquals("component-id:item1", event.sourceComponentId)
        assertEquals("2025-12-17T02:00:23.936Z", event.timestamp)
        assertNotNull(event.context)
    }

    @Test
    fun `ActionEvent with template item suffix`() {
        val event = ActionEvent(
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

        assertTrue(jsonStr.contains("\"surfaceId\":\"settings\""))
        assertTrue(jsonStr.contains("\"path\":\"/preferences/theme\""))
        assertTrue(jsonStr.contains("\"value\":\"dark\""))
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

    // ========== Envelope (toClientMessage) ==========

    @Test
    fun `ActionEvent toClientMessage wraps in v09 envelope`() {
        val event = ActionEvent(
            name = "submit",
            surfaceId = "main",
            sourceComponentId = "btn",
            timestamp = "2026-01-01T00:00:00Z"
        )
        val envelope = event.toClientMessage()!!
        assertEquals("v0.9", (envelope["version"] as JsonPrimitive).content)
        val action = envelope["action"] as JsonObject
        assertEquals("submit", (action["name"] as JsonPrimitive).content)
        assertEquals("main", (action["surfaceId"] as JsonPrimitive).content)
        assertEquals("btn", (action["sourceComponentId"] as JsonPrimitive).content)
        assertTrue(action.containsKey("context"))
    }

    @Test
    fun `ValidationError toClientMessage has code VALIDATION_FAILED and path`() {
        val event = ValidationError(
            surfaceId = "s",
            path = "/components/0/text",
            message = "must be a string"
        )
        val envelope = event.toClientMessage()!!
        val error = envelope["error"] as JsonObject
        assertEquals("VALIDATION_FAILED", (error["code"] as JsonPrimitive).content)
        assertEquals("/components/0/text", (error["path"] as JsonPrimitive).content)
    }

    @Test
    fun `ClientError toClientMessage has custom code and no path`() {
        val event = ClientError(
            code = "NETWORK_UNAVAILABLE",
            surfaceId = "s",
            message = "no connection"
        )
        val envelope = event.toClientMessage()!!
        val error = envelope["error"] as JsonObject
        assertEquals("NETWORK_UNAVAILABLE", (error["code"] as JsonPrimitive).content)
        assertTrue(!error.containsKey("path"))
    }

    @Test
    fun `DataChangeEvent toClientMessage returns null`() {
        val event = DataChangeEvent(surfaceId = "s", path = "/x", value = "v")
        assertNull(event.toClientMessage())
    }

    @Test
    fun `ClientError rejects VALIDATION_FAILED code`() {
        try {
            ClientError(code = "VALIDATION_FAILED", surfaceId = "s", message = "m")
            assertTrue(false, "expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    // ========== v0.8 envelopes ==========

    @Test
    fun `ActionEvent toClientMessage with V0_8 emits userAction without version`() {
        val event = ActionEvent(
            name = "submit",
            surfaceId = "main",
            sourceComponentId = "btn",
            timestamp = "2026-01-01T00:00:00Z",
            context = buildJsonObject { put("k", JsonPrimitive("v")) }
        )
        val envelope = event.toClientMessage(ProtocolVersion.V0_8)!!
        assertFalse(envelope.containsKey("version"))
        val userAction = envelope["userAction"] as JsonObject
        assertEquals("submit", (userAction["name"] as JsonPrimitive).content)
        assertEquals("main", (userAction["surfaceId"] as JsonPrimitive).content)
        assertEquals("btn", (userAction["sourceComponentId"] as JsonPrimitive).content)
        val ctx = userAction["context"] as JsonObject
        assertEquals("v", (ctx["k"] as JsonPrimitive).content)
    }

    @Test
    fun `DataChangeEvent toClientMessage with V0_8 emits dataChange wire message`() {
        val event = DataChangeEvent(surfaceId = "s", path = "/x", value = "new")
        val envelope = event.toClientMessage(ProtocolVersion.V0_8)!!
        assertFalse(envelope.containsKey("version"))
        val dc = envelope["dataChange"] as JsonObject
        assertEquals("s", (dc["surfaceId"] as JsonPrimitive).content)
        assertEquals("/x", (dc["path"] as JsonPrimitive).content)
        assertEquals("new", (dc["value"] as JsonPrimitive).content)
    }

    @Test
    fun `ValidationError toClientMessage with V0_8 returns null`() {
        // v0.8 has no client-error wire message shape; swallowed intentionally.
        val event = ValidationError(surfaceId = "s", path = "/x", message = "m")
        assertNull(event.toClientMessage(ProtocolVersion.V0_8))
    }

    @Test
    fun `ClientError toClientMessage with V0_8 returns null`() {
        val event = ClientError(code = "FOO", surfaceId = "s", message = "m")
        assertNull(event.toClientMessage(ProtocolVersion.V0_8))
    }
}
