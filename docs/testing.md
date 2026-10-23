# Testing and device verification

`AlarmProtectionFlowTest` is CI-emulator-only and requires explicit ciReview.
It injects UUID-isolated preferences/database, never schedules its display-only
alarm rows, and tests independent global settings, both links, disclosure Later
and recreation. It checks Alarm on is visible above Save and preserves personal
preferences/fixture rows. It never enables accessibility or runs an alarm/menu.
See the dated [configuration evidence](verification/2026-10-23-alarm-protection/README.md).

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
local test methods at that milestone. ChartSeriesTest adds five display-aggregation
tests (dense average, explicit/implicit gaps, true zero/invalid values, short final
epoch, finite extreme imports), bringing that milestone total to 23. TrackingClockTest (5), SessionTimingTest (3),
ClipBudgetTest (2) and two more ChartSeriesTest cases brought that milestone total to 35. AudioCopyTest adds seven actual-byte limit, empty/short/stalled read, provider failure and cancellation cases. NightWindowTest adds ten clock-window, boundary, weekly and DST cases. PowerMenuGuardTest adds eleven metadata, unknown-window-ID, emergency exclusion, liveness, timing, cancellation and failed-action cases with fake callbacks. The current local total is 126; see the dated
VERIFICATION entry for the exact native evidence.

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
python scripts/device_verification.py --adb /path/to/adb --serial DEVICE --flow charts
python scripts/device_verification.py --adb /path/to/adb --serial DEVICE --flow tracking
python scripts/device_storage_verification.py --adb /path/to/adb --serial DEVICE
```

Backups and output are under ignored `.device-backups/`; actual screenshots are
under ignored `captures/<flow>/`. Do not commit these private files. The helper
does not schedule alarms, start the microphone, change power state or uninstall
anything. Only the explicit tracking flow starts a movement-only session. It checks the instrumentation result rather than relying
on the shell command's zero exit code.

`RootVisualTest` exercises Tonight/Alarms/Report/Settings navigation and selected state,
captures the actual screens, rejects development prose/export controls on roots,
and leaves Tonight visible with a window-scoped keep-awake flag.

`AppearanceFlowTest` saves and reopens each manual palette, checks rendered root
colors, verifies Back cancellation, recreates MainActivity to check persistence,
and checks actual title scaling, static reduced-motion press drawables and
contrast. It scrolls to the enlarged primary action without invoking tracking.
It restores the exact original presence/values of its five preference keys in
finally. It never writes alarms, sessions or microphone settings. The helper
relaunches the review window even when the test reports a failure.

`ChartVisualTest` renders a clearly labeled synthetic eight-hour fixture in memory
through the actual native Report renderer, including a sensor outage and true
zeros. It selects an averaged bar and checks the announced range/count, then
captures the no-readings state. It writes no database rows or preferences and
recreates the real MainActivity afterward. Its screenshots are rendering evidence,
not proof of an overnight measurement or completed Report feature set.

`AlarmFlowTest` tests editor persistence and an actual quiet scheduled challenge.
It is currently **not passed**: a prior run failed after selecting a wrong answer.
Its follow-up cannot run while the user's no-alarm instruction remains in force.
No sound, screen-off, reboot or microphone test may be added to the silent test.

Screenshot capture is not a pixel-regression acceptance test. Compare actual
images with each numbered design board, record defects in DESIGN-REVIEW.md,
correct them and request the independent reviewer's fresh assessment. Responsive,
large-font, contrast and remaining permission/error states still need coverage.

## Tracking and storage checks

`TrackingFlowTest` first asserts no ringing alarm, wake check, existing recording,
or enabled stored alarm. It never disables an alarm to satisfy these conditions.
Microphone, clips, lullaby and light are forced off and their exact original key
presence/values restored. Only its captured new session ID is removed after
verifying the report. Failure injection uses a separate UUID-named DB and restores
the service's storage factory. No fake sensor values enter the production journey.
The short recording is controlled verification, not an overnight accuracy result.

`StorageMigrationTest` uses only isolated UUID-named databases and no AlarmManager
or preference effects. The storage helper backs up the whole private tree and
installed APK, runs these isolated tests before deliberately opening the new app,
then compares original logical column values and SQLite integrity. It avoids an
incompatible APK downgrade if schema migration has already happened. Format 2
exports preserve pause/active/interval provenance; format 1 remains readable.
Real document-picker export/import, microphone permission states and overnight
reliability still need their own device evidence. Never use a destructive reset
to work around a failed verification test.

## Record editing and calendar trends

Use `device_verification.py --flow history` for the silent isolated native
journey and `--flow recreation` for delayed save/recreate/Back checks. These
never start alarms, audio, tracking or microphone capture. Record storage is
injected through `Records.Factory` into UUID-named databases. The History fixture
is explicitly labeled synthetic/user-reported; its screenshots are display
evidence only. Draft cancellation, persisted trim/awake/tag/note edits, search,
filters, calendar ranges, manual no-measurement states and selected deletion are
exercised. Any synthetic WAV files contain silence and are never played.

`device_storage_verification.py` runs StorageMigrationTest, RecordStorageTest,
RecordReplayTest and RecordClockTest on isolated databases/in-memory views.
It compares the personal database's original columns after install/migration.
Tests include atomic rollback/retry, legacy format imports, reset revisions,
manual provenance validation, create replay after reopen, deletion/ID reuse, and
saved-zone labels using the phone's current clock format without changing it.
Recreation tests delay new/existing saves and require exactly one committed row.
Pure RecordModelTest covers interval unions, monotonic coverage, DST ambiguity,
calendar boundaries, missing days, overlapping naps and cross-day ownership.

Database schema is 4; portable backup format is 3 (1/2 remain readable). Do not
install an older schema-2 APK over a migrated personal database. Test failures
must retain a compatible APK and relaunch the scoped keep-awake window. Isolated
fixture databases can survive a process crash and are never production history.
Actual document-picker backup restoration, real overnight measurements and both
clock-format settings remain separate verification work.


`GoalModelTest` covers plan linkage/legacy precision, reminders through DST,
closed-day cutoffs, immutable commitments, missing days and frozen-zone balances.
`GoalStorageTest` uses UUID preferences and never schedules delivery.
`GoalFlowTest` uses isolated records/preferences plus a non-delivering reminder:
draft/recreation/cancel/save/reopen/commitment/Stop/Trends. It checks all personal
rows/preferences unchanged and drains owned readers/writers before deletion.
`GuardFlowTest` similarly isolates app-selection drafts and active pause state;
it does not grant accessibility access, open blocked apps or test alarms. Tests
reject active/interrupted personal tracking before opening MainActivity.

Final Privacy/Wind-down/system-Back rendering and navigation now have passing
API36 CI emulator evidence. Final S23 installation remains pending after USB loss.
See VERIFICATION.md and verification/2026-10-23-api36/README.md. Do not infer
behavior from an allowlist or source file alone. Alarm controls, including the
debug-only Developer stop, remain outside the permitted sleep-time test set.
Never run the whole connected suite.

SoundPlaybackTest has 24 local fake-sink cases and never instantiates Android
audio classes. SoundSettingsFlowTest is configuration-only and passes on the
API36 CI emulator and physical S23; see dated VERIFICATION.md for exact heads. It never
presses Play, starts tracking or imports a file. The manual Silent Android visual
review workflow defaults to it, RootVisualTest, NightArcFlowTest and AlarmProtectionFlowTest on an ephemeral no-audio emulator.

Optional workflow input `native_sounds=true` adds only NativeSoundFlowTest on that
same disposable emulator. The Python runner refuses non-GitHub execution or any
serial other than emulator-5554, checks qemu and launches its own emulator with
`-no-audio`. The test requires qemu plus explicit ciSilentPlayback=true. It is
absent from the physical-phone runner. This branch grants only POST_NOTIFICATIONS
in the fresh emulator, exercises actual MediaPlayer/FGS/background/notification/
focus/error behavior, and never changes system stream volume. Preferences are
UUID-isolated; cleanup restores the factory even when a barrier fails and retains
files whose readers cannot quiesce. Completion uses a valid 57-second offset in a
one-minute profile, not a full-minute fade test. All three allowlisted tests
pass at ce7f64c; exact durations and native scope are in VERIFICATION.md. The root
flow now repeats three Wind-down transitions and observes correct system-bar
flags before capturing; each detail also requires unique destination content,
absence of the Settings dock and a committed hardware frame before capture.
The [frame-commit API](https://developer.android.com/reference/android/view/ViewTreeObserver#registerFrameCommitCallback(java.lang.Runnable))
reports frame submission; UI idle and independent screenshot review remain
required. Configuration assertions wait for actual selected state, a distinct
resumed replacement after recreation and actual destruction after Back. SoundSettingsFlowTest
in CI asserts idle values and adds only absent ringing/tracking/trackingPaused/
session defaults before baseline, avoiding delayed first-boot writes. Every
key/value remains in body/cleanup equality, and physical execution is unchanged.


Native audio listening quality, long background sessions, real provider grants,
route changes, process death and S23 verification remain separate gates. Existing
no-alarm-testing instructions remain binding. All published CI data is fixture
only; never copy personal phone captures/backups into public evidence folders.

## Selected audio documents (CI only)

The manual silent-ui workflow has an `audio_imports` option, independent of `native_sounds`. Both run only in the fixed disposable qemu AVD with host output disabled. SoundImportPickerTest drives actual DocumentsUI against a read-only test-APK provider with fixed synthetic audio/invalid/oversize/denied/delayed fixtures. SoundImportReopenTest runs only after the first passes and the harness force-stops this disposable app, then explicitly plays a saved provider URI. The provider is absent from app debug/release APKs and advertises no roots on physical hardware. These tests are deliberately absent from the physical-phone script allowlist.

The tests bind UUID preferences, compare personal preferences and stream volume exactly, observe playback on main, close/drain every owned Activity and import worker before removing isolated preferences, and retain diagnostic fixtures if readers cannot finish. No source document is deleted or modified. Only known synthetic provider grants are released during CI cleanup. The five-flow run and actual PNGs are recorded in [dated evidence](verification/2026-10-23-audio-import/README.md); test source and a successful build alone are not a picker/playback pass.

SoundSettingsFlowTest also starts with an empty isolated preference profile and
asserts that merely opening the default/legacy mix writes neither defaults nor
errors. It then exercises saved choices, Activity recreation and reopen without
Play or import. The physical run compares the entire personal preference map.

Every sound configuration capture now waits for focused decoded artwork, a plum
Window background and light requested system bars across four observations,
then a committed hardware frame. The unchanged PNG must contain light glyph
pixels and at least 60% actual plum background within each status/navigation
inset. Tests never set those flags to make the capture pass. This catches the
observed first-Rain dark-glyph transition that idle/artwork checks alone missed.

## Rest-window clock (CI only)

NightArcFlowTest requires explicit ciReview plus qemu and is absent from the
physical-phone allowlist. It injects a clock only for Tonight's display, an
isolated database and an isolated saved plan. No OS clock, alarm scheduler,
tracking or audio action is invoked. The weekly alarm is a database-only display
fixture; it is never scheduled. The actual 30-second foreground Handler must
advance the retained arc while appearance is manual. Other checkpoints call
the same refresh method at constructed instants: 0/50/100 percent, after wake,
weekly occurrence and app extra-large text (1.3x). A separate scrolled capture
requires the large-text primary action to be at least 48dp visible above the
dock without activating it. It opens/closes the real
themed explanation. Every capture waits for a hardware frame.

Finally all owned Activities close before factories/clock restore and isolated
files are removed. Only the five modified appearance keys are restored; exact
whole-personal-map equality remains required. Pending/failed runs do not certify
the later states. See REST-WINDOW.md and dated VERIFICATION.md for actual scope.
