# Sound events and Sleep goals: verified layout before the options-menu correction

These are unmodified Android screenshots from commit `f1f839ce5da8c40f095b435744607864c0b6d7f1`. They **do not verify the later Sleep plan options-menu layout**. That source change requires its own integrated run and native captures.

- [Event-library run 40505530587](https://github.com/omeyerrato/SleepWell/actions/runs/40505530587): success; `SoundLibraryUiTest#recordedIntervalsFavoritesAndDeletionPersistWithFakePlaybackOnly`, one method, nine required originals.
- [Goal-plan run 40505587098](https://github.com/omeyerrato/SleepWell/actions/runs/40505587098): success; `GoalFlowTest#draftSaveCommitmentAndEffectiveProgressPersistWithoutDelivery`, one method, ten required originals.

Both workflow run heads and both artifact `environment.json` commits match the full commit above. Both runs used the disposable API 36 emulator at 1080×2316, density 420, with host audio disabled, `native_sounds=false` and `audio_imports=false`. There were no physical-phone actions. Every original listed below was viewed individually against the applicable supplied board. The two scope folders retain unchanged environment/summary/test output, run metadata, and image SHA256 checksums. These are bounded journeys, not full-app acceptance.

## Event library: board07 middle

Individually inspected: `library-recordings.png`, `library-playback.png`, `library-favorite-recreated.png`, `library-speech.png`, `library-recording-deleted.png`, `library-options.png`, `library-options-recreated.png`, `library-analysis-details.png`, `library-analysis-details-recreated.png`.

The compact event name/duration block, favorite/delete actions and inset selected waveform follow the reference roles without observed label/action overlap. Real generated WAV/PCM intervals, seek offsets, favorite persistence/recreation, Speech filtering, shared recording deletion while retaining estimates, Options selection/restoration, Analysis details restoration and no replay on menu restoration passed. Analysis-only events remain explicitly without audio. The recreated Options and Analysis details frames retain their visible content and selection.

The actual analysis status, estimate wording, extended Categories control and scrollable supporting Options differ from the raster. The WAVs are synthetic fixture files with injected playback output; this does not validate real audio output, microphone classification or physical-device behavior. Correction navigation, every extended filter, every error state and large font scaling are outside this one method.

## Goal plan: board05 right

Individually inspected: `goal-plan.png`, `goal-tonight.png`, `goal-saved-plan.png`, `goal-empty-bank.png`, `goal-create.png`, `goal-bank.png`, `goal-reopened-bank.png`, `goal-bank-details.png`, `goal-stopped.png`, `goal-trends.png`.

The real bedtime graph now places the orange Target key beside its heading, leaves an unrecorded day blank and uses readable axis spacing and subtle day guides. Draft cancellation/recreation, the 15-minute duration increment, reminder/wind-down settings, Save/reopen, dated-goal creation/reopen/stop, recorded goal progress and history/trends navigation passed. The fixture records produce seven observed days and a retained −1 h 45 min balance; the scheduler is non-delivering and personal-data preservation is asserted.

An observed layout defect remains in these originals: the extra Smart Bedtime and Wind-down rows extend beyond board05's primary Goal bank/Reminder composition. Wind-down's supporting line is partly outside the initial scroll viewport above the fixed Save button. Existing actions can scroll to it, but this is not reference parity. The follow-up correction moves those existing controls into a real header options menu. These images must not be used to claim that correction has been verified. The new heading's large-text fallback was also not exercised by this method.
