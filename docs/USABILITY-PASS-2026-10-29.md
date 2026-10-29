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

## Delivery limits

The package is a development build. This pass has no new screenshot comparison, interaction/persistence acceptance, lint, test, security or physical-device results. Earlier evidence remains historical and must not be treated as acceptance of changed layouts. All-36 matching stays open. Device/overnight accuracy and external-service prerequisites remain in the full checklist and feature matrix.

The original unfinished Privacy cleanup follow-up files and its failed combined run are preserved in its worktree. They were not folded into this usability pass. No claim of a successful combined Privacy run is made.
