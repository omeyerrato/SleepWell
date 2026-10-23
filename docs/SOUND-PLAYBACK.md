# Sleep sounds — screen 19

Status: **partial / native verification pending**. The source now provides a standalone
local player and two-layer mixer; this is not certification of playback quality,
Android background behavior or visual fidelity. No audio was played during this work.
The phone is disconnected. The user's prohibition on alarm testing remains in force.

## Behavior in source

- Opening the native dark detail page, reading preferences, choosing library art and
  importing audio do not start playback. Play is explicit, or a new user-started
  recording may start the mix if its saved tracking option is enabled.
- Rain, Ocean, Fireplace and Wind are primary choices; Brown noise is the second
  layer. A migrated Brown-only choice remains supported. The new three ambience
  WAVs are original procedural textures, not field recordings; the legacy Rain,
  Brown and alarm assets are unchanged. `scripts/generate_sleep_audio.py` recreates
  only the three new WAVs. Artwork provenance and exact prompts are in SOUND-ART.md.
- Source levels are real MediaPlayer gains. Sum above 100% is normalized; timer
  fade and focus ducking multiply every layer. System stream volume is never set.
- Timer uses monotonic **playing time**: loading and pause do not consume it.
  Fade occurs over the final configured minutes. Completion releases players,
  focus, wake lock and foreground notification. A non-playing finished snapshot
  remains in memory; no implicit replay after completion, process death or boot.
  An explicit new Play starts a new timer. Library changes during playback preserve
  elapsed time; a shortened timer can end the current mix immediately.
- Foreground notification and MediaSession share Play/Pause/Stop. Focus denial is
  an error; focus loss and route unplug pause without automatic resume. New focus
  requests invalidate old callbacks and reset ducking. The silent media channel
  has no vibration. Foreground starts precede focus requests for target API 36.
- Provider and decoder operations use a dedicated HandlerThread, with asynchronous
  prepare, generation guards and invalidation of queued starts. A 30-second loading
  timeout releases app playback state, wake/focus/notification. An unresponsive
  third-party provider can still delay its own binder/decoder IO; Android does not
  offer cancellation of every MediaPlayer data-source call. Such a worker may
  finish later; it cannot start the cancelled mix. This case needs native testing.
- Tracking attempts automatic audio once per service/session. It cannot steal an
  existing manual mix, restart music after completion, or stop unrelated playback.
  A queued tracking command validates the actual active session before playing.
  Only tracking's own pause may resume its owned mix; explicit user pause suppresses
  that ownership. Tracking continues if playback fails. Alarm-start source wiring
  stops sleep sounds and Play is refused while an alarm is active; deliberately
  **not exercised** under the no-alarm-testing instruction.

## Persistence and documents

`SoundPreferences` stores the whole profile in one preference edit. Existing
Rain/Brown tracking choices keep their 20% gain, timer and zero fade. Existing Off
stays off for automatic tracking; opening the player does not rewrite legacy data.
No SQLite schema or recorded night is changed. Portable backup still excludes app
preferences, documents and their URI grants.

Audio import uses Android's document picker and read-only `content://` access.
Metadata validation and known file-size checks run off the UI thread. A provider
must support a persistable grant. Only successful validation changes the mix;
failed or cancelled imports preserve the previous profile. The 100 MB size check
uses provider metadata when supplied, not a bounded copy of an unknown-size stream.
Provider deletion, revoked access and unsupported decoding remain real playback
errors. Source documents are never deleted. Old read grants are retained because
the same document may be an existing alarm tone; grant cleanup with shared-reference
ownership is future work. No HTTP, radio, Spotify or account integration is included.

## Evidence and open gates

`SoundPlaybackTest`: 24 local silent fake-clock/fake-sink cases cover preparation,
gain changes, normalization, fading/ducking, pause, completion, timeout, failed and
stale decoders, replacement, legacy settings, offsets and tracking ownership.
These tests do **not** instantiate Android MediaPlayer, audio focus or an FGS.

`SoundSettingsFlowTest` and `--flow sounds-settings` are compiled but **not run**.
They are configuration-only: isolated preferences, selection/timer/fade, recreation,
reopen and four actual screenshots when hardware is available. They never tap Play,
start a sound service or import a file. The existing movement-only test explicitly
disables and restores the new automatic sound flag to remain silent.

Open: actual device visual comparison with board 07, file picker/failed grant
behavior, service delivery races, focus loss and route changes, notification
controls, lock-screen/background playback, real timer/fade and audible loop quality.
Playlists, meditation, stereo binaural output, automatic sleep-detection fading,
broader sound packs and external music services remain missing. No artificial
waveforms, sleep stage graphs or classified sound events were added.

Design references: supplied board 07 / screen 19. Behavioral references:
[Sleep as Android lullabies](https://sleep.urbandroid.org/docs/sleep/lullaby.html),
[Android MediaPlayer states/resources](https://developer.android.com/media/platform/mediaplayer/state-resources),
[audio focus](https://developer.android.com/media/optimize/audio-focus) and
[media playback foreground services](https://developer.android.com/develop/background-work/services/fgs/service-types#media).
