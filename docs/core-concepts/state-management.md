# State Management

a2ui-4k provides `SurfaceStateManager` to process A2UI operations from agents
and maintain UI state across streaming responses.

> *For the canonical message specification, see the
> [A2UI v0.9 protocol documentation](https://github.com/google/A2UI/tree/main/specification/0.9).*

## Overview

AI agents send A2UI operations that modify the UI state. `SurfaceStateManager`
processes these operations and produces `UiDefinition` instances for
rendering.

## A2UI v0.9 Operations

A2UI v0.9 sends one operation per JSON object. Each message carries a
`version` tag plus exactly one operation key:

### createSurface

Initializes a new surface.

```json
{
  "version": "v0.9",
  "createSurface": {
    "surfaceId": "default",
    "catalogId": "https://github.com/google/A2UI/blob/main/specification/v0_9/json/standard_catalog.json",
    "theme": { /* optional */ },
    "sendDataModel": false
  }
}
```

### updateComponents

Adds or replaces components within a surface.

```json
{
  "version": "v0.9",
  "updateComponents": {
    "surfaceId": "default",
    "components": [
      {
        "id": "root",
        "component": "Column",
        "children": ["greeting", "submit-btn"]
      },
      { "id": "greeting", "component": "Text", "text": "Hello!" }
    ]
  }
}
```

### updateDataModel

Sets a value at a JSON Pointer path. Omitting `value` deletes the key (the
v0.9 way to remove data).

```json
// Set
{
  "version": "v0.9",
  "updateDataModel": {
    "surfaceId": "default",
    "path": "/user/name",
    "value": "Alice"
  }
}

// Delete
{
  "version": "v0.9",
  "updateDataModel": {
    "surfaceId": "default",
    "path": "/user/name"
  }
}
```

### deleteSurface

Removes a surface.

```json
{
  "version": "v0.9",
  "deleteSurface": { "surfaceId": "default" }
}
```

## Using SurfaceStateManager

```kotlin
import com.contextable.a2ui4k.state.SurfaceStateManager
import kotlinx.serialization.json.JsonObject

val stateManager = SurfaceStateManager()

// Process each decoded JSON message as it arrives
fun onAgentMessage(message: JsonObject) {
    val handled = stateManager.processMessage(message)
    if (handled) {
        val definition = stateManager.getSurface("default")
        if (definition != null) updateUI(definition)
    }
}
```

`processMessage` accepts both v0.9 envelopes and v0.8 `ACTIVITY_SNAPSHOT` /
`ACTIVITY_DELTA` envelopes — see [Deprecated Protocol Versions](../protocol/deprecated-versions.md).

## Streaming Integration

For streaming agent responses, process messages incrementally:

```kotlin
@Composable
fun StreamingUI() {
    val stateManager = remember { SurfaceStateManager() }
    var definition by remember { mutableStateOf<UiDefinition?>(null) }

    LaunchedEffect(Unit) {
        agentStream.collect { jsonMessage ->
            stateManager.processMessage(jsonMessage)
            definition = stateManager.getSurface("default")
        }
    }

    definition?.let { def ->
        A2UISurface(
            definition = def,
            catalog = CoreCatalog,
            onEvent = { event ->
                event.toClientMessage(
                    stateManager.getSurfaceProtocolVersion(event.surfaceId)
                        ?: ProtocolVersion.V0_9
                )?.let(::sendToAgent)
            }
        )
    }
}
```

## Multi-Surface Support

`SurfaceStateManager` can manage multiple surfaces simultaneously, each with
its own protocol version:

```kotlin
val stateManager = SurfaceStateManager()

// Each surface has its own state
val mainUI    = stateManager.getSurface("main")
val sidebarUI = stateManager.getSurface("sidebar")
val modalUI   = stateManager.getSurface("modal")

// And the protocol version it speaks
val v = stateManager.getSurfaceProtocolVersion("main")  // ProtocolVersion.V0_9
```

## DataModel Integration

`SurfaceStateManager` keeps a per-surface `DataModel`. `updateDataModel`
operations populate it automatically. When *any* surface has
`createSurface.sendDataModel == true`, you can build the v0.9
`a2uiClientDataModel` envelope to attach to outbound metadata:

```kotlin
val envelope: JsonObject? = stateManager.buildClientDataModel()
```

The envelope shape is:

```json
{
  "version": "v0.9",
  "surfaces": {
    "default": { /* current data model */ }
  }
}
```

Only surfaces with `sendDataModel = true` are included. v0.8 surfaces are
excluded — v0.8 has no equivalent envelope.

## See Also

- [A2UI v0.9 specification](https://github.com/google/A2UI/tree/main/specification/0.9)
- [Data Binding](data-binding.md)
- [Events](events.md)
- [UiDefinition API Reference](../api-reference/ui-definition.md)
- [Deprecated Protocol Versions](../protocol/deprecated-versions.md)
