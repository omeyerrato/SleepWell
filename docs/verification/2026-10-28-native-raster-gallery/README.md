# Native raster gallery verification fixture (prepared, not run)

Base: `b78d4c9a0eae84ad95581554db0f57da8e6caafc`. This branch adds native verification only. It changes no production source, artwork, preferences, activity admission, output ports, or release metadata. The remaining raster asset batches must be integrated before this fixture is dispatched: at this base some of the 73 semantic resources are still vectors, and the new assertion intentionally rejects those.

The existing selector remains exactly `NightArcFlowTest#clockProgressAndExplanationWithoutAnyAlarmDelivery`. There is still one `@Test`. Its original private clock/plan/database fixtures, lifecycle restrictions, no-output assertions, scheduling fake, restoration, and all 14 original Tonight capture calls remain intact. The appended gallery runs after those captures. A temporary `AlertDialog` is a diagnostic host, not a newly implemented app screen or a production navigation route.

## Appended native checks

- Reflect all 73 unique `R.drawable.ic_phosphor_*` IDs, sorted by resource name; aliases do not inflate the count. Three native dialogs contain exactly 25, 25, and 23 actual `ImageButton` controls. Every icon is loaded by Android's themed `getDrawable`, required to be a `BitmapDrawable`, and shown at its decoded intrinsic size within a fully visible 48dp control. Labels have the resource's full name in the content description.
- Require intrinsic width and height to equal 24dp at the actual display density, allowing one pixel of density rounding. Inspect the actual decoded bitmap's alpha channel, nonempty artwork, transparent pixels, and all four transparent corners. Record the decoded dimensions/density/alpha facts and actual window bounds in `arc-icon-metrics.tsv`.
- Apply two successive real `Drawable.setTint` values, ending with alternating espresso/teal diagnostic colors. After the real dialog frame commits, PixelCopy samples that actual window surface and requires visible pixels of the final tint inside every icon control. The fixture never calls `Drawable.draw` onto an offscreen canvas, invents an icon, paints a screenshot, or substitutes a generated metrics image for a native screenshot.
- Load the actual application icon with `PackageManager.getApplicationIcon`. Require `AdaptiveIconDrawable` and a non-null API33 monochrome layer. Show the loaded color icon and the actual monochrome layer, tinted inside a standard framework `AdaptiveIconDrawable`, at 120dp and 48dp. Require full preview visibility and nonempty framework masks contained by the native drawable bounds. The original color/monochrome artwork is unchanged.
- Capture only with `UiDevice.takeScreenshot`, after a hardware-accelerated frame-commit callback on the currently focused, attached, owned dialog. Dismiss each diagnostic and wait for its old window to detach and the Main owner to regain focus. Failure cleanup also dismisses any owned diagnostic before destroying activities and restoring the original factories/preferences.

The themed view is a native preview built from the package's actual monochrome layer. It does not enable the launcher OS theme or certify every OEM mask. Native screenshots still require individual inspection for visual clipping, icon semantics, legibility, and color/themed composition; source assertions alone do not establish those results.

API contracts used: [AdaptiveIconDrawable.getMonochrome/getIconMask](https://developer.android.com/reference/android/graphics/drawable/AdaptiveIconDrawable), [BitmapDrawable intrinsic density](https://developer.android.com/reference/android/graphics/drawable/BitmapDrawable), and [PixelCopy window surface capture](https://developer.android.com/reference/android/view/PixelCopy). The CI renderer is API36; the monochrome diagnostic deliberately requires API33+ without modifying app runtime support.

## Exact focused scope

Only `tonight-composition` changes from 14 to 18 required originals. Its method selector stays unchanged. All other scope selectors, frame mappings, workflow choices/defaults, and optional-output rules remain unchanged. Both optional flags must remain false. Other scopes which already run the same method may receive extra diagnostic files, but their required inventories are unchanged.

1. `arc-upcoming.png`
2. `arc-help.png`
3. `arc-start.png`
4. `arc-middle.png`
5. `arc-end.png`
6. `arc-next.png`
7. `arc-large.png`
8. `arc-large-action.png`
9. `arc-weekly.png`
10. `arc-off.png`
11. `arc-off-recreated.png`
12. `arc-weekday-recreated.png`
13. `arc-empty.png`
14. `arc-empty-action.png`
15. `arc-icon-page-1.png`
16. `arc-icon-page-2.png`
17. `arc-icon-page-3.png`
18. `arc-launcher-icons.png`

## Current result and limits

Prepared fixture only; no CI/native/device execution or screenshots are claimed here. Compile/static results and commit identities are recorded in `prepared-inventory.json`. The existing 14 captures remain actual Tonight UI; the four added captures are expressly diagnostic galleries. This is not a full-app review, a new unit batch, a physical-output test, or acceptance of all 36 reference screens. Root owns final asset integration and the later single focused dispatch.

Prepared commit order: native `28d2aeff2611de45063d37262dbc58f2c68e7724`, then scope `f0c47c59fed793a715ae6966064af43cc8fc34da`, followed by this documentation commit. Local `:app:compileDebugAndroidTestJavaWithJavac` passed in **2m 8s**, 25 actionable tasks (14 executed, 11 from cache). The exact compile log is retained in `native-compile.log.gz`; its uncompressed SHA256 is in the prepared inventory. No APK was assembled and no test method was executed. `git diff --check` passed.
