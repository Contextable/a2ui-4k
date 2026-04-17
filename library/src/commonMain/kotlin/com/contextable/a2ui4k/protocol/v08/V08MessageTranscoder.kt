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

package com.contextable.a2ui4k.protocol.v08

import com.contextable.a2ui4k.extension.A2UIExtension
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Translates A2UI v0.8 wire messages into the equivalent v0.9-shape operation
 * messages that `SurfaceStateManager.processMessage` already knows how to
 * handle.
 *
 * The v0.8 wire protocol is:
 *
 * - `ACTIVITY_SNAPSHOT` with an `operations` array (baseline).
 * - `ACTIVITY_DELTA` with an RFC 6902 JSON Patch applied against the prior
 *   snapshot's operations array.
 *
 * Each individual v0.8 operation (`beginRendering`, `surfaceUpdate`,
 * `dataModelUpdate`, `deleteSurface`) is rewritten to its v0.9 counterpart
 * (`createSurface`, `updateComponents`, `updateDataModel`, `deleteSurface`).
 *
 * This class is **stateful** per [V08MessageTranscoder] instance: it caches
 * the last snapshot for each `messageId` so deltas can replay against it.
 * In return, callers get idempotent behavior — a delta only emits the
 * operations that are *newly present* compared to the prior snapshot.
 *
 * Not thread-safe. One instance per connection/session.
 */
class V08MessageTranscoder {

    /** Map of messageId → current operations list (after any applied deltas). */
    private val cachedOperations = mutableMapOf<String, List<JsonObject>>()

    /**
     * Returns true if [envelope] looks like a v0.8 message that this
     * transcoder should handle. Cheap, no-op on v0.9 envelopes.
     */
    fun isV08Envelope(envelope: JsonObject): Boolean {
        if ((envelope["kind"] as? JsonPrimitive)?.contentOrNull in V08_ENVELOPE_KINDS) return true
        if ((envelope["type"] as? JsonPrimitive)?.contentOrNull in V08_ENVELOPE_KINDS) return true
        if ((envelope["version"] as? JsonPrimitive)?.contentOrNull == A2UIExtension.PROTOCOL_VERSION_V08) return true
        return false
    }

    /**
     * Transcodes a v0.8 envelope into zero-or-more v0.9-shape operation
     * messages. Non-v0.8 envelopes return an empty list; callers should
     * check [isV08Envelope] first (the dispatcher in SurfaceStateManager
     * already does).
     *
     * Each returned message is a fully-formed `{"version":"v0.9", "<op>":{…}}`
     * object ready for `SurfaceStateManager.processMessage`.
     */
    fun transcode(envelope: JsonObject): List<JsonObject> {
        val kind = (envelope["kind"] as? JsonPrimitive)?.contentOrNull
            ?: (envelope["type"] as? JsonPrimitive)?.contentOrNull
        val messageId = (envelope["messageId"] as? JsonPrimitive)?.contentOrNull
            ?: (envelope["id"] as? JsonPrimitive)?.contentOrNull
            ?: DEFAULT_MESSAGE_ID

        return when (kind) {
            "ACTIVITY_SNAPSHOT" -> handleSnapshot(messageId, envelope)
            "ACTIVITY_DELTA" -> handleDelta(messageId, envelope)
            else -> {
                // No wrapper — might be a bare operations list or unrecognized.
                if (envelope.containsKey("operations")) {
                    handleSnapshot(messageId, envelope)
                } else {
                    emptyList()
                }
            }
        }
    }

    private fun handleSnapshot(messageId: String, envelope: JsonObject): List<JsonObject> {
        val contentObj = (envelope["content"] as? JsonObject) ?: envelope
        val ops = contentObj["operations"]?.jsonArray
            ?.mapNotNull { it as? JsonObject }
            ?: return emptyList()

        cachedOperations[messageId] = ops
        return ops.mapNotNull { transcodeOperation(it) }
    }

    private fun handleDelta(messageId: String, envelope: JsonObject): List<JsonObject> {
        val patch = (envelope["patch"] as? JsonArray)
            ?: (envelope["content"] as? JsonObject)?.get("patch") as? JsonArray
            ?: return emptyList()

        val previousOps = cachedOperations[messageId] ?: emptyList()
        val snapshotDoc = buildJsonObject {
            put("operations", JsonArray(previousOps))
        }
        val patched = try {
            JsonPatch.apply(snapshotDoc, patch) as? JsonObject ?: return emptyList()
        } catch (e: JsonPatchException) {
            // Patch failed — skip rather than crash the stream.
            return emptyList()
        }
        val newOps = patched["operations"]?.jsonArray
            ?.mapNotNull { it as? JsonObject }
            ?: return emptyList()

        cachedOperations[messageId] = newOps

        // Emit only the operations not present in the previous snapshot
        // (by structural equality — v0.8 operations are small).
        val previousSet = previousOps.toSet()
        return newOps
            .filter { it !in previousSet }
            .mapNotNull { transcodeOperation(it) }
    }

