# Optional Home Assistant bedroom light

The Bedroom light settings connect to **one Home Assistant dimmable light entity**. This is an actual HTTPS REST integration; it does not claim direct support for every lamp vendor. The existing Appearance smart-light row now opens these settings. Root Integrations may also link `SmartLightActivity`.

## Setup and explicit effects

Enter a Home Assistant HTTPS base address, long-lived access token, and exact `light.bedroom`-style entity. The server must have a system-trusted TLS certificate, a reachable REST API and a token permitted to read/control that entity. No HTTP downgrade, embedded URL credentials, query token or redirect is accepted. Discovery, OAuth account pairing and custom CA import are not implemented.

The default is **off**. Save a 1–30 minute sunrise, 1–100% maximum, and whether the earlier light state should be restored after the alarm. Enabling applies to **all real alarms**, independently of the phone screen sunrise toggle. It starts when the alarm begins ringing; pre-alarm lamp scheduling is not implemented.

Opening, changing fields and saving do not contact the server or turn on a lamp. **Check connection (read only)** performs only GET `/api/states/<entity>`. It checks the actual entity state and advertised brightness capability. **Test lamp at up to 5%** requires confirmation, requests at most three seconds of low brightness and then attempts restoration. Stop test and leaving the page cancel that test. A real alarm preempts a test. Network failure, process death or lamp power loss can prevent restoration; the UI reports a fixed error rather than claiming success.

## Runtime and ownership

`AlarmService` admits `SmartLightAlarm` only for positive-ID real alarms and a saved opt-in. It performs all network/Keystore work on a separate serial worker. Start/tick/close exceptions are contained so sound, vibration, flashlight and challenges continue independently.

The controller snapshots on/off and brightness, then sends small increasing brightness requests at most once per five seconds. Only one request task can be pending; a slow server does not create an unbounded backlog. Brightness ramps toward the saved maximum and does not lower an already illuminated lamp. If the lamp was initially above the chosen target it stays unchanged. Brightness is the Home Assistant 1–255 command scale, not a measured lux or calibrated physical intensity.

Before another change, the controller reads the lamp and compares it with its last requested level. A different on/off state or brightness beyond a small quantization tolerance stops progression. Cleanup restores the original on/off/brightness only if the current lamp still matches the last owned level. Selecting “Restore after alarm stops” off leaves the last level intentionally. No color settings are changed. Snooze, dismissal and service cleanup stop subsequent requests. Changed settings and Disconnect invalidate the old ownership. No command is persisted for later replay.

This state comparison is best effort. Delayed entity reporting can conservatively stop a valid ramp; another actor selecting exactly the same brightness cannot be distinguished. A request already in flight can still reach the server after cancellation; serial cleanup follows it when possible. There is no guaranteed cleanup after process death, force-stop, network failure or power loss. A restarted service begins from the observed current lamp state; it cannot recover a lost pre-crash snapshot. Actual lamp response, fades, network reliability, overnight behavior and physical intensity remain unverified.

## Storage and protocol

Separate private `smart_light_v1` preferences hold a monotonically revised configuration. Destination, token and entity are encrypted with Android Keystore AES-GCM, separate from sleep-record backups. Stale saves and failed encryption retain the previous settings. Disconnect removes credentials and increments revision. Portable sleep JSON/CSV/audio exports do not include this connection. Settings re-entry is required on a new installation.

The client uses bearer authentication, verified HTTPS, five-second connect/read timeouts, no redirects, and a 64 KiB response bound. Platform DNS resolution may exceed those timeouts. It reads `/api/states/<entity>` and controls through POST `/api/services/light/turn_on` or `turn_off` with exact entity, brightness and `transition:0`; it never fabricates a device state using the state-write endpoint. No automatic network retry runs after its alarm owner ends.

Primary references: [Home Assistant REST API](https://contributors.home-assistant.io/core/internals/rest-api/), [light entities](https://www.home-assistant.io/integrations/light/), [turn-on brightness and transition fields](https://www.home-assistant.io/actions/light.turn_on/).

## Verification contract

- `SmartLightRunTest`: 9 pure cases, monotonic/clamped ramp, bounded cadence, no dimming, already-above-target, manual override, dead owner, optional restore, unavailable/non-dimmable/read/write failures.
- `HomeAssistantHttpTest`: 4 pure fake-connection cases, read-only GET, exact command body, token separation, headers/timeouts, redirect/HTTP/TLS failures and response-size bound.
- `SmartLightStorageTest`: 6 native UUID-preference/fake-vault cases, default off, encrypted persistence/reopen, stale/failed saves, disconnect/reconnect revision and corrupt-config preservation.
- `HomeAssistantProtocolTest`: 4 native Android-JSON/fake-HTTPS cases, actual entity/capability parsing, unknown/error states and exact real light-service command schema.

No test contacts Home Assistant, runs an alarm or controls a physical light. Native tests need execution evidence before they can be called passing. Provider and actual lamp interoperability require a separately authorized test with a configured server; they are not inferred from compilation or fake transports.
