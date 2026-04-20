# Skill: Adopt the A2UI v0.9 wire protocol in an a2ui-4k client app (alongside v0.8)

Switch a Kotlin/Compose client app that already depends on `com.contextable:a2ui-4k`
0.9.x from **v0.8-only** capability negotiation to **v0.9 + v0.8 (negotiate, prefer v0.9)**.
After this skill is applied, the app accepts v0.9 surfaces from new agents while continuing
to render v0.8 surfaces from legacy agents on the same code path.

### Library version vs. A2UI protocol version — read this first

This skill is **about the wire protocol the client speaks at runtime**. It does *not*
change the Kotlin/Compose API the app compiles against — that is the library version.
The two axes are independent:

| Axis | What it means | Changed by this skill? |
|---|---|---|
| **Library version** (`a2ui-4k` 0.8.x → 0.9.x) | The Kotlin/Compose API the app compiles against — class names, method signatures, event types. | **No.** Pre-condition: app must already be on the 0.9.x library. If it is on 0.8.x, run the `migrate-0.8-to-0.9` skill first. |
| **A2UI protocol version** (v0.8 / v0.9 wire format) | What the server sends over the wire and the client parses back. Selected at runtime via capability negotiation per agent, then per surface. | **Yes — this is the entire point.** |

Why these are independent: the 0.9.x library carries a hybrid transcoder
(`V08MessageTranscoder`) that converts inbound v0.8 envelopes to v0.9 shape internally,
and the per-surface `ProtocolVersion` tag drives outbound event serialization back to
whichever wire version each surface negotiated. So a 0.9.x library on the classpath
can advertise v0.9, v0.8, or both without changing any rendering code.

This file is a plain Markdown skill. It is intended to be usable by any coding agent
(Aider, Cline, Cursor, Copilot Workspace, Claude Code, …) and also reads top-to-bottom
as a human guide.

---

## 1. When to apply this skill

Apply this skill when **all** of the following are true for the target app:

- It depends on `com.contextable:a2ui-4k:0.9.*` (or platform variants).
- Its current capability negotiation advertises **only v0.8** —
  i.e., the codebase calls `a2uiV08StandardClientCapabilities()` or hand-builds
  `A2UIClientCapabilities(supportedCatalogIds = listOf(STANDARD_CATALOG_URI_V08))`.
- The goal is to also accept v0.9 surfaces from agents that emit them, while
  preserving v0.8 fallback for legacy agents.

Strong signals the app is currently v0.8-only on the wire (grep these):

| Pattern | File types |
|---|---|
| `a2uiV08StandardClientCapabilities\(` | `*.kt` |
| `STANDARD_CATALOG_URI_V08` (without also referencing `STANDARD_CATALOG_URI`) | `*.kt` |
| `URI_V08` in extension-metadata construction | `*.kt` |
| `PROTOCOL_VERSION_V08` literal use in outbound payloads | `*.kt` |

Do **not** apply this skill when:

- The app is on the 0.8.x library — it cannot speak v0.9 at all. Run
  `migrate-0.8-to-0.9` first.
- The app is the server/agent side. a2ui-4k is a **client** rendering library;
  server protocol upgrades are out of scope.
- The app already calls `a2uiStandardClientCapabilities()` (v0.9-only) or
  `a2uiBothVersionsClientCapabilities()`. There is nothing to do.
- The user wants to **drop v0.8 support entirely** (v0.9-only). That is a
  different posture — see `## 6. If you actually want v0.9-only` at the end of
  this file.

---

## 2. Prerequisites

Before making edits:

