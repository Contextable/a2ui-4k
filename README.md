# a2ui-4k

A Kotlin Multiplatform rendering engine for the [A2UI protocol](https://github.com/google/A2UI), enabling AI agents to generate dynamic user interfaces that render natively across platforms.

> *a2ui-4k implements the A2UI v0.9 specification natively, with transparent
> backwards-compatible support for v0.8 surfaces (transcoded internally to the
> v0.9 shape — see [Deprecated Protocol Versions](docs/protocol/deprecated-versions.md)).
> The A2UI protocol is under active development.*

## Features

- **Multiplatform** - Android, iOS, and JVM/Desktop via Compose Multiplatform
- **Standard Catalog** - Full A2UI v0.9 standard widget set, plus legacy aliases for v0.8 properties
- **Bidirectional Protocol** - Server→client UI streaming and client→server `action` / `error` events with the v0.9 wire envelope (`{"version":"v0.9", …}`)
- **Reactive Data Binding** - JSON Pointer path-based data binding with automatic UI updates
- **Validation** - First-class `CheckRule` evaluation backed by the function evaluator
- **Accessibility** - Common `Accessibility` props (label, description) honored across input widgets
- **Backwards-Compatible** - v0.8 `ACTIVITY_SNAPSHOT` / `ACTIVITY_DELTA` envelopes accepted on the same code path; per-surface protocol version drives outbound serialization

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

## Exposing rendering to an agent

If your agent can call client-side tools (AG-UI `ToolExecutor`, OpenAI
functions, Gemini tools, Anthropic `tool_use`), the library ships
`A2UiRenderTool` — a canonical, SDK-agnostic helper that maps one `render_a2ui`
tool call to `createSurface` + `updateComponents` + optional `updateDataModel`
on your `SurfaceStateManager`. No new dependencies; the library stays
transport-free.

```kotlin
import com.contextable.a2ui4k.agent.A2UiRenderTool
import com.contextable.a2ui4k.agent.A2UiRenderException
import com.contextable.a2ui4k.state.SurfaceStateManager

val manager = SurfaceStateManager()
val renderTool = A2UiRenderTool(manager)

// AG-UI Kotlin SDK adapter (~4 lines):
class RenderA2UiToolExecutor(private val tool: A2UiRenderTool) : ToolExecutor {
    override val name = tool.name
    override val description = tool.description
    override val parameters = tool.parameters
    override suspend fun execute(arguments: JsonObject) = try {
        ToolExecutionResult.success(tool.render(arguments).toString())
    } catch (e: A2UiRenderException) {
        ToolExecutionResult.failure(e.message ?: "render_a2ui failed")
    }
}

toolRegistry.registerTool(RenderA2UiToolExecutor(renderTool))
```

See [skills/expose-a2ui-as-agent-tool.md](skills/expose-a2ui-as-agent-tool.md)
for the full integration walkthrough and adapter snippets for other SDKs.

## Transport Integration

a2ui-4k is a **rendering engine, not an A2A SDK**. It is transport-agnostic and expects callers to plug it into an A2A (Agent-to-Agent) transport of their choice. Concretely, callers are responsible for:

1. **Receiving** — extract A2UI protocol JSON from inbound A2A message Parts (MIME `application/json+a2ui`) and pass it to `SurfaceStateManager.processMessage(...)`.
2. **Advertising capabilities** — attach `A2UIClientCapabilities` (built via `a2uiBothVersionsClientCapabilities()` or a sibling helper) under the `"a2uiClientCapabilities"` key in outbound A2A message metadata.
3. **Sending events back** — convert `UiEvent` instances via `toClientMessage(version)` and wrap the returned `JsonObject` as an A2A message Part. For v0.9 surfaces with `sendDataModel == true`, also attach `SurfaceStateManager.buildClientDataModel()` under `"a2uiClientDataModel"` in metadata.

For the A2A protocol layer itself (`AgentExtension` data structure, `X-A2A-Extensions` header, `Message`/`Task`/`Part` models, HTTP transport), use an A2A SDK such as the Google A2A Python SDK or your framework's agent library. See [docs/api-reference/agent-extension.md](docs/api-reference/agent-extension.md) for the full reference.

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
