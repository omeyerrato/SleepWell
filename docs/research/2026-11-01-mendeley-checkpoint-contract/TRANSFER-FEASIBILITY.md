# Candidate2 feasibility only — no approval to extract, fit or predict

Date2026-11-01. This is an executable **new domain-standardization hypothesis**,
not faithful reconstruction of the original classifier's preprocessing. Missing
original scaler still prevents that reconstruction. A feature-wise TRAIN-only
normalizer can instead be defined openly for the frozen MIT160-input checkpoint;
whether the resulting neural prior is useful is unknown. No weight retraining,
model search, calibration fit, threshold selection or numerical outcome occurred
during this feasibility check. Parent must approve a final protocol and pinned
workflow before any feature extraction/normalizer fit/inference.

## Installed trusted runtime and actual weights

Existing `C:/Users/omeye/.cache/sleepwell-stage-py/Scripts/python.exe`:
Python3.12.14, NumPy1.26.4, SciPy1.13.1, Librosa0.10.2.post1, h5py3.11.0,
Numba0.68.0, SoundFile0.14.0, audioread3.1.0. NumPy/SciPy/Librosa/h5py were imported
only for versions, signatures and data inspection. Package metadata lists
TensorFlow2.16.1/Keras3.15.1; neither was imported or tested. The checkpoint was
saved by Keras3.10.0, so installed versions alone are not a parity result.

ZIP plus h5py read-only in-memory parsing of the already cached, Git-blob/SHA
verified checkpoint found exactly22 required model arrays: five Dense kernels
and biases, three sets of BN gamma/beta/moving_mean/moving_variance. All expected
shapes match the configuration, all are numeric float32 and finite, and BN
variances are nonnegative. No soft/external HDF5 links or external dataset storage
were accepted. Optimizer state is not a model input and was not used.
`transfer-weight-inspection.json` records layer array hashes, shapes and installed
frontend signatures. Model arrays total86273 floats/345092bytes. Parsing does
not instantiate a model or produce any prediction.

## Literal160-feature layout for the NEW pinned frontend

Apply installed Librosa0.10.2.post1 to each actual10-second chunk after explicit
resampling; this pins defaults for the new candidate, not the unknown original
Librosa version. Original notebook feature order, zero-based:

| Indices | Family | Count |
|---|---|---:|
|0–12|MFCC coefficient means|13|
|13–25|MFCC coefficient population SDs|13|
|26–89|64Mel-dB band means|64|
|90–153|64Mel-dB band population SDs|64|
|154–155|ZCR mean, population SD|2|
|156–157|spectral-centroid mean, population SD|2|
|158–159|RMS mean, population SD|2|

Important distinction: the notebook's `mfcc(y=audio,sr=16000,n_mfcc=13)` uses its
own default128-band Mel frontend, `power_to_db` reference1.0, DCT2/ortho/lifter0.
The separate Mel branch is64 bands and `power_to_db(...,ref=np.max)`. They cannot
be collapsed into one64-band frontend. FFT2048, hop512, Hann/centered constant
padding and power2 follow the installed spectral defaults; ZCR uses its own edge
padding convention. MFCC/Mel stats aggregate across frames per coefficient/band;
ZCR/centroid/RMS each supply two scalar stats. Final vector is float32.

Proposed stricter missing-input rule for this new candidate: an exception,
unexpected shape, nonfinite source/chunk/vector or invalid normalization makes
the whole30-second target unavailable, with a reason in its receipt. No synthetic
zero feature family, partial three-chunk score or substitute target. For valid
finite inputs the notebook's `nanmean/nanstd` are ordinary population stats.
This differs explicitly from its silent error/NaN-to-zero fallback.

## Proposed frozen input/normalizer/score contract for parent review

1. Reuse only already cached90-second PCM bodies and exact acquisition hashes,
   annotations, participant split, event-duration-seconds conditional inference,
   event-union labels and gray exclusions from the first respiratory protocol.
   No additional audio/labels/network source. TRAIN01–30 (11/17 no phone),
   VAL31–40, respiratory TEST41–50. No stage-outcome prediction/reference is input.
2. For the target's central30 seconds, take actual contiguous10-second chunks at
   cached offsets30–40,40–50,50–60seconds. Decode PCM16 little-endian mono8k to
   float32 via division32768. No context features, waveform z-score, gain fitting,
   clock, demographics, stage, AHI, SpO2 or event metadata as model input.
