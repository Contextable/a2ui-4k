# Getting Started

This guide covers installing a2ui-4k and rendering your first A2UI surface.

> *a2ui-4k implements the A2UI v0.9 specification natively, with transparent
> backwards-compatible support for v0.8 surfaces. For protocol details, see the
> [A2UI specification](https://github.com/google/A2UI).*

## Installation

### Gradle (Kotlin DSL)

Add the dependency to your `build.gradle.kts`:

```kotlin
// For Kotlin Multiplatform projects
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.contextable:a2ui-4k:<version>")
        }
    }
}

// For Android-only projects
dependencies {
    implementation("com.contextable:a2ui-4k-android:<version>")
}

// For JVM/Desktop projects
dependencies {
    implementation("com.contextable:a2ui-4k-jvm:<version>")
}
```

### Repository

Until published to Maven Central, use `mavenLocal()` after running `./gradlew publishToMavenLocal`:

```kotlin
repositories {
    mavenLocal()
    // ... other repositories
}
```

## Basic Usage

### 1. Create a UiDefinition

A `UiDefinition` represents the complete UI state, typically received from an A2UI-compatible agent:

```kotlin
import com.contextable.a2ui4k.model.Component
import com.contextable.a2ui4k.model.UiDefinition
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

// In A2UI v0.9 the root is identified by convention: the component with id "root".
// (For v0.8 surfaces the explicit `rootComponentId` field is used instead.)
val uiDefinition = UiDefinition(
    surfaceId = "default",
    components = mapOf(
        "root" to Component.create(
            id = "root",
            widgetType = "Column",
            data = buildJsonObject {
                putJsonObject("children") {
                    putJsonArray("explicitList") {
                        add(buildJsonObject { put("componentId", "greeting") })
                    }
                }
            }
        ),
        "greeting" to Component.create(
            id = "greeting",
            widgetType = "Text",
            data = buildJsonObject {
                put("text", "Hello from A2UI!")
            }
        )
    )
)
```

### 2. Render with A2UISurface

Use the `A2UISurface` composable to render the UI:

```kotlin
import androidx.compose.runtime.Composable
import com.contextable.a2ui4k.catalog.CoreCatalog
import com.contextable.a2ui4k.render.A2UISurface

@Composable
fun MyScreen() {
    A2UISurface(
        definition = uiDefinition,
        catalog = CoreCatalog,
        onEvent = { event ->
            // Handle user interactions
            println("Event: $event")
        }
    )
}
```

### 3. Handle Events

The `onEvent` callback receives `UiEvent` instances when users interact with the UI:

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
            is ActionEvent -> {
                // Button clicks, form submissions, etc.
                println("Action: ${event.name} from ${event.sourceComponentId}")
            }
            is DataChangeEvent -> {
                // Text field changes, slider moves, etc.
                // (Local-only event; v0.9 has no upstream data-change wire message.)
                println("Data changed: ${event.path} = ${event.value}")
            }
            is ValidationError -> {
                // Client-side validation failure (CheckRule or schema)
                println("Validation: ${event.path} — ${event.message}")
            }
            is ClientError -> {
                // Any other client-side error code
                println("Error ${event.code}: ${event.message}")
            }
        }
    }
)
```

To send any of these events upstream as a wire-ready A2UI envelope, use
`event.toClientMessage()` (defaults to v0.9; pass `ProtocolVersion.V0_8` to
serialize for a v0.8 surface):

```kotlin
import com.contextable.a2ui4k.model.toClientMessage

val envelope = event.toClientMessage()  // JsonObject? — null when no wire shape applies
```

## Using with DataModel

For dynamic data binding, use `DataModel` to manage reactive state:

```kotlin
import com.contextable.a2ui4k.data.rememberDataModel
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

@Composable
fun DynamicScreen() {
    val dataModel = rememberDataModel(
        initialData = buildJsonObject {
            putJsonObject("user") {
                put("name", "Alice")
                put("email", "alice@example.com")
            }
        }
    )

    A2UISurface(
        definition = uiDefinition,
        catalog = CoreCatalog,
        dataModel = dataModel,
        onEvent = { event ->
            if (event is DataChangeEvent) {
                // DataModel updates automatically for bound fields
            }
        }
    )
}
```

## Processing Agent Responses

Use `SurfaceStateManager` to process streaming A2UI operations from agents.
v0.9 sends one operation per JSON object (`createSurface`, `updateComponents`,
`updateDataModel`, `deleteSurface`), each wrapped in a `version` envelope:

```kotlin
import com.contextable.a2ui4k.state.SurfaceStateManager
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

val stateManager = SurfaceStateManager()

// One v0.9 message — exactly one op key alongside "version"
val message = buildJsonObject {
    put("version", "v0.9")
    putJsonObject("createSurface") {
        put("surfaceId", "default")
        // …operation payload…
    }
}
stateManager.processMessage(message)

// Get current UI definition for a surface
val currentDefinition = stateManager.getSurface("default")
```

The same `processMessage` call also accepts v0.8 `ACTIVITY_SNAPSHOT` and
`ACTIVITY_DELTA` envelopes — they are transcoded to the v0.9 shape internally
and surfaces created this way are tagged so outbound events serialize in the
matching wire format.

## Next Steps

- [Data Binding](core-concepts/data-binding.md) - Learn about path-based reactive data
- [Events](core-concepts/events.md) - Understand event handling in depth
- [Widget Reference](widgets/index.md) - Explore available widgets
- [A2UI Specification](https://github.com/google/A2UI) - Protocol details
