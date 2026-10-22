# Verification record

## Changing light correction milestone — 2026-10-22

- All 12 supplied PNG boards were visually inspected, and the 17 ZIP entries
  checked against their extracted counterparts. The brief and screen map were read.
- Latest local `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest`
  passed: 15 unit tests, zero test failures. Lint has zero errors and outstanding
  warnings (including localization); this is not a claim of clean localization.
- Direct `RootVisualTest` passed on the S23 Ultra, including real selected-chart
  interaction and Tonight/Report/Settings selected navigation states. Actual
  screenshots were captured and visually compared with the references. The strict
  reviewer accepted specific corrections but **rejected full screen/goal completion**.
- No new alarm, screen-off, reboot, microphone or overnight test ran after the
  user said they were going to sleep. Those tests remain prohibited until renewed
  authorization. The app was left on Tonight with a window-scoped review keep-awake
  flag, without changing the global screen timeout or bypassing the keyguard.
- Earlier alarm-flow attempts did not pass: an onCreate window-controller crash
  was fixed; a later run verified editor persistence and quiet delivery over
  keyguard but failed after answer selection. That is an open verification gap.
- The Gradle device runner removed the development installation after its test.
  The pre-update database was restored: one session, four epochs, one disabled
  alarm. Preferences not included in the early database-only backup could not be
  recovered. Direct instrumentation now backs up the whole private tree and retains
  the installation. Never invoke that Gradle device runner on personal data.
- The native alarm editor, math-state persistence, root compositions and shared
  measured bar charts are implemented changes, not evidence that every screen or
  phone feature is complete. Consult FEATURE-MATRIX.md and DESIGN-REVIEW.md.

