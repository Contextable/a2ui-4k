# a2ui-4k

A Kotlin Multiplatform rendering engine for the [A2UI protocol](https://github.com/google/A2UI), enabling AI agents to generate dynamic user interfaces that render natively across platforms.

> *a2ui-4k currently implements the A2UI v0.8 specification. The A2UI protocol is under active development.*

## Features

- **Multiplatform** - Android, iOS, and JVM/Desktop support via Compose Multiplatform
- **Standard Catalog** - All 18 A2UI v0.8 standard widgets implemented
- **Reactive Data Binding** - JSON Pointer path-based data binding with automatic UI updates
- **Event Handling** - Full support for user actions and data change events

## Quick Start

```kotlin
import com.contextable.a2ui4k.catalog.CoreCatalog
import com.contextable.a2ui4k.render.A2UISurface

@Composable
fun MyScreen(uiDefinition: UiDefinition) {
    A2UISurface(
        definition = uiDefinition,
        catalog = CoreCatalog,
        onEvent = { event ->
            // Handle user interactions
        }
    )
}
```

## Documentation

- **[Getting Started](docs/getting-started.md)** - Installation and basic usage
- **[Full Documentation](docs/index.md)** - Complete guide and API reference

## A2UI Protocol

a2ui-4k implements the [A2UI specification](https://github.com/google/A2UI) from Google. For protocol details including message formats, component properties, and data binding, refer to the canonical specification.

## Platform Support

| Platform | Status |
|----------|--------|
| Android | Supported |
| JVM/Desktop | Supported |
| iOS | Supported |

## Building

```bash
# Run tests
./gradlew :library:allTests

# Publish to Maven Local
./gradlew :library:publishToMavenLocal
```

## License

```
Copyright 2025 Contextable LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
