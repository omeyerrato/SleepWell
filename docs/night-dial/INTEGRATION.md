# Night Dial integration

Status: work in progress. This is an isolated design branch, not an app-wide acceptance claim.

- Branch: `codex/night-dial-design`.
- Checkout: the managed `night-dial-design/SleepWell` worktree.
- Starting revision: `669ec8f6`.
- The feature agent's main checkout is not edited or reset. Nothing is merged automatically.
- Native Java Views, the existing Android services, persisted models, storage and permissions remain the application framework.

## Design authority

Read the root [design contract](../../design.md), then the [selected reference](../../design-concepts/2026-10-29-night-dial-flow-library/accepted-reference.png) and the selected boards in `final-manifest.json`. The contract corrects known generated-image mistakes. Runtime facts must always come from the existing feature owners.

## Integration boundaries

`NightDialTokens`, `NightDialSurface`, and `NightDialMotion` are presentation helpers. They do not save preferences, schedule alarms, request capture, or start audio. The new sound card delegates its explicit Play/Pause action to the existing `SleepSoundService.command` API. Opening the card or Tonight remains silent. Image decoding happens off the UI thread, transport polling stops with window visibility, and source artwork is separate from native labels and controls.

`Design`, `Ui`, `RecordUi`, `SoundSliders`, `AlarmSliders`, `SleepMixerUi` and `SleepPicker` adapt the existing rendering entry points. Their business callbacks and draft ownership remain with callers. Existing `Design` color names are temporarily compatible aliases; new presentation code must use semantic `NightDialTokens` names. **Do not copy the old `CREAM` name into new foreground code:** some old dark screens used it for light text, while light screens used it for backgrounds. Existing foreground sites need individual review.

`ReportDialogs` retains the actual native AlertDialog Buttons, IDs and callbacks while arranging actions as full-width rows with explicit spacing and minimum touch heights. This prevents long consent/destructive labels from running together on phones. It changes presentation, not the positive/negative/neutral callback meanings. The microphone consent window is owned by TrackingActivity, restored across recreation, and leaves the microphone Off until an explicit choice and any required Android permission grant.

`NightArc` keeps its constructors, `NightWindow`, clock update, nap semantics and explanation lifecycle. It renders a 24-hour local clock, with actual interval duration (including DST). The ring shows a plan, not measured sleep or estimated stages. Its optional `Editor` opens `PlanActivity.dial`; gestures never write storage. `NightDialRange` keeps the handles independent across midnight, with five-minute snapping and 20–810 minute limits. Exact one-minute entry uses the existing picker. Native accessibility actions expose each endpoint. A cancelled gesture restores the saved view; a released edit survives recreation in the plan owner and uses its existing revision-checked asynchronous save/reminder operation.

When a wake-up change is linked to the displayed alarm, the plan review offers a separate alarm review. `AlarmEditorActivity` verifies the original alarm snapshot before prefilling the proposed fixed time. Solar/next-occurrence overrides retain their original basis and require explicit review. The existing alarm editor, stale-write protection and Save alarm operation remain the only alarm commit owners. Plan-only save does not move an alarm.

`NightDialMixerPanel` delegates playback to `SleepSoundService` and gain commits to `SoundPreferences.saveIfRevision`. Timer/fade drafts use `SleepMixerChoiceEditor`. `NightDialWaveform` decodes bounded PCM previews off the UI thread; a real 25ms middle-of-loop excerpt preserves each sound's texture, and displayed amplitude follows the chosen gain. The waveform is a source preview, never a simulated microphone signal. Imported sources without a bundled preview retain an explicit unavailable state.

`TonightScreen` retains `Actions`, stored-plan/alarm selection, enabled/repeat callbacks, optional saved-data readers and the existing tracking entry. The main action opens existing tracking preparation. It does not start capture merely by rendering. Optional rows and their custom ordering remain available.

`MainActivity` has presentation changes and Tonight scroll restoration when returning from child routes or recreating. Integrate these changes into the latest feature version; do not replace the feature agent's whole Activity with this older baseline.

