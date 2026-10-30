# Original alarm-management provider, version 1

SleepWell exposes its own optional contract at `content://org.sleepwell.app.alarms/shared/v1/alarms`. It is not a vendor authority or drop-in compatibility implementation. The separate record provider remains read-only.

## Consent and callers

No foreign app has access by default. In **Alarm management access**, enter an installed package name, explicitly verify its UID and current APK signer SHA-256 values, acknowledge that supported edits can enable alarms that ring later, then choose **Allow alarm management**. This consent permits supported CRUD without a review dialog for each request. Allowing access itself never creates, schedules or changes an alarm. The local preview only reads existing summaries.

Android signature permissions protect read/write entry points; explicit scoped read/write prefix URI grants let a selected third-party package pass that boundary. Every foreign request also requires SleepWell’s independent private active grant, exact current package/UID/current signers, isolated UID and current boot. Signature permission or a forwarded URI alone is insufficient. Shared UIDs, unverifiable packages and system identities are rejected. Android package visibility may prevent verification; this flow does not request blanket installed-package visibility or launch the selected app.

**Revoke**, **Revoke all**, reboot, SleepWell update and history replacement/erasure remove authority. Identity changes deny new calls without automatically trusting rotated signers. Regrant always requires explicit verification and Allow. Grants and request receipts are private local authority, excluded from portable settings, record metadata and audio exports. Revocation cannot retract already-returned cursors/copies or undo already-saved alarms. A write already executing under the grant lock may finish before Revoke acquires that lock.

## Reading

Collection query parameters are `limit` (1–25, default 10) and `before_id` (positive decimal, exclusive). Individual paths append one positive alarm ID and accept no query parameters. Order is fixed `_id DESC`; cursor extras provide `contract_version=1`, `has_more` and, when applicable, `next_before_id`. Paging is bounded, not a snapshot across multiple calls: changed/new rows can require a fresh first page.

Whitelist projection columns:

| Column | Meaning |
| --- | --- |
| `_id` | Actual saved alarm ID |
| `revision` | Opaque content/occurrence ETag for a later conditional write |
| `hour`, `minute` | Configured local clock, not a computed solar or nap deadline |
| `days_mask` | Monday bit 0 through Sunday bit 6; zero is ordinary one-shot mode |
| `label` | Saved label, bounded to 80 characters or null when outside this scope |
| `enabled`, `vibrate` | Integer 0/1 configured values |
| `kind` | `clock`, `nap`, `sunrise`, `dated`, `custom_repeat` or `next_override` |
| `writable`, `write_issue` | Whether this bounded API currently accepts mutation, and reason if not |
| `next_deadline_ms` | Future persisted ordinary scheduling deadline, or null; not a delivery guarantee or live snooze projection |
| `scheduling_status` | `no_saved_error`, `needs_attention` or `unknown` |

The default/null projection remains the original 13 columns above, in that order. Explicit projections may additionally request these configured values:

| Optional column | Meaning |
| --- | --- |
| `snooze_minutes` | Snooze duration, integer 1–60; not a pending snooze deadline or remaining allowance |
| `smart_window_minutes` | Early-wake window, integer 0–60; zero disables smart wake-up |
| `smart_sensitivity` | -1 follows the tracking profile (including calibration), 0 Low, 1 Medium, 2 High |
| `smart_respect_goal` | -1 follows the global duration-goal setting, 0 Off, 1 On |
| `volume_percent` | Configured alarm volume, integer 10–100; extra-loud mode can override the runtime target |
| `ramp_seconds` | Configured gentle volume-ramp duration, integer 0–1440; zero has no ramp |
| `sound_delay_seconds` | Configured delay before primary sound starts, integer 0–1440 |
| `vibration_delay_seconds` | Configured vibration-start delay, integer 0–1440; `vibrate` separately controls whether vibration is enabled |
| `screen_sunrise` | Integer 0/1 configured screen-brightening choice; unrelated to a solar-time schedule |
| `flashlight` | Integer 0/1 configured flashlight wake-up choice |
| `extra_loud` | Integer 0/1 configured extra-loud alarm choice |

