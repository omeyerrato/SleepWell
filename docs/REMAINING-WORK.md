# SleepWell remaining-work checklist

Updated 24 October 2026. This is the remaining-work view of the build brief and
[full feature matrix](FEATURE-MATRIX.md), not a declaration that unchecked items
have no code. Some need implementation; others have code but still need correction,
visual comparison or functional evidence. No percentage is inferred from item counts.

The design reviewer owns **visual design only**: inspect actual Android originals
against the supplied boards, identify differences, and accept or reject appearance.
It does not approve algorithms, feature completeness, code correctness or tests.
Implementation and functional verification remain separate responsibilities.

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
- [ ] **Detection controls:** implement and verify per-class sound-duration/sensitivity
  settings while retaining exact existing default policy. Broader cough/cry/laughter
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
- [ ] Run combined unit tests, lint, builds and quiet native regression on the integrated
  candidate, not merely its separate author branches.
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
