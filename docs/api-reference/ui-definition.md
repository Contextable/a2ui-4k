# UiDefinition

Container for the complete UI state, including all components and the root reference.

> *a2ui-4k currently implements the A2UI v0.8 specification. The A2UI protocol is under active development.*

## Definition

```kotlin
data class UiDefinition(
    val root: String,
    val components: Map<String, Component>,
    val catalogId: String? = null
)
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `root` | `String` | ID of the root component to render |
| `components` | `Map<String, Component>` | All components by ID |
| `catalogId` | `String?` | Optional catalog identifier |

## Usage

### Creating from Components

```kotlin
val uiDefinition = UiDefinition(
    root = "main-column",
    components = mapOf(
        "main-column" to Component(
            id = "main-column",
            componentType = "Column",
            properties = mapOf(
                "children" to mapOf(
                    "explicitList" to listOf(
                        mapOf("componentId" to "title")
                    )
                )
            )
        ),
        "title" to Component(
            id = "title",
            componentType = "Text",
            properties = mapOf(
                "text" to mapOf("literalString" to "Hello!")
            )
        )
    )
)
```

### From SurfaceStateManager

```kotlin
val stateManager = SurfaceStateManager()

// Process operations from agent
stateManager.processOperation(beginRenderingOp)
stateManager.processOperation(surfaceUpdateOp)

// Get the current definition
val definition: UiDefinition? = stateManager.getDefinition("surface-id")
```

### Accessing Components

```kotlin
// Get a specific component
val component = uiDefinition.components["button-1"]

// Check if component exists
if ("modal-content" in uiDefinition.components) {
    // ...
}
```

## Protocol Mapping

`UiDefinition` is constructed from A2UI operations:

| Operation | Effect on UiDefinition |
|-----------|----------------------|
| `beginRendering` | Sets `root` and optional `catalogId` |
| `surfaceUpdate` | Adds/updates entries in `components` |
| `deleteSurface` | Removes the entire definition |

## Related

- [Component](component.md) - Individual component model
- [A2UISurface](a2ui-surface.md) - Renders UiDefinition
- [State Management](../core-concepts/state-management.md) - Building definitions from operations
