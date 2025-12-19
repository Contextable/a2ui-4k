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
