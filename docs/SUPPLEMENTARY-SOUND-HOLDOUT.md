# Supplementary audio-tagging operating point

Historical policy and failed holdout retained unchanged. The 31 October separately
frozen study supports a new explicit offline PANN instrument fallback; this old
holdout was not rerun or retuned. See [new protocol and results](FSD50K-RECOGNITION-CALIBRATION.md).

Declared before new holdout audio download/inference. Existing retained fixture
results are calibration evidence, not a new untouched holdout. Production mobile
YAMNet remains unchanged, including laughter0.65 and accepted snore/speech rules.

Candidate: PANNs MobileNetV2 (weight SHA256
22437af9eb5ed1854b5ad285b4a17d5fc566f1a3b23df40db5541f5705ca340f)
for laughter; EfficientAT mn10_as (weight SHA256
0bd7dc2443af498c289a2e739f02ebb515d6aa3fd3ab9db539c86123ae368a4e)
for a broad instrumental-sound estimate. A low PANNs score is not a YAMNet score.

Use actual complete nonoverlapping10second audio-tagging windows; a final real
tail of at least2seconds is inspected at its actual duration. Shorter tails are
reported omitted, never padded/repeated. PCM16/32768, deterministic polyphase
16→32kHz resampling, original model frontend and eval mode; no gain or denoising.
These are coarse window estimates, not975ms event onset measurements.

Model-specific candidate: PANNs maximum laughter-family score>=0.10 and margin
>=0.025 over unrelated classes, then EfficientAT maximum musical-instrument
family>=0.65 and margin>=0.15 over unrelated classes except compatible Music.
Laughter wins only when its own gate passes. No family-score summation.
Thresholds and hierarchy will not be retuned on these new holdout results.
The instrument family excludes Bell/bicycle bell/tuning fork/chimes. This policy
is experimental and does not grant alarm dismissal or overwrite user labels.

New predeclared whole-preview sources, all CC0 primary uploader descriptions:

| Source | Role | Primary page |
| --- | --- | --- |
| frenkfurth, Laughing007 | Laughter positive | https://freesound.org/people/frenkfurth/sounds/650910/ |
| kikorurelas, Female crowd laughing | Laughter positive | https://freesound.org/people/kikorurelas/sounds/767470/ |
| Bradovic, Piano | Instrument positive | https://freesound.org/people/Bradovic/sounds/164718/ |
| qubodup, Guitar Fail | Instrument positive | https://freesound.org/people/qubodup/sounds/191322/ |
| FunnyVoices, Counting1to10 | Laughter/instrument negative | https://freesound.org/people/FunnyVoices/sounds/546065/ |

Pin preview and decoded PCM hashes before inference; preserve each entire
public preview up to60seconds. Missing/inaccessible named sources remain missing,
not replaced after seeing a score. Broad descriptions are not interval ground
truth or proof of training disjointness. Require both new laughter sources and
both instruments to yield at least one accepted coarse window; the speech source
must yield no accepted new class. Retain all misses and surprising classes.
Failure does not justify another cutoff fit to this holdout.