3. Upsample each actual80000-sample chunk to160000 using installed
   `scipy.signal.resample_poly(up=2,down=1,window=('kaiser',5.0),padtype='constant',cval=0)`.
   This explicit low-pass anti-imaging filter provides a fixed16k representation;
   it **cannot recover original content above4kHz**. No replacement/padding of
   missing PCM. Original NPY capture/sample convention remains unknown.
4. Fit one unweighted per-feature mean/population variance normalizer on all
   three chunks of only eligible TRAIN targets, with no labels supplied to the
   statistics calculation. Prior frozen metadata has199 eligible TRAIN targets
   (54positive/145zero), hence at most597 vectors. Zero variance maps to scale1;
   nonfinite/invalid statistics fail. No clipping. Never refit on VAL/TEST or
   choose another normalizer based on outcomes. This normalization changes the
   original trained coordinate map and may destroy useful distinctions.
5. Freeze checkpoint weights. Apply the explicit ordered forward pass below to
   each normalized160-vector. Target probability is the arithmetic **mean of
   exactly three probabilities**, not the probability of averaged features.
   Fixed positive decision `p>=0.5`. No smoothing, calibrated rescore, output
   relabeling or clock prior. A>=10-second annotation-overlap target may contain
   only one eventful chunk; mean/0.5 is a substantive fixed hypothesis, not
   guaranteed alignment with that endpoint. No alternate aggregation after VAL.
6. Reuse ALL original respiratory VAL gates, supports, priors, calibration and
   full probability/confusion reporting. Previous88 eligible VAL targets are
   known to exist, so at most264 vectors. This is **candidate2 on previously
   inspected VAL**, after the fixed243-feature RF failed; VAL is not newly
   untouched. No further candidate/search is implied. Respiratory TEST labels
   remain unopened and may be constructed/evaluated once only if every frozen
   VAL gate passes and parent authorizes that conditional progression. The stage
   TEST was consumed separately and is never reopened for staging.

Original respiratory gate values to carry unchanged: coverage>=0.8 of all
selected targets (including gray exclusions), each class>=10 targets from>=3
participants, AUROC>=0.7, balanced accuracy>=0.6, sensitivity>=0.5,
specificity>=0.7, macroF1>=majority+0.05, and binary Brier/NLL strictly better than
both frozen TRAIN probability priors54/199 and7250/23734. The baseline, class
supports, per-participant results, confusion, reliability bins and raw
probabilities must all remain visible. No cutoff or denominator changes.

## Explicit decoder and parity prerequisite

For each first-three Dense layer, compute in order:
`z=max(x@W+b,0)`, then
`x=gamma*(z-moving_mean)/sqrt(moving_variance+0.001)+beta`.
BN is **after Dense's ReLU**, not before it. Dropout is the identity during
inference. Next compute Dense32 plus ReLU, identity dropout, Dense1 and stable
sigmoid. Kernels have layouts160×256,256×128,128×64,64×32,32×1; bias/BN axes are
last-dimension. No optimizer state, training-mode BN update or upstream Python is
needed for a NumPy implementation.

Before label outcomes, an approved workflow must validate the decoder against a
primary installed Keras/TF runtime built solely from these whitelisted known
layers and numeric arrays on fixed label-independent vectors. No saved-model
custom code/pickle is needed. Installed runtime compatibility is untested; if it
fails or parity exceeds the predeclared tolerance, stop and report rather than
silently accepting an unverified fallback. This is portable-math parity, not
original scaler/frontend fidelity or phone accuracy.

## Resource bound and current stop point

No network, installation or new model/audio acquisition is needed. The cached
checkpoint is1.09MB; required numeric arrays are345KB. All eligible TRAIN+VAL
float32 feature vectors together would occupy at most551040bytes. Per-chunk FFT
arrays are small; process one chunk at a time with at most two BLAS/OMP/Numba
threads. The frozen neural forward pass is about85000 multiply-accumulates per
chunk and is CPU-feasible. Full feature extraction includes FFT/Mel/DCT and
possible first-call JIT cost; it has **not been timed** and is not promised to
finish in seconds. A final approved workflow should fix a finite wall/CPU bound
and stop without fishing if exceeded. No Android export/shipping is authorized
by feasibility or even by an eventual modest offline pass.

The actual remaining prerequisite for this NEW method is a reviewed frozen
protocol/workflow and decoder parity, followed by honest held-out results; it
does not require recovering the original scaler. This is a precise experiment
possibility, not a fitted/useful model claim. At this commit no features,
normalizer, forward pass, candidate probability, VAL outcome or TEST target has
been computed. No app/build/native/unit/device action occurred.
