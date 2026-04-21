# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.9.3] - 2026-04-21

### Added
- **`A2UiRenderTool`** in new `com.contextable.a2ui4k.agent` package — a
  transport-agnostic, SDK-agnostic helper that packages the canonical
  `createSurface` + `updateComponents` + optional `updateDataModel` flow
  behind a single `render(JsonObject): JsonObject` call. Ships tool
  `name`, `description`, and JSON-Schema `parameters` ready to plug into
  any agent tool-calling SDK (AG-UI Kotlin, Gemini, OpenAI, Anthropic)
  via a ~4-line consumer adapter. Replaces bespoke `ACTIVITY_SNAPSHOT`
  handlers that call `SurfaceStateManager.processMessage` directly,
  closing the tool-result round-trip so ag_ui_adk's HITL loop resumes
  cleanly. No new dependencies; the library remains transport-agnostic.
  ([library/src/commonMain/kotlin/com/contextable/a2ui4k/agent/A2UiRenderTool.kt](library/src/commonMain/kotlin/com/contextable/a2ui4k/agent/A2UiRenderTool.kt))
- **`A2UiRenderException`** (same package) — public typed exception with
  a structured `ValidationCode` enum and `field` name, thrown by
  `A2UiRenderTool.render` on malformed tool-call arguments. Consumer
  adapters catch it and map to their SDK's native failure result.
- **`A2UIExtension.BASIC_CATALOG_URI_V09`** constant
  (`https://a2ui.org/specification/v0_9/basic_catalog.json`) and
  corresponding `SurfaceStateManager.processMessage` branch-match.
  Agents that emit the `a2ui.org` URI now resolve cleanly to
  `ProtocolVersion.V0_9`, alongside the existing `STANDARD_CATALOG_URI`.
  ([library/src/commonMain/kotlin/com/contextable/a2ui4k/extension/A2UIExtension.kt](library/src/commonMain/kotlin/com/contextable/a2ui4k/extension/A2UIExtension.kt))

## [0.9.2] - 2026-04-19

### Fixed
- **`A2UISurface` now renders v0.8 surfaces whose root component has an id
  other than `"root"`.** Previously the renderer invoked `ComponentBuilder`
  with a hardcoded `componentId = "root"`, ignoring `rootComponentId` even
  though `UiDefinition.rootComponent` resolved it correctly. Any v0.8 agent
  whose `beginRendering.root` named a custom id (e.g., `"header"`) rendered
  as "Missing component: root" instead of the actual UI tree. The renderer
  now uses `rootComponent.id`, which respects `rootComponentId` for v0.8
  compat and falls back to the `"root"` convention for v0.9.
  ([library/src/commonMain/kotlin/com/contextable/a2ui4k/render/A2UiSurface.kt](library/src/commonMain/kotlin/com/contextable/a2ui4k/render/A2UiSurface.kt))
- **v0.8 Button action shape is now transcoded to v0.9.** The v0.8 wire
  format put the action payload directly on `action` as
  `{"name": "...", "context": [{"key": "...", "value": ...}]}`; v0.9
  wraps it in an `event` discriminator and uses a flat `context` object.
  Because `V08ComponentFlattener` previously only unwrapped values and
  didn't rewrite property structure, any v0.8 Button click dispatched an
  `ActionEvent` with `name = "click"` and `context = null` — the action
  name and bindings were silently dropped. The flattener now rewrites
  `action` to `{"event": {"name": ..., "context": {...}}}` and converts
  the v0.8 array-of-pairs context into a flat JSON object. Honors
  `event` / `functionCall` when already set so native v0.9 actions pass
  through untouched.
  ([library/src/commonMain/kotlin/com/contextable/a2ui4k/protocol/v08/V08ComponentFlattener.kt](library/src/commonMain/kotlin/com/contextable/a2ui4k/protocol/v08/V08ComponentFlattener.kt))
- **v0.8 Button `primary: true` → v0.9 `variant: "primary"`.** Added to
  the flattener's Button-specific rewrites. An explicit `variant` takes
  precedence over the legacy boolean.
