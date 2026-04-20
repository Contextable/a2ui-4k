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
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * Client→server events that a surface can emit.
 *
 * The A2UI v0.9 protocol defines exactly two wire messages:
 *
 * - `action` — user interaction ([ActionEvent])
 * - `error`  — client-side error with two variants:
 *   [ValidationError] (code == `VALIDATION_FAILED`) and [ClientError] (any other code)
 *
 * [DataChangeEvent] is **local only** — v0.9 does not define an upstream
 * message for individual data mutations. It exists so input widgets can
 * mutate the client-side data model through the standard event bus.
 *
 * Use [toClientMessage] on the wire-emitting events to obtain the full
 * `{"version":"v0.9","<key>":{...}}` envelope ready for any transport.
 */
sealed class UiEvent {
    /** The surface ID where the event originated. */
    abstract val surfaceId: String
}

/** Current A2UI protocol version. */
const val A2UI_PROTOCOL_VERSION: String = "v0.9"

/**
 * User interaction event. Serialized on the wire as:
 * ```json
 * {
 *   "version": "v0.9",
 *   "action": {
 *     "name": "submit",
 *     "surfaceId": "default",
 *     "sourceComponentId": "submit-button",
 *     "timestamp": "2025-12-17T02:00:23.936Z",
 *     "context": { "key": "value" }
 *   }
 * }
 * ```
 */
@Serializable
data class ActionEvent(
    val name: String,
    override val surfaceId: String,
    val sourceComponentId: String,
    val timestamp: String,
    val context: JsonObject? = null
) : UiEvent()

/**
 * Client-side data change. **Not** a wire message — input widgets emit this
 * so the application can mirror changes into the local [com.contextable.a2ui4k.data.DataModel].
 */
@Serializable
data class DataChangeEvent(
    override val surfaceId: String,
    val path: String,
    val value: String
) : UiEvent()

/**
 * Validation failure (`code == "VALIDATION_FAILED"`). Serialized as:
 * ```json
 * {
 *   "version": "v0.9",
 *   "error": {
 *     "code": "VALIDATION_FAILED",
 *     "surfaceId": "s1",
 *     "path": "/components/0/text",
 *     "message": "Expected string, got integer"
 *   }
 * }
 * ```
 */
@Serializable
data class ValidationError(
    override val surfaceId: String,
    val path: String,
    val message: String
) : UiEvent() {
    val code: String get() = VALIDATION_FAILED

    companion object {
        const val VALIDATION_FAILED: String = "VALIDATION_FAILED"
    }
}

/**
 * Generic client error (any code other than `VALIDATION_FAILED`). Serialized as:
 * ```json
 * {
 *   "version": "v0.9",
 *   "error": {
 *     "code": "CUSTOM_CODE",
 *     "surfaceId": "s1",
 *     "message": "Human-readable explanation"
 *   }
 * }
 * ```
 */
@Serializable
data class ClientError(
    val code: String,
    override val surfaceId: String,
    val message: String
) : UiEvent() {
    init {
        require(code != ValidationError.VALIDATION_FAILED) {
            "Use ValidationError for VALIDATION_FAILED; ClientError is for other codes."
        }
    }
}

/**
 * Returns the wire-ready envelope for this event in the given protocol
 * [version], or `null` if this event doesn't emit under that version.
 *
 * The returned [JsonObject] is the payload for a new A2A message Part of
 * MIME type `application/json+a2ui`; the library does not construct the Part
 * envelope itself.
 *
 * ## v0.9
 *
 * - [ActionEvent] → `{"version":"v0.9","action":{…}}`
 * - [ValidationError] / [ClientError] → `{"version":"v0.9","error":{…}}`
 * - [DataChangeEvent] → `null` (v0.9 has no upstream data-change message;
 *   local changes ride along in `a2uiClientDataModel` metadata)
 *
 * ## v0.8
 *
 * - [ActionEvent] → `{"userAction":{name, surfaceId, sourceComponentId,
 *   timestamp, context}}` (no version envelope)
 * - [DataChangeEvent] → `{"dataChange":{surfaceId, path, value}}` (becomes
 *   a real wire message)
 * - [ValidationError] / [ClientError] → `null` (v0.8 has no formal client
 *   error shape; callers should log locally)
 */
fun UiEvent.toClientMessage(
    version: ProtocolVersion = ProtocolVersion.V0_9
): JsonObject? = when (version) {
    ProtocolVersion.V0_9 -> toClientMessageV09()
    ProtocolVersion.V0_8 -> toClientMessageV08()
}

private fun UiEvent.toClientMessageV09(): JsonObject? = when (this) {
    is ActionEvent -> buildJsonObject {
        put("version", JsonPrimitive(A2UI_PROTOCOL_VERSION))
        put("action", buildJsonObject {
            put("name", JsonPrimitive(name))
            put("surfaceId", JsonPrimitive(surfaceId))
            put("sourceComponentId", JsonPrimitive(sourceComponentId))
            put("timestamp", JsonPrimitive(timestamp))
            put("context", context ?: JsonObject(emptyMap()))
        })
    }
    is ValidationError -> buildJsonObject {
        put("version", JsonPrimitive(A2UI_PROTOCOL_VERSION))
        put("error", buildJsonObject {
            put("code", JsonPrimitive(code))
            put("surfaceId", JsonPrimitive(surfaceId))
            put("path", JsonPrimitive(path))
            put("message", JsonPrimitive(message))
        })
    }
    is ClientError -> buildJsonObject {
        put("version", JsonPrimitive(A2UI_PROTOCOL_VERSION))
        put("error", buildJsonObject {
            put("code", JsonPrimitive(code))
            put("surfaceId", JsonPrimitive(surfaceId))
            put("message", JsonPrimitive(message))
        })
    }
    is DataChangeEvent -> null
}

private fun UiEvent.toClientMessageV08(): JsonObject? = when (this) {
    is ActionEvent -> buildJsonObject {
        put("userAction", buildJsonObject {
            put("name", JsonPrimitive(name))
            put("surfaceId", JsonPrimitive(surfaceId))
            put("sourceComponentId", JsonPrimitive(sourceComponentId))
            put("timestamp", JsonPrimitive(timestamp))
            put("context", context ?: JsonObject(emptyMap()))
        })
    }
    is DataChangeEvent -> buildJsonObject {
        put("dataChange", buildJsonObject {
            put("surfaceId", JsonPrimitive(surfaceId))
            put("path", JsonPrimitive(path))
            put("value", JsonPrimitive(value))
        })
    }
    // v0.8 has no formal error-reporting wire shape. Swallow and log locally
    // in the caller; we return null here so no stray message goes on the wire.
    is ValidationError, is ClientError -> null
}
