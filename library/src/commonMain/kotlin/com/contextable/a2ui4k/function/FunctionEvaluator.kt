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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Evaluates A2UI v0.9 standard functions.
 *
 * The A2UI v0.9 protocol defines a set of named functions that can be
 * referenced in FunctionCall objects. These functions handle validation,
 * formatting, logic operations, and actions.
 *
 * ## Standard Functions
 *
 * **Validation:** required, regex, length, numeric, email
 * **Formatting:** formatString, formatNumber, formatCurrency, formatDate, pluralize
 * **Logic:** and, or, not
 * **Actions:** openUrl
 */
object FunctionEvaluator {

    /**
     * Evaluates a function call and returns the result.
     *
     * @param call The function name
     * @param args The function arguments (may contain dynamic values)
     * @param dataContext The data context for resolving path references in args
     * @return The function result, or null if the function is unknown or evaluation fails
     */
    fun evaluate(call: String, args: JsonObject?, dataContext: DataContext): JsonElement? {
        return when (call) {
            // Validation functions
            "required" -> evaluateRequired(args, dataContext)
            "regex" -> evaluateRegex(args, dataContext)
            "length" -> evaluateLength(args, dataContext)
            "numeric" -> evaluateNumeric(args, dataContext)
            "email" -> evaluateEmail(args, dataContext)

            // Logic functions
            "and" -> evaluateAnd(args, dataContext)
            "or" -> evaluateOr(args, dataContext)
            "not" -> evaluateNot(args, dataContext)

            // Formatting functions
            "formatString" -> evaluateFormatString(args, dataContext)
            "formatNumber" -> evaluateFormatNumber(args, dataContext)
            "formatCurrency" -> evaluateFormatCurrency(args, dataContext)
            "formatDate" -> evaluateFormatDate(args, dataContext)
            "pluralize" -> evaluatePluralize(args, dataContext)

            // Action functions
            "openUrl" -> null // openUrl is an action, not a value-returning function

            else -> {
                println("Info: Unknown function '$call'")
                null
            }
        }
    }

    /**
     * Evaluates a function call that returns a boolean result.
     */
    fun evaluateBoolean(call: String, args: JsonObject?, dataContext: DataContext): Boolean? {
        val result = evaluate(call, args, dataContext) ?: return null
        return (result as? JsonPrimitive)?.booleanOrNull
    }

    /**
     * Evaluates a function call that returns a string result.
     */
    fun evaluateString(call: String, args: JsonObject?, dataContext: DataContext): String? {
        val result = evaluate(call, args, dataContext) ?: return null
        return (result as? JsonPrimitive)?.contentOrNull
    }

    // --- Validation Functions ---

    private fun evaluateRequired(args: JsonObject?, dataContext: DataContext): JsonElement {
        val value = resolveArg(args, "value", dataContext)
        val isValid = when {
            value == null -> false
            value is JsonPrimitive && value.contentOrNull.isNullOrBlank() -> false
            else -> true
        }
        return JsonPrimitive(isValid)
    }

    private fun evaluateRegex(args: JsonObject?, dataContext: DataContext): JsonElement {
        val value = resolveArgString(args, "value", dataContext) ?: return JsonPrimitive(false)
        val pattern = resolveArgString(args, "pattern", dataContext) ?: return JsonPrimitive(false)
        return try {
            JsonPrimitive(Regex(pattern).matches(value))
        } catch (_: Throwable) {
            JsonPrimitive(false)
        }
    }

    private fun evaluateLength(args: JsonObject?, dataContext: DataContext): JsonElement {
        val value = resolveArgString(args, "value", dataContext) ?: return JsonPrimitive(false)
        val min = resolveArgNumber(args, "min", dataContext)?.toInt()
        val max = resolveArgNumber(args, "max", dataContext)?.toInt()

        val len = value.length
        val minOk = min == null || len >= min
        val maxOk = max == null || len <= max
        return JsonPrimitive(minOk && maxOk)
    }

    private fun evaluateNumeric(args: JsonObject?, dataContext: DataContext): JsonElement {
        val value = resolveArgNumber(args, "value", dataContext) ?: return JsonPrimitive(false)
        val min = resolveArgNumber(args, "min", dataContext)
        val max = resolveArgNumber(args, "max", dataContext)

        val minOk = min == null || value >= min
        val maxOk = max == null || value <= max
        return JsonPrimitive(minOk && maxOk)
    }

    private fun evaluateEmail(args: JsonObject?, dataContext: DataContext): JsonElement {
        val value = resolveArgString(args, "value", dataContext) ?: return JsonPrimitive(false)
        // Simple email validation
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return JsonPrimitive(emailRegex.matches(value))
    }

