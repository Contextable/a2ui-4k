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
 * Constants for the A2UI protocol.
 *
 * A2UI is a protocol for agents to render rich user interfaces. These constants
 * define the standard identifiers used in A2UI v0.8.
 *
 * ## Usage
 *
 * ```kotlin
 * // Check if a catalog is the standard catalog
 * if (catalogId == A2UIExtension.STANDARD_CATALOG_URI) {
 *     // Use CoreCatalog
 * }
 *
 * // Check MIME type of response data
 * if (part.mimeType == A2UIExtension.MIME_TYPE) {
 *     // Process as A2UI operation
 * }
 * ```
 *
 * @see A2UIExtensionParams
 * @see A2UIClientCapabilities
 */
object A2UIExtension {
    /**
     * The URI identifying the A2UI v0.8 extension.
     *
     * This URI is used to identify A2UI capability in extension negotiations.
     */
    const val URI_V08 = "https://a2ui.org/a2a-extension/a2ui/v0.8"

    /**
     * The URI of the standard A2UI v0.8 component catalog definition.
     *
     * This catalog includes the 18 standard widgets: Text, Button, Image,
     * Icon, Divider, Video, AudioPlayer, Column, Row, List, Card, Tabs,
     * Modal, TextField, CheckBox, Slider, MultipleChoice, and DateTimeInput.
     *
     * @see com.contextable.a2ui4k.catalog.CoreCatalog
     */
    const val STANDARD_CATALOG_URI = "https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json"

    /**
     * The MIME type for A2UI data in message parts.
     *
     * When A2UI operations are transmitted as data parts in messages,
     * this MIME type identifies the content as A2UI JSON.
     */
    const val MIME_TYPE = "application/json+a2ui"
}
