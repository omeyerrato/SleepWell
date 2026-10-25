# Feature delivery — 25 October 2026

This records the production feature window ending **17:23 Riyadh / 14:23 UTC**.
It is a delivery inventory, not full functional or design acceptance.

## Final candidate

- Application source: **6578575**; main database **schema 15**; portable metadata
  **backup format 13**.
- Final debug APK build: **PASS, 28 seconds**, as reported by the integration owner.
  Its signer matches the installed debug key.
- APK SHA-256:
  `15d8576c4e241cfccf9701cb93dc94d696b3887266654197b2ed4a572160e3aa`
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

- Optional vibration-only snoring cues during explicitly admitted contactless
  analysis, with output-error reporting and cue-contaminated capture gaps.
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
- [ ] Full contactless-noise parity: shared-stream WAV clips and sound-based snoring
  cues; the new contactless cue option is vibration-only.
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
