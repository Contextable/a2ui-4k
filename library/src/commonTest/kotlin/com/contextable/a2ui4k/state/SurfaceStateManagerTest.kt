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

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [SurfaceStateManager], covering all four A2UI v0.9 operations:
 * createSurface, updateComponents, updateDataModel, deleteSurface.
 */
class SurfaceStateManagerTest {

    private val json = Json { ignoreUnknownKeys = true }

    // --- createSurface ---

    @Test
    fun `createSurface initializes a new surface`() {
        val manager = SurfaceStateManager()

        val snapshot = json.decodeFromString<JsonObject>("""
            {
                "operations": [
                    {
                        "createSurface": {
                            "surfaceId": "surface-1",
                            "catalogId": "https://example.com/catalog.json"
                        }
                    }
                ]
            }
        """.trimIndent())

        manager.processSnapshot("msg-1", snapshot)

        assertEquals(1, manager.surfaceCount)
        val surface = manager.getSurface("surface-1")
        assertNotNull(surface)
        assertEquals("surface-1", surface.surfaceId)
        assertEquals("https://example.com/catalog.json", surface.catalogId)
    }

    @Test
    fun `createSurface with theme and sendDataModel`() {
        val manager = SurfaceStateManager()

        val snapshot = json.decodeFromString<JsonObject>("""
            {
                "operations": [
                    {
                        "createSurface": {
                            "surfaceId": "surface-2",
                            "catalogId": "https://example.com/catalog.json",
                            "theme": {"primaryColor": "#FF0000"},
                            "sendDataModel": true
                        }
                    }
                ]
            }
        """.trimIndent())

        manager.processSnapshot("msg-1", snapshot)

        val surface = manager.getSurface("surface-2")
        assertNotNull(surface)
        assertTrue(surface.sendDataModel)
        assertNotNull(surface.theme)
        assertEquals("#FF0000", (surface.theme!!["primaryColor"] as? JsonPrimitive)?.content)
    }

    @Test
    fun `createSurface without surfaceId is ignored`() {
        val manager = SurfaceStateManager()

        val snapshot = json.decodeFromString<JsonObject>("""
            {
                "operations": [
                    {
                        "createSurface": {
                            "catalogId": "https://example.com/catalog.json"
                        }
                    }
                ]
            }
        """.trimIndent())

        manager.processSnapshot("msg-1", snapshot)

        assertEquals(0, manager.surfaceCount)
    }

    // --- updateComponents ---

    @Test
    fun `updateComponents adds components to surface`() {
        val manager = SurfaceStateManager()

        val snapshot = json.decodeFromString<JsonObject>("""
            {
                "operations": [
                    {
                        "createSurface": {
                            "surfaceId": "s1",
                            "catalogId": "https://example.com/catalog.json"
                        }
                    },
                    {
                        "updateComponents": {
                            "surfaceId": "s1",
                            "components": [
                                {
                                    "id": "root",
                                    "component": "Column",
                                    "children": ["title"]
                                },
                                {
                                    "id": "title",
                                    "component": "Text",
                                    "text": "Hello World",
                                    "variant": "h1"
                                }
                            ]
                        }
                    }
                ]
            }
        """.trimIndent())

        manager.processSnapshot("msg-1", snapshot)

        val surface = manager.getSurface("s1")
        assertNotNull(surface)
        assertEquals(2, surface.components.size)

        val rootComp = surface.components["root"]
        assertNotNull(rootComp)
        assertEquals("Column", rootComp.widgetType)

        val titleComp = surface.components["title"]
        assertNotNull(titleComp)
        assertEquals("Text", titleComp.widgetType)
    }

    @Test
    fun `updateComponents replaces existing component`() {
        val manager = SurfaceStateManager()

        val snapshot1 = json.decodeFromString<JsonObject>("""
            {
                "operations": [
                    {
                        "createSurface": {
                            "surfaceId": "s1",
                            "catalogId": "catalog"
                        }
                    },
                    {
                        "updateComponents": {
                            "surfaceId": "s1",
                            "components": [
                                {"id": "title", "component": "Text", "text": "Original"}
                            ]
                        }
                    }
                ]
            }
        """.trimIndent())

        manager.processSnapshot("msg-1", snapshot1)

        // Update the same component with new text
        val snapshot2 = json.decodeFromString<JsonObject>("""
            {
                "operations": [
                    {
                        "updateComponents": {
                            "surfaceId": "s1",
                            "components": [
                                {"id": "title", "component": "Text", "text": "Updated"}
                            ]
                        }
                    }
                ]
            }
        """.trimIndent())

        manager.processSnapshot("msg-2", snapshot2)

        val surface = manager.getSurface("s1")
        assertNotNull(surface)
        val titleComp = surface.components["title"]
        assertNotNull(titleComp)
        assertEquals("Updated", (titleComp.widgetData["text"] as? JsonPrimitive)?.content)
    }

