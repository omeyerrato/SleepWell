# Organized settings and Live options: bounded evidence

[CI 40597518409](https://github.com/omeyerrato/SleepWell/actions/runs/40597518409), source `e6b3b8604b70d78ecfd746120183fb2793ca11a5`, **overall FAILED**. Two named methods passed: `OrganizedSettingsJourneyTest#organizedMenusPreserveDraftsAndSafeChoicesWithoutOutputs` in **46.762s / 20 PNGs**, and `SonarNightFlowTest#visibleStartPauseRecreateResumeAndExplicitStopSaveActualNight` in **9.75s / 9 PNGs**. Their raw transcripts report `OK (1 test)` independently.

The unchanged combined summary and two additional raw failure transcripts retain Guard's **23.423s** failure (`Focused owned window contains Start guide`) and Motion/Room's **30.472s** failure (`Room fixture reached its actual owned state`). Those methods are not accepted here; their PNGs and failure dumps are not duplicated. Only the 29 passing-method originals are included.

All 29 PNGs were opened individually. Tracking/Live were compared with [board 06](../../../design-concepts/2026-10-22-phone-app/06-tracking.png); Automatic/recovery and the Motion supporting page with [board 10](../../../design-concepts/2026-10-22-phone-app/10-phone-sensors.png). The remaining settings frames are supporting menus. Original images, four transcripts, environment and failed summary are byte-identical copies. SHA256SUMS covers every file except itself. `source-equivalence.json` pins 18 source blobs/SHA-256 values and both reference hashes; listed sources match the run and root at archive time. No APK or unlisted-source equivalence is asserted.

## Originals individually inspected

The first table's names have prefix `organized-settings-` and suffix `.png`.

| Original suffix | Observed result |
| --- | --- |
| `sensitivity-categories` | Five categories on, Laughter off; readable consequences and full Save. |
| `sensitivity-duration` | Owned Snoring duration dialog, Default selected, three choices and complete Cancel. |
| `sensitivity-reopened` | Saved 5-of-6 count and custom duration summary; full Save. |
| `retention-details` | Recording/favorite/history effects readable; full Apply, which this method does not press. |
| `snoring-options` | Actual output/About destinations, contactless switches off, full Save. |
| `automatic-conditions` | Retained charging-on/screen-off-off draft and full Done. |
| `automatic-permission-recovery` | At bedtime selected, denial message and one complete Open app settings action above Save/Arm. This is a synthetic denial fixture. |
| `recurring-conditions` | Charging-on draft, explicit skip-night semantics and required screen-off explanation; full Done. |
| `sounds-sources` | Source routes, Rain selected and explicit Play guidance; full Back. CI-only label has low contrast. |
| `sounds-playback` | Both choices off, separate media-fade consent route and full Back; same CI-label contrast limit. |
| `timer-recreated` | 45-minute draft retained, complete Use/Cancel. |
| `fade-reopened` | Saved 10-minute choice selected, complete Use/Cancel. |
| `tracking-main` | Main controls/placement/Manual route/full Start fit, but status clock, notification, Wi-Fi, cell, battery and gesture bar are dark on plum. |
| `tracking-more` | Named start/schedule and sensor/battery routes; full Back and light system glyphs. |
| `tracking-sensitivity` | Medium native range above full Test/Use; fake Active source, honestly empty graph and waiting message. |
| `tracking-power` | Battery-aware off, unsupported batching disabled, reserve Off; full Done. |
| `tracking-power-recreated` | Same settings/parent after recreation; original bytes equal preceding frame. |
| `tracking-recreated` | Light-on draft retained, sound off, Manual and full Start; light system glyphs. |
| `tracking-automatic-manual` | Actual Manual chooser with radio selection and full Save; cancelled without Save/Arm. |
| `tracking-phone-selected` | Phone selected in place from sonar intent fixture; sound/light off, full Start and light system glyphs. |

Live originals have prefix `sonar-night-` and suffix `.png`.

| Original suffix | Observed result |
| --- | --- |
| `setup` | Sonar selected, explicit setup guidance, analysis/clips off and light unavailable; full Start. |
| `measuring` | Actual fixture elapsed time, No alarm set, Measured/Off states, visible violet graph marks, More and full Pause/Stop; clipped supporting copy removed. |
| `paused` | Paused status and stopped-sensors copy, elapsed 8 sec, graph retained, full Resume/Stop. |
| `options` | Named Tracking options child, complete reserve/sampling/flip explanations and three applicable routes, full Done. |
| `options-recreated` | Same paused child/details/Done after recreation; original bytes equal preceding frame. |
| `paused-recreated` | Main paused view restored by Done; unchanged elapsed and full Resume/Stop. |
| `resumed` | Elapsed advances to 12 sec; real fixture graph marks separated by a pause gap, full Pause/Stop. |
| `missing` | Signal missing remains explicit, graph leaves the newest missing interval blank; full Pause/Stop. |
| `stop-confirmation` | Actual owned End this recording dialog, clear Keep tracking/End & review actions. |

## Executed behavior and evidence limits

Organized Settings retains its existing category/duration save+reopen, retention cancel, snoring/automatic/recurring draft recreation, silent source/playback navigation, timer recreation/Use and fade save+reopen assertions. Tracking uses private records/motion/delay stores and a MotionPort fake that registers no sensor and supplies no samples. Start/commands/runtime review, retention apply, cue output and scheduling ports throw. The method checks real radio state, phone selection in place, retained light draft, native sensitivity range/48dp bounds/ACTION_SET_PROGRESS and cancelled settings; no recording/delay admission is created. Original preferences and fixture lifetimes remain guarded/restored.

Automatic recovery uses the existing fake notifications-access port and a synthetic DENIED callback with an explicitly set in-flight flag. It verifies complete focused-frame bounds, exactly one recovery action after recreation, retained charging/mode draft, removal after fake access becomes available, no draft preference writes and actual Back cancellation. Open app settings, Save and Arm are not pressed in that fixture. This proves native app recovery rendering/ownership under controlled inputs, **not Android's permission controller, a real Settings return or a real grant**.

Live uses real single-use admission, SonarNight coordinator and private SQLite with synthetic duplex PCM; its Commands factory starts no Service and opens no device audio. The native Start, Pause, explicit Resume and Stop confirmation are exercised. While paused, More opens the actual child; recreation retains it, the old owner is destroyed, detached old row/Done callbacks are invoked and remain inert, Done returns to main, and reopening plus actual Back returns in place. Paused active duration and one closed fake PCM port remain unchanged until explicit Resume allocates the second port. The saved result has one completed session/pause, valid and missing epochs, no sound events, and a validated backup; fixtures close before restoration/removal.

Five Live main captures assert the entire chart, More and 48dp Pause/Resume/Stop bounds, chart/footer clearance, no action overlap and absence of moved supporting copy. Measuring/resumed also check actual violet pixels. Eight regular Live captures check rendered light status/gesture regions both before and in the saved original; Stop uses a separate focused-dialog/frame/full-bounds check. All nine were visually inspected. Fault-specific reserve/sampling/flip alerts and the conditional pair/travel routes are not independently exercised here.

## Remaining visual and acceptance gaps

**The first Organized Tracking frame's dark system glyphs/gesture remain a visible failure of reference fidelity despite that method passing.** Later frames are light. Production already requests focused dark-page system bars; this early capture alone does not establish the cause. No image was repainted, cropped or substituted, and this archive does not claim a correction.

**Live SonarSignalChart is still a rolling 15-second signal, explicitly labeled “15 s ago → Now”; board 06 right shows an entire-night start/time/Now axis.** The options change removes the clipped reserve/sampling/flip copy and restores the main hierarchy, but does not close that chart/reference gap. Icons, type/control proportions and placement-art crop also differ; the true Manual destination replaces the board's segmented control. No complete Live, Tracking, 36-screen or broad security acceptance follows from these two passes.

API 36 disposable emulator, 1080×2316 override, 420 dpi, host output disabled. Physical-device sensing, speaker/microphone behavior, alarm firing, automatic/recurring starts, network/provider operations, DND, haptics, permission grants/revocations, large text/RTL and other methods remain outside this acceptance. Archiving ran no build, test, CI, device or output action.
