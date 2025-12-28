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

package com.contextable.a2ui4k.util

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for BasicMarkdown utilities.
 *
 * Tests the stripMarkdown function which removes markdown formatting.
 * Note: parseBasicMarkdown returns AnnotatedString which requires Compose runtime,
 * so we focus on testing stripMarkdown in commonTest.
 */
class BasicMarkdownTest {

    // ========== stripMarkdown tests ==========

    @Test
    fun `stripMarkdown removes bold with asterisks`() {
        val input = "This is **bold** text"
        val result = stripMarkdown(input)

        assertEquals("This is bold text", result)
    }

    @Test
    fun `stripMarkdown removes bold with underscores`() {
        val input = "This is __bold__ text"
        val result = stripMarkdown(input)

        assertEquals("This is bold text", result)
    }

    @Test
    fun `stripMarkdown removes italic with asterisk`() {
        val input = "This is *italic* text"
        val result = stripMarkdown(input)

        assertEquals("This is italic text", result)
    }

    @Test
    fun `stripMarkdown removes italic with underscore`() {
        val input = "This is _italic_ text"
        val result = stripMarkdown(input)

        assertEquals("This is italic text", result)
    }

    @Test
    fun `stripMarkdown removes links keeping text`() {
        val input = "Click [here](https://example.com) for more"
        val result = stripMarkdown(input)

        assertEquals("Click here for more", result)
    }

    @Test
    fun `stripMarkdown removes h1 header prefix`() {
        val input = "# Header One"
        val result = stripMarkdown(input)

        assertEquals("Header One", result)
    }

    @Test
    fun `stripMarkdown removes h2 header prefix`() {
        val input = "## Header Two"
        val result = stripMarkdown(input)

        assertEquals("Header Two", result)
    }

    @Test
    fun `stripMarkdown removes h3 header prefix`() {
        val input = "### Header Three"
        val result = stripMarkdown(input)

        assertEquals("Header Three", result)
    }

    @Test
    fun `stripMarkdown handles plain text unchanged`() {
        val input = "Just plain text with no formatting"
        val result = stripMarkdown(input)

        assertEquals("Just plain text with no formatting", result)
    }

    @Test
    fun `stripMarkdown handles empty string`() {
        val result = stripMarkdown("")
        assertEquals("", result)
    }

    @Test
    fun `stripMarkdown handles multiple bold sections`() {
        val input = "**First** and **second** bold"
        val result = stripMarkdown(input)

        assertEquals("First and second bold", result)
    }

    @Test
    fun `stripMarkdown handles mixed formatting`() {
        val input = "**Bold** and *italic* and [link](url)"
        val result = stripMarkdown(input)

        assertEquals("Bold and italic and link", result)
    }

    @Test
    fun `stripMarkdown handles bold and italic together`() {
        val input = "This has ***bold italic*** text"
        val result = stripMarkdown(input)

        // After removing **, we get *bold italic*, then removing * gives us the text
        assertEquals("This has bold italic text", result)
    }

    @Test
    fun `stripMarkdown handles nested link in bold`() {
        val input = "Check **[this link](https://example.com)** out"
        val result = stripMarkdown(input)

        assertEquals("Check this link out", result)
    }

    @Test
    fun `stripMarkdown handles multiple links`() {
        val input = "[Link 1](url1) and [Link 2](url2)"
        val result = stripMarkdown(input)

        assertEquals("Link 1 and Link 2", result)
    }

    @Test
    fun `stripMarkdown handles URL with special characters`() {
        val input = "[Click](https://example.com/path?param=value&other=123)"
        val result = stripMarkdown(input)

        assertEquals("Click", result)
    }

    @Test
    fun `stripMarkdown preserves non-markdown special characters`() {
        val input = "Price: \$100 & more (50% off)"
        val result = stripMarkdown(input)

        assertEquals("Price: \$100 & more (50% off)", result)
    }

    @Test
    fun `stripMarkdown handles unclosed formatting gracefully`() {
        val input = "This is **unclosed bold"
        val result = stripMarkdown(input)

        // Unclosed formatting should remain as-is
        assertEquals("This is **unclosed bold", result)
    }

    @Test
    fun `stripMarkdown handles h6 header prefix`() {
        val input = "###### Header Six"
        val result = stripMarkdown(input)

        assertEquals("Header Six", result)
    }
}
