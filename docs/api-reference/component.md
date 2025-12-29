# Component

Represents a single UI component with its type, properties, and optional weight.

> *a2ui-4k currently implements the A2UI v0.8 specification. The A2UI protocol is under active development.*

## Definition

```kotlin
data class Component(
    val id: String,
    val componentType: String,
    val properties: Map<String, Any?> = emptyMap(),
    val weight: Float? = null
)
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `id` | `String` | Unique component identifier |
| `componentType` | `String` | Widget type name (e.g., "Text", "Button") |
| `properties` | `Map<String, Any?>` | Component-specific properties |
| `weight` | `Float?` | Optional flex weight for layout |

## Usage

### Creating Components

```kotlin
val textComponent = Component(
    id = "greeting",
    componentType = "Text",
    properties = mapOf(
        "text" to mapOf("literalString" to "Hello World"),
        "usageHint" to "h1"
    )
)

val buttonComponent = Component(
    id = "submit-btn",
    componentType = "Button",
    properties = mapOf(
        "child" to mapOf("componentId" to "btn-label"),
        "action" to mapOf(
            "name" to "submit",
            "context" to mapOf(
                "formId" to mapOf("literalString" to "contact")
            )
        ),
        "primary" to true
    )
)
```

### With Weight (for layouts)

```kotlin
val contentColumn = Component(
    id = "content",
    componentType = "Column",
    properties = mapOf(/* ... */),
    weight = 1.0f  // Takes remaining space
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

Components are created from `ComponentDef` in `surfaceUpdate` operations:

```json
{
  "id": "my-text",
  "component": "Text",
  "properties": {
    "text": { "literalString": "Hello" }
  }
}
```

## Related

- [UiDefinition](ui-definition.md) - Contains component map
- [CatalogItem](catalog-item.md) - Renders components
- [Data Binding](../core-concepts/data-binding.md) - Property resolution
