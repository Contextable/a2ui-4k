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
 * A2UI Protocol Properties (v0.9):
 * - url (required): Image source as string or path
 * - fit (optional): How image resizes - contain, cover, fill, none, scale-down
 * - variant (optional): Suggests intended size - icon, avatar, smallFeature, mediumFeature, largeFeature, header
 *
 * JSON Schema:
 * ```json
 * {
 *   "url": "https://example.com/image.jpg" | {"path": "/item/imageUrl"},
 *   "fit": "cover" | "contain" | "fill" | "none" | "scale-down" (optional),
 *   "variant": "icon" | "avatar" | "smallFeature" | "mediumFeature" | "largeFeature" | "header" (optional)
 * }
 * ```
 */
val ImageWidget = CatalogItem(
    name = "Image"
) { componentId, data, buildChild, dataContext, onEvent ->
    ImageWidgetContent(data = data, dataContext = dataContext)
}

private val EXPECTED_PROPERTIES = setOf("url", "fit", "variant")

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

    // Map fit to ContentScale (A2UI spec values map to CSS object-fit)
    val contentScale = when (fit?.lowercase()) {
        "contain" -> ContentScale.Fit
        "cover" -> ContentScale.Crop
        "fill" -> ContentScale.FillBounds
        "none" -> ContentScale.None
        "scale-down" -> ContentScale.Fit // Closest approximation
        else -> ContentScale.Crop // Default to cover behavior
    }

    // Map variant to appropriate dimensions and shape
    val (height, fillWidth, shape) = getImageDimensions(variant)

    if (url != null) {
        val sizeModifier = when {
            fillWidth && height != null -> Modifier.fillMaxWidth().height(height)
            fillWidth -> Modifier.fillMaxWidth()
            height != null -> Modifier.size(height) // Square image for non-fullWidth
            else -> Modifier.size(100.dp) // Sensible default square
        }
        val modifier = if (shape != null) sizeModifier.clip(shape) else sizeModifier

        SubcomposeAsyncImage(
            model = url,
            contentDescription = null,
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
 * Maps A2UI variant to appropriate image dimensions and shape.
 *
 * @return Triple of (height in dp or null, fillWidth boolean, shape or null)
 */
private fun getImageDimensions(variant: String?): Triple<Dp?, Boolean, Shape?> {
    return when (variant?.lowercase()) {
        "icon" -> Triple(24.dp, false, null) // Icons: no rounded corners
        "avatar" -> Triple(48.dp, false, CircleShape) // Avatars: circular
        "smallfeature" -> Triple(80.dp, true, ImageCornerRadius)
        "mediumfeature" -> Triple(150.dp, true, ImageCornerRadius)
        "largefeature" -> Triple(220.dp, true, ImageCornerRadius)
        "header" -> Triple(200.dp, true, ImageCornerRadius)
        else -> Triple(100.dp, false, ImageCornerRadius) // Default: rounded corners
    }
}
