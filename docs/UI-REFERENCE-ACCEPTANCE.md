# 36-screen reference acceptance

Reference: `design-concepts/2026-10-22-phone-app/SCREEN-MAP.md`. Each board has left / middle / right screens. This is a visual and interaction acceptance inventory, separate from feature availability. **No screen is declared fully finished by this initial inventory.** A bounded passing journey does not establish every reference state or physical-device behavior.

Evidence prefixes below are repository-relative. `CI:` paths are downloaded original Android captures under the root checkout's ignored `.android-local/ci-reviews/<run>/silent-android-review/`; they are not generated mockups. `Pending` means a current-source native comparison and main-action/persistence check is still required. Existing capture locations are identified without pretending they were all visually reviewed in this pass.

| ID | Reference screen (board / position) | Latest evidence identified | Open visual/interaction acceptance |
|---|---|---|---|
| 01 | Tonight (01 / left) | CI:40451121212 `arc-upcoming.png`, `arc-middle.png`, `arc-end.png`, `arc-weekly.png` | 9 originals inspected; foreground clock 0→50→100, help recreation and action roles passed. Remaining illustration crop, off-state spacing and intentional4-tab dock differ. Whole-board parity open. |
| 02 | Sleep report (01 / middle) | `docs/verification/2026-10-27-report-calendar-complete/report-reference-night.png` | Report agent owns presentation: date/Edit hierarchy and duration spacing; real missing stages/audio cannot be invented. |
| 03 | Wake-up challenge (01 / right) | Pending current-source capture | Focused private/silent challenge preview comparison needed; never trigger a real alarm for visual acceptance. |
| 04 | Alarm list (02 / left) | Pending current-source capture | List expansion, disabled state, actions and own-tab proportions need comparison. |
| 05 | Edit alarm (02 / middle) | CI:40445147772 `organization-alarm-editor.png` | Whole Schedule & dates row and footer passed; intentional supporting groups/search extend reference. Time/weekday/action proportions still need dedicated comparison. |
| 06 | Smart wake-up (02 / right) | CI:40445147772 `organization-smart-following.png`, `organization-smart-configured.png`, `organization-smart-reopened.png` | All inspected; no clipping, contrast/override/fullJSON Save+reopen passed. Timeline and extra inheritance choices differ; final reference acceptance open. |
| 07 | Alarm sound (03 / left) | Pending current-source capture | Real selection/import/volume/preview hierarchy needs quiet/fake-output journey. |
| 08 | Gentle wake-up (03 / middle) | Pending current-source capture | Actual ramp/delay/sunrise/flashlight choices and curve labels need comparison. |
| 09 | Snooze & backup (03 / right) | CI:40445147772 `organization-controls-recreated.png` | Grouped navigation/recreation bounded pass; full controls and reference hierarchy pending. |
| 10 | Challenge routine (04 / left) | CI:40467733318 `routine-ordered.png`, `routine-reordered.png`, `routine-recreated.png`, `routine-reopened.png` | 6 originals individually inspected; 3 cards + 4 actions+full More task types clear above Save, actual drag/cancel/edit/save/reopen passed. Original icon art/type count and default-state parity remain open. |
| 11 | QR / NFC enrollment (04 / middle) | Pending current-source capture | Camera unavailable/permission/captured/config persistence need actual quiet reference comparison. |
| 12 | Wake-up check (04 / right) | Pending current-source capture | Amber focused surface/countdown/confirmation needs private clock/ownership fixture; no physical alarm. |
| 13 | Wind-down (05 / left) | Pending current-source capture | Timed practice/narration/guard/sounds hierarchy and saved state pending. |
| 14 | Blocked apps (05 / middle) | Pending current-source capture | Selected app rows/schedule/pause/error states pending. |
| 15 | Sleep goals (05 / right) | `docs/verification/2026-10-25-inclusion-goal-backup-native/dated-goal-plan.png` | Original and board05 inspected. Source refinement puts target dash/key beside heading with large-text fallback, measured axis-label space and subtle day guides/baseline. Actual records/unknown gaps, bank/reminder/time/draft/save controls unchanged. Current-source native capture remains pending; extra Smart Bedtime/Wind-down options remain intentional. |
| 16 | Tracking setup (06 / left) | Pending after grouped setup implementation | Essential sensor/placement/sound/light/Start hierarchy needs actual current capture. |
| 17 | Sonar calibration (06 / middle) | Pending current-source capture | Real signal availability/permission/fallback states; no fabricated sensor bars. |
| 18 | Live tracking (06 / right) | Pending current-source capture | Private fake-sensor clock/actual controls; physical tracking not authorized. |
| 19 | Sleep sound mixer (07 / left) | `docs/verification/2026-10-27-organized-menus/` (inventory location) | Current grouped mixer source needs exact main+timer+fade+selected-library capture comparison. No audible preview. |
| 20 | Sound events (07 / middle) | `docs/verification/2026-10-24-ci40104638385-category-second-attempt/analysis-events-all.png`, `analysis-events-speech.png`; `library-recordings.png` | First2 originals inspected against board07. Compact label/duration target,48dp favorite/delete and actual-wave inset implementation pending current capture/action acceptance. Analysis-only events deliberately have no waveform. |
| 21 | Anti-snoring cues (07 / right) | `docs/verification/2026-10-27-organized-menus/organized-settings-snoring-options.png` | Exact main settings reference comparison pending; supporting options must remain discoverable. |
| 22 | Sleep history (08 / left) | `docs/verification/2026-10-27-report-calendar-complete/report-reference-history.png` | Report agent: 11-frame calendar/edit flow passed. Continuous real coverage differs from illustrated stage ribbons; visual refinement ongoing. |
| 23 | Weekly trends (08 / middle) | Same folder `report-reference-week.png`, `report-reference-month.png`, `report-reference-year.png` | Actual calendar/range/scoped Read details flow closed; graph spacing/axis/reference proportions remain Report agent-owned. |
| 24 | Edit sleep record (08 / right) | Same folder `report-reference-editor.png`, `report-reference-tags-notes.png`, `report-reference-reopened.png` | Actual tag/note/edit/fullJSON/persistence passed; detail/awake/footer proportions differ, Report agent-owned refinement. |
| 25 | Sleep regularity (09 / left) | Pending current-source capture | Real duration/bedtime/missing-data thresholds and chart labels pending. |
| 26 | Nap (09 / middle) | `docs/verification/2026-10-25-power-nap-pass/` (older inventory) | Current timer/minimum-rest/schedule profile options and saved state need reference comparison. |
| 27 | Travel (09 / right) | `docs/verification/2026-10-27-smart-wake-travel/` (inventory location) | Current trip/home/target/light prerequisites and clock hierarchy pending. |
| 28 | Automatic tracking (10 / left) | Pending current-source capture | Real armed/charging/screen-off/battery/delay states pending; no auto-recording during review. |
| 29 | Phone motion test (10 / middle) | `docs/verification/2026-10-24-native-configuration/motion-settings-ready.png`, `motion-settings-reopened.png` | Older saved-config evidence only; current live-test/reference comparison pending, no invented motion bars. |
| 30 | Room conditions (10 / right) | Pending current-source capture | Live unavailable/quiet/lux/dimming states require inert private ports, no fake sensor truth. |
| 31 | Settings home (11 / left) | CI:40445147772 `organization-settings-root.png` | 14-frame organization pass: all 8 groups+GitHub fit, category/search/back/persistence. Reference currently8 grouped rows+intentional Home row; final icon/spacing comparison pending. |
| 32 | Health Connect (11 / middle) | `docs/verification/2026-10-25-feature-sprint-expanded/health-connect-default.png`, `health-connect-configured.png` | Older fake-provider state/persistence only; current visual/reference comparison pending, no actual provider grant. |
| 33 | Automation rules (11 / right) | Pending current-source capture | Real rule/destination/default-off/error states pending; no external request. |
| 34 | Alarm readiness (12 / left) | Pending current-source capture | Actual permission/error/optional states and reference hierarchy pending; no test-alarm action. |
| 35 | Data & privacy (12 / middle) | Pending current-source capture | Current real storage/archive/settings/provider controls and supporting groups need comparison; no destructive actions. |
| 36 | Appearance (12 / right) | Root-owned current refinement | Root owns AppearanceActivity/ThemeScene and existing quiet appearance journey. Pending resulting capture/action acceptance. |

