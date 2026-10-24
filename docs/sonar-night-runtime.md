# Manual contactless recording

This flow adds a continuous, explicitly started phone speaker/microphone recording to the existing placement test. It measures relative off-direct echo change. It does not infer sleep stages, reuse accelerometer RMS thresholds, or start automatically at bedtime.

## Entry and ownership

Tracking setup opens the existing contactless test. Saving that configuration does not start capture. A separate focused **Start tracking** creates a single-use, ten-second in-process admission. Resume requires another focused action for the exact paused session; a notification opens that visible screen first. Recurring and one-night automatic tracking retain their movement-only paths.

The microphone foreground-service type is established before native audio allocation. The real adapter checks microphone permission, charging, built-in routes, calls, active alarms, audio focus, timestamp continuity, input backlog and actual recording silencing. It emits through the existing original 18.5–20 kHz PCM chirp only after confirmed built-in routes. It never changes a system volume setting. These frequencies can be audible; Stop remains available. Losing charging, focus, route or timing stops the run with an interrupted record and readable error. A native cleanup that cannot be confirmed retains process admission and reports an explicit blocked state; it does not pretend the microphone is released.

The native adapter has not been exercised on a real phone for this change. Public routing callbacks cannot guarantee a zero-length transient during OS rerouting, and hardware timestamp, frequency-response and background reliability vary by device.

## Durable timeline

The actual allocated microphone source is recorded before capture starts. A fixed session wall/elapsed pair maps AudioRecord BOOTTIME frame positions to wall-epoch intervals. The DSP's relative frame numbers are never used as wall time. The session remains on that anchor after a system-clock adjustment, with the existing clock-changed flag set by the storage adapter.

One ordered storage worker consumes a bounded 512-frame queue. It writes atomic schema-8 sonar checkpoints of at most five seconds, preserving actual quality changes. Startup, absent capture, unusable echoes and terminal loss intervals have NULL movement. An observed valid zero remains zero. No interval is averaged across a pause or quality gap. Paused time is excluded from active duration; resume opens a new real audio owner and calibrates again. A changed microphone source cannot silently resume the same provenance.

Stop and Pause cancel audio independently of a slow storage provider. The foreground service stays alive while final ordered writes complete. An error never reports successful finalization. The existing format-5 sensor codec preserves the configuration, measured values and NULL quality gaps in portable records; no raw microphone audio is saved by sonar.

## Current boundaries

- Sound analysis/clips are explicitly unavailable in this initial exclusive-microphone sonar path. Existing sound/light preferences are preserved, and no second microphone recorder starts. A shared 16 kHz analysis fanout remains required for simultaneous sonar and noise analysis.
- Smart wake and automatic sleep onset must not apply movement-sensor thresholds to echo-change values. This change does not add such inference.
- Synthetic PCM demonstrates algorithm/control/data behavior, not recognition accuracy, physical range, medical value or an overnight hardware qualification.
- Force-stop, process death, OEM battery management, charging loss and native driver failures remain genuine interruptions; no background replay restores audio.

## Verification contract

New pure tests cover immutable capture clocks, missing-vs-zero evidence, five-second aggregation, pause exclusion, one-use/ABA/expired admission, real continuous DSP through synthetic echo PCM, signal loss, bounded cancellation despite slow storage, late DB admission, future timestamps, source changes and failed foreground resume. These run without Android hardware.

`SonarNightStorageTest` has two isolated SQLite methods: real synthetic PCM through production Pump/DSP/coordinator into the actual Store adapter, pause/resume, quality gaps, charging-loss interrupted finalization, and backup validation. `SonarNightFlowTest` has two visible native methods with fake command/audio ports and real admission/coordinator/private SQLite. Required originals are `sonar-night-{setup,measuring,paused,paused-recreated,resumed,missing,stop-confirmation,needs-setup,charging-error,charging-recreated}.png`.

Local compilation and pure tests are distinct from native execution and visual acceptance. CI must execute the four native methods and inspect all ten actual originals before marking this flow accepted. Real microphone, emission, permissions, service delivery and overnight battery/device behavior remain untested here.

Public API references: [AudioRecord timestamps](https://developer.android.com/reference/android/media/AudioRecord#getTimestamp(android.media.AudioTimestamp,%20int)), [AudioTimestamp timebases](https://developer.android.com/reference/android/media/AudioTimestamp), [AudioRouting](https://developer.android.com/reference/android/media/AudioRouting), [microphone foreground-service requirements](https://developer.android.com/develop/background-work/services/fgs/service-types#microphone).

## Synthetic-clock correction

CI run 40089335942 preserved the original failure evidence: the real-time paced fake closed after 2,277 ms with only three CALIBRATING intervals (64–184 ms), and another closed after 1,585 ms with capture through 410 ms. Both reported `Capture arrived after its interval was checkpointed.` The fake generated historical PCM after host CPU/JIT time had advanced; the production worker correctly refused to overwrite a missing interval with late evidence.

The native fixtures now advance a paired wall/elapsed **synthetic signal clock** with the generated 48 kHz PCM. A forwarding sink still uses the unmodified real Store adapter and exact SQLite-created session start; it admits that returned clock pair once before capture. Explicit pause time is advanced in this same timeline. Real Start/Resume nonce validation and test wait bounds continue to use real SystemClock time.

Both isolated storage cases inject a 1.5-second host-processing stall before the first emitted chirp frame. It must not age synthetic PCM or fabricate a gap. The same real DSP, SQLite writes, quality gates, NULL gaps, interrupted tail, close and backup checks remain. All actual graph/system-bar pixel checks are retained. This simulator verifies the functional signal/data/control flow; it does not establish real-time throughput or device reliability. No production clock, timeout, queue, quality threshold or late-evidence policy was changed.
