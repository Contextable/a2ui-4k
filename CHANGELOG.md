# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

[Unreleased]: https://github.com/Contextable/a2ui-4k/compare/v0.8.1...HEAD
[0.8.1]: https://github.com/Contextable/a2ui-4k/compare/v0.8.0...v0.8.1
[0.8.0]: https://github.com/Contextable/a2ui-4k/releases/tag/v0.8.0
