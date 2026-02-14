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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
