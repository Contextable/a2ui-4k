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

import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for UiDefinition - the complete state of a UI surface.
 */
class UiDefinitionTest {

    @Test
    fun `empty creates UiDefinition with surfaceId`() {
        val def = UiDefinition.empty("test-surface")

        assertEquals("test-surface", def.surfaceId)
        assertTrue(def.components.isEmpty())
        assertNull(def.root)
        assertNull(def.catalogId)
    }

    @Test
    fun `rootComponent returns null when root not set`() {
        val def = UiDefinition(surfaceId = "test")

        assertNull(def.rootComponent)
    }

    @Test
    fun `rootComponent returns null when root ID not in components`() {
        val def = UiDefinition(
            surfaceId = "test",
            root = "missing-root"
        )

        assertNull(def.rootComponent)
    }

    @Test
    fun `rootComponent returns component when found`() {
        val component = Component.create("root", "Column", JsonObject(emptyMap()))
        val def = UiDefinition(
            surfaceId = "test",
            components = mapOf("root" to component),
            root = "root"
        )

        assertNotNull(def.rootComponent)
        assertEquals("root", def.rootComponent?.id)
        assertEquals("Column", def.rootComponent?.widgetType)
    }

    @Test
    fun `withComponents adds new components`() {
        val initial = UiDefinition(surfaceId = "test")
        val component = Component.create("text1", "Text", JsonObject(emptyMap()))

        val updated = initial.withComponents(mapOf("text1" to component))

        assertEquals(1, updated.components.size)
        assertNotNull(updated.components["text1"])
    }

    @Test
    fun `withComponents merges with existing components`() {
        val comp1 = Component.create("comp1", "Text", JsonObject(emptyMap()))
        val comp2 = Component.create("comp2", "Button", JsonObject(emptyMap()))

        val initial = UiDefinition(
            surfaceId = "test",
            components = mapOf("comp1" to comp1)
        )

        val updated = initial.withComponents(mapOf("comp2" to comp2))

        assertEquals(2, updated.components.size)
        assertNotNull(updated.components["comp1"])
        assertNotNull(updated.components["comp2"])
    }

    @Test
    fun `withComponents replaces existing component with same ID`() {
        val comp1 = Component.create("comp1", "Text", JsonObject(emptyMap()))
        val comp1Updated = Component.create("comp1", "Button", JsonObject(emptyMap()))

        val initial = UiDefinition(
            surfaceId = "test",
            components = mapOf("comp1" to comp1)
        )

        val updated = initial.withComponents(mapOf("comp1" to comp1Updated))

        assertEquals(1, updated.components.size)
        assertEquals("Button", updated.components["comp1"]?.widgetType)
    }

    @Test
    fun `withRoot sets root ID`() {
        val initial = UiDefinition(surfaceId = "test")

        val updated = initial.withRoot("my-root")

        assertEquals("my-root", updated.root)
    }

    @Test
    fun `withRoot sets catalogId when provided`() {
        val initial = UiDefinition(surfaceId = "test")

        val updated = initial.withRoot("root", "custom-catalog")

        assertEquals("root", updated.root)
        assertEquals("custom-catalog", updated.catalogId)
    }

    @Test
    fun `withRoot preserves existing catalogId when not provided`() {
        val initial = UiDefinition(
            surfaceId = "test",
            catalogId = "existing-catalog"
        )

        val updated = initial.withRoot("root")

        assertEquals("root", updated.root)
        assertEquals("existing-catalog", updated.catalogId)
    }

    @Test
    fun `UiDefinition preserves all properties`() {
        val comp = Component.create("text", "Text", JsonObject(emptyMap()))
        val def = UiDefinition(
            surfaceId = "main-surface",
            components = mapOf("text" to comp),
            root = "text",
            catalogId = "my-catalog"
        )

        assertEquals("main-surface", def.surfaceId)
        assertEquals(1, def.components.size)
        assertEquals("text", def.root)
        assertEquals("my-catalog", def.catalogId)
    }
}
