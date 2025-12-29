# CheckBox

A toggleable checkbox with label.

> **A2UI Spec:** See `CheckBox` in the [Standard Component Catalog v0.8](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json).
>
> *a2ui-4k currently implements the v0.8 specification. The A2UI protocol is under active development.*

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
    "label": { "literalString": "I agree to the terms" },
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
    "label": { "literalString": "Subscribe to newsletter" },
    "value": { "literalBoolean": false }
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

- [A2UI Standard Catalog](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json) - `CheckBox` component
- [Data Binding](../core-concepts/data-binding.md) - Two-way binding
- [MultipleChoice](multiple-choice.md) - For multiple selections
