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

package com.contextable.a2ui4k.docs

import com.contextable.a2ui4k.model.ActionEvent
import com.contextable.a2ui4k.model.ClientError
import com.contextable.a2ui4k.model.Component
import com.contextable.a2ui4k.model.DataChangeEvent
import com.contextable.a2ui4k.model.UiDefinition
import com.contextable.a2ui4k.model.UiEvent
import com.contextable.a2ui4k.model.ValidationError
import com.contextable.a2ui4k.model.toClientMessage
import com.contextable.a2ui4k.state.SurfaceStateManager
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Verifies the code examples in `docs/getting-started.md` compile and behave as documented.
 * These are not exhaustive functional tests — they pin the public API surface
 * the docs assume so the docs can't drift silently.
 */
class GettingStartedExamplesTest {

    /** "1. Create a UiDefinition" — v0.9 root-by-convention. */
    @Test
    fun `UiDefinition example compiles and creates valid structure`() {
        val uiDefinition = UiDefinition(
            surfaceId = "default",
            components = mapOf(
                "root" to Component.create(
                    id = "root",
                    widgetType = "Column",
                    data = buildJsonObject {
                        putJsonObject("children") {
                            putJsonArray("explicitList") {
                                add(buildJsonObject { put("componentId", "greeting") })
                            }
                        }
                    }
                ),
                "greeting" to Component.create(
                    id = "greeting",
                    widgetType = "Text",
                    data = buildJsonObject {
                        put("text", "Hello from A2UI!")
                    }
                )
            )
        )

        assertEquals("default", uiDefinition.surfaceId)
        assertEquals(2, uiDefinition.components.size)
        assertNotNull(uiDefinition.components["root"])
        assertNotNull(uiDefinition.components["greeting"])
        assertEquals("Column", uiDefinition.components["root"]?.widgetType)
        assertEquals("Text", uiDefinition.components["greeting"]?.widgetType)
        // v0.9: root is the component named "root" when rootComponentId is unset.
        assertEquals("Column", uiDefinition.rootComponent?.widgetType)
    }

    /** "3. Handle Events" — ActionEvent shape. */
    @Test
    fun `event handling example - ActionEvent has expected properties`() {
        val event: UiEvent = ActionEvent(
            name = "submit",
            surfaceId = "default",
            sourceComponentId = "button-1",
            timestamp = "2025-01-01T00:00:00Z"
        )

        val matched = when (event) {
            is ActionEvent -> {
                assertEquals("submit", event.name)
                assertEquals("button-1", event.sourceComponentId)
                assertEquals("default", event.surfaceId)
                true
            }
            is DataChangeEvent, is ValidationError, is ClientError -> false
        }
        assertTrue(matched, "ActionEvent branch should match")
    }

    /** "3. Handle Events" — DataChangeEvent shape. */
    @Test
    fun `event handling example - DataChangeEvent has expected properties`() {
        val event: UiEvent = DataChangeEvent(
            surfaceId = "default",
            path = "/user/name",
            value = "Alice"
        )

        val matched = when (event) {
            is DataChangeEvent -> {
                assertEquals("/user/name", event.path)
                assertEquals("Alice", event.value)
                true
            }
            is ActionEvent, is ValidationError, is ClientError -> false
        }
        assertTrue(matched)
    }

    /** "3. Handle Events" — ValidationError + ClientError split. */
    @Test
    fun `error events split into ValidationError and ClientError`() {
        val validation: UiEvent = ValidationError(
            surfaceId = "s1",
            path = "/components/0/text",
            message = "Expected string, got integer"
        )
        val custom: UiEvent = ClientError(
            code = "CATALOG_MISSING",
            surfaceId = "s1",
            message = "Catalog not found"
        )

        assertTrue(validation is ValidationError)
        assertEquals(ValidationError.VALIDATION_FAILED, (validation as ValidationError).code)
        assertTrue(custom is ClientError)
        assertEquals("CATALOG_MISSING", (custom as ClientError).code)
    }

    /** "Handle Events" — toClientMessage envelope shape (v0.9 default). */
    @Test
    fun `toClientMessage produces v0_9 envelope for ActionEvent`() {
        val event: UiEvent = ActionEvent(
            name = "submit",
            surfaceId = "default",
            sourceComponentId = "button-1",
            timestamp = "2025-01-01T00:00:00Z"
        )

        val envelope = event.toClientMessage()
        assertNotNull(envelope)
        assertEquals("v0.9", envelope["version"]?.jsonPrimitive?.content)
        val action = envelope["action"]?.jsonObject
        assertNotNull(action)
        assertEquals("submit", action["name"]?.jsonPrimitive?.content)
        assertEquals("button-1", action["sourceComponentId"]?.jsonPrimitive?.content)
    }

    /** v0.9 has no upstream wire shape for DataChangeEvent — it stays local. */
    @Test
    fun `toClientMessage returns null for DataChangeEvent under v0_9`() {
        val event: UiEvent = DataChangeEvent(
            surfaceId = "default",
            path = "/x",
            value = "y"
        )
        assertNull(event.toClientMessage())
    }

    /** "Using with DataModel" — DataModel accepts a JsonObject. */
    @Test
    fun `DataModel example - initialData accepts JsonObject`() {
        val initialData = buildJsonObject {
            putJsonObject("user") {
                put("name", "Alice")
                put("email", "alice@example.com")
            }
        }
        val dataModel = com.contextable.a2ui4k.data.DataModel(initialData)

        assertEquals("Alice", dataModel.getString("/user/name"))
        assertEquals("alice@example.com", dataModel.getString("/user/email"))
    }

    /** "Processing Agent Responses" — v0.9 envelope through processMessage. */
    @Test
    fun `SurfaceStateManager example - processMessage handles v0_9 envelope`() {
        val stateManager = SurfaceStateManager()

        val createSurface = buildJsonObject {
            put("version", "v0.9")
            putJsonObject("createSurface") {
                put("surfaceId", "default")
            }
        }
        stateManager.processMessage(createSurface)

        val current: UiDefinition? = stateManager.getSurface("default")
        assertNotNull(current)
        assertEquals("default", current.surfaceId)
    }
}
