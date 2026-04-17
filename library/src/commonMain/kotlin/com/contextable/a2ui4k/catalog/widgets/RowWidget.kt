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
import com.contextable.a2ui4k.model.ChildrenReference
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
 * A2UI Protocol Properties (v0.9):
 * - children (required): List of child component IDs
 * - justify (optional): start, center, end, spaceBetween, spaceAround
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
val RowWidget = CatalogItem(
    name = "Row"
) { componentId, data, buildChild, dataContext, onEvent ->
    RowWidgetContent(data = data, buildChild = buildChild, dataContext = dataContext)
}

// Accept both v0.9 and v0.8 property names. v0.8 used `distribution`/`alignment`.
private val EXPECTED_PROPERTIES = setOf("children", "justify", "align", "distribution", "alignment")

@Composable
private fun RowWidgetContent(
    data: JsonObject,
    buildChild: ChildBuilder,
    dataContext: DataContext
) {
    PropertyValidation.warnUnexpectedProperties("Row", data, EXPECTED_PROPERTIES)

    val childrenRef = DataReferenceParser.parseChildren(data["children"])
    val children = (childrenRef as? ChildrenReference.ExplicitList)?.componentIds ?: emptyList()

    // Prefer the v0.9 keys; fall back to the v0.8 aliases when absent.
    val justifyRef = DataReferenceParser.parseString(data["justify"])
        ?: DataReferenceParser.parseString(data["distribution"])
    val justify = when (justifyRef) {
        is LiteralString -> justifyRef.value
        is PathString -> dataContext.getString(justifyRef.path)
        else -> null
    }

    val alignRef = DataReferenceParser.parseString(data["align"])
        ?: DataReferenceParser.parseString(data["alignment"])
    val align = when (alignRef) {
        is LiteralString -> alignRef.value
        is PathString -> dataContext.getString(alignRef.path)
        else -> null
    }

    val definition = LocalUiDefinition.current

    // Apply fillMaxWidth only for justify values that need space to distribute.
    // `spaceEvenly` is v0.8-only but cheap to honor unconditionally for forward compat.
    val needsFullWidth = justify?.lowercase() in listOf(
        "spacebetween", "spacearound", "spaceevenly"
    )
    val modifier = if (needsFullWidth) Modifier.fillMaxWidth() else Modifier

    Row(
        modifier = modifier,
        horizontalArrangement = parseHorizontalArrangement(justify),
        verticalAlignment = parseVerticalAlignment(align)
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
 * Parse horizontal arrangement from A2UI justify values.
 *
 * Valid justify values per A2UI v0.9 spec:
 * start, center, end, spaceBetween, spaceAround. `spaceEvenly` (v0.8-only)
 * is also honored.
 */
private fun parseHorizontalArrangement(justify: String?): Arrangement.Horizontal {
    return when (justify?.lowercase()) {
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
 * Parse vertical alignment from A2UI align values.
 *
 * Valid align values per A2UI v0.9 spec:
 * start, center, end, stretch
 */
private fun parseVerticalAlignment(alignment: String?): Alignment.Vertical {
    return when (alignment?.lowercase()) {
        "start" -> Alignment.Top
        "center" -> Alignment.CenterVertically
        "end" -> Alignment.Bottom
        // "stretch" - Compose doesn't have a direct equivalent for vertical alignment
        // Using CenterVertically as fallback. True stretch would need to be handled at the child level.
        "stretch" -> Alignment.CenterVertically
        else -> Alignment.Top
    }
}
