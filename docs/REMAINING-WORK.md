# SleepWell current remaining work

Updated 26 October 2026. This is the consolidated current checklist, not a release
history. The goal remains the full applicable phone functionality and **all 36
supplied design screens across 12 boards**. Existing code, a successful build or a
bounded historical pass does not establish that whole goal is complete.

## Current delivery state

| Item | Current state |
| --- | --- |
| Installed phone build | **86d0d1e**, installed with matching checksum; original records, preferences and private files verified preserved. |
| Phone availability | Samsung Device Care still suspends the app. Last verified database is **schema 7**; first-open migration **7 → 17** remains pending. Installation did not establish migration or runtime success. |
| Published candidate | **8094497**, production debug build **PASS, 2m3s**, original signer and uploaded/local digest verified. **Not installed.** Schema 17 / metadata backup format 13. |
| New feature evidence | Production implementation/build only. New runtime, physical output, provider, TTS engine and overnight behavior remain unverified. |
| Current work policy | Features first. New unit/native test batches, screenshot/evidence batches, dedicated design review, broad bug review and security audits remain **deferred at the user's request**. This checklist does not restart them. Essential integration/build and data-preservation work remain applicable. |

The installed build is not 571018b or 121dbd5; those are historical milestones.
The newer 8094497 candidate is not the installed build. No further phone, alarm,
audio, microphone, sensor, provider or bridge actions are authorized by this note.

## Concrete remaining implementation and interpretation limits

- [ ] Resolve reliable automatic laughter recognition. The new **Laughter** task is
  an experimental YAMNet-family estimate; earlier failed independent positives and
  the rejected lower-threshold candidate remain unresolved. **Loud sound** uses
  relative microphone RMS, not calibrated dB or instrument recognition. Broad
  **Speak**, exact offline **Spoken phrase** and **Musical note** are distinct paths;
  none establishes generic instrument identification or laughter accuracy.
- [ ] Establish feasibility before claiming phone sleep-stage/REM, breathing,
  apnea or disturbance analysis. Motion, awake estimates, timed dream cues and
  experimental periodic echoes do not supply validated stage or medical evidence.
