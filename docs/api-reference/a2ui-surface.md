# A2UISurface

The main composable for rendering A2UI definitions.

> *a2ui-4k currently implements the A2UI v0.8 specification. The A2UI protocol is under active development.*

## Signature

```kotlin
@Composable
fun A2UISurface(
    definition: UiDefinition,
    modifier: Modifier = Modifier,
    dataModel: DataModel = rememberDataModel(),
    catalog: Catalog,
    onEvent: (UiEvent) -> Unit = {}
)
```

## Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `definition` | `UiDefinition` | The UI definition to render |
| `modifier` | `Modifier` | Compose modifier for the surface |
| `dataModel` | `DataModel` | Data store for path bindings (defaults to empty) |
| `catalog` | `Catalog` | Widget catalog defining available components |
| `onEvent` | `(UiEvent) -> Unit` | Callback for user interaction events |

## Usage

### Basic Usage

```kotlin
import com.contextable.a2ui4k.catalog.CoreCatalog
import com.contextable.a2ui4k.render.A2UISurface

@Composable
fun MyScreen(uiDefinition: UiDefinition) {
    A2UISurface(
        definition = uiDefinition,
        catalog = CoreCatalog,
        onEvent = { event ->
            println("Received event: $event")
        }
    )
}
```

### With DataModel

```kotlin
import com.contextable.a2ui4k.data.rememberDataModel

@Composable
fun DynamicScreen(uiDefinition: UiDefinition) {
    val dataModel = rememberDataModel(
        initialData = mapOf(
            "user" to mapOf("name" to "Alice")
        )
    )

    A2UISurface(
        definition = uiDefinition,
        catalog = CoreCatalog,
        dataModel = dataModel,
        onEvent = { event ->
            when (event) {
                is UserActionEvent -> sendToAgent(event)
                is DataChangeEvent -> { /* auto-handled by DataModel */ }
            }
        }
    )
}
```

### With Custom Catalog

```kotlin
val customCatalog = CoreCatalog + Catalog.of(
    id = "custom",
    MyCustomWidget
)

A2UISurface(
    definition = uiDefinition,
    catalog = customCatalog,
    onEvent = { /* ... */ }
)
```

## Behavior

1. **Rendering:** Starts from `definition.root` and recursively renders the component tree
2. **Data Binding:** Resolves path-bound properties using the `dataModel`
3. **Events:** Bubbles `UiEvent` instances up via `onEvent` callback
4. **Recomposition:** Automatically recomposes when `definition` or `dataModel` changes

## Related

- [UiDefinition](ui-definition.md) - UI state container
- [DataModel](data-model.md) - Reactive data store
- [CatalogItem](catalog-item.md) - Widget interface
- [Getting Started](../getting-started.md) - Usage examples
