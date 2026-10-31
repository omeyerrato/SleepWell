# Night Dial implementation progress

The full 43-flow design contract remains the target. Shared styling does not establish that every state is finished. No flow is accepted until actual native screens and interaction evidence cover its states, popups, cancellation, errors and return paths.

## Application-wide pass: 31 October 2026

The current implementation is `489f914c`, integrated with main `d1b954de`. All **127 manifest Activities are assigned to 43 flow families** in [SCREEN-COVERAGE.md](SCREEN-COVERAGE.md). The native Java View architecture and feature owners remain. This is a route inventory, not a claim that every hardware/provider branch is accepted.

The pass adds distinct native compositions for naps, guided breathing, media, diary and game workspaces; darkens secondary screens and dialogs; adds bounded natural typography/layout, source waveforms, requested-policy graphs, genuine measured charts, tactile surfaces and motion under the existing reduced-motion/haptic guards. All 28 task editors and 11 cognitive workspaces are in the current native review. No decorative fake sleep measurements are introduced.

Local current-source builds of app/test APKs and **84 focused unit methods pass**. The first broad native run at `f16f447d` passed 18/20 methods and preserved 175 actual PNGs. It exposed a genuine diary OS Back gap and an invalid task-editor launch fixture, both corrected in `489f914c`. Visual inspection also led to stacked long settings descriptions, corrected appearance samples, embossed Memory backs, disclosure-led wind-down controls and a configured stereo preview. The new exact-revision review is in progress; no APK from the failed f16 review is delivered.

A separate existing full feature-regression run at f16 exceeded its 25-minute budget. Its logs contain both obsolete cream/plum visual expectations and unresolved functional/setup assertions; it is **not a passing full regression**. No blanket assertion is made that these are all test-only failures. The broad local unit baseline remains non-green; the AlarmRequest null-unboxing case is now fixed and its four existing tests pass. See revision evidence for exact results rather than adding test counts from different revisions.

The user requested APK only. No phone installation or physical ADB session occurs in this pass.

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

## Alarm management and sheet geometry pass

- Skip reviews one named, dated occurrence and retains its review/attention sheet across recreation. Keep writes nothing; Apply changes the actual next occurrence; Undo restores it. Existing failed-scheduling Retry and expired one-time behavior are verified too.
- Duplicate opens an off, unsaved copy and preserves the source editor/draft underneath. It retains base configuration but clears skipped/moved occurrence authority. Only explicit Save creates the copy through the existing guarded owner.
- Delete names the saved alarm and preserves Cancel/recreation. Confirmation rejects an identity changed elsewhere before invoking the existing cancellation/delete operations. Dirty Back offers Save alarm/Keep editing/Discard; Save uses the existing validation and save owner.
- Visual review found that fixed dialog heights left a gap beneath short panels. SleepPicker.show now treats height as a cap and measures natural content; long lists retain scrolling. Selected corrected short, long-list, invalid-time and expired-alarm frames were inspected; native short-sheet assertions reject trailing viewport gaps.
- [6409f7a6](verification/6409f7a6/README.md) passed 80 focused unit methods and all 11 native methods in 274.602 seconds, with all 93 required captures. App/test compilation and the independent APK workflow passed. The 11 inherited broad-unit failures were not rerun here.
- Signed APK-only preview 6409f7a6 and its receipt are in Downloads, using the previous preview signer. Its app source matches the reviewed tree. No phone installation or physical ADB interaction occurred; the prior pinned APKs remain available.

## Flow matrix

The table distinguishes implementation coverage from verified interactions. Existing historical evidence remains valid only at its pinned revision; current integration uses a fresh combined native review. Shared dark surfaces alone do not prove every state.

