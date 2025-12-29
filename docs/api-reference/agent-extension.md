# A2UI Extension

Constants and models for A2UI protocol extension support.

> *a2ui-4k currently implements the A2UI v0.8 specification. The A2UI protocol is under active development.*

## Overview

A2UI is a protocol extension that enables agents to render rich user interfaces. This module provides:

- **Constants** for A2UI identifiers (URIs, MIME types)
- **A2UIExtensionParams** for agent capability declarations
- **A2UIClientCapabilities** for client capability declarations

## A2UIExtension Constants

```kotlin
object A2UIExtension {
    // Extension URI for A2UI v0.8
    const val URI_V08 = "https://a2ui.org/a2a-extension/a2ui/v0.8"

    // Standard catalog definition URI
    const val STANDARD_CATALOG_URI = "https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json"

    // MIME type for A2UI data
    const val MIME_TYPE = "application/json+a2ui"
}
```

| Constant | Description |
|----------|-------------|
| `URI_V08` | Identifies the A2UI v0.8 extension |
| `STANDARD_CATALOG_URI` | URI of the standard 18-widget catalog |
| `MIME_TYPE` | MIME type for A2UI data in message parts |

---

## Client Capabilities

### A2UIClientCapabilities

Declares which catalogs the client can render. Include this in message metadata when communicating with agents.

```kotlin
@Serializable
data class A2UIClientCapabilities(
    val supportedCatalogIds: List<String>
)
```

### Helper Functions

```kotlin
// Support for standard catalog only
val capabilities = a2uiStandardClientCapabilities()

// Support for multiple catalogs
val capabilities = a2uiClientCapabilities(
    A2UIExtension.STANDARD_CATALOG_URI,
    "https://my-company.com/custom_catalog.json"
)
```

### JSON Output

```json
{
  "a2uiClientCapabilities": {
    "supportedCatalogIds": [
      "https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json"
    ]
  }
}
```

---

## Agent Parameters

### A2UIExtensionParams

Schema for agent capability parameters. Agents use this to declare which catalogs they support and whether they accept inline catalog definitions.

```kotlin
@Serializable
data class A2UIExtensionParams(
    val supportedCatalogIds: List<String>? = null,
    val acceptsInlineCatalogs: Boolean = false
)
```

| Property | Type | Description |
|----------|------|-------------|
| `supportedCatalogIds` | `List<String>?` | Catalog URIs the agent can generate UI for |
| `acceptsInlineCatalogs` | `Boolean` | Whether agent accepts inline catalog definitions from clients |

This schema is used when constructing agent capability declarations with an A2A SDK.

---

## Catalog Negotiation

The extension enables catalog negotiation between agent and client:

1. **Agent advertises** - Declares supported catalogs in extension params
2. **Client declares** - Sends `a2uiClientCapabilities` with its supported catalogs
3. **Agent selects** - Chooses a catalog and specifies it in `beginRendering`

If `catalogId` is omitted in `beginRendering`, clients default to the A2UI v0.8 standard catalog.

---

## Out of Scope

This SDK focuses on A2UI rendering. The following A2A protocol features are not included:

- `AgentExtension` data structure (use your A2A SDK)
- HTTP header handling (`X-A2A-Extensions`)
- Message/Task/Part models
- HTTP client transport

For full A2A protocol support, use an A2A SDK (e.g., the Python SDK or CopilotKit).

---

## See Also

- [A2UI Specification](https://github.com/google/A2UI)
- [Catalogs](../core-concepts/catalogs.md) - Component catalogs
- [UiDefinition](ui-definition.md) - Surface state with catalog reference
