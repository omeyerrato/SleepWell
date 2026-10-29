# Cognitive retry feedback: six-frame native proof

[CI40829678446](https://github.com/omeyerrato/SleepWell/actions/runs/40829678446) passed at `b5afce4c4f681dd100de0c63db89a31710b081f0`: **20.525 seconds**, one existing `ChallengeRoutineFlowTest#orderedTasksConfigureReorderCancelRecreateAndPersistWithoutOutputs` method, all six required original PNGs, no failures. `cognitive-feedback` selects only its new private-practice continuation. The old58-frame routine journey was not replayed; no test method was added. Both optional output flags were false, and the original180-second timeout remains.

All six originals were opened individually against Board01's warm challenge card and Board04's routine context. These boards do not specify individual Sequence/Retype error screens. This proves the bounded inline-feedback correction at the captured viewport, not exact fidelity for an undocumented screen or all36-screen acceptance. The prior first-eleven originals show the Sequence Toast overlapping Cancel and the Retype native error popup covering Check. Those historical archives remain unchanged.

## Source and behavior

Only production `WakeCognitiveTask.java` changed, in `f6c0f467680e8bb921b351a3b17d8973a9b9eed9`, from base `e6a1707dc9e3e91f86fa48028c6a565495c324bf`. Sequence and Retype now use brown16sp status text inside the cream card. Native polite-live-region semantics expose the status without forced speech or output. There is no Toast or `EditText.setError` popup in these tasks. The responsive Memory block is byte-equivalent to the base.

Sequence retains its generated digits, original length rule, wrong-answer `fresh()` reset and2500ms reveal. Feedback clears on explicit Show or the next valid digit. Its Bundle restores retry status only with the same run/unit/type, generated sequence and entered input. Retype retains exact case-sensitive comparison after `trim()`, original practice length limit, and completion fencing. Feedback restores only with matching run/unit/type, phrase and exact rejected answer; changing that answer clears it. No saved preferences, record schema, admission, engine, art or shared Design changes were made.

Native commits `605a5871ec8ebeb276f1a8b0d2a693f10ff2273a` and `b5afce4c4f681dd100de0c63db89a31710b081f0` reuse actual Editor/ChallengePracticeActivity, production task controllers and generated puzzles. Read-only inspection obtains puzzle inputs; accepted keypad/Check/Cancel/Show actions use real native coordinate taps. The existing fixture enters text into the actual EditText on the UI thread; it does not claim physical keyboard input. Wrong answers do not earn units. Recreation retains error/input/sequence identity; explicit Cancel/Resume retains the run. Show clears Sequence status; the correctly repeated sequence advances to Retype with no inherited error. Editing and recreating the correct Retype answer clears feedback, and actual Check accepts its surrounding whitespace and completes the two-unit routine. Done returns to the editor. Detached controls are also exercised without advancing the current task.

Full visible status/Cancel/Check bounds and48dp action targets are asserted, along with nonintersection of status and Cancel, polite-live-region configuration and `EditText.getError()==null`. The private alarm backup and original preferences remain equal; scheduling and diary calls remain zero; services stay inactive. All owned Activities close and private factories/readers/timeline cleanup completes.

Scope commit `ae58fd8516f952647881ff5762588e11fb340c62` retains all49 existing image inventories and defaults. Existing Walk/Audio slice dispatch remains equivalent. Exact source hashes, unchanged named controller paths, Memory fragment hash and app-main tree are pinned in [source-pins.json](source-pins.json); this is not a whole-app equality claim. Production tree `7117117b75bc73bea04a68b4f83a05d4266d5935` is identical across both attempts. Final native compile passed43 seconds,25 tasks,1 executed/24 up-to-date; raw local compile logs are retained.

## Individual passing-original inspection

All PNGs below are untouched in `passed-40829678446/`.

| Original | Actual observation |
| --- | --- |
| routine-preview-cognitive-sequence-wrong.png | New sequence444 is visible with two-line inline retry status; keypad, Show and fixed Cancel all fit. No Toast covers Cancel. |
| routine-preview-cognitive-sequence-recreated.png | The same444 task and status survive actual Activity recreation; full controls and practice context remain visible. |
| routine-preview-cognitive-sequence-retry.png | Entered prefix4 remains visible after explicit Show/retry; status is absent and controls retain their normal placement. Subsequent repeated4 inputs genuinely finish the task. |
| routine-preview-cognitive-retype-wrong.png | MORNING9838 and rejected `wrong` remain visible; inline text fits above full Check. No red error icon or popup. Cancel is clear. |
| routine-preview-cognitive-retype-recreated.png | Same phrase, wrong answer and inline status survive recreation; native unfocused input underline is gray, with no popup or action clipping. |
| routine-preview-cognitive-retype-corrected.png | After Cancel/Resume, correction and another recreation, the full correct phrase remains and the error is gone. Actual Check subsequently completes the routine; this image is the pre-submit state. |

No new core clipping is visible in these six default-view captures. The unchanged amber hero, generated landscape art, cream card, original practice disclaimer and two-task progress shell remain. They are real practice UI, not a screenshot or bypass of actual AlarmActivity admission.

## Retained first failure and limits

[CI40828505805](https://github.com/omeyerrato/SleepWell/actions/runs/40828505805) remains **FAILED12.739 seconds** at `ae58fd8516f952647881ff5762588e11fb340c62`. Its three Sequence originals and failurePNG were individually inspected. Generic fixture text lookup found the entered prompt `4` before the keypad Button `4`, then reached no clickable ancestor. It stopped before all Retype captures. The native-only correction targets a Button inside the current Sequence card and dispatches each native tap once; no production code, timeout, guard or expected outcome was weakened. Its entire downloaded artifact, diagnostic XML/logs and failed summary are retained in `failed-40828505805/`. This failed run is not relabelled as a pass.

Android36 emulator,1080x2316 override/density420/default text size only. No physical device, native alarm admission, real alarm/service scheduling, hardware sensor/audio/flashlight output or physical TalkBack speech was tested. Polite status configuration is asserted, not a spoken-output claim. IME-open, large fonts, other viewports and full routine/hardware pools remain separate. Historical motion/audio/Light/Walk qualifications are unaffected. This branch does not change Root, publish/install an APK or update release metadata.

`passed-40829678446/` and `failed-40828505805/` preserve exact original artifact bytes, environments, raw method transcripts and combined summaries. Generated run/source metadata are separate files; `SHA256SUMS` covers the archive except itself.
