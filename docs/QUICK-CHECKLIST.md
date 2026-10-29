# SleepWell current checklist

Updated 29 October 2026. Latest published APK: **0.1.7 / 02aacc6b**. Last verified phone installation: **0.1.4 / 08d3911e**. The phone update is deferred by the user.

[Published APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.7-routine-preview-20261029-02aacc6b) · [172-feature matrix](FEATURE-MATRIX.md) · [36-screen acceptance](UI-REFERENCE-ACCEPTANCE.md) · [Detailed history and evidence](FEATURE-CHECKLIST.md).

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

## Next increment — not yet published

- [x] Integrate Motion sensor reference artwork, clearer real-data chart labels and complete calibration/footer layout. [Seven inspected originals](verification/2026-10-29-motion-reference/README.md); fake sensor ports, not hardware acceptance.
- [x] Reject unsupported external media fade before allocating its newer native listener. Source-only; next combined build pending.
- [x] Integrate the Privacy storage/backup composition and real loading/error/favorites persistence flow. [12 inspected originals](verification/2026-10-29-privacy-reference/README.md); audio-only bulk deletion and a persistent last-export receipt remain missing.
- [x] Build and lint next0.1.8 at `0b3cb9ed`: PASS11m43s.
- [ ] Complete its independent package audit and publish.

## Still unfinished

- [ ] Reference/interaction/security priorities above.
- [ ] Audio-only bulk Delete recordings retaining sleep history, and a persistent manual Last export receipt. Both are next-increment work.
- [ ] Initial Android system-bar contrast handoff: first light frame can retain white glyphs despite correct requested appearance. The attempted change failed and was rejected. [Actual diagnostic evidence](verification/2026-10-29-systemui-transitions/README.md).
- [ ] Reliable additional sound recognition, including laughter/instruments.
- [ ] Working phone-input sleep-stage/REM and breathing/apnea analysis.
- [ ] Remaining ambient-playlist/provider functionality and verification.
- [ ] Optional vendor-service/hardware parity listed in the full matrix.
- [ ] Verify real protection, loudness, ramp, sunrise, flashlight and challenges when output testing is authorized.
- [ ] Verify overnight tracking, sound accuracy, anti-snoring, battery behavior and external integrations.
- [ ] Install the newer update when the user is ready, preserving existing phone data.
