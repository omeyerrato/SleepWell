# SleepWell quick checklist

Checked means implemented in source/build, not fully verified on the phone.
Full supporting evidence stays in FEATURE-CHECKLIST.md and FEATURE-MATRIX.md.

## Current priorities requested by the user

- [x] Shared navigation/control haptics and pressed/selected feedback, with a saved off switch; physical feel remains unchecked.
- [x] First Settings/alarm-menu organization pass: eight groups, search and parent-aware Back; every feature retained.

Latest published **5266e54b**: compact Report editor, numerical/gap chart
inspection, grouped tracking/sound/mixer choices, simpler recording analysis and
switch overlap correction. Production build27s; original signer and uploaded
APK/checksum verified. [Release/evidence](verification/2026-10-27-report-menu-release/README.md).

Bounded Android passes: Report43.351s/9inspected captures; organized menus34.25s/11;
real recorded CPU analysis/save/reopen/delete13.826s/3. Each proves only its named
actions and persistence. Reliable additional recognition, all36screens and
physical/overnight acceptance remain open. The phone is still at2cbfe043.

Unreleased **69dbbca3** moves Repeat rule into Schedule & dates, keeping custom
or invalid repeat status visible and restoring room for the main Snooze row.
Production build21s/original signer verified; focused organization CI40407366922
is running. Its actual captures and persistence result are not accepted yet.

## Implemented

- [x] Best-effort prevent-leave and recognized power-menu handling.
- [x] Extra-loud toggle and optional audio gain.
- [x] Gentle volume increase and independent sound/vibration delays.
- [x] Screen sunrise wake-up.
- [x] Flashlight wake-up.
- [x] Snoring/speech analysis, recordings, playback, favorites and retention.
- [x] Automatic schedule/stillness tracking, recurring starts and optional false-start rejection.
- [x] Anti-snoring sound/vibration cues.
- [x] Travel planning, dated sleep shifts, reminders and wake-alarm drafts.
- [x] Per-alarm settings and custom time/duration selectors.
- [x] Weekday, odd/even-week and every-N-day repeats, skips and sunrise schedules.
- [x] Challenge routines, fallbacks and generated printable QR codes.
- [x] Naps, Power nap, weekly nap schedules and exact-second timers.
- [x] Real-data history, trends, record editing, notes and goal bank.
- [x] Bedtime guard, reminders and temporary pause.
- [x] Battery-aware sampling and low-battery reserve stopping.
- [x] Snooze-aware widgets.
- [x] Backup/export/restore and integration implementations.
- [x] Free, open-source development APK published.

## Still unfinished

- [ ] Match all36 reference screens, especially Report, charts, menus, icons and navigation.
- [ ] Remaining interaction bugs and security review.
- [ ] Reliable additional sound recognition, including laughter/instruments.
- [ ] Working phone-input sleep-stage/REM and breathing/apnea analysis.
- [ ] Remaining ambient-playlist/provider-specific functionality and verification.
- [ ] Optional vendor-service/hardware parity in the full checklist.
- [ ] Verify real alarm protection, loudness, ramp, sunrise, flashlight and challenges.
- [ ] Verify overnight tracking, sound accuracy, anti-snoring, battery behavior and integrations.
- [ ] Install the newest built update while preserving user data.

Installed phone source:2cbfe043. No further phone or output action is authorized by this document.
Priority implementation/verification scope is tracked in UI-ORGANIZATION.md.
