# Laughter-policy holdout plan — declared before inference

Production code, thresholds and all existing acceptance tests remain unchanged at declaration. The archived Android run 40079043221 passes the four-source classifier test but fails the ten-source category test: solo laughter has 64 windows, maximum laughter-family score 0.5859375, and no window reaches the 0.65 threshold. Host LiteRT 2.3.0 CPU inference reproduces all ten baseline event counts. An exploratory 480ms hop still produces no solo/crowd laughter, so it does not justify changing production framing.

The single frozen candidate is **laughter-family maximum >=0.30**, with the existing **>=0.15 margin against every unrelated label**, two consecutive matching 975ms windows and ten-window event bound. This is an engineering candidate, not calibrated confidence. Snoring, speech, infant crying and cough rules, class families, priorities, normalization, frame size, grouping, budgets and old acceptance assertions may not be relaxed. No sum of correlated family scores is allowed.

The following independent sources are selected from primary uploader descriptions and published CC0 licence information before downloading/decoding or inferring their audio. Preview URLs, exact hashes and decoded lengths will be recorded before evaluation. No audio has been played. These descriptions are broad source labels, not time-level ground truth or proof of disjointness from the model's training data.

| Name | Primary page | Declared source context | Compatibility role |
| --- | --- | --- | --- |
| laughter-jody | https://freesound.org/people/jodybruchon/sounds/475212/ | Woman's strong guffaw at an event, 2.493s | Positive; short-duration coverage retained even if missed |
| laughter-morning | https://freesound.org/people/morningfrost/sounds/118487/ | Girl laughing during an interview, 13.669s | Independent positive, possible interview context |
| laughter-eneasz | https://freesound.org/people/Eneasz/sounds/119459/ | Uploader's own male laughter, 5.543s | Independent positive |
| speech-balloon | https://freesound.org/people/balloonhead/sounds/410537/ | Solo male countdown from 30 to zero, 35.399s | Laughter distractor / unchanged speech evidence |
| music-archtects | https://freesound.org/people/archtects/sounds/577461/ | Uploader's peaceful piano loop, 55.920s | Laughter distractor |
| crying-alucard | https://freesound.org/people/AlucardsBride/sounds/172738/ | Adult woman crying with trembling breaths, 26.540s | Laughter distractor; not an infant-positive assertion |

Use each entire available public HQ preview, <=80 seconds and <=3MiB PCM16 mono16kHz, decoded with the same FFmpeg 8.1.1 command as the existing corpus. No gain normalization, denoising, repetition, artificial silence, time stretch, crop, selected successful interval, or replacement after inference. If origin, licence, public access, decoder validity or these resource bounds cannot be verified, mark that named source unavailable and retain the reason; do not replace it based on classifier output.

Before considering a production candidate, require all existing ten-source positive and cross-negative checks unchanged; exactly preserved snore/speech/cough/baby event counts on that old corpus; recovered old solo laughter; at least two of these three new laughter sources producing grouped laughter; and no grouped laughter on any of the three declared new distractors. Keep every missed positive and other unexpected class output visible. Candidate failure means retain production policy and report the limitation; do not tune another cutoff against these holdout outcomes in this round.

Run one host CPU interpreter at a time. This is reproducible compatibility evidence, not Android runtime acceptance, household accuracy, recognition of the sleeper, or clinical validation. Any production change still needs pure regressions, real CPU/framing/policy/SQLite native regression, source review and CI. No device capture, playback, effects or phone actions are permitted.

Primary model references: [official YAMNet model/score description](https://github.com/tensorflow/models/blob/master/research/audioset/yamnet/README.md), [official input conversion example](https://github.com/tensorflow/models/blob/master/research/audioset/yamnet/inference.py). These specify independent class scores and input preprocessing, not a universal 0.65 operating threshold.
