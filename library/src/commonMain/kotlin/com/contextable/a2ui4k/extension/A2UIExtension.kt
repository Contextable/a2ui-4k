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

package com.contextable.a2ui4k.extension

/**
 * Constants for the A2UI v0.9 protocol.
 *
 * A2UI is a protocol for agents to render rich user interfaces. These constants
 * define the standard identifiers used in A2UI v0.9.
 *
 * @see A2UIExtensionParams
 * @see A2UIClientCapabilities
 */
object A2UIExtension {
    /**
     * The URI identifying the A2UI v0.9 extension.
     */
    const val URI_V09 = "https://a2ui.org/a2a-extension/a2ui/v0.9"

    /**
     * The URI of the standard A2UI v0.9 component catalog definition.
     */
    const val STANDARD_CATALOG_URI = "https://github.com/google/A2UI/blob/main/specification/v0_9/json/standard_catalog.json"

    /**
     * The MIME type for A2UI data in message parts.
     */
    const val MIME_TYPE = "application/json+a2ui"

    /**
     * The protocol version string.
     */
    const val PROTOCOL_VERSION = "v0.9"
}
