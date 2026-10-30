# Current Report archive — run 41017261604

[GitHub Actions run](https://github.com/omeyerrato/SleepWell/actions/runs/41017261604), source `70bca1ecdf34fecb2a9bacded2d86e106ea434c8`, narrow scope `report-rounding`.

`ReportReferenceFlowTest#referenceHistoryTrendsEditAndNightReportPersistWithoutEffects` passed in **69.636 seconds**. All 18 required original Android screenshots were inspected against board 01's Report and board 08's History, Trends and Edit sleep. This archive replaces the parent note that the small `eeb0f88c` layout changes were still awaiting captures; no new execution was performed to create this archive.

The copied PNGs are unmodified 1080 × 2316 emulator originals. [Environment](environment.json), [focused summary](focused-review-summary.json), [instrumentation log](ReportReferenceFlowTest%23referenceHistoryTrendsEditAndNightReportPersistWithoutEffects.txt) and [SHA-256 provenance manifest](originals.json) identify the exact source/run and copied bytes. Visible fixture labels are retained; these records are private review fixtures rather than application seed content.

## Captured behavior and comparison

- [History](report-reference-history.png): seven actual fixture records, selected filter and period ribbons remain visible above the four-tab navigation. Navigation reflects the user's separate Alarms-tab override.
- [Week](report-reference-week.png), [selected day](report-reference-week-selected.png), [calendar](report-reference-week-calendar.png), [month](report-reference-month.png), [year](report-reference-year.png) and [selected year bin](report-reference-year-selected.png): real aggregate bars, readable axes, target line and Read details hierarchy; actual missing-data gaps remain blank.
- [Editor](report-reference-editor.png), [start drag](report-reference-period-drag-start.png), [end drag](report-reference-period-drag-end.png), [accessible boundary selection](report-reference-period-accessibility.png), [draft recreation](report-reference-period-draft-recreated.png), [cancelled draft](report-reference-period-cancelled.png), [persisted boundaries](report-reference-period-persisted.png), [tags/notes](report-reference-tags-notes.png) and [reopened record](report-reference-reopened.png): boundary editing, cancelling, draft restoration and Save/reopen preserve actual values and actions. The extra breathing space before Tags is present.
- [Night report](report-reference-night.png) and [selected missing bin](report-reference-chart-gap.png): duration, recorded interval, movement chart and report routes retain their primary hierarchy. Unknown data is not replaced with sample curves or invented stages.

The existing narrow journey exercised calendar navigation, chart selections, editing/trim interactions, cancel, recreation and persistence using the isolated emulator fixture setup. Its fit and persistence assertions passed. There were no phone actions, alarm delivery or real audio/hardware outputs in this flow.

## Open states

This establishes only the captured Report layouts and safe interactions at the recorded emulator geometry. **Inline saved-audio playback and provider-stage variants remain open**; the capture set does not establish those visual states or their real-device output behavior. It does not establish an on-phone sleep-stage estimator, clinical sleep stages, breathing/apnea analysis or overnight accuracy. A passing narrow run and these screenshots do not establish all 36 references, every font scale/device, or full application completion.
