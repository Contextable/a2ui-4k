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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.contextable.a2ui4k.model.CatalogItem
import com.contextable.a2ui4k.model.DataContext
import com.contextable.a2ui4k.model.DataReferenceParser
import com.contextable.a2ui4k.model.EventDispatcher
import com.contextable.a2ui4k.model.LiteralString
import com.contextable.a2ui4k.model.PathString
import com.contextable.a2ui4k.util.PropertyValidation
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Tabs widget for tabbed navigation between child components.
 *
 * JSON Schema (v0.9):
 * ```json
 * {
 *   "component": "Tabs",
 *   "properties": {
 *     "tabItems": [
 *       {"title": "Tab 1", "child": "content_1"},
 *       {"title": "Tab 2", "child": "content_2"}
 *     ]
 *   }
 * }
 * ```
 */
val TabsWidget = CatalogItem(
    name = "Tabs"
) { componentId, data, buildChild, dataContext, onEvent ->
    TabsWidgetContent(
        componentId = componentId,
        data = data,
        buildChild = buildChild,
        dataContext = dataContext
    )
}

private val EXPECTED_PROPERTIES = setOf("tabItems")

@Composable
private fun TabsWidgetContent(
    componentId: String,
    data: JsonObject,
    buildChild: @Composable (String) -> Unit,
    dataContext: DataContext
) {
    PropertyValidation.warnUnexpectedProperties("Tabs", data, EXPECTED_PROPERTIES)

    val tabItemsArray = data["tabItems"]?.jsonArray ?: return

    val tabs = tabItemsArray.mapNotNull { tabElement ->
        val tabObj = tabElement.jsonObject

        // Parse title (can be literal or path)
        val titleRef = DataReferenceParser.parseString(tabObj["title"])
        val title = when (titleRef) {
            is LiteralString -> titleRef.value
            is PathString -> dataContext.getString(titleRef.path) ?: ""
            else -> tabObj["title"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
        }

        // Parse child ID
        val childRef = DataReferenceParser.parseComponentRef(tabObj["child"])
        val childId = childRef?.componentId
            ?: tabObj["child"]?.jsonPrimitive?.contentOrNull
            ?: return@mapNotNull null

        title to childId
    }

    if (tabs.isEmpty()) return

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, (title, _) ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        // Render the selected tab's content
        val (_, childId) = tabs[selectedTabIndex]
        buildChild(childId)
    }
}