    @Test
    fun `updateComponents creates surface implicitly if not already created`() {
        val manager = SurfaceStateManager()

        val snapshot = json.decodeFromString<JsonObject>("""
            {
                "operations": [
                    {
                        "updateComponents": {
                            "surfaceId": "implicit",
                            "components": [
                                {"id": "root", "component": "Text", "text": "Auto"}
                            ]
                        }
                    }
                ]
            }
        """.trimIndent())

        manager.processSnapshot("msg-1", snapshot)

        assertEquals(1, manager.surfaceCount)
        val surface = manager.getSurface("implicit")
        assertNotNull(surface)
        assertEquals(1, surface.components.size)
    }

    // --- updateDataModel ---

    @Test
    fun `updateDataModel sets value at path`() {
        val manager = SurfaceStateManager()

        val snapshot = json.decodeFromString<JsonObject>("""
            {
                "operations": [
                    {
                        "createSurface": {
                            "surfaceId": "s1",
                            "catalogId": "catalog"
                        }
                    },
                    {
                        "updateDataModel": {
                            "surfaceId": "s1",
                            "path": "/user/name",
                            "value": "Alice"
                        }
                    }
                ]
            }
        """.trimIndent())

        manager.processSnapshot("msg-1", snapshot)

        val dataModel = manager.getDataModel("s1")
        assertNotNull(dataModel)
        assertEquals("Alice", dataModel.getString("/user/name"))
    }

    @Test
    fun `updateDataModel with object value`() {
        val manager = SurfaceStateManager()

        val snapshot = json.decodeFromString<JsonObject>("""
            {
                "operations": [
                    {
                        "createSurface": {
                            "surfaceId": "s1",
                            "catalogId": "catalog"
                        }
                    },
                    {
                        "updateDataModel": {
                            "surfaceId": "s1",
                            "path": "/user",
                            "value": {"name": "Bob", "age": 30}
                        }
                    }
                ]
            }
        """.trimIndent())

        manager.processSnapshot("msg-1", snapshot)

        val dataModel = manager.getDataModel("s1")
        assertNotNull(dataModel)
        assertEquals("Bob", dataModel.getString("/user/name"))
    }

    @Test
    fun `updateDataModel without value deletes the key`() {
        val manager = SurfaceStateManager()

        // First set a value
        val snapshot1 = json.decodeFromString<JsonObject>("""
            {
                "operations": [
                    {
                        "createSurface": {
                            "surfaceId": "s1",
                            "catalogId": "catalog"
                        }
                    },
                    {
                        "updateDataModel": {
                            "surfaceId": "s1",
                            "path": "/temp",
                            "value": "to-be-deleted"
                        }
                    }
                ]
            }
        """.trimIndent())

        manager.processSnapshot("msg-1", snapshot1)
        assertEquals("to-be-deleted", manager.getDataModel("s1")?.getString("/temp"))

        // Now delete the key by omitting value
        val snapshot2 = json.decodeFromString<JsonObject>("""
            {
                "operations": [
                    {
                        "updateDataModel": {
                            "surfaceId": "s1",
                            "path": "/temp"
                        }
                    }
                ]
            }
        """.trimIndent())

        manager.processSnapshot("msg-2", snapshot2)
        assertNull(manager.getDataModel("s1")?.getString("/temp"))
    }

