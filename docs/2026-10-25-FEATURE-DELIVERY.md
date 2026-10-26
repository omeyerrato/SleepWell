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


## Local playlists and owned stillness fade —26 October, b212a67

- Up to eight local audio documents use existing actual100MiB validation and a
  restored draft Add/move/remove→Save flow. Persistent read access is retained on
  Save; selection/restoration never starts playback. Queue uses manual Play only.
- Genuine finite MediaPlayer completion advances tracks. Native pause retains
  position, Brown noise stays continuous and the original total timer/fade spans
  the queue. Queue end and revoked/unreadable files stop explicitly; no track is
  silently skipped. Source documents stay unchanged. A separate bounded reader
  stages only the current file without holding native Stop/alarm/timer controls.
- Separate default-off stillness fading pins tracking policy and claims playback
  admission only after matching tracking-owned audio actually plays. No pre-play
  evidence is reused. Ten contiguous complete fresh low-motion screen-off minutes
  trigger a five-minute monotone multiplier. Missing/high/interactive/pause/clock/
  session changes reset evidence; Off/manual/end/destruction releases admission
  without raising faded levels. Existing timer/alarm cleanup remains authoritative.
  This estimates stillness, not sleep stages. Local playlists remain manual-only;
  this fade uses an eligible tracking-owned ambient mix.
- No new sensors, microphone, DB polling, global media-volume/other-app control,
  account service or alarm scheduling is introduced. Built-in rotating playlists,
  external-player control and Spotify remain separate incomplete scope.

Combined application224f2cd **assembleDebug PASS38s**; final cleanup source
**b212a67 assembleDebug PASS15s**. No new tests/lint/screenshots/provider/audio/
sensor/personal-phone actions. Real provider/decoder cleanup, sensor behavior,
overnight accuracy and visual acceptance remain unverified. Source commits:
852fb82 (fresh stillness signals),224f2cd (queue/service/UI) andb212a67 (destruction
cleanup). Schema15 / metadata backup13 unchanged; original installed signer retained.

Frozen APK **214979803 bytes**, SHA256
`5c4aec36418e5242273fb10b122bfeb048ea0bbfbf487b4864a5dd149b4d52c3`.
[Development prerelease](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-features-20261026-b212a67).
The personal phone remains untouched on571018b/schema7; this APK is uninstalled.


## Ambient rotation, external media and narration controls —26 October,5a73464

- Saved ordered2–5bundled-sound rotation changes primary loops on5/10/15-minute
  actual playing-time intervals. Draft Save/Cancel/recreation stay silent; current
  source, normal tracking ownership and one overall timer/fade are retained.
  Brown's independent bed is muted while Brown is primary. Local queues and
  single tracks remain separate.
- Separate default-off acknowledged external MEDIA-stream fading pins only on
  a new immediate manual nonsonar/nonbatch production recording. Requires Android13+,
  already-playing observable external media/config/routes, normal mode, controllable
  media volume and own mixer/autoplayOff. Distinct admission and actual10complete
  fresh screen-off low-motion minutes precede5min monotone reductions. Observed
  volume/playback/config/device/route/call changes, pause/end/Off/settings revisions
  cancel current authority. No automatic restoration, player launch/stop, audio
  focus takeover or alarm/ringer/DND change. Player may continue at minimum.
  Android cannot reveal every focus/player handover or atomically compare/set
  stream volume; anticipated routing is not identity proof. Disclosures, status,
  token-fenced Stop and acknowledged async Save are explicit.
- Saved narration rate.75/.90/1/1.15 and pitch.85/1/1.15 presets freeze at explicit
  Play and use checked TTS setters before each utterance. Provider rejection is
  visible; the silent guide remains usable. Existing installed offline English
  voice selection remains automatic. Settings/restore do not speak or download.

Source5a73464 **assembleDebug PASS1m5s**, schema15/backup13 and original installed
signer unchanged. No new tests/lint/screenshots/provider/sensor/audio/device actions.
Actual provider/route/concurrency/engine/output/overnight and visual acceptance
remain unverified. Direct SDK/service prerequisites are documented separately;
full parity remains incomplete. Phone untouched on571018b/schema7.

Frozen APK **215023099 bytes**, SHA256
`94b4e08190da2c0cd5f8552b461cb34db2eb60ca787ceaca8ae01bda9b298aca`.
[Development prerelease](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-features-20261026-5a73464).


## User-authorized phone update —26 October

