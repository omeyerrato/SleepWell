# SleepWell current remaining work

Latest published **0.1.22 / 6087d47e**. [APK and checksum](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.22-portable-feedback-20261030-6087d47e). APK packaging only; new tests, lint, screenshot verification, security review and a separate bug pass are deferred by the user. The phone remains on0.1.4; installation and all phone/output operations are deferred.

Updated30October2026. The full goal remains phone-capable feature parity and all36 supplied references. Source changes and successful packaging do not prove that goal complete. [Compact checklist](QUICK-CHECKLIST.md) · [172-feature matrix](FEATURE-MATRIX.md) · [Source increments](USABILITY-PASS-2026-10-29.md).

## Remaining priorities

- [ ] Match all36 reference screens and their required states. Current source includes the0.1.9–0.1.22 usability passes; current visual/action acceptance remains open. [Screen acceptance](UI-REFERENCE-ACCEPTANCE.md).
- [ ] Reaccept the changed Report/calendar/edit surfaces when verification resumes. The27October calendar/day/edit Save/reopen/Back-discard result passed at its own source; it does not accept later presentation changes. [Historical evidence](verification/2026-10-27-report-proportions/README.md).
- [ ] Complete remaining interaction bugs and security review after this usability phase. Existing scoped results remain historical; broad acceptance is open.
- [ ] Establish reliable additional sound recognition, including laughter and instruments. Existing YAMNet-family laughter and recorded-audio supplementary estimates are experimental. Loud sound is relative microphone RMS; spoken phrase and musical-note tasks are separate features.
- [ ] Implement usable phone deep/REM estimation with a suitable redistributable model and exact input contract or original licensed training. Existing motion, sound classes and cues are not that algorithm. The rejected Nidra replay remains rejected. Breathing/apnea are separate unfinished work. [Feasibility](PHONE-STAGE-FEASIBILITY.md).
- [ ] Complete remaining provider/service parity where the required contracts and access exist. Dedicated Sleep Phaser Bluetooth requires its actual protocol and terms. Direct legacy Fit/Samsung SDK, hosted SleepCloud/research services and Spotify account access remain absent or externally gated; existing Health Connect, folder backups and HTTPS radio are distinct paths.
- [ ] Verify real alarm protection, loudness, ramp, sunrise, flashlight and all challenge types when device/output work is authorized.
- [ ] Verify overnight tracking, audio accuracy, anti-snoring behavior, battery behavior and actual external integrations. Travel/light/dream tools retain descriptive or experimental limits; efficacy is unestablished.
- [ ] Install the latest update while preserving phone data when the user requests installation. Last verified phone installation:0.1.4 /08d3911e. [Evidence](verification/2026-10-28-014-final-build/phone-installation.md).

## Implemented source with acceptance still open

- [x] All28 routine-preview controllers are integrated, including screen, motion/NFC, audio and camera paths. Existing synthetic/private-port evidence is scoped; actual hardware/model/alarm acceptance is open.
- [x] Recording-only cleanup and persistent manual JSON/CSV export receipts are integrated. The failed combined Privacy journey and its unmerged follow-up files remain preserved; no successful combined run is claimed.
- [x] Usability increments organize Settings, search, alarm/task menus, Report/history/trends, bedtime, tracking, audio, data and integration controls. Existing source/packaging milestones are listed in the compact checklist; all36 acceptance remains open.
- [x] Shared saved Off/Light/Standard/Firm touch feedback and custom input hooks are in source. Physical feel remains untested.

## Delivery and phase boundaries

| Item | Current state |
| --- | --- |
| Published candidate | **0.1.22 / 6087d47e arm64**, APK packaging only; new verification deferred. |
| Installed phone | **0.1.4 /08d3911e**, historical installation/data-preservation evidence linked above. |
| Current policy | Usability first. No new tests, lint, screenshots, security review, bug campaign, phone queries or outputs. |
| Persistent formats | App database17 / portable backup14. Settings-profile transfer is a separate limited whitelist. |

Do not recreate features solely because an older release note called them missing. The feature matrix links each feature to its actual implementation, historical checks and limits. [Preserved earlier list](history/REMAINING-WORK-2026-10-30-before-consolidation.md) contains the full old release history and evidence; it is not the current checklist.
