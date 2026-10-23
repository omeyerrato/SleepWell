# Delayed alarm vibration

`vibrationDelaySeconds` is a per-alarm JSON field. Older alarms default to zero
(From start). The runtime bounds it to 0–1440 seconds without rewriting stored
data. Turning vibration off keeps its timing preference for later use.

The Gentle wake-up screen groups a From start / After … timing pill beneath the
Vibration switch. The timing control is disabled while vibration is off. It uses
the existing rounded Inter choice dialog; choices update the alarm draft, while
Save alarm persists it. Back discards unsaved changes.

`AlarmVibration` receives elapsed time from the original monotonic occurrence
anchor, not from service construction. It allocates its Android output only at
the deadline and makes at most one start attempt per output owner. Repeated
100 ms ticks or backup requests cannot start it twice. A restarted service gets
a new output owner using the same original deadline; if that deadline has
passed, vibration resumes without waiting through the delay again.

Quiet alarms (`id <= 0`) and vibration opt-out create no vibration controller,
perform no vibrator lookup and allocate no waveform. Backup bypasses the delay
only for opted-in real alarms. Output uses Android alarm usage. Stop, occurrence
replacement, liveness loss and owner destruction cancel the owned output.
Unavailable hardware, denied access and runtime failures get one bounded
attempt and a local error, with no retry loop.

`AlarmVibrationTest` exercises timing boundaries, restart timing, duplicate
ticks, backup, liveness races, hardware/release failures and quiet policy with
fake ports. `AlarmWakeSettingsTest` uses an isolated disabled alarm and fake
scheduler to check legacy JSON, delay selection/cancellation, disabled timing,
recreation, Save/reopen and unsaved Back. It starts no alarm, service, vibrator,
audio, microphone, camera or brightness output. It adds
`wake-settings-vibration-options.png` to the existing configuration captures.

Device vibration, timing under Android process eviction, DND/hardware behavior
and the visual comparison of fresh native captures remain unverified until
separately authorized evidence exists. Source and fake-output tests do not
certify physical wake-up behavior.
