# 0.1.8: independent frozen package audit

**Bounded package checks pass.** Frozen source `0b3cb9ed78fcb36d1a899c8c1dc56d0060387c59`, **0.1.8 / code 9**, app tree `377209b488d3a738d33790c6b800fc4b3e6749ca`, production tree `a6f297d6c9bb9b44315a555abd3585c5cbd59c99`. The actual APK independently matches **163,999,264 bytes**, SHA-256 **`247ea0e815d842a10850a779c9428a739d780abaa83f364328b1593fd899f8ae`**. No issue was found in the bounded package checks below. [Independent result](independent-candidate.json), [owner association](owner-supplied-record.json).

The published **0.1.7 / code 8** baseline is source `02aacc6b122cb1e2c50486190ece608f8d5e3e87`, 168,752,618 bytes, SHA-256 `7aa7e4fb3db0ec298a00459e83443f1342649e00fb3923396d867d2d83383143`. The preparation refreshed its full APK hash and reused ten prior audit records byte-for-byte after checking their archived SHA256SUMS. Its 948-entry content/CRC index and compiled declarations were not rebuilt. [Historical baseline preparation](baseline-preparation.md), [reuse record](baseline-record.json), [previous audit](../2026-10-29-017-build/README.md). The preparation's candidate-pending fields describe that earlier stage; the independent result above supersedes them.

## Signer and declarations

SDK 36 apksigner verifies one original signer, certificate SHA-256 **`49eeeb7f74344335321800e3f78b9bb9ab25c5fff059332c458133dca0da0e1c`**, with APK Signature Scheme v2. [Signer](package-signer.txt). Aapt confirms `org.sleepwell.app`, minSDK 30, targetSDK 36 and arm64-v8a only. [Badging](package-badging.txt).

The **entire compiled manifest tree is equal after only versionCode/versionName are removed**. There are no added or removed components; every existing Activity/service/receiver/provider subtree, permission, exported/access, grant/URI, launcher, feature and SDK declaration is unchanged. The unused Exposure Notifications WakeUpService remains absent. [Comparison and counts](manifest-comparison.json), [two-attribute diff](manifest-diff.patch), [full tree](package-manifest-tree.txt). This checks compiled declarations, not every IPC handler's runtime authorization.

## Package contents and resources

One full candidate uncompressed-entry pass against the reused 0.1.7 index found **949 unique names, all CRC reads passing**: one added entry, no removed entries, seven changed entries and 941 byte-identical entries. [Full candidate index](candidate-zip-index.json), [entry delta](zip-entry-delta.json).

| Content | Result |
| --- | --- |
| 65 assets | Every name and byte retained, including audio, fonts, models, labels and licenses. |
| Eight native libraries | Every name and byte retained; ELF64 little-endian AArch64, arm64-v8a only. |
| 775 existing resource entries | 772 byte-identical; only three notification XML files differ in resource IDs. |
| Added resource | `res/drawable-nodpi-v4/motion_sensor_handset.png`, 502,933 bytes; exact source bytes/SHA-256 `6f7ae09ce1c38ac8b0caba5e32b288613589bd94c3625d06ddf6147663c36420`. |
| Other changed entries | AndroidManifest.xml, classes2.dex, classes3.dex and resources.arsc. |

[All 73 asset/library hashes and native headers](retained-assets-native.json) and [DEX sizes/hashes/class-definition counts](dex-layout.json) are archived. DEX inspection does not establish source-to-DEX behavioral equivalence.

Only the three actually changed XML files were re-resolved: notification_bg.xml, notification_bg_low.xml and notification_tile_bg.xml. Their complete trees are equal when numeric app resource IDs are replaced with names from each APK's own table. **No unchanged resource XML was re-resolved.** The full resource table contains exactly one new named resource. Its complete values match after resolving app IDs, normalizing string-pool index numbers while retaining every following actual string, and removing only the new handset spec/value/path plus the drawable entry-count increment from 217 to 218. No other fields are normalized. [Detailed normalization and hashes](resource-comparison.json), [small resulting table diff](resource-table-diff.patch). The two complete raw SDK table dumps are retained losslessly as [baseline gzip](baseline-resources.txt.gz) and [candidate gzip](package-resources.txt.gz); each changed XML tree is archived separately. The existing home-widget payload is byte-identical, so it was not re-resolved.

