# Native audio import evidence — 2026-10-23

Code: [`114c25e2cf2e199da26347729f3d853e68567a0f`](https://github.com/omeyerrato/SleepWell/commit/114c25e2cf2e199da26347729f3d853e68567a0f).
[Successful five-flow CI run39919149440](https://github.com/omeyerrato/SleepWell/actions/runs/39919149440).
[Normal unit/lint/build run39919099010](https://github.com/omeyerrato/SleepWell/actions/runs/39919099010) also passed.

These26 PNGs are original Android API36 x86_64 Google APIs captures,
1080×2316/density420, default font,12-hour clock, disposable emulator5554.
Host audio output is disabled. [Environment](environment.json) records the actual
process restart, not just a planned action. [SHA256 manifest](sha256.json) covers
all32 original files:26 PNGs, five raw test transcripts and environment.
No image pixels were edited. No personal S23 screenshot, database, URI, account,
recording, notification or backup is published here.

| Test | Actual result / exact scope |
| --- | --- |
| [SoundSettingsFlowTest](SoundSettingsFlowTest.txt) | 1pass,16.203s. Rain→Ocean,45-minute timer,10-minute fade, Activity recreation/reopen and exact personal preference preservation; no Play/import. |
| [RootVisualTest](RootVisualTest.txt) | 1pass,22.135s. Four root tabs, unique detail destinations, hardware frame captures, three Wind-down transitions/white system bars and platform Back. No alarm action. |
| [NativeSoundFlowTest](NativeSoundFlowTest.txt) | 1pass,24.718s. Actual MediaPlayer/music activity, Home, notification PendingIntent Pause/Play/Stop, frozen pause time, live source replacement, competing focus pause/no auto-resume, final3seconds of a valid1minute timer using57-second offset, unreadable-source error and explicit Rain recovery. No listening/full fade certification. |
| [SoundImportPickerTest](SoundImportPickerTest.txt) | 1pass,106.078s. Actual DocumentsUI picker/cancel. Invalid audio, declared oversized, unknown-size actual100MiB+1 and denied-read documents preserve the entire mix and take no persistent grant. Delayed pipe exercises Cancel, newer-choice fencing and retained validation through portrait Activity recreate(). Unknown-size valid WAV and normal WAV save URI/title plus READ-only persisted access. Reopen stays silent. All4 built-in tiles stay unselected for My audio. Lower-controls capture requires a real visible44dp row, saved title, position above navigation and a committed hardware frame. Seeded abandoned private import snapshot is removed on next validation. |
| [SoundImportReopenTest](SoundImportReopenTest.txt) | 1pass,14.385s. Harness force-stops only this disposable app between tests. New process reopens the saved external URI and persisted READ grant silently, then explicit Play creates actual native music activity. Pause/Stop, Remove from mix and reopen return Rain without modifying/deleting original WAV bytes. App-wide grants remain until the test explicitly releases its own synthetic provider grants. |

The read-only provider is in the test APK only, fixed synthetic IDs/cache files,
protected by system MANAGE_DOCUMENTS with selected-document URI grants. It
advertises roots only on this emulator model/hardware and is absent from app
APK dex. No production storage permission or provider was added. Tests use UUID
preferences, exact personal preference/stream-volume equality, main-thread
playback observations and drained Activity/import cleanup. They are absent from
the physical-phone allowlist. System broadcast idle timed out; the existing
CI-only absent-idle-default bootstrap and exact preservation assertions remain
in force, as described in the [prior evidence](../2026-10-23-api36/README.md).

References: [board07/screen19](../../../design-concepts/2026-10-22-phone-app/07-sleep-audio.png),
[Everyday](../../../design-concepts/2026-10-22-phone-app/01-everyday.png),
[Alarms](../../../design-concepts/2026-10-22-phone-app/02-alarms.png),
[Bedtime](../../../design-concepts/2026-10-22-phone-app/05-bedtime.png),
[Settings](../../../design-concepts/2026-10-22-phone-app/11-settings-and-integrations.png),
[Privacy](../../../design-concepts/2026-10-22-phone-app/12-privacy-and-appearance.png).

| Actual image(s) | Comparison / captured state |
| --- | --- |
| [Checking](sounds-import-checking.png) | Preserves Ocean while validating; real byte count and48dp Cancel beside Play. White bars after DocumentsUI. |
| [Rejected](sounds-import-rejected.png) | One actionable error beside Play; previous Ocean and gains remain. |
| [Recreated](sounds-import-rotated.png) | Retained delayed import completes across portrait Activity recreation; this filename does not mean landscape/orientation coverage. Neutral hero and no built-in selection. |
| [Unknown size](sounds-import-unknown-size.png) | Valid unknown-size document accepted after actual bounded read/metadata validation. |
| [Selected](sounds-import-selected.png) | Quiet sea.wav selected, neutral hero, practical availability copy, no built-in outline; stopped. |
| [Lower controls](sounds-import-controls.png) | Actual scrolled view: My audio plus saved filename, off tracking-playback switch and timer/media-volume explanation. |
| [New process](sounds-import-reopened.png) | Saved document after process restart; stopped until Play. |
| [Playing](sounds-import-playing.png) | Actual imported playback shows Pause and advancing time. |
| [Removed](sounds-import-removed.png) | Rain restored after removing URI from mix; original document untouched. |
| [Rain](sounds-settings-rain.png), [Ocean](sounds-settings-ocean.png), [Recreated](sounds-settings-recreated.png), [Reopened](sounds-settings-reopened.png) | Existing board07 hierarchy, rounded slider tracks/white thumbs and real persisted timer/fade choices. |
| [Native playing](sounds-native-playing.png), [Paused](sounds-native-paused.png), [Error](sounds-native-error.png), [Recovered](sounds-native-recovered.png) | Native short-flow states; unreadable My audio now has the neutral hero. |
| [Tonight](root-tonight.png), [Alarms](root-alarms.png), [Report](root-report.png), [Settings](root-settings.png) | Existing four-tab selected states. Empty Report stays honest; no invented overnight curve. Four tabs are the user's explicit override. |
| [Readiness](root-readiness.png), [Sound analysis](root-sound-analysis.png), [Guard](root-bedtime-guard.png), [Privacy](root-privacy.png), [Wind-down](root-wind-down.png) | Correct unique detail destinations; white Wind-down bars and full-bleed scene. Permission/empty states reflect this fixture. |

The independent reviewer inspected all 26 originals and the five raw results:
bounded PASS for the captured flow, as recorded in DESIGN-REVIEW.md.
The previous863e206 run passed all five tests, but its lower-controls PNG
repeated the top view. That image was rejected; it is not used here. This run
requires actual visible bounds and frame commitment before the lower capture.

Separate physical S23 configuration/navigation evidence is private and described
in VERIFICATION.md. No physical playback/import/alarm/microphone/tracking test
was performed. Open: listening/loop quality, full native timer/fade, long/background/
screen-off and pending-import process death, paused20-second provider timeout,
missing persistable grants, disk-write failure reporting, route changes/ducking,
actual tracking ownership, large text/RTL/landscape and remaining phone features.
The current preference writer uses atomic apply; normal restart success does
not prove disk-failure handling. Provider contents can change after validation.
Playlists/guided audio/binaural output/sleep-detected fade/external music remain
missing. Full36-screen/feature completion is still rejected.
