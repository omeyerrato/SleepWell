# Report state increment: pending native acceptance

1 November 2026. Base: `9843587f`; branch:
`codex/report-state-usability-20261101`. This is a source-specific register,
not full 43-flow acceptance. Native Java Views, Night Dial tokens, actual saved
data, raw originals and existing save/correction owners remain.

## Inspected baseline

Read the accepted `design.md` sections/flows 26–28, 41 and 43 and the corresponding
boards 26, 27, 28, 41 and 43-v2. Inspected all 16 actual Report PNG originals from
native run `41212354914`, whose receipt pins source
`351b78bf0436412d8edd889d7eb234be0bf4f8b3`, reports PASS, no missing frames and
units not run. That is historical evidence; it does not verify this increment.

Those originals show the real 7 h 42 min duration ring, source/dated interval,
measured movement, unavailable Light selection, preserved Noise tooltip,
readable Imported filter/result, owned dirty Back and restored confirmation,
annotation-preserving original-time review, honest Week/Month/Year coverage,
and unknown empty ranges. They fit Night Dial, including readable actions and
system-bar clearance. No material visual defect was established in those frames.
The fixes below address previously uncaptured interaction/state gaps.

## Concrete behavior changed

- Empty Report offers **Start tonight** and **Add manual night** directly.
  History and Trends remain reachable while empty.
- History updates its visible search scope and **Clear filters** while typing.
  History and Trends retain separate scroll positions through re-render and
  recreation. Their scope, selected date/range and both scroll positions travel
  through the peer Night route, without adding persistent preferences or changing
  saved records. Requested History/Trends destination still wins over the saved
  view selection.
- A selected date offers an explicit saved-record action, or **Add period on
  selected date** with unknown-duration copy. If records exist outside the current
  filters, the action explains that and offers to clear the other filters while
  retaining the date. It does not open an unrelated filtered-out record.
- Failed local History/Trends refresh keeps its last-good rows/metrics and dated
  check timestamp with **Retry records**. The read-only Record Report similarly
  retains its last-good snapshot and **Retry record**, including recreation.
  Updating ends on failure. A confirmed missing record discards that snapshot;
  deletion cannot be presented as a current saved record. These in-memory caches
  contain no Activity, worker or audio, and are not written to disk. After process
  death, data must be read again. MainActivity's synchronous root-store ownership
  is unchanged; this does not claim every application refresh branch is covered.
- A manual save failure and the existing draft survive editor recreation. Only
  the existing explicit Save owner can commit a creation or edit.

## Finite state/evidence register

“Pending” means implemented/asserted in the named existing native cohort below,
not executed on this source. Baseline references are the 16 originals above.

| State | Branches and current evidence |
| --- | --- |
| 26A | Saved Night duration/source, read-only opening and return: baseline; current repeat pending. Empty Night/direct manual route: pending `empty`. |
| 26B | Actual Noise scrub, unavailable Light and inspected timestamp recreation: baseline `signal-scrub`/`signal-restored`; current repeat pending. |
| 26C | Sound correction/favorite/play retains its existing owner. No new output or correction acceptance in this cohort; remaining interaction combinations open. |
| 26D | Dirty Back/Keep/Discard and draft restoration: baseline; current repeat pending. Raw data unchanged and reviewed time restore with personal annotations retained: baseline; current repeat pending. |
| 27A | Source filters and Imported identity: baseline. Empty list, typed no-results, Clear filters, query recreation and Night-peer return: pending `history-empty`, `history-no-results`, `history-query-returned`. |
| 27B | Existing-date open and empty-date add using the real selected date: pending `calendar-empty-date` and data assertions. Owned calendar pending selection/Cancel/Use/recreation: pending seven `calendar-*` originals and exact committed-day assertions. |
| 27C | Manual selected-date creation, no sensor epochs, failed Save/draft recreation/retry and saved Manual report: pending `manual-save-error-restored`/`manual-date-saved`. Invalid time/DST/seconds boundaries remain owned by the existing time editor and calendar cohort. An explicit cross-record overlap review is still open: the current data model saves separate periods and duration trends count overlapping coverage once; this increment does not invent rejection/merge behavior. |
| 27D | CSV picker/reconciliation/import provenance remains with its existing owner. Not exercised or newly accepted here. |
| 28A | Week/Month/Year true averages and exact coverage: baseline; current repeat pending. Empty-range actions and real scrolled recreation: pending `trends-empty-restored`. Read-details selection is retained; extended day/year point-device acceptance remains open. |
| 28B | Rhythm and admitted insufficient-observation calculations remain existing behavior. Additional day-point/context coverage is open. |
| 28C | Tag contributing-group navigation and taxonomy operations remain existing behavior; not newly accepted by this cohort. |
| 28D | Chronotype insufficient-data and underlying-night routes remain existing behavior; not newly accepted by this cohort. |
| 41A/B | Tag quantity and global rename/delete scope are unchanged; full interaction acceptance remains open. |
| 41D | Restore original times/awake edits keeps notes/tags/rating by default: baseline; current repeat pending. Optional explicit annotation reset remains open. |
| 43A | Empty Report → empty History/Trends and direct manual entry/Cancel without a write: pending `empty`, `history-empty`, `trends-first-night`. |
| 43D | Retained read-only Report and History/Trends local refresh error/retry/recreation: pending `refresh-failed`, `refresh-retry`, `trends-refresh-failed`, `trends-refresh-retry`. Confirmed missing record clears cached report: pending `removed`. No network advice or automatic output is introduced. |

## Exact bounded native handoff

Run only these three existing methods on the disposable host-muted API36 review
emulator with `ciReview=true`, without units:

1. `NightDialReportFlowTest#latestReadOnlySignalsDraftGateAndReviewedRestoreKeepAnnotations`
2. `NightDialReportFlowTest#canonicalWeekUsesObservedDaysAndMissingRangesRemainUnknown`
3. `RecordCalendarFlowTest#pendingCalendarsRecreateCancelAndCommitWithoutChangingOffsetsOrSeconds`

The first two are extended existing journeys, not new test methods. They use
isolated UUID SQLite/preferences, actual native inputs and owned synthetic saved
records; failures are injected only into that fixture's record factory. Manual
Save actually commits through `RecordSaves`; no sensor epochs are generated.
The confirmed-deletion assertion removes only its owned synthetic fixture.
The prior saved-data/raw-original assertions remain. Reader/save completion and
window teardown precede fixture cleanup. No capture, playback, model evaluation,
OS alarm or personal phone action is in the cohort.

The existing calendar journey's two assertions formerly inferred date-filter
commit from generic helper text. Source inspection proves that helper is hidden
for the All filter, independently of date selection. They now inspect the owned
committed `day` on the main thread: Cancel leaves null, Use date equals the exact
pending date. Native calendar/offset/seconds/recreation and saved-data assertions
are preserved.

[Machine-readable pending scope and all 37 required original filenames](REPORT-STATE-NATIVE-PLAN.json).
Require every actual original, inspect all 30 Report and seven Calendar originals
against the accepted boards, and archive source/run/log/hash provenance before
marking these increments accepted. Screenshots are not present for this source
yet. No build, native run, unit campaign, phone, output or model replay ran in
this worktree. Full-app, provider, overnight, landscape/TalkBack and remaining
matrix branches remain open.
