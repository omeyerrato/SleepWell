# First-night review findings: df2476bc

[Run 41104738087](https://github.com/omeyerrato/SleepWell/actions/runs/41104738087), job 124231216963, artifact 13001277080. The build and 80 focused unit methods passed. Twelve of 14 native methods passed in 363.152 seconds; this run is failed, with six required frames missing. Artifact ZIP SHA256: c570dbe8c930a1d5e579f6423679589c9287a6ff7ed3749bef08ff8f8eebc98d.

The first-use/defaults and real Android notification-denial/settings-return journeys passed. Those results are narrower than the whole run. The optional microphone journey found the actual native denial action but its early tap left the prompt open. Its follow-up requires a focused permission-controller window and stable, fully visible denial bounds before dispatching one input; it never retries a tap or relaxes the denial checks.

The existing Skip journey read its model immediately after dispatch. Its failure PNG still shows the review, while the later hierarchy already shows the applied Skipped/Undo state. The follow-up waits for the returned applied row before reading the committed model, keeping all deadline, stored-state, schedule-count and Undo assertions.

Visual review found the unknown chart glyph falling back to a question mark in the notification explanation. It is replaced with the existing report glyph. The denied checklist exposes Open settings directly. Other follow-up adapters derive example accessibility duration/clock format from the same NightWindow used by its dial, and retain the existing app-settings fallback from a focused explanation.

These are actual native frames and synthetic private fixtures, without physical capture, playback or alarm execution. No new APK was delivered from this failed revision. Full app acceptance remains false.
