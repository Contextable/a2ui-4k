# Video

Displays a video player.

> **A2UI Spec:** See `Video` in the [Standard Component Catalog v0.8](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json).
>
> *a2ui-4k currently implements the v0.8 specification. The A2UI protocol is under active development.*

## Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `url` | BoundValue&lt;string&gt; | Yes | Video URL |

## a2ui-4k Implementation

> **Note:** Video playback is currently a placeholder implementation. Full video player functionality is planned for a future release.

- **Current:** Displays a placeholder with video icon
- **Planned:** Native video playback with controls

## Example

```json
{
  "id": "intro-video",
  "component": "Video",
  "properties": {
    "url": { "literalString": "https://example.com/intro.mp4" }
  }
}
```

With data binding:

```json
{
  "id": "product-video",
  "component": "Video",
  "properties": {
    "url": { "path": "/product/videoUrl" }
  }
}
```

## Platform Notes

- Currently renders placeholder on all platforms
- Full implementation will use platform-native video players

## See Also

- [A2UI Standard Catalog](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json) - `Video` component
- [AudioPlayer](audio-player.md) - For audio content
- [Image](image.md) - For static images
