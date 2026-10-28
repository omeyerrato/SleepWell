# Travel — separate Plan and Cue native proofs

The37 reference originals are available as a **composite of separately passing class scopes**, on a byte-identical production tree. There is no blanket37-frame or single-job PASS. All three full attempts remain explicitly FAILED.

| Passing class scope | Exact candidate | Native result | Original images |
| --- | --- | --- | --- |
| TravelPlanFlowTest, run[40627465253](https://github.com/omeyerrato/SleepWell/actions/runs/40627465253) | 2f0e6578d13f9608f69b560b725051f5272c8d2a | 1 existing method PASS41.859s | 11 in passing-plan/ |
| TravelCueFlowTest, run[40626264781](https://github.com/omeyerrato/SleepWell/actions/runs/40626264781) | 3ed244d9b49b30fa7ef74b70cada99c59b27a4f5 | 2 existing methods PASS54.505s | 26 in[attempt2](../2026-10-28-travel-reminders-attempt2/README.md) |

COMPOSITE-37.json maps every literal original to its actual passing class, run, commit, path and SHA256. It never re-labels a failed job. This archive preserves **all47 raw files** from40627465253 without changes: passing-plan/ has the11 Plan PNGs and exact class log; failed-cue/ retains the other method/class evidence including failure PNG/XML/original stack; run/ retains environment, original failed combined summary and all guest diagnostics. All34 emitted PNGs were opened individually. SHA256SUMS covers all47 raw files. Earlier failed full attempts are retained in[attempt1](../2026-10-28-travel-reminders-attempt1/README.md) and[attempt2](../2026-10-28-travel-reminders-attempt2/README.md).

The latest job's first Cue method failed at Review & arm because accessibility UiObject.click returnedfalse after navigation had already succeeded: its original failure PNG/XML show the actual Review light cues Activity and Not armed state. Four Cue originals were not emitted: review-from-menu and the three new tracking-reminder frames. Its second existing method passed; total Cue class duration47.426s includes the first failure and is not a class pass. New-reminder runtime proof comes from the preceding54.505s two-method pass, with no source change between candidates. Offline native-only correction1b85a2e6 replaces this acknowledgement with exactly one screen-coordinate input and the focused Review Activity postcondition. That correction has targeted javac proof only and was not replayed in CI.

## Source and ownership comparison

Both passing candidates have app/src/main tree **0e11bef2d0cee4925f29915438f8397addba7283**. The intervening2f0e6578 changes only the Plan native helper: actual getLocationOnScreen bounds and owned dialog detach/focus-return after one Use/Cancel input. SOURCE-EQUIVALENCE.json pins source/test ownership. Native hashes are recorded per passing class in COMPOSITE-37.json. Production source remains ddc9217e +83379ec4; existing new-reminder native extension is978ea10c. Root integration/source compilation is separate from these exact candidate captures.

Compared with prior UI-only d09bb7d7/34-frame proof, the new runtime adds an independent default-OFF reminder preference, future-window validation, separate immutable private receiver/identity, consumed receipt and final permission/revision/window/manual-token/lifecycle checks. The production travelManual and armTravel methods, TravelCues/controller/runtime/session and PhoneTorches are unchanged. TrackingService's whole file is not equivalent: pause, stop and non-ACTIVE sonar callbacks gain reminder invalidation/dismiss hooks. The prior UI-only pass is not used as new-runtime proof.

Plan's passing method verifies real Travel UI/zone search/date and DST calculation, actual draft time/day edits/recreation/Save/reopen, injected daylight delivery with durable consumption, duplicate rejection, scheduling-error/retry and disabling/cancellation. The new reminder stays OFF throughout this independent daylight journey. Cue's passing method verifies the actual new switch/draft/recreation/Save/reopen plus production reminder logic through private clock/eligible-or-absent-owner/scheduler/notification ports: receipt before output, duplicate/rollback/late/stale rejection, generation loss between consumption/publication, permission recheck and disabling. Its eligible fake notification records one call and zero torch arm/open calls. The other Cue method explicitly arms only its existing fake controller. These injected states do not establish end-to-end actual TrackingService pause/automatic sensor transitions or actual Android notification timing.

## Reference review and limits

All composite37 originals were inspected individually against Board09/right. Main controls and complete Save fit; 48dp targets/no-scroll default-fit assertions passed in Cue. Real IANA zone names and DST-dependent2h-current/3h-departure offsets intentionally remain truthful. The actual independent tracking-reminder switch now follows the reference's functional intent; its separate details page remains readable, though the cue-window value wraps PM onto a second line. The Plan failure/retry screen preserves the complete error/Retry/Save; About requires normal scrolling in that longer state. No blocking production visual regression was found. Decorative landmark/flag art and the plane/dotted connector remain simplified. Alternate font scales/devices and all36-reference acceptance are not claimed.

Both optional output flags werefalse, host audio disabled, on the disposable API36 emulator. No physical phone, real sensor/recording, flashlight, audio, OS notification/scheduler/boot behavior or clinical effect was exercised. Inexact delivery over60seconds late is skipped; starting/resuming inside a window never backfills. Process death may leave an already-posted notification until Android dismissal or window-end timeout. Travel remains excluded from typed profilev1/v2 and records/alarms backup14; explicit manual flashlight arming is preserved.

## Composite original index

| Original | Passing class run |
| --- | --- |
| [travel-plan.png](passing-plan/travel-plan.png) | TravelPlanFlowTest / 40627465253 |
| [travel-zones.png](passing-plan/travel-zones.png) | TravelPlanFlowTest / 40627465253 |
| [travel-date.png](passing-plan/travel-date.png) | TravelPlanFlowTest / 40627465253 |
| [travel-reminders.png](passing-plan/travel-reminders.png) | TravelPlanFlowTest / 40627465253 |
| [travel-reopened.png](passing-plan/travel-reopened.png) | TravelPlanFlowTest / 40627465253 |
| [travel-error.png](passing-plan/travel-error.png) | TravelPlanFlowTest / 40627465253 |
| [travel-disabled.png](passing-plan/travel-disabled.png) | TravelPlanFlowTest / 40627465253 |
| [travel-schedules.png](passing-plan/travel-schedules.png) | TravelPlanFlowTest / 40627465253 |
| [travel-reminders-recreated.png](passing-plan/travel-reminders-recreated.png) | TravelPlanFlowTest / 40627465253 |
| [travel-reminders-reopened.png](passing-plan/travel-reminders-reopened.png) | TravelPlanFlowTest / 40627465253 |
| [travel-disabled-main.png](passing-plan/travel-disabled-main.png) | TravelPlanFlowTest / 40627465253 |
| [travel-cues-settings-off.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-settings-off.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-window.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-window.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-date.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-date.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-time.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-time.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-window-recreated.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-window-recreated.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-settings-enabled.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-settings-enabled.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-settings-reopened.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-settings-reopened.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-invalid-window.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-invalid-window.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-no-manual.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-no-manual.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-ready.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-ready.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-active.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-active.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-active-recreated.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-active-recreated.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-stopped.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-stopped.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-stopped-reopened.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-stopped-reopened.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-output-error.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-output-error.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-interrupted.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-interrupted.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-light-settings.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-light-settings.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-light-settings-recreated.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-light-settings-recreated.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-review-from-menu.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-review-from-menu.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-about-recreated.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-about-recreated.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-tracking-settings.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-tracking-settings.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-tracking-settings-recreated.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-tracking-settings-recreated.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-about-main-recreated.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-about-main-recreated.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-tracking-reminder-draft.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-tracking-reminder-draft.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-tracking-reminder-saved.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-tracking-reminder-saved.png) | TravelCueFlowTest / 40626264781 |
| [travel-cues-tracking-reminder-details.png](../2026-10-28-travel-reminders-attempt2/originals/travel-cues-tracking-reminder-details.png) | TravelCueFlowTest / 40626264781 |
