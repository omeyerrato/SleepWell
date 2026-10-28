# Room and bedtime candidate: package verification

Frozen source `5bf664434ca303d26069e59287cc0e3c3a55a497`, **0.1.2 / code 3**, package `org.sleepwell.app`, arm64-v8a. The independently checked APK is **166,954,308 bytes**, SHA-256 `47ad0a0c11ed7020c4663df0143a990d9cab5eaeb23545398e587c921d475ec1`. [Candidate metadata](candidate.json), [aapt badging](package-badging.txt), [signature verification](signer.txt).

This folder records a local candidate. Publication and phone installation are pending and were not performed or verified by this task. It contains no APK, private backup, device data, or private filesystem paths. No build, CI run, device operation or output action was performed to create this evidence.

The release owner observed `BUILD SUCCESSFUL in 1m 8s`, 34 tasks / 4 executed / 30 up-to-date, using Gradle 8.14.3, JDK delta and `assembleDebug -PsleepwellAbi=arm64-v8a --offline --max-workers=2 --no-daemon`. Final stdout was not retained; this is explicitly an attributed terminal observation, not an archived raw build transcript. Package checks below were independently repeated read-only with SDK build-tools 36.0.0.

## Independent package checks

`apksigner verify --verbose --print-certs` succeeds with one v2 signer. Its certificate SHA-256 is the original `49eeeb7f74344335321800e3f78b9bb9ab25c5fff059332c458133dca0da0e1c`, matching the independently verified [f641 hotfix signer](baseline-signer.txt). This is the existing Android Debug certificate; signer continuity is not a production-key-hardening claim.

The comparison baseline is `f641cb5298748fc81012d723364eb5b9d02951be`, APK SHA-256 `d646a6f42cff152c944d195f81652679531c3553b4a1175d5ca3983dcba39332`. [Uncompressed ZIP inventory](package-assets.json) records both sizes and SHA-256 for every compared entry:

| Bundled section | Comparison with f641 |
| --- | --- |
| All 65 assets, including model/audio/font assets | 63 byte-identical; two text-only newline differences; none added or missing |
| All 8 native libraries | Byte-identical; arm64-v8a only; none added or missing |
| All 481 `res/` entries | Byte-identical; none added or missing |
| `resources.arsc` | Byte-identical, 423,332 bytes |

Only `assets/fonts/OFL.txt` and `assets/object_model_PROVENANCE.json` differ, exactly by CRLF versus LF. Equivalence was checked by UTF-8 decoding and replacing CRLF with LF, without stripping whitespace or altering content. Binary model/audio/library/resource bytes were never normalized. The inventory retains both original hashes. These checks establish retention of bundled files, not execution of every feature or model accuracy.

The entire aapt badging output differs from f641 only in its package/version line. It reports version 0.1.2/code 3, minimum SDK 30, target SDK 36, `MainActivity` launcher, SleepWell label and the original launcher resource references. Byte-identical compiled resources preserve the icon payloads; actual launcher presentation was not exercised.

[Source comparisons](source-equivalence.json) pin the frozen candidate and the historical native sources. `Store.java` and `Backup.java` exactly match f641, retaining database schema **17** and backup format **14**. This docs-only branch has no app-source drift from the frozen candidate and excludes the separate future Calendar verification branch. Whole-file differences in older Main/manifest/live/test snapshots are recorded rather than treated as blanket equivalence.

## Named native evidence, each separately bounded

| Existing method scope | Result / originals | Evidence |
| --- | --- | --- |
| Alarm protection and actual deny-only microphone recovery | 35.776s / 13 PNGs | [Readiness](../2026-10-28-readiness-recovery/README.md), CI40587737701 |
| Contactless qualification/save/reopen with generated PCM and fake I/O | 40.989s / 6 PNGs | [Sensing](../2026-10-28-sensing-reference/README.md), CI40587755991 |
| Snoring settings/recreation/private save and fake cue behavior | 14.855s / 5 PNGs | [Sensing](../2026-10-28-sensing-reference/README.md), CI40587755991 |
| Organized settings, Automatic recovery and rendered Tracking light bars | 58.691s / 20 PNGs | [Tracking light glyphs](../2026-10-28-tracking-light-glyphs/README.md), CI40601122904 |
| Live tracking and Tracking options recreation/cancellation | 9.750s / 9 PNGs | [Live](../2026-10-28-organized-live-ci40597518409/README.md), CI40597518409 |
| Motion settings plus Room conditions continuation | 30.873s / 17 PNGs | [Motion/Room](../2026-10-28-motion-room-reference/README.md), CI40601122904 |
| Final Tone label selection / inert preview and import flow | 31.887s / 16 PNGs | Release-owner observed latest result; separately owned original archive pending |
| Final Guard label selection / Wind-down navigation | 24.657s / 8 PNGs | Release-owner observed latest result; separately owned original archive pending |

Historical combined jobs CI40597518409 and CI40601122904 failed separate methods; their archives preserve those failures. The later label-flow result does not retroactively change their raw summaries. This table is not an aggregate full-app acceptance run. Each linked archive defines its own fake ports, private preferences/records, actual native inputs, OS-permission boundary and cleanup assertions.

The live chart still shows a rolling 15-second interval rather than the reference's whole-night axis. Supporting layouts, illustration/proportions, some cream-page system-bar contrast, font/device variants and full 36-screen reference acceptance remain open. This package evidence establishes neither whole-app security nor physical alarm/audio/haptic/light/sensor/overnight behavior. No actual output or provider behavior is inferred from retained bundles or injected fixtures.
