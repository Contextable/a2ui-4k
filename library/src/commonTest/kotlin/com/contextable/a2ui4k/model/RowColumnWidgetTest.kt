package com.contextable.a2ui4k.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests for Row and Column widget property parsing.
 *
 * A2UI Spec supported values:
 * - Row distribution: "start", "center", "end", "spaceBetween", "spaceAround", "spaceEvenly", "stretch"
 * - Row alignment: "start", "center", "end", "stretch"
 * - Column distribution: "start", "center", "end", "spaceBetween", "spaceAround", "spaceEvenly", "stretch"
 * - Column alignment: "start", "center", "end", "stretch"
 */
class RowColumnWidgetTest {

    private val json = Json { ignoreUnknownKeys = true }

    // Tests for children parsing
    @Test
    fun `parseComponentArray extracts explicit list of children`() {
        val jsonStr = """
            {
                "children": {"explicitList": ["child1", "child2", "child3"]}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val childrenRef = DataReferenceParser.parseComponentArray(data["children"])

        assertNotNull(childrenRef)
        assertEquals(listOf("child1", "child2", "child3"), childrenRef.componentIds)
    }

    // Tests for distribution parsing
    @Test
    fun `parseString extracts distribution value`() {
        val distributions = listOf(
            "start", "center", "end",
            "spaceBetween", "spaceAround", "spaceEvenly",
            "stretch"
        )

        distributions.forEach { dist ->
            val jsonStr = """{"distribution": "$dist"}"""
            val data = json.decodeFromString<JsonObject>(jsonStr)
            val ref = DataReferenceParser.parseString(data["distribution"])

            assertNotNull(ref, "Distribution '$dist' should parse")
            assertEquals(dist, (ref as LiteralString).value)
        }
    }

    // Tests for alignment parsing
    @Test
    fun `parseString extracts alignment value`() {
        val alignments = listOf("start", "center", "end", "stretch")

        alignments.forEach { align ->
            val jsonStr = """{"alignment": "$align"}"""
            val data = json.decodeFromString<JsonObject>(jsonStr)
            val ref = DataReferenceParser.parseString(data["alignment"])

            assertNotNull(ref, "Alignment '$align' should parse")
            assertEquals(align, (ref as LiteralString).value)
        }
    }

    // Tests for Row with all properties
    @Test
    fun `Row with distribution and alignment`() {
        val jsonStr = """
            {
                "children": {"explicitList": ["left", "center", "right"]},
                "distribution": "spaceBetween",
                "alignment": "center"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val childrenRef = DataReferenceParser.parseComponentArray(data["children"])
        assertNotNull(childrenRef)
        assertEquals(3, childrenRef.componentIds.size)

        val distRef = DataReferenceParser.parseString(data["distribution"])
        assertEquals("spaceBetween", (distRef as LiteralString).value)

        val alignRef = DataReferenceParser.parseString(data["alignment"])
        assertEquals("center", (alignRef as LiteralString).value)
    }

    // Tests for Column with all properties
    @Test
    fun `Column with distribution and alignment`() {
        val jsonStr = """
            {
                "children": {"explicitList": ["header", "content", "footer"]},
                "distribution": "spaceEvenly",
                "alignment": "center"
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)

        val childrenRef = DataReferenceParser.parseComponentArray(data["children"])
        assertNotNull(childrenRef)
        assertEquals(3, childrenRef.componentIds.size)

        val distRef = DataReferenceParser.parseString(data["distribution"])
        assertEquals("spaceEvenly", (distRef as LiteralString).value)

        val alignRef = DataReferenceParser.parseString(data["alignment"])
        assertEquals("center", (alignRef as LiteralString).value)
    }

    // Test path-based values for dynamic binding
    @Test
    fun `distribution from path binding`() {
        val jsonStr = """
            {
                "distribution": {"path": "/ui/rowDistribution"}
            }
        """.trimIndent()

        val data = json.decodeFromString<JsonObject>(jsonStr)
        val ref = DataReferenceParser.parseString(data["distribution"])

        assertNotNull(ref)
        assertEquals("/ui/rowDistribution", (ref as PathString).path)
    }
}
