# SleepWell remaining-work checklist

Updated 25 October 2026. This is the remaining-work view of the build brief and
[full feature matrix](FEATURE-MATRIX.md), not a declaration that unchecked items
have no code. Some need implementation; others have code but still need correction,
visual comparison or functional evidence. No percentage is inferred from item counts.

Current priority: **finish features first**. The dedicated design reviewer has been
stopped and its slot reassigned to implementation at your request. Visual correction,
broad bug review and security auditing are deferred. Essential build, data-preservation
and functional checks continue; unchecked visual items below remain future work.

## Six-hour production window — 25 October

Latest user deadline: **5:23 PM Riyadh (14:23 UTC)**. New unit tests, native test
batches, screenshot reviews, evidence archiving and broad audits are stopped.
The next APK requires a production build; build failures are fixed as integration.

The six-hour deadline has passed; full parity remains unfinished. Published
production source **90e2583** built in **14s**, with schema15 and backup
format13. The frozen debug APK has the same signing key as the installed app.
This is **build-only evidence**; no new tests, lint, screenshots or physical-phone
runs were performed in this window. The phone remains on571018b/schema7.

Follow-up application source **bff9f7e** builds in **2m59s** with the same schema,
backup format and installed signing key. Four additional source flows now exist:

- [x] Weekly nap definitions create reviewed recurring wake alarms, with durable
  group ownership, retry/cancel, cross-midnight weekdays and independent-edit
  detachment. They do not automatically start nap tracking.
- [x] Optional explicitly started narration speaks original guide steps using an
  installed English engine-declared offline voice; silent guidance remains usable.
- [x] Configured Object recognition uses actual local CameraX/LiteRT classification,
  repeated-frame confidence, saved targets and run-bound completion/Math fallback.
- [x] Pre-travel sleep shifts provide dated home/destination plans, default-off
  silent reminders and separately saved new wake-alarm drafts.

These checkmarks mean the described production paths exist, not that their runtime,
accuracy or visual acceptance is complete. They are absent from prerelease90e2583
and included in the publishedbff9f7e development prerelease.

Source **53a0c08** adds exact1second..24hour standard timer requests through a
styled duration picker, reviewed new nap draft, separate Start, actual millisecond
deadlines, saved state and reopening. Strict optional NapPlan v2 metadata preserves
seconds; legacy v1 minute plans remain unchanged. Exact timers disable Power nap
and smart early waking. Combined production build **PASS3m2s**, original signer
preserved; actual OS timer delivery remains unverified.

The [53a0c08 development APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-features-20261025-53a0c08)
is now published with all five additions; uploaded size/digest match the local APK.

### Additional source after53a0c08

- [x] Custom exact timers are reachable directly from Nap; cancelling the duration
  picker leaves the draft unchanged. Nap profiles and narration entry labels now
  describe their expanded behavior.
- [x] Default-off battery-aware ACTIVE movement/light sampling requests a reduced
  rate at20% unplugged, restoring normal at25% or charging. Automatic waiting,
  microphone/sonar, capture admission and the fixed alarm deadline are unchanged.
  Real sample counts and policy/calibration provenance are retained; no savings or
  accuracy amount is claimed.
- [x] Generated morning QR enrollment creates actual PNG/PDF pages and Android
  printing. Only label/hash enter the task after explicit Use/Save; export or
  Cancel does not replace enrollment. No camera/network is started by generation.
- [x] Home widgets compare acknowledged owned snoozes with ordinary/nap deadlines,
  showing seconds and a review state for unconfirmed snoozes. Widget refresh/taps
  cannot schedule/fire alarms. Old snoozes without receipts remain unconfirmed.

Combined source **9c6b1c9** builds **PASS1m23s**, with the original signing key,
unchanged schema15 and backup13. These additions still require runtime acceptance.
The published53a0c08 APK does not contain them.
They are included in the [9c6b1c9 development APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-features-20261025-9c6b1c9),
whose uploaded size/SHA256 match the frozen locally signed artifact. It remains
uninstalled on the phone.

