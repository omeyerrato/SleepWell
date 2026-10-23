# Verification record

## Manual movement recording milestone — 2026-10-23

Native tracking setup → live movement → Pause → activity recreation → Resume →
Stop → saved Report → reopened Report now has a passing, controlled S23 Ultra
journey. This is a short movement-only recording, not an overnight/stage/audio
validation. All existing alarms were already disabled; tests assert that
precondition and never switch them off. Microphone, clip saving, light recording
and sleep audio were off. No alarm, sound, vibration, screen-off or reboot test ran.

- Local unit/lint/app/test APK builds passed: **35 unit tests, 0 failures;
  lint 0 errors / 44 warnings**. Localization, deprecations and other warnings remain.
- `TrackingFlowTest`: **2 passed in 34.672 seconds** on the final UI. Checks real
  registered accelerometer samples, same session ID, frozen active duration and
  unregistered sensors while paused, recreation, fresh samples after resume,
  explicit gap boundaries, null unrequested noise/light signals, closed pauses,
  backup validation and report persistence. It also retains chart selection and
  keyboard focus through live data updates. Full TalkBack testing remains pending.
- The second test injects an epoch write failure in a uniquely named test DB;
  the service stops, clears running flags and retains an interrupted record/error.
  No real disk-full state or personal DB corruption is induced.
- `StorageMigrationTest`: **4 passed in 0.130 seconds**. Isolated temporary DBs
  cover nondestructive schema 1→2 migration, format 1/2 import, exact epoch starts,
  pause round trip, invalid-pause rejection, open/closed pause recovery and a
  failure after epoch insert/before active-duration update. The latter rolls back
  both writes and a retry leaves exactly one epoch.
- Personal schema migration separately compared every original alarm/session/
  epoch/clip column value and passed SQLite integrity. A fresh comparison after
  the final tracking/chart/root runs confirmed every logical DB row, preference
  key/value presence and clip hash matches the pre-tracking private backup.
  Test sessions were inspected and then only their own rows removed in finally.
- `ChartVisualTest`: 1 passed in 3.081 seconds; explicitly labeled synthetic
  in-memory dense fixture only. `RootVisualTest`: 1 passed in 4.200 seconds.
  Actual final images were inspected; Tonight's system navigation surface is plum.
- Seven actual tracking screenshots under ignored `captures/tracking/` show setup,
  genuine initial Collecting/empty state, real live bars, pause, resume, saved and
  reopened reports. No graph values were copied from the boards. The setup art is
  a standalone original asset; controls, text and charts remain native Views.

The monotonic clock excludes pauses from active duration, bounds total lifetime
and retains the original wall-time anchor for graph intervals. Calendar alarm
comparisons still use actual wall time. A detected clock change is disclosed in
Report. Clip timestamps share that session axis; clip count/cooldown survive
pause/resume. Clock shifts have pure/source checks, not a real device time-change
or audio test. Microphone restart/permission denial, 12-hour behavior, battery,
Doze/reboot recovery and overnight signal quality remain unverified.

Sonar, automatic starts, stage estimation, sound classification and the remaining
phone features are still missing or partial. Screens 16/18 are not fully accepted;
the strict visual/behavior gate covers only the stated manual movement flow.
The full goal remains **FAIL/incomplete**. Earlier records below are historical.

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

## Native records milestone — 2026-10-23

- Local unit suite: **52 passed**, including 17 RecordModelTest cases.
- Unit/lint/debug app/debug instrumentation APK build passed; lint **0 errors,
  57 warnings**. New warnings remain mostly localization/accessibility follow-up;
  this is not a zero-warning claim.
- Isolated StorageMigrationTest (4), RecordStorageTest (7), RecordReplayTest (5)
  and RecordClockTest (1): **17 passed in 0.575 s** on S23 Ultra/API 36.
- RecordRecreationTest: **1 passed in 10.802 s**, containing delayed new and
  existing saves, Activity recreation and Back while a save is pending.
- HistoryFlowTest: **1 passed in 101.657 s**. Actual native Add, draft cancel,
  save/reopen, awake/tag/note edits, search/filter, Week/Month/Year, tracked trim,
  second-level short edit, and deletion of only selected fixture/clip verified.
  All 13 final captures compared by the strict reviewer against board 08.
- RootVisualTest regression: **1 passed in 4.344 s**; ChartVisualTest:
  **1 passed in 2.504 s**. Actual captures rechecked locally. Tonight keeps a
  plum system-navigation surface, centered grouped dock and one alarm-off label.

