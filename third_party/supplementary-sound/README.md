# Recorded supplementary sound models

These bundled models power **explicit experimental analysis of recorded audio**.
They do not replace the automatic YAMNet sleep ledger. Scores are uncalibrated
model outputs, not probabilities, diagnoses or proof that a sound was absent.

## Attribution and licences

**PANNs MobileNetV2**, Qiuqiang Kong and the PANNs contributors, from
[PANNs pretrained AudioSet models](https://zenodo.org/records/3987831).
The original pretrained weights are **CC BY 4.0**, with the full legal code in
`PANN-WEIGHTS-CC-BY-4.0.txt`, also bundled inside the APK. The original
[architecture code](https://github.com/qiuqiangkong/audioset_tagging_cnn/tree/d2f4b8c18eab44737fcc0de1248ae21eb43f6aa4)
is MIT, copyright 2018–2020 Qiuqiang Kong; see `PANN-CODE-MIT.txt`.
SleepWell's changes: exported only the evaluation-time logmel-to-527-score
classifier to ONNX; implemented its separate frontend in Java; no weight training
or fine tuning. This is a modified format, not an endorsement by the authors.

**EfficientAT mn10_as**, Florian Schmid and contributors, from the official
[mn10_as_mAP_471 release](https://github.com/fschmid56/EfficientAT/releases/tag/v0.0.1)
and [pinned repository](https://github.com/fschmid56/EfficientAT/tree/a425fdce92572e602a1d5634799bd9f1f2efa806).
The publisher distributes it under the repository MIT licence, copyright 2022
Florian Schmid; see `EFFICIENTAT-MIT.txt`, also bundled inside the APK.
SleepWell's changes: evaluation-time classifier plus sigmoid exported to ONNX;
separate exact frontend implemented in Java; no weight training or fine tuning.

The legal-code text was obtained from Creative Commons' own
[legal-tools repository](https://github.com/creativecommons/cc-legal-tools-data/blob/main/docs/licenses/by/4.0/legalcode.txt).
Its SHA-256 is `9ba9550ad48438d0836ddab3da480b3b69ffa0aac7b7878b5a0039e7ab429411`.

## Original inputs and portable output

| Item | Bytes | SHA-256 |
| --- | ---: | --- |
| Original PANNs MobileNetV2 checkpoint | 20,834,417 | `22437af9eb5ed1854b5ad285b4a17d5fc566f1a3b23df40db5541f5705ca340f` |
| Original EfficientAT checkpoint | 19,708,753 | `0bd7dc2443af498c289a2e739f02ebb515d6aa3fd3ab9db539c86123ae368a4e` |
| PANNs exported ONNX | 16,372,202 | `f9dd56478e434e067cef505489d0ae4483d80d357e2c35bc3d94ba432eb08f3f` |
| EfficientAT exported ONNX | 19,516,171 | `7bb4227052d796a1ef35f0a5b703fdd724cd200fc7e6f85087289222e6c5b5c8` |

The exact shared 527-class AudioSet map SHA-256 is
`cdd1049833c4b86127c2773ac0d14a2754b6a6d0d1798002ed5c66e699708429`.
This is not YAMNet's 521-class map. Laughter is indices16–21; instrument names
map to138–199 and208–215; compatible Music is137. Bell, bicycle bell, tuning fork
and chimes are excluded. App assets contain the complete label list and the
exact frozen mel/window/resampling coefficient arrays, with hashes in
`assets.json`. Model and frontend asset hashes are checked before inference.

## Reproduce the export

`export_models.py` runs from the repository root; it never fetches or installs
anything. First place the original checkpoint, MIT licence, pinned
`pytorch/models.py`, and `metadata/class_labels_indices.csv` (as `labels.csv`) in
`.android-local/panns/`. Place the pinned EfficientAT `models/`, `helpers/`,
`metadata/` and official checkpoint `mn10_as_mAP_471.pt` in
`.android-local/efficientat/`. Primary checkpoint URLs:

- https://zenodo.org/records/3987831/files/MobileNetV2_mAP%3D0.383.pth?download=1
- https://github.com/fschmid56/EfficientAT/releases/download/v0.0.1/mn10_as_mAP_471.pt

The recipe verifies source/checkpoint/map hashes before importing model code or
loading weights. Loading uses `torch.load(weights_only=True, map_location='cpu')`
and strict state matching. Never replace it with a hub loader or unchecked pickle.
It exports standard CPU operators, ONNX opset17, dynamic time dimension and fixed
batch1. It excludes training masks, augmentation and dropout. No raw audio is
embedded in exported models.

Export environment: Python3.12.14, torch2.9.1+cpu, torchvision0.24.1+cpu,
torchaudio2.9.1+cpu, torchlibrosa0.1.0, librosa0.11.0, NumPy2.3.4,
SciPy1.16.3, ONNX1.19.1. CPU parity used onnxruntime1.23.2, one thread.
No CUDA/GPU runtime is required. The Android dependency is
`com.microsoft.onnxruntime:onnxruntime-android:1.23.2`.

## Evidence and limits

`holdout.json` pins source and decoded PCM digests before inference;
`holdout-results.json` retains all scores, top classes and misses. The declared
operating point is in `docs/SUPPLEMENTARY-SOUND-HOLDOUT.md`. All acceptance
examples are whole original previews with no gain, repetition or selected easy
cropping. They are broad uploader descriptions, not frame-level ground truth;
training overlap is unknown.

The new held-out gate **failed**: only2/4 positive sources yielded an accepted
window (crowd laughter and piano); solo laughter and guitar were missed. Speech
yielded neither new category. Thresholds were not changed after those results.
This is useful exploratory recorded analysis, **not reliable automatic laughter
or instrument recognition**. An uncertain or low-score window must never be
presented as absence of laughter or instrumental sound.

`onnx-export-parity.json` compares exported classifiers with original Torch on
the unchanged missed-laughter10s and guitar5.74525s vectors. Maximum absolute
score difference was5.37e-7. `frontend-port-parity.json` then runs the actual Java
frontend and compares its ONNX outputs with original Torch frontends: maximum
score difference6.92e-6, resampling maximum1.79e-7. Small low-energy PANN logmel
differences are retained (maximum0.02253dB). These are narrow numeric port checks,
not a clinical or field accuracy claim. Android native ABI/runtime performance
and integration are for the combined app build/device verification.
