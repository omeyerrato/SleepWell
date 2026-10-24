# Bounded snooze duration choices

Snooze configuration now supports 1–15, 20, 30, 45 and 60 minutes, plus optional **Choose duration each time**. When enabled, the on-screen Snooze action opens an owned duration menu; it does not immediately schedule. Cancel/pause/recreation/new occurrence dismisses a pending menu without scheduling. Flip and volume-key snooze still use the configured duration.

The menu offers positive durations up to the current maximum after shortening. For example, 9 minutes shortened twice offers 1, 3, 5 or 7 minutes. A `SnoozeOffer` pins one occurrence and one prior snooze count. Selection rechecks run identity, count, resumed/current ownership, remaining budget, selected index and current maximum, under the same occurrence lock as actual scheduling. Scheduling then rechecks its own bounded duration/count. No choice dismisses a challenge or changes volume/output policy.

The old default remains fixed five-minute snooze with one repeat. All previously supported 1–15 minute inputs retain exact old shortening decisions. The optional `inputs.chooseSnooze` Boolean defaults false; previous two-field input objects still decode/encode unchanged, malformed optional values are invalid. Behavior templates preserve the setting and larger duration. Saved alarm rows retain normal portable restore-as-inactive behavior.

Integration prerequisite: parent-owned `Backup.validate` must accept `snoozeMinutes` up to 60 (and reject invalid `inputs`). Until that narrow compatibility change is merged, the new 60-minute portable native case is expected to reject under the old validator. No schema/format bump is needed for optional alarm behavior metadata.

This increment does not add alarm timeout, backup duration/repeat policies or an “until deadline” snooze. Existing escalation behavior remains intact.

## Verification

- `SnoozePolicyTest`: five pure cases, including every old supported duration/count combination and bounded choice lists.
- `SnoozeOfferTest`: three pure current/stale/cancel/index/budget/immutable-offer cases.
- Existing `AlarmInputsTest` checks immutable choice/template transfer.
- `AdvancedSnoozeStorageTest`: two private JSON/SQLite/template/portable cases, fake scheduler only.
- `AdvancedSnoozeFlowTest`: one configuration-only emulator method, no AlarmActivity/hardware/output: toggle, long duration select/cancel, recreation, explicit Save and reopen. Four originals: `advanced-snooze-{initial,draft,recreated,reopened}.png`.

Actual runtime duration selection and alarm delivery are not device-tested because the no-alarm restriction remains in force. Native configuration execution and screenshots are parent-owned CI gates.
