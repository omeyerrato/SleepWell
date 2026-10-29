# SleepWell current checklist

Updated29October2026. Latest published APK: **0.1.6 /741b3718**. Last verified phone installation: **0.1.4 /08d3911e**. Phone update is deferred by the user. The newer0.1.7 source below is not published or installed.

[Published APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.6-clearer-settings-20261029-741b3718) · [172-feature matrix](FEATURE-MATRIX.md) · [36-screen acceptance](UI-REFERENCE-ACCEPTANCE.md) · [Detailed history and evidence](FEATURE-CHECKLIST.md).

Checked means implemented with the stated supporting evidence. Physical-device and overnight verification remain separate.

## Current priorities

- [ ] Match all36 reference screens, especially remaining charts, menus and visual states.
- [x] Report calendar/edit verification: actual date selection, Save/reopen and Back-discard. [Evidence](verification/2026-10-27-report-proportions/README.md).
- [ ] Finish remaining interaction bugs and security review.

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

## Newer0.1.7 source — not published or installed

- [x] Sleep sounds header navigation and fixed Timer/Fade actions, preserving draft/scroll/focus and advanced routes. [14-original configuration proof](verification/2026-10-29-sleep-mixer-reference/README.md); no active-playback or physical-output claim.
- [x] Integrate17 routine-preview controllers with explicit capture Start, Cancel/Resume, retained progress and one-use child results. [58-original private/fake-port proof](verification/2026-10-29-routine-motion/README.md); this is not full28-type or actual-alarm/hardware acceptance.
- [x] Report means round consistently. The first18-frame flow passed functionally; its tooltip/goal-caption overlap is retained as a visible failure in the archive.
- [ ] Integrate the corrected Report recapture evidence. The correction compiles and the fresh18-frame run passes; all18 were inspected, with the full goal caption and selected value visible.
- [ ] Finish the remaining11 microphone/camera preview adapters. Five audio adapters are being implemented separately; six camera adapters remain.
- [ ] Verify and integrate the actual Walk completion-after-failed-cleanup correction. Its separate source compiles; the current17-type integration does not claim that fix.
- [ ] Complete the combined build/release and prepare the next phone update.

## Still unfinished

- [ ] Reference/interaction/security priorities above.
- [ ] Initial Android system-bar contrast handoff: first light frame can retain white glyphs despite correct requested appearance. The attempted change failed and was rejected. [Actual diagnostic evidence](verification/2026-10-29-systemui-transitions/README.md).
- [ ] Reliable additional sound recognition, including laughter/instruments.
- [ ] Working phone-input sleep-stage/REM and breathing/apnea analysis.
- [ ] Remaining ambient-playlist/provider functionality and verification.
- [ ] Optional vendor-service/hardware parity listed in the full matrix.
- [ ] Verify real protection, loudness, ramp, sunrise, flashlight and challenges when output testing is authorized.
- [ ] Verify overnight tracking, sound accuracy, anti-snoring, battery behavior and external integrations.
- [ ] Install the newer update when the user is ready, preserving existing phone data.
