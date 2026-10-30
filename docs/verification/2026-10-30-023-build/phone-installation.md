# 0.1.23 phone update —30October2026

The user reconnected and authorized the Galaxy S23 Ultra (SM-S918B). ADB reported it authorized. Before installation, Android reported `org.sleepwell.app` version0.1.22 /23; this supersedes the older0.1.4 installation record as the observed starting version.

The local arm64 APK SHA-256 matched the frozen published0.1.23 source `023c5fb752be96070cbab28c0386d131c83c70b4`: `7d498517640b5fa0d088effe3a4cef9aa960e515d7a99ed94e36ad007ccd13c6`.

`adb install -r` returned `Success`. Android then reported:

```text
versionCode=24 minSdk=30 targetSdk=36
versionName=0.1.23
lastUpdateTime=2026-10-30 18:38:47
firstInstallTime=2026-10-22 21:48:54
```

This was an in-place update. No uninstall or app-data clear occurred, and Android retained the original first-install time. Individual records/settings/files were not opened or compared during this installation; this is not a separate data-integrity audit.

The app was not launched. No alarm, sound, vibration, flashlight, protection or tracking test ran. Successful installation does not establish all36 design acceptance, overnight performance or hardware behavior. The earlier silent-emulator evidence remains separately scoped.
