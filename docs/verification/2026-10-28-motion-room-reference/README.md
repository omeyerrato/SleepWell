# Motion and Room conditions reference evidence

CI run [40601122904](https://github.com/omeyerrato/SleepWell/actions/runs/40601122904), captured at `203cb4177c07d198fc8ea86874fa69ad215edd1e`.

`MotionConfigurationFlowTest#fakeSamplesSettingsPersistLifecycleAndUnsupportedErrorsWithoutHardware` **passed in 30.873 seconds (one existing method)**. That method includes its RoomConditionsFixture continuation. All 17 original Motion/Room PNGs were opened individually and compared with [Board 10, middle and right](../../../design-concepts/2026-10-22-phone-app/10-phone-sensors.png).

**The combined CI job failed.** The original summary reports OrganizedSettings and Motion passing, Guard failing, and `full_app_review: false`. Guard failed in **30.7 seconds** at `GuardFlowTest.openWindChild:55` / method line 98: one native Guided wind-down click did not launch GuidedWindDownActivity. The raw diagnostic records MainActivity still resumed after that input. The later `759ce8fe` source moves consuming child tooltips onto the clickable row; that fix is outside this capture and is not verified by this archive. Guard PNGs and the separate OrganizedSettings PNGs are not duplicated here.

## Original artifacts and provenance

The 17 PNGs, exact Motion instrumentation log, exact failing Guard instrumentation log, `guard-failure.txt`, `room-conditions-dialog-input.txt`, `environment.json`, and unmodified `focused-review-summary.json` are copied byte for byte from `.android-local/ci-reviews/40601122904/silent-android-review/`. [original-sha256.json](original-sha256.json) records all 23 original file sizes and SHA-256 hashes. Local attributes prevent Git text conversion of originals.

The environment records a disposable API 36 `sdk_gphone64_x86_64` emulator, 1080 x 2316 override at density 420, host output disabled, no native playback allowlist, and three selected existing methods. The summary's 45 required images describe the combined job, not this 17-image archive.

[source-equivalence.json](source-equivalence.json) verifies exact Git blobs and SHA-256 for 12 listed Room/Motion production files and the two existing fixture/test files between captured `203cb417` and comparison `759ce8fe9afed97ff950d59ff4cd73e68ca56b39`. The Room settings route, Main RoomDimming hook lines, and nonexported manifest entry also match. MainActivity as a whole differs in the unrelated Wind-down row fix; whole-file Main equivalence is not claimed.

## Individually inspected frames

| Original PNG | Actual observation |
| --- | --- |
| motion-settings-ready.png | Cream layout, registered sensor, sparse rolling movement bars, separate signal heading/detail, Medium sensitivity, and fixed Test again / Use this sensor actions. Earlier unsampled time remains blank. |
| motion-settings-changed.png | High sensitivity visibly selected while the existing sampled trace remains unchanged. |
| motion-settings-recreated.png | High draft survives; a new registration shows Waiting for motion and an empty chart rather than replaying Detected. |
| motion-settings-reopened.png | Saved High sensitivity reopens; no invented movement history appears. |
| motion-settings-conflict.png | Full red conflict explanation remains legible and the High draft is visible; the fixture verifies the concurrently saved Low value is preserved. |
| motion-settings-unavailable.png | Unavailable state, empty trace, complete missing-accelerometer explanation and disabled Use action. Supporting Surface calibration begins below the fold. |
| motion-settings-error.png | Not active state, empty trace, complete registration error and disabled Use action. Supporting calibration remains below the fold. |
| room-conditions-default.png | Plum light/sound cards, injected 2.0 lx current value and sparse current-session samples, Not measuring, microphone OFF and dimming OFF; both switches and fixed save action are visible. |
| room-conditions-microphone-access.png | Native owned rationale with complete privacy copy and separate Not now / Continue buttons; background measurement is inactive while the dialog owns focus. |
| room-conditions-measuring.png | Explicit activation shows 25% relative digital RMS, proportional segments and microphone ON; light honestly says no readings yet after a fresh launch. |
| room-conditions-recreated.png | Microphone returns OFF with Not measuring and empty segments; no automatic capture restart. |
| room-conditions-dimming-saved.png | Dimming ON with complete saved confirmation immediately above the fixed action; microphone remains OFF. |
| room-conditions-reopened.png | Saved dimming remains ON, microphone remains OFF, and absent new light readings stay empty. |
| room-conditions-microphone-error.png | Complete interrupted/unavailable explanation, microphone OFF and inactive segments; saved dimming remains ON. |
| room-conditions-unavailable.png | Complete missing-light-sensor explanation and empty chart; no substituted light value or Quiet label. |
| room-conditions-cleanup-blocked.png | Complete cleanup warning and full Retry microphone cleanup action; switches and fixed save remain visible. |
| room-conditions-cleaned.png | Retry action disappears after cleanup; stopped explanation and microphone OFF remain, with saved dimming ON. |

## Executed functional scope

Motion uses injected accelerometer callbacks and a private UUID preferences fixture. The existing method verifies sensitivity draft recreation, persistence, concurrent-change refusal, unavailable/start-error states, source closure and the 48 dp information action. No hardware sensor port is created.

Room uses the real Activity, controls, light-history rendering, RoomSoundLevel state owner and MicrophoneLease with injected light/audio/access ports. Its continuation verifies the 0 and 2 lux samples; rationale cancel, Back and recreation without permission/capture; a real native Continue input to the measured focused dialog button; one fake permission request only after Activity focus returns; then a separate explicit microphone activation. The dialog input log preserves root-visible and screen coordinates for both buttons.

The audio fixture supplies shorts of 8192 (quarter-scale RMS, displayed 25%). Recreation closes capture and leaves microphone OFF. Dimming draft survives recreation but saves only through the explicit footer action; the window brightness attribute becomes at most 0.05 and is restored exactly on leaving. Reopen preserves only the saved dimming preference. Injected open/unavailable/cleanup failures exercise truthful inactive states, held ownership during unconfirmed cleanup, and explicit retry releasing the lease. The fixture confirms the Android microphone grant is unchanged, restores both Room preference keys and all original preferences, and leaves no microphone owner or lease. These are fake-port/native-widget checks, not physical microphone or brightness measurements.

## Reference differences and limits

Motion now follows the reference's signal/card/sensitivity/action hierarchy. Its upright purple phone outline still differs from the horizontal illustrated phone with motion waves. The extra calibration row requires scrolling in the longer failure layouts; it is not fully visible in these initial captures. The captured primary controls and error text remain legible.

Room follows the reference's plum cards, light chart, segmented sound level, two switches and lavender footer. Its current-session time axis, sparse measured lux points, explicit uncalibrated RMS labels and OFF = Not measuring behavior intentionally avoid the board's illustrated evening history and Quiet value with microphone OFF. The sun glyph and decimal lux formatting also differ from the board's bulb/integer treatment. The ordinary Room states leave more space above the footer than the board; the cleanup state uses that space for its genuine recovery action.

Actual Android runtime permission dialogs/settings recovery, physical light/microphone behavior, native AudioRecord interruption/release, physical display dimming, device font/size variants, and Wind-down's application of saved dimming remain unverified here. The fixture never starts a real alarm, tracking recording, physical sensor, native microphone or playback. No raw PCM is persisted or uploaded by this fixture. F172 remains partial pending device and complete UI acceptance. This archive does not establish all 36 screens or whole-app acceptance, and no new tests, CI run, build or physical-device action was performed to create it.
