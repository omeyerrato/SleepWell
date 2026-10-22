# Verification record

Date: 2026-10-22 (Asia/Riyadh).

## Local build

JDK 21, Android SDK 36, Android Gradle Plugin 8.12.0, Gradle 8.14.3.

```text
testDebugUnitTest lintDebug assembleDebug
BUILD SUCCESSFUL
13 unit tests; 0 failures; 0 errors
```

Lint is enabled with abort-on-error. Initial missing-microphone-permission, legacy-back-navigation and local Windows property-escaping errors were fixed. Remaining warnings include English-only UI strings, known version-update notices and older-API compatibility annotations; the build is not described as warning-free.

Unit tests cover next-occurrence scheduling across midnight, exact-minute boundaries, weekday/weekend transitions, a DST clock gap, overnight/daytime bedtime windows, smart-window/activity/sample prerequisites, PCM RMS normalization, bounded duration arithmetic, CSV quote/newline escaping, sample-weighted noise RMS and missing-versus-silent audio windows.

## Physical-device smoke test

Galaxy S23 Ultra (SM-S918B), Android API 36, connected through authorized USB debugging.

- Debug APK installed successfully and launched.
- Tonight and Alarms screens inspected through Android layout output; Tonight screenshot visually checked.
- Notifications, exact-alarm and full-screen-alarm permissions granted for testing.
- Quiet preview scheduled for ten seconds; AlarmManager dump confirmed the pending wake-up alarm.
- Device screen put to sleep. The preview fired and the Math challenge screen appeared over the lock screen.
- No AndroidRuntime crash output observed during the launch/alarm smoke test.
- A 232-second movement-only recording continued with the screen off, saved four epochs with 258–294 accelerometer samples per epoch and ambient light readings, and stopped via the notification action. SQLite integrity check returned `ok`; the session was closed with `interrupted=0`. No microphone audio was collected in this test.
- The test alarm subsequently stopped and the device returned to keyguard; automated completion of its three challenge rounds was **not observed**, so it is not counted as a passing challenge-completion test.

## Not established by these checks

The screen-off preview does not prove overnight, reboot or Doze reliability. Volume changes were configured in code; subjective loudness and the ability to wake this user were not measured. The optional accessibility guard, NFC matching, backup escalation, wake-up check, audio recording/classification, full-night battery consumption, export/restore, and every challenge have not all been verified end to end on hardware.

The device became locked during further testing. Additional device interaction is pending an unlocked test session. No recordings or personal sleep data are published in this repository.

See [issue 1](https://github.com/omeyerrato/SleepWell/issues/1) for overnight/device validation and [FEATURES.md](FEATURES.md) for the complete implementation audit. GitHub Actions builds and uploads an APK independently of this local machine; a green run must be checked separately rather than inferred from local success. The independent [CI run for 569d7b6](https://github.com/omeyerrato/SleepWell/actions/runs/39739469746) passed; subsequent commits have their own runs.