The reconnected S23 received **5a73464** by successful streamed install-r after
private app-data and previous-APK backup. The installed APK SHA256 matches the
frozen published candidate. Read-back comparison confirmed every original table
row/column, preference value and private file unchanged. This is installation/data
preservation evidence, not feature acceptance; no alarm/audio/vibration/torch/sensor
test ran.

A normal launch request was intercepted by Samsung Device Care's existing package
suspension. It was left unchanged. The database remains schema7; first-open migration
to15 and normal foreground launch are still pending after the app is unpaused.
Private backup/journal paths remain ignored locally; no personal data was published.


## Calendar, AAC, legacy favorites and uninstall step —26 October,121dbd5

Combined **assembleDebug PASS1m41s**, original signer retained. New additive main
schema16 stores portable missing-audio favorites; metadataformat13 and ZIPv2 keep
versions with optional fields. Runtime migration/round-trip acceptance is unverified.

- Optional ordinary-uninstall friction uses explicit Android administrator activation,
  actual DPM status and user removal. Empty policy metadata requests no password,
  lock/wipe/camera/owner actions. System BIND_DEVICE_ADMIN protects the receiver;
  internal Activity is private, targets a real exported system activation screen
  and never forwards incoming nested intents. Opening/restore does not activate;
  normal deactivation/settings remain available. No hardware-off/force-stop/OEM
  guarantee and no activation/removal run.
- Direct calendar saving reviews a real effective saved period, title/zone and
  optional note into a chosen writable calendar. On-demand read/write permissions,
  worker source/destination rechecks, durable prepared/inserting/verified/uncertain
  receipts and package+UUID markers avoid blindly repeating uncertain effects.
  Existing external composer remains. No unrelated update/delete; local insertion
  is not remote-sync proof. Source/provider cannot be one atomic transaction and
  later record edits do not rewrite the reviewed calendar copy. Private receipts
  retain title/calendar/source identity/note hash, not raw note or sensor/audio data.
- Actual AAC-LC MediaCodec/MediaMuxer export accepts real selected effective PCM16
  intervals. One M4A or separate parts+manifest ZIP covers a night/favorites selection,
  preserving edit/awake/pause gaps and original WAVs. Bounded input/part/collection
  staging, encode/EOS/drain/finalization, explicit fresh SAF destination and partial/
  cancelled/unavailable-encoder states are implemented. Actual output/padding/
  decodability/provider behavior unverified. WAV/ZIP remains; MP3/Ogg not supplied.
- Optional clip_favoritesv1 preserves legacy independent clip stars without sound
  events and without inventing missing audio. Strict bounded identities, schema16
  orphan storage, transactional restore/erase/session-delete and review fingerprint
  are included. ZIPv2 verified audio attachment consumes exact orphan metadata;
  optional source_duration_ms handles legacy stored-versus-measured durations.
  Older backups remain readable by the new app; older apps may ignore metadata
  stars or reject newly extended ZIP rows. Existing event stars/corrections stay separate.

Source commits1cdadec (calendar),ec80054 (favorites),1c10a30 (AAC),121dbd5 (admin/hook).
No new unit/native/lint/screenshot batches, phone/provider/codec/calendar/admin/
audio/sensor actions. These are implementation/build paths, not functional or
visual completion. Candidate is uninstalled; last authorized phone update5a73464
and its preserved schema7/blocked-launch evidence remain separate.

Frozen APK **215123840 bytes**, SHA256
`23b4f3d03b5f505c9919669782e87c88fb009cfcd6bf5f4480db5e248a8aa46b`.
[Development prerelease](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-features-20261026-121dbd5).

### Subsequent authorized phone installation —26 October

The reconnected S23 received121dbd5 by successful streamed install-r. The
installed APK hash matches the frozen release. A fresh private backup precedes
installation; readback confirms all original table columns/rows, preferences and
private files are unchanged. Samsung Device Care still suspends the package;
no launch or suspension change was attempted. Database schema7 remains unchanged
until the first-open migration to16. No alarm, audio, vibration, torch, sensor,
provider, admin or UI tests ran on the phone. Earlier installation notes above
are chronological, not the current installed version.

## Portable settings, CSV source import and Tonight layout —26 October,32f87b9

Three implementation agents worked in separate worktrees. Their combined source
adds actual reviewed document import/export and saved optional home configuration:

- Settings-profile version1 exports selected Appearance, Sleep plan, Bundled
  sounds, Narration and Tonight layout. Strict64KiB typed JSON, source-value review,
  explicit Apply, stale settings/runtime checks and private interrupted multi-file
  recovery preserve the intended boundary. Imported reminder/playback activation
  stays off. Capture, credentials, grants, enrollments, permissions, history/alarm
  replacement and active authority remain excluded. Local audio export is rejected
  in the sounds category with explicit deselection guidance.
