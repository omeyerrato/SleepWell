# Named alarm deletion Undo — Night Dial 41C

Deleting the reviewed alarm now returns to Alarms with a named receipt and an
explicit Undo action. Its adjacent copy states **"Undo restores this alarm off
for review"** before the action is used. Restoration reports **"Restored off;
review before enabling"** and offers the ordinary alarm editor. It never silently
restarts an alarm or relative timer.

## Real behavior and identity

- The existing editor still checks its original saved alarm and cancels the same
  FIRE, SNOOZE, CHECK, BACKUP and nap work. Final deletion is an exact-row comparison
  and delete inside a SQLite write transaction; a changed row is not removed.
- A successful delete publishes one process-local receipt for 30 seconds of
  monotonic time. It pins the source factory, canonical database path, original
  JSON, alarm ID, mutation revision, runtime-admission generation and random nonce.
  A newer deletion replaces it. Activity recreation preserves it; process death
  or reboot does not recreate it from old saved state.
- Undo checks the exact live receipt, expiry, factory/path, mutation epoch and
  runtime admission. Its write transaction verifies that the original ID is still
  absent. It uses `insertOrThrow`, never replace/upsert, and consumes the receipt
  before attempting that insertion. A stale or second action cannot overwrite or
  duplicate an alarm.
- A restored alarm keeps its base configuration but is **off**. Old nap state,
  weekly-nap ownership, skip, next-only override and scheduling-error/run metadata
  are removed. Undo does not call a scheduling, notification, audio, flashlight or
  capture owner. A later user enable/save uses the normal existing owner afresh.
- Controls from a detached/destroyed/unfocused old window cannot invoke Undo,
  Dismiss or Review. The visible receipt can be dismissed without restoration.
  Expiry and stale-state failures explain that the alarm remains deleted.

The receipt is a process-local recovery convenience, not a disk backup or a
permission to resurrect a previous run. No app/portable schema, permission,
dependency or retained-user-data migration is added.

## Mutation fencing and lock ordering

The process mutation epoch advances inside each alarm-table write transaction.
Standalone `Store.save`/`deleteAlarm` writes get a small transaction; calls already
inside a transaction reuse it. Raw alarm-provider writes and backup replacement
also invalidate receipts inside their write transactions. History-replacement
admission invalidates earlier receipts immediately as well.

This closes the create/delete ABA case: even if another writer creates the same
ID and subsequently removes it, its mutations invalidate the old receipt. The
epoch is deliberately conservative: any alarm mutation, including another
database in a verification fixture, invalidates an available receipt. Production
has one normal alarm database. Failed writes may also invalidate an Undo rather
than making an unsafe recovery promise.

All receipt checks that protect insertion occur after obtaining the database's
write transaction. No receipt monitor is held while waiting to acquire a database
transaction. This avoids introducing an inverse receipt-lock/database-lock order
with backup or provider work. Normal scheduler synchronization remains in place.

## Bounded native flow to run on merged source

The existing
`NightDialAlarmFlowTest#duplicateIsAnOffDraftAndDeleteRequiresTheNamedCurrentAlarm`
retains its duplicate, changed-row deletion, cancel/recreation and dirty-save
assertions. Its private-database/silent-scheduler scene now additionally checks:

1. Named Undo and the off-restoration disclosure are visible after deletion.
2. Recreation retains the real Undo; a captured old-window control cannot restore.
3. One native Undo inserts the same alarm ID off, preserves base settings and the
   unrelated alarm, removes retired fields, and performs no scheduling.
4. A captured previous control cannot cause a duplicate restoration.
5. The restored-result state survives recreation.
6. Creating and deleting a different row under the reused ID invalidates the old
   receipt and leaves the row absent.
7. Advancing the injected monotonic clock expires a fresh receipt; the late native
   action cannot restore it.

New required original PNG names are:

- `night-dial-alarm-delete-undo-restored-review.png`
- `night-dial-alarm-delete-undone.png`
- `night-dial-alarm-delete-undone-recreated.png`
- `night-dial-alarm-delete-undo-stale.png`
- `night-dial-alarm-delete-undo-expired.png`

The scene restores its clock/factory and forgets its private receipt on cleanup.
Source/diff checks alone do not accept the feature. Root owns APK compilation,
native execution, original screenshot inspection and merged-source evidence.
No physical alarm or overnight behavior is claimed by these silent fixtures.
