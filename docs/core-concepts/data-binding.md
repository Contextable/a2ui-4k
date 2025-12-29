# Data Binding

a2ui-4k provides reactive data binding using JSON Pointer paths, allowing UI components to automatically update when underlying data changes.

> *For complete data binding specification, see [A2UI Data Binding and State Management](https://deepwiki.com/google/A2UI#3.3).*

## Overview

Data binding in a2ui-4k connects component properties to data values using two mechanisms:

1. **Literal Values** - Static values defined directly in the component
2. **Path Bindings** - Dynamic values resolved from a `DataModel` at runtime

## BoundValue Types

Component properties use "BoundValue" types that can be either literals or paths:

```json
// Literal string
{ "literalString": "Hello World" }

// Path binding - resolves from DataModel
{ "path": "/user/name" }

// Literal number
{ "literalNumber": 42 }

// Literal boolean
{ "literalBoolean": true }
```

## DataModel

The `DataModel` class provides a reactive data store accessed via JSON Pointer paths:

```kotlin
import com.contextable.a2ui4k.data.rememberDataModel

@Composable
fun MyScreen() {
    val dataModel = rememberDataModel(
        initialData = mapOf(
            "user" to mapOf(
                "name" to "Alice",
                "age" to 30
            ),
            "items" to listOf("Apple", "Banana", "Cherry")
        )
    )

    A2UISurface(
        definition = uiDefinition,
        catalog = CoreCatalog,
        dataModel = dataModel
    )
}
```

### Reading Data

```kotlin
// Get string value
val name = dataModel.getString("/user/name") // "Alice"

// Get number value
val age = dataModel.getNumber("/user/age") // 30.0

// Get array size
val count = dataModel.getArraySize("/items") // 3
```

### Updating Data

```kotlin
// Update a value - UI automatically refreshes
dataModel.update("/user/name", "Bob")
```

## Path Syntax

Paths follow JSON Pointer (RFC 6901) syntax:

| Path | Description |
|------|-------------|
| `/user/name` | Nested object property |
| `/items/0` | Array element by index |
| `/users/0/email` | Nested within array element |

## Template Data Binding

For `List` widgets with templates, data binding automatically scopes to the current item:

```json
{
  "component": "List",
  "properties": {
    "children": {
      "template": {
        "dataPath": "/items",
        "componentId": "item-template"
      }
    }
  }
}
```

Within the template, paths are relative to each item:

```json
{
  "id": "item-template",
  "component": "Text",
  "properties": {
    "text": { "path": "/name" }
  }
}
```

If `/items` contains `[{"name": "Apple"}, {"name": "Banana"}]`, the template renders twice with scoped data access.

## Two-Way Binding

Input components like `TextField` support two-way binding:

```json
{
  "component": "TextField",
  "properties": {
    "label": { "literalString": "Name" },
    "text": { "path": "/user/name" }
  }
}
```

When the user types, the `DataModel` updates automatically, and any other components bound to the same path refresh.

## See Also

- [A2UI Spec: Data Binding](https://deepwiki.com/google/A2UI#3.3)
- [DataModel API Reference](../api-reference/data-model.md)
- [Events](events.md) - Handling data change events
