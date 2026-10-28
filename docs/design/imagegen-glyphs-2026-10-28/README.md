# Imagegen raster identity and controls

The user requested built-in imagegen artwork instead of the old SVG/vector
icons. The app now ships **73 generated control glyphs × five Android densities**
(24/36/48/72/96px), a generated flat folded-quilt launcher identity, its generated
monochrome mask, and raster notification artwork. There are no SVG files or
`<vector>` resources in app/src/main. Native adaptive/inset/shape XML is Android
resource plumbing, not SVG artwork.

The historical `ic_phosphor_*` identifiers remain so existing callers, widget
resources, tint colors, states, accessibility labels and actions do not change.
The artwork at those identifiers is generated raster art. The former SVG importer
is retired; unused upstream reference files/licenses remain outside app sources
for history and attribution. Charts, clock progress and functional printable QR
codes remain live data/code renderers; they are not replaced with static images.

| Assets | Originals, exact prompts and hashes |
| --- | --- |
| Launcher / monochrome / notification | [Launcher provenance](launcher/README.md) |
| First20 controls | [First20](first20/README.md) |
| Middle20 controls | [Middle20](middle20/README.md) |
| Later20 controls | [Later20](later20/README.md) |
| Last13 controls | [Last13](root13/README.md) |

Every selected original and actual24px derivative was visually inspected.
Generated optical margins vary; provenance records those variations instead of
claiming exact geometry. Density packaging only resizes the original whole
canvas, preserving its alpha. The rejected glossy crescent is not shipped.

The bounded [native scope](../../verification/2026-10-28-native-raster-gallery/README.md)
checks all73 loaded bitmap resources, real native tinting, color/themed adaptive
mask previews and the14 actual Tonight states. Its execution is pending at this
source checkpoint. Neither this index nor the earlier source-only compile results
claim Android launcher acceptance or completion of all36 design references.