    // --- Logic Functions ---

    private fun evaluateAnd(args: JsonObject?, dataContext: DataContext): JsonElement {
        val conditions = args?.get("conditions")
        if (conditions is kotlinx.serialization.json.JsonArray) {
            return JsonPrimitive(conditions.all { elem ->
                when {
                    elem is JsonPrimitive -> elem.booleanOrNull ?: false
                    elem is JsonObject && elem.containsKey("call") -> {
                        val innerCall = elem["call"]?.jsonPrimitive?.contentOrNull ?: return@all false
                        val innerArgs = elem["args"] as? JsonObject
                        evaluateBoolean(innerCall, innerArgs, dataContext) ?: false
                    }
                    else -> false
                }
            })
        }
        return JsonPrimitive(false)
    }

    private fun evaluateOr(args: JsonObject?, dataContext: DataContext): JsonElement {
        val conditions = args?.get("conditions")
        if (conditions is kotlinx.serialization.json.JsonArray) {
            return JsonPrimitive(conditions.any { elem ->
                when {
                    elem is JsonPrimitive -> elem.booleanOrNull ?: false
                    elem is JsonObject && elem.containsKey("call") -> {
                        val innerCall = elem["call"]?.jsonPrimitive?.contentOrNull ?: return@any false
                        val innerArgs = elem["args"] as? JsonObject
                        evaluateBoolean(innerCall, innerArgs, dataContext) ?: false
                    }
                    else -> false
                }
            })
        }
        return JsonPrimitive(false)
    }

    private fun evaluateNot(args: JsonObject?, dataContext: DataContext): JsonElement {
        val condition = args?.get("condition")
        val value = when {
            condition is JsonPrimitive -> condition.booleanOrNull ?: false
            condition is JsonObject && condition.containsKey("call") -> {
                val innerCall = condition["call"]?.jsonPrimitive?.contentOrNull ?: return JsonPrimitive(true)
                val innerArgs = condition["args"] as? JsonObject
                evaluateBoolean(innerCall, innerArgs, dataContext) ?: false
            }
            else -> false
        }
        return JsonPrimitive(!value)
    }

    // --- Formatting Functions ---

    private fun evaluateFormatString(args: JsonObject?, dataContext: DataContext): JsonElement {
        val template = resolveArgString(args, "template", dataContext) ?: return JsonPrimitive("")

        // Replace ${expression} patterns with resolved values
        val result = Regex("\\$\\{([^}]+)\\}").replace(template) { match ->
            val expression = match.groupValues[1].trim()
            // Check if it's a path reference (starts with / or is a relative path)
            val resolved = dataContext.getString(expression)
                ?: dataContext.getNumber(expression)?.toString()
                ?: dataContext.getBoolean(expression)?.toString()
                ?: ""
            resolved
        }
        return JsonPrimitive(result)
    }

    private fun evaluateFormatNumber(args: JsonObject?, dataContext: DataContext): JsonElement {
        val value = resolveArgNumber(args, "value", dataContext) ?: return JsonPrimitive("")
        val minimumFractionDigits = resolveArgNumber(args, "minimumFractionDigits", dataContext)?.toInt() ?: 0
        val maximumFractionDigits = resolveArgNumber(args, "maximumFractionDigits", dataContext)?.toInt() ?: 3
        val useGrouping = resolveArgBoolean(args, "useGrouping", dataContext) ?: true

        // Basic number formatting
        val formatted = if (maximumFractionDigits == 0 && value == value.toLong().toDouble()) {
            val intStr = value.toLong().toString()
            if (useGrouping) addThousandsSeparator(intStr) else intStr
        } else {
            val str = doubleToPlainString(value)
            val parts = str.split(".")
            val intPart = if (useGrouping) addThousandsSeparator(parts[0]) else parts[0]
            if (parts.size > 1) {
                val fracPart = parts[1].take(maximumFractionDigits).padEnd(minimumFractionDigits, '0')
                if (fracPart.isEmpty()) intPart else "$intPart.$fracPart"
            } else {
                if (minimumFractionDigits > 0) "$intPart.${"0".repeat(minimumFractionDigits)}"
                else intPart
            }
        }
        return JsonPrimitive(formatted)
    }

    private fun evaluateFormatCurrency(args: JsonObject?, dataContext: DataContext): JsonElement {
        val value = resolveArgNumber(args, "value", dataContext) ?: return JsonPrimitive("")
        val currency = resolveArgString(args, "currency", dataContext) ?: "USD"

        val symbol = when (currency.uppercase()) {
            "USD" -> "$"
            "EUR" -> "\u20AC"
            "GBP" -> "\u00A3"
            "JPY" -> "\u00A5"
            else -> currency
        }
        val formatted = addThousandsSeparator(value.toLong().toString())
        val cents = kotlin.math.round((value - value.toLong()) * 100).toLong().toString().padStart(2, '0')
        return JsonPrimitive("$symbol$formatted.$cents")
    }

