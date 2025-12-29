# Slider

A slider for selecting a numeric value within a range.

> **A2UI Spec:** See `Slider` in the [Standard Component Catalog v0.8](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json).
>
> *a2ui-4k currently implements the v0.8 specification. The A2UI protocol is under active development.*

## Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `value` | BoundValue&lt;number&gt; | Yes | Current value (supports two-way binding) |
| `minValue` | number | No | Minimum value (default: 0) |
| `maxValue` | number | No | Maximum value (default: 100) |

## a2ui-4k Implementation

- **Two-way binding:** When `value` uses a path, sliding updates the DataModel
- **Events:** Emits `DataChangeEvent` when value changes
- **Range:** Enforces min/max constraints

## Example

Basic slider:

```json
{
  "id": "volume-slider",
  "component": "Slider",
  "properties": {
    "value": { "path": "/settings/volume" },
    "minValue": 0,
    "maxValue": 100
  }
}
```

With custom range:

```json
{
  "id": "rating-slider",
  "component": "Slider",
  "properties": {
    "value": { "path": "/review/rating" },
    "minValue": 1,
    "maxValue": 5
  }
}
```

## Event Output

When moved, emits:

```kotlin
DataChangeEvent(
    surfaceId = "default",
    path = "/settings/volume",
    value = "75"  // String representation of number
)
```

## Platform Notes

- Uses Material 3 `Slider` on all platforms
- Continuous value updates during drag

## See Also

- [A2UI Standard Catalog](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json) - `Slider` component
- [Data Binding](../core-concepts/data-binding.md) - Two-way binding
- [TextField](text-field.md) - For numeric text input
