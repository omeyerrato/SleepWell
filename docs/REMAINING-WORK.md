# SleepWell current remaining work

Latest published **0.1.11 / ce2cb571** contains the [usability-first source pass](USABILITY-PASS-2026-10-29.md). [APK and checksum](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.11-pickers-feedback-20261030-ce2cb571). APK packaging only; new verification/security/bug-fixing work is deferred by the user. Recording-only cleanup and Last export source are integrated; combined Privacy acceptance and all36 matching remain open. The phone remains on0.1.4; installation is deferred.

Updated 29 October 2026. This is the consolidated current checklist, not a release
history. The goal remains the full applicable phone functionality and **all 36
supplied design screens across 12 boards**. Existing code, a successful build or a
bounded historical pass does not establish that whole goal is complete.

Feature baseline **170b0e44** includes real backup tone/volume, corrected rails,
Gentle's quiet sample, clearer Tone/QR/Nap/rhythm layouts and the scoped
Health/Privacy/document/Cloud repairs. Original-signer arm64 build passed in
1m22s; remote tag and both uploaded hashes are verified. Selected native flows
exercise actions, cancellation, recreation and persistence. [Exact release scope](verification/2026-10-27-reference-menus-build/README.md).

**0.1.4 / 08d3911e** is installed on the S23 Ultra. The installed APK checksum and original user records/settings/files are verified. No personal-phone outputs ran. [Installation evidence](verification/2026-10-28-014-final-build/phone-installation.md). All36 reference states and broader interaction/security acceptance remain open.

## Published in 0.1.8 — phone update deferred

- [x] Integrate Motion sensor reference artwork, clearer real-data chart labels and complete calibration/footer layout. [Seven inspected originals](verification/2026-10-29-motion-reference/README.md); fake sensor ports, not hardware acceptance.
- [x] Reject unsupported external media fade before allocating its newer native listener. Included in the passing build/lint; older-Android runtime remains unverified.
- [x] Integrate the Privacy storage/backup composition and real loading/error/favorites persistence flow. [12 inspected originals](verification/2026-10-29-privacy-reference/README.md); audio-only bulk deletion and a persistent last-export receipt remain missing.
- [x] Build and lint next0.1.8 at `0b3cb9ed`: PASS11m43s.
- [x] Independent package/signature audit and publication passed; exact tag and both uploaded hashes verified. [Evidence](verification/2026-10-29-018-build/README.md).


## Published in0.1.5 — phone update deferred

- [x] Contactless Headphones/Microphone status and explicit access/settings recovery, real route invalidation and fit. [58.348s / 11 inspected originals](verification/2026-10-28-contactless-settled-glyphs/README.md); generated PCM/private ports, not bedroom hardware.
- [x] Report handle dragging/cancellation, both-boundary keyboard/accessibility actions and saved bounds. [68.984s / 17 inspected originals](verification/2026-10-28-report-period-gestures/README.md).
- [x] Tonight fixed Start above the four-tab dock, readable larger text, real first-alarm action and saved Off/weekdays. [52.457s / 14 inspected originals](verification/2026-10-28-tonight-composition/README.md).
- [x] Automatic tracking core choices/restorable supporting pages, explicit Save/Arm, active-wait guard and status-bar contrast. [83.548s / 28 inspected originals](verification/2026-10-28-automatic-tracking/README.md); synthetic callbacks/private phases, not actual service arming.
- [x] Generate and integrate73 raster control icons, flat launcher/themed mask and notification art; build and publish0.1.5 with both server hashes verified. Current native method passes50.332s/18 originals, all73 glyphs and native launcher previews inspected. UI04 SystemUI contrast remains open; this is not all18-screen visual acceptance. Earlier other-flow captures predate the rasters.


## Published in0.1.6: clearer navigation and settings

These changes are published in0.1.6; they are not installed on the phone.

