# Image

Displays an image from a URL.

> **A2UI Spec:** See `Image` in the [Standard Component Catalog v0.8](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json).
>
> *a2ui-4k currently implements the v0.8 specification. The A2UI protocol is under active development.*

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
    "url": { "literalString": "https://example.com/product.jpg" },
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

- [A2UI Standard Catalog](https://github.com/google/A2UI/blob/main/specification/0.8/json/standard_catalog_definition.json) - `Image` component
- [Icon](icon.md) - For vector icons
