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

/**
 * A2UI wire-protocol version a surface is speaking.
 *
 * Selected per-surface at creation time based on the `catalogId` the server
 * chose from `a2uiClientCapabilities.supportedCatalogIds`. Determines:
 *
 * - How incoming server messages are parsed
 *   (`ACTIVITY_SNAPSHOT`/`ACTIVITY_DELTA` → [V0_8]; flat JSONL → [V0_9]).
 * - How outgoing client events are serialized
 *   (`userAction`/`dataChange` → [V0_8]; `{version:"v0.9", action|error}` → [V0_9]).
 */
enum class ProtocolVersion(val wireTag: String) {
    V0_8("v0.8"),
    V0_9("v0.9");

    companion object {
        /** Parses a `version` field content, or returns `null` if unrecognized. */
        fun fromWireTag(tag: String?): ProtocolVersion? = when (tag) {
            V0_8.wireTag -> V0_8
            V0_9.wireTag -> V0_9
            else -> null
        }
    }
}
