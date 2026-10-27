# Current requested priorities

## Current top priorities

- [ ] Match all36 reference screens. Sound events, Report spacing and Sleep goals have passing current-source native screenshots/actions; Appearance fits its controls, saves all three themes and passes full large-text action reach. [Screen-by-screen acceptance](UI-REFERENCE-ACCEPTANCE.md).
- [x] Complete Report calendar/edit verification: source514a5800, CI40477887525,64.406s; all11 original screenshots inspected. Real date changes, selected-day chart, notes/tags/awake edits, Save/reopen and Back-discard pass. This is functional closure, not all36 design acceptance. [Evidence](verification/2026-10-27-report-calendar-complete/README.md).
- [ ] Remaining interaction bugs and security review. Cancelled shortcut navigation, obscured authorization gestures and calendar-permission recovery now pass their three-method native scope at795ab556 (CI40517817995). All23 remaining exported components have a documented static review; broader interaction/runtime acceptance remains open. [Issue status](INTERACTION-SECURITY-REVIEW.md).

The installed phone remains32f064e2; it predates the passing Report fix and these new source changes. No further phone action is included in this status update.

Three implementation worktrees own the priorities. Additional breathing implementation is preserved on its branch and deferred. Runtime checks use isolated host-muted CI emulators; no new unit-test batch or personal-phone output test is part of this increment.

## New source and focused verification

- [x] Report calendar/edit flow and subsequent spacing/proportion corrections: 11 original captures, actual actions and Save/reopen pass. [Current Report evidence](verification/2026-10-27-report-proportions/README.md).
- [x] Real backup sound/volume settings and format14/legacy13 compatibility pass the same native journey with fake outputs; 10 originals inspected. The later progress-rail rendering fix still needs its final capture. [Snooze evidence](verification/2026-10-27-snooze-backup/README.md).
- [x] Rhythm and Nap changed main layouts/actions pass, with 13 individually inspected originals. [Evidence](verification/2026-10-27-rhythm-nap/README.md).
- [ ] Final acceptance of the new Gentle sample/rail and Health/privacy repairs. CI40546119570 exposed an interruption-notice loss, a Health input-handling race and a real cleanup-observation race; each has a narrow source fix. Follow-up is restricted to those repairs.
- [ ] All36 reference screens and broad interaction/security acceptance remain open. The phone remains32f064e2; latest published795ab556 is separate from these newer source increments. No physical alarms or phone outputs ran.
