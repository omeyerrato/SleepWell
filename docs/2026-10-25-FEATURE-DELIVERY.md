# Feature delivery — 25 October 2026

This records the production feature window ending **17:23 Riyadh / 14:23 UTC**.
It is a delivery inventory, not full functional or design acceptance.

## Final candidate

- Application source: **90e2583**; main database **schema 15**; portable metadata
  **backup format 13**.
- Final debug APK build: **PASS, 14 seconds**, as reported by the integration owner.
  Its signer matches the installed debug key.
- Download: [development prerelease](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-features-20261025-90e2583).
- APK SHA-256:
  `7913bb2b72fa3568d629509ebd634c186d5d7a55c109dee685a520078a2755d0`
- The phone remains on **571018b / schema 7**. This candidate has **not** been installed.
- At the user's request this window used production builds only: no new unit/native
  test batches, device actions, screenshots, broad audits or design reviews. Earlier
  test results do not establish acceptance of these new changes.

## Delivered production features

### Alarm reliability, loudness and useful controls

- Explicit Extra loud now requests **+600 mB (+6 dB)** through a per-player-session
  `LoudnessEnhancer`. This is software gain, **not a hardware-limit bypass**; actual
  output and device handling have not been measured.
- Total snooze-time budgets, selected local-date exclusions, reviewed relative-time
  and optional speech time entry, and completed reviewed Android alarm/timer request
  handoff. Timer requests enter a normal nap draft rather than silently starting it.
- Smart Bedtime gained optional silent repeat reminders with owned dismissal, plus
  per-alarm **follow global / off / custom lead-time** settings consumed by reminder
  scheduling. These do not automatically change the alarm's selected wake time.
- Saved dated nap profiles, journaled batch application, and reviewed profile
  definition export/import were added.

### Sleep sensing, noise and snoring

- Contactless snoring cues now support Sound/Vibration/Both through separate
  remembered sound/vibration opt-ins. Each night pins permitted channels; Off
  revokes pending channel output. Headphone-only routing, cooldown/session bounds
  and cue/settling capture gaps are preserved. Actual output/efficacy is unverified.
- Separate per-night contactless clip consent now saves actual ten-second16kHz
  mono WAVs from the existing filtered capture stream, with no second microphone.
  Existing60/night and30-second limits, retention and event linkage are reused;
  incomplete/cue-gap clips are dropped. Analysis failure also stops these clips.
- Experimental periodic echo capture/reporting and measured surface-motion
  calibration; new recordings retain their actual calibration provenance.
- Google Sleep API integration now uses the published Play Services artifact.
  Its confidence input remains an optional estimate with platform prerequisites.
- Opt-in two-phone Nearby movement exchange, matching-code confirmation,
  clock alignment, separately saved peer evidence and a transparent experimental
  filter that preserves raw own/peer data.
- Optional read-only Home Assistant PIR input with recording ownership and saved
  provenance. It does not remotely start tracking or control the home sensor.

### Recorded graphs, sleep timing and honest interpretation

- Relative research-signal ZIP export and a transparent recorded-night score based
  on available saved inputs; neither supplies new clinical measurements.
- Original self-reported habitual work/free-day timing calculation, with unavailable
  results when the published method's prerequisites are not met.
- Optional reviewed sleep/wake SRI: actual retained sleep unions, including naps and
  awake cuts, explicit complete-day/remaining-awake/no-travel confirmation, changed
  record invalidation, and a minimum of seven comparable adjacent 24-hour day pairs.
  Missing days remain unknown; this is diary-based, not measured sleep staging.

### Challenge variety and optional wind-down tools

- Hands-raised pose, ambient light, original type-code, camera brightness,
  rotation-vector spin and live musical-note pitch challenge implementations.
- Explicitly armed timed dream-vibration schedules with actual request journals,
  pause/end/alarm cancellation and capture masking. They are **timed, not REM-triggered**.
- Manual local binaural stereo tones, silent timed guided wind-down, and reviewed
  private-diary JSON merging with conflict choices.

### Backups, privacy and sharing

- Whole-library audio ZIP export and reviewed v2 restore with metadata/audio linkage,
  imported alarms off, and honest pending old-file cleanup reporting.
- Manual audio versions in the selected SAF folder: actual content/size review,
  unique new files, full read-back SHA verification, separate version history and
  reuse of the explicit restore review. Complete raw clips may extend beyond edits.
- Independent default-off automatic whole-library audio consent, future successful
  tracked-night triggers, charging/unmetered deferral, durable receipts, expiry,
  bounded known pre-create retries, and no replay of uncertain writes. Off and local
  history erasure invalidate future/in-flight delivery; external copies may remain.
- Reviewed sleep-summary sharing/export and access invalidation around local-history
  erasure. Optional external copies are now described explicitly in privacy copy.

## Remaining implementation versus acceptance

**Implementation still incomplete or outside this delivered subset**

- [ ] Validated sleep-stage/REM detection and REM-triggered dream cues.
- [ ] Actual contactless capture/playback and snoring-cue effectiveness; shared-stream
  WAV clips and separately consented Sound/Vibration/Both paths now have code.
  Their runtime/hardware acceptance remains outstanding.
- [ ] Semantic object recognition and speech transcription/instrument recognition.
- [ ] Direct vendor-account cloud clients; SAF providers handle their own remote upload.
- [ ] Proven jet-lag prevention or clinical chronotype/regularity classification.
  Existing experimental cues and descriptive calculations are not equivalents.

**Code exists, but acceptance remains outstanding**

- [ ] Install/migrate this candidate on the phone with data preservation verified.
- [ ] Real alarm delivery, protected-leave/power-button behavior, loudness, ramp,
  sunrise, torch, snooze and cleanup on the actual device. Forced hardware restart
  and powered-off ringing are not ordinary-app guarantees.
- [ ] Overnight sensor/audio capture, battery use, real snoring/classifier accuracy
  and Google Sleep input; unresolved laughter accuracy is not declared fixed.
- [ ] Real camera/microphone/orientation challenges, pair transport, PIR exchange,
  SAF provider/cloud transfer, restore and background-job behavior.
- [ ] Full functional regression and reference-image design acceptance across the app.

This candidate finishes the listed production increments. It does not close the
user's complete feature, hardware or visual-quality goal.

## Cue-route error-state follow-up

Source6180a6f builds in15s and surfaces skipped headphone-route messages in live
contactless tracking and snoring-cue settings. Starting a new contactless night
clears the previous route error. This follow-up has build-only evidence, no new
tests/device actions, and is not included in the published90e2583 prerelease.
The locally preserved6180a6f APK has the same installed debug signing key.
