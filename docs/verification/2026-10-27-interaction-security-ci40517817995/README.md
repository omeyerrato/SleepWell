# Bounded interaction/security native pass

[Run 40517817995](https://github.com/omeyerrato/SleepWell/actions/runs/40517817995) passed on exact commit `795ab556f843b1570809e5bf85fd387b9ee63260`, with interaction-security scope and both optional output/import switches false. Job duration: 4m50s. Both APK builds and the isolated API36 renderer passed. Summary: two selected entries, three methods passed, three required original PNGs, zero failures.

`InteractionSecurityNativeTest` passed two methods in 16.297s: canceled/paused deferred HomeLaunch navigation and synthetic full/partial obscured-gesture rejection. The gesture method also uses the actual invalid-terminal IncomingAutomationActivity with an inert native Button. It verifies pressed-state cancellation, blocked later UP and a fresh clean gesture without preparing receipts, granting control or dispatching tracking commands.

The single calendar permission method passed in 9.79s after the fixture synchronization correction. It covers failed/canceled prompts, rationale cancellation/continuation, Settings recovery, access restoration and revocation, while asserting retained unsaved selections and zero rule Save/sync. No repeated taps or weakened count expectations were added.

All three original PNGs were individually inspected:

| Original | Observation |
|---|---|
| `calendar-rules-permission-settings.png` | Enabled draft, visible Settings recovery, timing choices and Save; no overlap. Denial and status wording is redundant but legible. |
| `calendar-rules-permission-granted-draft.png` | Private calendar listed; both rule selections remain on; timing controls and Save stay readable. |
| `calendar-rules-permission-revoked-draft.png` | Source controls hidden; enabled draft, timing choices, Settings recovery and Save remain. The test also asserts retained selected IDs and restored checked controls. |

Only unchanged selected originals, environment, summary, two instrumentation logs, this note and SHA-256 inventory are public. Full guest/renderer logs remain privately in ignored `.android-local/interaction-security-ci40517817995-original`. No failure diagnosis required publishing those logs.

This pass supersedes the calendar fixture failure in run 40515505467; that failure remains historical evidence. The build includes the exact unused Exposure Notifications manifest removal while preserving Nearby Connections, but this run does not exercise radio pairing. Synthetic flags/manual lifecycle callbacks and fake permission/provider boundaries do not verify real hostile overlays, OS permission dialogs, cross-UID grants, actual service command admission or physical-device outputs. Broad interaction/security acceptance remains open. No phone, real alarm Save/scheduling, audio, haptics, flashlight or personal data was used.
