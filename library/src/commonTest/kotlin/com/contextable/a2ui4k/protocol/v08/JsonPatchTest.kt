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
import kotlinx.serialization.json.JsonElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Covers all six RFC 6902 operations plus JSON Pointer edge cases.
 *
 * We're not trying to re-verify the spec — these tests exist so a regression
 * that breaks v0.8 `ACTIVITY_DELTA` replay fails loudly in CI.
 */
class JsonPatchTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun doc(str: String): JsonElement = json.parseToJsonElement(str)
    private fun patch(str: String): JsonArray = json.parseToJsonElement(str) as JsonArray

    // --- add ----------------------------------------------------------

    @Test
    fun `add to object`() {
        val result = JsonPatch.apply(
            doc("""{"a":1}"""),
            patch("""[{"op":"add","path":"/b","value":2}]""")
        )
        assertEquals(doc("""{"a":1,"b":2}"""), result)
    }

    @Test
    fun `add replaces existing object key`() {
        val result = JsonPatch.apply(
            doc("""{"a":1}"""),
            patch("""[{"op":"add","path":"/a","value":9}]""")
        )
        assertEquals(doc("""{"a":9}"""), result)
    }

    @Test
    fun `add at array index inserts`() {
        val result = JsonPatch.apply(
            doc("""[1,3]"""),
            patch("""[{"op":"add","path":"/1","value":2}]""")
        )
        assertEquals(doc("""[1,2,3]"""), result)
    }

    @Test
    fun `add at array end with dash appends`() {
        val result = JsonPatch.apply(
            doc("""[1,2]"""),
            patch("""[{"op":"add","path":"/-","value":3}]""")
        )
        assertEquals(doc("""[1,2,3]"""), result)
    }

    @Test
    fun `add nested path`() {
        val result = JsonPatch.apply(
            doc("""{"operations":[]}"""),
            patch("""[{"op":"add","path":"/operations/0","value":{"kind":"x"}}]""")
        )
        assertEquals(doc("""{"operations":[{"kind":"x"}]}"""), result)
    }

    @Test
    fun `add to root replaces document`() {
        val result = JsonPatch.apply(
            doc("""{"a":1}"""),
            patch("""[{"op":"add","path":"","value":{"b":2}}]""")
        )
        assertEquals(doc("""{"b":2}"""), result)
    }

    // --- remove -------------------------------------------------------

    @Test
    fun `remove from object`() {
        val result = JsonPatch.apply(
            doc("""{"a":1,"b":2}"""),
            patch("""[{"op":"remove","path":"/a"}]""")
        )
        assertEquals(doc("""{"b":2}"""), result)
    }

    @Test
    fun `remove from array shifts indices`() {
        val result = JsonPatch.apply(
            doc("""[10,20,30]"""),
            patch("""[{"op":"remove","path":"/1"}]""")
        )
        assertEquals(doc("""[10,30]"""), result)
    }

    @Test
    fun `remove missing key throws`() {
        assertFailsWith<JsonPatchException> {
            JsonPatch.apply(
                doc("""{}"""),
                patch("""[{"op":"remove","path":"/missing"}]""")
            )
        }
    }

    // --- replace ------------------------------------------------------

    @Test
    fun `replace in object`() {
        val result = JsonPatch.apply(
            doc("""{"a":1}"""),
            patch("""[{"op":"replace","path":"/a","value":42}]""")
        )
        assertEquals(doc("""{"a":42}"""), result)
    }

    @Test
    fun `replace in array`() {
        val result = JsonPatch.apply(
            doc("""[1,2,3]"""),
            patch("""[{"op":"replace","path":"/1","value":99}]""")
        )
        assertEquals(doc("""[1,99,3]"""), result)
    }

    // --- move ---------------------------------------------------------

    @Test
    fun `move within object`() {
        val result = JsonPatch.apply(
            doc("""{"a":1,"b":null}"""),
            patch("""[{"op":"move","from":"/a","path":"/b"}]""")
        )
        assertEquals(doc("""{"b":1}"""), result)
    }

    @Test
    fun `move within array`() {
        val result = JsonPatch.apply(
            doc("""[1,2,3]"""),
            patch("""[{"op":"move","from":"/0","path":"/2"}]""")
        )
        assertEquals(doc("""[2,3,1]"""), result)
    }

    // --- copy ---------------------------------------------------------

    @Test
    fun `copy duplicates value`() {
        val result = JsonPatch.apply(
            doc("""{"a":1}"""),
            patch("""[{"op":"copy","from":"/a","path":"/b"}]""")
        )
        assertEquals(doc("""{"a":1,"b":1}"""), result)
    }

    @Test
    fun `copy into array`() {
        val result = JsonPatch.apply(
            doc("""{"src":"hello","dst":[]}"""),
            patch("""[{"op":"copy","from":"/src","path":"/dst/0"}]""")
        )
        assertEquals(doc("""{"src":"hello","dst":["hello"]}"""), result)
    }

    // --- test ---------------------------------------------------------

    @Test
    fun `test passes when values equal`() {
        val result = JsonPatch.apply(
            doc("""{"a":1}"""),
            patch("""[{"op":"test","path":"/a","value":1}]""")
        )
        assertEquals(doc("""{"a":1}"""), result)
    }

    @Test
    fun `test fails when values differ`() {
        assertFailsWith<JsonPatchException> {
            JsonPatch.apply(
                doc("""{"a":1}"""),
                patch("""[{"op":"test","path":"/a","value":2}]""")
            )
        }
    }

    // --- multi-op sequences ------------------------------------------

    @Test
    fun `multiple ops applied in order`() {
        val result = JsonPatch.apply(
            doc("""{"operations":[]}"""),
            patch(
                """[
                    {"op":"add","path":"/operations/0","value":{"k":"a"}},
                    {"op":"add","path":"/operations/1","value":{"k":"b"}},
                    {"op":"add","path":"/operations/1","value":{"k":"mid"}}
                ]"""
            )
        )
        assertEquals(doc("""{"operations":[{"k":"a"},{"k":"mid"},{"k":"b"}]}"""), result)
    }

    // --- unknown op --------------------------------------------------

    @Test
    fun `unknown op throws`() {
        assertFailsWith<JsonPatchException> {
            JsonPatch.apply(
                doc("""{}"""),
                patch("""[{"op":"frobnicate","path":"/a","value":1}]""")
            )
        }
    }

    // --- pointer parsing ----------------------------------------------

    @Test
    fun `pointer parser handles empty path as root`() {
        assertEquals(emptyList(), JsonPointer.parse(""))
    }

    @Test
    fun `pointer parser unescapes tildes correctly`() {
        // "~0" → "~", "~1" → "/"; order matters per RFC 6901
        assertEquals(listOf("a/b", "c~d"), JsonPointer.parse("/a~1b/c~0d"))
    }

    @Test
    fun `pointer parser rejects non-slash-prefixed non-empty paths`() {
        assertFailsWith<IllegalArgumentException> {
            JsonPointer.parse("abc")
        }
    }
}
