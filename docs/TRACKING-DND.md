# Optional tracking Do Not Disturb (F065)

This opt-in uses a separate, app-owned Android `AutomaticZenRule`, not global interruption-filter or notification-policy changes. The preference is off by default and stored separately from records/backups. Saving, reopening, restoring sleep data, application launch, boot and an armed-but-not-recording automatic start do not turn on the rule.

## Flow and permissions

Open **Tracking Do Not Disturb** from tracking setup or the live recording screen. Open Android's DND access and exact-alarm access pages explicitly, then save the preference. The next accepted tracking session captures the preference immutably. Turning it on halfway through a recording does not escalate that recording; turning it off removes the held rule. Pause/end remove the held rule; resume may reapply only that recording's original opt-in while the saved preference still allows it. Policy/expiry access is checked again at activation and renewal.

The owned rule requests priority mode and permits all calls, repeat callers, alarms, media and system sounds. Ordinary messages, conversations, reminders and calendar events are restricted. Other Android modes and rules can still restrict sounds: this feature cannot guarantee call/alarm audibility. Airplane mode, networking, other applications' rules and the user's global policy are not modified.

The settings Activity is exported only so Android can open the rule's configuration page. It ignores all incoming extras. System receivers and explicit expiry receiver are private; their operations never activate a rule.

## Ownership and cleanup

Each accepted recording has a fresh run identity. Rule creation starts disabled, with an app condition URI and configuration component. A write-ahead marker and durable pending owner precede activation. An elapsed-realtime exact expiry is scheduled before requesting the active condition; the live run is checked again before that request. Only the exact saved rule/condition pair can be removed. Old run stop callbacks and old expiry nonce/deadline callbacks cannot remove a replacement.

A single application-context worker serializes rule APIs, preference commits, startup recovery, activation, renewal and cleanup. The UI never waits for that worker and receives immutable busy/result snapshots. Startup recovery is queued before later admissions; it also preserves a same-boot, unexpired lease whose exact recording is currently active. It never enables a rule. A touched marker allows recovery to find a disabled orphan left between Android creation and journal persistence.

The requested expiry is **15 minutes**, renewed once per minute only while recording remains active, with a **12-hour** hard cap. Renewal schedules a distinct new nonce/deadline PendingIntent, commits it, then cancels the old one; it never reasserts an active condition after user intervention. An unchanged hard-cap deadline is retained rather than canceling its own safeguard. Boot/package recovery removes owned stale rules; manual rule removal/disable is not counteracted by a heartbeat.

The expiry safeguard requires exact-alarm access. It is an additional cleanup path, **not a guarantee under force-stop, revoked permissions, system/provider failure or device shutdown**. Android rule operations and the last live-check/ON call are not one atomic OS transaction. Failure to remove a rule retains its journal and a visible cleanup error; the user can retry or review SleepWell's rule in Android settings. No prior global state is guessed/restored. The system, not SleepWell, combines all applications' active DND rules.

## Validation and limitations

- `TrackingDndOwnerTest`: 21 pure cases cover default-off, prerequisites, ordering, stale starts/stops/expiries, renewal without ON, hard limits, write failures, failure retry, user disable and process/reboot recovery.
- `TrackingDndStorageTest`: four isolated Android cases exercise real private SharedPreferences journal/reopen, stale expiry, failure persistence/retry, malformed ownership, and the actual `ZenPolicy` value object. Rule/expiry APIs are fake; constructing a policy value does not apply it.
- `TrackingDndFlowTest`: two inert native journeys / seven screenshots: default, prerequisites, saved, ready, owned, cleanup-error and cleaned. They use the actual screen/controls/recreation and fake permission launch/rule/expiry ports. Every original main-database row, main preference and original DND preference is checked unchanged.

Local validation: 21 owner tests passed (0 failures/errors); debug APK, instrumentation APK and lint passed (0 errors / 112 existing warnings, no warnings in the new DND classes). A final source-only UTF-8 text restoration preserved existing Tracking labels and made unreadable owner status explicit; root integration compilation remains required for that copy-only delta.

Build/test compilation is not actual native execution. Actual Android DND permission grant, system rule activation, call/alarm interaction, system broadcast/expiry delivery and OEM behavior remain unverified until an explicitly authorized real-system test. No physical phone effects are part of this change.

## Primary sources

- [NotificationManager](https://developer.android.com/reference/android/app/NotificationManager): explicit owner-scoped rule state, rule management, status broadcasts; Android 15 target behavior for global policy/filter APIs.
- [AutomaticZenRule](https://developer.android.com/reference/android/app/AutomaticZenRule): configuration Activity, condition identity, initially disabled rule and per-rule policy.
- [ZenPolicy.Builder](https://developer.android.com/reference/android/service/notification/ZenPolicy.Builder): explicit calls, alarms, media and other category values.
- [AlarmManager](https://developer.android.com/reference/android/app/AlarmManager): elapsed-realtime exact/idle scheduling and access requirements. Requested scheduling is subject to platform delivery constraints.