`TrackingActivity` also contains a baseline compile repair: two identical-signature `onWindowFocusChanged(boolean)` overrides existed at `669ec8f6`. The earlier, simpler override was removed because the later override already includes the dark-bar call plus reader suspension and pending resume. Preserve the later handler. Settle in reuses existing consent/admission and sensor-setting routes. The live dial renders real snapshot start/active duration, while the existing readers own measured graph data and gaps. End-night confirmation rechecks session identity; Cancel keeps the current active/paused state. The confirmation has an owned lifecycle and a saved session identity for recreation.

## Suggested integration order

1. Add `NightDial*` helpers, the sheet animation resources and native theme resources together.
2. Apply shared UI adapters, reviewing each remaining hardcoded foreground/background pair.
3. Apply the dial and Tonight composition while preserving the latest feature callbacks and async guards.
4. Apply the remaining screen and flow polish as it is completed; use the progress matrix as the checklist.
5. Run feature tests on the integrated revision and compare new native captures to the selected boards. This branch's screenshots cannot prove behavior of a later merge.

No Compose, Kotlin, navigation-library, database or Gradle-plugin migration is required. No dependency versions were changed for the visual work.

Reduced-motion adapters also select an explicit no-motion Window animation style for app-owned pages/sheets and settle native Switch state after `setChecked` returns. The latter is one presentation-only override in `InteractionFeedback.Toggle`; its gesture-only haptic dispatch, debounce, strength selection and quiet-context guards are preserved. Native transition and switch motion still require runtime review at reduced motion and with system animations disabled.

## Verification boundaries

`NightDialReviewTest` covers roots, mixer discovery, time validation, cancel and Activity recreation. `NightDialInteractionTest` adds actual pointer drags, independent endpoints, plan conflict/save/cancel/recreation, timer draft recreation, microphone-consent cancel and explicit linked-alarm save. `NightDialTrackingReviewTest` uses the existing synthetic duplex-PCM coordinator and private SQLite fixture to verify actual measured movement, pause/recreate/resume and explicit end/save. All run only with `ciReview=true` on an emulator; no physical capture, playback or OS alarm delivery occurs. UUID fixtures and preference restoration remain required. The workflow uploads actual native frames and diagnostics. A passing result is not whole-app, accessibility, motion or overnight reliability acceptance.

The host lacks the Android emulator hypervisor driver. Local app compilation/unit checks remain available; rendered Android review uses an ephemeral GitHub Actions emulator. Do not install or clear the personal phone to work around this host limitation.

## Asset provenance

`app/src/main/res/drawable-nodpi/night_dial_rain.png` is a standalone generated photograph used by native media UI. It was generated with the built-in imagegen tool on 2026-10-30, not extracted from another app or baked into a screen. The final prompt is preserved in [rain asset provenance](RAIN-ASSET.md). Existing Inter and Phosphor licenses remain applicable to their existing assets.

## Report and record presentation adapters

The Report dock now opens `RecordReportActivity` with `reportRoot=true`; its existing single worker reads the latest saved night and builds the report snapshot. `ReportNavigation` replaces peer Night/History/Trends pages instead of accumulating them. Specific history rows open a read-only report; editing requires the labelled Edit action. Merge these routes into the latest feature navigation rather than replacing MainActivity wholesale.

`NightDialReportSummary` compares actual effective duration with the current saved duration goal. It is not a sleep-quality score or a 24-hour clock. The report snapshot reads goal and subjective rating on its existing worker. Imported/user-reported periods keep their provenance; missing goals and sensor values remain unavailable. No sleep stages are inferred from local movement.

`RecordSignalsActivity` shows one selected saved metric at a time. `RecordChart` adds horizontal inspection and restores the selected real timestamp across recreation; vertical scrolling and cancelled pointer gestures preserve prior inspection. Existing aggregation, measured gaps, units and readers remain the data owners. `TrendsChart` uses a zero duration baseline and actual observed-day averages, with a saved-goal reference and visible range navigation. Year bars remain monthly averages of observed days. Missing dates do not become zero-duration nights.

