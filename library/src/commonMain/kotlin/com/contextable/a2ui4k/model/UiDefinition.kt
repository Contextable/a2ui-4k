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
import kotlinx.serialization.json.JsonObject

/**
 * Represents the complete state of a UI surface in the A2UI v0.9 protocol.
 *
 * A UiDefinition contains all the components that make up a surface,
 * along with metadata about which catalog should be used for rendering.
 *
 * In v0.9, the root component is identified by convention: the component
 * with id "root" serves as the entry point for rendering.
 *
 * @property surfaceId Unique identifier for this surface
 * @property components Map of component ID to [Component] definitions
 * @property catalogId Identifier of the catalog to use for this surface
 * @property theme Optional theme parameters for the surface
 * @property sendDataModel When true, client includes full data model in metadata
 *
 * @see Component
 * @see com.contextable.a2ui4k.state.SurfaceStateManager
 * @see com.contextable.a2ui4k.render.A2UISurface
 */
@Serializable
data class UiDefinition(
    val surfaceId: String,
    val components: Map<String, Component> = emptyMap(),
    val catalogId: String? = null,
    val theme: JsonObject? = null,
    val sendDataModel: Boolean = false
) {
    /**
     * Returns the root component (component with id "root").
     *
     * In v0.9, the root is identified by convention rather than
     * an explicit property.
     */
    val rootComponent: Component?
        get() = components["root"]

    /**
     * Creates a copy with updated components.
     */
    fun withComponents(newComponents: Map<String, Component>): UiDefinition =
        copy(components = components + newComponents)

    companion object {
        /**
         * Creates an empty UiDefinition for a surface.
         */
        fun empty(surfaceId: String): UiDefinition = UiDefinition(surfaceId = surfaceId)
    }
}
