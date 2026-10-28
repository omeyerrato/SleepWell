# 0.1.4 frozen candidate — independent package/source audit

**Historical candidate, not published, being superseded by pending fixes.** Source `63cc05cd816ce12c70936f25b37aa4cb27d6c270`, version **0.1.4 / code5**. Root has identified pending Math answer-owner/stale-callback and cold-process Travel-notification cancellation corrections. This archive preserves the exact earlier candidate; it does not certify those fixes or recommend publishing this APK.

Independent checks confirm **162,094,173 bytes**, SHA256 `f571e436cab88d639b43f41d7d5095d11f007c8ecbc6955571eb5870a913ab4f`, `org.sleepwell.app`, arm64-v8a, minSDK30 / target36. [Candidate record](candidate.json), [badging](package-badging.txt), [signature verification](package-signer.txt). Source app tree is `37af5ec78ec9d4a452e20a15f27f167ae3c34864`; the distinct app/src/main tree is `78c12f6b9828c0e34348f805b2b3f260d4b8dac2`. These source identifiers are pinned to the Root-owned frozen build, not inferred through a source-to-DEX equivalence proof.

Root's actual assembleDebug completed successfully in the transcript's **2m**,34 tasks/10 executed/24 up-to-date. [Original build transcript](build-original.txt) is copied byte-identically, SHA256 `5de713f61e7280762522e68f2c045b32266751447512985645a1b2fab7172970`. Root's recorded wall duration is122.032s; the verifier did not time or repeat the build. This task ran only read-only SDK/ZIP/Git inspections and wrote this audit folder. It performed no build, CI, release, install, phone/output operation or latest metadata modification.

## Package retention versus published 0.1.3

The independently rechecked baseline is frozen source `d4415a2b5194abd50c36921a5c7c317ef1f18c51`,0.1.3/code4,162,061,389 bytes,SHA256 `bfb3015de97e521951c681a80bcc39443ddca489fe0ade89a525a9295890023d`. [Earlier baseline audit](../2026-10-28-013-build/README.md). The new APK is32,784 bytes larger. Aapt's166 badging lines differ only in the package/version line; permissions, features, launcher and icon references remain equal. This does not imply equal runtime code.

| Audited uncompressed section | Exact comparison |
| --- | --- |
| All65 assets, including audio/models/fonts | Byte-identical; none added, removed or changed |
| All8 native libraries | Byte-identical; arm64-v8a only |
| All481 res entries | Byte-identical; none added, removed or changed |
| resources.arsc | Byte-identical |

[Every entry's size/hash](package-assets.json) is retained; no text-normalization exception was used. [Candidate](package-signer.txt) and [baseline](baseline-signer.txt) verify with one v2 signer and certificate SHA256 `49eeeb7f74344335321800e3f78b9bb9ab25c5fff059332c458133dca0da0e1c`. This is original Android Debug signer continuity. Store and Backup source remain byte-identical to0.1.3: database schema17 and records/alarms format14. No database migration or user-data operation ran in this audit. Typed profilev2 is used only with optional Appearance.roomDimming; valid legacyv1 remains supported. Travel configuration and its new local reminder receipts remain excluded from both typed profiles and record backups.

## Exact bounded source comparisons

[source-equivalence.json](source-equivalence.json) records capture/candidate hashes for every named path plus exact scoped fragments. No global MainActivity, whole-app or whole-APK runtime equality is inferred.

| Native proof | Candidate-source comparison and limits |
| --- | --- |
| Travel Plan41.859s/11 and Cue54.505s/26 | [Composite evidence](../2026-10-28-travel-plan-native/README.md): listed Travel runtime/UI/service/protection files match the respective passing candidates. Plan native test also matches. Cue differs only by the subsequent one-input/focused-owner native helper correction, not production behavior. The37 originals combine two individually passing classes on identical production source; both full jobs remain FAILED. The known cold-process notification cleanup limitation remains open in this frozen candidate. |
| Actual Tracking Activity29.722s/14 | [Actual owner proof](../2026-10-28-phone-motion-activity/README.md),5c794071: Activity, reader, chart, clock, store and native test match. The genuine Snapshot class matches exactly. Whole TrackingService differs by three Travel reminder invalidation hooks in real service paths excluded from that inert fixture. No real sensor/service admission, overnight or physical input claim. |
| Room settings Search12.917s/5 | [Discovery proof](../2026-10-28-room-settings-search/README.md),52b6dbb5: named Search/Room source and native method match. Main differs only in alarm-list code; exact settingsSection/openSettingsGroup fragments match. Room's private manifest component matches; the manifest separately adds the private Travel receiver. The main Settings copy change was not executed by this Search method; hardware light/audio/dimming/OS grants remain excluded. |
| Alarm organization41.023s/18 | [Alarm interaction proof](../2026-10-28-alarm-clock-interactions/README.md),4e9366d9: named AlarmClockUi/editor/Main/shared controls/scheduling/storage/native sources match. Manifest differs only by the new private Travel receiver; script inventory is separately described below. Inert drafts, actual controls/recreation/save/reopen and app-large-text list were exercised, with no alarm scheduling/output. Supporting Schedule/More routes still require scrolling; complete reference fidelity remains open. |

Source deltas from0.1.3 are explicitly listed in the JSON: the direct alarm clock controls and compact-list feedback, real phone-motion reader/chart integration, Room search entry, Travel hierarchy and default-OFF independent silent reminder, private receiver and three service cancellation hooks, plus version metadata. Their bounded native evidence does not establish every integration between these features. Existing0.1.3 proofs remain separately scoped; this audit does not replay or expand them.

## Workflow and inventory union

[verification-metadata.json](verification-metadata.json), [frozen delta](verification-metadata-diff.patch) and [post-build correction](post-build-organization-inventory.patch) preserve actual metadata differences. Frozen63 includes chart-phone-motion14 and room-settings-search5, preserves travel-ui's3 existing methods/37 literals, and reformats/unions the focused inventory dictionary. Whole workflow/script equality is not claimed.

Frozen63's organization inventory still lists14 rather than the alarm run's18 originals. Metadata-only commit `7ceb894e65be196dd2d5e7300f08387ae3b06b0c` subsequently restores the four list/list-large/time-stepped/time-recreated entries to18. Its app tree and app/src/main tree equal frozen63; no binary rebuild is needed for that metadata-only change. Conversely chart14 in frozen63 versus chart8 in the historical alarm candidate reflects the scope union. The unchanged selectors and named frame lists are compared structurally, rather than accepting formatting or a global dictionary match as proof.

This audit certifies the stated artifact identity/retention and listed-source comparisons only. The63 candidate remains unpublished and pending replacement; no full36-screen, whole-app security, physical audio/haptic/light/sensor, real alarm/provider/OS notification delivery, process-death or overnight acceptance is claimed.
