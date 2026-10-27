# SleepWell quick checklist

Checked means implemented in source/build, not fully verified on the phone.
Full supporting evidence stays in FEATURE-CHECKLIST.md and FEATURE-MATRIX.md.

## Current priorities requested by the user

- [x] Shared navigation/control haptics and pressed/selected feedback, with a saved off switch; physical feel remains unchecked.
- [x] First Settings/alarm-menu organization pass: eight groups, search and parent-aware Back; every feature retained.

Latest published and installed **32f064e2** adds Tonight arc/feedback/help
restoration and the organized challenge routine. arm64 APK166,649,432bytes,
original signer, build PASS11m19s; exact tag and both uploaded hashes verified.
[Release/evidence](verification/2026-10-27-tonight-report-routine/README.md).

Tonight45.786s/9 and challenge48.33s/6 inspected captures pass their named
actions/restoration/persistence. **New Report calendar verification failed and
remains incomplete.** These passes do not accept all36 designs or physical outputs.
Phone update installed with original database rows, preferences and user files
preserved; no app launch or physical feature/output test ran.

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
- [x] Install32f064e2 while preserving original records, preferences and user files.

Installed phone source:32f064e2. No further phone or output action is authorized by this document.
Priority implementation/verification scope is tracked in UI-ORGANIZATION.md.
