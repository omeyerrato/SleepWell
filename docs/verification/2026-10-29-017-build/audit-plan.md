# 0.1.7 package audit: baseline preparation

The final 0.1.7 source, APK and build transcript have not been supplied to this
audit. No frozen candidate or package acceptance is claimed here.

The baseline is published 0.1.6 source
`741b3718032ab4dc1d52eb67cf2194d4c59884f9`, version code 7. Its local APK is
163,250,139 bytes with SHA-256
`3e44d6899aa52ccc567de983b91c70765c5cbdc00562144a4bbdc04125940459`.
The expected original signing certificate SHA-256 is
`49eeeb7f74344335321800e3f78b9bb9ab25c5fff059332c458133dca0da0e1c`.
The published provenance is archived in
[the 0.1.6 distribution record](../2026-10-29-016-build/distribution.json).

Preparation independently refreshes SDK 36 signing verification, badging and
the complete compiled manifest against the actual baseline APK. The complete
ZIP index records each uncompressed entry's size, SHA-256 and CRC, with native
ELF headers. These are package-integrity observations; they do not establish
hardware, model, playback or feature execution.

For the supplied final candidate, the bounded comparison will check:

- Exact APK identity and owner-provided build/source association; version
  0.1.7 / code 8, original signer, package ID, SDK levels and arm64 ABI.
- Complete compiled manifest declarations against the baseline and exact
  frozen source. Expected additions must be private Activities declared in
  that source. The preparatory Root snapshot `0d30ff69675d5066dd40128c0a8ae0e53a2b3086`
  adds MotionChallengePracticeActivity, RoutineAudioPracticeActivity and
  ChallengePracticeActivity. This snapshot is not a frozen candidate; any
  later RoutineCameraPracticeActivity addition will be assessed from the
  supplied final source. Permission, exported/access, provider, URI/grant,
  launcher, feature and SDK declarations will receive the same exact diff.
- One candidate ZIP pass against the prepared baseline index, including all
  65 assets and 8 arm64 native libraries. All 775 `res/` entries and
  `resources.arsc` will be compared as exact bytes. Unchanged resources will
  reuse that evidence without repeating their XML resolution.
- Frozen Git trees/blobs and build/dependency differences, retaining Store
  schema 17 and Backup format 14, and naming any actual source changes.
- A sanitized copy of the supplied original build transcript with exact
  original/public hashes and explicit sanitization boundaries.

No rebuild, test batch, CI dispatch, installation, phone/device operation,
release or metadata publication is part of this audit. Source-to-DEX
behavioral equivalence and whole-app or all-reference-screen acceptance are
outside its scope. Only this verification folder is intended for integration.
