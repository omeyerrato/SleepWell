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
