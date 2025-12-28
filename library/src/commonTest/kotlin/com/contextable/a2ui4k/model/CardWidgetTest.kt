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

/**
 * Tests for CardWidget JSON parsing.
 *
 * The Card widget in A2UI:
 * - Takes a `child` property referencing another component
 * - Applies internal padding (8dp) around the child (matching A2UI protocol)
 * - Uses surface color from the theme
 */
class CardWidgetTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parseComponentRef extracts child component reference`() {
        val jsonStr = """
            {
                "child": "card-content"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val childRef = DataReferenceParser.parseComponentRef(data["child"])

        assertNotNull(childRef)
        assertEquals("card-content", childRef.componentId)
    }

    @Test
    fun `card with nested layout structure`() {
        // This matches the restaurant app structure:
        // Card -> Row -> [Image, Column]
        val jsonStr = """
            {
                "child": "card-layout"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val childRef = DataReferenceParser.parseComponentRef(data["child"])

        assertNotNull(childRef)
        assertEquals("card-layout", childRef.componentId)
    }
}
