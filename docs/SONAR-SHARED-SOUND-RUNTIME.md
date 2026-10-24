# Optional sound estimates from the manual contactless capture

This implements a manual, per-night, **analysis-only** connection. Audio clips and anti-snoring cues during sonar remain absent. No microphone/speaker/service or physical phone was exercised while authoring this flow. Native test execution and actual composed screenshots are pending.

## User flow

Tracking setup's **Analyze sleep sounds** switch defaults off for a new contactless night. The adjacent copy explains that sonar itself already uses the microphone, that enabling analysis applies only to this night, and that clips/cues are excluded. Rotation retains an unsaved choice; reopening a new night resets it off. Starting still requires the existing tested contactless profile, microphone permission, visible activity and real capability checks. Saving/reopening a setting never starts capture.

The single-use `SonarEntry.Ticket` carries immutable consent. Resume needs another visible one-use ticket matching the same session, gain and original consent; it cannot escalate a running night. Neither automatic nor recurring starts can select this path. No global `noiseEnabled`, `saveClips`, sound setting or engineering sensor configuration changes.

Live tracking reports preparing, analysis only, paused, off or needs attention based on actual successful inference state. Failure text is concise and specific. Existing sound thresholds/model/grouping are unchanged, including the unresolved limited solo-laughter coverage.

## Capture and storage ownership

`SonarPump` offers eligible actual 48 kHz blocks from its existing single AudioRecord through `NightSink.pcm`; calibration does not offer blocks. The array is copied before reuse. CLIPPED or unusable capture resets the sound frame/filter history. A usable microphone block may still be analyzed when the echo is NO_ECHO: no movement estimate is fabricated. The existing audio route, timing, silence and ownership checks still govern the pump.

`SonarSoundWorker` owns its one model and 64-block queue. Conversion uses the original capture BOOTTIME anchor, never inference/current wall time. No boundary padding, cross-gap partial frames, second mic, WAV allocation or cue output is introduced. A model failure or queue overload stops only analysis and remains explicit; sonar may continue. Resume cannot retry failed analysis for that night.

`SonarNightSound` holds a separate bounded queue of actual grouped estimates. The one existing storage worker persists estimates only inside durable capture coverage, and persists a successful-inference prefix only once durable active time covers it. Terminal error metadata waits until the model is released and its pending event queue has drained. The per-night 3,600-event cap stays bounded: the first discarded later estimate stops analysis and reports `error/limit`, never `complete`. This discloses missing later estimates; it is not an accuracy score.

Pause/stop first close the actual duplex IO. The storage executor then requests analysis finish at the owned original-time cutoff, returns immediately, and waits by callback for real model cleanup acknowledgement. It never joins or interrupts an in-flight model on the UI/storage thread. Counts refer to successful captured-PCM inferences; the model constructor’s initialization warm-up is excluded. Only then can it publish PAUSED/CLOSED, allow a replacement segment, or atomically save the final evidence with the night end. A blocked model retains the stopping state. A storage error still requests worker cleanup even when the final epoch write fails. The saved `sound_analysis` codec, recovery and format-six behavior are defined in [the proposal](SONAR-SHARED-SOUND-PROPOSAL.md).

## Verification contract

Final local debug/test APK compilation, focused unit tests and lint passed in 2m48s: 104 relevant pure tests in 15 suites, zero failures and zero lint errors (93 warnings). The exact native bodies, including fixture bounds/hash/CI gates, compiled. Native execution and all nine composed originals remain pending. Five new full pump/DSP/conversion/fake-model lifecycle tests cover single capture, pause/resume with cumulative evidence, held model cleanup, model failure without replay, and invalid capture quality. A real production-grouping cap-boundary test proves that three injected groups with a cap of two preserve two and finish as explicit `error/limit` with nine actual model calls. These are CPU/fake-port results, not hardware measurements.

`SonarSharedSoundFlowTest` has two inert configuration methods and nine original Android PNGs planned:

- `sonar-shared-opt-in-default-off.png`
- `sonar-shared-opt-in-opt-in-recreated.png`
- `sonar-shared-opt-in-analysis-live.png`
- `sonar-shared-opt-in-analysis-paused.png`
- `sonar-shared-opt-in-stop-confirmation.png`
- `sonar-shared-error-model-error.png`
- `sonar-shared-error-model-error-recreated.png`
- `sonar-shared-error-model-error-resumed.png`
- `sonar-shared-error-stop-confirmation.png`

It uses the paired virtual signal timeline and real SQLite admission from root's `2477cbe` fixture, while keeping nonce checks on real SystemClock. It verifies actual Start/Pause/Resume/Stop actions, draft rotation, private reopened zero-event/error evidence, next-night default-off, original preference preservation, and no service/hardware path.

`SonarSharedClassifierStorageTest` adds one CPU-only Android method using the existing three pinned CC0 whole previews (two snore sources and solo speech). It repeats their original samples to 48 kHz, feeds the actual causal filter/production stream/bundled model, verifies exact actual inference counts, preserves snoring positives and solo-speech cross-negative policy checks, reopens real private storage, and writes `sonar-shared-classifier-replay.json`. Test-producer pacing merely keeps the replay under the bounded queue; production capture never waits. The JSON evidence is written before its preserved positive/cross-negative policy gates are asserted. No result is claimed before that gate executes. The private fixture is clearly separate from personal history and its source hashes/licenses remain in the existing audio-fixture manifest.

Remaining limits include actual phone cold-model latency, sustained realtime CPU/battery use, echo leakage in real rooms, source preprocessing and classifier accuracy. None is established by a successful build, synthetic arrays or fixture replay. Classified sound events are estimates; this is not validated clinical sleep or anti-snoring detection, and it does not complete the wider requested app feature set.
