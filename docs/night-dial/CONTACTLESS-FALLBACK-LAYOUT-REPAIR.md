# Contactless fallback and focused destination capture

Actual run41260549703 failed the existing full-control geometry check: Microphone was clipped to one pixel after the new third fixed-footer action reduced the reading viewport. Its original PNG confirms Headphones at the bottom and Microphone obscured. Use phone movement now remains an ordinary scroll-content action beneath Microphone, while the existing Test/Stop and Use sonar actions retain the fixed footer. The movement journey already scrolls to that real action. No target or geometry assertion was removed.

The original also exposed old color aliases rendering the Back glyph and result heading black on the Night Dial dark surface; both use semantic TEXT. Disabled Use sonar keeps a visible secondary label. No audio qualification, permissions, cleanup, result or scheduling logic changed.

The same run’s breadth capture used any focused global window while a new Activity was entering, then threw NoSuchElementException when focus moved. It now waits for a focused, attached, laid-out window belonging to the exact expected Activity (including its owned dialogs) before frame capture. Dark-surface, unchanged-store, quiet-owner and voice-settings assertions remain. These are pending rerun, not accepted screenshots.

## Current capture classifier correction

Run41262356510 confirms the repaired access rows, Back, result heading and fixed actions in the actual original, but its legacy per-glyph test classified the Night Dial black background itself as dark glyph ink. For example the clock had538 light pixels and3705 background-black pixels; the visibly white Wi-Fi/mobile/battery and notification glyphs failed the same old ratio. The previous plum background did not enter that near-black counter.

The per-glyph classifier now excludes pixels matching the semantic Night Dial ground (12 RGB tolerance), then still requires24 light pixels and light ink greater than twice non-background dark foreground for EACH real SystemUI glyph region on two compositor samples and in the saved original. The complete dark-bar/background and actual light-glyph flag/current-window assertions remain. No production flags, image bytes, output ports or failed screenshots were changed. This corrects the foreground/background definition; it is not a claimed product rendering repair or a passing rerun.

## Error-state layout buffer

The later41265653342 original passed the light-glyph test and the initial/cancel/recreation/charging states, then a longer prerequisite message clipped one physical pixel from the Microphone target (125 visible of126). The target must keep its complete48dp area. The placement artwork now uses150dp rather than170dp, reserving20dp for real result wrapping while retaining the artwork, full signal chart, status rows, scrolling movement alternative and fixed Test/Use controls. The strict full-height/48dp assertions remain unchanged. This is a measured layout correction, pending its rerun.
