# Portable settings, CSV import and Tonight layout

Application source32f87b9 builds with `:app:assembleDebug` in2m2s. This is
implementation/build evidence only. Document-provider exchange, actual preference
recovery, SQLite migration and visual acceptance have not been exercised for this
batch. No new tests/lint/screenshots or personal-phone actions were run.

## Separate settings profiles

Settings → Data & privacy → Backup → Portable settings opens a separate profile
flow. The record JSON backup still excludes app preferences. A settings profile
does not replace alarms, recordings or history.

Five optional categories are supported:

| Category | Transferred configuration | Activation and exclusions |
| --- | --- | --- |
| Appearance | Changing light automatic/fixed phase, text size, reduce motion, contrast, Room/Wind-down app-window dimming | No permissions or external light credentials; import saves the dimming preference without applying window brightness |
| Sleep plan | Goal, bedtime, wake time, wind-down, reminder lead/repeat values | Imported bedtime reminder, Smart Bedtime and no-alarm reminder activation stay off |
| Bundled sounds | Bundled source, gains, timer/fade, rotation and stereo generator values | Sound-with-tracking stays off; local document/playlist references cannot be exported in this category |
| Narration | Supported rate and pitch presets | No engine/voice identities, download or speech |
| Tonight layout | Supported optional row order/visibility and artwork | No local revision/save receipt; mandatory controls cannot be hidden |

Prepare freezes the selected current values for review. Export uses a newly
selected Android Files document. Import reads a separately selected document,
validates all fields and displays the actual incoming configuration before Apply.
Cancelling selection or review does not apply values. Appearance exports include
the typed `roomDimming` Boolean and use settings-profile version2, because the
version1 Appearance schema has exactly five fields. Profiles without that new
field still export version1. All valid legacy version1 documents remain readable;
their absent dimming field leaves the current local preference unchanged. Review
names the imported dimming choice, or explicitly says it remains unchanged.
Older version1-only readers reject version2; omit Appearance when a version1
export is needed for an older installation.
Unknown categories/fields/types/duplicates, malformed UTF-8/JSON and files
above64KiB are rejected. No persistent document grant is requested.

The Room preference participates in the supported-settings fingerprint and
recoverable apply journal, including absent-key rollback and newer-choice
conflict checks. Permission-request markers such as `roomMicrophoneAsked` remain
device-local and excluded. The separate records/alarms backup remains format14
and still excludes app preferences; database schema17 is unchanged. A local data
restore therefore preserves the existing dimming choice, while moving that
choice to another installation requires the reviewed Appearance settings profile.

The existing `BackupDocumentFlowTest#explicitReviewedReplacementPersistsAcrossRotationAndReopen`
has a bounded private settings-API continuation: real JSON-file round-trip,
version2 review/apply/reread, legacy absence preserving current on/off values,
permission-marker exclusion, stale fingerprint refusal and absent-key rollback
from a deliberately seeded private recovery journal. It does not open the
Settings profile Activity or a provider, change window brightness, or simulate a
real process interruption. Its original eight Backup UI frames remain a separate
records-replacement scope. Native execution of this extension is pending; source
compilation alone is not round-trip runtime acceptance.

Apply checks the reviewed local-settings fingerprint and runtime admission,
blocks active alarms/tracking and active or paused sleep playback, and blocks
sleep-plan replacement while existing bedtime/guard automation needs attention.
Existing typed save APIs stage a bounded allowlist in memory. Main preferences,
narration preferences and Tonight layout each use acknowledged commits. They are
not one Android-wide transaction. A private recovery journal stores prior/planned
values before writes; interrupted/failed writes expose review and guarded rollback.
Rollback refuses to overwrite unsupported newer changes and reports unconfirmed
partial failure. It does not silently restart, schedule or enable imported work.
Concurrent writers and the final-check/write interval remain runtime limitations.

Tracking/capture configuration, background jobs, active ownership, credentials,
endpoints, integration/provider identities, permissions, enrolled challenges,
guard-app selection and history/alarm data are outside this profile format.

## Sleep as Android CSV append

