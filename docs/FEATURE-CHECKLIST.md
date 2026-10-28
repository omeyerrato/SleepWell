# SleepWell feature checklist — 27 October 2026

Latest published **0.1.4 / 08d3911e** adds direct alarm-time controls and consistent feedback, background phone-chart loading, Room search, silent Travel reminders and stale-Math/notification cleanup repairs. [APK and checksum](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.4-alarm-travel-20261028-08d3911e); [exact bounded evidence](verification/2026-10-28-014-final-build/README.md). Phone installation awaits USB; the last verified installation is170b0e44, with original user data preserved at that update.

Condensed version: [QUICK-CHECKLIST.md](QUICK-CHECKLIST.md).
Checked means implemented source/build, not full phone/overnight acceptance.
Feature baseline **170b0e44** includes real backup tone/volume, corrected rails,
Gentle's quiet sample, clearer Tone/QR/Nap/rhythm layouts and the scoped
Health/Privacy/document/Cloud repairs. Original-signer arm64 build passed in
1m22s; remote tag and both uploaded hashes are verified. Selected native flows
exercise actions, cancellation, recreation and persistence. [Exact release scope](verification/2026-10-27-reference-menus-build/README.md).

The phone now has **170b0e44**, installed at the user’s request with original records, user settings and user files preserved. No physical output test ran. All36 reference states and the broader interaction/security review remain open.

## Current top priorities

- [ ] Match all36 reference screens. Current Report, Tone/Gentle, Snooze, QR, sleep plan, rhythm/Nap, Settings root and Appearance have bounded comparison/action evidence. Remaining screens, variants and full reference fidelity remain open. [Screen-by-screen acceptance](UI-REFERENCE-ACCEPTANCE.md).
- [x] Complete Report calendar/edit verification. Current selected Report method passes55.975s/11 inspected originals, including real calendar/day changes, notes/tags/awake edits, Save/reopen and Back-discard. [Evidence](verification/2026-10-27-report-proportions/README.md).
- [ ] Remaining interaction bugs and security review. Concrete shortcut/touch/calendar/QR/NFC/Health/Privacy/document/Cloud fixes are merged; their selected native scopes pass. Readiness denial/rationale/permanent-denial Settings recovery now passes its real Android journey (35.776s/13 inspected originals). Contactless and Snoring keep their bounded passes. Automatic recovery, clear live Tracking options, Room/Motion and Wind-down/Guard now pass their named native action/recreation/persistence journeys; actual label taps also pass. The full-night sonar timeline, shared Math/wake-check UI and calendar-consent draft recovery now have bounded native evidence in published0.1.3; real alarm admission/provider/overnight behavior remains unverified. Broader interaction, provider, hostile-overlay and full security acceptance remain open.

The installed phone is170b0e44; its checksum and data preservation are verified. The0.1.4 update is published and awaiting USB reconnection.


## New priorities

- [x] Shared navigation/control haptics and pressed/selected feedback, with a saved off switch; physical feel remains unchecked.
- [x] First Settings/alarm-menu organization pass: eight groups, search and parent-aware Back; every feature retained.

This bounded increment passed one silent route/persistence flow and ten captured-screen comparisons. It does not establish whole-app or physical haptic acceptance.
See [organization map](UI-ORGANIZATION.md).

## Implemented feature paths

- [x] Best-effort prevent-leave and recognized power-menu handling during alarms.
- [x] Optional removable Android-admin step before ordinary uninstall, with no admin policies.
- [x] Extra-loud toggle: maximum alarm stream plus optional owned-session gain.
- [x] Gentle volume increase, sound delay and independent vibration timing.
- [x] Screen sunrise and flashlight wake-up.
- [x] On-device snoring/speech estimates, triggered clips, playback, favorites,
  correction, retention and export.
- [x] Automatic tracking from schedules/phone stillness, recurring starts and
  optional Google Sleep confidence input.
