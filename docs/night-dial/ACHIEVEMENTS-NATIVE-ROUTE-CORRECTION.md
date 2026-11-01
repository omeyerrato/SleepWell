# Achievements native route correction — 2026-11-01

The Achievements method in [run41254733103](https://github.com/omeyerrato/SleepWell/actions/runs/41254733103)
tested `ec2a1647365f19e7d30269994640912c9a5f5d27` and failed after 12.525 seconds.
Its full per-method log points to the assertion that Notes or dream exists
immediately after opening the searched History row. The actual original failure
PNG was inspected at 1080×2316. It shows the selected four-hour period's read-only
Your night report and its pencil Edit sleep action. The matching hierarchy has
that clickable Edit sleep content description and no Notes or dream editor.

The native journey treated the Report action's accessible name as proof that
RecordEditorActivity had opened. The current route intentionally opens
RecordReportActivity from a saved History row; choosing Edit sleep then opens
the editor. The correction follows those actual actions. It requires a focused
read-only Report for the exact selected record, taps its Edit sleep action,
then awaits a focused RecordEditorActivity for the same record and its notes
draft before entering and saving the note. Production routing is unchanged.

All saved-contributor recalculation assertions remain: the note must persist,
the actual trim must reduce qualifying days to zero, all 240 original epochs
must remain, reopen must retain the note and two-hour duration, and milestone
contributors, error/recreate/retry and first-seven/30-day basis checks continue.
No assertion or timeout was relaxed. The same native method and its existing
sixteen original capture names remain the pending integration cohort:
`RecordAchievementsFlowTest#reportActionPrivateSavedEvidenceEditSearchRecreationAndReopenRecalculate`.

The earlier failed run and artifacts are retained. Local validation covers the
diff and preservation of the capture/assertion set. No build, new hosted cohort,
emulator, phone, model evaluation or output action ran for this correction.
Current native and visual acceptance remain pending the consolidated rerun.

| Earlier failed artifact | SHA-256 |
| --- | --- |
| `RecordAchievementsFlowTest#reportActionPrivateSavedEvidenceEditSearchRecreationAndReopenRecalculate.txt` | `c99fde763f54792303cae3dc4a0581d73158a3d648e731560bb3c4ec94ef1bf5` |
| `record-achievements-failure.png` | `328d1bded55560465348b4680c41a01f89f3a5eb855b284fff20c952058f2d20` |
| `record-achievements-failure.xml` | `b8786600dcc2ac355ce191d9bf1cd1c817147eb6956124e703a977eb5fc48a65` |
| `environment.json` | `17d9f6e5fa0e24148d6bee6a8cb0b902483ca7a7bf4a246c5af1c6f0be52f378` |
