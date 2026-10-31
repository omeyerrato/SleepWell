# Pinned apnea checkpoint: publisher inventory and scaler reconstruction

The public inventory is now resolved. The original external scaler still cannot
be reconstructed from the inspected release. This updates, rather than replaces,
the earlier [respiratory contract](../2026-11-01-phone-respiratory-contract.md): its
historical guessed-route HTTP404 established neither access denial nor absence
of additional Mendeley files. No model evaluation or Android integration occurred.

## Actual primary artifacts

- [Mendeley v1](https://data.mendeley.com/datasets/wrf77nvb8z/1),
  [DOI](https://doi.org/10.17632/wrf77nvb8z.1): root acquired the public HTML,
  frontend JavaScript, root file inventory, empty folder list, and all four
  publisher-listed files. The frontend's public route is
  `https://data.mendeley.com/public-api/datasets/wrf77nvb8z/files?folder_id=root&version=1&$start=0&$limit=1000`.
  Normal public requests with User-Agent/Referer worked; a bare-client403 is not
  proof of an account restriction. Four completed files are two XLSX and two DOCX;
  there is no scaler, full feature matrix, audio or nested data directory in this
  pinned inventory. This is a statement about that actual inventory, not all
  possible external sources or future versions.
- [GitHub@ad979ff72de4cb27a308ed03d56c3729a4396352](https://github.com/SebasTS15/Sleep-Apnea-Detection-Model/tree/ad979ff72de4cb27a308ed03d56c3729a4396352):
  the complete, nontruncated tree contains ten blobs: LICENSE, README, notebook,
  checkpoint and six result images. It contains no raw audio, NPY arrays,
  unscaled/scaled X, source-row manifest, scaler, dependency lock or participant
  split. MIT LICENSE is pinned separately; publisher documents describe CC-BY4.
- The authorized checkpoint download was exactly **1,090,253 body bytes**, one
  attempt, HTTP200 with exact Content-Length checked before its bounded read.
  Git blob `df6ce773ae289d444c1ac42392717d4b17a165d1` matches the pinned tree;
  SHA256 is `c1938a82248bb3526b2c9ff7a76f5e5d7ceee35cf552706e6587766f0fedf664`.
  Private original remains under the study worktree's
  `.android-local/research/mendeley-contract-20261101/model_apnea.keras`.
  It was opened only as ZIP/JSON. Its three members are `metadata.json`,
  `config.json`, and `model.weights.h5`; weights were hashed, never deserialized.
  Metadata reports Keras3.10.0, saved `2025-11-14@19:25:53`.

`manifest.json` pins all ten local Mendeley files (the two TXT files are local
DOCX text derivatives), all four publisher IDs/URLs/hashes, complete Git tree,
notebook/code-cell hashes, checkpoint/member hashes/config, sheet dimensions and
initial rows, and embedded-image hashes. All four downloads match publisher
sizes and SHA256s. `inspect_local.py` regenerates it from already acquired files
using Python's standard library only. It performs no network request or model
runtime import. Original binary checkpoint/workbooks are not copied into the APK
or committed here.

## Two independent primary contract conflicts

1. **Feature/architecture identity.** Publisher documentation and
   `Raw_Data_All_Figures.xlsx` describe 313 features trimmed to approximately280:
   MFCC/Mel mean, SD, min, max, plus RMS/ZCR. The pinned notebook's cells1/3/4
   instead produce exactly160 float32 features:13MFCC mean/SD (26),64Mel-dB
   mean/SD (128), ZCR/centroid/RMS mean/SD (6). Its recorded X shape is
   `(103210,160)`. The actual checkpoint configuration confirms input160,
   Dense256/128/64/**32**/1 with ReLU hidden units, sigmoid output, BN after the
   first three Dense layers and dropout0.3/0.3/0.2/0.2. The publisher's architecture
   omits Dense32. There is no input Normalization/scaler layer. BN after a256-unit
   Dense layer is not the missing160-element raw-feature StandardScaler.
2. **Fitted normalization/QC.** Publisher documentation says StandardScaler is
   fitted on training data, clips to[-10,10], and excludes sub-signals with more
   than10% nonfinite values. Notebook cell5 actually calls
   `StandardScaler().fit_transform(X)` on **all103210 rows before** its stratified
   80/20 sample split (82568/20642). Its feature function catches per-family errors
   with zero-filled features and applies `nan_to_num`; it does not implement those
   clipping/exclusion rules. The checkpoint has no separate scaler ZIP member.
   No assertion is made that either publisher description identifies a second
   supplied fitted model: only this one pinned checkpoint was inspected.

## What reconstructing the original scaler would actually require

Notebook cell2 calls `kagglehub.dataset_download` for
`bryandarquea/psg-audio-apnea-audios`; its stored output identifies **version3** and
a27.3G download. It enumerates192 `_ap.npy` and191 `_nap.npy` files below
`PSG-AUDIO`. Cell3 requires each loaded file to be2D and processes every row's
first160000 samples, zero-padding shorter rows, at a **nominal**16000Hz. It does
not read a WAV header, resample input, or establish original array sample rate,
event time or participant identity. Cell4 reports103210 feature rows with
class totals38625(no apnea)/64585(apnea), after any file-level failures.
Those counts do not identify the original source arrays or their contents.

All original unscaled feature rows, or the exact accepted arrays plus the exact
frontend/runtime and failure/QC history, could supply the original mean and
population variance mathematically without the original row permutation.
Floating accumulation order can still affect bitwise results. Here neither the
full X nor those arrays exist in the pinned GitHub/Mendeley inventories; librosa,
NumPy and scikit-learn versions/defaults are also unpinned. The referenced Kaggle
collection was **not** downloaded or re-inventoried by this task. Its mention is
an actionable source pointer, not evidence that a matching complete fitted
corpus is available under the study budget or that its capture matches a bed
phone. Fitting a scaler on unrelated phone windows, examples or another subset
would change the checkpoint's trained input contract.

The immediate prerequisite is an original160-element `mean_`/`scale_` (with count,
feature order, zero-variance handling and compatible frontend), or a complete
versioned unscaled X plus provenance sufficient to reproduce those statistics.
A small known-input/unscaled-feature/scaled-feature/output reference is also
needed to verify that it belongs to this checkpoint. A different documented280
feature pipeline cannot be silently substituted. Even resolving preprocessing
would leave a separate phone-capture/generalization evaluation decision; no
clinical certification requirement is introduced here.

## Prediction examples do not repair the missing input contract

- `apnea_model_results.xlsx/Prediction_Examples` is `A1:A1`: its only cell is the
  title. It embeds `xl/media/image4.jpeg` (118088bytes, SHA in manifest), not a
  numerical feature/target table. Actual image inspection shows four plots,
  rounded scores0.97/0.00/0.51/0.11 and class text, with no file, participant or
  source-row IDs. The `.11` panel says `Real: No Apnea`.
- `Raw_Data_All_Figures.xlsx/Fig7_Prediction_Examples` is `A1:H174`,170 XML rows.
  It contains four160-point example columns and rounded outcome summaries; its
  `.11` column is labeled FN/RealApnea, conflicting with the actual embedded
  image's NoApnea label. There are no original file/row references. This is
  neither103210×160 unscaled X nor an exact inference vector/output reference.
- The pinned notebook's plotting function sets `signal = X_test[test_idx]`:
  this plots the **160 standardized feature coordinates**, although axes call
  them samples/amplitude. The publisher's Fig7 text calls its160 points reduced
  ten-second waveforms. No feature vector is relabeled audio here, and no plotted
  figure is digitized into invented training/inference input.
- The first workbook's other numeric cells are a2×2 confusion matrix; its
  training/architecture/ROC sheets are title cells with images. The second
  workbook is figure statistics, selected ROC points, binned probabilities and
  example points; it explicitly calls its epoch history reconstructed. Claims
  there of `data/test/` full probabilities or model/features directories are not
  fulfilled by the actual four-file/zero-folder inventory.

## Scope and accounting

This agent added only1,090,253 network body bytes for the approved checkpoint.
Parent-owned metadata acquisition accounting remains separate. The earlier
644877909-byte study and473538-byte lead-audit conservative charges are historical;
the parent owns the aggregate650MB ledger, including its own failed-request
reservations. The four publisher bodies total580544bytes; HTML/JS/inventory/folder
bodies present here total785395bytes. No further requests were made here.

No upstream Python, pickle, TensorFlow/Keras/HDF5 loader, model fit/inference,
scaler calculation, audio acquisition/playback, consumed TEST, phone, build or
test campaign was used. No app/checklist completion is claimed. Public inventory
access is resolved; exact fitted preprocessing and compatible phone evidence are
still missing for useful respiratory predictions. The rejected original stage
and respiratory pilots are unchanged.
