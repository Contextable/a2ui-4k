package com.contextable.a2ui4k.example.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.contextable.a2ui4k.model.UiDefinition

/**
 * Result of parsing JSON into a UiDefinition.
 */
sealed class ParseResult {
    data class Success(val definition: UiDefinition) : ParseResult()
    data class Error(val message: String) : ParseResult()
    data object Empty : ParseResult()
}

/**
 * State holder for the widget editor.
 */
@Stable
class EditorState(initialJson: String) {
    var jsonInput: String by mutableStateOf(initialJson)
        private set

    var parseResult: ParseResult by mutableStateOf(ParseResult.Empty)
        private set

    val errorMessage: String?
        get() = (parseResult as? ParseResult.Error)?.message

    init {
        parse()
    }

    fun updateJson(newJson: String) {
        jsonInput = newJson
        parse()
    }

    private fun parse() {
        parseResult = JsonParser.parseToUiDefinition(jsonInput)
    }
}

@Composable
fun rememberEditorState(initialJson: String): EditorState {
    return remember(initialJson) { EditorState(initialJson) }
}
