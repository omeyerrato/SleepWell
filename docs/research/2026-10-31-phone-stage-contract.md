# Nidra contract audit: no demonstrated decoder fix

Source-only audit at SleepWell `05f005a1`, 31 October 2026. F049 remains missing.
This record adds a concrete inference-contract finding to the earlier
[six-excerpt replay](../verification/2026-10-26-stage-feasibility/manifest.json).
The accompanying [manifest](2026-10-31-phone-stage-contract.json) pins the sources
and local capture implementation. No new inference or accuracy result is claimed.

## The upstream example is not a temporal decoder reference

At Nidra revision `2c4f17077f5e2a9c9f3cb1a0f50b3952f57253c7`,
[Step3](https://github.com/devangk003/Nidra/blob/2c4f17077f5e2a9c9f3cb1a0f50b3952f57253c7/scripts/Step3_Use_Model.py#L154-L164)
passes individual Mel slices directly to `model.predict`. Its producer returns
`[slice,224,224,3]`, while the published temporal configuration and
[training builder/generator](https://github.com/devangk003/Nidra/blob/2c4f17077f5e2a9c9f3cb1a0f50b3952f57253c7/scripts/Step2_train_modelCNN_BiLSTM.py#L281-L305)
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

## The actual phone input is transient, not a saved night

[Step1](https://github.com/devangk003/Nidra/blob/2c4f17077f5e2a9c9f3cb1a0f50b3952f57253c7/scripts/Step1_convert_audio_to_mel_and_label.py#L37-L68)
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
