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
import com.contextable.a2ui4k.model.Component
import com.contextable.a2ui4k.model.ComponentDef
import com.contextable.a2ui4k.model.UiDefinition
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Manages A2UI v0.9 surface state.
 *
 * The v0.9 protocol is transport-agnostic: the server streams JSON objects
 * (JSONL, SSE frames, WebSocket messages, MCP tool outputs, etc.), each
 * carrying a `version` tag and exactly one operation key:
 *
 * - `createSurface`
 * - `updateComponents`
 * - `updateDataModel`
 * - `deleteSurface`
 *
 * Pass each decoded message to [processMessage]. This class does no I/O
 * and has no knowledge of any specific transport.
 */
class SurfaceStateManager {

    /** Protocol version string this manager speaks. */
    val protocolVersion: String = PROTOCOL_VERSION

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
     * Processes a single server→client message.
     *
     * The message envelope must include `version: "v0.9"` and exactly one of
     * the four operation keys. Returns `true` if the message was recognized
     * and dispatched, `false` if it was ignored (e.g. wrong version).
     */
    fun processMessage(message: JsonObject): Boolean {
        val version = (message["version"] as? JsonPrimitive)?.contentOrNullSafe
        if (version != null && version != PROTOCOL_VERSION) {
            return false
        }
        return when {
            message.containsKey("createSurface") -> {
                handleCreateSurface(message["createSurface"]!!.jsonObject)
                true
            }
            message.containsKey("updateComponents") -> {
                handleUpdateComponents(message["updateComponents"]!!.jsonObject)
                true
            }
            message.containsKey("updateDataModel") -> {
                handleUpdateDataModel(message["updateDataModel"]!!.jsonObject)
                true
            }
            message.containsKey("deleteSurface") -> {
                handleDeleteSurface(message["deleteSurface"]!!.jsonObject)
                true
            }
            else -> false
        }
    }

    private fun handleCreateSurface(data: JsonObject) {
        val surfaceId = data["surfaceId"]?.asStringOrNull() ?: return
        val catalogId = data["catalogId"]?.asStringOrNull()
        val theme = data["theme"] as? JsonObject
        val sendDataModel = (data["sendDataModel"] as? JsonPrimitive)
            ?.contentOrNullSafe?.toBooleanStrictOrNull() ?: false

        val state = surfaces.getOrPut(surfaceId) { SurfaceState(surfaceId) }
        state.catalogId = catalogId
        state.theme = theme
        state.sendDataModel = sendDataModel
    }

    private fun handleUpdateComponents(data: JsonObject) {
        val surfaceId = data["surfaceId"]?.asStringOrNull() ?: return
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
        val surfaceId = data["surfaceId"]?.asStringOrNull() ?: return
        val path = data["path"]?.asStringOrNull() ?: "/"

        val state = surfaces.getOrPut(surfaceId) { SurfaceState(surfaceId) }

        // Spec distinguishes absent `value` (delete the key) from explicit
        // `"value": null` (set the key to JsonNull).
        if (!data.containsKey("value")) {
            state.dataModel.delete(path)
            return
        }
        state.dataModel.update(path, data["value"] ?: JsonNull)
    }

    private fun handleDeleteSurface(data: JsonObject) {
        val surfaceId = data["surfaceId"]?.asStringOrNull() ?: return
        surfaces.remove(surfaceId)
    }

    /** Returns a snapshot map of all active surfaces. */
    fun getSurfaces(): Map<String, UiDefinition> =
        surfaces.mapValues { it.value.toUiDefinition() }

    /** Returns the data model for a specific surface, or `null` if not present. */
    fun getDataModel(surfaceId: String): DataModel? = surfaces[surfaceId]?.dataModel

    /** Returns a specific surface definition, or `null` if not present. */
    fun getSurface(surfaceId: String): UiDefinition? =
        surfaces[surfaceId]?.toUiDefinition()

    /**
     * Returns the `a2uiClientDataModel` envelope as defined by v0.9, or `null`
     * if no surface has `sendDataModel = true`. Callers may attach this object
     * to any outbound message metadata their transport supports.
     */
    fun buildClientDataModel(): JsonObject? {
        val active = surfaces.values.filter { it.sendDataModel }
        if (active.isEmpty()) return null
        return buildJsonObject {
            put("version", JsonPrimitive(PROTOCOL_VERSION))
            put("surfaces", JsonObject(active.associate { it.surfaceId to it.dataModel.currentData }))
        }
    }

    /** Clears all surfaces. */
    fun clear() {
        surfaces.clear()
    }

    /** Number of active surfaces. */
    val surfaceCount: Int
        get() = surfaces.size

    companion object {
        const val PROTOCOL_VERSION: String = "v0.9"
    }
}

private fun kotlinx.serialization.json.JsonElement.asStringOrNull(): String? =
    (this as? JsonPrimitive)?.takeIf { it.isString }?.content

private val JsonPrimitive.contentOrNullSafe: String?
    get() = if (this is JsonNull) null else content
