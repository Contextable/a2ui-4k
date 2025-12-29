# Widget Reference

a2ui-4k implements all 18 widgets from the [A2UI v0.8 Standard Component Catalog](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json).

> *For complete property definitions and behavior specifications, refer to the canonical A2UI specification. This reference focuses on a2ui-4k implementation details.*

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
| [Button](button.md) | Clickable action trigger with context resolution |
| [TextField](text-field.md) | Text input with label and data binding |
| [CheckBox](checkbox.md) | Boolean toggle with label |
| [Slider](slider.md) | Numeric range input |
| [MultipleChoice](multiple-choice.md) | Single or multi-select options |
| [DateTimeInput](date-time-input.md) | Date and/or time picker |

## Common Patterns

### Property Types

All widgets use consistent property patterns:

```json
// Literal string value
{ "literalString": "Hello" }

// Data-bound string (resolves from DataModel)
{ "path": "/user/name" }

// Literal number
{ "literalNumber": 42 }

// Literal boolean
{ "literalBoolean": true }
```

### Child References

Container widgets reference children by ID:

```json
// Single child
{ "componentId": "my-child" }

// Explicit list of children
{
  "explicitList": [
    { "componentId": "child-1" },
    { "componentId": "child-2" }
  ]
}

// Template-based (for data-driven lists)
{
  "template": {
    "dataPath": "/items",
    "componentId": "item-template"
  }
}
```

### Weights

Children can specify weights for flex-like layouts:

```json
{
  "explicitList": [
    { "componentId": "sidebar", "weight": 1 },
    { "componentId": "main", "weight": 3 }
  ]
}
```

## See Also

- [A2UI Standard Catalog Spec](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json)
- [Catalogs](../core-concepts/catalogs.md) - Creating custom widgets
- [Data Binding](../core-concepts/data-binding.md) - Path-based property binding
