# Privacy

SleepWell stores data on your phone by default. It has no SleepWell account system,
advertising or SleepWell-operated telemetry endpoint. Optional integrations, radio,
Google SDKs and document/speech providers have their own privacy behavior, described
below. External accounts and services are supplied by the user.

## Data you choose to collect

- Alarms: names, times, weekdays, challenge preferences, sound selections, alarm state and optional manually entered sunrise coordinates.
- Tracking: timestamps, one-minute movement summaries, ambient light values, and optional microphone amplitude.
- Audio: optional on-device model estimates and, with separate consent, bounded ten-second amplitude-triggered clips. The initial clip limit is60 per tracked session; retention and favorite protection are configurable. Estimates may be wrong; corrections retain the original estimate. Custom category inclusion suppresses all new clips, and unreadable analysis settings block new analysis/clips until reviewed. Shared sonar analysis saves no raw sonar audio. Sleep capture and Speak do not transcribe speech.
- Journal: notes, tags, and subjective ratings you enter.
- App guard: package identifiers you select and window package/class/ID metadata supplied by Android Accessibility. The service cannot retrieve window content. Package transitions are not saved as an app-usage history.
- NFC: the identifier of a tag you intentionally enroll. This is not an authentication mechanism.
- Camera challenges: QR/barcode enrollment retains a location label, format and payload hash rather than raw code text. Picture matching retains a compact visual signature rather than the original image; it is not identity, semantic-object or liveness verification. Camera access is requested explicitly.
- Smile and Pose challenges use bundled proprietary Google ML Kit models for local
  camera processing. SleepWell saves no camera images or face/body templates.
  Google may receive SDK usage/performance metrics; the in-app details disclose
  this before capture. These tasks do not establish identity or liveness.
- Speak challenge: explicit listening uses on-device broad-speech estimates. It saves no captured audio, transcript or sound event. Sleep capture and Speak cannot own the microphone simultaneously.
- Optional relative-alarm speech entry explicitly opens an external Android speech
  provider. That provider may process audio remotely even when offline preference
  is requested. Returned candidate text is reviewed before applying a draft; the
  app does not automatically save an alarm or retain a speech transcript history.
- Optional Google Sleep API input requires Play Services and activity permission.
  Confidence is an estimate used alongside the existing stillness conditions;
  registration is optional and a callback does not start microphone/sonar capture.
- Optional paired movement requires two already-recording phones, Nearby transport
  and matching-code confirmation. It exchanges bounded movement summaries/timing
  data, not audio or remote commands. Local reports retain separate peer provenance.
- Optional PIR input reads a user-owned Home Assistant HTTPS binary sensor during
  an explicitly started recording. Credentials are independently encrypted; saved
  snapshots include hub state, age, latency and errors rather than inferred stages.
- Awake estimates, calibration summaries, timing questionnaires, reviewed-day SRI
  confirmations and dream-cue receipts are stored locally. Research export is an
  explicit relative-signal ZIP; removing dates/identifiers does not guarantee that
  a recognizable sleep pattern is anonymous.
- Calendar and Health Connect: optional selected-calendar reads inform configured alarm rules; selected sleep records may be imported/exported with separate sleep permissions. Calendar event drafts open another app for user review and Save. Imported records retain source provenance.
- Integrations: opted-in rules can send configured sleep/alarm event fields to a selected Tasker package, HTTPS endpoint or TLS MQTT broker. Home Assistant uses the configured server/token/light. Credentials and pending-delivery state stay in private app storage; receiving services determine their own retention. See the integration documents for exact fields and limits.
- Sleep sounds: source choices, mixer gains, timer/fade and an optional read-only audio-document URI/title stay in app preferences. Imported documents remain with their provider. Old read grants are retained because an alarm tone may share them; SleepWell never deletes the source document. Provider network/storage behavior is controlled by the provider you choose.
- Optional alarm radio contacts the explicitly selected public HTTPS station when
  that alarm plays. The station receives normal connection information, including
  the network address; fallback playback does not make the remote exchange private.

## Permissions

Exact alarms schedule wake-up times. Notifications and full-screen access display alarm/tracking status. Audio settings change the alarm stream while ringing and conditionally restore the prior volume. Vibration, torch and screen/light effects are optional. A bounded wake lock supports sensor delivery. Foreground services visibly run alarms and explicitly admitted tracking/Speak. Microphone and camera access are optional and requested in context. NFC is used during enrollment or a challenge. Optional step/activity, usage access, calendar, sleep Health Connect and Do Not Disturb access support their named features. Accessibility is enabled separately in Android Settings.

## Storage, export, and deletion

Data lives in internal SQLite databases, preferences and a private audio folder. Android system automatic backup is disabled. Explicit exports and optional app-managed metadata backups use a document destination you choose; its provider may upload to its cloud service. Exports can contain sensitive notes. JSON metadata versions include alarms, sessions, samples, intervals, tags and source/analysis provenance; audio, separate diary entries, integration credentials and many preferences are excluded. Audio and diary exports are separate explicit actions.

Automatic metadata backup is off by default and requires a remembered writable destination. Enabling it queues metadata versions after successfully saved tracked nights. Local receipts record destination/name/hash and verification state. Turning it off cancels future ownership; a provider write already underway may finish or remain partial, and its file is kept. Repeated versions and previously exported files are not deleted by uninstalling or deleting local history. Review the selected provider folder separately.

Whole-library audio ZIPs use a separate manual review or independent default-off
automatic consent. They include entire raw saved clips and metadata, which can
extend beyond edited record bounds. Automatic consent covers the existing and
future library, but only a later completed production night queues a copy;
charging/unmetered/battery conditions defer it. Readback hashes establish provider
content, not completed remote-server synchronization. Off/destination changes and
local-history erasure cancel or invalidate owned work. An already-started provider
write may still leave a partial or complete external file; uncertain writes are
not silently replayed. Exported copies are not erased with local history.

Explicit sleep-summary sharing grants read access only to a generated bounded
summary, excluding audio, notes, diary and credentials. History changes invalidate
prepared summaries, and history erasure revokes known grants/deletes generated
files. Already-open file descriptors and copies made by recipients cannot be
recalled. Nap-profile definition sharing excludes active scheduling authority;
private-diary imports use a separate reviewed merge.

Uninstalling deletes private app data. History, audio and diary deletion are separate controls; deleting history preserves alarm configuration. A backup restore shows its included data before transactional replacement. See [backup limits](docs/AUTOMATIC-METADATA-BACKUP.md) and the [feature matrix](docs/FEATURE-MATRIX.md).

SleepWell does not connect to the reference apps' services. External links open in
your browser. External exchange requires the relevant configuration and permission;
native fixture tests do not establish actual provider exchange or a completed
security audit. Wearable companions are excluded from the current scope.
