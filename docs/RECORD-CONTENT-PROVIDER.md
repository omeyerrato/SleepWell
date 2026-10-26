# SleepWell completed-record summary provider v1

This is an original, optional read-only SleepWell interface. It is **not** a drop-in implementation of another app’s authorities, sleep-data columns, alarm CRUD, or raw sensor/audio provider. Opening settings does not share anything. No app is trusted by default.

## User-controlled access

Open **Saved record access**, enter an installed app’s exact package name, press **Verify installed package**, review its package/UID/current SHA-256 signer fingerprints, acknowledge the scope, then press **Allow verified app**. This allows all completed summaries, including future completed records. Up to eight managed apps are supported. Android package visibility must permit verification; this app does not request broad installed-package visibility. System identities, SleepWell itself, declared shared UIDs, or a UID resolving to more than one package are rejected.

A separate **Revoke** action removes one app; **Revoke all apps** clears every grant. Package/UID/current signing-certificate changes deny new reads, including certificate rotations whose old certificate appears in signing history. No replacement app is automatically accepted. Grants require the current device boot identity and an Android read-URI grant; reboot expires access, and the owned boot/update receiver clears remembered entries. Interrupted grant preparation is never resumed or promoted automatically. Allow must be requested again explicitly.

Android’s scoped read-URI permission and the provider’s private package/UID/current-signer grant are both required. A forwarded URI grant alone is insufficient. Even an app holding the signature read permission must have explicit private authorization and a current URI grant. The private settings Activity is not externally launchable. The provider captures Binder UID/PID before clearing identity for its own database and PackageManager work; it does not trust package extras.

Previously returned **cursors, data, copies, exports or screenshots cannot be recalled**. Revocation controls new queries and rejects in-flight materialization when its authorization changes. A receiving app may retain or redistribute information it has already received.

## URI and query contract

- Collection: `content://org.sleepwell.app.records/shared/v1/records`
- Individual completed record: append `/123` using a canonical positive decimal internal record ID.
- Collection parameters: optional `limit=1..25` (default `10`) and `before_id=<positive ID>`.
- Page order: `_id DESC`, an immutable local identity order, **not** edited-time chronological order. No arbitrary selection, selection arguments, SQL sorting, joins or query-bundle extensions are accepted.
- Projection: null selects all documented columns; otherwise provide a nonempty unique whitelist of the columns below. Aliases, expressions, unknown and duplicate columns are rejected.
- Collection cursor extras: `contract_version=1`, `has_more`, optional `next_before_id`, `omitted_invalid_records`, `order`, and `duration_basis`.

Use `next_before_id` for the next collection page whenever `has_more=true`, even if the returned page has zero rows. It denotes the last **examined** ID, so malformed/oversized records do not trap a client on one page. Each page is a database transaction; separate pages are not a frozen history snapshot. Edits and additions can be observed between queries. Individual URIs have no query parameters; an absent, unfinished or rejected record produces no summary row. Unsupported/oversized pages fail explicitly rather than returning a partially materialized page.

Example client query, after the user explicitly allows that client:

```java
Uri page = Uri.parse("content://org.sleepwell.app.records/shared/v1/records?limit=10");
try (Cursor rows = getContentResolver().query(page,
        new String[]{"_id", "start_ms", "end_ms", "reviewed_duration_ms"},
        null, null, null)) {
    if (rows == null) throw new IllegalStateException("No cursor");
    while (rows.moveToNext()) {
        long recordId = rows.getLong(0);
        // UTC epoch milliseconds. Interpret duration using the basis below.
    }
    Bundle pageInfo = rows.getExtras();
}
```

A consuming Android app may need a `<queries><provider android:authorities="org.sleepwell.app.records" /></queries>` visibility declaration. Signature permissions are the provider’s baseline protection; an unrelated client does not gain access by requesting those permissions in its manifest. Use the explicit in-app Allow flow.

## Columns and missing values

