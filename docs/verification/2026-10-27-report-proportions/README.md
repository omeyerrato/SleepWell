# Report proportions: bounded native verification

The single Report journey **passed in 55.975 seconds** at
`2552adfda167764f1ab0b2e1ff133190216d8ac1` in
[CI 40538711852](https://github.com/omeyerrato/SleepWell/actions/runs/40538711852).
**The overall CI job failed.** Its other two journeys, Advanced Snooze and QR
configuration, failed separately. The unmodified [focused summary](focused-review-summary.json)
records those failures and missing images; this archive does not reclassify the
whole run as passing. It contains all eleven required original Report frames and
the successful [Report instrumentation result](ReportReferenceFlowTest.txt).

This verifies the presentation changes from `ef64a68d`: right-aligned History
durations with two-digit minutes, aligned editor time controls and the Notes
pencil, and a measured Trends bedtime-axis label lane. No Report test or production
source was changed for this evidence archive.

[The original environment](environment.json) identifies the API 36 x86_64 disposable
emulator, 1080×2316 override, density 420, host output disabled, no native playback
allowlist, and audio imports disabled. The captured app font size is default.
Every frame visibly labels its seven isolated fixture nights; these are synthetic
test records, not personal recordings or sensor-accuracy evidence. No physical
phone or output action was used for this review.

All eleven PNGs were opened individually and compared with board 08 and the middle
Report frame of board 01 in `design-concepts/2026-10-22-phone-app/`:

| Original | Observed result |
| --- | --- |
| [History](report-reference-history.png) | Duration values share the same right edge; `8 h 05 min` and `8 h 03 min` retain the reference's two-digit minute presentation. Seven rows, selected cream surface, lavender coverage, real pause gap, time icons, and dock fit without overlap. |
| [Week](report-reference-week.png) | The bedtime chart's measured label lane gives the plot more width while all four clock labels remain clear. Average duration leads, duration bars and goal guide remain intact, and the full Read details action fits above the dock. |
| [Calendar](report-reference-week-calendar.png) | October 25 is actually selected, matching the pending date. The complete Use date and Cancel controls remain visible. |
| [Selected week](report-reference-week-selected.png) | The physically selected fourth day shows its real `7 h 10 min` tooltip and warm column; the duplicate column label is suppressed. The bedtime plot remains unchanged by that selection. |
| [Month](report-reference-month.png) | Only seven fixture dates have duration columns and bedtime points. The narrower label lane stays legible; other dates remain blank. |
| [Year](report-reference-year.png) | Only October has an aggregate bar and bedtime point. Axis labels and Read details remain clear without inventing annual data. |
| [Night](report-reference-night.png) | Recorded-time hierarchy, start/end controls, measured movement selection and sound counts fit. Sleep stages remain Not estimated, with no saved-audio play control invented. |
| [Chart gap](report-reference-chart-gap.png) | The actual paused interval stays blank and a physical tap reports No readings. Empty sound-history copy remains legible. |
| [Editor](report-reference-editor.png) | Moon/sun icons are centered beside the complete Start/End label-and-time stacks, with separate aligned carets. The Notes pencil is visible. Period chart, duration, awake action, tags, Notes and the whole Save/Delete footer fit. |
| [Tags and notes](report-reference-tags-notes.png) | The same control alignment and pencil persist with two tag chips and an actual note draft; Notes stays above the complete footer. |
| [Reopened](report-reference-reopened.png) | The saved ten-minute awake exclusion changes `7 h 42 min` to `7 h 32 min`; Awake (1), both tags and the note survive reopening. The timeline reflects the exclusion and the edited controls retain their alignment. |

No new clipping, overlap, or interaction regression from these three presentation
changes was observed in the eleven frames at this runtime. The unchanged single
journey also passes actual calendar range change/restoration, chart selection and
gap inspection, Trend options/Sleep rhythm navigation, Report options, sound
category filtering, awake edit/save/reopen, and discard of a later unsaved note.
Its assertions retain complete 48dp Report actions, visible Trend details, and
the entire exposed Notes field above the editor footer. Raw session, epoch,
pause, event and clip rows and shared preferences remain unchanged; only intended
edit overlays persist. Isolated screens and readers finish before database cleanup.

This closes the **bounded three-change Report presentation regression check**.
It does not establish full Report or all 36 reference-screen fidelity. Large text,
other locales, many/long tags, extra edit sheets, provider-stage frames, retained
audio, and physical-phone behavior are outside this journey. Actual measurements,
honest missing values, and the later four-tab navigation remain intentional
differences from the illustrative boards. The separate Rhythm presentation change
`99ba8464` is not included in this tested source.

The eleven PNGs, environment, focused summary and Report log were copied directly
from this run's original artifact and compared with SHA-256 after copying.
[The hash manifest](original-sha256.json) records all fourteen byte-identical files.
