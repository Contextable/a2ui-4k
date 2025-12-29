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

import kotlinx.serialization.Serializable

/**
 * Represents the complete state of a UI surface in the A2UI protocol.
 *
 * A UiDefinition contains all the components that make up a surface,
 * along with metadata about which component is the root and which
 * catalog should be used for rendering.
 *
 * In the A2UI v0.8 protocol, UiDefinitions are built from `beginRendering`
 * and `surfaceUpdate` operations processed by [SurfaceStateManager].
 *
 * @property surfaceId Unique identifier for this surface
 * @property components Map of component ID to [Component] definitions
 * @property root The ID of the root component to start rendering from
 * @property catalogId Optional identifier of the catalog to use for this surface
 *
 * @see Component
 * @see com.contextable.a2ui4k.state.SurfaceStateManager
 * @see com.contextable.a2ui4k.render.A2UISurface
 */
@Serializable
data class UiDefinition(
    val surfaceId: String,
    val components: Map<String, Component> = emptyMap(),
    val root: String? = null,
    val catalogId: String? = null
) {
    /**
     * Returns the root component if it exists.
     */
    val rootComponent: Component?
        get() = root?.let { components[it] }

    /**
     * Creates a copy with updated components.
     */
    fun withComponents(newComponents: Map<String, Component>): UiDefinition =
        copy(components = components + newComponents)

    /**
     * Creates a copy with the root set.
     */
    fun withRoot(rootId: String, catalog: String? = null): UiDefinition =
        copy(root = rootId, catalogId = catalog ?: catalogId)

    companion object {
        /**
         * Creates an empty UiDefinition for a surface.
         */
        fun empty(surfaceId: String): UiDefinition = UiDefinition(surfaceId = surfaceId)
    }
}