- **`Button.sourceComponentId` no longer double-prefixes template keys.**
  The widget unconditionally prepended `"item"` to the template key, so
  clicking a button inside a data map keyed `{item1: …, item5: …}`
  produced `"btn:itemitem5"` instead of `"btn:item5"`. Now uses the key
  verbatim.
  ([library/src/commonMain/kotlin/com/contextable/a2ui4k/catalog/widgets/ButtonWidget.kt](library/src/commonMain/kotlin/com/contextable/a2ui4k/catalog/widgets/ButtonWidget.kt))
- **Empty v0.8 `ACTIVITY_SNAPSHOT` envelopes now return `true` from
  `SurfaceStateManager.processMessage`.** A snapshot with
  `"operations": []` is a legal baseline for subsequent deltas and still
  updates the transcoder's per-`messageId` cache; returning `false`
  earlier mistakenly triggered "envelope rejected" diagnostics in
  callers. `processMessage` now returns `false` only when the envelope
  itself is unrecognized.
  ([library/src/commonMain/kotlin/com/contextable/a2ui4k/state/SurfaceStateManager.kt](library/src/commonMain/kotlin/com/contextable/a2ui4k/state/SurfaceStateManager.kt))
- **`isV08Envelope` is stricter about bare version tags.** Previously any
  envelope with `version: "v0.8"` was treated as v0.8 even when the rest
  of the object was a v0.9 op (`createSurface`, etc.). A version tag
  alone no longer suffices — the envelope must carry a `type` / `kind`
  of `ACTIVITY_SNAPSHOT` / `ACTIVITY_DELTA`, or a bare `operations`
  array. Mis-tagged v0.9 ops now correctly fall through to v0.9
  dispatch, which rejects on version mismatch.

### Tests
- Regression test
  `v0_8 beginRendering with non-default root id preserves rootComponentId
  and resolves rootComponent` — every existing v0.8 transcoder test used
  `"root":"root"`, which is why the renderer bug slipped through.
- New `V08ComponentFlattenerTest` cases: v0.8 Button action with array
  context → v0.9 event-wrapped with flat context object; v0.8 object
  context → wrapped unchanged; already-wrapped `event` / `functionCall`
  actions pass through; `primary: true` → `variant: "primary"`;
  explicit `variant` beats `primary`.
- New `SurfaceStateManagerTest` case: empty v0.8 `ACTIVITY_SNAPSHOT`
  returns `true` and caches the baseline; a follow-up `ACTIVITY_DELTA`
  dispatches the newly-added `beginRendering` op.
- Updated `V08MessageTranscoderTest`: "bare version v0.8 envelope with
  operations list is recognized", "version v0.8 without operations is
  NOT recognized".

### Documentation
- New `skills/migrate-0.8-to-0.9.md` — agent-agnostic migration skill for
  client apps upgrading from `0.8.x` to `0.9.x`. Covers dependency bump
  (with Maven Central version resolution), API renames
  (`processSnapshot`/`processDelta` → `processMessage`,
  `UserActionEvent` → `ActionEvent`, `Component` shape change,
  `DataEntry` removal, etc.), transport-bridge integration (with an ag-ui
  example), outbound event serialization via `toClientMessage(version)`,
  capability helpers, and a runtime diagnostic checklist for "builds
  pass but A2UI content doesn't render".

## [0.9.1] - 2026-04-18

### Added — A2UI v0.9 spec alignment
- **Wire envelope**: incoming server messages are recognized by the `version`
  tag (`"v0.9"`) plus exactly one operation key (`createSurface`,
  `updateComponents`, `updateDataModel`, `deleteSurface`). Outbound client
  events are emitted as `{"version":"v0.9","action":{…}}` /
  `{"version":"v0.9","error":{…}}` envelopes.
- **`SurfaceStateManager.processMessage(JsonObject)`** — single dispatch entry
  point that handles both v0.9 native messages and v0.8 envelopes (transcoded
  internally). Replaces the old `processSnapshot` / `processDelta` API.
