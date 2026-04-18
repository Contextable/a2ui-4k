# Video

Displays a video player.

> **A2UI Spec:** See `Video` in the [A2UI v0.9 Standard Component Catalog](https://github.com/google/A2UI/tree/main/specification/0.9).
>
> *a2ui-4k implements A2UI **v0.9** with backwards-compatible support for v0.8 (see [Deprecated Protocol Versions](../protocol/deprecated-versions.md)).*

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
    "url": "https://example.com/intro.mp4"
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

- [A2UI v0.9 Standard Catalog](https://github.com/google/A2UI/tree/main/specification/0.9) - `Video` component
- [AudioPlayer](audio-player.md) - For audio content
- [Image](image.md) - For static images
