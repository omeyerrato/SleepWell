# 0.1.23 development package

[Published development release](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.23-ui-reference-20261030-023c5fb7), release ID428911445. Frozen source `023c5fb752be96070cbab28c0386d131c83c70b4`, versionName0.1.23, versionCode24. AAPT reports package `org.sleepwell.app`, Android30 minimum/36 target and `arm64-v8a` native ABI.

`assembleDebug -PsleepwellAbi=arm64-v8a --offline --max-workers=2 --no-daemon` completed successfully in7m7s. No lint, unit suite or broad acceptance task was run for packaging. The source-specific silent native journeys are documented separately in the [36-screen inventory](../../UI-REFERENCE-ACCEPTANCE.md); a successful build does not accept all36 references or full feature parity.

| Uploaded asset | Bytes | SHA-256 |
|---|---:|---|
| SleepWell-0.1.23-023c5fb7-arm64.apk |171758946 | `7d498517640b5fa0d088effe3a4cef9aa960e515d7a99ed94e36ad007ccd13c6` |
| SleepWell-0.1.23-023c5fb7-arm64.apk.sha256 |103 | `b1d70e58cb043035ec12bfa387c87bfd6ea7121d0ca5e67a6106e5deb5e7a337` |

GitHub's release asset metadata returned both assets in uploaded state with these byte counts/digests. The initial create timed out while uploading the APK and left a draft containing the checksum only; a retry uploaded the APK to that same draft. `gh release edit ... --draft=false` then exited successfully with the release URL above. No duplicate release was created. This is upload/digest evidence, not an independent signature audit or device installation.

The later Report fixture-state increment `ed6ccc14733c8357dc43b939149bc5bdb1b3fbe7` changes only instrumentation and its capture manifest, following documentation-only commits. Its `app/src/main` Git tree is byte-identical to the frozen package source: `7763931143d8d8ac4400a9afa7443951a459fa52`. Its later20-original run41051564032 passed89.758s; [separate source-bound evidence](../2026-10-30-reference-resume/report-variants-41051564032/README.md) records the inspected layouts and safe actions. Source identity alone is not acceptance.

The phone was untouched: no ADB discovery, install, app launch or real alarm/audio/vibration/torch output. Last verified historical phone installation is0.1.4 /08d3911e; this release is not an installation claim.

The raster illustrations were created with built-in imagegen. Exact prompts and asset paths are retained in [sound artwork](../../SLEEP-SOUND-ART-ASSETS.md) and [sunrise bedroom](../../SUNRISE-BEDROOM-ASSET.md).

Subsequent authorized phone update: [0.1.23 installation evidence](phone-installation.md). This supersedes the earlier installation-deferred state above; the app was not launched and no output test ran.
