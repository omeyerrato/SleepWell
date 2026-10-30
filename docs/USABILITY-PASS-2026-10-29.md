# Usability implementation pass — 29 October

Current instruction: make the app usable first. On30October the user resumed focused silent emulator comparisons and safe main-action checks. Security review, a broad/unit-test campaign, unrelated bug sweeps and phone/output operations remain deferred. This document records source increments and their source-specific bounded evidence, not acceptance of all36 references. Earlier dated entries below retain the authorization that applied at the time.

Three implementation agents worked in separate worktrees. The main checkout integrated their changes with the shared Settings and tracking navigation work. Package source: `4e47ab35`, version **0.1.9 / 10**. Packaging runs `:app:assembleDebug` only. The phone remains untouched.

The APK packaged successfully in3m47s and is [published as a development prerelease](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.9-usability-20261029-4e47ab35). No new acceptance or review jobs ran.

## Reference-led correction release —30October,0.1.23

Published frozen app source **`023c5fb752be96070cbab28c0386d131c83c70b4`**, version **0.1.23 /24**. [Development APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.23-ui-reference-20261030-023c5fb7). `:app:assembleDebug` passed in7m7s; APK SHA-256 `7d498517640b5fa0d088effe3a4cef9aa960e515d7a99ed94e36ad007ccd13c6`. This is packaging plus source-specific focused silent native evidence, not all36 acceptance.

- Report `13630bfc`, `eeb0f88c`, `421b857a`: remove the redundant rail, preserve measured ribbon continuity/gaps, recover chart/history space and correct Tags/goal units. The renewed calendar/chart/trim/cancel/recreate/Save/reopen flow passes69.636s with18 original captures at70bca1ec /41017261604.
- Tonight `6143f568`: measure the weekday gutter before layout; seven full circles/48dp targets fit.18 originals exercise actual Off/day persistence and moving clock-plan progress; initial SystemUI tint transition remains open.
- Alarms `5462134a`, `d2ca3576`, `f3aa37f7`: readable Set up actions, consistent sound/repeat glyphs, recovered Smart content space and selected brown thumb. Existing organization/configuration and shared safe Math/wake-check cohorts pass with fake outputs. Full ringing AlarmActivity is unaccepted.
- Settings `6dd63393`, `f9b7a226`, `f0de1ef7`, `fd090f41`: full lamp controls/large text, official Health Connect branding, warm automation confirmation, pinned readiness actions and new sunrise-bedroom art. Current settings and private manual-export journeys pass; combined privacy cleanup/real provider durability remains open.
- Tracking `371c5b9e`, `14fb6ff1`: full-width Automatic heading/named Options, existing correct lifecycle callback retained after duplicate callback was removed. Current bounded setup/Room/bedtime/sonar/live coordinator journeys pass with fake ports; no physical sensor or overnight run.
- Audio `70bca1ec`, `a20902af`, `d68d0e83`: soft controls, full event timestamps and aligned waveform/progress, flat illustrated Rain/Ocean/Fireplace/Wind assets. Mixer14 and events9 originals are individually compared; settings/recreation and synthetic-WAV/fake-playback persistence pass.

Three implementation agents worked in separate worktrees. [Current36-screen inventory](UI-REFERENCE-ACCEPTANCE.md) links exact run/source/originals and remaining visual states. No broad/unit-test campaign, security review or phone/real alarm/audio/vibration/torch output ran. No claim of phone installation, sound-model accuracy, new phone Deep/REM algorithm, real provider exchange or full feature parity.

Raster artwork was made with built-in imagegen; exact prompts are in [sound artwork](SLEEP-SOUND-ART-ASSETS.md) and [sunrise bedroom](SUNRISE-BEDROOM-ASSET.md).

## Main changes