## Current Sound events preservation map

- Existing stable category IDs, counts, filter persistence, all extended categories and per-night favorites remain. The three visible chips now expose actual radio state and shared user-gesture feedback.
- Event label + duration is one 48dp correction action, with the existing `Correct label event:<id>` target; user-corrected/original-estimate wording remains unchanged.
- Favorite and delete keep their original worker-backed persistence, confirmation and error handling, with 48dp targets. No hidden delete or auto-save is introduced.
- Play/pause, measured PCM peaks, seek intervals, source trimming, missing audio, foreground arbitration, generation fences and port cleanup use existing controllers unchanged. The selected audio gets an inset background; analysis-only rows stay explicitly nonplayable.
- Historical analysis title and attention/errors remain on the list. Full original-recording coverage/provenance and correction explanation are under More → Analysis details. Options are scrollable; Categories/Options/Analysis details restore only if actually open across recreation. Opening any menu never plays audio.

Source-only implementation is not final screenshot acceptance. Root coordinates the one existing silent event/library flow after integration; no new broad test campaign or physical output check is implied.

The bounded acceptance selector is `org.sleepwell.app.SoundLibraryUiTest#recordedIntervalsFavoritesAndDeletionPersistWithFakePlaybackOnly`. It uses real generated WAV/PCM and isolated storage with injected playback output. Required captures: `library-recordings.png`, `library-playback.png`, `library-favorite-recreated.png`, `library-speech.png`, `library-recording-deleted.png`, `library-options.png`, `library-options-recreated.png`, `library-analysis-details.png`, `library-analysis-details-recreated.png`. The same existing method now checks open/recreated Options selection and Analysis details, no new playback requests during either menu restoration, and dismissed dialogs staying dismissed. This is a proposed runner contract until its resulting native run is inspected.
