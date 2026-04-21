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

package com.contextable.a2ui4k.agent

import com.contextable.a2ui4k.extension.A2UIExtension
import com.contextable.a2ui4k.model.ProtocolVersion
import com.contextable.a2ui4k.state.SurfaceStateManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [A2UiRenderTool], the canonical client-side `render_a2ui` helper
 * that drives a [SurfaceStateManager] from a single tool-call argument
 * object.
 */
class A2UiRenderToolTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun args(body: String): JsonObject = json.decodeFromString(body)

    // --- happy paths ---

    @Test
    fun `happy path renders surface with components and data`() {
        val manager = SurfaceStateManager()
        val tool = A2UiRenderTool(manager)

        val result = tool.render(
            args(
                """
                {
                    "surfaceId":"s1",
                    "catalogId":"${A2UIExtension.STANDARD_CATALOG_URI}",
                    "components":[
                        {"id":"root","component":"Column","children":["title"]},
                        {"id":"title","component":"Text","text":"Hello"}
                    ],
                    "data":{"user":{"name":"Alice"}}
                }
                """.trimIndent()
            )
        )

        assertEquals("rendered", (result["status"] as? JsonPrimitive)?.content)
        assertEquals("s1", (result["surfaceId"] as? JsonPrimitive)?.content)

        val surface = manager.getSurface("s1")
        assertNotNull(surface)
        assertEquals(A2UIExtension.STANDARD_CATALOG_URI, surface.catalogId)
        assertEquals(2, surface.components.size)
        assertEquals("Column", surface.components["root"]?.widgetType)
        assertEquals("Text", surface.components["title"]?.widgetType)

        assertEquals("Alice", manager.getDataModel("s1")?.getString("/user/name"))
    }

    @Test
    fun `render without data still creates surface and components`() {
        val manager = SurfaceStateManager()
        val tool = A2UiRenderTool(manager)

        tool.render(
            args(
                """
                {
                    "surfaceId":"s2",
                    "components":[
                        {"id":"root","component":"Text","text":"Hi"}
                    ]
                }
                """.trimIndent()
            )
        )

        val surface = manager.getSurface("s2")
        assertNotNull(surface)
        assertEquals(1, surface.components.size)
        // No data was written.
        assertNull(manager.getDataModel("s2")?.getString("/user/name"))
    }

    // --- catalogId handling ---

    @Test
    fun `explicit catalogId overrides default`() {
        val manager = SurfaceStateManager()
        val tool = A2UiRenderTool(manager)

        tool.render(
            args(
                """
                {
                    "surfaceId":"s3",
                    "catalogId":"https://example.com/custom.json",
                    "components":[{"id":"root","component":"Text","text":"X"}]
                }
                """.trimIndent()
            )
        )

        assertEquals("https://example.com/custom.json", manager.getSurface("s3")?.catalogId)
    }

    @Test
    fun `omitted catalogId uses DEFAULT_CATALOG_ID`() {
        val manager = SurfaceStateManager()
        val tool = A2UiRenderTool(manager)

        tool.render(
            args(
                """
                {
                    "surfaceId":"s4",
                    "components":[{"id":"root","component":"Text","text":"X"}]
                }
                """.trimIndent()
            )
        )

        assertEquals(A2UIExtension.STANDARD_CATALOG_URI, A2UiRenderTool.DEFAULT_CATALOG_ID)
        assertEquals(A2UIExtension.STANDARD_CATALOG_URI, manager.getSurface("s4")?.catalogId)
    }

    // --- validation errors ---

    @Test
    fun `missing surfaceId throws with MISSING_REQUIRED_FIELD and field surfaceId`() {
        val manager = SurfaceStateManager()
        val tool = A2UiRenderTool(manager)

        val ex = assertFailsWith<A2UiRenderException> {
            tool.render(args("""{"components":[]}"""))
        }
        assertEquals(A2UiRenderException.ValidationCode.MISSING_REQUIRED_FIELD, ex.code)
        assertEquals("surfaceId", ex.field)
        // Nothing landed in the manager.
        assertEquals(0, manager.surfaceCount)
    }

    @Test
    fun `missing components throws with MISSING_REQUIRED_FIELD and field components`() {
        val manager = SurfaceStateManager()
        val tool = A2UiRenderTool(manager)

        val ex = assertFailsWith<A2UiRenderException> {
            tool.render(args("""{"surfaceId":"s"}"""))
        }
        assertEquals(A2UiRenderException.ValidationCode.MISSING_REQUIRED_FIELD, ex.code)
        assertEquals("components", ex.field)
    }

    @Test
    fun `non-string surfaceId throws with WRONG_TYPE`() {
        val manager = SurfaceStateManager()
        val tool = A2UiRenderTool(manager)

        val ex = assertFailsWith<A2UiRenderException> {
            tool.render(args("""{"surfaceId":42,"components":[]}"""))
        }
        assertEquals(A2UiRenderException.ValidationCode.WRONG_TYPE, ex.code)
        assertEquals("surfaceId", ex.field)
    }

    @Test
    fun `non-array components throws with WRONG_TYPE`() {
        val manager = SurfaceStateManager()
        val tool = A2UiRenderTool(manager)

        val ex = assertFailsWith<A2UiRenderException> {
            tool.render(args("""{"surfaceId":"s","components":{}}"""))
        }
        assertEquals(A2UiRenderException.ValidationCode.WRONG_TYPE, ex.code)
        assertEquals("components", ex.field)
    }

    @Test
    fun `blank surfaceId throws with EMPTY_VALUE`() {
        val manager = SurfaceStateManager()
        val tool = A2UiRenderTool(manager)

        val ex = assertFailsWith<A2UiRenderException> {
            tool.render(args("""{"surfaceId":"  ","components":[]}"""))
        }
        assertEquals(A2UiRenderException.ValidationCode.EMPTY_VALUE, ex.code)
        assertEquals("surfaceId", ex.field)
    }

    // --- edge cases ---

    @Test
    fun `non-object data is silently ignored`() {
        val manager = SurfaceStateManager()
        val tool = A2UiRenderTool(manager)

        tool.render(
            args(
                """
                {
                    "surfaceId":"s5",
                    "components":[{"id":"root","component":"Text","text":"X"}],
                    "data":[1,2,3]
                }
                """.trimIndent()
            )
        )

        // Surface exists, but data model was never populated from the array.
        val surface = manager.getSurface("s5")
        assertNotNull(surface)
        val dm = manager.getDataModel("s5")
        assertNotNull(dm)
        assertNull(dm.get("/0"))
    }

    @Test
    fun `repeated render into same surfaceId merges components`() {
        val manager = SurfaceStateManager()
        val tool = A2UiRenderTool(manager)

        tool.render(
            args(
                """
                {
                    "surfaceId":"s6",
                    "components":[{"id":"root","component":"Text","text":"First"}]
                }
                """.trimIndent()
            )
        )
        tool.render(
            args(
                """
                {
                    "surfaceId":"s6",
                    "components":[{"id":"root","component":"Text","text":"Second"}]
                }
                """.trimIndent()
            )
        )

        val root = manager.getSurface("s6")?.components?.get("root")
        assertNotNull(root)
        assertEquals("Second", (root.widgetData["text"] as? JsonPrimitive)?.content)
        assertEquals(1, manager.surfaceCount)
    }

    @Test
    fun `render passes v0_9 envelopes so manager tags surface as V0_9`() {
        val manager = SurfaceStateManager()
        val tool = A2UiRenderTool(manager)

        tool.render(
            args(
                """
                {
                    "surfaceId":"s7",
                    "components":[{"id":"root","component":"Text","text":"X"}]
                }
                """.trimIndent()
            )
        )

        assertEquals(ProtocolVersion.V0_9, manager.getSurfaceProtocolVersion("s7"))
    }

    // --- schema / description smoke ---

    @Test
    fun `description and parameters are non-empty and structured correctly`() {
        val tool = A2UiRenderTool(SurfaceStateManager())

        assertEquals("render_a2ui", tool.name)
        assertTrue(tool.description.isNotBlank())

        val params = tool.parameters
        assertEquals("object", (params["type"] as? JsonPrimitive)?.content)

        val required = params["required"] as? JsonArray
        assertNotNull(required)
        val requiredNames = required.mapNotNull { (it as? JsonPrimitive)?.content }.toSet()
        assertTrue(requiredNames.containsAll(setOf("surfaceId", "components")))

        assertEquals(false, (params["additionalProperties"] as? JsonPrimitive)?.content?.toBoolean())
    }

    @Test
    fun `custom description override is exposed`() {
        val tool = A2UiRenderTool(SurfaceStateManager(), description = "custom text")
        assertEquals("custom text", tool.description)
    }

    @Test
    fun `A2UiRenderException carries structured code and field`() {
        val ex = A2UiRenderException(
            A2UiRenderException.ValidationCode.WRONG_TYPE,
            "surfaceId",
            "test message",
        )
        assertEquals(A2UiRenderException.ValidationCode.WRONG_TYPE, ex.code)
        assertEquals("surfaceId", ex.field)
        assertEquals("test message", ex.message)
    }

    // --- schema boolean sanity (`assertFalse` to catch accidental inversion) ---

    @Test
    fun `top-level additionalProperties is false to block unknown fields`() {
        val tool = A2UiRenderTool(SurfaceStateManager())
        val additional = (tool.parameters["additionalProperties"] as? JsonPrimitive)?.content
        assertFalse(additional?.toBoolean() ?: true)
    }
}
