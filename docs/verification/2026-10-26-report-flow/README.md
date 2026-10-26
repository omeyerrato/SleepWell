# Focused saved-night Report flow — 26 October 2026

[CI40289201899](https://github.com/omeyerrato/SleepWell/actions/runs/40289201899)
passed **one existing native flow / eight actual Android screenshots** at
`4e3abe96ec2d856db5e1a3cd631effd4c5038f89`. This is a disposable host-muted API36
emulator using seven explicitly labelled isolated SQLite fixture nights. No unit,
lint, broad native/full-app, alarm, capture, native playback or physical phone
checks ran. No private phone data is in these artifacts.

The flow opens history, switches Week/Month/Year, opens the current saved night,
opens the event list and filters, edits tags/notes and a ten-minute awake interval,
saves, reopens, verifies persisted duration/notes/tags/awake time, then verifies
that Back discards a further unsaved note. Original records, measured fixture
rows, pause/event rows and preferences remain unchanged; only intended edit
overlays persist. Complete48dp primary actions are asserted on the captured frames.

Application source `2cbfe0432cfad943c76b500edb39069a8bbdeffc` builds with the original
signer in **48s**, schema17/metadata13. Its only production change after the passing
CI source clarifies provider-import duration copy. The imported provider stage
projection was built, but is **not exercised by these motion-only fixtures**.

## Actual comparison with the supplied references

All eight originals were visually opened and compared with board08 and the Report
frame on board01. These are bounded observations, **not finished-screen or all36
acceptance**:

| Capture | Observed comparison and remaining limits |
| --- | --- |
| history | Cream/chocolate Inter hierarchy, selected cream row, lavender true coverage ribbons, aligned time/duration columns and grouped floating dock follow board08. Four destinations retain the user's later dedicated Alarms request. Filter/search/date/large-font acceptance is not established here. |
| week | Lavender duration columns, faint goal grid and bedtime line are retained; Read details now fits completely above the dock. A visible date-range control is additional supporting functionality. Dated-goal gap is a dash because no dated goal exists; a fake2h gap is not substituted. |
| month | Missing fixture dates stay blank; actual seven dates remain on a month axis. Read details and selected Month remain visible. Sparse-state support does not certify every month/DST case. |
| year | Only the actual fixture month has a duration bar/bedtime point. Empty months remain blank. No history is invented to fill the reference's density. |
| night | Dominant recorded duration and start/end pair lead into measured activity. The large Awake review row moved to Report options; duplicate per-category no-audio notices collapsed to one status. Both counts and View sound events fit. This fixture has no audio or stages, so no invented waveform, Play or hypnogram appears. Root Report dock, retained audio, provider stages and all error states remain unverified. |
| editor | Start/end range, duration, paired controls, awake action, tags, note and fixed Save/Delete are real. The screenshot is scrolled to expose Notes and its complete footer; its header is outside the captured viewport. It does **not** match the full reference composition yet. |
| tags-notes | Actual saved tag drafts, icon chips and note fit above Save. Tag quantity/management actions make this view taller than board08; their compact placement remains design work. |
| reopened | The saved ten-minute awake interval changes duration7h42→7h32, with preserved tag/note state. The frame is scrolled, so full editor composition is still open. |

Earlier narrow attempts remain failures, not silently counted as passes:
CI40284673835 found Read details below the dock; source6ac2106b corrected it by
moving Recorded insights to Trend options. CI40287049190 reached selected Month
but hit a stale accessibility observation during view replacement; the bounded
helper reacquires that control without removing geometry or persistence assertions.
CI40288075037 exposed the night Report's repeated notices and overlarge Awake
link; sources2b7346ee/4e3abe96 corrected them. CI40288954127 was cancelled before
finishing after a partial edit was corrected. The final listed run alone passed.

Provider exchange, provider-stage rendering/inspection, retained audio playback,
new trend-date changes across process loss, overnight accuracy, personal phone
behavior and the rest of the36 reference screens are not validated by this run.

## Published artifact

[Development release](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-features-20261026-2cbfe04):
`SleepWell-2cbfe04.apk`,215577156bytes,
SHA256`a04c0cc37bd42df80339697e85a3467f1d66e4258ac5be5fabb8404ea5028492`.
GitHub draft=false/prerelease=true/exact full source target, both uploaded asset
states, APK size/digest and sidecar digest were verified against frozen local files.
The original signing identity matches the phone; this artifact is **not installed**.
