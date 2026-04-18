# DateTimeInput

A dedicated input for selecting date and/or time values.

> **A2UI Spec:** See `DateTimeInput` in the [A2UI v0.9 Standard Component Catalog](https://github.com/google/A2UI/tree/main/specification/0.9).
>
> *a2ui-4k implements A2UI **v0.9** with backwards-compatible support for v0.8 (see [Deprecated Protocol Versions](../protocol/deprecated-versions.md)).*

## Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `value` | BoundValue&lt;string&gt; | Yes | ISO 8601 date/time string (supports two-way binding) |
| `enableDate` | boolean | No | Show date picker (default: true) |
| `enableTime` | boolean | No | Show time picker (default: false) |

## a2ui-4k Implementation

- **Format:** Values are ISO 8601 strings (e.g., `2025-01-15T10:30:00`)
- **Two-way binding:** Selection updates the DataModel
- **Events:** Emits `DataChangeEvent` when value changes

## Example

Date only:

```json
{
  "id": "birthdate-input",
  "component": "DateTimeInput",
  "properties": {
    "value": { "path": "/user/birthdate" },
    "enableDate": true,
    "enableTime": false
  }
}
```

Date and time:

```json
{
  "id": "appointment-input",
  "component": "DateTimeInput",
  "properties": {
    "value": { "path": "/booking/dateTime" },
    "enableDate": true,
    "enableTime": true
  }
}
```

Time only:

```json
{
  "id": "reminder-time",
  "component": "DateTimeInput",
  "properties": {
    "value": { "path": "/reminder/time" },
    "enableDate": false,
    "enableTime": true
  }
}
```

## Event Output

When selected, emits:

```kotlin
DataChangeEvent(
    surfaceId = "default",
    path = "/booking/dateTime",
    value = "2025-01-15T10:30:00"
)
```

## Platform Notes

- Uses platform-appropriate date/time pickers
- Android uses Material DatePicker/TimePicker dialogs

## See Also

- [A2UI v0.9 Standard Catalog](https://github.com/google/A2UI/tree/main/specification/0.9) - `DateTimeInput` component
- [TextField](text-field.md) - Alternative with `textFieldType: "date"`
- [Data Binding](../core-concepts/data-binding.md) - Two-way binding
