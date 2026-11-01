# Contributing periods in tag comparisons — 1 November 2026

State 28C now opens the exact saved-record groups behind each comparison.
With tag and Without tag use the same `TagImpact` eligibility pass, including its
nap, unfinished/future, empty and overlapping-period exclusions. Their immutable
record-ID lists are collected alongside the existing unchanged duration statistics.

Selecting a group opens a read-only list with its actual count, mean duration,
recorded dates and periods. Each period opens its existing Report owner. Back
returns to the comparison; group selection and scroll survive recreation. A
missing tag stays unavailable rather than silently selecting another tag, and a
read failure offers Retry while labeling retained data. No record is modified.

The existing `TagManagementFlowTest#quantityDraftSaveRenameDeleteAndComparisonPersist`
now exercises both groups, exact inclusion/exclusion, recreation, Report/Back,
unchanged database fingerprint and the original quantity/rename/delete flow.
Additional actual capture names are `tags-with-group.png`,
`tags-with-group-recreated.png` and `tags-without-group.png`.

Native execution and visual acceptance of this increment are pending integration.
This does not accept all tag quantities, devices or statistical associations.

## Actual native run correction — 1 November

Run 41254733103 failed before quantity input: the existing fixture waited for the retired inline `Set tag quantity` control. The original failure PNG shows the implemented editor’s `Details` action, with accessibility name `Tag details and quantities`. The journey now enters that actual sheet and selects `Set quantity for Coffee`, then reopens the same sheet for `Manage saved tags`. Quantity persistence, exact contributing IDs, immutable record fingerprint, recreation, rename and delete assertions remain. This is a fixture route correction; it is not evidence of a passing rerun or a production layout fix.

Run 41256969775 reached the exact contributor Report, then the fixture waited for its retired `Sleep report` title instead of current `Your night`. The action now waits for that focused Report before Back; no contributor/data assertion was removed. Rerun pending.
