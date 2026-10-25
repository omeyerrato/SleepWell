# Speak wake challenge

Speak is an optional one-unit routine task: explicit Listen accepts two consecutive full 15,600-sample windows that the unchanged bundled CPU YAMNet and production policy classify as broad Speech. It detects speech sounds, not words, speaker identity, authentication or laughter. No captured audio, transcript or sound event is saved. Existing tasks and default alarms are unchanged.

The user must open the challenge and explicitly grant microphone permission and tap Listen while focused. Opening, recreation, permission return and saving the routine never start capture. A private one-use request expires after five seconds if the nonexported microphone foreground service cannot claim it. The service does not restart. A released service owner can adopt a new request, while its old completion callback cannot stop the replacement.

Before AudioRecord allocation, the matching alarm occurrence suspends all audible players and acknowledges their physical stop and the retiring-player barrier. Vibration, torch, screen sunrise, smart-light and alarm lifetime behavior retain their own settings. Only the still-current occurrence may resume sound after confirmed microphone cleanup. Silent alarms remain silent. Model loading and PCM inference run on one worker. A shared process microphone lease excludes sleep recording and sonar; their existing single-capture fanout remains intact. Calls, denied/revoked permission, client-silenced capture, another capture, foreground denial and quiet-barrier failure expose Math fallback. Math still has to be completed.

Listening eligibility is bounded to 15 seconds including model startup. Stop, focus loss, pause, expiry and occurrence replacement revoke completion. Native reads are nonblocking. Native release cannot be promised to finish within a deadline: if release fails, ownership and the matching sound pause remain blocked and the UI offers explicit cleanup retry. No timeout discards an uncertain native microphone owner. Retrying cleanup does not restart capture. The activity keeps the screen awake while visible; Android and OEM service/microphone restrictions still apply.

The frozen laughter-positive gate remains unchanged and failing; this task does not claim a new laughter detector or microphone/room accuracy. Six existing licensed CC0 whole PCM sources are pinned by SHA-256 in `SpeakFixtureEvaluationTest`: two broad-speech positives and snore/music/cry distractors. The model hash and original policy are pinned. This is source compatibility evidence, not a representative accuracy study.

## Verification contract

Host `SpeakRuntimeTest` covers rule/reset, immediate and delayed quiet callbacks, executor rejection, permission/cancel/deadline, owner replacement, uncertain cleanup, exclusive microphone lease, one-use admission expiry, service start-ID handoff and result proof. Native tests are configuration/private-storage/fake capture only, except CPU inference of licensed PCM; no actual microphone, service, alarm output or MediaPlayer is executed.

- `SpeakStorageTest`: 1 method; private SQLite, portable inactive restore, legacy preservation and invalid-round rollback.
- `SpeakConfigurationFlowTest`: 1 method; `speak-config-task.png`, `speak-config-recreated.png`, `speak-config-routine.png`, `speak-config-reopened.png`.
- `SpeakCaptureFlowTest`: 2 methods; `speak-capture-permission.png`, `speak-capture-ready.png`, `speak-capture-quiet.png`, `speak-capture-listening.png`, `speak-capture-closing.png`, `speak-capture-recreated.png`, `speak-capture-cleanup.png`, `speak-capture-error.png`.
- `SpeakFixtureEvaluationTest`: 1 method; required `speak-fixture-evaluation.json`, written before outcome assertions.

Native runtime, actual foreground delivery, real microphone/output cleanup and device behavior remain unverified until separately tested with user authorization. No physical-device effects were run while implementing this feature.

Local validation: final scoped Gradle build passed in 3m05s: debug APK, native test APK, 71 focused tests in six suites (18 Speak tests), lint zero errors/137 warnings/one hint. Superseding combined8027f83 passes1016 pure tests/125suites, both APKs and lint. Native CI40159540059 passes all five Speak methods,12 declared originals and the required six-trial CPU JSON; repeat CI40166582977 also passes this subset. Actual capture/output/service behavior remains unverified. The whole runs have separate failures and do not constitute full-app acceptance.
