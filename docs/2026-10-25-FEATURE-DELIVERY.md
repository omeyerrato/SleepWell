# Feature delivery — 25 October 2026

This records the production feature window ending **17:23 Riyadh / 14:23 UTC**.
It is a delivery inventory, not full functional or design acceptance.

## Candidate published before the deadline

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
- [ ] Reliable instrument/laughter recognition. A later on-device spoken-phrase
  challenge matches a displayed phrase; general saved transcription is not supplied.
  Local object classification also has code; its acceptance remains open.
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

## Four-feature source follow-up after the deadline

Application source **bff9f7e** builds successfully in **2m59s**. Database15 and
metadata backup13 remain unchanged; its APK retains the installed debug key.
It has not been installed. No new unit/native/lint/screenshot/device batches ran.
The earlier public90e2583 APK does not contain these changes.

- **Weekly nap schedules:** explicitly reviewed recurring local-clock wake alarms
  for selected start weekdays, including cross-midnight wake days, overlap checks,
  acknowledged journaled apply/retry/cancel and private nonce/full-row ownership.
  Independently edited rows detach. Portable definitions never schedule alarms;
  main-data restore removes imported authority and clears private groups after
  commit. Owned nap alarms bypass night calendar/reminder behavior. They are not
  elapsed timers or automatic nap-start tracking.
- **Optional narrated wind-down:** actual Android TTS speaks the current/subsequent
  original timed guide steps only after explicit Play. Requires an installed English
  engine-declared offline voice. Pause/end/focus loss/recreation/alarm/tracking stop
  speech; visible provider/timeout errors retain silent guidance. The app does not
  download voices; provider behavior and audible quality remain unverified.
- **Object recognition challenge:** bundled Apache-2.0 EfficientNet-Lite0 runs on
  real CameraX frames against16 curated labels, with score/margin/repeated-frame
  requirements. Saved task configuration, camera preview, run-bound one-use alarm
  results and persistent Math fallback are connected. No images are saved/uploaded.
  Photos/screens can pass image classification; this is not a liveness check.
- **Pre-travel sleep shifts:** user chooses Earlier/Later and15/30/60min daily shifts
  across1–7 days before a saved departure. Dated home/destination review rejects
  ambiguous/missing endpoints and overlapping windows. Default-off silent reminders
  are revision-bound and recover future delivery only. Each wake time opens a new
  ordinary alarm draft requiring separate Save; existing alarms are untouched.

Commits include5127f0e (narration),76ffd0e/46cec0b (object capture and challenge),
dfdb9fa/1c636b2 (travel plan/new draft),6f5c729/8e1cb43/0eaa290 (weekly naps and
ownership integration) andbff9f7e (working weekly editor links).

Actual camera/model accuracy, TTS providers, recurring OS delivery, travel reminders,
overnight/device behavior and design acceptance remain outstanding. Travel planning
does not establish clinical phase or effective jet-lag prevention. Full parity is
unfinished; the six-hour full-completion deadline was missed.

## Exact-second timer follow-up

Source **53a0c08** adds exact1..86400second standard timer requests. Duration
passes unchanged through owned hours/minutes/seconds review, new nap draft,
separate Start, the existing millisecond scheduler and saved/reopened countdown.
Optional strict NapPlan v2 metadata carries seconds; legacy v1 minute nap metadata
and ordinary nap presets remain compatible. Exact timers exclude smart early
waking and Power nap; selecting a minute preset explicitly returns to ordinary naps.
The combined APK builds in **3m2s**, retains the original installed signing key,
and leaves schema15 / metadata format13 unchanged. No personal-phone actions or
new test/lint/screenshot batches ran. Actual assistant/OS delivery is unverified.

Published [53a0c08 development prerelease](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-features-20261025-53a0c08)
includes all five follow-up flows. GitHub reports the214779315-byte APK uploaded;
its SHA256 matches the frozen local artifact:
`b86353e206feb0df1352fe5a869a6471ca26636c1a1fd26fce1e6a1b82a13638`.
Original signer was checked locally. This release remains uninstalled on the phone.

