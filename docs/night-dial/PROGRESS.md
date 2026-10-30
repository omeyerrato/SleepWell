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
- Java/test compilation, 25 focused unit tests and three native journeys passed at 7d726b2b. All 26 required native frames exist, including an actual pointer-cancel sequence, nonzero excluded paused time, separated consent actions and restored consent/End confirmations. A signed local preview APK is prepared; the user requested APK-only delivery, so the phone was not changed. Full-app acceptance remains open.

## Report and APK preview pass

- Report opens the latest saved night with a duration-against-goal ring, actual dated interval and source. History rows open read-only reports; Edit is explicit. Signal details select one measured metric and retain its inspected timestamp across recreation. Missing signals stay unavailable.
- History now has source filters and visible durations/source labels. Trends has a zero duration baseline, visible range navigation, current goal, observed-day coverage and honest empty ranges. Supporting signal destinations stack title and explanation.
- Unsaved record edits have an owned Back/discard prompt. Restore original times is a reviewed draft that keeps notes, tags and rating by default; only Save changes commits it through the existing owner.
- Native review fca53502 passed 59 focused unit methods and all five native methods, with all 42 required frames. Initial screenshot defects and intermediate failed test expectations are retained in the verification notes. This remains narrow progress, not full-app acceptance.
- APK-only preview c1d4a75f is saved locally and in Downloads with the previous preview signer. Its application-source tree matches reviewed fca53502 exactly. No phone installation or ADB interaction occurred this pass. See verification/fca53502/README.md and the adjacent local build receipt for exact scope/checksum.

## Alarm draft and configuration preview pass

- Alarm cards now show the saved routine and actual dated next occurrence, ordered by the admitted next occurrence. Name/value taps reach their owning rows. Name drafts, selection and the dirty Back confirmation survive recreation; Discard leaves the stored alarm unchanged.
- Repeat is directly reachable from the editor. Invalid interval input survives recreation without saving. Correcting it clears the stale error immediately without replacing the field or losing the caret; interval weekdays remain visibly inactive.
- Gentle wake draws the requested policy, including sound delay and ramp, with explicit time/percent axes. Snooze bars show configured policy durations, including shortening and zero snoozes. These are configuration previews, not measured output.
- The challenge catalog shows its actual filtered count. The Memory configuration, ordered routine and parent alarm draft retain their existing owners. Only explicit Save alarm commits the reviewed routine/settings and reaches scheduling.
- Native review 37de8841 passed all seven native methods and 80 focused unit methods; all 64 required frames were preserved and key states visually inspected. The independent APK workflow passed. The previous broad-suite failures remain open and were not rerun in this focused pass.
- Signed APK-only preview 37de8841 is prepared in Downloads, with receipt/checksum and the same preview signer. No phone installation or physical ADB interaction occurred. See [exact review scope](verification/37de8841/README.md).

## Flow matrix

The next alarm-management revision is under review: an owned named Skip sheet (including error restoration), off/unsaved Duplicate, named Delete with Cancel/recreation and stale-identity rejection, and Save from the dirty-exit sheet. These changes are not included in the delivered 37de8841 APK and do not gain verified status until their own native review passes.

