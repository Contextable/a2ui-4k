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

package com.contextable.a2ui4k.protocol.v08

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Converts a v0.8-shape `ComponentDef` into its v0.9 equivalent.
 *
 * The v0.8 wire format wraps almost everything: the component type is a
 * single-key object, literals are tagged objects, children are tagged arrays
 * or template objects. This flattener strips all that wrapping so the result
 * can be parsed by the existing v0.9 `ComponentDef.fromJson`.
 *
 * Deprecated v0.8 widget types that no longer exist in v0.9 are also mapped:
 *
 * - `MultipleChoice` → `ChoicePicker` with `variant: "multipleSelection"`
 * - `SingleChoice`   → `ChoicePicker` with `variant: "mutuallyExclusive"`
 *
 * The flattener does **not** rename widget-level property keys (e.g.
 * `distribution`→`justify`, `usageHint`→`variant`); those are handled as
 * legacy aliases inside individual widgets so both versions of the same
 * property resolve identically at render time.
 *
 * Pure function: input is not mutated; a fresh [JsonObject] is returned.
 */
object V08ComponentFlattener {

    /**
     * Flattens a v0.8 component definition to v0.9 shape.
     *
     * Expected v0.8 input:
     * ```json
     * {
     *   "id": "my-btn",
     *   "component": {
     *     "Button": {
     *       "label": {"literalString": "Click"},
     *       "action": {...}
     *     }
     *   },
     *   "weight": 1
     * }
     * ```
     *
     * Output (v0.9 flat shape):
     * ```json
     * {
     *   "id": "my-btn",
     *   "component": "Button",
     *   "label": "Click",
     *   "action": {...},
     *   "weight": 1
     * }
     * ```
     *
     * If the input already looks v0.9-shaped (`component` is a string), it's
     * returned unchanged — the flattener is idempotent.
     */
    fun flatten(componentDef: JsonObject): JsonObject {
        val rawComponent = componentDef["component"]
        // Already v0.9 flat shape — idempotent passthrough.
        if (rawComponent is JsonPrimitive && rawComponent.isString) {
            return componentDef
        }

        val componentObj = rawComponent as? JsonObject
            ?: return componentDef  // unrecognized; pass through

        val originalWidgetType = componentObj.keys.firstOrNull() ?: return componentDef
        val rawProps = componentObj[originalWidgetType] as? JsonObject ?: JsonObject(emptyMap())

        val (widgetType, extraProps) = renameDeprecatedWidget(originalWidgetType)

        val result = mutableMapOf<String, JsonElement>()
        // Preserve id and weight verbatim.
        componentDef["id"]?.let { result["id"] = it }
        componentDef["weight"]?.let { result["weight"] = it }
        result["component"] = JsonPrimitive(widgetType)

        // Hoist the original property set, unwrapping each value.
        for ((key, value) in rawProps) {
            result[key] = unwrapValue(value)
        }
        // Extra properties added by widget renaming (e.g. synthesized variant).
        for ((key, value) in extraProps) {
            result[key] = value
        }

        // Widget-specific post-passes that rewrite v0.8 prop shapes which
        // cannot be expressed as pure value unwrapping.
        applyWidgetSpecificRewrites(widgetType, result)

        return JsonObject(result)
    }

    /**
     * In-place rewrites for widgets whose v0.8 → v0.9 delta touches property
     * structure (not just value wrapping). Kept here rather than in
     * `unwrapValue` because the shape change is widget-aware.
     */
    private fun applyWidgetSpecificRewrites(
        widgetType: String,
        props: MutableMap<String, JsonElement>
    ) {
        when (widgetType) {
            "Button" -> rewriteButton(props)
        }
    }

