# Automatic start: foreground-armed candidate

This is an opt-in, **one-night foreground wait**, not unattended sleep recognition.
Open Tracking setup → Choose automatic start, select a radio option, then Arm tonight.
Saving a preference alone does not start sensors or schedule background work.

Implemented source paths:

- At bedtime: the saved local bedtime, including its calendar time zone, anchors an elapsed-realtime deadline.
- Before smart window: 45 minutes before an enabled alarm's smart window. A missing enabled smart alarm is an error.
- After settling down: at least 15 minutes of low accelerometer movement while the screen is off. Five-Hz sampling is requested; a gap over two seconds, insufficient event coverage, screen use, invalid energy, or movement resets evidence. A timer alone cannot trigger this mode.
- The wait expires after four hours. Schedule modes must be armed within that period. An ongoing silent notification provides cancel/open controls.
- No session row is created while waiting. A qualifying trigger creates a normal recording at the actual start time; pause/resume/end use the existing tracked-session path.
- Automatic recordings exclude the microphone and sleep audio even if those preferences are enabled for manual tracking. No boot receiver starts this flow, and it does not silently upgrade into a microphone foreground service.
- A stopped/killed wait is never represented as still active merely because its saved preference remains. Reopen and arm again; the service is deliberately `START_NOT_STICKY`.

Limitations: phone stillness does not prove a person is sleeping; an idle phone on a table can qualify. No Google Sleep API, activity-recognition model, automatic wake detection, retroactive missed-night estimate, sonar, charging policy, or all-day background inference is implemented here. Continuous foreground motion sampling uses a partial wake lock while armed and can consume battery. Device/process interruption can end the wait. Schedule changes after arming apply the following time it is armed. The elapsed deadline survives wall-clock changes within a live service, but the service does not resume a wait after a process kill or reboot.

Evidence is deliberately scoped. `AutomaticSleepTest` and `AutomaticStartTest` exercise production decision logic with sampled motion and clock inputs. `AutomaticTrackingFlowTest` is an emulator-only native settings/storage flow with fake arm output; it captures `automatic-manual.png`, `automatic-stillness.png`, and `automatic-reopened.png`. That test cannot certify actual Android sensor delivery, screen-state callbacks, foreground-service lifetime, or an overnight recorded-night transition. Those device effects remain unverified. No physical device, ADB, microphone, audio, alarm, vibration, flashlight, power, or screen-lock test is authorized for this milestone.

Reference behavior: [Sleep as Android automatic tracking](https://www.sleep.urbandroid.org/docs/sleep/automatic_sleep_tracking.html). Platform boundary: [Android foreground-service restrictions](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start). The supplied board `10-phone-sensors.png` informs the native radio rows; this candidate does not claim full board fidelity before Android screenshot comparison.
