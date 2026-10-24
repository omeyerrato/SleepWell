# Flip and volume-key snooze actions

Per alarm: **Snooze → Flip to snooze** and **Volume keys**. Choices are Phone volume (exact old platform behavior), Snooze and No action. Input actions do not dismiss a challenge. The on-screen, key and flip paths all recheck the current occurrence and remaining snooze count; a zero/exhausted limit cannot be bypassed. Held-key repeats, old key-downs from before resume/admission, key-up events and paused UI cannot start another snooze. No global volume writes are introduced.

Flip uses an independent real accelerometer listener while the alarm Activity is resumed. It requires stable face-up gravity for 400ms, followed by stable face-down gravity for 800ms. Events must be fresh, finite and monotonic; long gaps revoke posture qualification. Starting face-down, shaking or briefly inverting does not count. Pause revokes posture; resume starts a fresh gesture and rejects queued events timestamped before its new admission. The occurrence is captured separately from the Activity’s changing current run. The listener is released before snooze; failed unregister blocks that action and retains the handle for retry. It never unregisters the separate Shake or Walk listeners. Missing sensors/registration show a concise unavailable notice; the normal on-screen action remains available.

The optional strict `inputs` JSON keeps old absent metadata exactly default (flip off, platform volume behavior). Bad values are retained as invalid review state, never enable flip or alternate key behavior, and cannot become a reusable template. The new behavior transfers through saved alarm defaults and regular alarm JSON. Quiet development alarm clones clear it. Parent integration must reject `inputsInvalid` in portable backup validation; restores remain inactive. No database change or new permission.

Android may consume hardware keys before Activity delivery; actions apply while SleepWell receives input. These controls do not intercept power/shutdown. Sensor thresholds are deliberate-gesture heuristics, not a sleep or medical measurement. Real hardware behavior remains unverified because no physical alarm/sensor/key tests are authorized.

Primary API references: [Android motion sensors](https://developer.android.com/develop/sensors-and-location/sensors/sensors_motion) describes accelerometer force including gravity; [KeyEvent](https://developer.android.com/reference/android/view/KeyEvent) defines event timestamps/repeat delivery. The implementation uses elapsed-realtime nanoseconds for sensor samples and uptime milliseconds for key events, matching their different Android clocks.

## Verification contract

- `AlarmInputsTest` four pure policy/default/template cases; `FlipSnoozeTest` five pure gesture/freshness/duplicate/false-trigger cases.
- `AlarmInputsStorageTest` three private SQLite/JSON/template/portable cases, fake scheduler only.
- `AlarmInputsFlowTest` one emulator-only native configuration method. Select/cancel, flip toggle, recreate, explicit Save and reopen; no AlarmActivity, sensors or actual key events. Five originals: `alarm-inputs-{initial,selector,draft,recreated,reopened}.png`.

Native compilation/execution and original screenshots are separate gates; no real alarm delivery or sensor outcome is claimed from configuration fixtures.
