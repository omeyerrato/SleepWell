# Native review evidence

This folder contains actual Android renderer captures, not generated UI mockups. Fixtures are synthetic and isolated; no personal phone data is included. No full flow is accepted yet.

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

## Build and unit evidence

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