| Column | Cursor value | Meaning |
| --- | --- | --- |
| `_id` | integer | Local completed record identity; may be replaced by reviewed history restore, which revokes access. |
| `start_ms`, `end_ms` | integer | Effective user-reviewed UTC epoch milliseconds, including saved trims. |
| `reviewed_duration_ms` | integer | Same record coverage calculation as reports: subtract pauses, explicit awake edits and accepted awake estimates/provider awake intervals; respect known active timing. Unmarked awake time may remain. Not a validated sleep measurement. |
| `active_ms` | integer or **null** | Original saved active timing when available and nonnegative. Missing timing stays null; a saved zero remains zero. This is not a second adjustment to subtract from reviewed duration. |
| `recorded_zone` | text or **null** | Valid saved zone only. Unknown/invalid zones stay null, not today’s phone zone. |
| `source` | text | `tracked`, `legacy_timing`, `user_reported`, `provider_import`, or `unknown`. |
| `origin` | text | `local`, `imported`, `legacy`, or `unknown`; no account or source-record identity. |
| `sensor` | text | `phone_accelerometer`, `contactless_echo_change`, `none`, or `unknown`; no device identity/configuration. |
| `edited`, `interrupted`, `clock_changed`, `duration_limited` | integer 0/1 | Saved/projection provenance flags. |

Only closed, representable completed intervals whose reviewed end is not in the future are returned. Records are not synthesized for missing nights. Zero-duration reviewed coverage is retained if actual edits/intervals produce zero. Notes, tags, ratings, diary, raw epochs, classifications, waveforms, paths, files, provider IDs, credentials, sensor settings, permission/URI authority and current recording state are absent. No optional extended-text scope is provided in v1. Overlapping periods remain separate records; clients must not assume their durations are globally deduplicated.

Each query examines at most 25 records plus one lookahead. Metadata is size-bounded before shared record decoding; pause/awake child rows are bounded, with a cumulative page limit. Invalid/oversized record metadata is omitted with `omitted_invalid_records`; a total page budget failure asks clients to reduce `limit`. No unbounded `Records.all()` call is used. Waveforms, microphone, playback, alarm scheduling, permissions and integrations are never started by queries.

## Integration and revocation boundary

Private access state lives in `record_access_v1` and is excluded from existing metadata/audio/settings-profile backups. No restore/import authorizes another app. The exported provider declares separate signature read/write permissions; the only user-issued URI authority is read-only under `/shared/v1/records`. All insert/update/delete/bulk operations, batches, RPC `call`, files/assets/typed assets, refresh and canonicalization are unsupported.

Root integration must call `RecordAccessGrants.historyChanging(Context)` **before** any production history replacement/erase transaction, including `Store.historyReplacing()`. The existing `SleepSummaryFiles.historyChanging(Context)` must increment its epoch first, then call this helper before cleanup that might fail. The helper synchronously commits cleared grants and a changed private access revision before requesting OS URI revocation. It can throw `IllegalStateException`; callers must abort replacement/erase on failure. Do not catch-and-continue into reused record IDs. Provider checks retain the caller’s captured durable grant revision and history epoch through materialization and final return. After process restart, authorization still reads the durable private revision/grants and current boot/signers/URI permission, not only an in-memory epoch. No automatic regrant is attempted after a cleanup failure or boot.

Main settings needs only an explicit private navigation entry to `RecordAccessActivity`. Manifest includes that Activity, `RecordContentProvider`, signature permissions and the nonexported protected-event cleanup receiver. No schema migration is added.

## Verification limits

This increment is source-only under the requested workflow: no builds, test batches, external provider/client calls, device/emulator operations, permission dialogs or network actions were run. Android URI-grant behavior and real client interoperability require later controlled verification. Opening **Preview local summary fields** performs only a user-requested self-query and never grants another package.

Primary references: [Android provider implementation](https://developer.android.com/guide/topics/providers/content-provider-creating), [Android signing information](https://developer.android.com/reference/android/content/pm/SigningInfo), [Android permission declaration](https://developer.android.com/guide/topics/manifest/permission-element), and [vendor scope reference](https://sleep.urbandroid.org/docs/devs/content_provider_api.html). The installed Android permissions skill and its permission-element guide were applied narrowly to this new boundary.