    /**
     * v0.8 Button.action:
     *   { "name": "submit", "context": [{"key":"k","value":{"path":"/p"}}] }
     * v0.9 Button.action:
     *   { "event": { "name": "submit", "context": {"k": {"path":"/p"}} } }
     *
     * v0.8 also used `primary: true` for button emphasis; v0.9 uses
     * `variant: "primary"`. Do not overwrite an explicit `variant`.
     */
    private fun rewriteButton(props: MutableMap<String, JsonElement>) {
        // action: rewrap as event + convert context array-of-pairs to object.
        val action = props["action"] as? JsonObject
        if (action != null && !action.containsKey("event") && !action.containsKey("functionCall")) {
            val name = action["name"]
            val contextElement = action["context"]
            if (name != null) {
                val contextObj: JsonElement? = when (contextElement) {
                    is JsonArray -> {
                        // v0.8: array of {key, value} pairs
                        val map = mutableMapOf<String, JsonElement>()
                        for (entry in contextElement) {
                            val e = entry as? JsonObject ?: continue
                            val k = (e["key"] as? JsonPrimitive)?.contentOrNull ?: continue
                            val v = e["value"] ?: continue
                            map[k] = v
                        }
                        JsonObject(map)
                    }
                    is JsonObject -> contextElement
                    else -> null
                }
                val eventBody = buildMap<String, JsonElement> {
                    put("name", name)
                    if (contextObj != null) put("context", contextObj)
                }
                props["action"] = JsonObject(mapOf("event" to JsonObject(eventBody)))
            }
        }

        // primary: true → variant: "primary" (unless variant is already set).
        val primary = props["primary"] as? JsonPrimitive
        if (primary != null && primary.contentOrNull == "true" && !props.containsKey("variant")) {
            props["variant"] = JsonPrimitive("primary")
        }
    }

    /**
     * Recursively unwraps v0.8 literal/path/children wrappers.
     *
     * - `{"literalString": "x"}` → `"x"`
     * - `{"literalNumber": 42}`  → `42`
     * - `{"literalBoolean": t}`  → `t`
     * - `{"path": "/p"}`         → unchanged (v0.9 shape)
     * - `{"dataBinding": "/p"}`  → `{"path": "/p"}`
     * - `{"explicitList": [ids]}` → `[ids]`
     * - `{"template": {"componentId", "dataBinding"}}` → `{"componentId", "path"}`
     * - any other object → recurse into values.
     * - primitives and arrays → pass through (arrays recurse into elements).
     */
    fun unwrapValue(value: JsonElement): JsonElement {
        return when (value) {
            is JsonPrimitive -> value
            is JsonArray -> JsonArray(value.map { unwrapValue(it) })
            is JsonObject -> unwrapObject(value)
        }
    }

    private fun unwrapObject(obj: JsonObject): JsonElement {
        // Single-key wrappers first.
        if (obj.size == 1) {
            val key = obj.keys.first()
            val inner = obj[key]!!
            when (key) {
                "literalString", "literalNumber", "literalBoolean" -> return inner
                "dataBinding" -> {
                    // legacy path alias
                    val path = (inner as? JsonPrimitive)?.contentOrNull ?: return obj
                    return JsonObject(mapOf("path" to JsonPrimitive(path)))
                }
                "explicitList" -> return if (inner is JsonArray) {
                    JsonArray(inner.map { unwrapValue(it) })
                } else inner
                "template" -> {
                    val inner2 = inner as? JsonObject ?: return obj
                    val componentId = inner2["componentId"]
                    val dataBinding = inner2["dataBinding"]?.jsonPrimitive?.contentOrNull
                    if (componentId != null && dataBinding != null) {
                        return JsonObject(mapOf(
                            "componentId" to componentId,
                            "path" to JsonPrimitive(dataBinding)
                        ))
                    }
                    return obj
                }
            }
        }
        // Generic object — recurse into values (handles nested action/context etc.).
        return JsonObject(obj.mapValues { (_, v) -> unwrapValue(v) })
    }

    /**
     * Maps v0.8-only widget names to their v0.9 equivalents, returning the
     * new type plus any extra properties that need to be added to match v0.9
     * semantics.
     */
    private fun renameDeprecatedWidget(
        v08Type: String
    ): Pair<String, Map<String, JsonElement>> = when (v08Type) {
        "MultipleChoice" -> "ChoicePicker" to
            mapOf("variant" to JsonPrimitive("multipleSelection"))
        "SingleChoice" -> "ChoicePicker" to
            mapOf("variant" to JsonPrimitive("mutuallyExclusive"))
        else -> v08Type to emptyMap()
    }
}