`RecordEditorActivity.requestBack` compares its local draft with its initial snapshot and presents an owned discard prompt. Restore original times is now a reviewed draft operation: original start/end and awake edits are restored only in memory, annotations stay by default, and a separately labelled checkbox can restore original notes/tags/rating. The existing Save changes operation is the only commit owner. `Records.save`, `Records.reset`, SQLite schema and CAS/save-token behavior were not changed. Keep these UI adapters separate from any newer feature-agent changes in the same Activity.

`NightDialReportFlowTest` uses private UUID databases and synthetic dated records. It verifies read-only navigation, source filters, selected signal/time restoration, dirty draft prompt restoration, annotation-preserving restore, and independently computed Week/Month/Year summaries. It starts no hardware capture, playback or OS alarm. Passing evidence must be recorded at its actual source revision before these flows are described as verified.

Report adapters have passing narrow native evidence at [fca53502](verification/fca53502/README.md): five total native methods, all 42 frames, and 59 focused unit methods. Optional annotation-reset and remaining source/date/import/device branches still require review on an integrated revision. The final evidence commit does not change the reviewed application-source tree.

## Alarm presentation adapters

The alarm-list composition keeps the existing enable/skip/edit owners. It orders enabled alarms by their actual admitted next occurrence, including pending nap deadlines, then alarms needing review and disabled alarms. Each card adds the saved routine and a separate settings target. Calendar preview uses the existing admitted receipt; it does not request calendar reads or change scheduling. Preserve newer MainActivity feature callbacks when integrating this isolated adapter.

The ordinary AlarmEditorActivity now uses the same dirty-draft exit protection as the Settings entry. Its owned discard sheet and name draft survive recreation. SleepPicker.name is now a StateDialog that retains typed name/selection/error; opening or cancelling still writes nothing. Repeat rule is directly reachable beside the weekday controls and returns through the existing identity-checked child result. Interval mode retains the feature owner's disabled weekdays.

AlarmRampChart draws the exact requested AlarmWakePolicy envelope over time after the alarm starts, including sound delay. It is not measured speaker output; silent mode draws no sound curve. AlarmSnoozePreview derives the first configured durations from SnoozePolicy and the total budget. Neither view starts output, changes an occurrence or writes a setting. Challenge catalog counts derive from the same filtered rows; ordinal routine rows and their drag/Move up/down operations retain the existing bounded draft owner.

AlarmEditorUi and ChallengeRoutineUi attach explanatory tooltips to the actionable row rather than its nested value/description: a tooltip on the child consumed ordinary edit taps. Preserve the parent's action and focus target when adapting newer feature rows. Inactive weekday letters use readable secondary ink. AlarmRepeatActivity clears an already-shown validation message as soon as the current candidate becomes valid, without rebuilding the text field; existing rule validation/result identity remain authoritative.

The owned discard sheet now protects ordinary alarm editing as well as Settings drafts. Keep the complete original-snapshot comparison and the existing identity-checked Save owner when integrating. SleepPicker's saved name/selection/error state belongs to the sheet; a recreation must not commit that name to the alarm.

[37de8841 native evidence](verification/37de8841/README.md) passed seven total native methods, all 64 frames and 80 focused unit methods. NightDialAlarmFlowTest enters through the real alarm-list parent, checks name/repeat/discard recreation, orders Memory before Math, inspects requested gentle/snooze configuration and verifies one explicit Save reaches a silent scheduling port. Its private stores, unchanged alarm-stream volume and no-output guards must remain. The dial helper only reacquires stale accessibility geometry before preparing an input sequence; it never replays a gesture.

This evidence does not establish acceptance of alarm deletion/duplication, every schedule/source/input/backup/practice branch, all 28 challenge surfaces or device/accessibility/motion variants. The packaged APK's app/src/main tree is aeba57ed9b1cbded14ba535fead391dcd142d997. Later integration needs its own tests and captures. The feature branch has advanced and the draft PR currently has merge conflicts; resolve the narrow adapters rather than replacing Activities wholesale.

## Alarm management follow-up

