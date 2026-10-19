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

    @Test
    fun `DataContext update with List writes JsonArray`() {
        val model = DataModel()
        val context = model.createContext()

        context.update("/strings", listOf("a", "b"))
        context.update("/mixed", listOf("x", 1, true, null))

        assertEquals(
            JsonArray(listOf(JsonPrimitive("a"), JsonPrimitive("b"))),
            model.get("/strings")
        )
        assertEquals(
            JsonArray(listOf(JsonPrimitive("x"), JsonPrimitive(1.0), JsonPrimitive(true), JsonNull)),
            model.get("/mixed")
        )
        assertEquals(listOf("a", "b"), model.getStringList("/strings"))
    }

    @Test
    fun `DataContext update with JsonElement stores it unchanged`() {
        val model = DataModel()
        val value = JsonObject(mapOf("k" to JsonPrimitive("v")))

        model.createContext().update("/obj", value)

        assertEquals(value, model.get("/obj"))
    }

    @Test
    fun `DataContext get returns raw element`() {
        val array = JsonArray(listOf(JsonPrimitive("a")))
        val model = DataModel(JsonObject(mapOf("user" to JsonObject(mapOf("tags" to array)))))

        assertEquals(array, model.createContext("/user").get("/tags"))
    }

    @Test
    fun `typed getters return null for arrays and objects`() {
        val model = DataModel(JsonObject(mapOf(
            "list" to JsonArray(listOf(JsonPrimitive("a"))),
            "obj" to JsonObject(mapOf("k" to JsonPrimitive("v")))
        )))

        for (path in listOf("/list", "/obj")) {
            assertNull(model.getString(path))
            assertNull(model.getNumber(path))
            assertNull(model.getBoolean(path))
        }
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

    // ========== Array indexed writes (setAtPath array branch) ==========
    // RFC 6901 §4: integer token selects the array element at the zero-based index.
    // These tests cover the setAtPathInArray helper added to fix the silent-clobber bug
    // where any JsonArray at an intermediate path node was replaced with an empty JsonObject.

    @Test
    fun `index write into existing array appends at absolute index`() {
        val model = DataModel()
        val initial = JsonArray((0 until 3).map { JsonObject(mapOf("i" to JsonPrimitive(it))) })
        model.update("/items", initial)

        model.update("/items/3", JsonObject(mapOf("i" to JsonPrimitive(3))))

        val result = model.get("/items") as? JsonArray
        assertNotNull(result)
        assertEquals(4, result.size)
        assertEquals(3, ((result[3] as JsonObject)["i"] as JsonPrimitive).content.toInt())
    }

    @Test
    fun `index write preserves existing array elements`() {
        val model = DataModel()
        model.update("/items", JsonArray(listOf(
            JsonObject(mapOf("name" to JsonPrimitive("Row 0"))),
            JsonObject(mapOf("name" to JsonPrimitive("Row 1")))
        )))

        model.update("/items/2", JsonObject(mapOf("name" to JsonPrimitive("Row 2"))))

        val result = model.get("/items") as? JsonArray
        assertNotNull(result)
        assertEquals(3, result.size)
        assertEquals("Row 0", ((result[0] as JsonObject)["name"] as JsonPrimitive).content)
        assertEquals("Row 1", ((result[1] as JsonObject)["name"] as JsonPrimitive).content)
        assertEquals("Row 2", ((result[2] as JsonObject)["name"] as JsonPrimitive).content)
    }

    @Test
    fun `index write beyond bounds pads with JsonNull`() {
        val model = DataModel()
        model.update("/items", JsonArray(listOf(JsonPrimitive("a"))))

        // Write at index 3 — indices 1 and 2 must be padded
        model.update("/items/3", JsonPrimitive("d"))

        val result = model.get("/items") as? JsonArray
        assertNotNull(result)
        assertEquals(4, result.size)
        assertEquals("a", (result[0] as JsonPrimitive).content)
        assertEquals(JsonNull, result[1])
        assertEquals(JsonNull, result[2])
        assertEquals("d", (result[3] as JsonPrimitive).content)
    }

    @Test
    fun `index write overwrites existing element`() {
        val model = DataModel()
        model.update("/items", JsonArray(listOf(JsonPrimitive("original"), JsonPrimitive("keep"))))

        model.update("/items/0", JsonPrimitive("replaced"))

        val result = model.get("/items") as? JsonArray
        assertNotNull(result)
        assertEquals("replaced", (result[0] as JsonPrimitive).content)
        assertEquals("keep", (result[1] as JsonPrimitive).content)
    }

    @Test
    fun `non-numeric segment on array leaves array unchanged`() {
        val model = DataModel()
        model.update("/items", JsonArray(listOf(JsonPrimitive("a"), JsonPrimitive("b"))))

        // Non-numeric key into an array — should not crash, array unchanged
        model.update("/items/notAnIndex", JsonPrimitive("x"))

        val result = model.get("/items") as? JsonArray
        assertNotNull(result)
        assertEquals(2, result.size)
    }

    @Test
    fun `sparse index gap pads missing indices with JsonNull`() {
        val model = DataModel()
        model.update("/items", JsonArray((0 until 3).map { JsonObject(mapOf("i" to JsonPrimitive(it))) }))

        // Jump to index 5, skipping index 3 and 4
        model.update("/items/5", JsonObject(mapOf("i" to JsonPrimitive(5))))

        val result = model.get("/items") as? JsonArray
        assertNotNull(result)
        assertEquals(6, result.size)
        assertEquals(JsonNull, result[3])
        assertEquals(JsonNull, result[4])
        assertEquals(5, ((result[5] as JsonObject)["i"] as JsonPrimitive).content.toInt())
        // Null slots resolve to null, not crash
        assertNull(model.getString("/items/3/i"))
        assertNull(model.getString("/items/4/i"))
    }

    @Test
    fun `multiple sequential appends accumulate correctly`() {
        val model = DataModel()
        model.update("/rows", JsonArray((0 until 5).map { JsonObject(mapOf("i" to JsonPrimitive(it))) }))

        for (i in 5 until 10) {
            model.update("/rows/$i", JsonObject(mapOf("i" to JsonPrimitive(i))))
        }

        val result = model.get("/rows") as? JsonArray
        assertNotNull(result)
        assertEquals(10, result.size)
        assertEquals(0, ((result[0] as JsonObject)["i"] as JsonPrimitive).content.toInt())
        assertEquals(9, ((result[9] as JsonObject)["i"] as JsonPrimitive).content.toInt())
    }

    @Test
    fun `full array replace after index appends resets the array`() {
        val model = DataModel()
        model.update("/items", JsonArray(listOf(JsonPrimitive("old"))))
        model.update("/items/1", JsonPrimitive("appended"))

        // Full replace
        model.update("/items", JsonArray(listOf(JsonPrimitive("new"))))

        val result = model.get("/items") as? JsonArray
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("new", (result[0] as JsonPrimitive).content)
    }

    // ========== Array indexed deletes (deleteAtPath array branch) ==========
    // RFC 6901 §4: same integer-index traversal applies to delete operations.
    // These tests cover deleteAtPathInArray — the mirror of setAtPathInArray for removal.

    @Test
    fun `delete by index removes correct element`() {
        val model = DataModel()
        model.update("/items", JsonArray(listOf(
            JsonObject(mapOf("name" to JsonPrimitive("Row 0"))),
            JsonObject(mapOf("name" to JsonPrimitive("Row 1"))),
            JsonObject(mapOf("name" to JsonPrimitive("Row 2")))
        )))

        model.delete("/items/1")

        val result = model.get("/items") as? JsonArray
        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals("Row 0", ((result[0] as JsonObject)["name"] as JsonPrimitive).content)
        assertEquals("Row 2", ((result[1] as JsonObject)["name"] as JsonPrimitive).content)
    }

    @Test
    fun `delete first element shifts remaining elements`() {
        val model = DataModel()
        model.update("/items", JsonArray(listOf(JsonPrimitive("a"), JsonPrimitive("b"), JsonPrimitive("c"))))

        model.delete("/items/0")

        val result = model.get("/items") as? JsonArray
        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals("b", (result[0] as JsonPrimitive).content)
        assertEquals("c", (result[1] as JsonPrimitive).content)
    }

    @Test
    fun `delete last element shrinks array`() {
        val model = DataModel()
        model.update("/items", JsonArray(listOf(JsonPrimitive("a"), JsonPrimitive("b"), JsonPrimitive("c"))))

        model.delete("/items/2")

        val result = model.get("/items") as? JsonArray
        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals("a", (result[0] as JsonPrimitive).content)
        assertEquals("b", (result[1] as JsonPrimitive).content)
    }

    @Test
    fun `delete out of bounds index leaves array unchanged`() {
        val model = DataModel()
        model.update("/items", JsonArray(listOf(JsonPrimitive("a"), JsonPrimitive("b"))))

        model.delete("/items/5")

        val result = model.get("/items") as? JsonArray
        assertNotNull(result)
        assertEquals(2, result.size)
    }

    @Test
    fun `delete with non-numeric segment on array leaves array unchanged`() {
        val model = DataModel()
        model.update("/items", JsonArray(listOf(JsonPrimitive("a"), JsonPrimitive("b"))))

        model.delete("/items/notAnIndex")

        val result = model.get("/items") as? JsonArray
        assertNotNull(result)
        assertEquals(2, result.size)
    }

    @Test
    fun `delete nested field inside array element`() {
        val model = DataModel()
        model.update("/items", JsonArray(listOf(
            JsonObject(mapOf("name" to JsonPrimitive("Row 0"), "temp" to JsonPrimitive("remove me"))),
            JsonObject(mapOf("name" to JsonPrimitive("Row 1")))
        )))

        model.delete("/items/0/temp")

        val result = model.get("/items") as? JsonArray
        assertNotNull(result)
        val first = result[0] as JsonObject
        assertNull(first["temp"])
        assertEquals("Row 0", (first["name"] as JsonPrimitive).content)
        // Second element untouched
        assertEquals("Row 1", ((result[1] as JsonObject)["name"] as JsonPrimitive).content)
    }

    @Test
    fun `delete all elements one by one empties array`() {
        val model = DataModel()
        model.update("/items", JsonArray(listOf(JsonPrimitive("a"), JsonPrimitive("b"), JsonPrimitive("c"))))

        // Delete index 0 three times — each delete shifts remaining elements
        model.delete("/items/0")
        model.delete("/items/0")
        model.delete("/items/0")

        val result = model.get("/items") as? JsonArray
        assertNotNull(result)
        assertEquals(0, result.size)
    }

    @Test
    fun `delete at path where intermediate node is a primitive leaves model unchanged`() {
        val model = DataModel()
        model.update("/user/name", JsonPrimitive("Alice"))

        // "/user/name/first" — "name" is a primitive, not an object; deeper path is a no-op
        model.delete("/user/name/first")

        assertEquals("Alice", model.getString("/user/name"))
    }

    @Test
    fun `delete nested path through object then array`() {
        val model = DataModel()
        model.update("/data/items", JsonArray(listOf(
            JsonObject(mapOf("id" to JsonPrimitive(1))),
            JsonObject(mapOf("id" to JsonPrimitive(2)))
        )))

        model.delete("/data/items/0")

        val result = model.get("/data/items") as? JsonArray
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(2, ((result[0] as JsonObject)["id"] as JsonPrimitive).content.toInt())
    }

    @Test
    fun `delete nested path where array element is not an object leaves array unchanged`() {
        val model = DataModel()
        // Array of primitives — element at index 0 is not a JsonObject, so deeper path is a no-op
        model.update("/items", JsonArray(listOf(JsonPrimitive("a"), JsonPrimitive("b"))))

        model.delete("/items/0/name")

        val result = model.get("/items") as? JsonArray
        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals("a", (result[0] as JsonPrimitive).content)
    }

    @Test
    fun `index write nested path into existing array element`() {
        val model = DataModel()
        model.update("/items", JsonArray(listOf(
            JsonObject(mapOf("name" to JsonPrimitive("Row 0"))),
            JsonObject(mapOf("name" to JsonPrimitive("Row 1")))
        )))

        // Write a nested field inside an existing array element
        model.update("/items/0/label", JsonPrimitive("updated"))

        val result = model.get("/items") as? JsonArray
        assertNotNull(result)
        val first = result[0] as JsonObject
        assertEquals("Row 0", (first["name"] as JsonPrimitive).content)
        assertEquals("updated", (first["label"] as JsonPrimitive).content)
        // Second element untouched
        assertEquals("Row 1", ((result[1] as JsonObject)["name"] as JsonPrimitive).content)
    }

    @Test
    fun `index write at path where intermediate node is a primitive overwrites with object`() {
        val model = DataModel()
        model.update("/user/name", JsonPrimitive("Alice"))

        // "name" is a primitive — setAtPath's else branch creates a fresh JsonObject and continues
        model.update("/user/name/first", JsonPrimitive("Al"))

        assertEquals("Al", model.getString("/user/name/first"))
    }

    @Test
    fun `index write nested path where array element is null creates new object`() {
        val model = DataModel()
        // Pad with null first by writing beyond bounds
        model.update("/items", JsonArray(listOf(JsonPrimitive("a"))))
        model.update("/items/2", JsonNull)

        // Now write a nested path into the null slot — should create a new JsonObject
        model.update("/items/2/name", JsonPrimitive("created"))

        val result = model.get("/items") as? JsonArray
        assertNotNull(result)
        assertEquals("created", model.getString("/items/2/name"))
    }
}
