# Existing-audio respiratory-event endpoint: pre-fit proposal

There is a genuine new original-training possibility in the already acquired phone audio. Conditional on the declared seconds interpretation, the fixed TRAIN windows provide **54 source-event-overlap targets and 145 targets with no annotated overlap**, from 24 and 28 participants respectively; 18 partial-boundary targets are excluded. This is 199 labeled90-second contexts, each targeting its central 30 seconds. The failed stage classifier does not answer this separate binary question. No model has been fitted or evaluated for it.

The source JSON fields are exactly `record_start`, `events`, `event_type`, author-spelled `evnet_start`, `event_duration`, and `sleep_stage`. All 30 TRAIN annotation files match the original acquired SHA256s. Their 7,587 event rows contain 4,965 literal `hypo` and 2,622 `osa` types, finite starts/durations, and no central/mixed taxonomy. No subtype is invented. Source duration values range 0.9–126; no clinical event-duration validity is inferred.

**Unit resolution remains a pre-fit review item.** In the actual pinned author helper, line 304 declares a seconds timestamp convention, line 113 identifies `record_start` as audio alignment, and lines 143–153 return event JSON unchanged. Cached primary material does not explicitly state the `event_duration` field unit. The provisional counts assume seconds; they are not proof of that field-specific unit. No multiplication by 60, sample-rate scaling, guessed shift, or 10-second clinical assumption was applied. The author helper was read, never executed. No cached full source paper was found and no network was used for this assessment.

The proposed target uses half-open intervals and their union: at least10 seconds of annotated `hypo`/`osa` overlap in the central 30 seconds is class1; zero is class0; strictly positive overlap below10 is excluded without replacement. Concurrent types and duplicates cannot double-count time. The10-second threshold defines a research overlap target, not a new medical event. Absence from the released list is not verified normal breathing; the list has no explicit unscored-time mask. Awake/stage fields do not mask targets or enter features. Context-only events do not change the center label. All targets require the already verified full actual 90-second PCM context.

[The proposed fixed protocol](2026-11-01-apnea-protocol-proposal.json) is **not authorized for fitting**. It preserves TRAIN01–30/validation31–40/TEST41–50 and all 417 historical windows, with no new downloads or re-selection. TRAIN selection was stage-balanced in the earlier study; the new endpoint inherits that selection bias. Held-out audio/subjects were previously exposed for the separate stage endpoint; their respiratory labels have not been read here. This is a new endpoint, not an entirely unexposed corpus.

After explicit root approval/unit resolution only, the proposal is one binary RF128/depth8/minleaf4, seed20261101, fixed0.5 threshold and the unchanged 243 acoustic features. It does not reuse the stage RF weights. No SpO2, AHI, demographics, stage, clock or event metadata enters the feature vector. There are 54/199 selected-TRAIN positives; all natural eligible TRAIN times yield 7,250 positive/16,484 zero/2,350 partial targets. Both selected and natural TRAIN priors are fixed calibration baselines, alongside always-negative classification.

Proposed validation/TEST screen: at least 80% labeled/usable coverage,10 targets/3 participants per held-out class, AUROC≥.70, balanced accuracy≥.60, sensitivity≥.50, specificity≥.70, macroF1 above majority by.05, and Brier/NLL each better than both constant TRAIN priors. No calibration fit or threshold tuning rescues a failure. Validation failure stops before TEST respiratory labels/predictions. These are research usefulness criteria, not Android or clinical acceptance. Full confusion/probabilities/calibration/per-participant support are required even if a gate passes.

TRAIN label counts below are conditional on the same unit/overlap rule. Subjects 11/17 have no supported phone file and are not substituted. The committed JSON preserves every selected target/annotation/input hash, overlap and exclusion, plus all natural eligible TRAIN counts. Source script has no network, PCM, feature, fit, prediction or held-out annotation path. No audio was read in this step; no source recording or rejected stage result changed.

|TRAIN subject|event overlap|no annotated overlap|partial excluded|phone|
|---|---:|---:|---:|---|
|01|1|5|2|yes|
|02|4|1|2|yes|
|03|2|5|1|yes|
|04|0|6|0|yes|
|05|3|4|1|yes|
|06|3|5|0|yes|
|07|1|7|0|yes|
|08|2|6|0|yes|
|09|1|4|3|yes|
|10|0|5|3|yes|
|11|0|0|0|missing|
|12|1|7|0|yes|
|13|1|6|1|yes|
|14|2|6|0|yes|
|15|1|7|0|yes|
|16|1|6|1|yes|
|17|0|0|0|missing|
|18|1|7|0|yes|
|19|5|2|1|yes|
|20|5|3|0|yes|
|21|1|7|0|yes|
|22|1|7|0|yes|
|23|4|3|1|yes|
|24|2|4|0|yes|
|25|0|7|1|yes|
|26|4|4|0|yes|
|27|1|6|1|yes|
|28|3|5|0|yes|
|29|0|8|0|yes|
|30|4|2|0|yes|
