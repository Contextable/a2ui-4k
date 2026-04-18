# Component

Represents a single UI component with its type, properties, and optional weight.

> *a2ui-4k implements A2UI **v0.9** with backwards-compatible support for v0.8
> (see [Deprecated Protocol Versions](../protocol/deprecated-versions.md)).*

## Definition

```kotlin
@Serializable
data class Component(
    val id: String,
    val componentType: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val weight: Int? = null
)
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `id` | `String` | Unique component identifier |
| `componentType` | `String` | Widget type name (e.g., `"Text"`, `"Button"`) |
| `properties` | `JsonObject` | Component-specific properties (v0.9 flat shape) |
| `weight` | `Int?` | Optional flex weight for layout containers |

`widgetType` and `widgetData` are read-only aliases preserved for code that
predates the v0.9 rename.

## Usage

### Creating Components

```kotlin
import com.contextable.a2ui4k.model.Component
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

val textComponent = Component.create(
    id = "greeting",
    widgetType = "Text",
    data = buildJsonObject {
        put("text", "Hello World")
        put("variant", "h1")
    }
)

val buttonComponent = Component.create(
    id = "submit-btn",
    widgetType = "Button",
    data = buildJsonObject {
        put("child", "btn-label")
        put("variant", "primary")
        putJsonObject("action") {
            putJsonObject("event") {
                put("name", "submit")
                putJsonObject("context") {
                    put("formId", "contact")
                }
            }
        }
    }
)
```

### With Weight (for layouts)

```kotlin
val contentColumn = Component.create(
    id = "content",
    widgetType = "Column",
    data = buildJsonObject { /* … */ },
    weight = 1  // Takes remaining space
)
```

### Property Access Helpers

```kotlin
// Get string property (handles BoundValue resolution)
val text = component.getStringProperty("text", dataContext)

// Get number property
val value = component.getNumberProperty("value", dataContext)

// Get boolean property
val enabled = component.getBooleanProperty("enabled", dataContext)

// Get child reference
val childId = component.getChildReference("child")

// Get children references
val children = component.getChildrenReference("children")
```

## Protocol Mapping

Components are created from `ComponentDef` in `updateComponents` operations
(v0.9). Properties are hoisted directly onto the object — there is no nested
`properties` wrapper or boxed-literal shape:

```json
{
  "id": "my-text",
  "component": "Text",
  "text": "Hello",
  "variant": "h1"
}
```

Legacy v0.8 surfaces use `"component": { "Text": { "text": { "literalString":
"Hello" } } }` — the `V08ComponentFlattener` rewrites those into the v0.9
shape before they reach the catalog. See
[Deprecated Protocol Versions](../protocol/deprecated-versions.md).

## Related

- [UiDefinition](ui-definition.md) - Contains component map
- [CatalogItem](catalog-item.md) - Renders components
- [Data Binding](../core-concepts/data-binding.md) - Property resolution
