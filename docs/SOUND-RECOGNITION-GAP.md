# Recognition gap: current evidence and historical model study

## Explicit recorded-analysis update, 31 October 2026

The new source/uploader-separated FSD50K development/evaluation study supports a
bounded **offline instrument fallback**, using the already bundled PANN model only
when the original EfficientAT gate rejects. Frozen evaluation instrument sources
improve from 1/8 to 6/8, laughter stays 3/8, with no lost positives or new
distractor/wrong-family acceptances. Ten retained snore/speech/cry/cough controls
also produce no candidates in 33 actual windows. The original failed holdout below
was not rerun or tuned. See [protocol, results and limits](FSD50K-RECOGNITION-CALIBRATION.md).

Automatic YAMNet, capture, grouping and cue authority remain unchanged. This small
generic-clip result does not supply the independently interval-annotated target-room
data described below. Reliable automatic laughter/instrument recognition remains
unfinished. Older sections retain the evidence and policy that existed at their dates.

## Current source investigation, 27 October 2026

Inspected `5fe17124`, the frozen supplementary manifests/full scores, pinned
original model code, and Java frontend/analyzer. **No new preprocessing,
class-index, time-accounting or aggregation bug was demonstrated.** The PANNs
proposal/runtime prerequisites below are historical: both supplementary models
are already bundled, numerically matched and executed in the native recorded
flow. See [current implementation](RECORDED-SOUND-ANALYSIS.md) and
[provenance/evidence](../third_party/supplementary-sound/README.md).

All 527 Android labels match the pinned original CSV in order after the existing
CRLF fix. Laughter 16–21, instruments 138–199/208–215 and Music 137 match the
current family policy. PCM16/32768, polyphase 16→32 kHz resampling, the distinct
PANNs/EfficientAT frontends and tensor layouts match the pinned originals.
Retained classifier-export score error is <=5.37e-7; Java frontend score error
is <=6.92e-6 on the unchanged solo-laughter/guitar vectors. These are narrow
numeric checks, not general accuracy proof.

Every held-out sample is accounted for by actual 10-second windows/tails. Solo
laughter retains all 461,107 samples (10 s + 10 s + 8.8191875 s), guitar all
91,924 samples (5.74525 s). Neither missed positive was lost to the <2 s rule.
Family maximum, margin and candidate priority match the predeclared policy.

| Missed source/window | Original model family score | Strongest unrelated score |
| --- | ---: | ---: |
| Solo laughter, first 10 s | Snicker 0.076521941 | Groan 0.092058897 |
| Solo laughter, second 10 s | Laughter 0.033902798 | Grunt 0.185436890 |
| Solo laughter, final 8.8191875 s | Laughter 0.048307620 | Wail/moan 0.166872203 |
| Guitar, whole 5.74525 s | Musical instrument 0.483944803 | 0.028795986 |

Solo laughter fails the unchanged PANNs 0.10/0.025 score/margin gate; guitar
ranks the right family but falls below EfficientAT 0.65. Crowd/piano still pass,
speech accepts neither category: **2/4 positives remain missed**. The crowd's
actual 2 s tail has saturated incompatible EfficientAT classes in the original
Torch output too; the margin rejects it. This records unstable short-duration
estimates, not a demonstrated Java framing bug or a new accepted result.

The concrete missing prerequisite is independently interval-annotated native
16 kHz target-room development data, including solo/crowd/quiet laughter,
isolated/mixed instruments and speech/snore/cough/cry/moan/media/noise negatives
across microphones, distances and rooms. Separate development/calibration and
new untouched evaluation by original source, speaker and device; retain source
and PCM hashes. Uploader descriptions/current corrections are insufficient
interval ground truth or training-disjointness proof. Any duration policy or
model-specific calibration/fine-tuned redistributable head must use that new
development data, preserve licence/weight/preprocessing provenance, and pass the
new untouched set plus retained snore/speech/cry/cough behavior. Do not train or
retune against the failed existing holdout; upsampling restores no lost audio.

No classifier/frontend/model/gate/fixture changed. No new inference, download,
training, test batch, build, CI, phone/microphone/playback/output action ran.
Reliable automatic laughter/instrument recognition remains unfinished.

## Historical model study, 26 October 2026

Investigated at source `c4c7bd2071503c3888b75ad2b86221efea17f3b3` on 26 October
2026. No production classifier, threshold, capture path, fixture or user label
was changed. Reliable laughter recognition remains incomplete.

## Current implementation has no demonstrated framing or index correction

The retained [holdout result](SOUND-CLASSIFIER-HOLDOUT-RESULT.md) rejects the frozen
0.30 candidate: zero of three new positive sources produce grouped laughter.
Production remains 0.65, with its unrelated-label margin and duration rules.
The original solo maximum 0.5859375 and failed crowd/short positives stay visible.
Earlier 480 ms hopping also failed, so changing overlap is not a supported fix.

