# Usability implementation pass — 29 October

Current instruction: make the app usable first. New tests, lint, screenshot verification, security review and a separate bug-fixing pass are deferred by the user. This document records source changes, not visual acceptance of all 36 references.

Three implementation agents worked in separate worktrees. The main checkout integrated their changes with the shared Settings and tracking navigation work. Package source: `4e47ab35`, version **0.1.9 / 10**. Packaging runs `:app:assembleDebug` only. The phone remains untouched.

The APK packaged successfully in3m47s and is [published as a development prerelease](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.9-usability-20261029-4e47ab35). No new acceptance or review jobs ran.

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

## Delivery limits

The package is a development build. This pass has no new screenshot comparison, interaction/persistence acceptance, lint, test, security or physical-device results. Earlier evidence remains historical and must not be treated as acceptance of changed layouts. All-36 matching stays open. Device/overnight accuracy and external-service prerequisites remain in the full checklist and feature matrix.

The original unfinished Privacy cleanup follow-up files and its failed combined run are preserved in its worktree. They were not folded into this usability pass. No claim of a successful combined Privacy run is made.
