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

import kotlinx.serialization.Serializable

/**
 * Declares the A2UI capabilities of a client.
 *
 * Clients include this in message metadata when communicating with A2UI-capable
 * agents. It tells the agent which component catalogs the client can render.
 *
 * ## Example JSON
 *
 * ```json
 * {
 *   "a2uiClientCapabilities": {
 *     "supportedCatalogIds": [
 *       "https://github.com/google/A2UI/blob/main/specification/v0_9/json/standard_catalog.json"
 *     ]
 *   }
 * }
 * ```
 *
 * ## Usage
 *
 * ```kotlin
 * // Declare support for standard catalog
 * val capabilities = a2uiStandardClientCapabilities()
 *
 * // Include in message metadata
 * val metadata = mapOf("a2uiClientCapabilities" to capabilities)
 * ```
 *
 * @property supportedCatalogIds List of catalog URIs the client can render.
 *                               The agent will only generate components from
 *                               catalogs the client supports.
 *
 * @see A2UIExtension.STANDARD_CATALOG_URI
 * @see A2UIExtensionParams
 */
@Serializable
data class A2UIClientCapabilities(
    val supportedCatalogIds: List<String>
)

/**
 * Creates [A2UIClientCapabilities] for the standard A2UI v0.9 catalog.
 *
 * This is the most common configuration for clients that implement the
 * 18 standard widgets defined in the A2UI specification.
 *
 * ## Usage
 *
 * ```kotlin
 * val capabilities = a2uiStandardClientCapabilities()
 * // supportedCatalogIds = [STANDARD_CATALOG_URI]
 * ```
 *
 * @return Client capabilities declaring support for the standard catalog.
 *
 * @see A2UIExtension.STANDARD_CATALOG_URI
 * @see com.contextable.a2ui4k.catalog.CoreCatalog
 */
fun a2uiStandardClientCapabilities(): A2UIClientCapabilities =
    A2UIClientCapabilities(
        supportedCatalogIds = listOf(A2UIExtension.STANDARD_CATALOG_URI)
    )

/**
 * Creates [A2UIClientCapabilities] for the specified catalogs.
 *
 * Use this when your client supports custom catalogs in addition to
 * or instead of the standard catalog.
 *
 * ## Usage
 *
 * ```kotlin
 * // Support standard catalog plus a custom one
 * val capabilities = a2uiClientCapabilities(
 *     A2UIExtension.STANDARD_CATALOG_URI,
 *     "https://my-company.com/a2ui/custom_catalog.json"
 * )
 * ```
 *
 * @param catalogIds The catalog URIs the client can render.
 * @return Client capabilities declaring support for the specified catalogs.
 */
fun a2uiClientCapabilities(vararg catalogIds: String): A2UIClientCapabilities =
    A2UIClientCapabilities(
        supportedCatalogIds = catalogIds.toList()
    )
