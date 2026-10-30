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