### Advanced repeats and automatic-tracking follow-up

Combined source **0e352bc** builds **PASS2m31s**, original installed signer,
schema15 / backup13 unchanged. These are source paths with build-only evidence:

- [x] Odd/even ISO-week alarms and anchored every1..365day repeats, including
  sunrise, ordinary projections, skipped dates, actual delivery eligibility,
  backup validation and alarm-list labels. Explicit Use then Save alarm required.
- [x] Separate default-off battery reserve stops/saves tracking at the selected
  low unplugged threshold, cancels initial waiting before capture and never
  changes alarms or restarts tracking. Unknown battery never triggers it.
- [x] Separate default-off automatic STILLNESS false-start rejection requires
  sustained unlocked phone use and fresh upright/movement evidence within45min.
  It drains capture and transactionally discards only the current untouched owned
  record; edited/linked/uncertain records remain interrupted for review. No restart
  or successful-night backup; audio deletion uses only its session journal.
- [x] Nap Gentle now exposes real sound delay, independent vibration timing and
  flashlight; both editors offer Off/30s/every1–24minute timing values.

- [ ] Actual odd/even/interval/sunrise delivery, clock/DST/calendar/restore behavior.
- [ ] Real low-battery cleanup, alarm preservation and false-start accuracy/deletion.
- [ ] Expanded gentle-control persistence and physical effects on a controlled device.

These additions are absent from the earlier9c6b1c9 APK. Full parity is unfinished.

This window adds actual production paths for:

- Extra loud optional per-player+6dB processing, with max-stream fallback.
- Smart Bedtime, per-alarm reminder overrides, selected-date exclusions, relative/
  speech time entry and a total snooze-time budget.
- Optional Google sleep-confidence input, reversible awake estimates and measured
  foreground motion calibration, with per-night configuration provenance.
- Jump/Smile/Pose/Light/Camera light/Type code/Spin/Musical note challenges and radio.
- Paired-phone motion, periodic-echo experiments, generic Home Assistant PIR input,
  recorded-night scoring, reviewed-day SRI and timed dream cues.
- Named one-date nap profiles with portable definitions, silent timed guidance and
  real binaural generation.
- Whole-library audio ZIP restore, reviewed private-diary import, standard reviewed
  alarm/timer requests and explicit read-only sleep-summary sharing.
- Separately consented contactless Sound/Vibration/Both snoring cues with channel
  revocation, headphone routing and output/capture exclusion.
- Explicit per-night contactless ten-second WAV clips from the shared microphone
  stream, using actual persisted intervals, existing limits and the normal player.
- Manual and separately consented automatic whole-library audio-folder backups,
  including actual night-ending, cancellation and history-erasure hooks.

These are implemented source paths, not claims of verified delivery or accuracy.
See the [feature matrix](FEATURE-MATRIX.md) for exact controls, bounds and evidence.

### Implementation still missing or narrower than parity

- [ ] Validated phone sleep-stage/REM and breathing/apnea/disturbance analysis.
  Periodic echoes and inactivity estimates do not establish those measurements.
- [ ] Reliable laughter/instrument identity models. Object classification now has
  source, but real camera accuracy and framing remain acceptance work.
- [ ] Dedicated Sleep Phaser hardware protocol; current PIR uses a user-owned
  Home Assistant HTTPS binary sensor and requires that hub/sensor.
- [ ] Direct legacy Google Fit/Samsung Health SDK exchange; Health Connect is the
  existing sleep-data route and requires supported services and permission.
- [ ] Hosted SleepCloud/research community and Spotify account/service parity.
  Public HTTPS radio and user-owned SAF backup folders are the available paths.
- [ ] Clinical chronotype validation and established jet-lag/lucid-dream efficacy.
- [ ] Actual assistant/provider handoff and exact-timer OS delivery. Requests now
  support exact1second..24hours; no-UI scheduling remains intentionally unsupported.

