# In-place phone update —1 November2026

The authorized USB phone initially reported0.1.26/code27. No active SleepWell
ServiceRecord was listed before the update. No personal records, audio, backups
or alarm configuration were inspected.

The independently audited APK was copied to
`C:/Users/omeye/Downloads/SleepWell-Night-Dial-0.1.29-1c3ee4f6.apk` and its size and
SHA-256 rechecked after copying:190,495,403bytes,
`151e53bacb2b5f7840c98a07fa6a0ea148e0c53383afc8a84148adb383d641eb`.

The actual command used `adb install --no-incremental -r` with that APK. Its
complete output was:

```text
Performing Streamed Install
Success
```

Immediate package readback returned:

```text
versionCode=30 minSdk=30 targetSdk=36
versionName=0.1.29
lastUpdateTime=2026-11-01 08:42:56
```

The subsequent scoped service listing also contained no SleepWell ServiceRecord.
No app launch, uninstall, data clear, alarm execution, audio, vibration,
flashlight, capture or overnight test was requested by the installation command
or performed afterward. An in-place update was used; this receipt does not claim
an inspection or row-by-row verification of the user's actual data. Constructed
database17/18→19 preservation is separately covered by the native fixtures.

The APK retains the earlier certificate; [signature](signature.txt) and
[package audit](package-audit.json) are separate from installation evidence. No
APK release or repository-default-branch merge was published by this update.