## Size accounting

The APK is **4,753,354 bytes smaller** than 0.1.7. The difference reconciles exactly: compressed referenced payload **+515,945**, referenced local header/name/extra bytes **+3,605**, central directory **+93**, and prior unreferenced zero-payload local-header/extra padding **−5,272,997**. Signing-block and end-record sizes are unchanged. The candidate has **zero unreferenced gaps** and no data descriptors. [Physical accounting](zip-layout.json), [candidate gap record](zip-unreferenced-regions.json), [prior padding evidence](../2026-10-29-017-build/zip-unreferenced-regions.json). No APK was repacked or rebuilt.

## Source and runtime-proof boundaries

Exactly eight app production/version paths differ from 0.1.7: version metadata; ExternalMediaFade; MainActivity; MotionSensorActivity; MotionSignalChart; new PrivacyScreen/PrivacyStorageSummary helpers; and the handset PNG. App build changes are version-only, all other build/dependency configuration is unchanged, and the source manifest and assets tree are exact matches. **Store remains schema 17 and Backup format 14**, with their entire files byte-identical. No database/backup migration or restore was executed. [Source pins and unchanged contracts](source-boundaries.json).

This candidate composes two separately accepted private-fixture proofs, with exact candidate source pins:

- [Motion run 40862856617](https://github.com/omeyerrato/SleepWell/actions/runs/40862856617): one existing method, **22.359 seconds**, seven originals. All four named app/native source paths match. Injected accelerometer observations exercise the real chart, configuration, lifecycle and persistence path. The same agent individually inspected those seven frames during the Motion task; that earlier archive also retains the original failed attempt.
- [Privacy run 40864908326](https://github.com/omeyerrato/SleepWell/actions/runs/40864908326): one existing method, **75.212 seconds**, twelve actual originals: five script-required history frames plus seven additional top/supporting frames. All eleven app/native source pins match. Its owner/Root individually inspected the twelve originals. This package audit verified their archive hashes, source pins, original environment/log/summary and reported scope; it did not repeat the image inspection or private runtime journey.

[Proof boundaries](native-proof-boundaries.json) record all named pins and the raw original-file hash checks. The two flow environments are distinct; no new combined runtime or broad replay is claimed. The early API <33 rejection in ExternalMediaFade is a separate **source-only** addition before native listener construction; its native path was not exercised by either proof. [Exact source patch](external-media-guard-source.patch). Prior 0.1.7 qualifications for native guards and actual Back gestures remain. The owner candidate JSON originally described Privacy visual review as pending; the later Root handoff and completed Privacy archive supersede that earlier scope text, retained explicitly in the association record.

## Build provenance and limits

The original Root transcript records **assembleDebug + lintDebug SUCCESS in 11m43s**, **45 tasks / 24 executed / 21 up-to-date**. Root reports helper wall time 720.688 seconds; the auditor neither built nor timed it. [Sanitized transcript](build-sanitized.txt) replaces only the local workspace prefix; original/public hashes and replacement counts are in [sanitization record](build-log-sanitization.json). The final lint XML reports **zero errors, 361 warnings and one hint**, with report hash and issue counts in [lint status](lint-status.json). No zero-warning claim is made.

Only this documentation folder changes. No rebuild, tests, CI dispatch, physical device/ADB/phone, output, provider write, installation, release or latest-metadata update was performed by this audit. Source/package association relies on the Root handoff and original build evidence, not a reproducible independent build. Package integrity and the two bounded private-fixture proofs do not certify real hardware/sensors, calibration, alarm admission/outputs, providers, overnight/model accuracy, actual system Back gestures, broad security, all devices/themes/fonts or all 36 reference screens. Inherited Motion SystemUI glyph-contrast qualifications remain. [SHA256SUMS](SHA256SUMS) covers every other archive file.
