package com.contextable.a2ui4k.catalog.widgets

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.contextable.a2ui4k.model.CatalogItem
import com.contextable.a2ui4k.model.DataContext
import com.contextable.a2ui4k.model.DataReferenceParser
import com.contextable.a2ui4k.model.LiteralString
import com.contextable.a2ui4k.model.PathString
import com.contextable.a2ui4k.util.PropertyValidation
import kotlinx.serialization.json.JsonObject

/**
 * Divider widget that displays a thin line to separate content.
 *
 * JSON Schema:
 * ```json
 * {
 *   "axis": "horizontal" | "vertical" (optional, default: horizontal)
 * }
 * ```
 */
val DividerWidget = CatalogItem(
    name = "Divider"
) { componentId, data, buildChild, dataContext, onEvent ->
    DividerWidgetContent(data = data, dataContext = dataContext)
}

private val EXPECTED_PROPERTIES = setOf("axis")

@Composable
private fun DividerWidgetContent(
    data: JsonObject,
    dataContext: DataContext
) {
    PropertyValidation.warnUnexpectedProperties("Divider", data, EXPECTED_PROPERTIES)

    val axisRef = DataReferenceParser.parseString(data["axis"])
    val axis = when (axisRef) {
        is LiteralString -> axisRef.value
        is PathString -> dataContext.getString(axisRef.path)
        else -> null
    }

    val isVertical = axis?.lowercase() == "vertical"

    if (isVertical) {
        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
        )
    } else {
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
        )
    }
}
