# 0.1.7: independent frozen package audit

Frozen source **`02aacc6b122cb1e2c50486190ece608f8d5e3e87`**, version
**0.1.7 / code 8**, app tree `6ebd894db19c224ee83cf39a4e483b570107a98a`,
production tree `f6180f2b5f395826833bf59d7d3b13e301a7497f`.
The actual APK independently hashes to **168,752,618 bytes**, SHA-256
**`7aa7e4fb3db0ec298a00459e83443f1342649e00fb3923396d867d2d83383143`**.
No package issue was found in the bounded checks below.
[Independent result](independent-candidate.json),
[owner-supplied association](owner-supplied-record.json).

The published 0.1.6 baseline is source
`741b3718032ab4dc1d52eb67cf2194d4c59884f9`, **163,250,139 bytes**, SHA-256
`3e44d6899aa52ccc567de983b91c70765c5cbdc00562144a4bbdc04125940459`.
Preparation independently rehashed it and refreshed its SDK 36 signer,
badging and compiled manifest, matching the archived 0.1.6 outputs exactly.
[Baseline record](baseline-record.json).

## Signer and compiled declarations

SDK 36 `apksigner verify --verbose --print-certs` verifies the candidate with
one original signer, certificate SHA-256
**`49eeeb7f74344335321800e3f78b9bb9ab25c5fff059332c458133dca0da0e1c`**.
[Candidate signer](package-signer.txt), [baseline signer](baseline-signer.txt).
Aapt confirms `org.sleepwell.app`, minSDK 30, targetSDK 36 and **arm64-v8a
only**. [Candidate badging](package-badging.txt).

The complete compiled manifest differs only in version code/name and four
new nonexported Activities: ChallengePracticeActivity,
MotionChallengePracticeActivity, RoutineAudioPracticeActivity and
RoutineCameraPracticeActivity. All existing component trees and the remaining
full declaration tree are equal after only those named additions/version
attributes are removed. Permission, exported/access, provider, URI/grant,
launcher, feature and SDK policy has no other compiled drift. The unused
Exposure Notifications WakeUpService remains absent.
[Exact comparison](manifest-comparison.json),
[manifest diff](manifest-diff.patch),
[complete candidate tree](package-manifest-tree.txt).
This checks declared package policy, not every handler's runtime authorization.

## Retained content and size

| Actual package content | Comparison with 0.1.6 |
| --- | --- |
| **65 assets** | Identical names and exact uncompressed bytes, including audio, fonts, model files, label maps and licenses. |
| **8 native libraries** | Identical names and bytes; all arm64-v8a with ELF64/AArch64 headers. |
| **775 resource entries** | 774 exact matches. Only `res/layout/home_widget.xml` differs, solely in 22 binary XML source-line words. |
| `resources.arsc` | Exact byte match. |
| **948 referenced ZIP entries** | Unique names; every uncompressed read passes CRC. Zero entries added/removed, three changed, 945 byte-identical. |

[All 73 retained asset/library hashes](retained-assets-native.json),
[full entry delta](zip-entry-delta.json),
[candidate index](candidate-zip-index.json).
The one changed widget XML received a bounded tree/binary comparison. Its
complete aapt trees are equal after source-line positions are removed, and
its binary bytes are exact after only the 22 line-number words are zeroed.
The two `tools:ignore` attributes are stripped from the package; native tint,
layout and all runtime attributes are unchanged. None of the other 774
resource entries was re-resolved. [Resource comparison](resource-comparison.json),
[widget tree diff](home-widget-diff.patch).

The other changed entries are AndroidManifest.xml and `classes3.dex`.
`classes3.dex` grows by 225,304 uncompressed bytes; the other five DEX payloads
remain exact matches. [DEX sizes, hashes and class-definition counts](dex-layout.json)
do not establish source-to-DEX behavioral equivalence.

