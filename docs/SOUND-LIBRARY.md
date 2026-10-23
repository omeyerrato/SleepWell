# Sound library: storage milestone

This is backend preparation for board 07's real recording controls. Inline playback, export and the replacement library UI are not implemented by this milestone. Existing estimates without saved PCM must remain honest analysis-only rows. No physical audio or microphone tests were run.

## Additive schema 6

Migration 5→6 adds clips.favorite and sound_events.favorite, both default 0, an index for linked favorite events, and pending_clip_deletes. Existing alarms, session values, sensor readings, sound estimates and WAV files are preserved. Event favorites are independent: starring one estimate does not star every estimate in the same 10-second WAV. Retention preserves a WAV if its clip is starred or any linked estimate is starred. Removing expired audio nulls links through the existing trigger; the estimate and its star can remain as metadata.

SoundClips.favoriteEvent and favorite use identity checks before writes. Event identity includes session, original start, duration, category and model. Audio identity includes clip ID, session, original end timestamp and the private filename. Stale rows do not authorize deleting a replacement file.

A deletion intent is stored before filesystem changes. Failed unlink operations retain metadata and a pending row for an explicit retry; recover can finish after a process interruption. Shared legacy filenames remain until their last database reference is removed. Record/history/restore deletion stages audio identities transactionally before dropping rows. Recovery checks remaining path references and never treats a reused row ID as the original recording. FileSafety rejects paths outside the private clips directory.

## Parent integration required

- Replace MainActivity privacy's file-first deletion loop with a background call to SoundClips.eraseHistory(context,store). It first commits the data/journal transition, then recovers file deletions. Its boolean result is false if files remain pending, which the UI must report. Do not call FileSafety.deleteClip before staging.
- Remove TrackingService's old hardcoded 7-day pruneClips call/method. It would ignore favorites and run filesystem work on the service's main thread. NoiseRecorder now calls SoundClips.prune on its owned worker once per session when clip saving is enabled; the old service call must be removed at integration so it cannot bypass favorite retention.
- SoundClips.prune(context,store) reads audioRetentionDays (default 7, 0 means keep) and keepAudioFavorites (default true). UI controls and explicit policy-save processing are still pending.
- RecordEditor can continue deleting the returned paths: Records.delete now returns only files with no remaining clip references and stages recovery first. The next library open can call recover to complete journal cleanup.

## Real intervals and waveform data

RecordingSlice.contained computes an event's offset inside actual WAV duration and rejects partial coverage or overflowing times. ClipWave reads bounded PCM16 mono/stereo WAVs only, validates chunks/format, and extracts actual 64-bin peaks for the selected interval. Missing, empty, truncated or unsupported recordings fail rather than receive synthetic bars. There is no output playback in these classes.

## Verification

Pure tests cover silence, signed/stereo peaks, malformed/truncated WAVs, optional chunks, exact interval peaks and incomplete coverage. SoundLibraryStorageTest uses three uniquely named database fixtures and owned silent WAV files: real schema 5 migration, independent favorites/shared-WAV retention, and durable recovery with blocked deletions/reused IDs. These native tests still require CI execution. Local debug and instrumentation APK builds, 235 pure tests, and compatible lint 9.4.1 completed successfully (zero errors, 72 warnings). Native migration/storage tests still need CI; no UI or hardware acceptance is implied.
