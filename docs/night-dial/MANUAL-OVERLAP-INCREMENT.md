# Manual overlap review — source increment, native acceptance pending

2026-11-01. Flow27C, based on `80f06f44`, separate from the Import/Share commits.
The existing model stores completed periods separately and unions coverage in
Trends. Manual Add/Edit now explains that model before saving an overlap.
The historical source66 acceptance register remains an earlier snapshot.

The actual editor offers **Keep editing** or **Save separate**. Review shows the
number of other completed periods, their dated source intervals/time zones and
unique shared coverage. It excludes the edited row itself and uses the shared
`RecordEntry.coverage()` projection: user awake intervals, recorded pauses,
accepted awake estimates, provider awake data and active-duration limits remain
authoritative. A time shared by multiple periods counts once in the total.
Up to eight source rows are displayed; any additional count is explicit. There
is no automatic merge or trim, and active unfinished recordings are excluded.

The save worker owns an immutable copy of the submitted draft. Its preliminary
review closes storage before waiting for the user. Save separate rereads coverage
and verifies the draft/source fingerprint in the same write transaction as the
mutation. Changed coverage rejects the old review, preserves the draft and asks
for Save changes to obtain a fresh review. Retrying never confirms that review.
Existing record revision checks and durable creation tokens remain in force;
a completed creation token is checked before overlap evaluation, including when
its new row would otherwise overlap itself. Other records and raw evidence are
not merged, trimmed or rewritten.

An unconfirmed review can rerender/recreate without saving. Losing the volatile
owner requires a fresh review from the retained draft. An already confirmed
save can finish across Activity recreation and is observed once. Keep editing
releases the pending review and preserves the editor draft. Work is bounded by
512 candidate periods, 20,000 persisted pause/awake inputs and 20,000 effective
coverage intervals, with a 30-second cooperative review deadline; exceeding a
bound produces an error instead of an incomplete overlap claim.

The [pending native plan](MANUAL-OVERLAP-NATIVE-PLAN.json) names two exact methods:
one new focused editor journey and the existing save-recreation regression.
The new journey requires ten actual original PNGs. Its fixture proves a 7-hour
union across two other periods, then a 5-hour union after an intentional source
edit; stale confirmation must write nothing. It uses real Keep editing/Save
separate gestures, recreates the review after explicit volatile-owner eviction,
and recreates a confirmed creation while the save owner is delayed. Durable
creation-token replay must leave exactly one new row. Source/raw rows and
unrelated preferences are checked unchanged. All fixture owners/workers close
before their named isolated database is removed. Volatile-owner eviction is
an in-process simulation, not a full process-kill claim.

Local checks cover the diff and plan/capture consistency only. No build, native
run, unit campaign, model evaluation or personal-device action ran here. Night
visual/state acceptance for this increment remains pending the integration run
and actual inspection of all ten originals. No full-app completion box is set.

Run 41256960941 exposed a fixture race after Keep editing: the sheet disappeared before the editor Notes accessibility node returned. The original NPE is retained. The journey now waits for and requires that actual node before asserting its exact preserved note; no save or stale-source assertion was weakened. Rerun pending.
