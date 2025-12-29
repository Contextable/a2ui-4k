# Modal

A dialog overlay triggered by an entry point component.

> **A2UI Spec:** See `Modal` in the [Standard Component Catalog v0.8](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json).
>
> *a2ui-4k currently implements the v0.8 specification. The A2UI protocol is under active development.*

## Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `entryPointChild` | ComponentRef | Yes | Component that triggers the modal when clicked |
| `contentChild` | ComponentRef | Yes | Component displayed inside the modal |

## a2ui-4k Implementation

- **Trigger:** Clicking the entry point component opens the modal
- **Dismiss:** Modal can be dismissed by clicking outside or a close action
- **Overlay:** Uses Material dialog with scrim background

## Example

```json
{
  "id": "info-modal",
  "component": "Modal",
  "properties": {
    "entryPointChild": { "componentId": "info-button" },
    "contentChild": { "componentId": "info-content" }
  }
}
```

Entry point (trigger):

```json
{
  "id": "info-button",
  "component": "Button",
  "properties": {
    "child": { "componentId": "info-button-label" },
    "action": { "name": "show-info" }
  }
}
```

Modal content:

```json
{
  "id": "info-content",
  "component": "Column",
  "properties": {
    "children": {
      "explicitList": [
        { "componentId": "modal-title" },
        { "componentId": "modal-body" },
        { "componentId": "close-button" }
      ]
    }
  }
}
```

## Platform Notes

- Uses Compose `Dialog` on all platforms
- Entry point renders inline; content renders in overlay when triggered

## See Also

- [A2UI Standard Catalog](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json) - `Modal` component
- [Button](button.md) - Common entry point
- [Card](card.md) - Common content container
