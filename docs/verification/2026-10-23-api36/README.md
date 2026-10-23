# Native Android visual evidence — 2026-10-23

Code: [`ce7f64c8abba3ddc57b92d1d33f06388976b7ea7`](https://github.com/omeyerrato/SleepWell/commit/ce7f64c8abba3ddc57b92d1d33f06388976b7ea7).
[Successful CI run 39907851771](https://github.com/omeyerrato/SleepWell/actions/runs/39907851771).

These are original, unmodified PNGs from Android API36, x86_64 Google APIs,
1080×2316, density420, default font size, 12-hour clock. The emulator is a
disposable GitHub Actions fixture with host audio disabled. No physical phone,
personal database, recording or account data is included. See [environment](environment.json)
and [SHA256 manifest](sha256.json). The manifest covers captures, environment and
test transcripts; it does not cover this explanatory file.

`SoundSettingsFlowTest`: **1 pass,18.746 s**. Rain → Ocean, timer 45 min,
fade 10 min, recreation and reopen, with exact personal preference equality in
the test body and cleanup. No Play, import or tracking action in this configuration test. [Raw result](SoundSettingsFlowTest.txt).
CI-only setup asserts idle values and defines only absent ringing/tracking/
trackingPaused/session defaults before its baseline, because the fresh emulator
can deliver BOOT_COMPLETED late. Every key/value is then compared exactly in
body and cleanup; existing values are never overwritten. The phone runner does
not enable this bootstrap.

`RootVisualTest`: **1 pass,23.113 s**. Four root destinations and selected states,
read-only detail navigation, and platform Back. No alarm action. [Raw result](RootVisualTest.txt).

`NativeSoundFlowTest`: **1 pass,26.323 s**. Real MediaPlayer music activity,
advancing playing time, playback after Home, notification PendingIntent Pause/
Play/Stop, frozen pause timer, live Ocean replacement, competing focus pause
and no auto-resume, final three seconds of a one-minute timer using a 57-second
offset, unreadable provider failure and explicit Rain recovery. Android media
activity, foreground service and notification are absent after Stop/completion/
failure. Stream volume and personal preference values are unchanged.
[Raw result](NativeSoundFlowTest.txt). Only the disposable emulator receives
POST_NOTIFICATIONS before this test; root captures precede that grant.
No notification-shade touch, physical audio, alarm, microphone or tracking test.

References: [Everyday](../../../design-concepts/2026-10-22-phone-app/01-everyday.png),
[Alarms](../../../design-concepts/2026-10-22-phone-app/02-alarms.png),
[Wind-down](../../../design-concepts/2026-10-22-phone-app/05-bedtime.png),
[Sleep sounds](../../../design-concepts/2026-10-22-phone-app/07-sleep-audio.png),
[Settings](../../../design-concepts/2026-10-22-phone-app/11-settings-and-integrations.png),
[Privacy](../../../design-concepts/2026-10-22-phone-app/12-privacy-and-appearance.png).

| Actual Android capture | Reference / bounded comparison |
| --- | --- |
| [Tonight](root-tonight.png) | Board01: shared-path arc, four-item dock alignment and continuous plum system bars. Four tabs are the user's explicit override. No enabled alarm in this fixture. |
| [Alarms](root-alarms.png) | Board02: dedicated root, compact entries and anchored Nap/Add actions. Default disabled fixture; alarm behavior untested. |
| [Report](root-report.png) | Board01: empty-history supporting state. No invented overnight chart or stage strip. |
| [Settings](root-settings.png) | Board11: consistent icons/row alignment, small GitHub footer, selected dock. Sleep sounds is an additional working destination. |
| [Alarm readiness](root-readiness.png) | Board12: hierarchy and native allowed/attention/optional states. Permission status reflects this emulator. |
| [Sound analysis](root-sound-analysis.png) | Supporting settings and actual disabled/permission states; analysis and microphone untested. |
| [Bedtime guard](root-bedtime-guard.png) | Board05: dark editor, schedule, calls card and fixed actions. Accessibility is absent; no enforcement test. |
| [Data & privacy](root-privacy.png) | Board12: corrected summary heading and empty storage state. Export/restore actions untested by this read-only run. |
| [Wind-down](root-wind-down.png) | Board05: handless bedside scene bleeds to both display edges, plum top blend, centered button icons and dividers only between rows. White status icons and gesture handle remain readable on plum after three Tonight↔Wind-down transitions. |
| [Rain configuration](sounds-settings-rain.png) | Board07/screen 19: dark detail hierarchy, window/lamp hero, thick rounded native tracks and white thumbs, Timer/Fade and four illustrated choices. Stopped state is intentional. |
| [Ocean configuration](sounds-settings-ocean.png) | Correct Ocean outline,45 min and10 min settings. |
| [Recreated](sounds-settings-recreated.png) | Ocean and both durations survive Activity recreation. |
| [Reopened](sounds-settings-reopened.png) | Ocean and both durations survive closing and reopening. |
| [Playing](sounds-native-playing.png) | Board07: Pause and actual elapsed playback time; test gains 5% on each source. |
| [Paused](sounds-native-paused.png) | Ocean selection, Play control and frozen timer/paused copy. |
| [Unreadable file](sounds-native-error.png) | Supporting state: one error beside Play, usable library choices; neutral imported-audio hero remains a visual follow-up. |
| [Recovered](sounds-native-recovered.png) | Rain selected, error cleared, explicit Play shows Pause/playing state. |

The independent strict reviewer inspected all 17 captures and the supplied boards.
**All 17 captured compositions pass the independent bounded visual review**,
recorded in DESIGN-REVIEW.md. This does **not** certify every
screen, large fonts/RTL, a physical S23 installation, full native lifecycle, import,
long background/screen-off behavior, route changes, tracking ownership, full-minute
fade or listening quality. Full 36-screen/feature completion
remains **rejected**. [Feature matrix](../../FEATURE-MATRIX.md) preserves the gaps.

All three allowlisted tests pass at this commit. Earlier failed launch-helper
and fresh-boot fixture attempts remain described in VERIFICATION.md; they are
not accepted as overall green runs. A short native journey is not full feature
or 36-screen completion.