1. The target app builds cleanly against `com.contextable:a2ui-4k:0.9.*`.
   Confirm with `./gradlew build` (or the app's equivalent).
2. The app's capability call site is reachable — i.e., it calls one of the
   `a2uiClientCapabilities` helpers from a known location and attaches the
   result to outbound A2A message metadata. If you cannot find this call,
   stop and ask the user where capabilities are wired.
3. Working tree is clean so the agent's changes are diffable.

---

## 3. What the library already does for you (do not reimplement)

The 0.9.x library handles the wire-level differences automatically. Specifically,
**you do not need to**:

- Detect whether an inbound envelope is v0.8 or v0.9. `SurfaceStateManager.processMessage`
  inspects the envelope and dispatches to the v0.8 transcoder or the v0.9 path itself
  (see [SurfaceStateManager.kt:107](../library/src/commonMain/kotlin/com/contextable/a2ui4k/state/SurfaceStateManager.kt#L107)).
- Track which version each surface is speaking. The manager tags every surface at
  `createSurface` time with `ProtocolVersion.V0_8` or `ProtocolVersion.V0_9` based
  on the `catalogId` the server picked, and exposes it via
  `getSurfaceProtocolVersion(surfaceId)`.
- Serialize outbound events differently per version. `event.toClientMessage(version)`
  produces `{"version":"v0.9","action":{…}}` or `{"version":"v0.9","error":{…}}` for
  v0.9 surfaces and `{"userAction":{…}}` / `{"dataChange":{…}}` for v0.8 surfaces
  (see [UiEvent.kt:156](../library/src/commonMain/kotlin/com/contextable/a2ui4k/model/UiEvent.kt#L156)).
- Translate v0.8 widget shapes (`{"Text":{...}}`, `{"literalString":"x"}`,
  `{"explicitList":[...]}`, `Button.primary:true`, `Button.action:{name:"x"}`,
  `MultipleChoice`/`SingleChoice`, `TextField.text`, `validationRegexp`, `usageHint`,
  etc.) into v0.9 equivalents. `V08MessageTranscoder` and the v0.8 prop aliases on
  the standard widgets cover this.

Concretely, this skill changes **capability advertisement and a couple of small
outbound-routing call sites**. It does not touch rendering, components, or transports.

---

## 4. Migration steps

Each step is one mechanical change. Apply them in order.

### Step 1 — Switch the capability helper

**Detect:** call site of `a2uiV08StandardClientCapabilities()` or hand-built
`A2UIClientCapabilities(supportedCatalogIds = listOf(STANDARD_CATALOG_URI_V08))`.

**Before:**

```kotlin
import com.contextable.a2ui4k.extension.*

val caps = a2uiV08StandardClientCapabilities()
```

**After:**

```kotlin
import com.contextable.a2ui4k.extension.*

// Advertise both v0.9 and v0.8 standard catalogs. The agent picks; the
// library renders whichever it chose.
val caps = a2uiBothVersionsClientCapabilities()
```

If the app uses `a2uiClientCapabilities(vararg catalogIds)` for a custom catalog
list, add `A2UIExtension.STANDARD_CATALOG_URI` (the v0.9 entry) to the list while
preserving the v0.8 entry. Order matters: list v0.9 first to express preference.

```kotlin
val caps = a2uiClientCapabilities(
    A2UIExtension.STANDARD_CATALOG_URI,         // v0.9 — preferred
    A2UIExtension.STANDARD_CATALOG_URI_V08,     // v0.8 — fallback
    "https://my-company.com/a2ui/custom_catalog.json"
)
```

**Check:** the helper call returns an `A2UIClientCapabilities` whose
`supportedCatalogIds` contains both URIs.

---

### Step 2 — Advertise both extension URIs in A2A metadata (if the app sets them explicitly)

**Detect:** code that constructs the A2A `extensions` list with
`A2UIExtension.URI_V08` only.

Some apps include the extension URI list explicitly when handshaking with the agent
(separately from the catalog list). If yours does, advertise both URIs so the agent
can negotiate either version.

**Before:**

```kotlin
val extensions = listOf(A2UIExtension.URI_V08)
```

**After:**

```kotlin
val extensions = listOf(A2UIExtension.URI_V09, A2UIExtension.URI_V08)
```

If the app does not set extensions explicitly (relying entirely on
`supportedCatalogIds`), skip this step.

**Check:** any extension-URI advertisement includes both `URI_V09` and `URI_V08`.

---

### Step 3 — Verify outbound events use the per-surface protocol version

**Detect:** every call site of `UiEvent.toClientMessage(...)` (or a hand-built
outbound message wrapping `ActionEvent` / `DataChangeEvent` / `ValidationError` /
`ClientError`).

When the app speaks only v0.8, calling `toClientMessage()` with no argument or with
a hard-coded `ProtocolVersion.V0_8` is harmless — every surface is v0.8. Once the
app accepts v0.9 surfaces too, the outbound serializer must look up the version
**of the surface the event came from**, not assume one. Otherwise a v0.9 button
click ships as a `{"userAction":{…}}` envelope and the v0.9 agent rejects it (and
vice versa).

**Before:**

```kotlin
// Implicit default — was OK when only v0.8 surfaces existed.
val msg = event.toClientMessage()

// Or hard-coded:
val msg = event.toClientMessage(ProtocolVersion.V0_8)
```

**After:**

```kotlin
val version = manager.getSurfaceProtocolVersion(event.surfaceId)
    ?: ProtocolVersion.V0_9   // surface unknown — default to v0.9 wire shape
val msg = event.toClientMessage(version)
```

If the bridge does not have a reference to the `SurfaceStateManager` at the event
emission site, plumb one through. The lookup is `O(1)`.

Notes:

- `DataChangeEvent.toClientMessage(...)` returns `null` for v0.9 surfaces
  (v0.9 has no upstream data-change message; local changes ride along in
  `a2uiClientDataModel` metadata — see Step 4). Treat `null` as "do not emit"
  rather than as an error.
- `ValidationError` / `ClientError`. `toClientMessage(...)` returns `null` for v0.8
  surfaces (v0.8 has no formal client-error wire shape). Log locally in that case.

**Check:** every `toClientMessage` call passes a `ProtocolVersion` derived from
`getSurfaceProtocolVersion(surfaceId)`, not a hard-coded constant.

---

### Step 4 — Attach `a2uiClientDataModel` metadata to outbound messages

**Detect:** the outbound transport bridge (the layer that puts A2A messages on the
wire). Look for where extension metadata is assembled.

v0.9 introduces a `sendDataModel: Boolean` flag on `createSurface`. When a server
sets it, the client is expected to include the surface's full data model in the
metadata of **every** outbound message under the key `a2uiClientDataModel`. v0.8
has no equivalent — the library will simply not produce one for v0.8 surfaces.

`SurfaceStateManager.buildClientDataModel()` returns the assembled envelope (filtered
to `sendDataModel == true && protocolVersion == V0_9` surfaces) or `null` if no
surface qualifies.

**Before (typical v0.8-only bridge — no data-model envelope):**

```kotlin
val metadata = mapOf(
    "a2uiClientCapabilities" to capsJson
)
```

**After:**

```kotlin
val metadata = buildMap {
    put("a2uiClientCapabilities", capsJson)
    manager.buildClientDataModel()?.let { put("a2uiClientDataModel", it) }
}
```

If no v0.9 surface ever sets `sendDataModel: true`, `buildClientDataModel()` returns
`null` and the metadata stays the same as before — there is no harm in adding the
call unconditionally.

**Check:** the outbound metadata includes `a2uiClientDataModel` whenever
`buildClientDataModel()` is non-null.

---

### Step 5 — Be ready to receive `ValidationError` / `ClientError`

**Detect:** the app's `UiEvent` handler (the `when (event)` block that processes
events emitted by `A2UISurface`).

v0.9 input widgets can fire two new event types:

- `ValidationError(code = "VALIDATION_FAILED", surfaceId, path, message)` — a
  `CheckRule` on a `TextField` / `ChoicePicker` failed.
- `ClientError(code, surfaceId, message)` — any other client-side error worth
  reporting upstream.

Both are emitted only for v0.9 surfaces. v0.8 surfaces never produce them. If the
app's event handler was authored against v0.8 only, it likely doesn't have branches
for these types and may default to "unknown event — log and drop". Add explicit
branches so they round-trip to the agent.

**Before:**

```kotlin
when (event) {
    is ActionEvent -> sendAction(event)
    is DataChangeEvent -> sendDataChange(event)
    else -> log.w("unhandled event: $event")
}
```

**After:**

```kotlin
when (event) {
    is ActionEvent -> sendAction(event)
    is DataChangeEvent -> sendDataChange(event)
    is ValidationError, is ClientError -> {
        // toClientMessage(V0_9) emits {"version":"v0.9","error":{…}}.
        // For v0.8 surfaces it returns null; log locally in that case.
        val version = manager.getSurfaceProtocolVersion(event.surfaceId)
            ?: ProtocolVersion.V0_9
        event.toClientMessage(version)?.let { transport.send(it) }
            ?: log.w("client error on v0.8 surface (no wire shape): $event")
    }
}
```

**Check:** the event handler has explicit branches for `ValidationError` and
`ClientError` that route to the same outbound transport as `ActionEvent`.

---

## 4a. The catalog the agent picked drives everything

A single point worth internalizing, because nothing in client code makes it explicit:
**which protocol a given surface speaks is decided by the agent, per surface, at
`createSurface` time, based on the catalog URI it chose from `supportedCatalogIds`.**
The library tags the surface accordingly:

| `createSurface.catalogId` | Surface tagged as | Outbound shape |
|---|---|---|
| `STANDARD_CATALOG_URI` (v0.9) | `ProtocolVersion.V0_9` | `{"version":"v0.9","action":{…}}` etc. |
| `STANDARD_CATALOG_URI_V08` (v0.8) | `ProtocolVersion.V0_8` | `{"userAction":{…}}` etc. |
| Custom URI | Inherits the source envelope's version (v0.9 if the envelope was native, v0.8 if it came in via the transcoder) | Per source |
| Absent | Inherits the source envelope's version | Per source |

(See [SurfaceStateManager.kt:154-176](../library/src/commonMain/kotlin/com/contextable/a2ui4k/state/SurfaceStateManager.kt#L154-L176).)

This is also why advertising both URIs is **forward-compatible**: an agent that only
knows v0.8 will pick the v0.8 catalog and the surface will be v0.8-tagged; an agent
that knows v0.9 will pick the v0.9 catalog and the surface will be v0.9-tagged. A
mixed deployment can have both kinds of surfaces in flight simultaneously, and the
per-surface tag keeps outbound events consistent.

---

## 5. Verification

After the steps above, run the app's existing build and tests. Then run a
two-version smoke test:

1. **Inbound v0.9.** Feed `SurfaceStateManager.processMessage` a known-good v0.9
   `createSurface` envelope:

   ```kotlin
   val env = Json.parseToJsonElement("""
     {"version":"v0.9","createSurface":{
        "surfaceId":"s1",
        "catalogId":"https://github.com/google/A2UI/blob/main/specification/v0_9/json/standard_catalog.json"
     }}
   """).jsonObject
   assertTrue(manager.processMessage(env))
   assertEquals(ProtocolVersion.V0_9, manager.getSurfaceProtocolVersion("s1"))
   ```

2. **Inbound v0.8.** Feed it the same kind of v0.8 `ACTIVITY_SNAPSHOT` envelope the
   app already handles. Assert `getSurfaceProtocolVersion(...)` returns
   `ProtocolVersion.V0_8` for that surface.

3. **Outbound round-trip.** Build an `ActionEvent` for each surface, call
   `toClientMessage(getSurfaceProtocolVersion(surfaceId)!!)`, and assert:
   - The v0.9 surface's message has top-level `"version":"v0.9"` and an `"action"` key.
   - The v0.8 surface's message has top-level `"userAction"` and **no** `version` key.

4. **Capability advertisement.** Inspect the outbound A2A metadata the bridge
   produces on the first message. Assert `a2uiClientCapabilities.supportedCatalogIds`
   contains both `STANDARD_CATALOG_URI` and `STANDARD_CATALOG_URI_V08`.

If all four pass, the app speaks both protocols correctly. If only one direction
works, re-check Step 1 (capability advertisement) and Step 3 (outbound serializer).

### "Builds pass but v0.9 surfaces don't render"

If the app compiles and v0.8 surfaces still render but v0.9 surfaces silently fail,
the bug is almost always in the bridge layer (capabilities not propagated, or the
outbound `version` field missing from v0.9 messages so the agent rejects the response).
Run these checks:

1. **Did the agent actually pick v0.9?** Log the `catalogId` on every inbound
   `createSurface`. If it is the v0.8 URI, the agent never saw the v0.9 advertisement
   — re-check Step 1 (capability call site reachable, helper actually invoked).
2. **Is the outbound message a v0.9 envelope?** Log the JSON the transport sends for
   one button click on a v0.9 surface. It must start with `{"version":"v0.9","action":…}`.
   If it starts with `{"userAction":…}`, Step 3 wasn't applied at that call site.
3. **Is `a2uiClientDataModel` attached when expected?** If the v0.9 agent set
   `sendDataModel: true`, every outbound message must carry the envelope. Confirm
   Step 4 is in the bridge.

---

## 6. If you actually want v0.9-only

This skill assumes "both, prefer v0.9" — the recommended posture for any deployment
that still has v0.8 agents in the field. If the user instead wants to drop v0.8
entirely (e.g., they control both ends and have cut all servers over to v0.9):

- Step 1 becomes `val caps = a2uiStandardClientCapabilities()` (v0.9-only).
- Step 2 advertises only `URI_V09`.
- Step 3 still applies — but every surface will be `V0_9`, so the lookup degenerates
  to a constant. Leave the lookup in for safety.
- Steps 4 and 5 still apply unchanged.
- Surfaces from any remaining v0.8-only agent will fail to negotiate. That is the
  trade-off the user is choosing.

If the user is unsure which posture they want, default to "both" — it is
strictly more permissive and the runtime cost is negligible.

---

## 7. What NOT to change

These patterns are **still correct** after applying this skill:

- **`ProtocolVersion.V0_8` references.** Still used internally and by the outbound
  serializer to tag/handle legacy surfaces.
- **`STANDARD_CATALOG_URI_V08` and `URI_V08` constants.** Keep them — they appear
  in the both-versions capability list.
- **`A2UIExtension.PROTOCOL_VERSION` (= `"v0.9"`) and `PROTOCOL_VERSION_V08`
  (= `"v0.8"`).** Both still exported.
- **The v0.8 transcoder dispatch path in `processMessage`.** Do not bypass it; do
  not "pre-detect" v0.8 envelopes in the bridge. `processMessage` does the right
  thing.
- **v0.8 widget prop aliases** (`label`, `usageHint`, `text`, `textFieldType`,
  `primary: true`, `validationRegexp`, etc.) on hand-built test fixtures. The 0.9.x
  standard widgets accept them transparently.
- **Hand-built v0.8 envelopes used in tests** (`ACTIVITY_SNAPSHOT` / `ACTIVITY_DELTA`).
  They still drive the v0.8 code path verbatim and are useful for regression coverage.

---

## 8. Reference: wire-level v0.8 → v0.9 differences

Quick reference for what changes on the wire. The library handles all of these
internally — this table exists to let humans debug logs and unfamiliar payloads.

### Server → client envelopes

| v0.8 | v0.9 |
|---|---|
| Two top-level message kinds: `ACTIVITY_SNAPSHOT` (full state with `operations: [...]`), `ACTIVITY_DELTA` (JSON-Patch over the prior snapshot keyed by `messageId`). | Discrete, top-level operation per message: `{"version":"v0.9", "<op>":{…}}` with exactly one of `createSurface`, `updateComponents`, `updateDataModel`, `deleteSurface`. No JSON-Patch step. |
| `beginRendering` op inside the operations array. | `createSurface` (renamed; same semantic). |
| `surfaceUpdate` op carries `components: { "id1": {"Text": {...}}, "id2": {"Button": {...}} }` keyed by component id, components wrapped by widget-name key. | `updateComponents` carries `components: [ {"id": "id1", "component": "Text", ...}, {"id": "id2", "component": "Button", ...} ]` — flat list with `component` discriminator. |
| `dataModelUpdate.contents: [{path, value}, ...]` — multiple updates per op. | `updateDataModel` carries one `path` + one `value` per op (or no `path` to replace whole model). |
| No `version` field on the envelope. | `version: "v0.9"` required at top level. |

### Component literals & references inside payloads

| v0.8 | v0.9 |
|---|---|
| `{"literalString": "Hello"}` | `"Hello"` (raw JSON string) |
| `{"literalNumber": 42}` | `42` (raw JSON number) |
| `{"literalBoolean": true}` | `true` (raw JSON boolean) |
| `{"dataBinding": {"path": "/x"}}` | `{"path": "/x"}` |
| `{"explicitList": ["a", "b"]}` for children | `["a", "b"]` (raw JSON array) |
| (none) | `{"call": "fnName", "args": {…}}` for function-call references |

### Widget catalog deltas

| v0.8 | v0.9 |
|---|---|
| `Button.primary: true` | `Button.variant: "primary"` (also `"borderless"`) |
| `Button.action: {"name": "x", "context": [{"key":"k","value":"v"}]}` | `Button.action: {"event": {"name": "x", "context": {"k":"v"}}}` — wrapped + flat object context |
| `Text.usageHint`, `Image.usageHint` | `Text.variant`, `Image.variant` |
| `MultipleChoice` with `maxAllowedSelections > 1` | `ChoicePicker` with `variant: "multipleSelection"` |
| `SingleChoice` | `ChoicePicker` with `variant: "mutuallyExclusive"` |
| `TextField.text` (initial value) | `TextField.value` |
| `TextField.textFieldType` | `TextField.variant` |
| `TextField.validationRegexp` | `TextField.checks: [CheckRule(condition, message)]` (richer) |
| (none) | `Accessibility(label, description)` on input widgets |

### Client → server messages

| v0.8 | v0.9 |
|---|---|
| `{"userAction": {name, surfaceId, sourceComponentId, timestamp, context: [{key,value},...]}}` — no version envelope, context as kv array. | `{"version":"v0.9","action":{name, surfaceId, sourceComponentId, timestamp, context: {…flat object…}}}` |
| `{"dataChange": {surfaceId, path, value}}` — explicit upstream data change message. | No equivalent. Local data changes are pushed via the `a2uiClientDataModel` metadata envelope (see below) when `sendDataModel: true`. |
| Generic `error` message (loose shape). | `{"version":"v0.9","error":{code, surfaceId, path?, message}}` — structured. `code == "VALIDATION_FAILED"` for validation, anything else is `ClientError`. |

### Capability negotiation & metadata

| v0.8 | v0.9 |
|---|---|
| Extension URI: `https://a2ui.org/a2a-extension/a2ui/v0.8` | `https://a2ui.org/a2a-extension/a2ui/v0.9` |
| Standard catalog: `https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json` | `https://github.com/google/A2UI/blob/main/specification/v0_9/json/standard_catalog.json` |
| `a2uiClientCapabilities.supportedCatalogIds` shipped in metadata. | Same key, same shape — but list both URIs to negotiate. |
| (none) | `a2uiClientDataModel` metadata envelope: `{version:"v0.9", surfaces: {<surfaceId>: <data>}}`. Emitted from `SurfaceStateManager.buildClientDataModel()`; only includes surfaces with `sendDataModel == true && protocolVersion == V0_9`. |

---

## 9. Sources

Authoritative references inside the a2ui-4k repository:

- [library/src/commonMain/kotlin/com/contextable/a2ui4k/extension/A2UIClientCapabilities.kt](../library/src/commonMain/kotlin/com/contextable/a2ui4k/extension/A2UIClientCapabilities.kt) — `a2uiBothVersionsClientCapabilities` and friends.
- [library/src/commonMain/kotlin/com/contextable/a2ui4k/extension/A2UIExtension.kt](../library/src/commonMain/kotlin/com/contextable/a2ui4k/extension/A2UIExtension.kt) — `URI_V09`, `URI_V08`, `STANDARD_CATALOG_URI`, `STANDARD_CATALOG_URI_V08` constants.
- [library/src/commonMain/kotlin/com/contextable/a2ui4k/state/SurfaceStateManager.kt](../library/src/commonMain/kotlin/com/contextable/a2ui4k/state/SurfaceStateManager.kt) — dispatch (`processMessage`), per-surface tagging (`handleCreateSurface`), `getSurfaceProtocolVersion`, `buildClientDataModel`.
- [library/src/commonMain/kotlin/com/contextable/a2ui4k/model/UiEvent.kt](../library/src/commonMain/kotlin/com/contextable/a2ui4k/model/UiEvent.kt) — `toClientMessage(version)` and the v0.8/v0.9 outbound shapes.
- [library/src/commonMain/kotlin/com/contextable/a2ui4k/model/ProtocolVersion.kt](../library/src/commonMain/kotlin/com/contextable/a2ui4k/model/ProtocolVersion.kt) — the `ProtocolVersion` enum.
- [library/src/commonMain/kotlin/com/contextable/a2ui4k/protocol/v08/V08MessageTranscoder.kt](../library/src/commonMain/kotlin/com/contextable/a2ui4k/protocol/v08/V08MessageTranscoder.kt) — inbound v0.8 → v0.9 transcoding (read this if a v0.8 envelope is mysteriously rejected).
- [skills/migrate-0.8-to-0.9.md](migrate-0.8-to-0.9.md) — companion skill for the *library* upgrade (pre-condition for this one).
- [docs/protocol/deprecated-versions.md](../docs/protocol/deprecated-versions.md) — wire-level v0.8 ↔ v0.9 diff with prop-alias table.
- [A2UI v0.9 specification](https://github.com/google/A2UI/tree/main/specification/0.9) — upstream protocol definition.
