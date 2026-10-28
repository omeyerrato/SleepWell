# SleepWell quick checklist

Latest published: **0.1.3 / d4415a2b** — [APK and checksum](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.3-night-challenges-20261028-d4415a2b), [build and verification limits](verification/2026-10-28-013-build/README.md).

Last verified phone installation: **170b0e44**, with original records, settings and files preserved. USB currently does not expose the phone; installing 0.1.3 remains pending. No personal-phone alarm, sound, haptic or flashlight test ran.

Checked means implemented with the stated supporting evidence. It does not mean every screen, device or physical behavior is verified. [Full feature matrix](FEATURE-MATRIX.md), [172 detailed entries](feature-matrix.json), [36-screen acceptance](UI-REFERENCE-ACCEPTANCE.md).

## Current priorities

- [ ] Match all 36 reference screens, including remaining alarm screens, charts, menus, icons and navigation states.
- [x] Complete Report calendar/edit verification: actual date selection, editing, Save/reopen and Back-discard; 11 inspected originals. [Evidence](verification/2026-10-27-report-proportions/README.md).
- [ ] Complete remaining interaction fixes and security review. Named recovery, touch, settings, document and consent journeys pass; broader security/provider behavior remains open.

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
- [x] Backup/export/restore and integration implementations; external prerequisites remain documented.
- [x] Shared navigation/control feedback with a saved off switch; new icon and firmer tap preset in 0.1.3. Physical feel remains unchecked.
- [x] Settings/alarm-menu organization and search; every feature retained.
- [x] Free, open-source development APK published.

## Published in 0.1.3

- [x] Full-night live sonar chart with explicit pause/missing gaps and true zero values. [Generated-input/private-store proof](verification/2026-10-28-live-night-settled/README.md).
- [x] Revised wake-check and shared Math card. [Silent private action/recreation/persistence proof](verification/2026-10-28-wake-challenge-reference/README.md); actual alarm execution remains unverified.
- [x] Calendar-consent draft recovery and stale-revision protection. [Fake-access/private-state proof](verification/2026-10-28-calendar-consent-recovery/README.md); calendar-provider delivery remains unverified.
- [x] Room conditions, saved window dimming and portable Appearance profiles. [Private profile JSON/preferences/journal proof](verification/2026-10-28-profile-room-dimming/README.md).

## Integrated after 0.1.3 — not yet packaged

- [x] Clearer Travel main hierarchy and supporting routes; 34 inspected originals and saved-state actions pass. [Evidence](verification/2026-10-28-travel-reference/README.md).
- [x] Room/dimming search route and unsaved-draft Back/recreation; five inspected originals pass. [Evidence](verification/2026-10-28-room-settings-search/README.md).
- [x] Bounded background loading for live phone-motion history with selection/focus preserved. [Eight-original reader/chart proof](verification/2026-10-28-phone-motion-chart/README.md).
- [ ] Finish actual TrackingActivity lifecycle verification for that chart change.
- [ ] Finish the independent silent Travel reminder flow and verification.
- [ ] Package and publish the next combined update.

## Still unfinished

- [ ] Match all reference states and finish interaction/security review above.
- [ ] Reliable additional sound recognition, including laughter/instruments.
- [ ] Working phone-input sleep-stage/REM and breathing/apnea analysis.
- [ ] Remaining ambient-playlist/provider-specific functionality and verification.
- [ ] Optional vendor-service/hardware parity listed in the full feature matrix.
- [ ] Verify real alarm protection, loudness, ramp, sunrise, flashlight and challenges when output testing is authorized.
- [ ] Verify overnight tracking, sound accuracy, anti-snoring, battery behavior and external integrations.
- [ ] Install the latest published update while preserving phone data.
