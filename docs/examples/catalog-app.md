# Catalog Example App

The catalog example app is an interactive tool for exploring a2ui-4k widgets and testing A2UI JSON definitions.

> *Located at `examples/catalog/` in the repository.*

## Features

- **Widget Gallery:** Browse all 18 standard widgets with live examples
- **JSON Editor:** Write A2UI JSON and see it render in real-time
- **Data Binding Demo:** Test path bindings with editable data models
- **Cross-Platform:** Runs on Android and JVM/Desktop

## Running the App

### Desktop (JVM)

```bash
./gradlew :examples:catalog:run
```

### Android

```bash
./gradlew :examples:catalog:installDebug
```

Or open the project in Android Studio and run the `catalog` module.

## App Structure

### Widget List Page

Displays a scrollable list of all available widgets. Tapping a widget opens the editor with a sample definition.

**Location:** `examples/catalog/src/commonMain/kotlin/com/contextable/a2ui4k/example/catalog/WidgetListPage.kt`

### Widget Editor Page

Split-pane interface with:
- **Left Panel:** JSON editor for A2UI definitions
- **Right Panel:** Live preview of rendered UI

**Location:** `examples/catalog/src/commonMain/kotlin/com/contextable/a2ui4k/example/catalog/WidgetEditorPage.kt`

### Widget Samples

Pre-built A2UI JSON examples for each widget type.

**Location:** `examples/catalog/src/commonMain/kotlin/com/contextable/a2ui4k/example/widgets/WidgetSamples.kt`

## Using the Editor

### Basic Workflow

1. Select a widget from the list (or start with blank editor)
2. Edit the JSON in the left panel
3. See changes reflected instantly in the right panel
4. Test interactions (buttons, inputs, etc.)

### JSON Format

The editor expects A2UI surface update format:

```json
{
  "components": [
    {
      "id": "root",
      "component": "Column",
      "properties": {
        "children": {
          "explicitList": [
            { "componentId": "greeting" }
          ]
        }
      }
    },
    {
      "id": "greeting",
      "component": "Text",
      "properties": {
        "text": { "literalString": "Hello!" }
      }
    }
  ],
  "root": "root"
}
```

### Adding Initial Data

Include a `data` object for path bindings:

```json
{
  "components": [...],
  "root": "root",
  "data": {
    "user": {
      "name": "Alice"
    }
  }
}
```

Then reference in components:

```json
{
  "id": "name-display",
  "component": "Text",
  "properties": {
    "text": { "path": "/user/name" }
  }
}
```

## Code Highlights

### RenderPanel

The render panel creates a `DataModel` from initial data and renders with `A2UISurface`:

```kotlin
@Composable
fun RenderPanel(parseResult: ParseResult) {
    when (parseResult) {
        is ParseResult.Success -> {
            val dataModel = rememberDataModel(parseResult.initialData)

            A2UISurface(
                definition = parseResult.definition,
                dataModel = dataModel,
                catalog = CoreCatalog,
                onEvent = { event ->
                    println("Event: $event")
                }
            )
        }
        is ParseResult.Error -> {
            Text("Error: ${parseResult.message}")
        }
    }
}
```

### JSON Parser

Converts editor JSON to `UiDefinition`:

```kotlin
fun parseJson(json: String): ParseResult {
    // Parse components array
    // Extract root ID
    // Extract optional initial data
    // Return UiDefinition or error
}
```

## Extending the App

### Adding Widget Samples

Edit `WidgetSamples.kt` to add new examples:

```kotlin
val myWidgetSample = WidgetSample(
    name = "MyWidget",
    json = """
    {
      "components": [...],
      "root": "..."
    }
    """.trimIndent()
)
```

### Custom Catalogs

Test custom widgets by modifying `RenderPanel.kt`:

```kotlin
val customCatalog = CoreCatalog + Catalog.of(
    id = "custom",
    MyCustomWidget
)

A2UISurface(
    definition = definition,
    catalog = customCatalog,  // Use custom catalog
    ...
)
```

## Related

- [Getting Started](../getting-started.md) - Basic usage
- [Widget Reference](../widgets/index.md) - All widgets
- [Catalogs](../core-concepts/catalogs.md) - Custom widgets
