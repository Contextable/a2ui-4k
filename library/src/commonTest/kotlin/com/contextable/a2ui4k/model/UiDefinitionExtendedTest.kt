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
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Extended tests for [UiDefinition] and [Catalog] — the non-Compose data model
 * underlying A2UiSurface rendering.
 */
class UiDefinitionExtendedTest {

    // --- UiDefinition ---

    @Test
    fun `rootComponent returns component with id root`() {
        val root = Component.create("root", "Column", JsonObject(emptyMap()))
        val child = Component.create("child-1", "Text", JsonObject(mapOf("text" to JsonPrimitive("Hello"))))

        val definition = UiDefinition(
            surfaceId = "s1",
            components = mapOf("root" to root, "child-1" to child)
        )

        assertNotNull(definition.rootComponent)
        assertEquals("Column", definition.rootComponent!!.widgetType)
    }

    @Test
    fun `rootComponent returns null when no root component exists`() {
        val child = Component.create("child-1", "Text", JsonObject(mapOf("text" to JsonPrimitive("Hello"))))

        val definition = UiDefinition(
            surfaceId = "s1",
            components = mapOf("child-1" to child)
        )

        assertNull(definition.rootComponent)
    }

    @Test
    fun `empty UiDefinition has no components`() {
        val definition = UiDefinition.empty("s1")

        assertEquals("s1", definition.surfaceId)
        assertTrue(definition.components.isEmpty())
        assertNull(definition.rootComponent)
        assertNull(definition.catalogId)
        assertNull(definition.theme)
        assertEquals(false, definition.sendDataModel)
    }

    @Test
    fun `withComponents merges new components into existing`() {
        val original = UiDefinition(
            surfaceId = "s1",
            components = mapOf(
                "root" to Component.create("root", "Column", JsonObject(emptyMap()))
            )
        )

        val newComp = Component.create("title", "Text", JsonObject(mapOf("text" to JsonPrimitive("Title"))))
        val updated = original.withComponents(mapOf("title" to newComp))

        assertEquals(2, updated.components.size)
        assertNotNull(updated.components["root"])
        assertNotNull(updated.components["title"])
    }

    @Test
    fun `withComponents overwrites existing component with same id`() {
        val original = UiDefinition(
            surfaceId = "s1",
            components = mapOf(
                "title" to Component.create("title", "Text", JsonObject(mapOf("text" to JsonPrimitive("Old"))))
            )
        )

        val newTitle = Component.create("title", "Text", JsonObject(mapOf("text" to JsonPrimitive("New"))))
        val updated = original.withComponents(mapOf("title" to newTitle))

        assertEquals(1, updated.components.size)
        assertEquals("New", (updated.components["title"]!!.widgetData["text"] as? JsonPrimitive)?.content)
    }

    @Test
    fun `UiDefinition preserves all metadata`() {
        val theme = JsonObject(mapOf("primaryColor" to JsonPrimitive("#00FF00")))
        val definition = UiDefinition(
            surfaceId = "surface-main",
            catalogId = "https://example.com/catalog.json",
            theme = theme,
            sendDataModel = true
        )

        assertEquals("surface-main", definition.surfaceId)
        assertEquals("https://example.com/catalog.json", definition.catalogId)
        assertEquals(theme, definition.theme)
        assertTrue(definition.sendDataModel)
    }

    // --- Catalog ---

    @Test
    fun `Catalog empty creates catalog with no items`() {
        val catalog = Catalog.empty("test")

        assertEquals("test", catalog.id)
        assertNull(catalog["Text"])
    }

    @Test
    fun `Catalog get returns null for unknown widget`() {
        val catalog = Catalog.empty()

        assertNull(catalog["NonExistent"])
    }

    @Test
    fun `Catalog of creates catalog from CatalogItems`() {
        val textItem = CatalogItem("Text") { _, _, _, _, _ -> }
        val buttonItem = CatalogItem("Button") { _, _, _, _, _ -> }

        val catalog = Catalog.of("test-catalog", textItem, buttonItem)

        assertEquals("test-catalog", catalog.id)
        assertNotNull(catalog["Text"])
        assertNotNull(catalog["Button"])
        assertEquals("Text", catalog["Text"]!!.name)
        assertEquals("Button", catalog["Button"]!!.name)
    }

    @Test
    fun `Catalog plus merges catalogs with right side taking precedence`() {
        val item1 = CatalogItem("Text") { _, _, _, _, _ -> }
        val item2 = CatalogItem("Button") { _, _, _, _, _ -> }
        val item3 = CatalogItem("Icon") { _, _, _, _, _ -> }

        val catalogA = Catalog.of("a", item1, item2)
        val catalogB = Catalog.of("b", item3)

        val merged = catalogA + catalogB

        assertEquals("b", merged.id) // Right side ID takes precedence
        assertNotNull(merged["Text"])
        assertNotNull(merged["Button"])
        assertNotNull(merged["Icon"])
    }

    @Test
    fun `Catalog plus right side overwrites left side items`() {
        val original = CatalogItem("Text") { _, _, _, _, _ -> }
        val override = CatalogItem("Text") { _, _, _, _, _ -> }

        val catalogA = Catalog.of("a", original)
        val catalogB = Catalog.of("b", override)

        val merged = catalogA + catalogB

        // The merged catalog should have the overridden item
        assertEquals("Text", merged["Text"]!!.name)
    }

    @Test
    fun `Catalog plus preserves left id when right id is null`() {
        val catalogA = Catalog.of("a", CatalogItem("Text") { _, _, _, _, _ -> })
        val catalogB = Catalog(id = null, items = emptyMap())

        val merged = catalogA + catalogB

        assertEquals("a", merged.id)
    }

    // --- Component ---

    @Test
    fun `Component create sets all fields correctly`() {
        val data = JsonObject(mapOf("text" to JsonPrimitive("Hello")))
        val component = Component.create("t1", "Text", data, weight = 3)

        assertEquals("t1", component.id)
        assertEquals("Text", component.widgetType)
        assertEquals("Text", component.componentType)
        assertEquals(data, component.widgetData)
        assertEquals(data, component.properties)
        assertEquals(3, component.weight)
    }

    @Test
    fun `Component fromComponentDef maps all fields`() {
        val props = JsonObject(mapOf("variant" to JsonPrimitive("filled")))
        val def = ComponentDef(
            id = "btn-1",
            component = "Button",
            properties = props,
            weight = 1
        )

        val component = Component.fromComponentDef(def)

        assertEquals("btn-1", component.id)
        assertEquals("Button", component.widgetType)
        assertEquals(props, component.widgetData)
        assertEquals(1, component.weight)
    }

    @Test
    fun `Component default weight is null`() {
        val component = Component.create("c1", "Divider", JsonObject(emptyMap()))

        assertNull(component.weight)
    }
}
