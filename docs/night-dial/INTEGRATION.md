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
