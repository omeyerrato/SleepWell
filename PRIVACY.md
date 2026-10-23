# Privacy

SleepWell 0.1.0 stores data on your phone. There is no account system, advertising SDK, telemetry, analytics SDK, or Internet permission.

## Data you choose to collect

- Alarms: names, times, weekdays, challenge preferences, sound selections, and alarm state.
- Tracking: timestamps, one-minute movement summaries, ambient light values, and optional microphone amplitude.
- Audio: only if enabled, ten-second clips triggered by amplitude. Maximum 60 per tracking session, deleted after seven days when the next session starts. No speech transcription or sound classification is performed.
- Journal: notes, tags, and subjective ratings you enter.
- App guard: package identifiers you select and the current foreground package name supplied by Android Accessibility. The service cannot retrieve window content. Package transitions are not saved as an app-usage history.
- NFC: the identifier of a tag you intentionally enroll. This is not an authentication mechanism.
- Sleep sounds: source choices, mixer gains, timer/fade and an optional read-only audio-document URI/title stay in app preferences. Imported documents remain with their provider. Old read grants are retained because an alarm tone may share them; SleepWell never deletes the source document. Provider network/storage behavior is controlled by the provider you choose.

## Permissions

Exact alarms schedule wake-up times. Notifications and full-screen intent access display alarm/tracking status. Audio settings change only the alarm stream while ringing and restore its earlier volume if it still matches the app-set value. Vibration provides optional haptics. A bounded wake lock keeps overnight sensor delivery active. Foreground services visibly run alarms and user-started tracking. Microphone access is optional and requested in context. NFC is used only during enrollment or an NFC challenge. Accessibility is optional and enabled in Android Settings.

## Storage, export, and deletion

Data lives in the app's internal SQLite database, preferences, and private audio folder. Other ordinary apps cannot read it. Android automatic backup is disabled. CSV and JSON export use a document destination you choose; exported files may contain sensitive notes. JSON data backups exclude audio and preferences; they contain alarms, sessions, sensor samples, pause intervals and recorded-duration provenance. Uninstalling deletes the app's private data. **Setup → Delete all sleep history** deletes nights, audio, graphs, and notes while preserving alarm configuration.

No data is sent to the original reference apps or their vendors. External links open in your browser. Future network or wearable integrations require updating this policy and explicit in-app controls before collecting or transmitting data.
