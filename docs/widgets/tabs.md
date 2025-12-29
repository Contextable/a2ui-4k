# Tabs

A container that displays multiple content panels with tab navigation.

> **A2UI Spec:** See `Tabs` in the [Standard Component Catalog v0.8](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json).
>
> *a2ui-4k currently implements the v0.8 specification. The A2UI protocol is under active development.*

## Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `tabItems` | array&lt;TabItem&gt; | Yes | Tab definitions, each with `title` and `child` |

### TabItem Object

```json
{
  "title": "Tab Label",
  "child": "content-component-id"
}
```

## a2ui-4k Implementation

- **Navigation:** Clicking tabs switches visible content
- **State:** Tab selection is managed internally
- **Styling:** Uses Material 3 `TabRow`

## Example

```json
{
  "id": "settings-tabs",
  "component": "Tabs",
  "properties": {
    "tabItems": [
      { "title": "General", "child": "general-settings" },
      { "title": "Privacy", "child": "privacy-settings" },
      { "title": "Notifications", "child": "notification-settings" }
    ]
  }
}
```

With corresponding content components:

```json
{
  "id": "general-settings",
  "component": "Column",
  "properties": {
    "children": {
      "explicitList": [
        { "componentId": "language-setting" },
        { "componentId": "theme-setting" }
      ]
    }
  }
}
```

## Platform Notes

- Uses Material 3 `TabRow` with `Tab` components
- Content area shows only the selected tab's child
- Smooth transitions between tabs

## See Also

- [A2UI Standard Catalog](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json) - `Tabs` component
- [Column](column.md) - Common content layout
- [Card](card.md) - Alternative container
