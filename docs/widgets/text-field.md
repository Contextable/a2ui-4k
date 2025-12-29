# TextField

An input field for text entry with label and data binding support.

> **A2UI Spec:** See `TextField` in the [Standard Component Catalog v0.8](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json).
>
> *a2ui-4k currently implements the v0.8 specification. The A2UI protocol is under active development.*

## Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `label` | BoundValue&lt;string&gt; | Yes | Field label |
| `text` | BoundValue&lt;string&gt; | No | Current text value (supports two-way binding) |
| `textFieldType` | string | No | Input type: `shortText`, `longText`, `number`, `obscured`, `date` |
| `validationRegexp` | string | No | Regex pattern for validation |

## a2ui-4k Implementation

- **Two-way binding:** When `text` uses a path, user input automatically updates the DataModel
- **Events:** Emits `DataChangeEvent` when text changes
- **Input types:** Maps to appropriate Compose keyboard types

## Example

Basic text field:

```json
{
  "id": "name-input",
  "component": "TextField",
  "properties": {
    "label": { "literalString": "Full Name" },
    "text": { "path": "/user/name" }
  }
}
```

With input type:

```json
{
  "id": "email-input",
  "component": "TextField",
  "properties": {
    "label": { "literalString": "Email Address" },
    "text": { "path": "/user/email" },
    "textFieldType": "shortText"
  }
}
```

## Input Type Mapping

| Type | a2ui-4k Behavior |
|------|------------------|
| `shortText` | Single-line text input |
| `longText` | Multi-line text area |
| `number` | Numeric keyboard |
| `obscured` | Password field (masked input) |
| `date` | Date input (consider DateTimeInput instead) |

## Platform Notes

- Uses Material 3 `OutlinedTextField` on all platforms
- Keyboard types are platform-appropriate

## See Also

- [A2UI Standard Catalog](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json) - `TextField` component
- [Data Binding](../core-concepts/data-binding.md) - Two-way binding
- [DateTimeInput](date-time-input.md) - For date/time input