- **`CheckRule`** — first-class validation: `condition` + `message`, evaluated
  through `FunctionEvaluator`. Wired into all input widgets (`Button`,
  `TextField`, `CheckBox`, `Slider`, `ChoicePicker`, `DateTimeInput`).
- **`Accessibility`** — common a11y props (`label`, `description` as
  `DataReference<String>?`) honored across input widgets.
- **Error event split**: `UiEvent` now distinguishes `ValidationError`
  (`code == "VALIDATION_FAILED"`) from generic `ClientError` (any other code),
  matching the v0.9 wire shape. Exposes `toClientMessage(version)` to obtain
  the protocol-correct envelope.
- **`a2uiClientDataModel`** metadata produced from v0.9 surfaces with
  `sendDataModel = true`.

### Added — v0.8 backwards compatibility (hybrid)
- **`ProtocolVersion`** enum (`V0_8`, `V0_9`) tracked per surface; outbound
  events serialize in the matching wire shape.
- **`protocol/v08/` package**:
  - `JsonPatch` — full RFC 6902 implementation (add/remove/replace/move/copy/test)
    with a strict RFC 6901 JSON Pointer parser (handles `~0` / `~1` escapes).
  - `V08ComponentFlattener` — unwraps nested `{Button:{…}}` to flat
    `"component":"Button"`, hoists props, unwraps `literalString` /
    `literalNumber` / `literalBoolean` / `dataBinding` / `explicitList` /
    `template`, and rewrites `MultipleChoice` → `ChoicePicker +
    multipleSelection`, `SingleChoice` → `ChoicePicker + mutuallyExclusive`.
  - `V08MessageTranscoder` — stateful per-`messageId` transcoder. Recognises
    `ACTIVITY_SNAPSHOT` / `ACTIVITY_DELTA` envelopes and emits zero-or-more
    v0.9-shape op messages; deltas replay the JSON Patch against the cached
    snapshot and emit only newly-added ops.
- Widget legacy aliases for v0.8 props (Row/Column `distribution`∥`justify`,
  `alignment`∥`align`, `spaceEvenly`; Button `label` fallback,
  `usageHint`∥`variant`; Image `usageHint`∥`variant`, `scale-down`∥`scaleDown`,
  v0.8 size keywords; TextField `text`∥`value`, `textFieldType`∥`variant`,
  `date` variant).
- `A2UIClientCapabilities.inlineCatalogs` and helpers
  `a2uiBothVersionsClientCapabilities()` /
  `a2uiV08StandardClientCapabilities()` for capability negotiation that lets a
  client accept either protocol.
- v0.8 outbound serialization: `ActionEvent` →
  `{"userAction":{…}}` (no version envelope); `DataChangeEvent` becomes a real
  `{"dataChange":{…}}` wire message; `ValidationError`/`ClientError` swallow
  silently (v0.8 has no formal error wire shape).

### Tests
- `JsonPatchTest` (22), `V08ComponentFlattenerTest` (13),
  `V08MessageTranscoderTest` (12) covering RFC 6902, flattening rules, and
  end-to-end transcoding behavior.
- `SurfaceStateManagerTest` cross-version: SNAPSHOT/DELTA dispatch, mixed
  v0.8/v0.9 surfaces coexisting with distinct `protocolVersion` tags, v0.8
  surfaces correctly skipping the `a2uiClientDataModel` envelope.
- `UiEventTest` v0.8 envelope serialization for `ActionEvent` / `DataChangeEvent`.
- Bring `GettingStartedExamplesTest` up to v0.9 API and add envelope assertions.

### Documentation
- README, docs/index, getting-started, events, agent-extension and widget docs
  rewritten for v0.9 (with a "v0.8 backwards compatibility" callout where it
  matters).
- `protocol/v0.8-compliance.md` superseded by
  `protocol/deprecated-versions.md`.

