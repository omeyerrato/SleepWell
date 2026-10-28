# Wake-check and shared Math reference evidence

[CI 40607145222](https://github.com/omeyerrato/SleepWell/actions/runs/40607145222) **PASS: two existing methods / 21 required originals**, captured at `b25a80f20963832c3fd915a6e5e4998bfd223000` on isolated `codex/verify-wake-challenge-20261028`. `full_app_review` is false.

- `WakeCheckFlowTest#silentPromptCountsDownRecreatesConfirmsAndReopensDurably`: **25.682 seconds**, seven frames.
- `ChallengeRoutineFlowTest#orderedTasksConfigureReorderCancelRecreateAndPersistWithoutOutputs`: **48.983 seconds**, six routine frames plus eight shared Math practice frames.

All 21 originals were opened individually against [Board 01 right](../../../design-concepts/2026-10-22-phone-app/01-everyday.png) and [Board 04 right and left](../../../design-concepts/2026-10-22-phone-app/04-challenges.png). No blocking visual regression was found in this captured scope. Actual alarm admission/runtime and exact routine-screen reference acceptance remain outside that conclusion.

## Provenance and preserved order

The PNGs, two exact instrumentation logs, environment and unmodified passing summary come from `.android-local/ci-reviews/40607145222/silent-android-review/`. [original-sha256.json](original-sha256.json) records all 25 original artifact sizes/hashes; local attributes prevent conversion. [source-equivalence.json](source-equivalence.json) pins 16 captured UI, validator/routine-host, native-fixture, scope and landscape files to this docs branch's unchanged parent. Frozen root base `5bf66443` is a separate source state; no installation/integration claim is made.

[candidate-commit-order.json](candidate-commit-order.json) preserves full hashes after base `5bf664434ca303d26069e59287cc0e3c3a55a497`: Wake source `1c6b19ad`, existing Wake native bounds `83bd08cf`, shared Math source `2578269b`, stale-card fence `67cd8064`, question width `141f48c8`, existing routine-method continuation `b57b17d9`, focused scope `b25a80f2`. This docs-only archive follows without altering source, tests or workflow.

## Individually inspected frames

| Original PNG | Observation |
| --- | --- |
| wake-check-waiting.png | Scheduled 1:00, round filled sun, clear silent-check copy and Close; no premature acknowledgement. |
| wake-check-prompt.png | Still awake?, 2:00 timer, filled sun, centered clock/AM and full I'm awake action. |
| wake-check-countdown.png | Actual fake-clock remainder 1:15 and corresponding shortened ring; acknowledgement stays clear. |
| wake-check-recreated.png | Same 1:15 deadline/ring survives recreation. |
| wake-check-confirmed.png | You're awake and checkmark replace the timer; complete Done. |
| wake-check-reopened.png | Durable confirmed state reopens after fake-clock advance. |
| wake-check-storage-error.png | Full saved-state error and Done; no invented countdown. |
| routine-legacy.png | Legacy Math/three-round card, available tasks and fixed Save routine. |
| routine-ordered.png | Math, Memory, Retype appear in configured order with actual rounds. |
| routine-reordered.png | Retype moves ahead of Memory; numbering/details update. |
| routine-recreated.png | Actual Retype configuration retains three-round draft and Use Retype. |
| routine-removed.png | Memory removed; Math/two rounds and Retype/three remain. |
| routine-reopened.png | Saved two-task routine reopens with list/footer visible. |
| challenge-math-initial.png | Amber hero/round landscape sun, explicit practice context, cream card, 3 + 3 question, readable four-choice grid and full Check/Done. |
| challenge-math-selected.png | Wrong 8 has brown fill/cream numeral; question/order unchanged. |
| challenge-math-wrong.png | Try again below the same selected choice; full Check remains. |
| challenge-math-recreated.png | Correct 6, question and option order survive recreation. |
| challenge-math-completed.png | Explicit correct Check produces Practice completed, with disclaimer retained. |
| challenge-math-typed-wrong.png | 9 + 7, entered 0 and Try again clear in the real EditText. |
| challenge-math-typed-recreated.png | Question and entered 16 survive recreation; full Check visible. |
| challenge-math-typed-completed.png | Correct Check produces Practice completed without an alarm-dismissal claim. |

## Executed functional boundary

Wake uses private preferences/SQLite and injected clock, scheduler, notification/re-ring ports. Its selected method verifies WAITING to PROMPT, actual remaining deadline, recreation, explicit acknowledgement, durable confirmation, no fake re-ring after confirmation, reopen and malformed-state handling. Prompt/countdown/recreated captures assert the whole acknowledgement is visible and at least 64 dp high. The second WakeCheck method and actual notification/scheduling delivery were not run.

The existing routine method retains private-DB add/configure/reorder/remove/cancel/recreate/Save/reopen checks before its practice continuation. The continuation launches only the existing private RandomChallengePreviewActivity, uses shared production WakeChallengeUi/WakeMathCard widgets and MathTask choice/typed validators, and clicks measured full 48 dp targets. Wrong/correct answers, selected state, option order, recreation, typed entry and completion all execute. Typed values enter the actual EditText programmatically; keyboard/IME behavior is not proven. Practice preserves original preferences/default database and rejects any schedule attempt; owners are cleaned up. No new test method/batch was added.

AlarmActivity uses the same helpers with its actual routine caption/index, existing occurrence-fenced advance callback and task-identity fence. Its source diff changes hero/Math presentation and callback wiring; admission, engine, output, protection and emergency-stop behavior are untouched. **This native journey never launches AlarmActivity or bypasses admission.** Shared widget/validator proof through practice does not establish actual alarm-run dismissal, non-Math runtime or hardware protections.

## Reference differences and limits

Wake follows the amber landscape, clock/headline, circular sun/timer and brown acknowledgement hierarchy. Ring length follows actual remaining fake-clock time. Existing landscape details and extra silent-notification explanation differ from Board 04.

Shared Math follows the amber hero, landscape, cream card, large single-line question, two-column grid, selected contrast and brown Check. Practice intentionally says Task preview / Math practice and adds Back/Done; it displays no invented routine progress or ringing text. Actual production caption/dots and higher/multi-digit difficulty variants remain unexecuted. The question differs from the board's illustrative 3 + 4.

Routine retains numbered cards and available-task hierarchy. Fixture tasks/rounds differ from the illustrated Math/Memory/QR set. Configure chevrons replace plus controls, supporting types use More task types, and full-width Save replaces the pictured paired preview/save footer. These remaining organization differences are recorded, without exact-match acceptance.

Environment: disposable API 36 `sdk_gphone64_x86_64`, 1080 x 2316 override, density 420; host output disabled and optional output/import flags false. Physical phone, real alarms/audio/vibration/flashlight/sensors, OS delivery, full-screen admission, power/accessibility protections, process death and device/font variants remain unverified. This archive work ran no tests/build/CI/device/output action. Future candidate evidence does not establish a physical release, all 36 screens or whole-app acceptance.
