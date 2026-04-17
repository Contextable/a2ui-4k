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
import com.contextable.a2ui4k.extension.A2UIExtension
import com.contextable.a2ui4k.model.Component
import com.contextable.a2ui4k.model.ComponentDef
import com.contextable.a2ui4k.model.ProtocolVersion
import com.contextable.a2ui4k.model.UiDefinition
import com.contextable.a2ui4k.protocol.v08.V08MessageTranscoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Manages A2UI surface state for both v0.9 and v0.8 wire protocols.
 *
 * ## v0.9 (native)
 *
 * The server streams individual JSON objects each carrying a `version` tag
 * and exactly one operation key:
 *
 * - `createSurface`
 * - `updateComponents`
 * - `updateDataModel`
 * - `deleteSurface`
 *
 * ## v0.8 (transcoded)
 *
 * The server sends `ACTIVITY_SNAPSHOT` / `ACTIVITY_DELTA` envelopes. Each
 * incoming v0.8 envelope is transcoded through [V08MessageTranscoder] into
 * zero-or-more v0.9-shape messages and processed identically to native v0.9.
 * A surface created this way is tagged with [ProtocolVersion.V0_8] so
 * outbound client events can be serialized in the matching wire shape.
 *
 * Pass each decoded message to [processMessage]. This class does no I/O
 * and has no knowledge of any specific transport.
 */
class SurfaceStateManager {

    /** Protocol version string this manager speaks natively (v0.9). */
    val protocolVersion: String = PROTOCOL_VERSION

    private val v08Transcoder = V08MessageTranscoder()

    private data class SurfaceState(
        val surfaceId: String,
        var catalogId: String? = null,
        var theme: JsonObject? = null,
        var sendDataModel: Boolean = false,
        var rootComponentId: String? = null,
        var protocolVersion: ProtocolVersion = ProtocolVersion.V0_9,
        val components: MutableMap<String, Component> = mutableMapOf(),
        val dataModel: DataModel = DataModel()
    ) {
        fun toUiDefinition(): UiDefinition = UiDefinition(
            surfaceId = surfaceId,
            components = components.toMap(),
            catalogId = catalogId,
            theme = theme,
            sendDataModel = sendDataModel,
            rootComponentId = rootComponentId,
            protocolVersion = protocolVersion
        )
    }

    private val surfaces = mutableMapOf<String, SurfaceState>()

    /**
     * Processes a single server→client message.
     *
     * Recognized shapes:
     * - v0.9 envelope `{"version":"v0.9", "<op>":{…}}`
     * - v0.9 envelope with no version field (operation key presence is
     *   sufficient)
     * - v0.8 `ACTIVITY_SNAPSHOT` / `ACTIVITY_DELTA` envelope (transcoded
     *   internally to v0.9)
     *
     * Returns `true` if the message was recognized and dispatched, `false`
     * if it was ignored (wrong version, unknown op, etc.). A single v0.8
     * envelope may dispatch multiple underlying operations — in that case
     * returns `true` if any of them were recognized.
     */
    fun processMessage(message: JsonObject): Boolean {
        // v0.8 dispatch: transcode to v0.9 shape, then recurse per transcoded op.
        if (v08Transcoder.isV08Envelope(message)) {
            val transcoded = v08Transcoder.transcode(message)
            if (transcoded.isEmpty()) return false
            var any = false
            for (sub in transcoded) {
                if (processV09Message(sub, ProtocolVersion.V0_8)) any = true
            }
            return any
        }
        return processV09Message(message, ProtocolVersion.V0_9)
    }

