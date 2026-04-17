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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.contextable.a2ui4k.model.Accessibility
import com.contextable.a2ui4k.model.CatalogItem
import com.contextable.a2ui4k.model.DataContext
import com.contextable.a2ui4k.model.DataReferenceParser
import com.contextable.a2ui4k.model.LiteralString
import com.contextable.a2ui4k.model.PathString
import com.contextable.a2ui4k.util.PropertyValidation
import kotlinx.serialization.json.JsonObject

/**
 * Image widget that displays an image from a URL.
 *
 * A2UI v0.9 Image properties:
 * - `url`: DynamicString image source
 * - `fit`: `"contain"` | `"cover"` | `"fill"` | `"none"` | `"scaleDown"`
 * - `variant`: `"thumbnail"` | `"avatar"` (size/shape hint)
 * - `accessibility`: Optional accessibility metadata (label used as contentDescription)
 *
 * ```json
 * {
 *   "component": "Image",
 *   "url": "https://example.com/image.jpg",
 *   "fit": "cover",
 *   "variant": "thumbnail"
 * }
 * ```
 */
val ImageWidget = CatalogItem(
    name = "Image"
) { componentId, data, buildChild, dataContext, onEvent ->
    ImageWidgetContent(data = data, dataContext = dataContext)
}

private val EXPECTED_PROPERTIES = setOf("url", "fit", "variant", "accessibility")

@Composable
private fun ImageWidgetContent(
    data: JsonObject,
    dataContext: DataContext
) {
    PropertyValidation.warnUnexpectedProperties("Image", data, EXPECTED_PROPERTIES)

    val urlRef = DataReferenceParser.parseString(data["url"])
    val fitRef = DataReferenceParser.parseString(data["fit"])
    val variantRef = DataReferenceParser.parseString(data["variant"])

    val url = when (urlRef) {
        is LiteralString -> urlRef.value
        is PathString -> dataContext.getString(urlRef.path)
        else -> null
    }

    val fit = when (fitRef) {
        is LiteralString -> fitRef.value
        is PathString -> dataContext.getString(fitRef.path)
        else -> null
    }

    val variant = when (variantRef) {
        is LiteralString -> variantRef.value
        is PathString -> dataContext.getString(variantRef.path)
        else -> null
    }

    // v0.9 `fit` uses camelCase `scaleDown` (maps to CSS object-fit: scale-down).
    val contentScale = when (fit) {
        "contain" -> ContentScale.Fit
        "cover" -> ContentScale.Crop
        "fill" -> ContentScale.FillBounds
        "none" -> ContentScale.None
        "scaleDown" -> ContentScale.Fit
        else -> ContentScale.Crop
    }

    val (height, fillWidth, shape) = getImageDimensions(variant)

    val accessibility = Accessibility.fromJson(data["accessibility"])
    val contentDescription = accessibility?.resolveLabel(dataContext)

    if (url != null) {
        val sizeModifier = when {
            fillWidth && height != null -> Modifier.fillMaxWidth().height(height)
            fillWidth -> Modifier.fillMaxWidth()
            height != null -> Modifier.size(height)
            else -> Modifier.size(100.dp)
        }
        val modifier = if (shape != null) sizeModifier.clip(shape) else sizeModifier

        SubcomposeAsyncImage(
            model = url,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            loading = {
                Box(
                    modifier = modifier,
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            },
            error = {
                Box(
                    modifier = modifier,
                    contentAlignment = Alignment.Center
                ) {
                    Text("Failed to load image")
                }
            }
        )
    }
}

private val ImageCornerRadius = RoundedCornerShape(12.dp)

/**
 * Maps A2UI v0.9 `variant` to dimensions + shape.
 *
 * - `thumbnail`: small rounded square
 * - `avatar`: circular
 * - (absent): default rounded-corner 100dp square
 */
private fun getImageDimensions(variant: String?): Triple<Dp?, Boolean, Shape?> {
    return when (variant) {
        "thumbnail" -> Triple(80.dp, false, ImageCornerRadius)
        "avatar" -> Triple(48.dp, false, CircleShape)
        else -> Triple(100.dp, false, ImageCornerRadius)
    }
}