`SoundAnalysisStream` converts PCM16 by dividing by 32768, uses real 16 kHz mono
samples and discards incomplete frames. This matches the normalization in the
[official YAMNet inference example](https://github.com/tensorflow/models/blob/master/research/audioset/yamnet/inference.py).
Laughter indices 13–18 match the [official class map](https://github.com/tensorflow/models/blob/master/research/audioset/yamnet/yamnet_class_map.csv).
No output-score calibration, alternate threshold or stronger bedroom accuracy
follows from this contract match. There is no evidence here supporting a
production code change while preserving the other classes.

## Concrete redistributable alternative

The original PANNs **MobileNetV2** checkpoint is a bounded candidate for a new
model study, not a proven repair or an Android-ready replacement:

| Item | Verified primary-source identity |
| --- | --- |
| Weights | `MobileNetV2_mAP=0.383.pth`, Zenodo record 3987831 v3 |
| Published size | 20,834,417 bytes |
| Publisher checksum | `md5:838421a9262cce976aa247946d6e2983` |
| Weight licence | CC-BY-4.0, explicitly returned by the record API |
| Code revision | `d2f4b8c18eab44737fcc0de1248ae21eb43f6aa4` |
| Code licence | MIT, Qiuqiang Kong 2018–2020 |

Sources: [published model files](https://zenodo.org/records/3987831),
[record metadata including licence](https://zenodo.org/api/records/3987831),
[pinned code licence](https://github.com/qiuqiangkong/audioset_tagging_cnn/blob/d2f4b8c18eab44737fcc0de1248ae21eb43f6aa4/LICENSE.MIT).
The publisher MD5 identifies the selected file; it is not a new SHA256 or proof
of downloaded bytes. No weights were downloaded or deserialized in this turn.
Any redistributed conversion must retain attribution, licence and modification
notice, and record source and converted SHA256 values.

The [pinned inference defaults](https://github.com/qiuqiangkong/audioset_tagging_cnn/blob/d2f4b8c18eab44737fcc0de1248ae21eb43f6aa4/pytorch/inference.py)
use 32 kHz audio, FFT/window 1024, hop 320, 64 mel bands and 50–14,000 Hz.
The [MobileNetV2 implementation](https://github.com/qiuqiangkong/audioset_tagging_cnn/blob/d2f4b8c18eab44737fcc0de1248ae21eb43f6aa4/pytorch/models.py)
uses centered Hann/reflection STFT, log-mel, batch normalization, convolutional
blocks, temporal max-plus-mean pooling and sigmoid clip outputs. Inference must
disable training augmentation/dropout. This is a different preprocessing and
temporal contract from YAMNet's 15,600-sample input; a clip score cannot be
presented as a newly measured 975 ms event.

The [pinned 527-class map](https://github.com/qiuqiangkong/audioset_tagging_cnn/blob/d2f4b8c18eab44737fcc0de1248ae21eb43f6aa4/metadata/class_labels_indices.csv)
differs from YAMNet's 521 outputs. Example zero-based indices:

| Family | Current YAMNet | PANNs |
| --- | --- | --- |
| Speech and exported speech children | 0–5 | 0–7 |
| Laughter family | 13–18 | 16–21 |
| Baby cry | 20 | 23 |
| Snoring / Breathing | 38 / 36 | 43 / 41 |
| Cough / Throat clearing | 42 / 43 | 47 / 48 |

Join by stable AudioSet MID and verified labels, never by truncating or padding
score arrays. Published aggregate AudioSet mAP does not establish laughter,
bedroom accuracy, calibration or preservation of current positives.

## Exact prerequisites before an implementation swap

1. A CPU Python runtime with PyTorch, its matching audio/preprocessing dependencies
   and inspected official source is needed for safe `weights_only=True` loading.
   The active bundled Python has NumPy but no torch, torchaudio, torchlibrosa,
   scipy, librosa, soundfile or ONNX. Previous bundled site-packages and known
   project runtime/environment locations did not supply torch. PATH Python
   commands are Windows Store launch stubs. No large runtime was installed.
2. Run a bounded comparison on the unchanged original positives and distractors,
   preserving their source IDs, whole PCM hashes, every score and missed result.
   Proper 16→32 kHz resampling of retained fixtures restores no missing upper-band
   evidence. Do not normalize, crop, replace examples, train on them or tune a
   second cutoff against this failed holdout.
3. Produce an inspected, hash-pinned mobile graph with numerically matched
   preprocessing and CPU outputs. Operator availability, memory, runtime and
   streaming window/latency remain unproven. Merely renaming a `.pth` asset to
   `.tflite` cannot meet the app's CompiledModel contract.
4. Preserve existing YAMNet snore/speech/cry/cough decisions. A separately admitted
   laughter head needs bounded shared capture/resampling, independent evidence
   identity and truthful interval grouping; it must not silently reinterpret
   old recordings. Genuine 32 kHz input requires capture-path work beyond this
   source-only investigation.
5. Any operating-point or fine-tuning study needs independently annotated
   laughter/non-laughter time intervals across speakers, rooms, distances,
   simultaneous media, crying/cough/snore negatives and target microphones, with
   source-separated development data and a new untouched holdout. Existing
   uploader descriptions are not interval ground truth or training-disjointness
   proof. Accuracy improvement and preserved old behavior must be demonstrated
   before enabling a new production model.

No model comparison, unit batch, CI run, phone/emulator action, microphone,
playback, external recipient or vendor contact occurred. The current on-device
estimates continue to work with their recorded limitations; this investigation
does not mark the missing reliability requirement complete.