- [ ] Dedicated Sleep Phaser Bluetooth protocol is absent. The vendor publishes only
  other devices’ USB commands and asks developers to request this Bluetooth interface.
  Actual UUIDs/framing/PIR/light/auth/version terms are required before implementation.
  [Primary vendor source](https://www.happy-electronics.eu/support/developers/).
  Current secondary PIR is a separate user-owned Home Assistant binary-sensor path.
- [ ] Direct legacy Google Fit/Samsung SDK exchange and hosted SleepCloud,
  research-community or Spotify account-service parity remain absent or externally
  gated. Existing Health Connect, HTTPS radio, user-owned folder backups and local
  exports are narrower working source paths, not those services.
- [ ] Clinical chronotype, jet-lag-prevention and lucid-dream efficacy are not
  established. Existing calculations, dated travel plans, reminders, light tools
  and timed cues must retain their descriptive/experimental limits.

Do not rebuild features merely because an older checklist called them missing.
Current production already includes saved alarm defaults, date/repeat/exclusion
rules, advanced snooze/backup controls, reviewed requests, Health Connect/calendar
integration paths, local/ambient playlists, audio backup/restore and AAC export,
CSV source inspection, awake estimates, calibration and additional challenge paths.
**Jumping is implemented as an original touch-controlled hurdle game**, with saved
progress and a Math alternative; it is not physical-jump sensing. See
[JumpTask](../app/src/main/java/org/sleepwell/app/JumpTask.java) and
[ChallengeRoutine](../app/src/main/java/org/sleepwell/app/ChallengeRoutine.java).
Runtime acceptance for these paths remains open rather than implementation absent.

## Deferred functional acceptance — retain, do not execute now

- [ ] After the user permits reopening the suspended app, verify the installed
  86d0d1e first-open schema 7 → 17 migration and retained data. A separate authorized
  installation is needed for 8094497; it must preserve signer, records, preferences
  and private files. Do not silently remove Device Care suspension.
- [ ] Actual alarm delivery and best-effort prevent-leave/power-menu behavior;
  extra-loud output and restoration; gentle ramp, sound delay, vibration, screen
  sunrise, flashlight, radio/playlist fallback, backup/timeout and wake-up checks.
  Hardware forced-off cannot be prevented by an ordinary Android app.
- [ ] Date/repeat/ISO-week/interval/sunrise/calendar projections, one-occurrence
  overrides, exact snooze deadlines and retained random selections through snooze,
  process loss, reboot, timezone and DST changes.
- [ ] Real challenge permissions, unavailable-sensor alternatives, enrollment,
  camera/model accuracy, microphone quiet/cleanup, offline phrase language support
  and orientation behavior. New Laughter/Loud sound paths do not settle accuracy.
- [ ] Overnight manual/automatic/recurring capture, charging and OS restrictions,
  false-start rejection/deletion, low-battery stop, FIFO batching, contactless
  shared audio/clips, real gaps, battery use and anti-snore routing/cue behavior.
- [ ] Local document decoding, real AAC encoder/provider output, playlist completion,
  ambient rotation, own/external stillness fading and route/focus cleanup. Requested
  narration engines may fall back under Android; exact selected offline voice
  checks and actual output still need runtime evidence.
- [ ] Actual Health Connect/calendar/SAF/assistant/automation exchange, uncertain
  receipts and lifecycle handling; backup/restore, legacy stars and CSV-evidence
  migrations/round trips; profile recovery, history erasure, printing and widgets.
- [ ] Direct Hue independently verified identity/pin, HTTPS/v1 firmware/network
  compatibility, real sunrise and conditional restoration. Home Assistant and Hue
  remain exclusive; process death/in-flight commands cannot guarantee restoration.
- [ ] Recorded-duration/awake edits, dated-goal accounting, tags, scores, history,
  missing days and CSV source graphs/event annotations must stay based on actual saved evidence.
  New event units are explicitly inferred from the vendor sample; full/six-hour
  view, complete-pair rules and exact inspection remain runtime-unverified.
- [ ] Historical full-app native failures remain unresolved, including the independent
  laughter-positive gate. Earlier narrow passing batches do not supersede them or
  validate the current production-only candidate. Do not weaken those gates or
  schedule a new test batch under this deferred-work checklist.

## Deferred design acceptance — all 36 screens remain in scope

The user's complete references remain mandatory. Bounded accepted frames do not
finish a whole board, and adding feature paths does not finish UI/interaction work.

- [ ] **01 Everyday:** Tonight proportions/arc meaning, aligned selected navigation,
  Report hierarchy and focused wake-up challenge.
- [ ] **02 Alarms:** compact list, per-alarm details, custom time controls and truthful
  smart-window chart/states.
- [ ] **03 Sound/snooze:** tone list, real ramp chart, gentle/backup controls and states.
- [ ] **04 Challenges:** actual settings behind task rows, enrollment, previews,
  difficulty and wake-up-check states.
- [ ] **05 Bedtime:** wind-down, app guard, selected blocked apps and goal/bank layout.
- [ ] **06 Tracking:** bedside placement composition, calibration, real live signal,
  permission/quality/error and stopped states.
- [ ] **07 Audio:** actual event waveforms/player, category/filter/selection consistency,
  anti-snore controls and unavailable/analysis-only states.
- [ ] **08 History:** history, trends, real measured variation, gaps and record editor.
- [ ] **09 Rhythm/naps/travel:** charts, timing controls and supporting states.
- [ ] **10 Sensors:** automatic tracking, motion/room-signal and unavailable states.
- [ ] **11 Settings/integrations:** hierarchy, consistent icons and actual provider
  permission/disconnected/error states.
- [ ] **12 Reliability/privacy/appearance:** complete controls, previews and optional
  connection prerequisites.
- [ ] **Every screen:** Inter proportions, aligned icons/labels, coherent system bars,
  48dp complete targets, readable charts, unclipped footers, font scaling/reduced
  motion, selected states, predictable Save/Cancel/back/recreation and concise copy.

The dedicated reviewer remains stopped. Design/security/broad bug work is retained
as deferred scope, not silently removed and not reinstated as current execution.

## References and historical evidence

- [Feature checklist](FEATURE-CHECKLIST.md): compact current implementation inventory.
- [Feature matrix](FEATURE-MATRIX.md): detailed source/subfeature bounds; root-owned
  reconciliation continues separately. A stale historical cell is not proof that a
  current implemented path is absent.
- [Production-window delivery note](2026-10-25-FEATURE-DELIVERY.md): historical build,
  release and feature details. Its phone/build statements describe that earlier date.
- [Portable settings and CSV](PORTABLE-SETTINGS-AND-CSV.md),
  [automation requests and calendar](AUTOMATION-REQUESTS-AND-CALENDAR.md), and
  [challenge/narration/Hue boundaries](CHALLENGE-AND-LIGHT-FEATURES.md): delivered paths
  and explicit limits, not current phone acceptance.
- [Historical final feature-batch evidence](verification/2026-10-25-final-feature-batch/README.md):
  0a821eb passed 1,063 pure tests and 108 native methods/all 154 originals. This
  predates the current production-only additions and does not validate them.

Wearables remain excluded. No paywall, subscription or advertising is to be added.
External services retain their own prerequisites; SleepWell being free does not
make those services free or establish full completion.
