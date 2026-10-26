# Experimental offline recorded-sound analysis

`RecordedSoundAnalyzer` is a real CPU inference engine for explicit analysis of
a retained or imported recording. It uses bundled PANNs laughter and EfficientAT
instrument classifiers with their distinct frontends. No server, account, model
download, microphone access, playback, alarm or automatic cue is involved.

Production YAMNet, its0.65 laughter cutoff and accepted snoring/speech/cough/cry
rules are unchanged. This feature does not mutate sleep sound events, user
corrections or automation events. Its persisted results belong to a separate
experimental analysis record. It cannot grant alarm dismissal or infer sleep
stages. **Reliable automatic laughter/instrument recognition remains unfinished**:
the frozen new holdout missed2/4 positive sources. Uncertain does not mean absent.

## Worker contract

Create, use and close on the same background worker, inside try-with-resources:

```java
try (RecordedSoundAnalyzer analyzer = new RecordedSoundAnalyzer(context)) {
    RecordedSoundAnalyzer.Result result = analyzer.analyze(pcm, cancelled, progress);
    JSONObject saved = result.json();
}
```

Input is signed PCM16 mono at exactly16kHz, at most1,920,000 samples/120seconds.
The caller owns strict WAV/source validation, source identity/hash, stale clip
checks, progress lifecycle and result persistence. Constructor, `analyze` and
`close` throw `Exception`; model/runtime/integrity failures are errors, never
successful empty estimates. A process-wide lease admits only one live engine;
its sessions use one CPU thread and sequential execution. Process-owned ONNX
environment is not closed by this feature. Tensors, results and both sessions
are explicitly closed, including partial constructor failure.

There are actual nonoverlapping10second windows, plus a final real tail >=2s.
A shorter tail is reported omitted; input shorter than2s yields no analyzed
window. No source gain, denoising, repetition or invented duration is used.
The deterministic centered41tap Kaiser5 polyphase filter upsamples16→32kHz;
upsampling cannot recreate microphone frequencies discarded at16kHz.

PANNs: FFT1024, hop320, periodic1024 Hann, reflect center,64Slaney mel bands
50–14000Hz, power→10log10(max(1e-10,mel)). EfficientAT: valid preemphasis
[-.97,1], FFT1024, centered nonperiodic800Hann, reflect center,hop320,
128Kaldi mel bands0–15000Hz, (ln(mel+1e-5)+4.5)/5. Exact coefficient arrays
are exported from the original frontends; Java radix2 FFT is numerically checked.

Cancellation is polled during preprocessing and before/after every native run.
It throws `CancellationException`; callers must not persist a partial success.
Asset/session initialization and one in-flight native inference are not
immediately interruptible. No worker, timer or background service is started
inside the engine. Assets total about36.30MB before APK compression and the
separate native ONNX runtime; mobile latency, RAM and battery cost are not yet
device-verified.

## Public result within the app

`Progress.update(completed,total)` begins at0 and advances after each successful
window. `Result` exposes immutable `windows`, `analyzedMs`, `omittedMs`,
`modelVersion`; `json()` returns schema1:

```json
{"schema":1,"modelVersion":"recorded-experimental-v1-f9dd5647-7bb42270",
 "analyzedMs":10000,"omittedMs":1000,"windows":[
  {"startMs":0,"durationMs":10000,"candidate":"uncertain",
   "instrumentLabel":"Piano","laughter":{"score":0.05,"competitor":0.2,"qualifies":false},
   "instrument":{"score":0.4,"competitor":0.3,"qualifies":false}}
 ]}
```

The example shows schema only, not a fabricated production result. Scores are
uncalibrated model outputs, not percentages of likelihood. `instrumentLabel`
is the strongest family label even when uncertain. Laughter qualifies only if
its PANN family maximum>=.10 and margin>=.025 over unrelated classes. Instrument
qualifies only if EfficientAT family maximum>=.65 and margin>=.15, ignoring the
compatible Music parent. Candidate order is qualifying laughter, then qualifying
instrument, otherwise uncertain. It is a coarse window, not a pinpointed onset.
Both raw family estimates and their strongest competitors remain available.

Sources, licences, hashes, export recipe and all failures are retained in
`third_party/supplementary-sound/`. The two classifier exports and Java frontend
were checked on unchanged real vectors; no new unit-test batch, phone, emulator,
microphone, alarm or sound output was used for this implementation.
