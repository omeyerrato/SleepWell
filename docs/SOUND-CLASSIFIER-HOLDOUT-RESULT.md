# Laughter correction candidate rejected

The frozen 0.30 laughter cutoff does **not** generalize to the predeclared independent sources: **0 of 3** produce grouped laughter. Production `SoundAnalysis`, `SoundAnalysisStream`, model bytes and all existing native acceptance checks remain unchanged. Laughter recognition is still a coverage limitation, not a completed feature or a passing classifier gate.

The plan was committed as `bc92056874aa83005c99fbffbc788cac9251a33a` before new downloads/inference. All six exact whole-preview PCM files and CC0 origins were pinned as `60f929a2ccc637b567d97a12c53e89c2903dc1b4` before inference. None was cropped, normalized, repeated, replaced or played. Primary source descriptions are broad labels rather than annotated windows; sources are not guaranteed disjoint from model training data.

## Actual results

[Full host evidence](verification/2026-10-24-laughter-holdout/host-evaluation.json) includes every frame's top-eight model scores, both real Java policy/group decisions, all event durations/scores, original source/model/policy hashes, acceptance checks and Android comparison differences. One host LiteRT 2.3.0 CPU interpreter supplies scores to the actual Android-free production Java `SoundAnalysis` and `Group`. Only the analysis driver contains the frozen candidate; the app does not.

| Source | Production laughter events | Frozen candidate laughter events | Other named outputs retained |
| --- | ---: | ---: | --- |
| original solo laughter | 0 | 2 | No snoring, speech, infant cry or cough |
| original crowd laughter | 0 | 0 | 4 speech; missed laughter remains visible |
| new Jody guffaw | 0 | 0 | No other named event |
| new morningfrost laughter | 0 | 0 | No other named event |
| new Eneasz laughter | 0 | 0 | No other named event |
| new solo countdown | 0 | 0 | 4 speech |
| new piano | 0 | 0 | No other named event |
| new adult crying | 0 | 0 | No other named event; not an infant-positive claim |
| original bonehead infant | 2 | 2 | 2 infant cry; ambiguous laughter outputs are not hidden |
| original ciccarelli infant | 0 | 0 | 5 infant cry |
| original Fabrizio cough | 0 | 0 | 1 cough |
| original crowd cough | 0 | 0 | 4 speech, no cough; missed cough retained |
| original Sorts snoring | 0 | 0 | 9 snoring |
| original pencil snoring | 0 | 0 | 3 snoring |
| original solo speech | 0 | 0 | 1 speech |
| original crowd speech | 0 | 0 | No named event; existing coverage retained |

All old snoring/speech/cough/infant event counts and cross-negatives remain identical. All three new distractors produce zero laughter. However, the required two independent positive sources fail. Reducing the cutoff would fit the original solo clip without sufficient independent positive evidence. No other cutoff was tuned on these outcomes.

The original solo laughter's maximum score is 0.5859375, below the app's 0.65 cutoff. New maxima are 0.33203125 (Jody), 0.4140625 (morningfrost), and 0.33203125 (Eneasz); qualifying evidence is isolated or loses the unchanged unrelated-label margin. An earlier diagnostic 480ms-hop run on the original ten sources also produced zero grouped solo/crowd laughter, so framing was not rewritten. Scores are model outputs, not calibrated probabilities; family scores are never summed to manufacture confidence.

## Native comparison and verification limits

The [archived Android run 40079043221](https://github.com/omeyerrato/SleepWell/actions/runs/40079043221) passes `SoundClassifierFixtureEvaluationTest` but fails the independent solo-positive assertion in `SoundCategoryFixtureEvaluationTest`. Host baseline event counts, frame counts and PCM hashes match all ten Android sources. **Exact score parity does not pass:** one crowd-cough window has Speech 0.80078125 on this host versus 0.73828125 in Android, plus a child-score difference. The complete differences remain in the report and the exact-parity check stays false. This evaluation cannot substitute for Android execution.

Nine adversarial evidence tests pass: complete reference acceptance, truncated host/reference windows, repeated frame positions, duplicate sources, changed scores, empty rankings, invalid scores and duplicate labels. They protect evidence integrity rather than relabeling an audio failure as a test success. The actual evaluator writes evidence then exits **2** for the rejected candidate. No Gradle/native test execution, phone action, capture, playback, alarm, microphone, torch or sensor operation was performed for this investigation. The existing failing native positive assertion is retained.

## Reproduce without a device

Verify all sixteen pinned files without inference or extra dependencies:

```text
python scripts/evaluate_laughter_holdout.py
python scripts/test_laughter_evidence_integrity.py
```

For inference, install official `ai-edge-litert==2.3.0` into an isolated Python environment; it is an analysis-only dependency, not an app dependency. Supply the runtime folder if it is not already importable and a JDK containing `java`/`javac`:

```text
python scripts/evaluate_laughter_holdout.py --infer --runtime-dir <isolated-runtime> --jdk <jdk-folder> --output <report.json>
```

Optionally add `--android-baseline <archived-artifact>/analysis-category-fixture-evaluation.json` for the strict reference comparison. The output records that input's SHA256. Windows host tools use `java.exe`/`javac.exe`; one CPU thread and bounded 64MiB Java heaps are used. No package is installed automatically and no file is downloaded by this evaluator.

A further correction needs a separately designed model or operating-point study with annotated representative sounds, source-separated validation and a new untouched holdout. This failed holdout must stay retained. Until then, the app must continue presenting these outputs as estimates and must not claim reliable laughter recognition.
