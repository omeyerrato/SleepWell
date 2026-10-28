# Imagegen glyph replacements: middle 20

Isolated from `d5c2e03e6d78dfebf8ccfa5b74c28ecfd8fba5c9`. These are **20 per-asset built-in `image_gen.imagegen` replacements**, generated at the user's explicit request. No CLI/API fallback or SVG raster conversion was used. Resource names retain their `ic_phosphor_` prefixes for existing Android callers; the new raster artwork is generated, not an upstream Phosphor drawing.

## Deliverables and provenance

- `originals/`: the 20 selected, unmodified generated PNGs, including their original metadata and alpha.
- `attempts/`: two unmodified rejected first passes, retained because the flashlight was filled instead of outlined and the GitHub silhouette was a generic Octocat interpretation instead of the current circular mark.
- `prompts.json`: the exact initial 20 prompts and transparency argument.
- `generations.json`: every initial built-in output path and filename.
- `refinements.json`: exact two follow-up prompts, output paths and GitHub reference argument.
- `references/github-official-logo-reference.png`: unmodified official visual reference used for GitHub refinement. Source: [GitHub Brand Toolkit logo page](https://brand.github.com/foundations/logo), [specific reference PNG](https://brand.github.com/_next/static/media/logo-01.a1916c76.png), retrieved 2026-10-28. This reference is not a shipped drawable; its background and wordmark are absent from the generated glyph. GitHub's names and marks belong to GitHub.
- `manifest.json`: selected attempt, original hash/dimensions, replaced vector hash, all density paths/hashes and measured mdpi alpha information per asset.
- `SHA256SUMS.txt`: hashes for this provenance folder and all 100 shipped PNG variants, excluding the checksum file itself.

The prompt requests transparent flat dark espresso `#4A2414`, rounded medium strokes around 7% of symbol width, approximately 76% canvas occupancy, and no tiles, text, gradients, shadows or 3D. The actual generated originals are 1254×1254 RGBA. Their antialiased raster colors and margins vary slightly; they are preserved as delivered rather than flattened, traced, recolored or thresholded. Existing caller tinting remains unchanged.

## Exact resource scope

All names below have the `ic_phosphor_` prefix. Each old `app/src/main/res/drawable/<name>.xml` is removed and replaced with PNGs under `drawable-mdpi`, `drawable-hdpi`, `drawable-xhdpi`, `drawable-xxhdpi` and `drawable-xxxhdpi`, at **24, 36, 48, 72 and 96 pixels**, respectively. Every density therefore retains the same **24dp intrinsic resource size**. There are 20 XML removals and 100 PNG additions.

| Resource suffix | Individual original and 24px-alpha inspection |
|---|---|
| cloud_rain_regular | Cloud outline and three separate rain strokes remain distinct. |
| cloud_regular | Rounded cloud contour with an open center and flat lower base. |
| device_mobile_regular | Upright phone outline and bottom indicator remain identifiable. |
| dots_three_vertical_regular | Exactly three separate vertically aligned dots. |
| download_simple_regular | Downward arrow and receiving tray are distinct. |
| exclamation_mark_regular | Upright alert stem and separate dot; no enclosure. |
| faders_horizontal_regular | Three slider rails with different knob positions. |
| file_text_regular | Page outline, folded corner and three text-indicator lines. |
| flashlight_regular | Selected refinement has a hollow head/handle and visible switch. |
| footprints_regular | Two staggered soles and toe clusters remain recognizable. |
| fork_knife_regular | Three fork tines and a separate table knife. |
| gear_six_fill | Solid six-tooth gear with a clear center hole. |
| gear_six_regular | Six-tooth outline and center ring stay separate from the filled state. |
| github_logo_fill | Selected refinement retains the recognizable circular negative-space Invertocat, head/ears and left tail. This is generated artwork referenced to the official mark, not a claim of pixel identity with the official file. |
| headphones_regular | Open headband and two ear cups; no microphone boom. |
| heart_fill | Solid heart silhouette. |
| heart_regular | Open heart outline, visibly distinct from the filled state. |
| house_fill | Solid pitched-roof house with open doorway. |
| info_regular | Open circle with separated information stem and dot. |
| keyboard_regular | Keyboard border, key rows and long spacebar remain readable. |

Every initial original and its actual 24×24 alpha was individually opened; both refined originals and refined 24px alpha were then separately opened. For inspection, the 24px alpha was enlarged with nearest-neighbor scaling only; those temporary diagnostic masks are not shipped. All selected sources have real transparent pixels, and all 100 output sizes/density paths are verified against direct source resizing. The shipped derivative is only `original.convert('RGBA').resize((size,size), Image.Resampling.LANCZOS)` saved as PNG: no cropping, drawing, vector tracing, recoloring or alpha cleanup.

## Validation and limits

Resource compilation result is recorded in `VALIDATION.md`. No Java/Kotlin code, resource mapping IDs, layout, behavior, importer, CI workflow, native test, launcher, release metadata or device installation changes are included. Existing tint, state and accessibility behavior are owned by their existing callers. There was no emulator/device launch or native screenshot run for these replacements, so integrated in-app contrast, all sizes/themes and final icon-family consistency still need the parent's combined visual check. This set does not accept future launcher changes, other agents' glyphs or all 36 reference screens.
