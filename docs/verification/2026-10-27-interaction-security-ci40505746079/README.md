# Bounded interaction and security native verification

[CI run 40505746079](https://github.com/omeyerrato/SleepWell/actions/runs/40505746079) passed on exact commit `f1f839ce5da8c40f095b435744607864c0b6d7f1` with both optional output/import switches false. Job duration: 4m45s. The isolated API 36 emulator built both APKs and ran only the selected interaction-security scope.

`focused-review-summary.json` reports two selected entries, three expected methods, all three methods passed, all three required original PNGs present, and no failures. Native logs report `InteractionSecurityNativeTest`: two methods in 10.253s; the single calendar permission method: one method in 9.677s.

The three original PNGs were individually inspected:

| Original | Observation |
|---|---|
| `calendar-rules-permission-settings.png` | Enabled unsaved rules, readable denial feedback and visible app-permission Settings action. Timing options and Save remain visible. The denial/status sentences are redundant but readable. |
| `calendar-rules-permission-granted-draft.png` | Private calendar is listed; both selected rule toggles remain on. Timing controls and the Save footer remain readable without overlap. |
| `calendar-rules-permission-revoked-draft.png` | Source controls are hidden after revocation; enabled draft, preparation and maximum-advance options remain. Settings recovery and Save are visible. Retained selected IDs are asserted in the saved Activity state and after permission restoration. |

The route check blocks AlarmEditorActivity with an Instrumentation monitor, drains private draft workers, and asserts no alarms/sessions/defaults were saved. The obscured-input check uses synthetic full/partial flags with an inert attached native Button, verifying CANCEL, blocked consent and a fresh clean gesture. The calendar method uses private preferences and fake permission/provider/sync boundaries; it does not save rules or apply scheduling.

These checks validate the bounded native interaction contracts. They use manual lifecycle callbacks, synthetic flagged input and a fake permission controller. Real overlays, OS permission dialogs, physical-device background transitions and end-to-end authorization are not verified. This was not a full app review, broad test campaign or security certification. No phone, real alarm scheduling, microphone, playback, haptics, flashlight or personal data were used.

The initial run [40503965551](https://github.com/omeyerrato/SleepWell/actions/runs/40503965551) on `c04fa5828073552436c685132a60a74639c1e9b5` failed before runtime because the capture helper lacked the WindowInspector import. It produced zero artifacts; the import was corrected before this successful run.
