# Stale Math input: production guard and native ownership proof

[CI 40633181797](https://github.com/omeyerrato/SleepWell/actions/runs/40633181797) **passed**, head `95716b6adf14272ddc7d9a946a76161d3cf53563`: one existing method in **51.773s**, fourteen original PNGs, no failures. Scope `math-owner`; selector `org.sleepwell.app.ChallengeRoutineFlowTest#orderedTasksConfigureReorderCancelRecreateAndPersistWithoutOutputs`. Native sounds and audio imports were false; host output was disabled; `full_app_review` is false.

The source, native continuation and verification metadata are separate commits: `9481eaef`, `a2cb1638`, `95716b6a`. Production/instrumentation compilation passed in 1m24; the final native continuation compiled in 30s. This branch does not publish or install a release. All fourteen PNGs were opened individually against [board 04](../../../design-concepts/2026-10-22-phone-app/04-challenges.png), with [board 02](../../../design-concepts/2026-10-22-phone-app/02-alarms.png) for alarm styling context. Original PNG, method log, environment and summary bytes remain unchanged.

## Source defect and bounded repair

Source inspection found that a retained Math callback checked only its own Activity's MathTask identity. Pausing/destroying an old Activity did not invalidate that field. After another owner advanced the same occurrence, the old Check could still enter advance with its old local unit; advance lacked durable unit equality. It could publish stale progress and clear the current question. The old choice callback could also mutate selection/build without current run, unit or lifecycle ownership. This is a code-path finding; the native journey does not admit a real alarm to reproduce that complete path.

The real AlarmActivity callbacks now use MathAnswerOwner through WakeMathCard's guarded overload. It checks the main thread, resumed/nonfinishing/nondestroyed focused Activity, attached visible card rooted in that owner's window, question identity, local occurrence/unit and persisted ringing/run/unit/exact question cache. Guard and callback share Scheduler's existing lock. Rejection happens before choice, build, empty/wrong feedback, correct callback or keyboard Done mutation. Existing practice callers retain the original unguarded API. The central advance lock additionally requires durable expected-unit equality.

There is **no global focus/resume condition on advance**. Authenticated camera, pose, picture, QR, NFC and other asynchronous proofs may arrive before parent resume/focus; their original admission paths remain unchanged. See [exact production diff](production-fix.diff) and [security alignment report](security-alignment.md).

## What genuinely executes

The same existing routine method still performs actual editor navigation, configuration, drag reorder, cancellation, recreation, Save alarm, reopening and strict malformed-backup rejection against private SQLite. A second alarm remains unchanged. Its scheduling port observes only the disabled fixture alarm. Editor owners close and factories restore before the Math practice continuation starts.

The real RandomChallengePreviewActivity and production WakeMathCard/MathTask execute choice and typed practice, wrong/empty feedback, stable question/order/selection/text across actual Activity recreation, and actual practice completion. The eight Math captures show these normal production practice routes.

A further assertion-only continuation uses that real Practice Activity/card and the **same production MathAnswerOwner and guarded WakeMathCard overload** with isolated SharedPreferences. It confirms valid physical choice/Check input is admitted and correct submission invokes its private callback exactly once. It rejects retained callbacks with a different task object, changed cache, ringing off, changed durable/local run, changed durable/local unit, hidden/detached card, a real focused dialog, real same-task child pause, actual owner recreation, finishing and destruction. A current owner works again after dialog/child return. Guarded typed empty input cannot change feedback when stale; correct typed Done cannot advance a stale unit, advances the private callback once when current, then cannot submit twice. Off-main admission returns before reading Views.

Rejected-input assertions compare the entire private preference map, selection/progress callback count and feedback text. Real lifecycle callbacks supply resumed state. Only practice Activities are allowed; AlarmActivity admission is forbidden. The assertion-only guard cards are not additional captures and are not labelled actual alarm screens. All owned Activities close before private preferences are deleted and static scheduling restored. Global preferences and the original database backup remain unchanged; AlarmService/TrackingService remain inactive and global ringing remains false.

## Each original inspected

| Original | Observation |
| --- | --- |
| `routine-legacy.png` | One numbered Math card, real three-round summary, supporting task rows and complete Save footer. |
| `routine-ordered.png` | Three distinct task cards, two-round Math/Retype summaries, full list/footer without overlap. |
| `routine-reordered.png` | Actual order Math/Retype/Memory; drag controls and supporting rows remain visible. |
| `routine-recreated.png` | Retype child retains three-round draft and complete Use Retype action. |
| `routine-removed.png` | Two remaining cards show Math two rounds and Retype three rounds; full Save footer. |
| `routine-reopened.png` | Saved two-task order and round counts reopen unchanged. |
| `challenge-math-initial.png` | Real 7+3 question, four answer cards, disclosed practice state and complete Check/Done. |
| `challenge-math-selected.png` | Answer 14 visibly selected in brown; question/order unchanged. |
| `challenge-math-wrong.png` | Same selected wrong answer with red Try again feedback. |
| `challenge-math-recreated.png` | Same question/order with correct answer 10 still selected after recreation. |
| `challenge-math-completed.png` | Practice completed feedback; same correct answer and full controls. |
| `challenge-math-typed-wrong.png` | Real 8+6 question, typed 0 and wrong feedback; no keyboard occludes the capture. |
| `challenge-math-typed-recreated.png` | Typed 14 and question survive recreation with Check/Done complete. |
| `challenge-math-typed-completed.png` | Correct typed completion visible; no new clipping or footer overlap. |

No new visible regression was observed. Existing reference differences remain: task-dependent content/round summaries differ from the illustrative board; task choices use configuration chevrons rather than its add glyphs; the routine preview remains in its genuine More route instead of a second fixed footer button. Practice explicitly discloses that it cannot complete an alarm, uses a separate Done action and has no live alarm countdown. These are not claimed as exact screen completion.

## Equivalence and remaining boundary

`source-equivalence.json` pins exact production/native bytes at their separate commits and CI head; all five named files also match read-only Root `e08c945b`. It separately hashes workflow/script and records the structural audit: removing only the new scope restores the original Python AST and workflow bytes, preserving every prior manifest/default. SHA256SUMS covers all accompanying files.

The complete AlarmActivity onCreate/onNewIntent admission, central advance/dismiss, authenticated child-result integrations, AlarmService scheduling/stop, protection ownership, wake checks and physical audio/vibration/light are **not executed here**. The private callback updates isolated progress instead of calling AlarmActivity.advance. Existing AlarmScheduling.Port does not intercept Scheduler.at/cancelBackup/fire or direct Service output construction; no broad port rewrite was introduced. A complete inert real alarm admission/answer/dismiss journey remains open. No external component exposure, OS permission, personal-device, every font/device, all 36 screens or frozen-release acceptance is claimed.
