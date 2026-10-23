# Strict visual gate — 23 October 2026

**Full app: FAIL / incomplete.** This is a review of original Android pixels,
not approval of later source or a claim that configuration tests exercise alarm,
microphone, vibration, classifier accuracy, or background delivery.

## Evidence boundary

Reviewed every one of the 28 files listed below individually from
`captures/ci-40002966787`, against supplied boards 02, 03, 04, 07 and 10.
The artifact identifies source **e00ff55cb0c7ac3ae197561d78b95f084a42def5**, API 36,
1080×2316, density 420, disposable emulator, host audio disabled, no playback
actions allowlisted. [Original CI run](https://github.com/omeyerrato/SleepWell/actions/runs/40002966787).
This audit was written in a separate worktree based on 5d8862f; its newer source
is **not** certified by these older images. In particular, the subsequent
vibration delay and new sound library require their own fresh images.

Read raw transcripts: AlarmMenusFlowTest passed (60.845 s),
AutomaticTrackingFlowTest passed (11.617 s), SnoreCueSettingsNativeTest passed
(11.264 s), SoundEventsUiTest passed (4.361 s). The overall run failed.
SoundClassifierFixtureEvaluationTest failed its actual PCM→production pipeline→
reopened storage positive-snoring assertion. Synthetic event cards do not cure
that failure or establish real classifier behavior.

## Required corrections

| Priority | Finding | Concrete correction / acceptance gate |
|---|---|---|
| P1 | Automatic tracking's Save settings is partially clipped above the fixed Arm tonight footer in sounds/reopened/error states. | Put complete Save and activation actions in a deliberate footer group; require each full ≥48 dp target above system bars on the first frame, including error state. Do not scroll the test to conceal the defect. Save must only save. |
| P1 | Automatic tracking does not show the actual source bedtime or next wake-window time, and replaces board 10's source/access/supporting rows with a generic Tonight only row. | Surface the real selected schedule/deadline and permission/reliability state. Keep unsupported sonar/missed-night features honestly unavailable; one-night waiting is not proof of the full nightly automatic flow. |
| P1 | Alarm sound lacks board 03's actual tone waveform/preview controls and preview-volume panel. | Implement the real preview flow and measured/derived tone visualization with quiet-emulator evidence. Do not add decorative waveforms or dead Play buttons. The current radio list is not full screen acceptance. |
| P1 | Challenges is still a single-task selector, unlike board 04's numbered, ordered routine with per-task difficulty, add/reorder, QR/Walk and routine save/preview. | Implement the routine data/runtime/editor flow before claiming the reference screen. Honest task navigation fixes one affordance; it does not import the remaining routine feature. |
| P1 | Snoring cues substitutes four text-only segmented strips for the reference's icon-bearing alert cards, compact setting groups and dark selected fills. Preview cue is absent. | Match the alert-type icon cards, brown/white selected states and setting hierarchy; supply a functioning cue-preview flow only with authorized silent-emulator testing. Configuration persistence alone does not accept cue output. |
| P1 | The old classified-event screen has no favorite/delete/recording-management flow from board 07. | Review the newer library using real saved PCM/WAV and original native captures. These analysis-only fixtures cannot prove recording playback, waveform decoding, favorites, deletion or recovery. |
| P2 | Picker pill digits and down-carets have almost no separation (Rounds, snooze count/duration, escalation). | Keep an explicit inner gap and stable padding while retaining ≥48 dp touch targets; verify long values and large text. |
| P2 | Task icons reuse a checklist for Math/Sequence and a document for Memory/Retype; snooze count/escalation reuse clocks and shortening uses faders. | Use distinct semantic icons in the pinned family: arithmetic, stacked cards, keyboard, repeat/count, hourglass and shortening/skip. Family consistency alone does not supply correct identity. |
| P2 | Invalid time uses a muted-gray sentence while the invalid hour keeps normal styling. | Mark the invalid field/error clearly and announce it, without committing it; retain editable input and clear Use time/Cancel. |
| P2 | The editor's large time has no visible editing affordance; the reference has inline up/down controls. | Give time editing a visible cue consistent with the reference and preserve the themed custom picker. Do not revert to the stock Android time widget. |
| P2 | Automatic Save has secondary-looking peach styling even when it is the only meaningful action; error has no direct route to fix the missing alarm. | Use the reference's primary Save hierarchy, keep activation distinct, and offer a direct Alarms action for the missing-window error. Retain the truthful screen-off/stillness estimate disclosure. |
| P2 | Analysis-only event rows do not explicitly say that no audio was saved; repeated minute-only timestamps make nearby short events indistinguishable. | Show a compact analysis-only/unavailable-audio state and use seconds when needed. Do not fabricate a waveform/Play control to imitate the reference. |
| P2 | All analysis event rows are large filled cards with no category accent; board 07 uses compact divided rows and brown/lilac accents. | Restore the row proportions and category accents while keeping real intervals and no-audio states. The newer library must be inspected separately. |
| P2 | Snoring cue settings omit the reference's section icons and useful short intensity/repeat/sensitivity explanations, but retain a two-line bottom paragraph. | Use compact icon-led groups; keep only decision-relevant one-line helper copy and move longer timing/volume explanation into About. No decorative empty section or extra paragraph is needed. |

The primary navigation/detail affordance distinction is improved in these
images: task chevrons now open settings, explicit Use Memory applies the draft,
tone radios select, switches toggle and down-carets open pickers. Preserve this.
No full-screen pass follows merely from correcting those contracts.

## Every original inspected

| Original PNG | Observation and bounded verdict |
|---|---|
| menus-editor.png | Save and Alarm on fully visible; flat settings align. P2: time lacks an editing cue and header-to-time gap is looser than board 02. Unselected weekdays are legitimate fixture data, not a defect. Full editor fidelity remains pending. |
| menus-time.png | Cream/Inter 12 h dialog, clear AM selection and complete Use time/Cancel. Supporting dialog has no visible clipping; it does not prove selected weekday/editor states. |
| menus-time-24.png | 19:45 fits, AM/PM correctly absent, actions complete. Supporting-state geometry acceptable; not proof of alarm firing. |
| menus-time-invalid.png | 29 is preserved for correction, validation sentence present, no forced commit. P2 error styling/field indication remains weak. |
| menus-time-recreated.png | 07:45 PM remains visible after recreation; selected PM is distinct. No visible dialog overlap. |
| menus-sound.png | Daybreak radio selected; tone selection does not pretend to navigate. P1 preview/waveform/panel gaps remain; Alarm volume % · 80 should read cleanly as label plus 80%. |
| menus-tasks.png | Current Math and genuine chevrons are clear. P1 ordered routine missing; P2 repeated semantic icons. |
| menus-task-settings.png | Memory description, Rounds picker and Use Memory are explicit. P2 numeral/caret spacing; sparse single-field supporting screen is not the reference routine editor. |
| menus-task-chosen.png | Current moves to Memory; row/label alignment holds. Same routine and icon blockers as menus-tasks. |
| menus-snooze.png | All settings and fixed Save fit without overlap. P1 backup sound/volume are a static Pulse · 100% row instead of the reference's configurable tone/volume. P2 pill spacing/icons. |
| menus-snooze-changed.png | 1 min, three snoozes, shortening on, seven-minute escalation, ten-minute wake check all fit. Wake-check helper wraps because its value pill consumes width; improve column allocation. |
| menus-duration.png | Selected 5 min has matching radio and fill; visible scrollbar indicates more than the nine exposed options. Cancel is complete. No clipping defect found in this supporting state. |
| menus-duration-recreated.png | Same selected 5 min and list position survive. No visible regression from duration. |
| menus-recreated.png | Changed snooze controls retain their values/switch state; Save remains fully visible. Backup configurability/icons remain missing. |
| menus-reopened.png | Same changed configuration after reopen; labels align. This is persistence evidence only, not runtime snoozing. |
| menus-large-snooze.png | Large text wraps wake-check helper naturally; no label/value/button overlap and Save fully visible. Preserve this bounded layout success; static backup row still prevents full board 03 acceptance. |
| automatic-manual.png | Selected Manual only radio/card agree. Save is peach and sits far above the bottom, unlike the reference's primary footer. Actual source/support rows missing. |
| automatic-stillness.png | Selected After settling down is honest about 15 minutes still/screen-off estimation. Save and Arm are separate but dispersed; fixed footer group needed before adding more content. |
| automatic-sounds.png | P1: Save settings bottom is clipped by footer. Sound/clip switches are clear; selection does not prove microphone capture. |
| automatic-reopened.png | Sound/clip and settling-down state persist visually; the same Save clipping repeats. Reopen is not a visual pass. |
| automatic-error.png | Missing smart-window alarm is stated in visible error text. P1 Save more heavily clipped; P2 add direct resolution. Do not discard the error disclosure. |
| snore-settings-default.png | Off master, Vibration/Low/1/Medium selections visible and Save complete. P1 icon cards, dark selected fills and actual Preview cue absent; P2 helper hierarchy. |
| snore-settings-selected.png | On/Both/Medium/3/High clearly differ from default. Same reference-composition and preview blockers remain. |
| snore-settings-recreated.png | Selected states remain coherent after recreation, no cropping. Persistence does not accept cue delivery. |
| snore-settings-reopened.png | Same selected state after reopen, complete Save. Same full-screen blockers remain. |
| analysis-events-all.png | Explicit synthetic fixture, All (4), three Snoring and one Speech row. No fake Play/waveform, which is correct. P1 management flow missing; P2 analysis-only disclosure, timestamps, accents/card proportions. |
| analysis-events-speech.png | Speech (1) selected with exactly one visible Speech row. Empty remainder is legitimate data, not a reason to invent a graph. Same no-audio/management gaps. |
| analysis-events-recreated.png | Speech filter and row remain selected after recreation. No visible state mismatch; no real classifier/output certification. |

## Report gate and next evidence

All seven report-reference originals in this same artifact were also opened
individually against boards 01/08. The corrected Week/Month/Year Read details
target is now completely above the dock, ribbons are continuous instead of a
comb, and editor/tag/Save/Delete controls fit. Those narrow corrections pass
visual inspection. **Report flow still FAILS:** only seven of eight required
images exist; `report-reference-reopened.png` is missing. The raw failure was
masked by the diagnostic capture. The subsequent diagnostic/window-wait source
fix has no successful evidence in this artifact. Sleep-stage classification
remains explicitly unavailable; no fake stage strip is accepted as a substitute.

Next review must use one exact newer source artifact and inspect all eight report
images plus new travel, privacy, plan, library and vibration states. Do not mix
old passing frames with new failed ones, and do not call later source fixed
until its own actual captures and main-action/persistence checks support that.
No personal phone interaction or output test was performed for this review.

## Subsequent exact-head review: CI 40016966655

Source `f471c4d0f8339973dc363c26a39997c1e7547ba9`, API 36 emulator,
1080×2316 at density 420. This is a separate review of 32 originals opened
individually; it does not upgrade later source or substitute old frames for
missing new ones. The run failed. Its environment explicitly disallows playback
actions; the library uses synthetic WAVs and fake playback output.

| Original PNG | Strict finding |
| --- | --- |
| report-reference-history.png | All seven real fixture rows fit above the four-tab dock; continuous ribbons, aligned times and duration hierarchy are visible. No new clipping. |
| report-reference-week.png | Both charts, duration/goal gap and complete Read details fit above the dock. Circular point markers are heavier than board 08; P2. |
| report-reference-month.png | Missing dates stay blank; complete Read details remains visible. No invented full-month series. |
| report-reference-year.png | One observed month is shown honestly. Complete Read details remains above the dock. |
| report-reference-failure.png | Actual night report is visible with 3 Snoring/1 Speech estimates and truthful analysis-only disclosure. This is a diagnostic frame, not the required accepted night capture. Sleep stages remain unavailable. |
| wake-settings-sound.png | Tone radios, volume and fixed Use button fit. P2/full-board gap: actual tone waveforms, preview controls and preview-volume panel from board 03 remain absent. |
| wake-settings-loud-curve.png | 100% curve endpoint, grouped vibration timing pill and fixed Save are visible. Flashlight is below the first frame; no effect delivery has been tested. |
| wake-settings-vibration-options.png | Rounded cream/Inter dialog; selected From start plus all offered delays and Cancel fit. |
| wake-settings-failure.png | Timing pill actually displays After 1 min. Test failed during the unscoped dialog/underlying-value sequence; this frame does not prove Save/reopen/cancel. |
| travel-plan.png | Core plan, both clocks and fixed Save fit. P2: Home time zone uses a clock rather than house; Light source value wraps to two lines. Correctly distinguishes current offset from departure-day offset. |
| travel-zones.png | Search and a selected real zone row fit; supporting state coherent. |
| travel-date.png | Calendar month/date and single helper fit. This frame shows 23 Oct selected; it is not evidence of final saved departure date. |
| travel-failure.png | Rounded themed time dialog is visible. Failed journey means no Save/reopen acceptance. |
| privacy-history-failure.png | P1 visual regression: destructive confirmation is stock white, square, system-font UI over cream/Inter. No successful deletion/recovery evidence in this frame. |
| plan-picker-time.png | Themed bedtime dialog, PM state, fields and both buttons fit. |
| plan-picker-time-recreated.png | Pending 09:15 PM survives in the visible dialog. No cropping. |
| plan-picker-failure.png | Duration dialog is visible but hour/minute input surfaces touch with no gutter, P2. The frame-identity assertion failed; remaining required plan states are absent. Later parent gutter fix is not certified here. |
| library-recordings.png | Much closer to board 07: three category chips, time/duration, real fixture waveforms, hearts/delete and truthful analysis-only row. P2: category accent strips absent. |
| library-playback.png | Selected row expands with brown Pause and actual fake-sink position/seek. P2: bottom Recorded signals entry is half-clipped as continuation. No physical playback certification. |
| library-favorite-recreated.png | Filled favorite and selected seek position survive visually. Same half-clipped bottom continuation. |
| library-speech.png | Selected Speech chip and one lilac waveform match the category. Large empty remainder follows the one-row data. |
| library-recording-deleted.png | Removed recording loses its waveform/Play while the estimate remains; no invented sound availability. |
| library-retention-failure.png | Themed retention-choice dialog fits, selected 7 days visible. Full retention journey failed and remains unaccepted. |
| automatic-manual.png | Narrow Save-clipping correction PASS: full brown Save is fixed above system bars. |
| automatic-stillness.png | Full Save and secondary Arm are separately visible; no overlap. |
| automatic-sounds.png | Both sound switches plus full Save/Arm fit without scrolling to hide the earlier defect. |
| automatic-reopened.png | Same full controls and selected mode remain visible. |
| automatic-error.png | Error stays visible above full Save/Arm. Narrow footer correction PASS; this does not certify actual automatic sensor capture or recurring scheduling. |
| snore-settings-default.png | **P1 REJECT: completely white content frame, only the bottom handle visible, despite native class PASS.** A screenshot name and passing transcript do not prove a rendered screen. |
| snore-settings-selected.png | Old text-only cream segments still differ from board 07 brown icon cards; Preview absent. |
| snore-settings-recreated.png | Same old selected states are visible, no clipping; composition remains rejected. |
| snore-settings-reopened.png | Same old controls remain visible. Does not accept later icon-card/preview source. |

Report remains **FAIL: only 4/8 required named frames**, with native failure at
line 79. Night/editor/tags-notes/reopened are missing from this head. No stage
estimator exists. Travel, privacy, plan and retention journeys also failed;
their incomplete image sets must be replaced by complete successful same-head
evidence. Snoring cues needs five new actual images after its icon/preview
change, with focused hardware-frame and visible-action checks that reject the
white default. Overall 36-screen feature and design completion remains FAIL.
