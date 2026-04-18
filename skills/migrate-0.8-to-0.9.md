# Skill: Migrate an a2ui-4k client app to the 0.9.x library (from 0.8.x)

Upgrade a Kotlin/Compose app's **dependency on the `com.contextable:a2ui-4k`
library** from `0.8.x` to `0.9.x` (authored against `0.9.1`). The 0.9 line
broke Kotlin API compatibility in ways a compiler/runtime can't paper over,
so client code has to change.

### Library version vs. A2UI protocol version — read this first

This skill is **about the library dependency only**. It does *not* change
which A2UI wire-protocol version the client speaks to servers. Those are
two independent axes:

| Axis | What it means | Changed by this skill? |
|---|---|---|
| **Library version** (`a2ui-4k` 0.8.x → 0.9.x) | The Kotlin/Compose API the app compiles against — class names, method signatures, event types. | **Yes — this is the entire point.** |
| **A2UI protocol version** (v0.8 / v0.9 wire format) | What the server sends over the wire and the client parses back. Selected at runtime via capability negotiation with the agent. | **No.** 0.9.x of the library ships with a hybrid transcoder that accepts both v0.8 and v0.9 wire envelopes. |

Concretely: after this migration the app still talks to existing v0.8 A2UI
agents. The transcoder in the 0.9.x library converts v0.8 messages to v0.9
shape internally, and outbound events serialize back to whichever wire
version each surface was created under. Protocol-version selection happens
via the capability helpers in Step 11 — unrelated to which library version
is on the classpath.

This file is a plain Markdown skill. It is intended to be usable by any
coding agent (Aider, Cline, Cursor, Copilot Workspace, Claude Code, …) and
also reads top-to-bottom as a human migration guide.

---

## 1. When to apply this skill

Apply this skill when **all** of the following are true for the target app:

- Its build file declares a dependency on `com.contextable:a2ui-4k:0.8.*`
  (or the `-android` / `-jvm` / `-ios*` platform variants).
- The goal is to upgrade that **library dependency** to `0.9.1` (or any
  later 0.9.x).

Strong signals the app is compiled against the 0.8.x library API (grep
these patterns):

| Pattern | File types |
|---|---|
| `com.contextable:a2ui-4k:0\.8` | `*.gradle`, `*.gradle.kts`, `gradle/libs.versions.toml` |
| `\.processSnapshot\(` | `*.kt` |
| `\.processDelta\(` | `*.kt` |
| `UserActionEvent` | `*.kt` |
| `A2UIExtension\.URI_V08` | `*.kt` |
| `import .*\.DataEntry` or `DataEntry\(` | `*.kt` |
| `componentProperties\s*=` | `*.kt` |
| `\.withRoot\(` | `*.kt` |
| `A2UIActivityContent`, `A2UIOperation`, `BeginRendering`, `SurfaceUpdate`, `DataModelUpdate` | `*.kt` |

Do **not** apply this skill when:

- The app is the server/agent side (a2ui-4k is a **client** rendering
  library; server migration is out of scope).
- The app already uses the 0.9.x library API (`processMessage`,
  `ActionEvent`, `URI_V09`).
- The ask is "make the client speak the v0.9 **wire protocol** to an
  agent" — that's a runtime/negotiation choice, not a library upgrade.
  See Step 11 for the capability helpers that control it.

---

## 2. Prerequisites

Before making edits:

