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

Production code since the prior tested batch: Jump, Smart Bedtime, reversible awake
estimates (schema11/backup8), Smile, radio alarms, optional Google Sleep API input,
circular recorded-midpoint groups, and optional contactless vibration cues.
Debug APKb9c206e builds with Pose/Light/Type code, timed-bedtime repeats, recording
ZIP restore and experimental echo report. Later Pair/Note/Research/Score/Binaural
source awaits a combined build after the scoped Note seed compile correction. No new device validation is claimed.
The phone is untouched. Full visual, hardware, provider and overnight acceptance
remain deferred, and the missing feature rows in the matrix remain authoritative.

## Current feature sprint — 25 October

Latest fully checked source **6ec80fb** passes **1046 pure tests/127 suites**, debug
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
  Semantic object recognition, real camera framing/accuracy and liveness remain absent.
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
- [ ] **Extra loud:** verify actual maximum device alarm-volume behavior, restore the
  prior volume correctly and confirm independent per-alarm persistence. Existing
  settings tests are not proof of speaker output or amplification beyond hardware.
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
- [ ] **Anti-snoring:** verify actual detection-to-cue behavior, cooldown, repeat limits,
  routing and stopping. Contactless analysis currently excludes clips and cues.
- [ ] **Jet lag:** complete useful travel planning and honestly labelled light cues;
  validate scheduling and output. Effective jet-lag prevention is not established.
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
- [x] Run combined unit tests, lint, builds and quiet native regression on the integrated
  current feature batch:1063 pure and108 native methods/all154 originals pass. Full
  remaining-app regression and physical acceptance stay open above.
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
