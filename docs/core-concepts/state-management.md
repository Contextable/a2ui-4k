# State Management

a2ui-4k provides `SurfaceStateManager` to process A2UI operations from agents and maintain UI state across streaming responses.

> *For complete operation specification, see [A2UI Server-to-Client Messages](https://deepwiki.com/google/A2UI#4.1).*

## Overview

AI agents send A2UI operations that modify the UI state. `SurfaceStateManager` processes these operations and produces `UiDefinition` instances for rendering.

## A2UI Operations

a2ui-4k currently supports these v0.8 operations:

### BeginRendering

Initializes a new surface:

```kotlin
data class BeginRendering(
    val surfaceId: String,  // Unique surface identifier
    val root: String,       // Root component ID
    val styles: JsonObject? // Optional global styles
)
```

### SurfaceUpdate

Adds or updates components:

```kotlin
data class SurfaceUpdate(
    val surfaceId: String,
    val components: List<ComponentDef>
)
```

### DataModelUpdate

Updates data at a path:

```kotlin
data class DataModelUpdate(
    val surfaceId: String,
    val path: String,
    val contents: List<DataEntry>
)
```

### DeleteSurface

Removes a surface:

```kotlin
data class DeleteSurface(
    val surfaceId: String
)
```

## Using SurfaceStateManager

```kotlin
import com.contextable.a2ui4k.state.SurfaceStateManager

val stateManager = SurfaceStateManager()

// Process operations as they arrive from the agent
fun processAgentMessage(operation: A2UIOperation) {
    stateManager.processOperation(operation)

    // Get updated UI definition
    val definition = stateManager.getDefinition("surface-id")
    if (definition != null) {
        // Trigger recomposition with new definition
        updateUI(definition)
    }
}
```

## Streaming Integration

For streaming agent responses, process operations incrementally:

```kotlin
@Composable
fun StreamingUI() {
    val stateManager = remember { SurfaceStateManager() }
    var definition by remember { mutableStateOf<UiDefinition?>(null) }

    LaunchedEffect(Unit) {
        agentStream.collect { operation ->
            stateManager.processOperation(operation)
            definition = stateManager.getDefinition("default")
        }
    }

    definition?.let { def ->
        A2UISurface(
            definition = def,
            catalog = CoreCatalog,
            onEvent = { sendToAgent(it) }
        )
    }
}
```

## Multi-Surface Support

`SurfaceStateManager` can manage multiple surfaces simultaneously:

```kotlin
val stateManager = SurfaceStateManager()

// Each surface has its own state
val mainUI = stateManager.getDefinition("main")
val sidebarUI = stateManager.getDefinition("sidebar")
val modalUI = stateManager.getDefinition("modal")
```

## DataModel Integration

Each surface maintains its own `DataModel` for data binding:

```kotlin
// Get the DataModel for a specific surface
val dataModel = stateManager.getDataModel("surface-id")

// DataModelUpdate operations automatically update this
stateManager.processOperation(
    DataModelUpdate(
        surfaceId = "surface-id",
        path = "/user",
        contents = listOf(
            DataEntry(key = "name", valueString = "Alice")
        )
    )
)
```

## Parsing Agent Responses

Parse JSON operations from agent responses:

```kotlin
import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true }

fun parseOperation(jsonString: String): A2UIOperation? {
    val jsonObj = json.parseToJsonElement(jsonString).jsonObject
    return when (jsonObj["type"]?.jsonPrimitive?.content) {
        "beginRendering" -> json.decodeFromString<BeginRendering>(jsonString)
        "surfaceUpdate" -> json.decodeFromString<SurfaceUpdate>(jsonString)
        "dataModelUpdate" -> json.decodeFromString<DataModelUpdate>(jsonString)
        "deleteSurface" -> json.decodeFromString<DeleteSurface>(jsonString)
        else -> null
    }
}
```

## See Also

- [A2UI Spec: Server-to-Client Messages](https://deepwiki.com/google/A2UI#4.1)
- [Data Binding](data-binding.md)
- [UiDefinition API Reference](../api-reference/ui-definition.md)
