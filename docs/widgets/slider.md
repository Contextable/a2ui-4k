# Slider

A slider for selecting a numeric value within a range.

> **A2UI Spec:** See `Slider` in the [A2UI v0.9 Standard Component Catalog](https://github.com/google/A2UI/tree/main/specification/0.9).
>
> *a2ui-4k implements A2UI **v0.9** with backwards-compatible support for v0.8 (see [Deprecated Protocol Versions](../protocol/deprecated-versions.md)).*

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

- [A2UI v0.9 Standard Catalog](https://github.com/google/A2UI/tree/main/specification/0.9) - `Slider` component
- [Data Binding](../core-concepts/data-binding.md) - Two-way binding
- [TextField](text-field.md) - For numeric text input
