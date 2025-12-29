# DataModel

Reactive data store for path-based data binding.

> *a2ui-4k currently implements the A2UI v0.8 specification. The A2UI protocol is under active development.*

## Definition

```kotlin
class DataModel {
    fun getString(path: String): String?
    fun getNumber(path: String): Double?
    fun getBoolean(path: String): Boolean?
    fun getArraySize(path: String): Int
    fun getObjectKeys(path: String): List<String>
    fun update(path: String, value: Any?)
    fun observeString(path: String): StateFlow<String?>
    fun observeNumber(path: String): StateFlow<Double?>
    fun observeBoolean(path: String): StateFlow<Boolean?>
}
```

## Creating a DataModel

### In Compose

```kotlin
import com.contextable.a2ui4k.data.rememberDataModel

@Composable
fun MyScreen() {
    // Empty DataModel
    val dataModel = rememberDataModel()

    // With initial data
    val dataModel = rememberDataModel(
        initialData = mapOf(
            "user" to mapOf(
                "name" to "Alice",
                "age" to 30
            ),
            "items" to listOf("Apple", "Banana")
        )
    )
}
```

### Standalone

```kotlin
import com.contextable.a2ui4k.data.DataModel

val dataModel = DataModel()
dataModel.update("/user/name", "Alice")
```

## Methods

### Reading Data

```kotlin
// Get values by path
val name = dataModel.getString("/user/name")      // "Alice"
val age = dataModel.getNumber("/user/age")        // 30.0
val active = dataModel.getBoolean("/user/active") // true/false/null

// Get collection info
val itemCount = dataModel.getArraySize("/items")  // 2
val keys = dataModel.getObjectKeys("/user")       // ["name", "age"]
```

### Writing Data

```kotlin
// Update values - triggers UI recomposition
dataModel.update("/user/name", "Bob")
dataModel.update("/user/age", 31)
dataModel.update("/settings/darkMode", true)
```

### Observing Changes

```kotlin
// Get reactive StateFlow for path
val nameFlow: StateFlow<String?> = dataModel.observeString("/user/name")

// Use in Compose
@Composable
fun UserDisplay(dataModel: DataModel) {
    val name by dataModel.observeString("/user/name").collectAsState()
    Text(text = name ?: "Unknown")
}
```

## Path Syntax

Paths follow JSON Pointer (RFC 6901) syntax:

| Path | Description |
|------|-------------|
| `/user` | Root-level "user" key |
| `/user/name` | Nested property |
| `/items/0` | Array element by index |
| `/items/0/name` | Property within array element |

## Integration with A2UISurface

```kotlin
A2UISurface(
    definition = uiDefinition,
    catalog = CoreCatalog,
    dataModel = dataModel,  // Pass DataModel here
    onEvent = { event ->
        // DataChangeEvents automatically update dataModel
        // for two-way bound components
    }
)
```

## Related

- [Data Binding](../core-concepts/data-binding.md) - Conceptual guide
- [A2UISurface](a2ui-surface.md) - Uses DataModel for rendering
- [State Management](../core-concepts/state-management.md) - DataModelUpdate operations