    private fun evaluateFormatDate(args: JsonObject?, dataContext: DataContext): JsonElement {
        val value = resolveArgString(args, "value", dataContext) ?: return JsonPrimitive("")
        // Basic pass-through for now — full Unicode TR35 format support would require
        // a date parsing library
        return JsonPrimitive(value)
    }

    private fun evaluatePluralize(args: JsonObject?, dataContext: DataContext): JsonElement {
        val count = resolveArgNumber(args, "count", dataContext)?.toLong() ?: 0L
        val other = resolveArgString(args, "other", dataContext) ?: ""
        val one = resolveArgString(args, "one", dataContext)
        val zero = resolveArgString(args, "zero", dataContext)

        // Basic CLDR-like pluralization for English
        val result = when {
            count == 0L && zero != null -> zero
            count == 1L && one != null -> one
            else -> other
        }
        return JsonPrimitive(result)
    }

    // --- Argument Resolution Helpers ---

    private fun resolveArg(args: JsonObject?, name: String, dataContext: DataContext): JsonElement? {
        val element = args?.get(name) ?: return null
        return when {
            element is JsonObject && element.containsKey("path") -> {
                val path = element["path"]?.jsonPrimitive?.contentOrNull ?: return null
                dataContext.getString(path)?.let { JsonPrimitive(it) }
                    ?: dataContext.getNumber(path)?.let { JsonPrimitive(it) }
                    ?: dataContext.getBoolean(path)?.let { JsonPrimitive(it) }
            }
            else -> element
        }
    }

    private fun resolveArgString(args: JsonObject?, name: String, dataContext: DataContext): String? {
        val element = args?.get(name) ?: return null
        return when {
            element is JsonPrimitive -> element.contentOrNull
            element is JsonObject && element.containsKey("path") -> {
                val path = element["path"]?.jsonPrimitive?.contentOrNull ?: return null
                dataContext.getString(path)
            }
            else -> null
        }
    }

    private fun resolveArgNumber(args: JsonObject?, name: String, dataContext: DataContext): Double? {
        val element = args?.get(name) ?: return null
        return when {
            element is JsonPrimitive -> element.doubleOrNull
            element is JsonObject && element.containsKey("path") -> {
                val path = element["path"]?.jsonPrimitive?.contentOrNull ?: return null
                dataContext.getNumber(path)
            }
            else -> null
        }
    }

    private fun resolveArgBoolean(args: JsonObject?, name: String, dataContext: DataContext): Boolean? {
        val element = args?.get(name) ?: return null
        return when {
            element is JsonPrimitive -> element.booleanOrNull
            element is JsonObject && element.containsKey("path") -> {
                val path = element["path"]?.jsonPrimitive?.contentOrNull ?: return null
                dataContext.getBoolean(path)
            }
            else -> null
        }
    }

    /**
     * Converts a Double to a plain decimal string without scientific notation.
     * This is a multiplatform replacement for JVM's BigDecimal.toPlainString().
     */
    private fun doubleToPlainString(value: Double): String {
        val str = value.toString()
        if ('E' !in str && 'e' !in str) return str

        val eIndex = str.indexOfFirst { it == 'E' || it == 'e' }
        val mantissa = str.substring(0, eIndex)
        val exponent = str.substring(eIndex + 1).toInt()

        val negative = mantissa.startsWith("-")
        val absMantissa = if (negative) mantissa.substring(1) else mantissa
        val dotIndex = absMantissa.indexOf('.')
        val digits = absMantissa.replace(".", "")
        val currentDecimalPos = if (dotIndex >= 0) dotIndex else digits.length
        val newDecimalPos = currentDecimalPos + exponent

        val result = when {
            newDecimalPos <= 0 -> {
                "0." + "0".repeat(-newDecimalPos) + digits
            }
            newDecimalPos >= digits.length -> {
                digits + "0".repeat(newDecimalPos - digits.length)
            }
            else -> {
                digits.substring(0, newDecimalPos) + "." + digits.substring(newDecimalPos)
            }
        }

        return if (negative) "-$result" else result
    }

    private fun addThousandsSeparator(intPart: String): String {
        val negative = intPart.startsWith("-")
        val digits = if (negative) intPart.substring(1) else intPart
        val result = digits.reversed().chunked(3).joinToString(",").reversed()
        return if (negative) "-$result" else result
    }
}