- [x] Search reaches the chosen alarm and actual Extra loud or Flashlight options, with an explicit existing-alarm/new-draft choice. Query, options and unsaved draft restore; only Save alarm writes. [18-original action/persistence evidence](verification/2026-10-29-feature-findability/README.md).
- [x] Challenge rows open configuration, direct choices show selection, and the28-task catalog is searchable. Cancel also restores the pinning draft. [18-original settled comparison/action evidence](verification/2026-10-29-challenge-selection/README.md).
- [x] Automation uses compact event-to-destination cards. Reselecting a destination preserves its details; Test, Delete and Save have distinct behavior, and scheduling failures remain visible. [10-original card evidence](verification/2026-10-29-automation-rule-cards/README.md); external delivery remains unverified.
- [x] Protection separates saved choices from missing Android access, restores its explanations and supports read-error Retry. [16-original recovery evidence](verification/2026-10-29-protection-recovery/README.md); live alarm enforcement remains unverified.
- [x] Verify Search and Challenge together after merging their shared alarm editor: two existing methods passed69.905s/33.847s; all36 scoped originals inspected. [Evidence](verification/2026-10-29-editor-usability-integration/README.md). This is not acceptance of all36 design references.
- [x] Build and publish0.1.6 with the original signing identity, retained assets/libraries and unchanged schema17/backup14; exact source tag and both server asset hashes verified. Phone installation remains deferred.
- [ ] Finish full-routine interactive preview across all28 task types and random routines. Work continues separately; this feature is not complete.

## Published in 0.1.7 — phone update deferred

- [x] Sleep sounds header navigation and fixed Timer/Fade actions, with draft/scroll/focus retained. [14-original proof](verification/2026-10-29-sleep-mixer-reference/README.md).
- [x] All 28 routine-preview controllers integrated: 11 screen, six motion/NFC, five audio and six camera. [Motion](verification/2026-10-29-routine-motion/README.md), [audio](verification/2026-10-29-routine-audio-visual/README.md), [camera](verification/2026-10-29-routine-camera/README.md). Genuine rules, synthetic inputs; physical/model and actual-alarm acceptance remain open.
- [x] Walk completion waits for confirmed owned-listener cleanup; failed cleanup retains progress and exposes Retry. [Four-original proof](verification/2026-10-29-routine-audio-walk/README.md).
- [x] Inline Sequence/Retype error feedback replaces overlapping Toast/native input error. [Six-original proof](verification/2026-10-29-cognitive-inline-feedback/README.md).
- [x] Code/NFC tabs, dedicated NFC task setup and explicit retained tag enrollment. [22-original proof](verification/2026-10-29-morning-code-enrollment/README.md); no real tag/camera claim.
- [x] Consistent Report averages and corrected tooltip/goal-caption overlap. [18-original proof](verification/2026-10-29-report-rounded-means/corrected-40822971575/README.md).
- [x] Nine supporting Back handlers use lifecycle dispatchers; native permission/API call guards and documented native-view lint exceptions are repaired. Final merged private audio-practice flow passes 32.298s / 26 inspected originals. This exercises buttons/recreation/cleanup, not all nine actual Back gestures. [Evidence](verification/2026-10-29-017-routine-integration/README.md).
- [x] Combined arm64 APK and lint pass at `02aacc6b` (8m 4s, 45 tasks). The first candidate's 19-error lint failure remains archived.
- [x] Independent original-signer/package audit passed: retained assets/libraries, compiled manifest and unchanged schema 17 / backup 14. [Evidence](verification/2026-10-29-017-build/README.md).
- [x] Publish 0.1.7 and verify its exact tag, APK and checksum hashes. Installation remains deferred by the user.


## Current top priorities

