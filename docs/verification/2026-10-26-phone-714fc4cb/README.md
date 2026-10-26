# Authorized phone update — 26 October 2026

The user explicitly requested an update after reconnecting the Galaxy S23 Ultra.
ADB streamed `install -r` returned **Success** for original-signed source
`714fc4cb976f4bc0e3f578c1717378ec1ef730b1`.

- Installed base APK SHA256 matches the frozen published APK:
  `01fcfb2e69e01dddbec2d10495e2aa1142de43dc2b576a5ce90b53927229a22b`.
- APK size: **215567816 bytes**; original signer SHA256:
  `49eeeb7f74344335321800e3f78b9bb9ab25c5fff059332c458133dca0da0e1c`.
- Private before/after snapshots both use database **schema17**, each SQLite quick
  check succeeds. Every original table row/column, preference value and user file
  matches. This is one alarm, one session and four epochs; no clips were present.
- The sole private file difference is the 24-byte AndroidX `files/profileInstalled`
  cache: only `mPackageLastUpdateTime` changed. Cache schema/result/profile size are
  identical, confirmed against the installed ProfileVerifier.Cache class layout.
  It was not overwritten or treated as a changed sleep record.
- The prior installed APK and private data backups remain locally private under
  `.device-backups/update-20261026-092520`; no personal data is committed here.

No app launch, suspension removal, alarm, audio, vibration, flashlight, microphone,
tracking or provider test was performed. Existing schema17 proves neither that this
installation ran a migration nor functional/overnight acceptance. Later source
builds are not implicitly installed by this authorization.
