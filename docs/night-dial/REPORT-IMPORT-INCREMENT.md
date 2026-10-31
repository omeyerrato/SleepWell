# History CSV route: pending native acceptance

1 November 2026, based on `80f06f44`. State27A now has a visible **Import Sleep as
Android CSV** action. It opens the existing non-exported `SleepCsvActivity` with
the current review context; browsing does not choose a file or import anything.
Close/Cancel returns to the same History instance, whose scope and scroll remain
owned by History. The importer retains its actual file-size/row limits,
reconciliation, explicit selection/append and source provenance. No importer,
schema or public caller contract was replaced.

The existing
`NightDialReportFlowTest#latestReadOnlySignalsDraftGateAndReviewedRestoreKeepAnnotations`
now opens this actual route, recreates the importer, closes it and checks the
fixture still has exactly its three original periods. It requires two added
originals: `night-dial-report-history-import.png` and
`night-dial-report-history-import-restored.png`. The owned CSV worker must
terminate before fixture cleanup. This verifies the route/recreation/return,
not the separate27D selected-file reconciliation/import contract.

Native execution and original-PNG inspection are pending integration. No build,
test campaign, model, device, capture, playback or OS alarm ran here. Earlier
Report37 plan/receipts remain unchanged; this source adds two required captures.
