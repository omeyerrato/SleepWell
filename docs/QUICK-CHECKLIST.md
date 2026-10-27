# SleepWell quick checklist

Checked means implemented in source/build, not fully verified on the phone.
Full supporting evidence stays in FEATURE-CHECKLIST.md and FEATURE-MATRIX.md.

## Current priorities requested by the user

- [x] Shared navigation/control haptics and pressed/selected feedback, with a saved off switch; physical feel remains unchecked.
- [x] First Settings/alarm-menu organization pass: eight groups, search and parent-aware Back; every feature retained.

Latest published **c6f2fcaa** adds real per-alarm smart-wake overrides, the configured dawn window, simpler Travel menus and restored help dialogs. assembleDebug passed in 18s; original signer, exact Git tag and both uploaded hashes verified. [Release/evidence](verification/2026-10-27-smart-wake-travel/README.md).

Organization 26.671s/13 inspected captures; Travel plan 43.569s and cue flow 37.968s/31 inspected captures. Each proves its named actions and persistence. Sensor early-wake runtime/accuracy and physical/overnight acceptance remain open. The phone is still2cbfe043.

This includes the prior compact Report/chart, organized sound/tracking/mixer menus, recording-analysis and alarm Schedule & dates hierarchy. [Prior release scope](verification/2026-10-27-report-menu-release/README.md). All36 designs and the nine unfinished items below remain open; these bounded passes do not check them off.

Unreleased **a8842ada** adds optional guarded API scalars for smart-wake window, sensitivity and recorded-duration goal. Production build **PASS19s** and original signer verified; default13 columns and existing grant/revision/ownership guards remain. No external-caller verification or phone action ran. These fields are outside publishedc6f2fcaa.

## Implemented

Unreleased source milestones: per-alarm settings search and first-viewport menu
clearance pass the existing Android journey (34.969s/14 inspected captures);
actual challenge drag ordering passes40.237s/6 inspected captures, including
cancel, tap fallback and Save/reopen. [Challenge evidence](verification/2026-10-27-challenge-ordering/README.md).
A post-dialog status-bar contrast issue and AAC export/import timing correction
are still being verified. These are not yet in the publishedc6f2fcaa APK or on
the phone; none completes the nine broad unfinished items below.

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
