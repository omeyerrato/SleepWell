# Contactless fallback and focused destination capture

Actual run41260549703 failed the existing full-control geometry check: Microphone was clipped to one pixel after the new third fixed-footer action reduced the reading viewport. Its original PNG confirms Headphones at the bottom and Microphone obscured. Use phone movement now remains an ordinary scroll-content action beneath Microphone, while the existing Test/Stop and Use sonar actions retain the fixed footer. The movement journey already scrolls to that real action. No target or geometry assertion was removed.

The original also exposed old color aliases rendering the Back glyph and result heading black on the Night Dial dark surface; both use semantic TEXT. Disabled Use sonar keeps a visible secondary label. No audio qualification, permissions, cleanup, result or scheduling logic changed.

The same run’s breadth capture used any focused global window while a new Activity was entering, then threw NoSuchElementException when focus moved. It now waits for a focused, attached, laid-out window belonging to the exact expected Activity (including its owned dialogs) before frame capture. Dark-surface, unchanged-store, quiet-owner and voice-settings assertions remain. These are pending rerun, not accepted screenshots.
