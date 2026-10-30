# Night Dial implementation progress

The full 43-flow design contract remains the target. Shared styling does not establish that every state is finished. No flow is accepted until actual native screens and interaction evidence cover its states, popups, cancellation, errors and return paths.

## Current evidence

- Isolated branch starts at `669ec8f6`; main feature checkout is untouched.
- Native Java Views retained; no framework/dependency migration.
- Shared tokens, flat navigation, white pill actions, amber switches/sliders, press/focus surfaces and bottom-origin picker motion added.
- 24-hour planned interval dial and a source-specific Rain sound card added to Tonight.
- Existing haptic authorization and quiet-context guards remain in `InteractionFeedback`.
- Baseline duplicate TrackingActivity focus override repaired so compilation can proceed.
- Local Java compilation passed. Unit run: 1,063 tests, 11 failures in AlarmRequestTest, QrScanSessionTest, SonarNightTest, SonarSharedNightTest, TrackingDelayEntryTest and TrackingDelayTest. Baseline comparison is pending; do not claim a green unit suite.
- Fresh APK/test-APK build and native GitHub review are pending.
- Local AVD creation succeeded; boot exited because the host lacks the emulator hypervisor driver. No physical-device installation was attempted.

## Flow matrix

| Board | Flow | Current status |
| --- | --- | --- |
| 01 | First night | Native design and full interaction review pending |
| 02 | Plan tonight | Partial native composition; visual review pending |
| 03 | Wind down and guard | Native design and full interaction review pending |
| 04 | Create and manage alarms | Native design and full interaction review pending |
| 05 | Dates and smart waking | Native design and full interaction review pending |
| 06 | Tone and gentle wake | Native design and full interaction review pending |
| 07 | Snooze, backup, input | Native design and full interaction review pending |
| 08 | Build a wake routine | Native design and full interaction review pending |
| 09 | Enroll morning codes | Native design and full interaction review pending |
| 10 | Screen challenges I | Native design and full interaction review pending |
| 11 | Screen challenges II | Native design and full interaction review pending |
| 12 | Writing and light tasks | Native design and full interaction review pending |
| 13 | Movement challenges | Native design and full interaction review pending |
| 14 | Camera challenges | Native design and full interaction review pending |
| 15 | Sound challenges | Native design and full interaction review pending |
| 16 | Notes, camera light and fallback | Native design and full interaction review pending |
| 17 | Alarm rings to dismissal | Native design and full interaction review pending |
| 18 | Choose sounds and settle in | Shared controls updated; Settle in composition pending |
| 19 | Advanced audio sources | Native design and full interaction review pending |
| 20 | Start, pause and finish tracking | Native design and full interaction review pending |
| 21 | Automatic tracking | Native design and full interaction review pending |
| 22 | Sensors and calibration | Native design and full interaction review pending |
| 23 | Room, awake and experimental signals | Native design and full interaction review pending |
| 24 | Sound analysis and recordings | Native design and full interaction review pending |
| 25 | Gentle cues and dream tools | Native design and full interaction review pending |
| 26 | Review a recorded night | Native design and full interaction review pending |
| 27 | History, manual records and import | Native design and full interaction review pending |
| 28 | Trends, rhythm and context | Native design and full interaction review pending |
| 29 | Sleep plan, bank and milestones | Native design and full interaction review pending |
| 30 | Naps and timers | Native design and full interaction review pending |
| 31 | Travel and calendar schedules | Native design and full interaction review pending |
| 32 | Find and personalize | Shared appearance tokens only; editor/preview revision pending |
| 33 | Reliability and alarm protection | Native design and full interaction review pending |
| 34 | Health and calendar exchange | Native design and full interaction review pending |
| 35 | Lights and automation | Native design and full interaction review pending |
| 36 | Local data, export and restore | Native design and full interaction review pending |
| 37 | Cloud versions, profiles and conflicts | Native design and full interaction review pending |
| 38 | Imported signals, sharing and access | Native design and full interaction review pending |
| 39 | Home shortcuts and system handoffs | Native design and full interaction review pending |
| 40 | Recovery and details across flows | Native design and full interaction review pending |
| 41 | Tags, confirmation and custom values | Native design and full interaction review pending |
| 42 | Physical challenge and permission branches | Native design and full interaction review pending |
| 43 | Empty, search and loading states | Native design and full interaction review pending |

## Next work

1. Obtain and inspect the first actual Android captures; fix geometry/readability before broad propagation.
2. Complete Tonight alarm summary, linked dial editing and first-viewport composition while preserving optional sections.
3. Complete Settle in and active/paused/end tracking compositions using real runtime state and the existing capture owners.
4. Replace stale appearance/theme previews with the Night Dial preview while retaining saved legacy values.
5. Review alarm editor, challenge catalog and all 28 task surfaces; keep challenge interaction and scheduling logic unchanged.
6. Complete report/history/chart states and source/missing-data language.
7. Review every remaining settings/editor sheet, confirmation, popup, keyboard and permission-return state against the 43-flow matrix.
8. Verify compact/large widths, enlarged fonts, landscape, TalkBack focus/order, reduced motion, keyboard navigation, press cancellation, haptic quiet guards and lifecycle cleanup.
9. Rebase/integrate against the latest feature branch in a separate review step, resolve adapters narrowly, and record precise validation.
10. Publish the final reviewable PR and handoff with native captures, known limitations and integration instructions. Keep it draft until the whole objective is verified.
