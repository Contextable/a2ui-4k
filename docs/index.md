# a2ui-4k Documentation

**a2ui-4k** is a Kotlin Multiplatform rendering engine for the [A2UI protocol](https://github.com/google/A2UI), enabling AI agents to generate dynamic user interfaces that render natively on Android, iOS, and JVM/Desktop platforms.

> *a2ui-4k currently implements the A2UI v0.8 specification. The A2UI protocol is under active development and subject to change.*

## Quick Links

- [Getting Started](getting-started.md) - Installation and basic usage
- [A2UI Specification](https://github.com/google/A2UI) - Canonical protocol documentation

## Core Concepts

- [Data Binding](core-concepts/data-binding.md) - Reactive data with JSON Pointer paths
- [Events](core-concepts/events.md) - Handling user interactions
- [Catalogs](core-concepts/catalogs.md) - Widget collections and custom components
- [State Management](core-concepts/state-management.md) - Processing A2UI operations

## Widget Reference

a2ui-4k implements the [A2UI v0.8 Standard Component Catalog](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json):

### Basic Content
- [Text](widgets/text.md) - Text display with markdown support
- [Image](widgets/image.md) - Image rendering
- [Icon](widgets/icon.md) - Material Design icons
- [Divider](widgets/divider.md) - Visual separators
- [Video](widgets/video.md) - Video playback
- [AudioPlayer](widgets/audio-player.md) - Audio playback

### Layout & Containers
- [Row](widgets/row.md) - Horizontal layout
- [Column](widgets/column.md) - Vertical layout
- [List](widgets/list.md) - Scrollable lists
- [Card](widgets/card.md) - Content cards
- [Tabs](widgets/tabs.md) - Tabbed navigation
- [Modal](widgets/modal.md) - Dialog overlays

### Interactive & Input
- [Button](widgets/button.md) - Action triggers
- [TextField](widgets/text-field.md) - Text input
- [CheckBox](widgets/checkbox.md) - Boolean toggles
- [Slider](widgets/slider.md) - Numeric ranges
- [MultipleChoice](widgets/multiple-choice.md) - Selection lists
- [DateTimeInput](widgets/date-time-input.md) - Date/time pickers

## API Reference

- [A2UISurface](api-reference/a2ui-surface.md) - Main rendering composable
- [UiDefinition](api-reference/ui-definition.md) - UI state container
- [Component](api-reference/component.md) - Component model
- [DataModel](api-reference/data-model.md) - Reactive data store
- [CatalogItem](api-reference/catalog-item.md) - Widget interface

## Protocol

- [v0.8 Compliance](protocol/v0.8-compliance.md) - Spec implementation status

## Examples

- [Catalog App](examples/catalog-app.md) - Interactive widget catalog and JSON editor

## Platform Support

| Platform | Status |
|----------|--------|
| Android | Supported |
| JVM/Desktop | Supported |
| iOS | Supported |

## License

a2ui-4k is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

Copyright 2025 Contextable LLC
