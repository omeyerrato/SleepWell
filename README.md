# SleepWell

An original, open-source Android app for protecting bedtime and making mornings harder to sleep through. All implemented features are free. No ads, accounts, subscriptions, or paid unlocks.

**Status: early development. This is not full feature parity with Sleep as Android or Challenges Alarm Clock.** The [feature audit](docs/FEATURES.md) records what exists, how it works, and what remains. Do not replace a dependable backup alarm until this app has been tested overnight on your device.

The current phone-only build follows the supplied Changing light designs. See the
[phone feature matrix](docs/FEATURE-MATRIX.md), [persistent plan](docs/IMPLEMENTATION-PLAN.md),
and [strict visual review](docs/DESIGN-REVIEW.md). The first UI pass was rejected;
the new root compositions are corrective work, not final acceptance of the 36 screens.
Wearables and watch companions are excluded from this scope.

## Current development update — 27 October

Latest published **0.1.8 / 0b3cb9ed** improves the Privacy storage/backup layout and Motion sensor artwork/chart/controls. [APK and checksum](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.8-reference-layouts-20261029-0b3cb9ed); [package audit](docs/verification/2026-10-29-018-build/README.md); [Privacy12 proof](docs/verification/2026-10-29-privacy-reference/README.md); [Motion7 proof](docs/verification/2026-10-29-motion-reference/README.md). The phone remains on 0.1.4; installation is deferred. Audio-only bulk deletion, a durable manual Last export receipt and broader design/security/hardware work remain unfinished.

Feature baseline **170b0e44** includes real backup tone/volume, corrected rails,
Gentle's quiet sample, clearer Tone/QR/Nap/rhythm layouts and the scoped
Health/Privacy/document/Cloud repairs. Original-signer arm64 build passed in
1m22s; remote tag and both uploaded hashes are verified. Selected native flows
exercise actions, cancellation, recreation and persistence. [Exact release scope](docs/verification/2026-10-27-reference-menus-build/README.md).

The last verified phone installation is **0.1.4 / 08d3911e**. No physical output test ran. All36 reference states and broader interaction/security acceptance remain open; newer source is not automatically installed on the phone.

## Working capabilities

- Multiple one-shot or weekday alarms using Android's exact `setAlarmClock` API, rescheduled on reboot, clock/time-zone changes, and updates.
- Original Daybreak and Pulse alarm tones, Android's default alarm sound, or your own audio file; configurable alarm-stream volume, gradual ramp, vibration, and optional loud backup escalation.
- Math, matching pairs, sequence, retyping, shaking, and enrolled NFC challenges. Configurable rounds, limited snoozes, optional screen pinning, and post-dismissal wake-up checks.
- Additional saved tasks include graded arithmetic, QR/barcodes, walking, mirror text, dream diary, visual search, counting, flags, tilt maze, broad-speech detection, picture similarity and configurable local object classification. The matrix distinguishes real implementation, historical fixture checks and unverified hardware behavior; reliable laughter/instrument recognition and object-camera acceptance remain incomplete.
- Optional accessibility-based return to the active alarm, plus a bedtime guard for explicitly selected distracting apps. Calls and Android Settings remain available. Developer stop is shown only in debug builds.
- Native user-started tracking setup and live movement, same-session pause/resume, excluded paused duration and saved reports. Optional light/noise capture; real measured graphs and local history. Only short movement-only flows have current device verification.
- An experimental early wake-up window based on sustained movement, with the fixed alarm scheduled independently. Movement is not a measured sleep stage.
- Optional offline sound estimates, event browsing/correction/favorites and bounded noise-triggered clips. Per-class accepted duration and inclusion are frozen per night; custom inclusion suppresses all new clips. Shared contactless analysis uses one capture; separately chosen per-night clips save filtered16kHz WAVs, while48kHz raw sonar is not retained. Sound estimates and fixture replay do not establish representative bedroom accuracy. See [analysis limits](docs/SOUND-ANALYSIS.md).
- Native Sleep sounds player and two-layer mixer source, per-layer gains, playing-time timer/fade and local audio import. Silent model tests and the API36 configuration/persistence flow pass, with a bounded screen 19 visual comparison. A short real MediaPlayer/background/notification/focus/error-retry journey also passes on the silent CI emulator. Provider access, listening quality, long lifecycle and S23 installation remain pending. See [playback limits](docs/SOUND-PLAYBACK.md). Bedtime reminders, nap alarms, recorded-duration goals, notes, tags and subjective ratings also exist.
- Native history search/date/context filters; manual periods and measured-record trims, awake edits, notes/tags/ratings, restore-original and selected-record deletion. Calendar Week/Month/Year duration and bedtime charts merge overlaps and leave missing dates unknown.
- Native linked sleep plan and free 14/30-day duration/bedtime goal bank with observed balances, motivation and preserved stopped history. Missing days remain unknown; these preferences are currently excluded from portable backups.
- CSV export, versioned JSON data backup, validated transactional restore, and deletion of local history.
- Saved Changing light colors for Tonight (automatic local-clock phases or fixed Midnight/Pre-dawn/Dawn), text-size options, high-contrast controls and reduced-motion press feedback. Optional Home Assistant light controls require a separately configured compatible server and lamp; physical exchange remains unverified.
- Optional manual-coordinate sunrise alarm schedules, explicit dated alarms, reviewed fixed alarm-time copy from a saved sleep plan, calendar rules, selected-calendar event drafts, sleep-only Health Connect exchange and Tasker/HTTPS/MQTT automations. Actual provider/account exchange requires separate verification.
- Optional verified metadata versions in a selected Android document-provider folder, with reviewed restore and default-off deferred backup after successfully saved tracked nights. Audio is excluded from these metadata versions. See [automatic backup](docs/AUTOMATIC-METADATA-BACKUP.md).

