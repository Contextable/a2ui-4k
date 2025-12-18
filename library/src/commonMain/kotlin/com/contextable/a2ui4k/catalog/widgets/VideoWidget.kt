package com.contextable.a2ui4k.catalog.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.contextable.a2ui4k.model.CatalogItem
import com.contextable.a2ui4k.model.DataContext
import com.contextable.a2ui4k.model.DataReferenceParser
import com.contextable.a2ui4k.model.LiteralString
import com.contextable.a2ui4k.model.PathString
import kotlinx.serialization.json.JsonObject

/**
 * Video widget for displaying video content.
 *
 * A2UI Protocol Video properties:
 * - `url`: URL of the video source
 * - `description`: Accessibility description
 *
 * See A2UI protocol: standard_catalog_definition.json - Video component
 *
 * JSON Schema (v0.9):
 * ```json
 * {
 *   "component": "Video",
 *   "url": "https://example.com/video.mp4",
 *   "description": "Product demo video"
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

@Composable
private fun VideoWidgetContent(
    componentId: String,
    data: JsonObject,
    dataContext: DataContext
) {
    val urlRef = DataReferenceParser.parseString(data["url"])
    val descriptionRef = DataReferenceParser.parseString(data["description"])

    val url = when (urlRef) {
        is LiteralString -> urlRef.value
        is PathString -> dataContext.getString(urlRef.path) ?: ""
        else -> ""
    }

    val description = when (descriptionRef) {
        is LiteralString -> descriptionRef.value
        is PathString -> dataContext.getString(descriptionRef.path) ?: ""
        else -> ""
    }

    Column(modifier = Modifier.fillMaxWidth()) {
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

        if (description.isNotEmpty()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