### Chores
- Fix `formatNumber` to apply thousands separators on the integer fast-path and `formatCurrency` to round cents instead of truncating, fixing Kotlin/Native test failures
- Expand test coverage for v0.9.0 features:
  - Add `ChoicePickerWidgetTest` (14 tests): option parsing, variants (`multipleSelection`/`mutuallyExclusive`), value binding (path and inline array), `displayStyle`, `filterable`, and `ComponentDef` integration
  - Add `SurfaceStateManagerTest` (20 tests): all four v0.9 operations (`createSurface`, `updateComponents`, `updateDataModel`, `deleteSurface`), snapshot and delta processing, implicit surface creation, data deletion, surface lifecycle, and `UiDefinition` integration
  - Expand `FunctionEvaluatorTest` (30+ new tests): complete coverage for all 13 standard functions — `regex`, `length`, `numeric`, `and`/`or`/`not`, `formatString`, `formatDate`, `pluralize`, additional `formatCurrency` currencies (EUR, GBP, JPY, CHF), `openUrl`, unknown function handling, and path resolution in args
  - Add `A2UIExtensionParamsTest` (9 tests): serialization round-tripping, default values, and `A2UIExtension` constant verification
  - Add `UiDefinitionExtendedTest` (14 tests): `UiDefinition` (`rootComponent`, `withComponents`, `empty`, metadata), `Catalog` (`empty`, `of`, `+` merge, precedence rules), and `Component` (`create`, `fromComponentDef`, defaults)

## [0.9.0] - 2026-02-14

A2UI protocol upgrade from v0.8 to v0.9. This is a **breaking release** that changes the wire format, operation names, component structure, and data binding system.

### Added
- Function evaluation engine (`FunctionEvaluator`) with 13 standard functions:
  - Validation: `required`, `regex`, `length`, `numeric`, `email`
  - Logic: `and`, `or`, `not`
  - Formatting: `formatString`, `formatNumber`, `formatCurrency`, `formatDate`, `pluralize`
- `FunctionCallReference<T>` data reference type for computed values via `{"call": "fn", "args": {...}}`
- `ValidationError` event type for client-to-server validation reporting (`VALIDATION_FAILED`)
- `DataModel.delete(path)` for removing keys (v0.9: omitting `value` in `updateDataModel` deletes the key)
- `sendDataModel` flag on `createSurface` — when true, client includes full data model in A2A metadata
- `DataReferenceParser.parseStringList()` for v0.9 plain string arrays
- `A2UIExtension.PROTOCOL_VERSION` constant (`"v0.9"`)
- `FunctionEvaluatorTest` covering `formatNumber`, `formatCurrency`, `required`, and `email` functions
- Alpha/beta pre-release publishing channels:
  - `publish.sh --channel alpha` / `--channel beta`
  - Auto-computed version from commit count (e.g., `0.9.0-alpha.3`)
  - CI workflow updated with channel selection dropdown
  - Gradle version override via `-PpublishVersion`

### Changed
- **Operations renamed:**
  - `beginRendering` → `createSurface` (now takes `catalogId` and optional `theme` instead of `root` and `styles`)
  - `surfaceUpdate` → `updateComponents`
  - `dataModelUpdate` → `updateDataModel` (simplified: single `path` + `value` instead of `path` + `contents: List<DataEntry>`)
- **Component format flattened** (v0.8 nested → v0.9 flat discriminator):
  - v0.8: `{"id": "x", "component": {"Text": {"text": {"literalString": "Hi"}}}}`
  - v0.9: `{"id": "x", "component": "Text", "text": "Hi", "variant": "h1"}`
- **Data references simplified** (implicit typing):
  - Plain JSON primitives are now literals (`"hello"`, `42`, `true`)
  - `{"path": "/x"}` for data bindings (unchanged)
  - `{"call": "fn", "args": {...}}` for function calls (new)
  - Removed: `{"literalString": "..."}`, `{"literalNumber": 42}`, `{"literalBoolean": true}`
- **Root component by convention:** root is now the component with `id: "root"` rather than an explicit `root` property on `createSurface`
- **Event format updated:**
  - `UserActionEvent` → `ActionEvent`
  - Action context changed from array of `{key, value}` pairs to flat JSON object
  - Button actions now use `{"event": {"name": "...", "context": {...}}}` envelope