- **Settings:** the eight primary destinations follow the reference hierarchy. Sound analysis and Bedtime guard open directly. Secondary tracking, bedtime, integration and home-screen options have named sections. Recording threshold has its own page. Alarm readiness contains permission/device setup; the smart wake-up default lives under Alarms.
- **Report:** visible Night / History / Trends navigation replaces discovery through overflow menus. Supporting actions are grouped. Editor time/tag dialogs use the warm app styling. History/editor ribbons retain measured bins and gaps. Chart selections, time axes, duration goals and unavailable-stage explanations are clearer.
- **Alarms:** four primary settings precede a compact More entry. Advanced options are grouped by purpose. The task catalog has All / Think / Move / Camera / Voice / Codes filters alongside search. Sound, Gentle and Snooze have labelled Options controls. Gentle wake-up exposes the actual Flashlight toggle. Code enrollment has a larger preview and QR/Barcode labels. Wake-task instructions are shorter.
- **Tonight and tracking:** weekdays align under the alarm label. Clock progress has a visible meaning; its endpoint badges use the existing raster icons. Tracking options are labelled, sensor choices use matching lilac controls, and battery compatibility explanations sit behind explicit information rows. Sonar and room charts show their real scales and observations; gaps remain blank.
- **Bedtime, naps and travel:** bedtime schedule and app selection have explicit entry points. Nap options collect secondary tools. Trip plan is a visible action.
- **Audio:** playback keeps a visible Stop action, event tools are grouped, category selections use radio semantics, and the snoring preview has a full-row action. Analysis and playback rules are retained.
- **Privacy and integrations:** main actions follow the reference grouping. Export/navigation menus use action rows instead of selection-style lists. Appearance routes have matching labels/icons. Health Connect and automation setup use clearer headings and choices. Existing recording-only cleanup and persistent export-receipt source is integrated.

## Command and discovery follow-up

Published package source: `44cb41e7`, version **0.1.10 / 11**. [Development APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.10-clearer-actions-20261029-44cb41e7), APK packaging3m15s only. This adds:

- Five compact search shortcuts: Extra loud, Automatic tracking, Snoring cues, Sleep goal and Flashlight. They use the existing search entries and retain the alarm chooser for alarm-specific settings.
- Separate command rows for record/audio tools, playlist reorder/remove, radio station actions, CSV interval details, backup reset and playlist additions. Genuine saved-value selections keep their radio controls.
- Grouped record-editor tools with scroll restoration. Task setup rows say Set up, and their final actions distinguish adding a new task from saving an existing one.
- Warm Inter styling for app-owned alarm, record, microphone and guard confirmations, preserving the existing buttons and callbacks.

This follow-up also runs APK packaging only. It does not add new verification or acceptance claims.

Source `6d56b05b` replaces Appearance's platform theme radio indicators with app-owned lilac rings. Existing tile selection and saving paths remain. It is included in0.1.11, after the frozen0.1.10 package.

## Pickers and feedback — 30 October

Published source **`ce2cb571`**, version **0.1.11 / 12**. [Development APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.11-pickers-feedback-20261030-ce2cb571). APK packaging2m24s only.

- Duration preset chips edit the same draft fields as typing; selected chips follow valid typed/restored values. Existing Use, Cancel and bounds remain. Exact-timer presets are1/5/10/20/30/90minutes; generic duration presets respect the current minimum/maximum.
- Travel-zone choices put the city first and pin the current exact zone. Country metadata comes from Android ICU, falling back to the actual zone ID. Search supports cities/countries/regions/IDs, while Show more keeps further choices reachable. Saved zones, trip timing and reminders are unchanged.
- Feedback strength has saved Light / Standard / Firm choices and an explicit Try feedback button. Standard retains the old behavior. All requests retain Android settings, saved Off, quiet/lifecycle guards and debounce. No physical feel is claimed.
- Search category labels, icons and breadcrumbs reflect the reorganized Settings hierarchy. Appearance rings are included.

No new tests, lint, visual verification, security review or physical outputs ran. Implementation references and deferred status are recorded in F035/F064/F169 of the feature matrix.

## Readiness control — 0.1.12

`9822fc65` adds the reference's explicit quiet-alarm review/scheduling control. A fresh one-question Math alarm is scheduled only after confirmation; the requested level is capped at25%, with no boosted sound, vibration, torch or screen sunrise. The review states that completing Math stops it; no automatic-stop claim is made. Permission/busy/error/request-note states are included, and ordinary saved alarm configurations are not edited.

The initial candidate `6cfcc62e` packaged successfully in1m48s but was held and never published or executed. It was superseded by **`031d44e5`**, [published as0.1.12](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.12-readiness-20261030-031d44e5), APK packaging1m3s only. Positive real alarm occurrences replace an active id-1 quiet test through the existing occurrence handoff; queued quiet delivery yields to active alarms, tracking and wake-up checks. Ordinary alarm-overlap behavior and lifetime rules remain. This flow has no new runtime or visual acceptance and was never executed.

