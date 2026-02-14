# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

[Unreleased]: https://github.com/Contextable/a2ui-4k/compare/v0.9.0...HEAD
[0.9.0]: https://github.com/Contextable/a2ui-4k/compare/v0.8.2...v0.9.0
[0.8.2]: https://github.com/Contextable/a2ui-4k/compare/v0.8.1...v0.8.2
[0.8.1]: https://github.com/Contextable/a2ui-4k/compare/v0.8.0...v0.8.1
[0.8.0]: https://github.com/Contextable/a2ui-4k/releases/tag/v0.8.0