- [x] Optional guarded rejection of likely false automatic starts.
- [x] Anti-snoring sound/vibration cues, with separate consent and output bounds.
- [x] Travel time-zone planning, dated sleep shifts, reminders and wake-alarm drafts.
- [x] Real recorded-history graphs/trends, record edits, tags/notes and goal bank.
- [x] Per-alarm detail settings and custom time/duration selectors.
- [x] Weekday, odd/even ISO-week and anchored day-interval repeats, skips,
  excluded dates and sunrise schedules.
- [x] Reviewed one-occurrence rescheduling, Skip/Undo and ordinary recurrence restoration.
- [x] Optional per-alarm orientation lock with saved input settings.
- [x] Configurable challenge routines, previews/fallbacks, QR/barcode/NFC,
  text/memory/Math/walking and additional motion/camera/object tasks.
- [x] Jumping: original touch-controlled hurdle game, saved progress and Math
  alternative; this is not physical-jump sensing.
- [x] Separate explicitly started on-device spoken-phrase challenge and Math fallback.
- [x] Generated morning QR codes, PNG/PDF export and Android printing.
- [x] Naps, Power nap, weekly nap profiles and exact-second timers.
- [x] Bedtime reminders, app guard and temporary pause.
- [x] Battery-aware movement/light sampling and separate low-battery reserve stop.
- [x] Optional manual wake-up sensor FIFO batching on supported hardware.
- [x] Snooze-aware home widgets and screen-only shortcuts.
- [x] Local ambient mixer, imported audio, timed fade, optional narration/binaural tools.
- [x] Saved local-document playlists with ordered track completion, pause/stop and one timer.
- [x] Optional tracking-owned fade after sustained measured phone stillness.
- [x] Saved built-in sound rotation on5/10/15-minute playing-time intervals.
- [x] Separate opted-in external MEDIA-stream stillness fade with cancellation and no restoration.
- [x] Saved narration speed/pitch presets applied after explicit Play.
- [x] Metadata/audio backup and restore, including optional user-selected folder versions.
- [x] Portable legacy clip stars without sound events, including missing-audio metadata round trips.
- [x] Selected effective audio intervals/collections export as AAC-LC M4A or multi-part ZIP.
- [x] Reviewed direct saved-record insertion into a selected writable calendar, with receipts.
- [x] Integration paths for sleep-only Health Connect, calendar, smart lights,
  Tasker/webhooks/MQTT, subject to their external services/permissions.
- [x] Published development APK; original signer and uploaded checksum verified.
- [x] Reviewed Sleep as Android CSV append with portable original source evidence.
- [x] Separate settings-profile export/review/Apply and interrupted-save recovery.
- [x] Optional Tonight row order/visibility and illustration controls, with default layout kept.
- [x] Fourteen automation kinds, including acknowledged sleep-player and anti-snore sound plus saved recognition estimates; actual new-source runtime behavior unverified.
- [x] Explicit package/signer-controlled completed-summary provider with allow/revoke/self-preview and history-replacement invalidation; client behavior unverified.
- [x] Reviewed version1 incoming tracking requests; START stages normal setup.
- [x] Actual imported CSV source charts/raw values and exact original single-record export.
- [x] Imported explicit event-pair timeline, six-hour view and exact timestamp inspection.
- [x] Separate opted-in calendar export for future successful recordings, with shared receipts.
- [x] Saved occurrence-frozen random pools for all 28 existing task categories,
  real enrolled targets, per-entry settings, prerequisites and Math alternatives.
- [x] Explicit experimental Laughter and relative Loud sound task/preview paths; accuracy unverified.
- [x] Requested installed offline English narration voice selection, with no synthesis on Save.
- [x] Explicitly paired direct Hue HTTPS/v1 sunrise path; hardware/TLS compatibility unverified.
- [x] Saved movement-night analysis request, actual processed-window counts and failure outcomes.
- [x] Report inline retained event playback with measured waveforms and lifecycle cleanup.
- [x] Historical analysis empty/error/legacy-unknown states across live tracking and reports.
- [x] Parent microphone tasks wait for confirmed capture cleanup before switching to Math.
- [x] User-authorized 2cbfe043 phone update; installed checksum, schema17 rows,
  preferences and original private files preserved.

