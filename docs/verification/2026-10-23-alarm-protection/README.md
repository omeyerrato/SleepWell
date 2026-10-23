# Alarm protection configuration evidence — 2026-10-23

Source [`ab01ba59337e1e767b573a2e61ad40037eb6ed3c`](https://github.com/omeyerrato/SleepWell/commit/ab01ba59337e1e767b573a2e61ad40037eb6ed3c).
[Silent CI39954661465](https://github.com/omeyerrato/SleepWell/actions/runs/39954661465)
and [normal Android CI39954614529](https://github.com/omeyerrato/SleepWell/actions/runs/39954614529) pass at that head.
All 30 PNGs are unchanged Android API36 x86_64 Google APIs captures at
1080×2316/density420. The [environment](environment.json) declares disabled
host output and no playback allowlist. [SHA256 manifest](sha256.json) covers
35 originals: 30 images, four raw transcripts and environment. No private phone
captures, preferences, database rows or backups are published.

| Test | Actual result and scope |
| --- | --- |
| [AlarmProtectionFlowTest](AlarmProtectionFlowTest.txt) | 1 pass, 15.794 s. Isolated configuration/display database, independent global switches, both editor/global links, disclosure Later and Activity recreation. Unchanged personal preferences and fixture alarm rows; checked 48dp Alarm on visible above Save. |
| [RootVisualTest](RootVisualTest.txt) | 1 pass, 23.342 s. Four selected roots, distinct supporting pages, repeated Wind-down and system bars. No alarm action. |
| [NightArcFlowTest](NightArcFlowTest.txt) | 1 pass, 38.697 s. Retained clock-window view, real 30-second foreground tick with injected display clock, 0/50/100/next/weekly/help and 1.3x text. No scheduling or OS clock changes. |
| [SoundSettingsFlowTest](SoundSettingsFlowTest.txt) | 1 pass, 21.255 s. Stopped Rain/Ocean configuration, recreation/reopen and light-on-plum system-bar pixels. No Play/import. |

| New original image | Compared state |
| --- | --- |
| [Compact list](protection-alarms.png) | Board02 cream/Inter time, switch and name/days. Repeated settings removed; four-tab dock follows the user's later override. |
| [Editor](protection-editor.png) | Board02 hierarchy/day selection/settings. Alarm on and Save remain visible in the fixed footer. Protection/delete scroll; partial Snooze row at viewport edge. Spinner arrows/full editor parity are not certified. |
| [Off](protection-off.png) | Independent optional switches, access prerequisite and limits, board02/12 cream/brown family. |
| [Disclosure](protection-disclosure.png) | Rounded cream/Inter dialog with contained text and visible explicit choices; no automatic access enablement. |
| [On](protection-on.png), [Recreated](protection-recreated.png) | Both saved states survive recreation without repeated disclosure. Access remains absent, so these images do not imply enforcement. |
| [Global](protection-global.png), [Power only](protection-power-only.png) | Same global settings through Alarm options; turning app-return off leaves power protection on. |

The remaining 22 originals repeat prior root/rest-window/stopped-sound gates at
this exact head, with no older Rain substitution. The strict reviewer opened
all 30 individually: bounded configuration/model/UI PASS, no new regression.

Enabled alarm rows are **display fixtures only** in a UUID-named database. No
Save/enable action, Scheduler call, AlarmActivity/Service, access enablement or
power menu occurs. The test requires qemu plus explicit ciReview and restores
factories in finally. Eleven pure PowerMenuGuard tests use fake callbacks for
known/unknown/emergency classes, optional IDs, liveness, timing, cancellation
and failed BACK. Total local: 126 passes, zero failures/errors/skips. Lint: zero
errors / 68 existing warnings. Debug/test/release builds pass. AOSP source-ID
stripping was corrected before this final run; event fencing cannot prove
current input focus. See [scope and limits](../../ALARM-PROTECTION.md).

Separate physical S23 RootVisualTest: 1 pass/19.215 s, ten private captures on
the same source. Immediate preinstall/postrun archives confirm exact personal
preferences/all database rows unchanged and SQLite integrity OK. All existing
private files are identical except AndroidX profileInstalled. Main is left open
outside instrumentation with scoped keep-awake; no global timeout/nav/power
changes, alarm, playback, microphone, vibration, flashlight or tracking test.

Actual Samsung menu recognition/BACK delivery and app-return enforcement remain
**unverified** under the user's sleep-time testing restriction. Forced hardware
restart, disabled access, unknown OEM menus and powered-off delivery remain
limitations. Full 36-screen and feature completion remains **FAIL / incomplete**.
