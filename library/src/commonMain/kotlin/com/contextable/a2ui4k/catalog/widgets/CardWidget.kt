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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.contextable.a2ui4k.model.CatalogItem
import com.contextable.a2ui4k.model.ChildBuilder
import com.contextable.a2ui4k.model.DataReferenceParser
import com.contextable.a2ui4k.util.PropertyValidation
import kotlinx.serialization.json.JsonObject

/**
 * Card widget that wraps a child component in a Material 3 Card.
 *
 * Matches A2UI protocol Card:
 * - Uses surface color from theme
 * - Applies 8dp internal padding around child
 *
 * JSON Schema:
 * ```json
 * {
 *   "child": "childComponentId"
 * }
 * ```
 */
val CardWidget = CatalogItem(
    name = "Card"
) { componentId, data, buildChild, dataContext, onEvent ->
    CardWidgetContent(data = data, buildChild = buildChild)
}

private val EXPECTED_PROPERTIES = setOf("child")

@Composable
private fun CardWidgetContent(
    data: JsonObject,
    buildChild: ChildBuilder
) {
    PropertyValidation.warnUnexpectedProperties("Card", data, EXPECTED_PROPERTIES)

    val childRef = DataReferenceParser.parseComponentRef(data["child"])
    val childId = childRef?.componentId

    // Match A2UI protocol: Card with surface color and internal padding
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        // Internal padding around child (8dp as per A2UI styling)
        Box(modifier = Modifier.padding(8.dp)) {
            if (childId != null) {
                buildChild(childId)
            }
        }
    }
}
