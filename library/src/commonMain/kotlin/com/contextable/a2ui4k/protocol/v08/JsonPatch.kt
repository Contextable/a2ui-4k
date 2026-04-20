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
 * Minimal JSON Patch (RFC 6902) engine used to replay v0.8 `ACTIVITY_DELTA`
 * patches against a cached snapshot of the operations array.
 *
 * Supports all six op types: `add`, `remove`, `replace`, `move`, `copy`, `test`.
 * Path syntax is JSON Pointer (RFC 6901).
 *
 * Intentionally side-effect free — [apply] returns a new [JsonElement] rather
 * than mutating the input. Errors throw [JsonPatchException]; `test` failures
 * also throw (per RFC 6902 §5).
 */
object JsonPatch {

    /**
     * Applies a JSON-Patch array to [target], returning the patched document.
     *
     * @throws JsonPatchException if any operation fails (invalid op, path not
     *         found where required, failing `test`, type mismatch, etc.).
     */
    fun apply(target: JsonElement, patch: JsonArray): JsonElement {
        var current = target
        for ((index, opElement) in patch.withIndex()) {
            val op = opElement as? JsonObject
                ?: throw JsonPatchException("Patch[$index] is not an object")
            current = applyOne(current, op, index)
        }
        return current
    }

    private fun applyOne(target: JsonElement, op: JsonObject, index: Int): JsonElement {
        val opName = op["op"]?.jsonPrimitive?.contentOrNull
            ?: throw JsonPatchException("Patch[$index] missing 'op'")
        val path = op["path"]?.jsonPrimitive?.contentOrNull
            ?: throw JsonPatchException("Patch[$index] ($opName) missing 'path'")
        val pointer = JsonPointer.parse(path)

        return when (opName) {
            "add" -> {
                val value = op["value"]
                    ?: throw JsonPatchException("Patch[$index] (add) missing 'value'")
                addAt(target, pointer, value)
            }
            "remove" -> removeAt(target, pointer).first
            "replace" -> {
                val value = op["value"]
                    ?: throw JsonPatchException("Patch[$index] (replace) missing 'value'")
                val (withoutOld, _) = removeAt(target, pointer)
                addAt(withoutOld, pointer, value)
            }
            "move" -> {
                val from = op["from"]?.jsonPrimitive?.contentOrNull
                    ?: throw JsonPatchException("Patch[$index] (move) missing 'from'")
                val fromPointer = JsonPointer.parse(from)
                val (withoutOld, removed) = removeAt(target, fromPointer)
                addAt(withoutOld, pointer, removed)
            }
            "copy" -> {
                val from = op["from"]?.jsonPrimitive?.contentOrNull
                    ?: throw JsonPatchException("Patch[$index] (copy) missing 'from'")
                val fromPointer = JsonPointer.parse(from)
                val value = getAt(target, fromPointer)
                    ?: throw JsonPatchException("Patch[$index] (copy) 'from' path not found: $from")
                addAt(target, pointer, value)
            }
            "test" -> {
                val expected = op["value"]
                    ?: throw JsonPatchException("Patch[$index] (test) missing 'value'")
                val actual = getAt(target, pointer)
                    ?: throw JsonPatchException("Patch[$index] (test) path not found: $path")
                if (actual != expected) {
                    throw JsonPatchException("Patch[$index] (test) mismatch at $path")
                }
                target
            }
            else -> throw JsonPatchException("Patch[$index] unknown op '$opName'")
        }
    }

    // --- navigation ---------------------------------------------------

    private fun getAt(target: JsonElement, pointer: List<String>): JsonElement? {
        var current: JsonElement = target
        for (segment in pointer) {
            current = when (current) {
                is JsonObject -> current[segment] ?: return null
                is JsonArray -> {
                    val idx = segment.toIntOrNull() ?: return null
                    current.getOrNull(idx) ?: return null
                }
                else -> return null
            }
        }
        return current
    }

