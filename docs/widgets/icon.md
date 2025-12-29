# Icon

Renders a predefined Material Design icon.

> **A2UI Spec:** See `Icon` in the [Standard Component Catalog v0.8](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json).
>
> *a2ui-4k currently implements the v0.8 specification. The A2UI protocol is under active development.*

## Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `name` | BoundValue&lt;string&gt; | Yes | Icon name from predefined set |

## a2ui-4k Implementation

- **Icon set:** Maps to Material Design Icons (Filled variant)
- **Fallback:** Unknown icon names render a help/question icon
- **Sizing:** Icons size based on container context

## Example

```json
{
  "id": "settings-icon",
  "component": "Icon",
  "properties": {
    "name": { "literalString": "settings" }
  }
}
```

With data binding:

```json
{
  "id": "status-icon",
  "component": "Icon",
  "properties": {
    "name": { "path": "/item/statusIcon" }
  }
}
```

## Available Icons

The A2UI spec defines these standard icon names:

| Category | Icons |
|----------|-------|
| Navigation | `arrowBack`, `arrowForward`, `menu`, `home`, `close` |
| Actions | `add`, `delete`, `edit`, `refresh`, `search`, `send`, `share`, `download`, `upload`, `print` |
| Status | `check`, `error`, `info`, `warning`, `help` |
| Media | `camera`, `photo`, `visibility`, `visibilityOff` |
| Communication | `mail`, `call`, `phone`, `notifications`, `notificationsOff` |
| Social | `person`, `accountCircle`, `favorite`, `favoriteOff`, `star`, `starHalf`, `starOff` |
| Files | `folder`, `attachFile` |
| Commerce | `shoppingCart`, `payment` |
| Security | `lock`, `lockOpen` |
| Other | `settings`, `moreVert`, `moreHoriz`, `calendarToday`, `event`, `locationOn` |

## Platform Notes

- Uses Compose Material Icons on all platforms
- Icon color inherits from content color context

## See Also

- [A2UI Standard Catalog](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json) - `Icon` component
- [Image](image.md) - For raster images
- [Button](button.md) - Often contains icons
