# Interaction follow-up: security pass, calendar fixture failure

[Run 40515505467](https://github.com/omeyerrato/SleepWell/actions/runs/40515505467) used exact commit `8b303e247d0a4a70fb031c64b7d6803f46f5f4ef`, interaction-security scope, and both optional output/import switches false. Both APK builds and isolated renderer setup passed. The selected native scope **failed overall**.

`InteractionSecurityNativeTest` passed both methods in 8.855s. This includes the existing canceled/paused deferred-route check and the new synthetic full/partial obscured-gesture sequences through an actual invalid-terminal IncomingAutomationActivity with an inert attached Button. It asserts zero blocked clicks, cancellation of a pressed control, and acceptance of a fresh clean gesture. The invalid request does not enable automation, prepare a receipt, grant control or dispatch tracking commands.

The single calendar permission method failed in 4.666s at line 44: expected request count 4, observed 3 immediately after the native Continue tap. The click helper did not wait for a committed replacement layout before coordinates or for posted native click handling afterward. This is a fixture timing diagnosis; no production exception or ANR was recorded. The bounded correction reuses the exact focused native frame callback, drains the main queue after injection and awaits the actual expected request/Settings callback state. It does not retry taps, add cases or weaken expected counts. That correction is not executed by this run.

The failure occurred before the first capture. **Zero PNGs were produced; all three required calendar images are absent.** The summary correctly reports the failed calendar method and three missing originals. The earlier calendar pass and images at run 40505746079 are historical evidence, not a substitute for this failed run.

No production source changed in response. No automatic rerun was dispatched. Physical overlays, real OS permission-controller interaction, cross-UID grant enforcement, actual service command admission and phone pairing remain outside this bounded native check. Broad interaction/security acceptance remains open.
