# SleepWell

An original, open-source Android app for protecting bedtime and making mornings harder to sleep through. All implemented features are free. No ads, accounts, subscriptions, or paid unlocks.

**Status: early development. This is not full feature parity with Sleep as Android or Challenges Alarm Clock.** The [feature audit](docs/FEATURES.md) records what exists, how it works, and what remains. Do not replace a dependable backup alarm until this app has been tested overnight on your device.

## Working capabilities

- Multiple one-shot or weekday alarms using Android's exact `setAlarmClock` API, rescheduled on reboot, clock/time-zone changes, and updates.
- Original Daybreak and Pulse alarm tones, Android's default alarm sound, or your own audio file; configurable alarm-stream volume, gradual ramp, vibration, and optional loud backup escalation.
- Math, matching pairs, sequence, retyping, shaking, and enrolled NFC challenges. Configurable rounds, limited snoozes, optional screen pinning, and post-dismissal wake-up checks.
- Optional accessibility-based return to the active alarm, plus a bedtime guard for explicitly selected distracting apps. Emergency dismissal, calls, and Android Settings remain available.
- User-started overnight accelerometer and ambient-light tracking, with real movement/noise/light graphs and local session history.
- An experimental early wake-up window based on sustained movement, with the fixed alarm scheduled independently. Movement is not a measured sleep stage.
- Optional microphone amplitude analysis and up to 60 ten-second noise-triggered WAV recordings per night; seven-day clip retention and local playback. **No sound classification yet.**
- Rain and brown-noise sound loops with a sleep timer; bedtime reminders; nap alarms; recorded-duration goals, notes, tags, subjective ratings, and a seven-day session average.
- CSV export, versioned JSON data backup, validated transactional restore, and deletion of local history.

## Privacy

The app has **no Internet permission**. Microphone use is optional and occurs only in a visible user-started foreground tracking service. Recordings, sensor data, and notes stay in private app storage. Android cloud backup is disabled. Export requires Android's document picker. The accessibility service reads foreground package names only, never window text. See [PRIVACY.md](PRIVACY.md).

## Build

Requires Android SDK 36, JDK 17 or newer (CI uses 21), and Android 11+ for the installed app.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
```

On Windows use `gradlew.bat`. Set `ANDROID_HOME` or an untracked `local.properties` with `sdk.dir=...`. APK output: `app/build/outputs/apk/debug/app-debug.apk`. GitHub Actions publishes a debug APK artifact for each successful build. Debug signing is for development, not a production signing identity.

## First run

1. Open **Setup**. Grant notifications, exact alarms, and full-screen alarm access.
2. Review battery restrictions. On Samsung, put SleepWell in **Never auto sleeping apps**.
3. Create an alarm, choose its challenge and sound, and enable it. Use the ten-second quiet test first.
4. Optionally choose distracting apps in **Bedtime**, then enable the guard in Android Accessibility settings. This is voluntary friction, not an unbreakable device lock.
5. To track a night, choose whether to use the microphone, then tap **Start my night**. Place the phone uncovered on the mattress; a bedside table does not transmit body movement reliably. Tap **End night** to review recorded data.

The system may present a heads-up notification instead of launching the full-screen alarm when the phone is already in use. A powered-off or force-stopped app cannot ring. Speaker volume is bounded by the device hardware. No Android app can guarantee waking someone.

## Research and originality

Behavioral references are [Sleep as Android's documentation](https://sleep.urbandroid.org/docs/) and the [Challenges Alarm Clock publisher listing](https://play.google.com/store/apps/details?id=com.garageapp.alarmchallenges). This project contains independently written code, original UI, and synthesized original audio. It does not modify either app, unlock purchases, copy their proprietary assets, or claim affiliation. See [architecture](docs/ARCHITECTURE.md), [reference index](docs/REFERENCE_INDEX.md), and [verification](docs/VERIFICATION.md).

MIT licensed. Maintained by Omeyer Najjar.
