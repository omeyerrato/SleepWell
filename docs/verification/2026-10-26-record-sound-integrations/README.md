# Record access and sound automation source milestone

Production source **c4c7bd2071503c3888b75ad2b86221efea17f3b3** combines isolated
implementation contributions8426e1c4/c4c7bd20 and root hook integration38df15c.

- `:app:assembleDebug` passed in **3m8s**. The original signing certificate SHA256
  is49eeeb7f74344335321800e3f78b9bb9ab25c5fff059332c458133dca0da0e1c.
- APK size **215631924 bytes**; SHA256
  `08e3d7737b9eb7739a7a0fb1f2278bbb29eeec47cc980a4bd76abe7969696d22`.
- [Published prerelease](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-features-20261026-c4c7bd2)
  has exact source target, both assets uploaded, APK size/digest and checksum-sidecar
  digest matching the frozen local artifacts. It is not installed on the phone.
- Sleep database schema17 and metadata13 stay unchanged. The separate private
  automation queue advances1→2 additively, preserving existing event IDs and using
  an empty class for older events. Older builds cannot read the upgraded queue.

[Record contract](../../RECORD-CONTENT-PROVIDER.md) documents explicit grant/revoke,
bounded fields, identity/boot checks, and history replacement invalidation.
[Sound contract](../../CONFIRMED-SOUND-AUTOMATIONS.md) distinguishes player/cue
acknowledgements and persisted recognition from physical sound or model accuracy.

No new unit/lint/native/UI/screenshot/client/provider/recipient/output/microphone
checks ran for this increment. Source inspection and packaging do not establish
actual URI grant behavior, client interoperability, asynchronous lifecycle behavior,
recognition accuracy, migration execution, remote delivery or36screen visual parity.
The earlier Report flow and separately authorized2cbfe043 installation retain their
own narrower evidence. No all-feature or all-screen acceptance claim is made.
