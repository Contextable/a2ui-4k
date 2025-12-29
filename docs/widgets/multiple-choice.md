# MultipleChoice

A component for selecting one or more options from a list.

> **A2UI Spec:** See `MultipleChoice` in the [Standard Component Catalog v0.8](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json).
>
> *a2ui-4k currently implements the v0.8 specification. The A2UI protocol is under active development.*

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

- [A2UI Standard Catalog](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json) - `MultipleChoice` component
- [CheckBox](checkbox.md) - Single boolean toggle
- [Data Binding](../core-concepts/data-binding.md) - Array bindings