    @Test
    fun `updateDataModel defaults path to root`() {
        val manager = SurfaceStateManager()

        val snapshot = json.decodeFromString<JsonObject>("""
            {
                "operations": [
                    {
                        "createSurface": {
                            "surfaceId": "s1",
                            "catalogId": "catalog"
                        }
                    },
                    {
                        "updateDataModel": {
                            "surfaceId": "s1",
                            "value": {"greeting": "hello"}
                        }
                    }
                ]
            }
        """.trimIndent())

        manager.processSnapshot("msg-1", snapshot)

        val dataModel = manager.getDataModel("s1")
        assertNotNull(dataModel)
        assertEquals("hello", dataModel.getString("/greeting"))
    }

    // --- deleteSurface ---

    @Test
    fun `deleteSurface removes surface`() {
        val manager = SurfaceStateManager()

        val snapshot = json.decodeFromString<JsonObject>("""
            {
                "operations": [
                    {
                        "createSurface": {
                            "surfaceId": "s1",
                            "catalogId": "catalog"
                        }
                    },
                    {
                        "deleteSurface": {
                            "surfaceId": "s1"
                        }
                    }
                ]
            }
        """.trimIndent())

        manager.processSnapshot("msg-1", snapshot)

        assertEquals(0, manager.surfaceCount)
        assertNull(manager.getSurface("s1"))
        assertNull(manager.getDataModel("s1"))
    }

    @Test
    fun `deleteSurface for non-existent surface is no-op`() {
        val manager = SurfaceStateManager()

        val snapshot = json.decodeFromString<JsonObject>("""
            {
                "operations": [
                    {
                        "deleteSurface": {
                            "surfaceId": "nonexistent"
                        }
                    }
                ]
            }
        """.trimIndent())

        manager.processSnapshot("msg-1", snapshot)

        assertEquals(0, manager.surfaceCount)
    }

    // --- processDelta ---

    @Test
    fun `processDelta adds operation via JSON Patch`() {
        val manager = SurfaceStateManager()

        // First create a surface via snapshot
        val snapshot = json.decodeFromString<JsonObject>("""
            {
                "operations": [
                    {
                        "createSurface": {
                            "surfaceId": "s1",
                            "catalogId": "catalog"
                        }
                    }
                ]
            }
        """.trimIndent())

        manager.processSnapshot("msg-1", snapshot)

        // Delta adds a new updateComponents operation
        val patch = json.decodeFromString<JsonArray>("""
            [
                {
                    "op": "add",
                    "path": "/operations/1",
                    "value": {
                        "updateComponents": {
                            "surfaceId": "s1",
                            "components": [
                                {"id": "root", "component": "Text", "text": "Delta added"}
                            ]
                        }
                    }
                }
            ]
        """.trimIndent())

        manager.processDelta("msg-1", patch)

        val surface = manager.getSurface("s1")
        assertNotNull(surface)
        assertEquals(1, surface.components.size)
        assertNotNull(surface.components["root"])
    }

    @Test
    fun `processDelta ignores non-add operations`() {
        val manager = SurfaceStateManager()

        val patch = json.decodeFromString<JsonArray>("""
            [
                {
                    "op": "remove",
                    "path": "/operations/0"
                }
            ]
        """.trimIndent())

        manager.processDelta("msg-1", patch)

        assertEquals(0, manager.surfaceCount)
    }

    @Test
    fun `processDelta ignores patches to non-operations paths`() {
        val manager = SurfaceStateManager()

        val patch = json.decodeFromString<JsonArray>("""
            [
                {
                    "op": "add",
                    "path": "/metadata/key",
                    "value": "something"
                }
            ]
        """.trimIndent())

        manager.processDelta("msg-1", patch)

        assertEquals(0, manager.surfaceCount)
    }

    // --- getSurfaces ---

    @Test
    fun `getSurfaces returns all active surfaces`() {
        val manager = SurfaceStateManager()

        val snapshot = json.decodeFromString<JsonObject>("""
            {
                "operations": [
                    {
                        "createSurface": {
                            "surfaceId": "s1",
                            "catalogId": "catalog"
                        }
                    },
                    {
                        "createSurface": {
                            "surfaceId": "s2",
                            "catalogId": "catalog"
                        }
                    }
                ]
            }
        """.trimIndent())

        manager.processSnapshot("msg-1", snapshot)

        val surfaces = manager.getSurfaces()
        assertEquals(2, surfaces.size)
        assertTrue(surfaces.containsKey("s1"))
        assertTrue(surfaces.containsKey("s2"))
    }

    // --- clear ---

