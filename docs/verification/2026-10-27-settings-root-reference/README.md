# Settings home: bounded no-change reference review

The [original Settings root frame](organization-settings-root.png) from
[CI 40445147772](https://github.com/omeyerrato/SleepWell/actions/runs/40445147772)
was individually opened and compared with board 11's left Settings frame.
At the captured default text size, all eight groups, the complete GitHub row,
search action and floating four-destination dock are visible. Titles, subtitles,
icon tiles and chevrons align without clipping. No root presentation change was
needed.

The group structure and dedicated Alarms tab reflect later user requirements.
They intentionally differ from the board's original destinations and three-tab
dock; features such as Bedtime guard remain inside their corresponding group.
This is a bounded Settings home assessment, not an all-36-screen fidelity claim.

[The exact class log](SettingsOrganizationFlowTest.txt) records the existing
`SettingsOrganizationFlowTest#groupedSettingsSearchAndAlarmDraftPersistWithoutOutputs`
passing in **33.114 seconds**. The unmodified [focused summary](focused-review-summary.json)
records one passing method, fourteen required original frames and no failures.
Only its relevant root PNG is duplicated in this archive; the other thirteen
frames are outside this no-change review. The journey asserts the complete Home
screen row is visible without scrolling, then exercises group navigation, search
and draft/recreation behavior. It was not replayed for this assessment.

[The original environment](environment.json) pins captured source
`eb3a7da884cdea93a23d54f6b2e7beb2fa4b6956`, an API 36 x86_64 disposable emulator,
1080×2316 override and density 420, with host audio disabled and no native playback
allowlisted. No physical-phone action or output was used for this review.

At reviewed revision `170b0e44785c52429f7b4c089e234ca9d18ce102`, the
[method-source comparison](method-source-equivalence.json) confirms the exact
declarations and bodies of `settingsHome`, `settingsRow`, `settingsSearch`,
`openSettingsGroup` and `openSettingsPage` match the captured revision after only
CRLF-to-LF normalization. Each method's SHA-256 is recorded for both revisions.
**This is not whole-file equivalence:** other MainActivity methods changed.
Design.java is unchanged between these revisions, and the only Icons.java change
adds `settings-outline`, which this root does not use. Existing root icon mappings
remain the same.

The four original files were copied directly from the run artifact and verified
byte-for-byte with [SHA-256](original-sha256.json). Folder-local Git attributes
preserve those bytes. This archive adds no source/test changes, new CI run,
large-text/locale verification or new navigation-coverage claim.
