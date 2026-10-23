# Verification record

## Global optional alarm protection — 2026-10-23

Code `e8f4eb0` adds app-return/power-menu guards and the compact Alarms list;
`bc034e4` fixes metadata-only unknown window IDs; `ab01ba5` themes the disclosure
and keeps Alarm on above Save. [Final same-head native results and 30 original
captures](verification/2026-10-23-alarm-protection/README.md) retain four raw
passing transcripts/environment and hashes. CI39954661465 and normal Android
CI39954614529 pass. Local: 126 tests, zero failures/errors/skips; lint zero
errors/68 existing warnings; debug/test/release builds pass.

AlarmProtectionFlowTest (15.794 s) exercises independent saved global switches,
both entry points, disclosure Later, recreation, unchanged personal preferences
and unchanged isolated display alarm rows. It asserts a checked 48dp Alarm-on
control above Save. Other configuration regressions pass: roots 23.342 s, clock
arc 38.697 s, stopped sound settings 21.255 s. Enabled fixture rows are never
scheduled, saved through the editor or activated. No AlarmActivity/Service,
power menu or accessibility enablement occurs. Eleven pure guard tests use
fake callbacks. AOSP strips source IDs without content-retrieval capability;
unknown IDs are therefore allowed with package/class/event-generation fencing.
This reduces stale actions but cannot prove current input focus.

The same source is installed on the S23. Read-only RootVisualTest passes in
19.215 s with ten private actual captures. Immediate before/after archives
prove exact personal preferences and every database row unchanged, integrity
OK; only AndroidX profileInstalled changes among existing private files. Main
remains open with scoped keep-awake. No global timeout/nav/power changes, phone
alarm/audio/microphone/flashlight/vibration/tracking or screen-off test.

The independent reviewer opened all 30 final emulator originals and passed only
the bounded configuration/model/UI gate. Actual Samsung menu detection/BACK,
alarm app-return and delivery remain unverified by the user's no-alarm request.
Forced hardware restart, powered-off delivery and unknown OEM menus remain
limitations. Full editor parity and 36-screen/feature completion are incomplete.


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
## Sleep sounds and final root rendering — 2026-10-23

