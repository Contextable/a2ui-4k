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

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * A2UI v0.9 `ComponentCommon.accessibility`:
 *
 * ```json
 * "accessibility": {
 *   "label": <DynamicString>,
 *   "description": <DynamicString>
 * }
 * ```
 *
 * Both fields are optional. Resolved via [DataReferenceParser.parseString].
 */
data class Accessibility(
    val label: DataReference<String>? = null,
    val description: DataReference<String>? = null
) {
    /** Returns `true` if every field is null (nothing to render). */
    val isEmpty: Boolean
        get() = label == null && description == null

    /** Resolves the accessibility label against [dataContext], or `null` if absent. */
    fun resolveLabel(dataContext: DataContext): String? = resolve(label, dataContext)

    /** Resolves the accessibility description against [dataContext], or `null` if absent. */
    fun resolveDescription(dataContext: DataContext): String? = resolve(description, dataContext)

    companion object {
        /** Parses `accessibility` from a component's raw JSON. Returns `null` if absent/empty. */
        fun fromJson(element: JsonElement?): Accessibility? {
            val obj = element as? JsonObject ?: return null
            val label = DataReferenceParser.parseString(obj["label"])
            val description = DataReferenceParser.parseString(obj["description"])
            val result = Accessibility(label, description)
            return if (result.isEmpty) null else result
        }
    }
}

private fun resolve(ref: DataReference<String>?, dataContext: DataContext): String? = when (ref) {
    null -> null
    is LiteralString -> ref.value
    is PathString -> dataContext.getString(ref.path)
    else -> null
}
