# Report spacing: native verification

[CI 40505624465](https://github.com/omeyerrato/SleepWell/actions/runs/40505624465)
**passed** at `f1f839ce5da8c40f095b435744607864c0b6d7f1`. The existing single
Report journey passed in **60.046 seconds**; the job, including both APK builds,
took **5m13s**. [The focused summary](focused-review-summary.json) requires eleven
original PNGs and records no failures. This rerun verifies the production Report
spacing change from `63843475`; it adds no test method or broader test campaign.

[The environment](environment.json) pins the disposable API 36 x86_64 emulator at
1080×2316, density 420, default font size, with host audio disabled, audio imports
disabled and no native playback allowlisted. No physical phone action was used.
The seven fixture nights are explicitly labelled synthetic on every frame; they
are neither personal recordings nor model-accuracy evidence.

All eleven originals were opened individually and compared with board 08 and the
Report frame on board 01:

| Original | Observed result |
| --- | --- |
| [History](report-reference-history.png) | Cream/brown hierarchy, selected record, measured lavender coverage, pause gap, time icons, aligned durations and four-destination dock remain coherent. |
| [Week](report-reference-week.png) | Average duration leads; duration bars, goal line, bedtime plot and Read details fit above the dock. Missing dated goal stays a dash. |
| [Calendar](report-reference-week-calendar.png) | The actual owned calendar shows October 25 selected and the matching pending date; full Use date and Cancel controls fit. |
| [Selected week](report-reference-week-selected.png) | A physical tap selects the actual fourth recorded day, with warm column and 7 h 10 min tooltip. The ordinary duplicate value is suppressed. |
| [Month](report-reference-month.png) | Only the seven fixture dates have columns/bedtime points; unknown dates remain blank. |
| [Year](report-reference-year.png) | Only October has recorded aggregate data; no invented annual history. |
| [Night](report-reference-night.png) | The shorter date and Recording start/end labels remain legible. Tighter gaps bring movement and sound events higher. The real selected interval and relative average remain visible; absent stages and saved audio are not invented. |
| [Chart gap](report-reference-chart-gap.png) | The known pause remains blank and its actual tap reports No readings. Compact spacing retains the empty sound-analysis explanation. |
| [Editor](report-reference-editor.png) | Recorded period, duration, Start/End, awake action, tags, Notes and the complete Save/Delete footer fit. |
| [Tags and notes](report-reference-tags-notes.png) | Two actual tag chips and the saved-note draft fit above the full footer. |
| [Reopened](report-reference-reopened.png) | The saved 10 minute awake exclusion changes 7 h 42 min to 7 h 32 min. Awake (1), both tags and the note survive reopening; the timeline reflects the exclusion. |

[The original instrumentation result](ReportReferenceFlowTest.txt) passes calendar
date change/reopen/restoration, real chart selection and gap inspection, report
options and sound-category filtering, edit save/reopen, and discard of a later
unsaved note. Complete 48 dp report actions, visible trend details and editor footer
remain asserted. Raw session/epoch/pause/event/clip rows and shared preferences
remain unchanged; only intended edit overlays persist. Owned screens and readers
finish before isolated database cleanup.

The previous attempt at `c04fa582` ([CI 40503787499](https://github.com/omeyerrato/SleepWell/actions/runs/40503787499))
stopped at test-APK compilation because CalendarRulesFlowTest lacked its
WindowInspector import. It produced no native frames. This corrected run follows
the shared build fix; no Report source change was made in response to that build
failure.

This closes the **bounded Report spacing regression check at the stated runtime**
and reconfirms calendar/edit behavior. It does **not** close full Report or all 36
reference-screen fidelity. Large text, long/many tags, extra awake/quantity sheets,
provider-stage frames, retained-audio playback and a new physical-phone install
remain outside this journey. Actual measurements, unknown dates and the later
four-tab navigation are preserved rather than copied from illustrative reference
values.
