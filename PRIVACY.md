# Privacy

SleepWell stores data on your phone by default. It has no SleepWell account system, advertising or app telemetry. Internet permission supports optional user-configured HTTPS/MQTT automations and Home Assistant lights. A document provider or Health Connect may have its own account and privacy terms.

## Data you choose to collect

- Alarms: names, times, weekdays, challenge preferences, sound selections, and alarm state.
- Tracking: timestamps, one-minute movement summaries, ambient light values, and optional microphone amplitude.
- Audio: optional on-device model estimates and, with separate consent, bounded ten-second amplitude-triggered clips. The initial clip limit is60 per tracked session; retention and favorite protection are configurable. Estimates may be wrong; corrections retain the original estimate. Custom category inclusion suppresses all new clips, and unreadable analysis settings block new analysis/clips until reviewed. Shared sonar analysis saves no raw sonar audio. No speech transcription is performed.
- Journal: notes, tags, and subjective ratings you enter.
- App guard: package identifiers you select and window package/class/ID metadata supplied by Android Accessibility. The service cannot retrieve window content. Package transitions are not saved as an app-usage history.
- NFC: the identifier of a tag you intentionally enroll. This is not an authentication mechanism.
- Camera challenges: QR/barcode enrollment retains a location label, format and payload hash rather than raw code text. Picture matching retains a compact visual signature rather than the original image; it is not identity, semantic-object or liveness verification. Camera access is requested explicitly.
- Speak challenge: explicit listening uses on-device broad-speech estimates. It saves no captured audio, transcript or sound event. Sleep capture and Speak cannot own the microphone simultaneously.
- Calendar and Health Connect: optional selected-calendar reads inform configured alarm rules; selected sleep records may be imported/exported with separate sleep permissions. Calendar event drafts open another app for user review and Save. Imported records retain source provenance.
- Integrations: opted-in rules can send configured sleep/alarm event fields to a selected Tasker package, HTTPS endpoint or TLS MQTT broker. Home Assistant uses the configured server/token/light. Credentials and pending-delivery state stay in private app storage; receiving services determine their own retention. See the integration documents for exact fields and limits.
- Sleep sounds: source choices, mixer gains, timer/fade and an optional read-only audio-document URI/title stay in app preferences. Imported documents remain with their provider. Old read grants are retained because an alarm tone may share them; SleepWell never deletes the source document. Provider network/storage behavior is controlled by the provider you choose.

## Permissions

Exact alarms schedule wake-up times. Notifications and full-screen access display alarm/tracking status. Audio settings change the alarm stream while ringing and conditionally restore the prior volume. Vibration, torch and screen/light effects are optional. A bounded wake lock supports sensor delivery. Foreground services visibly run alarms and explicitly admitted tracking/Speak. Microphone and camera access are optional and requested in context. NFC is used during enrollment or a challenge. Optional step/activity, usage access, calendar, sleep Health Connect and Do Not Disturb access support their named features. Accessibility is enabled separately in Android Settings.

## Storage, export, and deletion

Data lives in internal SQLite databases, preferences and a private audio folder. Android system automatic backup is disabled. Explicit exports and optional app-managed metadata backups use a document destination you choose; its provider may upload to its cloud service. Exports can contain sensitive notes. JSON metadata versions include alarms, sessions, samples, intervals, tags and source/analysis provenance; audio, separate diary entries, integration credentials and many preferences are excluded. Audio and diary exports are separate explicit actions.

Automatic metadata backup is off by default and requires a remembered writable destination. Enabling it queues metadata versions after successfully saved tracked nights. Local receipts record destination/name/hash and verification state. Turning it off cancels future ownership; a provider write already underway may finish or remain partial, and its file is kept. Repeated versions and previously exported files are not deleted by uninstalling or deleting local history. Review the selected provider folder separately.

Uninstalling deletes private app data. History, audio and diary deletion are separate controls; deleting history preserves alarm configuration. A backup restore shows its included data before transactional replacement. See [backup limits](docs/AUTOMATIC-METADATA-BACKUP.md) and the [feature matrix](docs/FEATURE-MATRIX.md).

No data is sent to the original reference apps or their vendors. External links open in your browser. External exchange requires the relevant explicit configuration and permission; native fixture tests do not establish actual provider exchange or a completed security audit. Wearable companions are excluded from the current scope.
