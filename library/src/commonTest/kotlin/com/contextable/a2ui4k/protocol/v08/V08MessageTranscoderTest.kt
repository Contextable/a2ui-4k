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

import com.contextable.a2ui4k.extension.A2UIExtension
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class V08MessageTranscoderTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun obj(str: String): JsonObject = json.parseToJsonElement(str) as JsonObject

    // --- envelope detection ---

    @Test
    fun `ACTIVITY_SNAPSHOT recognized as v08`() {
        val t = V08MessageTranscoder()
        assertTrue(t.isV08Envelope(obj("""{"kind":"ACTIVITY_SNAPSHOT","content":{"operations":[]}}""")))
    }

    @Test
    fun `ACTIVITY_DELTA recognized as v08`() {
        val t = V08MessageTranscoder()
        assertTrue(t.isV08Envelope(obj("""{"kind":"ACTIVITY_DELTA","patch":[]}""")))
    }

    @Test
    fun `version v08 recognized as v08`() {
        val t = V08MessageTranscoder()
        assertTrue(t.isV08Envelope(obj("""{"version":"v0.8","createSurface":{}}""")))
    }

    @Test
    fun `v09 envelope not recognized as v08`() {
        val t = V08MessageTranscoder()
        assertFalse(t.isV08Envelope(obj("""{"version":"v0.9","createSurface":{"surfaceId":"s"}}""")))
    }

    // --- beginRendering → createSurface ---

    @Test
    fun `beginRendering becomes createSurface with v08 catalogId`() {
        val t = V08MessageTranscoder()
        val out = t.transcode(obj("""
            {
                "kind":"ACTIVITY_SNAPSHOT",
                "messageId":"m1",
                "content":{"operations":[
                    {"beginRendering":{"surfaceId":"s1","root":"root","styles":{"theme":"dark"}}}
                ]}
            }
        """.trimIndent()))

        assertEquals(1, out.size)
        val msg = out[0]
        assertEquals("v0.9", (msg["version"] as JsonPrimitive).content)
        val createSurface = msg["createSurface"] as JsonObject
        assertEquals("s1", (createSurface["surfaceId"] as JsonPrimitive).content)
        assertEquals(A2UIExtension.STANDARD_CATALOG_URI_V08, (createSurface["catalogId"] as JsonPrimitive).content)
        val theme = createSurface["theme"] as JsonObject
        assertEquals("dark", (theme["theme"] as JsonPrimitive).content)
        assertEquals("root", (createSurface["rootComponentId"] as JsonPrimitive).content)
    }

    // --- surfaceUpdate → updateComponents ---

    @Test
    fun `surfaceUpdate transcodes and flattens components`() {
        val t = V08MessageTranscoder()
        val out = t.transcode(obj("""
            {
                "kind":"ACTIVITY_SNAPSHOT",
                "content":{"operations":[
                    {"surfaceUpdate":{"surfaceId":"s1","components":[
                        {"id":"root","component":{"Column":{"children":{"explicitList":["t"]}}}},
                        {"id":"t","component":{"Text":{"text":{"literalString":"Hello"}}}}
                    ]}}
                ]}
            }
        """.trimIndent()))

        assertEquals(1, out.size)
        val upd = out[0]["updateComponents"] as JsonObject
        val components = upd["components"] as JsonArray
        assertEquals(2, components.size)

        val col = components[0] as JsonObject
        assertEquals("Column", (col["component"] as JsonPrimitive).content)
        val children = col["children"] as JsonArray
        assertEquals("t", (children[0] as JsonPrimitive).content)

        val text = components[1] as JsonObject
        assertEquals("Text", (text["component"] as JsonPrimitive).content)
        assertEquals("Hello", (text["text"] as JsonPrimitive).content)
    }

    // --- dataModelUpdate → updateDataModel ---

    @Test
    fun `dataModelUpdate with mixed DataEntry types`() {
        val t = V08MessageTranscoder()
        val out = t.transcode(obj("""
            {
                "kind":"ACTIVITY_SNAPSHOT",
                "content":{"operations":[
                    {"dataModelUpdate":{"surfaceId":"s1","path":"/user","contents":[
                        {"key":"name","valueString":"Alice"},
                        {"key":"age","valueNumber":30},
                        {"key":"active","valueBoolean":true}
                    ]}}
                ]}
            }
        """.trimIndent()))

        assertEquals(1, out.size)
        val udm = out[0]["updateDataModel"] as JsonObject
        assertEquals("/user", (udm["path"] as JsonPrimitive).content)
        val value = udm["value"] as JsonObject
        assertEquals("Alice", (value["name"] as JsonPrimitive).content)
        assertEquals(JsonPrimitive(30), value["age"])
        assertEquals(JsonPrimitive(true), value["active"])
    }

    @Test
    fun `dataModelUpdate with nested valueMap`() {
        val t = V08MessageTranscoder()
        val out = t.transcode(obj("""
            {
                "kind":"ACTIVITY_SNAPSHOT",
                "content":{"operations":[
                    {"dataModelUpdate":{"surfaceId":"s1","path":"/","contents":[
                        {"key":"address","valueMap":[
                            {"key":"city","valueString":"NYC"},
                            {"key":"zip","valueString":"10001"}
                        ]}
                    ]}}
                ]}
            }
        """.trimIndent()))

        val udm = out[0]["updateDataModel"] as JsonObject
        val value = udm["value"] as JsonObject
        val address = value["address"] as JsonObject
        assertEquals("NYC", (address["city"] as JsonPrimitive).content)
        assertEquals("10001", (address["zip"] as JsonPrimitive).content)
    }

    // --- deleteSurface (identity) ---

    @Test
    fun `deleteSurface is passed through`() {
        val t = V08MessageTranscoder()
        val out = t.transcode(obj("""
            {
                "kind":"ACTIVITY_SNAPSHOT",
                "content":{"operations":[
                    {"deleteSurface":{"surfaceId":"s1"}}
                ]}
            }
        """.trimIndent()))
        val del = out[0]["deleteSurface"] as JsonObject
        assertEquals("s1", (del["surfaceId"] as JsonPrimitive).content)
    }

    // --- ACTIVITY_DELTA ---

    @Test
    fun `delta applies JSON Patch and emits only new operations`() {
        val t = V08MessageTranscoder()

        // First: establish snapshot baseline with one op.
        val snap = t.transcode(obj("""
            {
                "kind":"ACTIVITY_SNAPSHOT",
                "messageId":"m1",
                "content":{"operations":[
                    {"beginRendering":{"surfaceId":"s1","root":"root"}}
                ]}
            }
        """.trimIndent()))
        assertEquals(1, snap.size)

        // Delta adds a second op to the cached operations array.
        val delta = t.transcode(obj("""
            {
                "kind":"ACTIVITY_DELTA",
                "messageId":"m1",
                "patch":[
                    {"op":"add","path":"/operations/1","value":{
                        "surfaceUpdate":{"surfaceId":"s1","components":[
                            {"id":"t","component":{"Text":{"text":{"literalString":"Hi"}}}}
                        ]}
                    }}
                ]
            }
        """.trimIndent()))

        // Only the *new* operation is emitted; the baseline isn't re-emitted.
        assertEquals(1, delta.size)
        assertNotNull(delta[0]["updateComponents"])
        assertNull(delta[0]["createSurface"])
    }

    @Test
    fun `delta that fails patch returns empty list rather than throwing`() {
        val t = V08MessageTranscoder()

        // Baseline: one op.
        t.transcode(obj("""
            {
                "kind":"ACTIVITY_SNAPSHOT",
                "messageId":"m1",
                "content":{"operations":[{"beginRendering":{"surfaceId":"s1","root":"root"}}]}
            }
        """.trimIndent()))

        // Patch points at a non-existent path.
        val delta = t.transcode(obj("""
            {
                "kind":"ACTIVITY_DELTA",
                "messageId":"m1",
                "patch":[{"op":"remove","path":"/operations/99"}]
            }
        """.trimIndent()))

        assertEquals(emptyList(), delta)
    }

    // --- MultipleChoice rename ---

    @Test
    fun `MultipleChoice widget becomes ChoicePicker with multipleSelection variant`() {
        val t = V08MessageTranscoder()
        val out = t.transcode(obj("""
            {
                "kind":"ACTIVITY_SNAPSHOT",
                "content":{"operations":[
                    {"surfaceUpdate":{"surfaceId":"s1","components":[
                        {"id":"mc","component":{"MultipleChoice":{"value":{"path":"/sel"}}}}
                    ]}}
                ]}
            }
        """.trimIndent()))

        val components = (out[0]["updateComponents"] as JsonObject)["components"] as JsonArray
        val mc = components[0] as JsonObject
        assertEquals("ChoicePicker", (mc["component"] as JsonPrimitive).content)
        assertEquals("multipleSelection", (mc["variant"] as JsonPrimitive).content)
    }
}
