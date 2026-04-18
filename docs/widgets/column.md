# Column

Arranges child components vertically.

> **A2UI Spec:** See `Column` in the [A2UI v0.9 Standard Component Catalog](https://github.com/google/A2UI/tree/main/specification/0.9).
>
> *a2ui-4k implements A2UI **v0.9** with backwards-compatible support for v0.8 (see [Deprecated Protocol Versions](../protocol/deprecated-versions.md)).*

## Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `children` | Children | Yes | Child components (explicit list or template) |
| `distribution` | string | No | Vertical distribution: `start`, `center`, `end`, `spaceBetween`, `spaceAround`, `spaceEvenly` |
| `alignment` | string | No | Horizontal alignment: `start`, `center`, `end`, `stretch` |

## a2ui-4k Implementation

- **Layout:** Uses Compose `Column` with `Arrangement` and `Alignment`
- **Weights:** Children with `weight` property use `Modifier.weight()`
- **Templates:** Supports data-driven children via template reference

## Example

Explicit children:

```json
{
  "id": "main-layout",
  "component": "Column",
  "properties": {
    "children": {
      "explicitList": [
        { "componentId": "header" },
        { "componentId": "content", "weight": 1 },
        { "componentId": "footer" }
      ]
    },
    "alignment": "stretch"
  }
}
```

With distribution:

```json
{
  "id": "centered-stack",
  "component": "Column",
  "properties": {
    "children": {
      "explicitList": [
        { "componentId": "icon" },
        { "componentId": "title" },
        { "componentId": "subtitle" }
      ]
    },
    "distribution": "center",
    "alignment": "center"
  }
}
```

## Distribution Mapping

| Value | Compose Arrangement |
|-------|---------------------|
| `start` | `Arrangement.Top` |
| `center` | `Arrangement.Center` |
| `end` | `Arrangement.Bottom` |
| `spaceBetween` | `Arrangement.SpaceBetween` |
| `spaceAround` | `Arrangement.SpaceAround` |
| `spaceEvenly` | `Arrangement.SpaceEvenly` |

## Alignment Mapping

| Value | Compose Alignment |
|-------|-------------------|
| `start` | `Alignment.Start` |
| `center` | `Alignment.CenterHorizontally` |
| `end` | `Alignment.End` |
| `stretch` | Fill max width |

## Platform Notes

- Consistent behavior across all platforms via Compose Multiplatform

## See Also

- [A2UI v0.9 Standard Catalog](https://github.com/google/A2UI/tree/main/specification/0.9) - `Column` component
- [Row](row.md) - Horizontal equivalent
- [List](list.md) - Scrollable alternative
