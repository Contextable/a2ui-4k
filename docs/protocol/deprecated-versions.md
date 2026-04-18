# Deprecated Protocol Versions

a2ui-4k implements the [A2UI v0.9 specification](https://github.com/google/A2UI/tree/main/specification/0.9)
natively. Earlier protocol versions are **deprecated but supported** through a
hybrid transcoder, so existing v0.8 agents continue to render correctly while
clients move to v0.9.

This page documents what changed, what is still supported, and how the
backwards-compatibility layer works.

## Status at a glance

| Version | Status | Native? | Notes |
|---------|--------|---------|-------|
| **v0.9** | Current | ✓ | All operations, validation, accessibility, and the wire envelope are first-class. |
| **v0.8** | Deprecated, supported | — | Server messages are transcoded to v0.9 internally; outbound events serialize back to the v0.8 wire shape. |
| Earlier  | Unsupported | — | a2ui-4k has never targeted pre-v0.8 protocol drafts. |

## What changed between v0.8 and v0.9

Per Google, v0.8 and v0.9 are **not wire-compatible**. The major shifts:

| Area | v0.8 | v0.9 |
|------|------|------|
| Wire envelope | `ACTIVITY_SNAPSHOT` / `ACTIVITY_DELTA` carrying batched operations; deltas are RFC 6902 JSON Patches. | Per-operation messages tagged `{"version":"v0.9", "<op>":{…}}`. Streaming is a sequence of these envelopes. |
| Operations | `beginRendering`, `surfaceUpdate`, `dataModelUpdate` (with `contents:[DataEntry]`), `deleteSurface` | `createSurface`, `updateComponents`, `updateDataModel` (single `path`+`value`; omitting `value` deletes), `deleteSurface` |
| Component shape | Nested discriminator: `{"id":"x","component":{"Button":{…}}}` | Flat discriminator: `{"id":"x","component":"Button", …props…}` |
| Data references | Boxed literals `{"literalString":"…"}`, `{"literalNumber":42}`, `{"literalBoolean":true}`; `{"dataBinding":{…}}` for paths | Plain JSON primitives are literals; `{"path":"/x"}` for bindings; new `{"call":"fn","args":{…}}` for function calls |
| Children | `{"explicitList":[…]}`, `{"template":{"dataPath":"/items", …}}` | Plain `["id1","id2"]` array, `{"explicitList":[…]}`, or `{"template":{"path":"/items","componentId":"…"}}` |
| Root | Explicit `root` property on `beginRendering` | Convention: the component with id `"root"` |
| Action events | `userAction`, no version envelope | `action` inside `{"version":"v0.9","action":{…}}` envelope |
| Errors | No formal client error wire shape | `error` envelope with two variants: `VALIDATION_FAILED` and any other code |
| Validation | None at the protocol level | First-class `CheckRule` (`condition` + `message`) on input widgets |
| Accessibility | Ad-hoc per widget | Shared `Accessibility` block (`label`, `description`) on inputs |
| Choice widgets | Separate `MultipleChoice` / `SingleChoice` | Single `ChoicePicker` with `multipleSelection` / `mutuallyExclusive` variants |

## How the v0.8 hybrid works

Clients only need one entry point — `SurfaceStateManager.processMessage` —
regardless of which version the server speaks.

```
┌────────────────────┐    v0.9 envelope     ┌────────────────────────┐
│ agent (v0.9)       │─────────────────────▶│                        │
└────────────────────┘                      │                        │
                                            │  SurfaceStateManager   │
┌────────────────────┐ ACTIVITY_SNAPSHOT /  │  (single dispatcher)   │
│ agent (v0.8)       │─── ACTIVITY_DELTA ──▶│                        │
└────────────────────┘                      └─────────┬──────────────┘
                                                      │
                          ┌───────────────────────────┴────────────────────┐
                          │ V08MessageTranscoder                            │
                          │  • recognizes v0.8 envelopes                    │
                          │  • applies RFC 6902 patch for DELTA             │
                          │  • V08ComponentFlattener: nested → flat,        │
                          │    boxed literals → primitives, MultipleChoice/ │
                          │    SingleChoice → ChoicePicker variants         │
                          │  • emits zero-or-more v0.9-shape op messages    │
                          └────────────────────────────────────────────────┘
```

Each surface is tagged with a `ProtocolVersion` (`V0_8` or `V0_9`). When the
client sends an event back, `UiEvent.toClientMessage(version)` serializes to
the matching wire shape:

| Event | Under V0_9 | Under V0_8 |
|-------|------------|------------|
| `ActionEvent` | `{"version":"v0.9","action":{…}}` | `{"userAction":{…}}` (no version envelope) |
| `DataChangeEvent` | `null` (local-only in v0.9; full data model rides on next `action` as `a2uiClientDataModel`) | `{"dataChange":{…}}` (real wire message) |
| `ValidationError` | `{"version":"v0.9","error":{"code":"VALIDATION_FAILED",…}}` | `null` (v0.8 has no formal error shape; log locally) |
| `ClientError` | `{"version":"v0.9","error":{…}}` | `null` |

To send the right shape automatically:

```kotlin
val v = stateManager.getSurfaceProtocolVersion(event.surfaceId) ?: ProtocolVersion.V0_9
event.toClientMessage(v)?.let(::sendToAgent)
```

## v0.8-only props still accepted by widgets

For convenience the widgets accept a small set of v0.8 property aliases even
when running against a v0.9 surface — useful when transcribing legacy JSON or
serving mixed agents:

| Widget | v0.8 alias | v0.9 equivalent |
|--------|------------|-----------------|
| `Button` | `label` | `child` (renders as text when `child` is omitted) |
| `Button` | `usageHint` | `variant` |
| `Row` / `Column` | `distribution` | `justify` |
| `Row` / `Column` | `alignment` | `align` |
| `Row` / `Column` | `spaceEvenly` | (still recognized) |
| `Image` | `usageHint` | `variant` |
| `Image` | `scale-down` | `scaleDown` (kebab→camel) |
| `Image` | `icon` / `smallFeature` / `mediumFeature` / `largeFeature` / `header` | v0.8 size keywords still mapped |
| `TextField` | `text` | `value` |
| `TextField` | `textFieldType` | `variant` |
| `TextField` | `date` variant | mapped to v0.9 date input |

## Capability negotiation

When the agent supports both versions, your client can advertise either or
both:

```kotlin
import com.contextable.a2ui4k.extension.*

// Prefer v0.9, accept v0.8 fallback
val caps = a2uiBothVersionsClientCapabilities()

// v0.9 only (default for new apps)
val caps = a2uiStandardClientCapabilities()

// v0.8 only (legacy clients)
val caps = a2uiV08StandardClientCapabilities()
```

The agent picks a catalog (and therefore wire version); `processMessage`
handles whichever one shows up.

## When to drop v0.8 support

The hybrid transcoder is small and well-isolated — primarily
`protocol/v08/` plus a handful of widget-prop aliases. There is no immediate
plan to remove it, but it will be deprecated in a future major release once
v0.8 agent traffic in the field has trailed off.

## See Also

- [State Management](../core-concepts/state-management.md) — single-entry dispatch
- [Events](../core-concepts/events.md) — `toClientMessage(version)` details
- [A2UI Extension](../api-reference/agent-extension.md) — capability negotiation
- [A2UI v0.9 specification](https://github.com/google/A2UI/tree/main/specification/0.9)
