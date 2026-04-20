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

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * Represents a reference to data in the A2UI v0.9 protocol.
 *
 * In v0.9, property values use implicit typing:
 * - Literal values: `"Hello"`, `42`, `true` (plain JSON primitives)
 * - Path bindings: `{"path": "/user/name"}` (DataBinding object)
 * - Function calls: `{"call": "formatString", "args": {...}}` (FunctionCall object)
 *
 * This sealed class hierarchy provides type-safe access to referenced data
 * and is used by widget implementations to resolve property values.
 *
 * @see DataReferenceParser
 * @see DataContext
 * @see com.contextable.a2ui4k.data.DataModel
 */
sealed class DataReference<T> {
    /**
     * Gets the value, either as a literal or by resolving a path.
     * For path references, requires a resolver function.
     */
    abstract fun resolve(pathResolver: (String) -> T?): T?
}

/**
 * A literal string value.
 */
data class LiteralString(val value: String) : DataReference<String>() {
    override fun resolve(pathResolver: (String) -> String?): String = value
}

/**
 * A string value bound to a data model path.
 */
data class PathString(val path: String) : DataReference<String>() {
    override fun resolve(pathResolver: (String) -> String?): String? = pathResolver(path)
}

/**
 * A literal number value.
 */
data class LiteralNumber(val value: Double) : DataReference<Double>() {
    override fun resolve(pathResolver: (String) -> Double?): Double = value
}

/**
 * A number value bound to a data model path.
 */
data class PathNumber(val path: String) : DataReference<Double>() {
    override fun resolve(pathResolver: (String) -> Double?): Double? = pathResolver(path)
}

/**
 * A literal boolean value.
 */
data class LiteralBoolean(val value: Boolean) : DataReference<Boolean>() {
    override fun resolve(pathResolver: (String) -> Boolean?): Boolean = value
}

/**
 * A boolean value bound to a data model path.
 */
data class PathBoolean(val path: String) : DataReference<Boolean>() {
    override fun resolve(pathResolver: (String) -> Boolean?): Boolean? = pathResolver(path)
}

/**
 * A reference to a FunctionCall for computed values.
 *
 * @property call The function name (e.g., "formatString", "required")
 * @property args The function arguments
 * @property returnType The expected return type
 */
data class FunctionCallReference<T>(
    val call: String,
    val args: JsonObject?,
    val returnType: String?
) : DataReference<T>() {
    override fun resolve(pathResolver: (String) -> T?): T? {
        // FunctionCall resolution requires the function evaluation engine
        // which is provided by the FunctionEvaluator
        return null
    }
}

/**
 * A reference to a child component by ID.
 */
data class ComponentReference(val componentId: String)

/**
 * Represents children of a container widget (ChildList in v0.9).
 *
 * In v0.9, ChildList is either:
 * - A plain array of ComponentId strings: `["child1", "child2"]`
 * - A template object: `{"componentId": "item-template", "path": "/items"}`
 */
sealed class ChildrenReference {
    data class ExplicitList(val componentIds: List<String>) : ChildrenReference()
    data class Template(val componentId: String, val path: String) : ChildrenReference()
}

/**
 * Utilities for parsing data references from JSON.
 *
 * In v0.9, data references use implicit typing:
 * - Plain primitives are literals: `"text"`, `42`, `true`
 * - Objects with `path` key are DataBinding: `{"path": "/user/name"}`
 * - Objects with `call` key are FunctionCall: `{"call": "formatString", "args": {...}}`
 */
object DataReferenceParser {

    /**
     * Parses a DynamicString from a JSON element.
     *
     * v0.9 format:
     * - Plain string: literal value
     * - `{"path": "/x"}`: data binding
     * - `{"call": "fn", "args": {...}}`: function call
     */
    fun parseString(element: JsonElement?): DataReference<String>? {
        if (element == null) return null

        return when (element) {
            is JsonPrimitive -> LiteralString(element.contentOrNull ?: "")
            is JsonObject -> {
                element["path"]?.jsonPrimitive?.contentOrNull?.let { PathString(it) }
                    ?: element["call"]?.jsonPrimitive?.contentOrNull?.let { call ->
                        FunctionCallReference<String>(
                            call = call,
                            args = element["args"] as? JsonObject,
                            returnType = element["returnType"]?.jsonPrimitive?.contentOrNull
                        )
                    }
            }
            else -> null
        }
    }

