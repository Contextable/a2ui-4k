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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
 * Row widget that arranges children horizontally.
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
val RowWidget = CatalogItem(
    name = "Row"
) { componentId, data, buildChild, dataContext, onEvent ->
    RowWidgetContent(data = data, buildChild = buildChild, dataContext = dataContext)
}

private val EXPECTED_PROPERTIES = setOf("children", "distribution", "alignment")

@Composable
private fun RowWidgetContent(
    data: JsonObject,
    buildChild: ChildBuilder,
    dataContext: DataContext
) {
    PropertyValidation.warnUnexpectedProperties("Row", data, EXPECTED_PROPERTIES)

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
    // This allows simple Rows to be centered by parent alignment, while
    // spaceAround/spaceBetween/etc. get full width to distribute children.
    val needsFullWidth = distribution?.lowercase() in listOf(
        "spacebetween", "spacearound", "spaceevenly"
    )
    val modifier = if (needsFullWidth) Modifier.fillMaxWidth() else Modifier

    Row(
        modifier = modifier,
        horizontalArrangement = parseHorizontalArrangement(distribution),
        verticalAlignment = parseVerticalAlignment(alignment)
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
private fun RowScope.BuildWeightedChild(
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
 * Parse horizontal arrangement from A2UI distribution values.
 *
 * Valid distribution values per A2UI v0.8 spec:
 * start, center, end, spaceBetween, spaceAround, spaceEvenly
 */
private fun parseHorizontalArrangement(distribution: String?): Arrangement.Horizontal {
    return when (distribution?.lowercase()) {
        "start" -> Arrangement.Start
        "center" -> Arrangement.Center
        "end" -> Arrangement.End
        "spacebetween" -> Arrangement.SpaceBetween
        "spacearound" -> Arrangement.SpaceAround
        "spaceevenly" -> Arrangement.SpaceEvenly
        else -> Arrangement.Start
    }
}

/**
 * Parse vertical alignment from A2UI alignment values.
 *
 * Valid alignment values per A2UI v0.8 spec:
 * start, center, end, stretch
 */
private fun parseVerticalAlignment(alignment: String?): Alignment.Vertical {
    return when (alignment?.lowercase()) {
        "top", "start" -> Alignment.Top
        "center" -> Alignment.CenterVertically
        "bottom", "end" -> Alignment.Bottom
        // "stretch" - Compose doesn't have a direct equivalent for vertical alignment
        // Using CenterVertically as fallback. True stretch would need to be handled at the child level.
        "stretch" -> Alignment.CenterVertically
        else -> Alignment.Top
    }
}
