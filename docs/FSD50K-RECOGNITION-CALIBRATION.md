# Bounded FSD50K supplementary-recognition study

Declared before downloading selected audio or running inference, 31 October 2026.
Source: feature commit `3956a2f9`; production models and old failed holdouts are unchanged.

The frozen selection contains 48 new original FSD50K WAVs: 24 official development
and 24 official evaluation sources. Each contains eight laughter positives, eight
instrument positives, and two each speech/cough/cry/environment distractors.
Every selected clip has a different uploader, including across the two splits;
all uploaders in the previous classifier fixtures/holdouts are excluded. Selection
uses SHA256 ordering of `sleepwell-fsd50k-study-v1:<Freesound id>`, before model
scores, CC0-1.0 or CC-BY-3.0/4.0 only, 180000–900000 original WAV bytes, and at
least two matching human predominant-present ratings without a negative rating.
Labels and all accepted ratings are retained in the manifest. Instrument labels
can inherit a verified child label; broad parent labels are not interval labels.

Official metadata and ground truth come from [FSD50K record 4060432](https://zenodo.org/records/4060432).
Publisher ZIP MD5s are checked. Selected original WAV entries are obtained through
bounded HTTPS byte ranges in those archives, checked against ZIP CRC32 and pinned
with SHA256. All initial network bytes, including metadata and ZIP indexes, stay
below 100 MB. No playback, capture, user files, external upload or new weights.
FSD50K is CC BY 4.0 as a curated dataset; individual authors/licenses remain in
the selection. Metadata itself is not executable code.

The exact unchanged Java `RecordedPcmResampler` converts complete original
44.1 kHz PCM to 16 kHz, retaining its real supported interval and origin. The
exact unchanged Java `RecordedSoundFrontend` then supplies each bundled ONNX
model's own mel input. Actual nonoverlapping 10-second windows and tails at least
two seconds retain the shipping policy's timing. No gain, denoising, repetition,
padding to an invented duration, or model-weight training. CPU ONNX sessions use
one intra/inter thread, sequential mode; the JVM sees at most two processors.

Baseline is the unchanged recorded-analysis policy: PANN laughter .10/.025
score/margin and EfficientAT instruments .65/.15. Development-only calibration
may choose laughter thresholds {.10,.15,.20,.30} with margins {.025,.05,.10}, and
instrument thresholds {.35,.45,.55,.65} with margins {.15,.25}. These candidates
are predeclared; no additional settings will be invented after evaluation.
Instrument family/Music compatibility and laughter priority remain unchanged.

Select separately per family using development data only: require no newly
accepted distractor source and maximize true-positive source count; ties prefer
the higher threshold, then higher margin. If no candidate meets that constraint,
retain the baseline and reject calibration for that family. Freeze the resulting
policy and hashes before inference of evaluation audio. Evaluation runs once.
A source is accepted if any actual analyzed window qualifies; preserve every
window score, competitor, uncertain/missed source and omitted tail.

A shipping policy change needs at least two additional correctly accepted
evaluation positives overall, no family loses correct positives, and no newly
accepted evaluation distractor or wrong target-family source. Otherwise record
the rejected candidate, not a production threshold change. Existing automatic
YAMNet snore/speech/cry/cough code and decisions must remain byte-identical;
these supplementary models remain explicit recorded-audio analysis only.

FSD50K's human clip-level annotations are stronger than uploader descriptions,
but development labels can be incomplete and evaluation completeness covers
only its vocabulary. This study does not establish target-room accuracy,
interval onset precision, AudioSet training disjointness or automatic overnight
recognition. Reliable automatic laughter/instrument recognition remains a
separate unfinished requirement even if this small generic-clip study passes.

## Development-only model-selection amendment

Before any evaluation inference, the first development pass completed on all 24
sources (31.022 seconds measured DSP/inference wall time). Current laughter
accepts 2/8 and EfficientAT instruments 0/8; the entire original instrument
threshold grid also accepts 0/8. Several short clips saturate unrelated
EfficientAT classes, so reducing that threshold does not address the failure.
The already-executed PANN instrument-family outputs accept 4/8 at .35/.15 with
no wrong-family/distractor development acceptances. All raw vectors are retained.

The extended development-only candidate is an explicit PANN instrument fallback:
preserve every originally qualified EfficientAT decision, otherwise compare the
PANN instrument family with the same predeclared instrument threshold/margin
grid. No new model, frontend, family indices or weights. The fallback excludes
the compatible Music parent from competition exactly as the existing instrument
policy does. Families without a strict development true-positive gain retain
their original policy, so the laughter cutoff remains .10/.025. Freeze this
selected model policy before the first evaluation inference. The original
evaluation gate above remains unchanged, including no newly accepted negatives.

## Frozen evaluation and shipping result

Selection/protocol commit: `ecb8fc8f`. Development-only model-selection amendment
and frozen policy: `67491a79`, before the first evaluation inference. Exactly 48
original WAVs from 48 uploaders were fetched; the initial metadata/index/audio
network total was 29,563,956 bytes. Neither existing model was downloaded again,
re-exported or trained. The unchanged Java DSP and bundled CPU models each ran
once on the 24 evaluation sources. No evaluation scores selected a setting.

| Source-level outcome | Development baseline → selected | Frozen evaluation baseline → selected |
| --- | ---: | ---: |
| Laughter, eight positives per split | 2 → 2 | 3 → 3 |
| Instruments, eight positives per split | 0 → 4 | 1 → 6 |
| Newly accepted distractor or wrong-family sources | 0 | 0 |
| Lost correctly accepted positives | 0 | 0 |

The five additional evaluation instrument sources are Freesound IDs `233213`,
`328665`, `348311`, `351519`, `434629`; the retained EfficientAT positive is
`339982`. Five laughter and two instrument evaluation positives remain missed.
The predeclared shipping gate passes for this bounded offline policy only.

The shipping Java `RecordedSoundPolicy` was compiled directly with the existing
JDK and checked against **cached** development and evaluation vectors: all 48
actual window decisions match the frozen policy, and every qualified EfficientAT
estimate is preserved. This check did not rerun either split's model inference.
Ten retained negative controls (two snoring, three speech, two cough, two infant
cry, one adult cry) ran through the unchanged Java frontend and bundled ONNX
Runtime 1.23.2 CPU models over 33 actual windows. They accepted no laughter or
instrument candidates and no new fallback instrument windows. These old control
examples were not a tuning set; the original failed laughter/instrument holdout
was neither rerun nor tuned. Automatic YAMNet/capture/cue code is unchanged, so
these control checks concern supplementary false candidates, not a fresh
validation of automatic overnight event sensitivity.

The production change is limited to explicit recorded analysis: preserve the
original EfficientAT .65/.15 decision first, otherwise admit PANN instruments at
.35/.15; laughter remains .10/.025 and keeps priority. The instrument label and
score identify the selected model. New schema-1 results add policy/model identity
and both original instrument estimates; old stored results remain intact under
their original version. There is no additional model inference, asset or frontend
change at runtime. The existing native journey's expected version was updated,
but that journey, Gradle, CI and device execution were not run in this source slice.

### Auditable retained outputs

`third_party/supplementary-sound/fsd50k-study/` retains:

- `selection.json`: all human labels/ratings, author, source, licence and archive entry identities.
- `download-receipt.json`: original WAV SHA256 values and bounded network accounting.
- `frozen-policy.json`: complete development-only grid and selected settings.
- `result-summary.json`: source acceptance IDs, misses, exact raw-result hashes,
  shipping-policy/DSP/model hashes, control attribution and PCM hashes.
- `source-windows.csv` and `control-windows.csv`: every actual analyzed window's
  family/competitor scores, gate outcome and timing, including uncertain results.

Complete 527-score vectors, original WAV/PCM, exact converted origins and control
scores remain locally in ignored `.android-local/recognition-fsd50k/` (controls
under `policy-verification/`), pinned by the tracked receipts. They are not bundled
in the APK. `study.py` prevents a second evaluation run; `verify_policy.py` checks
shipping Java decisions against existing vectors and runs only retained controls.
Its resume option skips already completed controls and requires unchanged policy
and DSP hashes. No personal-phone, microphone, playback, alarm, audio upload,
new unit-test campaign or security sweep was used.

This result does **not** establish reliable automatic bedroom recognition,
training disjointness, calibrated probabilities, interval accuracy or mobile
performance. Human FSD50K clip labels are not native target-room interval truth;
48 generic clips with seven missed evaluation positives cannot close F171's
automatic-recognition requirement. The old failed holdout and all previous model
and source attribution remain available.