### Acceptance deferred at your request

- [ ] Actual overnight accuracy/background/battery and interruption behavior.
- [ ] Physical alarm, ramp, volume restore, sunrise, torch and best-effort app/power
  restriction behavior. Hardware forced-off cannot be prevented by an ordinary app.
- [ ] Actual model/camera/microphone/pitch calibration and two-phone behavior.
- [ ] Weekly nap delivery/cancellation across clock, DST, reboot and data restore;
  offline TTS provider/output/focus behavior; dated travel-reminder delivery.
- [ ] Actual hub/Health Connect/calendar/SAF/cloud/assistant service exchange.
- [ ] Backup then install the new schema15 candidate and verify original data.
- [ ] Reference-by-reference visual correction, bugs and security review.

## Historical checked feature batch — before this production-only window

Historical checked source **6ec80fb** passes **1046 pure tests/127 suites**, debug
and native-test APKs, and lint with zero errors/142 warnings in5m37.
[Raw local evidence](verification/2026-10-25-armed-snooze-build/README.md).

[CI40167823818](https://github.com/omeyerrato/SleepWell/actions/runs/40167823818)
on the same source passes **38classes/97native methods/all121 required originals**.
This includes corrected picture matching, armed conditions and exact-clock snooze.
[Original native evidence](verification/2026-10-25-feature-increment-pass/README.md).
This is explicit-scope functional evidence, not full app/design/hardware acceptance.
Final application/test source **0a821eb** passes **1063pure/129suites**, debug/native
APKs and lint0errors142warnings. [CI40170787840](https://github.com/omeyerrato/SleepWell/actions/runs/40170787840)
passes **44classes/108native methods/all154 originals**, including the corrected
waveform control race, protected Power Nap and ordinary-nap regression. [Final
raw evidence](verification/2026-10-25-final-feature-batch/README.md). The predecessor
ba2d478 also builds an unsigned release APK; it is not a signed current release.
This verifies the named feature batch, not every app feature or physical effect.

- [x] Alarm defaults, explicit dates, saved inputs and bounded per-press snooze choices.
- [x] Calendar alarm rules, sleep-only Health Connect and optional integration contracts
  in private/fake-provider native flows; actual accounts/providers remain unverified.
- [x] Saved visual search, counting/flags/tilt, mirror text/diary and shared previews.
- [x] Tags/quantities/management, recorded insights, history search and dated goal bank.
- [x] Manual cloud-folder metadata versions and reviewed restore; automatic default-off
  backup Storage12/Flow2/all8 originals now pass with private provider ports.
- [x] Local sunrise Storage6/Flow1/all4 and fixed sleep-plan time copy Storage3/Flow2/
  all7 originals pass. Save is required; the copied time does not follow later plans.
- [x] Silent/ordered/shuffled modes, stable sound controls and active-tracking DND
  configuration pass; actual output/OS ownership still require separate checks.
- [x] Manual delay Storage4/Flow2/all9 and optional flip-pause Storage3/Flow2/all7 pass.
- [x] QR/general barcode enrollment Storage3/Flow1/all5 originals pass.
- [x] Per-class duration plus category inclusion Storage3/Flow3/all8 originals pass.
  Custom inclusion suppresses all new clips; unreadable settings block analysis/clips.
- [x] Speak configuration/storage/capture plus six pinned CPU trials pass narrowly.
  This detects broad speech, not words, laughter, instruments, identity or liveness.
- [x] Picture match: actual pixel/private storage3 + configuration1/all5 originals pass.
  This historical batch predates semantic object classification. Real camera
  framing/accuracy remains unverified; liveness is not supplied by either task.
- [x] Arm-tonight charging/screen-off conditions Storage3/Flow1/all3 originals pass;
  smart-window projection follows actual dated/sunrise/skip/calendar deadlines.
- [x] Optional exact same-day clock snooze Flow2/all7 originals pass; midnight and
  ambiguous/skipped DST times retain relative-duration alternatives.
- [x] Recorded-night milestones: Storage2/Flow1/all6 originals pass; actual saved coverage, edit/recalculate/reopen and errors verified privately.
- [x] Power Nap minimum-rest/latest-wake interval and unchanged ordinary naps: Storage3 + new Flow1 + old Flow3/Integration1, all27 originals pass with fake output ports.
- [ ] Live hardware, overnight capture, actual external exchange, new phone installation,
  remaining matrix subfeatures and later design/security review.

Earlier bounded milestones remain archived: [778-test/77-native-method sprint](verification/2026-10-25-feature-sprint/README.md),
[102-method expanded native gate](verification/2026-10-25-feature-sprint-expanded/README.md),
[956-test speech/flip build](verification/2026-10-25-speech-flip-build/README.md),
[Speak/flip narrow native evidence](verification/2026-10-25-speak-native/README.md),
and [sunrise/backup predecessor run](verification/2026-10-25-sunrise-backup-native/README.md).
The full app native run and independent laughter accuracy gate remain failing.

## Your highest-priority features

- [ ] **Prevent leaving/powering off during alarms:** finish best-effort behavior and
  later verify actual Samsung handling. Configuration is tested; enforcement is not.
  Forced hardware restart and powered-off ringing remain device limitations.
- [ ] **Extra loud:** verify the new optional owned-session+6dB processing, fallback,
  actual maximum stream output and prior-volume restoration. No processing target
  can bypass speaker hardware limits; this source has build-only evidence.
- [ ] **Gentle volume increase:** verify real ramp, sound delay and independent
  vibration timing through start, snooze, backup and dismissal.
- [ ] **Sunrise wake-up:** verify actual screen-brightness progression and cleanup,
  including locked-screen delivery. Settings and policy code exist.
- [ ] **Flashlight wake-up:** verify actual torch ownership, unavailable-camera errors
  and cleanup. Do not run this or any alarm/output test on the sleeping user's phone.
- [ ] **Sleep noise analysis:** finish shared contactless/noise UI flow verification;
  preserve explicit off/unknown/complete/error states. Real-model fixture replay
  passes, but representative bedroom accuracy, sustained capture and battery do not.
- [x] **Detection controls:** per-class accepted-duration settings retain the existing
  default policy; native save/reopen/error flow passed at3463834/CI40124391172.
  Broader cough/cry/laughter
  recognition remains incomplete; the rejected laughter candidate stays rejected.
- [ ] **Auto-detect sleeping:** validate real scheduled/inactivity/recurring starts,
  permission and charging changes, false starts and background restrictions. Phone
  inactivity is an estimate, not proof of sleep or measured sleep stage.
- [ ] **Anti-snoring:** verify actual detection-to-cue behavior, cooldown, repeats,
  routing and stopping. Contactless Sound/Vibration/Both now has separately saved
  channel consents and capture masks; clips use a separate per-night opt-in.
- [ ] **Jet lag:** pre-travel sleep-shift planning and separate wake drafts now exist;
  validate reminder scheduling and experimental light output. Effective jet-lag
  prevention is not established.
- [ ] **Reports and charts:** finish the reference composition throughout the Report
  flow; complete historical dated-goal accounting, missing-day handling and insights.
- [ ] **Alarm interaction:** keep the list compact and detailed settings inside each
  alarm; finish task-row/selector/back/cancel/recreation consistency. Verify the new
  walking Math fallback without running a real alarm on the phone.

## Visual acceptance: every board remains a whole-board gate

Individual states have passed comparisons; that does not finish their entire board.
Every required state needs an actual Android original and design-only comparison.

- [ ] **01 Everyday:** Tonight proportions/arc meaning, grouped navigation, full Report
  hierarchy and focused wake-up challenge.
- [ ] **02 Alarms:** compact list, editor time controls, smart-window chart and states.
- [ ] **03 Sound and snooze:** sound list, real ramp chart, gentle settings, snooze/backup.
- [ ] **04 Challenges:** routine rows, enrollment, difficulty controls and wake-up check.
- [ ] **05 Bedtime:** wind-down, selected blocked apps, sleep-goal/bank presentation.
- [ ] **06 Tracking:** restore the reference's bedside placement composition; reduce
  disabled-control/helper bulk; finish calibration and live-tracking layout.
- [ ] **07 Audio:** event rows/waveforms/player, consistent selectors and anti-snore UI;
  keep sound-mixer work below the user's core alarm/noise priorities.
- [ ] **08 History:** finish full history/trends/editor parity beyond the bounded
  corrected ribbon/filter/editor states already accepted.
- [ ] **09 Rhythm/naps/travel:** charts, controls, lower content and supporting states.
- [ ] **10 Sensors:** automatic tracking, motion test and actual room-signal states.
- [ ] **11 Settings/integrations:** icon consistency, hierarchy and complete supporting
  permission/disconnected/error states for working integrations.
- [ ] **12 Reliability/privacy/appearance:** complete states, previews and consistent
  controls, including optional light connection prerequisites.
- [ ] **Across all screens:** Inter scale, icon weight, 48dp targets, spacing, selected
  states, readable charts, no clipped footers, coherent system bars, font scaling,
  reduced motion, concise copy and no development terminology in product UI.

## Remaining broader phone-feature scope

The matrix contains exact subfeatures, source files, tests and current limitations.
These groups remain open even after the priority list above is addressed.

- [ ] Complete alarm defaults, supported date/repeat rules, advanced snooze/backup,
  timezone/DST/rescheduling and reliability cases identified in the matrix.
- [ ] Complete applicable additional challenge categories, barcode/enrollment variants,
  previews, accessible alternatives and unavailable-sensor/fallback flows.
- [ ] Finish bedtime guard/reminder and pause behavior under actual OS restrictions.
- [ ] Finish awake/stage/breathing feasibility and analysis. Do not substitute
  decorative stage graphs, phone inactivity or movement for validated measurements.
- [ ] Finish rhythm/chronotype/regularity calculations with adequate real observations,
  tag quantities/management/impact, dreams and remaining record-edit subfeatures.
- [ ] Finish remaining nap schedules/routines and feasible phone dream/light flows.
- [ ] Finish supported sleep-sound/local-source features after core priorities.
- [ ] Implement real sleep-only Health Connect exchange, calendar rules, optional smart
  lights and explicit Tasker/Intent/webhook/MQTT integrations, with permissions,
  credentials, actual exchange and failure handling.
- [ ] Finish optional user-controlled cloud/audio backup and remaining export/import
  coverage; document each external provider/account prerequisite.

## Release and verification

- [ ] Fix remaining failing full native checks, including the existing independent
  laughter positive. Keep genuine failures visible; do not weaken gates to get green.
- [ ] Complete new shared-analysis and walking native journeys; inspect their original
  screenshots and verify saved/reopened state and unchanged existing data.
- [x] Historical0a821eb batch passed1063 pure and108 native methods/all154 originals.
  This evidence predates the current production-only source and does not validate it.
- [x] Build current production debug APK53a0c08 and verify the local signing key.
  No new test/lint/screenshot batches ran; physical acceptance stays open.
- [ ] Verify real overnight background behavior, capture quality, battery use and
  interruptions when a suitable controlled test is available.
- [ ] Perform alarm/speaker/vibration/torch/power-menu checks only when permitted;
  current no-disruptive-phone-test restriction remains in force.
- [ ] Back up, install the verified candidate on the phone, and verify data/schema/APK
  preservation. Latest verified phone installation remains `571018b`.
- [ ] Update the feature matrix, README, privacy/permission limitations, release APK
  and installation instructions to match the final verified source.

Wearables remain excluded. No feature paywall, subscription or advertising is to be
introduced. External services do not become free merely because SleepWell is free.
