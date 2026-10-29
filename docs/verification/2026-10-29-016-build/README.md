# 0.1.6: independent frozen package audit

Frozen source **`741b3718032ab4dc1d52eb67cf2194d4c59884f9`**, version
**0.1.6 / code7**, app tree `ed2b45fb19ce648973b9e2a82a2bec0620b00879`.
Independently read and hashed the frozen APK: **163,250,139 bytes**, SHA-256
**`3e44d6899aa52ccc567de983b91c70765c5cbdc00562144a4bbdc04125940459`**.
[Independent result](independent-candidate.json),
[sanitized frozen owner record](owner-frozen-record-sanitized.json).
No package issue was found in the bounded checks below.

The published0.1.5 baseline is `ef69408258e900a50274d6f2488e859647138714`,
**163,200,933 bytes**, independently rehashed SHA-256
`41447d6bce55abb20ce504d44dd21dfee949a88f68ee7e7633e160cebef4cdde`, matching
[its archived published record](../2026-10-28-015-build/distribution.json).

## Signer, manifest and retained contents

SDK36 `apksigner verify --verbose --print-certs` independently verifies both
APKs with one v2 signer and the original certificate SHA-256
**`49eeeb7f74344335321800e3f78b9bb9ab25c5fff059332c458133dca0da0e1c`**.
[Current signer](package-signer.txt), [baseline signer](baseline-signer.txt).
Aapt confirms `org.sleepwell.app`, minSDK30, targetSDK36 and **arm64-v8a only**;
[current badging](package-badging.txt), [baseline badging](baseline-badging.txt).

The complete compiled [current manifest](package-manifest-tree.txt) and
[baseline manifest](baseline-manifest-tree.txt) differ only in versionCode6→7
and versionName0.1.5→0.1.6. Removing only those two named attribute lines yields
exact equality: **no component, exported/access attribute, permission, URI/grant
attribute, feature, launcher or SDK declaration drift**. The exact difference
is retained in [manifest-diff.patch](manifest-diff.patch). This checks declared
package policy; it is not a new audit of every handler or runtime authorization.

| Actual package contents | Independent comparison with0.1.5 |
| --- | --- |
| **65 assets** | Identical names and exact uncompressed bytes, including audio, all bundled models, Inter font and licenses. No newline-equivalence exception was needed. |
| **8 native libraries** | Identical names and uncompressed bytes. All under arm64-v8a, with ELF64/AArch64 headers. |
| **775 `res/` entries** | Identical names and exact bytes; none added, removed or changed. `resources.arsc` also matches exactly. No repeated resource-XML resolution was needed. |
| ZIP entries |947→948: one added, zero removed, four changed and943 content-identical at the same names. Names are unique and CRC reads pass. |

[All73 retained asset/library hashes](retained-assets-native.json) and
[complete entry delta](zip-entry-delta.json) derive from actual uncompressed
bytes. Package inclusion and integrity do not establish successful model,
provider, playback or hardware execution.

The added ZIP entry is `classes6.dex`. Existing `classes4.dex` and `classes5.dex`
move to `classes5.dex` and `classes6.dex` with exact original payloads; the new
612-byte `classes4.dex` contains `com.android.tools.r8.RecordTag`. `classes.dex`
and `classes2.dex` remain byte-identical; application `classes3.dex` changes.
A bounded DEX identity read finds24,691→24,738 classes, none removed;46 added
identities are app-owned and the other is RecordTag. [DEX layout](dex-layout.json)
records counts/hashes/identities only, without method bodies or vendor
implementation listings. This is not source-to-DEX behavioral equivalence.

The APK grows **49,206 bytes**. [Physical ZIP accounting](zip-layout.json)
reconciles it exactly: compressed payload +53,883, referenced local
header/name/extra bytes −4,735, central directory +58; both4096-byte signing
blocks and22-byte end records are unchanged, with no unreferenced gaps.
No retained feature asset/library was removed to produce this size change.

## Frozen source and native-proof boundaries

[Source boundaries](source-boundaries.json) pin exact Git blobs/trees. Twelve
Java files change from0.1.5, covering Search/alarm-editor arrivals, challenge
configuration, Automation and Protection UI. Only versionCode/versionName
change in app/build.gradle; the remaining build configuration/dependencies
match. Source assets/res trees and the complete source manifest are unchanged.
**Store remains schema17 and records/alarms Backup remains format14**; their
entire source files, including existing migration/legacy-admission paths, match
0.1.5. No user-data migration or import/export was executed by this audit.

| Separate evidence | Relationship to frozen741 |
| --- | --- |
| [Merged Search/Challenge](../2026-10-29-editor-usability-integration/README.md),CI40792566584 at669cc50f | Two existing methods/36 original images; archive reports individual inspection and69.905s/33.847s method passes. Across all production app/src/main files, only AlarmProtectionActivity differs from this tested source; build changes are version metadata only. Both exact native source files match. These36 images are not36-screen acceptance. |
| [Automation cards](../2026-10-29-automation-rule-cards/README.md),CI40777798244 at0cadd897 |33.903s/one method/10 originals. Named Activity/store/transport/operation/UI-helper source matches frozen741. Private-store/fake scheduling; no external-delivery/Keystore/JobScheduler/process-death claim. |
| [Protection recovery](../2026-10-29-protection-recovery/README.md),CI40790536650 at1fdaf354 |35.916s/one method/16 originals. Named Activity/policy/UI-helper source matches frozen741. This separately covers the Protection change outside the merged editor proof. Access-enabled/live guard/real alarm and nonzero-scroll restoration remain unverified. |

This package audit does not replay or aggregate those journeys into whole-app
acceptance. The version bump was not part of their runtime runs, and it does not
justify claiming whole-source equality to either historical proof head.

## Build provenance and limits

The original Root transcript prints **BUILD SUCCESSFUL in5m30s** with
**34 tasks/11 executed/23 up to date**. Original SHA-256 is
`15532263f1424e1d0a72d7b9fbed5d6791b69efd7097b930254a804578df6f16`.
The [public transcript](build-sanitized.txt) replaces only one local workspace
prefix in its generated Problems-report URL; all other bytes are unchanged.
[Sanitization record](build-log-sanitization.json) records both hashes and the
single replacement. The original remains private. The owner record's two
absolute path fields are likewise replaced by archive basenames and its
original digest is retained. Its publication/install flags describe the frozen
checkpoint, not continuing release status. Root reported333.266s wall time;
this auditor neither ran nor timed a build.

No rebuild, test/native run, lint, CI dispatch, phone/ADB/device operation,
output, install, release or latest-metadata update was performed. Only this
audit folder changes. Source/package association comes from the frozen owner
record and build evidence, not an independent reproducible build or proven
source-to-DEX equality. Physical alarm/tracking/audio/light/overnight behavior,
all screen/theme/device states, model/provider performance, broad security and
the existing global SystemUI limits remain outside this package audit.
[SHA256SUMS](SHA256SUMS) covers every other archive file.
