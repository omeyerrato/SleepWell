# In-place Night Dial installation — 31 October 2026

The authorized in-place update of the original-signer arm64 APK completed with
the literal ADB response **`Success`** from `adb install -r`. No device serial is
published here. This receipt records the completed installation action; it does
not claim a subsequent package readback or launch.

| Identity | Recorded value |
| --- | --- |
| Application | `org.sleepwell.app` |
| Version name / code | `0.1.26` / `27` |
| Source | `351b78bf0436412d8edd889d7eb234be0bf4f8b3` |
| ABI | `arm64-v8a` |
| Delivered file | `SleepWell-Night-Dial-integrated-351b78bf.apk` |
| APK bytes | `184402952` |
| APK SHA256 | `3e73f519ef5b876cb6cd5ae5625530443a34abf4c7d2d53f6eb4edb5821774a6` |
| Signer certificate SHA256 | `49eeeb7f74344335321800e3f78b9bb9ab25c5fff059332c458133dca0da0e1c` |
| Install mode / result | In-place `-r` / literal `Success` |
| Post-install readback | Unavailable: USB disconnected before readback |

The [original build receipt](build-receipt.json) and [signature verification](signing.txt)
pin the artifact and original signer. They predate installation, so their
APK-only delivery/`phone_installed:false` fields remain historical rather than
being silently rewritten.

No uninstall, data clear, app launch, alarm/audio/output execution or individual
personal-data inspection was performed. In-place replacement avoids an explicit
data-removal operation; this is not a row/file/settings preservation audit or
physical/overnight acceptance. Existing native evidence is separately scoped to
22 methods/226 original emulator PNGs. The accepted Night Dial design supersedes
the original Changing Light target, while full43-flow/all-state acceptance stays
open.

Latest public release remains0.1.25. Candidate0.1.27 has not yet been built,
released or installed; newer source fixes and policy evidence do not describe
the artifact installed in this receipt.
