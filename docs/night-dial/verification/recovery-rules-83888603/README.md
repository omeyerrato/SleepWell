# Recovery and automation receipts at 83888603

[Run 41265280017](https://github.com/omeyerrato/SleepWell/actions/runs/41265280017) reports **PASS: both selected native methods and all 25 required original images**, with `failures: []` and `full_app_review: false`. The unchanged [environment](environment.json), [focused summary](focused-review-summary.json) and two instrumentation logs are retained here. No new build, test run, capture or device action was performed for this archive.

| Exact selected native method | Original result | Time | Required originals |
| --- | --- | --- | --- |
| `InterruptedRecoveryFlowTest#unfinishedRecoveryReviewKeepSetupAndNamedDiscardRejectReusedIdentity` | `OK (1 test)` | 21.427 s | 15 |
| `AutomationStorageTest#configReopensWithoutPlaintextCredentialsInRows` | `OK (1 test)` | 31.227 s | 10 |

[native-methods.json](native-methods.json) binds each method to its exact log and emitted success-frame names. There is no success JSON writer or invented proof file for either method. The automation method includes its actual `RuleJourney` UI sequence; this pass is not a run of the other methods in `AutomationStorageTest`.

## Source and environment

The original environment pins source `83888603122154abc9c1323d6ea6e65c611167af`. Its `app/src/main` tree is `c96213193398a7b3defc1f34edef5fa5ef25fc9d`: database **18**, portable backup format **14**. [source-manifest.json](source-manifest.json) records exact source blobs/SHA-256, copied-receipt hashes and artifact 13067152870's original ZIP SHA-256 (`6b4de155160b197ed1989bcfca189beb8f503acc0e76a673f87126e12cfce9a5`, 3,389,299 bytes). This is source/tree identity evidence, not a separately computed APK digest.

The disposable emulator reports API 36, `sdk_gphone64_x86_64`, density 420, 1080 × 2316 override size (physical 1080 × 2400), three startup focus samples, and **`system_broadcast_idle: false`**. The exact environment is preserved rather than replacing that observation with another run's value. Host audio output is disabled and no native playback output is allowlisted. The selected fixtures use private UUID databases/preferences and injected inert recording, alarm and scheduling ports. No phone data, real provider operation or remote delivery is involved.

## Source-derived contract and actual assertions

All source references below are pinned to `83888603` in the manifest.

- `Store.java:14,49,84–98` and `InterruptedRecords.java:20–43`: schema 17→18 adds only the private `interrupted_reviews` table and `forget_interrupted_review` session-delete trigger. Recovery finalizes the actual retained checkpoint and creates its random token, canonical-database scope, exact typed raw-session fingerprint and detection time in the same transaction. Missing time is not filled. Receipts are local UX state, excluded from portable record payloads; portable backup remains 14.
- `InterruptedRecoveryFlowTest.java:83–86`: the native fixture constructs v17 by removing precisely those additive v18 objects and setting `user_version=17`, then opens the real helper. It asserts schema 18 and byte-equivalent JSON rows for existing sessions, epochs and alarms, an initially empty receipt table and unchanged portable format 14. This proves that constructed migration path; it is not a test against an independently supplied old-user database.
- `InterruptedRecords.require/keep`, `InterruptedRecordActivity.load/open/keep`, and `RecordReportActivity.load`: admissions verify the nonce, exact receipt/session identity, canonical storage scope and raw fingerprint. Reads and UI publication retain factory/lifecycle/generation guards. Keep deletes only the receipt in a transaction. The passing native sequence recreates and reopens review, checks the retained record/epoch fingerprint after Keep and rejects the retired token.
- `RecordReportActivity.java:19,27` and `ReportScreen`'s `readOnly()` branches: retained review preserves observed data while suppressing record mutation and raw-ID action escapes. The native method checks absence of Edit sleep, Report options, Explore recorded signals and View sound events, then recreates the report. Explicit History/Trends navigation leaves this read-only scope.
- `InterruptedRecordActivity.open` and `TrackingActivity.java:27`: Start a new night re-admits the retained identity and opens ordinary setup. The fixture's actual Start command would fail the test if called; its start count remains zero after setup and Back. There is no automatic restart of capture, invented interruption cause, replacement segment or stage estimate.
- `RecordEditorActivity.java:99–101`: discard uses the existing named editor confirmation and deletion owner. Receipt admission and final record deletion occur inside the same SQLite transaction; dialog generation retires old callbacks. The native method recreates the named confirmation, invokes its destroyed predecessor, cancels with Keep record, deletes/reinserts an identical raw session under the same ID, and proves that the trigger retires its receipt and the stale Delete cannot mutate the replacement. A separate fresh named confirmation actually deletes the admitted record and epochs. Other records and the fixture alarm remain; all-history erase subsequently retires all remaining receipts.
- `AutomationStore.java:24–27` and `AutomationStorageTest.RuleJourney`: rule changes and deletion use revision ownership. Current deletion and its queued deliveries commit in one SQLite transaction. The actual UI sequence asserts that a changed revision and its pending event survive stale confirmation, a retired dialog cannot delete the new revision, and the newly reviewed named revision and its queue are actually removed. Recreated test-event confirmation cannot replay the destroyed positive callback; an accepted blocked event remains one queued event after recreation. Only explicit actions call the **fake** scheduler. Real delivery is not exercised.

The automation fixture also asserts saved/draft separation, error/retry and reopen behavior, including a saved rule after an injected scheduling failure and saved rows after an injected load failure. Its test vault uses reversible Base64 encoding with a fixture prefix. The absence of literal credentials in those fixture rows and their successful reopening is **not** evidence of production encryption strength or credential-provider behavior.

## Actual visual inspection

All 25 originals were surveyed in three labelled contact overviews. Seven were then opened individually and copied without modification:

| Copied original | Observed state |
| --- | --- |
| [Review recreated](interrupted-recovery-review-restored.png) | Retained start/checkpoint and detection time, honest unknown cause/interruption time, Review / Start / Keep / Review discard actions. |
| [Read-only report recreated](interrupted-recovery-retained-report-restored.png) | One minute of retained recording and the saved movement interval; missing tail is not shown as recorded. |
| [Manual setup](interrupted-recovery-manual-setup.png) | Ordinary Settle in setup with explicit Start tracking; the native assertion proves no recording command ran. |
| [Named discard recreated](interrupted-recovery-discard-restored.png) | Exact retained record number, dated bounds, deletion scope and Delete record / Keep record actions. |
| [Stale discard rejected](interrupted-recovery-stale-discard-rejected.png) | Visible rejection of changed/unavailable retained identity; no replacement record selected. |
| [After successful deletion](interrupted-recovery-discarded.png) | The parent recovery page is now unavailable/empty, with Check again and Back. It does **not** automatically redirect to Tonight. The successful SQLite deletion is asserted separately by the native method. |
| [Automation busy recreated](automation-rule-busy-recreated.png) | Queueing the reviewed test is retained, with unavailable duplicate actions; the fake scheduler is held by the fixture. |

This is a bounded inspection of these originals and the overview, not all-state visual acceptance. The final named-discard overview frame also retains a transient toast from the preceding deliberately rejected stale deletion; it is not a second failure or a success message. The method's later database assertions establish actual deletion.

[original-captures.json](original-captures.json) inventories every required original's SHA-256, size and **1080 × 2316** dimensions, and distinguishes individual-original inspection from contact-only inspection. Contact sheets are derived aids and are not committed as original evidence. Full original galleries, logs and ZIP remain in the ignored `.android-local/ci-reviews/41265280017/` cache. The local attributes preserve copied bytes without line-ending conversion.

## Failed predecessor and limits

[Run 41262371127](https://github.com/omeyerrato/SleepWell/actions/runs/41262371127), at source `71181142772adb551904519382bb0828408cac36`, **failed both selected methods**. Its exact [environment](earlier-failures/41262371127/environment.json), [summary](earlier-failures/41262371127/focused-review-summary.json) and two failure logs are preserved unchanged. Interrupted recovery failed at line 98 with `visible Delete record`; automation failed on the stale activation-switch selector with a null `Switch`. The later source includes fixture corrections `378219c5` (screen-coordinate dialog taps) and `9d707ddd` (await the actual described activation Switch). Both sources share the exact `c9621319…` app-main tree. The earlier failure remains a failure; its PNG hashes/dimensions and original cache location are recorded in the manifest without duplicating its gallery.

The earlier [40A source/pending-plan record](../local-features-1c1fe0c1/PENDING-40A-INCREMENT.md) and [current-core receipts](../current-core-71181142/README.md) remain historical, separately scoped evidence. This archive supplies the later dedicated recovery/rules pass. It does not claim a process force-stop/reboot trial, production credential-vault validation, arbitrary migration coverage, real audio cleanup/output, real automation delivery or whole-app acceptance. No production feature or polish change accompanies it.