## Report and bedtime — 0.1.13

Published source **`f11cc1cf`**, version **0.1.13 / 14**. [Development APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.13-report-bedtime-20261030-f11cc1cf). APK packaging1m55s only.

- Routine sound-analysis and clip-count details move to the existing About path. Errors and unavailable-playback explanations remain on the main Report. No data or analysis rules change.
- The guard picker searches real app names and package IDs. Clearing/filtering retains the owned selection; query/draft rotation state is saved, Cancel discards the picker copy, Use updates the outer draft and Save keeps its existing persistence behavior.
- The guard interruption screen follows the Wind-down visual vocabulary and uses the existing night illustration. Open Wind-down opens that exact route without starting tracking. Guard settings, Pause for5minutes and Home remain available. Visible status reads the saved schedule/pause/access state.

No new tests, lint, screenshot verification, security review, device or output actions ran. F042 links the guard source; the full36 acceptance remains open.

## Defaults and data tools — 0.1.14

Published source **`701747b3`**, version **0.1.14 / 15**. [Development APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.14-defaults-data-20261030-701747b3). APK packaging1m39s only.

- Future-alarm defaults have an actual edit/review/save flow. Template mode omits identity, time, repeat, enable and scheduling controls. It returns behavior values to the parent's explicit Save defaults action; existing alarms remain independent.
- Backup has two primary actions, Export and Import. Other data tools remain in a named group below.
- Portable settings selection is concise, with named category/value review cards and details behind About. Apply prerequisites, recovery, status and errors remain visible. Backend and file formats are unchanged.
- Sound analysis names the next-recording scope and offers the app-settings route when selected microphone features lack access.

No new tests, lint, screenshot verification, security review, device or output actions ran. This records implementation and packaging, not all36 acceptance.

## Clearer everyday and supporting flows — 0.1.15

Published source **`a8aa491f`**, version **0.1.15 / 16**. [Development APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.15-clearer-flows-20261030-a8aa491f). APK packaging only.

- History exposes the active date filter, change/clear actions, selection states and useful empty-state routes. Trends adds previous/next week/month/year navigation and a date chooser; empty ranges lead to actual records or manual entry. Data and chart mathematics are unchanged.
- Repeat has an editable interval stepper and immediate occurrence preview. Next-alarm-only emphasizes date/time, Review and Use. Skipped dates show selection counts and clear Add/Remove actions. Detailed ISO/time-zone/date rules remain under Options; scheduling policies are unchanged.
- Do Not Disturb, Flip to pause and Start delay keep their choices and current status prominent, with longer explanations in About. Save actions retain their existing jobs and no service is started by these presentation changes.
- Automation groups rule, connection, optional authentication and activation. Incoming requests and provider access have clearer setup steps and prerequisites, with copyable actual event names/endpoints. Existing consent, grant/revoke, request, persistence and delivery callbacks remain.

Implementation commits: `6d0b3433`, `6eddf14f`, `97b37481`, `3b4530e4`, `23232c83`. No tests, lint, screenshot verification, security review, phone or output operations ran. All36 acceptance and external-service delivery remain open.

## Reference controls and charts — 0.1.16

Published source **`32d207cc`**, version **0.1.16 / 17**. [Development APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.16-reference-controls-20261030-32d207cc). APK packaging only.

- Alarm editor: larger Inter clock, circular weekday visuals inside existing touch targets, bounded Name/value columns, aligned core icons/rules and a bell/switch Alarm on card. Dropdowns use consistent pills; Backup sound signals a value picker. Use wake-up/snooze settings returns the existing draft; final Save alarm retains its role.
- Report charts: compact selected-time capsules with separate actual-value captions. Trends has a selected-date column/guide, dated month/year readouts, clearer weekly labels and a visible goal rule. Real provider-reported stage strips are compact with consistent actual-category legend order. No stages, points or missing samples are invented; data and calculations are unchanged.
- Appearance: proportioned theme previews replace viewport-squeezed art; Save stays pinned. Options is labelled. The existing bedside artwork uses more of the light card, and its explicit action distinguishes Connect, Configure and Review using locally saved configuration.
- Tracking setup names its automatic-start destination and shows the actual configured manual delay in its Start action. Wind-down has a labelled Options control. Runtime/scheduling behavior is unchanged.

