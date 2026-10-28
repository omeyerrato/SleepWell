# 0.1.4 final candidate: independent package/source audit

Source `08d3911e90af666d79a1f57fb0ea1326e91c3929`, version **0.1.4 / code5**. This is a separate archive from the [historical, superseded 63cc05cd candidate](../2026-10-28-014-build/README.md). This audit performs no build, native rerun, release, install, phone/output operation or latest-metadata modification.

Independent package checks confirm **167,067,516 bytes**, SHA256 `3b6c698fbfc1d71a21fa2e9b5420b7cf08ad39632608648e226482d11709a024`, `org.sleepwell.app`, arm64-v8a, minSDK30 / target36. [Frozen record](candidate.json), [badging](package-badging.txt) and [signature verification](package-signer.txt). Source app tree `4859aa32a69de918ffc4f599577938454a45bf1e` and distinct app/src/main tree `4459930509280751c888fb17dfb4a38a8c91776d` are pinned to Root's frozen build result; this is not a source-to-DEX equivalence proof.

Root assembleDebug succeeded in the transcript's **1m19s**,34 tasks/4 executed/30 up-to-date. [Verbatim build transcript](build-original.txt) is byte-identical to Root's retained log, SHA256 `aca7baa73063584313d1cf21188dce69f8ba5e0304efbb17392b425eea0534b6`. The auditor did not time or repeat that build. Root had not published or installed this candidate when it was audited.

## Retained package content and identity

Both frozen references were independently rehashed: published0.1.3 `d4415a2b5194abd50c36921a5c7c317ef1f18c51` is162,061,389 bytes/SHA256 `bfb3015de97e521951c681a80bcc39443ddca489fe0ade89a525a9295890023d`; historical0.1.4 `63cc05cd816ce12c70936f25b37aa4cb27d6c270` is162,094,173 bytes/SHA256 `f571e436cab88d639b43f41d7d5095d11f007c8ecbc6955571eb5870a913ab4f`. All three independently verify with one Android Debug v2 signer, certificate SHA256 `49eeeb7f74344335321800e3f78b9bb9ab25c5fff059332c458133dca0da0e1c` ([baseline](baseline-signer.txt), [historical](historical-signer.txt)).

| Uncompressed content | Final versus both references |
| --- | --- |
| All65 assets (audio/models/fonts included) | Byte-identical; none added/missing/changed |
| All8 native libraries | Byte-identical; arm64-v8a only |
| All481 res entries | Byte-identical; none added/missing/changed |
| resources.arsc | Byte-identical |

[Per-entry exact hashes](package-assets.json) use no text normalization. Aapt's166 badging lines equal historical63 completely; only the package/version line differs from published0.1.3. Existing permission/feature/launcher/icon declarations remain equal. This does not imply equal runtime code.

[Complete ZIP-entry delta](package-code-delta.json) finds the same653 entries as63; only `classes3.dex` content changes (+5,388 uncompressed/stored bytes). The total APK is4,973,343 bytes larger than63 and5,006,127 larger than0.1.3. [Physical ZIP layout](package-layout.json) accounts for the larger63 delta: final has an extra4,967,956-byte unreferenced gap made of76 empty local records with zero-length names/data and all-zero extra padding, plus the5,388-byte DEX increase and one-byte local-record layout difference. Both have a4,096-byte signing block. The gap is not a new central-directory asset/code entry or retained old payload. Original signature verification passes; no APK repacking/rebuilding was performed.

## Named source comparisons and native boundaries

[source-equivalence.json](source-equivalence.json) independently hashes each named capture/candidate path and scoped fragment. It does not assert whole-app or source-to-DEX equivalence. [Production delta from historical63](production-from-historical.patch) contains five paths: MathAnswerOwner, AlarmActivity, WakeMathCard, TravelTrackingReminders and AlarmEditorActivity. Version/build.gradle is unchanged from historical63.

