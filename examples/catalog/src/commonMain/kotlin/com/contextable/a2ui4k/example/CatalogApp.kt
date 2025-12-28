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

package com.contextable.a2ui4k.example

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.contextable.a2ui4k.example.catalog.WidgetEditorPage
import com.contextable.a2ui4k.example.catalog.WidgetListPage
import com.contextable.a2ui4k.example.theme.CatalogTheme

/**
 * Navigation destinations for the catalog app.
 */
sealed class Screen {
    data object WidgetList : Screen()
    data class WidgetEditor(val widgetName: String) : Screen()
}

/**
 * Main entry point for the A2UI Catalog app.
 */
@Composable
fun CatalogApp() {
    var currentScreen: Screen by remember { mutableStateOf(Screen.WidgetList) }

    CatalogTheme {
        AnimatedContent(targetState = currentScreen) { screen ->
            when (screen) {
                is Screen.WidgetList -> WidgetListPage(
                    onWidgetSelected = { widgetName ->
                        currentScreen = Screen.WidgetEditor(widgetName)
                    }
                )
                is Screen.WidgetEditor -> WidgetEditorPage(
                    widgetName = screen.widgetName,
                    onBack = { currentScreen = Screen.WidgetList }
                )
            }
        }
    }
}
