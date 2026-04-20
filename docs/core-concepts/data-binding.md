# Data Binding

a2ui-4k provides reactive data binding using JSON Pointer paths, allowing UI
components to automatically update when underlying data changes.

> *For the canonical data-binding specification, see the
> [A2UI v0.9 documentation](https://github.com/google/A2UI/tree/main/specification/0.9).*

## Overview

A2UI v0.9 collapsed the v0.8 boxed-literal types (`literalString` /
`literalNumber` / `literalBoolean`) into plain JSON values. Component
properties now resolve through three forms:

1. **Literal values** — JSON primitives are the values themselves.
2. **Path bindings** — `{"path": "/json/pointer"}` resolved from the surface's `DataModel`.
3. **Function calls** — `{"call": "fn", "args": {…}}` evaluated by the function evaluator.

## DataReference forms

```json
// Literal — the JSON value IS the value
"text": "Hello World"
"count": 42
"enabled": true

// Path binding — resolves from DataModel at render time
"text": { "path": "/user/name" }

// Function call — computed value (validation, formatting, logic)
"text": {
  "call": "formatCurrency",
  "args": {
    "value": { "path": "/total" },
    "spec": { "currency": "USD" }
  }
}
```

## DataModel

The `DataModel` class provides a reactive data store accessed via JSON Pointer
paths:

```kotlin
import com.contextable.a2ui4k.data.rememberDataModel
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

@Composable
fun MyScreen() {
    val dataModel = rememberDataModel(
        initialData = buildJsonObject {
            putJsonObject("user") {
                put("name", "Alice")
                put("age", 30)
            }
            putJsonArray("items") {
                add("Apple"); add("Banana"); add("Cherry")
            }
        }
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
val name  = dataModel.getString("/user/name")  // "Alice"
val age   = dataModel.getNumber("/user/age")   // 30.0
val count = dataModel.getArraySize("/items")   // 3
```

### Updating Data

```kotlin
dataModel.update("/user/name", "Bob")  // UI recomposes automatically

dataModel.delete("/user/name")          // v0.9: removing a key is first-class
```

The agent's equivalent is `updateDataModel` — passing a `value` sets the path,
omitting `value` deletes the key. See [State Management](state-management.md).

## Path Syntax

Paths follow JSON Pointer (RFC 6901) syntax:

| Path | Description |
|------|-------------|
| `/user/name` | Nested object property |
| `/items/0` | Array element by index |
| `/users/0/email` | Nested within array element |

## Template Data Binding

For `List` widgets with templates, data binding automatically scopes to the
current item:

```json
{
  "id": "products-list",
  "component": "List",
  "children": {
    "template": {
      "path": "/items",
      "componentId": "item-template"
    }
  }
}
```

Within the template, paths are relative to each item:

```json
{
  "id": "item-template",
  "component": "Text",
  "text": { "path": "/name" }
}
```

If `/items` contains `[{"name": "Apple"}, {"name": "Banana"}]`, the template
renders twice with scoped data access.

## Two-Way Binding

Input components like `TextField` support two-way binding — the user's input
updates the `DataModel`, and any other component bound to the same path
refreshes:

```json
{
  "id": "name-field",
  "component": "TextField",
  "label": "Name",
  "value": { "path": "/user/name" }
}
```

The widget emits a local `DataChangeEvent` for the change. Note that v0.9 has
no upstream wire shape for individual data mutations — if the agent needs the
full data model, set `createSurface.sendDataModel = true` and the
`a2uiClientDataModel` envelope rides along on the next `action`.

## See Also

- [A2UI v0.9 specification](https://github.com/google/A2UI/tree/main/specification/0.9)
- [DataModel API Reference](../api-reference/data-model.md)
- [Events](events.md) — Handling data change events
- [State Management](state-management.md) — `updateDataModel` operation