    private fun addAt(target: JsonElement, pointer: List<String>, value: JsonElement): JsonElement {
        if (pointer.isEmpty()) return value
        return mutate(target, pointer) { parent, lastSegment ->
            when (parent) {
                is JsonObject -> JsonObject(parent.toMutableMap().apply { put(lastSegment, value) })
                is JsonArray -> {
                    val list = parent.toMutableList()
                    if (lastSegment == "-") {
                        list.add(value)
                    } else {
                        val idx = lastSegment.toIntOrNull()
                            ?: throw JsonPatchException("Array index not numeric: $lastSegment")
                        if (idx < 0 || idx > list.size) {
                            throw JsonPatchException("Array index out of range: $idx (size ${list.size})")
                        }
                        list.add(idx, value)
                    }
                    JsonArray(list)
                }
                else -> throw JsonPatchException(
                    "Cannot add to non-container at ${pointer.dropLast(1).joinToString("/")}"
                )
            }
        }
    }

    private fun removeAt(target: JsonElement, pointer: List<String>): Pair<JsonElement, JsonElement> {
        if (pointer.isEmpty()) {
            throw JsonPatchException("Cannot remove root")
        }
        var removed: JsonElement? = null
        val result = mutate(target, pointer) { parent, lastSegment ->
            when (parent) {
                is JsonObject -> {
                    val map = parent.toMutableMap()
                    removed = map.remove(lastSegment)
                        ?: throw JsonPatchException("Path not found: ${pointer.joinToString("/")}")
                    JsonObject(map)
                }
                is JsonArray -> {
                    val list = parent.toMutableList()
                    val idx = lastSegment.toIntOrNull()
                        ?: throw JsonPatchException("Array index not numeric: $lastSegment")
                    if (idx < 0 || idx >= list.size) {
                        throw JsonPatchException("Array index out of range: $idx")
                    }
                    removed = list.removeAt(idx)
                    JsonArray(list)
                }
                else -> throw JsonPatchException(
                    "Cannot remove from non-container at ${pointer.dropLast(1).joinToString("/")}"
                )
            }
        }
        return result to (removed
            ?: throw JsonPatchException("Internal error: removed element was null"))
    }

    /**
     * Walks [target] along [pointer] (all but last segment), invoking
     * [transform] with the parent container and the final segment, then
     * rebuilds the path back to the root.
     */
    private fun mutate(
        target: JsonElement,
        pointer: List<String>,
        transform: (parent: JsonElement, lastSegment: String) -> JsonElement
    ): JsonElement {
        if (pointer.size == 1) {
            return transform(target, pointer[0])
        }
        val head = pointer[0]
        val tail = pointer.drop(1)
        return when (target) {
            is JsonObject -> {
                val child = target[head]
                    ?: throw JsonPatchException("Path not found: $head")
                JsonObject(target.toMutableMap().apply { put(head, mutate(child, tail, transform)) })
            }
            is JsonArray -> {
                val idx = head.toIntOrNull()
                    ?: throw JsonPatchException("Array index not numeric: $head")
                if (idx < 0 || idx >= target.size) {
                    throw JsonPatchException("Array index out of range: $idx")
                }
                val list = target.toMutableList()
                list[idx] = mutate(list[idx], tail, transform)
                JsonArray(list)
            }
            else -> throw JsonPatchException("Cannot descend into non-container at $head")
        }
    }
}

/** Thrown by [JsonPatch.apply] on any failure, including `test` mismatches. */
class JsonPatchException(message: String) : RuntimeException(message)

/** RFC 6901 JSON Pointer parser. Internal to this package. */
internal object JsonPointer {
    /**
     * Parses a JSON Pointer string into its reference tokens.
     *
     * - `""` / `"/"`-prefixed paths supported per RFC 6901.
     * - `~1` unescapes to `/`, `~0` to `~` (order matters).
     */
    fun parse(path: String): List<String> {
        if (path.isEmpty()) return emptyList()
        require(path.startsWith("/")) { "JSON Pointer must be empty or start with '/': $path" }
        return path.substring(1).split("/").map { segment ->
            segment.replace("~1", "/").replace("~0", "~")
        }
    }
}
