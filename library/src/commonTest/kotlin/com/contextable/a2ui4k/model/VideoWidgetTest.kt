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

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for Video widget JSON parsing.
 *
 * A2UI Spec properties (v0.8):
 * - url (required): URL of the video source
 */
class VideoWidgetTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parseString extracts url literalString`() {
        val jsonStr = """
            {
                "url": {"literalString": "https://example.com/video.mp4"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val urlRef = DataReferenceParser.parseString(data["url"])

        assertNotNull(urlRef)
        assertEquals("https://example.com/video.mp4", (urlRef as LiteralString).value)
    }

    @Test
    fun `parseString extracts url path binding`() {
        val jsonStr = """
            {
                "url": {"path": "/media/videoUrl"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val urlRef = DataReferenceParser.parseString(data["url"])

        assertNotNull(urlRef)
        assertEquals("/media/videoUrl", (urlRef as PathString).path)
    }

    @Test
    fun `video url with query parameters`() {
        val jsonStr = """
            {
                "url": {"literalString": "https://cdn.example.com/video.mp4?token=abc123&quality=hd"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val urlRef = DataReferenceParser.parseString(data["url"])

        assertNotNull(urlRef)
        assertEquals("https://cdn.example.com/video.mp4?token=abc123&quality=hd", (urlRef as LiteralString).value)
    }

    @Test
    fun `video url with various file extensions`() {
        val extensions = listOf("mp4", "webm", "mov", "avi", "m3u8")

        extensions.forEach { ext ->
            val jsonStr = """{"url": {"literalString": "https://example.com/video.$ext"}}"""
            val data = json.decodeFromString<JsonObject>(jsonStr)
            val urlRef = DataReferenceParser.parseString(data["url"])

            assertNotNull(urlRef, "Video URL with .$ext should parse")
            assertEquals("https://example.com/video.$ext", (urlRef as LiteralString).value)
        }
    }

    @Test
    fun `missing url returns null`() {
        val jsonStr = """{}"""
        val data = json.decodeFromString<JsonObject>(jsonStr)

        assertNull(DataReferenceParser.parseString(data["url"]))
    }

    @Test
    fun `youtube embed url`() {
        val jsonStr = """
            {
                "url": {"literalString": "https://www.youtube.com/embed/dQw4w9WgXcQ"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val urlRef = DataReferenceParser.parseString(data["url"])

        assertNotNull(urlRef)
        assertEquals("https://www.youtube.com/embed/dQw4w9WgXcQ", (urlRef as LiteralString).value)
    }
}
