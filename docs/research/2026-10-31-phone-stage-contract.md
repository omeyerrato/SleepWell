# Nidra contract audit: no demonstrated decoder fix

Source/artifact audit at SleepWell `05f005a1`, 31 October 2026. F049 remains missing.
This record adds a concrete inference-contract finding to the earlier
[six-excerpt replay](../verification/2026-10-26-stage-feasibility/manifest.json).
The accompanying [manifest](2026-10-31-phone-stage-contract.json) pins the sources
and local capture implementation. No new inference or accuracy result is claimed.

The existing private checkpoint, faithful full/encoder/head TFLite files and
replay/conversion scripts **do survive** in the root checkout's ignored research
directory. They were read without modification and their SHA-256 values match
the earlier manifest. Their absence from this isolated worktree is not a missing
weight or access prerequisite. No model, recording or private path is published
with this record.

## The upstream example is not a temporal decoder reference

At Nidra revision `2c4f17077f5e2a9c9f3cb1a0f50b3952f57253c7`,
[Step3](https://github.com/devangk003/Nidra/blob/2c4f17077f5e2a9c9f3cb1a0f50b3952f57253c7/scripts/Step3_Use_Model.py#L174-L182)
passes individual Mel slices directly to `model.predict`. Its producer returns
`[slice,224,224,3]`, while the published temporal configuration and
[training builder/generator](https://github.com/devangk003/Nidra/blob/2c4f17077f5e2a9c9f3cb1a0f50b3952f57253c7/scripts/Step2_train_modelCNN_BiLSTM.py#L293-L318)
require `[batch,7,224,224,3]`, with each slice normalized separately and the
fourth slice providing the target label. These are rank four and rank five
respectively, including the batch dimension. Step3 neither constructs those
windows nor applies the generator's per-slice normalization. This is a source
deduction, not an executed upstream error report.

**Repairing that example is not new evidence of useful staging.** The recorded
SleepWell replay already used seven-slice contexts, per-slice normalization and
the center label clock. It still produced Light on all six excerpts. Adding
these same operations to an Android adapter cannot explain away that result.
Multiplying inputs by 255, selecting a different class with thresholds, or
forcing a sleep cycle would change the evaluated contract without evidence.

## The author's published recording also has a constant interior

The pinned repository contains a
[hypnogram](https://github.com/devangk003/Nidra/blob/2c4f17077f5e2a9c9f3cb1a0f50b3952f57253c7/exports/hypnogram/00000995-100507_ambient_hypnogram.png)
and [confusion matrix](https://github.com/devangk003/Nidra/blob/2c4f17077f5e2a9c9f3cb1a0f50b3952f57253c7/exports/hypnogram/00000995-100507_ambient_confusion.png)
for `00000995-100507_ambient.npy`. The images were inspected directly. The matrix,
in Wake/REM/Light/Deep row and column order, is:

| True / predicted | Wake | REM | Light | Deep |
| --- | ---: | ---: | ---: | ---: |
| Wake | 3 | 0 | 99 | 0 |
| REM | 0 | 0 | 134 | 0 |
| Light | 3 | 0 | 2165 | 0 |
| Deep | 0 | 0 | 91 | 0 |

There are 2,489 Light predictions and six Wake predictions, with no REM or Deep.
The hypnogram shows Wake only at the ends and Light throughout the interior.
Its displayed accuracy, 0.8689, equals the all-Light majority baseline:
2,168 of 2,495 labels. Thus that number does not demonstrate discrimination.

[The author's test script](https://github.com/devangk003/Nidra/blob/2c4f17077f5e2a9c9f3cb1a0f50b3952f57253c7/scripts/test_trained_model.py#L390-L467)
does construct seven-slice contexts and normalize each slice, but pads the first
and last three probability rows with zeros. Its subsequent `argmax` turns those
unpredicted rows into Wake. This matches the six endpoint predictions; without
the original generation log and weight hash, the PNG's exact execution cannot
be independently reconstructed. Its loader also uses `weights=None`, unlike
the training builder's primary ImageNet branch used in the faithful conversion.
No padding-derived Wake is acceptable as a detected
stage in SleepWell. The test script calls this evaluation on training data.

The complete pinned repository tree contains **no audio, EDF, RML or NumPy input
or label files**. It contains plots rather than a replayable example. The cached
PSG-Audio mirror inventory has no subject `00000995-100507`; it does list seven
EDF parts for the already inspected `00001016-100507`. Those names do not provide
the complete-record features or global reference locally. The original dataset's
CC-BY-4.0 metadata survives, so licensing is not the asserted obstacle for that
dataset. Obtaining and processing an original complete recording remains an
untried context correction, not a demonstrated fix. The separate published
`best_model_weights.h5` has a different Git blob, but no corresponding
discriminative reference result was obtained; switching weights is not evidence
by itself. No new full-record download or model run was made in this audit.

## The actual phone input is transient, not a saved night

[Step1](https://github.com/devangk003/Nidra/blob/2c4f17077f5e2a9c9f3cb1a0f50b3952f57253c7/scripts/Step1_convert_audio_to_mel_and_label.py#L41-L75)
uses ambient audio, whole-record maximum-referenced Mel power in decibels,
80-dB clipping, then slices. A target needs seven consecutive slices, nominally
50.176 seconds; its advance is 7.168 seconds, not 30 seconds. Per-slice scaling
does not restore values lost when a locally referenced excerpt clips differently
from its source night. The prior 55-second excerpts did not establish that
whole-night preprocessing, and their all-Light result does not establish that
every possible phone-audio method fails.

SleepWell does receive mono 16-kHz PCM in `NoiseRecorder`; a new method could use
that actual stream. What it currently saves is different:

- `SignalWindow` reduces audio to an epoch RMS. `Store` persists movement, noise,
  light and sample counts, not their waveforms or Mel tensors.
- Optional threshold-triggered PCM clips stop at approximately ten seconds
  (the ordinary path can overshoot by one read of at most 1,024 samples).
  `ClipBudget` permits at most 60 and waits 30 seconds after each saved clip
  before another can start. Concatenating them would invent missing sound and
  time; no individual clip supplies a seven-slice context.
- The contactless path also saves optional ten-second clips under that budget.
  Its shared sound pipeline consumes downsampled PCM for YAMNet and discards
  incomplete classifier/FIR history across real gaps; it does not retain a
  whole-night staging input.

This is a retention/coverage incompatibility, not a claim that a phone cannot
capture the modality. Existing records cannot be retroactively staged by this
checkpoint. No clip, provider interval, movement summary or raw record was changed.

## Exact prerequisite before integration

For this checkpoint, the missing evidence is a reproducible reference run using
the pinned graph and weights, the complete original-record clipping context,
and its real center-slice time axis, showing meaningful input-dependent stage
behavior on licensed ambient recordings. Record waveform/feature hashes,
reference-runtime versions, raw probabilities and annotation provenance.
Clinical certification is not required; a tensor-shape fix or stage-diversity
rule does not satisfy this prerequisite. The current rejection remains bounded
by the excerpt-context and subject-overlap limitations already recorded.

Only after such a method works is there a justified capture implementation:
preserve the continuous actual PCM or sufficient unclipped Mel features with
sample timing and explicit gaps until the whole-record reference is known.
That would be a new opt-in input contract; current sparse clips are insufficient.
An alternative is a redistributable trained ambient-phone method with a complete
working preprocessing/decoder contract, or original training on licensed,
synchronized ambient recordings and expert labels. None was obtained in this
audit. No estimator, capture scaffolding or hypnogram is shipped by this change.
