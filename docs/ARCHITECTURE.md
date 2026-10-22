# Architecture

SleepWell is a native Android Java application with a small AndroidX Core dependency. Android framework views keep the initial build inspectable and avoid a web wrapper. Components are deliberately separated by responsibility.

| Area | Implementation | Contract |
| --- | --- | --- |
| Alarms | `Alarm`, `Scheduler`, `AlarmReceiver` | Per-alarm immutable explicit PendingIntents; wake-up time scheduled with AlarmManager independently of tracking. |
| Recovery | `BootReceiver` | Handles protected OS reboot/time/time-zone/package events, reschedules enabled alarms, marks interrupted sessions. Never starts the microphone at boot. |
| Ringing | `AlarmService`, `AlarmActivity` | Foreground media playback, alarm audio attributes, volume ramp, vibration, challenge UI, and optional wake-up check. |
| Night capture | `TrackingActivity`, `TrackingService`, `TrackingClock` | Draft setup and same-session live/pause/resume. Measured 1-second live previews, atomic minute epochs and active duration, persisted gaps. Monotonic total lifetime and CPU wake lock bounded to 12 hours. |
| Audio events | `NoiseRecorder`, `FileSafety` | PCM energy threshold, optional 10-second WAV clips, capped storage, private-file path validation. No ML classification. |
| Behavior rules | `SleepMath` | Pure scheduling, bedtime windows, digital RMS, and smart-window predicates with unit tests. |
| Storage | `Store`, `Backup` | SQLite schema 2 sessions/epochs/clips/alarms/pauses, private preferences, nondestructive migration, format 1/2 validation and atomic database replacement. |
| Bedtime friction | `BedtimeGuard`, `FocusActivity` | Optional accessibility service receives window-state package names only. No window-content capability. Redirects chosen apps; Settings, calls and emergencies exempted. |
| Screens | `MainActivity`, `Ui`, `GraphView` | Tonight, alarms, history, bedtime, setup. Width capped on large displays; system bar/IME insets handled. Empty graphs are empty rather than fabricated. |

## Measurements and limits

Movement is gravity-filtered accelerometer RMS. This is **phone movement**, not body movement in all placements and not EEG staging. The experimental early-wake predicate needs two consecutive active one-minute buckets, at least 20 sensor samples, and a time inside the configured window. It cannot promise improved alertness.

Noise is normalized digital amplitude; the device microphone is uncalibrated, so the UI does not label it decibels. Noise epochs use sample-weighted RMS across all captured frames. Missing microphone, light or movement samples are stored as nulls and graph gaps, rather than measured zeroes. Audio retention is time- and count-bounded. No speculative snore/cough/REM labels are stored.

Alarms use one-shot OS scheduling even for repeating weekdays. Next occurrences are computed in the current time zone with `java.time`, including DST normalization. The main alarm and snooze/check intents have distinct actions and identities. A single active ringing session owns playback at a time.

## Component security

Activities other than the launcher, the alarm receiver, and services are private. The accessibility service is exported only with Android's `BIND_ACCESSIBILITY_SERVICE` permission. The exported boot receiver accepts only allowlisted protected OS actions. No arbitrary nested intents, public mutation API, or shared database provider exist. PendingIntents are explicit and immutable. File selections use Storage Access Framework URI grants; no broad storage permission is requested.

## Integration boundaries

Future sleep-only Health Connect, ML sound recognition, sonar, smart lights, and external automation belong in separate adapters. They must preserve provenance and missing-data states, use consent-scoped permissions, and never replace the independent fixed alarm with a tracking-only timer.

## Pause and clock provenance

A pause stops sensor listeners, sleep audio and microphone capture; microphone
closing occurs on a worker before storage finalization. Resume closes the same
session's pause interval and starts fresh sensor windows. Active time uses elapsed
realtime; graph timestamps retain the original wall-clock anchor. Actual wall time
still determines alarm calendar windows. Clock changes are flagged in the report.
Process recovery ends an unfinished session at the latest persisted epoch or pause
endpoint and labels it interrupted, rather than pretending the app recorded until
relaunch. Live graph updates preserve the View, selected time and accessibility
focus; saved minute averages can have lower resolution than the live preview.
