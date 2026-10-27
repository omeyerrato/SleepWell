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
were checked on unchanged real vectors; no new unit-test batch, personal-phone, microphone, alarm or sound output was used. A later bounded host-muted Android flow executes the real models as documented below.

Latest source5266e54b builds production in27s with the original signer. Native saved-clip CPU analysis/save/reopen/delete passes13.826s with three inspected captures; both windows remain Uncertain. [Exact scope and original outputs](verification/2026-10-27-recording-analysis/README.md). Imported providers, physical capture/output/overnight behavior and recognition accuracy remain open. No new unit/lint/broad campaign or personal-phone action.

## Selected decoded-audio resampling contract

`RecordedPcmResampler.resample(short[] mono, int rate, BooleanSupplier active)`
is an original pure-Java conversion primitive for explicitly selected, decoded
continuous audio. It throws `IOException`; false `active` or worker interruption
throws `InterruptedIOException`, without returning partial PCM. Callers retain
source/decoded identity and must not concatenate separate or gapped intervals.
No decoder, capture, player, permission, model or automatic YAMNet path starts
inside this helper. The existing retained 16 kHz/WAV paths and model frontend
coefficients are unchanged.

Input is nonempty signed mono PCM16 at 8000–96000 Hz, at most 120 seconds.
Exactly 16000 Hz returns an independent bit-identical clone without filtering
or trimming. Other rates use a 193-tap Blackman-windowed sinc FIR with cutoff
`0.45 * min(inputRate, 16000)` Hz, exact rational phases (at most 16000 phases),
and unity **filter DC** gain; source peaks are never normalized. The coefficient
table is bounded to 3,088,000 floats (~12.35 MB plus array overhead), output to
1,920,000 samples, and processing to 193 tap operations per retained output.
There is no padding, edge repetition, denoising or invented duration. Rounding
and saturation are solely the final PCM16 representation. Cancellation is
checked during each phase construction and every 256 output samples.

The untrimmed grid has `floor(inputFrames * 16000 / inputRate)` frames. A centre
is retained only if all 193 actual input frames exist; unsupported edge centres
are omitted, and a too-short interval with none remaining is rejected.
`Result` contains `pcm`, `inputRate`, `firstOutputFrame`, `fromInputFrame`,
`omittedStartMs`, `omittedEndMs`. **Exact output origin is
`firstOutputFrame / 16000` seconds relative to decoded source start**;
`fromInputFrame` is only the floor of that position in input-frame units.
Omission milliseconds round down for display; retain the original decoded
frame count/rate and exact output origin for interval provenance. Output ends
at `(firstOutputFrame + pcm.length) / 16000` seconds, never at the original EOF
by implication. Decoder/job/UI integration must preserve those trims.

A bounded direct JDK probe used analytical two-second, peak-10000 PCM16 tones,
not recognition fixtures or a unit-test batch. At 1 kHz for 8/44.1/48/96 kHz
input, retained output durations were 1.976/1.9956875/1.996/1.998 seconds and
first output frames 192/35/32/16 respectively. Measured 1 kHz amplitude stayed
within 0.009% of 10000; RMS alignment error was at most 0.172 PCM units.
12 kHz input tones at 48/96 kHz produced zero-valued PCM16 output after filtering
and rounding: residual was below that probe's PCM quantization resolution,
not a claim of infinite attenuation or rejection at every frequency. Exact
16 kHz clone behavior and pre-conversion cancellation were also checked.
No Gradle build, new model inference, training, download, device, microphone or
playback action ran. This narrow DSP evidence does not establish recognition
accuracy, Android decoder compatibility or device performance.
