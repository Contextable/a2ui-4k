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
 *
 * In v0.9, root is identified by convention (component with id "root")
 * rather than an explicit root property.
 */
class UiDefinitionTest {

    @Test
    fun `empty creates UiDefinition with surfaceId`() {
        val def = UiDefinition.empty("test-surface")

        assertEquals("test-surface", def.surfaceId)
        assertTrue(def.components.isEmpty())
        assertNull(def.catalogId)
    }

    @Test
    fun `rootComponent returns null when no root component exists`() {
        val def = UiDefinition(surfaceId = "test")

        assertNull(def.rootComponent)
    }

    @Test
    fun `rootComponent returns null when root ID not in components`() {
        val comp = Component.create("not-root", "Text", JsonObject(emptyMap()))
        val def = UiDefinition(
            surfaceId = "test",
            components = mapOf("not-root" to comp)
        )

        assertNull(def.rootComponent)
    }

    @Test
    fun `rootComponent returns component with id root`() {
        val component = Component.create("root", "Column", JsonObject(emptyMap()))
        val def = UiDefinition(
            surfaceId = "test",
            components = mapOf("root" to component)
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
    fun `UiDefinition preserves all properties`() {
        val comp = Component.create("root", "Text", JsonObject(emptyMap()))
        val def = UiDefinition(
            surfaceId = "main-surface",
            components = mapOf("root" to comp),
            catalogId = "my-catalog"
        )

        assertEquals("main-surface", def.surfaceId)
        assertEquals(1, def.components.size)
        assertNotNull(def.rootComponent)
        assertEquals("my-catalog", def.catalogId)
    }
}