An absent output scalar uses the same valid legacy default as SleepWell’s alarm model. A malformed or out-of-range saved output scalar is null; it is not presented as a clamped runtime value. Existing malformed metadata remains subject to the unchanged nonwritable/round-trip rules. These columns report configuration, not current brightness, torch availability, measured loudness or whether an output has run.

An out-of-range configured duration/window is null; invalid saved smart-wake metadata produces null sensitivity/goal values and remains nonwritable. No inherited value is invented for corrupt metadata. Absent valid smart-wake metadata reports -1 for both inherited choices. These are configured choices, not inferred sleep stages, current movement or proof that a duration goal has been met. Explicit projections still reject unknown or duplicate columns.

Unrepresentable/oversized rows retain their real ID with `writable=0`, a reason and unknown fields; no sample clock is invented. The ETag hashes exact stored JSON, saved deadline, consumed occurrence and boot identity. It is a content token, not a monotonic version or permanent incarnation ID. Re-query before each edit; do not infer that matching content proves unchanged history. No raw JSON, challenge codes, PINs, tone URIs, notes, recordings, credentials or live run tokens are exposed. Arbitrary SQL, selection, sort orders and nonempty query bundles are rejected.

## Writing

Use single-row ContentResolver operations only. Create at the collection URI with `?request_id=<canonical UUID>`. Update/delete at `/{id}?request_id=<canonical UUID>&if_match=<current revision>`. A create requires `hour` and `minute`. All calls must use a fresh reviewed configuration; setting `enabled=true` may schedule a future alarm using SleepWell’s real runtime.

Supported ContentValues are exactly:

| Field | Accepted value |
| --- | --- |
| `hour` | Integer 0–23 |
| `minute` | Integer 0–59 |
| `days_mask` | Integer 0–127 |
| `label` | Nonblank plain text, at most 80 characters, no control/format/line separator characters |
| `enabled`, `vibrate` | Boolean or Integer 0/1 |
| `snooze_minutes` | Optional integer 1–60; strings, floats, booleans and null are rejected |
| `smart_window_minutes` | Optional integer 0–60; 0 disables early waking |
| `smart_sensitivity` | Optional integer -1 (inherit), 0 (Low), 1 (Medium), 2 (High) |
| `smart_respect_goal` | Optional integer -1 (inherit), 0 (Off), 1 (On) |
| `volume_percent` | Optional integer 10–100 |
| `ramp_seconds` | Optional integer 0–1440 |
| `sound_delay_seconds`, `vibration_delay_seconds` | Optional integer 0–1440 |
| `screen_sunrise`, `flashlight`, `extra_loud` | Optional Boolean or Integer 0/1 |

Smart-wake scalars accept only integral ContentValues types (`Integer`, `Long`, `Short`, `Byte`) within their documented bounds; strings, floats, booleans and null are rejected. The window bound follows the existing editor/defaults/backup validity of 0–60 minutes. Patching either sensitivity or goal preserves the other choice through `setSmartWake`; omitting all three preserves the existing smart-wake configuration. Create inherits valid defaults for omitted choices. Invalid existing metadata requires review in SleepWell and cannot be repaired through this API. Setting smart-wake choices never starts tracking: early wake still requires active phone-movement tracking and eligible measured intervals. The duration goal counts active recorded time excluding pauses, not confirmed sleep; the fixed alarm deadline remains scheduled.

Output durations and volume accept only integral ContentValues types (`Integer`, `Long`, `Short`, `Byte`) within their bounds. Output switches accept only Boolean or Integer 0/1, matching `vibrate`; Long, strings, floats and null are rejected for switches. Omitted output values preserve the saved choice on update and inherit valid user defaults on create. Updating an output scalar never repairs malformed saved metadata, changes the sound source or enrollment, grants access, requests permissions, previews an output or rings immediately.

