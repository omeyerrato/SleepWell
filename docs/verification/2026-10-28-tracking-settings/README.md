# Tracking setup and organized settings: bounded evidence

[CI 40584591092](https://github.com/omeyerrato/SleepWell/actions/runs/40584591092), source `fdb8006966ceec726174cfd25ca2bfe0ba72aa11`: **OrganizedSettingsJourneyTest#organizedMenusPreserveDraftsAndSafeChoicesWithoutOutputs passed in 55.256s, one method, 19 original captures. Overall CI FAILED.** AlarmProtectionFlowTest failed in 29.637s looking for `com.google.android.permissioncontroller:id/permission_deny_button`; its raw transcript and the unchanged combined summary retain that separate failure and four missing Protection frames. This folder contains only the passing method's 19 PNGs, without duplicating unrelated Protection captures.

All 19 originals were opened individually. The eight Tracking frames were compared with board 06 left and the previous run 40569431021 main/More/Motion screenshots. Original PNG/log/environment/summary bytes are unchanged. `run.json` independently records the same source and failed job; `source-equivalence.json` records scoped source/asset blob digests and equality with root's committed and working-tree state at archive time. SHA256SUMS covers every retained file except itself.

## Tracking findings

The sensor cards, placement image/guidance, optional sound/light controls, true Manual row, header More and full fixed Start action fit in the represented normal-text main states. The selected phone card acts as selection without opening sensitivity. More opens named settings; sensitivity has a complete native range above Use, and Sampling & battery retains its parent through recreation. The light-on draft survives child navigation and recreation; Manual chooser Back retains it. A separate sonar-selected intent fixture returns to phone in place. It does not prove contactless calibration.

The previous native failure was a helper treating a native SeekBar as an OnClick target. Its XML showed an enabled/focusable 48dp range with `clickable=false`. This passing method independently checks full range width/height, 48dp target, dock/footer clearance, native 0..2 range and ACTION_SET_PROGRESS, changes Medium to High beside the actual heading, then cancels and verifies no private setting was saved. The image is the initial Medium state. The earlier and current Tracking setup source preceding the live method is identical; the full TrackingActivity file includes later live-page work which this journey never executes.

**Visual issue left open:** first `tracking-main` renders notification and Wi-Fi status glyphs dark on plum, while the clock/cell/battery and later Tracking frames are light. Reported separately before mutation; this archive accepts neither first-frame system-bar fidelity nor a correction. No production code changed here. Reference differences also remain: a truthful Manual chooser replaces the board's segmented control; the illustration/art crop, shared glyphs, card/control proportions and extra privacy guidance differ. The supporting pages extend the board. This is configuration acceptance, not complete screen 16 or 36-screen acceptance.

## Originals inspected

Names below share the `organized-settings-` prefix.

| Original suffix | Inspected result |
| --- | --- |
| `tracking-main.png` | Phone selected, sound/light off, all essentials and full Start visible; initial dark system glyphs noted above. |
| `tracking-more.png` | Named start/schedule, Sensors & battery and recording tools; sensitivity/Medium and full Back action. |
| `tracking-sensitivity.png` | Medium native range and complete Use/Test footer; fake-port Active with an honestly empty signal and waiting message. |
| `tracking-power.png` | Battery-aware off, batching unsupported/disabled, reserve Off; full Done. No power setting was changed. |
| `tracking-power-recreated.png` | Same page, supported/unsupported states and complete Done after recreation. |
| `tracking-recreated.png` | Phone selected, light-on draft retained, sound off, Manual and full Start. |
| `tracking-automatic-manual.png` | Real Manual-only chooser selected with full Save; no Arm action in this mode. Back cancels. |
| `tracking-phone-selected.png` | Sonar-fixture to phone selection returns to the main hierarchy with light/sound off and full Start. |
| `sensitivity-categories.png` | Laughter off and five categories on; descriptions and full Save fit. |
| `sensitivity-duration.png` | Native duration dialog, default selected, three readable choices and complete Cancel. |
| `sensitivity-reopened.png` | Saved 5-of-6 category count and custom duration summary reopen; full Save. |
| `retention-details.png` | Recording/favorites/history consequences readable with full Apply action; deletion was not executed. |
| `snoring-options.png` | Actual output/About routes, both contactless switches off and descriptions clear of switches; complete Save. |
| `automatic-conditions.png` | Charging draft on, screen-off off, tonight-only limit and full Done. |
| `recurring-conditions.png` | Charging draft on; distinct required screen-off/stillness explanation and full Done. |
| `sounds-sources.png` | Real source routes, selected Rain and explicit Play guidance; full Back. CI-only label has low contrast. |
| `sounds-playback.png` | Playback/tracking choices off, separate media-fade consent route, full Back; CI-only label has low contrast. |
| `timer-recreated.png` | 45min draft remains selected after recreation; complete Use/Cancel. |
| `fade-reopened.png` | Saved 10min fade reopens selected with complete Use/Cancel. |

## Verified behavior and limits

The same method retains its 11 organized-settings checks: category/duration Save+reopen; retention draft without Apply; snoring and automatic/recurring draft recreation without activation; silent source/playback navigation; 45min timer draft recreation/Use and 10min fade Save/reopen. Original preference values and absent keys are restored after all owned windows and workers drain.

Tracking uses a private database, private motion/delay preferences, a MotionPort fake with no sensor registration or samples, and a null snapshot plus throwing Start/command/review ports. It verifies radio role/check state, no unexpected Activity on phone selection, light saved-instance draft persistence, cancelled sensitivity/automatic choices, real parent Back paths, complete main controls/Start bounds without scrolling to Start, and no created session/delay admission. Main tracking Start, Arm, capture, sampling behavior and stored recording results were not run; battery/batching/reserve changes were not exercised.

API 36 emulator, 1080x2316 override at 420dpi; host output disabled. Microphone permission grant/deny/permanent-denial, notifications, unsupported main accelerometer/light, contactless preflight/calibration, charging/audio-route errors, real sensor streams, audible/haptic output, hardware behavior, RTL/large text and personal-phone behavior are not established by this method. Sonar selection starts from an inert intent fixture; no Test/Use sonar or microphone action is pressed. No new build, CI, tests, device or output action was run to produce this archive.
