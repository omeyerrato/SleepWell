# Image-generated utility glyphs: later20

Base: `d5c2e03e6d78dfebf8ccfa5b74c28ecfd8fba5c9`.
Branch: `codex/imagegen-later20-20261028`.

These are the 20 assigned icons from lightbulb through question. Each was
created with one built-in `image_gen.imagegen` call and
`transparent_background: true`; no CLI/API fallback, sheet generation, vector
tracing or hand-drawn replacement was used. All 20 generated originals are
copied unchanged into [originals](originals/). The complete final prompt set,
original hashes and alpha/bounds/color measurements are in [manifest.json](manifest.json).
The `ic_phosphor_` names remain stable Android resource identifiers; these PNGs
are new generated artwork, not copies of the former Phosphor vector paths.

The shared prompt requested a transparent flat espresso #4A2414 glyph,
rounded medium strokes near 7% of symbol width and about 76% square occupancy,
with no tile, wording, gradient, shadow or 3D. Generation is approximate: exact
opaque bounds range 57.7–76.9%, and the generated ink has small color variation
around espresso rather than exact constant RGB. Pause and some list/music
symbols have smaller optical bounds. The lock body is filled. These differences
are recorded rather than presented as exact geometric/color compliance.

## Retained originals and inspection

Every generated original and every actual 24×24 mdpi derivative was opened
individually. A 192×192 nearest-neighbor enlargement of each 24px image was also
inspected privately to expose the actual small-size pixels. All 20 glyphs retain
their intended utility semantics, transparent negative spaces and unclipped
outer silhouettes. No original was cropped, recolored, repainted, reshaped or
normalized to conceal generated variation. This is asset inspection, not an
Android screen capture or all-device/readability acceptance.

| Resource / unchanged original | Longest opaque dimension |24px inspection |
| --- | --- | --- |
| [ic_phosphor_lightbulb_regular](originals/ic_phosphor_lightbulb_regular.png) |73.8% |Rounded bulb outline and two base bars. |
| [ic_phosphor_link_simple_regular](originals/ic_phosphor_link_simple_regular.png) |73.1% |Two diagonal linked loops. |
| [ic_phosphor_list_checks_regular](originals/ic_phosphor_list_checks_regular.png) |62.6% |Three checks and three list bars. |
| [ic_phosphor_list_numbers_regular](originals/ic_phosphor_list_numbers_regular.png) |63.0% |Three numbered rows; tiny digits are part of the icon. |
| [ic_phosphor_lock_key_regular](originals/ic_phosphor_lock_key_regular.png) |72.2% |Closed padlock, filled body and transparent keyhole. |
| [ic_phosphor_magnifying_glass_regular](originals/ic_phosphor_magnifying_glass_regular.png) |66.8% |Round search lens and diagonal handle. |
| [ic_phosphor_math_operations_regular](originals/ic_phosphor_math_operations_regular.png) |70.8% |Four arithmetic pictograms remain separated. |
| [ic_phosphor_microphone_regular](originals/ic_phosphor_microphone_regular.png) |74.0% |Outlined capsule/cradle and short base. |
| [ic_phosphor_minus_regular](originals/ic_phosphor_minus_regular.png) |70.8% |Single horizontal bar. |
| [ic_phosphor_moon_fill](originals/ic_phosphor_moon_fill.png) |71.3% |Filled crescent with open right-hand cutout. |
| [ic_phosphor_music_notes_regular](originals/ic_phosphor_music_notes_regular.png) |61.6% |Two connected musical notes. |
| [ic_phosphor_palette_regular](originals/ic_phosphor_palette_regular.png) |75.6% |Palette silhouette and well/thumb openings. |
| [ic_phosphor_pause_fill](originals/ic_phosphor_pause_fill.png) |57.7% |Two separate vertical bars; smaller optical bounds. |
| [ic_phosphor_pencil_simple_regular](originals/ic_phosphor_pencil_simple_regular.png) |67.1% |Diagonal pencil with nib and end band. |
| [ic_phosphor_person_simple_run_regular](originals/ic_phosphor_person_simple_run_regular.png) |76.4% |Running person, separated head and bent limbs. |
| [ic_phosphor_play_fill](originals/ic_phosphor_play_fill.png) |66.7% |Filled right-pointing triangle. |
| [ic_phosphor_plug_regular](originals/ic_phosphor_plug_regular.png) |76.9% |Two prongs, plug body and short cable. |
| [ic_phosphor_plus_regular](originals/ic_phosphor_plus_regular.png) |64.6% |Equal-armed rounded plus. |
| [ic_phosphor_qr_code_regular](originals/ic_phosphor_qr_code_regular.png) |70.2% |Sparse QR pictogram only; no encoded content. |
| [ic_phosphor_question_regular](originals/ic_phosphor_question_regular.png) |76.6% |Circular help outline and question mark. |

All originals are 1254×1254 RGBA with alpha extrema 0/255 and transparent corner
pixels. All 100 variants retain alpha 0 and a substantial visible mark; four
corners remain fully transparent. Small antialiased glyphs need not contain an
alpha 255 pixel after downsizing. Exact per-variant extrema and hashes are in
the manifest. No opaque background/checkerboard was introduced.

The QR resource is only a simplified menu pictogram with three finder shapes
and sparse blocks. It is not a generated scannable code. The real QR generator,
enrollment and payload handling are unchanged.

## Android resources

The 20 original `drawable/<name>.xml` files are replaced by matching-name PNGs
in five density folders, preserving nominal 24dp dimensions:

| Folder | Pixels |
| --- | --- |
| `drawable-mdpi` |24×24 |
| `drawable-hdpi` |36×36 |
| `drawable-xhdpi` |48×48 |
| `drawable-xxhdpi` |72×72 |
| `drawable-xxxhdpi` |96×96 |

Each variant is a direct whole-canvas Pillow LANCZOS resize of its retained
original, encoded as RGBA PNG. There is no crop, alpha threshold, tint bake,
background removal, recolor, geometry repair or compositing in the final
resource transformation. Existing consumers/tint behavior are unchanged.
Importer, notification/launcher adapters, behavior and layouts belong to the
parent integration and are not edited here. Other resource names are untouched.

Android SDK Build Tools 36.0.0 AAPT2 (`2.20-13193326`) completed
`compile --dir app/src/main/res` with exit 0 and empty diagnostics. The compiled
ZIP contains all 100 owned PNG entries; its hash and exact list are in
[resource-compile.json](resource-compile.json). This compiles resources only;
it does not establish application linking, runtime rendering, density
selection, accessibility, device behavior or release acceptance. No Gradle
build, CI/native test, physical device, alarm/audio/provider or release action
was performed by this work.

`SHA256SUMS` checks all 20 originals, 100 density variants and these provenance
files using repository-relative paths. The original paths/hashes in the
manifest preserve the generated bytes; the source-map with local generated
paths remains private under ignored `.android-local`.
