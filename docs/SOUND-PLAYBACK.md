# Sleep sounds — screen 19

Status: **partial**. The native configuration flow and a short real MediaPlayer/
foreground-service integration journey pass on the disposable API36 CI emulator,
with bounded board07 visual comparison. Listening quality, import and long phone
background behavior remain unverified. No phone audio or alarm was run. The phone
remains disconnected; the user's prohibition on alarm testing remains in force.

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
A process-retained validation job reads a disposable private snapshot off the UI thread, bounded by actual bytes (100 MiB) even when a provider omits or misreports its size. Metadata parsing uses the local snapshot. A provider
must support a persistable grant. Only successful validation changes the mix;
failed or cancelled imports preserve the previous profile. The visible screen exposes live bytes and a 48dp Cancel beside Play. Its 20-second deadline is polled while resumed; an uncooperative provider can retain a bounded worker/snapshot until it returns or the process dies. The next import removes abandoned private snapshots. Cancellation and late results never write preferences. A profile revision prevents importing over a newer mix edit. Rotation retains the job ID; process death asks for reselection and preserves the previous mix.
Provider deletion, revoked access and unsupported decoding remain real playback
errors. Source documents are never deleted. Old read grants are retained because
the same document may be an existing alarm tone; grant cleanup with shared-reference
ownership is future work. No HTTP, radio, Spotify or account integration is included.

## Evidence and open gates

`SoundPlaybackTest`: 24 local silent fake-clock/fake-sink cases cover preparation,
gain changes, normalization, fading/ducking, pause, completion, timeout, failed and
stale decoders, replacement, legacy settings, offsets and tracking ownership.
These tests do **not** instantiate Android MediaPlayer, audio focus or an FGS.

`SoundSettingsFlowTest`: **1 pass in 18.746 s** at `ce7f64c`. Rain → Ocean,
45-minute timer, 10-minute fade, recreation and reopen preserve the exact choices.
Both body and cleanup compare personal preference maps exactly. CI-only setup
asserts idle values and adds only absent inactive defaults before the baseline;
late BOOT_COMPLETED then writes the same values. Existing values are never
replaced and the physical runner does not enable this initialization. No Play,
import or tracking action occurs in this configuration test.

`RootVisualTest`: **1 pass in 23.113 s**, four tabs/detail/system Back.
`NativeSoundFlowTest`: **1 pass in 26.323 s** on the same commit. Explicit Play
starts actual Android music activity; time advances after Home. Notification
PendingIntent Pause/Play/Stop, frozen pause timing, live Ocean replacement,
competing focus pause without auto-resume, timed completion, unreadable-provider
failure and explicit Rain recovery pass. Stop/completion/failure remove native
media activity, foreground service and notification. The test compares stream
volume and personal preferences unchanged. It does not touch the notification
shade. It requires explicit CI opt-in, qemu and the fixed disposable emulator
launched with host audio disabled; it is absent from the phone allowlist.

The timer check starts with a valid 57-second offset and exercises the final
three seconds, not a full-minute fade. This is not a listening or loop-seam test.
All 17 final images pass the independent bounded comparison; exact acceptance
is recorded in DESIGN-REVIEW.md. The earlier Wind-down bar and Privacy wrong-page
rejections were corrected in source/capture synchronization before this final run.
The new imported-audio/Brown hero is a neutral record-player still life. Actual import-state captures await fresh native CI review.

[Persistent evidence](verification/2026-10-23-api36/README.md) pins code,
environment, all 17 original PNGs, hashes and the three result files. These are
emulator fixtures, not S23 or overnight data. Physical `--flow sounds-settings`
remains unrun. The changed movement-only test has not been rerun; its prior pass
does not certify native tracking/audio ownership wiring.

New picker/import verification is in progress; no native import pass is claimed until the dated evidence is recorded. Seven AudioCopyTest cases pass; source/security scope and exact diffs are in AUDIO-IMPORT-SECURITY.md.

Open: S23 playback, file picker/persisted grants, long/background/screen-off and
process-death behavior, service delivery races, route unplug/ducking, full native
timer/fade, actual tracking ownership and listening/loop quality. Gain/fade
arithmetic has fake-clock evidence; this cannot establish speaker output quality.
Playlists, meditation, stereo binaural output, automatic sleep-detection fading,
broader sound packs and external music services remain missing. No artificial
waveforms, sleep stage graphs or classified sound events were added.

Design references: supplied board 07 / screen 19. Behavioral references:
[Sleep as Android lullabies](https://sleep.urbandroid.org/docs/sleep/lullaby.html),
[Android MediaPlayer states/resources](https://developer.android.com/media/platform/mediaplayer/state-resources),
[audio focus](https://developer.android.com/media/optimize/audio-focus) and
[media playback foreground services](https://developer.android.com/develop/background-work/services/fgs/service-types#media).
