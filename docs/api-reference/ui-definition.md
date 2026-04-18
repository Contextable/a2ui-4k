# UiDefinition

Container for the complete UI state, including all components and the root
reference.

> *a2ui-4k implements A2UI **v0.9** with backwards-compatible support for v0.8
> (see [Deprecated Protocol Versions](../protocol/deprecated-versions.md)).*

## Definition

```kotlin
@Serializable
data class UiDefinition(
    val surfaceId: String,
    val components: Map<String, Component> = emptyMap(),
    val catalogId: String? = null,
    val theme: JsonObject? = null,
    val sendDataModel: Boolean = false,
    val rootComponentId: String? = null,
    val protocolVersion: ProtocolVersion = ProtocolVersion.V0_9
) {
    val rootComponent: Component?
        get() = rootComponentId?.let { components[it] } ?: components["root"]
}
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `surfaceId` | `String` | Unique surface identifier |
| `components` | `Map<String, Component>` | All components on this surface, by ID |
| `catalogId` | `String?` | Optional catalog identifier |
| `theme` | `JsonObject?` | Optional theme parameters from `createSurface` |
| `sendDataModel` | `Boolean` | When true, the client emits `a2uiClientDataModel` metadata for this surface |
| `rootComponentId` | `String?` | Explicit root id (v0.8). When `null`, the component with id `"root"` is the surface root (v0.9 convention). |
| `protocolVersion` | `ProtocolVersion` | Wire version this surface was created under; drives outbound event serialization. |

`rootComponent` resolves the root: it returns `components[rootComponentId]`
when set, otherwise `components["root"]`.

## Usage

### Creating from Components

```kotlin
import com.contextable.a2ui4k.model.Component
import com.contextable.a2ui4k.model.UiDefinition
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

val uiDefinition = UiDefinition(
    surfaceId = "default",
    components = mapOf(
        "root" to Component.create(
            id = "root",
            widgetType = "Column",
            data = buildJsonObject {
                putJsonObject("children") {
                    putJsonArray("explicitList") {
                        add(buildJsonObject { put("componentId", "title") })
                    }
                }
            }
        ),
        "title" to Component.create(
            id = "title",
            widgetType = "Text",
            data = buildJsonObject { put("text", "Hello!") }
        )
    )
)
```

### From SurfaceStateManager

```kotlin
val stateManager = SurfaceStateManager()

// Process v0.9 envelopes (or v0.8 envelopes — they are transcoded internally)
stateManager.processMessage(createSurfaceMessage)
stateManager.processMessage(updateComponentsMessage)

// Get the current definition
val definition: UiDefinition? = stateManager.getSurface("default")
```

### Accessing Components

```kotlin
// Get a specific component
val component = uiDefinition.components["title"]

// Check if a component exists
if ("modal-content" in uiDefinition.components) {
    // ...
}

// Walk from the root
val root = uiDefinition.rootComponent
```

## Protocol Mapping

`UiDefinition` is constructed from A2UI v0.9 operations:

| Operation | Effect on UiDefinition |
|-----------|----------------------|
| `createSurface` | Creates the entry; sets `catalogId`, `theme`, `sendDataModel` |
| `updateComponents` | Adds/updates entries in `components` |
| `updateDataModel` | Updates the surface's `DataModel` (no `UiDefinition` change) |
| `deleteSurface` | Removes the entire definition |

For v0.8 surfaces, the equivalent `beginRendering` / `surfaceUpdate` /
`dataModelUpdate` / `deleteSurface` operations are transcoded into the v0.9
shape before reaching this model. See
[Deprecated Protocol Versions](../protocol/deprecated-versions.md).

## Related

- [Component](component.md) — Individual component model
- [A2UISurface](a2ui-surface.md) — Renders `UiDefinition`
- [State Management](../core-concepts/state-management.md) — Building definitions from operations
