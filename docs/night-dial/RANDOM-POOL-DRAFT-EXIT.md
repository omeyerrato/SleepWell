# 08D random pool exit review

Outer Back and Cancel now ask **Discard changes / Keep editing** when the random
pool or selection count differs from the initial child draft. Back inside a task
editor still returns to the pool with that edit retained. Opening random mode's
initial defaults alone does not make the child dirty.

The initial baseline is rebuilt from the original invoking parent routine before
restoring an edited child draft. Rotation therefore retains both the edit and the
correct comparison. Existing `PlanDraftExit` owns the native Night review,
recreation flag, foreground/focus admission and retired-dialog guards. The child
passes no Save callback. Discard finishes with the existing cancelled result;
**Use random routine** retains the original request/result boundary. Only the
parent's later Save alarm can write the alarm. No pool materialization, eligibility
fallback, enrollment, scheduling or storage policy changed.

One focused native journey is authored in
`RandomChallengeDraftFlowTest#dirtyBackKeepDiscardAndUseReturnOnlyParentDraftWithoutOutputs`.
It enters through the actual parent AlarmEditorActivity and configures real child
controls. It checks nested Back, dirty outer Back, Keep, Discard, clean Back,
review/draft recreation, a retained retired Discard view, and Use returning only a
parent draft which survives its own recreation. Both private fixture alarms are
disabled, schedules must stay zero, and original library/preferences/services
must remain unchanged. All real input uses the focused owned window, a committed
frame, complete visible 48dp targets and a single dispatched gesture.

This worktree ran no Android build, native method, unit campaign or phone output.
The [native plan](RANDOM-POOL-DRAFT-NATIVE-PLAN.json) remains pending root's
consolidated build and silent emulator execution. Screenshots alone do not pass
the functional assertions.

## First native run and coordinate correction

Run `41260557207` at integrated source `1c1fe0c1` failed after five captured
frames, while awaiting the parent after Discard. The original discard screenshot
shows the correctly named bottom sheet; the failure screenshot/XML shows the
still-edited child with Hard Math and count 2. This is not a passing journey.

The fixture incorrectly passed `getGlobalVisibleRect`'s root-coordinate rectangle
directly to `UiDevice.click`, which needs screen coordinates. A dialog's window
origin makes those different. The same helper also missed the requested count 1.
It now derives the actual hit rectangle using `getLocationOnScreen`, retaining
all focused-window, full-visibility, committed-frame, minimum-target and
single-dispatch checks. The selected count is asserted immediately, and the
post-Discard wait names its actual expected transition. No production behavior
or guard changed. The original nine screenshot names remain; a consolidated
rerun is required before claiming this flow passes.
