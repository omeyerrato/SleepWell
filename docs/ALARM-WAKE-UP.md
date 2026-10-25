# Alarm volume and light outputs — work in progress

The current candidate connects Extra loud, gentle volume, sound delay, screen
sunrise and phone flashlight settings to native alarm output. Defaults preserve
existing saved choices; new loud/light flags default off. Existing alarm rows
are not rewritten by opening settings. Portable alarm JSON includes the new
fields; legacy imports retain defaults. Native editor recreation/save/reopen/cancel and bounded visual review pass
at 69298aa (CI 39967552077, five silent flows, 39 screenshots). This is not
a completed-device-effects claim.

Extra loud selects maximum device alarm volume while preserving the separate
normal percentage. Source6578575 additionally requests a LoudnessEnhancer target
of600mB(+6dB), attached only to the prepared alarm player's positive owned audio
session immediately before start. Quiet developer policy excludes this effect.
Missing effect/control/enable support records an error and retains maximum-stream
playback. Each player releases its effect on source change, failure, snooze or
dismissal. This is optional signal processing, not a speaker-limit bypass; actual
hardware output remains unverified. The
quadratic player-gain curve is shared with the planned-volume chart, with
100 ms updates. The global alarm stream is claimed once, rather than rewritten
every second. A durable occurrence/owner/baseline journal precedes stream
changes; a matching owned value is restored after playback becomes quiet.
Subsequent user changes are preserved. Equal numeric values cannot identify
who last changed a stream; ownership is necessarily best effort.

Sound delay and ramp use the occurrence's monotonic start. A late decoder joins
the curve in progress; it does not restart the ramp. Provider/decoder calls run
off main. Preparation and activation have a bounded four-second fallback wait;
cancelled callbacks cannot restart output. Primary playback is retained until
backup playback acknowledges native start. A provider blocked inside Android
cannot be forcibly killed; its bounded worker attempt is cancelled and cleans
up if the call returns. No repeated unbounded retry loop is used.

Quiet developer tests ignore loud/light/delay/backup settings, never raise the
global stream and retain a digital gain cap of at most 25%, including after
volume-key changes. This is a signal-amplitude limit, not a calibrated loudness
measurement. A muted system alarm stream can therefore make the test silent.
Backup PendingIntent identity includes the occurrence; stale IDs/runs do not
escalate a newer or quiet alarm. Stale challenge actions cannot dismiss a new run.

Screen sunrise changes only the alarm Window brightness, from 2% to full over
the sound delay, otherwise ramp duration, otherwise two minutes. Warm alarm
art is already on screen. It neither changes global brightness nor controls
smart lights. A separate lifecycle ticker survives challenge rebuilds; pause
restores the Window value and resume continues occurrence time.

Flashlight uses CameraManager.setTorchMode without opening a camera. It waits
for initial state, leaves a preexisting light alone, does not fight external
OFF/unavailability and releases its pending/owned request on stop. Torch
ownership is nonexclusive; another app's indistinguishable ON while ours is on
cannot be identified. Missing/busy/denied torch output records an error while
sound and the challenge continue. There is no strobe or camera recording.

Sources: [Android MediaPlayer](https://developer.android.com/reference/android/media/MediaPlayer),
[torch lifecycle](https://developer.android.com/reference/android/hardware/camera2/CameraManager#setTorchMode(java.lang.String,%20boolean)),
[Sleep as Android alarm settings](https://sleep.urbandroid.org/docs/alarms/alarm_settings.html).
SleepWell's ramp-after-delay behavior is deliberate and differs from the
documented vendor behavior that can advance its volume ramp during silent delay.

Six policy, fifteen playback, ten volume-lease and nine light-controller pure
tests currently pass alongside the prior 126 tests (166 total). They use fake
clocks/ports; they do not prove native output delivery. Real alarm/audio,
sunrise/torch, vibration, screen-off and power-menu tests remain prohibited on
the personal phone. The native configuration-only test uses isolated data and
a scheduling recorder; it never creates AlarmActivity or an alarm Service.
The strict reviewer accepted the bounded configuration controls and curves
after inspecting all 39 originals. Full Alarm sound board 03 still fails
reference fidelity; vibration timing and low-volume preview are missing from
the Gentle reference flow. Offscreen helper copy and large-text wake settings
remain visually unverified. Full feature/36-screen parity remains incomplete.
