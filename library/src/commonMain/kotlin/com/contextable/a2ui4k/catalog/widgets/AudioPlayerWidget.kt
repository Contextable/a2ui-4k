package com.contextable.a2ui4k.catalog.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
 * AudioPlayer widget for playing audio content.
 *
 * A2UI Protocol AudioPlayer properties:
 * - `url`: URL of the audio source
 * - `description`: Accessibility description / title
 *
 * See A2UI protocol: standard_catalog_definition.json - AudioPlayer component
 *
 * JSON Schema (v0.9):
 * ```json
 * {
 *   "component": "AudioPlayer",
 *   "url": "https://example.com/audio.mp3",
 *   "description": "Episode 42: Introduction"
 * }
 * ```
 *
 * Note: This is a placeholder implementation. Full audio playback requires
 * platform-specific implementations (ExoPlayer for Android, AVPlayer for iOS).
 */
val AudioPlayerWidget = CatalogItem(
    name = "AudioPlayer"
) { componentId, data, buildChild, dataContext, onEvent ->
    AudioPlayerWidgetContent(
        componentId = componentId,
        data = data,
        dataContext = dataContext
    )
}

@Composable
private fun AudioPlayerWidgetContent(
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
        else -> "Audio"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = { /* Placeholder - implement platform-specific playback */ }) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play audio",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (url.isNotEmpty()) {
                    Text(
                        text = url.substringAfterLast('/'),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
