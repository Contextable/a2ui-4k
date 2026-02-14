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
 * Represents a user interaction event from an A2UI surface.
 *
 * Events are generated when users interact with UI components
 * (button clicks, form submissions, etc.) and can be sent back
 * to the AI agent for processing.
 *
 * In A2UI v0.9, client-to-server messages include:
 * - [ActionEvent] maps to the `action` message type
 * - [DataChangeEvent] is an internal event for two-way data binding
 * - [ValidationError] maps to the `error` message type with VALIDATION_FAILED code
 *
 * @see ActionEvent
 * @see DataChangeEvent
 * @see ValidationError
 */
sealed class UiEvent {
    /**
     * The surface ID where the event originated.
     */
    abstract val surfaceId: String
}

/**
 * An action event triggered by user interaction (e.g., button click).
 *
 * In v0.9, the client-to-server format wraps this in an `action` envelope:
 * ```json
 * {
 *   "version": "v0.9",
 *   "action": {
 *     "name": "action_name",
 *     "surfaceId": "default",
 *     "sourceComponentId": "component-id",
 *     "timestamp": "2025-12-17T02:00:23.936Z",
 *     "context": { "key": "value" }
 *   }
 * }
 * ```
 *
 * @property name The action identifier
 * @property surfaceId The surface where the action occurred
 * @property sourceComponentId The component ID with optional template item suffix
 * @property timestamp ISO8601 timestamp of when the event occurred
 * @property context Resolved context data from action.event.context
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
 * A data change event when the user modifies a bound value.
 *
 * This is an internal client-side event used for two-way data binding.
 * Input widgets emit this when the user changes a form value, allowing
 * the application to update the data model accordingly.
 *
 * @property surfaceId The surface where the change occurred
 * @property path The data model path that was modified
 * @property value The new value
 */
@Serializable
data class DataChangeEvent(
    override val surfaceId: String,
    val path: String,
    val value: String
) : UiEvent()

/**
 * A validation error sent from client to server.
 *
 * In v0.9, validation errors follow a structured format:
 * ```json
 * {
 *   "version": "v0.9",
 *   "error": {
 *     "code": "VALIDATION_FAILED",
 *     "surfaceId": "surface-1",
 *     "path": "/components/0/text",
 *     "message": "Required field missing"
 *   }
 * }
 * ```
 *
 * @property code Error code (e.g., "VALIDATION_FAILED")
 * @property surfaceId The surface where the error occurred
 * @property path JSON Pointer to the problematic field
 * @property message Human-readable error description
 */
@Serializable
data class ValidationError(
    val code: String = "VALIDATION_FAILED",
    override val surfaceId: String,
    val path: String,
    val message: String
) : UiEvent()
