# Recognition gap: evidence and next viable model

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
