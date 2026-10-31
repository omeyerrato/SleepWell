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
