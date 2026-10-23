# Local sleep-sound estimates

This implementation adds a genuine on-device YAMNet classifier, not an amplitude-to-snoring rule. It is a general sound model, not a validated sleep monitor or a calibrated probability estimator. Speech can come from television or another person; it does not establish that the sleeper talked. Snoring estimates can be wrong. No anti-snoring output is enabled by this work.

## Actual path

The existing user-enabled microphone capture remains 16 kHz mono PCM16. A fixed 15,600-sample float window (PCM16 / 32768) is sent to the bundled mobile model on the same capture worker. Model creation, warm-up, inference, buffer reuse and destruction all run there. CPU only is intentional for compatibility and avoids pretending a GPU fallback was exercised. There is no model-download button or network dependency.

A snoring estimate requires Snoring to beat all 520 alternatives, score >=0.60 and margin >=0.15. Speech needs >=0.65 with the same margin. Dominant silence is ignored; uncertain recognized sounds retain Other sound. Scores are uncalibrated model scores, not percentages of certainty. Two consecutive matching 0.975-second windows are required before an event is stored. Events split after ten windows; incomplete PCM tails and isolated positive windows are discarded on stop/pause. At most 3,600 metadata events are retained per tracked session, with the existing 60 WAV budget separate and shared across microphone restarts. Thresholds are conservative engineering defaults, not thresholds calibrated on clinical bedroom recordings.

SQLite schema5 adds sound_events and clip metadata columns without rewriting old alarms, sessions, epochs or WAV rows. Existing clips default to noise/score0; no retroactive labels are invented. Analysis-only mode persists events without saving any audio. The current energy-triggered 10-second WAV path remains separate: a stored estimate does not imply an associated clip or playable waveform. Clip playback stays in Recorded signals. Favorites, per-event linked recordings and the complete board07 player design remain incomplete.

The Sound events screen is reachable from Recorded signals and displays actual timestamps, durations and category filters. Empty/loading/storage-error states are explicit. Current layout is partial board07 fidelity; screenshot review is still required before acceptance.

Model initialization/inference failure disables labels for that microphone instance and records analysisError; measured room amplitude and optional WAV capture continue. Stop still joins capture before the database closes. A failure to store events is surfaced rather than silently claimed as success. Erasing a night/history or restoring a backup also clears its event metadata. Portable backup format3 still excludes WAVs and sound-event metadata; this limitation is explicit and requires a future portable-audio backup flow.

## Evidence and limits

Pure tests cover all-label dominance, score/margin rejection, malformed outputs, transient false positives, duration grouping, PCM chunk boundaries/normalization, no incomplete-window padding, initialization/inference fallback, no work after close, and shared metadata/clip budgets. Native tests perform actual CPU inference on deterministic silence, sine and white-noise tensors and persist/reopen/delete an analysis-only fixture in a uniquely named database. Synthetic negatives check installation/false-positive regressions; they do not validate human snoring or speech accuracy. No licensed human bedroom evaluation set has been incorporated yet.

No physical microphone, playback, alarm, flashlight or vibration was exercised for this change. 177 local pure tests passed, debug/application-test APK builds passed, and lint reported zero errors /66 warnings. Native fixture tests have not run yet and must pass on the CI emulator before this feature is reported verified. Lint also prints a LiteRT Kotlin2.4 metadata diagnostic from its bundled Kotlin2.2 analyzer despite the successful lint task; this should be addressed with a compatible toolchain update, not ignored as an inference result. Background battery cost, long-night capture continuity, positive-class accuracy and real-phone inference latency remain unverified.

Primary references: [mobile model card](https://www.kaggle.com/models/google/yamnet/tfLite/classification-tflite/1), [TensorFlow model installation tests](https://github.com/tensorflow/models/blob/master/research/audioset/yamnet/yamnet_test.py), [LiteRT CompiledModel](https://ai.google.dev/edge/litert/android).
