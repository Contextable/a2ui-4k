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
import com.contextable.a2ui4k.model.ChildrenReference
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
 * A2UI Protocol Properties (v0.9):
 * - children (required): List of child component IDs
 * - justify (optional): start, center, end, spaceBetween, spaceAround, spaceEvenly
 * - align (optional): start, center, end, stretch
 *
 * JSON Schema:
 * ```json
 * {
 *   "children": ["child1", "child2"],
 *   "justify": "spaceBetween",
 *   "align": "center"
 * }
 * ```
 */
val ColumnWidget = CatalogItem(
    name = "Column"
) { componentId, data, buildChild, dataContext, onEvent ->
    ColumnWidgetContent(data = data, buildChild = buildChild, dataContext = dataContext)
}

private val EXPECTED_PROPERTIES = setOf("children", "justify", "align")

@Composable
private fun ColumnWidgetContent(
    data: JsonObject,
    buildChild: ChildBuilder,
    dataContext: DataContext
) {
    PropertyValidation.warnUnexpectedProperties("Column", data, EXPECTED_PROPERTIES)

    val childrenRef = DataReferenceParser.parseChildren(data["children"])
    val children = (childrenRef as? ChildrenReference.ExplicitList)?.componentIds ?: emptyList()

    val justifyRef = DataReferenceParser.parseString(data["justify"])
    val justify = when (justifyRef) {
        is LiteralString -> justifyRef.value
        is PathString -> dataContext.getString(justifyRef.path)
        else -> null
    }

    val alignRef = DataReferenceParser.parseString(data["align"])
    val align = when (alignRef) {
        is LiteralString -> alignRef.value
        is PathString -> dataContext.getString(alignRef.path)
        else -> null
    }

    val definition = LocalUiDefinition.current

    // Apply fillMaxWidth only for justify values that need space to distribute.
    // Alignment works within the Column's natural width.
    // This prevents Columns inside Rows from each trying to fill full width.
    val needsFullWidth = justify?.lowercase() in listOf(
        "spacebetween", "spacearound", "spaceevenly"
    )
    val modifier = if (needsFullWidth) Modifier.fillMaxWidth() else Modifier

    Column(
        modifier = modifier,
        verticalArrangement = parseVerticalArrangement(justify),
        horizontalAlignment = parseHorizontalAlignment(align)
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
 * Parse vertical arrangement from A2UI justify values.
 *
 * Valid justify values per A2UI v0.9 spec:
 * start, center, end, spaceBetween, spaceAround, spaceEvenly
 */
private fun parseVerticalArrangement(justify: String?): Arrangement.Vertical {
    return when (justify?.lowercase()) {
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
 * Parse horizontal alignment from A2UI align values.
 *
 * Valid align values per A2UI v0.9 spec:
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
