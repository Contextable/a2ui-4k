package com.contextable.a2ui4k.catalog.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.contextable.a2ui4k.model.CatalogItem
import com.contextable.a2ui4k.model.ChildBuilder
import com.contextable.a2ui4k.model.DataContext
import com.contextable.a2ui4k.model.DataReferenceParser
import com.contextable.a2ui4k.model.LiteralBoolean
import com.contextable.a2ui4k.model.LiteralString
import com.contextable.a2ui4k.model.PathBoolean
import com.contextable.a2ui4k.model.PathString
import kotlinx.serialization.json.JsonObject

/**
 * Modal widget for displaying dialog overlays.
 *
 * A2UI Protocol Modal properties:
 * - `child`: Component ID of the content to display
 * - `title`: Modal title text
 * - `open`: Boolean or path controlling visibility
 *
 * See A2UI protocol: standard_catalog_definition.json - Modal component
 *
 * JSON Schema (v0.9):
 * ```json
 * {
 *   "component": "Modal",
 *   "child": "modal-content",
 *   "title": "Confirmation",
 *   "open": {"path": "/ui/modalOpen"} | true
 * }
 * ```
 */
val ModalWidget = CatalogItem(
    name = "Modal"
) { componentId, data, buildChild, dataContext, onEvent ->
    ModalWidgetContent(
        componentId = componentId,
        data = data,
        buildChild = buildChild,
        dataContext = dataContext
    )
}

@Composable
private fun ModalWidgetContent(
    componentId: String,
    data: JsonObject,
    buildChild: ChildBuilder,
    dataContext: DataContext
) {
    val titleRef = DataReferenceParser.parseString(data["title"])
    val openRef = DataReferenceParser.parseBoolean(data["open"])
    val childRef = DataReferenceParser.parseComponentRef(data["child"])

    val title = when (titleRef) {
        is LiteralString -> titleRef.value
        is PathString -> dataContext.getString(titleRef.path) ?: ""
        else -> ""
    }

    val isOpen = when (openRef) {
        is PathBoolean -> dataContext.getBoolean(openRef.path) ?: false
        is LiteralBoolean -> openRef.value
        else -> false
    }

    val childId = childRef?.componentId

    var showDialog by remember(isOpen) { mutableStateOf(isOpen) }

    if (showDialog && childId != null) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                // Update path if bound
                if (openRef is PathBoolean) {
                    dataContext.update(openRef.path, false)
                }
            },
            title = if (title.isNotEmpty()) {
                { Text(text = title, style = MaterialTheme.typography.headlineSmall) }
            } else null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    buildChild(childId)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    if (openRef is PathBoolean) {
                        dataContext.update(openRef.path, false)
                    }
                }) {
                    Text("Close")
                }
            }
        )
    }
}
