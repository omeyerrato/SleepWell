# Tracking setup: rendered light-glyph evidence

[CI 40601122904](https://github.com/omeyerrato/SleepWell/actions/runs/40601122904), source `203cb4177c07d198fc8ea86874fa69ad215edd1e`: **OrganizedSettingsJourneyTest#organizedMenusPreserveDraftsAndSafeChoicesWithoutOutputs passed in 58.691s, one method, 20 original PNGs. Overall job FAILED.** GuardFlowTest failed in 30.7s: one native Guided wind-down tap did not launch GuidedWindDownActivity. The unchanged combined summary retains that failure and missing `guard-wind-down-paused.png`. MotionConfigurationFlowTest passed separately; this folder does not review its images or claim its coverage.

All 20 PNGs below were opened individually. Original image, method-log, environment and combined-summary bytes are unchanged. `run.json` records the failed GitHub job; `source-equivalence.json` records scoped source/asset blob hashes, equality with root's committed and Git-filtered working files at archive time, and the comparison-only old image hash. SHA256SUMS covers every retained file except itself. Guard diagnostics belong to the separate Room/Guard evidence; they are not duplicated here. Earlier evidence folders are unchanged.

## Board 06 comparison

The new [Tracking main original](organized-settings-tracking-main.png) has visibly light clock, notification, Wi-Fi, cellular, battery and gesture glyphs on plum. The exact earlier `40597518409` original was opened for comparison: those glyphs and gesture were dark. The earlier public [tracking/settings archive](../2026-10-28-tracking-settings/README.md) remains unchanged, including its unresolved dark-glyph observation.

TrackingActivity and Design source blobs are identical between the older `e6b3b8604b70d78ecfd746120183fb2793ca11a5` source and this run. The strengthened native capture waits for actual rendered light system-bar pixels while the same owned window is focused, then independently decodes and checks the saved PNG. This establishes the captured state after handoff, not a new production layout correction or a guarantee about every transient first-draw frame.

Compared with [board 06 left](../../../design-concepts/2026-10-22-phone-app/06-tracking.png), the represented normal-text main states retain the selected phone radio card, second contactless card, bedside placement art/guidance, sound/light controls, true Manual row and fully visible fixed Start action. Header More opens named supporting pages. Main/body/control bounds and the native sensitivity range pass the existing full-visibility and 48dp gates. The sensor card stays selection-only; sensitivity is reached through its named settings row. The light-on draft survives child navigation, Manual chooser cancellation and recreation. A separate sonar-selected intent fixture returns to phone selection in place; it does not qualify a real sonar route.

The board's segmented automatic control is still represented by an honest Manual chooser with a separate review/Arm flow. Art/crop, shared glyphs, card proportions, additional privacy guidance and supporting pages still differ. Start is inspected, never pressed. This is setup/configuration evidence, not actual capture-runtime acceptance or completed screen 16/all 36 screens.

## Actual state and input coverage

The same method uses private records/settings and throwing activation/output ports. It changes the real native sensitivity range from Medium to High, checks its actual current label, cancels and verifies no saved motion setting. Sound category/duration settings are saved to private preferences and reopened; sound timer 45 minutes and fade 10 minutes persist. Retention application, preview/playback, capture, scheduling and Arm actions remain forbidden.

Automatic recovery uses an injected denied `onRequestPermissionsResult` callback and an existing fake notification-capability port. Its app-owned recovery action survives recreation without duplication; restoring fake access removes it, while the unsaved bedtime/charging draft stays intact. No OS permission dialog, real grant/revocation, system-settings action or recording was exercised. Manual and recurring chooser drafts are cancelled. Motion's Active badge comes from a fake port with no sensor registration: its empty graph and waiting message truthfully contain no samples.

## Individually inspected originals

Each name has the `organized-settings-` prefix. All listed actions and footers fit in their represented frame; conditional omissions are recorded below.

| Original suffix | Observation |
| --- | --- |
| `tracking-main.png` | Phone selected, sound/light off, Manual and complete Start; light system glyphs and gesture on plum. |
| `tracking-recreated.png` | Light-on draft retained; phone selected, sound off, Manual and complete Start; light bars. |
| `tracking-more.png` | Named start/schedule, sensitivity/Medium, Sampling & battery and recording tools; full Back action and light bars. |
| `tracking-sensitivity.png` | Medium native range with complete Test/Use footer; fake-port Active, empty measured graph and honest waiting message. Cream status glyphs are faint/white. |
| `tracking-power.png` | Battery-aware off, unsupported batching disabled, reserve Off, complete Done; light bars. |
| `tracking-power-recreated.png` | Same page/state/parent and complete Done after recreation; light bars. |
| `tracking-phone-selected.png` | Sonar-intent fixture returned to phone in place, sound/light off and complete Start; light bars. |
| `tracking-automatic-manual.png` | Real Manual-only choice with full Save and no Arm; Back cancels. Some system glyphs/gesture are white on cream. |
| `automatic-conditions.png` | Charging-on/screen-off-off draft after recreation; explicit save/arm scope and full Done. |
| `automatic-permission-recovery.png` | At-bedtime draft retained, notification warning and one Open app settings action; Save/Arm visible but never activated. Fake denied callback only. |
| `recurring-conditions.png` | Charging-on draft, screen-off-required stillness explanation and full Done; schedule is not armed. |
| `sensitivity-categories.png` | Five of six estimates selected, Laughter off, honest sound-clips-off summary and full Save. |
| `sensitivity-duration.png` | Real Snoring duration picker, Default about two seconds selected and complete Cancel. |
| `sensitivity-reopened.png` | Saved five-of-six/custom durations reopened; restore-defaults action and full Save. |
| `retention-details.png` | Deletion/favorite/history consequences and full Save & apply; application is not invoked. |
| `snoring-options.png` | Media route, contactless cue switches off, requirements and full Save; no cue output. |
| `sounds-sources.png` | Actual import/rotation/playlist/radio/stereo routes and full return action; source/import/playback actions not invoked. |
| `sounds-playback.png` | Tracking-start/stillness switches off, explanatory routes and complete return action; no player starts. |
| `timer-recreated.png` | Real 45-minute draft survives recreation, full Use/Cancel; subsequent Use saves privately. |
| `fade-reopened.png` | Saved 10-minute fade reopened with full Use/Cancel; Cancel retains the setting. |

## Limits still open

The rendered dark-Tracking glyph gate covers TrackingActivity's six plum frames. It does not establish every child screen's system-bar contrast: `tracking-sensitivity` and `tracking-automatic-manual` retain the observed faint/white glyphs on cream. The CI-only brown review caption on dark sound pages also has low contrast; it is instrumentation labeling, not production product copy. These observations are retained without source mutation.

No device, microphone, speaker, light sensor, accelerometer, real permission, battery/FIFO behavior, actual Start/Arm, haptic/audio output, live-night graph or physical-phone result is accepted by this archive. Imported sources, retention deletion and broader font/device-size states were not exercised. Full 36-screen reference acceptance remains open.
