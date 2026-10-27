# SleepWell quick checklist

Checked means implemented in source/build, not fully verified on the phone.
Full supporting evidence stays in FEATURE-CHECKLIST.md and FEATURE-MATRIX.md.

## Current top priorities

- [ ] Match all36 reference screens. Sound events, Report spacing and Sleep goals have passing current-source native screenshots/actions; Appearance fits its controls, saves all three themes and passes full large-text action reach. [Screen-by-screen acceptance](UI-REFERENCE-ACCEPTANCE.md).
- [x] Complete Report calendar/edit verification: source514a5800, CI40477887525,64.406s; all11 original screenshots inspected. Real date changes, selected-day chart, notes/tags/awake edits, Save/reopen and Back-discard pass. This is functional closure, not all36 design acceptance. [Evidence](verification/2026-10-27-report-calendar-complete/README.md).
- [ ] Remaining interaction bugs and security review. Cancelled shortcut navigation, obscured authorization gestures and calendar-permission recovery now pass their three-method native scope at795ab556 (CI40517817995). All23 remaining exported components have a documented static review; broader interaction/runtime acceptance remains open. [Issue status](INTERACTION-SECURITY-REVIEW.md).

The installed phone remains32f064e2; it predates the passing Report fix and these new source changes. No further phone action is included in this status update.


## Current priorities requested by the user

- [x] Shared navigation/control haptics and pressed/selected feedback, with a saved off switch; physical feel remains unchecked.
- [x] First Settings/alarm-menu organization pass: eight groups, search and parent-aware Back; every feature retained.

Latest published **795ab556** corrects Report, Sound events, Sleep plan options,
Appearance and the scoped interaction/security issues. The arm64 build passed in
4m19s; its remote tag, original signer and both uploaded hashes are verified.
Five focused native journeys pass seven methods with 47 individually inspected
Android captures. [Release and exact scope](verification/2026-10-27-design-interactions-build/README.md).

The phone remains **32f064e2**, with the original records, preferences and user
files preserved by its earlier update. No further phone action or physical
output test ran. Report calendar/edit verification now passes; all 36 designs
and the broader interaction/security review remain open.

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

## New source and focused verification

- [x] Report calendar/edit flow and subsequent spacing/proportion corrections: 11 original captures, actual actions and Save/reopen pass. [Current Report evidence](verification/2026-10-27-report-proportions/README.md).
- [x] Real backup sound/volume settings and format14/legacy13 compatibility pass the same native journey with fake outputs; 10 originals inspected. The later progress-rail rendering fix still needs its final capture. [Snooze evidence](verification/2026-10-27-snooze-backup/README.md).
- [x] Rhythm and Nap changed main layouts/actions pass, with 13 individually inspected originals. [Evidence](verification/2026-10-27-rhythm-nap/README.md).
- [ ] Final acceptance of the new Gentle sample/rail and Health/privacy repairs. CI40546119570 exposed an interruption-notice loss, a Health input-handling race and a real cleanup-observation race; each has a narrow source fix. Follow-up is restricted to those repairs.
- [ ] All36 reference screens and broad interaction/security acceptance remain open. The phone remains32f064e2; latest published795ab556 is separate from these newer source increments. No physical alarms or phone outputs ran.