Independent CI for [034c345](https://github.com/omeyerrato/SleepWell/actions/runs/39842509047) completed successfully. That confirms its build checks, not full device/feature acceptance.

The earlier alpha verification record below is historical.

Date: 2026-10-22 (Asia/Riyadh).

## Local build

JDK 21, Android SDK 36, Android Gradle Plugin 8.12.0, Gradle 8.14.3.

```text
testDebugUnitTest lintDebug assembleDebug
BUILD SUCCESSFUL
13 unit tests; 0 failures; 0 errors
```

Lint is enabled with abort-on-error. Initial missing-microphone-permission, legacy-back-navigation and local Windows property-escaping errors were fixed. Remaining warnings include English-only UI strings, known version-update notices and older-API compatibility annotations; the build is not described as warning-free.

Unit tests cover next-occurrence scheduling across midnight, exact-minute boundaries, weekday/weekend transitions, a DST clock gap, overnight/daytime bedtime windows, smart-window/activity/sample prerequisites, PCM RMS normalization, bounded duration arithmetic, CSV quote/newline escaping, sample-weighted noise RMS and missing-versus-silent audio windows.

## Physical-device smoke test

Galaxy S23 Ultra (SM-S918B), Android API 36, connected through authorized USB debugging.

- Debug APK installed successfully and launched.
- Tonight and Alarms screens inspected through Android layout output; Tonight screenshot visually checked.
- Notifications, exact-alarm and full-screen-alarm permissions granted for testing.
- Quiet preview scheduled for ten seconds; AlarmManager dump confirmed the pending wake-up alarm.
- Device screen put to sleep. The preview fired and the Math challenge screen appeared over the lock screen.
- No AndroidRuntime crash output observed during the launch/alarm smoke test.
- A 232-second movement-only recording continued with the screen off, saved four epochs with 258–294 accelerometer samples per epoch and ambient light readings, and stopped via the notification action. SQLite integrity check returned `ok`; the session was closed with `interrupted=0`. No microphone audio was collected in this test.
- The test alarm subsequently stopped and the device returned to keyguard; automated completion of its three challenge rounds was **not observed**, so it is not counted as a passing challenge-completion test.

## Not established by these checks

The screen-off preview does not prove overnight, reboot or Doze reliability. Volume changes were configured in code; subjective loudness and the ability to wake this user were not measured. The optional accessibility guard, NFC matching, backup escalation, wake-up check, audio recording/classification, full-night battery consumption, export/restore, and every challenge have not all been verified end to end on hardware.

The device became locked during further testing. Additional device interaction is pending an unlocked test session. No recordings or personal sleep data are published in this repository.

See [issue 1](https://github.com/omeyerrato/SleepWell/issues/1) for overnight/device validation and [FEATURES.md](FEATURES.md) for the complete implementation audit. GitHub Actions builds and uploads an APK independently of this local machine; a green run must be checked separately rather than inferred from local success. The independent [CI run for 569d7b6](https://github.com/omeyerrato/SleepWell/actions/runs/39739469746) passed; subsequent commits have their own runs.
## Appearance preference milestone — 2026-10-22

The new settings journey is functional: explicit Save, unsaved Back cancellation,
automatic local-clock phases or fixed Midnight/Pre-dawn/Dawn, native text-size
scaling, stronger high-contrast controls and static reduced-motion press feedback.
Tonight's surface, system bars, panel, plan arc, weekdays, action and dock share
the saved palette; changing a preference does not schedule an alarm.

- `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest`: passed;
  18 local test methods, no failures; lint 0 errors / 38 warnings. Existing
  localization and deprecated-API work remains.
- `AppearanceFlowTest`: passed on S23 Ultra (46.607 seconds; final thumbnail
  correction rerun 45.556 seconds), after correcting
  an initial stale-node harness failure during activity recreation. Each of
  three themes was saved/reopened and checked against the actual root color;
  MainActivity recreation retained size/contrast/motion; Back discarded changes;
  enlarged primary action was reached by scrolling without starting tracking.
- `RootVisualTest`: passed after the update (4.173 seconds). Fresh Tonight,
  Report, selected epoch and Settings captures were visually inspected locally.
- Original shared-preference values match the pre-test private backup; SQLite
  database/WAL files are byte-identical. No alarm/tracking service was running
  afterward. Whole-private-tree backups remain ignored and local.
- Strict independent review accepted the **narrow appearance-preferences flow**;
  full screen 36 remains partial (smart lights absent), and the full app remains
  incomplete. See DESIGN-REVIEW.md for visual corrections and remaining gaps.

No alarm, audio, vibration, microphone, screen-off or reboot test was run during
this milestone. The earlier alarm/challenge failure remains unresolved until
the user's restriction is lifted. Do not read these settings results as alarm
or overnight-classification evidence.

## Dense graph rendering — 2026-10-22

The display renderer now averages only contiguous recorded epochs into readable
columns for long sessions. Explicit null epochs and absent minute intervals split
those groups; a true zero has a small baseline mark. Raw stored epochs are not
rewritten. Each chart still labels relative activity rather than clinical stages.
Selected aggregates expose their time range, reading count and arithmetic mean.

- Five ChartSeriesTest cases passed; current local total is 23 with no failures.
  Unit/lint/debug app/test APK builds passed (lint 0 errors / 38 warnings).
- ChartVisualTest passed silently on S23 Ultra in 3.063 seconds after review fixes. Three actual
  Android captures under ignored `captures/charts/` were visually inspected:
  dense eight-hour fixture, selected aggregate and empty signal. The first two
  visibly say “VISUAL TEST · synthetic measurements.” They are held only in
  test memory; no production/demo sessions or preferences are inserted.
- The fixture has a deliberate outage and real zero-valued samples. This tests
  display fidelity and honest missingness, not live sensor accuracy, an overnight
  recording, stages or sound classification. Those gates remain incomplete.

The Appearance commit's independent CI run passed:
[22c399e](https://github.com/omeyerrato/SleepWell/actions/runs/39849186925).

The strict reviewer caught finite-sum overflow and record-detail bounds derived
from samples. These were fixed with an incremental mean, division before pixel
scaling, and actual session bounds passed to all detail plots. Tests cover finite
maximum values and verify visible rendered bars, out-of-session filtering and a
partial final epoch. Averaged selections now show their range visually as well
as through accessibility. Preferences and database/WAL files remained unchanged
after the silent chart/root tests. No new recording or alarm was started.
