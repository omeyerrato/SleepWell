# Saved contactless signals

`RecordSignalsActivity` reads the saved record, intervals, sound events and clips on its own worker through a pinned storage factory. Opening the view does not start tracking, audio analysis or any output. The supporting view uses the supplied board 01 report's cream background, Inter typography, violet measured bars and shared spacing; it is not a new dashboard.

For `sonar_fmcw_v1` records the chart is labelled **Relative echo change**, with saved intervals of up to five seconds. Values have no acceleration, distance, breathing or medical sleep-stage units. Null intervals remain blank. A saved zero remains a real selectable reading. The quality summary totals the actual visible duration of each persisted quality state. Time with no saved interval is labelled **Not recorded**, including any tail after the final checkpoint. Edited boundaries clip the summary and graph; original capture selection survives recreation and never rewrites evidence.

Noise and light charts appear only when their visible intervals actually contain finite saved measurements. Existing additional measurements, sound analysis and saved clips remain accessible when present; a contactless night with none does not acquire empty plots or a playback action. Existing local direct MediaPlayer playback is removed. Available saved recordings route to SoundEventsActivity, which uses the shared ClipReviewAccess/ClipPlayback trim and identity guards. Merely opening Signals cannot play audio; this verification flow never exercises playback.

## Native verification contract

`SonarReportFlowTest.savedSonarQualityZeroAndMissingIntervalsSurviveRealReportNavigation`: **one method, six original Android screenshots**:

- `sonar-report-night.png`: actual saved-night report and its real Report options action.
- `sonar-report-signals.png`: actual navigation through Recorded signals & notes, complete chart and quality-row bounds.
- `sonar-report-zero.png`: the real chart accessibility action selects the stored zero; calibration and no-echo intervals cannot become selectable bars.
- `sonar-report-reopened.png`: reopening the same record restores its data and sensor-specific semantics.
- `sonar-report-legacy-unknown.png`: historical empty analysis metadata is not labelled off or completed.
- `sonar-report-analysis-error.png`: a saved model error is shown separately from a completed zero-event analysis.

The disposable emulator fixture uses the actual private `Store.begin(sensor, config)`, `sonarCheckpoint` and `end` APIs. Its clearly labelled synthetic intervals are calibration/null, valid/zero and no-echo/null, followed by a missing tail. A pre-existing isolated row and a typed fingerprint of every application table must remain identical. Raw timestamps, quality, config, clock-change flag and global preferences are checked after the native journey. The fixture does not run `TrackingService`, `AlarmActivity`, microphone capture, playback, torch, sensors or scheduling.

Source compilation, pure tests and native configuration execution are separate evidence. At `2477cbe`/CI40091982679 the original four frames pass native execution and individual strict visual review against boards01/08. At `892ea74` two status fixtures expand the contract to six frames; fresh execution is pending. Requested/completed-zero, explicit off, unknown legacy and errors read immutable schema9 metadata, never current user preferences. No model is run by this report fixture. A passing fixture cannot establish actual sonar hardware accuracy, sleep-stage detection or physical overnight reliability.

## Local checks

- Scoped build 33783 passed debug and native test APK assembly, 38 record/chart pure tests (four suites, zero failures/errors), and lint.
- After removing the inherited direct playback shortcut, final incremental build 1908 passed debug/native APK assembly and lint in 2m22s. Final lint: zero errors, 91 warnings, none in the two owned Java files. Both builds used one Gradle worker and the repository-compatible lint 9.4.1 override.
- Strict UTF-8 decoding and `git diff --check` passed. No physical device or native test execution was performed during implementation. The four actual Android frames and visual comparison remain pending.
