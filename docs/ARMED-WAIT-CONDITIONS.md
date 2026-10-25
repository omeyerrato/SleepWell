# Arm tonight: charging and screen-off conditions (bounded F116)

The pre-existing **Repeat nightly → Only when charging** checks charging once at its due time and skips that night if unplugged. It does not govern a manually armed one-night wait. This increment adds optional **Wait for charging** and **Wait for screen off** controls to **Automatic tracking → Arm tonight**, without changing that recurring behavior.

Both conditions default off. Screen-off is offered for the two time-based modes; After settling down already requires a continuous 15-minute screen-off, still-phone period. Settings survive Save, recreation and reopen; choosing Save does not arm anything. A typed immutable condition snapshot travels with the explicit foreground arming request. Later preference edits affect the next request, not the current wait. Corrupt condition types are shown for explicit repair and cannot silently weaken a new armed request.

## Actual behavior

The existing bounded foreground wait remains alive while a selected condition is unmet. It does not create a session, open recording audio or count blocked stillness toward sleep evidence. Every blocked sample/tick resets quiet evidence. Once charging/screen conditions permit admission, the selected time still has to arrive or a new qualifying stillness window must complete. A second condition read immediately before the recording boundary can revoke a pending START proposal. The existing exactly-once transition then owns session creation.

The original four-hour deadline is never extended by unplugging, using the screen, retrying a callback or changing preferences. Expiry and cancellation create no empty record. A missing/failed condition service blocks and reports an unavailable condition rather than inventing charging or screen state. The armed tracking view and silent ongoing notification show charging, screen-off or unavailable status. No separate exact timer, boot start or background foreground-service workaround is introduced.

The mode/sensor/notification/microphone foreground-service eligibility checks stay in place. For explicitly consented sound recording, the existing visible microphone foreground type is still established at arming, while actual audio capture waits for recording admission. Movement sensor observation and the existing bounded wake lock remain active during the wait; this is **not battery-adaptive sampling** or a claim of reduced standby power.

After recording starts, these are no longer gates: unplugging does not discard or pause an active night. Immediate manual recording, manual delayed start, sonar admission, pause/resume and recurring due-time charging/skip semantics remain separate. Recurring service admission explicitly selects no new per-night conditions, so saved Arm tonight preferences cannot leak into a background nightly token.

## Implementation and verification contract

- `ArmedWaitConditions`: immutable policy and explicit blocked reason.
- `ArmedWaitSettings`: strict private settings and typed internal Intent handoff; defaults preserve existing behavior.
- `AutomaticSleep.hold` / `AutomaticTransition`: blocked evidence reset, original deadline/cancel/once-only behavior and a final condition recheck before creating any record.
- Narrow `TrackingService` / `TrackingActivity` seams: freeze policy for ARM, read charging/current-screen state, show the actual blocked reason, preserve the existing recording/capture lifecycle.
- `AutomaticTrackingActivity`: actual saved controls, recreation, malformed-setting review and explicit Arm action.

Direct host JDK/JUnit passed **28 methods** in 0.159 sec: eight new `ArmedWaitTest` cases plus the existing seven `AutomaticTransitionTest` and thirteen `AutomaticSleepTest` cases. New cases cover default compatibility, independent charging/screen gates, timed waiting, no blocked stillness accumulation, revocation after qualifying evidence, final-check races, unchanged deadline/cancel behavior and no duplicate or active-recording stop effect.

New native fixtures are source-only and not yet compiled/executed at author handoff:

- `ArmedWaitNativeTest`: three methods / zero PNG; real private preferences, typed Intent snapshot freeze, unrelated recurring settings/default compatibility and malformed payload rejection without mutation.
- `ArmedWaitFlowTest`: one method / three PNG; actual native switches, unsaved draft recreation, Save versus fake Arm, reopen and immutable admitted settings. No Service, movement sensor, battery/charging change, audio or physical phone is involved.

Frames: `armed-wait-settings.png`, `armed-wait-recreated.png`, `armed-wait-reopened.png`. Actual charging transitions, OEM wake/foreground-service behavior, sensors, microphone establishment and overnight reliability remain unverified. No hardware/output or external service was tested. No dependency or schema migration is added.
