# Current requested priorities

## Current top priorities

- [ ] Match all36 reference screens. Sound events, Report spacing and Sleep goals have passing current-source native screenshots/actions; Appearance fits its controls, saves all three themes and passes full large-text action reach. [Screen-by-screen acceptance](UI-REFERENCE-ACCEPTANCE.md).
- [x] Complete Report calendar/edit verification: source514a5800, CI40477887525,64.406s; all11 original screenshots inspected. Real date changes, selected-day chart, notes/tags/awake edits, Save/reopen and Back-discard pass. This is functional closure, not all36 design acceptance. [Evidence](verification/2026-10-27-report-calendar-complete/README.md).
- [ ] Remaining interaction bugs and security review. Cancelled shortcut navigation, obscured authorization gestures and calendar-permission recovery now pass their three-method native scope at795ab556 (CI40517817995). All23 remaining exported components have a documented static review; broader interaction/runtime acceptance remains open. [Issue status](INTERACTION-SECURITY-REVIEW.md).

The installed phone remains32f064e2; it predates the passing Report fix and these new source changes. No further phone action is included in this status update.

Three implementation worktrees own the priorities. Additional breathing implementation is preserved on its branch and deferred. Runtime checks use isolated host-muted CI emulators; no new unit-test batch or personal-phone output test is part of this increment.