Implementation commits: `de909c16`, `b8c22e5c`, `c9f539f6`, `72c4d2bc`, `cfad2cc6`. Three implementation agents worked in isolated worktrees. No tests, lint, screenshot verification, security review, phone or output operations ran. All36 visual/action acceptance stays open.

## Bedtime, challenges and planning — 0.1.17

Published source **`3d4c8422`**, version **0.1.17 / 18**. [Development APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.17-bedtime-planning-20261030-3d4c8422). APK packaging only.

- Challenge routine rows and footer spacing follow the reference proportions. Number choices wrap in balanced rows with existing touch targets; Math answer choices expose their actual checked state. All28 tasks, filters, configuration/result routes and defaults mode remain.
- Guard app rows respond across their full area. Sleep goals has visible duration/time picker marks, actual edit/selection feedback and compact summary/Save layout. Existing installed-app picker/search, drafts, pause and persistence remain.
- Nap simplifies its primary duration row and exposes labelled Options, retaining exact-timer editing there. Presets have explicit selection semantics. Rhythm improves metric hierarchy, empty-state navigation and dated-row chart selection with keyboard/accessibility actions. Travel aligns its real city/clock comparison and shows the active cue window. Statistics, timers, schedules and cue rules are unchanged.
- Audio labels and cue choices are more readable; sound-library tiles expose their real selection state. Room conditions uses the existing bulb raster, aligns light status beneath its reading and improves control captions. No audio, sensing or cue logic is changed.

Implementation commits: `38675541`, `9cf73271`, `46624612`, `b6c32730`, `9ab54593`, `9fb56be0`. Three implementation agents worked in isolated worktrees. No tests, lint, screenshot verification, security review, device or output operations ran. All36 acceptance stays open.

## Alarm, record and connection controls — 0.1.18

Published source **`0825f39c`**, version **0.1.18 / 19**. [Development APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.18-alarm-record-flows-20261030-0825f39c). APK packaging only.

- Alarm list: labelled Options, larger fitted clock, bounded name/schedule text and a single full-row settings action with full tooltip/state text. The wake-up check has a larger confirmation target. Existing alarm and confirmation callbacks remain.
- Smart wake-up: clearer window hierarchy, leading section icons, selected sensitivity controls and a concise tracking prerequisite card. QR/NFC setup exposes location editing, fallback selection, genuine captured state and disabled reader/save states. Policies, permissions, enrollment and draft/defaults paths are unchanged.
- Record editor: labelled Options, aligned date, useful awake count/excluded duration and clear Back to edit actions. Trim guides follow the actual time ticks. Existing notes, data, gestures, calendar, save and discard behavior remain.
- Health Connect: labelled Options, status for the selected import/export permissions, whole-row direction controls and a visually disabled Connect action. Existing sleep-only consent, review, synchronization and error callbacks remain.

Implementation commits: `0bd655e3`, `265852da`, `e4221efa`, `1a352738`, `0305ec22`. Three implementation agents worked in isolated worktrees. No tests, lint, screenshot verification, security review, device or output operations ran. All36 acceptance stays open.

## Custom control feedback — 0.1.19

Published source **`2dfcd82b`**, version **0.1.19 / 20**. [Development APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.19-touch-feedback-20261030-2dfcd82b). APK packaging only.

- Sound mix levels acknowledge changed completed drags and discrete user adjustments. Import-cancel controls acknowledge their actual click. Rendering and programmatic level restoration do not request feedback.
- Record trimming acknowledges a changed active boundary on completed taps or explicit keyboard handle selection. Existing completed-drag feedback and shared quiet guards remain.
- Appearance, guard schedule/app/picker switches, accessibility setup and custom automation confirmations now use existing guarded input feedback. Restores and programmatic checked changes stay silent.
- Previously uncovered alarm dropdowns, tone controls, volume sliders and accepted Math-operator choices now use the shared input helpers. Programmatic picker reopening has a local restoration guard. No alarm or task-runtime outputs were changed.

Implementation commits: `00885a3c`, `0f129ff8`, `e029185b`, `fb12ce63`. Three implementation agents worked in isolated worktrees. No tests, lint, screenshot verification, security review, device or output operations ran. All36 acceptance stays open.

## Recent settings quick access — 0.1.20

Published source **`471007a8`**, version **0.1.20 / 21**. [Development APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.20-quick-access-20261030-471007a8). APK packaging only.