AlarmSkipUi.Controller owns the pending snapshot, reviewed deadline, sheet and attention notice. MainActivity gives it onCreate/save/resume/destroy hooks. A root clock refresh must not dismiss the review; recreation restores the same reviewed occurrence, never silently selects a later one. Only AlarmSkips.skip/undo/retry persist or schedule, and their stale-occurrence guards remain unchanged. Cancel and dismissal write nothing. Integrate these hooks into the latest MainActivity rather than copying the Activity.

AlarmDuplicateDraft creates only a detached ordinary alarm draft. It preserves base schedule and behavior, chooses a new identity in the editor, turns the copy off and clears skip/next-override/weekly occurrence authority and scheduling errors. The bounded copy name fits the existing 80-character editor field without cutting a surrogate pair. No store or scheduling call occurs until the existing Save owner is explicitly invoked; originalExisted=false keeps its existing collision rejection. The source editor and any unsaved source draft remain available beneath the copy.

The editor's Delete command opens a named owned StateDialog; its recreation uses the existing picker restoration path. Confirmation checks the full originally opened saved alarm before invoking the original cancellation/delete operations under their existing Scheduler lock. A changed/deleted identity leaves data untouched and exposes an error in the editor. Recorded nights are outside the existing deleteAlarm scope. The dirty-exit sheet now offers Save alarm/Keep editing/Discard; Save calls the same existing save() or defaults-return owner and retains validation/access checks.

Expanded native assertions cover reviewed Skip cancellation/recreation/undo/stale state, off duplicate draft/recreation/explicit Save, named Delete cancellation/recreation/stale identity, and Save from dirty Back. They use the existing private databases, silent scheduling port and output/volume/prefs guards. The pre-existing AlarmSkipFlowTest adds failed-scheduling/retry/reopen and one-time/expired behavior with its unchanged sentinel alarm. Only changed sheet wording is updated in that test and SearchFlowTest; the rest of SearchFlowTest was not rerun here.

The c210599c functional pass exposed a shared sheet geometry defect: the window reserved the requested maximum height even when its painted panel was shorter. SleepPicker.show now wraps its original content once in a bounded measurement container and uses natural window height. The requested height remains a cap; long choices still scroll. Original scroll references, draft restoration, callbacks, dimming and reduced-motion policy remain. Verify both short confirmations and long radio lists when applying this helper to the latest feature branch. Native short-sheet assertions reject trailing viewport gaps; full device/text/IME review is still pending.

[6409f7a6 evidence](verification/6409f7a6/README.md) passes 11 native methods, 93 required frames and 80 focused unit methods. Selected final captures were visually inspected. The matching signed local APK is in Downloads with receipt/checksum; app/src/main tree d281d8f280345e8a6ae767169ed847284a7391a3 is pinned to that review. The app remains native Java Views with no framework/dependency migration. Keep this PR draft: the remaining design matrix and latest-feature integration are incomplete. Earlier 37de8841 delivery/evidence remains historical and is not overwritten.

## First night and readiness under review

NightDialWelcome offers a guide only on a launcher MAIN entry with no saved preferences, alarms or nights, no active alarm/tracking, and no explicit system/settings/report destination. Existing users keep their normal route. Reliability also has a deliberate First-night guide entry. The dial is labelled Example night; exampleFace hides the live Now marker and the welcome preview cannot edit a plan. Explore first/Continue explicitly save only the UI acknowledgment key nightDialFirstNightDone. No account, plan, alarm, permission, capture or audio is created by opening/restoring the guide.

MainActivity retains the guide stage/origin and AlarmReadinessUi.Controller state across recreation. Back from checklist returns to the guide; Back from a guide opened in Reliability returns to Reliability. Continue/Explore opens Tonight. Merge these small hooks into the newer MainActivity without replacing feature callbacks or navigation branches.

AlarmReadinessUi retains actual Android notification/exact/full-screen/battery reads and the existing asked-marker/request primitive. The first-night checklist excludes optional microphone access. Its notification explanation always precedes Android, and a denied first-night request exposes Android settings plus Use app for now. The ordinary Reliability route retains first-request/rationale/settings behavior for optional microphone access. The controller owns explanation/information windows, restoring selected scope and scroll without requesting access. Permission results and Android returns re-read state; a saved preference does not imply a grant. No microphone option, sensor or playback is enabled by denial/Cancel/restore.

