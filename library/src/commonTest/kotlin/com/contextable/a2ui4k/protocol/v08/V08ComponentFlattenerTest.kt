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

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class V08ComponentFlattenerTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun obj(str: String): JsonObject = json.parseToJsonElement(str) as JsonObject

    // --- component discriminator flattening ---

    @Test
    fun `nested component object becomes flat component string`() {
        val input = obj("""{"id":"t1","component":{"Text":{"text":{"literalString":"Hi"}}}}""")
        val out = V08ComponentFlattener.flatten(input)
        assertEquals("t1", (out["id"] as JsonPrimitive).content)
        assertEquals("Text", (out["component"] as JsonPrimitive).content)
        assertEquals("Hi", (out["text"] as JsonPrimitive).content)
    }

    @Test
    fun `already-flat v09 component is idempotent`() {
        val input = obj("""{"id":"t1","component":"Text","text":"Hi"}""")
        assertEquals(input, V08ComponentFlattener.flatten(input))
    }

    @Test
    fun `preserves weight on outer level`() {
        val input = obj("""{"id":"c","component":{"Column":{"children":{"explicitList":["a"]}}},"weight":3}""")
        val out = V08ComponentFlattener.flatten(input)
        assertEquals(JsonPrimitive(3), out["weight"])
        assertEquals("Column", (out["component"] as JsonPrimitive).content)
    }

    // --- literal unwrappers ---

    @Test
    fun `literalString is unwrapped to primitive`() {
        val out = V08ComponentFlattener.flatten(
            obj("""{"id":"x","component":{"Text":{"text":{"literalString":"hello"}}}}""")
        )
        assertEquals("hello", (out["text"] as JsonPrimitive).content)
    }

    @Test
    fun `literalNumber is unwrapped to primitive`() {
        val out = V08ComponentFlattener.flatten(
            obj("""{"id":"x","component":{"Slider":{"min":{"literalNumber":0},"max":{"literalNumber":100}}}}""")
        )
        assertEquals(JsonPrimitive(0), out["min"])
        assertEquals(JsonPrimitive(100), out["max"])
    }

    @Test
    fun `literalBoolean is unwrapped to primitive`() {
        val out = V08ComponentFlattener.flatten(
            obj("""{"id":"x","component":{"Button":{"primary":{"literalBoolean":true}}}}""")
        )
        assertEquals(JsonPrimitive(true), out["primary"])
    }

    @Test
    fun `path binding passes through unchanged`() {
        val out = V08ComponentFlattener.flatten(
            obj("""{"id":"x","component":{"Text":{"text":{"path":"/user/name"}}}}""")
        )
        val pathObj = out["text"] as JsonObject
        assertEquals("/user/name", (pathObj["path"] as JsonPrimitive).content)
    }

    @Test
    fun `legacy dataBinding rewrites to path`() {
        val out = V08ComponentFlattener.flatten(
            obj("""{"id":"x","component":{"Text":{"text":{"dataBinding":"/user/name"}}}}""")
        )
        val pathObj = out["text"] as JsonObject
        assertEquals("/user/name", (pathObj["path"] as JsonPrimitive).content)
    }

    // --- children unwrappers ---

    @Test
    fun `explicitList unwraps to plain array`() {
        val out = V08ComponentFlattener.flatten(
            obj("""{"id":"c","component":{"Column":{"children":{"explicitList":["a","b","c"]}}}}""")
        )
        val children = out["children"] as JsonArray
        assertEquals(3, children.size)
        assertEquals("a", (children[0] as JsonPrimitive).content)
    }

    @Test
    fun `template with dataBinding rewrites to v09 shape`() {
        val out = V08ComponentFlattener.flatten(
            obj("""{"id":"l","component":{"List":{"children":{"template":{"componentId":"item-tmpl","dataBinding":"/items"}}}}}""")
        )
        val tmpl = out["children"] as JsonObject
        assertEquals("item-tmpl", (tmpl["componentId"] as JsonPrimitive).content)
        assertEquals("/items", (tmpl["path"] as JsonPrimitive).content)
    }

    // --- deprecated widget renames ---

    @Test
    fun `MultipleChoice becomes ChoicePicker with multipleSelection variant`() {
        val out = V08ComponentFlattener.flatten(
            obj("""{"id":"mc","component":{"MultipleChoice":{"value":{"path":"/selections"}}}}""")
        )
        assertEquals("ChoicePicker", (out["component"] as JsonPrimitive).content)
        assertEquals("multipleSelection", (out["variant"] as JsonPrimitive).content)
    }

    @Test
    fun `SingleChoice becomes ChoicePicker with mutuallyExclusive variant`() {
        val out = V08ComponentFlattener.flatten(
            obj("""{"id":"sc","component":{"SingleChoice":{"value":{"path":"/selection"}}}}""")
        )
        assertEquals("ChoicePicker", (out["component"] as JsonPrimitive).content)
        assertEquals("mutuallyExclusive", (out["variant"] as JsonPrimitive).content)
    }

    // --- Button action rewrites (v0.8 shape → v0.9 event/functionCall wrapper) ---

    @Test
    fun `v0_8 Button action with array context is wrapped in event and flattened`() {
        val out = V08ComponentFlattener.flatten(
            obj("""
                {
                    "id":"btn",
                    "component":{"Button":{
                        "action":{
                            "name":"book_restaurant",
                            "context":[
                                {"key":"restaurantName","value":{"path":"name"}},
                                {"key":"address","value":{"path":"address"}}
                            ]
                        }
                    }}
                }
            """.trimIndent())
        )
        val action = out["action"] as JsonObject
        val event = action["event"] as JsonObject
        assertEquals("book_restaurant", (event["name"] as JsonPrimitive).content)
        val context = event["context"] as JsonObject
        val name = context["restaurantName"] as JsonObject
        assertEquals("name", (name["path"] as JsonPrimitive).content)
        val address = context["address"] as JsonObject
        assertEquals("address", (address["path"] as JsonPrimitive).content)
    }

    @Test
    fun `v0_8 Button action with object context is wrapped in event without reshaping context`() {
        val out = V08ComponentFlattener.flatten(
            obj("""
                {
                    "id":"btn",
                    "component":{"Button":{
                        "action":{
                            "name":"submit",
                            "context":{
                                "orderId":{"literalString":"123"},
                                "amount":{"literalNumber":42}
                            }
                        }
                    }}
                }
            """.trimIndent())
        )
        val action = out["action"] as JsonObject
        val event = action["event"] as JsonObject
        assertEquals("submit", (event["name"] as JsonPrimitive).content)
        val context = event["context"] as JsonObject
        assertEquals("123", (context["orderId"] as JsonPrimitive).content)
        assertEquals(JsonPrimitive(42), context["amount"])
    }

    @Test
    fun `v0_9 Button action with existing event wrapper is left alone`() {
        val out = V08ComponentFlattener.flatten(
            obj("""
                {
                    "id":"btn",
                    "component":{"Button":{
                        "action":{"event":{"name":"go","context":{"k":"v"}}}
                    }}
                }
            """.trimIndent())
        )
        val action = out["action"] as JsonObject
        val event = action["event"] as JsonObject
        assertEquals("go", (event["name"] as JsonPrimitive).content)
    }

    @Test
    fun `v0_9 Button action with functionCall is left alone`() {
        val out = V08ComponentFlattener.flatten(
            obj("""
                {
                    "id":"btn",
                    "component":{"Button":{
                        "action":{"functionCall":{"call":"openUrl","args":{"u":"x"}}}
                    }}
                }
            """.trimIndent())
        )
        val action = out["action"] as JsonObject
        assertEquals(true, action.containsKey("functionCall"))
        assertEquals(false, action.containsKey("event"))
    }

    @Test
    fun `v0_8 Button primary boolean becomes variant primary`() {
        val out = V08ComponentFlattener.flatten(
            obj("""{"id":"b","component":{"Button":{"primary":{"literalBoolean":true}}}}""")
        )
        assertEquals("primary", (out["variant"] as JsonPrimitive).content)
    }

    @Test
    fun `Button explicit variant wins over primary boolean`() {
        val out = V08ComponentFlattener.flatten(
            obj("""{"id":"b","component":{"Button":{"variant":{"literalString":"borderless"},"primary":{"literalBoolean":true}}}}""")
        )
        assertEquals("borderless", (out["variant"] as JsonPrimitive).content)
    }
}