- [ ] Match all36 reference screens. Current Report, Tone/Gentle, Snooze, QR, sleep plan, rhythm/Nap, Settings root and Appearance have bounded comparison/action evidence. Remaining screens, variants and full reference fidelity remain open. [Screen-by-screen acceptance](UI-REFERENCE-ACCEPTANCE.md).
- [x] Complete Report calendar/edit verification. Current selected Report method passes55.975s/11 inspected originals, including real calendar/day changes, notes/tags/awake edits, Save/reopen and Back-discard. [Evidence](verification/2026-10-27-report-proportions/README.md).
- [ ] Remaining interaction bugs and security review. Concrete shortcut/touch/calendar/QR/NFC/Health/Privacy/document/Cloud fixes are merged; their selected native scopes pass. Readiness denial/rationale/permanent-denial Settings recovery now passes its real Android journey (35.776s/13 inspected originals). Contactless and Snoring keep their bounded passes. Automatic recovery, clear live Tracking options, Room/Motion and Wind-down/Guard now pass their named native action/recreation/persistence journeys; actual label taps also pass. The full-night sonar timeline, shared Math/wake-check UI and calendar-consent draft recovery now have bounded native evidence in published0.1.3; real alarm admission/provider/overnight behavior remains unverified. Broader interaction, provider, hostile-overlay and full security acceptance remain open.

**0.1.4 / 08d3911e** is installed on the S23 Ultra. The installed APK checksum and original user records/settings/files are verified. No personal-phone outputs ran. [Installation evidence](verification/2026-10-28-014-final-build/phone-installation.md).


## Current user priorities

- [x] Shared navigation/control haptics and pressed/selected feedback, with a saved off switch; physical feel remains unchecked.
- [x] First Settings/alarm-menu organization pass: eight groups, search and parent-aware Back; every feature retained.

Sourcef0c1a274 builds app/UI-check APKs in18s. The bounded organization flow
CI40356841938 passes at test-only successor 957f3380, with ten inspected actual
captures. Physical haptics and every remaining menu are not accepted by this pass.
All17 former root destinations remain in eight groups. Guarded ordinary alarm
API/snooze ownership source is included; actual client/delivery behavior remains
unverified. [Scoped evidence](verification/2026-10-26-settings-organization/README.md).
[Organization map](UI-ORGANIZATION.md) · [compact checklist](QUICK-CHECKLIST.md).

## Current delivery state

| Item | Current state |
| --- | --- |
| Installed phone build | **0.1.4 / 08d3911e**, installed APK hash and original user-data preservation verified; expected internal API revisions/profile cache only. |
| Phone availability | Update deferred by user; no phone/ADB action is being taken. Last verified installed build0.1.4. |
| Published candidate | **0.1.8 / 0b3cb9ed arm64**, APK/lint11m43s; signer/tag/server file hashes verified. Phone0.1.4; update deferred. Schema17/backup14 unchanged. |
| New feature evidence | Organization 33.114s/14 inspected captures, challenge ordering 40.237s/6 and recording analysis 40.27s/5. Prior Report/menu/Travel evidence remains separate. Per-alarm sensor accuracy, physical output, all 36 screens and overnight acceptance remain open. |
| Current work policy | Features first; no new unit-test batches or broad campaigns. Essential builds, preservation and bounded changed-flow checks remain applicable. Dedicated reviewer stays stopped. |

[Latest published release and scoped evidence](verification/2026-10-27-reference-menus-build/README.md).
[Prior model/radio/rhythm scope](verification/2026-10-26-radio-rhythm-recognition/README.md).

[Phone update evidence](verification/2026-10-26-phone-2cbfe043/README.md) is separate
from the earlier [single Report flow](verification/2026-10-26-report-flow/README.md).
That Report flow passed with8fixture captures and persisted edits, but did not
cover provider-stage rendering, real audio, these new integrations or all36screens.
The later compact editor flow shows the full default-size main composition and saved awake/tag/note state; secondary sheets, large fonts and full Report parity remain open. See [latest Report evidence](verification/2026-10-26-report-editor-and-chart/README.md).
Phone actions follow the human's explicit instructions; this status artifact grants no additional authority.

## Concrete remaining implementation and interpretation limits

Published **33cb5693** includes the optional guarded smart-wake API fields from
a8842ada; external-caller/runtime acceptance remains open. [API contract](ALARM-CONTENT-PROVIDER.md).

