# Earlier Report first-method failure:41249512029

Actual source66 native run41249512029 failed
`NightDialReportFlowTest#latestReadOnlySignalsDraftGateAndReviewedRestoreKeepAnnotations`
after62.316s at `Scene.launch(RecordReportActivity)` (test line57), before restore
review. The second Report method passed19.341s and the Calendar method passed.
This failure remains a failure; later code is not execution evidence.

The actual original `night-dial-report-failure.png`, inspected at original
resolution, shows the already-open **7 h 42 min** read-only Report foreground.
At this step the owned fixture had been changed to7 h25 min and the test expected
a newly observed Activity followed by that edited report. The retained guest
logcat tail has no RecordReport/FATAL entries that establish a production crash.
Task reuse is an inference consistent with the actual old foreground Report,
the absent new-owner monitor callback and the fixture's `NEW_TASK`-only launch.
The newly added empty/peer Report entry supplies another existing Report task.

The fixture launcher now pairs `NEW_TASK` with `MULTIPLE_TASK` to require a fresh
owned task for its explicit launch contract. This is the documented behavior of
the [Android Intent flags](https://developer.android.google.cn/reference/android/content/Intent#FLAG_ACTIVITY_MULTIPLE_TASK).
No production navigation, duration calculation, save/restore owner, timeout or
assertion changed. The nonnull new owner,7 h25 min edited report, reviewed restore,
personal annotations and exact raw-data assertions remain required. No rerun or
build occurred here; integration owns the next execution.
