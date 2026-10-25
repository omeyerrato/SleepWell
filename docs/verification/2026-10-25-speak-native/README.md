# Narrow Speak and flip-control native results

Source **a3445e7**, [CI40159540059](https://github.com/omeyerrato/SleepWell/actions/runs/40159540059).
The whole focused increment **FAILS**; its summary retains every failure.

The following narrower results pass:

- SpeakStorageTest1, SpeakConfigurationFlowTest1, SpeakCaptureFlowTest2 and
  SpeakFixtureEvaluationTest1; all12 declared original Android screenshots.
- Six pinned whole-source CPU model trials: two declared independent speech
  positives accepted and four declared snore/music/cry distractors rejected.
  This limited corpus does not establish bedroom accuracy, speaker identity,
  transcription, laughter recognition or instrument recognition.
- TrackingFlipFlowTest2/all7 originals, using fake sensors and private settings.
  One separate storage timing assertion failed; full flip acceptance is pending.

Actual native buttons, permission/error choices, one-use admission, pending quiet,
cleanup/recreation/fallback and settings persistence are exercised with replaced
capture/output/provider ports. CPU inference uses actual licensed PCM and the
pinned model. No physical microphone, audio output, real foreground service,
phone alarm, sensor, DND change or overnight recording ran. These are not visual
approval. Raw originals/logs/JSON were copied byte for byte; SHA256SUMS covers them.