- Settings search keeps the same maximum five shortcut slots. Known recently opened settings come first; existing popular shortcuts fill unused slots without duplicates. Fresh or cleared history shows the existing defaults.
- Reset shortcuts is an explicit48dp action shown only with valid history. It clears only shortcut order. Actual setting opens feed the history; queries, record/alarm IDs, rendering and restoration do not.
- Private bounded setting metadata is serialized off the main thread. Writes retain tap order and outlive SearchActivity shutdown; older completions cannot replace newer choices. Storage failure does not block real navigation. Existing per-alarm chooser and draft routes remain.
- The current remaining-work list and feature-matrix summary are consolidated. Superseded release-history items and original evidence remain in a dated historical snapshot.

Implementation commits: `0c31fa38`, `524cb00f`. One implementation agent worked in an isolated worktree; the main checkout integrated the feature and consolidated current documentation. No tests, lint, screenshot verification, security review, device or output operations ran. All36 acceptance stays open.

## Configured alarm-output API — 0.1.21

Published source **`d7ef1deb`**, version **0.1.21 / 22**. [Development APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.21-alarm-output-api-20261030-d7ef1deb). APK packaging only.

- The ordinary-alarm provider adds seven explicit optional query/write scalars: volume_percent10–100; ramp_seconds, sound_delay_seconds and vibration_delay_seconds0–1440; screen_sunrise, flashlight and extra_loud. Boolean writes accept Boolean or Integer0/1; integer fields retain strict integral bounds.
- The default13-column projection and contract_version1 remain unchanged. Queries report configured values, with null for malformed/out-of-range saved output scalars. Omitted patches preserve existing choices, and create retains valid inherited defaults. Existing admission, grants, ETag, request ledger, ordinary-alarm restrictions and runtime behavior remain.
- Documentation includes the field contracts and actual query/patch examples. No external write, scheduling call or alarm output was executed.
- A source-only reading records the OpenSleep motion/sonar lead and its ignored audio handlers. No third-party staging algorithm or model was imported, and F049 remains missing. Earlier rejected model experiments remain unchanged.

Implementation commits: `9ff0769c`, `2be78f57`. One implementation agent worked in an isolated worktree; the main checkout integrated the API family and recorded the model-source finding. No tests, lint, screenshot verification, security review, device or output operations ran. All36 acceptance stays open.

## Portable feedback and built-in tone API — 0.1.22

Published source **`6087d47e`**, version **0.1.22 / 23**. [Development APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.22-portable-feedback-20261030-6087d47e). APK packaging only.

- Appearance profile exports use version3 with feedbackEnabled Boolean and feedbackStrength Light0/Standard1/Firm2 alongside existing appearance/dimming choices. Capture uses strict typed saved preferences. Readable review and category/About copy show the actual feedback values.
- Exact version1/2 schemas remain accepted. Missing legacy feedback fields do not write current choices, and version1 room-dimming absence remains unchanged. New keys participate in staged fingerprints, apply, readback and rollback through the existing typed main-preference path. The recovery envelope and old key-subset handling remain; profile processing requests no haptic output. Older readers reject the new version3 Appearance payload.
- The ordinary-alarm API adds optional builtin_tone for exact daybreak/pulse/chime configured slots, plus read-only playback_mode for single/silent/playlist/radio context. Changing the slot preserves the playback mode and private data; unsupported/malformed slots return null. Default projections, eligibility, permissions, revisions and receipts remain unchanged.
- Documentation covers format compatibility, staged behavior and configured-slot semantics. No profile export/import or external API mutation was executed.

Implementation commits: `4f9a4ad7`, `02dce5d2`, `a039c316`. Two implementation agents worked in isolated worktrees; the main checkout integrated both feature flows. No tests, lint, screenshot verification, security review, device or output operations ran. All36 acceptance stays open.

## Delivery limits

The package is a development build. This pass has no new screenshot comparison, interaction/persistence acceptance, lint, test, security or physical-device results. Earlier evidence remains historical and must not be treated as acceptance of changed layouts. All-36 matching stays open. Device/overnight accuracy and external-service prerequisites remain in the full checklist and feature matrix.

The original unfinished Privacy cleanup follow-up files and its failed combined run are preserved in its worktree. They were not folded into this usability pass. No claim of a successful combined Privacy run is made.
