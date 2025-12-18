package com.contextable.a2ui4k.catalog.widgets

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.contextable.a2ui4k.catalog.AvailableIcons
import com.contextable.a2ui4k.model.CatalogItem
import com.contextable.a2ui4k.model.DataContext
import com.contextable.a2ui4k.model.DataReferenceParser
import com.contextable.a2ui4k.model.LiteralString
import com.contextable.a2ui4k.model.PathString
import kotlinx.serialization.json.JsonObject

/**
 * Icon widget that displays a Material icon from the predefined set.
 *
 * JSON Schema:
 * ```json
 * {
 *   "name": {"literalString": "home"} | {"path": "/icon/name"}
 * }
 * ```
 *
 * Available icons: home, menu, arrow_back, arrow_forward, close, add, remove,
 * delete, edit, save, search, refresh, share, settings, check, email, phone,
 * chat, send, notifications, favorite, star, info, warning, error, help,
 * play_arrow, pause, stop, skip_next, skip_previous, volume_up, volume_off,
 * folder, file_copy, attach_file, download, upload, person, people,
 * account_circle, location_on, map, calendar_today, schedule, broken_image
 */
val IconWidget = CatalogItem(
    name = "Icon"
) { componentId, data, buildChild, dataContext, onEvent ->
    IconWidgetContent(data = data, dataContext = dataContext)
}

@Composable
private fun IconWidgetContent(
    data: JsonObject,
    dataContext: DataContext
) {
    val nameRef = DataReferenceParser.parseString(data["name"])
    val iconName = when (nameRef) {
        is LiteralString -> nameRef.value
        is PathString -> dataContext.getString(nameRef.path)
        else -> null
    } ?: "broken_image"

    val icon = AvailableIcons.fromName(iconName)

    Icon(
        imageVector = icon,
        contentDescription = iconName,
        tint = LocalContentColor.current
    )
}