    @Test
    fun `clear removes all surfaces`() {
        val manager = SurfaceStateManager()

        val snapshot = json.decodeFromString<JsonObject>("""
            {
                "operations": [
                    {
                        "createSurface": {
                            "surfaceId": "s1",
                            "catalogId": "catalog"
                        }
                    }
                ]
            }
        """.trimIndent())

        manager.processSnapshot("msg-1", snapshot)
        assertEquals(1, manager.surfaceCount)

        manager.clear()
        assertEquals(0, manager.surfaceCount)
    }

    // --- UiDefinition integration ---

    @Test
    fun `surface UiDefinition has correct rootComponent`() {
        val manager = SurfaceStateManager()

        val snapshot = json.decodeFromString<JsonObject>("""
            {
                "operations": [
                    {
                        "createSurface": {
                            "surfaceId": "s1",
                            "catalogId": "catalog"
                        }
                    },
                    {
                        "updateComponents": {
                            "surfaceId": "s1",
                            "components": [
                                {"id": "root", "component": "Column", "children": ["text1"]},
                                {"id": "text1", "component": "Text", "text": "Hello"}
                            ]
                        }
                    }
                ]
            }
        """.trimIndent())

        manager.processSnapshot("msg-1", snapshot)

        val surface = manager.getSurface("s1")
        assertNotNull(surface)
        assertNotNull(surface.rootComponent)
        assertEquals("Column", surface.rootComponent!!.widgetType)
    }

    @Test
    fun `surface without root component returns null rootComponent`() {
        val manager = SurfaceStateManager()

        val snapshot = json.decodeFromString<JsonObject>("""
            {
                "operations": [
                    {
                        "createSurface": {
                            "surfaceId": "s1",
                            "catalogId": "catalog"
                        }
                    },
                    {
                        "updateComponents": {
                            "surfaceId": "s1",
                            "components": [
                                {"id": "not-root", "component": "Text", "text": "Hello"}
                            ]
                        }
                    }
                ]
            }
        """.trimIndent())

        manager.processSnapshot("msg-1", snapshot)

        val surface = manager.getSurface("s1")
        assertNotNull(surface)
        assertNull(surface.rootComponent)
    }

    // --- Multiple operations in sequence ---

    @Test
    fun `full lifecycle - create, update components, update data, delete`() {
        val manager = SurfaceStateManager()

        // Create
        manager.processSnapshot("msg-1", json.decodeFromString("""
            {"operations": [{"createSurface": {"surfaceId": "life", "catalogId": "cat"}}]}
        """.trimIndent()))
        assertEquals(1, manager.surfaceCount)

        // Update components
        manager.processSnapshot("msg-2", json.decodeFromString("""
            {"operations": [{"updateComponents": {"surfaceId": "life", "components": [
                {"id": "root", "component": "Text", "text": {"path": "/msg"}}
            ]}}]}
        """.trimIndent()))
        assertNotNull(manager.getSurface("life")?.rootComponent)

        // Update data model
        manager.processSnapshot("msg-3", json.decodeFromString("""
            {"operations": [{"updateDataModel": {"surfaceId": "life", "path": "/msg", "value": "Hello!"}}]}
        """.trimIndent()))
        assertEquals("Hello!", manager.getDataModel("life")?.getString("/msg"))

        // Delete
        manager.processSnapshot("msg-4", json.decodeFromString("""
            {"operations": [{"deleteSurface": {"surfaceId": "life"}}]}
        """.trimIndent()))
        assertEquals(0, manager.surfaceCount)
    }

    @Test
    fun `component weight is preserved through processing`() {
        val manager = SurfaceStateManager()

        val snapshot = json.decodeFromString<JsonObject>("""
            {
                "operations": [
                    {
                        "createSurface": {"surfaceId": "s1", "catalogId": "cat"}
                    },
                    {
                        "updateComponents": {
                            "surfaceId": "s1",
                            "components": [
                                {"id": "col1", "component": "Column", "children": ["a"], "weight": 2}
                            ]
                        }
                    }
                ]
            }
        """.trimIndent())

        manager.processSnapshot("msg-1", snapshot)

        val comp = manager.getSurface("s1")?.components?.get("col1")
        assertNotNull(comp)
        assertEquals(2, comp.weight)
    }
}
