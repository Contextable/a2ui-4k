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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.contextable.a2ui4k.model.CatalogItem
import com.contextable.a2ui4k.model.DataContext
import com.contextable.a2ui4k.model.DataReferenceParser
import com.contextable.a2ui4k.model.LiteralString
import com.contextable.a2ui4k.model.PathString
import com.contextable.a2ui4k.util.PropertyValidation
import kotlinx.serialization.json.JsonObject

/**
 * Video widget for displaying video content.
 *
 * A2UI Protocol Properties (v0.9):
 * - url (required): URL of the video source
 *
 * JSON Schema:
 * ```json
 * {
 *   "url": "https://example.com/video.mp4"
 * }
 * ```
 *
 * Note: This is a placeholder implementation. Full video playback requires
 * platform-specific implementations (ExoPlayer for Android, AVPlayer for iOS).
 */
val VideoWidget = CatalogItem(
    name = "Video"
) { componentId, data, buildChild, dataContext, onEvent ->
    VideoWidgetContent(
        componentId = componentId,
        data = data,
        dataContext = dataContext
    )
}

private val EXPECTED_PROPERTIES = setOf("url")

@Composable
private fun VideoWidgetContent(
    componentId: String,
    data: JsonObject,
    dataContext: DataContext
) {
    PropertyValidation.warnUnexpectedProperties("Video", data, EXPECTED_PROPERTIES)

    val urlRef = DataReferenceParser.parseString(data["url"])

    val url = when (urlRef) {
        is LiteralString -> urlRef.value
        is PathString -> dataContext.getString(urlRef.path) ?: ""
        else -> ""
    }

    // Placeholder video player UI
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play video",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    }
}
