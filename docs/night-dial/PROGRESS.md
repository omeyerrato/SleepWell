# Night Dial implementation progress

The full 43-flow design contract remains the target. Shared styling does not establish that every state is finished. No flow is accepted until actual native screens and interaction evidence cover its states, popups, cancellation, errors and return paths.

## Current evidence

- Isolated branch starts at `669ec8f6`; main feature checkout is untouched.
- Native Java Views retained; no framework/dependency migration.
- Shared tokens, flat navigation, white pill actions, amber switches/sliders, press/focus surfaces and bottom-origin picker motion added.
- 24-hour planned interval dial and a source-specific Rain sound card added to Tonight.
- Existing haptic authorization and quiet-context guards remain in `InteractionFeedback`.
- Baseline duplicate TrackingActivity focus override repaired so compilation can proceed.
- Local Java compilation passed. Unit run: 1,063 tests, 11 failures in AlarmRequestTest, QrScanSessionTest, SonarNightTest, SonarSharedNightTest, TrackingDelayEntryTest and TrackingDelayTest. A separate archive of baseline 669ec8f6 reproduced all 11 named failures in 44 tests; the suite remains non-green.
- APK and test-APK builds passed locally and in GitHub at 37155aed. The native review passed at 359fd614 with eight focused frames, invalid-input/cancel/recreation assertions, no playback/capture, and unchanged saved fixture values; see verification/README.md. This remains narrow evidence, not full app acceptance.
- Three focused NightDialContrastTest methods passed for text/action/control contrast pairs.
- Local AVD creation succeeded; boot exited because the host lacks the emulator hypervisor driver. At the user's explicit request, revision 1b1bf6e5 was installed as an in-place phone update, with a private backup and verification that existing database values were retained. Subsequent UI review runs use disposable CI emulators.

## Interactive pass

- Tonight now has independent moon/sun drag targets, five-minute snapping, live draft duration/time feedback and guarded tick haptics. Cancelled pointer sequences do not write. Exact entry is available through named accessibility actions and the review screen.
- Releasing a changed handle opens an owned plan review. Use plan retains the existing revision-checked save/reminder owner. Rotation retains the pending interval; stale saves require reloading. Wake-up alarm changes have a separate review and explicit Save alarm step.
- Settle in uses actual bundled PCM waveform previews, native volume sliders, the saved timer/fade editor and existing microphone-consent flow. Opening the screen is silent. Waveform height follows the chosen volume; it does not simulate microphone input.
- Active tracking now uses a subdued clock dial, actual recorded duration, a compact sensor state and the existing measured movement timeline. Future clock time is grey; elapsed clock time is muted amber. Paused time remains excluded by the existing coordinator.
- Java/test compilation and 24 focused unit tests passed during development. New native gesture/recreation/conflict/consent evidence is pending CI; this is not whole-app acceptance.

## Flow matrix

| Board | Flow | Current status |
| --- | --- | --- |
| 01 | First night | Native design and full interaction review pending |
| 02 | Plan tonight | Partial native composition; first viewport and linked picker journey verified at 359fd614 |
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

- Draft PR: https://github.com/omeyerrato/SleepWell/pull/7

## Next work

1. Continue from the passing 359fd614 native captures. Recheck future-date captions and illustration-off compact cards; verify Tonight scroll restoration and the existing weekday/alarm-control flow after moving optional rows.
2. Complete Tonight alarm summary, linked dial editing and first-viewport composition while preserving optional sections.
3. Complete Settle in and active/paused/end tracking compositions using real runtime state and the existing capture owners.
4. Replace stale appearance/theme previews with the Night Dial preview while retaining saved legacy values.
5. Review alarm editor, challenge catalog and all 28 task surfaces; keep challenge interaction and scheduling logic unchanged.
6. Complete report/history/chart states and source/missing-data language.
7. Review every remaining settings/editor sheet, confirmation, popup, keyboard and permission-return state against the 43-flow matrix.
8. Verify compact/large widths, enlarged fonts, landscape, TalkBack focus/order, reduced motion, keyboard navigation, press cancellation, haptic quiet guards and lifecycle cleanup.
9. Rebase/integrate against the latest feature branch in a separate review step, resolve adapters narrowly, and record precise validation.
10. Publish the final reviewable PR and handoff with native captures, known limitations and integration instructions. Keep it draft until the whole objective is verified.
