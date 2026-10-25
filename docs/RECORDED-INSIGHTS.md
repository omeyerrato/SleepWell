# Recorded-night insights

This increment extends F074/F075 and provides a narrow original descriptive part of F078. Open **Report → Report options → Recorded insights**, or **History → Trends → Recorded insights**. It adds no clinical score, inferred sleep stage, sleep-onset/latency estimate, chronotype or prediction of an ideal bedtime.

## Actual inputs and behavior

The native screen reads effective completed saved records through the existing `Records` projection. Pauses, trims, marked-awake periods and imported Health Connect awake periods therefore affect the same durations used by History. It offers 7-, 30- and 90-calendar-day windows; choosing a window saves that display preference in a separate private preferences file. Metric selection survives normal Android recreation. Opening from a saved report anchors the view to that record's existing sleep-day label; Trends passes its actual selected anchor.

The coverage card reports observed and missing dates. Missing dates contribute neither zero duration nor a deficit. A valid saved period whose entire interval is explicitly marked awake has known zero remaining recorded coverage. Daily overlaps merge, and duplicate physical coverage assigned to another zone/date cannot create a second zero-duration observed day. Duration average, median and range use observed dates only. Naps count in daily recorded duration; provenance counts identify phone/legacy, manual and provider-imported periods. Interrupted records remain visible as incomplete coverage.

The chart changes among real daily duration, saved dated-goal balance and main-period start time. Unknown points are blank; no line bridges missing dates. Main-period timing reuses the existing circular `RhythmStats` projection: the longest non-nap effective period of at least three hours for each sleep day. Midnight wraps correctly. At least two main periods are required to describe the containing start-time range. This is recorded-period timing, not measured sleep onset or a clinical regularity index.

Historical comparisons reuse `DatedGoalBank` and immutable `GoalCommitment` values. Only completed closed dates with an actual saved duration commitment receive a balance. Its frozen time zone, cutoff, target and stopped-date rules apply. Today's current plan is never applied retrospectively. Missing goal dates, current/open goal dates and unavailable goal history remain distinct from a real zero balance. A malformed goal ledger leaves other descriptive statistics available and offers the real dated-goal review screen.

Tag summaries reuse the existing `TagImpact` calculation on main-length records within the chosen window. A comparison requires at least three tagged and three untagged eligible records. Naps, future/unfinished, empty and overlapping ambiguous records are excluded by that model. Differences are descriptive means with both sample counts, not causal recommendations. To bound work/UI, up to four comparisons are offered from the first 24 alphabetically sorted saved tag names; **Explore all saved tags** opens the existing complete management flow. No quantity regression or tag association is invented when a cohort is too small.

The main **Review saved records** action opens the real History screen. Empty windows also offer the existing manual-record editor; no period is auto-added or estimated. Other actions open the actual goal bank, recorded rhythm, selected tag management or latest saved report. Returning reloads current saved data.

## Preservation and limits

This is a read-only projection, except for its private display-window preference. It changes no record, goal, backup format or database schema. Background reads are serialized with generation fences; stale Activity results cannot replace a newer requested view. No sensor, service, microphone, alarm, playback, flashlight, external provider or network action is started. The reader rejects more than 3,000 saved sessions or 20,000 pause rows with a visible preservation/error state rather than silently truncating the history. Storage contention can still delay a read.

## Verification contract

`RecordInsightsTest` has 12 pure cases covering observed/missing coverage and averaging, overlap/awake union, immutable historical targets, unavailable goals, known-zero coverage, circular midnight starts/naps, supported tag cohorts, unfinished/future rejection, duplicate cross-zone intervals, day cutoffs, stopped goals and admission bounds.

`RecordInsightsNativeTest` has three native methods using UUID private SQLite/preferences: actual effective-record/frozen-goal reopen without mutation, persisted range/error preservation and native chart drawing/accessibility with unknown dates. It has 0 PNG contract.

`RecordInsightsFlowTest` has one actual native UI method using injected private storage, preferences, goal repository, cutoff and clock. It selects and persists the window, changes a real chart, recreates/reopens, executes the real History navigation, verifies unchanged saved records, and exercises the empty state. It captures four original images:

- `record-insights-week.png`
- `record-insights-goals.png`
- `record-insights-reopened.png`
- `record-insights-empty.png`

These are functional evidence, not a design-review gate. Compilation is not native execution, and native fixtures do not establish overnight sensor accuracy or clinical meaning. No personal-phone test is included.

Product scope reference: [Sleep as Android Charts / Trends](https://sleep.urbandroid.org/docs/sleep/charts.html). SleepWell's implementation is independently original and intentionally uses only its observable saved inputs.

Author verification on 2026-10-25: scoped Gradle passed 41 pure tests across 3 actual suites (12 RecordInsights, 15 RhythmStats, 14 TagImpact), debug/native APKs and lint in 4 min 59 sec. A final explicit invalid-window recovery control and matching native-flow action recompiled with both APKs and lint in 2 min 35 sec (0 errors, 126 warnings, 1 hint). The four new native methods are compiled, not executed here. No personal-phone, hardware, alarm/output or provider test was performed.