Earlier failures were fixed before acceptance: Android ICU rejected a JVM-valid
regex flag; Trends attempted to parent one View twice; editor content needed
scroll-aware lookup; charts clipped labels/summary; and post-dialog screenshots
caught icon transitions. Migration/record tests were rerun after the fixes. No
alarm, audio playback, microphone capture, screen-off or reboot was exercised.

Private full-app backups and original APKs were retained before device updates.
Comparison against the pre-record-test archive confirmed all original personal
alarm/session/epoch/clip/pause/edit values, exact preference keys/values and clip
bytes unchanged; schema 4 SQLite integrity passed. The Trends crash had left one
test cutoff key; its original absence was proven in the private archive and
restored by a one-time local instrumentation cleanup. The History test now avoids
changing that preference at all. The cleanup source was removed afterward.

Schema 4 adds edit metadata/provenance and persistent local creation tokens;
backup format 3 preserves overlays and zones and accepts legacy formats 1/2.
Original captures remain distinguishable from effective edited/manual periods.
Portable backups exclude creation tokens, audio, preferences and URI grants.
Missing dates stay unknown, overlapping duration is merged, and tagged naps do
not become main bedtimes. These checks prove the stated short/isolate behaviors,
not overnight tracking accuracy or full feature parity. See DESIGN-REVIEW.md for
the bounded pass and remaining full-goal failure.


## Plan, free goal bank and visible UI revision — 2026-10-23

- Local model suite: **74 tests, zero failures/errors**, including 22 new plan,
  commitment, closed-day, missing-data, overlap, nap, frozen-zone and DST cases.
- Unit/lint/debug app/debug instrumentation/release assembly pass. Current lint:
  **0 errors, 67 warnings**; this is not a zero-warning claim.
- Isolated storage/migration/replay/clock plus GoalStorageTest: **27 passed in
  0.955 s** on the S23 Ultra/API36. Ten new preference tests cover atomic revision
  rejection, legacy precision, commitment replay/overlap/cancellation, corruption
  preservation and concrete repository binding.
- GoalFlowTest: **1 passed in 34.526 s**. Native plan draft/recreation/cancel,
  save/reopen/downstream values, free commitment draft/cancel/create/reopen,
  derived balance, Stop/history preservation and Trends verified. The reminder
  adapter is a non-delivering test double. Ten real Android screenshots use
  declared isolated manual records and were visually compared with board05 and
  related supporting-state conventions. They are not a measured overnight night.
- Final GuardFlowTest: **1 passed in 9.583 s**. Actual installed-app selection,
  recreation, Back cancellation, atomic save, reopen and persistent pause deadline
  verified in an isolated preferences file. Personal table rows and preference
  values are compared with fixed-message assertions. No accessibility service
  was enabled, no selected app was launched, and enforcement remains unverified.
- RootVisualTest passed **12.274 s** for four selected dock states and read-only
  detail navigation before the last corrections. A subsequent run exposed the
  settings system-Back path returning through the wrong MainActivity. Source now
  registers platform Back handling and scopes test app buttons by package; final
  recreation/destination behavior is compiled but **not yet device-verified**.
- Debug/release compile only for developer-stop gating and unavailable-sensor/NFC
  Math fallbacks. No alarm/challenge/stop, sound, microphone, NFC, screen-off,
  reboot or actual reminder-delivery test ran.

The USB device disappeared before final RootVisualTest backup/install at04:45.
That attempt failed before installing or running anything. Final Privacy heading,
Wind-down row/countdown and system-Back fixes therefore remain pending actual
phone screenshots/behavior checks. Earlier images must not be labeled current
acceptance for those fixes. An isolated software emulator was attempted without
sound; its Windows process exited with access violation0xC0000005 before ADB
became available. No host hypervisor/driver or reboot changes were made.

All original personal table values, original preference keys/values and clip bytes
were compared between the private pre-update03:50 archive and last connected
pre-root04:37 archive: unchanged; SQLite integrity passed. Goal/Guard cleanup
waits for owned writers, Activities and readers before deleting only their UUID
fixtures. A failed barrier retains those private files instead of deleting under
an active writer. UI automation hands the CLI helper's exclusive connection to
the test runner; no uninstall/clear-data runner is used.

Full feature/screen parity remains incomplete. Remaining device constraints and
implementation gaps are preserved in FEATURE-MATRIX and DESIGN-REVIEW.
