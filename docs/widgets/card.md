# Card

A container that visually groups content with Material card styling.

> **A2UI Spec:** See `Card` in the [A2UI v0.9 Standard Component Catalog](https://github.com/google/A2UI/tree/main/specification/0.9).
>
> *a2ui-4k implements A2UI **v0.9** with backwards-compatible support for v0.8 (see [Deprecated Protocol Versions](../protocol/deprecated-versions.md)).*

## Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `child` | ComponentRef | Yes | Component ID for card content |

## a2ui-4k Implementation

- **Styling:** Uses Material 3 `Card` composable
- **Elevation:** Default card elevation and shape
- **Content:** Renders single child component inside card

## Example

```json
{
  "id": "user-card",
  "component": "Card",
  "properties": {
    "child": { "componentId": "user-card-content" }
  }
}
```

Card with Column content:

```json
{
  "id": "product-card",
  "component": "Card",
  "properties": {
    "child": { "componentId": "product-layout" }
  }
}
```

```json
{
  "id": "product-layout",
  "component": "Column",
  "properties": {
    "children": {
      "explicitList": [
        { "componentId": "product-image" },
        { "componentId": "product-name" },
        { "componentId": "product-price" }
      ]
    }
  }
}
```

## Platform Notes

- Uses Material 3 `Card` on all platforms
- Elevation and rounded corners follow Material guidelines

## See Also

- [A2UI v0.9 Standard Catalog](https://github.com/google/A2UI/tree/main/specification/0.9) - `Card` component
- [Column](column.md) - Common card content layout
- [List](list.md) - Cards often used in lists
