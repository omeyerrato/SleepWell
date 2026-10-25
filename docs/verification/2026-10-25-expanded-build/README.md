# Combined expanded build

Exact source **1275be1**. The combined local run passed in **6m49**:

- **900 pure tests /113 suites**, zero failures, errors or skipped tests.
- Debug APK and native test APK builds.
- Lint: **zero errors, 137 warnings, one hint**.

This includes standard alarm requests, counting/tilt/flag challenges, active-tracking
DND, alarm sound modes/playlists, calendar event drafts, recorded-night insights
and manual tracking delay, in addition to the earlier integrated features.

The original unit XML, lint XML and build transcript are copied byte-for-byte.
SHA256SUMS covers these raw files; `.gitattributes` prevents newline conversion.
The local lint8.13.0 workaround still emits an incompatible LiteRT Kotlin metadata
diagnostic. Successful Java checks do not certify analysis of third-party Kotlin.

Compilation and unit tests are not physical output or native flow acceptance.
Native CI40154059415 at earlier source d346441 passed sound-mode5, insights4 and
DND6 methods with their required originals, but the whole focused run failed
on restored-goal ownership, diary completion and calendar switch input. Those
failures are retained and addressed separately. No full-app or visual approval,
phone alarm, audio, microphone, vibration, flashlight, real DND change, account
exchange or overnight recording is inferred from this build.
