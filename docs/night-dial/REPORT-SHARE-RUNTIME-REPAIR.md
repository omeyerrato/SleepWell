# Private Report Share runtime repair — 2026-11-01

Source under repair: `ec2a1647365f19e7d30269994640912c9a5f5d27`.
[Native run41254730755](https://github.com/omeyerrato/SleepWell/actions/runs/41254730755)
ran the completion-reference slice on an API36 emulator. Its focused summary
records eleven of twelve native methods passing. The private Share method
failed after47.643seconds; this remains an earlier failed run, not acceptance
of this repair. No other method's assertions are changed here.

The full per-method log reports
`UiObjectNotFoundException: UiSelector[SCROLLABLE=true]` in `description()`
at the Include notes toggle following Prepare preview again. The three missing
required originals are `night-dial-report-share-actions.png`,
`night-dial-report-share-chooser.png` and `night-dial-report-share-returned.png`.

All five actual original Share/failure PNGs listed below were inspected at their
original1080×2316 resolution. Preview already has three Share/Cancel action
sets. The notes, recreated and changed states retain stale action sets. By the
failure frame the scrolling preview has disappeared behind six accumulated
footers. The matching actual hierarchy contains five Share buttons, one
Checking preview… button and six Cancel buttons, with no scrollable node.
This is a proven production layout defect; no screenshot preview inference is
needed to explain the failure.

`RecordUi.footer(body)` creates a sibling of the ScrollView under the page
root. `ReportShareActivity.changed()` cleared only the body while appending a
new footer for every asynchronous or lifecycle state update. Stale enabled and
disabled actions remained attached and consumed the available viewport. The
private activity now creates one footer with its page and repopulates that same
footer with the current state's actions. Shared shell, chooser construction,
provider admission, notes opt-in, raw records and public GET_CONTENT checks
are unchanged by this layout repair.

The existing native Share method now checks one Share action and one Cancel
across the whole current window at every awaited READY/ERROR state, including
recreation and error recovery. It also requires a visibly reachable preview
scroll viewport. The share-actions frame reveals and checks the actual expiry
text inside the scrolling preview while independently requiring the fixed Share
action to be enabled. Earlier code tried to scroll to the fixed sibling action.
All existing notes, stale-record rejection, actual Android chooser, read-only
provider, durable admission/generation and unchanged-record assertions remain.
The method and its same seven required original PNGs are unchanged in the
native cohort; no new test campaign or extra frame is required.

Exact next native method, integration-owned:
`org.sleepwell.app.NightDialReportFlowTest#privateReportShareKeepsNotesOptInAndOpensOnlyExplicitSystemChooser`.
A successful pass still needs all seven original Share frames and the completed
`night-dial-report-share-proof.json`. The failed run contains four Share frames
and no completed Share proof.
The proof records action uniqueness/viewport checks only if the whole method
completes. Local validation of this repair is limited to diff and source/capture
plan consistency checks. No build, emulator run, hosted cohort, phone action,
recipient selection or output playback ran here. Current native/visual acceptance
for the repair remains pending.

The original failed artifacts remain under the local run directory above.
Hashes identify those existing originals without relabeling them as a new pass.

| Existing artifact | SHA-256 |
| --- | --- |
| `NightDialReportFlowTest#privateReportShareKeepsNotesOptInAndOpensOnlyExplicitSystemChooser.txt` | `6efa4ecf9a51a1b71adf53f79b8fbb4e44629a948c693b22bb4b02088e8fe6a3` |
| `focused-review-summary.json` | `cb62af052a3b62b4fdb7262a4fa8eeabbd65ea28eb6813f0f8f9983bb3b67261` |
| `environment.json` | `3b8e6753346046689b1560bf118894b5f60b0b4d8b68412a75207b86f49f9ce6` |
| `night-dial-report-failure.xml` | `54cb45b9a4ab9a27f5307648413f310ffa9967a0aa8f54fb5fac481aa345e113` |
| `night-dial-report-failure.png` | `183353f564f857137f1a3a7739ca5053cbcfbbd8102a165bbcc9a50c3ea6fd27` |
| `night-dial-report-share-preview.png` | `c62f71ef574702820528380876e0a226ad8d1b2699777a89e0082e7add37f375` |
| `night-dial-report-share-notes.png` | `d788b910479a6c46e0dcabd8acaa2be251a8c04b0d1dc6dc8b69b57bb880ad97` |
| `night-dial-report-share-restored.png` | `4b2ff65925e91674f2cd0ffe8d0077eb5061b9df3271cd48cd9f440c944733fb` |
| `night-dial-report-share-changed.png` | `858188fbc3930b558ba66f69b92419fe33eb87add27c61d97759da7a061f309c` |
