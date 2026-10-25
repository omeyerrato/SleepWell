# Reviewed goal-derived alarm time (F103)

Edit alarm → **Use sleep plan** opens a private review using actual saved bedtime and sleep-duration goal. Both saved keys are required; built-in defaults are not presented as an existing user plan. Legacy saved values without a revision remain readable. Invalid/missing values show a concise error plus Refresh saved plan/Open Sleep goals; no data is repaired or invented.

The calculation is `(saved bedtime minute + saved goal minutes) mod 1440`, retaining minute precision and a same-day/next-day label. It copies a **fixed local clock time**, never follows later plan edits and never creates an elapsed-duration timer. The saved plan's wake-time field is shown separately if it disagrees with bedtime + duration. Current early-smart-wake/goal enforcement behavior is unchanged.

**Use fixed time** returns hour/minute to the existing editor draft; **Save alarm** is still required for persistence and the existing guarded scheduling port. Cancel/back/recreation do not write alarms. Saved-plan revision/values and review time zone are checked again both at acceptance and result delivery. A changed plan requires explicit Refresh/review. No alarm JSON metadata, database migration, new permission or Main route is added.

Date, repeat days, enabled state, protection/challenges, sound, screen/flashlight wake-up and all unrelated fields remain unchanged. Relative naps stay routed through NapActivity and never enter this action. A solar alarm explicitly says **Use as sunrise fallback**, retains its coordinates/offset/time basis and changes only the fallback clock fields; invalid solar/date metadata must be reviewed before applying. The action does not silently turn sunrise into a clock alarm or claim that a fallback is the primary wake-up time.

For a dated alarm, the selected date is the wake date and the bedtime date is the previous date only when the arithmetic crosses midnight. Its preview uses the existing `LocalDateTime.atZone` alarm resolution: spring-gap times move forward and autumn-overlap times use the earlier offset. Clock changes can make elapsed duration shorter or longer than the goal; the review displays that interval, including an honest nonpositive-interval warning for a bedtime inside a skipped clock period. A copied date never rolls forward; existing Save validation still rejects an enabled past date. Recurring alarms retain ordinary local clock/DST resolution, so a fixed time does not promise an identical elapsed sleep interval each night.

## Verification

The existing testing-setup skill's Java Views/JUnit4/UIAutomator + injected private storage/scheduling approach is reused. No real alarm, sound, microphone, sensor, phone UI or service runtime is invoked.

- `GoalAlarmTimeTest`: 11 pure tests cover precise midnight wrapping, valid bounds, rejected corrupt values, actual date/zone resolution, spring/fall elapsed changes, wake gaps/overlaps and skipped-bedtime nonpositive intervals. Direct javac/JUnit passed 11/11 (0.109s).
- `GoalAlarmPlanStorageTest`: 3 native private-preferences methods cover missing keys, typed corruption, legacy revision0, immutable reading, historical goal/unknown-key preservation and changed revision identity.
- `GoalAlarmTimeFlowTest`: 2 native private-SQLite/fake-scheduler journeys cover missing/invalid/stale plans, refresh, Cancel, child/editor recreation, draft-only application, explicit Save/reopen, later plan changes leaving saved alarms fixed, dated DST explanation and unchanged sunrise fallback/time basis. Unrelated private alarms and original production preferences are preserved by AlarmSkipFixture.
- PNG contract: `goal-alarm-{missing,invalid,review,recreated,copied,dated,sunrise-fallback}.png` (7).

Native execution/images and the scoped Android compile remain pending when this source document is created. Source/model tests do not establish real alarm delivery.

Reference behavior: [Sleep as Android — Create alarm, Sleep Goal Alarm](https://sleep.urbandroid.org/docs/alarms/create_alarm.html). This increment uses a reviewed persisted plan rather than automatically following plan changes.
