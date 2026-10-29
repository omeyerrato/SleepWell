# Ordered routine preview: future increment in progress

This work is isolated from the released app. Base: Search-complete Root `000458fd`; branch `codex/routine-preview-20261029`. The earlier prerequisite branch `codex/routine-preview-investigation-20261029` is preserved. **The feature remains incomplete until all 28 task adapters and actual random-pool execution work and are verified.** No native execution, hardware action, release or phone operation is claimed by this source preparation. Initial source chain on this Search-complete base: `ea5e5035`, `e1e575c1`, `39ee226b`, `6e9f2910`, `2840f25c`; native continuation is separate.

## Implemented foundations and first vertical slice

The new internal, non-exported `ChallengePracticeActivity` owns an immutable copy of the current alarm-editor routine draft. `ChallengePracticeSession` uses the real `ChallengeRoutine.at/advance` unit mapping and preserves configured rounds, current puzzle/input and ordered progress across recreation. Random examples use the existing read-only selection function once and retain that concrete selection across recreation; their copy explicitly separates this example from the next alarm's independently admitted selection. Preflight examines every configured pool entry, so an unsupported task cannot be concealed by a lucky random sample.

`ChallengePracticeOwner` gates synchronous input before mutation using the actual main thread, resumed/focused live Activity, attached/shown card, exact run/unit/configuration and render generation. It does not invent ringing preferences or reuse an alarm admission. `PracticeCheckpoints` is a bounded memory-only SharedPreferences port (128 keys / 64KiB) for the existing controller algorithms; its state is carried in the preview Bundle. It never opens a preference file or alarm database. Current answer state is separately bounded to 64KiB. Oversized or invalid state fails explicitly instead of silently replacing the draft.

The first implemented dispatch covers all 11 screen-only types: Math, Memory, Sequence, Retype, Visual search, Counting, Flags, Mirror text, Dream journal, Jumping and Type code. It uses the shared production engines/widgets. `WakeCognitiveTask` extracts the actual Memory/Sequence/Retype interactions; the real AlarmActivity delegates those three synchronous cards and retains its original admission, durable advance, authenticated child-result, scheduling and dismiss paths. It now cancels/resumes cognitive timers around focus/lifecycle changes and can restore earned Memory/Sequence state. Actual AlarmActivity admission/output remains outside the planned private-practice proof.

The existing TypedWakeTasks constructor keeps its real JournalWrites default. Its explicit practice overload uses the identical JournalRule engagement validation but labels the action **Check practice entry** and never starts a diary write, including when presented with a restored writing flag. Mirror/journal draft values retain their existing field limits. Retype's shared input has at least a 48dp height; only practice adds a 120-character answer cap.

The challenge footer now contains genuine **Preview routine / Save routine** actions at its existing height. Visible **Cancel** moves to a full 48dp header text action beside Back/More and retains its existing accessibility selector and exact discard behavior. Partial preview results return to the editor through a one-use request/snapshot check; Resume retains the private progress, and changing the routine invalidates that state. Completion clears the resumable state. No Save alarm call is added. Results may arrive before onResume; private state is published then and rendering waits for a resumed, focused owner. The Search restoration source is retained.

## Remaining hardware/result work

All remaining 17 types are deliberately preflight-blocked until their real routine adapters exist. They are not silently skipped or converted to an invented fallback. The remaining work comprises Shake, Walk, NFC, Tilt maze, Light, Spin around; Speak, Laughter, Loud sound, Spoken phrase, Musical note; QR code, Picture match, Object recognition, Smile, Pose and Camera light.

The private `PracticeChildTicket` foundation binds canonical task configuration, preview run, global unit and nonce. It is separate from every alarm proof. A matching child must confirm its own cleanup before publishing a one-use result; success additionally requires a real started attempt and visible child owner. Parent receipt consumption does not require parent focus/resume. Worker capture checks use only a main-thread-issued volatile attempt lease, not Activity Views. Cancel retains the same unit. A fallback selects a separate real Math practice step, using the configured QR fallback where relevant; it never reports original-task success. Pending children invalidate synchronous parent card admission.

Next adapters must start physical capture only on the explicit per-step action, never after merely returning from permission/Settings. They must yield to real alarms/night capture and cancel only their own matching practice ticket, with no global stop command. Existing NoteCaptureService may be reused only after explicit Listen for its foreground microphone cleanup owner; no AlarmService/audio output/protection/scheduling is allowed. Missing process-local receipt ownership must fail closed at the same preview unit.

## Native preparation, not acceptance

Only the existing `ChallengeRoutineFlowTest#orderedTasksConfigureReorderCancelRecreateAndPersistWithoutOutputs` is extended, through `RoutinePracticeFixture`. Its original 18 captures/assertions are retained; no @Test is added. The continuation uses actual editor launch/result actions, both private native owners, a UUID private alarm store, forbidden scheduling and diary-write ports, existing fake JumpTimeline clock/frame delivery, real puzzle validation and actual touch actions. It reads a documented finite set of current model/controller fields to solve the displayed puzzles and compare state; it never injects earned progress or invokes a completion method.

Two ordered runs exercise the 11 screen-only types and their configured rounds; a real random example is completed after proving selection restoration. The continuation prepares wrong journal validation, partial-entry Cancel/Resume, destroyed/focus/unit rejection, actual recreation, full exit-target bounds and an honest unsupported NFC page. Original preferences and the private saved-alarm backup must remain unchanged. Diary/record factories restore only after owners are destroyed and journal work drains. Actual alarm admission/output, all 17 hardware adapters and their consent/result/resource boundaries remain unverified.

Proposed 20 additional original names (one existing method, original mappings unchanged):

```text
routine-preview-math.png
routine-preview-math-round2.png
routine-preview-memory.png
routine-preview-memory-recreated.png
routine-preview-sequence.png
routine-preview-sequence-recreated.png
routine-preview-retype.png
routine-preview-counting.png
routine-preview-flags.png
routine-preview-mirror.png
routine-preview-journal-invalid.png
routine-preview-journal-recreated.png
routine-preview-completed.png
routine-preview-search.png
routine-preview-type-code.png
routine-preview-jumping.png
routine-preview-second-completed.png
routine-preview-random.png
routine-preview-random-recreated.png
routine-preview-unsupported.png
```

The separately authorized `routine-preview` verification scope selects only this one existing method and its 18 retained + 20 new originals (38 total), with both optional output flags false. Other mappings/defaults remain unchanged. Its companion verification branch will carry the scope metadata and actual evidence; no result is claimed here. Production and instrumentation compilation passed after editor/native integration in **2m28s**, 25 tasks / 2 executed / 23 up to date. Board 04's full Preview routine gap remains open. No all-28, all-36 or hardware acceptance follows from compilation.
