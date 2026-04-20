# Row

Arranges child components horizontally.

> **A2UI Spec:** See `Row` in the [A2UI v0.9 Standard Component Catalog](https://github.com/google/A2UI/tree/main/specification/0.9).
>
> *a2ui-4k implements A2UI **v0.9** with backwards-compatible support for v0.8 (see [Deprecated Protocol Versions](../protocol/deprecated-versions.md)).*

## Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `children` | Children | Yes | Child components (explicit list or template) |
| `distribution` | string | No | Horizontal distribution: `start`, `center`, `end`, `spaceBetween`, `spaceAround`, `spaceEvenly` |
| `alignment` | string | No | Vertical alignment: `start`, `center`, `end`, `stretch` |

## a2ui-4k Implementation

- **Layout:** Uses Compose `Row` with `Arrangement` and `Alignment`
- **Weights:** Children with `weight` property use `Modifier.weight()`
- **Templates:** Supports data-driven children via template reference

## Example

Explicit children:

```json
{
  "id": "toolbar",
  "component": "Row",
  "properties": {
    "children": {
      "explicitList": [
        { "componentId": "back-btn" },
        { "componentId": "title", "weight": 1 },
        { "componentId": "menu-btn" }
      ]
    },
    "alignment": "center"
  }
}
```

With distribution:

```json
{
  "id": "button-row",
  "component": "Row",
  "properties": {
    "children": {
      "explicitList": [
        { "componentId": "cancel-btn" },
        { "componentId": "submit-btn" }
      ]
    },
    "distribution": "spaceEvenly"
  }
}
```

## Distribution Mapping

| Value | Compose Arrangement |
|-------|---------------------|
| `start` | `Arrangement.Start` |
| `center` | `Arrangement.Center` |
| `end` | `Arrangement.End` |
| `spaceBetween` | `Arrangement.SpaceBetween` |
| `spaceAround` | `Arrangement.SpaceAround` |
| `spaceEvenly` | `Arrangement.SpaceEvenly` |

## Alignment Mapping

| Value | Compose Alignment |
|-------|-------------------|
| `start` | `Alignment.Top` |
| `center` | `Alignment.CenterVertically` |
| `end` | `Alignment.Bottom` |
| `stretch` | Fill max height |

## Platform Notes

- Consistent behavior across all platforms via Compose Multiplatform

## See Also

- [A2UI v0.9 Standard Catalog](https://github.com/google/A2UI/tree/main/specification/0.9) - `Row` component
- [Column](column.md) - Vertical equivalent
- [List](list.md) - Scrollable alternative
