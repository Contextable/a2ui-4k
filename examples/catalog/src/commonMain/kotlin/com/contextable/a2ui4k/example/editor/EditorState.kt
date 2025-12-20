package com.contextable.a2ui4k.example.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.contextable.a2ui4k.model.UiDefinition
import kotlinx.serialization.json.JsonObject

/**
 * Result of parsing JSON into a UiDefinition.
 */
sealed class ParseResult {
    data class Success(
        val definition: UiDefinition,
        val initialData: JsonObject
    ) : ParseResult()
    data class Error(val message: String) : ParseResult()
    data object Empty : ParseResult()
}

/**
 * State holder for the widget editor with separate Components and Data panes.
 */
@Stable
class EditorState(initialComponents: String, initialData: String) {
    var componentsJson: String by mutableStateOf(initialComponents)
        private set

    var dataJson: String by mutableStateOf(initialData)
        private set

    var parseResult: ParseResult by mutableStateOf(ParseResult.Empty)
        private set

    val errorMessage: String?
        get() = (parseResult as? ParseResult.Error)?.message

    init {
        parse()
    }

    fun updateComponents(newJson: String) {
        componentsJson = newJson
        parse()
    }

    fun updateData(newJson: String) {
        dataJson = newJson
        parse()
    }

    private fun parse() {
        parseResult = JsonParser.parse(componentsJson, dataJson)
    }
}

@Composable
fun rememberEditorState(initialComponents: String, initialData: String): EditorState {
    return remember(initialComponents, initialData) {
        EditorState(initialComponents, initialData)
    }
}
