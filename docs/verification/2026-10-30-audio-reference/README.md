# Sleep sounds and sound events: bounded native evidence

Every one of the **23 original PNGs** below was individually opened as a complete native window and compared with [board 07](../../../design-concepts/2026-10-22-phone-app/07-sleep-audio.png). These are actual emulator windows, not rendered mockups or a contact-sheet-only comparison. No new test, CI run, build, production edit or device/output action was performed while preparing this archive.

| Run | Exact captured source | Selected native method | Result |
| --- | --- | --- | --- |
| [41035861764](https://github.com/omeyerrato/SleepWell/actions/runs/41035861764) | `d68d0e8343d802162680dbff41b35b21c706133c` | [SoundSettingsFlowTest#silentChoicesRecreateAndReopenWithoutPlayback](mixer-41035861764/SoundSettingsFlowTest%23silentChoicesRecreateAndReopenWithoutPlayback.txt) | PASS, 37.375 s, 14 required originals. |
| [41034030180](https://github.com/omeyerrato/SleepWell/actions/runs/41034030180) | `14fb6ff11045dba8562285abd4ef8eb67167ee99` | [SoundLibraryUiTest#recordedIntervalsFavoritesAndDeletionPersistWithFakePlaybackOnly](events-41034030180/SoundLibraryUiTest%23recordedIntervalsFavoritesAndDeletionPersistWithFakePlaybackOnly.txt) | PASS, 20.384 s, nine required originals. |

Both original focused summaries explicitly record `full_app_review: false`. Environments are API 36 x86_64 emulators at 1080×2316 / 420 dpi, host audio disabled and no native playback allowlisted. Sources remain separate; neither run is relabelled as a capture of the later frozen 0.1.23 package.

## Reference comparison

The mixer preserves board 07's dark plum/lilac composition: Rain bedroom artwork, source title, primary circular player control and progress, separate primary/brown-noise sliders, Timer/Fade rows and four artwork tiles with a visible selected outline. The rain illustration, Ocean scene and all four source thumbnails are real current raster assets with the reference's subjects; they are not claimed to be identical reference pixels. All main controls fit in the captured window. Its stopped 0:00/Play state and disabled Stop are the actual idle state, rather than a fabricated playing progress.

Sound Events preserves the warm cream/brown filters, aligned time/name/heart/trash rows, category color rails and waveform/playback hierarchy. The active row gains its warm panel, Pause and seek/time controls. The fixture's short synthetic WAVs produce their own measured waveform shapes and one-second display values, rather than the reference's illustrative 10–18 second recordings. `Speech estimate` deliberately describes the stored estimate rather than promising confirmed talking. Missing recorded audio is shown as unavailable, without a fabricated waveform or Play action. No material clipping issue requiring a product edit was found in these 23 originals.

Supporting Timer/Fade use custom checked rows and full Use/Cancel actions. Sound settings groups Sources & playlists, Playback & tracking and Guided wind-down. Sources/playback menus retain named routes. Sound-event Options is a scrollable supporting sheet; its longer contents extend below the visible excerpt. Analysis details has a complete Got it action, warm surface and readable status/explanation before and after recreation.

## Actual actions and persistence

**Mixer configuration:** the real Ocean tile updates UUID-private saved mix preferences. Timer/Fade drafts preserve their selection, keyboard focus and actual scroll through recreation; Cancel/Back leave saved values unchanged. Explicit Use saves 45-minute Timer and 10-minute Fade, which reopen and survive Activity relaunch. Existing non-preset 37/7-minute values appear as real selectable choices; cancelling Off retains the seven-minute Fade. A newer concurrent saved timer is not overwritten by a stale draft. Sources/playback menus open and return without changing the mix. Main controls are checked for full bounds. **Play is never pressed, the playback service snapshot stays null, and this proves configuration persistence rather than audible playback, elapsed playback, timer expiry or fading output.** The fixture restores the sound-preference factory and preserves captured personal preferences; this is not physical-device behavior.

**Recorded event library:** two real UUID-named synthetic WAV files and a UUID-private SQLite night feed the actual event UI. A fake playback port handles explicit Play/Pause/Seek (900 ms), verifies correct event clip offsets/durations and never creates MediaPlayer or AudioRecord. Favorite state is actually saved and survives recreation; restored pages/options do not replay audio. Speech filtering selects the actual event. Explicit deletion removes only the fixture's first recording while retaining event rows and its favorite; the three events that then lack saved audio lose their Play controls. The remaining recording still displays its real waveform. Analysis details/options survive recreation and remain dismissed after explicit Got it. Readers are awaited before restoring factories and removing owned files/database; browsing preferences use their own private namespace.

These two journeys do not measure recognition accuracy, real snoring/speech classification, recorded-night capture, real audio output, notification/background playback, cue delivery, retention expiry, external providers or hardware. The Sound Events banner explicitly labels synthetic WAVs/fake playback; it is fixture copy, not normal production page copy. Snoring cues (board 07 right) are not included in these two selected methods.

**Inline Report gap remains open:** these Event-library waveforms and fake Play actions are in SoundEventsActivity. They do not provide a populated inline RecordReport audio-row capture or an imported provider-stage strip inside Report. Existing Report fixture captures without saved clips/provider stages are not upgraded to populated-state evidence by this archive. No all-36-reference acceptance is claimed.

## Individual original inventory

### Mixer and supporting choices (14)

- [sounds-settings-rain](mixer-41035861764/sounds-settings-rain.png): Rain art, stopped player, current sliders, Timer/Fade and selected Rain tile.
- [sounds-settings-ocean](mixer-41035861764/sounds-settings-ocean.png): Actual Ocean selection with saved 45-minute Timer and 10-minute Fade.
- [sounds-settings-recreated](mixer-41035861764/sounds-settings-recreated.png): Saved Ocean and durations after recreation.
- [sounds-settings-reopened](mixer-41035861764/sounds-settings-reopened.png): Same saved choices after closing and relaunching the Activity.
- [sounds-settings-timer-draft](mixer-41035861764/sounds-settings-timer-draft.png): 240-minute draft with checked row and full Use/Cancel.
- [sounds-settings-timer-recreated](mixer-41035861764/sounds-settings-timer-recreated.png): Retained selection/focus/scroll after recreation.
- [sounds-settings-fade-draft](mixer-41035861764/sounds-settings-fade-draft.png): Ten-minute Fade draft with full actions.
- [sounds-settings-fade-recreated](mixer-41035861764/sounds-settings-fade-recreated.png): Same draft after recreation, before cancelling.
- [sounds-settings-custom-timer](mixer-41035861764/sounds-settings-custom-timer.png): Actual non-preset 37-minute saved choice.
- [sounds-settings-custom-fade](mixer-41035861764/sounds-settings-custom-fade.png): Actual non-preset seven-minute saved choice.
- [sounds-settings-menu](mixer-41035861764/sounds-settings-menu.png): Three named settings groups and Back action.
- [sounds-settings-menu-recreated](mixer-41035861764/sounds-settings-menu-recreated.png): Same settings destination restored.
- [sounds-settings-sources](mixer-41035861764/sounds-settings-sources.png): Actual source/playlist routes and selected Ocean summary.
- [sounds-settings-playback](mixer-41035861764/sounds-settings-playback.png): Existing playback/tracking choices and explicit immediate-save description.

### Recorded event library and supporting states (9)

- [library-recordings](events-41034030180/library-recordings.png): All four actual private events; three available waveform/Play rows and one audio-not-recorded state.
- [library-playback](events-41034030180/library-playback.png): Active snoring row with Pause and seek controls through fake playback.
- [library-favorite-recreated](events-41034030180/library-favorite-recreated.png): Persisted filled heart and retained seek position; restored page is paused.
- [library-speech](events-41034030180/library-speech.png): Speech filter selected and its waveform active through fake playback.
- [library-recording-deleted](events-41034030180/library-recording-deleted.png): Deleted fixture audio is unavailable; event rows/favorite and remaining WAV persist.
- [library-options](events-41034030180/library-options.png): Actual scrollable Sound event options sheet.
- [library-options-recreated](events-41034030180/library-options-recreated.png): Same selected All events and supporting routes restored.
- [library-analysis-details](events-41034030180/library-analysis-details.png): Unavailable historical analysis status plus full Got it action.
- [library-analysis-details-recreated](events-41034030180/library-analysis-details-recreated.png): Same owned explanation restored without playback.

## Original bytes

Each run folder contains its unchanged original PNGs, native method transcript, environment and focused summary. The local attributes preserve copied text bytes. Original sizes and SHA-256 are pinned separately for [mixer](mixer-41035861764/original-sha256.json) and [events](events-41034030180/original-sha256.json). No screenshots were painted, transformed or replaced. Earlier failed runs, Settings evidence and the separate first-frame SystemUI limitation remain outside this archive and are not relabelled as passing.
