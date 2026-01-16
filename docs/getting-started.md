# Getting Started

This guide covers installing a2ui-4k and rendering your first A2UI surface.

> *a2ui-4k currently implements the A2UI v0.8 specification. For protocol details, see the [A2UI specification](https://github.com/google/A2UI).*

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

val uiDefinition = UiDefinition(
    surfaceId = "default",
    root = "main-column",
    components = mapOf(
        "main-column" to Component.create(
            id = "main-column",
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
                putJsonObject("text") {
                    put("literalString", "Hello from A2UI!")
                }
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
import com.contextable.a2ui4k.model.UserActionEvent
import com.contextable.a2ui4k.model.DataChangeEvent

A2UISurface(
    definition = uiDefinition,
    catalog = CoreCatalog,
    onEvent = { event ->
        when (event) {
            is UserActionEvent -> {
                // Button clicks, form submissions, etc.
                println("Action: ${event.name} from ${event.sourceComponentId}")
            }
            is DataChangeEvent -> {
                // Text field changes, slider moves, etc.
                println("Data changed: ${event.path} = ${event.value}")
            }
        }
    }
)
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

Use `SurfaceStateManager` to process streaming A2UI operations from agents:

```kotlin
import com.contextable.a2ui4k.state.SurfaceStateManager

val stateManager = SurfaceStateManager()

// Process ACTIVITY_SNAPSHOT from agent (contains operations array)
stateManager.processSnapshot(messageId, activityContent)

// Process ACTIVITY_DELTA for incremental updates
stateManager.processDelta(messageId, jsonPatch)

// Get current UI definition for a surface
val currentDefinition = stateManager.getSurface("surface-id")
```

## Next Steps

- [Data Binding](core-concepts/data-binding.md) - Learn about path-based reactive data
- [Events](core-concepts/events.md) - Understand event handling in depth
- [Widget Reference](widgets/index.md) - Explore available widgets
- [A2UI Specification](https://github.com/google/A2UI) - Protocol details