    /**
     * Processes a v0.9-shape envelope. [sourceVersion] is the wire version
     * that actually produced this message — v0.9 for native, v0.8 for
     * transcoded. Used to tag each surface so outbound events get the right
     * wire shape.
     */
    private fun processV09Message(
        message: JsonObject,
        sourceVersion: ProtocolVersion
    ): Boolean {
        val version = (message["version"] as? JsonPrimitive)?.contentOrNullSafe
        if (version != null && version != PROTOCOL_VERSION) {
            return false
        }
        return when {
            message.containsKey("createSurface") -> {
                handleCreateSurface(message["createSurface"]!!.jsonObject, sourceVersion)
                true
            }
            message.containsKey("updateComponents") -> {
                handleUpdateComponents(message["updateComponents"]!!.jsonObject, sourceVersion)
                true
            }
            message.containsKey("updateDataModel") -> {
                handleUpdateDataModel(message["updateDataModel"]!!.jsonObject, sourceVersion)
                true
            }
            message.containsKey("deleteSurface") -> {
                handleDeleteSurface(message["deleteSurface"]!!.jsonObject)
                true
            }
            else -> false
        }
    }

    private fun handleCreateSurface(data: JsonObject, sourceVersion: ProtocolVersion) {
        val surfaceId = data["surfaceId"]?.asStringOrNull() ?: return
        val catalogId = data["catalogId"]?.asStringOrNull()
        val theme = data["theme"] as? JsonObject
        val sendDataModel = (data["sendDataModel"] as? JsonPrimitive)
            ?.contentOrNullSafe?.toBooleanStrictOrNull() ?: false
        val rootComponentId = data["rootComponentId"]?.asStringOrNull()

        // If the catalog URI is the v0.8 one, that overrides the source
        // version — but normally they agree.
        val version = when (catalogId) {
            A2UIExtension.STANDARD_CATALOG_URI_V08 -> ProtocolVersion.V0_8
            A2UIExtension.STANDARD_CATALOG_URI -> ProtocolVersion.V0_9
            else -> sourceVersion
        }

        val state = surfaces.getOrPut(surfaceId) { SurfaceState(surfaceId) }
        state.catalogId = catalogId
        state.theme = theme
        state.sendDataModel = sendDataModel
        state.rootComponentId = rootComponentId
        state.protocolVersion = version
    }

    private fun handleUpdateComponents(data: JsonObject, sourceVersion: ProtocolVersion) {
        val surfaceId = data["surfaceId"]?.asStringOrNull() ?: return
        val componentsArray = data["components"]?.jsonArray ?: return

        val state = surfaces.getOrPut(surfaceId) { SurfaceState(surfaceId) }
        if (state.protocolVersion == ProtocolVersion.V0_9 && sourceVersion == ProtocolVersion.V0_8) {
            state.protocolVersion = ProtocolVersion.V0_8
        }

        for (compElement in componentsArray) {
            val compObj = compElement.jsonObject
            val componentDef = ComponentDef.fromJson(compObj)
            val component = Component.fromComponentDef(componentDef)
            state.components[component.id] = component
        }
    }

    private fun handleUpdateDataModel(data: JsonObject, sourceVersion: ProtocolVersion) {
        val surfaceId = data["surfaceId"]?.asStringOrNull() ?: return
        val path = data["path"]?.asStringOrNull() ?: "/"

        val state = surfaces.getOrPut(surfaceId) { SurfaceState(surfaceId) }
        if (state.protocolVersion == ProtocolVersion.V0_9 && sourceVersion == ProtocolVersion.V0_8) {
            state.protocolVersion = ProtocolVersion.V0_8
        }

        // Spec distinguishes absent `value` (delete the key) from explicit
        // `"value": null` (set the key to JsonNull). v0.8 never sent an
        // absent-value update, so both paths behave identically there.
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
     * Returns the [ProtocolVersion] a given surface is speaking, or `null` if
     * the surface is not registered. Callers that serialize client events
     * (`ActionEvent.toClientMessage`) use this to pick the right wire shape.
     */
    fun getSurfaceProtocolVersion(surfaceId: String): ProtocolVersion? =
        surfaces[surfaceId]?.protocolVersion

    /**
     * Returns the `a2uiClientDataModel` envelope as defined by v0.9, or `null`
     * if no surface has `sendDataModel = true`. Callers may attach this object
     * to any outbound message metadata their transport supports.
     *
     * Not emitted for v0.8 surfaces — v0.8 has no equivalent envelope.
     */
    fun buildClientDataModel(): JsonObject? {
        val active = surfaces.values.filter {
            it.sendDataModel && it.protocolVersion == ProtocolVersion.V0_9
        }
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
