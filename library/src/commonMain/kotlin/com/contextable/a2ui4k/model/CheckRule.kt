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

import com.contextable.a2ui4k.function.FunctionEvaluator
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * A single client-side validation rule, per A2UI v0.9.
 *
 * ```json
 * {
 *   "condition": <DynamicBoolean>,
 *   "message": "Error text"
 * }
 * ```
 *
 * `condition` is a `DynamicBoolean`: a literal `true`/`false`, a `{"path":"/…"}`
 * binding, or a `{"call":"…","args":{…}}` function call. The rule **passes**
 * when `condition` resolves to `true`.
 */
data class CheckRule(
    val condition: JsonElement,
    val message: String
) {
    /**
     * Returns `null` if the rule passes, or the failure [message] otherwise.
     */
    fun evaluate(dataContext: DataContext): String? {
        return if (resolveBoolean(condition, dataContext)) null else message
    }

    companion object {
        /** Parses a single CheckRule object. Returns `null` if required keys are missing. */
        fun fromJson(element: JsonElement?): CheckRule? {
            val obj = element as? JsonObject ?: return null
            val condition = obj["condition"] ?: return null
            val message = obj["message"]?.jsonPrimitive?.contentOrNull ?: return null
            return CheckRule(condition, message)
        }

        /**
         * Parses a `checks` array. Accepts `null`/absent as empty list.
         * Silently drops malformed entries.
         */
        fun fromJsonArray(element: JsonElement?): List<CheckRule> {
            val array = element as? JsonArray ?: return emptyList()
            return array.mapNotNull { fromJson(it) }
        }

        /**
         * Evaluates a list of rules in order. Returns the collected failure
         * messages (empty list when all rules pass).
         */
        fun evaluateAll(rules: List<CheckRule>, dataContext: DataContext): List<String> =
            rules.mapNotNull { it.evaluate(dataContext) }
    }
}

private fun resolveBoolean(element: JsonElement, dataContext: DataContext): Boolean {
    return when (element) {
        is JsonPrimitive -> element.booleanOrNull ?: false
        is JsonObject -> {
            element["path"]?.jsonPrimitive?.contentOrNull?.let { path ->
                dataContext.getBoolean(path) ?: false
            } ?: element["call"]?.jsonPrimitive?.contentOrNull?.let { call ->
                FunctionEvaluator.evaluateBoolean(
                    call = call,
                    args = element["args"] as? JsonObject,
                    dataContext = dataContext
                ) ?: false
            } ?: false
        }
        else -> false
    }
}
