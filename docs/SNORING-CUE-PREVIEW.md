# Snoring cues and explicit preview

The board 07 controls use three icon cards for Sound, Vibration and Both. Brown selection, aligned icon-led groups and a fixed Save action follow the supplied reference. These are draft controls: selecting a value does not persist it, start analysis or allocate an output. Save retains the existing microphone prerequisite for enabled overnight cues. Back cancels the draft.

Preview is an explicit action, including when overnight cues are off. It requests one short cue at the lowest intensity in the selected mode. It does not start a microphone, save a synthetic sound event, change global media volume, save the draft or enable overnight cues. Tracking or a ringing alarm blocks preview.

The preview uses the existing cue policy and detection thresholds, with a separate process-local budget: at least two minutes between requests and at most 30 per app process. Closing or recreating the screen does not reset that budget. Every pulse checks its ephemeral permission token, ringing state and closed state. Losing window focus, pausing, destroying the Activity or changing a choice closes the previous output. No preview token or controller survives recreation.

Production overnight output retains the persisted `snoreCues` check before each pulse; the preview-only constructor supplies an ephemeral allow check. Sound follows the existing media-stream policy. Physical cue delivery, muted-device behavior and OEM vibration behavior are not certified by configuration tests.

`SnoreCuePolicyTest` verifies forced-low/single preview settings and shared cooldown/cap isolation from the night's budget. `SnoreCueSettingsNativeTest` replaces every preview output with a fake before launching the screen. It checks no allocation on load, explicit preview while overnight cues stay off, unchanged preferences, cancellation of the old output after actual Activity recreation, retained cooldown after reopen, and normal Save/Back behavior. It captures five actual Android configuration frames, including `snore-settings-preview.png`, only after first-frame Save and Preview geometry checks and a focused hardware-frame commit. Compiling this test is not a native execution or visual acceptance.

Fresh native screenshots and the test transcript are required before accepting this UI. No physical alarm, microphone, vibration or audio test is part of this change. The overall 36-screen feature and visual gate remains incomplete.
