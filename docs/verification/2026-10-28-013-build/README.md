# 0.1.3 candidate: independent package audit

Frozen source `d4415a2b5194abd50c36921a5c7c317ef1f18c51`, **0.1.3 / code4**, `org.sleepwell.app`, arm64-v8a. Independently checked APK: **162,061,389 bytes**, SHA-256 `bfb3015de97e521951c681a80bcc39443ddca489fe0ade89a525a9295890023d`. [Candidate](candidate.json), [aapt badging](package-badging.txt), [signature check](signer.txt).

This task performed read-only package/source checks and wrote docs. It did not build, dispatch CI, publish, install, or operate a phone/provider/window/output. Publication, installation and latest-update metadata remain separately owned by the release owner; this folder makes no publication or installation claim.

The release owner observed `BUILD SUCCESSFUL in 2m3s`, 34 tasks / 10 executed / 24 up-to-date. No raw final build transcript was retained. That attributed terminal observation is distinct from these independently repeated SDK build-tools36.0.0 `aapt`/`apksigner` and uncompressed-ZIP checks; no build was rerun to manufacture a log.

## Bundled files and identity

`apksigner verify --verbose --print-certs` succeeds with one v2 signer, certificate SHA-256 `49eeeb7f74344335321800e3f78b9bb9ab25c5fff059332c458133dca0da0e1c`, identical to the [0.1.2 baseline signer](baseline-signer.txt). This is the existing Android Debug certificate; continuity is not a production-signing-hardening claim.

The comparison APK is frozen0.1.2 source `5bf664434ca303d26069e59287cc0e3c3a55a497`, 166,954,308 bytes, SHA-256 `47ad0a0c11ed7020c4663df0143a990d9cab5eaeb23545398e587c921d475ec1`. Its identity matches the prior [package audit](../2026-10-28-room-bedtime-build/README.md). [package-assets.json](package-assets.json) records original uncompressed sizes and SHA-256 values for every compared entry.

| Section | Result against 0.1.2 |
| --- | --- |
| All65 assets, including models/audio/fonts | Byte-identical; none added, missing or changed |
| All8 native libraries | Byte-identical; arm64-v8a only |
| All481 `res/` entries | Byte-identical; none added, missing or changed |
| `resources.arsc` | Byte-identical, 423,332 bytes |

There are **no text-line-ending exceptions** in this comparison. The smaller APK has no missing entry in these audited sections; retained bundles do not establish execution of every feature or model accuracy. Aapt badging differs only in its package/version line, reports minimum SDK30 / target36 and preserves SleepWell/MainActivity launcher/icon references. Identical resource bytes retain icon payloads without claiming a physical launcher-display test.

[source-equivalence.json](source-equivalence.json) pins unchanged Store/Backup source against0.1.2: database schema17 and records/alarms Backup14 remain unchanged. It also pins eight listed profile/native files byte-identical to the passing private-API verification source. No global MainActivity, whole-app or whole-APK runtime equivalence is inferred from those listed-source comparisons. This docs-only branch has no application-source drift from the frozen candidate.

Portable Appearance profiles use version2 only when they contain the typed Room dimming field; valid legacy version1 profiles remain readable and absence preserves the current local choice. Permission-request markers remain device-local. Records-backup14 still excludes app settings. The profile proof below uses actual private JSON/preferences/journal APIs, including explicit-absence rollback; older omitted-key journal compatibility remains a static code finding. It does not apply window brightness.

## Separately bounded native proofs

| Existing method | Result / originals | Exact evidence boundary |
| --- | --- | --- |
| Calendar Rules permission/draft and Automatic calendar consent continuation | 29.271s / 6 | [CI40607144683](../2026-10-28-calendar-consent-recovery/README.md): native UI/recreation with fake access and private AtomicReference CAS; no OS grant/provider/storage-CAS execution |
| Wake-check waiting/prompt/countdown/reopen | 25.682s / 7 | [CI40607145222](../2026-10-28-wake-challenge-reference/README.md): silent fake-clock/private-state flow; actual alarm admission excluded |
| Ordered routine plus shared Math practice | 48.983s / 14 | [Same two-method run](../2026-10-28-wake-challenge-reference/README.md): local drafts and practice, not alarm runtime |
| Live sonar, settled Pause/recreation and whole-night timeline | 21.564s / 10 | [CI40612296204](../2026-10-28-live-night-settled/README.md): generated PCM/fake duplex, real coordinator/private SQLite and seeded long history; no physical/overnight recording |
| Reviewed Backup replacement plus Room profile API continuation | 15.905s / 8 | [CI40615701815](../2026-10-28-profile-room-dimming/README.md): eight actual Backup frames, then real private JSON/profilev2/v1/stale/journal APIs; no Settings profile Activity/provider/window operation |

Each archive preserves its exact source, selected method(s), original logs/images and limits. The settled-timeline archive contains new loaded paused-history frames; the earlier23.302s loading-state archive remains historical and unchanged. The five named methods above were independently bounded runs, not an aggregate full-app campaign. Earlier0.1.2 evidence remains separately scoped rather than being replayed or expanded by this package check.

No whole-app security, all36-screen/reference completion, real OS/provider delivery, alarm admission, physical audio/haptic/light/sensor behavior, process-death recovery or overnight result is claimed. Package retention, typed preference round-trip and the linked fixture/native results establish their stated boundaries only.
