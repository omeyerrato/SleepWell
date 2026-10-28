# Live sonar: settled paused-history evidence

[CI 40612296204](https://github.com/omeyerrato/SleepWell/actions/runs/40612296204), source `526de5e700a0d934c168e7231ed89ef8e34043f9`: **SonarNightFlowTest#visibleStartPauseRecreateResumeAndExplicitStopSaveActualNight passed in 21.564s; one existing method and all ten originals.** The focused job passed; `full_app_review` is false.

All ten originals were opened individually against [board 06 right](../../../design-concepts/2026-10-22-phone-app/06-tracking.png). PNG, method-log, environment and summary bytes are unchanged. `run.json` preserves the passing head; `source-equivalence.json` records scoped source hashes and app-code equality with integrated root at archive time. SHA256SUMS covers every retained file except itself. The [earlier loading-state archive](../2026-10-28-live-night-timeline/README.md) is unchanged, including its original paused PNGs and stated limits.

## Settled-state proof

Both [paused](sonar-night-paused.png) and [paused/recreated](sonar-night-paused-recreated.png) originals now visibly contain actual lavender bars and a **4:03 AM → Now** axis, with the same **six-second** active duration and complete Resume/Stop controls. They are new captures after loading settled, not relabelled earlier frames.

Before each capture, the native-only follow-up requires actual persisted valid intervals and waits for the rendered timeline's current session start/end, stored-endpoint coverage, positive measured history no greater than persisted evidence, and visible measured columns. Session/paused phase, active duration and fake-port allocation remain unchanged. The existing committed-frame, full bounds and saved-PNG measurement-mark checks then apply to both paused frames. Production rendering/source flags were not changed to obtain these frames.

The [whole-night original](sonar-night-whole-night.png) spans **8:03 PM → 11 PM → 1 AM → 3 AM → Now**. The active counter is **7 h 45 min**; the axis spans eight hours including the real persisted 15-minute pause. Native assertions retain exactly **7 h 15 min / 26,100,000 ms** measured duration after also excluding 30 minutes of `NO_ECHO`/null data, a valid zero interval, both blank gap midpoints, positive history and a measured pending tail counted only once after durable data. The eight-hour history is explicitly seeded through real private SQLite checkpoints; it is not an overnight physical recording.

## Individually inspected originals

All names below have the `sonar-night-` prefix.

| Original suffix | Observed state |
| --- | --- |
| `setup.png` | Contactless selected, truthful placement/analysis/clip/light states and complete Start. |
| `measuring.png` | Five-second measured history, start clock → Now axis, full chart and Pause/Stop. |
| `paused.png` | Settled measured bars/axis, six-second duration frozen, complete Resume/Stop. |
| `options.png` | Paused facts, named supporting routes and complete Done. |
| `options-recreated.png` | Same Options/paused state after recreation; no capture restarted. |
| `paused-recreated.png` | Settled bars/axis restored after Done, same duration and full controls. |
| `resumed.png` | Measured status and prior history retained; partially covered current time remains blank rather than extrapolated. |
| `missing.png` | Explicit Signal missing, historical measured bars and blank unmeasured regions. |
| `stop-confirmation.png` | Focused End confirmation, complete choices; one measured physical End input saves the short fake-PCM night. |
| `whole-night.png` | Eight-hour clock span, low valid-zero marks, separate missing/pause blanks and complete footer after reader Retry. |

No new overlap/footer regression was observed. The graph, More and primary controls fit their normal-text viewport. Nine standard originals pass the rendered/saved-image light-glyph gate; the separate dimmed stop dialog was inspected visually. Typography/proportions, truthful contactless labels, no-alarm fixture state and data-driven bar shapes still differ from the illustrative board.

## Limits

The same method retains generated-PCM coordinator/storage pause/resume/end proof, Options/Back/restoration and stale-callback checks, actual reader error/Retry, pending deduplication and graph-worker termination before private-file deletion. There is no separate Retry-error PNG. The previous short-night backup validation occurs before the separate eight-hour seeded fixture. These are fake-port/private-store checks with host outputs disabled, not device audio, hardware, permissions or acoustic performance evidence.

Columns touching missing/paused coverage remain conservatively blank, which can widen small visible gaps. Maximum-span/performance/ANR stress, DST, every font/device size and complete 36-screen acceptance remain open. Contactless setup retains its unchanged 15-second probe chart. The phone-motion RecordChart path, its inspection behavior and known main-thread full-history query remain unchanged and outside this native selector.
