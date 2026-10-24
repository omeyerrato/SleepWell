# Calendar alarm rules

Calendar rules are optional and off by default. Settings → Calendar rules requests
read-only calendar access, lists visible calendars, and saves selected all-day skip
and early-meeting rules. The phone needs an installed calendar provider with visible
events. SleepWell does not request calendar write access or read titles, accounts,
descriptions, attendees or locations.

Rules apply to enabled repeating alarms. One-time alarms and naps keep their own
times. All-day dates use the provider's UTC date convention and exclusive end date.
Timed canceled, declined and free events are ignored. Preparation time can advance
the wake-up deadline only within its configured limit and the same local day; it
cannot postpone an alarm or backfill an already passed time. No more than 31
consecutive exclusions are applied. Missing/stale/out-of-range evidence retains the
ordinary schedule.

Provider reads run on a worker. A content-change job and 15-minute periodic job
refresh a bounded 35-day horizon; Android may delay background jobs. A saved
occurrence receipt fences changed settings and preserves the original scheduled
hour when consuming an earlier alarm, preventing a duplicate at the old hour.
Skip/Undo and Tonight's next-alarm preview use the actual adjusted occurrence.
Configuration and tiny receipts are local and excluded from portable sleep backups.

Twelve pure policy tests pass. Four private-preference admission tests and one
native save/reopen flow are authored; execution and actual provider/OS delivery
remain unverified until recorded separately. No phone alarm or calendar access test
has been run. Calendar record writing/export is separate remaining scope.
