# Settings references 31–36: bounded native evidence

[CI 41017469167](https://github.com/omeyerrato/SleepWell/actions/runs/41017469167) passed **five explicit native journeys with all 35 required original PNGs**, at captured source **`f9b7a226e7113579bfcab89a239f05770d43e490`**. The unmodified [focused summary](focused-review-summary.json) has no failures and explicitly records `full_app_review: false`.

Every original below was individually opened as a complete native window and compared with boards [11](../../../design-concepts/2026-10-22-phone-app/11-settings-and-integrations.png) and [12](../../../design-concepts/2026-10-22-phone-app/12-privacy-and-appearance.png). These are emulator captures, not rendered mockups or a contact-sheet-only review. [Environment](environment.json): API 36 x86_64, 1080×2316 at 420 dpi, host audio disabled, no native playback allowlisted. The phone was not used.

## Primary reference comparison

| Reference | Actual original | Finding at this captured source |
| --- | --- | --- |
| 31 Settings | [Settings root](protection-settings-root.png) | Eight aligned category rows, meaningful raster icons, GitHub row, search and complete grouped dock are visible. The dedicated Alarms tab follows the user's later instruction. |
| 32 Health Connect | [Not connected](health-connect-default.png), [selected access](health-connect-permissions.png) | Real supplied Health Connect mark on white; warm status card, separate Import/Export rows, permissions, last sync and pinned primary action. The Connect action is disabled until a direction is selected. |
| 33 Automations | [Saved-rule list](automation-rules-main.png), [reopened editor](automation-rule-reopened.png) | Rule card, event→destination chips, adjacent Add rule and destination rows follow the reference hierarchy. Test/Save belong to the selected rule's editor/options. One genuine private fixture rule is shown; a second illustrative rule is not fabricated. |
| 34 Alarm readiness | [Readiness](protection-readiness.png), [returned from Android](protection-readiness-returned.png) | Five actual Android-access states, readable state markers and complete pinned action pair. Reliability tips remain in the labelled Options action. Test a quiet alarm was never pressed. |
| 35 Data & privacy | [Read-only main](protection-privacy-main.png) | Local totals, retention, favorites, backup pair, cloud, audio export and distinct audio-only deletion row are visible. Empty totals are actual zeros. Export, import and deletion were not invoked. |
| 36 Appearance | [Automatic](appearance-automatic.png), [extra-large/contrast](appearance-large-contrast.png) | Three equal theme previews and checked rings, settings rows, sunrise bedroom raster, full Connect light target and separate pinned Save are visible. Enlarged lamp labels/values stack without colliding. The new illustration preserves the reference subject/composition; it is not an identical copy of its pixels. |

Normal Appearance frames include the full 48dp lamp controls on the first default frame; the previous Connect/Save clipping assertion now passes. The extra-large Appearance frame intentionally scrolls to the lamp controls, while Save remains pinned. The selected theme frames show each actual manual choice, and the Tonight frames show its saved effect. Automatic mode selects Dawn at the captured morning clock, rather than pretending the reference's Midnight selection is current.

## Actual actions and persistence

| Native method | Time | Exercised scope and limits |
| --- | --- | --- |
| [Appearance save/reopen/relaunch/cancel](AppearanceFlowTest%23saveReopenRelaunchCancelAndRenderAllThemes.txt) | 59.094 s | Manual theme selection cancels on Back, saves all three themes, reopens/relaunches, changes text size/motion/contrast and checks real saved values/effects. Lamp controls are inspected for full bounds; no lamp connection or tracking is started. |
| [Health import/sync/conflict](HealthConnectFlowTest%23selectedImportManualSyncReopenSourceAndConflictChoicePersist.txt) | 8.293 s | Uses a fake provider and private SQLite/preferences. Import choice survives recreation; explicit sync creates the provider-owned record, source details open, and Keep local copy preserves a real local note through conflict review. No real Health Connect account or provider is accessed. |
| [Health prerequisites](HealthConnectFlowTest%23providerAndPermissionPrerequisitesNeverReadSleepOrFakeConnected.txt) | 4.108 s | Fake unavailable/revoked/error states keep actual import choices and never read or write provider sleep data. This does not verify the real Android Health Connect permission UI. |
| [Automation private rule journey](AutomationStorageTest%23configReopensWithoutPlaintextCredentialsInRows.txt) | 27.291 s | Actual private rule Off/On, destination route, edited fields, event/type choices, recreation, Save/reopen and cancelled draft. Confirmed Test queues exactly one private fixture event across recreation through a fake scheduler; **no HTTP, MQTT or Tasker delivery occurs**. Load/scheduling failures and retry remain visible. |
| [Settings/Privacy/readiness](AlarmProtectionFlowTest%23settingsPrivacyAndReadinessWithoutAlarmExecution.txt) | 13.369 s | Opens Settings and read-only Privacy, then denies microphone access, cancels the owned rationale, visits Android App info and returns/recreates. Actual access states are checked and shared preferences are preserved. No alarm editor, alarm runtime, quiet test, export/import or deletion action runs. |

## Remaining confirmation styling at this source

The passing run also revealed a visual mismatch in [the recreated test confirmation](automation-rule-test-confirmation-recreated.png): it still has the old square white Android dialog and default typography/capitalized buttons. Production commit `37d4a84f` applies existing `ReportDialogs.style` immediately after this owned dialog is shown, retaining the same builder, wording, dismissal ownership and guarded callbacks. **That later visual correction is not evidenced by these f9b7a226 captures.** Its separate Automation-only rerun must be inspected before marking that supporting state finished.

## Complete original inventory

### Appearance and saved themes (10)

- [appearance-automatic](appearance-automatic.png)
- [appearance-selected-0](appearance-selected-0.png)
- [appearance-selected-1](appearance-selected-1.png)
- [appearance-selected-2](appearance-selected-2.png)
- [appearance-tonight-0](appearance-tonight-0.png)
- [appearance-tonight-1](appearance-tonight-1.png)
- [appearance-tonight-2](appearance-tonight-2.png)
- [appearance-large-contrast-tonight](appearance-large-contrast-tonight.png)
- [appearance-large-contrast-tonight-action](appearance-large-contrast-tonight-action.png)
- [appearance-large-contrast](appearance-large-contrast.png)

### Automation rules and supporting states (10)

- [automation-rules-main](automation-rules-main.png)
- [automation-rules-options](automation-rules-options.png)
- [automation-rules-destination](automation-rules-destination.png)
- [automation-rule-type-recreated](automation-rule-type-recreated.png)
- [automation-rule-draft](automation-rule-draft.png)
- [automation-rule-test-confirmation-recreated](automation-rule-test-confirmation-recreated.png)
- [automation-rule-busy-recreated](automation-rule-busy-recreated.png)
- [automation-rules-saved-notice](automation-rules-saved-notice.png)
- [automation-rules-load-error](automation-rules-load-error.png)
- [automation-rule-reopened](automation-rule-reopened.png)

### Health Connect and provider review (8)

- [health-connect-default](health-connect-default.png)
- [health-connect-configured](health-connect-configured.png)
- [health-connect-synced](health-connect-synced.png)
- [health-connect-source](health-connect-source.png)
- [health-connect-conflict](health-connect-conflict.png)
- [health-connect-kept](health-connect-kept.png)
- [health-connect-unavailable](health-connect-unavailable.png)
- [health-connect-permissions](health-connect-permissions.png)

### Settings, read-only Privacy and readiness (7)

- [protection-settings-root](protection-settings-root.png)
- [protection-privacy-main](protection-privacy-main.png)
- [protection-readiness](protection-readiness.png)
- [protection-readiness-rationale](protection-readiness-rationale.png)
- [protection-readiness-rationale-cancel](protection-readiness-rationale-cancel.png)
- [protection-readiness-settings-recovery](protection-readiness-settings-recovery.png)
- [protection-readiness-returned](protection-readiness-returned.png)

## Provenance and boundaries

The 35 PNGs, five method transcripts, environment and focused summary were copied unchanged from the run artifact. [Original sizes and SHA-256](original-sha256.json) pin those bytes; folder-local attributes preserve the original text files. No capture was painted, cropped into a replacement screen, or relabelled as another source revision.

The run proves these five journeys on this emulator and supports this six-screen-group comparison. It does not establish all 36 references, every option/state/locale/device, transient SystemUI contrast, physical-phone acceptance, smart-light control, real Health Connect sync, external automation delivery, alarm loudness/protection, sound analysis or overnight behavior. No new CI, build, phone or hardware-output action was dispatched while preparing this archive.


## Warm Automation confirmation follow-up

[CI 41020300714](https://github.com/omeyerrato/SleepWell/actions/runs/41020300714) passed the same single Automation journey in **28.656 s**, with all ten required originals, at **`f0de1ef754a5d694e99883bacc47946d5f7ab97b`** after the owned confirmation styling change. The [unaltered summary](warm-automation-41020300714/focused-review-summary.json) still records `full_app_review: false`. The earlier 35 originals and their source identity above are preserved; this follow-up does not silently relabel them.

All ten follow-up PNGs were individually opened in full. The [recreated confirmation](warm-automation-41020300714/automation-rule-test-confirmation-recreated.png) now has a rounded cream surface, warm Inter typography and sentence-case Cancel/Send test actions, both fully visible. This resolves the square white/default dialog mismatch documented above for this captured supporting state. The normal confirmation is opened and asserted before recreation by the same method, but **no separate normal-confirmation PNG exists in this artifact**; visual evidence here is specifically the recreated confirmation.

| Original | Inspected result |
| --- | --- |
| [Main rules](warm-automation-41020300714/automation-rules-main.png) | Complete private rule card, event/destination chips, Add rule and three destinations. |
| [Options](warm-automation-41020300714/automation-rules-options.png) | Named supporting routes and full Done action. |
| [Webhook destination](warm-automation-41020300714/automation-rules-destination.png) | Actual connection prerequisites and full Add Webhook rule action. |
| [Destination recreated](warm-automation-41020300714/automation-rule-type-recreated.png) | Visible checked Webhook and full Use destination action. |
| [Edited draft](warm-automation-41020300714/automation-rule-draft.png) | Actual edited name/URL, masked token, activation selection and full Save. |
| [Confirmation recreated](warm-automation-41020300714/automation-rule-test-confirmation-recreated.png) | Warm rounded confirmation, readable message and complete Cancel/Send test pair. |
| [Busy recreated](warm-automation-41020300714/automation-rule-busy-recreated.png) | Queueing state remains visible with full Done action. |
| [Saved notice](warm-automation-41020300714/automation-rules-saved-notice.png) | Saved rule and truthful scheduling-attention notice remain visible. |
| [Load error](warm-automation-41020300714/automation-rules-load-error.png) | Actual load failure, retained saved rule and complete Refresh action. |
| [Reopened editor](warm-automation-41020300714/automation-rule-reopened.png) | Saved name/URL and masked token reopen; remaining activation content is scrollable above pinned Save. |

The [original method transcript](warm-automation-41020300714/AutomationStorageTest%23configReopensWithoutPlaintextCredentialsInRows.txt) covers the same private Off/On, route choices, draft recreation, confirmation cancellation, stale-owner rejection, one explicit accepted test event across recreation, saved field persistence, load/scheduling failure presentation and cancelled edit. It uses private storage and a fake scheduling port: **no real HTTP, MQTT or Tasker delivery, alarm, sound or hardware output is established**. The visible CI fixture banner identifies that context; it is not production page copy.

The ten PNGs, method transcript, environment and focused summary were copied unchanged into `warm-automation-41020300714/`; [byte sizes and SHA-256](warm-automation-41020300714/original-sha256.json) identify those originals. No new CI/device action was dispatched while inspecting and archiving this run. The follow-up supports this Automation styling/state correction only; all other emulator, first-frame SystemUI, real-provider, device and all-36-reference limitations above remain.


## Privacy backup main action: export follow-up

[CI 41034030023](https://github.com/omeyerrato/SleepWell/actions/runs/41034030023) passed `BackupDocumentFlowTest#realMainRouteProviderWaitCancelRecreateAndCsvStayResponsive` in **20.201 s** at **`14fb6ff11045dba8562285abd4ef8eb67167ee99`**, with all eight required original PNGs. The [focused summary](manual-export-41034030023/focused-review-summary.json) records one method and `full_app_review: false`; [environment](manual-export-41034030023/environment.json) retains API 36 / 1080×2316 / 420 dpi and no native playback allowlist.

This adds an actual action to reference 35's earlier read-only composition evidence: the existing method opens **Main Settings → Data & privacy → Export backup**, reaches the real BackupActivity JSON action, cancels document selection, cancels a delayed write across recreation, then exports CSV through the real Other data tools row and completes JSON export. Exact generated CSV and JSON bytes are compared with the private database's contents. JSON/CSV completion receipts persist separately across a fresh reader/reopened Activity; failed write/close/access operations preserve the prior receipt, and receipt failure does not relabel a successfully closed export as failed. A later successful export recovers its receipt. Original preferences, production database contents and files outside the owned fixture are checked unchanged.

All eight native windows were individually opened and compared for supporting-flow coherence with board 12/reference 35: warm cream/brown palette, Inter typography, consistent icons/rows, grouped secondary tools and complete primary/cancel actions. No material clipping or new visible issue requiring a product edit was found. These are supporting Backup windows; this run does **not** contain a new screenshot of the parent Data & privacy page or the Android document chooser.

| Original | Inspected result |
| --- | --- |
| [Selection pending](manual-export-41034030023/backup-document-chooser.png) | Actual Backup selecting phase; clear document instruction and complete Cancel selection. The external chooser is injected, not pictured. |
| [Selection cancelled](manual-export-41034030023/backup-document-selection-cancelled.png) | Clear unchanged-data notice, three grouped secondary tools and full Export/Import pair. |
| [Writing](manual-export-41034030023/backup-document-writing.png) | Progress/status and complete Cancel operation; secondary tools are withheld while occupied. |
| [Writing recreated](manual-export-41034030023/backup-document-writing-recreated.png) | Same actual retained operation and complete cancellation action after recreation. |
| [Write cancelled](manual-export-41034030023/backup-document-cancelled.png) | Partial-file warning is readable; tools and Export/Import return. |
| [CSV complete](manual-export-41034030023/backup-document-csv-done.png) | Actual CSV completion notice and complete main actions. |
| [JSON complete](manual-export-41034030023/backup-document-json-done.png) | Actual JSON completion and explicit audio/settings exclusion. |
| [Receipt unavailable](manual-export-41034030023/backup-document-receipt-warning.png) | Saved-document versus unavailable-last-export-date distinction remains readable above full controls. |

**Boundary:** this journey uses private SQLite and receipt preferences, an injected document picker and fake provider streams. Actual export serialization and stream close happen, but exported bytes are retained in the fixture's memory; this does not prove a physical file saved by a real Android SAF/cloud provider, provider durability, or process-death recovery. Forgetting the retained job creates a fresh reader; it is not an actual process kill. The fixture's private enabled nap is never scheduled. No import/replacement, audio export/playback, deletion, alarm or hardware output action is performed by this selected method.

The [original method transcript](manual-export-41034030023/BackupDocumentFlowTest%23realMainRouteProviderWaitCancelRecreateAndCsvStayResponsive.txt), environment, summary and eight PNGs were copied unchanged into `manual-export-41034030023/`; [byte sizes and SHA-256](manual-export-41034030023/original-sha256.json) identify those originals. Earlier archives remain unchanged. No new CI, device or output action was dispatched while inspecting/archiving this run; no broader all-36, physical-device or external-provider acceptance is claimed.
