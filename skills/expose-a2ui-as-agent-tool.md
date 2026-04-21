# Skill: Expose a2ui-4k rendering to an agent as the `render_a2ui` tool

Wire an agent that supports client-side tool calling (AG-UI, OpenAI,
Gemini, Anthropic) into an a2ui-4k client app so that the agent can
call `render_a2ui(surfaceId, catalogId, components, data)` and the
client renders the resulting surface. After this skill is applied the
app gets a clean tool-call round-trip (agent → client → rendered UI →
`ToolMessage` → agent resumes) and can delete any bespoke
`ACTIVITY_SNAPSHOT` handler that was driving `SurfaceStateManager`
directly.

### Library version vs. tool-adapter layer — read this first

This skill adds a single canonical tool helper shipped by the
`a2ui-4k` library plus a thin SDK-specific adapter that lives in the
**app**. The two layers are independent:

| Axis | What it is | Who owns it |
|---|---|---|
| **`A2UiRenderTool`** (library) | A pure `JsonObject → JsonObject` helper that drives `SurfaceStateManager` through `createSurface` + `updateComponents` + optional `updateDataModel`. Ships the canonical tool `name`, `description`, and JSON-Schema `parameters`. Transport-free; depends on nothing outside `a2ui-4k`. | `com.contextable:a2ui-4k:0.9.3+` |
| **Tool-executor adapter** (app) | A small class in your app that implements your tool-calling SDK's `ToolExecutor` / `FunctionDeclaration` / tool-handler interface and forwards to `A2UiRenderTool.render(...)`. Typically 3–6 lines. | Your app code |

Why these are independent: the library cannot depend on any specific
tool-calling SDK (AG-UI, OpenAI, Gemini, …) without pulling that SDK
into every a2ui-4k consumer. So the SDK glue stays in app code, while
the rendering logic, description, and schema stay canonical.

This file is a plain Markdown skill. It is intended to be usable by
any coding agent (Aider, Cline, Cursor, Copilot Workspace, Claude
Code, …) and also reads top-to-bottom as a human guide.

---

## 1. When to apply this skill

Apply this skill when **all** of the following are true:

- The app depends on `com.contextable:a2ui-4k:0.9.3` (or later).
- The app has an agent integration that supports client-side tool
  calling — AG-UI Kotlin SDK, Gemini Kotlin SDK, OpenAI SDK, Anthropic
  SDK, or equivalent.
- The app has (or can obtain) a `SurfaceStateManager` instance that is
  bound to the Compose `A2UISurface` composable that renders surfaces.

Strong signals the app currently needs this skill (grep these):

| Pattern | File types | What it means |
|---|---|---|
| `ACTIVITY_SNAPSHOT` handler that calls `SurfaceStateManager.processMessage` | `*.kt` | App is consuming the server-side middleware pattern. The tool round-trip never closes. Replace with `A2UiRenderTool`. |
| `ActivitySnapshotEvent` handler in controller / view-model code | `*.kt` | Same as above — custom event path bypassing the tool interface. |
| `ToolExecutor` interface references with no `render_a2ui` handler | `*.kt` | App has the AG-UI SDK wired up but never advertised `render_a2ui`. Register it. |
| `"render_a2ui"` string appearing only in prompts / system messages | `*.kt`, `*.md` | App is asking the agent to call a tool it has not registered. Register it. |

Do **not** apply this skill when:

- The app is the server/agent side. `a2ui-4k` is a **client** rendering
  library; the tool is called on the client and its execution renders
  the UI. Server-side tool definitions live in the agent framework
  (ADK, CopilotRuntime, etc.).
- The app has no agent integration — it only renders surfaces pushed
  over a direct a2ui channel. `SurfaceStateManager.processMessage` is
  already the right entry point; there is no tool to register.
- The app is on the 0.8.x library. `A2UiRenderTool` is a 0.9.3+ API.
  Run `migrate-0.8-to-0.9` first.

---

## 2. Prerequisites

Before making edits:

1. The target app builds cleanly against `com.contextable:a2ui-4k:0.9.3`
   (or later). Confirm with `./gradlew build` (or the app's equivalent).
2. You can locate **the** `SurfaceStateManager` instance that the
   rendering `A2UISurface` composable reads from. If there are
   multiple, identify the one bound to the on-screen surface. If you
   cannot find it, stop and ask the user.
3. You know which tool-calling SDK the app integrates with (AG-UI
   Kotlin, Gemini, OpenAI, Anthropic, custom). The adapter snippet
   you write depends on this.
4. Working tree is clean so the agent's changes are diffable.

---

## 3. Integration steps

### Step 1 — Instantiate `A2UiRenderTool`

**Detect** (grep): `A2UiRenderTool` — zero matches expected before this step.

**Before:** The app has a `SurfaceStateManager` instance wired into the
renderer but no `A2UiRenderTool` alongside it.

```kotlin
val surfaceStateManager = SurfaceStateManager()
// surfaceStateManager is passed to A2UISurface(...) and previously fed
// directly by an ACTIVITY_SNAPSHOT handler.
```

**After:**

```kotlin
import com.contextable.a2ui4k.agent.A2UiRenderTool

val surfaceStateManager = SurfaceStateManager()
val renderTool = A2UiRenderTool(surfaceStateManager)
```

**Check:** `renderTool.name == "render_a2ui"`; `renderTool.parameters`
is a non-null `JsonObject` with a `required` array containing
`surfaceId` and `components`.

### Step 2 — Wrap in your SDK's tool-executor

**Detect** (grep): how existing tools are registered in the app —
`ToolExecutor`, `FunctionDeclaration`, `Tool(...)`, etc.

**Before:** No `render_a2ui` handler is wired into the tool registry.

**After** — pick one of the following adapters based on the app's
tool-calling SDK.

#### AG-UI Kotlin SDK

```kotlin
import com.contextable.a2ui4k.agent.A2UiRenderException
import com.contextable.a2ui4k.agent.A2UiRenderTool
import kotlinx.serialization.json.JsonObject

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
```

#### Gemini Kotlin SDK (`com.google.ai.client.generativeai`)

```kotlin
val renderA2UiFunction = defineFunction(
    name = renderTool.name,
    description = renderTool.description,
    // Gemini uses its own schema type; serialize parameters to that shape.
    // See the Gemini SDK docs for the exact helper; most apps already
    // have a `jsonSchemaToGeminiSchema` adapter or equivalent.
) { args ->
    try {
        JSONObject(renderTool.render(args.toA2uiJsonObject()).toString())
    } catch (e: A2UiRenderException) {
        JSONObject(mapOf("error" to e.message))
    }
}
```

#### OpenAI / Anthropic SDK (generic shape)

Both SDKs take a name, description, and JSON Schema parameters at
registration time and dispatch tool calls to a handler by name. The
adapter collapses to:

```kotlin
tools.register(
    name = renderTool.name,
    description = renderTool.description,
    parameters = renderTool.parameters,
) { arguments: JsonObject ->
    try {
        renderTool.render(arguments).toString()
    } catch (e: A2UiRenderException) {
        """{"error":${Json.encodeToString(e.message ?: "render_a2ui failed")}}"""
    }
}
```

**Check:** Your SDK's tool-registration log lists `render_a2ui` among
the tools sent with each request. For AG-UI specifically, the agent-
side log should read
`Tools from frontend: [..., 'render_a2ui']`.

### Step 3 — Register the adapter with the tool registry

**Detect** (grep): the registration site — `registerTool`,
`addTool`, `tools += ...`, etc.

**Before:** the tool registry has no `render_a2ui` entry.

**After:**

```kotlin
toolRegistry.registerTool(RenderA2UiToolExecutor(renderTool))
```

**Check:** After running the app once, the agent-side log shows a
closed HITL round-trip on the first `render_a2ui` call:

```
[EXEC] HITL_RESUME ... tool_results=['call_...']
Removed tool call ... from pending list
```

If the `Removed tool call` line never appears, the round-trip is still
open — see Step 4 and the runtime troubleshooting section.

### Step 4 — Delete any bespoke `ACTIVITY_SNAPSHOT` / middleware handler

**Detect** (grep):

| Pattern | File types |
|---|---|
| `ActivitySnapshotEvent` | `*.kt` |
| `"ACTIVITY_SNAPSHOT"` string literals in event handling | `*.kt` |
| Any custom handler that calls `SurfaceStateManager.processMessage(...)` with a JsonObject parsed from an event stream | `*.kt` |

**Before:** Your controller or view-model has a handler that intercepts
streamed `ACTIVITY_SNAPSHOT` events and calls
`SurfaceStateManager.processMessage` directly:

```kotlin
fun onActivitySnapshot(event: ActivitySnapshotEvent) {
    surfaceStateManager.processMessage(event.payload)
}
```

**After:** Delete the handler. The `render_a2ui` tool executor
registered in Step 3 now owns the path from agent → surface. Two paths
feeding the same `SurfaceStateManager` will double-render surfaces
and race on data-model updates.

**Check:** Run the app and confirm surfaces still render — this time
through the tool-call path — and that no surface renders twice on the
same agent turn.

---

## 4. Verification

Build and test:

```bash
./gradlew build -x compileKotlinIosArm64 -x compileKotlinIosX64 \
    -x compileKotlinIosSimulatorArm64 -x commonizeNativeDistribution
./gradlew allTests
```

End-to-end smoke with a live agent:

1. Start the agent (e.g., an ADK-backed server via `ag_ui_adk`).
2. Prompt it to render a simple surface — e.g., "show me a card with
   one button labeled Hello". The agent should call `render_a2ui`.
3. Confirm the surface appears in the app, driven by the tool call
   rather than an `ACTIVITY_SNAPSHOT` event.
4. On the agent side, confirm the tool-result round-trip closes —
   logs show `HITL_RESUME` / `Removed tool call ... from pending list`.
5. Tap a button in the rendered surface. The resulting `action` event
   should round-trip back to the agent via the existing
   `A2UISurface.onEvent` path. (That path is unchanged by this skill.)

---

## 4a. Runtime verification — "LLM calls the tool but X"

| Symptom | Likely cause | Fix |
|---|---|---|
| Tool call reaches the adapter but nothing renders | Adapter is wired to a **different** `SurfaceStateManager` instance than the one `A2UISurface` reads from. | Pass the same `SurfaceStateManager` instance to both `A2UISurface` and `A2UiRenderTool`. |
| SDK rejects the JSON Schema at registration time | The SDK dislikes `additionalProperties: false` (Gemini has historically been strict here) or requires a flat schema. | Serialize `renderTool.parameters` into the SDK's preferred shape. Do not change the schema shipped by the library. |
| LLM never calls `render_a2ui` | Description is too terse, or the system prompt routes rendering to a different tool. | Leave `renderTool.description` as the default and make sure the system prompt references `render_a2ui` by name with an example. |
| HITL round-trip stays open (`Removed tool call` never appears) | The adapter is throwing an uncaught exception instead of returning a `ToolExecutionResult`. | Wrap the `render(...)` call in `try / catch (A2UiRenderException)` and map to the SDK's failure result. |
| Surface renders twice on every agent turn | A bespoke `ACTIVITY_SNAPSHOT` handler is still driving `SurfaceStateManager.processMessage` alongside the tool path. | Complete Step 4 — delete the legacy handler. |
| `A2UiRenderException: 'surfaceId' is required` | LLM omitted `surfaceId`. | Usually a weak system prompt. Reinforce that every `render_a2ui` call must specify a stable `surfaceId`. |

---

## 5. What NOT to change

- Do not fork `A2UiRenderTool.description` or `A2UiRenderTool.parameters`
  unless you genuinely need to. Both are library-owned and shared across
  every Kotlin host so LLMs see a consistent contract. If you need a
  different description, override it via the constructor param rather
  than patching the library.
- Do not re-implement the `createSurface` → `updateComponents` →
  `updateDataModel` sequence in the adapter. The library owns that
  flow; the adapter is a transport shim.
- Do not swallow `A2UiRenderException` and return success anyway. The
  LLM needs to see the failure result to recover.
- Do not change the tool's `name` (`"render_a2ui"`). It is the
  contract other parts of the ecosystem — including
  `@copilotkit/a2ui-renderer` on the React side — advertise to agents.

---

## 6. Reference map

| File | Purpose |
|---|---|
| `../library/src/commonMain/kotlin/com/contextable/a2ui4k/agent/A2UiRenderTool.kt` | The library-side helper class + `A2UiRenderException`. |
| `../library/src/commonMain/kotlin/com/contextable/a2ui4k/state/SurfaceStateManager.kt` | The downstream `processMessage(JsonObject)` entry point that `A2UiRenderTool` drives. |
| `../library/src/commonMain/kotlin/com/contextable/a2ui4k/extension/A2UIExtension.kt` | Catalog-URI constants, including `STANDARD_CATALOG_URI` (default) and `BASIC_CATALOG_URI_V09` (wire-compat). |

---

## 7. Sources

- Library source: [../library/src/commonMain/kotlin/com/contextable/a2ui4k/agent/A2UiRenderTool.kt](../library/src/commonMain/kotlin/com/contextable/a2ui4k/agent/A2UiRenderTool.kt)
- Upstream A2UI spec: <https://github.com/google/A2UI>
- Reference React implementation (structural analog): `@copilotkit/a2ui-renderer`
