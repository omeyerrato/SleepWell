# Manual tracking delay: F120 increment

F120 also includes flip-to-pause; that separate gesture remains unavailable.

Choose Off, 1, 5, 10, 15 or 30 minutes from Tracking setup > Manual start delay. Off preserves existing behavior. Saving never starts a service. The next visible Manual Start consumes a single-use duration/audio ticket and keeps the existing admitted foreground service through the wait. Sonar consumes its original visible admission too; no new background ticket is created.

DELAYED records no motion, light, sound or sonar transmission. It creates no session, applies no DND and starts no sleep sounds or recording automation. The elapsed-realtime countdown survives Activity recreation/backgrounding while the service lives. Cancel and Start now are fenced to its nonce. Wall/zone changes cannot stretch the duration. Delivery over 30 seconds late or across boot is interrupted.

Only actual capture calls Store.begin. Its original sensor provenance and real start time remain intact; a separate private journal links that session ID. Waiting is excluded, rather than fabricated as sensed sleep or an awake epoch. Sonar retains existing allocation/storage/cleanup ownership. Movement audio/light choices are immutable for this admission; pause/resume retain the same night. DND remains ACTIVE-only.

The private worker journal records WAITING, CLAIMED, STARTED, CANCELLED, INTERRUPTED or FAILED. Lost processes/boot cannot restart capture: reopening marks a lost pending owner interrupted and requires an explicit Start. A durable claim before capture is at-most-once; death between them can lose the start. OS service/microphone restrictions and power management can still prevent capture. Real OEM behavior is unverified. A microphone-service indication may appear during waiting although AudioRecord has not opened.

## Evidence contract

TrackingDelayTest has 16 pure cases; TrackingDelayEntryTest has 6. TrackingDelayStorageTest has 4 private preferences/SQLite cases. TrackingDelayFlowTest has 2 inert journeys and 9 screenshots: default, draft-recreated, saved, reopened, countdown, countdown-recreated, cancelled, second-wait, started. Fake clock/commands only; no Service, sensor, microphone, audio, alarm or DND effect is launched. Original main DB and preferences are checked unchanged. Compilation is not native execution or hardware proof.

## Primary sources

- [Sleep as Android awake detection](https://sleep.urbandroid.org/docs/sleep/awake_detection.html): delayed start and flip are separate options. SleepWell starts its actual record after waiting.
- [Android foreground types](https://developer.android.com/develop/background-work/services/fgs/service-types#microphone): initial microphone service admission restrictions.
- [SystemClock](https://developer.android.com/reference/android/os/SystemClock): elapsed realtime avoids wall-clock changes.

Local validation: 22 new host tests passed; scoped Gradle passed 51 focused tests (22 new + 8 AutomaticCapture + 21 DND ownership), debug/instrumentation APK builds and lint. First scoped build passed in 6m11; final journal-owner/copy incremental passed in 2m25. Native execution and all nine original screenshots remain pending.
