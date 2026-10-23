# Phosphor icon verification — 2026-10-23

Code: [`efa9cae98afa9ceb9a13396a35e0012a25686484`](https://github.com/omeyerrato/SleepWell/commit/efa9cae98afa9ceb9a13396a35e0012a25686484).
[Silent navigation/configuration CI](https://github.com/omeyerrato/SleepWell/actions/runs/39928589826)
and [normal Android checks](https://github.com/omeyerrato/SleepWell/actions/runs/39928587756) passed.

These 13 PNGs are original Android API36 x86_64 Google APIs captures at
1080×2316/density420, default font and 12-hour clock. Host output is disabled.
[Environment](environment.json) and [SHA256 manifest](sha256.json) preserve all
16 original files: 13 images, two test transcripts and environment. Pixels are
unchanged. Only disposable emulator fixtures are public; personal phone images,
backups, database rows, recordings and preferences remain private.

| Test | Result and scope |
| --- | --- |
| [RootVisualTest](RootVisualTest.txt) | 1 pass, 25.796 s. Four tabs, unique detail destinations, system Back, three Wind-down transitions, matching Window background/system-bar appearance and committed hardware frames. No alarm action. |
| [SoundSettingsFlowTest](SoundSettingsFlowTest.txt) | 1 pass, 23.383 s. Empty profile opens without writing defaults/errors; Rain→Ocean, 45-minute timer, 10-minute fade, portrait Activity recreation/reopen, exact personal preference equality. No Play or import. |

| Original image | Reference and state |
| --- | --- |
| [Tonight](root-tonight.png) | Board01: plan, arc, linked rows, illustration and four-tab selected dock. Four tabs are the user's explicit override. |
| [Alarms](root-alarms.png) | Board02: real alarm list and conventional three-dot More control. |
| [Report](root-report.png) | Board01 supporting empty state; no invented overnight measurements or stage strip. |
| [Settings](root-settings.png) | Board11: distinct bell, moon, speaker, waveform, shield, palette, gear, lock and GitHub logo. |
| [Readiness](root-readiness.png) | Board12: actual permission and battery states. |
| [Sound analysis](root-sound-analysis.png) | Supporting analysis settings; no classification result claimed. |
| [Guard](root-bedtime-guard.png) | Board05: actual app choices and plum navigation. |
| [Privacy](root-privacy.png) | Board12: actual local-storage summary and data controls. |
| [Wind-down](root-wind-down.png) | Board05: full-bleed scene, centered row glyphs and light system bars on plum. |
| [Rain](sounds-settings-rain.png), [Ocean](sounds-settings-ocean.png), [Recreated](sounds-settings-recreated.png), [Reopened](sounds-settings-reopened.png) | Board07: native play/control glyphs, sliders, timer/fade choices and selected library tile. Stopped throughout this run. |

The prior [five-flow run at cdf17d5](https://github.com/omeyerrato/SleepWell/actions/runs/39927399518)
passed SoundSettingsFlowTest (18.146 s), RootVisualTest (21.685 s),
NativeSoundFlowTest (25.664 s), SoundImportPickerTest (108.542 s) and
SoundImportReopenTest (13.883 s). Its 26 actual images included the new icons.
The filled More glyph was visually rejected despite green tests: upstream Fill
uses a capsule with three holes. This final commit replaces it with unchanged
Phosphor Regular three-dot geometry and reruns the relevant navigation/settings
flows. The three native/import tests were not rerun at this final icon-only head.
Earlier behavior and all 26 corresponding originals are also preserved in the
[audio-import evidence](../2026-10-23-audio-import/README.md).

The 51 selected SVGs are pinned and SHA256-locked; exact paths/winding are
converted to native vectors. Full upstream MIT license is vendored and packaged.
No downloaded executable/package, web font or runtime icon network request is
introduced. See [notices](../../../THIRD-PARTY-NOTICES.md).

Independent visual acceptance is recorded in DESIGN-REVIEW.md. This evidence
does not certify all 36 screens, missing features, landscape/RTL/large fonts,
physical playback/import, listening quality, long tracking, sonar, integrations,
or alarm delivery. Full app completion remains rejected.
