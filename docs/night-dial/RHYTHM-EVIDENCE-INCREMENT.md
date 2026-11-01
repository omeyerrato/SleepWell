# Rhythm observed-night evidence — source increment, native acceptance pending

2026-11-01. Design28D requires a Tip route to the underlying observed nights while
insufficient/unsupported chronotype remains unknown. Based on `091d8e95`, the
Chronotype section now has Review observed timing evidence. The same retained
`SleepRhythmActivity` owns the route and its independent viewport/page state.

The review renders only the existing immutable `RhythmStats.nights` selection:
one eligible main period per admitted sleep-day, with actual provenance,
effective start/end clock and zone, and effective recorded coverage. It states
the exact observed and phone-recorded counts. Manual/imported observations can
describe timing but do not count toward the unchanged 30 phone-recorded-night
history target. The existing inclusion, coverage, pause/awake, nap, future and
main-night calculations are unchanged. The shared statistics still do not assign
a clinical chronotype or owl/lark category; reaching the history target is not a
validated diagnostic cutoff.

At most 20 actual rows render per page, with explicit Older/Newer controls. Each
row rechecks its attached focused route and immutable source-ID/day membership
before opening that exact read-only Record Report. No latest-record fallback is
used. Report return and recreation retain the list page and reading position.
Back returns to the invoking Rhythm view; its existing Back retains the real
Report/Trends caller. Empty history shows no eligible observations and no invented
night action. Refresh failure keeps the ordinary explicit error/retry path.
No record, setting, model, output or capture policy is changed.

The [bounded native plan](RHYTHM-EVIDENCE-NATIVE-PLAN.json) extends the same existing
`SleepRhythmFlowTest#actualSevenNightsWorkdaysPersistRecreateAndEmptyWithoutOutputs`
method and retains all prior nine frames/assertions. Five new actual original
frames cover the review, exact-source Report, returned/recreated list and empty
evidence. A real older-row scroll must survive return/recreation. Source database
snapshots stay identical. Unknown copy and the saved workday controls retain
their existing checks. The earlier selected-night plan remains historical.

Only the diff, finite plan/capture names and source consistency were checked
locally. No build, native run, hosted cohort, unit campaign, phone, alarm, audio,
download or model study ran here. State-specific native/visual acceptance is
pending integration; the historical source66 register is unchanged.
