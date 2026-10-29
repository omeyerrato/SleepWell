> **Corrected follow-up:** [CI40822971575 / 62.071s / 18 fresh originals](corrected-40822971575/README.md) resolves the selected-tooltip/goal-caption overlap in the captured default scope. The first pass below and its original qualification remain preserved.

# Report rounded means: functional pass, visual overlap open

[CI40802190314](https://github.com/omeyerrato/SleepWell/actions/runs/40802190314) **PASSED in 64.885s**, one existing method / 18 required original PNGs, at `bdd09c4170f97becae77a8c7681bae23a071c70a`. All 18 originals were individually opened. **Visual acceptance remains partial:** the selected October tooltip in `report-reference-year-selected.png` covers part of the right-aligned **8 h current goal** caption. The tooltip value is readable and correct; this is not an claim that all 18 frames are visually clean.

## Pinned change and numerical result

Production `bae73ec43e22fc3b895f534429b70a415bbf17ac` changes only `RecordUi`, `HistoryActivity` and `TrendsChart`: display means/chart text at nearest-minute precision, with the existing sub-minute formatter retained. `bdd09c41` adds assertions/capture/scope only. Both commits share `app/src/main` tree `c3db8080e71bbe937abfef333fffdb2dda053efe`.

`source-preflight.json` pins all three production files, the exact native method file and both scope files. It byte-compares ten unchanged record/coverage/editor/store files with pre-rounding `6cd00676356534b58388d7664fa584fa52a38ecc`; the original individual-record formatter and chart point/range/axis construction prefix also match. Raw calculations, plotted heights, interval gaps and stores were not rounded or rewritten. No whole-app or later-source equality is claimed.

The actual seven fixture durations are 462, 455, 485, 430, 475, 450, 483 minutes: 3240/7 = 462.857142…minutes. The **weekly mean visibly rounds to 7 h 43 min**, while the latest individual record remains 7 h 42 min. The selected October monthly tooltip visibly shows **7 h 43 min**. The method navigated the real chart's accessibility actions and asserted its actual content description equals **`2026-10-01: 7 h 43 min`**, matching the monthly mean. This verifies the spoken accessibility string; no physical TalkBack/TTS audio was produced or heard.

## Existing flow retained

Exact selector: `ReportReferenceFlowTest#referenceHistoryTrendsEditAndNightReportPersistWithoutEffects`, still one `@Test`. `report-rounding` requires the original 17 Report images plus `report-reference-year-selected.png`; `scope-inventory.json` lists every literal. Native sounds/import flags are false. The raw summary reports one class/one method / 18 images/no failures and `full_app_review=false`. Root reported compilation success in 7m 20s, 25 tasks (5 executed / 20 up-to-date); this archive task ran no build or test.

The same native method completed History/Week/Month/Year, real calendar selection and unknown-date handling, real point selection, Read details, About/View trends/Sound events routes, full Report/editor actions and the existing period-edit journey. It exercised ordinary scroll/handle tap, valid drags, return across an awake annotation, Cancel, second-pointer cancellation, real dialog focus loss, recreation, both named boundary actions and keyboard controls.

The actual intentional bounds became **11:18 PM–6:00 AM**,6 h 42 min before adding a 10-minute awake interval. Explicit Save gives 6 h 32 min; reopened/persisted frames retain those bounds, Awake (1), both tags and the saved note. Back discards a subsequent unsaved boundary/note change. Exact raw-table fingerprints (`sessions`, `epochs`, `tracking_pauses`, `sound_events`, `clips`) remain unchanged; edits are separate overlays. Personal preferences and inactive alarm/tracking invariants passed through owned-reader cleanup.

## Individual visual review and limits

Compared with Board 08 (all) and Board 01 (middle), the History/Trends/Report/editor hierarchy, main actions, time handles, tags/notes and Save/Delete remain readable. The selected-month goal-caption overlap above is the concrete open issue; no other new core clipping was observed. The detailed frame notes/hashes/dimensions are in `inspection.json`. The new tooltip frame is retained unchanged so a later placement correction can be assessed separately.

Four navigation tabs are the intentional user-requested organization. Missing days/months remain blank, the absent dated goal remains a dash, actual pause selection says **No readings**, and sleep stages say **Not estimated**. The native fixture label and synthetic movement/events are explicitly private test data; they are not personal nights, physical sensor measurements, clinical stages or a real model/provider qualification. The fixture saves no raw sound playback clip.

`originals/` contains all 28 original artifact files copied byte-for-byte from Root's completed download, including all 18 PNGs, exact method log, environment, raw summary and guest diagnostics. No duplicate download, CI rerun, source edit, Root/release/device mutation was performed. `run.json` independently pins the terminal API identity; `SHA256SUMS` covers archived files except itself. Physical inputs/output, alternate devices/fonts/themes, DST/manual-limit variants, broader SystemUI/model/provider/security and complete 36-screen acceptance remain outside this scoped result.
