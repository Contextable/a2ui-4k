# Divider

Renders a visual separator line.

> **A2UI Spec:** See `Divider` in the [A2UI v0.9 Standard Component Catalog](https://github.com/google/A2UI/tree/main/specification/0.9).
>
> *a2ui-4k implements A2UI **v0.9** with backwards-compatible support for v0.8 (see [Deprecated Protocol Versions](../protocol/deprecated-versions.md)).*

## Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `axis` | string | No | Orientation: `horizontal` (default), `vertical` |

## a2ui-4k Implementation

- **Styling:** Uses Material 3 `HorizontalDivider` or `VerticalDivider`
- **Color:** Uses theme divider color

## Example

Horizontal divider (default):

```json
{
  "id": "section-divider",
  "component": "Divider",
  "properties": {}
}
```

Vertical divider:

```json
{
  "id": "column-divider",
  "component": "Divider",
  "properties": {
    "axis": "vertical"
  }
}
```

## Usage in Layouts

In a Column:

```json
{
  "id": "settings-list",
  "component": "Column",
  "properties": {
    "children": {
      "explicitList": [
        { "componentId": "setting-1" },
        { "componentId": "divider" },
        { "componentId": "setting-2" }
      ]
    }
  }
}
```

## Platform Notes

- Consistent styling across all platforms via Material 3

## See Also

- [A2UI v0.9 Standard Catalog](https://github.com/google/A2UI/tree/main/specification/0.9) - `Divider` component
- [Column](column.md) - Common parent for horizontal dividers
- [Row](row.md) - Common parent for vertical dividers
