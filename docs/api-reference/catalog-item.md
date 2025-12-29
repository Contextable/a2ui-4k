# CatalogItem

Interface for defining renderable widgets.

> *a2ui-4k currently implements the A2UI v0.8 specification. The A2UI protocol is under active development.*

## Definition

```kotlin
interface CatalogItem {
    val name: String

    @Composable
    fun Render(
        component: Component,
        dataContext: DataContext,
        renderChild: @Composable (String) -> Unit,
        onEvent: (UiEvent) -> Unit
    )
}
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `name` | `String` | Widget type name (e.g., "Text", "Button") |

## Render Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `component` | `Component` | The component instance to render |
| `dataContext` | `DataContext` | Access to bound data values |
| `renderChild` | `(String) -> Unit` | Function to render child components by ID |
| `onEvent` | `(UiEvent) -> Unit` | Callback to emit user events |

## Implementing a Custom Widget

```kotlin
object MyCustomWidget : CatalogItem {
    override val name = "MyCustom"

    @Composable
    override fun Render(
        component: Component,
        dataContext: DataContext,
        renderChild: @Composable (String) -> Unit,
        onEvent: (UiEvent) -> Unit
    ) {
        // 1. Read properties
        val title = component.getStringProperty("title", dataContext) ?: ""
        val showBorder = component.getBooleanProperty("showBorder", dataContext) ?: false

        // 2. Render UI
        Column(
            modifier = if (showBorder) {
                Modifier.border(1.dp, Color.Gray)
            } else {
                Modifier
            }
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)

            // 3. Render children
            component.getChildReference("content")?.let { childId ->
                renderChild(childId)
            }
        }

        // 4. Handle user interactions
        Button(onClick = {
            onEvent(UserActionEvent(
                name = "custom-action",
                surfaceId = "default",
                sourceComponentId = component.id,
                timestamp = Clock.System.now().toString()
            ))
        }) {
            Text("Click Me")
        }
    }
}
```

## DataContext

`DataContext` provides access to data values:

```kotlin
interface DataContext {
    fun getString(path: String): String?
    fun getNumber(path: String): Double?
    fun getBoolean(path: String): Boolean?
    fun getArraySize(path: String): Int
    fun withBasePath(basePath: String): DataContext
}
```

### Scoped Data Context

For template rendering, use `withBasePath` to scope data access:

```kotlin
// In a List template, each item gets a scoped context
val scopedContext = dataContext.withBasePath("/items/0")
// Now "/name" resolves to "/items/0/name"
```

## Creating a Catalog

```kotlin
val myCatalog = Catalog.of(
    id = "my-widgets",
    MyCustomWidget,
    AnotherWidget
)

// Combine with CoreCatalog
val fullCatalog = CoreCatalog + myCatalog
```

## Related

- [Catalogs](../core-concepts/catalogs.md) - Conceptual guide
- [Component](component.md) - Component model
- [Widget Reference](../widgets/index.md) - Standard widgets