## Printable codes, sampling and widget follow-up

Combined source **9c6b1c9** builds in **1m23s**, same installed signer, schema15
and backup13. It adds:

- Direct Nap → Custom timer access to the actual exact-second timer path, with
  restored unfinished duration fields and Cancel leaving the draft unchanged.
- Default-off battery-aware ACTIVE movement/light sampling, pinned per run.
  At20% unplugged it requests500ms accelerometer/1s light, restoring normal at25%
  or charging. Automatic waiting keeps5Hz; mic/sonar/admission/deadlines stay as
  before. Counts are real, empty measurement minutes stay absent, original motion
  calibration is retained in strict per-session policy metadata. Actual savings
  and sensor/accuracy behavior are unverified.
- Locally generated random morning QR codes with real PNG/PDF exports and Android
  printing. Only label/hash return through a fresh request after explicit Use;
  existing Save code/Save alarm remain required. Payload lives only in the
  unfinished screen/Bundle, not task/preferences/backups. Export/cancel does not
  replace enrollment. Provider failures disclose possible partial destination
  files; a kept-copy confirmation prevents accidental unrecoverable enrollment.
- Next-alarm widgets now include acknowledged owned snooze deadlines and exact
  seconds. Receipt validation fences changed/deleted rows, run/budget/source,
  consumed one-shot/claimed nap, occurrence and boot. Unconfirmed/old snoozes show
  review/fallback. This display-only receipt never authorizes delivery; widget
  refresh/taps do not schedule or fire alarms and failed receipt persistence
  cannot roll back real scheduling.

Root commits3913808 (direct custom timers),9b7e260 (battery sampling),98e4f05
(printable QR) and9c6b1c9 (snooze widget). No new tests/lint/screenshots or physical
phone/camera/printer/provider actions ran. These source flows are build-only,
not runtime or visual acceptance, and are absent from the earlier53a0c08 APK.

Published [9c6b1c9 development APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-features-20261025-9c6b1c9),
214821691bytes. GitHub uploaded SHA256 matches the frozen local artifact:
`628fda78cabef51496f6851b3c8e449852df5087fb546f278462701d3a80423c`.

## Advanced recurrence and automatic-tracking follow-up

Application **0e352bc** builds **PASS2m31s** with the original installed signer,
schema15 / backup13 unchanged. Root merged parallel implementations and completed
the private Activity, backup-validation, alarm-label and stop-cleanup hooks.

- **Repeats:** optional strict repeatRule v1 adds selected weekdays on odd/even
  ISO weeks or every1..365 calendar days forward from a chosen anchor. Interval
  alarms retain recurring status; old weekly/one-shot JSON stays unchanged. Clock
  and sunrise projections, polar fallback, occurs/skip/excluded-date/calendar
  identity and runtime validation use the same rule. Explicit detail Use then
  Save alarm; restored stale results cannot replace a changed draft. Elapsed naps
  and one-time Run on are distinct modes. Existing alarms/defaults stay as saved.
- **Battery reserve:** default-off5/10/15/20/30% threshold pinned per admitted run.
  A known unplugged reading at/below it claims one orderly stop/save, including
  movement/paused/sonar paths. Initial ARMED/DELAYED cancellation creates no fake
  night; unknown battery keeps tracking with visible status. No restart or alarm
  changes. Existing sonar charging prerequisites remain stronger.
- **Automatic false starts:** separate default-off choice for STILLNESS starts.
  Within45min, actual USER_PRESENT/focused interaction plus2min unlocked use and
  fresh sustained upright/movement can stop capture. Screen-on alone cannot.
  Pause/alarm/owner changes suppress the heuristic. After mic/Awake drain, a
  transaction checks current identity and modification/provenance evidence before
  deleting only this run's untouched local record; otherwise it remains interrupted
  for review. Audio unlink is restricted to that session's deletion journal; failed
  files stay pending. Both outcomes skip successful-night backup and never re-arm.
