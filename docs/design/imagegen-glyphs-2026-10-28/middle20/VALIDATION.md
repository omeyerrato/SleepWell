# Validation

- `:app:processDebugResources --offline --no-daemon --max-workers=2`: **BUILD SUCCESSFUL in 44s**, 13 tasks (6 executed, 7 up to date), using the existing Gradle 8.14.3 wrapper distribution and configured local JDK/SDK.
- Exactly 20 assigned drawable XML resources removed; exactly 100 same-name PNG resources added across mdpi/hdpi/xhdpi/xxhdpi/xxxhdpi at 24/36/48/72/96 pixels.
- Every shipped PNG has RGBA pixels identical to a direct LANCZOS resize of its selected unmodified original. Source/output hashes and actual alpha metrics are in `manifest.json`.
- All 22 generated originals (20 selected and two rejected) are byte-identical to their built-in output files. Every initial original and 24px alpha, then both refined originals and their 24px alpha, was individually inspected.
- `git diff --cached --check` passes. Staged bytes and the complete SHA-256 inventory are verified before committing. Run `sha256sum -c docs/design/imagegen-glyphs-2026-10-28/middle20/SHA256SUMS.txt` from the repository root to verify the archive and shipped PNGs.

No unit/native test, broad lint, CI dispatch, emulator, personal device, audio output, install or release action was performed for this resource-only increment. Resource compilation proves packaging, not final combined app screenshot fidelity or every existing tint/state path.
