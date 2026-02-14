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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [FunctionEvaluator], focusing on formatting functions
 * that must compile and run correctly on all Kotlin targets (JVM, JS, iOS).
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

    private fun formatNumberArgs(
        value: Double,
        minimumFractionDigits: Int? = null,
        maximumFractionDigits: Int? = null,
        useGrouping: Boolean? = null
    ): JsonObject {
        val map = mutableMapOf<String, JsonPrimitive>("value" to JsonPrimitive(value))
        minimumFractionDigits?.let { map["minimumFractionDigits"] = JsonPrimitive(it) }
        maximumFractionDigits?.let { map["maximumFractionDigits"] = JsonPrimitive(it) }
        useGrouping?.let { map["useGrouping"] = JsonPrimitive(it) }
        return JsonObject(map)
    }

    // --- formatNumber tests ---

    @Test
    fun formatNumber_integer() {
        val result = FunctionEvaluator.evaluateString(
            "formatNumber",
            formatNumberArgs(value = 42.0, maximumFractionDigits = 0),
            emptyContext
        )
        assertEquals("42", result)
    }

    @Test
    fun formatNumber_withDecimals() {
        val result = FunctionEvaluator.evaluateString(
            "formatNumber",
            formatNumberArgs(value = 1234.56, minimumFractionDigits = 2, maximumFractionDigits = 2),
            emptyContext
        )
        assertEquals("1,234.56", result)
    }

    @Test
    fun formatNumber_largeNumber() {
        val result = FunctionEvaluator.evaluateString(
            "formatNumber",
            formatNumberArgs(value = 1000000.0, maximumFractionDigits = 0),
            emptyContext
        )
        assertEquals("1,000,000", result)
    }

    @Test
    fun formatNumber_noGrouping() {
        val result = FunctionEvaluator.evaluateString(
            "formatNumber",
            formatNumberArgs(value = 1234.5, useGrouping = false, maximumFractionDigits = 1),
            emptyContext
        )
        assertEquals("1234.5", result)
    }

    @Test
    fun formatNumber_minimumFractionDigitsPads() {
        val result = FunctionEvaluator.evaluateString(
            "formatNumber",
            formatNumberArgs(value = 5.0, minimumFractionDigits = 2, maximumFractionDigits = 2),
            emptyContext
        )
        assertEquals("5.00", result)
    }

    @Test
    fun formatNumber_smallDecimal() {
        // Regression test: small decimals must not produce scientific notation
        val result = FunctionEvaluator.evaluateString(
            "formatNumber",
            formatNumberArgs(value = 0.001, minimumFractionDigits = 3, maximumFractionDigits = 3),
            emptyContext
        )
        assertEquals("0.001", result)
    }

    @Test
    fun formatNumber_negativeNumber() {
        val result = FunctionEvaluator.evaluateString(
            "formatNumber",
            formatNumberArgs(value = -1234.5, maximumFractionDigits = 1),
            emptyContext
        )
        assertEquals("-1,234.5", result)
    }

    // --- formatCurrency tests ---

    @Test
    fun formatCurrency_usd() {
        val args = JsonObject(
            mapOf(
                "value" to JsonPrimitive(1234.56),
                "currency" to JsonPrimitive("USD")
            )
        )
        val result = FunctionEvaluator.evaluateString("formatCurrency", args, emptyContext)
        assertEquals("$1,234.56", result)
    }

    // --- required validation tests ---

    @Test
    fun required_emptyString_isFalse() {
        val args = JsonObject(mapOf("value" to JsonPrimitive("")))
        val result = FunctionEvaluator.evaluateBoolean("required", args, emptyContext)
        assertEquals(false, result)
    }

    @Test
    fun required_nonEmpty_isTrue() {
        val args = JsonObject(mapOf("value" to JsonPrimitive("hello")))
        val result = FunctionEvaluator.evaluateBoolean("required", args, emptyContext)
        assertEquals(true, result)
    }

    // --- email validation tests ---

    @Test
    fun email_valid() {
        val args = JsonObject(mapOf("value" to JsonPrimitive("test@example.com")))
        val result = FunctionEvaluator.evaluateBoolean("email", args, emptyContext)
        assertEquals(true, result)
    }

    @Test
    fun email_invalid() {
        val args = JsonObject(mapOf("value" to JsonPrimitive("not-an-email")))
        val result = FunctionEvaluator.evaluateBoolean("email", args, emptyContext)
        assertEquals(false, result)
    }

    // --- regex validation tests ---

    @Test
    fun regex_matchesPattern() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive("ABC123"),
            "pattern" to JsonPrimitive("^[A-Z]+[0-9]+$")
        ))
        val result = FunctionEvaluator.evaluateBoolean("regex", args, emptyContext)
        assertEquals(true, result)
    }

    @Test
    fun regex_doesNotMatchPattern() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive("abc"),
            "pattern" to JsonPrimitive("^[0-9]+$")
        ))
        val result = FunctionEvaluator.evaluateBoolean("regex", args, emptyContext)
        assertEquals(false, result)
    }

    @Test
    fun regex_invalidPatternReturnsFalse() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive("test"),
            "pattern" to JsonPrimitive("[invalid(")
        ))
        val result = FunctionEvaluator.evaluateBoolean("regex", args, emptyContext)
        assertEquals(false, result)
    }

    @Test
    fun regex_missingValueReturnsFalse() {
        val args = JsonObject(mapOf(
            "pattern" to JsonPrimitive(".*")
        ))
        val result = FunctionEvaluator.evaluateBoolean("regex", args, emptyContext)
        assertEquals(false, result)
    }

    // --- length validation tests ---

    @Test
    fun length_withinRange() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive("hello"),
            "min" to JsonPrimitive(3),
            "max" to JsonPrimitive(10)
        ))
        val result = FunctionEvaluator.evaluateBoolean("length", args, emptyContext)
        assertEquals(true, result)
    }

    @Test
    fun length_tooShort() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive("hi"),
            "min" to JsonPrimitive(3)
        ))
        val result = FunctionEvaluator.evaluateBoolean("length", args, emptyContext)
        assertEquals(false, result)
    }

    @Test
    fun length_tooLong() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive("this is too long"),
            "max" to JsonPrimitive(5)
        ))
        val result = FunctionEvaluator.evaluateBoolean("length", args, emptyContext)
        assertEquals(false, result)
    }

    @Test
    fun length_exactMatch() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive("abc"),
            "min" to JsonPrimitive(3),
            "max" to JsonPrimitive(3)
        ))
        val result = FunctionEvaluator.evaluateBoolean("length", args, emptyContext)
        assertEquals(true, result)
    }

    @Test
    fun length_noConstraintsAlwaysTrue() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive("anything")
        ))
        val result = FunctionEvaluator.evaluateBoolean("length", args, emptyContext)
        assertEquals(true, result)
    }

    // --- numeric validation tests ---

    @Test
    fun numeric_withinRange() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive(5.0),
            "min" to JsonPrimitive(1.0),
            "max" to JsonPrimitive(10.0)
        ))
        val result = FunctionEvaluator.evaluateBoolean("numeric", args, emptyContext)
        assertEquals(true, result)
    }

    @Test
    fun numeric_belowMin() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive(0.5),
            "min" to JsonPrimitive(1.0)
        ))
        val result = FunctionEvaluator.evaluateBoolean("numeric", args, emptyContext)
        assertEquals(false, result)
    }

    @Test
    fun numeric_aboveMax() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive(100.0),
            "max" to JsonPrimitive(50.0)
        ))
        val result = FunctionEvaluator.evaluateBoolean("numeric", args, emptyContext)
        assertEquals(false, result)
    }

    @Test
    fun numeric_missingValueReturnsFalse() {
        val args = JsonObject(mapOf(
            "min" to JsonPrimitive(0.0)
        ))
        val result = FunctionEvaluator.evaluateBoolean("numeric", args, emptyContext)
        assertEquals(false, result)
    }

    // --- and logic tests ---

    @Test
    fun and_allTrueReturnsTrue() {
        val args = JsonObject(mapOf(
            "conditions" to JsonArray(listOf(
                JsonPrimitive(true),
                JsonPrimitive(true),
                JsonPrimitive(true)
            ))
        ))
        val result = FunctionEvaluator.evaluateBoolean("and", args, emptyContext)
        assertEquals(true, result)
    }

    @Test
    fun and_oneFalseReturnsFalse() {
        val args = JsonObject(mapOf(
            "conditions" to JsonArray(listOf(
                JsonPrimitive(true),
                JsonPrimitive(false),
                JsonPrimitive(true)
            ))
        ))
        val result = FunctionEvaluator.evaluateBoolean("and", args, emptyContext)
        assertEquals(false, result)
    }

    @Test
    fun and_emptyConditionsReturnsTrue() {
        val args = JsonObject(mapOf(
            "conditions" to JsonArray(emptyList())
        ))
        val result = FunctionEvaluator.evaluateBoolean("and", args, emptyContext)
        assertEquals(true, result) // all() on empty returns true
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
        val result = FunctionEvaluator.evaluateBoolean("and", args, emptyContext)
        assertEquals(true, result)
    }

    @Test
    fun and_noConditionsKeyReturnsFalse() {
        val args = JsonObject(emptyMap())
        val result = FunctionEvaluator.evaluateBoolean("and", args, emptyContext)
        assertEquals(false, result)
    }

    // --- or logic tests ---

    @Test
    fun or_oneTrueReturnsTrue() {
        val args = JsonObject(mapOf(
            "conditions" to JsonArray(listOf(
                JsonPrimitive(false),
                JsonPrimitive(true),
                JsonPrimitive(false)
            ))
        ))
        val result = FunctionEvaluator.evaluateBoolean("or", args, emptyContext)
        assertEquals(true, result)
    }

    @Test
    fun or_allFalseReturnsFalse() {
        val args = JsonObject(mapOf(
            "conditions" to JsonArray(listOf(
                JsonPrimitive(false),
                JsonPrimitive(false)
            ))
        ))
        val result = FunctionEvaluator.evaluateBoolean("or", args, emptyContext)
        assertEquals(false, result)
    }

    @Test
    fun or_emptyConditionsReturnsFalse() {
        val args = JsonObject(mapOf(
            "conditions" to JsonArray(emptyList())
        ))
        val result = FunctionEvaluator.evaluateBoolean("or", args, emptyContext)
        assertEquals(false, result) // any() on empty returns false
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
        val result = FunctionEvaluator.evaluateBoolean("or", args, emptyContext)
        assertEquals(true, result)
    }

    // --- not logic tests ---

    @Test
    fun not_trueReturnsFalse() {
        val args = JsonObject(mapOf(
            "condition" to JsonPrimitive(true)
        ))
        val result = FunctionEvaluator.evaluateBoolean("not", args, emptyContext)
        assertEquals(false, result)
    }

    @Test
    fun not_falseReturnsTrue() {
        val args = JsonObject(mapOf(
            "condition" to JsonPrimitive(false)
        ))
        val result = FunctionEvaluator.evaluateBoolean("not", args, emptyContext)
        assertEquals(true, result)
    }

    @Test
    fun not_withNestedFunctionCall() {
        val args = JsonObject(mapOf(
            "condition" to JsonObject(mapOf(
                "call" to JsonPrimitive("required"),
                "args" to JsonObject(mapOf("value" to JsonPrimitive("")))
            ))
        ))
        // required("") = false, not(false) = true
        val result = FunctionEvaluator.evaluateBoolean("not", args, emptyContext)
        assertEquals(true, result)
    }

    @Test
    fun not_missingConditionReturnsTrue() {
        val args = JsonObject(emptyMap())
        // condition is null -> value defaults to false -> not(false) = true
        val result = FunctionEvaluator.evaluateBoolean("not", args, emptyContext)
        assertEquals(true, result)
    }

    // --- formatString tests ---

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

        val args = JsonObject(mapOf(
            "template" to JsonPrimitive("Hello, \${/user/name}!")
        ))
        val result = FunctionEvaluator.evaluateString("formatString", args, dataContext)
        assertEquals("Hello, Alice!", result)
    }

    @Test
    fun formatString_missingPathReplacesWithEmpty() {
        val args = JsonObject(mapOf(
            "template" to JsonPrimitive("Value: \${/missing}")
        ))
        val result = FunctionEvaluator.evaluateString("formatString", args, emptyContext)
        assertEquals("Value: ", result)
    }

    @Test
    fun formatString_noPlaceholders() {
        val args = JsonObject(mapOf(
            "template" to JsonPrimitive("No placeholders here")
        ))
        val result = FunctionEvaluator.evaluateString("formatString", args, emptyContext)
        assertEquals("No placeholders here", result)
    }

    @Test
    fun formatString_missingTemplateReturnsEmpty() {
        val args = JsonObject(emptyMap())
        val result = FunctionEvaluator.evaluateString("formatString", args, emptyContext)
        assertEquals("", result)
    }

    // --- formatDate tests ---

    @Test
    fun formatDate_passesThrough() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive("2026-01-15")
        ))
        val result = FunctionEvaluator.evaluateString("formatDate", args, emptyContext)
        assertEquals("2026-01-15", result)
    }

    @Test
    fun formatDate_missingValueReturnsEmpty() {
        val args = JsonObject(emptyMap())
        val result = FunctionEvaluator.evaluateString("formatDate", args, emptyContext)
        assertEquals("", result)
    }

    // --- pluralize tests ---

    @Test
    fun pluralize_zeroUsesZeroForm() {
        val args = JsonObject(mapOf(
            "count" to JsonPrimitive(0),
            "zero" to JsonPrimitive("no items"),
            "one" to JsonPrimitive("1 item"),
            "other" to JsonPrimitive("items")
        ))
        val result = FunctionEvaluator.evaluateString("pluralize", args, emptyContext)
        assertEquals("no items", result)
    }

    @Test
    fun pluralize_oneUsesOneForm() {
        val args = JsonObject(mapOf(
            "count" to JsonPrimitive(1),
            "zero" to JsonPrimitive("no items"),
            "one" to JsonPrimitive("1 item"),
            "other" to JsonPrimitive("items")
        ))
        val result = FunctionEvaluator.evaluateString("pluralize", args, emptyContext)
        assertEquals("1 item", result)
    }

    @Test
    fun pluralize_multipleUsesOtherForm() {
        val args = JsonObject(mapOf(
            "count" to JsonPrimitive(5),
            "zero" to JsonPrimitive("no items"),
            "one" to JsonPrimitive("1 item"),
            "other" to JsonPrimitive("items")
        ))
        val result = FunctionEvaluator.evaluateString("pluralize", args, emptyContext)
        assertEquals("items", result)
    }

    @Test
    fun pluralize_zeroWithoutZeroFormUsesOther() {
        val args = JsonObject(mapOf(
            "count" to JsonPrimitive(0),
            "one" to JsonPrimitive("1 item"),
            "other" to JsonPrimitive("items")
        ))
        val result = FunctionEvaluator.evaluateString("pluralize", args, emptyContext)
        assertEquals("items", result)
    }

    @Test
    fun pluralize_oneWithoutOneFormUsesOther() {
        val args = JsonObject(mapOf(
            "count" to JsonPrimitive(1),
            "other" to JsonPrimitive("items")
        ))
        val result = FunctionEvaluator.evaluateString("pluralize", args, emptyContext)
        assertEquals("items", result)
    }

    // --- formatCurrency additional tests ---

    @Test
    fun formatCurrency_eur() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive(99.99),
            "currency" to JsonPrimitive("EUR")
        ))
        val result = FunctionEvaluator.evaluateString("formatCurrency", args, emptyContext)
        assertEquals("\u20AC99.99", result)
    }

    @Test
    fun formatCurrency_gbp() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive(50.0),
            "currency" to JsonPrimitive("GBP")
        ))
        val result = FunctionEvaluator.evaluateString("formatCurrency", args, emptyContext)
        assertEquals("\u00A350.00", result)
    }

    @Test
    fun formatCurrency_jpy() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive(1000.0),
            "currency" to JsonPrimitive("JPY")
        ))
        val result = FunctionEvaluator.evaluateString("formatCurrency", args, emptyContext)
        assertEquals("\u00A51,000.00", result)
    }

    @Test
    fun formatCurrency_unknownCurrencyUsesCode() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive(42.0),
            "currency" to JsonPrimitive("CHF")
        ))
        val result = FunctionEvaluator.evaluateString("formatCurrency", args, emptyContext)
        assertEquals("CHF42.00", result)
    }

    @Test
    fun formatCurrency_defaultsToUsd() {
        val args = JsonObject(mapOf(
            "value" to JsonPrimitive(10.0)
        ))
        val result = FunctionEvaluator.evaluateString("formatCurrency", args, emptyContext)
        assertEquals("$10.00", result)
    }

    // --- required additional tests ---

    @Test
    fun required_nullArgsReturnsFalse() {
        val result = FunctionEvaluator.evaluateBoolean("required", null, emptyContext)
        assertEquals(false, result)
    }

    @Test
    fun required_blankStringIsFalse() {
        val args = JsonObject(mapOf("value" to JsonPrimitive("   ")))
        val result = FunctionEvaluator.evaluateBoolean("required", args, emptyContext)
        assertEquals(false, result)
    }

    @Test
    fun required_numberIsTrue() {
        val args = JsonObject(mapOf("value" to JsonPrimitive(42)))
        val result = FunctionEvaluator.evaluateBoolean("required", args, emptyContext)
        assertEquals(true, result)
    }

    // --- openUrl ---

    @Test
    fun openUrl_returnsNull() {
        val args = JsonObject(mapOf("url" to JsonPrimitive("https://example.com")))
        val result = FunctionEvaluator.evaluate("openUrl", args, emptyContext)
        assertNull(result) // openUrl is an action, not a value function
    }

    // --- unknown function ---

    @Test
    fun unknownFunction_returnsNull() {
        val result = FunctionEvaluator.evaluate("nonexistent", null, emptyContext)
        assertNull(result)
    }

    // --- path resolution in args ---

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

        val args = JsonObject(mapOf(
            "value" to JsonObject(mapOf("path" to JsonPrimitive("/email")))
        ))
        val result = FunctionEvaluator.evaluateBoolean("email", args, dataContext)
        assertEquals(true, result)
    }
}
