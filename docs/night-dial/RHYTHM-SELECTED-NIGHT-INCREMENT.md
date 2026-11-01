# Rhythm selected night — source increment, native acceptance pending

2026-11-01. Based on `76170c4120e943d169b02da990a76dbd1dcb8053`.
Flow28B promises a plotted day → its saved Night report. The earlier chart only
selected and announced a bedtime; it exposed no route to that source record.

Selecting a marker or using the existing D-pad/accessibility selection now shows
the selected sleep-day date and source plus **Open selected night**. The explicit
action opens `RecordReportActivity` for that observation's saved record ID.
Selection/scrubbing stays on the chart. It does not open whichever row becomes
latest after a refresh. The selected record/day and separate Rhythm/workday
viewport positions survive Report return and Activity recreation. Refresh that
removes the selected observation clears the action and asks for another plotted
night. There is no default selected record, fake future point or fabricated
measurement. The existing `RhythmStats` eligibility, recorded coverage, main-night
choice, workday calculations and insufficient-data/chronotype wording are unchanged.

The [bounded pending plan](RHYTHM-SELECTED-NIGHT-NATIVE-PLAN.json) extends the
existing `SleepRhythmFlowTest#actualSevenNightsWorkdaysPersistRecreateAndEmptyWithoutOutputs`.
It retains all five previous captures and assertions and adds four original
frames: `rhythm-day-selected.png`, `rhythm-day-report.png`,
`rhythm-day-returned.png` and `rhythm-day-restored.png`. A focused actual D-pad
selection followed by the visible Open action must launch the exact selected
record, preserve the read-only database snapshot, and restore a real nonzero
viewport and the selected date on return/recreation. The existing empty state
must have no Open action.

Local validation is diff and source/plan consistency only. No build, unit campaign,
native run, hosted cohort, personal-device action, model evaluation or output
playback ran here. Functional and Night visual acceptance remain pending root's
consolidated native run and inspection of all nine originals. The historical
source66 state register remains unchanged.
