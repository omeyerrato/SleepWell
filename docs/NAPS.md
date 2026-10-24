# Relative nap alarms

Board 09, middle screen. `NapActivity` is a native configuration flow, not a tracking session or a second alarm runtime. It saves ordinary `Alarm` settings plus optional `NapPlan` metadata and ultimately uses the existing protected challenge alarm. Existing alarms without this metadata retain their previous behavior, including old nap-labelled calendar alarms.

## Behavior

- 10/20/30/90-minute presets; chosen alarm sound, independent explicit preview capped at 25%, volume, gentle ramp, vibration and screen sunrise settings use the existing runtime settings. Previews stop when the activity pauses, loses focus, changes route or starts the timer. The sound screen uses the existing decoder, tone import validation and lifecycle owner.
- Start saves a relative timer with exact millisecond anchors. The UI shows an actual end time; it does not floor the timer into the next local-clock minute. Schedule entry accepts an explicit future date and local time and includes both the waiting time and the chosen duration. Repeated DST times select the earlier offset; non-existent times fail visibly. It does not automatically begin a tracking session at the future start.
- Multiple naps opens actual saved nap records. Pending timers can be cancelled. Repeated Start submissions cannot replace an enabled pending timer or extend its deadline; replacement requires explicit cancellation first. Ended/cancelled records can be explicitly restarted with a fresh nonce or removed after confirmation. No new emergency-stop path is added to a ringing challenge.
- Smart wake-up is **motion-based early waking within the last five minutes before the fixed deadline**, only when phone movement tracking is already active. It uses the configured sensitivity, actual samples, and two recent active minutes. It is not a sleep-stage estimate. It does not arm tracking, microphone recording, or any service. If tracking subsequently stops, the exact deadline remains. This is not Sleep as Android's separate minimum-rest-plus-later-smart-window Power Nap algorithm.
- A nap is disabled and durably claimed before invoking the existing alarm runtime. Snoozing retains the same nap identity. Delete-after-dismissal waits until the challenge actually dismisses. If a wake-up check is configured, deletion waits for matching confirmation; the saved check must still match both the original alarm occurrence and the nap nonce. Cancelling or superseding a check does not falsely count as confirmation.

## Ownership and timing

`NapReceiver` is non-exported and uses an explicit immutable broadcast PendingIntent with alarm ID and per-timer nonce in its identity. It never accepts external URIs or payloads. The current persisted row must match the nonce and pending/enabled state. Cancelled, disabled, replaced, imported, already claimed and finished rows reject late callbacks. A stale legacy `FIRE` broadcast cannot run a nap through calendar scheduling.

All production transitions use `Scheduler.class`'s occurrence lock. The saved alarm is reread after mutation. PendingIntent cancellation is an optimization; durable nonce/state checks remain authoritative if cancellation fails. The scheduler uses `setAlarmClock` for an actual alarm deadline, not an inexact background job. Exact-alarm access, notifications, channel importance and Android 14 full-screen access are checked, with a visible settings/retry path. Access can still change after scheduling, and delivery is never guaranteed while a phone is off or force-stopped.

On the same device and boot, remaining duration uses `elapsedRealtime`; wall-clock, time-zone and DST changes cannot shorten or extend it. A boot/device change uses the original saved wall deadline; an overdue pending timer is scheduled immediately for one owned delivery, never rolled to the next day. Reconciliation uses the existing normal `BOOT_COMPLETED` receiver after credential-protected app storage is available; it is not direct-boot delivery before unlock. Reboot cannot recover time spent powered off if the user also changes the wall clock. Backups strictly validate all optional metadata, preserve old JSON without a nap field, reject malformed data before replacement, and restore nap rows inactive/cancelled. Explicit Start creates fresh anchors and a fresh nonce. No timer resumes automatically from a portable import.

**Crash boundary:** a durable claim and an external foreground-service launch cannot be one atomic transaction. A crash after claim but before service launch, or an uncertain launch error, is not automatically retried because that could duplicate an alarm. An uncertain delivery remains disabled with review information. Starting another nap is explicit and remains blocked while an alarm is actually ringing. Overlapping alarms still share the existing single alarm runtime; a conflicting nap delivery reports an error rather than promising a second independent alarm. Failed cleanup may leave an ended record to remove manually.

