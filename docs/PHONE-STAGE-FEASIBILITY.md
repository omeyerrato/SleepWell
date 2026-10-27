# Phone sleep-stage implementation gap

Reviewed 26 October 2026. **F049 remains missing.** The brief permits honestly
labelled experimental estimates; clinical certification is not an added gate.
The outstanding requirement is a useful, redistributable phone-input method with
coherent preprocessing and timing. Inactivity, clock cycles, YAMNet labels and
minute movement/light summaries cannot be relabelled REM/light/deep. Imported
provider stages retain their provider attribution.

The metadata-only [evidence manifest](verification/2026-10-26-stage-feasibility/manifest.json)
records pinned sources, hashes, input contracts and the six actual replay outputs.
It contains no model binaries, audio, raw datasets, study annotations, machine
paths or user recordings. No new search, download or model run was performed for
this documentation update.

## Nidra: converted faithfully, not accepted for useful staging

Inspected [Nidra at revision 2c4f1707](https://github.com/devangk003/Nidra/tree/2c4f17077f5e2a9c9f3cb1a0f50b3952f57253c7),
including its Apache-2.0 licence, NOTICE, preprocessing/training scripts and
`final_model_weights.h5`. The checkpoint **was downloaded privately**, inspected
and converted; it was **not shipped in SleepWell**. Its SHA-256 is
`affbc5445f40d31c2109a4118f34f7fee177a4249bf403da0de10d8dfe2be873`.

The intended training input is ambient mono audio resampled to 16 kHz; a power Mel
spectrogram with 224 bands, FFT 2048 and hop 512; whole-record maximum-referenced
decibels with 80-dB clipping; whole-record min-max normalization; then 224-frame
slices repeated into three identical channels. The temporal generator applies
per-slice min-max normalization with `1e-8` in the denominator. Seven consecutive
slices form `[1,7,224,224,3]`; output classes are **Wake, REM, Light, Deep**.

A slice advances **7.168 seconds** (`224 × 512 / 16000`), and the label belongs to
the fourth slice in the seven-slice context, nominally 50.176 seconds. Upstream
label preprocessing uses this computed duration despite a separate 30-second
constant/plot convention. No 30-second plotting clock or synthetic sleep-cycle
timing was substituted. Whole-record reference/clipping still matters after
per-slice normalization; the published contract is post-night, not a live
smart-wake classifier.

The first conversion, SHA-256
`1c52e73a24bf473ab533c025ca0a5524770256c41bb35d213b860e4be75bbaf9`,
is **rejected**. Constructing EfficientNet with `weights=None` omitted the
ImageNet-only, weightless `Rescaling(1/sqrt([.229,.224,.225]))` after normalization.
Strict H5 loading did not catch that graph difference.

The corrected conversion reconstructs the ImageNet graph, strictly loads all
5,531,176 parameters, preserves weights and statically unrolls the recurrent
layer. The full builtin-only TFLite SHA-256 is
`1a3d8d639cebd8b666169a019b935df7cbed71911e18b5f2b09345ebb7c29369`.
Across three synthetic tensors, maximum absolute differences from the corrected
Keras graph were `3.06964e-6` for the full model and `1.99378e-5` for the split
encoder/head; static unrolling differed by zero. **Conversion parity proves
numerical fidelity on those inputs, not meaningful sleep-stage predictions.**

## Six bounded real-input excerpts: all predicted Light

The prior replay used the ambient **Mic** channel from
[PSG-Audio V3](https://doi.org/10.11922/sciencedb.00345), whose original Science Data
Bank metadata specifies CC-BY-4.0. Delivery used
[a pinned mirror](https://huggingface.co/datasets/dust-systems/psg-audio/tree/f1e8c79f27d5d2c1716c24dea4252840eda5c406),
not a new licensing authority. Expert RML annotations selected/checked intervals;
EEG, annotations and expected stages were never neural-network inputs.

Six exact HTTP 206 record ranges supplied six 55-second excerpts: 65,150,580 bytes
in total, without downloading full nights. First-hour Deep/REM were absent, so
two Light/N2 fallbacks were declared before scoring. A second selection pass
fixed actual Wake/N3/REM intervals before obtaining their model scores. Times
below are target-slice seconds from the source recording start.

| Excerpt | Expert label | Target slice (s) | Wake / REM / Light / Deep probabilities | Prediction |
| --- | --- | --- | --- | --- |
| First-hour wake | Wake | 911.504–918.672 | .336191 / .212482 / .388066 / .063262 | Light |
| Deep fallback | N2 | 116.504–123.672 | .332731 / .219502 / .393354 / .054413 | Light |
| REM fallback | N2 | 536.504–543.672 | .335238 / .214395 / .391379 / .058988 | Light |
| Later wake | Wake | 31.504–38.672 | .331369 / .221693 / .394909 / .052029 | Light |
| Later deep | N3 | 4261.504–4268.672 | .330620 / .223691 / .393861 / .051828 | Light |
| Later REM | REM | 11641.504–11648.672 | .331697 / .221479 / .394401 / .052424 | Light |

Exact probabilities, ranges, source identifiers, annotation digests and input
digests are in the manifest. Full/split conversions agreed within `9.71556e-6`
on these real inputs. No thresholds, forced labels or stage-cycle smoothing were
introduced to produce different stages.

**Limits:** each excerpt used its own global reference/clipping rather than the
original whole-night context. Librosa 0.10.2.post1 was pinned for reproducibility;
the author specified only `>=0.8.0`. Checkpoint training-subject exclusion is
unknown, so these are neither held-out validation nor an independent accuracy
estimate. All-Light outputs do not establish useful discrimination, but also do
not prove that every phone-audio method is impossible. This candidate remains
private and unaccepted; no capture scaffolding or predicted hypnogram is shipped.

## Other inspected primary sources

| Candidate | Concrete resource and remaining blocker |
| --- | --- |
| [Andrew-Wong-CN/Snoring](https://github.com/Andrew-Wong-CN/Snoring/tree/d9bd48176b6b6309ebe4d4c8c139099289e95e5d) | Publishes a 13,214,085-byte `parameters/training_SSNV2.pth` and smaller variants. Source uses stereo 16-kHz phone audio, pre-emphasis .97, FFT 2048/hop 1024, 20 Mel bands, 469-frame features and five stage labels. No repository licence was found (`license=null`); public access alone does not grant redistribution. Checkpoint/inference correspondence and the stereo input contract remain unresolved. Weights were not downloaded; mono input was not silently duplicated. |
| [SPIresearch/SnoreSleep](https://github.com/SPIresearch/SnoreSleep/tree/174f535ffce5a8c592b3487663f3661003cbbbc2) | MIT-licensed author project for snore-event staging. Inspected tree/releases contain README, image and event/feature archives, but no trained checkpoint or inference implementation. Exact preprocessing/time contract is unavailable there. No weight download was attempted. |
| [HomeSleepNet](https://www.jmir.org/2023/1/e46216/) | Demonstrates home smartphone-audio staging; inspected article/supplement links did not supply a redistributable checkpoint. Study recordings are not public. An architecture alone is insufficient. |
| [Oxford asleep](https://github.com/OxWearables/asleep) | Weights target wrist accelerometry, not phone-on-mattress summaries. Its [Academic Use Licence](https://github.com/OxWearables/asleep/blob/main/LICENSE.md) excludes consumer/product use. |

These are bounded findings, not an exhaustive absence claim. Do not restart the
same candidate search without new weights, licence or input-contract evidence.

## New primary-source check — 27 October 2026

The bounded follow-up found no additional usable checkpoint. Its
[source manifest](verification/2026-10-27-stage-feasibility/manifest.json)
records two new leads, without repeating the six Nidra replays:

- [Choi et al., Sensors 2025](https://doi.org/10.3390/s25206282)
  segments **tracheal** audio into respiratory events using fine-tuned Whisper,
  then estimates whole-night Wake/Light/Deep/REM **proportions**, explicitly not
  epoch-by-epoch stages. Inspected article and author-publication links did not
  provide its task-specific segmentation checkpoint or fitted stage predictors.
  Generic Whisper weights do not replace those components. This would need those
  reusable artifacts and demonstrated ambient-phone compatibility; even then its
  proportion output would not support a timed hypnogram.
- [Jaworski et al., Frontiers 2026](https://doi.org/10.3389/fnetp.2026.1779111)
  combines a **Murata SCA11h bed sensor** (1-kHz acceleration, derived cardiac
  measures) with 44.1-kHz directional-microphone audio. It predicts 30-second
  labels from 62 features using five-minute windows. The inspected article links
  no trained checkpoint; its data statement offers data from the authors. A
  reusable trained release and matching cardiac inputs are missing. SleepWell's
  phone-motion summaries cannot substitute for this sensor's cardiac features.

These are resource/domain gaps, not a clinical-certification requirement or a
claim that no suitable model can exist. No new weights/data were downloaded,
inference run, recording added or stage graph implemented in this follow-up.

## Work still needed

Obtain a reusable trained phone-input method demonstrating meaningful real-input
behavior, or train original weights on licensed synchronized signals and expert
stage labels. Preserve the actual preprocessing, time mapping, class definitions
and source/model licences. Then implement bounded opt-in capture/postprocessing,
missing-input handling, cancellation/cleanup and model/version/coverage provenance
before rendering only supported estimates. Existing summaries cannot silently
stand in for richer inputs. Echo-rhythm review is separate periodic-reflection
analysis; it is not REM/light/deep or apnea inference. F049 is not implemented.
