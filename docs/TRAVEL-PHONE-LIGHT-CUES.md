# Experimental phone-light cues

This extends board 09 Travel mode with an actual, opt-in phone-flashlight output path. It does not turn the existing daylight reminder plan into a jet-lag prevention claim. Research and primary-source links are in [the proposal](JETLAG-LIGHT-CUES-PROPOSAL.md).

## User flow

1. Choose the actual home/destination IANA zones and departure date.
2. Open Experimental light cues and explicitly review the first home-local night, start clock, 15/30/60-minute window and 1–14 nights before departure. Defaults are disabled and unreviewed, not prescribed timing.
3. Save. Trip and cue revisions are committed together. Saving never emits light. Editing trip identity disables/unreviews the draft cue plan; older callers saving a trip also disable cues rather than silently carrying an old intervention forward.
4. Start an ordinary manual tracking recording. Its active session and fresh admission token are required. From Tracking or Travel, open Review light cues and explicitly Arm remaining window. Only one finite window, no farther than twelve hours ahead, is armed by that action.
5. Stop light cues explicitly, pause/end the recording, or let the window end. Resuming, relaunching, rotating, rebooting and automatic/recurring recording starts do not arm anything. A later night requires another explicit arm action.

The Review screen reports requests and skipped requests, not delivered flashes. It retains terminal/error status and shows a saved busy run without a living owner as Interrupted; it never restarts it. Unsupported/busy flashlight, state-save failure, clock/zone changes and unavailable output are errors or stopped states, not success.

## Timing and ownership

The requested pulse is 2 ms every 15 seconds, using an absolute elapsed-time schedule within the original window. Handler delays, asynchronous CameraManager commands and LED hardware mean that actual optical duration, intensity and dose are unknown. Deep sleep, scheduling contention and vendor restrictions can stretch/miss requests. A request over one second late is skipped; no catch-up burst or end-time extension occurs. Home-local DST gaps advance to a valid instant; a repeated hour uses the earlier offset once. A wall/elapsed discrepancy over two seconds or device-zone change stops the run for explicit review.

The ongoing recording notification identifies armed cues and offers a private, immutable, nonce-fenced Stop light cues action. Its refresh uses notification updates without changing foreground-service types. If Android hides notifications, the Review screen remains an explicit stop path. The tracking foreground service and its existing wake-lock lifetime own the worker; there is no new boot receiver, background launch, microphone capture, sensor selection, or automatic service admission. An accepted ringing alarm stops travel cues even if that alarm does not enable flashlight. Every ON also rechecks the exact active manual owner, current saved revision and alarm state.

Alarm and travel flashlight adapters share one lazy serial broker. Alarm priority revokes the old travel lease before acquiring light; stale travel callbacks/close requests cannot turn off a new alarm lease. A reservation taken before enqueueing prevents idle worker shutdown from stranding new clients. Camera status/availability callbacks revoke unsupported external changes. Existing externally lit alarm borrowing is preserved: that alarm does not claim exclusive OS ownership or turn off another app's already-lit torch on cleanup. Physical camera ownership remains Android's, not this broker's.

Status persistence precedes each ON request. This creates an honest crash boundary: a persisted request may not have reached hardware, and the OS may have changed/released the light independently. Interrupted requests are never replayed. Cleanup failures are retained as errors and cannot overwrite a newer nonce's journal. No claim of guaranteed delivery, measured 2 ms light, REM targeting, calibrated dose, clinical prevention or personalized phase-shifting efficacy is made.

## Evidence and pending checks

- Direct javac/JUnit and scoped Gradle: 47 tests passed (15 broker, 5 admission reservation/queue, 15 session, 12 window/DST/configuration tests). No Android hardware or service was run.
- `TravelCueStorageTest`: eight isolated preference/codec/journal/alarm-adapter methods are written. These inject the light provider and never access camera hardware or original preference files.
- `TravelCueFlowTest`: two native configuration/review methods, 16 planned original captures. Settings, cancellation, Save/reopen, explicit fake Arm/Stop, output error, active recreation and interrupted reopening use private preferences and a fake clock/timer/light port. No AlarmActivity, TrackingService, permission UI or real output is admitted by the fixture.
- Debug and native-test APKs compiled successfully in Gradle session 36083 (5m14s), with all 47 focused tests passing. Final context-retention cleanup was compiled again in session 17924 (1m15s): both APKs and lint pass, 0 errors / 87 existing warnings. The new stored-Context warning was removed without suppression. Strict source review cleared the bounded ownership/lifecycle scope.
- Native execution and all 16 original screenshots remain pending. Originals must be visually reviewed against board 09 before accepting composition. Compilation and fake model tests are not notification delivery or optical/hardware verification. No physical output/timing/calibration has been tested.

Planned captures: `travel-cues-{settings-off,window,date,time,window-recreated,settings-enabled,settings-reopened,invalid-window,no-manual,ready,active,active-recreated,stopped,stopped-reopened,output-error,interrupted}.png`.

Wearables, light masks, smart lights, physiological/circadian phase measurement and automatically prescribed jet-lag timing remain outside this bounded phone-only flow. User-authored timing is not equivalent to a validated prevention algorithm.
