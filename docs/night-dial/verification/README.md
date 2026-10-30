# Native review evidence

This folder contains actual Android renderer captures, not generated UI mockups. Fixtures are synthetic and isolated; no personal phone data is included. No full flow is accepted yet.

## Current passing review: 7d726b2b

[The final run](https://github.com/omeyerrato/SleepWell/actions/runs/41059754092) passed the application/test-APK builds, 25 focused unit methods and all three native journeys. All 26 required screenshots exist. [Result](7d726b2b/review.json), [instrumentation](7d726b2b/instrumentation.txt). The matching signed local preview APK was built successfully and verified with apksigner; the user selected APK-only delivery and no phone update was performed.

- [Dial under a real pointer](7d726b2b/night-dial-dial-under-finger.png): independent time handles, exact live value and duration. Pointer cancellation restores the saved interval; release opens review. [Restored plan draft](7d726b2b/night-dial-dial-review-restored.png), [stale-save conflict](7d726b2b/night-dial-dial-conflict.png), [linked alarm review](7d726b2b/night-dial-dial-linked-review.png).
- [Settle in](7d726b2b/night-dial-settle-in.png): actual source waveform excerpts, volume controls, timer/fade and sensor settings. [Timer draft](7d726b2b/night-dial-settle-timer.png).
- [Microphone consent](7d726b2b/night-dial-settle-microphone-consent.png): full-width native actions with separated, measured 48dp-or-larger bounds. [The restored consent](7d726b2b/night-dial-settle-consent-restored.png) retains the choice without enabling the microphone; Cancel restores Off and starts nothing.
- [Active tracking](7d726b2b/night-dial-tracking-active.png), [paused](7d726b2b/night-dial-tracking-paused.png), [resumed](7d726b2b/night-dial-tracking-resumed.png): actual coordinator/SQLite data from synthetic PCM, with the full measured graph above controls. Two seconds of paused wall time remain excluded from recorded time and exist in the saved pause interval.
- [End confirmation](7d726b2b/night-dial-tracking-end-confirmation.png), [restored confirmation](7d726b2b/night-dial-tracking-end-restored.png): rotation does not stop/restart capture, cancellation keeps the current state, explicit End & review saves the session and measured epochs.

The reviewed `app/src/main` Git tree is `c9bc0b1597ecedd13ec2639c84c858f0133e81f1`. Documentation/capture additions do not change it. These checks cover one normal-size API 35 phone renderer. Physical output/haptics, large text, TalkBack, other device sizes, overnight behavior and the remaining app flows are not accepted by this run.

## Initial native review: 37155aed

- [GitHub review run](https://github.com/omeyerrato/SleepWell/actions/runs/41037128022).
- Android API 35 emulator, 1080 × 2316px at 420dpi, 12-hour clock, normal font size, animations enabled, host audio disabled.
- Native roots and dial-help rendered: [Tonight](37155aed/night-dial-tonight.png), [dial explanation](37155aed/night-dial-dial-help.png), [Alarms](37155aed/night-dial-alarms.png), [Settings](37155aed/night-dial-settings.png).
- The test failed when the capture helper checked for focus once during the Sleep sounds Activity transition. It reached the sound page but did not produce an accepted focused sound capture. [Instrumentation result](37155aed/instrumentation.txt) and [scope/result](37155aed/review.json) are retained. The full artifact also has guest logcat/window diagnostics.
- The revised helper waits up to five seconds for a real focused, laid-out, hardware-rendered app window, then requires a committed frame. It retains the same capture precondition. Failure diagnostics are independent of that acceptance helper.

Visual findings from inspecting the actual frames:

1. The circular clock is substantial and the planned duration/time endpoints come from the isolated saved plan. The native dial, white primary action and amber selected navigation render on the dark surface.
2. Tonight's old optional planning block crowds the sound card below the first viewport. The next revision moves optional rows below the sound card, keeps the mandatory alarm summary above it and trims unused dial space. Re-verification is pending.
3. Settings carries too many amber boxed icons. The next revision uses neutral unboxed icons, calmer row type and more useful label space. Re-verification is pending.
4. The off alarm time keeps an old brown-grey literal and excessive display weight. The next revision uses the secondary-text token and 48sp/500 display role. Re-verification is pending.
5. The dial explanation overrides its primary action with amber. The next revision restores the shared white primary action. Re-verification is pending.
6. This run covers one phone size and default text only. It provides no evidence yet for enlarged fonts, TalkBack, landscape, all popup states, complete tracking/report/challenge flows or tactile feel on hardware.

## Second native review: 62a6eb21

- [GitHub run](https://github.com/omeyerrato/SleepWell/actions/runs/41039330821).
- Seven accepted focused-frame captures were produced: Tonight, dial help, Alarms, Settings, Sleep sounds, time picker and invalid time. Activity focus waiting worked at the sound transition.
- [Tonight](62a6eb21/night-dial-tonight.png) now shows the complete sound card with the dial and compact alarm summary in the first viewport. The optional plan section scrolls below. [Settings](62a6eb21/night-dial-settings.png) now uses neutral unboxed icons. [Invalid time](62a6eb21/night-dial-time-invalid.png) shows an app-owned raised sheet, proper fields and reachable Cancel.
- The test then attempted to find a root-page control during the sheet's exit animation and dereferenced a missing UiObject. [The failure frame](62a6eb21/night-dial-failure.png) shows the actual sheet partway through its exit, not a disappeared root control after completion. The next test explicitly requires the sheet to leave before reopening the field and retains a bounded wait/assertion for that field. Recreation is still unproven until the next run passes.
- Further findings: the endpoint icon wells must draw after ticks, the sound player's Rain artwork still needs the new tonal image, and invalid-time copy should use the semantic error role. These corrections are in the next revision. The sound player still needs the full Settle in composition; styling alone is not that flow's completion.

## Passing native review: 359fd614

- [GitHub run](https://github.com/omeyerrato/SleepWell/actions/runs/41041383860) passed its app/test-APK builds, focused contrast and interval tests, and native review journey.
- All eight required frames exist, with no missing frames: Tonight, dial help, Alarms, Settings, Sleep sounds, time, invalid time and restored time. [Result and exact scope](359fd614/review.json).
- [Tonight](359fd614/night-dial-tonight.png) has the foreground endpoint wells above the ticks and the saved-mix Rain card. [The restored time sheet](359fd614/night-dial-time-restored.png) retains 09:15 after Activity recreation. [Sleep sounds](359fd614/night-dial-sounds.png) uses the new Rain photograph with native controls.
- The journey asserts cancellation leaves saved plan values unchanged, restores appearance preferences, preserves its disabled alarm, and creates no recording or playback. This is still one normal-size phone configuration, not acceptance of all 43 flows, all 28 challenges, keyboard/TalkBack/large-text behavior, bedside recording or physical haptic feel.
- The next small compatibility correction retains a visible date caption for alarms beyond the next night and respects the saved illustration-off setting with a compact artwork-free sound card. Its source is locally built; extended native coverage is still pending.

## Passing interactive review: e0cdba42

- [Native review run](https://github.com/omeyerrato/SleepWell/actions/runs/41057504399) passed the APK/test-APK build, 25 focused unit methods and all three native journeys in 91 seconds of instrumentation. [Exact result](e0cdba42/review.json), [instrumentation](e0cdba42/instrumentation.txt).
- All 24 required native frames exist. The environment remains API 35, 1080 × 2316px at 420dpi, normal font, animations enabled and host audio disabled. No phone data, hardware capture, audio playback or OS alarm delivery is involved. Tracking uses generated PCM through the existing coordinator and real private SQLite adapter.
- Dial: actual independent handle gestures, live preview, review Cancel, saved bedtime/wake/duration, pending-edit recreation and stale-save rejection. [Plan review](e0cdba42/night-dial-dial-review-restored.png), [conflict](e0cdba42/night-dial-dial-conflict.png).
- Linked alarm: saving the plan leaves the alarm untouched; the proposed alarm time survives recreation; only explicit Save alarm reaches the silent scheduling port. Enabled state, all weekdays and Math challenge remain. [Linked review](e0cdba42/night-dial-dial-linked-review.png), [alarm draft](e0cdba42/night-dial-dial-linked-alarm.png).
- Settle in: opening remains silent, timer draft selection survives recreation and Cancel leaves the saved mix unchanged. Microphone-consent Cancel returns the switch to Off and starts no capture. [Source waveforms and controls](e0cdba42/night-dial-settle-in.png), [timer](e0cdba42/night-dial-settle-timer.png), [consent](e0cdba42/night-dial-settle-microphone-consent.png), [placement](e0cdba42/night-dial-settle-sensors.png).
- Tracking: actual generated-PCM measurements are visible in the entire graph above the fixed controls. Pause closes the capture port, recreation stays paused, Resume opens one new port, and End & review saves the session, pause and measured epochs. Cancelling End keeps capture active; recreating the confirmation neither ends nor restarts it. [Active](e0cdba42/night-dial-tracking-active.png), [paused](e0cdba42/night-dial-tracking-paused.png), [restored confirmation](e0cdba42/night-dial-tracking-end-restored.png), [resumed](e0cdba42/night-dial-tracking-resumed.png).
- The `app/src/main` Git tree is `55011706952e3c2be98a99db3703d8758102b72e` at this reviewed revision and at c696c17f. Later test-only hardening additionally checks a pointer-cancel sequence, waits explicitly for microphone-dialog dismissal, and advances synthetic paused wall time. Its separate result must not be confused with this passing run.
- Visual corrections from previous runs: wake-up chip width reserved, duplicated editor times removed, Now marker kept clear of handle glyphs, real short audio excerpts replace flattened whole-loop peak previews, and bedside chart bars/axes use neutral/amber tokens instead of the old purple gradient.
- Limits: one normal-size phone renderer, not all 43 flows, all challenges, large text, TalkBack, hardware haptic feel, physical playback/sensing, interruption reliability or an overnight run. The broad inherited unit failures below remain open.

### Earlier interactive review findings

9eedbeba passed the original root/picker journey and drag/save/conflict assertions but attempted a capture before the new Activity had focus. ed23c026 passed its full linked-dial/Settle in journey; its tracking capture helper incorrectly required the Activity to retain focus while its Dialog was open. c6be5d98 passed the measured tracking journey but raced the microphone Dialog's exit before checking its parent switch. The final helper waits for an actual focused, laid-out, hardware-rendered window and committed frame; the microphone return assertion now also waits for sheet dismissal and the parent control. No model assertions were removed.

### Pointer cancellation and paused wall time: c696c17f

[This review run](https://github.com/omeyerrato/SleepWell/actions/runs/41058493552) passed all three native journeys with 25 required frames. [The actual drag frame](c696c17f/night-dial-dial-under-finger.png) shows the selected wake-up value and live planned duration before release. Injected ACTION_CANCEL restores the original dial, opens no review and saves nothing. Advancing the synthetic clock by two seconds while paused leaves recorded time unchanged; the saved pause spans at least those two seconds. [Result](c696c17f/review.json), [instrumentation](c696c17f/instrumentation.txt).

Inspecting the earlier consent frame also exposed a visual defect that functional checks alone did not catch: the three horizontal native AlertDialog actions touched each other. The subsequent ReportDialogs adapter keeps the same native Buttons, IDs and callbacks but stacks full-width actions with explicit spacing. New assertions require full touch height, usable width and separated bounds. Microphone consent now has an owned recreation path, stays Off until accepted and closes its old window on destruction. These changes require their own later capture revision; the older passing frame remains historical evidence, not visual approval of that defect.

## Baseline build and unit evidence

`assembleDebug assembleDebugAndroidTest` passed locally at the initial review revision. GitHub's application and test-APK builds also passed for `37155aed`.

The broad local unit command completed 1,063 tests with 11 failures. A separate archive of baseline `669ec8f6`, with only the duplicate TrackingActivity focus override removed to allow compilation, reproduced the same 11 named failures in a 44-test run across those six classes. No feature-logic fix or changed assertion was applied in that baseline comparison. These are inherited failures; the suite is still not green.

Reproduced failures:

- `AlarmRequestTest.noDaysMeansOneTimeAndMissingTimeRemainsExplicit`
- `QrScanSessionTest.failedReleaseBlocksCaptureAndRearmUntilOwnedReleaseSucceeds`
- `SonarNightTest.realSignalLossPersistsNullRatherThanZeroAndStopRemainsResponsive`
- `SonarNightTest.userPauseCannotOverrideDiskFailureStopWhileNativeCloseIsPending`
- `SonarNightTest.realPcmNightPauseResumeStopPreservesAnchorQualityAndActiveGap`
- `SonarSharedNightTest.offNeverLoadsAnalysisOrChangesTheOneActualCaptureOwner`
- `SonarSharedNightTest.pauseWaitsForAnalysisAcknowledgementWithoutBlockingCaptureClose`
- `SonarSharedNightTest.oneCaptureFeedsBothRealSignalPathAndSoundFramingThroughPauseResume`
- `SonarSharedNightTest.modelFailureIsSavedAndCannotRetryOnResumeWhileSonarStillWorks`
- `TrackingDelayEntryTest.sonarConsentDoesNotAcquirePhoneClipOrLightChoices`
- `TrackingDelayTest.sonarNeverRequestsClipsOrLight`

The three focused `NightDialContrastTest` methods passed: reading/metadata/error ink on all three surfaces; ink on filled actions and disabled controls; essential boundaries/focus. These validate token pairs, not every remaining hardcoded color or every pixel in the app.

Local emulator boot is unavailable because the Android emulator hypervisor driver is missing. No personal installation was used as a fallback. The disposable GitHub renderer successfully booted and produced the four frames above.

## Report review finding at 5eb55a6e

The [first Report review](https://github.com/omeyerrato/SleepWell/actions/runs/41066938357) built both APKs and passed 59 focused unit methods. Four of five native methods passed, including the complete saved-night/signal/filter/dirty-draft/annotation-preserving restore journey. The Week/Month/Year fixture reached the empty range but failed when its assertion expected an offscreen empty-state heading without scrolling. This run is not a passing review: two final required frames are missing.

Visual inspection also found defects the initial assertions missed: the right-hand history durations and trend goal/coverage columns measured to almost zero width, and supporting signal links laid long titles and subtitles beside each other. Explicit weighted column widths and stacked destination copy correct these. The follow-up test checks actual visible text bounds, scrolls to the empty actions, and captures both the empty chart and its actions. No data assertion was removed and no missing range is treated as zero sleep.
