# Per-class sound sensitivity: minimum event duration

The controls adjust **temporal acceptance**, not loudness, score calibration or model accuracy. Each inference window is 15,600 actual 16 kHz samples (975 ms). An accepted estimate must persist in consecutive windows of the same class:

| Choice | Minimum accepted windows | Actual minimum audio |
|---|---:|---:|
| High |1|975 ms (about 1 second)|
| Default |2|1,950 ms (about 2 seconds)|
| Low |3|2,925 ms (about 3 seconds)|

Snoring, Speech, Cough/throat clearing, Baby crying and Laughter have independent choices. Other sounds retain their two-window rule. High retains shorter events and can increase false detections. Interrupted input, silence or a class change cannot bridge shorter fragments into a qualifying event. Ten-window event bounds, score means and original capture times remain unchanged. No partial classifier window is padded or counted.

Every existing score threshold, unrelated-label margin and AudioSet family rule is unchanged. Missing settings use exactly the old two-window defaults; the original default-policy assertions and pinned human-fixture tests remain unchanged. The existing incomplete recognition of independent laughter recordings remains a genuine coverage limitation, not a threshold problem claimed solved here. Manual labels/model IDs/scores are never rewritten by settings changes.

## Runtime ownership and persistence

A genuine TrackingService admission snapshots the complete immutable policy once. Its per-night ClipBudget supplies that object to ordinary 16 kHz noise analysis and every restart. Contactless capture passes the same policy through SonarNight, its per-night sound owner, bounded worker, resampler and analysis stream; pauses/gaps/resumes retain the policy. Saving during an active or armed night affects its next admitted night, not its current inference.

Settings live in a separate private `sound_detection_v1` namespace. A strict versioned JSON codec requires exactly five class values and an integer version. Unknown/missing fields, nonnumeric/fractional/out-of-range values and oversized content reject. An absent legacy profile is the exact default. Corrupt saved data produces an explicit settings warning and safe defaults without silently overwriting the original value. Saving is explicit and runs through one retained immutable worker, with disabled controls while busy, rotation observation, failure/retry and pinned storage ownership. Saving opens no recordings, requests no permission and constructs no model, microphone, clip, cue or output.

Portable backup format 6 remains unchanged and excludes these global preferences, as it already excludes other global preference settings. Restoring sleep records does not import duration choices. Original sound-event category/score/model/timing, manual corrections, existing clips and per-night sound-analysis provenance retain their existing semantics. A newly accepted short event still passes the same Store/portable interval checks.

Snoring-cue score sensitivity, strength, cooldown, session cap and default-off consent remain separate and unchanged. These duration controls grant no capture or cue consent. They also do not alter the amplitude threshold used to save optional WAV clips.

## Verification contract

- SoundSensitivityTest adds 12 focused pure tests: exact default compatibility, independent 1/2/3-window rules, immutable snapshots, gap/class fences, maximum bounds, original mean/start, unchanged classifier gates, invalid policies, no padding and the shared-sonar resampler seam. Original sound policy/stream/sonar tests remain unchanged.
- SoundSensitivityStorageTest has 3 native private-fixture methods covering default/persistence/next-run snapshots; strict malformed data without mutation; actual SQLite short-event persistence and unchanged portable evidence/restore. No capture or model allocation is involved.
- SoundSensitivityFlowTest has 2 native configuration journeys with 11 required actual originals: `sound-sensitivity-default`, `selector`, `draft`, `recreated`, `saved`, `reopened`, `invalid`, `saving`, `saving-recreated`, `save-error`, `retry` (all.png). They exercise owned selectors/Cancel, per-class state, save/reopen, a held worker across rotation and explicit failure retry. Settings use private injected preferences; recording-store access is a hard test failure.
- Host JUnit sound-policy/stream/sonar subset: 43 tests passed. Final Android debug and native-test APK builds passed in 3m43s; 64 focused tests across nine suites passed with zero failures/skips, and lint reported zero errors/93 warnings. Native execution and fresh visual comparison remain pending. No personal-phone action, microphone, alarm, output or live permission test was performed.

This is a user-selected event-duration policy. It supplies no medical accuracy claim, speaker identification, calibrated probabilities or proof that an original recording contains a particular sound.
