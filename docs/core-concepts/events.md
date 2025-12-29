# Events

a2ui-4k emits events when users interact with the UI, allowing your application to respond to actions and data changes.

> *For complete event specification, see [A2UI Client-to-Server Messages](https://deepwiki.com/google/A2UI#4.2).*

## Event Types

a2ui-4k currently emits two event types:

### UserActionEvent

Triggered by interactive components like buttons:

```kotlin
data class UserActionEvent(
    val name: String,              // Action name (e.g., "submit", "click")
    val surfaceId: String,         // Surface that emitted the event
    val sourceComponentId: String, // Component ID (with template suffix if applicable)
    val timestamp: String,         // ISO 8601 timestamp
    val context: JsonObject?       // Optional action context data
)
```

### DataChangeEvent

Triggered when bound data changes (e.g., text field input):

```kotlin
data class DataChangeEvent(
    val surfaceId: String,  // Surface containing the component
    val path: String,       // JSON Pointer path that changed
    val value: String       // New value as string
)
```

## Handling Events

Use the `onEvent` callback in `A2UISurface`:

```kotlin
A2UISurface(
    definition = uiDefinition,
    catalog = CoreCatalog,
    onEvent = { event ->
        when (event) {
            is UserActionEvent -> handleAction(event)
            is DataChangeEvent -> handleDataChange(event)
        }
    }
)

fun handleAction(event: UserActionEvent) {
    println("Action: ${event.name}")
    println("From: ${event.sourceComponentId}")
    println("Context: ${event.context}")
}

fun handleDataChange(event: DataChangeEvent) {
    println("Path ${event.path} changed to: ${event.value}")
}
```

## Button Actions

Buttons define an `action` property that generates `UserActionEvent`:

```json
{
  "component": "Button",
  "properties": {
    "child": { "componentId": "button-label" },
    "action": {
      "name": "submit-form",
      "context": {
        "formId": { "literalString": "contact-form" },
        "userId": { "path": "/user/id" }
      }
    }
  }
}
```

When clicked, generates:

```kotlin
UserActionEvent(
    name = "submit-form",
    sourceComponentId = "my-button",
    context = JsonObject(mapOf(
        "formId" to JsonPrimitive("contact-form"),
        "userId" to JsonPrimitive("user-123")  // Resolved from DataModel
    ))
)
```

## Template Item Events

For components inside templates, `sourceComponentId` includes the item key:

```kotlin
// Button with id="add-btn" inside a List template
sourceComponentId = "add-btn:item-0"  // Suffix indicates which item
```

This allows you to identify which list item triggered the action.

## Sending Events to Agents

Events should be serialized and sent back to A2UI-compatible agents:

```kotlin
import kotlinx.serialization.json.Json

onEvent = { event ->
    when (event) {
        is UserActionEvent -> {
            val json = Json.encodeToString(event)
            sendToAgent(json)  // Your agent communication
        }
        is DataChangeEvent -> {
            // Handle locally or send to agent
        }
    }
}
```

## See Also

- [A2UI Spec: Client-to-Server Messages](https://deepwiki.com/google/A2UI#4.2)
- [Button Widget](../widgets/button.md)
- [Data Binding](data-binding.md)
