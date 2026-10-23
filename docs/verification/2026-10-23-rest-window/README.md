# Rest-window clock evidence — 2026-10-23

Code [`cfb5e68e24cdfcaa9f335107a2392987399d54fc`](https://github.com/omeyerrato/SleepWell/commit/cfb5e68e24cdfcaa9f335107a2392987399d54fc).
[Actual silent CI run](https://github.com/omeyerrato/SleepWell/actions/runs/39940420018).
[Normal Android CI](https://github.com/omeyerrato/SleepWell/actions/runs/39940417536)
also passes at that exact code head.
The 22 PNGs are unchanged Android API36 x86_64 Google APIs captures,
1080×2316/density420, default OS font and 12-hour clock. Host audio is disabled.
[Environment](environment.json) and [SHA256 manifest](sha256.json) cover all
26 original files: 22 PNGs, three raw transcripts and environment. Phone images,
preferences, database rows and backups remain private and are not published.

| Test | Actual result and scope |
| --- | --- |
| [SoundSettingsFlowTest](SoundSettingsFlowTest.txt) | 1 pass, 21.212 s. No Play/import; choices, recreation/reopen and exact personal preferences. |
| [RootVisualTest](RootVisualTest.txt) | 1 pass, 23.385 s. Four root tabs, distinct details, repeated Wind-down, system bars, committed frames; no alarm action. |
| [NightArcFlowTest](NightArcFlowTest.txt) | 1 pass, 38.907 s. Isolated plan/database and injected display clock, real 30-second foreground Handler in manual Midnight theme, retained View identity, 0/50/100% boundaries, next plan, actual explanation open/close, app 1.3x text and dated weekly occurrence. All personal preferences restored exactly, saved plan unchanged. No scheduling, audio, tracking or OS clock change. |

The weekly enabled alarm is stored only in an isolated database for display. It
is never submitted to AlarmManager. The clock seam affects Tonight's display
only and is restored after owned Activities close. Screens show synthetic
fixtures at declared instants, not an overnight recording or real phone night.

| Actual image | Comparison with board01 / verified state |
| --- | --- |
| [Upcoming](arc-upcoming.png) | Neutral curve and bedtime countdown; no fabricated 80% fill. |
| [Start](arc-start.png) | At bedtime: zero fill, Now above the unchanged moon. |
| [Middle](arc-middle.png) | Half of the same path, ring on the free curve, Now and four hours remaining. |
| [End](arc-end.png) | Full curve at the exact wake instant, unchanged sunrise icon and Now above it. |
| [Next](arc-next.png) | After wake: next plan, neutral curve and honest countdown. |
| [Explanation](arc-help.png) | Rounded themed Inter dialog, clear clock-vs-sleep meaning, working close control and light system bars on plum. |
| [Large text](arc-large.png) | 1.3x app text: arc/marker/duration/endpoints remain readable; this viewport requires scrolling to the primary action. |
| [Large action](arc-large-action.png) | Actual scrolled view; Start sleep tracking is at least 48dp visible and above the dock. It is not activated. |
| [Weekly](arc-weekly.png) | Sunday date in the header, eight-hour Saturday-night window, countdown rather than multi-day progress. |
| [Tonight](root-tonight.png), [Alarms](root-alarms.png), [Report](root-report.png), [Settings](root-settings.png) | Root selected states and honest empty Report; four tabs follow the user's override. |
| [Readiness](root-readiness.png), [Analysis](root-sound-analysis.png), [Guard](root-bedtime-guard.png), [Privacy](root-privacy.png), [Wind-down](root-wind-down.png) | Distinct destinations, real fixture permission/storage states and continuous dark-page system bars. |
| [Rain](sounds-settings-rain.png), [Ocean](sounds-settings-ocean.png), [Recreated](sounds-settings-recreated.png), [Reopened](sounds-settings-reopened.png) | Stopped configuration, native controls and persisted isolated choices. |

Ten pure NightWindowTest cases cover overnight/daytime windows, before/after,
weekly occurrence, exact inclusive endpoint, spring/fall elapsed duration and
collapsed/inverted DST gaps. Total local tests: 115 passes, zero failures/errors/
skips. Lint: zero errors, 68 existing warnings. Debug, test and release builds
pass. The first native run failed at a case-sensitive platform dialog-button
selector before progression; it certified only its two early captures. A later
21-image run passed all tests but was visually rejected because endpoint rings
overpainted the moon/sun. The final clearance check removes the ring near either
disc, retaining Now and the free-curve marker. These are corrected actual
captures, not edited pixels or acceptance inferred from compilation. A further
22-image candidate passed tests but was rejected for black Rain system glyphs
on plum. Focused dark detail windows now reapply appearance on the next post
and after 200 ms, guarded against lost focus/destruction. Configuration captures
require stable requested appearance and a committed frame, then read the
original PNG inset regions for both light glyphs and a predominantly plum
background. No test setter, image editing or older-image substitution is used.

Separate physical S23 RootVisualTest passes in 19.215 s with matching production source,
and SoundSettingsFlowTest passes in 10.929 s with four stopped captures and actual light-on-plum pixel assertions. All 14 images are private, with exact before/after personal preference/database
row preservation. Only the AndroidX compilation marker changes among existing
private files. SleepWell is left open with scoped keep-awake; no global timeout,
power setting, phone audio, alarm, microphone, vibration or tracking test.
Active-clock/weekly/large states above are emulator-only evidence.

Independent visual verdict is recorded in DESIGN-REVIEW.md. Dynamic system
time/zone broadcasts, broad locale/RTL/landscape/OS font scales and a real
overnight session are not certified here. This passes only a bounded clock
component after review; full 36-screen/feature parity remains rejected.
