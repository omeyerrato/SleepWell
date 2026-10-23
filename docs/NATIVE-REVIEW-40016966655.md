# Independent original-capture review: CI 40016966655

Reviewed all files individually at original resolution from the downloaded CI artifact. This records bounded observations, not completion of all feature or design requirements.

## Automatic tracking: five originals

- `automatic-manual.png`: cream/Inter layout and radio state align; the sole Save footer is visible. The blank lower region reflects this bounded manual configuration.
- `automatic-stillness.png`: selected After settling down, the 15-minute screen-off estimate, sound-off state and Save/Arm controls are readable without clipping.
- `automatic-sounds.png`: Record sounds and Keep clips state are visible above the full two-button footer.
- `automatic-reopened.png`: sound/clip selections persist; rows and footer remain aligned.
- `automatic-error.png`: Smart wake window without an enabled alarm displays the actual prerequisite error above usable controls.

These pass a readability/alignment review of the limited explicit arm-before-bed configuration. Board 10's broader automatic scheduling, missed-night estimation, charging rules and activity access are not all implemented here. These captures do not establish sleep accuracy, microphone operation or full board fidelity.

## Sleep rhythm: five originals

- `rhythm-seven-nights.png`: real seven-night fixture yields 1 h 20 min, seven dated purple markers and a goal line. Metric/group/progress hierarchy follows board 09. **Correction required:** right endpoint 12:00 AM clips.
- `rhythm-workdays.png`: all seven switches and fixed Save footer are visible. Monday is off and Saturday on after user action.
- `rhythm-recreated.png`: that unsaved draft survives recreation with the same complete control layout.
- `rhythm-reopened.png`: persisted schedule changes the actual group comparison. **Correction required:** floored 8 min differs from the 9-minute difference between rounded 2:54 AM/3:03 AM clocks.
- `rhythm-empty.png`: dash/zero-night states contain no fabricated markers or social-jetlag estimate. The workday setting remains reachable through scrolling.

The follow-up source clamps measured tick text to chart bounds and derives the displayed gap from nearest-minute clock endpoints. Fresh originals are required before accepting those corrections visually. Chronotype remains a disclosed history target, not an implemented clinical estimator.

## Travel: three originals before test failure

- `travel-plan.png`: destination/home clocks correctly show the fixture's current two-hour London/Riyadh difference and the departure's three-hour difference. The home icon should be a house and the light-source value wraps; corrected in the separate travel commit.
- `travel-zones.png`: searchable Europe/London selection is readable with an appropriate one-result empty region.
- `travel-date.png`: the owned cream calendar has legible October 2026 cells and selected date.

The separate failure original shows the time dialog with Hour/Minute fields present. Its test searched before the window transition settled and then tried to scroll a non-scrolling dialog. The fix waits for the expected window and semantic controls. Reminder save/reopen/delivery/cancel behavior remains pending in a passing native run.

## Privacy failure original

No ordinary privacy captures were produced before failure. The failure original shows a stock white dialog with uppercase KEEP/DELETE HISTORY labels. The generic Keep lookup missed it and attempted a nonexistent scroll. The follow-up replaces this production dialog with owned cream/Inter/rounded confirmation controls and tests their stable semantic descriptions, while retaining the worker, database/file preservation and cancellation assertions. Full erase/error/retry/recreation native verification is still pending.
