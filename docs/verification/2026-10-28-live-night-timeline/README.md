# Whole-night live sonar: bounded native evidence

[CI 40607283611](https://github.com/omeyerrato/SleepWell/actions/runs/40607283611), verification source `a166c922ee25e48d0ca322cfff1ee8d863cd8b8e`: **SonarNightFlowTest#visibleStartPauseRecreateResumeAndExplicitStopSaveActualNight passed in 23.302s, one existing method, all ten required originals.** The focused `live-night-timeline` job passed; `full_app_review` remains false. This is verification-branch evidence, not the frozen 0.1.2 release or physical-phone acceptance.

All ten originals were opened individually against [board 06, right](../../../design-concepts/2026-10-22-phone-app/06-tracking.png). PNG, method-log, environment and combined-summary bytes are unchanged. `run.json` records the authoritative passing head. `source-equivalence.json` records scoped source/asset hashes matching the verification branch, and deliberately distinguishes it from root's separate release source. SHA256SUMS covers every retained file except itself. No unrelated older artifacts are copied.

**Paused-history capture remains open:** both paused originals show the truthful asynchronous “Opening movement history…” state. They prove the represented paused controls/layout, not settled paused bars or a settled paused time axis. A later native gate must wait for actual loaded history before accepting that state. These images alone do not establish a persistent production failure or completion of screen 18/all 36 screens.

## Actual clock span, gaps and zero

The [whole-night original](sonar-night-whole-night.png) visibly spans **7:16 PM → 10 PM → 12 AM → 2 AM → Now**, crossing midnight over eight hours. Its **7 h 45 min** active counter excludes the actual stored 15-minute pause; the graph's wall-clock axis retains it. The native rendered-data assertion independently requires the full eight-hour start/end span and **26,100,000 measured milliseconds (7 h 15 min)**, also excluding 30 minutes of stored `NO_ECHO`/null movement.

The first 15 minutes contain genuine valid zero values, shown as low marks near the baseline rather than missing data. The persisted missing interval and separate pause appear as blank regions. The method checks zero at a known measured time, null at both gap midpoints, a positive stored interval and a measured current tail. It requires at most 240 display columns. A pending six-second frame list intentionally overlaps the durable endpoint by one second; the exact measured-duration assertion must still count only its new five-second tail. These assertions run before and after reader recreation/error/retry.

The long history is an explicit private test fixture: actual `Store.sonarCheckpoint` calls write homogeneous intervals of at most five seconds, plus real pause/resume rows. An injected snapshot supplies its current clock and pending frames without allocating another audio port. It is not an eight-hour physical recording. Repeating fixture values average into the nearly level high bars; no production history or decorative waveform is generated. The preceding short-night part uses the real coordinator/DSP/storage path with generated echo PCM and a fake duplex port, then explicitly ends the night, checks stored valid/missing epochs and pause, and validates that saved short-night backup. That backup assertion precedes the separate long-history fixture.

## Visual comparison and each original

The whole-span clock axis, High/Low relative scale, lavender columns, plum surface and complete Pause/Stop footer follow the board's live-chart hierarchy. Actual status, sound-off and no-alarm states remain explicit. The graph and fixed controls fit without overlap; native full-width/full-height, 48dp control and dock-clearance gates pass. Short five-to-thirteen-second sessions have a few wide time-proportional columns; the eight-hour span has narrow bounded columns. Bar shape follows the represented data, not the reference's illustrative waveform.

| Original | Individually inspected state |
| --- | --- |
| `sonar-night-setup.png` | Contactless selected, real placement art/guidance, sound analysis off, dependent clips/light unavailable and complete Start; no audio allocated before Start. |
| `sonar-night-measuring.png` | Five-second measured state, real visible columns on the session-start clock → Now axis and complete main controls. |
| `sonar-night-paused.png` | Six-second active duration frozen, capture stopped and Resume/Stop complete; timeline still displays Opening movement history. |
| `sonar-night-options.png` | Paused Options shows reserve/sampling/flip facts and named supporting settings; complete Done. |
| `sonar-night-options-recreated.png` | Same paused Options page after recreation, same facts/routes and complete Done; no capture restarted. |
| `sonar-night-paused-recreated.png` | Same six-second active duration and paused main controls after Done; timeline is still captured loading, not settled. |
| `sonar-night-resumed.png` | New measured segment after resume with a visible intervening gap, session-start clock axis and complete controls. |
| `sonar-night-missing.png` | Signal missing explicitly shown; prior valid history stays visible and the current missing region is blank. |
| `sonar-night-stop-confirmation.png` | Focused owned End confirmation with complete Keep tracking/End & review; the measured physical end input dismisses it. |
| `sonar-night-whole-night.png` | Eight-hour wall span, 7 h 45 min active duration, low zero marks and two blank intervals; complete chart and footer after actual reader Retry. |

The nine standard captures pass the existing rendered/saved-image light-glyph gate. The separate stop-confirmation original was visually inspected: its light system glyphs/gesture remain readable over the dimmed background. Typography/vertical spacing, shared glyphs, contactless-specific truthful labels and the fixture's no-alarm state still differ from the board. This archive does not accept all device sizes, font scales or transient frames.

## Source and native boundaries

The new live-sonar reader captures the owned store factory, reads indexed epoch pages off the main thread and retains bounded statistics. Durable wall intervals stay on their original clock; session-relative pending sonar frames add the session wall anchor. The active-duration counter is separate. Time-weighted display columns never fill missing or paused time with zero. A one-second cell or wider display column touching a gap is conservatively blank, so a small gap can blank a wider visible column.

The method requires its reader factory to run off the main thread, injects a real history-read failure, observes the actual Retry action, clears the fault, triggers Retry and rechecks the settled rendered data. There is no separate error-state PNG. Activity/focus/session/generation fences prevent obsolete delivery; the native fixture waits for every owned graph reader to terminate before deleting its private database, retaining owned files if draining fails. This is bounded execution evidence, not a maximum-duration performance/ANR benchmark or exhaustive race proof.

Options restoration, Done/Back return, detached-callback inertness, frozen pause duration and unchanged fake-port allocation are retained. The named supporting routes are present; their separate child features are not all executed here. The saved short night has no sound events. Original unrelated preference values are checked and the two setup flags are restored; no claim is made that every unrelated personal database table was independently snapshotted by this method.

**Phone motion is unchanged.** Its existing RecordChart query/render/inspection path, including the known main-thread full-history reload, remains outside this correction and this native selector. Contactless setup still uses the unchanged rolling 15-second SonarSignalChart; this job does not rerun its qualification flow. No schema, DSP, sensor/sonar runtime, protection, permission default or alarm behavior is changed by this increment.

The branch commits in integration order are source `2053e85aa6b0a410664c5a72a3690a08ca4ded4a`, native extension `b33f96d66a26ed1eecc97cfb9703c57c5d86716a`, scope metadata `a166c922ee25e48d0ca322cfff1ee8d863cd8b8e`, then this docs-only archive. Native hardware/audio outputs, physical permissions/routes, acoustic performance, full-night sensing reliability, DST/maximum-span stress and complete 36-screen reference acceptance remain unproven. Root owns integration and subsequent CI.
