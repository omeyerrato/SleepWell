# Contactless calibration implementation and evidence

Source baseline: d43f480. This is an explicit foreground calibration flow, not overnight contactless tracking. No microphone capture, speaker output, Android service start or physical-device test was performed by this author.

## Implemented boundary

ContactlessSetupActivity is non-exported. Opening, saving, permission grant, resume and recreation do not open audio. Only the focused visible Test signal action can request the production duplex adapter. Granting microphone permission requires another Test tap; permanent denial offers app settings. Charging, hardware hints, normal audio mode and no external/competing audio owner are checked. Actual built-in routes and advancing timestamps must be established before chirps. Losing focus/pause/rotation cancels; saving the independent profile cannot start tracking.

The production adapter allocates AudioTrack and privacy-sensitive AudioRecord at mono PCM16/48kHz on one worker. It requests ordinary transient media focus, starts muted with zero PCM, confirms actual built-in routes, and emits original phase-continuous 18.5–20kHz FMCW chirps at a user-selected 1–10% digital gain (5% default). System volume is never changed. Emission has a 50ms PCM start ramp; stopping prioritizes immediate mute/release. Digital gain is not an acoustic SPL guarantee; the probe may be audible.

DSP uses bounded direct-reference correlation over every integer lag, complex dechirp/Hann FFT, and independent off-direct echo/noise/direct-leakage gates. After a two-second baseline, a consecutive moving-echo check is required. The graph contains actual 40ms relative echo-change intervals; failed/unknown intervals remain blank. An observed stationary echo after qualification can be zero. The first sample after a gap cannot fabricate a zero delta. This scalar never enters accelerometer thresholds, smart wake, stillness inference or sleep-stage estimates.

Reads/writes are nonblocking with separate 600ms progress deadlines. A test lasts at most 20 seconds after allocation. A stalled native constructor/release may outlive cancellation but retains the sole process lease, preventing unlimited new owners. Route, charging, permission, focus, silencing, timing and format loss fail closed. Every cleanup step is attempted independently. Unconfirmed cleanup retains admission until process restart. Public callbacks cannot guarantee zero transient sound during an OS reroute.

Profile writes use a retained PlanOperations job and pinned sonar-only SharedPreferences. Busy/error survives recreation without duplicate save or capture. The strict versioned codec accepts the current rate/algorithm/gain only. Existing noise, clip, alarm, motion and recurring preferences remain unchanged. Test qualification is not restored after screen recreation.

## Integration still required

The entry hook, overnight microphone-FGS ownership, shared 48k-to-16k noise-analysis fanout, ordered storage sink, additive schema 8 sensor/quality provenance, portable format 5 round trips and sensor-labelled reports remain parent-owned prerequisites. Setup visibly says overnight recording is not connected. Saving a profile alone does not deliver the overnight feature. No concurrent second microphone starts here.

Hardware acoustics, placement, processing/AGC, battery use, routes, thermal load and clock tolerance remain unverified. Generated PCM/fake ports do not establish physical, clinical, sleep-stage or medical accuracy.

## Verification contract

- SonarDspTest: 8 pure methods covering exact known path, gain/periodicity, leakage, stationary/moving delayed echoes, measured zero, noise/silence, clipping/timing and missing first-after-gap evidence.
- SonarPumpTest: 7 pure methods covering partial duplex into real DSP, route admission/loss, independent stalls, drift, silencing/charging/cancel and original error retained through cleanup failure.
- SonarRuntimeTest: 5 pure methods covering factory/cancel cleanup, blocked close ownership, actual DSP readiness/bounded history, unconfirmed cleanup refusing allocation, and queued readiness invalidated by cancel.
- ContactlessSetupFlowTest: 3 native methods / 15 screenshots. Explicit generated-PCM test, gain/retest/save/reopen; cancel/rotation; charging/unsupported/permission states; retained busy save/commit failure. All audio ports are fake. Requires ciReview and qemu, private preferences, worker drain, every owned Activity finished, and full original preference equality.
- SonarSettingsNativeTest: 1 native method/no screenshot. Private codec round trip and rejection of fractional/string rate, string gain, unknown algorithm and extra fields.

Screenshot prefix sonar-setup-: initial, ready, gain-changed, retested, reopened, recreated, testing, cancelled, cancel-recreated, charging-error, unsupported, permission, saving, saving-recreated, save-error. Fresh native execution and actual screenshots remain required; source compilation is not UI acceptance.

The first direct pure run exposed a stride-four correlation defect (known lag 73 selected 68). The search now checks every integer in the same bounded ±480 range; the exact-path assertion remains. The corrected first 17 tests passed in 12.818s. Cleanup/cancel/gap regressions bring the source contract to 20 tests. Final Android build results are reported separately after terminal output.

Final local validation: production debug APK, native test APK, focused pure tests and lint passed in 3m33s (session93001), with zero lint errors and87 warnings. The final fixture-only delta matched virtual timing to480 PCM frames/10ms and retained generated moving echoes inside the actual final15-second chart window; all20 pure tests and native Java compilation passed again in55s (session48414). Final pure suites: DSP8/0fail, pump7/0fail, runtime5/0fail. No native test or screenshot execution has occurred locally.

## Primary references

[Sleep as Android sensors](https://sleep.urbandroid.org/docs/sleep/sensors.html), [sonar FAQ](https://www.sleep.urbandroid.org/docs/faqs/sonar_faq.html), [UW FMCW paper](https://modernmobile.cs.washington.edu/docs/apneaapp.pdf), [AudioRouting](https://developer.android.com/reference/android/media/AudioRouting), [capability hints](https://developer.android.com/reference/android/media/AudioManager), [microphone FGS limits](https://developer.android.com/develop/background-work/services/fgs/service-types#microphone), [capture silencing](https://developer.android.com/media/platform/sharing-audio-input), [nonblocking capture](https://developer.android.com/reference/android/media/AudioRecord#READ_NON_BLOCKING), [playback timestamps](https://developer.android.com/reference/android/media/AudioTrack#getTimestamp(android.media.AudioTimestamp)). API/feasibility references do not validate SleepWell hardware accuracy.
