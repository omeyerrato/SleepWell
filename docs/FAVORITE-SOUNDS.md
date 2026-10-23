# Global favorite sounds

Sound events → options → Favorite sounds opens an album across ended nights. It uses the existing sound_events.favorite and clips.favorite columns; no schema or data migration is added. A starred estimate and a starred whole recording are separate entries even when they share the same WAV, because the stars and intervals are independent. Recordings receive no invented snoring or speech category. Open night returns to the original night's sound-events screen, where estimate-versus-recording deletion remains explicit. The album itself only removes stars and never deletes a file, event, session or recording.

All/Snoring/Speech show real counts for the current date range; Other sounds is in options. All dates, Past7days and Past30days use actual capture timestamps, not the time a star was added. Estimate starts and recording ends supply their existing database capture timestamps. The current schema has no favorite-created timestamp. Counts and page metadata use one short database snapshot; actual WAV decoding happens after that transaction ends and never holds a deletion lock. Fifty-row Older/Newer pages can access the full matching album; no unbounded collection of decoded audio is retained. Date/category/page/position survive Activity recreation. Fresh album opens start at All dates/All, while the stars themselves persist in SQLite.

Rows reuse SoundEventRows waveform/target preparation, ClipWaveView, ClipSeekView and the existing ClipReviewPlayer/ClipPlayback adapter. WAV peaks and event offsets come from actual private PCM files. Playback is explicit, foreground-only and bounded to the selected interval; switching rows disposes the previous session and recreation does not resume sound. Alarm/tracking/sleep-sound occupancy and audio-focus policies remain in the existing native playback adapter. No global volume or retention behavior is changed.

Edited sleep bounds are respected. A star outside the edited period remains visible with Outside edited sleep period and no Play/waveform, rather than silently disappearing or playing hidden portions. Analysis-only and shared-deleted-audio estimates remain metadata with no fake waveform or Play. A missing/invalid WAV is Recording unavailable. An album database failure has a generic error and explicit retry. Changing or deleting originals while the album is open can make a snapshot stale; returning to/reopening the album reloads it, and existing playback/identity checks still reject stale selections. Removing a stale star uses existing identity-checked SoundClips mutations, not a blind row update.

The new Activity is private, uses explicit internal navigation and registers the noisy-audio receiver through ContextCompat's non-exported contract. There are no new permissions, dependencies, network calls, external services or broad storage grants. Reader/playback factories are pinned per Activity. Destroyed owners fence UI callbacks and close their review port; test fixtures finish all owned Activities and drain workers before restoring factories or removing fixture files.

### Playback after another screen edits a night

Review identified a real stale-trim gap: clip identity and WAV duration do not
change when the record's visible period is trimmed. The shared native review
adapter now calls ClipReviewAccess before opening a player and again before
Play/Resume. One database snapshot checks original clip/event identity, queued
deletion, the selected absolute interval against the current saved record bounds,
and the event's original interval. A changed period fails visibly and requires
reopening; original WAV bytes remain intact. These are point-in-time checks,
not a claim of atomic synchronization with a later concurrent edit while audio
is already playing. No database transaction is held during native playback.

ClipReviewAccessTest exercises unchanged clip/event identities across real
SQLite start/end trims, refreshed selections, restore-original and exact WAV
hash preservation using private fixtures. It allocates no player or hardware.
Compilation/native execution of this follow-up remains pending.

## Verification status

FavoriteSoundsStorageTest has3 real named-database/WAV methods: global independent stars and actual date/category counts with precise playback boundaries; shared deletion/missing PCM/edited-window metadata preservation; and51-item paging, active-night exclusion, unstar persistence and page clamping without data deletion. FavoriteSoundsFlowTest has1 disposable CI-emulator method. It injects fake playback only and exercises the real sound-events menu entry, whole-clip versus event playback offsets, accessible seek, recreation without autoplay, date/category filtering, unstar/reopen, shared deletion, missing WAV, empty album and database failure/retry. Both classes require ciReview/qemu and idle alarm/tracking guards before fixture work. They do not start MediaPlayer, AudioRecord, vibration, flashlight or alarms.

The UI flow requests14actual Android captures: favorites-all, favorites-playback, favorites-recreated, favorites-week, favorites-filter-empty, favorites-speech, favorites-playback-error, favorites-unstarred, favorites-reopened, favorites-deleted-audio, favorites-missing-audio, favorites-empty, favorites-error and favorites-retry. Compare the list/chips/row controls/real waveform with board07. Debug/native APK builds, all358 pure tests with zero failures and compatible lint9.4.1 with zero errors/79warnings passed. No lint issue is located in the new Favorite classes. The4 new native methods and14screenshots still require CI execution and visual inspection. Passing fake-output checks will not verify actual hardware audio, audio-focus behavior or device route reliability. The existing sound archive still exports complete raw clips separately; this album is not a portable favorite/audio backup.
