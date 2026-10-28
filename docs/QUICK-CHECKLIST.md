# SleepWell quick checklist

Latest published: **0.1.4 / 08d3911e** — [APK and checksum](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.4-alarm-travel-20261028-08d3911e), [build and verification limits](verification/2026-10-28-014-final-build/README.md).

Last verified phone installation: **170b0e44**, with original records, settings and files preserved. USB currently does not expose the phone; installing 0.1.4 remains pending. No personal-phone alarm, sound, haptic or flashlight test ran.

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

## Published 0.1.4 — phone installation pending

Frozen source **08d3911e**, version **0.1.4 / code5**: Root build succeeded in **1m19s**. APK SHA256 `3b6c698fbfc1d71a21fa2e9b5420b7cf08ad39632608648e226482d11709a024`; original signer and retained assets/libraries/resources independently checked. [Final package/source audit](verification/2026-10-28-014-final-build/README.md). The source tag and both uploaded asset hashes/sizes are verified. Phone installation remains pending.

- [x] Travel hierarchy, child restoration, Save/reopen and real default-OFF silent reminder preference/runtime. [Composite 37 originals](verification/2026-10-28-travel-plan-native/README.md): Plan 41.859s / 11 plus Cue 54.505s / 26 are separate passing scopes from two failed full jobs, not one green37-frame run. Private/fake ports only; manual flashlight arming stays separate.
- [x] Actual TrackingActivity chart lifecycle: ticks, selection/focus, child return, Pause/Resume, recreation and session replacement. [29.722s/14 originals](verification/2026-10-28-phone-motion-activity/README.md); private seeded history and fake commands, no real sensor/service or overnight claim.
- [x] Room/dimming search route and unsaved-draft Back/recreation. [12.917s/5 originals](verification/2026-10-28-room-settings-search/README.md); fake unavailable access/sensor/audio ports.
- [x] Direct draft hour/minute controls and compact alarm-list feedback, Save/reopen and larger app text. [41.023s/18 originals](verification/2026-10-28-alarm-clock-interactions/README.md); no alarm scheduling/output. New smart-window footer wording is source-only and uncaptured.
- [x] Current/stale Math answer ownership guards in shared real widgets. [51.773s/14 originals](verification/2026-10-28-stale-math-owner/README.md); explicit private Practice context, not actual AlarmActivity admission/central advance or physical challenge execution.
- [x] Unconditional owned Travel-notification cleanup: [39.92s/18 inspected originals](verification/2026-10-28-travel-reminder-cleanup/README.md), including cold-state/error/retry and private preference restoration. This models leftover fake-port state, not a real Android restart/controller.
- [x] Publish0.1.4 with the original signer, exact source tag and both server asset hashes verified.
- [ ] Install0.1.4 when USB exposes the phone, preserving existing data.

## Still unfinished

- [ ] Match all reference states and finish interaction/security review above.
- [ ] Reliable additional sound recognition, including laughter/instruments.
- [ ] Working phone-input sleep-stage/REM and breathing/apnea analysis.
- [ ] Remaining ambient-playlist/provider-specific functionality and verification.
- [ ] Optional vendor-service/hardware parity listed in the full feature matrix.
- [ ] Verify real alarm protection, loudness, ramp, sunrise, flashlight and challenges when output testing is authorized.
- [ ] Verify overnight tracking, sound accuracy, anti-snoring, battery behavior and external integrations.
- [ ] Install the latest published update while preserving phone data.
