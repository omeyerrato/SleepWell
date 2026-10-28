# First 20 generated functional glyphs

This isolated asset change replaces the first 20 alphabetical `ic_phosphor_*` Android resources at base `d5c2e03e6d78dfebf8ccfa5b74c28ecfd8fba5c9`. Twenty separate built-in `image_gen` calls each generated one distinct transparent raster glyph; no atlas, vector tracing, or programmatic icon drawing was used.

## Assets and provenance

Every exact prompt is in `prompts/`; every original generated PNG is copied byte-identically into `originals/`. `generation-results.json` records the original built-in output location, which was left intact. `mapping.json` records resource names, existing functional aliases, subjects, prompts, original paths, and SHA256 of each replaced XML. `raster-qa.json` records original and all 100 shipped density PNG hashes, dimensions, and alpha facts.

| Resource suffix (`ic_phosphor_`) | Existing aliases |
| --- | --- |
| `alarm_regular` | `alarm` |
| `arrow_counter_clockwise_regular` | `undo`, `reset` |
| `arrow_left_regular` | `back` |
| `arrow_square_out_regular` | `external` |
| `battery_vertical_medium_regular` | `battery` |
| `bed_regular` | `bed` |
| `bell_regular` | `bell` |
| `bell_slash_regular` | `skip` |
| `broadcast_regular` | `sonar` |
| `calendar_blank_regular` | `calendar` |
| `camera_regular` | `camera` |
| `cards_regular` | `memory` |
| `caret_down_regular` | `down` |
| `caret_right_regular` | `chevron` |
| `caret_up_regular` | `up` |
| `chart_bar_fill` | `report` |
| `chats_regular` | `speech` |
| `check_regular` | `check` |
| `circle_half_regular` | `contrast` |
| `clock_regular` | `clock` |

Exactly 20 old XML vectors are removed. The same resource basenames now resolve to 24dp PNGs at mdpi 24px, hdpi 36px, xhdpi 48px, xxhdpi 72px, and xxxhdpi 96px. `resize-density.py` reproduces these using ordinary Pillow LANCZOS resize only. No crop, redraw, alpha cleanup, recoloring, or silhouette manipulation was applied. The original generated RGB has slight pixel variation; existing `Drawable.setTint` supplies the app's flat per-state color while preserving alpha.

The first 3 actual prompts used espresso `#4A2412`, 2px-per24-grid weight and 80% composition. Subsequent prompts align with the coordinated set's `#4A2414`, ~7%-symbol-width weight and 76% composition. These exact prompts are retained as used, rather than rewritten after generation. All 20 actual outputs were 1254x1254 despite the prompt's 1024 request. Their actual glyph extent varies; no false claim of exact geometric equivalence is made.

## Inspection

Each original was individually displayed and reviewed from its own generation result. All 20 shipped 24px glyphs were then individually examined in the labeled `inspection-24px.png` contact sheet, which shows the actual mdpi PNG at native 24px and a 4x nearest-neighbor enlargement on cream. This inspection-only sheet is a composition of existing outputs, not generated artwork or a shipping asset. All 20 functional meanings remain legible: round alarm vs plain clock, counterclockwise undo, left and external arrows, half-charge upright battery, bed, bell and slash-bell, broadcast, blank calendar, camera, paired cards, down/right/up carets, three-bar report, paired speech bubbles, check, and half-filled contrast circle. No text, decorative tile, white canvas, or visible clipping was found. The directional carets point in the required directions, and the contrast circle is filled only on the left.

All originals are square RGBA, alpha extrema 0..255 and four corner alpha values 0. Hollow centers and gaps were visually transparent; no painted checkerboard or white interior was found. Density variants retain RGBA and alpha extrema 0..255. Original bytes match each retained default generated file. Small antialiased edge pixels are preserved. These are asset-level checks, not a device screenshot or native full-app acceptance claim.

## Existing rendering and SVG inventory

At the pinned base there were 73 `ic_phosphor_*` Android XML vectors and 73 source SVGs under `third_party/phosphor/assets/`. There are 0 SVG files under `app/` and no runtime SVG renderer/reference found. `Icons.java` maps aliases to these drawable IDs; `Design.Icon` loads the drawable, mutates it, applies the existing tint, and draws into the existing bounds. Those Java files, actions, content descriptions, target sizes, layouts, and all other resources are unchanged here. The existing widget/shortcut resource IDs continue resolving. Vendor source SVGs/license are provenance rather than Android runtime art; broader importer/vendor/launcher migration is separately owned by Root.

## Verification and limits

Resource compile **PASS**: `BUILD SUCCESSFUL in 50s`, 13 actionable tasks (8 executed, 5 from cache). The verbatim output is in `resource-compile.log.gz` (gzip retains the exact original log bytes). Only `:app:processDebugResources` was requested, arm64 property, offline, two workers. No APK assembly, native/UI tests, physical device, phone outputs, release, or installation occurred. This set does not complete the other 53 functional glyphs or the separately owned launcher/notification assets.

The uncompressed raw compile log SHA256 is `c69c645e1cdcccb6e6aeb101ab6542d3bfa7d1766ab7b24efadb8f3caff21284`. It is compressed solely to preserve its original CRLF/trailing-space bytes through Git text normalization.
