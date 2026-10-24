# Explicit-date alarms

The ordinary alarm editor’s **Run on** opens SleepWell’s calendar. Only **Use date** applies the selected date to the draft and clears repeat days. Cancel leaves the draft unchanged; calendar/draft state survives recreation. **Clear date** returns to the legacy next-occurrence schedule; choosing a weekday also clears a date. **Save alarm** retains the existing durable-intent and scheduling-error flow. An enabled past date is rejected before saving.

The optional `date` is canonical ISO local calendar date (1970–9999). It follows the phone’s local time zone, with the same gap-forward/earlier-fold DST resolution as existing local alarms. It yields one timestamp, then zero; it never rolls to tomorrow. The date participates in stale-editor ownership and actual delivery eligibility. Existing one-shot delivery disables it. Skip/Undo retains the selected date; skipping does not create a second date. Reconciliation cancels expired requests, clears their deadline and continues reconciling other alarms. A scheduling retry cannot turn an expired date into another day.

Absent metadata keeps legacy scheduling unchanged. Invalid/mixed repeat/nap metadata is retained as invalid for review, never interpreted as a daily alarm. Portable alarm JSON keeps the date; restoration remains inactive. Parent integration must include `dateInvalid` in strict portable backup alarm validation alongside its separately owned format update. No database schema change.

## Verification contract

- `AlarmDateTest`: seven host tests for one occurrence/expiry, timezone/DST, strict canonical input, smart-window/claimed/invalid fences, skip and unchanged legacy behavior.
- `AlarmDateStorageTest`: three private real-SQLite/fake-scheduler methods for exact saved occurrence/portable inactive restore, stale date edits and malformed conflict metadata.
- `AlarmDateFlowTest`: one emulator-only configuration method; owned date selector cancel/apply, draft recreation/save/reopen/clear; no actual scheduler or alarm output. Five originals: `alarm-date-{initial,selector,draft,recreated,reopened}.png`.

Local native compilation and CI execution are separate gates. No physical alarm delivery or hardware behavior is tested or claimed.
