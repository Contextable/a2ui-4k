# Modal

A dialog overlay triggered by an entry point component.

> **A2UI Spec:** See `Modal` in the [A2UI v0.9 Standard Component Catalog](https://github.com/google/A2UI/tree/main/specification/0.9).
>
> *a2ui-4k implements A2UI **v0.9** with backwards-compatible support for v0.8 (see [Deprecated Protocol Versions](../protocol/deprecated-versions.md)).*

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

- [A2UI v0.9 Standard Catalog](https://github.com/google/A2UI/tree/main/specification/0.9) - `Modal` component
- [Button](button.md) - Common entry point
- [Card](card.md) - Common content container