| Board | Flow | Current status |
| --- | --- | --- |
| 01 | First night | Native design and full interaction review pending |
| 02 | Plan tonight | Native composition, independent drag/save/cancel/conflict/recreation and explicit linked alarm review verified at 7d726b2b; extended accessibility/device review pending |
| 03 | Wind down and guard | Native design and full interaction review pending |
| 04 | Create and manage alarms | Actual next-occurrence list, name draft/recreation, dirty Back/Keep/Discard and explicit Save verified at 37de8841; create/delete/duplicate/skip and other entry branches pending |
| 05 | Dates and smart waking | Interval validation/correction, draft recreation and return verified at 37de8841; odd/even, anchor/date/exclusions, solar and other schedule branches pending |
| 06 | Tone and gentle wake | Requested delay/ramp curve, silent opening and draft recreation verified at 37de8841; source selection, physical output and other gentle-wake branches pending |
| 07 | Snooze, backup, input | Configured shortening bars, zero-snooze state, recreation and explicit Save verified at 37de8841; caps/choose-each/input/backup/wake-check branches pending |
| 08 | Build a wake routine | Memory search/configure/add, move-before-Math, draft-only routine Save and explicit parent Save verified at 37de8841; random/practice/all 28 task surfaces pending |
| 09 | Enroll morning codes | Native design and full interaction review pending |
| 10 | Screen challenges I | Native design and full interaction review pending |
| 11 | Screen challenges II | Native design and full interaction review pending |
| 12 | Writing and light tasks | Native design and full interaction review pending |
| 13 | Movement challenges | Native design and full interaction review pending |
| 14 | Camera challenges | Native design and full interaction review pending |
| 15 | Sound challenges | Native design and full interaction review pending |
| 16 | Notes, camera light and fallback | Native design and full interaction review pending |
| 17 | Alarm rings to dismissal | Native design and full interaction review pending |
| 18 | Choose sounds and settle in | Native source waveforms and mixer composition verified at 7d726b2b, with silent opening, timer draft recreation and microphone-consent Cancel; playback-output review pending |
| 19 | Advanced audio sources | Native design and full interaction review pending |
| 20 | Start, pause and finish tracking | Native active/paused/resumed/end-confirmation compositions and lifecycle verified at 7d726b2b with synthetic PCM + real coordinator/SQLite; real overnight/device review pending |
| 21 | Automatic tracking | Native design and full interaction review pending |
| 22 | Sensors and calibration | Native design and full interaction review pending |
| 23 | Room, awake and experimental signals | Native design and full interaction review pending |
| 24 | Sound analysis and recordings | Native design and full interaction review pending |
| 25 | Gentle cues and dream tools | Native design and full interaction review pending |
| 26 | Review a recorded night | Night summary, signal selection/scrub/recreation, dirty Back and annotation-preserving restore verified at fca53502; remaining provider, awake, optional-reset and device branches pending |
| 27 | History, manual records and import | Source filters, visible row durations/provenance and read-only navigation verified at fca53502; calendar, manual-create, import and extended filter flows pending |
| 28 | Trends, rhythm and context | Actual Week/Month/Year averages, coverage, range navigation, missing range and recreation verified at fca53502; rhythm/context/detail-selection and device branches pending |
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

1. Continue from the passing 37de8841 native captures (64 required frames, seven native methods). Review remaining alarm menus, deletion/duplication/skip confirmations and alternate schedule/source/input branches. Keep exact capture revisions in verification/README.md.
2. Extend Tonight editing evidence to interrupted pointer sequences, non-daily/solar/override alarms and accessible exact entry while retaining independent saved-plan and alarm owners.
3. Review Settle in playback/gain/error states and active/paused/end tracking with physical devices, additional sensors, long recordings and missing-data cases. Preserve the existing capture owners.
4. Replace stale appearance/theme previews with the Night Dial preview while retaining saved legacy values.
5. Extend the reviewed name/repeat/routine/gentle/snooze branches to random/practice modes and all 28 task surfaces; keep challenge interaction and scheduling logic unchanged.
6. Complete report provider/CSV/calendar/manual/filter/awake/optional-reset branches and trend detail selection/rhythm/context, retaining actual source/missing-data language.
7. Review every remaining settings/editor sheet, confirmation, popup, keyboard and permission-return state against the 43-flow matrix.
8. Verify compact/large widths, enlarged fonts, landscape, TalkBack focus/order, reduced motion, keyboard navigation, press cancellation, haptic quiet guards and lifecycle cleanup.
9. Rebase/integrate against the latest feature branch in a separate review step, resolve adapters narrowly, and record precise validation.
10. Publish the final reviewable PR and handoff with native captures, known limitations and integration instructions. Keep it draft until the whole objective is verified.
