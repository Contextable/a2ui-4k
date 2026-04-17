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

package com.contextable.a2ui4k.function

import com.contextable.a2ui4k.model.DataContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [FunctionEvaluator] — v0.9 standard functions. All argument
 * names come straight from the v0.9 basic catalog.
 */
class FunctionEvaluatorTest {

    private val emptyContext = object : DataContext {
        override fun getString(path: String): String? = null
        override fun getNumber(path: String): Double? = null
        override fun getBoolean(path: String): Boolean? = null
        override fun getStringList(path: String): List<String>? = null
        override fun getArraySize(path: String): Int? = null
        override fun getObjectKeys(path: String): List<String>? = null
        override fun update(path: String, value: Any?) {}
        override fun withBasePath(basePath: String): DataContext = this
    }

    private fun numArgs(
        value: Double,
        decimals: Int? = null,
        grouping: Boolean? = null
    ): JsonObject {
        val map = mutableMapOf<String, JsonPrimitive>("value" to JsonPrimitive(value))
        decimals?.let { map["decimals"] = JsonPrimitive(it) }
        grouping?.let { map["grouping"] = JsonPrimitive(it) }
        return JsonObject(map)
    }

    // --- formatNumber ---

    @Test
    fun formatNumber_integer() {
        assertEquals(
            "42",
            FunctionEvaluator.evaluateString("formatNumber", numArgs(42.0, decimals = 0), emptyContext)
        )
    }

    @Test
    fun formatNumber_withDecimals() {
        assertEquals(
            "1,234.56",
            FunctionEvaluator.evaluateString("formatNumber", numArgs(1234.56, decimals = 2), emptyContext)
        )
    }

    @Test
    fun formatNumber_largeNumber() {
        assertEquals(
            "1,000,000",
            FunctionEvaluator.evaluateString("formatNumber", numArgs(1000000.0, decimals = 0), emptyContext)
        )
    }

    @Test
    fun formatNumber_noGrouping() {
        assertEquals(
            "1234.5",
            FunctionEvaluator.evaluateString("formatNumber", numArgs(1234.5, decimals = 1, grouping = false), emptyContext)
        )
    }

    @Test
    fun formatNumber_decimalsPads() {
        assertEquals(
            "5.00",
            FunctionEvaluator.evaluateString("formatNumber", numArgs(5.0, decimals = 2), emptyContext)
        )
    }

    @Test
    fun formatNumber_smallDecimal() {
        assertEquals(
            "0.001",
            FunctionEvaluator.evaluateString("formatNumber", numArgs(0.001, decimals = 3), emptyContext)
        )
    }

    @Test
    fun formatNumber_negativeNumber() {
        assertEquals(
            "-1,234.5",
            FunctionEvaluator.evaluateString("formatNumber", numArgs(-1234.5, decimals = 1), emptyContext)
        )
    }

    // --- formatCurrency ---

