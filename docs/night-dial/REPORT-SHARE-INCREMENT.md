# Private Report share: pending native acceptance

1 November 2026, based on `80f06f44` plus the separate Import route and Report
fixture-launch repairs. Design38B requires an actual preview, notes/audio excluded
by default, separate notes opt-in, explicit Share → Android chooser, and Cancel
back to the same Report. This implementation supplies that local flow.

**Report options → Share this night** opens a non-exported native preview for one
completed saved period. The exact shared UTF-8 text contains dated UTC bounds,
known/unknown recorded time zone, reviewed duration, source/provenance and
interrupted/clock-change flags. **Include notes** starts off and survives draft
recreation after explicit selection. Audio, tags, ratings, raw signals, database
IDs, credentials and device/provider IDs are never shared. The actual document
text is visible before the Share button. Cancel returns without a chooser.

The private worker reads a coherent record snapshot, writes a unique fsynced
cache file and re-reads the record/fingerprint and verifies the exact file before
Share. A changed/deleted record, expired file, cancellation or history replacement
rejects that handoff. No URI access is admitted while merely browsing. Only a
visible, unobscured explicit Share tap creates `ACTION_SEND` inside the native
chooser with a single stream/ClipData URI and temporary read-only permission.
Returning says that opening options does not confirm delivery; it never reports
an upload or recipient success.

`ReportShareActivity`, data/job/files helpers and `ReportShareFileProvider` are
private and separate from `SleepSummaryActivity`'s public GET_CONTENT picker.
Its caller checks, custom MIME/schema and source-selection behavior are unchanged.
The new authority exposes only generated reports in one canonical cache directory,
with a friendly filename and read-only name/size metadata. It has no database,
directory listing or writable surface. Files are at most128 KiB, sixteen recent
files are admitted, and read access expires after ten minutes or process loss.
Unshared copies are discarded. Global history-changing cleanup also revokes and
removes these private files; failure does not skip the existing public-summary
cleanup attempt. The only change in `SleepSummaryFiles` is that cleanup hook.

The route inventory now accounts for128 manifest Activities in the same43 flow
families. This is implementation accounting, not acceptance of additional states.

## Exact native handoff

The existing first `NightDialReportFlowTest` method now exercises actual Report
navigation, default-excluded notes, explicit notes/recreation, provider rejection
before Share, changed-record rejection, explicit reprepare, the real Android
chooser and Back without choosing a recipient. It reads the exact admitted file
through the actual provider, rejects a write, checks source/raw data and removes
only its owned generated file. The compact `night-dial-report-share-proof.json`
records those synthetic-fixture assertions and file SHA; it is not produced yet.
Owned workers terminate before fixture cleanup, including failures.

[Cumulative pending plan](REPORT-IMPORT-SHARE-NATIVE-PLAN.json): the same three
existing methods, **46 required originals** (39 Report, seven Calendar), no new
test methods or units. Six Share frames use focused app hardware-frame capture;
`share-chooser` uses the actual foreground Android package/window and UiAutomator
capture because a system window is not owned by the app's WindowInspector.
No recipient is selected or message sent. All originals need actual inspection.
The earlier Report37 plan and failed41249512029 run remain separate history.

Diff/manifest/inventory/scope consistency checks run locally; build and native
execution are integration-owned and pending. No model evaluation, download,
personal phone, microphone, playback or alarm action ran here.
