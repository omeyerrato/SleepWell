# Measured chart clock axis

The horizontal coordinates of measured bars and gaps remain elapsed instants
inside the saved session. Only axis tick generation and label placement change.
Overnight interiors use readable wall-clock boundaries, for example 1 AM, 3 AM
and 5 AM, with the exact captured start and end retained. The end label takes
priority when space cannot fit every label. Labels are measured with the actual
chart font and omitted rather than allowed to overlap.

Ranges shorter than two minutes use elapsed seconds, including an exact decimal
endpoint. Longer ranges follow the device's 12/24-hour clock setting and the
record's zone. A repeated daylight-saving hour includes its UTC offset on axis
labels, selection capsules and accessibility descriptions. Spring-forward gaps
do not create nonexistent local times. Multi-day labels include dates.

`ChartTicksTest` covers overnight alignment, 12/24-hour positions, spring gaps,
both fall-fold occurrences, a window entirely inside a repeated hour, elapsed
seconds, narrow label spacing, multiline measurement, multi-day dates and dates
before the epoch. No test changes the OS clock, time zone or clock preference.

`ChartClockFlowTest` is a disposable-emulator-only, inert rendering journey.
Four labelled in-memory synthetic fixtures render cream overnight, dark signal,
short duration and repeated-hour axes. The owner Activity reads an isolated
named database. The test checks selection and focus survive a data refresh,
both fold offsets are exposed through actual accessibility selection, captures
only after the focused native frame commits, and checks exact preference-map
equality before cleanup. It never starts a service or any physical output.

Expected original PNG names:

- `chart-clock-overnight.png`
- `chart-clock-night.png`
- `chart-clock-short.png`
- `chart-clock-fold.png`

Local verification: all 10 `ChartTicksTest` cases passed; debug APK, Android test
APK and lint completed successfully (0 errors, 77 warnings). Native execution
and visual acceptance are pending fresh CI originals. These
fixtures do not prove an overnight recording, sleep-stage estimation, a sensor
pipeline or any other missing feature.
