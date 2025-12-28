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

package com.contextable.a2ui4k.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for DataModel - the centralized reactive data store for A2UI surfaces.
 *
 * DataModel uses JSON Pointer paths (e.g., "/user/name") for data access.
 */
class DataModelTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ========== Basic operations ==========

    @Test
    fun `empty DataModel has empty data`() {
        val model = DataModel()

        assertTrue(model.currentData.isEmpty())
    }

    @Test
    fun `DataModel with initial data`() {
        val initial = JsonObject(mapOf("name" to JsonPrimitive("John")))
        val model = DataModel(initial)

        assertEquals(initial, model.currentData)
    }

    @Test
    fun `setData replaces entire data`() {
        val model = DataModel(JsonObject(mapOf("old" to JsonPrimitive("data"))))
        val newData = JsonObject(mapOf("new" to JsonPrimitive("data")))

        model.setData(newData)

        assertEquals(newData, model.currentData)
        assertNull(model.getString("/old"))
    }

    // ========== getString tests ==========

    @Test
    fun `getString returns value at path`() {
        val data = JsonObject(mapOf("name" to JsonPrimitive("Alice")))
        val model = DataModel(data)

        assertEquals("Alice", model.getString("/name"))
    }

    @Test
    fun `getString returns null for missing path`() {
        val model = DataModel()

        assertNull(model.getString("/missing"))
    }

    @Test
    fun `getString with nested path`() {
        val data = JsonObject(mapOf(
            "user" to JsonObject(mapOf(
                "profile" to JsonObject(mapOf(
                    "name" to JsonPrimitive("Bob")
                ))
            ))
        ))
        val model = DataModel(data)

        assertEquals("Bob", model.getString("/user/profile/name"))
    }

    // ========== getNumber tests ==========

    @Test
    fun `getNumber returns value at path`() {
        val data = JsonObject(mapOf("count" to JsonPrimitive(42)))
        val model = DataModel(data)

        assertEquals(42.0, model.getNumber("/count"))
    }

    @Test
    fun `getNumber returns null for missing path`() {
        val model = DataModel()

        assertNull(model.getNumber("/missing"))
    }

    @Test
    fun `getNumber with decimal`() {
        val data = JsonObject(mapOf("price" to JsonPrimitive(19.99)))
        val model = DataModel(data)

        assertEquals(19.99, model.getNumber("/price"))
    }

    // ========== getBoolean tests ==========

    @Test
    fun `getBoolean returns true`() {
        val data = JsonObject(mapOf("enabled" to JsonPrimitive(true)))
        val model = DataModel(data)

        assertEquals(true, model.getBoolean("/enabled"))
    }

    @Test
    fun `getBoolean returns false`() {
        val data = JsonObject(mapOf("disabled" to JsonPrimitive(false)))
        val model = DataModel(data)

        assertEquals(false, model.getBoolean("/disabled"))
    }

    @Test
    fun `getBoolean returns null for missing path`() {
        val model = DataModel()

        assertNull(model.getBoolean("/missing"))
    }

    // ========== getStringList tests ==========

    @Test
    fun `getStringList returns list`() {
        val data = JsonObject(mapOf(
            "tags" to JsonArray(listOf(
                JsonPrimitive("a"),
                JsonPrimitive("b"),
                JsonPrimitive("c")
            ))
        ))
        val model = DataModel(data)

        assertEquals(listOf("a", "b", "c"), model.getStringList("/tags"))
    }

    @Test
    fun `getStringList returns empty list for empty array`() {
        val data = JsonObject(mapOf("tags" to JsonArray(emptyList())))
        val model = DataModel(data)

        assertEquals(emptyList(), model.getStringList("/tags"))
    }

    @Test
    fun `getStringList returns null for missing path`() {
        val model = DataModel()

        assertNull(model.getStringList("/missing"))
    }

    // ========== getArraySize tests ==========

    @Test
    fun `getArraySize returns correct size`() {
        val data = JsonObject(mapOf(
            "items" to JsonArray(listOf(
                JsonPrimitive("1"),
                JsonPrimitive("2"),
                JsonPrimitive("3")
            ))
        ))
        val model = DataModel(data)

        assertEquals(3, model.getArraySize("/items"))
    }

    @Test
    fun `getArraySize returns 0 for empty array`() {
        val data = JsonObject(mapOf("items" to JsonArray(emptyList())))
        val model = DataModel(data)

        assertEquals(0, model.getArraySize("/items"))
    }

    @Test
    fun `getArraySize returns null for non-array`() {
        val data = JsonObject(mapOf("notArray" to JsonPrimitive("string")))
        val model = DataModel(data)

        assertNull(model.getArraySize("/notArray"))
    }

    // ========== getObjectKeys tests ==========

    @Test
    fun `getObjectKeys returns keys`() {
        val data = JsonObject(mapOf(
            "config" to JsonObject(mapOf(
                "theme" to JsonPrimitive("dark"),
                "language" to JsonPrimitive("en")
            ))
        ))
        val model = DataModel(data)

        val keys = model.getObjectKeys("/config")
        assertNotNull(keys)
        assertTrue(keys.contains("theme"))
        assertTrue(keys.contains("language"))
    }

    @Test
    fun `getObjectKeys returns null for non-object`() {
        val data = JsonObject(mapOf("value" to JsonPrimitive("string")))
        val model = DataModel(data)

        assertNull(model.getObjectKeys("/value"))
    }

    // ========== update tests ==========

    @Test
    fun `updateString sets value at path`() {
        val model = DataModel()

        model.updateString("/name", "Charlie")

        assertEquals("Charlie", model.getString("/name"))
    }

    @Test
    fun `updateNumber sets value at path`() {
        val model = DataModel()

        model.updateNumber("/score", 100.0)

        assertEquals(100.0, model.getNumber("/score"))
    }

    @Test
    fun `updateBoolean sets value at path`() {
        val model = DataModel()

        model.updateBoolean("/active", true)

        assertEquals(true, model.getBoolean("/active"))
    }

    @Test
    fun `update creates nested path`() {
        val model = DataModel()

        model.updateString("/user/profile/name", "David")

        assertEquals("David", model.getString("/user/profile/name"))
    }

    @Test
    fun `update preserves sibling values`() {
        val data = JsonObject(mapOf(
            "user" to JsonObject(mapOf(
                "name" to JsonPrimitive("Eve"),
                "age" to JsonPrimitive(25)
            ))
        ))
        val model = DataModel(data)

        model.updateString("/user/name", "Frank")

        assertEquals("Frank", model.getString("/user/name"))
        assertEquals(25.0, model.getNumber("/user/age"))
    }

    @Test
    fun `update with JsonElement directly`() {
        val model = DataModel()
        val obj = JsonObject(mapOf(
            "city" to JsonPrimitive("NYC"),
            "zip" to JsonPrimitive("10001")
        ))

        model.update("/address", obj)

        assertEquals("NYC", model.getString("/address/city"))
        assertEquals("10001", model.getString("/address/zip"))
    }

    @Test
    fun `update root with JsonObject`() {
        val model = DataModel(JsonObject(mapOf("old" to JsonPrimitive("data"))))
        val newRoot = JsonObject(mapOf("new" to JsonPrimitive("data")))

        model.update("/", newRoot)

        assertEquals("data", model.getString("/new"))
    }

    // ========== Array access tests ==========

    @Test
    fun `get with array index path`() {
        val data = JsonObject(mapOf(
            "items" to JsonArray(listOf(
                JsonObject(mapOf("name" to JsonPrimitive("First"))),
                JsonObject(mapOf("name" to JsonPrimitive("Second")))
            ))
        ))
        val model = DataModel(data)

        assertEquals("First", model.getString("/items/0/name"))
        assertEquals("Second", model.getString("/items/1/name"))
    }

    @Test
    fun `get with out of bounds array index returns null`() {
        val data = JsonObject(mapOf(
            "items" to JsonArray(listOf(JsonPrimitive("only one")))
        ))
        val model = DataModel(data)

        assertNull(model.get("/items/5"))
    }

    // ========== DataContext tests ==========

    @Test
    fun `createContext returns working context`() {
        val data = JsonObject(mapOf("value" to JsonPrimitive("test")))
        val model = DataModel(data)
        val context = model.createContext()

        assertEquals("test", context.getString("/value"))
    }

    @Test
    fun `createContext with basePath`() {
        val data = JsonObject(mapOf(
            "user" to JsonObject(mapOf(
                "name" to JsonPrimitive("Grace")
            ))
        ))
        val model = DataModel(data)
        val context = model.createContext("/user")

        assertEquals("Grace", context.getString("/name"))
    }

    @Test
    fun `DataContext update modifies model`() {
        val model = DataModel()
        val context = model.createContext()

        context.update("/name", "Henry")

        assertEquals("Henry", model.getString("/name"))
    }

    @Test
    fun `DataContext withBasePath creates nested context`() {
        val data = JsonObject(mapOf(
            "level1" to JsonObject(mapOf(
                "level2" to JsonObject(mapOf(
                    "value" to JsonPrimitive("deep")
                ))
            ))
        ))
        val model = DataModel(data)
        val context = model.createContext("/level1")
        val nestedContext = context.withBasePath("/level2")

        assertEquals("deep", nestedContext.getString("/value"))
    }

    @Test
    fun `DataContext update with different types`() {
        val model = DataModel()
        val context = model.createContext()

        context.update("/string", "text")
        context.update("/number", 42)
        context.update("/boolean", true)
        context.update("/null", null)

        assertEquals("text", model.getString("/string"))
        assertEquals(42.0, model.getNumber("/number"))
        assertEquals(true, model.getBoolean("/boolean"))
        assertEquals(JsonNull, model.get("/null"))
    }

    // ========== Edge cases ==========

    @Test
    fun `empty path returns root`() {
        val data = JsonObject(mapOf("key" to JsonPrimitive("value")))
        val model = DataModel(data)

        assertEquals(data, model.get(""))
    }

    @Test
    fun `slash only path returns root`() {
        val data = JsonObject(mapOf("key" to JsonPrimitive("value")))
        val model = DataModel(data)

        assertEquals(data, model.get("/"))
    }

    @Test
    fun `path with trailing slash`() {
        val data = JsonObject(mapOf("name" to JsonPrimitive("test")))
        val model = DataModel(data)

        // Trailing slash should be handled
        assertEquals("test", model.getString("/name/"))
    }

    @Test
    fun `observePath returns StateFlow`() {
        val model = DataModel()
        val flow = model.observePath("/test")

        assertNotNull(flow)
    }
}
