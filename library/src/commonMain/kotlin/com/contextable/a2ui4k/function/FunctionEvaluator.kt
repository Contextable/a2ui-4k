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
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Evaluates A2UI v0.9 standard functions.
 *
 * Argument names match the v0.9 basic catalog exactly. Each arg may be a
 * literal, a `{"path":"…"}` binding, or a nested `{"call":"…","args":{…}}`
 * function call — all three resolve uniformly.
 *
 * ## Standard functions
 *
 * | Group | Function | Args |
 * |---|---|---|
 * | Validation | `required` | `value` |
 * |            | `regex` | `value`, `pattern` |
 * |            | `length` | `value`, `min?`, `max?` |
 * |            | `numeric` | `value`, `min?`, `max?` |
 * |            | `email` | `value` |
 * | Logic      | `and` | `conditions` |
 * |            | `or`  | `conditions` |
 * |            | `not` | `value` |
 * | Formatting | `formatString` | `value` |
 * |            | `formatNumber` | `value`, `decimals?`, `grouping?` |
 * |            | `formatCurrency` | `value`, `currency`, `decimals?`, `grouping?` |
 * |            | `formatDate` | `value`, `format?`, `locale?` |
 * |            | `pluralize` | `count`, `zero?`, `one?`, `other` |
 * | Actions    | `openUrl` | `url`, `target?` |
 */
object FunctionEvaluator {

    fun evaluate(call: String, args: JsonObject?, dataContext: DataContext): JsonElement? {
        return when (call) {
            "required" -> evaluateRequired(args, dataContext)
            "regex" -> evaluateRegex(args, dataContext)
            "length" -> evaluateLength(args, dataContext)
            "numeric" -> evaluateNumeric(args, dataContext)
            "email" -> evaluateEmail(args, dataContext)

            "and" -> evaluateAnd(args, dataContext)
            "or" -> evaluateOr(args, dataContext)
            "not" -> evaluateNot(args, dataContext)

            "formatString" -> evaluateFormatString(args, dataContext)
            "formatNumber" -> evaluateFormatNumber(args, dataContext)
            "formatCurrency" -> evaluateFormatCurrency(args, dataContext)
            "formatDate" -> evaluateFormatDate(args, dataContext)
            "pluralize" -> evaluatePluralize(args, dataContext)

            "openUrl" -> null // action, not a value-returning function

            else -> {
                println("Info: Unknown function '$call'")
                null
            }
        }
    }

    fun evaluateBoolean(call: String, args: JsonObject?, dataContext: DataContext): Boolean? =
        (evaluate(call, args, dataContext) as? JsonPrimitive)?.booleanOrNull

    fun evaluateString(call: String, args: JsonObject?, dataContext: DataContext): String? =
        (evaluate(call, args, dataContext) as? JsonPrimitive)?.contentOrNull

    // --- Validation -----------------------------------------------------