    /**
     * Converts a single v0.8 operation object (with one of the four v0.8 op
     * keys) to a v0.9 envelope message. Returns `null` if the operation is
     * unrecognized.
     */
    private fun transcodeOperation(op: JsonObject): JsonObject? {
        return when {
            op.containsKey("beginRendering") ->
                transcodeBeginRendering(op["beginRendering"]!!.jsonObject)
            op.containsKey("surfaceUpdate") ->
                transcodeSurfaceUpdate(op["surfaceUpdate"]!!.jsonObject)
            op.containsKey("dataModelUpdate") ->
                transcodeDataModelUpdate(op["dataModelUpdate"]!!.jsonObject)
            op.containsKey("deleteSurface") ->
                transcodeDeleteSurface(op["deleteSurface"]!!.jsonObject)
            else -> null
        }
    }

    private fun transcodeBeginRendering(data: JsonObject): JsonObject {
        val createSurface = buildJsonObject {
            data["surfaceId"]?.let { put("surfaceId", it) }
            put("catalogId", JsonPrimitive(A2UIExtension.STANDARD_CATALOG_URI_V08))
            // v0.8 `styles` → v0.9 `theme`.
            data["styles"]?.let { put("theme", it) }
            // Preserve the declared root so downstream can find it even if
            // the id is not literally "root".
            data["root"]?.let { put("rootComponentId", it) }
        }
        return envelope("createSurface", createSurface)
    }

    private fun transcodeSurfaceUpdate(data: JsonObject): JsonObject {
        val surfaceId = data["surfaceId"]
        val componentsArray = data["components"] as? JsonArray ?: JsonArray(emptyList())
        val flattened = componentsArray.mapNotNull { element ->
            val obj = element as? JsonObject ?: return@mapNotNull null
            V08ComponentFlattener.flatten(obj)
        }

        val updateComponents = buildJsonObject {
            surfaceId?.let { put("surfaceId", it) }
            put("components", JsonArray(flattened))
        }
        return envelope("updateComponents", updateComponents)
    }

    private fun transcodeDataModelUpdate(data: JsonObject): JsonObject {
        val surfaceId = data["surfaceId"]
        val path = (data["path"] as? JsonPrimitive)?.contentOrNull ?: "/"
        val contents = data["contents"] as? JsonArray

        val updateDataModel = buildJsonObject {
            surfaceId?.let { put("surfaceId", it) }
            put("path", JsonPrimitive(path))
            val value = if (contents != null) {
                dataEntriesToJsonElement(contents)
            } else {
                // No contents — treat as explicit null (v0.8 had no absent-vs-null distinction).
                kotlinx.serialization.json.JsonNull
            }
            put("value", value)
        }
        return envelope("updateDataModel", updateDataModel)
    }

    private fun transcodeDeleteSurface(data: JsonObject): JsonObject =
        envelope("deleteSurface", data)

    /**
     * Recursively converts a v0.8 `contents` array of DataEntry objects into
     * a single v0.9 `value` JSON element — typically a JsonObject keyed by
     * each DataEntry's `key`.
     */
    private fun dataEntriesToJsonElement(entries: JsonArray): JsonElement {
        return buildJsonObject {
            for (entryElement in entries) {
                val entry = entryElement as? JsonObject ?: continue
                val key = (entry["key"] as? JsonPrimitive)?.contentOrNull ?: continue
                val value = dataEntryToJsonElement(entry)
                put(key, value)
            }
        }
    }

    private fun dataEntryToJsonElement(entry: JsonObject): JsonElement {
        return when {
            entry.containsKey("valueString") -> entry["valueString"]!!
            entry.containsKey("valueNumber") -> entry["valueNumber"]!!
            entry.containsKey("valueBoolean") -> entry["valueBoolean"]!!
            entry.containsKey("valueMap") -> {
                val nested = entry["valueMap"] as? JsonArray ?: return kotlinx.serialization.json.JsonNull
                dataEntriesToJsonElement(nested)
            }
            entry.containsKey("valueJson") -> entry["valueJson"]!!
            else -> kotlinx.serialization.json.JsonNull
        }
    }

    private fun envelope(op: String, body: JsonObject): JsonObject = buildJsonObject {
        put("version", JsonPrimitive(A2UIExtension.PROTOCOL_VERSION))
        put(op, body)
    }

    companion object {
        private val V08_ENVELOPE_KINDS = setOf("ACTIVITY_SNAPSHOT", "ACTIVITY_DELTA")
        private const val DEFAULT_MESSAGE_ID = "__default__"
    }
}
