# Conditional respiratory-event classifier: validation failed

The explicitly approved fixed offline fit completed once. Validation failed five predeclared gates; **TEST respiratory labels were not constructed or evaluated, and this model must not ship in Android**. The rejected stage result and all original recordings remain unchanged. This is a real original respiratory-event-overlap fit, separate from the stage classifier, with no new network or audio acquisition.

The target remains conditional on an inferred seconds interpretation of source `event_duration`, not independently proved field-specific units or clinical truth. The author's source event list and its timing anchor were used exactly as disclosed and approved. No normal-breathing, apnea-diagnosis, subtype, AHI or full-night performance claim follows.

| Validation result | Fixed model | Constant selected-TRAIN prior | Constant natural-TRAIN prior |
|---|---:|---:|---:|
| Accuracy |.71591|.72727|.72727|
| AUROC |.61914|.50000|.50000|
| Balanced accuracy |.59635|.50000|.50000|
| Sensitivity |.33333|.00000|.00000|
| Specificity |.85938|1.00000|1.00000|
| Macro F1 |.60253|.42105|.42105|
| Average precision |.38725|.27273|.27273|
| Binary Brier |.20650|.19835|.19942|
| Negative log likelihood |.60022|.58596|.58853|
| Positive-score 10-bin ECE |.14364|.00137|.03274|

Binary Brier is mean `(p_event-y)^2`; the original JSON also reports twice that value as the two-class Brier sum. Lower Brier/NLL is better. Priors were fixed from TRAIN only: selected 54/199 and natural 7250/23734. Both predict class0 at the fixed 0.5 threshold; the always-negative majority baseline has the same confusion/accuracy. No calibration fit or threshold selection was performed.

There are **88 labeled, usable targets from 10 validation participants**: 64 no-annotated-overlap targets from 10 participants and 24 event-overlap targets from 9. Twelve of the 100 fixed targets were excluded by the predeclared partial-overlap boundary rule, with no replacement and no quality failures. Each target is the central 30 seconds of an actual 90-second context. The sampled 88 targets are 44 minutes of scored target time, not full-night monitoring.

Confusion, rows truth and columns prediction, ordered `[no_annotated_overlap,event_overlap]`:

```text
55   9
16   8
```

Coverage, class/participant support, specificity and macro F1 gates passed. AUROC, balanced accuracy, sensitivity, Brier and NLL gates failed. In particular, 16 of 24 positive targets were missed and both fixed prior baselines had lower probability error. A higher macro F1 than always-negative does not override these failures. Per-participant support/confusion, full probabilities and all reliability bins are retained in `validation.json`.

## Execution and model provenance

Approval was recorded in 13137a52 and the complete paired-receipt freeze in 44804351 **before fitting**. The original 7daff72c proposal and its parameters/threshold/features were unchanged. One binary RF fit used 199 TRAIN contexts: 145 class0 from 28 participants and 54 class1 from 24. Eighteen TRAIN partial-overlap targets were excluded. All consumed PCM hashes and paired original 243-feature hashes matched the earlier immutable receipts. Stage labels/predictions and the old stage RF weights were not model inputs.

The new model is RF128/depth8/minleaf4, balanced_subsample, sqrt feature selection, seed20261101, fixed `p>=0.5`, at most two CPU threads. Probabilities are uncalibrated mean tree-leaf class proportions. The primary sklearn runtime and separate two-class portable tree decoder agreed within 2.22e-16 on TRAIN and 1.11e-16 on validation. That checks faithful execution, not endpoint usefulness.

The 618,734-byte JSON model SHA256 is `534b3ebf651a13ce77fc7bdeb448ba5e86f9628c35ff447a446f9927e9c312d3`. It and the 352,976-byte own reference pickle remain in the ignored local research cache. No weights are bundled in the app. `manifest.json` pins their hashes, source/protocol/approval identities, exact original result bytes, runtime versions and once-only markers. The workflow refuses a second fit/evaluation and rejects TEST access after a failed validation gate. No failure rescue or alternative fit was attempted.

The actionable finding is bounded: these labels and cached inputs support a reproducible conditional respiratory-event endpoint, but this one fixed feature/RF combination did not meet the accepted usefulness criteria. It does not establish that apnea cannot be learned from phone audio. A different representation/training study would need a separately frozen protocol and honest handling of this now-exposed validation set. Independent unit/annotation provenance and an actual capture/time/gap integration decision would still be necessary even for a useful research result. No app scaffolding or stage/respiratory ribbon is justified by this failed fit.
