# List

Renders a scrollable list of items, supporting both explicit children and data-driven templates.

> **A2UI Spec:** See `List` in the [Standard Component Catalog v0.8](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json).
>
> *a2ui-4k currently implements the v0.8 specification. The A2UI protocol is under active development.*

## Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `children` | Children | Yes | Child components (explicit list or template) |
| `direction` | string | No | Scroll direction: `vertical` (default), `horizontal` |
| `alignment` | string | No | Cross-axis alignment: `start`, `center`, `end`, `stretch` |

## a2ui-4k Implementation

- **Scrolling:** Uses Compose `LazyColumn` or `LazyRow`
- **Templates:** Data-driven rendering with automatic data context scoping
- **Item keys:** Template items use data index as stable key

## Example

Explicit list:

```json
{
  "id": "menu",
  "component": "List",
  "properties": {
    "children": {
      "explicitList": [
        { "componentId": "item-home" },
        { "componentId": "item-settings" },
        { "componentId": "item-help" }
      ]
    },
    "direction": "vertical"
  }
}
```

Template-based (data-driven):

```json
{
  "id": "product-list",
  "component": "List",
  "properties": {
    "children": {
      "template": {
        "dataPath": "/products",
        "componentId": "product-card"
      }
    },
    "direction": "vertical"
  }
}
```

## Template Data Binding

When using templates, each item's data context is scoped to its array element:

```json
// DataModel contains:
{
  "products": [
    { "name": "Apple", "price": 1.50 },
    { "name": "Banana", "price": 0.75 }
  ]
}

// Template component uses relative paths:
{
  "id": "product-card",
  "component": "Text",
  "properties": {
    "text": { "path": "/name" }  // Resolves to "Apple", "Banana", etc.
  }
}
```

## Template Events

Buttons inside templates include the item index in `sourceComponentId`:

```kotlin
// Button with id="buy-btn" in second item
sourceComponentId = "buy-btn:1"
```

## Horizontal Lists

```json
{
  "id": "image-gallery",
  "component": "List",
  "properties": {
    "children": {
      "template": {
        "dataPath": "/images",
        "componentId": "gallery-image"
      }
    },
    "direction": "horizontal"
  }
}
```

## Platform Notes

- Uses `LazyColumn`/`LazyRow` for efficient rendering of large lists
- Consistent scrolling behavior across platforms

## See Also

- [A2UI Standard Catalog](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json) - `List` component
- [Data Binding](../core-concepts/data-binding.md) - Template data scoping
- [Column](column.md) - Non-scrolling alternative
