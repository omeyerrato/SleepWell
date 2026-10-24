# Tone and Wake control gate correction

[Focused CI 40058849301](https://github.com/omeyerrato/SleepWell/actions/runs/40058849301) failed both native flows. The Tone XML shows a 51px nonclickable import label inside its complete 126px clickable row; the helper measured the label and incorrectly demanded scrolling. The Wake XML shows the Flashlight control below the viewport, while its helper required accessibility existence before scrolling. The loud-curve original depicts Alarm sound, so it cannot count as chart evidence.

The corrected helpers resolve the actual clickable Tone ancestor and verify complete native and accessibility bounds of at least 48dp. Wake searches/scrolls absent or clipped controls first and still fails missing or incomplete targets. Curve captures require the actual focused Gentle wake-up page, a native AlarmRampChart, exact draft target/ramp/delay metadata and full chart width/height before and after frame capture. Existing selection, cancel, recreation and SQLite persistence checks remain intact; no sleeps or production changes were added.

AlarmToneFlowTest and AlarmWakeSettingsTest retain one method each and the existing sixteen Tone plus ten Wake PNG names. Compilation does not establish that these flows or screenshots pass. Fresh disposable-emulator execution and review of each original remain required; there were no physical-device actions or native audio, microphone, alarm, brightness, vibration or flashlight effects during this correction.


Fresh focused CI40062368995 at4bd5317: WakeSettings1 and all10 frames now pass;
WakeCheck2 and13 frames, Assets1 and Grant1 also pass. Tone fails at the first
selector read with StaleObjectException during the asynchronous asset rerender,
before any of16 named frames. This does not establish a broken user click or a
successful tone flow. The next helper waits for all three measured waveforms
before first lookup and retries only bounded selector/geometry reads (four
attempts) on StaleObjectException. The caller's click/action is never retried,
and complete48dp bounds, persistence and cancellation requirements remain.
Fresh native execution is still required.
