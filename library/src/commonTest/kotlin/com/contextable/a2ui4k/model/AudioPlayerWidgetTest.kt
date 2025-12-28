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
 * Tests for AudioPlayer widget JSON parsing.
 *
 * A2UI Spec properties (v0.8):
 * - url (optional): URL of the audio source
 * - description (optional): Accessibility description / title
 */
class AudioPlayerWidgetTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parseString extracts url literalString`() {
        val jsonStr = """
            {
                "url": {"literalString": "https://example.com/audio.mp3"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val urlRef = DataReferenceParser.parseString(data["url"])

        assertNotNull(urlRef)
        assertEquals("https://example.com/audio.mp3", (urlRef as LiteralString).value)
    }

    @Test
    fun `parseString extracts url path binding`() {
        val jsonStr = """
            {
                "url": {"path": "/podcast/audioUrl"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val urlRef = DataReferenceParser.parseString(data["url"])

        assertNotNull(urlRef)
        assertEquals("/podcast/audioUrl", (urlRef as PathString).path)
    }

    @Test
    fun `parseString extracts description literalString`() {
        val jsonStr = """
            {
                "url": {"literalString": "https://example.com/episode.mp3"},
                "description": {"literalString": "Episode 42: Introduction to A2UI"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val descRef = DataReferenceParser.parseString(data["description"])

        assertNotNull(descRef)
        assertEquals("Episode 42: Introduction to A2UI", (descRef as LiteralString).value)
    }

    @Test
    fun `parseString extracts description path binding`() {
        val jsonStr = """
            {
                "url": {"path": "/audio/url"},
                "description": {"path": "/audio/title"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val descRef = DataReferenceParser.parseString(data["description"])

        assertNotNull(descRef)
        assertEquals("/audio/title", (descRef as PathString).path)
    }

    @Test
    fun `complete AudioPlayer with url and description`() {
        val jsonStr = """
            {
                "url": {"literalString": "https://cdn.example.com/podcast/ep1.mp3"},
                "description": {"literalString": "Welcome to Our Podcast"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val urlRef = DataReferenceParser.parseString(data["url"])
        assertEquals("https://cdn.example.com/podcast/ep1.mp3", (urlRef as LiteralString).value)

        val descRef = DataReferenceParser.parseString(data["description"])
        assertEquals("Welcome to Our Podcast", (descRef as LiteralString).value)
    }

    @Test
    fun `missing optional properties return null`() {
        val jsonStr = """{}"""
        val data = json.decodeFromString<JsonObject>(jsonStr)

        assertNull(DataReferenceParser.parseString(data["url"]))
        assertNull(DataReferenceParser.parseString(data["description"]))
    }

    @Test
    fun `audio url with various file extensions`() {
        val extensions = listOf("mp3", "wav", "ogg", "m4a", "aac", "flac")

        extensions.forEach { ext ->
            val jsonStr = """{"url": {"literalString": "https://example.com/audio.$ext"}}"""
            val data = json.decodeFromString<JsonObject>(jsonStr)
            val urlRef = DataReferenceParser.parseString(data["url"])

            assertNotNull(urlRef, "Audio URL with .$ext should parse")
            assertEquals("https://example.com/audio.$ext", (urlRef as LiteralString).value)
        }
    }

    @Test
    fun `AudioPlayer with only description`() {
        val jsonStr = """
            {
                "description": {"literalString": "Background Music"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        assertNull(DataReferenceParser.parseString(data["url"]))

        val descRef = DataReferenceParser.parseString(data["description"])
        assertNotNull(descRef)
        assertEquals("Background Music", (descRef as LiteralString).value)
    }
}
