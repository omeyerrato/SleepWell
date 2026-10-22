# SleepWell 0.1.0 alpha

First public development build of an original Android bedtime guard, challenge alarm and private sleep tracker. MIT licensed; all implemented features are free.

- Exact weekday/one-shot alarms, original tones/custom audio, volume ramp, vibration and backup escalation.
- Math, matching pairs, sequence, retype, shake and NFC wake-up challenges, bounded snoozing and optional wake-up check.
- Voluntary app-guard and screen-pinning flows with emergency access.
- Movement/light recording, optional local noise clips, sleep sounds, notes and history graphs.
- CSV/data backup export, validated restore and deletion controls.
- Documentation audit of 129 reference pages and six public implementation/validation workstreams.

**Early alpha: not full feature parity with either reference app.** Sonar, sound classification, camera challenges and wearable/health/smart-home integrations remain pending. No clinical sleep-stage detection or guaranteed wake-up performance is claimed. Use an independent backup alarm while validating this build.

Local build, lint and 13 unit tests pass. The APK installed, a quiet scheduled alarm fired with the S23 Ultra screen off, and a short background movement/light recording saved and closed correctly. See docs/VERIFICATION.md for limits and pending hardware tests.

The attached APK is debug-signed for development. Android 11 or newer required. No Internet permission, account, ads or payment SDK.
