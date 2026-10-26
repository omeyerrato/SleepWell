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

The default/null projection remains the original 13 columns above, in that order. An explicit projection may additionally request `snooze_minutes`: the configured snooze duration as an integer from 1 to 60, or null when outside this bounded range. This is not a pending snooze deadline or remaining snooze allowance. Explicit projections still reject unknown or duplicate columns.

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

Unknown/null/coerced field values are rejected. Update is a patch; omitted fields, including `snooze_minutes`, preserve their saved values. Create inherits valid user behavior defaults, including snooze duration when omitted, but starts disabled unless enabled is explicitly supplied. Supplying `snooze_minutes` only changes the configured duration; it does not snooze now, reset snooze counts or total-budget limits, or bypass active/pending ownership guards. Omitted sound, challenge, brightness and other saved behavior remain intact. Invalid defaults require review inside SleepWell. The API cannot enroll challenges or request permissions. Enabling requires existing exact-alarm access and a projected occurrence more than one minute away; no permission/settings prompt opens.

Only ordinary clock alarms are writable. Naps, date-specific/solar/custom-repeat/next-only schedules, weekly-profile ownership, active skip markers, invalid/unknown non-round-tripping metadata, due/near deadlines and active alarm/wake-check/snooze ownership require their existing app editor. A positive pending snooze reservation, including an overdue or uncertain one, remains read-only. An inert historical snoozed snapshot alone no longer blocks ordinary edits: production continuation now requires current durable ownership, pending deadline and an exact saved source match; cancellation clears pending authority before asking Android to cancel. Hidden unknown fields are not silently removed to make an alarm writable. This API is bounded to at most 128 saved alarms for mutation; listing remains paged.

Create returns the actual saved item URI. Update/delete acknowledge one logical row, including an idempotent replay of a completed request receipt. A receipt is historical acknowledgement, not proof that the row still exists or remains unchanged: query current state separately. Reusing the same UUID for a different payload is rejected. The receipt ledger is capped at 4,096 requests; exhaustion fails closed rather than discarding old deduplication authority.

## Commit and failure boundaries

Writes take the existing Scheduler lock, then the private grant lock, and compare exact raw saved content plus ETag again inside a main-database transaction. A separate durable request preparation precedes that transaction. Stored changes preserve SleepWell’s scheduling-unconfirmed marker and then use its existing `AlarmSkips.retry` scheduling/status path after the database commit. Deletion removes the exact row before requesting cancellation of its own pending actions. New IDs avoid current rows, receipt IDs and retained known scheduling/snooze ownership.

A SQLite commit and Android AlarmManager actions cannot be one transaction. Scheduling/cancellation errors can therefore leave a saved alarm with unconfirmed scheduling or a deleted row with unconfirmed pending-action cleanup. The private management screen shows the last mutation status, while each existing alarm exposes its saved scheduling status. `no_saved_error` means no saved scheduling error, not verified output or guaranteed OS delivery. Inspect/retry in SleepWell if attention is required.

If a process fails between request preparation, alarm commit, scheduling or receipt completion, repeating that UUID does **not** automatically rerun an uncertain mutation. It reports the alarm ID for explicit inspection. Revocation/errors do not claim rollback of already committed alarms. The provider never rings now, dismisses an active alarm, executes batches/RPC, opens files, starts playback/capture, launches another app or silently replays requests after boot.

## Integration and scope

Data & privacy → **Alarm access** opens the private `AlarmAccessActivity`. `Store.historyReplacing()` and `SleepSummaryFiles.historyChanging()` now call `AlarmAccessGrants.historyChanging(Context)` before production history replacement/erasure transactions (after the shared in-flight history epoch invalidation where applicable). It durably clears authority/revises the grant state before Android cleanup and can throw: abort replacement if it fails. It does not acquire Scheduler or access the main database; call before the replacement database transaction to preserve lock order.

Latest source5266e54b builds production in27s with the original signer; the provider has not been exercised against an external caller. No real alarm/output/provider calls were performed. It is intentionally narrower than full alarm-editor parity. The optional `snooze_minutes` extension has build/source evidence only; no external-client runtime verification was run for it. Sound selection, challenges, light settings and specialized schedules remain outside the write contract, as does vendor API compatibility.

Primary platform references: [creating a content provider](https://developer.android.com/guide/topics/providers/content-provider-creating), [SigningInfo current signers](https://developer.android.com/reference/android/content/pm/SigningInfo), and [custom permissions](https://developer.android.com/guide/topics/manifest/permission-element).