Backup → Import sleep CSV opens a separate append flow. Choose the vendor's
`sleep-export.csv`, not its ZIP backup. The parser is original code based on the
[official format documentation](https://sleep.urbandroid.org/docs/devs/csv.html)
and a format example in the vendor's public repository. No vendor parser or
sleep-classification algorithm is copied.

The supported layout uses a repeated fixed15-field header, record values and an
optional noise row. Quoting, escaped quotes, actual embedded newlines and the
vendor comment newline representation are handled. Source `Id` and `Hours`
provide absolute bounds, checked against From/To in the source time zone. DST
overlap offsets must agree with that evidence; invalid gaps, unknown zones,
mismatches, unsupported headers and incomplete records reject the document.

Only selected new periods append. Review distinguishes identical source IDs,
changed contents under the same ID, existing exact periods and retained deleted-ID
receipts. These do not overwrite or merge into current records. One main-database
transaction adds the period, edit overlay, stable create receipt and source rows;
stale history or runtime admission invalidates review. Cancellation before the
commit boundary rolls back. An unconfirmed commit asks the user to inspect history
and review again; it does not blindly replay the import.

| Source value | App projection |
| --- | --- |
| Original bounds/time zone | Manual-source, imported-origin, no-sensor period |
| Comment and supported tags | Editable note/tags |
| Exact integral nonzero rating | Existing integer subjective-rating field |
| Quarter-star rating | Original source detail; never rounded into an app rating |
| LenAdjust/net duration | Original source detail; aggregate placement is unknown |
| Actigraphy/noise/Event/stage fields | Exact source rows retained; no fabricated local sensor/stage graph |
| Sched/Geo and remaining fields | Source evidence only; no alarm or location service starts |

App duration is the imported period after local edits, not the vendor's adjusted
sleep estimate. Edit sleep → Original CSV source shows the original period,
adjustment/net duration, fractional rating and retained signal/event counts. Local
edits preserve immutable original evidence. Full semantic conversion of vendor
sensor scales, events and stage estimates remains unimplemented.

Limits:4MiB input,2000 source records,10000 columns/row,20000 characters/field,
3MiB retained raw source and4MiB portable source payload. The existing8MiB total
record-backup bound remains authoritative. Exceeding limits fails visibly without
silently discarding fields or partially appending selected records.

Schema17 adds `sleep_csv_sources` and a session-deletion trigger. Original rows
and hashes are included in the optional `sleep_csv` version1 metadata13 extension,
typed stale-restore fingerprint, replacement restore and whole-library metadata.
Record/history deletion removes linked evidence; old backups without the extension
remain readable. Older readers can drop the optional extension, so a round trip
through an older app does not promise source retention. The source can contain
sensitive comments, location hashes and event/device metadata already present in
the chosen file; import explicitly discloses their private/portable retention.

## Subsequent imported-source inspection —86d0d1e

Edit sleep → Original CSV source and the imported report's Original CSV signals
now open a private source viewer. It reads actual retained actigraphy/noise fields,
shows original fractional rating/adjusted duration and pages original event fields.
Charts page64 original positions without dropping data, retain a global original
scale and allow raw-value inspection by touch or buttons. Measured zero is distinct
from missing/negative sentinel columns; no interpolation bridges missing data.

Literal clock labels are used only where header/value column structure establishes
the association. Legacy noise rows without established alignment use source-order
positions and disclose unmapped clocks. There are no invented absolute timestamps,
DST offsets, uniform minute intervals or converted sleep stages/RMS values.
Explicit Save original record CSV copies the exact retained2/3rows through Android
Files after fresh record/source checks, with cancellation and no process replay.
This includes original private comment/Geo/event fields, disclosed before export.
Source/provider/chart/visual behavior remains build-only at86d0d1e PASS1m14s.

## Optional Tonight layout

Appearance → Tonight layout offers row order/visibility and bedside illustration.
Default order is Bedtime goal then Wind-down, with the original artwork. Dated
goal-bank balance and latest saved non-nap period are optional and off by default.
They read actual saved values off-main and open the corresponding real detail,
with empty/error states rather than sample data. Missing days stay unknown.

The alarm time/arc, enable/day controls, Start tracking, active-alarm/check routes
and nap start/duration remain mandatory. Reset changes the draft, Save applies
with a stale revision check, and Cancel leaves the saved layout untouched. A
settings profile can transfer the supported optional arrangement. This is bounded
row customization, not a general card library or evidence of36-screen design
completion.
