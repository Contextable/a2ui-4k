# Image

Displays an image from a URL.

> **A2UI Spec:** See `Image` in the [A2UI v0.9 Standard Component Catalog](https://github.com/google/A2UI/tree/main/specification/0.9).
>
> *a2ui-4k implements A2UI **v0.9** with backwards-compatible support for v0.8 (see [Deprecated Protocol Versions](../protocol/deprecated-versions.md)).*

## Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `url` | BoundValue&lt;string&gt; | Yes | Image URL |
| `fit` | string | No | How image fits: `contain`, `cover`, `fill`, `none`, `scale-down` |
| `usageHint` | string | No | Size hint: `icon`, `avatar`, `smallFeature`, `mediumFeature`, `largeFeature`, `header` |

## a2ui-4k Implementation

- **Loading:** Uses Coil for async image loading
- **Caching:** Images are cached automatically
- **Placeholder:** Shows placeholder while loading

## Example

Basic image:

```json
{
  "id": "product-image",
  "component": "Image",
  "properties": {
    "url": "https://example.com/product.jpg",
    "fit": "cover"
  }
}
```

With data binding:

```json
{
  "id": "user-avatar",
  "component": "Image",
  "properties": {
    "url": { "path": "/user/avatarUrl" },
    "usageHint": "avatar"
  }
}
```

## Fit Mapping

| Fit | Compose ContentScale |
|-----|---------------------|
| `contain` | `ContentScale.Fit` |
| `cover` | `ContentScale.Crop` |
| `fill` | `ContentScale.FillBounds` |
| `none` | `ContentScale.None` |
| `scale-down` | `ContentScale.Inside` |

## Platform Notes

- Uses Coil 3 for image loading on all platforms
- Network images require internet permission on Android

## See Also

- [A2UI v0.9 Standard Catalog](https://github.com/google/A2UI/tree/main/specification/0.9) - `Image` component
- [Icon](icon.md) - For vector icons
