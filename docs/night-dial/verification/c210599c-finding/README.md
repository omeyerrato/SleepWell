# Functional pass with a sheet geometry finding: c210599c

[Run 41089152348](https://github.com/omeyerrato/SleepWell/actions/runs/41089152348), job 124178117866, artifact 12993147651. All nine native methods passed in 256.763 seconds, all 78 required frames existed and all 80 focused unit methods passed. The independent APK workflow also passed. Artifact ZIP SHA256: 2f2dfd9d42e311bed3ee6dddca73c61d0492fbda6e82b2ce5e4912f26fcee780.

The new journeys verify named Skip review/cancel/recreation, actual next occurrence/Undo, restored stale-occurrence attention, off unsaved Duplicate/recreation/explicit Save, named Delete/cancel/recreation/stale identity, and Save from dirty Back. Source alarms, private stores, output/volume guards and existing scheduling owners remain checked.

Visual inspection found blank space beneath the painted short sheet: SleepPicker.show allocated a fixed window height while the panel occupied only its natural content height. The attached frames intentionally preserve that gap above the root footer. This functional pass is not visual approval of the gap. The follow-up makes the height a maximum with a bounded content wrapper and natural window height, while preserving existing scroll references and listeners. Native assertions now require that short sheet viewports reserve no trailing blank area.

The following test-only revision adds the pre-existing failed-scheduling/retry/reopen and one-time/expired skip regression journeys, retaining their domain assertions. SearchFlowTest and AlarmSkipFlowTest update only the changed sheet wording. Their broader scope has separate results. This revision was not delivered as a phone installation or a new APK. Full-app acceptance remains false.
