package com.contextable.a2ui4k.catalog.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.contextable.a2ui4k.model.CatalogItem
import com.contextable.a2ui4k.model.DataChangeEvent
import com.contextable.a2ui4k.model.DataContext
import com.contextable.a2ui4k.model.DataReferenceParser
import com.contextable.a2ui4k.model.EventDispatcher
import com.contextable.a2ui4k.model.LiteralString
import com.contextable.a2ui4k.model.PathString
import com.contextable.a2ui4k.render.LocalUiDefinition
import com.contextable.a2ui4k.util.PropertyValidation
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * DateTimeInput widget for date and/or time input.
 *
 * A2UI Protocol Properties (v0.8):
 * - value (required): Date/time value, supports path binding for two-way data binding
 * - enableDate (optional): Enable date picker (default true)
 * - enableTime (optional): Enable time picker (default false)
 *
 * JSON Schema:
 * ```json
 * {
 *   "value": {"path": "/form/date"},
 *   "enableDate": {"literalBoolean": true},
 *   "enableTime": {"literalBoolean": true}
 * }
 * ```
 */
val DateTimeInputWidget = CatalogItem(
    name = "DateTimeInput"
) { componentId, data, buildChild, dataContext, onEvent ->
    DateTimeInputWidgetContent(
        componentId = componentId,
        data = data,
        dataContext = dataContext,
        onEvent = onEvent
    )
}

private val EXPECTED_PROPERTIES = setOf("value", "enableDate", "enableTime")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimeInputWidgetContent(
    componentId: String,
    data: JsonObject,
    dataContext: DataContext,
    onEvent: EventDispatcher
) {
    PropertyValidation.warnUnexpectedProperties("DateTimeInput", data, EXPECTED_PROPERTIES)

    val valueRef = DataReferenceParser.parseString(data["value"])

    // Get surfaceId from UiDefinition
    val uiDefinition = LocalUiDefinition.current
    val surfaceId = uiDefinition?.surfaceId ?: "default"

    val enableDate = data["enableDate"]?.jsonPrimitive?.booleanOrNull ?: true
    val enableTime = data["enableTime"]?.jsonPrimitive?.booleanOrNull ?: false

    val initialValue = when (valueRef) {
        is PathString -> dataContext.getString(valueRef.path) ?: ""
        is LiteralString -> valueRef.value
        else -> ""
    }

    var dateTimeValue by remember(initialValue) { mutableStateOf(initialValue) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Parse existing date value for picker initialization
    val initialDateMillis: Long? = try {
        if (dateTimeValue.isNotEmpty()) {
            // Try to parse ISO date format (YYYY-MM-DD)
            val datePart = dateTimeValue.substringBefore(" ").substringBefore("T")
            if (datePart.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                val parts = datePart.split("-")
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                // Convert to epoch millis using days since epoch calculation
                val daysFromEpoch = calculateDaysFromEpoch(year, month, day)
                daysFromEpoch * 24L * 60L * 60L * 1000L
            } else null
        } else null
    } catch (e: Exception) {
        null
    }

    // Parse existing time value for picker initialization
    val (initialHour, initialMinute) = try {
        if (dateTimeValue.isNotEmpty() && enableTime) {
            val timePart = dateTimeValue.substringAfter(" ", "").substringAfter("T", "").substringBefore("Z")
            if (timePart.matches(Regex("\\d{2}:\\d{2}(:\\d{2})?"))) {
                val parts = timePart.split(":")
                Pair(parts[0].toInt(), parts[1].toInt())
            } else Pair(12, 0)
        } else Pair(12, 0)
    } catch (e: Exception) {
        Pair(12, 0)
    }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
    val timePickerState = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute)

    // Determine placeholder based on what's enabled
    val placeholder = when {
        enableDate && enableTime -> "YYYY-MM-DD HH:MM"
        enableDate -> "YYYY-MM-DD"
        enableTime -> "HH:MM"
        else -> ""
    }

    fun updateValue(newValue: String) {
        dateTimeValue = newValue
        if (valueRef is PathString) {
            dataContext.update(valueRef.path, newValue)
            onEvent(
                DataChangeEvent(
                    surfaceId = surfaceId,
                    path = valueRef.path,
                    value = newValue
                )
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = dateTimeValue,
            onValueChange = { newValue ->
                updateValue(newValue)
            },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                if (enableDate) {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select date"
                        )
                    }
                } else if (enableTime) {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Select time"
                        )
                    }
                }
            }
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val (year, month, day) = millisToDate(millis)
                            val dateStr = "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

                            if (enableTime) {
                                // Preserve existing time if present, otherwise show time picker
                                val existingTime = dateTimeValue.substringAfter(" ", "").substringAfter("T", "").substringBefore("Z")
                                if (existingTime.isNotEmpty()) {
                                    updateValue("$dateStr $existingTime")
                                } else {
                                    updateValue(dateStr)
                                    showTimePicker = true
                                }
                            } else {
                                updateValue(dateStr)
                            }
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                showTimePicker = false
                val hour = timePickerState.hour.toString().padStart(2, '0')
                val minute = timePickerState.minute.toString().padStart(2, '0')
                val timeStr = "$hour:$minute"

                if (enableDate) {
                    // Append time to existing date
                    val existingDate = dateTimeValue.substringBefore(" ").substringBefore("T")
                    if (existingDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                        updateValue("$existingDate $timeStr")
                    } else {
                        updateValue(timeStr)
                    }
                } else {
                    updateValue(timeStr)
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        text = { content() }
    )
}

// Date calculation helpers - pure Kotlin, no external dependencies

private fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

private fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (isLeapYear(year)) 29 else 28
    else -> 30
}

private fun calculateDaysFromEpoch(year: Int, month: Int, day: Int): Long {
    var days = 0L
    // Add days for years since 1970
    for (y in 1970 until year) {
        days += if (isLeapYear(y)) 366 else 365
    }
    // Add days for months in current year
    for (m in 1 until month) {
        days += daysInMonth(year, m)
    }
    // Add days in current month
    days += day - 1
    return days
}

private data class DateComponents(val year: Int, val month: Int, val day: Int)

private fun millisToDate(millis: Long): DateComponents {
    var remainingDays = (millis / (24L * 60L * 60L * 1000L)).toInt()
    var year = 1970

    // Find year
    while (true) {
        val daysInYear = if (isLeapYear(year)) 366 else 365
        if (remainingDays < daysInYear) break
        remainingDays -= daysInYear
        year++
    }

    // Find month
    var month = 1
    while (month <= 12) {
        val daysInThisMonth = daysInMonth(year, month)
        if (remainingDays < daysInThisMonth) break
        remainingDays -= daysInThisMonth
        month++
    }

    // Remaining is day of month (0-indexed, so add 1)
    val day = remainingDays + 1

    return DateComponents(year, month, day)
}
