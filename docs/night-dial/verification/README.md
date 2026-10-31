# Native review evidence

## Current delivered package: c5d558af

[Final combined native evidence](c5d558af/README.md): 20 methods, all 206 required frames, 84 focused unit methods. [Run 41196433872](https://github.com/omeyerrato/SleepWell/actions/runs/41196433872) passes in 462.107 seconds. The separate [appearance review](https://github.com/omeyerrato/SleepWell/actions/runs/41195038401) passes its full method and 11 original frames. The current source is integrated with main cc4871ee and all 127 native destinations are assigned across the 43 flow families.

Signed APK 0.1.25/code 26 is in Downloads, with the prior signer and exact [receipt](c5d558af/build-receipt.json). [Actual native preview](c5d558af/native-preview.png). The searchable Downloads gallery includes 217 original frames. No phone installation occurred.

The full local unit suite is still non-green: 1,078 tests, 10 inherited failures. See [exact limits/results](eb4ced67/full-unit-summary.json). All hardware/provider/state acceptance is not inferred from route accounting or emulator captures. Historical previews and failed intermediate reviews below remain pinned and are not substituted for final evidence.

## Historical preview: 6409f7a6

The [native review](https://github.com/omeyerrato/SleepWell/actions/runs/41092841075), job 124190892729, artifact 12994923015, passed all 11 native methods in 274.602 seconds. All 93 required PNGs exist: 78 Night Dial frames and 15 existing skip-regression frames. Eighty focused unit methods passed with zero failures/errors. [Raw result](6409f7a6/review.json), [instrumentation](6409f7a6/instrumentation.txt), [exact scope and limits](6409f7a6/README.md). The [independent APK workflow](https://github.com/omeyerrato/SleepWell/actions/runs/41092841167) passed; its default broad unit/lint step is skipped. Artifact ZIP SHA256: `30dd9a13f20b5ac8b8bfe806b5ab75117a3ecd0ee9c8f48a406e8bd333fa7487`.

New evidence covers named Skip review/cancel/recreation/actual next time/Undo/stale notice, off duplicate draft/recreation/explicit Save, named Delete/cancel/recreation/stale identity, and Save from dirty Back. Existing recurring skip, failed-scheduling/retry/reopen and one-time expiry assertions remain. Native sheet geometry checks and inspected screenshots confirm the short-panel trailing gap is corrected while long lists still scroll.

Selected frames: [Skip restored](6409f7a6/night-dial-alarm-skip-restored.png), [Skip applied](6409f7a6/night-dial-alarm-skip-applied.png), [copy draft](6409f7a6/night-dial-alarm-duplicate-restored.png), [Delete restored](6409f7a6/night-dial-alarm-delete-restored.png), [dirty actions](6409f7a6/night-dial-alarm-unsaved-actions.png), [ramp choices](6409f7a6/night-dial-alarm-ramp-picker.png), [expired one-time](6409f7a6/alarm-skip-expired.png), [scheduling failure](6409f7a6/alarm-skip-schedule-error.png).

The signed local APK was built from 6409f7a6 with app/src/main tree `d281d8f280345e8a6ae767169ed847284a7391a3`. Size: 327,023,844 bytes; SHA256 `08e5ee1cc242726fb23c222dad6b486d2450706c62711fc85425cd0800cc5755`. The signer certificate SHA256 remains `49eeeb7f74344335321800e3f78b9bb9ab25c5fff059332c458133dca0da0e1c`. APK and receipt are in Downloads, with prior pinned previews preserved. Delivery is APK only; no phone installation or physical ADB interaction occurred.

This is narrow progress: one normal-size API 35 renderer, with the remaining 43-flow states, challenge/schedule/source branches, large text, landscape, TalkBack, reduced-motion runtime, physical haptics/playback/sensing, overnight reliability and latest-feature integration pending. The 11 baseline-reproduced broad-unit failures were not rerun here. Full app acceptance remains false.

Historical finding: [c210599c](c210599c-finding/README.md) passed nine native methods, 78 frames and 80 focused unit methods, but screenshots exposed a trailing gap beneath short sheets. The [7da4cff7 regression expansion](https://github.com/omeyerrato/SleepWell/actions/runs/41090492581) passed 11 methods and 93 frames in 312.988 seconds with the same pre-correction app source. Final 6409f7a6 independently verifies the shared sizing correction. Neither earlier functional pass approves the old gap.

## Earlier reviewed preview: 37de8841

The [native review](https://github.com/omeyerrato/SleepWell/actions/runs/41083871279), job 124159782214, artifact 12989899796, passed all seven native methods in 193.422 seconds. All 64 required PNGs exist; 80 focused unit methods passed with zero failures/errors. [Raw result](37de8841/review.json), [instrumentation](37de8841/instrumentation.txt), [exact scope and limits](37de8841/README.md). The [independent APK workflow](https://github.com/omeyerrato/SleepWell/actions/runs/41083871221) also passed; its default broad unit/lint step is skipped. This is not a global-suite-green claim.

The review retains the prior dial, mixer, tracking and report journeys and adds alarm-list-to-editor draft flows: name entry, repeat validation/correction, recreated discard confirmation, Memory task selection/order, requested gentle-wake envelope and configured snooze shortening/off. Only explicit Save alarm writes the reviewed settings and reaches the silent scheduling port. Native images were inspected, including corrected inactive weekday contrast and cleared repeat validation. No measured health/output claims are inferred from configuration previews.

Native captures: [alarm list](37de8841/night-dial-alarm-list.png), [name draft](37de8841/night-dial-alarm-name-restored.png), [corrected repeat](37de8841/night-dial-alarm-repeat-interval.png), [discard restored](37de8841/night-dial-alarm-discard-restored.png), [gentle configuration](37de8841/night-dial-alarm-gentle.png), [snooze configuration](37de8841/night-dial-alarm-snooze.png), [dial under pointer](37de8841/night-dial-dial-under-finger.png).

The signed local APK was built from 37de8841 with app/src/main tree `aeba57ed9b1cbded14ba535fead391dcd142d997`. Size: 327,016,400 bytes; SHA256 `54ccd19d88c2769caa7d66b8bfe735be20c0347031bab050ea8e42b7e0a834e3`. Signer certificate SHA256 `49eeeb7f74344335321800e3f78b9bb9ab25c5fff059332c458133dca0da0e1c` matches the previous local preview. APK and receipt are in Downloads; the user selected APK only. No personal phone installation or physical ADB interaction occurred in this pass.

Limits: one normal-size API 35 renderer; all 43 flows, remaining challenge/schedule/source/import/provider branches, enlarged text, landscape, TalkBack, reduced-motion runtime, physical haptics/playback/sensing, overnight reliability and integration against the latest feature branch remain open. The 11 baseline-reproduced broad-unit failures below were not rerun here. Full app acceptance remains false.

## Earlier reviewed preview: fca53502

The [native review](https://github.com/omeyerrato/SleepWell/actions/runs/41069491851), job 124109531703, artifact 12984193075, passed all five native methods in 162.904 seconds. All 42 required native PNGs exist; 59 focused unit methods passed with zero failures/errors. [Raw result](fca53502/review.json), [instrumentation](fca53502/instrumentation.txt), [exact scope and limits](fca53502/README.md). The independent APK workflow also passed; its default broad unit/lint step is skipped, so this is not a global-suite-green claim.

This extends the prior dial/Settle in/tracking review with saved-night navigation, source filtering, actual signal scrub/recreation, protected edit drafts and reviewed original-time restore. The canonical dated night is 462 minutes against a 480-minute goal (18 minutes short). An independent seven-day fixture totals 3,234 minutes, averaging 462 minutes per observed day; missing date ranges show an unknown average and zero coverage, not zero sleep.

Native captures: [Night](fca53502/night-dial-report-night.png), [signal inspection](fca53502/night-dial-report-signal-restored.png), [history source filter](fca53502/night-dial-report-history-imported.png), [week](fca53502/night-dial-report-trends-week.png), [empty range](fca53502/night-dial-report-trends-empty.png), [empty actions](fca53502/night-dial-report-trends-empty-actions.png), [restored dirty prompt](fca53502/night-dial-report-dirty-restored.png), [restore review](fca53502/night-dial-report-restore-review-restored.png), [restored report](fca53502/night-dial-report-restored-night.png).

The signed local APK was built from c1d4a75f; `app/src/main` is `7a7a88147f7074879f0a8e7e6eecf9c39777928f` at both c1d4a75f and fca53502. APK size 327,008,384 bytes, SHA256 `a8ca970f79a92220bef099375ed7457bdc63cc06d572685274e4b360fb7ca5b4`. Signer certificate SHA256 `49eeeb7f74344335321800e3f78b9bb9ab25c5fff059332c458133dca0da0e1c` matches the previous local preview. The user selected APK-only delivery; the file and receipt are in Downloads and the ignored local delivery directory. No phone installation occurred.

Limits: one normal-size API 35 emulator; all 43 flows, provider/import/calendar/manual branches, optional annotation reset, enlarged text, landscape, TalkBack, physical haptics/playback/sensing, overnight reliability and integration against the latest feature branch remain open. The 11 reproduced inherited failures below remain open. Intermediate failed runs are explained later and [initial visual finding evidence](5eb55a6e-finding/README.md) is preserved. Full app acceptance is false.


This folder contains actual Android renderer captures, not generated UI mockups. Fixtures are synthetic and isolated; no personal phone data is included. No full flow is accepted yet.

## Earlier passing review: 7d726b2b

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


The [follow-up at c1d4a75f](https://github.com/omeyerrato/SleepWell/actions/runs/41068333159) passed the full canonical Week/Month/Year/missing-range journey and confirmed readable goal/coverage columns. Its saved-night journey stopped on an incorrect new test label, `8 h`, while the actual native row correctly showed `8 h 0 min`. fca53502 corrects only that exact test expectation; the application-source tree and packaged APK remain unchanged. This intermediate run is also a failed review, not a passing release claim.


## Alarm review finding at 7c5f191f

The [first expanded alarm review](https://github.com/omeyerrato/SleepWell/actions/runs/41075386373) passed its build, 80 focused unit methods and the five prior native methods. The new name route exposed a child tooltip consuming taps on the name value. The routine/output/snooze method verified its domain assertions, then attempted its final capture after a standalone editor had returned to Home. Follow-up 57f4cec0 moves tooltips to their actionable rows, exercises a pointer on the name value, retains name/dirty/repeat restoration assertions, returns through the real alarm list, and adds the zero-snooze branch. [Historical result and images](7c5f191f-finding/README.md). This intermediate review is explicitly failed, not a passing alarm acceptance claim.

## Alarm observation and visual correction follow-up

The [57f4cec0 review](https://github.com/omeyerrato/SleepWell/actions/runs/41078614533) passed both alarm journeys, four other native methods and all 80 focused unit methods, but failed while the dial test reused a replaced accessibility node to prepare geometry before pointer injection. The helper now snapshots one fresh rectangle with bounded observation reacquisition; it never retries input. [Historical frames/result](57f4cec0-finding/README.md) also retain unreadable inactive weekdays and a stale repeat error after correction.

[d3a59ff1](https://github.com/omeyerrato/SleepWell/actions/runs/41082082148) passed all seven methods and 64 frames in 207.642 seconds after geometry/readability fixes. A further visual finding led to clearing corrected repeat validation without replacing the field and an explicit native assertion. Final 37de8841 independently passed with that correction; earlier functional green does not approve the stale-error pixels.
