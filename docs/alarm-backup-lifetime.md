# Backup sound bursts and optional ringing timeout

Ordinary alarms now offer **Backup sound duration**, **Additional backup bursts**, **Between backup bursts**, and **Stop ringing after** in Snooze & backup. Defaults remain continuous backup sound after the existing escalation delay, no additional bursts, and **Never** automatically stop. No new setting requests permissions, schedules output, or starts a player when saved.

Optional sound bursts last 30–300 seconds, with 0–5 additional bursts separated by 1–15 minutes of the primary alarm. The first burst follows the alarm's configured escalation delay. Primary sound returns to its configured target between and after bursts; vibration, flashlight and sunrise retain their existing primary-alarm behavior. Finite burst controls never dismiss tasks or imply that the person woke up.

The phase is calculated from the occurrence's saved monotonic start, so service restart does not replay the first burst or reset its duration. The actual owned BACKUP-delivery flag is still required. Late delivery/blocked execution can skip elapsed windows; the engine does not fabricate catch-up bursts. Native player preparation, Android volume handling and main-loop scheduling are asynchronous, so these are policy deadlines, not a hard real-time acoustic guarantee. Hardware timing has not been measured.

The optional 1–120 minute timeout stops the matching occurrence's ringing without completing its challenges, creating an awake check, or emitting the successful-dismiss automation event. It leaves future schedules and challenge progress alone. A short inline notice states that distinction. Timeout takes priority over a burst, including an already expired service restart; no alarm notification, audio, wake lock or lamp owner is then allocated. Cancellation of the matching pending backup is best effort; clearing the ringing owner also fences a later queued backup. The service uses its start ID when stopping, preserving a newer queued service request. Existing lamp-close semantics remain unchanged (it stops further lamp updates rather than introducing a new light-off command).

`AlarmTiming` is immutable and the optional strict `timing` JSON retains invalid metadata for review instead of silently enabling a timeout. Missing legacy metadata remains exact default and emits no new field. Naps do not expose or accept this ordinary-alarm policy. Reusable defaults transfer valid timing choices. Portable alarm backup validation rejects malformed timing; restoration remains inactive. Quiet development clones explicitly clear timing, and the policy also ignores escalation/expiry for nonpositive test IDs. No schema/backup-format change.

The playback controller cancels a pending obsolete sound before changing mode; returning to primary restores the configured volume lease rather than leaving the backup's 100% stream target stuck. Queued stream changes carry a revision fence. Existing user volume overrides and saved baseline restoration remain protected. Maximum supported finite sequence has six bursts and five gaps, bounded to 105 minutes after escalation; the UI's maximum escalation delay adds 15 minutes. **Never** can still leave the primary alarm ringing until normal completion.

## Validation contract

- `AlarmLifetimeTest`: nine pure policy/fake-owner cases: exact boundaries, late/restart behavior, count bounds, quiet behavior, stale ownership, timeout once, immutability and invalid saved metadata.
- `AlarmPlaybackTest`: three added fake-layer cases for primary return, obsolete backup preparation, and direct/quiet restart; all existing cases remain.
- `AlarmVolumeLeaseTest`: three added fake-stream/journal cases for repeated transitions, user overrides and refused native downgrade compensation; all existing cases remain.
- `AlarmTimingStorageTest`: three real isolated JSON/SQLite/template/portable methods with fake scheduler.
- `AlarmTimingFlowTest`: one native configuration method: choose/cancel, recreate, explicit save/reopen, continuous backup and Never opt-out. Seven originals: `alarm-timing-{initial,selector,configured,recreated,reopened,continuous,never}.png`.

Local validation: debug APK, native-test APK and lint tasks passed in 4m51s; 44 focused tests across lifetime/playback/volume/defaults passed with no failures or skips. Lint reported 0 errors and 107 warnings. Native configuration/storage methods are compiled, not executed locally.

No AlarmActivity/AlarmService instance, physical device, audio, vibration, flashlight, wake lock or external lamp action is executed by these tests. Native compilation and parent-owned emulator settings execution are separate from unverified physical delivery/output behavior.

Primary lifecycle reference: [Android Service stopSelfResult/start-ID behavior](https://developer.android.com/reference/android/app/Service#stopSelfResult(int)).
