# Counting, tilt maze and flag challenges

This increment adds three actual optional phone challenge types to the existing ordered alarm routine, without changing saved alarm/backup formats or replacing existing Memory (matching) and Sequence (remembered digits) tasks. Each supports one to six rounds, participates in the existing completed-unit checkpoint, and is configurable from Alarms → an alarm → Challenges. Task configuration opens a separate settings screen with a silent shared-component preview.

## Counting

An original 4×4 or 5×5 board mixes circles, diamonds and triangles. The prompt selects one shape; the user must type its actual count. Wrong/empty/noninteger answers do not advance. Every occurrence/unit/round has a deterministic board, so a rebuild or process recreation cannot replace the question silently. Typed text survives normal Android saved-state recreation; an occurrence/unit key prevents its reuse on another question. An accessible board description enumerates cells without supplying the count directly.

## Tilt maze

An original deterministic depth-first maze has a reachable gold target and solid outer walls. The purple ball has a radius; continuous, subdivided collision checks prevent crossing walls or tunneling through corners. There is no touch/drag shortcut that grants completion.

Opening a maze does **not** register a sensor. The explicit **Calibrate tilt** button starts the phone's actual accelerometer. It requires a still, face-up calibration with at least 12 fresh samples spanning 600 ms. Sensor timestamps must be monotonic, at/after the current arm time, not future dated and no more than 250 ms old. Nonfinite, violent or unsuitable posture readings are rejected. The calibrated offset, dead zone, low-pass filter, display-axis mapping, 50 ms integration cap and bounded speed control motion. Display rotation requires recalibration; long gaps cannot catch up by jumping the ball.

The owner guards every sample/completion with the active occurrence and completed unit. Pausing closes the listener, checkpoints the current legal position and requires recalibration on return. The deterministic maze and position are restored for the same unit. Position checkpoints are limited to once per 500 ms while moving, plus pause and completion; only one unit is retained. Stale controllers cannot overwrite a successor. Goal completion stops the sensor and saves its checkpoint before invoking the existing routine advancement. No alarm/step/recording service or special sensor permission is introduced.

Missing/failed sensors, no stable fresh readings, invalid saved position and failed persistence have visible error states. **Use Math instead** is an explicit persisted alternative for that unit. Sensor release must succeed before the alternative or completion is accepted. Uncertain release retains the handle and offers cleanup retry instead of starting a replacement listener. A corrupt saved maze shows the Math alternative through the existing arithmetic task rather than being labeled solved.

This is a phone tilt game, not sleep-stage analysis or an activity-recognition claim. Device orientation, actual OEM accelerometer delivery and physical usability remain unverified until a separately authorized quiet sensor test.

## Flags

The bounded initial recognition pack contains Japan, France, Italy, Germany, Poland, Ukraine, Sweden and Finland. A deterministic question shows one flag with four unique country choices. Only the correct country advances. The simple recognition illustrations are drawn originally in `WakePuzzleView.FlagArt` using basic geometry; no third-party image, font emoji, downloaded asset or app artwork is included. They are illustrative flag patterns, not a claim to reproduce every country's legal construction specification. Source artwork is covered by this repository's MIT license. This is an eight-country pack, not a worldwide flag library.

## Silent preview and verification boundary

`ChallengePackPreviewActivity` is nonexported. It uses the same counting/flag controls and `TiltMazeTask` as the alarm. It never starts an alarm, plays sound, opens a camera or starts sleep tracking. A real user's explicit Calibrate action can use the actual accelerometer. Tests replace the sensor factory before constructing any maze; they never register a real sensor, open `AlarmActivity`, send a PendingIntent or change actual alarm state. The preview's explicit Math alternative is itself solvable and uses the existing arithmetic model.

Pure tests cover stable/varied generated questions, correct typed answers and four unique flag choices; bounded owner/round admission; maze connectivity, solid walls, legal persisted positions and collision; calibration/freshness/rotation/dead-zone/velocity/gap behavior; and synthetic calibrated traversal of generated corridors. `ChallengePackNativeTest` adds eight private-preference/native-control/fake-sensor methods: checkpoint/fallback reopening and stale writes; corrupt checkpoints; real alarm routine round-trip and completed-unit persistence; counting wrong/stale/correct action; flags wrong/stale/correct action; fresh calibrated path completion and release ordering; pause/reopen/recalibration; unavailable sensor and failed-cleanup alternative behavior. It has **0 PNG** contract and makes no design-fidelity claim.

Native methods must be executed separately before being called passed. A compiled APK and pure geometry tests do not verify a physical phone alarm or accelerometer.

The additional **jumping** category remains unimplemented; no claim is made without a validated actual sensor behavior. Reference: [Android motion sensor values and coordinate systems](https://developer.android.com/develop/sensors-and-location/sensors/sensors_motion).

Author verification on 2026-10-25: scoped Gradle passed in 4 min 5 sec with 24 pure tests across 4 suites (14 new challenge tests plus 10 existing routine tests), debug APK, native test APK and lint (0 errors, 135 warnings, 1 hint). Eight native methods compiled but have not been executed here. No physical sensor, AlarmActivity, alarm/audio/flashlight or phone test was performed.
