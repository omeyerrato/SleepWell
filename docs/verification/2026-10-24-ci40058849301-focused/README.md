# Focused tone/wake evidence — CI40058849301

Frozen source: `e0a78b49658dc0790f22c48af4829daa6638f3f5`.
[Original run](https://github.com/omeyerrato/SleepWell/actions/runs/40058849301).
Scope: **tone-wake only**, `full_app_review=false`; five classes/six methods,
39 required named images. This folder preserves all31 available original PNGs,
five exact instrumentation logs, both failure XMLs, environment and scope summary.
The emulator used fake playback, scheduling, notification and re-ring boundaries;
no personal phone data or hardware effects are included.

**Overall focused result: FAIL.** Three classes/four methods pass.
Assets1 PASS1.883s, grants1 PASS0.284s, WakeCheck2 PASS42.064s.
Tone1 and WakeSettings1 fail; ten required named originals are absent.

Two independent reviewers inspected all13 WakeCheck originals individually.
Their bounded visual gate passes: coherent original dawn crop, centered timer,
grouped awake glyph, unobstructed persistent/error/terminal controls, and no stale
injected save Toast. The supporting Editor save failure is visible inline.
This does not prove actual notifications, alarm delivery, full board04 fidelity
outside this screen, or acceptance of the full app.

Tone has12 named frames plus diagnostic: complete preview slider, Play/Pause,
Stop and Use footer now fit. The test rejects the51px Import text label instead
of targeting its126px clickable parent; four import/reopen/cancel states are
missing. WakeSettings has four named frames plus diagnostic; its `loud-curve`
image is actually Alarm sound and is rejected. The helper attempts to find the
offscreen Flashlight control before scrolling. Six later frames are missing.
Both flows require corrected controls/readiness and a fresh complete run.

Original bytes are retained, not resized, re-encoded or composited.
`SHA256SUMS` covers every raw evidence file copied from the run.
