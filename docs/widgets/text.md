# Text

Displays text content with optional markdown support and styling hints.

> **A2UI Spec:** See `Text` in the [A2UI v0.9 Standard Component Catalog](https://github.com/google/A2UI/tree/main/specification/0.9).
>
> *a2ui-4k implements A2UI **v0.9** with backwards-compatible support for v0.8 (see [Deprecated Protocol Versions](../protocol/deprecated-versions.md)).*

## Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `text` | BoundValue&lt;string&gt; | Yes | Text content to display |
| `usageHint` | string | No | Styling hint: `h1`, `h2`, `h3`, `h4`, `h5`, `caption`, `body` |

## a2ui-4k Implementation

- **Markdown support:** Basic markdown (bold, italic, links) is parsed and rendered
- **Typography:** `usageHint` maps to Material Design typography styles
- **Data binding:** `text` property supports path binding for dynamic content

## Example

```json
{
  "id": "greeting",
  "component": "Text",
  "properties": {
    "text": "Hello, **World**!",
    "usageHint": "h1"
  }
}
```

With data binding:

```json
{
  "id": "user-name",
  "component": "Text",
  "properties": {
    "text": { "path": "/user/displayName" },
    "usageHint": "body"
  }
}
```

## Usage Hints

| Hint | a2ui-4k Rendering |
|------|-------------------|
| `h1` | Material `headlineLarge` |
| `h2` | Material `headlineMedium` |
| `h3` | Material `headlineSmall` |
| `h4` | Material `titleLarge` |
| `h5` | Material `titleMedium` |
| `caption` | Material `labelSmall` |
| `body` | Material `bodyMedium` (default) |

## Platform Notes

- All platforms use Compose Multiplatform typography
- Markdown rendering is consistent across platforms

## See Also

- [A2UI v0.9 Standard Catalog](https://github.com/google/A2UI/tree/main/specification/0.9) - `Text` component
- [Data Binding](../core-concepts/data-binding.md)