Verified code: `ce7f64c8abba3ddc57b92d1d33f06388976b7ea7`.
[Native review run 39907851771](https://github.com/omeyerrato/SleepWell/actions/runs/39907851771)
and [regular checks 39907849882](https://github.com/omeyerrato/SleepWell/actions/runs/39907849882)
are successful. Original evidence is committed in
[verification/2026-10-23-api36](verification/2026-10-23-api36/README.md): 17 unchanged
PNG captures, environment, three instrumentation transcripts and SHA256 manifest.
Only disposable emulator fixtures are public; no phone captures/backups were added.

- **98 local unit tests**, including 24 fake-clock/fake-sink SoundPlaybackTest
  cases; 0 failures/errors/skips. These are separate from native integration.
- **Lint: 0 errors / 68 warnings**. One new RtlHardcoded warning concerns the native
  slider clip origin; RTL behavior remains unverified. Debug app/test APK and
  release builds pass locally. The regular CI repeats unit/lint/debug checks.
- **SoundSettingsFlowTest: 1 pass, 18.746 s**. Rain→Ocean, 45-minute timer, 10-minute
  fade, recreation/reopen and exact preference equality in body/cleanup. It never
  presses Play or imports a document. CI-only fixture setup asserts idle values
  and initializes only absent inactive keys before baseline; the physical runner
  does not enable this branch. No existing value is overwritten or excluded from
  the equality comparison. Selection checks wait for the Ocean tile to become
  selected; recreation waits for a distinct resumed instance with the original
  destroyed; reopening waits for the actual Back action to destroy the screen.
- **RootVisualTest: 1 pass, 23.113 s**. Four root tabs/details/platform Back and
  three Tonight↔Wind-down transitions. The test observes light-glyph system-bar
  flags for four successive observations before the final Wind-down capture;
  it never sets appearance itself. Each detail requires unique destination
  content and absence of the Settings dock, then a hardware frame commit and
  UI idle before capture. This corrected a prior green test that saved Settings
  under the Privacy filename. No screenshot pixels were edited.
- **NativeSoundFlowTest: 1 pass, 26.323 s**. Explicit UI Play creates Android music
  activity; playing time advances after Home. Notification PendingIntent actions
  Pause/Play/Stop, frozen pause clock, live Ocean replacement, competing focus
  pause/no-auto-resume, offset timer completion, unreadable-provider error and
  explicit Rain retry pass. Native media activity, service and notification are
  absent after Stop/completion/failure. Stream volume and preference values are
  unchanged. No notification-shade/lock-screen touch test occurred.

The one-minute timer check begins at a 57-second offset and covers the final
three seconds. It is not full-minute fading, a long session or a listening test.
Host audio output is disabled. Only this opt-in CI branch grants notification
permission in its fresh emulator; root captures precede that grant. No physical
phone audio, alarm, microphone, tracking, vibration, screen-off, reboot or
Developer stop action ran. The S23 remains absent from ADB and has not received
this APK. Changed TrackingFlowTest was compiled, not rerun; earlier movement
results do not certify the new native sound ownership wiring.

The strict screenshot gate caught black Wind-down status/gesture glyphs at
cffc67b despite all three tests passing. Source now rejects posted appearance
updates from detached/replaced roots and reapplies the current route on window
focus. The new actual image shows white glyphs on plum and the Privacy image shows
its actual summary rather than the Settings root; independent final review
is recorded in DESIGN-REVIEW.md. Prior launch-helper and fresh-boot fixture
failures were not called complete passes: task reuse was fixed by closing/draining
owned Activities before reopening, and exact preservation exposed only the four
missing idle defaults added by delayed BOOT_COMPLETED. The equality assertion
was retained. Cleanup always restores the preferences factory and retains files
whose readers cannot quiesce.

Full 36-screen/feature parity remains incomplete. Native import/persisted grants,
listening/loop quality, full timer/fade, long/background/process-death behavior,
route changes/ducking, tracking ownership and S23 acceptance remain open. The
imported-audio hero still uses Rain art and needs a neutral visual. No fabricated
overnight chart, classified event or stage strip was substituted for missing data.
See FEATURE-MATRIX.md and SOUND-PLAYBACK.md for exact scope and remaining work.


## 2026-10-23 — actual audio picker, persistent access and quiet S23 update

Source/test commit114c25e2cf2e199da26347729f3d853e68567a0f.
[Five-flow silent CI39919149440](https://github.com/omeyerrato/SleepWell/actions/runs/39919149440)
and [normal CI39919099010](https://github.com/omeyerrato/SleepWell/actions/runs/39919099010) passed.
[Persistent32 raw files,26 actual PNGs, scope and hashes](verification/2026-10-23-audio-import/README.md).

- 105 local unit tests pass,0 failures/errors/skips. Seven new AudioCopyTest cases cover actual byte limits, partial/empty/stalled reads, cancellation-before-write and provider failure; no microphone or native sink.
- Debug, AndroidTest and release APKs build; lint has0 errors/68 warnings. Test-only synthetic DocumentsProvider is absent from app debug/release dex.
- SoundSettingsFlowTest16.203s, RootVisualTest22.135s, NativeSoundFlowTest24.718s, SoundImportPickerTest106.078s, SoundImportReopenTest14.385s; each1pass on the final API36 emulator head. Host audio disabled. Exact native scope is in the linked evidence README, not inferred from these counts.

Actual DocumentsUI selection now has byte-bounded validation using a disposable
private snapshot, retained job token, cancellation, newer-profile revision fence,
read-only persisted grants, native playback after an actual separate-process
restart and removal without deleting/modifying source bytes. Unknown-size valid
WAV and actual100MiB+1 overflow, invalid audio, declared oversized and denied-read
fixtures are exercised. No stopped mix starts merely because it was selected,
imported, recreated or reopened. Delayed-pipe recreation is portrait Activity
recreate(), not an orientation or landscape test. Interrupted pending-import
process death, background timeout, unsupported persistent grants, disk-write
failure and broad document/codec/cloud-provider compatibility are still open.

The first f2a2029 run passed the three old flows but failed the picker on a stale
shortcut obscured by the roots drawer. The drawer selector is now uniquely
scoped. A subsequent review caught an inadvertently recursive snapshot wrapper;
that candidate was cancelled and fixed before acceptance.1595432 passed all
five tests/25 captures.863e206 passed all five tests but the new lower-controls
image repeated the top view; the strict reviewer rejected that screenshot gate.
114c25e now scrolls to the end, requires a real44dp visible row above navigation
with the saved title, and waits for a committed hardware frame. The new actual
image shows My audio, saved filename and the tracking-playback switch. Neither
the old failed attempt nor the duplicate PNG is presented as final acceptance.
Final independent visual verdict is recorded in DESIGN-REVIEW.md.

### Physical phone: configuration/navigation only

The user reconnected the Samsung S23 Ultra (SM-S918B), API36,1080×2316,
override density420 (physical450), Samsung three-button navigation. A complete
private run-as tar archive was saved before install-r. Existing preference
values, every personal main-database table row and pre-existing private file
hashes were retained locally as the preservation baseline. No uninstall,
pm clear, lock bypass, screen-power action or global timeout/power change.

Samsung Maximum power saving initially displayed “App paused by Maximum power
saving” and blocked RootVisualTest before any root capture. Android CLI layout
and a private actual screenshot identified that system prompt. Selecting only
“Unpause temporarily” resumed SleepWell; global power saving remains unchanged.
A separate fresh read-only RootVisualTest then passed in20.315s, producing ten
private root/detail/real-report-selection captures. SoundSettingsFlowTest passed
in9.055s with four private Rain/Ocean/recreated/reopened captures. It never taps
Play/import or starts tracking. No ciReview bootstrap was passed on the phone.
The visible app was left in its scoped visualReview keep-awake window.

Post-run archive comparison proves exact personal preference values and exact
rows in every personal main-database table. The older isolated history-test DB
and journal also remain byte-identical and were deliberately retained. The only
pre-existing private-file change is files/profileInstalled, AndroidX's24-byte
compilation-verification cache. Its package-update timestamp matches the APK
install; this is not a user recording or preference. See the official
[ProfileVerifier source](https://android.googlesource.com/platform/frameworks/support/%2B/928fdc3d271dd4799ba085245c4d9c030e68b09e%5E%21/).
There were no pre-existing user audio files in this archive, so no clip-byte
preservation claim is inferred from an empty set. Baselines, private PNGs and
raw phone transcripts stay under ignored .device-backups/ and captures/;
no personal content was copied into public evidence.

The observed Samsung pause is a genuine reliability prerequisite: temporary
unpause is not a permanent vendor battery exemption. The readiness screen still
shows Background battery needs attention. Alarm delivery, reboot, screen-off,
physical playback/import, microphone, vibration, NFC, actual tracking/audio
ownership and Developer stop were not tested. These remain separate gates.
The user's no-alarm/no-emergency-stop-test instruction remains binding.
Full36-screen/phone-feature completion remains rejected; F061/F126 remain partial.

## 2026-10-23 — Phosphor icons and final silent phone review

Icon implementation: `f7f88ce`; conventional overflow correction:
[`efa9cae98afa9ceb9a13396a35e0012a25686484`](https://github.com/omeyerrato/SleepWell/commit/efa9cae98afa9ceb9a13396a35e0012a25686484).
[Final two-flow visual CI](https://github.com/omeyerrato/SleepWell/actions/runs/39928589826)
and [normal Android CI](https://github.com/omeyerrato/SleepWell/actions/runs/39928587756) passed.
[Persistent final 13 images, two raw results, environment and hashes](verification/2026-10-23-phosphor-icons/README.md).

The shared hand-drawn icon switch is replaced by 51 selected Phosphor SVGs,
converted without changing their paths or winding to local Android vectors.
Sources are pinned to upstream commit 2b75f3ad12b420c9504ef05df8d2564a28f8500e,
SHA256-locked and bundled with the full MIT license. The icon View retains
existing accessibility labels, click targets and tint behavior; it centers the
vector in a square instead of stretching it. The source audit verified every
alias, source hash, generated vector and license. The first filled overflow
glyph looked like a remote control in actual captures and was rejected despite
green tests. The final regular three-dot asset resolves that specific blocker.
See [notices](../THIRD-PARTY-NOTICES.md) and DESIGN-REVIEW.md.

Final local validation: 105 unit tests, zero failures/errors/skips; lint zero
errors and 68 existing warnings; debug, test and release APK builds pass.
API36 RootVisualTest passes in 25.796 s and SoundSettingsFlowTest in 23.383 s at
efa9cae. The latter now proves an empty/default sound profile opens without
persisting defaults or errors, before exercising choices/recreation/reopen.

The preceding five-flow run at cdf17d5 also passed with the new icon family:
settings 18.146 s, roots 21.685 s, native sound 25.664 s, picker 108.542 s,
reopened import 13.883 s, each one test. Its overflow image failed the visual
gate. Only the relevant roots/configuration flows were rerun after the More
asset correction; the three native/import tests are not claimed at efa9cae.
All native audio work occurred in the disposable emulator with host output
disabled. No phone playback or alarm was run.

### Physical S23 and preservation

At efa9cae, RootVisualTest passes in 19.187 s with ten actual private captures,
including the existing real sparse Report and selected measurement.
SoundSettingsFlowTest passes in 9.079 s with four stopped Rain/Ocean/recreated/
reopened images. No Play, import, alarm, emergency stop, microphone, vibration,
tracking, power-button, reboot or screen-off test. MainActivity is restored
outside instrumentation with scoped visualReview keep-awake; no global timeout,
power saving or navigation setting is changed.

Each install-r has an immediate private preinstall tar backup. The final archive
comparison proves the complete personal preference map and every row of every
personal main-database table unchanged; SQLite integrity passes. Every
pre-existing private file is byte-identical except AndroidX files/profileInstalled
compilation cache, which updates with package installation. The older isolated
database/journal remains untouched. Archives and phone captures remain ignored
and private.

Between the first earlier inspection and the 0d38193 install, default Rain mix
keys appeared before that second install; their cause was not established.
Every original preference value remained. The immediate preinstall baseline,
rather than an older map missing those keys, is used for each later full-map
comparison. No keys were reset to make the check pass. Opening an empty profile
now has its own passing no-write assertion.

The prior 0d38193 Window-background correction also has private S23 root/config
passes (19.814 s / 9.023 s). Actual white Samsung three-button glyphs replaced
the dim dark buttons on plum. Android edge-to-edge uses Window-background
luminance when deciding forced light navigation bars; the page and Window now
share their real color. Public API only; see
[Android Views edge-to-edge](https://developer.android.com/develop/ui/views/layout/edge-to-edge)
and [AOSP DecorView](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/com/android/internal/policy/DecorView.java).

Samsung Maximum power saving remains a reliability prerequisite; temporary app
unpause is not a permanent battery exemption. Full design/feature acceptance is
still rejected. Icon improvement is not certification of missing sonar,
classification, routines, integrations, long-night behavior or alarm delivery.

## 2026-10-23 — rest-window clock replaces the fixed arc

Code [`cfb5e68e24cdfcaa9f335107a2392987399d54fc`](https://github.com/omeyerrato/SleepWell/commit/cfb5e68e24cdfcaa9f335107a2392987399d54fc).
[Three-flow silent CI39940420018](https://github.com/omeyerrato/SleepWell/actions/runs/39940420018)
and [normal CI39940417536](https://github.com/omeyerrato/SleepWell/actions/runs/39940417536) pass.
[Persistent 22 original PNGs, three raw results, environment and hashes](verification/2026-10-23-rest-window/README.md).

The old 80% accent segment was fixed decoration. It now represents elapsed
clock time in the planned bedtime-to-wake window, with a Now marker, remaining
time, neutral upcoming/next states and an actual themed explanation. Hero,
endpoints, duration and fraction share the resolved instant/zone snapshot.
It does not measure sleep or imply that tracking is running. Weekly alarms use
one intended night and show their date; DST gaps cannot silently add a day.
The existing foreground Handler updates the retained view every 30 seconds,
including with manual appearance, without scheduling or saving preferences.

NightWindowTest adds ten meaningful pure boundary/daytime/weekly/DST tests.
Local total: 115 passes, no failures/errors/skips. Lint: zero errors, 68 existing
warnings. Debug, test and release builds pass. Native CI results: settings
21.212 s, roots 23.385 s, arc 38.907 s; each one passing test. The arc flow observes
the real Handler tick with an injected display clock and isolated plan/database,
retains the original View, opens/closes the real dialog and exercises 0/50/100%,
next window, weekly date and app 1.3x text. The scrolled large-text action must
be at least 48dp visible above the dock; it is not activated. No OS clock change,
alarm scheduling, playback, microphone or tracking action occurs.

The first native candidate failed at a case-sensitive platform dialog-button
selector before progression. The next passed all three tests but was visually
rejected because the marker ring overpainted the moon/sun endpoint glyphs.
The corrected ring clearance preserves those identities while retaining Now and
the ring on the free curve. Tick spacing/exclusion also avoids duration text.
These corrections use new actual captures; no screenshot pixels are edited.
Another 22-image candidate passed tests but was rejected for dark Rain system
glyphs on plum. Focused dark detail windows now reapply their appearance on the
next post and after 200 ms, guarded against focus loss/destruction. Sound
configuration capture requires stable flags/background and a committed frame,
then reads original PNG inset regions for actual light glyphs and predominantly
plum pixels. It never changes appearance from the test. All 22 final images
come from the same cfb5e68 run; no older Rain substitution.
DESIGN-REVIEW.md records the independent bounded verdict.

Matching production source is installed on the S23 (93ef122; cfb5e68 changes
only test assertions). Read-only RootVisualTest passes in 19.215 s with ten
private captures. SoundSettingsFlowTest passes in 10.929 s with four stopped
captures, including actual light-on-plum system-bar pixel checks.
Immediate preinstall and post-run
archives prove exact full personal preference-map and every personal database
row unchanged, SQLite integrity OK; all existing private files are identical
except the AndroidX compilation cache marker. Main is left open outside the
runner with scoped keep-awake. No global timeout/power/nav setting, phone audio,
alarm, emergency stop, vibration, microphone, tracking or screen-off test.
Active-window/weekly/large states were tested only on the disposable emulator.

Real system time/zone broadcast delivery, broad OS font scales/RTL/landscape,
an overnight recording and the remaining phone features are not certified.
The dashboard feature row stays partial. Full 36-screen/feature completion
remains rejected.

## Parallel features and integrated native checks — 2026-10-23

[Silent run39997627697](https://github.com/omeyerrato/SleepWell/actions/runs/39997627697)
at `8fd7dd9` is an overall **failure**, with nine of twelve allowlisted native
classes passing. SoundAnalysisNativeTest passes all three methods: the bundled
CPU model processes synthetic negative signals; real SQLite analysis-only
events, interval linking and clip deletion are checked. SoundEventsUiTest and
SnoreCueSettingsNativeTest pass filtering/configuration/recreation/reopen with
fake effects. StorageMigrationTest passes all four methods. These are distinct
from microphone capture, positive-class accuracy, cue delivery or overnight
tracking, none of which is certified.

AlarmMenusFlowTest fails because its first-option visibility assumption no
longer matches the selected-option reveal. AutomaticTrackingFlowTest times out
on its second Activity launch; the old Save oracle also did not distinguish Save
from Arm. ReportReferenceFlowTest produces seven original report images but fails
the immediate focus assertion on reopened capture. Each failure remains a gate;
the following source fixes require a new native run. The report reviewer also
rejects clipped Read details and repeated capsule history ribbons.

Three agents work in isolated Git worktrees on reports, automatic/travel flows
and sound analysis/library; the primary handles shared controls and integration.
See [current assignments](PARALLEL-WORK.md). The real CC0 fixture gate now requires
actual PCM through production framing, classification, grouping and reopened
storage to yield both snoring and speech events. Its source and licensed bytes
are committed; an upcoming run must establish actual results. No physical phone
alarm, audio, microphone, vibration, torch, power-menu or screen-off tests ran.
