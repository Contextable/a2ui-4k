# Widget Reference

a2ui-4k implements all 18 widgets from the
[A2UI v0.9 Standard Component Catalog](https://github.com/google/A2UI/tree/main/specification/0.9).

> *For complete property definitions and behavior specifications, refer to the
> canonical A2UI specification. This reference focuses on a2ui-4k
> implementation details. Legacy v0.8 surfaces are transcoded into the v0.9
> shape before reaching widgets — see
> [Deprecated Protocol Versions](../protocol/deprecated-versions.md).*

## Basic Content

Display static or data-bound content.

| Widget | Description |
|--------|-------------|
| [Text](text.md) | Text display with markdown support and styling hints |
| [Image](image.md) | Image rendering from URLs |
| [Icon](icon.md) | Material Design icons |
| [Divider](divider.md) | Horizontal or vertical visual separators |
| [Video](video.md) | Video playback (placeholder implementation) |
| [AudioPlayer](audio-player.md) | Audio playback (placeholder implementation) |

## Layout & Containers

Arrange and group child components.

| Widget | Description |
|--------|-------------|
| [Row](row.md) | Horizontal layout with distribution and alignment |
| [Column](column.md) | Vertical layout with distribution and alignment |
| [List](list.md) | Scrollable lists with explicit or template-based children |
| [Card](card.md) | Material card container |
| [Tabs](tabs.md) | Tabbed navigation with multiple panels |
| [Modal](modal.md) | Dialog overlay triggered by an entry point |

## Interactive & Input

Capture user input and trigger actions.

| Widget | Description |
|--------|-------------|
| [Button](button.md) | Clickable action trigger with `default` / `primary` / `borderless` variants |
| [TextField](text-field.md) | Text input with label, validation, and data binding |
| [CheckBox](checkbox.md) | Boolean toggle with label |
| [Slider](slider.md) | Numeric range input |
| [MultipleChoice](multiple-choice.md) | `ChoicePicker` (variants `multipleSelection` / `mutuallyExclusive`) |
| [DateTimeInput](date-time-input.md) | Date and/or time picker |

## Common Patterns (v0.9)

### Data references

A2UI v0.9 simplified data references — primitives are literals, and
non-literal values use a single-key object discriminator:

```json
// Literal values — JSON primitives are the values themselves
"text": "Hello"
"count": 42
"enabled": true

// Path binding — resolves from the surface's DataModel
"text": { "path": "/user/name" }

// Function call — evaluated by FunctionEvaluator
"text": {
  "call": "formatCurrency",
  "args": {
    "value": { "path": "/total" },
    "spec": { "currency": "USD" }
  }
}
```

### Children

Container widgets accept a flat array of component IDs (v0.9), an
`explicitList`, or a `template`:

```json
// Plain id list (preferred in v0.9)
"children": ["row-1", "row-2", "row-3"]

// Explicit list with weights
"children": {
  "explicitList": [
    { "componentId": "sidebar", "weight": 1 },
    { "componentId": "main",    "weight": 3 }
  ]
}

// Template — bind one component over an array path
"children": {
  "template": {
    "path": "/items",
    "componentId": "item-template"
  }
}
```

### Validation

Input widgets accept a `checks` array of `CheckRule`s. When any check fails
the widget reports a `ValidationError` (and disables itself, where relevant):

```json
"checks": [
  {
    "condition": { "call": "required" },
    "message": "This field is required"
  }
]
```

### Accessibility

Input widgets accept an `accessibility` object with optional
`label` / `description` `DataReference<String>` fields, exposed to the
platform's screen-reader APIs.

## See Also

- [A2UI v0.9 specification](https://github.com/google/A2UI/tree/main/specification/0.9)
- [Catalogs](../core-concepts/catalogs.md) — Creating custom widgets
- [Data Binding](../core-concepts/data-binding.md) — Path-based property binding
- [Events](../core-concepts/events.md) — `ActionEvent`, `ValidationError`, `ClientError`