These values use the existing real-alarm output policy: the primary sound waits for `sound_delay_seconds`, then its quadratic gain ramp uses `ramp_seconds`; extra loud chooses the 100% device alarm-volume target while retaining the separately configured volume for when extra loud is off. Actual output still follows the selected sound mode, Android/OEM volume rules and existing optional gain policy. Screen sunrise uses the sound delay as its brightening duration when nonzero, otherwise the ramp duration when nonzero, otherwise two minutes. It does not schedule an alarm at astronomical sunrise. Flashlight output remains subject to existing hardware/runtime availability; this contract neither promises torch access nor controls it now. Vibration waits for its own delay only when `vibrate` is true. Backup sound, backup escalation and all other saved behavior remain unchanged unless already supported fields are supplied.

Unknown/null/coerced field values are rejected. Update is a patch; omitted fields, including `snooze_minutes`, preserve their saved values. Create inherits valid user behavior defaults, including snooze duration when omitted, but starts disabled unless enabled is explicitly supplied. Supplying `snooze_minutes` only changes the configured duration; it does not snooze now, reset snooze counts or total-budget limits, or bypass active/pending ownership guards. Omitted sound, challenge, brightness and other saved behavior remain intact. Invalid defaults require review inside SleepWell. The API cannot enroll challenges or request permissions. Enabling requires existing exact-alarm access and a projected occurrence more than one minute away; no permission/settings prompt opens.

Only ordinary clock alarms are writable. Naps, date-specific/solar/custom-repeat/next-only schedules, weekly-profile ownership, active skip markers, invalid/unknown non-round-tripping metadata, due/near deadlines and active alarm/wake-check/snooze ownership require their existing app editor. A positive pending snooze reservation, including an overdue or uncertain one, remains read-only. An inert historical snoozed snapshot alone no longer blocks ordinary edits: production continuation now requires current durable ownership, pending deadline and an exact saved source match; cancellation clears pending authority before asking Android to cancel. Hidden unknown fields are not silently removed to make an alarm writable. This API is bounded to at most 128 saved alarms for mutation; listing remains paged.

Create returns the actual saved item URI. Update/delete acknowledge one logical row, including an idempotent replay of a completed request receipt. A receipt is historical acknowledgement, not proof that the row still exists or remains unchanged: query current state separately. Reusing the same UUID for a different payload is rejected. The receipt ledger is capped at 4,096 requests; exhaustion fails closed rather than discarding old deduplication authority.

## Optional smart-wake examples

These snippets are for a caller already explicitly granted access. Obtain and review the current item, including `writable` and `revision`, before editing. Do not execute a write solely because the example supplies values.

```java
Uri item = ContentUris.withAppendedId(
    Uri.parse("content://org.sleepwell.app.alarms/shared/v1/alarms"), reviewedAlarmId);
String[] columns = {"_id", "revision", "writable", "write_issue",
    "smart_window_minutes", "smart_sensitivity", "smart_respect_goal"};
try (Cursor current = resolver.query(item, columns, null, null, null)) {
    // Review the returned configuration and preserve its exact current revision.
}
```

After reviewing a writable current alarm, this conditional patch changes its window and sensitivity while restoring inheritance for the duration-goal choice. It preserves all other fields and uses a fresh request UUID. An enabled alarm may subsequently ring under its normal scheduling rules.

```java
ContentValues patch = new ContentValues();
patch.put("smart_window_minutes", 30);
patch.put("smart_sensitivity", 1);
patch.put("smart_respect_goal", -1);
Uri conditional = item.buildUpon()
    .appendQueryParameter("request_id", UUID.randomUUID().toString())
    .appendQueryParameter("if_match", reviewedRevision)
    .build();
int acknowledgedRows = resolver.update(conditional, patch, null, null);
// Query again for current saved values and scheduling status; acknowledgement is historical.
```

To change only goal behavior, supply only `smart_respect_goal`; to disable early waking, supply only `smart_window_minutes=0`. Neither patch resets the other smart-wake choices. The same optional scalars may accompany a create with required `hour`/`minute`; omission inherits valid defaults and create remains disabled unless explicitly enabled.

## Optional gentle wake-up example

For an already granted caller, request the configured output family explicitly alongside the current revision and write status:

```java
String[] outputColumns = {"_id", "revision", "writable", "write_issue",
    "volume_percent", "ramp_seconds", "sound_delay_seconds",
    "vibration_delay_seconds", "screen_sunrise", "flashlight", "extra_loud"};
try (Cursor current = resolver.query(item, outputColumns, null, null, null)) {
    // Review configured values and retain the exact current revision before editing.
}
```

After reviewing a currently writable alarm, the following patch requests 80% configured volume, a two-minute ramp and screen sunrise. It leaves enabled state, delays, vibration, flashlight, extra loud, tone and challenges intact. If extra loud remains enabled, its existing policy still overrides the runtime volume target. An already enabled alarm can ring later under its normal scheduling rules.

```java
ContentValues outputPatch = new ContentValues();
outputPatch.put("volume_percent", 80);
outputPatch.put("ramp_seconds", 120);
outputPatch.put("screen_sunrise", true);
Uri outputConditional = item.buildUpon()
    .appendQueryParameter("request_id", UUID.randomUUID().toString())
    .appendQueryParameter("if_match", reviewedRevision)
    .build();
int outputAcknowledgedRows = resolver.update(outputConditional, outputPatch, null, null);
// Re-query current values and scheduling status; acknowledgement is not a delivery guarantee.
```

A create can include the same output fields with required hour/minute; it remains disabled unless explicitly enabled. Neither example should be run without reviewing the selected alarm and its current revision.

## Commit and failure boundaries

Writes take the existing Scheduler lock, then the private grant lock, and compare exact raw saved content plus ETag again inside a main-database transaction. A separate durable request preparation precedes that transaction. Stored changes preserve SleepWell’s scheduling-unconfirmed marker and then use its existing `AlarmSkips.retry` scheduling/status path after the database commit. Deletion removes the exact row before requesting cancellation of its own pending actions. New IDs avoid current rows, receipt IDs and retained known scheduling/snooze ownership.

A SQLite commit and Android AlarmManager actions cannot be one transaction. Scheduling/cancellation errors can therefore leave a saved alarm with unconfirmed scheduling or a deleted row with unconfirmed pending-action cleanup. The private management screen shows the last mutation status, while each existing alarm exposes its saved scheduling status. `no_saved_error` means no saved scheduling error, not verified output or guaranteed OS delivery. Inspect/retry in SleepWell if attention is required.

If a process fails between request preparation, alarm commit, scheduling or receipt completion, repeating that UUID does **not** automatically rerun an uncertain mutation. It reports the alarm ID for explicit inspection. Revocation/errors do not claim rollback of already committed alarms. The provider never rings now, dismisses an active alarm, executes batches/RPC, opens files, starts playback/capture, launches another app or silently replays requests after boot.

## Integration and scope

Data & privacy → **Alarm access** opens the private `AlarmAccessActivity`. `Store.historyReplacing()` and `SleepSummaryFiles.historyChanging()` now call `AlarmAccessGrants.historyChanging(Context)` before production history replacement/erasure transactions (after the shared in-flight history epoch invalidation where applicable). It durably clears authority/revises the grant state before Android cleanup and can throw: abort replacement if it fails. It does not acquire Scheduler or access the main database; call before the replacement database transaction to preserve lock order.

Latest source5266e54b builds production in27s with the original signer; the provider has not been exercised against an external caller. No real alarm/output/provider calls were performed. It is intentionally narrower than full alarm-editor parity. The optional `snooze_minutes` extension has build/source evidence only; no external-client runtime verification was run for it. Sound-source selection, challenges and specialized schedules remain outside the write contract, as does vendor API compatibility. The optional smart-wake scalar extension in unreleased a8842ada builds production in19s with the original signer verified. It is outside published c6f2fcaa. No external-caller/runtime, unit/lint/native or device/output verification ran for that extension.

The optional configured-output scalar family is source implementation only in this change. No external-caller/runtime, unit/lint/native, device or output verification was performed for it. Existing admission, grant, ETag, idempotency and scheduling boundaries are unchanged.

Primary platform references: [creating a content provider](https://developer.android.com/guide/topics/providers/content-provider-creating), [SigningInfo current signers](https://developer.android.com/reference/android/content/pm/SigningInfo), and [custom permissions](https://developer.android.com/guide/topics/manifest/permission-element).
