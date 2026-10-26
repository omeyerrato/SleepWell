# Imported CSV event annotations

This note describes source **8094497**. Its production build is **pending at the
time of writing**. No tests, screenshots, runtime flows, devices, providers or
network calls were exercised for this documentation. Source implementation is
not evidence of completed visual acceptance or correct interpretation of every
vendor export.

## Source facts and timestamp inference

The primary [Sleep as Android CSV format documentation](https://sleep.urbandroid.org/docs/devs/csv.html)
describes the exported CSV format. The vendor's public
[sleep-test2.csv example](https://github.com/urbandroid-team/sleep-csv-to-json/blob/master/sleep-test2.csv)
contains named event fields with numeric timestamps. **The interpretation of
those event numbers as Unix epoch milliseconds is an inference from that public
example; the CSV specification does not explicitly document event timestamp
units.** The chart therefore presents imported source annotations, not a new
SleepWell measurement or a guaranteed complete vendor-event specification.

The parser recognizes a field shaped as `NAME-number` with an optional trailing
payload. It retains the exact original field, name, numeric value, source position
and payload. Interpreted timestamps are compared with the original imported
period; displayed local clocks use that record's saved source zone. No dates,
offsets or event positions are inferred from the actigraphy/noise sample columns.
An unparseable field remains available in the original-field list and contributes
to the unreadable count.

This is original SleepWell interpretation/rendering code. It does not copy the
public converter's implementation or proprietary sleep-analysis algorithms.

## Independent lanes and retained uncertainty

Only these explicit start/end names can form interval lanes:

| Lane | Start | End |
| --- | --- | --- |
| LIGHT | `LIGHT_START` | `LIGHT_END` |
| DEEP | `DEEP_START` | `DEEP_END` |
| REM | `REM_START` | `REM_END` |
| PAUSED | `TRACKING_PAUSED` | `TRACKING_RESUMED` |
| BROKEN | `BROKEN_START` | `BROKEN_END` |

Events are ordered by numeric time, then original source position. A drawable pair
needs an unambiguous start followed by a strictly later end, no payload on either
anchor, and both endpoints inside the original period. Missing starts/ends and
multiple distinct pending starts do not create filled intervals. Repeated exact
name/time anchors are counted as duplicates and do not create additional spans.
Pairs are not clipped into validity when their original endpoints fall outside
the source period.

The lanes remain independent: overlapping LIGHT/DEEP/REM/PAUSED/BROKEN source
annotations are retained rather than flattened into an invented stage sequence.
Blank areas are unknown. In-period `ALARM_*` fields without payloads appear as
point markers on a separate ALARM row, not inferred durations or awake intervals.

The UI reports complete-pair and alarm-marker counts, plus unpaired/ambiguous,
outside-period, duplicate, other/payload and unreadable counts. These diagnostic
counts can overlap. Unknown names, payload-bearing fields, unmatched anchors and
outside-period values remain inspectable in their exact original form; their
absence from the plotted lanes does not delete or silently correct source data.

## Inspection, lifecycle and unchanged records

Open an imported CSV record's **Imported source signals** detail, then **Original
event annotations**. **Full period** shows the original period; **6-hour view**
pages through it with Earlier/Later controls. Chart edges crop only the displayed
view, not the stored event pair. Tapping a paired interval or using **Inspect paired
intervals** opens its exact zoned endpoints, duration in milliseconds and original
start/end strings and field numbers. **Original event fields** exposes raw fields
in pages, including those not drawn.

View mode, timeline page, interval-list page and original-field page survive
Activity recreation. A changed source hash resets source-specific selection. The
retained read-only job loads off the UI thread in a database read transaction and
checks the record's existence, imported/manual origin, raw period, source zone,
source identity and hash. Missing or mismatched evidence produces an unavailable
state with explicit retry rather than a fabricated chart.

These annotations do not create local stage epochs, projected awake intervals,
new sensor provenance or duration adjustments. They do not trim or retime source
evidence to later app-period edits and do not mutate recordings or manual edits.
The aggregate CSV length adjustment still has no known placement. The existing
actigraphy/noise graphs keep their separate source-order/vendor-scale meaning.

The feature adds no capture permission or export authority. The existing **Save
original record CSV** action still requires its own explicit document destination,
rechecks reviewed record/source identity and exports the exact retained two/three
rows. Those rows can include comments, tags and original location/device/event
metadata; the annotation chart does not sanitize that existing export or make it a
full sleep backup. Cancel/error can leave a partial destination as already disclosed
by that export flow.

Source: [event interpretation](../app/src/main/java/org/sleepwell/app/SleepCsvEvents.java),
[lane chart](../app/src/main/java/org/sleepwell/app/SleepCsvEventChart.java),
[source-detail UI](../app/src/main/java/org/sleepwell/app/SleepCsvSourceActivity.java),
and [pinned read/export job](../app/src/main/java/org/sleepwell/app/SleepCsvSourceJob.java).
