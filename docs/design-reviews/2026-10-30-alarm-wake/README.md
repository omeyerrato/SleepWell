# Alarm and wake-up reference comparison — 30 October 2026

This is a bounded visual and configuration-flow record for source `371c5b9e9b53655cb7d32d85ae9d6fc6f27fda79`. It does not mark all 36 designs or physical alarm delivery complete.

The primary screens were inspected at full screenshot size against boards 01/right, 02, 03 and 04. All **83 alarm** and **25 wake** captured originals, including supporting states, were also visually inspected in labelled contact sheets. The twelve PNGs here are selected original Android captures, copied without cropping, resizing or repainting; their source run and SHA-256 are in [originals.json](originals.json).

## Evidence and actions

- [Run 41021582967](https://github.com/omeyerrato/SleepWell/actions/runs/41021582967), `alarm-reference-current`: five selected configuration journeys passed; all 70 required originals present. [Recorded summary](41021582967-summary.json).
- [Run 41021600537](https://github.com/omeyerrato/SleepWell/actions/runs/41021600537), `wake-reference-current`: two selected journeys passed; all 25 required originals present. [Recorded summary](41021600537-summary.json).

These runs used an API36 disposable emulator at 1080×2316 and 420dpi. They exercised the actual native menus, accessible controls, draft changes, Cancel/Use/Save, recreation and reopening of private stored fixtures. Scheduling, sound previews, camera/NFC input and wake-check time/delivery were replaced with existing inert ports where relevant. Neither journey launched real `AlarmActivity`. There was no phone, real alarm, speaker, vibration, flashlight or broad 28-task practice run.

## Primary comparison

| Reference | Original capture | Visual findings and retained intent |
| --- | --- | --- |
| 02 left | [Alarms](organization-alarm-list.png) | Large clock/period hierarchy, aligned switches, labels and grouped floating navigation. The user-requested compact list and separate fourth Alarms tab intentionally replace expanded settings on every list item. Both fixtures are disabled, so their clocks are grey. |
| 02 middle | [Edit alarm](organization-alarm-editor.png) | Centred clock/steppers, circular checked weekdays, named row, four primary settings and fixed Alarm on/Save section. Search and More retain the additional features without crowding the main rows. |
| 02 right | [Smart wake-up, inherited settings](organization-smart-following.png) | Window range, landscape interval, slider, sensitivity controls and goal setting align with the reference hierarchy. The inherited profile/goal remain explicit and have no invented manual selection. The prerequisite card now fits above Use smart wake-up. |
| 03 left | [Alarm sound](alarm-tone-builtins.png) | Warm selected tabs/radios, three tone rows, actual decoded waveforms, volume controls, preview panel and fixed Use action. The test-only fixture label is visible; production has no such label. Wave shapes reflect the audio rather than copied decorative peaks. |
| 03 middle | [Gentle wake-up](wake-settings-normal-curve.png) | Actual 80%/two-minute output curve, readable axes, warm duration pills, independent vibration delay and sunrise controls. Flashlight is kept directly discoverable. Use wake-up settings updates the draft; Save alarm commits it. |
| 03 right | [Snooze and backup](advanced-snooze-initial.png) | Clear section hierarchy, selected duration pills, count/repeat glyph, music glyph for backup tone, requested volume and wake-up check. Advanced duration budgets and button/orientation options remain in Options. |
| 04 left | [Ordered routine](routine-ordered.png) | Numbered task cards, icon wells, separate edit/reorder affordances, complete Set up labels, available tasks and paired footer actions. Set up navigates to configuration rather than adding an unexplained task immediately. |
| 04 middle | [Morning code captured](qr-code-captured.png) | Code/NFC tabs, location field, green captured state, Math fallback and fixed Save code. The camera image exists only during an explicitly started scan; this post-capture fixture honestly shows the retained code state rather than fabricating the reference bathroom photo. |
| 04 right | [Wake-up check](wake-check-prompt.png), [countdown](wake-check-countdown.png) | Dawn scene, clock/heading hierarchy, large sun/countdown ring and dominant confirmation. The ring is full at 2:00 and shrinks at 1:15 from the fixture clock; it does not copy the mockup’s static partial ring. |
| 01 right — shared Math card | [Initial](challenge-math-initial.png), [selected](challenge-math-selected.png) | Large question, four balanced answer targets, brown selected answer and clear Check action match the card hierarchy. This is explicitly the safe practice route, retaining its preview message and Done action. It provides shared-card evidence, not real alarm-header/delivery evidence. |

## Supporting-state review

The originals include long snooze values; picker cancellation/recreation; invalid backup-sound review; sound import, decoder-busy and preview error/recovery; clock stepping and reopening; smart inheritance/override; schedule, orientation and advanced menus; QR permission/format/scanning/error/fallback/recreation; shared NFC enrollment, conflicts and unavailable access; task search/empty/configuration/reordering; Math wrong/typed/selected/recreated/completed; and wake-check waiting/confirmed/reopened/storage error. Relevant actions and exact private-data preservation gates passed in the selected journeys.

No new material clipping or selection ambiguity was found in the inspected primary states after the preceding corrections. Supporting pages remain scrollable when their extra content requires it. This result is limited to these captures and fixtures. Physical/OEM outputs, real camera/NFC hardware, other screen sizes, production alarm execution and the remaining reference boards are outside this record.


## Final Smart window thumb follow-up

[Organization run 41039108244](https://github.com/omeyerrato/SleepWell/actions/runs/41039108244) captured source `090be37da587f8b1756158f0bb0bf9e138df0c22`, including the Smart-only solid brown thumb change `f3aa37f7`. The existing single configuration journey passed in 61.372 seconds with all 18 required images present; [summary](final-smart-thumb-41039108244/focused-review-summary.json), [environment](final-smart-thumb-41039108244/environment.json) and [original runner log](final-smart-thumb-41039108244/SettingsOrganizationFlowTest.txt) are archived unchanged.

The final [following](final-smart-thumb-41039108244/organization-smart-following.png), [configured](final-smart-thumb-41039108244/organization-smart-configured.png) and [reopened](final-smart-thumb-41039108244/organization-smart-reopened.png) original PNGs were inspected at full size. All three show the solid brown window-length thumb from board 02/right. The inherited tracking/goal labels remain explicit, the prerequisite note fits above the footer, and the configured/reopened High sensitivity and goal-on states remain visible. [SHA-256 manifest](final-smart-thumb-41039108244/sha256.json) records the unchanged archived files. Earlier white-ring-thumb images above remain historical evidence and are superseded by these three images for this control detail.

This follow-up is limited to the single organization configuration journey and these three inspected Smart states. It does not repeat the other alarm/wake journeys, establish physical alarm/sound/torch/haptic behavior, validate other screen sizes, or mark the full 36-screen design set complete. No app code, build, CI run, phone action or output was executed as part of archiving this evidence.
