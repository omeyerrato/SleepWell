# Phone automation events

Screen 33 has real saved rules for tracking start, tracking end and completed alarm dismissal. Every new rule is off. Opening, configuring or saving a rule never sends a test. The separate **Send test event** action requires confirmation because it can trigger the destination's actions. Unsaved changes are not used for a test.

## Destinations

- **Tasker / app broadcast:** explicit receiving package, default `net.dinglisch.android.taskerm`. Create a Tasker **Event → System → Intent Received** profile with action `org.sleepwell.app.event.TRACKING_STARTED`, `org.sleepwell.app.event.TRACKING_STOPPED` or `org.sleepwell.app.event.ALARM_DISMISSED`. No component is selected, so active dynamically registered receivers can match it. Receiving app/profile installation, enablement and permissions are external prerequisites. Android accepting a broadcast is displayed as **Broadcast sent; receiver confirmation unavailable**; it does not prove a task executed. See [Tasker's Intent guide](https://tasker.joaoapps.com/userguide/en/intents.html).
- **Webhook:** HTTPS POST to the exact saved URL, JSON UTF-8, optional bearer token, `Idempotency-Key` header. Any 2xx is accepted; redirects are rejected without forwarding credentials. 408/429/5xx and connection failures can retry; other HTTP errors and TLS verification failures stop. Each connection/read timeout is 8 seconds; platform DNS resolution can take longer. The receiving endpoint must implement its own action and use the event ID to deduplicate.
- **MQTT:** `ssl://broker:8883`, explicit publish topic and optional username/password, system-trusted server certificates and hostname verification. MQTT 3.1.1 clean session, one non-retained QoS 1 publish, matching PUBACK required, then disconnect. No subscription or incoming device control. Broker access, topic authorization and an actuator subscriber are external prerequisites. PUBACK means broker acceptance, not physical actuator success. See the [OASIS MQTT 3.1.1 specification](https://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html).

## Minimal event schema

```json
{"schema":1,"event_id":"64 lowercase hex characters","event":"tracking_started","occurred_at":1792800000000,"test":false}
```

Other values are `tracking_stopped` and `alarm_dismissed`. `occurred_at` is Unix wall-clock milliseconds. Broadcasts provide the same five keys as extras. No notes, labels, sound recordings, sleep metrics, record IDs, contacts or location are included. Event ID is stable for the same kind/occurrence. A confirmed test uses a fresh ID and `test:true` and only targets the chosen saved enabled rule.

## Persistence and off behavior

Rules and pending events use a separate private `automations-v1.db`; the sleep database and portable sleep JSON/CSV/audio backups are untouched. The complete destination/credential configuration is AES-GCM encrypted using an Android Keystore key. App backup is disabled. Rules and credentials must be re-entered on another installation; they are intentionally excluded from portable sleep exports.

At most 20 rules and 100 pending deliveries are allowed. Duplicate event/rule/revision combinations are suppressed. Completed rows retain a bounded recent history for deduplication. Saving, toggling or deleting a rule invalidates and removes its queued attempts; enabling applies only to new events. An already in-flight broadcast or network request cannot be recalled. The receiving service can also execute an action after its request completes.

Each delivery permits at most six attempts over 24 hours: delays 1 min, 5 min, 15 min, 1 hour and 3 hours. Deadline/clock reversal checks prevent dispatching stale due work. Permanent errors stay visible until another event or rule edit updates the status. A full queue reports the dropped event rather than growing indefinitely. Errors use fixed credential-free messages.

Android JobScheduler persists pending job admission across reboot; the private queue is authoritative. Jobs process at most ten attempts or 90 seconds of loop time, with bounded transport operations. OS quotas, Doze, OEM restrictions, force-stop, connectivity and DNS can delay delivery. Reopening Automations reconciles unscheduled work. See [JobService](https://developer.android.com/reference/android/app/job/JobService) and [persisted job scheduling](https://developer.android.com/reference/android/app/job/JobInfo.Builder#setPersisted(boolean)).

Delivery is **at least once**, not exactly once. Process death after an accepted remote request but before local acknowledgement can cause a repeated request; payload ID is retained. Event enqueueing is asynchronous so alarm/sensor threads are not blocked by database/network work. A process death before the queued event is durably inserted can lose that event. There is no incoming automation receiver and no remote alarm control.

## Runtime event boundaries

Movement recording emits start after its record is created. Contactless recording emits start on the first active state. Pause/resume produces no extra start/end pair. A recording closing emits end; an automatic wait cancelled before recording produces no event. An interrupted closing can emit end, so the event does not imply a complete report. Alarm dismissal is emitted only after a current positive-ID alarm successfully stops with its complete challenge routine. Snooze, stale callbacks, negative-ID quiet tests and the debug emergency stop do not emit dismissal.

## Verification scope

`AutomationPolicyTest` (6), `MqttPublishTest` (6), `WebhookPostTest` (4) exercise event privacy/identity, validation, retry limits, lifecycle pairs, real MQTT wire bytes/acknowledgements, HTTP payload/headers/status/redirect/TLS policy using fake streams/connections. `AutomationStorageTest` (10 native methods) uses UUID databases, a fake vault and fake transport: reopen, no plain-text config, dedup, off/edit/delete, stale save, vault failure, retries/expiry, queue bound, targeted tests and disable-during-attempt fencing.

These checks do not prove actual Tasker execution, real HTTPS/MQTT server interoperability, real JobScheduler delivery/Doze/reboot behavior or actuator effects. No personal phone, alarm, microphone, playback or external endpoint is used by these tests. Native execution results must be recorded separately; authored/compiled tests are not a runtime pass.