Readiness cards use neutral surfaces, readable semantic status ink and one deliberate action per row. The existing bounded quiet test remains an explicit supporting action on ordinary Reliability; it is absent from the first-night checklist. The empty Alarms state adds a plain alarm illustration and an explicit draft explanation; Add alarm, defaults loading and the existing save/scheduling owner remain unchanged.

NightDialFirstNightTest uses private UUID stores/defaults and restores its complete preference snapshot. It is restricted to ciReview on an emulator. It exercises the actual launcher/Explore/relaunch/existing-data bypass, Add-from-defaults/Discard/off Save, real Android notification denial/settings return, and optional microphone rationale recreation/Cancel. It checks unchanged alarm-stream volume, no alarm/tracking/sound/microphone lease, and no saved night or alarm before explicit Save. App/test Java compilation passed; final native evidence and pixel review are pending for this revision.

## Application-wide pass, 31 October 2026

Main `d1b954de` is a parent of merge `f5d30db6`. The sole merge conflict was the sound artwork reader; the Night Dial Rain photograph and bounded decoder were retained. No framework/dependency migration was performed. Current implementation `489f914c` extends presentation across secondary destinations and all task families; see the 127-destination/43-flow inventory.

Important adapters: NapActivity updates the duration wheel, preset state and actual end time in place. ACTION_CANCEL restores the draft; release and accessibility edits do not schedule. GuidedWindDown retains the existing elapsed/state/narration owners and pauses on focus loss; the contour animates only when running, visible and permitted by motion preferences. Memory changes only card presentation; matches/advancement remain existing game logic. PCM waveforms are actual bundled source excerpts, while the stereo plot is explicitly a mathematical configuration preview. None starts playback.

DiaryActivity handles both legacy OS Back and registered predictive Back through the same dirty-draft review, and restores owned review/delete/export dialogs. Confirmation titles name the saved entry, not an uncommitted replacement title. Long shared setting descriptions stack beneath titles; preserve parent click/focus ownership. Activity headers and buttons measure natural text height. Appearance keeps legacy AUTO/THEME saved values while displaying the accepted dark design at every hour.

Native dialogs inherit the same dark Inter theme, charcoal rounded panel and preference-aware enter/exit motion. Existing SleepPicker/ReportDialogs still own draft persistence, geometry, IME and dismissal. Additional photos are original generated assets with provenance; historical reference assets remain intact.

One narrow functional repair is intentionally included: AlarmRequest's nullable minute ternary now boxes zero explicitly, preventing null unboxing for a time-less external request. Four existing AlarmRequest tests pass. Scheduling, model thresholds, capture and provider semantics were otherwise not replaced by this presentation pass.

The f16 broad existing feature run timed out and contains unresolved failures as well as obsolete light-theme expectations; it is not accepted as a full regression. See PROGRESS.md and revision evidence for exact review scope. Hardware/output/provider acceptance is not implied by emulator screenshots. The phone is untouched; delivery remains APK only.

Latest integration follow-up: main advanced through `05f005a1` to the frozen merge parent `cc4871ee` while review ran. Its recording-cleanup storage invalidation and related PrivacyScreen/PrivacyStorageSummary changes are retained. AppearanceLightCard's bounded fitting/summary refinements merge with the dark raster filter. WakeCheck's accepted dark composition is retained rather than restoring the bright dawn background; the new footer identity tag is preserved. Existing source-specific feature evidence remains historical. The appearance test now names the actual Begin night action while retaining its complete-bounds and saved preference assertions.

The home widget now uses charcoal/neutral launcher resources. HomeWidgetSettings displays the actual existing HomeWidgetViews projection loaded on a worker; every preview action is inert, and pin/refresh remain separate explicit commands. The preview closes its worker/callbacks with the screen and retains no synthetic alarm or report values.