1. Kotlin 2.1.20+ and JDK 21 are available.
2. Working tree is clean (so the agent's changes are diffable). If the app
   has uncommitted changes, stop and ask the user.
3. Run the app's existing build to establish a pre-migration baseline. Record
   the current pass/fail state — some steps below add **new** compile errors
   that get cleared by later steps.

---

## 3. Migration steps

Each step is one mechanical transformation with a detection pattern,
before/after code, and a check. Apply them in order; later steps depend on
the changes in earlier steps.

### Step 1 — Bump the dependency coordinate

**Detect:** `com.contextable:a2ui-4k(-android|-jvm|-ios[A-Za-z0-9]*)?:0\.8\.[0-9]+`
in any Gradle/TOML file.

**Pick the target version** — in this priority order:

1. **If the user specified a target version, use it.** This is the
   preferred path; accept any value matching `0\.9\.[0-9]+` (or a valid
   pre-release like `0.9.2-alpha.3`). If the user gave a v0.8.x value,
   stop and ask — they do not need this skill.

2. **Otherwise, resolve the latest 0.9.x from Maven Central** by fetching
   the artifact metadata and picking the highest version whose major.minor
   is `0.9`:

   ```bash
   curl -s https://repo1.maven.org/maven2/com/contextable/a2ui-4k/maven-metadata.xml
   ```

   Parse the `<versioning><versions>` list. Select the highest
   `0.9.N` entry (prefer stable over `-alpha.*` / `-beta.*` unless the
   user asked for a pre-release channel). Do **not** pick 0.10.x or
   higher automatically — future majors may reintroduce breaking changes
   this skill does not cover.

3. **Fallback if offline or the fetch fails:** use `0.9.1`, the
   version this skill was authored against. Note the fallback in the
   agent's progress output so the user can override.

**Before:**

```kotlin
implementation("com.contextable:a2ui-4k:0.8.2")
```

**After (example, substitute the resolved version):**

```kotlin
implementation("com.contextable:a2ui-4k:0.9.1")
```

Also update any version catalog entry (`libs.versions.toml`) — both the
version string and any alias that references it.

**Check:** `./gradlew dependencies | grep a2ui-4k` resolves the chosen
version and reports no conflicts.

---

### Step 2 — Replace the extension URI constant

**Detect:** `A2UIExtension.URI_V08`.

**Before:**

```kotlin
val uri = A2UIExtension.URI_V08
```

**After:**

```kotlin
val uri = A2UIExtension.URI_V09
```

Notes:

- `A2UIExtension.URI_V08` was removed in 0.9.0.
- If the app intentionally wants the v0.8 URI for capability negotiation,
  use `A2UIExtension.STANDARD_CATALOG_URI_V08` (the catalog URI, which is
  distinct) and re-read Step 11 on capabilities.
- The v0.9 protocol version string is also available as
  `A2UIExtension.PROTOCOL_VERSION` (= `"v0.9"`).

**Check:** no references to `URI_V08` remain; code compiles past this call site.

---

### Step 3 — Replace `processSnapshot` / `processDelta` with `processMessage`

**Detect:** `\.processSnapshot\(` or `\.processDelta\(` on a
`SurfaceStateManager` instance.

The 0.8.x library exposed two entry points, one per activity-event type:

```kotlin
// 0.8.x library API
manager.processSnapshot(messageId, activityContent)   // ACTIVITY_SNAPSHOT
manager.processDelta(messageId, jsonPatch)            // ACTIVITY_DELTA
```

In 0.9.x there is a single entry point that takes the full decoded envelope
as a `JsonObject`. It recognises both v0.9 native envelopes
(`{"version":"v0.9","<op>":{…}}`) **and** v0.8 envelopes
(`ACTIVITY_SNAPSHOT`, `ACTIVITY_DELTA`), transcoding the latter internally.

**Before (v0.8 activity-event handler, typical shape):**

```kotlin
when (activity.event) {
    "ACTIVITY_SNAPSHOT" ->
        manager.processSnapshot(activity.messageId, activity.content)
    "ACTIVITY_DELTA" ->
        manager.processDelta(activity.messageId, activity.patch)
}
```

**After:**

```kotlin
// Pass the full decoded envelope JsonObject as-is. The manager figures
// out whether it's v0.9 or v0.8 and dispatches accordingly.
val handled: Boolean = manager.processMessage(activity.envelope)
```

**What to pass — exact field names matter.** The library detects v0.8 by
looking at specific top-level keys on the envelope it is given. **Do not
rename these keys** when re-wrapping an already-parsed event, and do not
strip them by passing only the inner `content` or `patch`. The detector
(`V08MessageTranscoder.isV08Envelope`) accepts envelopes that satisfy
any of the following:

- `type` is `"ACTIVITY_SNAPSHOT"` or `"ACTIVITY_DELTA"` (preferred)
- `kind` is `"ACTIVITY_SNAPSHOT"` or `"ACTIVITY_DELTA"`
- `version` is `"v0.8"`

The cache key comes from `messageId` (or `id`) — snapshots and deltas
must use the *same* `messageId` so deltas replay on the right baseline.

If the transport layer only gives you parsed pieces and not the original
raw JSON envelope, reconstruct it using these exact keys:

```kotlin
val envelope = buildJsonObject {
    put("type", JsonPrimitive("ACTIVITY_SNAPSHOT"))   // or "ACTIVITY_DELTA"
    put("messageId", JsonPrimitive(activity.messageId))
    put("content", activity.content)                  // for SNAPSHOT
    // put("patch", activity.patch)                   // for DELTA
}
manager.processMessage(envelope)
```

If the transport already hands you the raw SSE `data:` payload as a
`JsonObject`, pass it through unchanged — no re-wrap needed.

**Signature reference** (from
[library/src/commonMain/kotlin/com/contextable/a2ui4k/state/SurfaceStateManager.kt](../library/src/commonMain/kotlin/com/contextable/a2ui4k/state/SurfaceStateManager.kt)):

```kotlin
fun processMessage(message: JsonObject): Boolean
```

Returns `true` if the message was recognised and dispatched, `false` if it
was ignored. Treat a `false` return as a bug — log it loudly during
migration, it almost always means the envelope shape is wrong.

**Check:** no references to `processSnapshot` / `processDelta` remain, and
the envelope being passed uses the exact keys `type`, `messageId`, and
`content` / `patch`.

---

### Step 3a — Bridge your transport into `processMessage`

a2ui-4k never touches the network itself. Some layer in the app decodes
SSE (or other transport) `data:` lines and is responsible for handing the
resulting `JsonObject` to `processMessage`. The bridge must:

1. Preserve the top-level envelope keys (see Step 3 — `type`, `messageId`,
   `content` / `patch`).
2. Fire for **both** `ACTIVITY_SNAPSHOT` and `ACTIVITY_DELTA`. Dropping
   snapshots leaves the delta cache empty and nothing renders.
3. Use the same `messageId` the server sent. Don't substitute a local id.

**Detection — when is this step needed?** Look for any of:

- The app imports `io.ktor.client.plugins.sse`, `okhttp3.sse.EventSource`,
  or any SSE client.
- The app imports ag-ui types such as `ActivitySnapshotEvent`,
  `ActivityDeltaEvent`, `ChatController` (package names vary; grep the
  simple names).
- After Step 3 the app has a `processMessage` call site, but runtime logs
  show no `processMessage` ever firing in response to activity events.

**ag-ui example.** Common case — if the detection rules above match
ag-ui specifically, the bridge typically lives in the `ChatController`
event handler. Use this pattern:

```kotlin
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

chatController.onEvent { event ->
    when (event) {
        is ActivitySnapshotEvent -> {
            if (event.activityType == "a2ui-surface") {
                val envelope = buildJsonObject {
                    put("type", JsonPrimitive("ACTIVITY_SNAPSHOT"))
                    put("messageId", JsonPrimitive(event.messageId))
                    put("activityType", JsonPrimitive(event.activityType))
                    put("content", event.content)
                }
                val ok = surfaceStateManager.processMessage(envelope)
                if (!ok) log.w("a2ui: snapshot envelope rejected")
            }
        }
        is ActivityDeltaEvent -> {
            if (event.activityType == "a2ui-surface") {
                val envelope = buildJsonObject {
                    put("type", JsonPrimitive("ACTIVITY_DELTA"))
                    put("messageId", JsonPrimitive(event.messageId))
                    put("activityType", JsonPrimitive(event.activityType))
                    put("patch", event.patch)
                }
                val ok = surfaceStateManager.processMessage(envelope)
                if (!ok) log.w("a2ui: delta envelope rejected")
            }
        }
        else -> Unit
    }
}
```

For other transports: follow the same invariant. Parse the SSE line as a
`JsonObject`, preserve its original top-level keys, and call
`processMessage`. Prefer passing the raw parsed JSON directly when it is
available — fewer moving parts than a re-wrap.

**Check:**

- Both `ACTIVITY_SNAPSHOT` and `ACTIVITY_DELTA` paths reach
  `processMessage`.
- The `messageId` passed to the library matches the one the server
  emitted (log both sides to confirm).
- `processMessage` returns `true` for at least the first snapshot of a
  session.

---

### Step 4 — Rename `UserActionEvent` to `ActionEvent`

**Detect:** `UserActionEvent` (any usage — import, type reference, pattern
match, constructor call).

**Before:**

```kotlin
import com.contextable.a2ui4k.model.UserActionEvent

when (event) {
    is UserActionEvent -> handleAction(event.name, event.context)
    is DataChangeEvent -> handleDataChange(event.path, event.value)
}
```

**After:**

```kotlin
import com.contextable.a2ui4k.model.ActionEvent

when (event) {
    is ActionEvent -> handleAction(event.name, event.context)
    is DataChangeEvent -> handleDataChange(event.path, event.value)
}
```

The property set is identical: `name`, `surfaceId`, `sourceComponentId`,
`timestamp`, `context: JsonObject?`.

**Check:** `grep -n UserActionEvent` returns no results.

---

### Step 5 — Action `context` is already a `JsonObject`

This step applies only if, when built against the 0.8.x library, the app
had **its own wrapper types** that converted `context` into a
`List<Pair<String, ...>>` / `Map` / `List<Entry>` before consuming it.

**Before (hand-rolled 0.8.x-era wrapping):**

```kotlin
val entries: List<Pair<String, String>> = event.context
    ?.entries
    ?.map { it.key to it.value.toString() }
    ?: emptyList()
```

**After:**

```kotlin
// v0.9: event.context is already a JsonObject with flat key→JsonElement entries.
val ctx: JsonObject = event.context ?: JsonObject(emptyMap())
val nameField = ctx["name"]?.jsonPrimitive?.content
```

Nothing to do if the app already consumed `JsonObject` directly.

**Check:** no code still assumes `context` is an array or list of pairs.

---

### Step 6 — Use `toClientMessage(version)` for outbound envelopes

**Detect:** any code that builds a client-to-server message envelope by hand,
typically containing string literals like `"userAction"`, `"dataChange"`,
or manual `{"version":"v0.9", …}` construction.

The library now owns envelope serialization. Each surface is tagged with a
`ProtocolVersion`; ask the manager, pass it to `toClientMessage`, and send
whatever non-null `JsonObject` comes back.

**Before (v0.8, typical manual envelope):**

```kotlin
val wire = buildJsonObject {
    put("userAction", buildJsonObject {
        put("name", JsonPrimitive(event.name))
        put("surfaceId", JsonPrimitive(event.surfaceId))
        put("sourceComponentId", JsonPrimitive(event.sourceComponentId))
        put("timestamp", JsonPrimitive(event.timestamp))
        put("context", event.context ?: JsonObject(emptyMap()))
    })
}
transport.send(wire)
```

**After:**

```kotlin
import com.contextable.a2ui4k.model.ProtocolVersion
import com.contextable.a2ui4k.model.toClientMessage

val version = manager.getSurfaceProtocolVersion(event.surfaceId)
    ?: ProtocolVersion.V0_9

event.toClientMessage(version)?.let { wire ->
    transport.send(wire)
}
```

What each event serialises to:

| Event | Under `V0_9` | Under `V0_8` |
|---|---|---|
| `ActionEvent` | `{"version":"v0.9","action":{…}}` | `{"userAction":{…}}` |
| `DataChangeEvent` | `null` (local-only in v0.9; rides on the next action as `a2uiClientDataModel` metadata) | `{"dataChange":{…}}` |
| `ValidationError` | `{"version":"v0.9","error":{"code":"VALIDATION_FAILED",…}}` | `null` (log locally) |
| `ClientError` | `{"version":"v0.9","error":{…}}` | `null` (log locally) |

A `null` return means "no wire message for this event under this version" —
do not send anything.

**Check:** no handwritten `"userAction"` / `"dataChange"` / `"action"` string
keys remain in the app's outbound serialization paths.

---

### Step 7 — Update `Component` construction sites

**Detect:** `Component(` constructor calls with the 0.8.x-era named
argument `componentProperties = …`, or references to
`Component.componentProperties` / the old `widgetType` / `widgetData`
computed getters (which were backed by a map).

The 0.8.x `Component` put widget type and props into one map:

```kotlin
// 0.8.x library
@Serializable
data class Component(
    val id: String,
    val componentProperties: Map<String, JsonObject> = emptyMap(),
    val weight: Int? = null
)
```

The 0.9.x `Component` has them as separate, non-nullable fields:

```kotlin
// 0.9.x library
@Serializable
data class Component(
    val id: String,
    val componentType: String,
    val properties: JsonObject,
    val weight: Int? = null
)
```

**Before:**

```kotlin
val c = Component(
    id = "greeting",
    componentProperties = mapOf(
        "Text" to buildJsonObject {
            put("text", buildJsonObject {
                put("literalString", JsonPrimitive("Hello"))
            })
        }
    )
)
```

**After:**

```kotlin
val c = Component.create(
    id = "greeting",
    widgetType = "Text",
    data = buildJsonObject {
        put("text", JsonPrimitive("Hello"))   // plain literal in v0.9
    }
)
```

If the app builds components from raw JSON, prefer `ComponentDef.fromJson`
and `Component.fromComponentDef` to keep up with any future shape tweaks.

Reads against `component.componentProperties` → `component.properties`; the
widget-type getter is now just `component.componentType` (non-nullable).

**Check:** no `componentProperties` references remain; all `Component(…)`
sites either use the three-arg form `(id, componentType, properties)` or
call `Component.create(...)`.

---

### Step 8 — Stop using `DataEntry`; pass `JsonElement` directly

**Detect:** `import com.contextable.a2ui4k.model.DataEntry` or any
`DataEntry(` / `DataEntry.StringValue(` / `DataEntry.NumberValue(` etc.

`DataEntry` and its nested value classes were removed in 0.9.0. The v0.9
`updateDataModel` wire op carries a single `path` + raw JSON `value`, so
there is no longer a typed-entry layer.

**Before:**

```kotlin
dataModel.applyUpdate(
    path = "/user/name",
    contents = listOf(DataEntry.StringValue("Ada"))
)
```

**After:**

```kotlin
dataModel.update("/user/name", JsonPrimitive("Ada"))
// or use the primitive-typed helpers:
dataModel.updateString("/user/name", "Ada")
dataModel.updateNumber("/user/score", 42.0)
dataModel.updateBoolean("/user/active", true)
```

To delete a key, use:

```kotlin
dataModel.delete("/user/name")
```

(Wire-side: v0.9's `updateDataModel` with `value` omitted deletes; 0.9.0+
`SurfaceStateManager` handles that — no client code change needed.)

**Check:** no `DataEntry` references remain.

---

### Step 9 — Remove `UiDefinition.withRoot(...)`

**Detect:** `\.withRoot\(` on a `UiDefinition`.

v0.9 identifies the root by convention: it's the component with `id =
"root"`. There is no explicit root pointer on `UiDefinition`, and
`withRoot` was removed.

**Before:**

```kotlin
val def = UiDefinition(surfaceId = "default")
    .withComponents(components)
    .withRoot("header")
```

**After:**

```kotlin
// Rename whichever component should be root to id = "root".
val def = UiDefinition(
    surfaceId = "default",
    components = components.mapValues { (id, c) ->
        if (id == "header") c.copy(id = "root") else c
    }.mapKeys { (id, _) -> if (id == "header") "root" else id }
)
```

For most apps there is already a component with id `"root"` (since A2UI
agents have emitted that convention throughout v0.9 development), in which
case simply delete the `withRoot(...)` call.

**Check:** no `withRoot` references remain.

---

### Step 10 — Update literal widget references (only if the app hand-builds definitions)

**Detect:** `MultipleChoice` or `SingleChoice` as a widget-type string
(`"MultipleChoice"`, `widgetType = "MultipleChoice"`, etc.) in code that
builds `UiDefinition`s or `Component`s from scratch.

In v0.9 these collapsed into a single widget, `ChoicePicker`, with a
selection-mode variant:

| v0.8 | v0.9 |
|---|---|
| `MultipleChoice` | `ChoicePicker` + `variant: "multipleSelection"` |
| `SingleChoice` | `ChoicePicker` + `variant: "mutuallyExclusive"` |

**Before:**

```kotlin
Component.create(
    id = "sizes",
    widgetType = "MultipleChoice",
    data = buildJsonObject { /* …options… */ }
)
```

**After:**

```kotlin
Component.create(
    id = "sizes",
    widgetType = "ChoicePicker",
    data = buildJsonObject {
        put("variant", JsonPrimitive("multipleSelection"))
        /* …options… */
    }
)
```

**Note:** if the app only *renders* definitions coming from an agent (i.e.
never names these widgets in its own code), skip this step — v0.8 JSON
received over the wire is transcoded automatically by the library.

**Check:** no literal `"MultipleChoice"` / `"SingleChoice"` widget-type
strings remain in the app's source.

---

### Step 11 — Pick the right capability helper

**Detect:** calls to `a2uiStandardClientCapabilities()` or
`a2uiClientCapabilities(...)` in the app's capability-negotiation path.

0.9.1 offers three standard helpers. Pick based on what agents the app
talks to **in production**:

```kotlin
import com.contextable.a2ui4k.extension.*

// v0.9-only (recommended default for new integrations)
val caps = a2uiStandardClientCapabilities()

// Accept either v0.9 or v0.8 from the agent; agent picks.
val caps = a2uiBothVersionsClientCapabilities()

// v0.8 only (legacy deployments, rare)
val caps = a2uiV08StandardClientCapabilities()
```

Guidance:

- If the app previously advertised the v0.8 catalog and agents in the field
  still emit v0.8, use `a2uiBothVersionsClientCapabilities()`. The library's
  transcoder will accept either wire version.
- If the app controls both ends (client + agent) and the agent is on v0.9,
  use `a2uiStandardClientCapabilities()`.
- `a2uiClientCapabilities(vararg catalogIds)` is unchanged; use it for
  custom catalogs.

**Check:** the capability builder call reflects the deployed agent
version(s).

---

### Step 12 — Handle the new `ValidationError` / `ClientError` events

**Detect:** `when (event)` expressions on `UiEvent` that previously only
needed two branches (`UserActionEvent`, `DataChangeEvent`).

The 0.9.x library adds two more `UiEvent` subtypes. Exhaustive `when`
expressions will fail to compile without branches for them.

**Before:**

```kotlin
when (event) {
    is UserActionEvent -> sendToAgent(event)
    is DataChangeEvent -> mirrorLocally(event)
}
```

**After:**

```kotlin
import com.contextable.a2ui4k.model.ActionEvent
import com.contextable.a2ui4k.model.ClientError
import com.contextable.a2ui4k.model.DataChangeEvent
import com.contextable.a2ui4k.model.ValidationError

when (event) {
    is ActionEvent      -> sendToAgent(event)
    is DataChangeEvent  -> mirrorLocally(event)
    is ValidationError  -> logValidation(event.path, event.message)
    is ClientError      -> logClientError(event.code, event.message)
}
```

`ValidationError.code` is always `"VALIDATION_FAILED"`; `ClientError.code`
is any other string. Both are wrapped into
`{"version":"v0.9","error":{…}}` by `toClientMessage(V0_9)` — see Step 6.

**Check:** every `when (event: UiEvent)` in the app either has all four
branches or an explicit `else`.

---

## 4. Verification

After the steps above, run:

```bash
./gradlew build \
  -x compileKotlinIosArm64 \
  -x compileKotlinIosX64 \
  -x compileKotlinIosSimulatorArm64 \
  -x commonizeNativeDistribution

./gradlew allTests
```

(Drop the `-x` flags on macOS if iOS targets are in scope.)

Smoke test (recommended, transport-agnostic):

1. Instantiate a `SurfaceStateManager`.
2. Feed it one known-good v0.8 `ACTIVITY_SNAPSHOT` envelope via
   `processMessage(envelope)`. Assert it returns `true`.
3. Assert `manager.getSurfaces()` contains a surface with a non-null
   component tree.
4. Assert `manager.getSurfaceProtocolVersion(surfaceId) ==
   ProtocolVersion.V0_8` — confirming the surface is correctly tagged.

If all three pass, the hybrid transcoder is intact and outbound events will
be serialised in the v0.8 wire shape for that surface.

---

## 4a. Runtime verification — "builds pass but A2UI content doesn't render"

This is a distinct failure mode from a broken build: the app compiles,
starts, and chats with the server, but no surfaces ever render. The cause
is almost always on the bridge between the transport layer and
`processMessage` (Step 3 / Step 3a). Run these checks in order; the first
failing item localises the bug.

1. **Is `processMessage` actually invoked?** Add a log at every call site
   (or a single spot in the bridge). For each activity event from the
   server, expect exactly one call. If zero calls fire, Step 3a's bridge
   is missing.

2. **What does `processMessage` return?** Log the `Boolean` return value.
   - `false` on every call → the envelope is not being recognised as
     v0.8. The most common cause is wrong top-level field names (e.g.,
     `"event"` / `"activityId"` instead of `"type"` / `"messageId"`) or
     passing only the inner `content` / `patch`. Re-read Step 3.
   - `true` but still no UI → continue to the next checks.

3. **After the first `ACTIVITY_SNAPSHOT`, is `manager.getSurfaces()`
   non-empty?** If not, either (a) the snapshot's `operations` array was
   empty and the real ops only come in subsequent deltas (legal — move
   on), or (b) the `beginRendering` op was malformed. Log the transcoded
   v0.9 ops: temporarily instrument
   `V08MessageTranscoder.transcode(envelope)` from your bridge, or log
   the surface map after each call.

4. **Do snapshot and delta share the same `messageId`?** The transcoder
   caches operations by `messageId`; a delta whose id does not match the
   prior snapshot replays against an empty baseline and emits nothing.
   Log the `messageId` at the bridge for every envelope.

5. **Does the created surface have a root component?** Call
   `manager.getSurface(surfaceId)`. It should contain a component with
   `id == "root"` (v0.9 convention) OR a non-null `rootComponentId`
   (preserved by the transcoder from `beginRendering.root`). If neither
   is present, `A2UISurface` has nothing to mount.

6. **What is the surface's protocol version tag?** Call
   `manager.getSurfaceProtocolVersion(surfaceId)`. Against a v0.8 agent
   it must be `ProtocolVersion.V0_8`. `V0_9` means the v0.8 detector
   rejected the envelope and the surface was created from something else
   — go back to item 2.

If items 1–6 all succeed and surfaces are populated but nothing appears
on screen, the problem is in the Compose layer (e.g., `A2UISurface` not
being wired to the `UiDefinition` from `manager.getSurface`), not in
this migration's scope.

---

## 5. What NOT to change

Avoid over-migrating. These patterns are **still correct in 0.9.1**:

- **`ProtocolVersion.V0_8` references.** The enum still has `V0_8`; it is
  used to tag legacy surfaces so outbound events round-trip correctly.
- **`ACTIVITY_SNAPSHOT` / `ACTIVITY_DELTA` envelopes** received from a
  legacy server. Do **not** rewrite them to v0.9 shape — the library's
  `V08MessageTranscoder` handles that internally. Just pass them to
  `processMessage`.
- **Top-level envelope keys when re-wrapping.** The transcoder recognises
  only `type` (or `kind`), `messageId` (or `id`), and `content` /
  `patch`. Do not rename these to `event`, `activityId`, or anything
  else — a rename silently disables v0.8 recognition and
  `processMessage` returns `false`.
- **v0.8 widget prop aliases** (`label`, `distribution`, `alignment`,
  `spaceEvenly`, `usageHint`, `scale-down`, TextField `text` /
  `textFieldType`, Image size keywords). They are still accepted by v0.9
  widgets; legacy JSON does not need to be edited.
- **The `a2uiV08StandardClientCapabilities()` helper.** Still supported;
  only switch away from it if the app actually needs v0.9.

---

## 6. Reference map

Quick 0.8.x library → 0.9.x library symbol lookup. "Removed" means the
symbol no longer exists in `com.contextable.a2ui4k.*` as of 0.9.1.

| 0.8.x library symbol | 0.9.x library replacement |
|---|---|
| `SurfaceStateManager.processSnapshot(messageId, content)` | `SurfaceStateManager.processMessage(envelope)` |
| `SurfaceStateManager.processDelta(messageId, patch)` | `SurfaceStateManager.processMessage(envelope)` |
| `UserActionEvent` | `ActionEvent` |
| (hand-built envelope `{"userAction": {…}}`) | `event.toClientMessage(version)` |
| `DataChangeEvent` | `DataChangeEvent` (unchanged name; now local-only under v0.9) |
| `DataEntry`, `DataEntry.StringValue`, `DataEntry.NumberValue`, `DataEntry.BooleanValue` | **Removed.** Use `JsonElement` directly with `DataModel.update(path, value)` |
| `DataModelUpdate.contents: List<DataEntry>` | **Removed.** `updateDataModel` now carries `path` + `value` |
| `A2UIOperation`, `BeginRendering`, `SurfaceUpdate`, `DataModelUpdate`, `DeleteSurface` model classes | **Internal.** Don't reference directly; pass raw `JsonObject` envelopes to `processMessage` |
| `A2UIActivityContent` | **Internal / removed from public API.** Pass envelopes as `JsonObject` |
| `Component(id, componentProperties, weight)` | `Component(id, componentType, properties, weight)` or `Component.create(id, widgetType, data, weight)` |
| `Component.componentProperties: Map<String, JsonObject>` | `Component.componentType: String` + `Component.properties: JsonObject` |
| `Component.widgetType: String?` (nullable) | `Component.componentType: String` (non-null) |
| `UiDefinition.withRoot(componentId)` | **Removed.** Root is the component with `id = "root"` (convention) |
| `UiDefinition.rootComponentId` setter | Set via the convention; read-only in v0.9 |
| `ComponentArrayReference`, `TemplateReference` | **Removed.** Replaced by `ChildrenReference` sealed class |
| `A2UIExtension.URI_V08` | `A2UIExtension.URI_V09` |
| `A2UIExtension.PROTOCOL_VERSION` (= `"v0.8"`) | `A2UIExtension.PROTOCOL_VERSION` (= `"v0.9"`); `PROTOCOL_VERSION_V08` still exists (= `"v0.8"`) |
| `"MultipleChoice"` widget type | `"ChoicePicker"` + `variant: "multipleSelection"` |
| `"SingleChoice"` widget type | `"ChoicePicker"` + `variant: "mutuallyExclusive"` |
| `{"literalString": "..."}` in hand-built JSON | Plain JSON string primitive |
| `{"literalNumber": 42}` | Plain JSON number |
| `{"literalBoolean": true}` | Plain JSON boolean |
| `{"dataBinding": {"path": "/x"}}` | `{"path": "/x"}` |
| (none) | `{"call": "fn", "args": {…}}` — new function-call references |
| (none) | `ValidationError` event (`code == "VALIDATION_FAILED"`) |
| (none) | `ClientError` event (any other code) |
| (none) | `CheckRule(condition, message)` validation on input widgets |
| (none) | `Accessibility(label, description)` on input widgets |
| (none) | `sendDataModel: Boolean` flag on surface; produces `a2uiClientDataModel` A2A metadata |

---

## 7. Sources

Authoritative references inside the a2ui-4k repository (read these if
anything above is ambiguous for a specific edit):

- [docs/protocol/deprecated-versions.md](../docs/protocol/deprecated-versions.md) — wire-level diff, widget prop aliases, capability negotiation.
- [CHANGELOG.md](../CHANGELOG.md) — `[0.9.0]` lists every breaking change; `[0.9.1]` lists the v0.8 compatibility layer.
- [library/src/commonMain/kotlin/com/contextable/a2ui4k/state/SurfaceStateManager.kt](../library/src/commonMain/kotlin/com/contextable/a2ui4k/state/SurfaceStateManager.kt) — `processMessage` signature and dispatch behaviour.
- [library/src/commonMain/kotlin/com/contextable/a2ui4k/model/UiEvent.kt](../library/src/commonMain/kotlin/com/contextable/a2ui4k/model/UiEvent.kt) — `ActionEvent`, `ValidationError`, `ClientError`, `toClientMessage(version)`.
- [library/src/commonMain/kotlin/com/contextable/a2ui4k/extension/A2UIClientCapabilities.kt](../library/src/commonMain/kotlin/com/contextable/a2ui4k/extension/A2UIClientCapabilities.kt) — capability helpers.
- [library/src/commonMain/kotlin/com/contextable/a2ui4k/data/DataModel.kt](../library/src/commonMain/kotlin/com/contextable/a2ui4k/data/DataModel.kt) — `update`, `updateString`, `updateNumber`, `updateBoolean`, `delete`.
- [A2UI v0.9 specification](https://github.com/google/A2UI/tree/main/specification/0.9) — upstream protocol definition.
