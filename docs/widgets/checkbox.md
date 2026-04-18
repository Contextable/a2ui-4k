# CheckBox

A toggleable checkbox with label.

> **A2UI Spec:** See `CheckBox` in the [A2UI v0.9 Standard Component Catalog](https://github.com/google/A2UI/tree/main/specification/0.9).
>
> *a2ui-4k implements A2UI **v0.9** with backwards-compatible support for v0.8 (see [Deprecated Protocol Versions](../protocol/deprecated-versions.md)).*

## Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `label` | BoundValue&lt;string&gt; | Yes | Checkbox label text |
| `value` | BoundValue&lt;boolean&gt; | Yes | Checked state (supports two-way binding) |

## a2ui-4k Implementation

- **Two-way binding:** When `value` uses a path, toggling updates the DataModel
- **Events:** Emits `DataChangeEvent` when toggled
- **Layout:** Label displayed next to checkbox

## Example

Basic checkbox:

```json
{
  "id": "terms-checkbox",
  "component": "CheckBox",
  "properties": {
    "label": "I agree to the terms",
    "value": { "path": "/form/termsAccepted" }
  }
}
```

With literal initial value:

```json
{
  "id": "newsletter-checkbox",
  "component": "CheckBox",
  "properties": {
    "label": "Subscribe to newsletter",
    "value": false
  }
}
```

## Event Output

When toggled, emits:

```kotlin
DataChangeEvent(
    surfaceId = "default",
    path = "/form/termsAccepted",
    value = "true"  // or "false"
)
```

## Platform Notes

- Uses Material 3 `Checkbox` with `Row` layout on all platforms

## See Also

- [A2UI v0.9 Standard Catalog](https://github.com/google/A2UI/tree/main/specification/0.9) - `CheckBox` component
- [Data Binding](../core-concepts/data-binding.md) - Two-way binding
- [MultipleChoice](multiple-choice.md) - For multiple selections
