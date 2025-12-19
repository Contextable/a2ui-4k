package com.contextable.a2ui4k.example

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import coil3.network.ktor3.KtorNetworkFetcherFactory

fun main() {
    // Configure Coil for desktop with network support
    SingletonImageLoader.setSafe {
        ImageLoader.Builder(PlatformContext.INSTANCE)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .crossfade(true)
            .build()
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "A2UI Widget Catalog",
            state = rememberWindowState(width = 1200.dp, height = 800.dp)
        ) {
            CatalogApp()
        }
    }
}
