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

import com.contextable.a2ui4k.data.DataModel
import com.contextable.a2ui4k.model.A2UIActivityContent
import com.contextable.a2ui4k.model.A2UIOperation
import com.contextable.a2ui4k.model.CreateSurface
import com.contextable.a2ui4k.model.Component
import com.contextable.a2ui4k.model.ComponentDef
import com.contextable.a2ui4k.model.UpdateDataModel
import com.contextable.a2ui4k.model.DeleteSurface
import com.contextable.a2ui4k.model.UpdateComponents
import com.contextable.a2ui4k.model.UiDefinition
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Manages the state of A2UI surfaces.
 *
 * Receives A2UI v0.9 operations from streaming messages and builds
 * [UiDefinition] instances that can be rendered by A2UISurface.
 *
 * In the A2UI v0.9 protocol, the manager handles four operation types:
 * - `createSurface`: Initializes a surface with catalogId and optional theme
 * - `updateComponents`: Adds or updates component definitions
 * - `updateDataModel`: Updates data at specified path with any JSON value
 * - `deleteSurface`: Removes a surface
 *
 * **Note:** This class processes parsed JSON operations. Stream parsing
 * is left to the application's transport layer.
 *
 * ## Usage
 *
 * ```kotlin
 * val manager = SurfaceStateManager()
 *
 * // Process operations from streaming messages
 * manager.processSnapshot(messageId, activityContent)
 * manager.processDelta(messageId, jsonPatch)
 *
 * // Get current surfaces for rendering
 * val surfaces = manager.getSurfaces()
 * val dataModel = manager.getDataModel(surfaceId)
 * ```
 *
 * @see UiDefinition
 * @see com.contextable.a2ui4k.data.DataModel
 * @see com.contextable.a2ui4k.render.A2UISurface
 */
class SurfaceStateManager {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Internal state for a single surface.
     */
    private data class SurfaceState(
        val surfaceId: String,
        var catalogId: String? = null,
        var theme: JsonObject? = null,
        var sendDataModel: Boolean = false,
        val components: MutableMap<String, Component> = mutableMapOf(),
        val dataModel: DataModel = DataModel()
    ) {
        fun toUiDefinition(): UiDefinition = UiDefinition(
            surfaceId = surfaceId,
            components = components.toMap(),
            catalogId = catalogId,
            theme = theme,
            sendDataModel = sendDataModel
        )
    }

    private val surfaces = mutableMapOf<String, SurfaceState>()

    /**
     * Processes an ACTIVITY_SNAPSHOT event for a2ui-surface.
     *
     * @param messageId The message ID associated with this activity
     * @param content The activity content containing operations
     */
    fun processSnapshot(messageId: String, content: JsonElement) {
        val contentObj = content.jsonObject
        val operationsArray = contentObj["operations"]?.jsonArray ?: return

        for (opElement in operationsArray) {
            val opObj = opElement.jsonObject
            processOperationObject(opObj)
        }
    }

    /**
     * Processes an ACTIVITY_DELTA event for a2ui-surface.
     *
     * The patch contains JSON Patch operations that may add new operations
     * to the surface state.
     *
     * @param messageId The message ID associated with this activity
     * @param patch The JSON Patch array
     */
    fun processDelta(messageId: String, patch: JsonArray) {
        for (patchOp in patch) {
            val patchObj = patchOp.jsonObject
            val op = (patchObj["op"] as? kotlinx.serialization.json.JsonPrimitive)?.content
            val path = (patchObj["path"] as? kotlinx.serialization.json.JsonPrimitive)?.content
            val value = patchObj["value"]

            // Handle add operations to /operations/-
            if (op == "add" && path?.startsWith("/operations/") == true && value != null) {
                val opObj = value.jsonObject
                processOperationObject(opObj)
            }
        }
    }

    /**
     * Processes a raw operation JSON object.
     *
     * v0.9 message types: createSurface, updateComponents, updateDataModel, deleteSurface
     */
    private fun processOperationObject(opObj: JsonObject) {
        when {
            opObj.containsKey("createSurface") -> {
                val data = opObj["createSurface"]!!.jsonObject
                handleCreateSurface(data)
            }
            opObj.containsKey("updateComponents") -> {
                val data = opObj["updateComponents"]!!.jsonObject
                handleUpdateComponents(data)
            }
            opObj.containsKey("updateDataModel") -> {
                val data = opObj["updateDataModel"]!!.jsonObject
                handleUpdateDataModel(data)
            }
            opObj.containsKey("deleteSurface") -> {
                val data = opObj["deleteSurface"]!!.jsonObject
                handleDeleteSurface(data)
            }
        }
    }

    private fun handleCreateSurface(data: JsonObject) {
        val surfaceId = (data["surfaceId"] as? kotlinx.serialization.json.JsonPrimitive)?.content ?: return
        val catalogId = (data["catalogId"] as? kotlinx.serialization.json.JsonPrimitive)?.content
        val theme = data["theme"]?.jsonObject
        val sendDataModel = (data["sendDataModel"] as? kotlinx.serialization.json.JsonPrimitive)?.let {
            it.content.toBooleanStrictOrNull()
        } ?: false

        val state = surfaces.getOrPut(surfaceId) { SurfaceState(surfaceId) }
        state.catalogId = catalogId
        state.theme = theme
        state.sendDataModel = sendDataModel
    }

    private fun handleUpdateComponents(data: JsonObject) {
        val surfaceId = (data["surfaceId"] as? kotlinx.serialization.json.JsonPrimitive)?.content ?: return
        val componentsArray = data["components"]?.jsonArray ?: return

        val state = surfaces.getOrPut(surfaceId) { SurfaceState(surfaceId) }

        for (compElement in componentsArray) {
            val compObj = compElement.jsonObject
            val componentDef = ComponentDef.fromJson(compObj)
            val component = Component.fromComponentDef(componentDef)
            state.components[component.id] = component
        }
    }

    private fun handleUpdateDataModel(data: JsonObject) {
        val surfaceId = (data["surfaceId"] as? kotlinx.serialization.json.JsonPrimitive)?.content ?: return
        val path = (data["path"] as? kotlinx.serialization.json.JsonPrimitive)?.content ?: "/"
        val value = data["value"]

        val state = surfaces.getOrPut(surfaceId) { SurfaceState(surfaceId) }

        if (value != null) {
            // v0.9: value is any JSON - set it directly at the path
            state.dataModel.update(path, value)
        } else {
            // Omitting value deletes the key
            state.dataModel.delete(path)
        }
    }

    private fun handleDeleteSurface(data: JsonObject) {
        val surfaceId = (data["surfaceId"] as? kotlinx.serialization.json.JsonPrimitive)?.content ?: return
        surfaces.remove(surfaceId)
    }

    /**
     * Returns a map of all active surfaces.
     */
    fun getSurfaces(): Map<String, UiDefinition> {
        return surfaces.mapValues { it.value.toUiDefinition() }
    }

    /**
     * Returns the data model for a specific surface.
     */
    fun getDataModel(surfaceId: String): DataModel? {
        return surfaces[surfaceId]?.dataModel
    }

    /**
     * Returns a specific surface definition.
     */
    fun getSurface(surfaceId: String): UiDefinition? {
        return surfaces[surfaceId]?.toUiDefinition()
    }

    /**
     * Clears all surfaces.
     */
    fun clear() {
        surfaces.clear()
    }

    /**
     * Returns the number of active surfaces.
     */
    val surfaceCount: Int
        get() = surfaces.size
}
