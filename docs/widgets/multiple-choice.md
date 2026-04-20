# MultipleChoice

A component for selecting one or more options from a list.

> **A2UI Spec:** See `MultipleChoice` in the [A2UI v0.9 Standard Component Catalog](https://github.com/google/A2UI/tree/main/specification/0.9).
>
> *a2ui-4k implements A2UI **v0.9** with backwards-compatible support for v0.8 (see [Deprecated Protocol Versions](../protocol/deprecated-versions.md)).*

## Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `selections` | BoundValue&lt;array&gt; | Yes | Currently selected values (supports two-way binding) |
| `options` | array&lt;Option&gt; | Yes | Available options, each with `label` and `value` |
| `maxAllowedSelections` | integer | No | Max selections (1 = single select, omit for unlimited) |

### Option Object

```json
{
  "label": "Display Text",
  "value": "stored-value"
}
```

## a2ui-4k Implementation

- **Single select:** When `maxAllowedSelections` is 1, renders as radio buttons
- **Multi select:** Otherwise renders as checkboxes
- **Events:** Emits `DataChangeEvent` when selection changes

## Example

Single selection (radio):

```json
{
  "id": "priority-select",
  "component": "MultipleChoice",
  "properties": {
    "selections": { "path": "/task/priority" },
    "options": [
      { "label": "Low", "value": "low" },
      { "label": "Medium", "value": "medium" },
      { "label": "High", "value": "high" }
    ],
    "maxAllowedSelections": 1
  }
}
```

Multiple selection (checkboxes):

```json
{
  "id": "tags-select",
  "component": "MultipleChoice",
  "properties": {
    "selections": { "path": "/item/tags" },
    "options": [
      { "label": "Featured", "value": "featured" },
      { "label": "Sale", "value": "sale" },
      { "label": "New", "value": "new" }
    ]
  }
}
```

## Platform Notes

- Uses Material 3 `RadioButton` or `Checkbox` based on `maxAllowedSelections`
- Options rendered in vertical list

## See Also

- [A2UI v0.9 Standard Catalog](https://github.com/google/A2UI/tree/main/specification/0.9) - `MultipleChoice` component
- [CheckBox](checkbox.md) - Single boolean toggle
- [Data Binding](../core-concepts/data-binding.md) - Array bindings