- **Gentle controls:** Nap now exposes sound delay, independent vibration start
  and flashlight alongside sunrise/ramp. Both editors share Off/30s/all1–24minute
  values; expanded nap selectors restore to their correct setting. Values reach
  the existing actual wake policy after Start/Save, never while choosing them.

Commits63cc7b7 (reserve),cb245e2 (repeat rules),28ceaa4 (false starts),
258174e/b9f5a4c/e6f6ad8 (gentle controls) and0e352bc (integration).
No new tests, lint, screenshots, alarms, microphone, sensor, camera, provider or
personal-phone actions ran. OS delivery, accuracy, data cleanup and visual/runtime
acceptance remain unverified. The earlier9c6b1c9 APK excludes this follow-up.

Published [0e352bc development APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-features-20261025-0e352bc),214865843bytes.
Uploaded SHA256 matches the frozen local signed artifact:
`f3516def81934f9a4ef5edb5b425b27f7140f70bf43f9d426a70f74106ce3836`.
[Compact checkmarked checklist](FEATURE-CHECKLIST.md) separates implemented paths
from missing scope and runtime/design acceptance. No personal-phone installation.


## Next occurrence, offline phrase and FIFO batching —8399109

Combined production **assembleDebug PASS2m33s**. Schema15 / metadata backup13
unchanged. Original installed signer retained. Build-only evidence; no new tests,
lint, screenshots, provider/sensor calls or personal-phone actions.

- Reviewed **Next alarm only** replaces one occurrence of an enabled saved recurring
  alarm. Review/Use changes the draft and Save applies it. Frozen source/config
  identity fences stale changes; the target must be future, unambiguous and before
  the following ordinary occurrence. Source suppression, consumption, Skip/Undo,
  work-calendar bypass for the explicit target and ordinary recurrence restoration
  share the same override. Missed targets are not backfilled. Current-zone changes
  may require review. Imports retain disabled review-only metadata; Enable clears it.
- Separate **Spoken phrase** challenge has1–3 actual units and stable original
  English prompts. Only explicit Listen can invoke Android12+ on-device recognition;
  Android13+ checks installed en-US support before acquiring microphone/quiet
  ownership. No default/network recognizer or model download. Exact normalized top
  final text and a fresh one-use task proof are required. Cancel/destroy precedes
  release; failed/unclear cleanup retains ownership with Retry. Recording configs
  are diagnostic, not proof of physical release. No audio/transcript is retained;
  speaker identity, liveness and bedroom accuracy are not established.
- Optional **manual sensor FIFO batching** requires a continuous wake-up accelerometer
  with at least150 reserved/max FIFO events. Requests5Hz/15s delivery, removes the
  normal full-night CPU wake lease and records actual capture-time buckets/counts.
  Gaps stay blank. Pause/end freeze the cut, then asynchronously flush before storage
  finalization under a bounded8s drain lease. Incomplete drain marks interruption;
  unconfirmed unregister retains blocked ownership and Retry. Manual immediate
  movement only; microphone/light/sonar/automatic/delay/flip/adaptive/sleep-sound
  combinations are rejected. Hardware, Doze, cleanup, accuracy and energy savings
  remain unverified. The fixed alarm deadline is unchanged.
- Optional **Lock alarm orientation** is default-off, portable and copied by behavior
  defaults. An actual alarm requests its current orientation; global autorotate is
  unchanged and external challenge screens retain their own lifecycle. OS/window
  restrictions and device behavior are unverified.

Frozen APK: **214934427 bytes**, SHA256
`f2a8c27273ff69ea161764acb04080ac8b2d2957af4a534fb407cd27fe170102`.
[Development prerelease](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-features-20261025-8399109).
The phone remains on571018b/schema7; this candidate is uninstalled.
