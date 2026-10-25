# Optional flip-to-pause (F120)

This is a phone-movement recording control, separate from alarm flip snooze and sleep/awake classification. It is off by default. Save the choice from Tracking setup or the live screen; it applies to the next admitted recording, not the middle of an existing run. Manual Pause and Resume remain available.

## Gesture and real pause

An active movement recording first requires a fresh, stable face-up posture for at least one second. Turning face down and holding it still for 1.2 seconds requests a five-minute pause. A phone that starts face down cannot trigger it. At least five samples, plausible gravity, less than 250 ms delivery age, no 300 ms sample gap, no future/pre-admission/duplicate timestamps, and a recently observed baseline are required. Unreliable input resets the sequence. These are conservative interaction thresholds, not measured sleep or medical cutoffs.

The existing recording pause path stops measurement listeners and sound capture, waits for audio cleanup, then writes its normal pause row. Only that acknowledgment lets this owner resume. After the pause starts, briefly hold face down and then face up to resume early; otherwise the same run resumes at five elapsed minutes. Further face-down input cannot extend the deadline. If cleanup or delivery arrives more than 30 seconds late, recording stays paused with an explicit Resume fallback.

Ordinary manual movement recordings that enable this feature snapshot their original sound/clip/light choices using the existing foreground-capture contract. Their original microphone-typed foreground service stays admitted through a flip pause, but AudioRecord is closed. No new microphone permission, ticket or background foreground-type upgrade is created. A no-audio run cannot escalate on resume. DND uses its existing ACTIVE-only pause/resume ownership. Sleep sounds use their normal capture pause/resume hooks.

Paused time is excluded through the existing same-session clock and tracking_pauses rows. Gesture samples are never inserted as motion measurements, sleep stages or fabricated awake events. An independent low-rate posture listener remains on during this particular pause; the UI says so. A bounded additional wake lease permits the five-minute timer while the normal recording wake lease is released.

## Ownership and unsupported states

Each gesture owner captures the exact recording ID and service-run identity. Manual Pause (or **Keep paused** during a flip pause) revokes its automatic resume; manual Resume rearms a fresh baseline. Stop, a lost owner, process death and boot cannot resume it. Delayed/automatic-armed waiting never constructs or starts the new listener. Old callbacks are generation fenced. Failed unregister retains the release handle and retries without restarting capture. Failure is visible; manual controls still work.

Contactless sonar is explicitly unsupported: its pause/resume requires a fresh visible entry and microphone admission. This control does not bypass that requirement. No accelerometer or unreliable/unavailable input gives a manual Pause fallback. Ten-Hz accelerometer use adds no runtime permission or high-rate sensor permission. Real device placement, OEM batching, background delivery, wake behavior and microphone retention remain unverified.

The explicit 30-second gesture-only preview reads orientation while the screen is visible; it never controls a recording. Saving or leaving stops it. No sensor runs merely because settings were saved. The local preference is separate from sleep records and backups; there is no schema migration or gesture lease restored from disk.

## Evidence contract

- TrackingFlipGestureTest: 14 pure freshness/posture cases.
- TrackingFlipSessionTest: 18 pure owner, cleanup acknowledgment, timer, manual override, stale callback, failed registration/release and same-run rearming cases.
- TrackingFlipStorageTest: three private SQLite/controller cases proving actual pause rows, five-minute excluded duration, unchanged sensor provenance, early resume and no fabricated measurement/audio rows.
- TrackingFlipFlowTest: two inert native config/preview journeys and seven PNGs: tracking-flip-default, draft-recreated, saved-reopened, preview, detected, unsupported, preview-stopped. The sensor/wake port is fake and original database/preferences are checked unchanged.

No test launches TrackingService or AlarmActivity, registers a real sensor, records sound, changes DND or uses a physical phone. Compilation/pure passes are not native execution or hardware certification.

Local validation passed: 32 new host cases; scoped Android debug/instrumentation APK builds, 67 focused pure tests across six suites (0 failures/errors), and lint (0 errors / 142 warnings, none in the new TrackingFlip files). The scoped build completed in 5m01. Native execution and all seven original screenshots remain pending.

## Primary sources

- [Sleep as Android awake detection](https://sleep.urbandroid.org/docs/sleep/awake_detection.html): flip adds a five-minute pause. SleepWell uses an explicit face-up-to-face-down sequence and separate resume ownership.
- [Android motion sensors](https://developer.android.com/develop/sensors-and-location/sensors/sensors_motion): accelerometer gravity, coordinate system, variable availability and sampling restrictions.
- [Android foreground service types](https://developer.android.com/develop/background-work/services/fgs/service-types#microphone): original visible microphone admission requirements.
