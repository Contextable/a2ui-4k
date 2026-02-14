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
 * A2UI-specific parameters for the AgentExtension.
 *
 * These parameters are included in the `params` field of an [AgentExtension]
 * when declaring A2UI capabilities in an agent's AgentCard.
 *
 * ## Example JSON
 *
 * ```json
 * {
 *   "supportedCatalogIds": [
 *     "https://github.com/google/A2UI/blob/main/specification/v0_9/json/standard_catalog.json",
 *     "https://my-company.com/a2ui/v0.9/my_custom_catalog.json"
 *   ],
 *   "acceptsInlineCatalogs": true
 * }
 * ```
 *
 * @property supportedCatalogIds List of catalog definition URIs that the agent can generate
 *                               UI components for. If empty or null, defaults to the standard catalog.
 * @property acceptsInlineCatalogs Whether the agent can accept inline catalog definitions
 *                                 from the client's `a2uiClientCapabilities`. Defaults to false.
 *
 * @see AgentExtension
 * @see a2uiAgentExtension
 */
@Serializable
data class A2UIExtensionParams(
    val supportedCatalogIds: List<String>? = null,
    val acceptsInlineCatalogs: Boolean = false
)
