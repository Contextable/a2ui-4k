# A2UI Extension

Constants and models for A2UI protocol extension support and capability
negotiation between agents and clients.

> *a2ui-4k implements the A2UI **v0.9** specification natively, with
> backwards-compatible support for v0.8 (see
> [Deprecated Protocol Versions](../protocol/deprecated-versions.md)).*

## Overview

A2UI is an A2A protocol extension that enables agents to render rich user
interfaces. This module provides:

- **Constants** for A2UI identifiers (URIs, MIME types, protocol version)
- **`A2UIExtensionParams`** for agent capability declarations
- **`A2UIClientCapabilities`** for client capability declarations and helper
  builders for common configurations

## A2UIExtension Constants

```kotlin
object A2UIExtension {
    // Extension URI for A2UI v0.9 (current)
    const val URI_V09 = "https://a2ui.org/a2a-extension/a2ui/v0.9"

    // Extension URI for A2UI v0.8 (advertise alongside v0.9 to negotiate down)
    const val URI_V08 = "https://a2ui.org/a2a-extension/a2ui/v0.8"

    // Standard v0.9 catalog definition URI
    const val STANDARD_CATALOG_URI =
        "https://github.com/google/A2UI/blob/main/specification/v0_9/json/standard_catalog.json"

    // Standard v0.8 catalog definition URI (for backwards-compat negotiation)
    const val STANDARD_CATALOG_URI_V08 =
        "https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json"

    // MIME type for A2UI data in message parts
    const val MIME_TYPE = "application/json+a2ui"

    // Wire-protocol version strings
    const val PROTOCOL_VERSION     = "v0.9"
    const val PROTOCOL_VERSION_V08 = "v0.8"
}
```

| Constant | Description |
|----------|-------------|
| `URI_V09` | Identifies the A2UI v0.9 extension |
| `URI_V08` | Identifies the A2UI v0.8 extension (legacy / negotiation) |
| `STANDARD_CATALOG_URI` | URI of the v0.9 standard catalog |
| `STANDARD_CATALOG_URI_V08` | URI of the v0.8 standard catalog |
| `MIME_TYPE` | MIME type for A2UI data in message parts |
| `PROTOCOL_VERSION` | Wire `version` tag for outbound v0.9 messages |
| `PROTOCOL_VERSION_V08` | Wire `version` tag for v0.8 messages |

---

## Client Capabilities

### A2UIClientCapabilities

Declares which catalogs the client can render. Include this in message
metadata when communicating with agents. May optionally ship inline catalog
definitions for ad-hoc or custom components.

```kotlin
@Serializable
data class A2UIClientCapabilities(
    val supportedCatalogIds: List<String>,
    val inlineCatalogs: List<JsonObject>? = null
)
```

### Helper Functions

```kotlin
// v0.9 standard catalog only (typical)
val capabilities = a2uiStandardClientCapabilities()

// v0.9 + v0.8 — let the agent pick based on its own capabilities
val capabilities = a2uiBothVersionsClientCapabilities()

// v0.8 only — for clients that explicitly want legacy
val capabilities = a2uiV08StandardClientCapabilities()

// Custom set of catalogs
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
      "https://github.com/google/A2UI/blob/main/specification/v0_9/json/standard_catalog.json"
    ]
  }
}
```

When `inlineCatalogs` is set, those catalog definitions are shipped verbatim
to the agent so it can reference custom components without a separate URI.

---

## Agent Parameters

### A2UIExtensionParams

Schema for agent capability parameters. Agents use this to declare which
catalogs they support and whether they accept inline catalog definitions.

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
| `acceptsInlineCatalogs` | `Boolean` | Whether the agent accepts inline catalog definitions from clients |

This schema is used when constructing agent capability declarations with an
A2A SDK.

---

## Catalog and Version Negotiation

The extension lets agent and client negotiate both *which catalog* and
*which protocol version* to use:

1. **Agent advertises** — declares supported catalogs in its extension params.
2. **Client declares** — sends `a2uiClientCapabilities` listing the catalogs it
   can render. If both v0.9 and v0.8 standard catalog URIs are present, the
   agent may pick either.
3. **Agent selects** — chooses a catalog and emits messages using the
   matching wire format. Either:
   - v0.9: per-op envelopes carrying `"version": "v0.9"` (`createSurface`,
     `updateComponents`, `updateDataModel`, `deleteSurface`).
   - v0.8: `ACTIVITY_SNAPSHOT` / `ACTIVITY_DELTA` envelopes.
4. **Client receives** — `SurfaceStateManager.processMessage(...)` accepts
   either format. Surfaces created from v0.8 envelopes are tagged
   `ProtocolVersion.V0_8` so outbound user events serialize in the matching
   wire shape.

If `catalogId` is omitted from `createSurface`, clients default to the
A2UI v0.9 standard catalog.

---

## Out of Scope

This SDK focuses on A2UI rendering. The following A2A protocol features are
not included:

- `AgentExtension` data structure (use your A2A SDK)
- HTTP header handling (`X-A2A-Extensions`)
- Message/Task/Part models
- HTTP client transport

For full A2A protocol support, use an A2A SDK (e.g., the Python SDK or CopilotKit).

---

## See Also

- [A2UI v0.9 specification](https://github.com/google/A2UI/tree/main/specification/0.9)
- [Catalogs](../core-concepts/catalogs.md) — Component catalogs
- [UiDefinition](ui-definition.md) — Surface state with catalog reference
- [Deprecated Protocol Versions](../protocol/deprecated-versions.md)
