# Fast diary completion handoff

CI40154059415 `TypedWakeFlowTest.silentMirrorJournalAndDiaryActionsRequireRealInputAndPersistence` failed after its confirmed delete while waiting for the empty diary state. The failure PNG/XML show the editor still present, with neither a confirmation dialog nor a busy/error message. Earlier mirror, journal, edit and delete-confirmation captures were produced.

`DiaryActivity` handled a waiting operation's terminal transition only inside the poller's `drawn != job.state` branch. The explicit save/delete action starts an asynchronous operation and immediately renders. If the small private SQLite operation finishes first, that render records the terminal state in `drawn` while leaving `waiting` and `editing` unchanged. The poller sees no new identity and cannot settle the editor. This is a timing-dependent UI handoff; increasing a test timeout cannot fix it.

The same existing success/error transition now runs inside `render()` before assigning `drawn`. Both explicit rendering and poll rendering therefore consume terminal state. Success returns to the real list; errors clear the wait while preserving the editor. No storage admission, conflict checking, confirmation, schema, data format or native test assertion is relaxed. Existing `TypedWakeFlowTest` is unchanged.

`DiaryTerminalStateTest` adds one native private SQLite/Activity method with 0 PNG. It deliberately waits on the private worker's queued barrier while the test owns the main thread, then renders, forcing completion-before-render without relying on scheduler sleeps. It verifies actual save persistence, a stale-revision error retaining the editor and unchanged saved row, then a real delete returning to an empty list. The barrier is test-only and bounded to three seconds; production rendering never waits on a worker. No alarm, microphone, hardware, network or personal-phone test is performed.

At author handoff this narrow source/test increment is uncompiled and unexecuted, per root's exclusive combined-build coordination. Fresh native execution remains required to verify the regression and the original end-to-end fixture.