- Sleep as Android CSV review appends selected new periods, notes, tags, zones and
  exact integral ratings. Conflicting IDs, exact periods and retained deleted-ID
  receipts are kept. Exact source rows retain fractional rating, LenAdjust/net
  aggregate and vendor signal/event fields in the same main database transaction.
  App duration is the imported period; original adjusted duration is separately
  visible. No sensor/stage graph or located awake interval is fabricated.
- Schema17 adds linked CSV source storage/deletion; optional metadata13 `sleep_csv`
  is included in backups, library metadata, stale fingerprint and restore.
  The older sensor validator's format8 ceiling is replaced with the supported
  Backup.FORMAT bound, restoring current-format9–13 acceptance at source level.
- Tonight optional row order/show/hide and artwork are saved drafts with Reset,
  Cancel and stale explicit Save. Goal bank/latest non-nap links default off and
  read actual stored data. Default composition and mandatory alarm/nap/tracking
  controls remain. Arrangement transfers through the separate profile category.

Source commits7df9408 (layout),f7387d5 (profiles),00cba5e (layout profile),dd88594
(CSV),32f87b9 (root lifecycle/entry/provenance integration). Production
`:app:assembleDebug` **PASS2m2s**, original installed signer, schema17/backup13.
New source paths have build-only evidence. No new unit/native/lint/screenshots,
provider/document, migration/recovery, sensor/output or personal-phone actions.
Full visual, runtime/overnight, classifier accuracy and service parity remain open.
Details: [portable settings/CSV](PORTABLE-SETTINGS-AND-CSV.md).

Frozen APK215249828bytes, SHA256
`44cd037266f37b7a87690262967fe41c21d4a826abc01ec4715d9da0f1ae2dfb`.
Candidate is uninstalled; last authorized phone source121dbd5 remains Device Care
suspended, with schema7 last verified pending its first-open target16.

[Development prerelease](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-features-20261026-32f87b9)
is published. GitHub confirms draftfalse, prereleasetrue, the exact source target
and both assets uploaded; APK size and SHA256 match the frozen local artifact.

## Confirmed automation, original CSV signals and future calendar —26 October,86d0d1e

Three separate implementation worktrees supplied source-signal inspection,
confirmed lifecycle events and incoming reviewed tracking requests. Root added
future-night calendar consent/queue/jobs and integrated the actual boundaries.

- Ten opt-in outbound event kinds, seven new: committed recording pause/resume,
  foreground alarm admission, acknowledged snooze plus stopped owner, durable
  confirmed/missed check phases and submitted wind-down notifications. Same source
  transition has a stable event ID; schema1/minimal fields and existing transports
  remain. Source-to-outbox handoff is async, so crash loss before enqueue is
  possible. Recipient dedup is required for at-least-once delivery. No player,
  phase/REM/apnea or audible/visible-output claim.
- Own strict TRACKING_REQUESTv1 entrypoint, separate default-off consent and
  unlocked/focused user review. START stages existing setup/final Start. Other
  commands pin current run/phase/admission and consume one-use10s service grants.
  Durable prepared request IDs prevent blind repeat/process replay; primitive
  results distinguish staged/observed applied from pending/interrupted/rejected.
  No nested forwarding, unattended capture or alarm dismissal.
- Source CSV charts page every original64 positions, preserving original relative
  actigraphy/noise, zeros, missing/negative sentinels and exact raw inspection.
  Literal clocks require established column association; otherwise source order
  is explicit. Original aggregate/fractional/event fields remain separate from
  app measurements. Exact original2/3-row SAF export rechecks record/source.
- Separate future-night calendar consent queues only successfully completed new
  production recordings started after Save. Fixed title/start/end/zone only,
  without notes/audio. Bounded private queue, deferrable persisted jobs and shared
  direct-export receipts prevent blind uncertain retries. Off/consent/history
  changes fence pending ownership; in-flight provider calls may finish. No
  external updates/deletion, old-history backfill or remote-sync claim.

Commits ec2af49 (CSV signals),b38b88d (events),545b54a (incoming),86d0d1e
(calendar/entries/service guards/lifecycle). Production `:app:assembleDebug`
**PASS1m14s**, original signer, schema17/backup13 unchanged. New paths have
build-only evidence: no tests/lint/screenshots, request/provider/recipient/native
or personal-phone/output actions. Runtime/OEM/permission/background/recovery,
graph/visual and overnight acceptance remain unverified. Full parity is not done.