    @Test
    fun formatCurrency_usdDefaults() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive(1234.56),
            "currency" to JsonPrimitive("USD")
        ))
        assertEquals("$1,234.56", FunctionEvaluator.evaluateString("formatCurrency", args, emptyContext))
    }

    @Test
    fun formatCurrency_eur() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive(99.99),
            "currency" to JsonPrimitive("EUR")
        ))
        assertEquals("\u20AC99.99", FunctionEvaluator.evaluateString("formatCurrency", args, emptyContext))
    }

    @Test
    fun formatCurrency_gbp() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive(50.0),
            "currency" to JsonPrimitive("GBP")
        ))
        assertEquals("\u00A350.00", FunctionEvaluator.evaluateString("formatCurrency", args, emptyContext))
    }

    @Test
    fun formatCurrency_jpy() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive(1000.0),
            "currency" to JsonPrimitive("JPY")
        ))
        assertEquals("\u00A51,000.00", FunctionEvaluator.evaluateString("formatCurrency", args, emptyContext))
    }

    @Test
    fun formatCurrency_unknownCurrencyUsesCode() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive(42.0),
            "currency" to JsonPrimitive("CHF")
        ))
        assertEquals("CHF42.00", FunctionEvaluator.evaluateString("formatCurrency", args, emptyContext))
    }

    @Test
    fun formatCurrency_defaultsToUsd() {
        val args = JsonObject(mapOf("value" to JsonPrimitive(10.0)))
        assertEquals("$10.00", FunctionEvaluator.evaluateString("formatCurrency", args, emptyContext))
    }

    @Test
    fun formatCurrency_customDecimals() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive(10.0),
            "currency" to JsonPrimitive("USD"),
            "decimals" to JsonPrimitive(0)
        ))
        assertEquals("$10", FunctionEvaluator.evaluateString("formatCurrency", args, emptyContext))
    }

    @Test
    fun formatCurrency_noGrouping() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive(1234567.0),
            "currency" to JsonPrimitive("USD"),
            "grouping" to JsonPrimitive(false),
            "decimals" to JsonPrimitive(0)
        ))
        assertEquals("$1234567", FunctionEvaluator.evaluateString("formatCurrency", args, emptyContext))
    }

    // --- required ---

    @Test
    fun required_emptyString_isFalse() {
        val args = JsonObject(mapOf("value" to JsonPrimitive("")))
        assertEquals(false, FunctionEvaluator.evaluateBoolean("required", args, emptyContext))
    }

    @Test
    fun required_nonEmpty_isTrue() {
        val args = JsonObject(mapOf("value" to JsonPrimitive("hello")))
        assertEquals(true, FunctionEvaluator.evaluateBoolean("required", args, emptyContext))
    }

    @Test
    fun required_nullArgsReturnsFalse() {
        assertEquals(false, FunctionEvaluator.evaluateBoolean("required", null, emptyContext))
    }

    @Test
    fun required_blankStringIsFalse() {
        val args = JsonObject(mapOf("value" to JsonPrimitive("   ")))
        assertEquals(false, FunctionEvaluator.evaluateBoolean("required", args, emptyContext))
    }

    @Test
    fun required_numberIsTrue() {
        val args = JsonObject(mapOf("value" to JsonPrimitive(42)))
        assertEquals(true, FunctionEvaluator.evaluateBoolean("required", args, emptyContext))
    }

    // --- email ---

    @Test
    fun email_valid() {
        val args = JsonObject(mapOf("value" to JsonPrimitive("test@example.com")))
        assertEquals(true, FunctionEvaluator.evaluateBoolean("email", args, emptyContext))
    }

    @Test
    fun email_invalid() {
        val args = JsonObject(mapOf("value" to JsonPrimitive("not-an-email")))
        assertEquals(false, FunctionEvaluator.evaluateBoolean("email", args, emptyContext))
    }

    // --- regex ---

    @Test
    fun regex_matchesPattern() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive("ABC123"),
            "pattern" to JsonPrimitive("^[A-Z]+[0-9]+$")
        ))
        assertEquals(true, FunctionEvaluator.evaluateBoolean("regex", args, emptyContext))
    }

    @Test
    fun regex_doesNotMatchPattern() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive("abc"),
            "pattern" to JsonPrimitive("^[0-9]+$")
        ))
        assertEquals(false, FunctionEvaluator.evaluateBoolean("regex", args, emptyContext))
    }

    @Test
    fun regex_invalidPatternReturnsFalse() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive("test"),
            "pattern" to JsonPrimitive("[invalid(")
        ))
        assertEquals(false, FunctionEvaluator.evaluateBoolean("regex", args, emptyContext))
    }

    @Test
    fun regex_missingValueReturnsFalse() {
        val args = JsonObject(mapOf("pattern" to JsonPrimitive(".*")))
        assertEquals(false, FunctionEvaluator.evaluateBoolean("regex", args, emptyContext))
    }

    // --- length ---

    @Test
    fun length_withinRange() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive("hello"),
            "min" to JsonPrimitive(3),
            "max" to JsonPrimitive(10)
        ))
        assertEquals(true, FunctionEvaluator.evaluateBoolean("length", args, emptyContext))
    }

    @Test
    fun length_tooShort() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive("hi"),
            "min" to JsonPrimitive(3)
        ))
        assertEquals(false, FunctionEvaluator.evaluateBoolean("length", args, emptyContext))
    }

    @Test
    fun length_tooLong() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive("this is too long"),
            "max" to JsonPrimitive(5)
        ))
        assertEquals(false, FunctionEvaluator.evaluateBoolean("length", args, emptyContext))
    }

    @Test
    fun length_exactMatch() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive("abc"),
            "min" to JsonPrimitive(3),
            "max" to JsonPrimitive(3)
        ))
        assertEquals(true, FunctionEvaluator.evaluateBoolean("length", args, emptyContext))
    }

    @Test
    fun length_noConstraintsAlwaysTrue() {
        val args = JsonObject(mapOf("value" to JsonPrimitive("anything")))
        assertEquals(true, FunctionEvaluator.evaluateBoolean("length", args, emptyContext))
    }

    // --- numeric ---

    @Test
    fun numeric_withinRange() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive(5.0),
            "min" to JsonPrimitive(1.0),
            "max" to JsonPrimitive(10.0)
        ))
        assertEquals(true, FunctionEvaluator.evaluateBoolean("numeric", args, emptyContext))
    }

    @Test
    fun numeric_belowMin() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive(0.5),
            "min" to JsonPrimitive(1.0)
        ))
        assertEquals(false, FunctionEvaluator.evaluateBoolean("numeric", args, emptyContext))
    }

    @Test
    fun numeric_aboveMax() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive(100.0),
            "max" to JsonPrimitive(50.0)
        ))
        assertEquals(false, FunctionEvaluator.evaluateBoolean("numeric", args, emptyContext))
    }

    @Test
    fun numeric_missingValueReturnsFalse() {
        val args = JsonObject(mapOf("min" to JsonPrimitive(0.0)))
        assertEquals(false, FunctionEvaluator.evaluateBoolean("numeric", args, emptyContext))
    }

    // --- and ---

    @Test
    fun and_allTrueReturnsTrue() {
        val args = JsonObject(mapOf(
            "conditions" to JsonArray(listOf(JsonPrimitive(true), JsonPrimitive(true), JsonPrimitive(true)))
        ))
        assertEquals(true, FunctionEvaluator.evaluateBoolean("and", args, emptyContext))
    }

    @Test
    fun and_oneFalseReturnsFalse() {
        val args = JsonObject(mapOf(
            "conditions" to JsonArray(listOf(JsonPrimitive(true), JsonPrimitive(false), JsonPrimitive(true)))
        ))
        assertEquals(false, FunctionEvaluator.evaluateBoolean("and", args, emptyContext))
    }

    @Test
    fun and_emptyConditionsReturnsTrue() {
        val args = JsonObject(mapOf("conditions" to JsonArray(emptyList())))
        assertEquals(true, FunctionEvaluator.evaluateBoolean("and", args, emptyContext))
    }

    @Test
    fun and_withNestedFunctionCall() {
        val args = JsonObject(mapOf(
            "conditions" to JsonArray(listOf(
                JsonPrimitive(true),
                JsonObject(mapOf(
                    "call" to JsonPrimitive("required"),
                    "args" to JsonObject(mapOf("value" to JsonPrimitive("present")))
                ))
            ))
        ))
        assertEquals(true, FunctionEvaluator.evaluateBoolean("and", args, emptyContext))
    }

    @Test
    fun and_noConditionsKeyReturnsFalse() {
        assertEquals(false, FunctionEvaluator.evaluateBoolean("and", JsonObject(emptyMap()), emptyContext))
    }

    // --- or ---

    @Test
    fun or_oneTrueReturnsTrue() {
        val args = JsonObject(mapOf(
            "conditions" to JsonArray(listOf(JsonPrimitive(false), JsonPrimitive(true), JsonPrimitive(false)))
        ))
        assertEquals(true, FunctionEvaluator.evaluateBoolean("or", args, emptyContext))
    }

    @Test
    fun or_allFalseReturnsFalse() {
        val args = JsonObject(mapOf(
            "conditions" to JsonArray(listOf(JsonPrimitive(false), JsonPrimitive(false)))
        ))
        assertEquals(false, FunctionEvaluator.evaluateBoolean("or", args, emptyContext))
    }

    @Test
    fun or_emptyConditionsReturnsFalse() {
        val args = JsonObject(mapOf("conditions" to JsonArray(emptyList())))
        assertEquals(false, FunctionEvaluator.evaluateBoolean("or", args, emptyContext))
    }

    @Test
    fun or_withNestedFunctionCall() {
        val args = JsonObject(mapOf(
            "conditions" to JsonArray(listOf(
                JsonPrimitive(false),
                JsonObject(mapOf(
                    "call" to JsonPrimitive("email"),
                    "args" to JsonObject(mapOf("value" to JsonPrimitive("user@test.com")))
                ))
            ))
        ))
        assertEquals(true, FunctionEvaluator.evaluateBoolean("or", args, emptyContext))
    }

    // --- not ---

    @Test
    fun not_trueReturnsFalse() {
        val args = JsonObject(mapOf("value" to JsonPrimitive(true)))
        assertEquals(false, FunctionEvaluator.evaluateBoolean("not", args, emptyContext))
    }

    @Test
    fun not_falseReturnsTrue() {
        val args = JsonObject(mapOf("value" to JsonPrimitive(false)))
        assertEquals(true, FunctionEvaluator.evaluateBoolean("not", args, emptyContext))
    }

    @Test
    fun not_withNestedFunctionCall() {
        val args = JsonObject(mapOf(
            "value" to JsonObject(mapOf(
                "call" to JsonPrimitive("required"),
                "args" to JsonObject(mapOf("value" to JsonPrimitive("")))
            ))
        ))
        // required("") = false, not(false) = true
        assertEquals(true, FunctionEvaluator.evaluateBoolean("not", args, emptyContext))
    }

    @Test
    fun not_missingValueReturnsTrue() {
        // Missing value → defaults to false → not(false) = true
        assertEquals(true, FunctionEvaluator.evaluateBoolean("not", JsonObject(emptyMap()), emptyContext))
    }

    // --- formatString ---

    @Test
    fun formatString_simpleTemplate() {
        val dataContext = object : DataContext {
            override fun getString(path: String): String? = when (path) {
                "/user/name" -> "Alice"
                else -> null
            }
            override fun getNumber(path: String): Double? = null
            override fun getBoolean(path: String): Boolean? = null
            override fun getStringList(path: String): List<String>? = null
            override fun getArraySize(path: String): Int? = null
            override fun getObjectKeys(path: String): List<String>? = null
            override fun update(path: String, value: Any?) {}
            override fun withBasePath(basePath: String): DataContext = this
        }

        val args = JsonObject(mapOf("value" to JsonPrimitive("Hello, \${/user/name}!")))
        assertEquals("Hello, Alice!", FunctionEvaluator.evaluateString("formatString", args, dataContext))
    }

    @Test
    fun formatString_missingPathReplacesWithEmpty() {
        val args = JsonObject(mapOf("value" to JsonPrimitive("Value: \${/missing}")))
        assertEquals("Value: ", FunctionEvaluator.evaluateString("formatString", args, emptyContext))
    }

    @Test
    fun formatString_noPlaceholders() {
        val args = JsonObject(mapOf("value" to JsonPrimitive("No placeholders here")))
        assertEquals("No placeholders here", FunctionEvaluator.evaluateString("formatString", args, emptyContext))
    }

    @Test
    fun formatString_missingValueReturnsEmpty() {
        assertEquals("", FunctionEvaluator.evaluateString("formatString", JsonObject(emptyMap()), emptyContext))
    }

    // --- formatDate ---

    @Test
    fun formatDate_passesThroughWhenNoFormat() {
        val args = JsonObject(mapOf("value" to JsonPrimitive("2026-01-15")))
        assertEquals("2026-01-15", FunctionEvaluator.evaluateString("formatDate", args, emptyContext))
    }

    @Test
    fun formatDate_acceptsFormatAndLocale() {
        // Platform date formatting isn't implemented; ensure args are accepted without error.
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive("2026-01-15"),
            "format" to JsonPrimitive("yyyy-MM-dd"),
            "locale" to JsonPrimitive("en-US")
        ))
        assertEquals("2026-01-15", FunctionEvaluator.evaluateString("formatDate", args, emptyContext))
    }

    @Test
    fun formatDate_missingValueReturnsEmpty() {
        assertEquals("", FunctionEvaluator.evaluateString("formatDate", JsonObject(emptyMap()), emptyContext))
    }

    // --- pluralize ---

    @Test
    fun pluralize_zeroUsesZeroForm() {
        val args = JsonObject(mapOf(
            "count" to JsonPrimitive(0),
            "zero" to JsonPrimitive("no items"),
            "one" to JsonPrimitive("1 item"),
            "other" to JsonPrimitive("items")
        ))
        assertEquals("no items", FunctionEvaluator.evaluateString("pluralize", args, emptyContext))
    }

    @Test
    fun pluralize_oneUsesOneForm() {
        val args = JsonObject(mapOf(
            "count" to JsonPrimitive(1),
            "zero" to JsonPrimitive("no items"),
            "one" to JsonPrimitive("1 item"),
            "other" to JsonPrimitive("items")
        ))
        assertEquals("1 item", FunctionEvaluator.evaluateString("pluralize", args, emptyContext))
    }

    @Test
    fun pluralize_multipleUsesOtherForm() {
        val args = JsonObject(mapOf(
            "count" to JsonPrimitive(5),
            "zero" to JsonPrimitive("no items"),
            "one" to JsonPrimitive("1 item"),
            "other" to JsonPrimitive("items")
        ))
        assertEquals("items", FunctionEvaluator.evaluateString("pluralize", args, emptyContext))
    }

    @Test
    fun pluralize_zeroWithoutZeroFormUsesOther() {
        val args = JsonObject(mapOf(
            "count" to JsonPrimitive(0),
            "one" to JsonPrimitive("1 item"),
            "other" to JsonPrimitive("items")
        ))
        assertEquals("items", FunctionEvaluator.evaluateString("pluralize", args, emptyContext))
    }

    @Test
    fun pluralize_oneWithoutOneFormUsesOther() {
        val args = JsonObject(mapOf(
            "count" to JsonPrimitive(1),
            "other" to JsonPrimitive("items")
        ))
        assertEquals("items", FunctionEvaluator.evaluateString("pluralize", args, emptyContext))
    }

    // --- openUrl ---

    @Test
    fun openUrl_returnsNull() {
        val args = JsonObject(mapOf(
            "url" to JsonPrimitive("https://example.com"),
            "target" to JsonPrimitive("_blank")
        ))
        // openUrl is an action, not a value-returning function
        assertNull(FunctionEvaluator.evaluate("openUrl", args, emptyContext))
    }

    // --- unknown function ---

    @Test
    fun unknownFunction_returnsNull() {
        assertNull(FunctionEvaluator.evaluate("nonexistent", null, emptyContext))
    }

    // --- arg resolution ---

    @Test
    fun resolveArg_resolvesPathReference() {
        val dataContext = object : DataContext {
            override fun getString(path: String): String? = when (path) {
                "/email" -> "user@test.com"
                else -> null
            }
            override fun getNumber(path: String): Double? = null
            override fun getBoolean(path: String): Boolean? = null
            override fun getStringList(path: String): List<String>? = null
            override fun getArraySize(path: String): Int? = null
            override fun getObjectKeys(path: String): List<String>? = null
            override fun update(path: String, value: Any?) {}
            override fun withBasePath(basePath: String): DataContext = this
        }

        val args = JsonObject(mapOf("value" to JsonObject(mapOf("path" to JsonPrimitive("/email")))))
        assertEquals(true, FunctionEvaluator.evaluateBoolean("email", args, dataContext))
    }

    @Test
    fun resolveArg_resolvesNestedFunctionCall() {
        // `regex` value is itself a `formatString` call that builds the target string.
        val args = JsonObject(mapOf(
            "value" to JsonObject(mapOf(
                "call" to JsonPrimitive("formatString"),
                "args" to JsonObject(mapOf("value" to JsonPrimitive("ABC123")))
            )),
            "pattern" to JsonPrimitive("^[A-Z]+[0-9]+$")
        ))
        assertEquals(true, FunctionEvaluator.evaluateBoolean("regex", args, emptyContext))
    }
}
