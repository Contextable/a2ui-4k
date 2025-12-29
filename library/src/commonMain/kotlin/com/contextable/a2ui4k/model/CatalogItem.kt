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

import androidx.compose.runtime.Composable
import kotlinx.serialization.json.JsonObject

/**
 * Type alias for a function that builds a child component by ID.
 */
typealias ChildBuilder = @Composable (String) -> Unit

/**
 * Type alias for a function that dispatches UI events.
 */
typealias EventDispatcher = (UiEvent) -> Unit

/**
 * Defines a widget type that can be rendered by A2UI.
 *
 * A CatalogItem maps a widget name (e.g., "Text", "Button") to a
 * Composable function that renders it. The compose function receives
 * the widget's configuration data, functions to build children and
 * dispatch events, and a [DataContext] for resolving path bindings.
 *
 * The A2UI v0.8 specification defines 18 standard widgets in the
 * standard_catalog_definition.json. These are implemented in [CoreCatalog].
 *
 * @property name The widget type identifier (e.g., "Text", "Column", "Button")
 * @property compose The Composable function that renders this widget
 *
 * @see Catalog
 * @see com.contextable.a2ui4k.catalog.CoreCatalog
 */
class CatalogItem(
    val name: String,
    val compose: @Composable (
        componentId: String,
        data: JsonObject,
        buildChild: ChildBuilder,
        dataContext: DataContext,
        onEvent: EventDispatcher
    ) -> Unit
)

/**
 * A collection of [CatalogItem]s that define available widgets.
 *
 * Catalogs can be combined using the `+` operator to create richer
 * widget vocabularies. Each catalog has an optional ID that can be
 * referenced by surfaces via [UiDefinition.catalogId].
 *
 * @property id Optional identifier for this catalog
 * @property items Map of widget name to CatalogItem definition
 *
 * @see CatalogItem
 * @see com.contextable.a2ui4k.catalog.CoreCatalog
 */
class Catalog(
    val id: String? = null,
    val items: Map<String, CatalogItem>
) {
    /**
     * Gets a CatalogItem by widget name.
     */
    operator fun get(name: String): CatalogItem? = items[name]

    /**
     * Combines this catalog with another, with the other's items taking precedence.
     */
    operator fun plus(other: Catalog): Catalog = Catalog(
        id = other.id ?: id,
        items = items + other.items
    )

    companion object {
        /**
         * Creates an empty catalog.
         */
        fun empty(id: String? = null): Catalog = Catalog(id = id, items = emptyMap())

        /**
         * Creates a catalog from a list of CatalogItems.
         */
        fun of(id: String? = null, vararg items: CatalogItem): Catalog = Catalog(
            id = id,
            items = items.associateBy { it.name }
        )
    }
}

/**
 * Provides access to data model values for resolving path bindings.
 *
 * This interface abstracts the data binding system, allowing widgets
 * to read values from and write values to the data model. Paths follow
 * JSON Pointer syntax (RFC 6901).
 *
 * In template contexts (e.g., List with template children), a scoped
 * DataContext is provided where paths are relative to the current item.
 *
 * @see com.contextable.a2ui4k.data.DataModel
 * @see DataReference
 */
interface DataContext {
    /**
     * Gets a string value at the given path.
     */
    fun getString(path: String): String?

    /**
     * Gets a number value at the given path.
     */
    fun getNumber(path: String): Double?

    /**
     * Gets a boolean value at the given path.
     */
    fun getBoolean(path: String): Boolean?

    /**
     * Gets a list of strings at the given path.
     */
    fun getStringList(path: String): List<String>?

    /**
     * Gets the size of an array at the given path.
     * Returns null if the path doesn't point to an array.
     */
    fun getArraySize(path: String): Int?

    /**
     * Gets the keys of an object/map at the given path.
     * Returns null if the path doesn't point to an object.
     */
    fun getObjectKeys(path: String): List<String>?

    /**
     * Updates a value at the given path.
     */
    fun update(path: String, value: Any?)

    /**
     * Creates a child context with a base path.
     * Paths in the child context are relative to the base path.
     */
    fun withBasePath(basePath: String): DataContext
}