    /**
     * Parses a DynamicNumber from a JSON element.
     *
     * v0.9 format:
     * - Plain number: literal value
     * - `{"path": "/x"}`: data binding
     * - `{"call": "fn", "args": {...}}`: function call
     */
    fun parseNumber(element: JsonElement?): DataReference<Double>? {
        if (element == null) return null

        return when (element) {
            is JsonPrimitive -> element.doubleOrNull?.let { LiteralNumber(it) }
            is JsonObject -> {
                element["path"]?.jsonPrimitive?.contentOrNull?.let { PathNumber(it) }
                    ?: element["call"]?.jsonPrimitive?.contentOrNull?.let { call ->
                        FunctionCallReference<Double>(
                            call = call,
                            args = element["args"] as? JsonObject,
                            returnType = element["returnType"]?.jsonPrimitive?.contentOrNull
                        )
                    }
            }
            else -> null
        }
    }

    /**
     * Parses a DynamicBoolean from a JSON element.
     *
     * v0.9 format:
     * - Plain boolean: literal value
     * - `{"path": "/x"}`: data binding
     * - `{"call": "fn", "args": {...}}`: function call
     */
    fun parseBoolean(element: JsonElement?): DataReference<Boolean>? {
        if (element == null) return null

        return when (element) {
            is JsonPrimitive -> element.booleanOrNull?.let { LiteralBoolean(it) }
            is JsonObject -> {
                element["path"]?.jsonPrimitive?.contentOrNull?.let { PathBoolean(it) }
                    ?: element["call"]?.jsonPrimitive?.contentOrNull?.let { call ->
                        FunctionCallReference<Boolean>(
                            call = call,
                            args = element["args"] as? JsonObject,
                            returnType = element["returnType"]?.jsonPrimitive?.contentOrNull
                        )
                    }
            }
            else -> null
        }
    }

    /**
     * Parses a DynamicStringList from a JSON element.
     *
     * v0.9 format:
     * - Plain array of strings: literal value
     * - `{"path": "/x"}`: data binding
     * - `{"call": "fn", "args": {...}}`: function call
     */
    fun parseStringList(element: JsonElement?): List<String>? {
        if (element == null) return null

        return when (element) {
            is JsonArray -> element.mapNotNull { it.jsonPrimitive.contentOrNull }
            is JsonObject -> {
                // Path binding or function call - would need runtime resolution
                null
            }
            else -> null
        }
    }

    /**
     * Parses a ComponentId reference from a JSON element.
     */
    fun parseComponentRef(element: JsonElement?): ComponentReference? {
        if (element == null) return null

        return when (element) {
            is JsonPrimitive -> element.contentOrNull?.let { ComponentReference(it) }
            else -> null
        }
    }

    /**
     * Parses a ChildList from a JSON element.
     *
     * v0.9 format:
     * - Plain array: `["child1", "child2"]` (static children)
     * - Object: `{"componentId": "template", "path": "/items"}` (template)
     */
    fun parseChildren(element: JsonElement?): ChildrenReference? {
        if (element == null) return null

        return when (element) {
            is JsonArray -> {
                val ids = element.mapNotNull { it.jsonPrimitive.contentOrNull }
                ChildrenReference.ExplicitList(ids)
            }
            is JsonObject -> {
                val componentId = element["componentId"]?.jsonPrimitive?.contentOrNull
                val path = element["path"]?.jsonPrimitive?.contentOrNull
                if (componentId != null && path != null) {
                    ChildrenReference.Template(componentId, path)
                } else {
                    null
                }
            }
            else -> null
        }
    }
}
