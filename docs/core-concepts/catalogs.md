# Catalogs

Catalogs define the available widgets that a2ui-4k can render. The library provides a standard catalog implementing all A2UI v0.8 widgets, and supports custom widget development.

> *For complete catalog specification, see [A2UI Component Model and Catalogs](https://deepwiki.com/google/A2UI#3.2).*

## CoreCatalog

a2ui-4k provides `CoreCatalog` with all 18 standard A2UI v0.8 widgets:

```kotlin
import com.contextable.a2ui4k.catalog.CoreCatalog

A2UISurface(
    definition = uiDefinition,
    catalog = CoreCatalog,
    onEvent = { /* ... */ }
)
```

### Included Widgets

| Category | Widgets |
|----------|---------|
| Basic Content | Text, Image, Icon, Divider, Video, AudioPlayer |
| Layout | Row, Column, List, Card, Tabs, Modal |
| Interactive | Button, TextField, CheckBox, Slider, MultipleChoice, DateTimeInput |

## CatalogItem Interface

Each widget is defined by a `CatalogItem`:

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

### Parameters

- **component** - The component instance with id, type, and properties
- **dataContext** - Access to bound data values
- **renderChild** - Function to render child components by ID
- **onEvent** - Callback to emit user events

## Creating Custom Widgets

Implement `CatalogItem` to create custom widgets:

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
        // Get properties using DataReferenceParser
        val title = component.getStringProperty("title", dataContext)

        // Render your custom UI
        Column {
            Text(text = title ?: "Default")
            // Render a child component if specified
            component.getChildReference("content")?.let { childId ->
                renderChild(childId)
            }
        }
    }
}
```

## Combining Catalogs

Use the `+` operator to combine catalogs:

```kotlin
val myCatalog = CoreCatalog + Catalog.of(
    id = "custom",
    MyCustomWidget,
    AnotherWidget
)

A2UISurface(
    definition = uiDefinition,
    catalog = myCatalog,
    onEvent = { /* ... */ }
)
```

Later catalogs override earlier ones if widget names conflict.

## Catalog.of Factory

Create catalogs using the factory method:

```kotlin
val customCatalog = Catalog.of(
    id = "my-catalog",
    MyWidget1,
    MyWidget2,
    MyWidget3
)
```

## Property Access

Use `DataReferenceParser` helpers to read component properties:

```kotlin
// String property (literal or path-bound)
val text = component.getStringProperty("text", dataContext)

// Number property
val count = component.getNumberProperty("count", dataContext)

// Boolean property
val enabled = component.getBooleanProperty("enabled", dataContext)

// Child component reference
val childId = component.getChildReference("child")

// Children list
val children = component.getChildrenReference("children")
```

## See Also

- [A2UI Spec: Component Model](https://deepwiki.com/google/A2UI#3.2)
- [CatalogItem API Reference](../api-reference/catalog-item.md)
- [Widget Reference](../widgets/index.md)
