# Travel occurrence admission follow-up — native acceptance pending

2026-11-01, source parent `1c1fe0c1`; follows 31C source `e86340d3`, native
journey `cc2ad3da` and idle-check prerequisite `1fc310cc`.

Independent source review found two concrete omissions. The final SQLite admission
rechecked the full alarm and trip proposal but did not retain the travel preference
lock through persistence. A completing plan writer could change the reviewed trip
between that check and alarm Save. Also, linked weekly Nap alarms passed the
recurring selector, while ordinary individual editing deliberately detaches their
`weeklyNapOwner`; Apply once must not silently perform that detachment.

`TripAlarmShift.commit` now owns the final travel Save and acquires locks in this
order: **Scheduler → TravelPlans.LOCK → existing AlarmSkips SQLite transaction**.
It keeps the shared trip lock through admission, commit and the existing scheduling
boundary. The ordinary non-travel Save path is unchanged. `TripSleepStore.LOCK` is
the same object, and `TripSleepStore.save` now takes it defensively, including
callers that do not already hold the runtime wrapper's lock. The complete saved
alarm, trip/sleep-plan JSON, occurrence marker, source/target and zone checks still
run inside the final mutation owner. No new scheduling path or weaker check is
introduced; the editor's explicit Save remains the only mutation action.

The scoped lock-order audit inspected all current shared travel-lock holders:

| Owner | Existing guarded work / order |
| --- | --- |
| TravelPlans.Repository.save | Trip/config/cue preference commit under TravelPlans.LOCK; no Scheduler acquisition |
| TripSleepStore.save; TripSleepRuntime.save/reconcile/receive | Same shared lock; private plan/delivery state and Android reminder operations, without Scheduler.class |
| TravelReminders.apply/receive | Shared lock and reminder port operations; no Scheduler.class |
| TravelTrackingReminders.apply/receive/publish | Shared lock, private reminder state and asynchronous main posting; recording admission reads a volatile TrackingService snapshot |
| TravelCueStore.begin/save | Shared lock and cue state; live-run inspection reads the current volatile runtime snapshot |
| AlarmSkips mutation and Scheduler/AlarmScheduling path | Scheduler precedes its SQLite mutation/scheduling; these paths do not acquire a travel lock in reverse order |

There was no production TravelPlans.LOCK → Scheduler.class path in this inspected
call graph. The new travel commit obtains Scheduler first, never waits for it while
holding TravelLock, and reenters the existing same-thread Scheduler owner normally.
Android AlarmManager operations in reminder owners are not that Java monitor.
Read-only preview checks can still race, because final admission now fences the
actual mutation; an old preview never becomes permission to skip the final check.

The selector explicitly omits nonempty or malformed weekly Nap ownership.
`TripAlarmShift.prepare`, `check` and `checkDraft` defensively reject it too,
including a saved alarm that changed to a linked owner after selection. The
message directs users to an ordinary recurring alarm and Nap profiles. These rows,
profile ownership and recurrence are preserved; no implicit detach is performed.

The [focused native plan](TRAVEL-OCCURRENCE-ADMISSION-NATIVE-PLAN.json) extends the
same existing TripOccurrence method. It keeps all 15 original frames and assertions,
adds one actual linked-exclusion frame, and requires an admission proof receipt.
Valid ownership-marker and malformed-metadata alarm fixtures must be absent from
the selector and rejected by prepare/check/checkDraft, with exact JSON retained.
The existing explicit stale-plan result remains asserted before a valid Save.

For the race, the existing Records.Factory returns a private Store subclass at the
real SQLite save seam. After final admission, that seam asserts both locks are
owned and starts a competing private plan writer. Its monitor attempt must reach
BLOCKED while the alarm commit is pending. After release it must read the committed
override before deliberately saving its own new plan revision. The test then
verifies the preserved ordinary recurrence and both linked fixture rows. The
worker is bounded and drained before restoring factories/deleting fixture data.
No production test flag, new clock port, actual reminder, provider, phone or output
is used. This is one meaningful native journey, not a unit campaign.

Only diff, finite capture-plan consistency and source/lock-order inspection ran
locally. No build, emulator, native execution or hosted CI ran here. Root owns the
single focused rerun and actual original-PNG inspection. Earlier failed receipts
and the historical Night state register remain unchanged.
