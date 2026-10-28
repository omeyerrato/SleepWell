# Phone-motion chart: actual Activity ownership

[CI 40627223359](https://github.com/omeyerrato/SleepWell/actions/runs/40627223359), head `5c7940716ded2a3620814a302f90d147d994a081`: **passed in 29.722s, one existing method / fourteen originals.** Selector `org.sleepwell.app.ChartClockFlowTest#alignedClocksElapsedSecondsAndFoldSelectionRenderWithoutEffects`, scope `chart-phone-motion`; native sounds/audio imports false; `full_app_review` false.

All fourteen passing PNGs and all nine originals from the first failed Activity attempt were opened individually against [board 06 right](../../../design-concepts/2026-10-22-phone-app/06-tracking.png). PNG/log/environment/summary bytes are unchanged; authoritative run metadata, scoped source equivalence and SHA256SUMS accompany them. The [earlier eight-frame archive](../2026-10-28-phone-motion-chart/README.md) remains unchanged, including its narrower execution boundary and original nullable-fixture failure.

## Actual boundary now exercised

The continuation opens the real `TrackingActivity` through its existing captured Commands/StoreFactory ports. The genuine `Snapshot(TrackingService)` constructor assembles snapshots from a guarded, **unregistered native-only SnapshotSource**, using the real TrackingClock, MotionSoundAnalysis(false), private SQLite epochs and pause rows. Eight hours of seeded history include a valid-zero interval, 30 minutes of null movement and a persisted 15-minute pause; the active counter is 7 h 45 min. This is seeded evidence, not an overnight hardware recording.

Only nine service data fields are reflected: session, start, windowStart, clock, phase, motionRegistered, count, sum and motionSound. Handler/closer are accessed solely for cleanup. Service attachment, lifecycle, virtual start/bind/broadcast/output paths fail explicitly; final platform Service output APIs are never called. No Service is registered, no sensor/audio port is opened, and no production test hook was added. The displayed movement On/Paused states come from this disclosed snapshot fixture.

Real Activity ticks load the production asynchronous reader into the actual RecordChart. Keyboard/accessibility inspection, the same chart instance and inspected wall time survive ticks, More/Done/Back and pause. Physical measured inputs invoke the real controls; fake Commands persist pause/resume rows on a private storage worker and update the real clock. The active counter stays frozen across later paused publications and actual Activity recreation. Recreation reloads settled history into a new view; selection persistence across recreation is not claimed.

A real same-task report child causes Android onPause/onResume while a history read is held; the background chart cannot receive that result, and return resumes publication. A second held read spans a session swap: the actual tick replaces the chart, the detached old view remains unchanged and the new session shows exactly its one measured epoch. Full chart/footer bounds and enabled 48dp controls are checked. Every owned Activity and graph/report reader, the fixture writer, handlers and inert executor must close before factories are restored or private files are deleted. Preferences remain unchanged.

## Individually inspected passing originals

All suffixes below have the `chart-clock-` prefix.

| Original suffix | Actual observation |
| --- | --- |
| `overnight.png` | Overnight clock labels, selected contiguous average and blank missing span. |
| `night.png` | Existing in-memory chart retains lavender-on-plum bars and clock axis. |
| `short.png` | Seconds axis through 21.321 s, true zero and a blank gap. |
| `fold.png` | Repeated 2 AM ticks and selected interval expose distinct UTC offsets. |
| `phone-history.png` | Private worker-loaded history, low zero marks and two visible gaps. |
| `phone-selected.png` | Actual selected zero reads 0.00 relative avg; pending tail is measured. |
| `phone-paused.png` | A new settled reader result retains bars and selected zero. |
| `phone-retry.png` | Explicit unavailable/Retry message; keyboard Retry subsequently restores data. |
| `tracking-main.png` | Full actual main: 10:36 PM-6:36 AM history, 7 h 45 min active, complete More/Pause/Stop and light system glyphs. |
| `tracking-options.png` | Genuine named supporting rows, current facts, complete Back/Done. |
| `tracking-returned.png` | Actual main returns with the same zero tooltip, graph and full footer. |
| `tracking-paused.png` | Settled history and zero selection remain; Paused and Resume/Stop are complete. |
| `tracking-recreated.png` | Recreated actual owner has settled bars/clock span and unchanged paused duration. |
| `tracking-session.png` | New session replaces eight-hour history with its single measured bar on a 61.001 s span. |

No new product overlap or footer regression was observed. The known clipped second heading line remains in the four labelled RecordReport fixture frames, not the actual TrackingActivity frames. Saved images were visually inspected; this selector has no automatic saved-PNG color gate. Typography/proportions, deliberate fixture values, no-alarm state, interactive tooltip/focus and numeric clock endpoint rather than literal Now still differ from the illustrative board.

## Failed Activity handoff retained

[CI 40626002219](https://github.com/omeyerrato/SleepWell/actions/runs/40626002219), head `b820bafa5485ed1d57691c5524052bcacba74b88`, crashed after producing nine originals. The raw method log/failed summary/environment, full guest thread-dump log, no-ANR record, run metadata and all nine PNGs are retained under `failed-attempt/`. There is no authoritative completed-method duration and no failure PNG/XML: an uncaught worker AssertionError killed instrumentation before that catch could run.

The worker timed out at the native held-read gate while instrumentation waited in MonitoringInstrumentation.startActivitySync for the NEW_TASK child request; the main thread was idle in MessageQueue.nativePollOnce and Android reported no ANR. Native-only `5c794071` launches the child from the real current owner without NEW_TASK and waits on an exact bounded ActivityMonitor. The same focused/resumed/real onPause assertions remain. Held-gate timeout now raises an explicit Exception so diagnostics can survive. No production source change was needed for this correction.

## Equivalence and limits

`source-equivalence.json` pins named graph/native bytes and root comparison. The named scope's selector, fourteen originals, focused-output rejection and workflow defaults/choice are structurally equivalent; whole metadata-file equality is not inferred from formatting. The genuine Snapshot class is byte-identical to root, while root's whole TrackingService file additionally cancels travel reminders in real Service paths that are unexecuted here.

The earlier paging, equal-time rows, exact aggregation/counts, gaps/zero, immutable pending/durable dedup, coalescing, error/Retry and pathological-detail checks remain in this same method. This closes the Activity tick/lifecycle integration boundary for this inert journey; it does not verify real Service startup, sensor delivery, OS permissions, microphone, battery behavior, physical outputs, Stop/final report, TalkBack service, latency/ANR stress, every device/font size or all 36 screens. Root release/install/profile operations remain outside this branch.
