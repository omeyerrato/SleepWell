# Report data-state comparisons — run 41051564032

[Run 41051564032](https://github.com/omeyerrato/SleepWell/actions/runs/41051564032), source `ed6ccc14733c8357dc43b939149bc5bdb1b3fbe7`, narrow scope `report-rounding`: **PASS 89.758 seconds**, one existing native journey, **20 required originals**. All 20 were inspected individually against boards 01 and 08. This is a bounded screen/action result, not a global application result.

The `app/src/main` Git tree is `7763931143d8d8ac4400a9afa7443951a459fa52`, identical to the frozen 0.1.23 source `023c5fb7`. The increment adds isolated capture fixtures to the existing journey and changes no production UI or app features. It does not require a new application release.

All PNGs are unchanged 1080 × 2316 Android emulator originals. [Environment](environment.json), [focused summary](focused-review-summary.json), [instrumentation log](ReportReferenceFlowTest%23referenceHistoryTrendsEditAndNightReportPersistWithoutEffects.txt) and [SHA-256/source manifest](originals.json) retain exact provenance. Fixture labels remain visible; no screenshots were cropped, recolored or composited.

## Newly captured states

### Available inline recording

[Original inline-audio capture](report-reference-inline-audio.png) shows two populated sound rows with decoded waveform peaks, circular Play/Pause controls, event counts, row options and the View sound events action. The same measured movement chart and honest Not estimated stage state remain above them.

The isolated journey writes two synthetic PCM WAVs into uniquely named private clip files, decodes their actual peaks and uses existing real interval-admission logic. It taps the native Snoring preview and Pause controls through the existing fake playback port. The captured 0:02 / 0:09 progress is the fake session position applied to the real retained interval. It verifies both preview controls retain their full 48dp targets, drains owned readers/ports and removes only its private files after teardown. No MediaPlayer, recording microphone, sensor service, scheduler or physical output is used.

Compared with board 01, the main row structure and waveform/control alignment are present. **Snoring estimates and Speech estimates wrap to two lines**, unlike the mockup's shorter category labels; the estimate distinction is intentional and remains accessible. Retained-event duration/progress copy also makes the actual preview scope explicit. This is not a claim of an exact visual match or real sound classification/playback quality.

### Imported provider stage strip

[Original provider-stage capture](report-reference-provider-stages.png) shows the actual imported interval strip, axis labels, Awake/Light/Deep/REM legend, Source details action and explicit Reported by com.sleepwell.fixture provenance. The 7h42 duration excludes the fixture's 18 source-reported awake minutes.

The fixture enters the private UUID database through existing HealthSleepStore contracts and reopens the actual report. Source payload bytes and duration remain unchanged. No phone epochs or clips are attached to this imported record; no Health Connect API/service is invoked. The synthetic provider intervals exercise the existing strip rather than supplying production sample data.

The purple/gold strip and legend address the board 01 stage-state gap with clear provenance. This provider report does not fabricate a phone movement graph or unrelated audio rows. It is **not evidence of a phone Deep/REM estimator**, clinical stage validity, breathing/apnea analysis or real provider access.

## Existing 18-state comparison

- [History](report-reference-history.png): all seven records and their actual ribbons/values fit above the four-tab dock, reflecting the user's separate Alarms-tab override.
- [Week](report-reference-week.png), [selected week](report-reference-week-selected.png), [calendar](report-reference-week-calendar.png), [month](report-reference-month.png), [year](report-reference-year.png) and [selected year](report-reference-year-selected.png): readable axes/selection, true absent-data gaps and the complete Read details target remain present above navigation.
- [Night](report-reference-night.png) and [chart gap](report-reference-chart-gap.png): measured interval selection and no-readings selection stay distinct; no substitute stages or waveforms appear when absent.
- [Editor](report-reference-editor.png), [cancelled](report-reference-period-cancelled.png), [start drag](report-reference-period-drag-start.png), [end drag](report-reference-period-drag-end.png), [accessible selection](report-reference-period-accessibility.png), [draft recreation](report-reference-period-draft-recreated.png), [tags/notes](report-reference-tags-notes.png), [reopened](report-reference-reopened.png) and [persisted boundaries](report-reference-period-persisted.png): current boundary labels, awake annotation, tags, notes and Save/Delete hierarchy remain readable. Existing immutable-data, cancel, recreation and persistence assertions are retained.

## Remaining boundaries

The two fixture states are now captured and visually compared at this emulator geometry. Physical output behavior, real recordings, provider availability, every device/font scale and the wider 36-reference acceptance remain separate. Estimates-label wrapping is a recorded visual difference. Other uncaptured interactions and the separately documented SystemUI transition are not resolved by this run. No phone or real alarm/audio/flashlight outputs were exercised, and no new run or product edit was performed to create this archive.
