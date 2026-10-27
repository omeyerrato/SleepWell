# Health UI, local choices and injected permission recovery

[Run 40550003988](https://github.com/omeyerrato/SleepWell/actions/runs/40550003988), exact tested commit `ecd1c7ec5667708c37dbe884b5d1c3f586ba1c37`: `HealthConnectFlowTest` passed **2 methods in 16.961 seconds**, with all eight required original Health PNGs. The log reports the combined class time, not individual method times. Environment: API 36, `sdk_gphone64_x86_64` emulator, 420 dpi, 1080×2316 override size. This evidence predates the subsequent Cloud folder-state changes.

**The overall job failed.** The unchanged `focused-review-summary.json` records failures in `AlarmWakeSettingsTest#actualDraftRecreationSaveReopenAndCancelWithoutOutputs` and `PrivacyHistoryFlowTest#confirmedEraseIsRetainedDbFirstAndPendingAudioRetriesWithoutDeletingNewHistory`, plus their missing required frames. This folder archives the passing Health scope only; it does not relabel the job as passed.

## Native assertions that passed

`selectedImportManualSyncReopenSourceAndConflictChoicePersist` uses real native controls and private SQLite with an injected `HealthSleepPort`. Import selection survives recreation; explicit manual sync creates one imported fixture record with saved source evidence and no measured epochs. The saved original source is reopened separately. A later fake provider change conflicts with a local note edit. Cancelling its confirmation, then invoking the retained old provider-choice callback, leaves the conflict and local edit intact. Keeping the local copy succeeds while injected provider access throws: the access-call counter does not increase, storage has no error, the local note remains and the link becomes `kept`. Fake provider writes remain zero.

`providerAndPermissionPrerequisitesNeverReadSleepOrFakeConnected` starts with injected unavailability and no grants, then checks availability again, selects Import and recreates with that selection retained but no permission granted. It gates an inspection holding an older fake READ grant, requests another inspection while busy, revokes the fake grant, then releases the first. The assertions require the follow-up inspection, the latest empty grant set and an idle worker. A subsequent injected access failure must publish unknown availability (`-1`), empty grants and the exact fixture error. Sleep read/write counters stay zero throughout this method.

The test requires the CI emulator flag, rejects active tracking/alarm/ringing state, injects a private database and preferences, and checks the original database fingerprint and preferences remain unchanged. It drains retained workers/readers and closes owned Activities before restoring factories. Physical control taps require a focused committed native window and the full enabled control bounds; switch taps wait for the native checked-state transition before recreation. Capture helpers wait for a focused native frame commit.

## Individually inspected originals

Compared against the middle Health Connect panel of [board 11](../../../design-concepts/2026-10-22-phone-app/11-settings-and-integrations.png). The main screen preserves its title/subtitle, status card, separate import/export choices, permission/status rows and primary action hierarchy. Actual availability/grant wording and the fixture banner supplement the reference. Source details and conflict review are supplemental functional states, not separate board panels. These are readable-state observations, not pixel-equivalence acceptance: the app uses a generic link status glyph and omits the reference's import/export row glyphs.

| Original | Inspection |
| --- | --- |
| `health-connect-default.png` | Both choices off; Sleep access needed, Never and Connect remain readable. Cream status card and bottom primary action follow the board hierarchy. |
| `health-connect-configured.png` | Import on, Export off, injected access granted and Sync now visible after recreation; no overlapping labels or controls. |
| `health-connect-synced.png` | Same selection retained; actual fixture last-sync timestamp and zero-conflict summary appear above the informational row and primary action. |
| `health-connect-source.png` | Supplemental saved provider report shows source identity, reported 7 h 59 min, start/end, source stage bar and totals, original title/notes and the source limitation text. These are fixture/provider-reported stages, not phone measurements. |
| `health-connect-conflict.png` | One waiting provider change is visible with Your local edits were kept. Longer sync summary wraps within its row; the review target and Sync now remain legible. |
| `health-connect-kept.png` | Supplemental Provider changes screen shows Choose what to keep and the empty review state after the accepted local choice; no stale conflict row remains. |
| `health-connect-unavailable.png` | Both choices off, Unavailable on this device and Check again are visible; no connected state or sync result is fabricated. |
| `health-connect-permissions.png` | Import on, Export off, Sleep access needed, Never and Connect visible together. This frame captures missing injected access; later refresh/revocation/error checks are assertions in the same passing method, not separately captured states. |

## Evidence integrity and limits

The eight PNGs, `HealthConnectFlowTest.txt`, `environment.json` and the overall `focused-review-summary.json` were copied byte for byte from the downloaded original artifact, with source/destination SHA-256 equality checked. `SHA256SUMS` covers these files, this note and the folder-local attributes that preserve the summary bytes on checkout. Full guest, window, renderer and unrelated failure logs remain private in ignored `.android-local`; no image was cropped, resized or generated for this archive.

The permission and availability values are injected. No real Health Connect account, provider read/write, system permission dialog, Settings-return interaction, OS grant/revocation, cross-UID authorization or system permission controller was exercised. Cancellation here is the conflict confirmation and its stale callback; this run does not prove in-flight sync cancellation or the separate Allow hidden imports choice. Source details and local copies use synthetic records. No physical phone or device outputs were used. The passing scope does not establish whole-app security, all-device visual acceptance, or acceptance of the overall failed job. This archive operation launched no new tests or CI.