## Integration hooks (parent-owned MainActivity)

1. `MainActivity.nap()`: `startActivity(RecordUi.intent(this, NapActivity.class));`.
2. Opening a saved row with `alarm.nap != null` should open `NapActivity` with its `id`, rather than the recurring alarm editor. Invalid nap metadata should produce a review error, not be silently reinterpreted as an ordinary alarm.
3. Disabling a pending nap: `Naps.cancel(context, alarm)` then refresh the alarm list. This performs durable cancellation before removing its matching job.
4. Enabling an inactive/restored nap must open `NapActivity` with its `id` for an explicit **Start nap**. Do not set `enabled=true` and pass stale anchors to calendar scheduling. The screen starts a new duration and nonce. Ordinary alarm toggles retain their current path.
5. Tonight/NightWindow must include enabled pending naps by `NapPlan.projectedDeadline(Naps.clock(context))`, rather than computing a recurring occurrence from `Alarm.hour/minute`. This is necessary for future scheduled dates as well as clock changes.
6. When removing an alarm elsewhere, cancel its owned nap job via `Naps.cancelJob(context, alarm)` as well as the existing scheduling cleanup. An orphaned job is still fenced by the missing row, but explicit cleanup removes its system alarm-clock indicator.

## Verification and limits

`NapEngineTest`: 16 pure tests cover exact seconds, one-shot claims, cancellation, replacement, wall shifts, boot/device fallback, overdue delivery, scheduled starts, permission/scheduler failures without deadline extension, failed persistence, uncertain delivery and the actual motion gate.

`NapFlowTest`: three inert emulator-only methods, private SQLite plus fake clock/scheduling/delivery/preview. They exercise actual preset, sound, explicit quiet-preview, recreation, save, reopen, cancellation, scheduled entry, list, permission/scheduling errors/retry, strict backup rejection, inactive restore, duplicate delivery, deferred matching deletion, and a user-enabled fake-motion configuration that drives the actual smart-window transition. The fixture preserves an unrelated alarm, all original preferences and the original app database; it never starts `AlarmActivity`, services, recording, real notifications or hardware outputs. Planned 15 actual PNGs: `nap-{default,preset,tracking-required,sound,pending,recreated,reopened,cancelled,schedule,schedule-error,scheduled,list,permission-error,scheduler-error,retry}.png`.

Compilation and pure tests do not establish native test passage, visual parity, actual alarm delivery, Doze/reboot behavior, real smart wake effectiveness or hardware correctness. Those remain separately reported evidence gates.

## Permission and intent review

Assessment: two private components, no new permission, no exported receiver or Activity, explicit immutable PendingIntents, UUID ownership plus persisted row/phase checks, no mutable result channel and no URI-opening action. Existing exact/notification/full-screen permissions are checked before arming; protected platform calls catch runtime denial and retain the fixed deadline with an error. The activity does not silently request recording/sensor access. No external code, network SDK or dependency was added.

## Primary sources

- [Sleep as Android: Naps](https://sleep.urbandroid.org/docs/alarms/naps.html): relative timer and deletion after dismissal; duration range 1 minute–10 hours.
- [Sleep as Android: Power Nap](https://sleep.urbandroid.org/docs/alarms/power_nap.html): distinct minimum-rest plus subsequent smart-window behavior; excluded from parity claims here.
- [Android SystemClock](https://developer.android.com/reference/android/os/SystemClock): elapsed time includes deep sleep and resets on boot.
- [Android AlarmManager](https://developer.android.com/reference/android/app/AlarmManager): alarm-clock scheduling and exact-alarm access requirements.
- [Android Doze and App Standby](https://developer.android.com/training/monitoring-device-state/doze-standby): alarm-clock behavior, idle scheduling restrictions and device limits.
- [Android PendingIntent](https://developer.android.com/reference/android/app/PendingIntent): identity and immutable explicit operations.
