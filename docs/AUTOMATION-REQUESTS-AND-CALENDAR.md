# Tracking requests, lifecycle events and future-night calendar export

Source86d0d1e passes production `:app:assembleDebug` in1m14s. This is build-only
evidence. No new unit/native/lint/screenshot batches, automation recipients,
calendar providers, phone or physical outputs were exercised. Runtime behavior,
process/OEM delivery and visual acceptance remain open.

## Incoming requests

Automations → Incoming tracking requests is a separate saved access preference,
off by default. Enabling it only permits a request review screen. It never grants
unattended background control, remote microphone access or alarm dismissal.

Contract:

```text
Action: org.sleepwell.app.action.TRACKING_REQUEST
Package: org.sleepwell.app
Activity: org.sleepwell.app.IncomingAutomationActivity
Category: android.intent.category.DEFAULT (optional)
Extras, exactly:
  version: Integer 1
  command: String START | PAUSE | RESUME | STOP
  request_id: String canonical UUID, unique for this request
```

Use a normal Activity launch, or launch for result to receive status. NEW_TASK is
accepted for a non-Activity caller; caller task-rewriting/URI-grant flags, data,
MIME, selectors, ClipData, nested/unknown extras and wrong types are rejected.
The Activity is intentionally standard, not singleTop or showWhenLocked. A new
request never silently replaces an already confirmed operation. Caller identity
is displayed only when supplied by Android; arbitrary caller labels are not trusted.

| Request | Resulting action |
| --- | --- |
| START | Explicit review opens the existing normal tracking setup. Sensor choices, permissions and its final Start are still required; result is `staged`, not `applied`. |
| PAUSE | Explicit unlocked/focused confirmation targets the exact reviewed current recording. Applied only after its target phase is observed. |
| RESUME | Same-run confirmation uses the existing control builder, including contactless SonarEntry prerequisites. It cannot import arbitrary capture flags. |
| STOP | Same-run confirmation waits for observed full owned cleanup; a pending/error result is not a saved-night guarantee. |

Review expires after two minutes. In-process service-dispatch grants expire after
ten seconds, are single-use and recheck recording ID/start/sensor/phase, admission
generation, visible ownership, alarm state and saved access revision at actual
service delivery. The exported Activity cannot supply a nested service Intent.

A private bounded journal retains prepared request IDs. Repeated IDs, ambiguous
dispatch and process recreation do not automatically replay controls. After
`pending`/`interrupted`, inspect tracking before requesting another operation.
Turning access off invalidates queued unaccepted grants; it does not undo an
already accepted operation.

Result extras are primitive `version`, `command`, `request_id`, `status` only.
RESULT_OK covers `staged`, observed `applied`, or `already`; other outcomes include
`duplicate`, `rejected`, `locked`, `stale`, `disabled`, `permission_required`,
`not_visible`, `alarm_active`, `failed`, `pending`, `needs_attention`, `interrupted`
and `cancelled`. A result is not sensor-release proof beyond the existing tracking
cleanup model. This is SleepWell's own contract, not a vendor Intent alias.

## Outbound event catalog

Existing opt-in Tasker/app broadcasts, HTTPS POST and TLS MQTT rules now offer ten
types. Old enum names, rule data and schema1 remain valid.

| Kind | Emission boundary |
| --- | --- |
| TRACKING_STARTED | Existing saved recording entered capture |
| TRACKING_STOPPED | Existing recording owner ended capture; not a complete-night certification |
| TRACKING_PAUSED | Actual persisted pause row, including contactless ordered storage |
| TRACKING_RESUMED | Actual persisted pause-row closure |
| ALARM_STARTED | Owned real foreground alarm session admitted; sound output is not established |
| ALARM_SNOOZED | Acknowledged snooze scheduling receipt and successful owned alarm stop |
| ALARM_DISMISSED | Existing completed wake routine/dismissal |
| WAKE_CHECK_CONFIRMED | Durable explicit confirmed check nonce/phase |
| WAKE_CHECK_MISSED | Delivered check's confirmation deadline durably claimed as expired; re-ring success is separate |
| WIND_DOWN_REMINDER_POSTED | Real fixed/smart reminder submitted to Android notifications; seeing/hearing it is separate |

Source identities include actual transition/occurrence/check/series identities, so
the same committed transition has a stable event ID. The payload remains type,
time, schema, test flag and hash ID, without notes, audio, names, record IDs or
location. Existing destination queue uniqueness, encrypted credentials, bounded
attempts and destination revision invalidation remain. Editing/deleting a rule
cancels future queued attempts; an in-flight external request cannot be recalled.

The source transition and async outbox handoff are not a crash-atomic transaction.
Process death can lose an event before durable enqueue. Transport retries can
deliver more than once; receivers must deduplicate `event_id`. Android can delay
jobs/broadcasts. Native decoder/player-start events, inferred REM/deep/light,
apnea and unsupported recognition events are not offered.

## Future-night calendar export

Calendar rules → Save future nights opens separate consent. Calendar read/write
permission lists a writable destination and verifies local receipts. Granting
permissions or opening this page writes no event. Enable requires explicit
confirmation of the destination and future fields.

Only a successful production tracked night **started after saved consent** can
queue. Existing history, imports/restores, manual periods, interrupted/rejected
false starts and records subsequently edited are not automatically exported.
Both ordinary movement and contactless successful-save paths use the same hook.
App settings/profile restore cannot import this live authority.

Each event contains fixed title `Sleep period`, original start/end and recorded
time zone, with no note, tags, rating, raw signals or audio. This is a recording
period, including pauses/awake time, not a claim of measured sleep duration.
Calendar app controls server sync and calendar sharing.

The private32-entry queue is revision-fenced and persisted before admitting work.
Only verified/cancelled old queue entries may be removed for capacity; unresolved
entries are retained and a full queue reports that the new night was kept locally
but not queued. Job scheduling is deferrable and can fail/be delayed. A90s app
deadline cancels authority; a provider call already underway may still complete.
There is no blocking wait in tracking for calendar I/O.

Automatic requests share `CalendarEventWrites` with direct manual export: same
period/calendar identity, durable PREPARED/INSERTING/VERIFIED/UNCERTAIN receipts,
observable ownership marker, current source/destination checks and verified local
readback. Outcome uncertainty goes to receipt review and is not blindly retried.
Source and provider cannot share one atomic transaction. A final-check/insert
race and delayed/unobservable provider state remain limitations.

Off or changed consent invalidates queued/in-flight ownership; provider work
already admitted may finish. History erase/replacement invalidates pending queue
ownership and clears its local queue receipts; previously exported calendar events
and the separate direct-export receipt journal remain. The app never edits/deletes
external calendar events or treats local insertion as remote sync. Reboot/process
recovery never replays a RUNNING/uncertain insertion. Future successful nights can
queue independently under still-valid consent.

Primary references: [vendor Intent scope](https://sleep.urbandroid.org/docs/devs/intent_api.html),
[Calendar Provider](https://developer.android.com/identity/providers/calendar-provider),
[JobScheduler](https://developer.android.com/reference/android/app/job/JobScheduler),
[Android broadcasts](https://developer.android.com/develop/background-work/background-tasks/broadcasts).
