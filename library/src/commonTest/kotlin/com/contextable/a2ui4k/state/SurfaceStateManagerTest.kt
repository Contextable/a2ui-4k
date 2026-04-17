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

package com.contextable.a2ui4k.state

import com.contextable.a2ui4k.model.ProtocolVersion
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [SurfaceStateManager], covering the A2UI v0.9 server-to-client
 * envelope (`{"version":"v0.9", "<op>": {...}}`) and all four operations:
 * createSurface, updateComponents, updateDataModel, deleteSurface.
 */
class SurfaceStateManagerTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun envelope(opKey: String, body: String): JsonObject =
        json.decodeFromString(
            """{"version":"v0.9","$opKey":$body}"""
        )

    // --- version envelope ---

    @Test
    fun `message with correct v09 version is processed`() {
        val manager = SurfaceStateManager()
        val handled = manager.processMessage(
            envelope("createSurface", """{"surfaceId":"s","catalogId":"cat"}""")
        )
        assertTrue(handled)
        assertEquals(1, manager.surfaceCount)
    }

    @Test
    fun `message with foreign version is rejected`() {
        val manager = SurfaceStateManager()
        val msg: JsonObject = json.decodeFromString(
            """{"version":"v0.8","createSurface":{"surfaceId":"s","catalogId":"c"}}"""
        )
        val handled = manager.processMessage(msg)
        assertFalse(handled)
        assertEquals(0, manager.surfaceCount)
    }

    @Test
    fun `message without version key is still processed`() {
        // The spec requires version, but we accept the operation if absent rather
        // than reject — the version check's job is to catch wrong versions.
        val manager = SurfaceStateManager()
        val msg: JsonObject = json.decodeFromString(
            """{"createSurface":{"surfaceId":"s","catalogId":"c"}}"""
        )
        val handled = manager.processMessage(msg)
        assertTrue(handled)
        assertEquals(1, manager.surfaceCount)
    }

    @Test
    fun `unknown operation key returns false`() {
        val manager = SurfaceStateManager()
        val msg: JsonObject = json.decodeFromString(
            """{"version":"v0.9","mysteryOp":{}}"""
        )
        assertFalse(manager.processMessage(msg))
    }

    // --- createSurface ---

    @Test
    fun `createSurface initializes a new surface`() {
        val manager = SurfaceStateManager()
        manager.processMessage(
            envelope(
                "createSurface",
                """{"surfaceId":"surface-1","catalogId":"https://example.com/catalog.json"}"""
            )
        )

        assertEquals(1, manager.surfaceCount)
        val surface = manager.getSurface("surface-1")
        assertNotNull(surface)
        assertEquals("surface-1", surface.surfaceId)
        assertEquals("https://example.com/catalog.json", surface.catalogId)
    }

    @Test
    fun `createSurface with theme and sendDataModel`() {
        val manager = SurfaceStateManager()
        manager.processMessage(
            envelope(
                "createSurface",
                """{"surfaceId":"surface-2","catalogId":"c","theme":{"primaryColor":"#FF0000"},"sendDataModel":true}"""
            )
        )

        val surface = manager.getSurface("surface-2")
        assertNotNull(surface)
        assertTrue(surface.sendDataModel)
        assertNotNull(surface.theme)
        assertEquals("#FF0000", (surface.theme!!["primaryColor"] as? JsonPrimitive)?.content)
    }

    @Test
    fun `createSurface without surfaceId is ignored`() {
        val manager = SurfaceStateManager()
        manager.processMessage(
            envelope("createSurface", """{"catalogId":"https://example.com/catalog.json"}""")
        )
        assertEquals(0, manager.surfaceCount)
    }

    // --- updateComponents ---

    @Test
    fun `updateComponents adds components to surface`() {
        val manager = SurfaceStateManager()
        manager.processMessage(envelope("createSurface", """{"surfaceId":"s1","catalogId":"c"}"""))
        manager.processMessage(
            envelope(
                "updateComponents",
                """{"surfaceId":"s1","components":[
                    {"id":"root","component":"Column","children":["title"]},
                    {"id":"title","component":"Text","text":"Hello World","variant":"h1"}
                ]}"""
            )
        )

        val surface = manager.getSurface("s1")
        assertNotNull(surface)
        assertEquals(2, surface.components.size)
        assertEquals("Column", surface.components["root"]?.widgetType)
        assertEquals("Text", surface.components["title"]?.widgetType)
    }

    @Test
    fun `updateComponents replaces existing component`() {
        val manager = SurfaceStateManager()
        manager.processMessage(envelope("createSurface", """{"surfaceId":"s1","catalogId":"c"}"""))
        manager.processMessage(
            envelope(
                "updateComponents",
                """{"surfaceId":"s1","components":[{"id":"t","component":"Text","text":"Original"}]}"""
            )
        )
        manager.processMessage(
            envelope(
                "updateComponents",
                """{"surfaceId":"s1","components":[{"id":"t","component":"Text","text":"Updated"}]}"""
            )
        )

        val comp = manager.getSurface("s1")?.components?.get("t")
        assertNotNull(comp)
        assertEquals("Updated", (comp.widgetData["text"] as? JsonPrimitive)?.content)
    }

    @Test
    fun `updateComponents creates surface implicitly if not already created`() {
        val manager = SurfaceStateManager()
        manager.processMessage(
            envelope(
                "updateComponents",
                """{"surfaceId":"implicit","components":[{"id":"root","component":"Text","text":"Auto"}]}"""
            )
        )
        assertEquals(1, manager.surfaceCount)
        assertEquals(1, manager.getSurface("implicit")?.components?.size)
    }

    // --- updateDataModel ---

    @Test
    fun `updateDataModel sets value at path`() {
        val manager = SurfaceStateManager()
        manager.processMessage(envelope("createSurface", """{"surfaceId":"s1","catalogId":"c"}"""))
        manager.processMessage(
            envelope("updateDataModel", """{"surfaceId":"s1","path":"/user/name","value":"Alice"}""")
        )

        assertEquals("Alice", manager.getDataModel("s1")?.getString("/user/name"))
    }

    @Test
    fun `updateDataModel with object value`() {
        val manager = SurfaceStateManager()
        manager.processMessage(envelope("createSurface", """{"surfaceId":"s1","catalogId":"c"}"""))
        manager.processMessage(
            envelope(
                "updateDataModel",
                """{"surfaceId":"s1","path":"/user","value":{"name":"Bob","age":30}}"""
            )
        )
        assertEquals("Bob", manager.getDataModel("s1")?.getString("/user/name"))
    }

    @Test
    fun `updateDataModel with absent value deletes the key`() {
        val manager = SurfaceStateManager()
        manager.processMessage(envelope("createSurface", """{"surfaceId":"s1","catalogId":"c"}"""))
        manager.processMessage(
            envelope("updateDataModel", """{"surfaceId":"s1","path":"/temp","value":"to-be-deleted"}""")
        )
        assertEquals("to-be-deleted", manager.getDataModel("s1")?.getString("/temp"))

        manager.processMessage(
            envelope("updateDataModel", """{"surfaceId":"s1","path":"/temp"}""")
        )
        assertNull(manager.getDataModel("s1")?.getString("/temp"))
        assertNull(manager.getDataModel("s1")?.get("/temp"))
    }

    @Test
    fun `updateDataModel with explicit null value stores JsonNull, not delete`() {
        val manager = SurfaceStateManager()
        manager.processMessage(envelope("createSurface", """{"surfaceId":"s1","catalogId":"c"}"""))
        manager.processMessage(
            envelope("updateDataModel", """{"surfaceId":"s1","path":"/nullable","value":null}""")
        )
        assertEquals(JsonNull, manager.getDataModel("s1")?.get("/nullable"))
    }

    @Test
    fun `updateDataModel defaults path to root`() {
        val manager = SurfaceStateManager()
        manager.processMessage(envelope("createSurface", """{"surfaceId":"s1","catalogId":"c"}"""))
        manager.processMessage(
            envelope("updateDataModel", """{"surfaceId":"s1","value":{"greeting":"hello"}}""")
        )
        assertEquals("hello", manager.getDataModel("s1")?.getString("/greeting"))
    }

    // --- deleteSurface ---

    @Test
    fun `deleteSurface removes surface`() {
        val manager = SurfaceStateManager()
        manager.processMessage(envelope("createSurface", """{"surfaceId":"s1","catalogId":"c"}"""))
        manager.processMessage(envelope("deleteSurface", """{"surfaceId":"s1"}"""))

        assertEquals(0, manager.surfaceCount)
        assertNull(manager.getSurface("s1"))
        assertNull(manager.getDataModel("s1"))
    }

    @Test
    fun `deleteSurface for non-existent surface is no-op`() {
        val manager = SurfaceStateManager()
        manager.processMessage(envelope("deleteSurface", """{"surfaceId":"nonexistent"}"""))
        assertEquals(0, manager.surfaceCount)
    }

    // --- clientDataModel metadata ---

    @Test
    fun `buildClientDataModel returns null when no surface has sendDataModel`() {
        val manager = SurfaceStateManager()
        manager.processMessage(envelope("createSurface", """{"surfaceId":"s","catalogId":"c"}"""))
        assertNull(manager.buildClientDataModel())
    }

    @Test
    fun `buildClientDataModel returns envelope for sendDataModel surfaces`() {
        val manager = SurfaceStateManager()
        manager.processMessage(
            envelope("createSurface", """{"surfaceId":"s","catalogId":"c","sendDataModel":true}""")
        )
        manager.processMessage(
            envelope("updateDataModel", """{"surfaceId":"s","path":"/n","value":"x"}""")
        )
        val payload = manager.buildClientDataModel()
        assertNotNull(payload)
        assertEquals("v0.9", (payload["version"] as? JsonPrimitive)?.content)
        val surfaces = payload["surfaces"] as? JsonObject
        assertNotNull(surfaces)
        assertTrue(surfaces.containsKey("s"))
    }

    // --- getSurfaces / clear ---

    @Test
    fun `getSurfaces returns all active surfaces`() {
        val manager = SurfaceStateManager()
        manager.processMessage(envelope("createSurface", """{"surfaceId":"s1","catalogId":"c"}"""))
        manager.processMessage(envelope("createSurface", """{"surfaceId":"s2","catalogId":"c"}"""))
        val surfaces = manager.getSurfaces()
        assertEquals(2, surfaces.size)
        assertTrue(surfaces.containsKey("s1"))
        assertTrue(surfaces.containsKey("s2"))
    }

    @Test
    fun `clear removes all surfaces`() {
        val manager = SurfaceStateManager()
        manager.processMessage(envelope("createSurface", """{"surfaceId":"s1","catalogId":"c"}"""))
        assertEquals(1, manager.surfaceCount)
        manager.clear()
        assertEquals(0, manager.surfaceCount)
    }

    // --- UiDefinition integration ---

    @Test
    fun `surface UiDefinition has correct rootComponent`() {
        val manager = SurfaceStateManager()
        manager.processMessage(envelope("createSurface", """{"surfaceId":"s1","catalogId":"c"}"""))
        manager.processMessage(
            envelope(
                "updateComponents",
                """{"surfaceId":"s1","components":[
                    {"id":"root","component":"Column","children":["text1"]},
                    {"id":"text1","component":"Text","text":"Hello"}
                ]}"""
            )
        )

        val surface = manager.getSurface("s1")
        assertNotNull(surface)
        assertNotNull(surface.rootComponent)
        assertEquals("Column", surface.rootComponent!!.widgetType)
    }

    @Test
    fun `surface without root component returns null rootComponent`() {
        val manager = SurfaceStateManager()
        manager.processMessage(envelope("createSurface", """{"surfaceId":"s1","catalogId":"c"}"""))
        manager.processMessage(
            envelope(
                "updateComponents",
                """{"surfaceId":"s1","components":[{"id":"not-root","component":"Text","text":"Hello"}]}"""
            )
        )

        val surface = manager.getSurface("s1")
        assertNotNull(surface)
        assertNull(surface.rootComponent)
    }

    // --- Multiple operations in sequence ---

    @Test
    fun `full lifecycle - create then update components then update data then delete`() {
        val manager = SurfaceStateManager()
        manager.processMessage(envelope("createSurface", """{"surfaceId":"life","catalogId":"cat"}"""))
        assertEquals(1, manager.surfaceCount)

        manager.processMessage(
            envelope(
                "updateComponents",
                """{"surfaceId":"life","components":[{"id":"root","component":"Text","text":{"path":"/msg"}}]}"""
            )
        )
        assertNotNull(manager.getSurface("life")?.rootComponent)

        manager.processMessage(
            envelope("updateDataModel", """{"surfaceId":"life","path":"/msg","value":"Hello!"}""")
        )
        assertEquals("Hello!", manager.getDataModel("life")?.getString("/msg"))

        manager.processMessage(envelope("deleteSurface", """{"surfaceId":"life"}"""))
        assertEquals(0, manager.surfaceCount)
    }

    @Test
    fun `component weight is preserved through processing`() {
        val manager = SurfaceStateManager()
        manager.processMessage(envelope("createSurface", """{"surfaceId":"s1","catalogId":"c"}"""))
        manager.processMessage(
            envelope(
                "updateComponents",
                """{"surfaceId":"s1","components":[{"id":"col1","component":"Column","children":["a"],"weight":2}]}"""
            )
        )

        val comp = manager.getSurface("s1")?.components?.get("col1")
        assertNotNull(comp)
        assertEquals(2, comp.weight)
    }

    // --- v0.8 dispatch (ACTIVITY_SNAPSHOT transcode) ---

    @Test
    fun `v0_8 ACTIVITY_SNAPSHOT is transcoded and dispatched`() {
        val manager = SurfaceStateManager()
        val v08Msg: JsonObject = json.decodeFromString(
            """
            {
                "kind":"ACTIVITY_SNAPSHOT",
                "messageId":"m1",
                "content":{"operations":[
                    {"beginRendering":{"surfaceId":"s1","root":"root"}},
                    {"surfaceUpdate":{"surfaceId":"s1","components":[
                        {"id":"root","component":{"Column":{"children":{"explicitList":["t"]}}}},
                        {"id":"t","component":{"Text":{"text":{"literalString":"Hello"}}}}
                    ]}}
                ]}
            }
            """.trimIndent()
        )
        val handled = manager.processMessage(v08Msg)
        assertTrue(handled)
        assertEquals(1, manager.surfaceCount)
        val surface = manager.getSurface("s1")
        assertNotNull(surface)
        assertEquals(ProtocolVersion.V0_8, surface.protocolVersion)
        // v0.8 `root` was captured as rootComponentId.
        assertEquals("root", surface.rootComponentId)
        // Components are flattened and available.
        assertEquals(2, surface.components.size)
        assertEquals("Text", surface.components["t"]?.widgetType)
    }

    @Test
    fun `v0_8 and v0_9 surfaces coexist and carry different protocolVersion`() {
        val manager = SurfaceStateManager()
        // v0.9 surface
        manager.processMessage(
            envelope("createSurface", """{"surfaceId":"v9","catalogId":"c"}""")
        )
        // v0.8 surface
        val v08Msg: JsonObject = json.decodeFromString(
            """
            {
                "kind":"ACTIVITY_SNAPSHOT",
                "content":{"operations":[
                    {"beginRendering":{"surfaceId":"v8","root":"root"}}
                ]}
            }
            """.trimIndent()
        )
        manager.processMessage(v08Msg)

        assertEquals(ProtocolVersion.V0_9, manager.getSurfaceProtocolVersion("v9"))
        assertEquals(ProtocolVersion.V0_8, manager.getSurfaceProtocolVersion("v8"))
    }

    @Test
    fun `v0_8 ACTIVITY_DELTA replays JSON Patch and dispatches new ops`() {
        val manager = SurfaceStateManager()
        manager.processMessage(
            json.decodeFromString(
                """
                {
                    "kind":"ACTIVITY_SNAPSHOT",
                    "messageId":"m1",
                    "content":{"operations":[
                        {"beginRendering":{"surfaceId":"s1","root":"root"}}
                    ]}
                }
                """.trimIndent()
            )
        )
        val before = manager.getSurface("s1")?.components?.size ?: 0
        assertEquals(0, before)

        manager.processMessage(
            json.decodeFromString(
                """
                {
                    "kind":"ACTIVITY_DELTA",
                    "messageId":"m1",
                    "patch":[{"op":"add","path":"/operations/1","value":{
                        "surfaceUpdate":{"surfaceId":"s1","components":[
                            {"id":"root","component":{"Text":{"text":{"literalString":"Hi"}}}}
                        ]}
                    }}]
                }
                """.trimIndent()
            )
        )
        assertEquals(1, manager.getSurface("s1")?.components?.size)
    }

    @Test
    fun `v0_8 surface does not emit a2uiClientDataModel envelope`() {
        // v0.8 has no equivalent mechanism; buildClientDataModel should skip v0.8 surfaces.
        val manager = SurfaceStateManager()
        val v08Msg: JsonObject = json.decodeFromString(
            """
            {
                "kind":"ACTIVITY_SNAPSHOT",
                "content":{"operations":[
                    {"beginRendering":{"surfaceId":"s1","root":"root"}}
                ]}
            }
            """.trimIndent()
        )
        manager.processMessage(v08Msg)
        assertNull(manager.buildClientDataModel())
    }
}
