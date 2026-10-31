# Fixed ambient-phone acoustic pilot: rejected for Android staging

This is an actual original fit and one frozen held-out evaluation, separate from the rejected Nidra excerpts. **Do not ship this model or mark F049 implemented.** Public dataset access is resolved. The modest prospective gate passed, but useful phone staging did not follow: the model makes substantially more target and probability errors than the natural-training baselines.

| Saved evaluation | Validation 31–40 | Test 41–50 |
|---|---:|---:|
| Actual usable 90s contexts / selected |100/100|100/100|
| Correct central 30s targets |31%|34%|
| Always-Light accuracy (natural TRAIN majority) |62%|67%|
| Macro F1 / majority macro F1 |.3070/.1914|.2841/.2006|
| Balanced accuracy |.3550|.5309|
| Brier sum / constant natural-TRAIN-prior Brier |.7221/.5474|.7240/.5003|
| Negative log likelihood / same prior baseline |1.2959/1.0018|1.3091/.9254|
| Top-score 10-bin ECE |.0692|.0686|
| Actual Wake/REM/Light/Deep target counts |25/11/62/2|23/9/67/1|

Brier is the mean sum of squared errors across the four probabilities; lower Brier/NLL is better. The prior baseline uses all eligible TRAIN target counts [4962,3221,15950,1951], never validation/test frequencies. A low top-score ECE does not compensate for worse discrimination or probability error. Scores are uncalibrated mean tree-leaf class proportions; no calibration fit was performed.

TEST confusion (rows truth, columns prediction; Wake, REM, Light, Deep):

```text
12  4  7  0
 3  3  2  1
22 19 18  8
 0  0  0  1
```

TEST precision is [.3243,.1154,.6667,.1000], recall [.5217,.3333,.2687,1.0000]. Only one Deep target makes its 100% recall and the resulting balanced accuracy unstable; nine of ten Deep predictions are wrong. Validation has only two Deep targets. These sampled, separated 30s targets are not complete-night scoring or an estimate of performance on users' recordings. No clinical-certification requirement is imposed.

## Frozen acquisition, labels and fit

The source is Tao et al., Scientific Data(2025), [paper DOI10.1038/s41597-025-05583-8](https://doi.org/10.1038/s41597-025-05583-8), [ScienceDB dataset DOI10.57760/sciencedb.19070](https://doi.org/10.57760/sciencedb.19070), V5, registered CC-BY-4.0. The article's licence is separate. Actual released phone WAV headers are PCM16 mono 8000 Hz, unlike the original stereo48k recording description. No EEG, oximetry, AHI, event annotations, demographics, labels or clock values were acoustic model inputs.

The author's helper was read, never executed. Its exact absolute stage-clock minus same-subject annotation.record_start convention supplies alignment; the WAV lacks an embedded start clock, so synchronization is not independently verified. N1/N2 map to Light, N3 to Deep, W to Wake, R to REM. Labels outside actual PCM, blank labels and missing phone inputs remain absent. Subjects 11/17 lack the supported phone WAV; no recorder was substituted. The 48 supported phone subjects have 49,341 eligible 90s contexts; the fixed pilot uses 417 separated contexts, not those complete recordings.

The protocol was committed before fit/inference; class-balanced TRAIN selection and label-independent validation/test selection were hash-ranked with seed20261031. The fixed original classifier used 243 acoustic features and 128 RF trees, max depth8, min leaf4, balanced_subsample, max_features=sqrt, two CPU threads. It was fitted once on 217 selected contexts from 28 TRAIN subjects. TRAIN class counts are [56,54,56,51]. Validation was saved before opening TEST metadata. All 100 TEST contexts were then acquired and the frozen model evaluated once. No holdout-driven tuning, thresholds, stage-diversity rules or synthetic padding was applied.

The portable tree decoder matched the primary sklearn runtime within 2.22e-16 on TRAIN/validation and 1.11e-16 on TEST. This establishes decoder consistency, not staging validity. The 1,286,509-byte modelJSON and 625,212-byte own reference pickle remain private research artifacts and are not bundled in Android. Model SHA256 is `e1af3712022a27cad50eb36b859b4ed4c230551bac3274b3f2a24c6eea18f878`.

## Budget and evidence

The initial protocol's 100 MB restriction is historical and unchanged. The user subsequently authorized a hard total 650,000,000-byte study ceiling. Final conservative accounting is 644,877,909 bytes: 40,000,000 prior-traffic upper bound plus 604,877,909 charged/reserved bytes. It includes failed-request reservations, not just retained files. The 417 retained PCM ranges total600,480,000 bytes. One TEST body timeout stopped acquisition; one bounded resume reused completed hash-verified ranges. No complete WAV/archive, playback, phone action, user recording or upload occurred. No further network, fit or TEST inference is needed.

`manifest.json` pins source/runtime/protocol/model hashes and every archived file. `subject-sources.json` pins actual V5 file IDs, headers, sidecars, duration, author clock offsets, coverage and full original contract hashes. `range-receipts.json` pins all 417 exact206 ranges and their SHA256s. Crop plans pin selected windows. `train-inputs.json` and the original validation/test reports retain input/feature hashes and raw class probabilities; the metrics files describe those saved probabilities without new inference. Original full sidecars, rawPCM and rejected fitted weights stay in the ignored local research cache.

Commit order before the original fit: cbaf533a(protocol), bfa1b4bb(public access receipts), 1d5d64d4(original feature/RF implementation), 02605d32(frozen TEST guard), b678c305(source/protocol guard), d668b013(natural TRAIN baseline), 55d63ff3(literal protocol LF). After fit, f78fe7ce added only a separate saved-probability metrics reader; it did not change the frozen feature/inference module. Exact literal hashes are in the manifest. Executed once: `phone_stage_acoustic.py fit`; after its saved passing modest validation gate, `phone_stage_acoustic.py evaluate-test`. `phone_stage_metrics.py` then read each saved phase report. No broad app tests/builds were run by this study.

The remaining prerequisite is useful evidence from a different, fixed trained ambient-phone method with adequate class support on fresh participant-held-out evaluation and a coherent real capture/time/gap contract. This consumed TEST cannot become a tuning set while still being called untouched. Current sparse event clips cannot retrospectively supply these 90s contexts. Access is no longer the obstacle; this fitted method's held-out behavior is. No capture scaffolding or stage ribbon is justified by this result.
