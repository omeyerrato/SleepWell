# Testing and device verification

The app uses native Java Views, SQLiteOpenHelper, JUnit4, and AndroidX Test/UI
Automator. No framework migration is required to preserve its working storage
and services. The existing stack is retained. Pure scheduling/signal/task logic
is tested locally; OS delivery and native navigation require a device.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest
```

Current local suites cover midnight/DST/repeating alarms, bedtime boundaries,
smart-window guards, digital amplitude and missing/noise epochs, CSV quoting,
and unique/stable math task choices. AppearancePolicyTest adds boundary and fixed
theme cases, including midnight wrapping and bounded text scales. There are 18
local test methods.

## Personal phone: preserve installation and data

Do not invoke `connectedDebugAndroidTest` on the personal installation. The
observed runner uninstalled the app after a run. Direct instrumentation avoids
that teardown. Back up all private app files first, use `adb install -r`, and
never use `pm clear` or uninstall as a troubleshooting step.

After building, the following helper backs up private app files locally, installs
both APKs in place, and runs a hard-coded allowlist of silent tests:

```sh
python scripts/device_verification.py --adb /path/to/adb --serial DEVICE
python scripts/device_verification.py --adb /path/to/adb --serial DEVICE --flow appearance
```

Backups and output are under ignored `.device-backups/`; actual screenshots are
under ignored `captures/roots/`. Do not commit these private files. The helper
does not schedule alarms, start tracking or the microphone, change power state,
or uninstall anything. It checks the instrumentation result rather than relying
on the shell command's zero exit code.

`RootVisualTest` exercises Tonight/Report/Settings navigation and selected state,
captures the actual screens, rejects development prose/export controls on roots,
and leaves Tonight visible with a window-scoped keep-awake flag.

`AppearanceFlowTest` saves and reopens each manual palette, checks rendered root
colors, verifies Back cancellation, recreates MainActivity to check persistence,
and checks actual title scaling, static reduced-motion press drawables and
contrast. It scrolls to the enlarged primary action without invoking tracking.
It restores the exact original presence/values of its five preference keys in
finally. It never writes alarms, sessions or microphone settings. The helper
relaunches the review window even when the test reports a failure.

`AlarmFlowTest` tests editor persistence and an actual quiet scheduled challenge.
It is currently **not passed**: a prior run failed after selecting a wrong answer.
Its follow-up cannot run while the user's no-alarm instruction remains in force.
No sound, screen-off, reboot or microphone test may be added to the silent test.

Screenshot capture is not a pixel-regression acceptance test. Compare actual
images with each numbered design board, record defects in DESIGN-REVIEW.md,
correct them and request the independent reviewer's fresh assessment. Responsive,
large-font, contrast and remaining permission/error states still need coverage.
