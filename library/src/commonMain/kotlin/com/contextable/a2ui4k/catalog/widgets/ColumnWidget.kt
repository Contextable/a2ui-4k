package com.contextable.a2ui4k.catalog.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.contextable.a2ui4k.model.CatalogItem
import com.contextable.a2ui4k.model.ChildBuilder
import com.contextable.a2ui4k.model.DataContext
import com.contextable.a2ui4k.model.DataReferenceParser
import com.contextable.a2ui4k.model.LiteralString
import com.contextable.a2ui4k.model.PathString
import com.contextable.a2ui4k.render.LocalUiDefinition
import com.contextable.a2ui4k.util.PropertyValidation
import kotlinx.serialization.json.JsonObject

/**
 * Column widget that arranges children vertically.
 *
 * A2UI Protocol Properties (v0.8):
 * - children (required): List of child component IDs
 * - distribution (optional): start, center, end, spaceBetween, spaceAround, spaceEvenly
 * - alignment (optional): start, center, end, stretch
 *
 * JSON Schema:
 * ```json
 * {
 *   "children": {"explicitList": ["child1", "child2"]},
 *   "distribution": {"literalString": "spaceBetween"},
 *   "alignment": {"literalString": "center"}
 * }
 * ```
 */
val ColumnWidget = CatalogItem(
    name = "Column"
) { componentId, data, buildChild, dataContext, onEvent ->
    ColumnWidgetContent(data = data, buildChild = buildChild, dataContext = dataContext)
}

private val EXPECTED_PROPERTIES = setOf("children", "distribution", "alignment")

@Composable
private fun ColumnWidgetContent(
    data: JsonObject,
    buildChild: ChildBuilder,
    dataContext: DataContext
) {
    PropertyValidation.warnUnexpectedProperties("Column", data, EXPECTED_PROPERTIES)

    val childrenRef = DataReferenceParser.parseComponentArray(data["children"])
    val children = childrenRef?.componentIds ?: emptyList()

    val distributionRef = DataReferenceParser.parseString(data["distribution"])
    val distribution = when (distributionRef) {
        is LiteralString -> distributionRef.value
        is PathString -> dataContext.getString(distributionRef.path)
        else -> null
    }

    val alignmentRef = DataReferenceParser.parseString(data["alignment"])
    val alignment = when (alignmentRef) {
        is LiteralString -> alignmentRef.value
        is PathString -> dataContext.getString(alignmentRef.path)
        else -> null
    }

    val definition = LocalUiDefinition.current

    // Apply fillMaxWidth only for distributions that need space to distribute.
    // Alignment works within the Column's natural width.
    // This prevents Columns inside Rows from each trying to fill full width.
    val needsFullWidth = distribution?.lowercase() in listOf(
        "spacebetween", "spacearound", "spaceevenly"
    )
    val modifier = if (needsFullWidth) Modifier.fillMaxWidth() else Modifier

    Column(
        modifier = modifier,
        verticalArrangement = parseVerticalArrangement(distribution),
        horizontalAlignment = parseHorizontalAlignment(alignment)
    ) {
        children.forEach { childId ->
            val weight = definition?.components?.get(childId)?.weight
            BuildWeightedChild(weight = weight, buildChild = buildChild, childId = childId)
        }
    }
}

/**
 * Helper that wraps a child with Modifier.weight() if weight is specified.
 * Similar to A2UI protocol's buildWeightedChild pattern.
 */
@Composable
private fun ColumnScope.BuildWeightedChild(
    weight: Int?,
    buildChild: ChildBuilder,
    childId: String
) {
    if (weight != null && weight > 0) {
        Box(modifier = Modifier.weight(weight.toFloat())) {
            buildChild(childId)
        }
    } else {
        buildChild(childId)
    }
}

/**
 * Parse vertical arrangement from A2UI distribution values.
 *
 * Valid distribution values per A2UI v0.8 spec:
 * start, center, end, spaceBetween, spaceAround, spaceEvenly
 */
private fun parseVerticalArrangement(distribution: String?): Arrangement.Vertical {
    return when (distribution?.lowercase()) {
        "start", "top" -> Arrangement.Top
        "center" -> Arrangement.Center
        "end", "bottom" -> Arrangement.Bottom
        "spacebetween" -> Arrangement.SpaceBetween
        "spacearound" -> Arrangement.SpaceAround
        "spaceevenly" -> Arrangement.SpaceEvenly
        else -> Arrangement.Top
    }
}

/**
 * Parse horizontal alignment from A2UI alignment values.
 *
 * Valid alignment values per A2UI v0.8 spec:
 * start, center, end, stretch
 */
private fun parseHorizontalAlignment(alignment: String?): Alignment.Horizontal {
    return when (alignment?.lowercase()) {
        "start", "left" -> Alignment.Start
        "center" -> Alignment.CenterHorizontally
        "end", "right" -> Alignment.End
        // "stretch" - Compose doesn't have a direct equivalent for horizontal alignment
        // Using CenterHorizontally as fallback. True stretch would need fillMaxWidth on children.
        "stretch" -> Alignment.CenterHorizontally
        else -> Alignment.Start
    }
}