| Board | Flow | Current status |
| --- | --- | --- |
| 01 | First night | First-night example dial, optional guide, real readiness cards and owned permission explanations implemented; four native first-night/readiness journeys passed at 0e887d5b and remain in combined review. |
| 02 | Plan tonight | Native composition, independent drag/save/cancel/conflict/recreation and explicit linked alarm review verified at 7d726b2b; extended accessibility/device review pending |
| 03 | Wind down and guard | Read/Breathe modes, paced contour, narration disclosure and app-selection checkboxes implemented; wind-down/guard roots captured at f16; live narration, guard enforcement and external app handoffs require device checks. |
| 04 | Create and manage alarms | Prior list/name/repeat review plus named Skip/Undo/stale state, off duplicate draft/Save, named Delete/Cancel/recreation/stale identity and dirty Back Save verified at 6409f7a6; Add-from-defaults and other entry/device branches pending |
| 05 | Dates and smart waking | Interval validation/correction, draft recreation and return verified at 37de8841; odd/even, anchor/date/exclusions, solar and other schedule branches pending |
| 06 | Tone and gentle wake | Requested delay/ramp curve, silent opening and draft recreation verified at 37de8841; source selection, physical output and other gentle-wake branches pending |
| 07 | Snooze, backup, input | Configured shortening bars, zero-snooze state, recreation and explicit Save verified at 37de8841; caps/choose-each/input/backup/wake-check branches pending |
| 08 | Build a wake routine | Routine ownership retained; all 28 task editor roots and 11 cognitive workspaces included in current review. Prior Memory configuration/reorder/parent Save journey retained. |
| 09 | Enroll morning codes | Dark QR enrollment, legible 220dp code and common owned sheets implemented; generated barcode/camera/NFC handoffs still require external-device validation. |
| 10 | Screen challenges I | Shared wake horizon and actual segmented completion; distinct cognitive canvases and controls implemented. Eleven actual workspaces captured at f16; Memory embossed back/reveal refined afterward. |
| 11 | Screen challenges II | Screen task families share dark workspaces and real completion state; all configuration roots included in current review. Full solving/error/timeout combinations are not all accepted. |
| 12 | Writing and light tasks | Physical task configuration and quiet practice styling integrated; hardware execution/permission-return tests remain required. |
| 13 | Movement challenges | Dark physical/sensor task shells, ordered progress and existing output owners retained; camera/light/movement validation requires hardware. |
| 14 | Camera challenges | Voice and audio task configuration uses dark fields/choices and owned dialogs; actual microphone permission/recognition/output branches require device validation. |
| 15 | Sound challenges | Audio practice/configuration uses dark native controls and genuine progress; physical playback, microphone and interruption branches require device validation. |
| 16 | Notes, camera light and fallback | Camera/light challenge shells use Night Dial chrome and controls; preview/enrollment/torch behavior remains with existing feature owners and requires device validation. |
| 17 | Alarm rings to dismissal | Alarm and wake-check dark compositions retain actual deadline/countdown and segmented task progress; live alarm delivery and overnight interruption reliability remain device tests. |
| 18 | Choose sounds and settle in | Guided contour, reading/breathing modes and narration disclosure implemented; root captured at f16, updated controls included in current review. |
| 19 | Advanced audio sources | Photographic sound shelf, actual PCM gain previews and configured stereo traces implemented; source-specific artwork and time/fade sheets retained. Opening stays silent; output requires device checks. |
| 20 | Start, pause and finish tracking | Media library and source controls use dark typography, native gain and owned choices; favorite/library root capture passed at f16; real picker/import/playback errors remain external branches. |
| 21 | Automatic tracking | Automatic/recurring start methods use readable radio/checkbox choices and dark schedule rows; roots captured at f16. Live triggers and Android restrictions remain device checks. |
| 22 | Sensors and calibration | Sonar/contactless settings and signal charts use dark surfaces and labeled source/provenance; physical calibration, acoustic output and long-night review remain device checks. |
| 23 | Room, awake and experimental signals | Awake/sensitivity settings and measured timelines retain source owners; dark roots captured at f16; actual capture/classification correctness is outside visual acceptance. |
| 24 | Sound analysis and recordings | Sound event/history/library surfaces use dark source-aware rows and actual provenance; library/favorites roots captured at f16. Import/playback and all archive recovery states remain external regression work. |
| 25 | Gentle cues and dream tools | Dream/snoring cue controls, history and consent use dark native presentation; both settings roots captured at f16. Output/recording authorization and physical effect remain existing owners. |
| 26 | Review a recorded night | Night summary, signal selection/scrub/recreation, dirty Back and annotation-preserving restore verified at fca53502; remaining provider, awake, optional-reset and device branches pending |
| 27 | History, manual records and import | Source filters, visible row durations/provenance and read-only navigation verified at fca53502; calendar, manual-create, import and extended filter flows pending |
| 28 | Trends, rhythm and context | Actual Week/Month/Year averages, coverage, range navigation, missing range and recreation verified at fca53502; rhythm/context/detail-selection and device branches pending |
| 29 | Sleep plan, bank and milestones | Plan/bank tactile invitation, actual saved goal and progress charts implemented; goal/bank roots captured at f16. Milestone/date edge cases need broader interaction evidence. |
| 30 | Naps and timers | Turnable duration wheel, exact accessible adjustment, selected presets and live end time implemented; cancel/recreation/a11y draft assertions passed at f16. In-place focus refinement included in current review. |
| 31 | Travel and calendar schedules | Actual travel clocks, timezone/plan cards, calendar rule rows and dark sheets integrated; travel/calendar roots captured at f16. Provider, DST and delivery branches remain existing feature checks. |
| 32 | Find and personalize | Night Dial appearance preview, saved-plan mark, dark lamp artwork, scalable text and motion/contrast controls implemented; legacy theme values retained. Dedicated save/cancel/relaunch review running. |
| 33 | Reliability and alarm protection | Actual readiness cards, dark protection/feedback settings and named recovery actions implemented; native first-night permission-return evidence retained; protection root captured at f16. OS-dependent enforcement remains device checks. |
| 34 | Health and calendar exchange | Health/calendar consent and exchange screens use common dark sheets and source-aware copy; Health Connect root captured at f16. Actual providers and destructive/import operations require dedicated integration checks. |
| 35 | Lights and automation | Lights/automation use existing actual state and configured previews with dark controls; light/Hue/automation roots captured at f16. Hardware/network delivery not asserted. |
| 36 | Local data, export and restore | Backup has a native step journey; exports/imports retain actual document pickers and confirmation owners with dark sheets. Backup/CSV/research roots captured at f16; real document-provider branches need integration checks. |
| 37 | Cloud versions, profiles and conflicts | Cloud/profile/version/conflict screens use dark source rows and owned confirmations; cloud/profile roots captured at f16. External accounts/providers and conflict operations require integration checks. |
| 38 | Imported signals, sharing and access | Read/export/access/automation screens use readable stacked descriptions and dark confirmations; access/incoming automation roots captured at f16. External callers/provider authorization remain feature tests. |
| 39 | Home shortcuts and system handoffs | Dark home widget and shortcut settings implemented; launcher root capture at f16 exposed an unsettled compositor frame and current capture now verifies final dark pixels. Actual launcher pinning is user-driven and untested on phone. |
| 40 | Recovery and details across flows | Skip stale-occurrence notice/recreation, scheduling failure/Retry/reopen, expired one-time state and stale Delete rejection verified at 6409f7a6; remaining cross-flow recovery pending |
| 41 | Tags, confirmation and custom values | Textured diary/editor and named deletion/dirty Back gate implemented. Real OS Back gap found at f16 and fixed for both legacy and predictive paths. Native draft/restore/cancel review included in current run; tag taxonomy branches need further regression evidence. |
| 42 | Physical challenge and permission branches | All 28 task configurations assigned and current-review targets; dark permission shells/owned dialogs integrated. Camera, NFC, light, voice and sensor execution require physical validation. |
| 43 | Empty, search and loading states | Intentional no-data views and clear entry actions retained; no fake metrics. Forty-five roots plus five enlarged/reduced-motion roots captured at f16; new screenshot settling rejects launcher/transition overlays. |

- Draft PR: https://github.com/omeyerrato/SleepWell/pull/7

## Delivery gates

1. Finish the combined exact-revision native run, preserve original PNGs/XML/raw results, and inspect changed screens/popups and large text.
2. Freeze a signed APK whose application source tree matches that reviewed revision; preserve prior pinned APKs and the signer; provide checksum and receipt.
3. Record unresolved broad regression/device/provider limits explicitly. Do not label a root capture as full state acceptance or a configured graph as a measured signal.
