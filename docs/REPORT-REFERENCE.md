# Report reference flow

The Report collection is `HistoryActivity`: the selected Report dock item stays
on that surface, with direct Trends and Search controls. Its rows open the sleep
editor. Trends' Read details opens `RecordReportActivity` for the latest saved
night; that report has direct Edit, History and Trends actions.

The history ribbons show effective recorded coverage and relative movement from
actual minute epochs. Blank intervals remain gaps. Manual entries have uniform
coverage without invented sensor readings. Neither ribbons nor the editor
timeline are sleep-stage hypnograms. The report explicitly marks sleep stages
unavailable. Night reports use the same effective duration projection as editing,
history, CSV and trends; raw readings remain unchanged after edits.

Trends merge overlap using the existing RecordTrends model. Missing dates remain
blank; period averages exclude them. Duration uses labeled scales and actual
daily totals. Bedtime joins known main-period start times, breaking at missing
days. A surplus is labeled Above goal rather than falsely displaying a zero gap.
Long explanations and the existing day-boundary setting are under Trend options.

`ReportReferenceFlowTest` has one test and is restricted to an explicitly opted-in
disposable Android emulator. It creates seven isolated DB records and synthetic
minute measurements, marks every screen VISUAL TEST, verifies awake edits, tags,
notes, Save/reopen and unsaved Back, and compares raw rows and personal preferences.
It starts no recording, microphone, audio, alarm, torch, or scheduler operation.
It emits `report-reference-{history,week,month,year,night,editor,tags-notes,reopened}.png`.

Native execution and comparison of those actual captures are required before
visual acceptance. Compilation and pure tests alone do not establish that gate.
Sleep-stage classification, sound classification and broader app parity remain
outside this change and incomplete.

The first CI journey (39997627697) produced seven original screenshots and failed
on a focus assertion while reopening the editor. Reviewing each image found the
Trends detail action clipped beneath the dock and overly segmented history
ribbons. The follow-up compacts both charts and their spacing, keeps measurement
colours inside continuous coverage spans, and requires the complete detail touch
target above the selected dock in all three trend periods. Capture now waits up
to eight seconds for one resumed, attached, laid-out, focused decor to commit a
hardware frame, and rechecks that same window before saving the screenshot.
These corrections still require a fresh successful eight-screenshot native run.
