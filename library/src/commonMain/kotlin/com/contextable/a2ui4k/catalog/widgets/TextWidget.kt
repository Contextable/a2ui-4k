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

package com.contextable.a2ui4k.catalog.widgets

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import com.contextable.a2ui4k.model.CatalogItem
import com.contextable.a2ui4k.model.DataContext
import com.contextable.a2ui4k.model.DataReferenceParser
import com.contextable.a2ui4k.model.LiteralString
import com.contextable.a2ui4k.model.PathString
import com.contextable.a2ui4k.util.PropertyValidation
import com.contextable.a2ui4k.util.parseBasicMarkdown
import kotlinx.serialization.json.JsonObject

/**
 * Text widget that displays a string with optional markdown formatting.
 *
 * A2UI Protocol Properties (v0.8):
 * - text (required): Text content to display
 * - usageHint (optional): h1, h2, h3, h4, h5, caption, body
 *
 * JSON Schema:
 * ```json
 * {
 *   "text": {"literalString": "Hello"} | {"path": "/user/name"},
 *   "usageHint": {"literalString": "h1"} | {"literalString": "body"}
 * }
 * ```
 */
val TextWidget = CatalogItem(
    name = "Text"
) { componentId, data, buildChild, dataContext, onEvent ->
    TextWidgetContent(data = data, dataContext = dataContext)
}

private val EXPECTED_PROPERTIES = setOf("text", "usageHint")

@Composable
private fun TextWidgetContent(
    data: JsonObject,
    dataContext: DataContext
) {
    PropertyValidation.warnUnexpectedProperties("Text", data, EXPECTED_PROPERTIES)

    val textRef = DataReferenceParser.parseString(data["text"])
    val usageHint = data["usageHint"]?.let {
        DataReferenceParser.parseString(it)
    }

    val text = when (textRef) {
        is LiteralString -> textRef.value
        is PathString -> dataContext.getString(textRef.path) ?: ""
        else -> ""
    }

    val hint = when (usageHint) {
        is LiteralString -> usageHint.value
        is PathString -> dataContext.getString(usageHint.path)
        else -> null
    }

    val style = getTextStyle(hint)
    val annotatedString = parseBasicMarkdown(text)

    Text(
        text = annotatedString,
        style = style
    )
}

/**
 * Maps A2UI usageHint values to Compose TextStyle.
 *
 * Valid usageHint values per A2UI v0.8 spec:
 * h1, h2, h3, h4, h5, caption, body
 */
@Composable
private fun getTextStyle(usageHint: String?): TextStyle {
    return when (usageHint?.lowercase()) {
        "h1" -> MaterialTheme.typography.headlineLarge
        "h2" -> MaterialTheme.typography.headlineMedium
        "h3" -> MaterialTheme.typography.headlineSmall
        "h4" -> MaterialTheme.typography.titleLarge
        "h5" -> MaterialTheme.typography.titleMedium
        "body" -> MaterialTheme.typography.bodyLarge
        "caption" -> MaterialTheme.typography.bodySmall
        else -> LocalTextStyle.current
    }
}
