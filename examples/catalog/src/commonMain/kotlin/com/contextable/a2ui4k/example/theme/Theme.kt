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

package com.contextable.a2ui4k.example.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme()
private val DarkColorScheme = darkColorScheme()

// Custom shapes matching A2UI styling patterns
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),  // Cards, images
    large = RoundedCornerShape(16.dp),   // Buttons, dialogs
    extraLarge = RoundedCornerShape(24.dp)
)

// Custom typography with italic caption style per A2UI spec
private val AppTypography: Typography
    @Composable
    get() {
        val defaultTypography = Typography()
        return Typography(
            displayLarge = defaultTypography.displayLarge,
            displayMedium = defaultTypography.displayMedium,
            displaySmall = defaultTypography.displaySmall,
            headlineLarge = defaultTypography.headlineLarge,
            headlineMedium = defaultTypography.headlineMedium,
            headlineSmall = defaultTypography.headlineSmall,
            titleLarge = defaultTypography.titleLarge,
            titleMedium = defaultTypography.titleMedium,
            titleSmall = defaultTypography.titleSmall,
            bodyLarge = defaultTypography.bodyLarge,
            bodyMedium = defaultTypography.bodyMedium,
            // Caption uses bodySmall with italic
            bodySmall = defaultTypography.bodySmall.copy(fontStyle = FontStyle.Italic),
            labelLarge = defaultTypography.labelLarge,
            labelMedium = defaultTypography.labelMedium,
            labelSmall = defaultTypography.labelSmall
        )
    }

@Composable
fun CatalogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
