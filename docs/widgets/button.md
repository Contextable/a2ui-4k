# Button

A clickable element that triggers a `UserActionEvent` when pressed.

> **A2UI Spec:** See `Button` in the [Standard Component Catalog v0.8](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json).
>
> *a2ui-4k currently implements the v0.8 specification. The A2UI protocol is under active development.*

## Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `child` | ComponentRef | Yes | Component ID for button content |
| `action` | Action | Yes | Action definition with name and optional context |
| `primary` | boolean | No | Whether to use primary button styling |

### Action Object

```json
{
  "name": "action-name",
  "context": {
    "key1": { "literalString": "value" },
    "key2": { "path": "/data/path" }
  }
}
```

## a2ui-4k Implementation

- **Event emission:** Clicking emits `UserActionEvent` with resolved context
- **Context resolution:** Path-bound context values are resolved at click time
- **Styling:** `primary=true` uses Material `FilledButton`, otherwise `OutlinedButton`

## Example

Basic button:

```json
{
  "id": "submit-btn",
  "component": "Button",
  "properties": {
    "child": { "componentId": "submit-label" },
    "action": {
      "name": "submit"
    },
    "primary": true
  }
}
```

With context:

```json
{
  "id": "add-to-cart",
  "component": "Button",
  "properties": {
    "child": { "componentId": "add-label" },
    "action": {
      "name": "add-item",
      "context": {
        "productId": { "path": "/product/id" },
        "quantity": { "literalNumber": 1 }
      }
    }
  }
}
```

## Event Output

When clicked, the button emits:

```kotlin
UserActionEvent(
    name = "add-item",
    surfaceId = "default",
    sourceComponentId = "add-to-cart",
    timestamp = "2025-01-15T10:30:00.000Z",
    context = JsonObject(mapOf(
        "productId" to JsonPrimitive("prod-123"),
        "quantity" to JsonPrimitive(1)
    ))
)
```

## Template Context

Buttons inside List templates include the item key in `sourceComponentId`:

```kotlin
sourceComponentId = "add-to-cart:item-2"  // Button in third list item
```

## Platform Notes

- Uses Material 3 button components on all platforms
- Touch feedback provided by Compose Multiplatform

## See Also

- [A2UI Standard Catalog](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json) - `Button` component
- [Events](../core-concepts/events.md)
- [Text](text.md) - Common child for button labels
