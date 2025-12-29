# AudioPlayer

Renders an audio player.

> **A2UI Spec:** See `AudioPlayer` in the [Standard Component Catalog v0.8](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json).
>
> *a2ui-4k currently implements the v0.8 specification. The A2UI protocol is under active development.*

## Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `url` | BoundValue&lt;string&gt; | Yes | Audio URL |
| `description` | BoundValue&lt;string&gt; | No | Description text |

## a2ui-4k Implementation

> **Note:** Audio playback is currently a placeholder implementation. Full audio player functionality is planned for a future release.

- **Current:** Displays a placeholder with audio icon and description
- **Planned:** Native audio playback with controls

## Example

```json
{
  "id": "podcast-player",
  "component": "AudioPlayer",
  "properties": {
    "url": { "literalString": "https://example.com/episode.mp3" },
    "description": { "literalString": "Episode 42: Introduction" }
  }
}
```

With data binding:

```json
{
  "id": "track-player",
  "component": "AudioPlayer",
  "properties": {
    "url": { "path": "/track/audioUrl" },
    "description": { "path": "/track/title" }
  }
}
```

## Platform Notes

- Currently renders placeholder on all platforms
- Full implementation will use platform-native audio players

## See Also

- [A2UI Standard Catalog](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json) - `AudioPlayer` component
- [Video](video.md) - For video content
- [Text](text.md) - For displaying track info