## Still missing or narrower than full parity

- [ ] Implement phone-input sleep-stage/REM estimates with an appropriate licensed model and input contract; breathing/apnea analysis remains separate. See PHONE-STAGE-FEASIBILITY.md.
- [ ] Reliable laughter/instrument recognition: experimental laughter and relative amplitude paths
  now exist, but previous failed positive evidence and actual accuracy remain unresolved.
- [ ] Dedicated Sleep Phaser Bluetooth protocol: vendor-provided specification is required.
- [ ] Direct vendor SDK clients: Google Fit sign-ups are closed/deprecated; Samsung
  public SDK distribution requires partner approval. Health Connect is the existing route.
- [ ] Hosted SleepCloud/research-community/Spotify account-service parity.
- [ ] Established chronotype, jet-lag-prevention or lucid-dream efficacy.

## Acceptance and deferred work

- [ ] Physical alarm delivery, leave/power-menu handling, extra loud, volume restore,
  ramp, sunrise, flashlight, snooze and challenge behavior on the actual phone.
- [ ] Overnight automatic tracking, noise accuracy, battery and anti-snoring behavior.
- [ ] New repeat/DST/calendar behavior, false-start data cleanup, printing, widget
  hosting, one-occurrence overrides, FIFO/Doze capture and real provider exchange.
- [ ] On-device phrase recognition, microphone cleanup and orientation behavior.
- [ ] Local playlist provider/decoder lifecycle and stillness-fade sensor/output behavior.
- [ ] Built-in rotation, external media volume/route cancellation, requested narration voices,
  random-routine occurrence/snooze handling and microphone cleanup/accuracy.
- [ ] Direct Hue TLS/firmware/network exchange, physical ramp/restore and restart ownership.
- [ ] Schema16 favorite and schema17 CSV-evidence migration/round trips, AAC encoder/provider output, direct calendar
  insertion/uncertain receipts and Android-admin activation/removal behavior.
- [ ] CSV/profile document exchange, duplicate/conflict/cancel/restore and multi-file recovery.
- [ ] Tonight layout persistence, real optional data links and large-text presentation.
- [ ] Incoming request/replay/service ownership and outbound event/recipient lifecycle behavior.
- [ ] CSV source graphs/event-pair inference/inspection/export and future-night calendar job/uncertainty handling.
- [ ] Full36-screen reference design fidelity, especially Report/charts and menus.
- [ ] Remaining interaction bugs and security review; now a current priority.
- [x] Authorized 2cbfe043 phone installation and data-preservation comparison.
- [ ] Phone runtime/navigation, Device Care state and actual migration-path acceptance.
- [ ] Motion analysis count/checkpoint/recovery/backup and inline Report playback lifecycle.
- [ ] Full completion verification; current builds alone do not establish it.

Forced hardware power-off cannot be prevented by an ordinary Android app. New
runtime behavior is unverified. Historical full-app native failures and independent
laughter-positive failures remain unresolved; narrower historical passes do not
supersede them or validate the current candidate. No new tests/native/lint/
screenshot batches or phone/provider/codec/admin/alarm/audio/sensor runtime actions ran for
714fc4cb. Its subsequent authorized installation is recorded separately above. This checklist does not authorize such actions or reinstate deferred work.

The consolidated actionable/deferred scope is in [REMAINING-WORK.md](REMAINING-WORK.md).
Source/subfeature detail remains in the root-maintained [FEATURE-MATRIX.md](FEATURE-MATRIX.md);
historical delivery chronology belongs in [the delivery note](2026-10-25-FEATURE-DELIVERY.md),
not a competing current phone-status assertion. See also
[the latest challenge/narration/Hue boundaries](CHALLENGE-AND-LIGHT-FEATURES.md).
