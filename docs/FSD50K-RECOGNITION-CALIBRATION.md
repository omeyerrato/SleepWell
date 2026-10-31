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
