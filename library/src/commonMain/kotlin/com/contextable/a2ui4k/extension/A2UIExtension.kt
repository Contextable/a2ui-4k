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
     * The URI identifying the A2UI v0.8 extension. Advertised alongside
     * [URI_V09] when a client wants to accept either protocol from the server.
     */
    const val URI_V08 = "https://a2ui.org/a2a-extension/a2ui/v0.8"

    /**
     * The URI of the standard A2UI v0.9 component catalog definition.
     */
    const val STANDARD_CATALOG_URI = "https://github.com/google/A2UI/blob/main/specification/v0_9/json/standard_catalog.json"

    /**
     * Alternate v0.9 catalog URI emitted by parts of the ecosystem (notably
     * `@copilotkit/a2ui-middleware` and `google/A2UI` sample agents). Wire-
     * compatible with [STANDARD_CATALOG_URI] — [com.contextable.a2ui4k.state.SurfaceStateManager]
     * treats either as a v0.9 signal when resolving a surface's protocol
     * version.
     */
    const val BASIC_CATALOG_URI_V09 = "https://a2ui.org/specification/v0_9/basic_catalog.json"

    /**
     * The URI of the standard A2UI v0.8 component catalog definition. Clients
     * that want to negotiate down to v0.8 when talking to legacy agents should
     * include this in `a2uiClientCapabilities.supportedCatalogIds`.
     */
    const val STANDARD_CATALOG_URI_V08 = "https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json"

    /**
     * The MIME type for A2UI data in message parts.
     */
    const val MIME_TYPE = "application/json+a2ui"

    /**
     * The protocol version string.
     */
    const val PROTOCOL_VERSION = "v0.9"

    /** The v0.8 protocol version string. */
    const val PROTOCOL_VERSION_V08 = "v0.8"
}
