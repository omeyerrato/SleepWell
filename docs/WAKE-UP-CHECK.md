# Silent wake-up check

Board04's Still awake screen now has a distinct delay and confirmation window.
The alarm's existing awakeCheck minutes mean the delay after dismissal. At the first
prompt delivery attempt a persisted two-minute window starts. The prompt is a silent
notification opening a private dawn-themed Activity, with a real remaining-time
arc and explicit I'm awake action. It creates no playback, microphone, vibration,
torch or foreground service. Expiry requests the saved alarm through the existing
Scheduler only after claiming the same check once.

## Ownership and persistence

A versioned additive wakeCheckV1 record holds frozen alarm JSON, source occurrence,
UUID nonce, phase, delivery/error status and wall/elapsed/boot anchors. It commits
atomically with existing checkPending/check_alarm compatibility keys; existing
alarms, records and preferences are preserved. This is private app storage with
unpredictable request identifiers, not a claim of cryptographic record signing.
Every prompt/expiry PendingIntent is explicit, immutable and identified by nonce
and stage. Reopen, retry, duplicate deliveries and Activity recreation never reset
a prompt deadline. Confirmation consumes only its nonce before cancelling its
prompt/expiry/notification. Matching check Off or alarm deletion cancels its owned
check; editing unrelated alarms does not. A newly starting actual alarm occurrence
intentionally supersedes the old single pending check. Editing a positive delay
keeps the already dismissed occurrence's frozen delay and settings.

Same-boot wall-clock/time-zone changes leave elapsed deadlines unchanged. Reboot
reconciliation uses persisted wall anchors and reschedules remaining/overdue work
through exact alarm delivery; it does not ring directly from BootReceiver. The
silent prompt uses elapsed exact-idle scheduling. Expiry uses setAlarmClock because
Android throttles successive exact-idle events to at least nine minutes in Doze;
that expiry is the actual user-requested re-ring alarm. Its wall trigger is computed
from the remaining elapsed deadline and reconciled on clock changes. A wall
clock changed while powered off cannot be reconciled to a lost monotonic clock.

The old CHECK receiver had no stored due-time and immediately restarted its alarm.
An already scheduled old CHECK is migrated only at its genuine delivery when its saved alarm still matches the last dismissed alarm
and no alarm is ringing, then gets
a fresh visible two-minute window. If a device reboots before that legacy delivery,
its missing due-time cannot be recovered honestly. New checks have complete anchors. Legacy state has no original request nonce or
dismissal-time anchor; those historical facts cannot be recreated. A mismatched
legacy alarm is skipped with an error rather than assigned to a newer occurrence.

## Prerequisites, error handling and limits

Exact alarm access and enabled high-importance check notifications are checked
before scheduling/delivery. Missing access leaves a visible error and explicit
retry. A notification delivery failure retains the original nonce/deadline; retry
never buys a new window. If its never-delivered window expires it is cancelled
without an unseen surprise re-ring. Once delivered, scheduling failure can retry
the same expiry. Error states remain reviewable from Tonight.

AndroidX setSilent(true) guarantees silence and also suppresses peeking, regardless
of channel settings. Thus a silent heads-up popup is not promised. A permitted
full-screen intent may open the screen; otherwise the user taps the notification.
Android's full-screen, lock-screen, DND and OEM restrictions remain real platform
prerequisites. No permission dialog is forced while a check runs.

Expiry claims durable state before requesting the existing alarm service. A process
interruption between claim and external effect cannot provide exactly-once output;
the Activity reports an unverified restart and never blindly repeats it. Audio
hardware/service failure remains covered by the alarm's existing reliability
limits. A repeat-count limit and user-configurable confirmation duration are not
part of this change; the window is two minutes as in the supplied board.

## Validation strategy and evidence

Existing stack: Java Views, JUnit4, AndroidJUnitRunner, UIAutomator, injected ports,
private SQLite fixture stores and first-frame screenshot gates. This change keeps
that stack and does not migrate the application to Hilt/Compose or alter AGP.

WakeCheckEngineTest has 16 meaningful fake clock/storage/scheduler/delivery tests:
separate delay/window, duplicate/reopen, owned confirm, stale occurrence, claim-before-
effect, settings cancellation, clock changes, reboot, missing prerequisites, retry
deadline preservation, interrupted delivery recovery and ambiguous ring failure.
WakeCheckFlowTest has two inert native methods and twelve intended screenshots:
wake-check-{waiting,prompt,countdown,recreated,confirmed,reopened,storage-error,access-error,
delivery-error,retry,expired,stale}. It launches only WakeCheckActivity, uses private
preferences/database, fake notifications/scheduling/re-ring and asserts all original
private fixture records/alarms and global preferences remain unchanged.

Local full debug/test-APK/pure/lint validation passed in 1m27: 416 pure tests,
zero failures, lint zero errors / 86 warnings. Final legacy eligibility guard and
all 16 focused engine tests/native Java compilation passed in 12s; no full
post-guard suite is claimed. Native execution, original screenshot comparison and physical delivery/hardware
behavior are pending. Compilation alone does not certify them. No physical phone,
ADB, alarm, output, notification, clock-change or power tests are run by this task.

## Best practices and security alignment update

Area: private component and immutable PendingIntent ownership. Priority: high for
stale alarm output prevention. Scope: new WakeCheckState/Engine/Storage/Checks/
Receiver/Activity, manifest registration and narrow Scheduler, AlarmReceiver,
AlarmActivity.dismiss, BootReceiver, TonightScreen/MainActivity review hook. No
new permissions, dependencies, exported components or remote intents are added.
Both private component declarations use exported=false. Explicit immutable intents
carry only a canonical nonce; the receiver's action allowlist and durable current
state govern delivery. No caller-provided alarm JSON is launched. Exact unified
changes are available in the feature commit and the local build/verification
wake-check-security.diff artifact generated before commit.

Primary sources:
- https://sleep.urbandroid.org/docs/alarms/captcha.html
- https://developer.android.com/reference/android/app/AlarmManager
- https://developer.android.com/develop/ui/views/notifications/time-sensitive
- https://developer.android.com/develop/ui/compose/notifications/channels
- https://developer.android.com/reference/androidx/core/app/NotificationCompat.Builder#setSilent(boolean)

- https://developer.android.com/training/monitoring-device-state/doze-standby