- [ ] Resolve reliable automatic laughter recognition. The new **Laughter** task is
  an experimental YAMNet-family estimate; earlier failed independent positives and
  the rejected lower-threshold candidate remain unresolved. **Loud sound** uses
  relative microphone RMS, not calibrated dB or instrument recognition. Broad
  **Speak**, exact offline **Spoken phrase** and **Musical note** are distinct paths;
  none establishes generic instrument identification or laughter accuracy.
- [ ] Implement phone sleep-stage/REM estimates with a suitable redistributable
  model and exact input contract or original licensed training. Clinical certification
  is not a prerequisite for honestly labelled experimental estimates. Existing motion,
  YAMNet classes and timed cues do not provide that algorithm. Breathing/apnea remain
  separate. See [feasibility and next work](PHONE-STAGE-FEASIBILITY.md).
- [ ] Complete broader integration parity: original read-only summary access and
  reviewed incoming requests now exist, but full alarm-editor API coverage and extended/vendor
  semantic conversion remain absent; ordinary guarded CRUD is now in source. Actual client grants/revocation and four new
  sound-event lifecycles/recipients require bounded runtime acceptance.
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
Current production also retains movement-night sound-analysis outcomes and offers
explicit Report playback/PCM waveforms from actual linked event audio. These are
build-only paths; capture, recovery and actual playback remain unverified. New
metadata still uses schema17/format13, but older SONAR-only readers reject motion
analysis evidence. The current reader accepts earlier empty/SONAR data. See
[motion/report boundaries](MOTION-ANALYSIS-AND-REPORT-AUDIO.md).

Current production already includes saved alarm defaults, date/repeat/exclusion
rules, advanced snooze/backup controls, reviewed requests, Health Connect/calendar
integration paths, local/ambient playlists, audio backup/restore and AAC export,
CSV source inspection, awake estimates, calibration and additional challenge paths.
**Jumping is implemented as an original touch-controlled hurdle game**, with saved
progress and a Math alternative; it is not physical-jump sensing. See
[JumpTask](../app/src/main/java/org/sleepwell/app/JumpTask.java) and
[ChallengeRoutine](../app/src/main/java/org/sleepwell/app/ChallengeRoutine.java).
Runtime acceptance for these paths remains open rather than implementation absent.

## Functional acceptance still required

- [x] Separately authorized installation of 2cbfe043 with installed hash and
  original rows/preferences/user files preserved. Both snapshots already use
  schema17; this is not a first-open migration test or app runtime acceptance.
- [ ] Real phone app navigation, current Device Care state and functional acceptance
  remain unverified; do not change suspension or run outputs without authorization.
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
- [ ] New motion per-night request/count/error/checkpoint/finalization and pause/recovery;
  actual inline Report audio/PCM waveforms, stale-state revocation, capture cleanup
  before parent Math switching and backup compatibility/persistence.
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

The dedicated reviewer remains stopped. The full design/security/broad bug campaigns remain deferred. A bounded Report
implementation correction does not reinstate the stopped reviewer or accept all screens.

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

## New source and focused verification

- [x] Report calendar/edit and proportion corrections; actual actions, Save/reopen and eleven inspected captures.
- [x] Real backup tone/volume, format14 with legacy imports and correct progress rails; same native journey plus fake-output policy assertions.
- [x] Gentle quiet ramp sample, Tone selection/import/preview controls and compact QR enrollment; real configuration/persistence with injected outputs.
- [x] Rhythm/Nap layout/actions, Health permission recovery, Privacy deletion/cleanup recovery and Cloud folder rotation pass their named private native flows.
- [ ] All36 reference states and broader interaction/security acceptance remain open. Published0.1.2 has inspected native action/persistence evidence for these changed flows. All36 reference states and physical/overnight/provider behavior remain open.

Published170b0e44 is now installed with verified original user-data preservation. No alarm, sound, flashlight or haptic test ran. Version0.1.4 is installed with verified original user-data preservation. [Installed-build evidence](verification/2026-10-27-reference-menus-build/README.md).
