# Report and Travel reference refinements

Actual native Android captures from30October2026, individually inspected and archived31October against [Everyday board01](../../../design-concepts/2026-10-22-phone-app/01-everyday.png), [History/editor board08](../../../design-concepts/2026-10-22-phone-app/08-history-and-trends.png) and [Travel board09](../../../design-concepts/2026-10-22-phone-app/09-rhythm-and-naps.png). **All31 originals were opened individually as complete windows.** These are unchanged emulator screenshots, not generated screen mockups or contact-sheet-only comparisons.

Both runs captured exact source `d3f7422dd498dcce869cd18c51530ee0e44b0c05`. Their environment files report API36,1080×2316 override geometry,420density and isolated emulator serial `emulator-5554`; host audio was disabled and both native-sound/import-output options were false.

| Flow / run | Existing selected method | Result | Original evidence |
|---|---|---|---|
| [Report41104642545](https://github.com/omeyerrato/SleepWell/actions/runs/41104642545) | `ReportReferenceFlowTest#referenceHistoryTrendsEditAndNightReportPersistWithoutEffects` | PASS57.891s, one method,20 required originals | [Summary](report-41104642545/focused-review-summary.json), [environment](report-41104642545/environment.json), [native log](report-41104642545/ReportReferenceFlowTest%23referenceHistoryTrendsEditAndNightReportPersistWithoutEffects.txt), [SHA/source manifest](report-41104642545/originals.json) |
| [Travel41104818623](https://github.com/omeyerrato/SleepWell/actions/runs/41104818623) | `TravelPlanFlowTest#savedZonesDstRemindersDeliveryCancelAndFailureAreRealExceptInjectedOutputs` | PASS45.164s, one method,11 required originals | [Summary](travel-41104818623/focused-review-summary.json), [environment](travel-41104818623/environment.json), [native log](travel-41104818623/TravelPlanFlowTest%23savedZonesDstRemindersDeliveryCancelAndFailureAreRealExceptInjectedOutputs.txt), [SHA/source manifest](travel-41104818623/originals.json) |

## Comparison and exercised behavior

**Report:** [populated inline audio](report-41104642545/report-reference-inline-audio.png) now shows Snoring and Speech on single lines, with truthful3 estimates/1 estimate captions underneath. This corrects the previous two-line category headings while retaining full native Play/Pause targets, genuine decoded PCM peaks, fake playback position and missing-audio state. User-labelled and mixed categories retain their distinct caption rules in source; this one captured populated fixture is all automatic estimates. Audio preview's full semantic labels are unchanged.

[History](report-41104642545/report-reference-history.png) retains seven visible nights, real contiguous coverage with an actual missing-data gap and right-aligned durations. [Week](report-41104642545/report-reference-week.png) keeps its actual mean, duration/bedtime charts and complete Read details above the dock; Month/Year keep missing dates unknown. The selected bar/tooltips remain readable. The [calendar](report-41104642545/report-reference-week-calendar.png), [editor](report-41104642545/report-reference-editor.png), [retained annotations](report-41104642545/report-reference-reopened.png) and [provider strip](report-41104642545/report-reference-provider-stages.png) remain clear. The existing method exercises actual calendar selection, trim handles, keyboard/accessibility boundary control, cancellation, recreation, Save/reopen, preserved original data, inline fake Play/Pause and imported-stage source persistence. Private SQLite and synthetic WAV fixtures never enter production user data.

**Travel:** [main plan](travel-41104818623/travel-plan.png) and [reopened plan](travel-41104818623/travel-reopened.png) show the London-only landmark, quiet plane/dotted connector, fuller native clock faces and compact difference/direction. Zone cards have softer borders. The diagram is closer to board09 without freezing its illustrated numbers: the actual fixture's current London offset isUTC+01:00 and the current difference is2h; its departure-day DST difference is3h, explicitly retained below. Civil dates, actual selected zones and real clock-policy values remain visible. Save fits fully. The later user-directed Trip plan entry and the separate Alarms tab are intentional organizational overrides of the original boards.

The existing Travel method exercises real zone/date/reminder drafts, recreation, Save/reopen, injected reminder delivery/rearm/cancel policy and explicit scheduling-error/retry. [Error](travel-41104818623/travel-error.png) shows readable failure/Retry/Save controls in the expanded fixed footer; part of the underlying During sleep tracking row continues in the scrollable body. This is a supporting error layout, not a claim that every body row is simultaneously visible. No real notification or flashlight was emitted.

These runs use private fixtures and injected playback/clock/reminder-output ports. They do not establish physical audio, actual OS notification delivery, jet-lag efficacy, real lamp/torch behavior, phone Deep/REM estimation, overnight accuracy, every font/device variant or complete all36 acceptance. Provider stages are explicitly source-reported imports; they are not a phone-stage algorithm. The current source work and these bounded results are separate from APK publication/phone installation. No new tests, CI dispatches, builds or device actions were performed while making this archive.

## Individually inspected Report originals —20

- [history](report-41104642545/report-reference-history.png): seven actual private nights and selected Report dock.
- [week](report-41104642545/report-reference-week.png): duration mean, real bars/bedtime line and complete Read details.
- [week-selected](report-41104642545/report-reference-week-selected.png): selected bar and readable tooltip.
- [week-calendar](report-41104642545/report-reference-week-calendar.png): actual selected date and full Use/Cancel.
- [month](report-41104642545/report-reference-month.png): actual seven-date cluster, missing dates blank.
- [year](report-41104642545/report-reference-year.png): one populated month, missing months blank.
- [year-selected](report-41104642545/report-reference-year-selected.png): actual monthly tooltip within chart bounds.
- [night](report-41104642545/report-reference-night.png): short sound titles/counts and honest analysis-only state.
- [chart-gap](report-41104642545/report-reference-chart-gap.png): selected missing interval says No readings.
- [editor](report-41104642545/report-reference-editor.png): actual handles, duration, time controls, Tags/Notes and actions.
- [period-drag-start](report-41104642545/report-reference-period-drag-start.png): moved start boundary and changed duration.
- [period-drag-end](report-41104642545/report-reference-period-drag-end.png): moved end boundary and changed duration.
- [period-accessibility](report-41104642545/report-reference-period-accessibility.png): owned boundary focus ring and true value.
- [period-draft-recreated](report-41104642545/report-reference-period-draft-recreated.png): restored unsaved boundaries.
- [period-cancelled](report-41104642545/report-reference-period-cancelled.png): cancellation restores boundaries and retains explicit awake edit.
- [period-persisted](report-41104642545/report-reference-period-persisted.png): saved trim reopens.
- [tags-notes](report-41104642545/report-reference-tags-notes.png): visible selected tags, saved note and main actions.
- [reopened](report-41104642545/report-reference-reopened.png): saved awake period/annotations and correct duration.
- [inline-audio](report-41104642545/report-reference-inline-audio.png): single-line categories, estimates and actual waveform/fake play progress.
- [provider-stages](report-41104642545/report-reference-provider-stages.png): source stage strip, legend and explicit fixture provenance.

## Individually inspected Travel originals —11

- [plan](travel-41104818623/travel-plan.png): native clocks, new landmark/connector, actual DST values and complete Save.
- [zones](travel-41104818623/travel-zones.png): actual London search and selected result.
- [date](travel-41104818623/travel-date.png): complete calendar and real departure-date choice.
- [schedules](travel-41104818623/travel-schedules.png): named reminder/sleep-plan routes and complete Done.
- [reminders](travel-41104818623/travel-reminders.png): actual enabled draft,time and days.
- [reminders-recreated](travel-41104818623/travel-reminders-recreated.png): retained unsaved reminder selection.
- [reopened](travel-41104818623/travel-reopened.png): saved main plan with reference connector.
- [reminders-reopened](travel-41104818623/travel-reminders-reopened.png): actual saved reminder and next occurrence.
- [error](travel-41104818623/travel-error.png): failure message and full Retry/Save footer.
- [disabled](travel-41104818623/travel-disabled.png): explicit Off draft and pending saved occurrence before Save.
- [disabled-main](travel-41104818623/travel-disabled-main.png): returned main plan before saving disabled reminders.