APK215360032bytes, SHA256
`0941b8bc0e98daccf97d4cff9b5685d8a2d70f320c51d9d8d0e8403bf55dab8f`.
Candidate remains uninstalled. Phone source121dbd5 and its preserved data/Device
Care suspension/schema7first-open-to16 evidence are unchanged.

[Development prerelease](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-features-20261026-86d0d1e)
is published. GitHub confirms the exact source target, draftfalse/prereleasetrue
and both assets uploaded. APK size/SHA256 match the frozen local artifact.

## 26 October — source a8f5f23 challenge/light batch

Production **assembleDebug PASS1m54s**; schema17, metadata13 and ZIPv2 unchanged,
original signer retained. New random configured pools/count/difficulty have
occurrence-atomic concrete selection and snooze source mapping; missing eligible
entries become real Math alternatives. Fixed microphone routines gain experimental
Laughter and relative Loud sound, with explicit previews and no saved recording.
Requested installed offline English narration identity is saved separately. Direct
Hue HTTPS/v1 adds explicit physical-button pairing, independently verified identity/
certificate pin, chosen dimmable-light checks and optional real-alarm sunrise with
conditional restoration; Home Assistant stays a separate exclusive path.

These are build-only paths. No new tests/lint/screenshots, microphone, TTS/provider,
bridge or physical-output checks. Prior failed laughter positive evidence remains;
no reliable instrument/laughter recognition, calibrated dB, v2-only Hue, all-firmware
or guaranteed restore/engine-package binding claim. Runtime/visual acceptance and
full parity remain unfinished. See [feature limits](CHALLENGE-AND-LIGHT-FEATURES.md).

The earlier **86d0d1e** update was subsequently installed at the user's request.
Its exact APK checksum, all original records/preferences/private files were verified;
Device Care suspension remains and first-open schema7→17 migration is pending.
The new **a8f5f23** candidate is uninstalled.

Published [a8f5f23 development APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-features-20261026-a8f5f23), exact target a8f5f23d04c4b880d538b742ac1b0a3f0d48653d.
Both assets are uploaded; APK size 215,504,432 bytes and remote digest match the
preserved local artifact. SHA-256:
`7226ca3307e8ca3cbb72ac8031913d126965493335564fed2584e7dcb898fb8d`.

## 26 October — source 8094497 expanded pools and CSV annotations

Production **assembleDebug PASS2m3s**, original signer, schema17/metadata13/ZIPv2
unchanged. Random pools now accept all 28 existing task categories, including exact
configured picture/object/code references and real per-task settings. Read-only
camera/microphone/call/capture/SDK prerequisites exclude unavailable entries; Math
fills the requested count. Model/offline-English readiness is still checked only
at explicit child Start/Listen, using the existing actual private previews.

A separate original CSV annotation timeline displays complete unambiguous in-period
LIGHT/DEEP/REM/PAUSED/BROKEN pairs and ALARM_* points, with independent overlapping
lanes, full/six-hour view and exact raw-field/millisecond inspection. Event units
are explicitly inferred from the vendor public example, not guaranteed by its
CSV specification. No guessed endpoints, gap interpolation, local-stage/awake
projection, record mutation or additional export authority is introduced.

The current checklists have been consolidated; stale phone builds and the false
Jumping-missing assertion were corrected. The original 36-screen goal and actual
functional/visual failures remain open. Sleep Phaser remains externally gated by
its unpublished Bluetooth interface; the vendor's USB table describes other devices.
No contact or fake substitute client was added.

Frozen APK size **215,530,780 bytes**; local SHA-256:
`1cfe6c86ad70b2b28ead150371b420abd086aae98770b29056ef106766321dc1`.
No new tests/lint/screenshots, device/provider/recognizer/microphone/output actions
ran for this candidate. Runtime/format/accuracy/recovery/visual acceptance remains
unverified. Installed phone remains 86d0d1e with data preserved, Device Care suspended
and schema7→17 first-open migration pending. This candidate is uninstalled.
See [annotation interpretation](CSV-EVENT-ANNOTATIONS.md) and
[challenge boundaries](CHALLENGE-AND-LIGHT-FEATURES.md).

Published [8094497 development APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-features-20261026-8094497). Both assets are uploaded;
exact source target, remote APK size and SHA-256 digest match the frozen local build.
The first GitHub upload timed out and its failed draft was removed; retry used
the same frozen APK, with no rebuild or phone action.
