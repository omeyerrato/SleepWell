# Sleep-only Health Connect manual exchange

This increment uses the official AndroidX Health Connect client 1.1.0. Only READ_SLEEP and WRITE_SLEEP are requested. Import and export are independent, default-off choices; changing a switch or reopening the screen performs no sleep-data exchange. The explicit Connect action opens Android's actual permission contract. Sync now performs the selected directions on a retained worker. Availability and actual grants are checked again for each API operation; unavailable/update-needed, denied, revoked, cancelled and provider/storage failure states are visible. Static rationale is the only exported screen and reads no records.

## Local and provider ownership

Initial import requests the previous 30 days after acquiring a changes token. Pages are bounded, followed by the changes stream so changes during the initial read are not lost. Expired change history rebaselines that window; older copies are retained and the result discloses this, rather than interpreting absence as deletion. Provider record ID plus origin deduplicates updates. Original reported stages/offsets are preserved separately from local edits; unreported gaps stay unreported. Provider awake stages affect imported duration, while no phone movement, microphone, light, sonar or audio graph is fabricated.

Changed/deleted unedited imported copies follow the provider. A locally edited copy becomes an explicit conflict with Use provider version/deletion or Keep local copy. The confirmation rechecks both the current local fingerprint and provider payload. Keeping local disconnects future overwrites. Deleting an imported copy locally retains a minimal hidden ID, so manual sync cannot silently recreate it; Allow hidden imports again explicitly resets that rule. Privacy history erase drops source payloads, leaves minimal hidden IDs, resets sync ownership and never deletes another app's source records.

Exports include completed local records, effective edited period, notes and actual explicit awake periods. Imported copies are excluded to prevent feedback. No estimated sleep stage is exported as measured. A durable UUID clientRecordId and monotonic clientRecordVersion provide idempotent upsert/retry; updating local edits updates the same remote copy. Local deletion retires only SleepWell's own export IDs for the next explicitly selected export sync. No API call is made under a SQLite transaction or Scheduler lock.

## Persistence and portability

Schema 10 adds `sessions.health_connect` and separate import/export/state tables. Typed schema-9 migration tests preserve every original table value and owned WAV file. Format 7 preserves only historical source metadata, validates strict bounded typed stages/identity/intervals before replacement, and retains formats 1–6 as unknown Health Connect provenance. Restore resets live IDs/tokens/export ownership and imports alarms disabled through the existing contract. Both CSV exporters include the same quoted historical-source column. Whole-backup UTF-8 8 MiB bounds are unchanged. Record projections retain sensor, sound and Health Connect provenance.

A durable local epoch invalidates prepared jobs after replacement/erase. Remote calls cannot be atomic with SQLite: cancellation or a 45-second API timeout may occur after a provider has committed. Completed changes stay; retries use the same client ID/version. A tiny last-local-check to external API-call race remains; stale acknowledgement is rejected and the result requests another sync rather than claiming local/provider atomicity. Cancellation does not block the UI, interrupt provider/model code or promise rollback.

## Integration hooks

- Settings Integrations: open private `HealthConnectActivity`.
- Report options for a record with nonempty `RecordEntry.healthConnect`: open private `HealthSleepSourceActivity` with long extra `record`.
- No background worker, boot receiver, runtime service or automatic exchange is registered.

## Verification contract

`HealthSleepTest` and `HealthSleepSyncTest` provide 18 pure tests. Existing RecordModel/GoalModel/DatedGoalBank tests remain enabled (66 focused tests together). `HealthSleepStorageTest` has 12 private SQLite methods: dedup/update, conflict/stale choice, keep-local, deletion ownership, hidden copies, malformed batch rollback, format7/legacy restore, strict invalid metadata, stable exports, epoch fences, paired CSV and genuine 9→10 migration preservation.

`HealthConnectFlowTest` has 2 fake-provider configuration methods and 8 planned actual Android captures: default, configured, synced, source, conflict, kept, unavailable and permissions. They use private databases/preferences, real controls/worker/local storage, and never grant real permissions or instantiate the production provider. Compilation does not certify native execution. Real Android provider exchange, user permission grant, store eligibility/profile restrictions and real source-app compatibility remain unverified. No phone/account data, alarms, audio, sensors or physical output is exercised.

## Primary sources

- https://developer.android.com/health-and-fitness/health-connect/get-started
- https://developer.android.com/health-and-fitness/health-connect/features/sleep-sessions
- https://developer.android.com/health-and-fitness/health-connect/sync-data
- https://developer.android.com/jetpack/androidx/releases/health-connect

Android 14+ uses a system Health Connect module; earlier supported versions require its provider app. Actual access depends on OS/profile/provider availability and granted sleep permissions. Distribution may require Health Connect policy declarations/approval. This source increment is a manual sleep-exchange feature, not verified interoperability with every provider or completion of SleepWell.

## Main Report projection — source21796c55

The saved provider stages now appear directly on the main Report. The existing
strict historical codec is decoded with the report snapshot; no provider query,
permission prompt, sensor inference or metadata rewrite occurs on opening it.
Bars retain their actual source intervals, clipped to the current visible record
period. Missing/unknown evidence is never filled with light/deep/REM labels.
Tap or keyboard/accessibility navigation selects an actual interval and identifies
its source kind and clock interval (minute-level display; retained timestamps are unchanged). The legend includes only visible kinds; awake,
out-of-bed and awake-in-bed share the Awake legend but retain exact selection names.

Source details still opens the unchanged original provider report. Local edits may
trim/extend the view or exclude awake time from the displayed duration; they never
relabel provider stages. If known endpoint offsets differ, the chart explicitly
uses UTC, since offsets alone cannot identify the intervening timezone rules.
No stages supplied/no stages in the selected period have explicit empty states.

Combined current source2cbfe043 assembles in48s. This new provider projection has not had
runtime/screenshot or real provider validation. The separately selected single
Report history/edit flow uses isolated motion fixtures, so it cannot validate
provider stages or clinical sleep accuracy even when it passes.
