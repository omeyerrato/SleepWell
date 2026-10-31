# Shared wake presentation and safe routine flow

[Run41185003781](https://github.com/omeyerrato/SleepWell/actions/runs/41185003781), scope `wake-reference-current`, exact source `d1b954deed7c1bad8cb6d46dad9063082b21f7ea`.

Both selected existing native methods passed; all **25 required Android originals** are present and were opened individually at full-window size against [board01/right](../../../design-concepts/2026-10-22-phone-app/01-everyday.png) and [board04](../../../design-concepts/2026-10-22-phone-app/04-challenges.png). The originals are unchanged1080×2316 screenshots from an API36 emulator at420dpi. No collage, crop, repainting or generated mockup substitutes for them.

| Native method | Result | Captured states |
|---|---|---|
| `WakeCheckFlowTest#silentPromptCountsDownRecreatesConfirmsAndReopensDurably` | PASS28.134s |7 waiting/prompt/countdown/recreation/confirmation/reopen/storage-error frames |
| `ChallengeRoutineFlowTest#orderedTasksConfigureReorderCancelRecreateAndPersistWithoutOutputs` | PASS63.058s |10 routine/catalog/configuration frames and8 shared Math practice frames |

[Environment](environment.json), [focused summary](focused-review-summary.json), [wake-check native log](WakeCheckFlowTest%23silentPromptCountsDownRecreatesConfirmsAndReopensDurably.txt), [routine native log](ChallengeRoutineFlowTest%23orderedTasksConfigureReorderCancelRecreateAndPersistWithoutOutputs.txt), [requested SystemUI glyph state](routine-system-glyphs.txt) and [byte/SHA provenance](originals.json) preserve the exact source and execution scope. Host audio was disabled; no native playback output was allowlisted.

## Shared production renderer, without ringing admission

`AlarmActivity.build` calls `WakeChallengeUi.card` with its actual clock, caption, current routine position and task count. `RandomChallengePreviewActivity.math` calls the **same production component** for safe practice, with its actual one-task selection. These captures therefore show the shared time/header, task-progress marker, full112dp amber landscape and full-width cream sheet; they are not a separate illustration of the alarm screen. The sheet has rounded top corners and square bottom corners, removing the earlier orange bottom-corner slivers.

[Initial](challenge-math-initial.png), [selected](challenge-math-selected.png), [wrong](challenge-math-wrong.png), [recreated](challenge-math-recreated.png) and [completed](challenge-math-completed.png) show the large question, balanced four-answer grid, brown selected state and full Check action. [Typed wrong](challenge-math-typed-wrong.png), [typed recreation](challenge-math-typed-recreated.png) and [typed completion](challenge-math-typed-completed.png) retain the input, inline feedback and complete action. The same native capture path asserts that the shared clock, progress marker and landscape are visible and unclipped, and that the sheet spans its parent width.

Practice is explicitly labelled `Task preview`, `Task1of1`, `Math practice` and `Check practice answer`, with its no-current-alarm explanation below the action and a separate Done button. One real practice task yields one progress bar; it does not fabricate the reference's three-task example. The question, selected answer, typed input and completion survive the exercised recreations. Original occurrence/input-owner guards, complete48dp targets and personal-preference/database preservation assertions remain in the existing journey.

**This does not launch or accept real `AlarmActivity` admission, full-screen delivery, ringing, snooze/completion authority, screen pinning, sunrise brightness, speaker, vibration or flashlight behavior.** The safe practice Activity supplies different truthful copy and a back/Done route. No phone operation or real output ran. Passing these native component states does not finish reference03's real-runtime acceptance, every size/font, all28 task runtimes or all36 references.

## Supporting configuration and wake-check comparison

- [Legacy routine](routine-legacy.png), [ordered](routine-ordered.png), [reordered](routine-reordered.png), [removed](routine-removed.png) and [reopened](routine-reopened.png): numbered warm task cards, clear edit versus reorder affordances, readable Set up routes and paired Preview/Save actions. Current saved fixture order/rounds are visible; the reference's QR task is not invented for this fixture.
- [Empty catalog](routine-catalog-empty.png), [filtered catalog](routine-catalog-search.png), [task explanation](routine-task-about.png), [task values](routine-task-values.png) and [recreated task draft](routine-recreated.png): actual search/empty state and round selection, explicit Add versus Save intent, full fixed actions and draft restoration. Extra task categories remain horizontally scrollable.
- [Waiting](wake-check-waiting.png), [prompt](wake-check-prompt.png), [countdown](wake-check-countdown.png), [recreated countdown](wake-check-recreated.png), [confirmed](wake-check-confirmed.png), [reopened confirmation](wake-check-reopened.png) and [storage error](wake-check-storage-error.png): amber scene, dominant clock/heading, genuine fake-clock2:00→1:15 ring, confirmation/reopen and visible unavailable-state recovery. The native method uses private state and injected clock/scheduler/re-ring outputs; the ring reflects its deadline instead of copying a static partial arc from the board.

## Visible residuals: before further WakeCheck refinement

The shared Math shell has no newly observed clipping or disconnected selected state in these eight captures. This is a bounded comparison, not exact pixel parity or delivery acceptance.

Two existing WakeCheck differences remain visible in this source and are retained as **before evidence**: the explanatory caption sits almost immediately below the brown button in prompt/countdown/recreated and waiting frames, whereas board04 leaves a clear gap; the large center check/sun icons in confirmed/reopened/storage-error frames look softer than the crisp native ring/sun in prompt. These differences are not waved through because this increment did not introduce them. Any later source correction needs its own native comparison; these earlier originals cannot accept it.

No new build, test run, source mutation, device action or output was performed to prepare this archive. The two run results above belong to the exact captured source. The metadata phrase `Math practice card only` is the existing scope label; the source and new tagged-component assertions establish that practice also invokes the shared full presentation shell, while continuing to exclude real alarm admission and outputs.