| Bounded native proof | Final source comparison and limits |
| --- | --- |
| Travel Plan41.859s/11 and Cue54.505s/26 | [Composite37](../2026-10-28-travel-plan-native/README.md) combines individually passing scopes from two FAILED full jobs, not a single37-image pass. All named production paths match except the subsequent TravelTrackingReminders cleanup fix. Plan native remains equal; Cue native differs by the one-input/focused-owner helper and new cleanup assertions, [exact diff](native-cue-helper-diff.patch). Older proof does not certify new cleanup runtime. |
| Actual Tracking Activity29.722s/14 | [Actual owner proof](../2026-10-28-phone-motion-activity/README.md),5c794071: named Activity/reader/chart/clock/store/native files and actual Snapshot class match. Whole TrackingService differs by three Travel reminder invalidation hooks in excluded real service paths. No sensor/service admission or physical/overnight claim. |
| Room Search12.917s/5 | [Discovery proof](../2026-10-28-room-settings-search/README.md),52b6dbb5: named Search/Room files, native method and Main settingsSection/openSettingsGroup fragments match. Whole Main differs in alarm-list code; manifest separately adds a private Travel receiver while Room component attributes match. Search captures do not establish the changed Settings-main copy or actual light/audio/dimming/OS grants. |
| Alarm organization41.023s/18 | [Alarm controls proof](../2026-10-28-alarm-clock-interactions/README.md),4e9366d9: named AlarmClockUi/Main/shared/scheduling/storage/native files match. AlarmEditorActivity differs only in footer subtitle selection: smart-wake invalid requires review, nonzero wake window may ring within the window, otherwise chosen local time. This copy is **source-only and uncaptured**; old18 images do not prove its new wording/fit. Supporting Schedule/More routes still require scrolling; full reference fidelity remains open. |
| Math owner51.773s/14 | [Stale Math owner proof](../2026-10-28-stale-math-owner/README.md),CI40633181797 at95716b6a: all five named production/native sources match. Shared guarded production Math widgets run in explicit private Practice context with private occurrence state. Current/stale selection, typed/Done, dialog/pause/recreate/hidden/detached and off-main fences are bounded proofs. Actual AlarmActivity admission, central advance, dismissal/service/protection and physical alarm behavior were excluded. |
| Travel cleanup | Existing first Cue method retryCI40635063367 at747de525 is **pending**; priorCI40633953280 failed before cleanup assertions and is not accepted as a cleanup pass. Named TravelTrackingReminders production bytes match frozen08. Native Cue source differs only by the actual owned-time-dialog focus wait ([patch](native-cue-final-wait.patch)).747 predates the separately verified Math source, so whole production-tree equality is explicitly excluded. Cleanup proof will be separately archived by its owner; this package audit does not claim it has passed. |

The final Math callback is fenced by main thread, resumed/focused live Activity, attached visible card in that Activity window, exact current question, local run/unit, durable ringing/run/unit/cache and the existing Scheduler lock. The central advance adds durable expected-unit equality; it does not introduce a global focus/resume gate into asynchronous QR/camera/NFC proof paths. Passing private Practice assertions cannot establish the excluded real AlarmActivity integration.

Travel cleanup removes the process-static visibility precondition and always best-effort cancels the fixed owned notification. A cancellation exception stores an actionable diagnostic while invalidation continues. The new probe models a notification retained by the fake output port without a process-local show; it **does not restart an Android process or verify the OS notification controller**. The reminder remains default OFF, active-manual-recording only, revision/window identified and consume-before-output; paused/automatic/no owner, stale generation and delivery more than60s late do not show. Scheduling is inexact and may skip; the notification timeout limits stale display. Explicit manual flashlight arming is separate and preserved.

Database schema17, records/alarms Backup14 and typed profile source remain byte-identical to published0.1.3. No migration or user-data action ran here. Typed profilev2 applies iff optional Appearance.roomDimming is present; legacyv1 remains supported. Travel configuration/reminder receipts stay excluded from both profile versions and records backups.

## Verification metadata union

[Static structural comparison](verification-metadata.json) and [exact workflow/script delta](verification-metadata-diff.patch) preserve formatting and union differences. Neither script nor workflow is claimed globally identical. CI code was parsed as data and never imported/executed by this audit.

Final inventories retain travel-ui37 (two classes/three existing methods), chart-phone-motion14 and Room Search5. Organization18 corrects historical63's14-entry inventory to the actual passing18; this metadata-only correction does not change packaged runtime. Historical organization-capture chart8 versus final chart14 reflects the expected union. New math-owner14 and travel-cue-cleanup18 select existing methods only. Their optional native-sounds/audio-imports flags are false with host audio disabled.

The archive certifies the stated artifact identity/retention and named source comparisons. It does not establish complete36-screen fidelity, whole-app security, real OS restart/notification/provider behavior, alarm admission/central advancement, physical sensor/audio/haptic/light behavior or overnight acceptance. Publication remains Root-owned and outside this audit.
