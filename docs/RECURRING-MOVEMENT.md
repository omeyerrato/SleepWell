# Recurring movement-only tracking

Reach this opt-in screen from Automatic tracking → Repeat nightly. Existing tonight-only modes and explicit microphone consent remain unchanged. The schedule stays private to this installation; portable backups do not enable it on another phone.

## Behavior

- Save a local nightly start time. “At this time” starts timed movement tracking. “After settling” begins the existing bounded wait for fifteen minutes of still, screen-off phone activity. This estimates phone inactivity; it cannot establish that a person is asleep or identify sleep stages.
- “Only when charging” checks BatteryManager.isCharging at the due boundary. If unplugged then, skip that occurrence. Plugging in later does not start a service. Unplugging after a successful boundary check does not stop the recording.
- Existing four-hour waiting and twelve-hour recording limits apply. Cancel tonight through the ongoing tracking notification. Disable stops only the schedule’s owned active run. Editing an enabled schedule preserves an admitted current wait/recording while changing future occurrences; a queued, unadmitted request is revoked.
- The service validates a single-use saved token and revision before foreground promotion, sensors, wake locks or storage. It rebuilds the ARM request from persisted metadata, forcing sound/clip flags false. Recurring capture also omits light readings and never starts the sleep-sound service, regardless of legacy preferences.
- One exact RTC_WAKEUP setExactAndAllowWhileIdle occurrence is scheduled at a time. Recompute local calendar time after delivery, reboot, clock/time-zone changes, package replacement, exact-access grant or reopening the app/settings. DST gaps use java.time’s next valid offset; overlaps use its earlier offset. A claimed date is not replayed after clock rollback. A stale time-zone occurrence is rescheduled without starting.
- Delivery has a five-minute grace period. No missed starts are backfilled. Occupied alarms/tracking, missing permissions/sensor, or an unmet charging condition skip that occurrence. Scheduling and last-run status remain separate. If configuration saves but scheduling fails, stay on screen and explicitly report that only settings were saved.

## Platform and ownership limits

Android 12+ restricts background foreground-service starts. This implementation uses the exact-alarm exemption for the explicit timed action the user saved, with live canScheduleExactAlarms checks and catches for races. There is no inexact fallback assuming an exemption. Revoking exact access can stop the app and cancel future alarms; grant/open reconciliation restores future schedules only. The existing specialUse type describes overnight movement tracking and has no while-in-use runtime permission prerequisite. Google Play distribution still requires review of the declared subtype.

The microphone while-in-use rule is stricter than the generic FGS exemption. This path never requests microphone foreground type or creates NoiseRecorder. Sound analysis requires an eligible explicit foreground flow. The background timer never opens an Activity.

Production entries enforce the main looper before preference access. Activity saves, broadcast delivery and service admission therefore cannot interleave disable between validation and service ownership/effects. Revisions and opaque occurrence tokens fence stale requests, disable/re-enable ABA, duplicate starts and manual takeover. The skipped-status update for an occupied manual owner is best effort on storage failure. An idle service entry clears previous per-instance schedule ownership before admitting another request. Visible/system reconciliation retires dead-process run claims without replaying them; a currently owned live run is retained. Boot discards prior-process claims and schedules future occurrences only.

The occurrence receiver is private, with an explicit immutable PendingIntent. The exported reconciliation receiver accepts protected system actions only and rejects null/unrelated actions before any port access. Reconciliation schedules future alarms and never starts tracking directly from boot/time/permission broadcasts.

Force-stop, power-off, OEM battery restrictions, permission/channel blocking, interrupted process delivery, sensor failure and platform scheduling restrictions can prevent a night. No claim of infallible delivery is made. Notification permission and the unblocked tracking channel are required for visible Cancel; LOW importance is sufficient. Sensor registration failure stops a recurring attempt with an error. Actual hardware-specific background delivery and overnight capture remain unverified.

## Validation scope

RecurringNightTest has nine JVM cases for local dates/no backfill, DST intervals/gaps, repeated dates/clock rollback, delivery grace, invalid minutes and notification/channel visibility. RecurringTrackingTest has twelve emulator-only methods using isolated real private preferences and fake scheduler/launcher/stop ports. They cover admission normalization, stale/ABA claims, active-run preservation, disable ownership, manual takeover, charging/occupancy skips, permission/sensor rechecks, late arrival, schedule/launch failure, time-zone changes, invalid broadcasts and off-main production rejection.

RecurringTrackingFlowTest has two emulator-only methods and thirteen PNGs for save/cancel, time picker/draft recreation, reopening, disable, unavailable permissions and visible scheduling failure. These fixtures start no service, microphone, speaker, alarm, sensor, wake lock or actual permission/settings Activity. Native methods and screenshots are compiled but await the parent’s disposable-emulator CI gate and strict review against board 10. Fake ports do not prove AlarmManager/FGS delivery.

## Primary references

- [Background FGS restrictions](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start)
- [FGS types: specialUse and microphone](https://developer.android.com/develop/background-work/services/fgs/service-types)
- [Exact alarm scheduling and access changes](https://developer.android.com/develop/background-work/services/alarms)
- [BatteryManager.isCharging](https://developer.android.com/reference/android/os/BatteryManager#isCharging())
