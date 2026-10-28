# Guard and Wind-down: bounded native pass

[CI 40605830605](https://github.com/omeyerrato/SleepWell/actions/runs/40605830605), source `5bf664434ca303d26069e59287cc0e3c3a55a497`: **GuardFlowTest#blockedAppDraftCancelsAndSavedChoicesReopen passed in 24.657s, one existing method, all eight required originals.** The whole `label-navigation` job passed both selected methods and all 24 required images. This archive reviews only Guard/Wind-down; the other method's 16 Tone images are not duplicated.

All eight original PNGs were opened individually against [board 05, left and middle](../../../design-concepts/2026-10-22-phone-app/05-bedtime.png). PNG, method-log, environment and combined-summary bytes are unchanged. `run.json` retains the successful job identity; `source-equivalence.json` records 22 scoped source/asset blob hashes matching root's committed and Git-filtered working files at archive time. SHA256SUMS covers every retained file except itself. Earlier failed evidence remains unchanged.

## Production correction and actual input

The run includes the actual MainActivity touch-ownership fix [759ce8fe](https://github.com/omeyerrato/SleepWell/commit/759ce8fe9afed97ff950d59ff4cd73e68ca56b39): title/subtitle child tooltips moved to their clickable Wind-down row. The Guard picker has the corresponding owning-row tooltip change from `ae791574`. These are production changes; the earlier test-only focus/input synchronization and destination monitor did not fix the consuming child tooltip.

The app-picker test physically taps the actual app name's measured screen bounds, explicitly outside its independent Switch. It checks that the child has no tooltip and the owning clickable row retains the full-name tooltip. Calendar changes only in the picker draft, survives recreation, and Cancel restores the still-saved selection. This exercises the label's touch path rather than calling its row listener directly.

Wind-down actions use a single native UiAutomator click after owned-focus/frame, enabled-action and matching native/published bounds checks. More opens the real Guided wind-down and Sleep plan activities; an ActivityMonitor plus the actual resumed owner and destination labels must match. Back destroys each child and restores its exact Main parent. Guided practice/narration and plan Save are never activated. Start sleep tracking opens the real Tracking setup activity, then Back returns without pressing its inner Start or writing recording state. More survives Main recreation; Done and Back close it without opening a child or saving settings.

Both Guard and Main write their real five-minute pause deadline in the represented fixture. Main checks an approximately 300-second deadline, recreates the Activity, verifies the exact saved deadline and captures App guard as Paused. The bedtime countdown remains a countdown to bedtime; this pause does not freeze that clock. Expiry after five real minutes and actual blocking/resumption are not exercised.

## Board comparison and original frames

The dark plum surfaces, cream titles, lavender actions, clock/handset glyphs, real installed-app rows and fixed footers follow the board's hierarchy. Guard shows four real launchable apps with access to the full picker, rather than inventing the board's social-app inventory. Wind-down retains the bedside art, actual countdown/target, Alarm/App guard/Rain panel and both fixed actions. The main body, 48dp header row/Back/More/target, all three rows above Start, and complete non-overlapping Start/Pause controls pass the existing bounds gates. Start has no scrolling ancestor.

| Original | Individually inspected state |
| --- | --- |
| `guard-draft.png` | Calendar selected and Schedule off in a recreated unsaved draft; actual Camera/Chrome/Clock rows, calls notice and complete Save/Pause. Back discards the draft. |
| `guard-saved.png` | Saved Calendar selection and enabled schedule reopen; full Save/Pause and access-needed guidance remain visible. |
| `guard-options-recreated.png` | Full app chooser restored with Calendar locally off, real additional apps and complete Use selection/Cancel footer. The saved selection remains on; Cancel returns to it. |
| `guard-paused.png` | Actual paused-until time and Resume now state, saved Calendar retained and complete footer. Resume is shown, not pressed. |
| `guard-wind-down.png` | Actual 30-minute future target, Alarm Off, App guard Access needed, Rain/30-minute timer and complete fixed Start/Pause. Dark system glyphs noted below. |
| `guard-wind-down-options.png` | Real Guided wind-down/Sleep plan menu, truthful descriptions and complete Done; no placeholder actions. |
| `guard-wind-down-recreated.png` | The same More dialog restored after Main recreation. This frame is a restored menu, not a separate plain-main capture. |
| `guard-wind-down-paused.png` | App guard Paused after real pause-write/recreation; target/countdown, Alarm/Rain and both fixed controls remain intact. |

**Remaining visual deviation:** the first `guard-wind-down.png` has dark clock, notification, Wi-Fi, cell, battery and gesture glyphs on plum. Later Wind-down/menu frames are light. This method checks window/frame/bounds, not the stricter Tracking saved-pixel glyph gate. This pass does not accept that first-frame system-bar fidelity. Art/crop, vertical spacing, shared glyph geometry, actual app inventory and permission/error-dependent copy still differ from the board. The extra accessibility guidance and supporting menu are real product states beyond the illustrated reference.

## Data and output limits

The fixture uses private records, plan, sound-mix, recurring and delayed-start stores. Original preferences are restored with their exact values and absent keys. During Main navigation, opening/cancelling/restoring menus and child pages must leave the inert preference map unchanged; the explicit pause may add only `focusPauseUntil`, which is then restored. The private sleep-plan and Guided preference maps stay unchanged, recurring preferences stay empty, the disabled alarm sentinel stays byte-equivalent as JSON and no recording session is created.

The original database assertion snapshots these seven core tables: `alarms`, `sessions`, `epochs`, `clips`, `tracking_pauses`, `record_edits`, `record_create_requests`. They must match before/after. This is not an independent snapshot of every auxiliary table or an export round trip. Activity/read-worker/plan-operation/delay cleanup barriers run before owned files are deleted. Throwing scheduling, reminder, playback and tracking-command ports reject accidental activation; alarm/recording/service/microphone-lease/sleep-player state remains quiet.

No accessibility grant, real app blocking, calls/emergency access, OS permission/settings flow, hardware sensor, actual tracking Start, alarm, audio, haptic or physical-phone behavior is certified. Room dimming is explicitly off in this fixture. Guided completion/narration, plan saving, pause expiry and broader font/device-size states are outside this method. This is bounded layout, native navigation and draft/persistence evidence; full 36-screen reference acceptance remains open.