## Privacy

Data is stored locally by default. Optional HTTPS/MQTT/Home Assistant, radio,
Nearby/Google services, external speech recognition and document providers have
their own prerequisites and privacy behavior. Bundled Google camera models may
send SDK usage/performance metrics. Optional microphone analysis and Speak capture
require explicit consent and foreground admission. Android system backup is
disabled; metadata and whole-library audio backups have separate opt-ins. The
accessibility service uses window package/class/ID metadata, never window text.
See [PRIVACY.md](PRIVACY.md).

## Build

Requires Android SDK 36, JDK 17 or newer (CI uses 21), and Android 11+ for the installed app.

```sh
./gradlew assembleDebug
```

On Windows use `gradlew.bat`. Set `ANDROID_HOME` or an untracked `local.properties` with `sdk.dir=...`. APK output: `app/build/outputs/apk/debug/app-debug.apk`. GitHub Actions publishes a debug APK artifact for each successful build. Debug signing is for development, not a production signing identity. CI defaults to APK builds only during this feature window; deferred unit/lint checks can be explicitly requested through its `full_checks` dispatch input. A CI debug key may differ from an existing local installation; do not uninstall an app containing needed data to bypass a signature mismatch.

For an explicitly selected device architecture, add `-PsleepwellAbi=arm64-v8a`
to `:app:assembleDebug` (the Galaxy S23 Ultra uses arm64). Other supported values
are `armeabi-v7a`, `x86` and `x86_64`. Without this option the build remains
universal. Architecture-specific APKs retain the same app ID and signing setup;
only the matching native libraries are packaged. Their filenames/release notes
must identify the architecture. Emulator checks do not verify physical arm64
hardware behavior.

## First run

1. Open **Settings → Reliability**. Grant notifications, exact alarms, and full-screen alarm access.
2. Review battery restrictions. On Samsung, put SleepWell in **Never auto sleeping apps**.
3. Open the **Alarms** tab, create an alarm, choose its challenge and sound, and enable it. Use the ten-second quiet test first.
4. Optionally choose distracting apps in **Bedtime guard**, then enable the guard in Android Accessibility settings. This is voluntary friction, not an unbreakable device lock.
5. To track a night, tap **Start sleep tracking** on Tonight, review the sensor and optional microphone settings, then **Start tracking**. Place the phone uncovered on the mattress; a bedside table does not transmit body movement reliably. Use **Pause tracking** / **Resume tracking** as needed, then **Stop tracking → End & review**.

Report opens the latest saved period; its calendar opens History. From History, add a missed period, edit a record, or open Trends. Manual entries are labeled and never receive fabricated sensor measurements. Exports and
restore are under **Settings → Data & privacy**. Inter's license and illustration
provenance are documented in [ASSETS.md](docs/ASSETS.md). Device testing instructions,
including preserving the personal installation, are in [testing.md](docs/testing.md).

The system may present a heads-up notification instead of launching the full-screen alarm when the phone is already in use. A powered-off or force-stopped app cannot ring. Speaker volume is bounded by the device hardware. No Android app can guarantee waking someone.

## Research and originality

Behavioral references are [Sleep as Android's documentation](https://sleep.urbandroid.org/docs/) and the [Challenges Alarm Clock publisher listing](https://play.google.com/store/apps/details?id=com.garageapp.alarmchallenges). This project contains independently written code, original UI, and synthesized original audio. It does not modify either app, unlock purchases, copy their proprietary assets, or claim affiliation. See [architecture](docs/ARCHITECTURE.md), [reference index](docs/REFERENCE_INDEX.md), and [verification](docs/VERIFICATION.md).

MIT licensed. Maintained by Omeyer Najjar.