    private fun evaluateRequired(args: JsonObject?, dataContext: DataContext): JsonElement {
        val value = resolveArg(args, "value", dataContext)
        val isValid = when {
            value == null || value is JsonNull -> false
            value is JsonPrimitive && value.contentOrNull.isNullOrBlank() -> false
            value is JsonArray && value.isEmpty() -> false
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
        return JsonPrimitive((min == null || len >= min) && (max == null || len <= max))
    }

    private fun evaluateNumeric(args: JsonObject?, dataContext: DataContext): JsonElement {
        val value = resolveArgNumber(args, "value", dataContext) ?: return JsonPrimitive(false)
        val min = resolveArgNumber(args, "min", dataContext)
        val max = resolveArgNumber(args, "max", dataContext)
        return JsonPrimitive((min == null || value >= min) && (max == null || value <= max))
    }

    private fun evaluateEmail(args: JsonObject?, dataContext: DataContext): JsonElement {
        val value = resolveArgString(args, "value", dataContext) ?: return JsonPrimitive(false)
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return JsonPrimitive(emailRegex.matches(value))
    }

    // --- Logic ----------------------------------------------------------

    private fun evaluateAnd(args: JsonObject?, dataContext: DataContext): JsonElement {
        val conditions = args?.get("conditions") as? JsonArray ?: return JsonPrimitive(false)
        return JsonPrimitive(conditions.all { resolveBoolean(it, dataContext) })
    }

    private fun evaluateOr(args: JsonObject?, dataContext: DataContext): JsonElement {
        val conditions = args?.get("conditions") as? JsonArray ?: return JsonPrimitive(false)
        return JsonPrimitive(conditions.any { resolveBoolean(it, dataContext) })
    }

    private fun evaluateNot(args: JsonObject?, dataContext: DataContext): JsonElement {
        val value = args?.get("value") ?: return JsonPrimitive(true)
        return JsonPrimitive(!resolveBoolean(value, dataContext))
    }

    // --- Formatting -----------------------------------------------------

    private fun evaluateFormatString(args: JsonObject?, dataContext: DataContext): JsonElement {
        val template = resolveArgString(args, "value", dataContext) ?: return JsonPrimitive("")
        val result = Regex("\\$\\{([^}]+)\\}").replace(template) { match ->
            val expression = match.groupValues[1].trim()
            dataContext.getString(expression)
                ?: dataContext.getNumber(expression)?.let { formatPlain(it) }
                ?: dataContext.getBoolean(expression)?.toString()
                ?: ""
        }
        return JsonPrimitive(result)
    }

    private fun evaluateFormatNumber(args: JsonObject?, dataContext: DataContext): JsonElement {
        val value = resolveArgNumber(args, "value", dataContext) ?: return JsonPrimitive("")
        val decimals = resolveArgNumber(args, "decimals", dataContext)?.toInt()
        val grouping = resolveArgBoolean(args, "grouping", dataContext) ?: true
        return JsonPrimitive(formatDecimal(value, decimals, grouping))
    }

    private fun evaluateFormatCurrency(args: JsonObject?, dataContext: DataContext): JsonElement {
        val value = resolveArgNumber(args, "value", dataContext) ?: return JsonPrimitive("")
        val currency = resolveArgString(args, "currency", dataContext) ?: "USD"
        val decimals = resolveArgNumber(args, "decimals", dataContext)?.toInt() ?: 2
        val grouping = resolveArgBoolean(args, "grouping", dataContext) ?: true

        val symbol = when (currency.uppercase()) {
            "USD" -> "$"
            "EUR" -> "\u20AC"
            "GBP" -> "\u00A3"
            "JPY" -> "\u00A5"
            else -> currency
        }
        return JsonPrimitive("$symbol${formatDecimal(value, decimals, grouping)}")
    }

    private fun evaluateFormatDate(args: JsonObject?, dataContext: DataContext): JsonElement {
        val value = resolveArgString(args, "value", dataContext) ?: return JsonPrimitive("")
        // `format` and `locale` are spec-defined but require a platform date library
        // to honor fully; recognize them so they don't warn, pass through the ISO value.
        resolveArgString(args, "format", dataContext)
        resolveArgString(args, "locale", dataContext)
        return JsonPrimitive(value)
    }

    private fun evaluatePluralize(args: JsonObject?, dataContext: DataContext): JsonElement {
        val count = resolveArgNumber(args, "count", dataContext)?.toLong() ?: 0L
        val other = resolveArgString(args, "other", dataContext) ?: ""
        val one = resolveArgString(args, "one", dataContext)
        val zero = resolveArgString(args, "zero", dataContext)
        val result = when {
            count == 0L && zero != null -> zero
            count == 1L && one != null -> one
            else -> other
        }
        return JsonPrimitive(result)
    }

    // --- Argument resolution -------------------------------------------

    /**
     * Resolves an arg value as a raw [JsonElement]. Handles:
     *   * absent / `null` → `null`
     *   * primitive literal → itself
     *   * `{"path":"…"}` → looked up via [dataContext] (string then number then boolean)
     *   * `{"call":"…","args":{…}}` → recursive function evaluation
     */
    private fun resolveArg(args: JsonObject?, name: String, dataContext: DataContext): JsonElement? {
        val element = args?.get(name) ?: return null
        return resolveElement(element, dataContext)
    }

    private fun resolveArgString(args: JsonObject?, name: String, dataContext: DataContext): String? {
        val resolved = resolveArg(args, name, dataContext) ?: return null
        return (resolved as? JsonPrimitive)?.contentOrNull
    }

    private fun resolveArgNumber(args: JsonObject?, name: String, dataContext: DataContext): Double? {
        val resolved = resolveArg(args, name, dataContext) ?: return null
        return (resolved as? JsonPrimitive)?.let { p ->
            p.doubleOrNull ?: p.contentOrNull?.toDoubleOrNull()
        }
    }

    private fun resolveArgBoolean(args: JsonObject?, name: String, dataContext: DataContext): Boolean? {
        val resolved = resolveArg(args, name, dataContext) ?: return null
        return (resolved as? JsonPrimitive)?.booleanOrNull
    }

    private fun resolveElement(element: JsonElement, dataContext: DataContext): JsonElement? {
        return when (element) {
            is JsonPrimitive -> element
            is JsonObject -> when {
                element.containsKey("path") -> {
                    val path = element["path"]?.jsonPrimitive?.contentOrNull ?: return null
                    dataContext.getString(path)?.let { JsonPrimitive(it) }
                        ?: dataContext.getNumber(path)?.let { JsonPrimitive(it) }
                        ?: dataContext.getBoolean(path)?.let { JsonPrimitive(it) }
                }
                element.containsKey("call") -> {
                    val call = element["call"]?.jsonPrimitive?.contentOrNull ?: return null
                    val innerArgs = element["args"] as? JsonObject
                    evaluate(call, innerArgs, dataContext)
                }
                else -> element
            }
            else -> element
        }
    }

    private fun resolveBoolean(element: JsonElement, dataContext: DataContext): Boolean {
        val resolved = resolveElement(element, dataContext)
        return (resolved as? JsonPrimitive)?.booleanOrNull ?: false
    }

    // --- Number formatting helpers --------------------------------------

    private fun formatDecimal(value: Double, decimals: Int?, grouping: Boolean): String {
        val isIntegral = value == value.toLong().toDouble()
        val effective = when {
            decimals != null -> decimals
            isIntegral -> 0
            else -> 3
        }
        val str = if (effective == 0 && isIntegral) {
            value.toLong().toString()
        } else {
            val plain = doubleToPlainString(value)
            val parts = plain.split(".")
            val intPart = parts[0]
            val frac = (parts.getOrNull(1) ?: "")
            val truncated = frac.take(effective).padEnd(effective, '0')
            if (effective > 0) "$intPart.$truncated" else intPart
        }
        return if (grouping) groupInt(str) else str
    }

    private fun formatPlain(value: Double): String =
        if (value == value.toLong().toDouble()) value.toLong().toString()
        else doubleToPlainString(value)

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
            newDecimalPos <= 0 -> "0." + "0".repeat(-newDecimalPos) + digits
            newDecimalPos >= digits.length -> digits + "0".repeat(newDecimalPos - digits.length)
            else -> digits.substring(0, newDecimalPos) + "." + digits.substring(newDecimalPos)
        }
        return if (negative) "-$result" else result
    }

    private fun groupInt(numberStr: String): String {
        val dot = numberStr.indexOf('.')
        val intPart = if (dot >= 0) numberStr.substring(0, dot) else numberStr
        val fracPart = if (dot >= 0) numberStr.substring(dot) else ""
        val negative = intPart.startsWith("-")
        val digits = if (negative) intPart.substring(1) else intPart
        val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
        return (if (negative) "-$grouped" else grouped) + fracPart
    }
}
