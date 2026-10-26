# SleepWell quick checklist

Checked means implemented in source/build, not fully verified on the phone.
Full supporting evidence stays in FEATURE-CHECKLIST.md and FEATURE-MATRIX.md.

## Current priorities requested by the user

- [x] Shared navigation/control haptics and pressed/selected feedback, with a saved off switch; physical feel remains unchecked.
- [x] First Settings/alarm-menu organization pass: eight groups, search and parent-aware Back; every feature retained.

Latest built source **25ed7a9d** adds grouped sound/tracking/mixer menus and direct detail search (production build32s; Android acceptance pending). It also retains the prior source731e9725 feature increment, which adds saved radio stations, simpler Tracking setup, measured echo-rhythm review and experimental recorded-audio estimates. These new Android flows still need acceptance; the check marks below do not imply full parity.

Latest unreleased work: compact Report editor, named tag/awake detail sheets,
themed editor option/rating pickers, numerical chart inspection and explicit
unmeasured-gap selection are committed. Their combined app/UI-check build passed
in1m12s at source28e68f13. Two focused silent Android journeys are running at
CI40387065340/40387070994; their screenshots have not yet been inspected.
These changes do not close the Report/design checklist item.
The sleep-stage research evidence is now recorded publicly; no useful stage model
was accepted or shipped. Latest published APK25ed7a9d remains separate from this work.

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
