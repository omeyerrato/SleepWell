# Report calendar and editing: bounded native verification

[CI 40477887525](https://github.com/omeyerrato/SleepWell/actions/runs/40477887525)
**passed** the single existing Report journey in **64.406 seconds** at
`514a58000f8e47a07bbab4645de886daaf0982d5`. The CI job, including both APK builds,
took 5m40s. Its focused summary requires all **11 original Android PNGs** and
reports no failures. The source and runtime are pinned; no additional test method,
unit batch, physical-phone action, microphone, alarm, torch, or playback was used.

This is the disposable API36 x86_64 emulator at 1080x2316, 420dpi and default font
scale, with host audio disabled and audio imports/native sound outputs disabled.
Startup requires three actual MainActivity focus samples. Each capture requires a
focused, attached, laid-out hardware-rendered window and a real frame commit.
The calendar capture checks its actual owned Dialog, selected date and pending
label; it does not require the obscured Activity to own focus.

The seven isolated SQLite nights and their sensor/audio-event rows are explicit
fixtures, labelled on every frame. They are neither personal recordings nor
sleep-stage or model-accuracy evidence. All eleven originals below were opened
individually and compared with board08 and the Report frame on board01.

| Original | Visual comparison and actual action evidence |
| --- | --- |
| [History](report-reference-history.png) | Cream/brown Inter hierarchy, aligned rows, lavender measured-coverage ribbons and selected Report dock remain. The actual paused night retains its blank ribbon interval. Four destinations preserve the later dedicated Alarms request. |
| [Week](report-reference-week.png) | Header calendar opens dates. The small noninteractive range caption removes the duplicated date action and gives the average and charts more space. Lavender columns, quiet grid, bedtime points and Read details clear the dock. No dated goal exists, so goal gap stays a dash. |
| [Calendar](report-reference-week-calendar.png) | Actual owned Trend date dialog: October25 is selected and the pending date agrees. Full Use date and Cancel controls fit. Use changes the plotted range, preserves the newly exposed unknown date, and reopening retains the chosen date; restoring October26 restores the original range. |
| [Selected week](report-reference-week-selected.png) | The real fourth duration column is selected by a physical tap. Its warm fill and brown7h10 tooltip are clear; its ordinary duplicate label is suppressed. The exact selected date and recorded duration are asserted. |
| [Month](report-reference-month.png) | Month selection is visible. Only the seven actual fixture dates have columns/bedtime points; missing days remain blank. Read details fits above the dock. |
| [Year](report-reference-year.png) | Year selection is visible. Only October has measured aggregate data; other months remain blank. No fabricated trend fills the year. |
| [Night](report-reference-night.png) | The selected actual movement interval and numerical relative average are visible. Rounded column tops meet the baseline; ticks and quiet grid remain legible. Absent stages and retained audio are not invented. |
| [Chart gap](report-reference-chart-gap.png) | Physical tap in the known120–130minute pause shows No readings and leaves the gap blank, rather than choosing a distant measured bar. |
| [Editor](report-reference-editor.png) | Header/date, true recorded period, duration, Start/End, awake action, tags, Notes and full Save/Delete controls fit together at default size. |
| [Tags and notes](report-reference-tags-notes.png) | Actual Late meal/Exercise chips, Add tag, saved note draft and complete footer fit without covering the header. Supporting tag management remains in Details. |
| [Reopened editor](report-reference-reopened.png) | Saving a real10minute awake exclusion changes7h42 to7h32. Both tags and the note persist after reopening; Awake(1) exposes the saved period. |

The journey also physically opens Trend options and Sleep rhythm, returns to
Read details, exercises report options and sound-event category filtering, then
saves/reopens the edit and discards a subsequent unsaved note through Back.
Original session, epoch, pause, sound-event, clip rows and shared preferences are
asserted unchanged; only intended edit overlays persist. Actual target-size and
full-visible-footer assertions remain. Cleanup requires all owned screens and
reader executors to finish before deleting only the isolated database.

The observed teardown race is fixed in History and RecordReport reader drains:
a shutdown admission race or removed queued barrier is accepted only after actual
executor termination within the original monotonic timeout budget. The calendar
and selector checks await actual rendered values instead of immediate stale
accessibility snapshots. Ordinary Activity capture checks are unchanged.

This closes **calendar/edit verification at the stated runtime and default size**.
It does **not** close all36screens or full Report design parity. Large text,
long/many tags, additional awake/quantity sheets, provider-stage frames and
retained-audio playback are outside this journey. The fixture has no stage
estimates or audio files, so its Report intentionally differs from the reference
stage strip and playable waveforms. Actual movement stays unsmoothed and dates,
labels, unknown values and the later four-tab dock follow real state.
