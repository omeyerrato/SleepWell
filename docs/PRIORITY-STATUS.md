# Current requested priorities

## Current top priorities

- [ ] Match all36 reference screens. Current Report, Tone/Gentle, Snooze, QR, sleep plan, rhythm/Nap, Settings root and Appearance have bounded comparison/action evidence. Remaining screens, variants and full reference fidelity remain open. [Screen-by-screen acceptance](UI-REFERENCE-ACCEPTANCE.md).
- [x] Complete Report calendar/edit verification. Current selected Report method passes55.975s/11 inspected originals, including real calendar/day changes, notes/tags/awake edits, Save/reopen and Back-discard. [Evidence](verification/2026-10-27-report-proportions/README.md).
- [ ] Remaining interaction bugs and security review. Concrete shortcut/touch/calendar/QR/NFC/Health/Privacy/document/Cloud fixes are merged; their selected native scopes pass. Readiness recovery and additional Tracking work are a separate source increment awaiting native acceptance. Broader interaction, provider, hostile-overlay and full security acceptance remain open.

The installed phone remains32f064e2; it predates the passing Report fix and these new source changes. No further phone action is included in this status update.

Three implementation worktrees own the priorities. Additional breathing implementation is preserved on its branch and deferred. Runtime checks use isolated host-muted CI emulators; no new unit-test batch or personal-phone output test is part of this increment.

## New source and focused verification

- [x] Report calendar/edit and proportion corrections; actual actions, Save/reopen and eleven inspected captures.
- [x] Real backup tone/volume, format14 with legacy imports and correct progress rails; same native journey plus fake-output policy assertions.
- [x] Gentle quiet ramp sample, Tone selection/import/preview controls and compact QR enrollment; real configuration/persistence with injected outputs.
- [x] Rhythm/Nap layout/actions, Health permission recovery, Privacy deletion/cleanup recovery and Cloud folder rotation pass their named private native flows.
- [ ] All36 reference states and broader interaction/security acceptance remain open. Source-only Tracking/readiness refinements require their focused native capture checks next.

Published170b0e44 is separate from installed32f064e2. No personal-phone or physical output action ran. [Current release and exact evidence](verification/2026-10-27-reference-menus-build/README.md).
