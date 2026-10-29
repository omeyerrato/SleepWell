# Motion reference, Board 10 middle / UI 29

This increment brings the existing native Motion sensor settings screen closer to the Board 10 middle panel. It adds generated transparent raster handset art, readable rolling-chart time labels and an explicit current relative range; missing samples remain blank and real zeroes are not shown as artificial bars. The existing Active/Unavailable states, sensitivity rail, fixed Test again / Use actions and surface-calibration route remain.

The compact layout keeps the calibration row visible without shrinking body text or touch targets. The illustration is 72 dp high and the complete chart is 116 dp high; the chart uses the existing 30 half-second RMS bins from MotionPreview, with at least a 0–0.24 range and headroom for larger measured values. Range changes affect presentation only. There is no sample history generator, permission/dependency/schema/manifest change or shared Design change.

Source: `dd2813c56df821e03ad09b8b26b8e635828de622` and fit correction `38e895ff` on base `19e49e74e58ff05812229743df441e8942775db1`. Native scope: `7cabc29cdd9db920baa96d6229a106dc0b87ce00` and dialog handoff correction `a46f1b7cc4cb8737cc5b532a318072ff515ad8d8`. Exact source/blob hashes are in [preflight.json](preflight.json); raster generation provenance is in [asset-generation.json](asset-generation.json).

## Bounded verification

The single existing method is `MotionConfigurationFlowTest#fakeSamplesSettingsPersistLifecycleAndUnsupportedErrorsWithoutHardware`, selected using `motion-reference`, `motionSlice=reference`, `native_sounds=false` and `audio_imports=false`. It retains the original settings/recreation/save/conflict/error checks and captures the same seven Motion states. The default invocation still continues into RoomConditionsFixture; this focused scope deliberately does not execute Room. No additional @Test or unit batch was added.

The method uses injected accelerometer callbacks and isolated test preferences. It checks the actual Test again reset, sensitivity action, no pre-save writes, recreation retaining the selected setting and closing the old port, real Use persistence, reopen, concurrent change rejection, unavailable/startup-error disabling of Use, unchanged personal preferences and closure of all owned fake subscriptions. It also requires the entire title, handset image, chart/scale, sensitivity rail/info, calibration action, current feedback and both footer actions to be visible, with at least 48 dp action targets before the real committed frame capture. No delays, focus forcing, repeated clicks, timeout growth or pixel waits were added.

Local final source compilation passed `:app:compileDebugAndroidTestJavaWithJavac --offline --max-workers=2 --no-daemon --console=plain`, 1m34s, 25 tasks / 3 executed / 22 up to date. Compilation is not runtime or hardware acceptance.

## Attempts

[First run 40861054915](https://github.com/omeyerrato/SleepWell/actions/runs/40861054915) failed in the existing method before the first required capture, with a null direct UI action lookup following the information-dialog transition. Its original screenshot also exposed calibration-row clipping behind the footer. All original artifacts and the original source preflight are retained under [attempt-1](attempt-1/); [attempt-1-files.json](attempt-1-files.json) records byte counts and SHA-256 values. Subsequent source changes compacted gaps/art/chart without reducing text/touch sizes; the fixture awaits and identifies the dialog handoff controls explicitly using existing bounded semantic waits. This failed run is not a passing visual proof.

[Second run 40862856617](https://github.com/omeyerrato/SleepWell/actions/runs/40862856617) completed **successfully**, one existing method in **22.359 seconds**, seven required original images and `failures: []`. Terminal head is `a46f1b7cc4cb8737cc5b532a318072ff515ad8d8`, production tree `c6a65d0c93c3d4328b395f97a59a9bf8cc33b34e`. The API 36 renderer used 1080×2316 at density 420. [Run metadata](run.json), [original summary](originals/focused-review-summary.json), [environment](originals/environment.json), [native log](originals/MotionConfigurationFlowTest%23fakeSamplesSettingsPersistLifecycleAndUnsupportedErrorsWithoutHardware.txt) and [original artifact hashes](files.json) are retained. No source changed after this run.

Every original was opened separately and compared with the Board 10 middle panel; [visual-review.json](visual-review.json) records the individual observations. The handset, Active pill, violet chart and rail, cream Inter hierarchy and fixed actions match the reference direction. The supporting calibration row adds functionality beyond the concept and now fits fully above the footer in all seven states. System status/navigation glyph contrast remains qualified.

| Original frame | Individual review |
| --- | --- |
| [ready](originals/motion-settings-ready.png) | Medium; Scale 0–0.27; only the latest injected six seconds have bars, earlier missing history blank. Handset, all time ticks, complete signal feedback, rail, full calibration row and fixed actions visible. |
| [changed](originals/motion-settings-changed.png) | High selected at the far rail endpoint; observed data range now 0–0.53. Same complete layout, with an uncut thumb and info target. |
| [recreated](originals/motion-settings-recreated.png) | High selection retained; newly started subscription has no samples and shows a blank chart with Scale 0–0.24. The complete waiting message wraps safely; calibration and footer remain visible. |
| [reopened](originals/motion-settings-reopened.png) | Persisted High setting is visible after the real Use save/reopen. Chart correctly remains blank until new callbacks; complete waiting feedback and all controls fit. |
| [conflict](originals/motion-settings-conflict.png) | Complete orange concurrent-change feedback survives recreation. High is the attempted selection; the original test asserts stored concurrent Low remains unchanged. Calibration/footer remain visible. |
| [unavailable](originals/motion-settings-unavailable.png) | Unavailable status and full no-accelerometer feedback; blank chart, stored Low, Use visibly disabled. Test/calibration row remain available and fully visible. |
| [error](originals/motion-settings-error.png) | Not active status and full startup error; blank chart, stored Low, Use visibly disabled. Test/calibration row and chart labels remain fully visible. |

The first attempted redispatch failed during the GitHub workflow GET with a TLS handshake timeout, before accepting a dispatch. The remote run list was checked and contained only the original failed attempt. A single subsequent dispatch produced run 40862856617. Its watch later lost API connectivity; direct lookup of the same run established terminal success. No duplicate run was dispatched.

[Source inspection](source-inspection.json) records exact unchanged definitions for sensor lifecycle, registration/test, slider styling, save/observe, calibration result, back and destruction methods. The only update-method change publishes the chart's current display range. This source comparison does not extend the runtime scope.

## Scope limits

This is one Motion settings journey at the renderer configuration recorded in environment.json. Injected callbacks exercise the real preview/configuration/persistence path; they do not establish real accelerometer accuracy, physical calibration, smart wake outcomes, all devices/font scales, actual system Back gestures, Room conditions, whole-app security or all 36 designs. Calibration navigation and Cancel remain unchanged by source inspection; the calibration journey and a Cancel interaction were not executed here. Inherited system status/navigation glyph contrast remains qualified. The earlier separate [30.873-second Motion and Room evidence](../2026-10-28-motion-room-reference/README.md) is preserved with its original scope and failures; it is not relabelled as this focused run. Nothing was installed or run on the user's phone. Published 0.1.7 is independent; this increment is unreleased.
