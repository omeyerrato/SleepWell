# Dated goal bank correctness and native controls

## Existing flow retained

SleepWell already stores immutable 14- or 30-day duration/bedtime commitments in `goalCommitments`. This change extends that existing flow rather than adding a second bank, rewriting old plans, or changing the database/portable format. Targets, start dates, frozen time zone/day boundary, motivation and stopped history remain unchanged. Plans and commitments remain local settings and are explicitly excluded from the portable backup. No alarm, reminder, smart-wake rule or recording preference changes merely by inspecting this bank.

## Correctness change

Before this change, Trends applied today's plan duration to every observed historical day. A new ten-hour plan therefore rewrote the apparent gap for an older eight-hour commitment. Trends now uses only closed dates belonging to saved duration commitments and their original target. A day with no recorded coverage remains missing, and an uncommitted date creates no debt. Current and future days do not accrue a deficit. Bedtime commitments remain a separate metric and are not summed into duration minutes.

`DatedGoalBank` is the shared ledger used by Trends, the Sleep goals row and the goal detail. Activities calculate immutable progress/range snapshots on their existing reader workers from pinned repositories and a single captured Instant; request generations reject stale publications. Rendering never reruns the goals-by-records calculation. Loading/error copy identifies a pending or unavailable refresh. It retains `GoalProgress`'s frozen-zone sleep-day and DST boundary rules, edited/manual intervals, pauses and overlap arithmetic. Recorded minutes are credited once across duration commitments, in chronological start-date order. An adjacent commitment in another frozen zone can label the same real interval as another date; already credited minutes are excluded. A wholly repeated interval stays unknown rather than becoming a duplicate eight-hour surplus or an invented zero-hour deficit. A real zero-duration overlay (all recorded time explicitly marked awake) remains a recorded zero, while empty interrupted-recovery rows do not invent an observed night. These balances describe recorded coverage, not measured sleep onset or clinical sleep debt.

The plan row prefers the current duration period, then actual duration history, then the next future duration period; a future or bedtime commitment no longer masks it. The plan link carries the exact goal ID. Trends opens an actual selected-range total and its per-period contributions; each row opens its exact goal, so an aggregate is not confused with one individual period. Trends' dashed chart reference is explicitly the **current** plan goal; its bank amount uses the dated commitments instead. Missing/corrupt goal history keeps the ordinary history graphs available and exposes an unavailable bank.

The existing goal editor now uses native SleepWell-owned cream duration/time/calendar controls, supports cancel and recreation of pending values, and keeps a failed Save message inline across recreation. Overlapping-period rejection does not replace the saved history. The stop confirmation uses the same typography/surface and preserves its existing explicit action.

## Sources and visual reference

- Supplied board05 Sleep goals and board08 Trends were individually opened before implementation. Their cream/brown typography, compact bank row, chart hierarchy and detail navigation are retained; the unmocked editor uses those controls and surfaces.
- [Official Goals manual](https://sleep.urbandroid.org/docs/sleep/goals.html): 14/30-day duration/bedtime goals and observed surplus/shortfall bank. Independently implemented, no wager/payment.
- [Official Statistics manual](https://sleep.urbandroid.org/docs/sleep/statistics.html): duration goals and sleep-day cutoff. Android/java.time explicit zone and elapsed interval arithmetic govern the actual stored calculation.

## Verification

New `DatedGoalBankTest` contains nine pure regressions: historical target preservation, adjacent changed targets, missing/current days, preferred exact identity, zone-transition duplicate coverage, effective edits/overlap, selected-range slicing, repeated DST boundary, and stopped/empty recovery rows. The existing `GoalModelTest` assertions remain intact.

`DatedGoalBankFlowTest` is two inert Android methods with ten planned original PNGs:

- dated-goal-plan.png
- dated-goal-history.png
- dated-goal-trends.png
- dated-goal-linked.png
- dated-goal-empty.png
- dated-goal-target.png
- dated-goal-calendar.png
- dated-goal-saved.png
- dated-goal-error.png
- dated-goal-error-recreated.png

The fixture uses owned UUID preferences/database, real committed records, actual editor/calendar controls and a reminder port that must never fire. It verifies target/date cancel, pending-calendar recreation, Save/reopen, historical gap/identity links, persistent rejection, full original database fingerprint/preferences preservation and cleanup barriers. Existing `GoalFlowTest` keeps all prior assertions while selecting dates through the new owned calendar.

Final scoped build `13771` passed debug/test APK assembly, 31 pure tests in two suites (22 existing + nine new), and lint with zero errors/93 warnings in 1m55s. This includes the final off-main immutable snapshots and range-overview navigation. Native bodies compiled; they have not executed. The new native methods and screenshots have not run yet. Actual native originals must be inspected against boards before any visual acceptance. No phone, hardware, alarm/service, output or real reminder test is authorized or claimed here.
