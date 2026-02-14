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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull

/**
 * Represents an A2UI v0.9 operation that modifies the surface state.
 *
 * Operations are received as streaming JSON messages and processed
 * by [SurfaceStateManager] to build [UiDefinition] instances.
 *
 * In v0.9, the four message types are:
 * - `createSurface`: Initializes a surface with catalogId and optional theme
 * - `updateComponents`: Adds or updates component definitions
 * - `updateDataModel`: Updates data at specified path with any JSON value
 * - `deleteSurface`: Removes a surface
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
sealed class A2UIOperation

/**
 * Initializes a new surface for rendering.
 *
 * In v0.9, the root component is identified by convention (component with id "root")
 * rather than an explicit root property.
 *
 * @property surfaceId Unique identifier for the surface
 * @property catalogId URI identifying the component catalog to use
 * @property theme Optional theme parameters for the surface
 * @property sendDataModel When true, client includes full data model in A2A metadata
 */
@Serializable
@SerialName("createSurface")
data class CreateSurface(
    val surfaceId: String,
    val catalogId: String,
    val theme: JsonObject? = null,
    val sendDataModel: Boolean = false
) : A2UIOperation()

/**
 * Updates components in a surface.
 *
 * In v0.9, components use a flat discriminator format:
 * ```json
 * {
 *   "id": "title",
 *   "component": "Text",
 *   "text": "Hello World",
 *   "variant": "h1"
 * }
 * ```
 *
 * @property surfaceId The surface to update
 * @property components List of component definitions to add or update
 */
@Serializable
@SerialName("updateComponents")
data class UpdateComponents(
    val surfaceId: String,
    val components: List<ComponentDef>
) : A2UIOperation()

/**
 * Updates the data model for a surface.
 *
 * In v0.9, data updates use standard JSON values with upsert semantics:
 * - Existing paths are updated
 * - Non-existent paths are created
 * - Omitting value deletes the key
 *
 * @property surfaceId The surface whose data model to update
 * @property path JSON Pointer path (defaults to "/" for entire model)
 * @property value The new value; omitting deletes the key
 */
@Serializable
@SerialName("updateDataModel")
data class UpdateDataModel(
    val surfaceId: String,
    val path: String = "/",
    val value: JsonElement? = null
) : A2UIOperation()

/**
 * Deletes a surface and all its state.
 *
 * @property surfaceId The surface to delete
 */
@Serializable
@SerialName("deleteSurface")
data class DeleteSurface(
    val surfaceId: String
) : A2UIOperation()

/**
 * A component definition in A2UI v0.9 format.
 *
 * v0.9 uses a flat discriminator where `component` is a string type name
 * and all properties are at the top level:
 * ```json
 * {
 *   "id": "my-button",
 *   "component": "Button",
 *   "child": "button-text",
 *   "variant": "primary",
 *   "action": {"event": {"name": "submit"}}
 * }
 * ```
 */
@Serializable
data class ComponentDef(
    val id: String,
    val component: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val weight: Int? = null
) {
    companion object {
        /**
         * Creates a ComponentDef from a raw JsonObject.
         *
         * Parses v0.9 flat format where component is a string discriminator
         * and properties are at the top level:
         * ```json
         * {
         *   "id": "my-id",
         *   "component": "Column",
         *   "children": ["child1", "child2"],
         *   "justify": "spaceBetween"
         * }
         * ```
         */
        fun fromJson(json: JsonObject): ComponentDef {
            val id = json["id"]?.let {
                (it as? kotlinx.serialization.json.JsonPrimitive)?.content
            } ?: error("Component missing 'id'")

            val component = json["component"]?.let {
                (it as? kotlinx.serialization.json.JsonPrimitive)?.content
            } ?: error("Component missing 'component' string")

            // Extract weight from top level
            val weight = json["weight"]?.let {
                (it as? kotlinx.serialization.json.JsonPrimitive)?.intOrNull
            }

            // All other properties (excluding id, component, weight) become the properties object
            val reservedKeys = setOf("id", "component", "weight")
            val propertyMap = json.filterKeys { it !in reservedKeys }

            return ComponentDef(
                id = id,
                component = component,
                properties = JsonObject(propertyMap),
                weight = weight
            )
        }
    }
}

/**
 * Container for A2UI operations in an ACTIVITY_SNAPSHOT event.
 */
@Serializable
data class A2UIActivityContent(
    val operations: List<JsonObject> = emptyList()
)
