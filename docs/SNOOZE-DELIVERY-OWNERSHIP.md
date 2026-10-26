# Snooze delivery ownership

The private `snoozed_<id>` JSON is retained history, never sufficient authority
to ring again. `AlarmReceiver` now admits SNOOZE under `Scheduler.class` through
`SnoozeDelivery`; `Scheduler.fire` rechecks before preparing the continuation.

Admission requires all of the following:

- A canonical UUID budget owner equals the current `alarmRun`.
- Positive pending deadline, supported spent/count, and a matching alarm ID.
- Budget snapshot, historical snoozed JSON and current `active_alarm` are exact,
  canonical matches.
- No alarm is currently ringing and the pending wall deadline is due, with the
  existing one-second scheduling tolerance. Valid delayed Android delivery has
  **no arbitrary upper lateness expiration**.
- The current saved row matches the configured-to-consumed source through the
  same `SnoozeSource` predicate used by `SnoozeDeadline`. This restores only the
  current exact random-task selection to its configured source. One-shot
  `enabled=false` consumption remains valid; an arbitrary disabled recurring
  alarm does not. Naps require the same claimed, disabled snapshot; pending,
  replaced, finished or missing nap rows do not qualify.

New immutable, explicit SNOOZE PendingIntents include `snoozeRun` (String) and
`snoozeDeadline` (Long), checked against that exact reservation. A missing member,
wrong primitive type or stale value is rejected. Older acknowledged intents
with neither extra remain eligible only through every private owner, pending,
source and due-time check above. They cannot identify their original run on
their own, and can never replace the current private proof.

The display receipt is not read to authorize playback. A missing/unreadable
receipt may hide the home-screen projection without suppressing a genuinely
owned pending delivery. Neither UI reads nor a broadcast consume the budget.
Successful `SnoozeBudgets.started` consumes pending/snapshot and carries spent
time into the new run; a failed continuation consumption aborts publication and
output. A duplicate delivery has no remaining pending authority. A process
failure between budget consumption and new alarm publication can lose a
continuation; it does not automatically replay uncertain output.

## Cancellation and provider integration

`Scheduler.cancel(context,id,Scheduler.SNOOZE)` commits removal of pending and
budget snapshot **before** cancelling Android's operation, under the same lock.
Reservation, rollback and consumption also hold that lock. If durable revocation
fails, cancellation throws; callers must report failure rather than claiming
the operation completed. An OS cancellation failure after durable revocation
cannot authorize a subsequently delivered stale PendingIntent.

Provider/edit code should call the read-only
`SnoozeDelivery.pending(SharedPreferences prefs, Alarm current)` while holding
`Scheduler.class` and using its current saved row. True means coherent private
pending authority remains, including overdue delivery and the brief reservation
while its old alarm is stopping. False permits removing the old blanket check
for *any* `snoozed_` key. Keep independent active-alarm and wake-check guards.
Do not infer absence of authority from the display receipt or from a past due
time. Malformed/inconsistent private authority cannot pass actual delivery.

All normal mutations that cancel/replace/delete an occurrence must revoke its
SNOOZE authority before changing or deleting the saved row. Source comparison
independently rejects edited/missing rows. An exact byte-identical raw database
delete/reinsert that bypasses every mutation/cancellation hook has no separate
row-generation identity; JSON comparison alone cannot detect that bypass.

## Quiet test alarm and scope

The existing negative test ID is limited to **-1**, independent of positive saved
alarms. It needs matching current private proof and exact configured `test_alarm`
source, plus the Scheduler.test quiet caps: at most25% volume, no ramp, backup,
wake check, pin, vibration, extra-loud, sunrise, flashlight or delayed sound;
default input, sound-mode and timing policy. It cannot borrow a positive row.
This preserves a genuine reserved debug snooze without permitting arbitrary
negative IDs or using missing production rows as a test escape hatch.

No schema/backup-format change, unit batch, build, phone/emulator delivery,
alarm, sound, torch or vibration test occurred in this source milestone.
Device/OEM delivery and failure recovery remain unverified. No alarm timing or
transport success is claimed from source inspection alone.
