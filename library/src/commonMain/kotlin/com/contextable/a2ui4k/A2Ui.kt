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

/**
 * A2UI - Agent-to-UI Components for Compose Multiplatform
 *
 * This library provides Compose Multiplatform components for rendering
 * A2UI (Agent-to-UI) surfaces. It implements the component model from
 * the A2UI protocol, enabling AI agents to generate dynamic UIs.
 *
 * a2ui-4k currently implements the A2UI v0.9 specification. The A2UI protocol
 * is under active development at [google/A2UI](https://github.com/google/A2UI).
 *
 * ## Quick Start
 *
 * ```kotlin
 * @Composable
 * fun MyScreen(definition: UiDefinition) {
 *     A2UISurface(
 *         definition = definition,
 *         catalog = CoreCatalog,
 *         onEvent = { event ->
 *             // Handle user interactions
 *         }
 *     )
 * }
 * ```
 *
 * ## Key Components
 *
 * - [A2UISurface][com.contextable.a2ui4k.render.A2UISurface]: Main composable for rendering a UI definition
 * - [CoreCatalog][com.contextable.a2ui4k.catalog.CoreCatalog]: Built-in widgets (Text, Column, Row, List, Card, etc.)
 * - [Catalog]: Widget registry that can be extended with custom widgets
 * - [DataModel]: Reactive data store for path-based bindings
 * - [UiDefinition]: Component tree definition received from AI agent
 * - [SurfaceStateManager][com.contextable.a2ui4k.state.SurfaceStateManager]: Processes A2UI operations into UI definitions
 *
 * ## Custom Widgets
 *
 * Register custom widgets by creating a [CatalogItem] and combining catalogs:
 *
 * ```kotlin
 * val MyWidget = CatalogItem("MyWidget") { id, data, buildChild, dataContext, onEvent ->
 *     // Compose implementation
 * }
 *
 * val myCatalog = Catalog.of("custom", MyWidget)
 * val combined = CoreCatalog + myCatalog
 * ```
 *
 * @see com.contextable.a2ui4k.render.A2UISurface
 * @see com.contextable.a2ui4k.catalog.CoreCatalog
 * @see com.contextable.a2ui4k.state.SurfaceStateManager
 */
package com.contextable.a2ui4k

// Re-export main types for convenient imports

// Model types
public typealias Component = com.contextable.a2ui4k.model.Component
public typealias UiDefinition = com.contextable.a2ui4k.model.UiDefinition
public typealias UiEvent = com.contextable.a2ui4k.model.UiEvent
public typealias ActionEvent = com.contextable.a2ui4k.model.ActionEvent
public typealias DataChangeEvent = com.contextable.a2ui4k.model.DataChangeEvent
public typealias ValidationError = com.contextable.a2ui4k.model.ValidationError
public typealias Catalog = com.contextable.a2ui4k.model.Catalog
public typealias CatalogItem = com.contextable.a2ui4k.model.CatalogItem
public typealias DataContext = com.contextable.a2ui4k.model.DataContext

// Data binding
public typealias DataModel = com.contextable.a2ui4k.data.DataModel

// Catalog
public typealias CoreCatalogItems = com.contextable.a2ui4k.catalog.CoreCatalogItems
public typealias AvailableIcons = com.contextable.a2ui4k.catalog.AvailableIcons
