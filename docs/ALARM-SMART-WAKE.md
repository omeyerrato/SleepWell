# Per-alarm smart wake-up metadata

`Alarm.smartWake` is immutable `AlarmSmartWake`. Missing metadata uses `DEFAULT(-1,-1)` and retains the existing global behavior. `sensitivity` is `-1` (inherit the active tracking profile), `0` (Low, gain 0.5), `1` (Medium, gain 1), or `2` (High, gain 2). `goal` is `-1` (inherit the global duration-goal gate), `0` (Off), or `1` (On). The goal concerns active recorded duration, not measured sleep or sleep stages.

The optional alarm JSON is `"smartWake":{"sensitivity":-1,"goal":1}`. It requires exactly those two finite whole-number fields in range. Nulls, strings, booleans, fractions, extra or missing fields are rejected. Both inherited values are omitted on encoding; older alarm JSON and defaults remain compatible without a database migration or backup-format change.

`Alarm.read` preserves malformed metadata unchanged through `smartWakeInvalid` and a private raw value. `setSmartWake` is the explicit repair path. Backup/default-template validation rejects invalid configuration; the ordinary alarm API marks it non-writable. Valid overrides survive alarm JSON copies, behavior defaults, metadata backups and unrelated supported API edits. Naps use their separate policy: nondefault imported nap overrides are rejected and `Naps.start` resets ordinary overrides before serializing a nap. `NapActivity` creates plain alarms, not ordinary behavior defaults.

Runtime consumers use `gain(MotionProfile)`, `requiresGoal(boolean)` and stable `config()` (`smart-wake-v1:<sensitivity>:<goal>`). Inherited gain may include saved surface calibration, so runtime counter identity must also include the resolved profile/gain, goal, window and occurrence deadline. Invalid configuration must deny early waking; it must never cancel the independent fixed alarm. Per-alarm consecutive-motion counters belong to the runtime integration, not this codec. UI and runtime are integrated separately; this source-only increment does not establish device or sleep/wake accuracy.

The combined model/runtime/UI source **2d99178e** builds production in **2m** with
the original signer. Ordinary and paired-phone early waking now use per-alarm
evidence with at least45seconds of contiguous fresh capture in a45–75second
interval. FIFO keeps its existing full/sample/freshness bounds. The unchanged
two-active-minute,20sample and0.12normalized thresholds apply independently to
each alarm/configuration/deadline. Partial, duplicate, stale or gapped evidence,
pause and configuration changes reset those counters. The historical ordinary
epoch counter had weaker quality checks; this stricter early-wake gate is
intentional. Original measured data and both nap movement paths are unchanged.

The alarm editor shows the configured window, real sensitivity selections and
recorded-duration goal, with explicit Follow resets. Its timeline is a schedule,
not a sleep-stage trace. Child Save settings keeps a draft; final Save alarm owns
the existing conditional write. Active recorded duration excludes pauses, while
missing/stale movement cannot trigger early waking. Independent fixed deadlines
remain scheduled. This does not establish sleep-stage, clinical or wake accuracy.

The latest packaged source **c6f2fcaa** assembleDebug passed in 18s, with original signer and uploaded hashes verified. The existing organization Android journey at9429af18 passes **26.671s**, with all13 originals individually inspected; only TravelActivity differs afterward. It exercises inherited controls, High/goal On, More Cancel, About recreation/close, draft preservation and final Save/reopen with full alarm equality and disabled fixture output. Header More retains explicit Follow resets/About without clipped main-page rows. This is configuration/persistence evidence; sensor early-wake runtime and accuracy remain unverified. [Exact evidence](verification/2026-10-27-smart-wake-travel/README.md). No new unit/lint/broad campaign or personal-phone/output action ran.
