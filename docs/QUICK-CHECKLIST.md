# SleepWell current checklist

Updated 30 October 2026. Latest published APK: **0.1.13 / f11cc1cf** (usability development build; new verification deferred). Last verified phone installation: **0.1.4 / 08d3911e**. The phone update is deferred by the user.

[Published APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.13-report-bedtime-20261030-f11cc1cf) · [172-feature matrix](FEATURE-MATRIX.md) · [36-screen acceptance](UI-REFERENCE-ACCEPTANCE.md) · [Detailed history and evidence](FEATURE-CHECKLIST.md).

Checked means implemented with the stated supporting evidence. Physical-device and overnight verification remain separate.

## Current priorities

**Current phase: usability and reference layouts first.** Three parallel implementation agents have delivered the [0.1.9 source pass](USABILITY-PASS-2026-10-29.md). New verification, security review and a separate bug-fixing pass are deferred by the user. Source changes are not full36 visual acceptance.

- [ ] Match all36 reference screens, especially remaining charts, menus and visual states.
- [x] Report calendar/edit verification: actual date selection, Save/reopen and Back-discard. [Evidence](verification/2026-10-27-report-proportions/README.md).
- [ ] Finish remaining interaction bugs and security review after the usability phase.

## Implemented

- [x] Best-effort prevent-leave and recognized power-menu handling.
- [x] Extra-loud toggle and optional audio gain.
- [x] Gentle volume increase and independent sound/vibration delays.
- [x] Screen sunrise and flashlight wake-up.
- [x] Snoring/speech estimates, recordings, playback, favorites and retention.
- [x] Automatic schedule/stillness tracking, recurring starts and false-start rejection.
- [x] Anti-snoring sound/vibration cues.
- [x] Travel planning, dated sleep shifts, reminders and alarm drafts.
- [x] Per-alarm settings and custom time/duration selectors.
- [x] Advanced repeats, skips and sunrise schedules.
- [x] Challenge routines, fallbacks and printable QR codes.
- [x] Naps, Power nap, weekly nap schedules and exact-second timers.
- [x] Real-data history, trends, editing, notes and goal bank.
- [x] Bedtime guard, reminders and temporary pause.
- [x] Battery-aware sampling and low-battery stopping.
- [x] Snooze-aware widgets.
- [x] Backup/export/restore and integration code; external prerequisites remain.
- [x] Haptic feedback with a saved Off switch, generated raster icons and the new flat launcher. Physical feel remains unchecked.
- [x] Settings organization and search, genuine task-configuration rows, compact automation cards and clearer protection setup.
- [x] Free, open-source development APK published.

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

## Published in 0.1.8 — phone update deferred

- [x] Integrate Motion sensor reference artwork, clearer real-data chart labels and complete calibration/footer layout. [Seven inspected originals](verification/2026-10-29-motion-reference/README.md); fake sensor ports, not hardware acceptance.
- [x] Reject unsupported external media fade before allocating its newer native listener. Included in the passing build/lint; older-Android runtime remains unverified.
- [x] Integrate the Privacy storage/backup composition and real loading/error/favorites persistence flow. [12 inspected originals](verification/2026-10-29-privacy-reference/README.md); audio-only bulk deletion and a persistent last-export receipt remain missing.
- [x] Build and lint next0.1.8 at `0b3cb9ed`: PASS11m43s.
- [x] Independent package/signature audit and publication passed; exact tag and both uploaded hashes verified. [Evidence](verification/2026-10-29-018-build/README.md).

## Published in 0.1.9 — usability first

These checks record implemented source and packaging, not new visual or runtime acceptance. [Full change scope](USABILITY-PASS-2026-10-29.md).

- [x] Direct Settings routes, named option groups and clearer tracking controls.
- [x] Night / History / Trends navigation, chart selection labels and sleep-goal presentation.
- [x] Compact alarm editor, task-category filters, labelled Options and direct Flashlight control.
- [x] Clearer bedtime app selection, Nap options, Trip plan, audio/event and integration menus.
- [x] Integrate recording-only cleanup and manual export-receipt source; combined acceptance remains open.
- [x] Produce and publish0.1.9 from `4e47ab35`; APK packaging3m47s. Tests, lint, new screenshots, security and device checks were not run.

## Published in 0.1.10 — clearer controls

- [x] Distinguish command menus from saved-value selections and group record-editor tools.
- [x] Add common settings shortcuts while retaining the alarm chooser for per-alarm choices.
- [x] Use clearer task setup/confirmation labels and warm app-owned confirmations.
- [x] Produce and publish0.1.10 from `44cb41e7`; APK packaging3m15s. New verification and physical-device actions remain deferred. [Changes](USABILITY-PASS-2026-10-29.md#command-and-discovery-follow-up).

Appearance theme rings (`6d56b05b`) are included in0.1.11.

## Published in 0.1.11 — pickers and feedback

- [x] Duration presets that edit fields before explicit confirmation; custom durations remain.
- [x] City-first time-zone selection with country search, current selection and progressive results.
- [x] Saved Light / Standard / Firm touch feedback, explicit Try feedback and preserved Off/quiet behavior.
- [x] App-owned Appearance rings and search labels/icons aligned with Settings.
- [x] Produce and publish0.1.11 (`ce2cb571`), APK packaging2m24s only. New interaction, visual and device acceptance remains deferred.

## Published in 0.1.12 — readiness control

- [x] Add explicit review/confirmation for the reference's quiet-alarm control, plus permission/busy/error/request states.
- [x] Give real alarm occurrences priority over the new quiet request; retain ordinary alarm overlap behavior.
- [x] Produce and publish0.1.12 (`031d44e5`), APK packaging1m3s. No alarm was executed and new verification remains deferred. The earlier6cfcc62e package was held and never published.

## Published in 0.1.13 — report and bedtime

- [x] Keep Report sound rows and the main action prominent; move routine details into About while retaining errors.
- [x] Add name/package search, selection counts and an empty state to the guard app picker.
- [x] Replace the blocking screen's misleading Start label with Open Wind-down, night styling, settings and saved-state information. Preserve Pause/Home actions.
- [x] Produce and publish0.1.13 (`f11cc1cf`), APK packaging1m55s only. New visual/action/device acceptance remains deferred.

## Still unfinished

- [ ] Reference/interaction/security priorities above.
- [ ] Finish acceptance of recording-only cleanup and the persistent Last export display. Source is integrated in0.1.9; the previous combined Privacy run failed and new verification is deferred.
- [ ] Initial Android system-bar contrast handoff: first light frame can retain white glyphs despite correct requested appearance. The attempted change failed and was rejected. [Actual diagnostic evidence](verification/2026-10-29-systemui-transitions/README.md).
- [ ] Reliable additional sound recognition, including laughter/instruments.
- [ ] Working phone-input sleep-stage/REM and breathing/apnea analysis.
- [ ] Remaining ambient-playlist/provider functionality and verification.
- [ ] Optional vendor-service/hardware parity listed in the full matrix.
- [ ] Verify real protection, loudness, ramp, sunrise, flashlight and challenges when output testing is authorized.
- [ ] Verify overnight tracking, sound accuracy, anti-snoring, battery behavior and external integrations.
- [ ] Install the newer update when the user is ready, preserving existing phone data.
