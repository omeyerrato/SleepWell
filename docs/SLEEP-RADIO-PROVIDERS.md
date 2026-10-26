# Named radio source for the sleep mixer

The original sleep mixer now offers **Internet radio** beside bundled loops,
ambient rotation, local audio/playlists and stereo tones. This implements the
custom named stream path described in the [lullaby manual](https://sleep.urbandroid.org/docs/sleep/lullaby.html).
It is not Spotify/account-service integration.

## Working source flow

Sleep sounds → Internet radio → Add radio station → enter its name and direct
public HTTPS URL → Keep station in draft → Select station → Use station → return
and tap Play. Opening, editing, validation, selecting and saving never initiate a
connection; a previously playing mix can continue while its editor is open until
it is paused or saved.
Only an explicit Use or Save stations only retains the library; Cancel keeps the
saved library/mix unchanged. The station/form draft and original mix revision
survive recreation. A changed mix rejects stale Save.

The bounded library stores at most twelve unique station IDs and URLs as optional
`mixRadio` version1 JSON in the existing atomic mix update. Older preferences
default to an empty library. Names allow 1–80 plain characters. URL validation
reuses the existing alarm-radio parser: public HTTPS, at most2,048 characters,
no credentials, query tokens, fragments or web-player pages. Known playlist and
adaptive-manifest suffixes are rejected; direct supported audio is required.
A syntactically valid saved URL is not proof that the station will play.

Selection preserves existing local document references, playlists, bundled
rotation, gains and overall timer/fade. Radio uses manual Play only; choosing it
turns tracking autoplay off. Save pauses the current mix and connects to nothing.
Removing the active selection explicitly discloses that Save stations will select
stopped Rain; no fallback audio begins. No directory, station discovery, account,
token, credential, subscription bypass or built-in station claims are added.

## Runtime ownership and errors

Explicit mixer/notification/media-session Play uses a real owned async MediaPlayer
and existing foreground media service, audio focus, noisy-route, alarm preemption,
gain normalization, overall timer/fade and optional brown-noise bed. Radio cannot
claim tracking autoplay or tracking-owned stillness admission. The player freezes
its source; selection first pauses and closes the old player. Pause closes the
stream and freezes the mix timer. Explicit Resume reconnects; it does not promise
a retained position in live audio.

Initial preparation and reported buffering have an eight-second fenced watchdog.
An unavailable/offline/unsupported/ended stream stops all layers with an error.
There is no automatic reconnect, silent skip or bundled fallback. After initial
Play, a reported buffering stall still counts toward the session timer until
failure; the timer does not measure audible samples. Stop, focus/noisy changes,
selection, alarm preemption and destruction invalidate queued starts and retain
existing cleanup. Blocked native I/O may take longer to unwind, but a closed
layer cannot intentionally start later.

The platform's normal HTTPS/certificate checks remain. The same documented
`android-allow-cross-domain-redirect=0` MediaPlayer header used by alarm radio is
supplied. The [Android reference](https://developer.android.com/reference/android/media/MediaPlayer)
documents this control; [MediaHTTPConnection](https://android.googlesource.com/platform/frameworks/base/+/master/media/java/android/media/MediaHTTPConnection.java)
also uses it for protocol redirect permission. No trust override, custom DNS,
HTTP downgrade or authentication workaround is introduced. OEM codec/stream
behavior still needs actual compatibility checks.

## Root integration and remaining work

No manifest/dependency/schema changes are needed. SleepSoundsActivity owns its
in-screen editor, and existing saved local playlists/rotation remain intact.

Root owns the portable-settings hooks: reject Internet radio as a Bundled sounds
profile source; include `mixRadio` in device-local rollback staging; preserve the
existing station library when applying a bundled-sound profile. Endpoints remain
excluded from portable settings. Record metadata/audio backup formats are unchanged.

This increment is source-only pending root's combined build. No new unit/native
batch, stream/provider request, phone/emulator, microphone, audio or output action
was executed. Real station decoding, buffering, offline errors, pause/reconnect,
focus/route cleanup and timer/fade output remain acceptance work. Spotify library
selection, proprietary provider playback and broader sound-catalog licensing remain
separate missing prerequisites/features.

Root integration731e9725: production build PASS1m5s; original signer and bundled model hashes verified. New Android UI/output/accuracy acceptance is still open. No new unit/lint/phone campaign.
