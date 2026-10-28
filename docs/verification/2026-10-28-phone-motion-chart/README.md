# Phone-motion chart: bounded asynchronous history

[CI 40623012447](https://github.com/omeyerrato/SleepWell/actions/runs/40623012447), head `662e44cf9a7a9990348be0c923ef53c04cf30021`: **passed in 12.185s, one existing method / eight required originals.** Scope `chart-phone-motion`; native sounds and audio imports false; `full_app_review` false. The selector is `org.sleepwell.app.ChartClockFlowTest#alignedClocksElapsedSecondsAndFoldSelectionRenderWithoutEffects`.

Every passing original below was opened individually against [board 06 right](../../../design-concepts/2026-10-22-phone-app/06-tracking.png). Original PNG, method-log, environment and summary bytes are unchanged. `run.json` pins the authoritative head/result; `source-equivalence.json` hashes the listed source and records differences from the frozen root checkout. SHA256SUMS covers all retained files except itself.

## Change and exercised behavior

Previously, the LIVE phone path queried all session epochs and parsed/sorted/rebuilt its chart on the main Handler every second. The new production reader owns a coalescing worker and incremental indexed 1,024-row pages, retaining equal-time rows across page boundaries. It prepares the existing `ChartSeries` aggregation off main and applies immutable results to the same interactive `RecordChart`. Limits are 65,536 samples, 256 pending points and 2,048 exact bars; pathological detail produces an explicit retryable error, without truncation, gap filling or invented data. Normal Report chart APIs and contactless setup/live charts are unchanged.

The same existing method continues with labelled private SQLite evidence. Its initial history crosses a page boundary with equal-time rows, a real zero interval, null measurements and an omitted one-minute interval. Exact bar ranges, values, reading counts and missing intervals match the existing aggregation. A copied pending point survives mutation of the source JSON; durable/pending overlap counts once. A new settled result is required after requesting the paused phase, rather than accepting the preceding loaded object.

Keyboard and accessibility forward actions inspect actual bars; inspected wall time and keyboard focus survive subsequent loading, pause-phase refresh and read-error/Retry. A held worker allows main-thread actions and 100 requests to coalesce into the newest request. The fixture also checks explicit pathological-detail errors, session/generation late-result suppression and worker termination before owned database deletion. No tracking service, sensor registration, recording or audio port is started. Preferences are checked unchanged. These assertions do not claim an independent fingerprint of every personal database table.

## Eight individually inspected passing originals

All names have the `chart-clock-` prefix.

| Original suffix | Actual observation |
| --- | --- |
| `overnight.png` | In-memory 10:48 PM–6:30 AM series, contiguous-average tooltip and visible blank missing span. |
| `night.png` | The same measured fixture on plum with lavender bars, clock axis and focus outline. |
| `short.png` | Seconds axis through 21.321 s, a low true-zero mark and an empty missing interval. |
| `fold.png` | Repeated 2 AM ticks and tooltip expose distinct +02:00 / +01:00 offsets. |
| `phone-history.png` | Actual worker-loaded private history, 10 PM–10:30 PM axis, low zero marks and two separated gaps. |
| `phone-selected.png` | Zero selection reads `0.00 relative avg`; the measured pending tail is visible. |
| `phone-paused.png` | Newly settled paused-phase data retains the same visible bars, zero selection and clock axis. |
| `phone-retry.png` | Actual injected worker error renders the complete unavailable/Retry message, without an empty-data claim; keyboard Retry subsequently restores data. |

The phone chart has full visible bounds and the captures follow a committed, focused application frame. This scope has no automatic saved-PNG measurement-color assertion; the originals were visually inspected. The four phone fixtures have a visible **fixture-only clipped second title line**: “Phone movement history” wraps inside the shared fixed-height header. The chart, tooltip, axis and error message fit. This is not the production TrackingActivity heading, and this evidence does not accept that fixture heading or the full main-screen layout.

The board's lavender-on-plum measured timeline is retained within the chart. The labelled test shell, deliberate fixture shapes, interactive focus/tooltip, numeric end-clock label rather than literal “Now”, and absence of production tracking controls differ from the board.

## Failed first attempt retained

[CI 40622196708](https://github.com/omeyerrato/SleepWell/actions/runs/40622196708), head `b11b666783403ae8992f722d920c4dd4ae2659dd`, failed after 7.797s. Its raw log/summary/environment/run metadata and all four produced originals are retained in `failed-attempt/`; those four were also individually inspected. No phone-history originals existed in that run. `phoneHistory:50` unboxed a null `Double` while seeding the native missing-value fixture, before reaching the production reader. Native-only `662e44cf` replaces that numeric conditional with an explicit nullable variable. No production failure is inferred or overwritten by the later pass.

## Execution boundary and remaining limits

**The production reader and RecordChart apply/interaction path execute on a focused RecordReportActivity fixture. TrackingActivity's actual tick/snapshot/lifecycle scheduling, same-session pause/More/back reattachment and focus restoration are source-reviewed only.** The paused fixture requests the reader phase explicitly; the omitted time interval is private seeded evidence, not a service-driven pause/resume or a persisted `tracking_pauses` journey. Activity recreation of this phone live path is not proven here. Accessibility actions are checked, not a running TalkBack service.

These are inert emulator/private-store checks, not an overnight hardware recording, permission grant, physical audio/sensor check, latency benchmark or proof that every ANR risk is removed. Other startup/alarm/report database work remains outside this increment. All font/device sizes, pathological-limit recovery after external data replacement, production main-screen acceptance and complete 36-screen acceptance remain open. Root release/install/profile operations are outside this branch.
