# Events

a2ui-4k emits events when users interact with the UI, allowing your application to respond to actions, validation failures, and data mutations.

> *For the canonical event specification, see the
> [A2UI v0.9 protocol documentation](https://github.com/google/A2UI/tree/main/specification/0.9).*

## Event Types

A2UI v0.9 defines **two upstream wire messages** — `action` and `error` — and
a2ui-4k models them with four `UiEvent` subclasses (`DataChangeEvent` is
local-only):

| Class                 | Wire? | Purpose                                            |
| --------------------- | ----- | -------------------------------------------------- |
| `ActionEvent`         | ✓     | User interaction (button click, form submit, …)    |
| `ValidationError`     | ✓     | `code == "VALIDATION_FAILED"` from a `CheckRule`   |
| `ClientError`         | ✓     | Any other client-side error code                   |
| `DataChangeEvent`     | —     | Local-only mirror of input changes for the data model |

### ActionEvent

Triggered by interactive components like buttons:

```kotlin
data class ActionEvent(
    val name: String,              // Action name (e.g., "submit", "click")
    val surfaceId: String,         // Surface that emitted the event
    val sourceComponentId: String, // Component ID (with template suffix if applicable)
    val timestamp: String,         // ISO 8601 timestamp
    val context: JsonObject? = null
)
```

### ValidationError

Emitted when a `CheckRule` fails (e.g., a required field is empty):

```kotlin
data class ValidationError(
    val surfaceId: String,
    val path: String,    // JSON Pointer into the offending value
    val message: String  // Rule's user-visible message
) {
    val code: String = "VALIDATION_FAILED"
}
```

### ClientError

Anything else the client wants to surface — catalog miss, render fault, etc.
The `code` may not be `"VALIDATION_FAILED"` (use `ValidationError` for that):

```kotlin
data class ClientError(
    val code: String,
    val surfaceId: String,
    val message: String
)
```

### DataChangeEvent

**Local only** — A2UI v0.9 has no upstream wire shape for individual data
mutations. Input widgets emit this so your application can mirror the change
into the local `DataModel`. (If your `createSurface` set
`sendDataModel = true`, the full data model is included in metadata on the
*next* `action` envelope as `a2uiClientDataModel`.)

```kotlin
data class DataChangeEvent(
    val surfaceId: String,
    val path: String,    // JSON Pointer that changed
    val value: String    // New value, stringified
)
```

## Handling Events

```kotlin
import com.contextable.a2ui4k.model.ActionEvent
import com.contextable.a2ui4k.model.ClientError
import com.contextable.a2ui4k.model.DataChangeEvent
import com.contextable.a2ui4k.model.ValidationError

A2UISurface(
    definition = uiDefinition,
    catalog = CoreCatalog,
    onEvent = { event ->
        when (event) {
            is ActionEvent      -> handleAction(event)
            is ValidationError  -> showInlineValidation(event)
            is ClientError      -> reportError(event)
            is DataChangeEvent  -> handleDataChange(event)
        }
    }
)
```

## Sending Events Upstream

Use `toClientMessage()` to obtain the wire-ready envelope:

```kotlin
import com.contextable.a2ui4k.model.toClientMessage

onEvent = { event ->
    val envelope = event.toClientMessage() ?: return@onEvent  // local-only events return null
    sendToAgent(envelope)  // your transport
}
```

Wire shapes (v0.9):

```json
// ActionEvent
{
  "version": "v0.9",
  "action": {
    "name": "submit-form",
    "surfaceId": "default",
    "sourceComponentId": "submit-btn",
    "timestamp": "2026-04-18T10:00:00Z",
    "context": { "formId": "contact-form" }
  }
}

// ValidationError
{
  "version": "v0.9",
  "error": {
    "code": "VALIDATION_FAILED",
    "surfaceId": "default",
    "path": "/user/email",
    "message": "Email address required"
  }
}

// ClientError
{
  "version": "v0.9",
  "error": {
    "code": "CATALOG_MISSING",
    "surfaceId": "default",
    "message": "Catalog 'my-app/v1' not found"
  }
}
```

## Button Actions

`Button.action` is a v0.9 action object — `name` plus optional `context`. The
context is resolved against the data model before being placed on the
`ActionEvent`:

```json
{
  "id": "submit-btn",
  "component": "Button",
  "child": "submit-label",
  "action": {
    "name": "submit-form",
    "context": {
      "formId": "contact-form",
      "userId": { "path": "/user/id" }
    }
  }
}
```

When clicked, this produces:

```kotlin
ActionEvent(
    name = "submit-form",
    surfaceId = "default",
    sourceComponentId = "submit-btn",
    timestamp = "2026-04-18T10:00:00Z",
    context = buildJsonObject {
        put("formId", "contact-form")
        put("userId", "user-123")  // Resolved from DataModel
    }
)
```

## Template Item Events

For components inside `template` `children`, `sourceComponentId` carries the
item key suffix so you can tell which row fired the action:

```
sourceComponentId = "add-btn:item-0"
```

## Sending v0.8 surfaces upstream

If a surface was created from a v0.8 `ACTIVITY_SNAPSHOT`, its
`protocolVersion` is `V0_8` and the v0.9 envelope is **not** what the agent
expects. Pass the version explicitly:

```kotlin
import com.contextable.a2ui4k.model.ProtocolVersion

val v = stateManager.getSurfaceProtocolVersion(event.surfaceId) ?: ProtocolVersion.V0_9
val envelope = event.toClientMessage(v) ?: return
sendToAgent(envelope)
```

In v0.8 mode:

- `ActionEvent` → `{"userAction":{…}}` (no `version` envelope)
- `DataChangeEvent` → `{"dataChange":{…}}` (a real wire message under v0.8)
- `ValidationError` / `ClientError` → `null` (v0.8 has no formal error shape;
  log locally)

See [Deprecated Protocol Versions](../protocol/deprecated-versions.md).

## See Also

- [A2UI v0.9 specification](https://github.com/google/A2UI/tree/main/specification/0.9)
- [Button Widget](../widgets/button.md)
- [Data Binding](data-binding.md)
- [State Management](state-management.md)