The **5,502,479-byte** APK increase reconciles exactly: compressed referenced
payload **+225,408**, local header/name/extra bytes **+4,074**, and
unreferenced local ZIP padding **+5,272,997**. Signing block, central directory
and end-record sizes are unchanged. The two padding regions parse entirely
as 82 zero-name, zero-payload local header records plus 5,270,537 zero-filled
extra bytes. They contain no additional entry payloads; their offsets, hashes
and accounting are retained. [Physical ZIP accounting](zip-layout.json),
[padding regions](zip-unreferenced-regions.json). The audit reports that space
explicitly and did not rebuild or repack the APK.

## Frozen source and proof boundaries

[Source boundaries](source-boundaries.json) pin all 70 changed production/version
paths against 0.1.6. Build dependencies/configuration are unchanged except
version metadata; the assets tree is exact. **Store remains schema 17 and
Backup remains format 14**, with their entire files byte-identical to the
baseline. The only source resource change is the documented widget lint
metadata/comment. No migration, backup restore or import/export was executed.

The separately accepted [camera run 40844350768](https://github.com/omeyerrato/SleepWell/actions/runs/40844350768)
passed one existing method in **53.322 seconds** with 24 originals at
`f88fe04acf92d4c8e3cd25c3d565c1a2f1e54ab7`. Its injected observations exercise
real rules/controllers and the cleanup state machine, rather than physical
camera or detector/model execution. Eighteen of 20 named camera production
paths match that source exactly. The two later changes are the parent
Activity's ComponentActivity/back-dispatcher migration and the manifest's
scoped lint suppression/comment; their source differences are explicit in
[native-proof boundaries](native-proof-boundaries.json).

Nine Activities subsequently migrated to lifecycle back dispatchers, and five
native call sites gained permission/API checks. These changes received bounded
source review and the combined compile/lint; the affected native API guards
and actual back gestures were not exercised by the older proofs.
The later [audio run 40853867058](https://github.com/omeyerrato/SleepWell/actions/runs/40853867058)
passed one existing method in **32.298 seconds** with 26 originals at
`2d3891e199d5015b8010507827661bb315311421`, whose app/production trees match
this candidate. It covers private parent/audio lifecycle and UI actions after
the migration, using injected inputs. Its fixture does not press Back or invoke
the back callback, so it adds **no actual back-gesture acceptance**. Native API
guard execution, physical hardware and model accuracy remain outside these
injected proofs. This package audit did not replay either method or inspect
their screenshot sets. The camera SystemUI status-glyph contrast qualification
remains; all 36 reference screens are not accepted by this audit.

## Build provenance and limits

The original Root transcript records **assembleDebug + lintDebug SUCCESS in
8m4s**, **45 tasks / 20 executed / 25 up-to-date**. Its original SHA-256 is
`73875d5461eb4e76f870dd09c1a15942162cfb617fac029d8e3463400117fc95`.
The [public transcript](build-sanitized.txt) replaces only two occurrences of
the local workspace prefix; every other byte is unchanged.
[Sanitization record](build-log-sanitization.json). Root reported 496.281 seconds
wall time; this auditor neither built nor timed it. The final lint XML has
**zero errors, 357 warnings and one hint**, recorded without a zero-warning
claim in [lint status](lint-status.json).

The earlier `8ba95c95` attempt compiled an APK but failed lint; it is not an
accepted candidate. Its sanitized [failed transcript](prior-failed-build-sanitized.txt)
and original/public hashes are retained separately. The baseline-preparation
record and this final audit do not silently substitute that stale APK.

No rebuild, test batch, CI dispatch, device/ADB/phone operation, output,
installation, release or latest-metadata update was performed by this audit.
Only this folder changes. The frozen source/package association relies on the
Root handoff and original build evidence, not an independent reproducible
build. This is not broad security certification, physical/overnight acceptance,
whole-app acceptance or completion of all 36 reference screens.
[SHA256SUMS](SHA256SUMS) covers every other archive file.