- **Widget changes:**
  - `MultipleChoice` → `ChoicePicker` (supports both single and multiple selection)
  - Button `primary` boolean and `usageHint` replaced by `variant` string (`"filled"`, `"outlined"`, `"text"`, `"elevated"`, `"tonal"`)
  - All widgets updated to parse v0.9 flat property format
- **Component model:**
  - `Component.componentProperties: Map<String, JsonObject>` → `Component.componentType: String` + `Component.properties: JsonObject`
  - `widgetType` and `widgetData` are now non-nullable
- Extension URIs: `A2UIExtension.URI_V08` → `A2UIExtension.URI_V09`
- Standard catalog URI updated to `v0_9/json/standard_catalog.json`
- All existing tests updated for v0.9 format

### Removed
- `DataEntry` class and its nested value types (replaced by direct JSON values in `updateDataModel`)
- `ComponentArrayReference` and `TemplateReference` types (replaced by `ChildrenReference` sealed class)
- `UiDefinition.withRoot()` (root is now by convention)
- `A2UIExtension.URI_V08` constant (replaced by `URI_V09`)
- Debug print statements from `SurfaceStateManager`

### Fixed
- `FunctionEvaluator.evaluateFormatNumber()` used JVM-only `Double.toBigDecimal().toPlainString()` in `commonMain`, breaking JS and iOS compilation. Replaced with multiplatform `doubleToPlainString()` helper.

## [0.8.2] - 2026-02-10

### Added
- iOS artifacts (`iosArm64`, `iosX64`, `iosSimulatorArm64`) now published to Maven Central

### Changed
- Publish workflow switched from Ubuntu to macOS runner to enable iOS artifact builds

## [0.8.1] - 2026-01-15

### Added
- A2UI extension support for protocol negotiation
  - `A2UIClientCapabilities` for declaring supported catalogs
  - `A2UIExtensionParams` for agent capability schema
  - `A2UIExtension` constants: `URI_V08`, `STANDARD_CATALOG_URI`, `MIME_TYPE`
  - Helper functions: `a2uiStandardClientCapabilities()`, `a2uiClientCapabilities()`
- Documentation for extension support
- JavaScript/browser target support
  - Karma test runner with Chrome Headless for JS tests
  - Custom Karma configuration for containerized CI environments
  - Ktor JS client dependency for network operations

### Changed
- Lower Android minSdk from 26 to 24 for broader device compatibility
- Lower Kotlin version from 2.2.21 to 2.1.20 for wider build tool compatibility

## [0.8.0] - 2025-12-28

Initial implementation of the A2UI v0.8 rendering engine.

### Added
- Core rendering engine with `A2UISurface` composable
- All 18 standard A2UI v0.8 widgets:
  - Basic: Text, Image, Icon, Divider, Video, AudioPlayer
  - Layout: Column, Row, List, Card, Tabs, Modal
  - Interactive: Button, TextField, CheckBox, Slider, MultipleChoice, DateTimeInput
- State management with `SurfaceStateManager`
- Reactive data binding with `DataModel`
- A2UI operation processing: beginRendering, surfaceUpdate, dataModelUpdate, deleteSurface
- Event system: UserActionEvent, DataChangeEvent
- Kotlin Multiplatform support: Android, iOS, JVM/Desktop

[Unreleased]: https://github.com/Contextable/a2ui-4k/compare/v0.9.3...HEAD
[0.9.3]: https://github.com/Contextable/a2ui-4k/compare/v0.9.2...v0.9.3
[0.9.2]: https://github.com/Contextable/a2ui-4k/compare/v0.9.1...v0.9.2
[0.9.1]: https://github.com/Contextable/a2ui-4k/compare/v0.9.0...v0.9.1
[0.9.0]: https://github.com/Contextable/a2ui-4k/compare/v0.8.2...v0.9.0
[0.8.2]: https://github.com/Contextable/a2ui-4k/compare/v0.8.1...v0.8.2
[0.8.1]: https://github.com/Contextable/a2ui-4k/compare/v0.8.0...v0.8.1
[0.8.0]: https://github.com/Contextable/a2ui-4k/releases/tag/v0.8.0
