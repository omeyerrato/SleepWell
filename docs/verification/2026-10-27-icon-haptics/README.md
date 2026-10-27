# 0.1.1: launcher icon and crisper tap feedback

Hotfix source `f641cb5298748fc81012d723364eb5b9d02951be`, built from the verified170b0e44 baseline plus only the requested icon/haptics changes. It excludes later unverified Tracking/readiness refinements. Version0.1.1/code2, arm64, original signer; production build passed3m36s. [Candidate](candidate.json), [build](production-build.txt).

The old green launcher tile is replaced by a native adaptive plum/lavender dawn-clock mark, with round and Android13+ monochrome layers. [Editable icon and design renders](../../design/app-icon-2026-10-27/README.md). Those previews are vector renders, not Android screenshots.

Tap/selection feedback requests VIRTUAL_KEY (Android’s CLICK preset) instead of softer TICK/TEXTURE_TICK mappings. Saved OFF, Android settings, foreground/quiet/accessibility guards, user-only switch provenance and80ms rate limit remain. Physical strength is device-tuned and has not been measured. No direct vibrator or settings override is introduced.

All model/audio assets and native libraries remain byte-identical to170b. Only two text assets differ by line endings; the license text and JSON provenance are equivalent. No resource is removed. SQLite17 is unchanged. This small hotfix has not yet been installed because the phone disconnected after the successful170b update; no physical effect was tested.

[Published APK and checksum](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.1-icon-haptics-20261027-f641cb52). Both server asset hashes/sizes and exact source tag are verified: [distribution](distribution.json), [original signer](signer.txt). Phone installation remains pending USB reconnection; installed170b is preserved separately.
