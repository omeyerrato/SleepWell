# Travel reminder entrypoint follow-up

Reviewed source `6c325721efd52e6ff881faf2b4ad28c8cd17d47f`, whose production
`app/src/main` tree equals frozen `63cc05cd`. This targeted follow-up uses the
Android intent and permission security skills and the
[prior exported-component review](../EXPORTED-COMPONENT-SECURITY-REVIEW.md).
The [current source inventory](../verification/2026-10-28-travel-reminder-cleanup/source-audit.json)
pins every app-declared export and 17 follow-up source/workflow files.

All **17 app-declared exported definitions, filters and permissions are identical**
to the prior inventory. Dependency declarations are unchanged apart from app
version metadata; no fresh merged manifest/dependency implementation audit was
performed. Prior SDK inspection remains historical, with its recorded limits.

| Entry / reachable input | Actual guard and first sink | Finding / evidence limit |
| --- | --- | --- |
| Public navigation: Main/HomeLaunch, standard alarm/timer requests | Main clamps destinations and allowlists settings pages/groups. HomeLaunch and alarm/timer parsers construct fresh explicit internal destinations, not forwarded input Intents or URI grants. Draft review/ordinary Save or Start remains required. | Current navigation changes do not introduce a new command sink. HomeLaunch, alarm/timer request implementations are unchanged from the prior review; Main changes include additional finite navigation routes. No external-caller runtime test. |
| Incoming automation review | Strict parser, default-off configuration, visible/unlocked confirmation, request/recording/settings identities and one-shot private dispatch. The only Activity change since the original inventory is the already reviewed obscured-gesture hook. | Prior bounded native coverage remains separate. No new bypass established. |
| Alarm/record providers and summary export | Signature read/write permissions, scoped URI grants and per-UID consent revalidation; summary returns only its generated read-only document after explicit review. | Implementations unchanged; no new cross-UID grant/openFile experiment. No URI/signer policy weakened. |
| Platform configuration/binding entries | Health explanation has no data sink; permission-usage alias, JobScheduler, admin and accessibility bindings retain their OS permission boundaries. DND configuration ignores incoming rule identity and uses owned rule state. | Unchanged declarations/implementations; no platform binding or policy effects exercised. |
| Exported boot/time receivers, including TravelBootReceiver | Exact protected-action allowlists. Travel restoration loads saved private configuration and calls reconciliation; it does not accept caller-supplied trip data, start recording or arm light cues. | TravelBootReceiver unchanged. OS protected-broadcast enforcement is a declared boundary, not newly runtime-proven here. |
| New private TravelTrackingReminderReceiver | `exported=false`, explicit immutable broadcast PendingIntent. Exact action, bounded typed/versioned JSON and matching identifier. Worker checks saved pending identity, trip/cue revisions and actual saved civil window; consumes a durable receipt before any notification attempt. | No new externally exported entry. Malformed or stale delivery cannot select arbitrary URI, component, trip or recording. No adversarial broadcast delivery executed. |
| Tracking-reminder publication | Main-thread snapshot and final recheck require the same currently ACTIVE manual recording session/token; automatic, recurring and batch runs are excluded. Generation fences pause/stop/config changes. Current POST_NOTIFICATIONS, app/channel access, lateness and window end are rechecked. | Sink is a silent notification only. No recorder, torch, service-start or grant API is called from this path. A concrete cold-process cleanup issue was fixed below. |

The two reminder notification actions open a fresh explicit private TravelActivity
only; they carry no supplied request/URI/grant or automatic arm/start action.
Tracking reminders use request/notification ID 748, distinct from daylight ID
740. Scheduled deliveries include their complete request identity in both the
immutable PendingIntent identifier and its checked payload. Reconciliation
cancels the previous exact delivery token before scheduling the next saved one.

PendingIntent-producing source changes since the prior review are limited to
TrackingService, TravelReminders and the new TravelTrackingReminders. The
TravelReminders change adds tracking-reminder reconciliation; its daylight
PendingIntent remains explicit and immutable. Tracking's navigation/pause/stop
tokens remain explicit and immutable; ordinary pause/stop control the current
private service, while delayed cancellation checks its delay owner and sonar
resume requires a fresh same-run admission ticket. Travel-cue and pair-stop
actions additionally bind their current nonce/link and do not start a recording.
This does not assert that ordinary current-service controls have per-run replay
semantics. No hostile notification-listener/token-holder experiment was run.

The unchanged Google Sleep callback is intentionally mutable so Play services
can fill classification extras. It targets the explicit private receiver with
the app package and random subscription URI; the consumer requires the current
subscription owner, registration, clock/freshness/access and bounded event data.
It supplies evidence to an already reviewed bounded arm; it is not an exported
start command. No Google subscription or external provider was invoked here.

## Concrete cleanup fix

Before `381355cd`, cancellation of notification 748 depended on static
`visible=true`, set only by this process's publication path. Process loss resets
that flag while a previously posted notification can remain until dismissal or
timeout. Consequently a later disabled-trip reconciliation or recording shutdown
could skip cancellation and leave the stale manual-recording message until the
old window ended. This is a bounded interaction defect, not a demonstrated
external exploit or recording/torch admission flaw.

The process-survival scenario is a source/platform-contract inference. Android
documents notification removal by user action, ID cancellation or configured
timeout, and exposes ID cancellation independently of an app-memory flag.
See [notification removal](https://developer.android.com/develop/ui/compose/notifications/create-notification#remove_a_notification)
and [NotificationManager.cancel](https://developer.android.com/reference/android/app/NotificationManager#cancel(int)).
Actual Android process loss/NotificationManager persistence is not exercised.

Cleanup now always cancels that fixed owned ID. Failure records an actionable
ERROR and NOTICE without preventing recording-state invalidation; NOTICE also
survives reconciliation clearing an older ERROR. No `cancelAll`, new permission,
notification sound, recording start, torch action, data migration or grant change
is introduced. Existing revision/receipt/manual-run/permission gates remain.

Native-only `b4ce0769` extends the **same first existing** TravelCueFlowTest method
with a private notification port that retains an item without a prior local
show. It checks cleanup, a contained cancellation failure, visible diagnostics,
retry and exact private preference/status restoration. This models the lost
process cache boundary; it does not manufacture a real OS process-recovery claim.
The existing receipt/duplicate/revision/lateness/permission and no-light-admission
checks remain. Source and native files compiled with JDK21, release17, API36 and
cached project/AndroidX classes; no local Gradle or device run was performed.
Focused native evidence is retained in the
[verification folder](../verification/2026-10-28-travel-reminder-cleanup/).
The exact first method passed39.92s with18 individually inspected originals in
[CI40636332489](https://github.com/omeyerrato/SleepWell/actions/runs/40636332489).
Its fake-port cleanup checks completed; two earlier native handoff failures
remain recorded and provide no cleanup-pass claim.

## Separate editor behavior copy

`ee4deeff` changes only the editor Alarm on subtitle. A valid nonzero per-alarm
window says “May ring within the wake window”; Off retains the fixed local-time
copy, and unreadable smart settings request review. Window length is per alarm;
inherited movement sensitivity and duration-goal choices still determine
eligibility and are untouched. “May” avoids promising an early ring when those
tracking/goal gates are not met. This focused Travel method does not render or
accept the changed editor footer; its screenshot acceptance remains open.

Broad priority 3 remains open. This pass does not certify all components,
dependency internals, real OS caller/grant enforcement, hostile overlays,
physical behavior or full-app security. Frozen APK/metadata/release state and
the root worktree were not modified by this work.
