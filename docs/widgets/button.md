# Button

A clickable element that triggers an `ActionEvent` (or a `FunctionCall`) when
pressed.

> **A2UI Spec:** See `Button` in the
> [A2UI v0.9 Standard Component Catalog](https://github.com/google/A2UI/tree/main/specification/0.9).
>
> *a2ui-4k implements A2UI **v0.9** with backwards-compatible support for v0.8
> (see [Deprecated Protocol Versions](../protocol/deprecated-versions.md)).*

## Properties (v0.9)

| Property         | Type                | Required | Description |
|------------------|---------------------|----------|-------------|
| `child`          | `ComponentRef`      | Yes      | Component ID for button content (typically `Text` or `Icon`) |
| `action`         | `Action`            | Yes      | Action definition: `{"event": {…}}` or `{"functionCall": {…}}` |
| `variant`        | string              | No       | `"default"` (filled), `"primary"` (filled+tonal), `"borderless"` (text/outlined) |
| `checks`         | `CheckRule[]`       | No       | When any check fails, the button is disabled |
| `accessibility`  | `Accessibility`     | No       | `label` / `description` for screen readers |

### Action Object

```json
{
  "event": {
    "name": "submit",
    "context": {
      "key1": "value",
      "key2": { "path": "/data/path" }
    }
  }
}
```

A2UI v0.9 also supports a `functionCall` form for actions that should be
resolved by the function evaluator instead of bubbling up as an `ActionEvent`.

## a2ui-4k Implementation

- **Event emission:** Clicking emits `ActionEvent` with the resolved
  `context` (path bindings resolved at click time).
- **Variants:** `variant` maps to Material 3 `FilledButton` (`default`),
  `FilledTonalButton` (`primary`), or `TextButton` (`borderless`).
- **Validation:** `checks` are evaluated through `FunctionEvaluator`. Any
  failure disables the button until the underlying data changes.
- **Accessibility:** `accessibility.label` is exposed as the button's content
  description; `accessibility.description` becomes its on-click hint.

### v0.8 backwards-compatible aliases

For v0.8 surfaces (or hand-written legacy JSON) the widget also accepts:

| v0.8 alias  | v0.9 equivalent | Notes |
|-------------|-----------------|-------|
| `label`     | `child`         | When `child` is omitted, `label` is rendered as the button's text |
| `usageHint` | `variant`       | Read with the same semantics |
| `primary` (boolean) | `variant: "primary"` | Legacy boolean still recognized for inputs from older agents |

## Example

Basic button (v0.9):

```json
{
  "id": "submit-btn",
  "component": "Button",
  "child": "submit-label",
  "variant": "primary",
  "action": {
    "event": { "name": "submit" }
  }
}
```

With context and a validation check:

```json
{
  "id": "add-to-cart",
  "component": "Button",
  "child": "add-label",
  "variant": "default",
  "action": {
    "event": {
      "name": "add-item",
      "context": {
        "productId": { "path": "/product/id" },
        "quantity": 1
      }
    }
  },
  "checks": [
    {
      "condition": { "call": "numeric", "args": { "spec": { "min": 1 } } },
      "message": "Choose a quantity"
    }
  ]
}
```

## Event Output

When clicked, the button emits:

```kotlin
ActionEvent(
    name = "add-item",
    surfaceId = "default",
    sourceComponentId = "add-to-cart",
    timestamp = "2026-04-18T10:30:00.000Z",
    context = buildJsonObject {
        put("productId", "prod-123")
        put("quantity", 1)
    }
)
```

Use `event.toClientMessage()` to obtain the wire envelope:

```json
{
  "version": "v0.9",
  "action": {
    "name": "add-item",
    "surfaceId": "default",
    "sourceComponentId": "add-to-cart",
    "timestamp": "2026-04-18T10:30:00.000Z",
    "context": { "productId": "prod-123", "quantity": 1 }
  }
}
```

## Template Context

Buttons inside template `children` include the item key in `sourceComponentId`:

```kotlin
sourceComponentId = "add-to-cart:item-2"  // Button in third list item
```

## Platform Notes

- Uses Material 3 button components on all platforms.
- Touch feedback provided by Compose Multiplatform.

## See Also

- [A2UI v0.9 Standard Catalog](https://github.com/google/A2UI/tree/main/specification/0.9) — `Button` component
- [Events](../core-concepts/events.md)
- [Text](text.md) — Common child for button labels
