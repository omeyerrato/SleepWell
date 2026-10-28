# Travel reminder cleanup: bounded native evidence

[CI 40636332489](https://github.com/omeyerrato/SleepWell/actions/runs/40636332489)
passed at `32d024657093d3cd3ed098e69dff92c4edb18c81`: **39.92 seconds, one
existing method, all 18 required original PNGs**. The
[raw transcript](TravelCueFlowTest%23windowReviewCancelSaveRecreateAndReopenNeverArmAnything.txt)
reports status 0 and `OK (1 test)`; the unchanged
[summary](focused-review-summary.json) has no failures and `full_app_review: false`.
[run.json](run.json) records the authoritative head and successful APK build.

Exact selector: `TravelCueFlowTest#windowReviewCancelSaveRecreateAndReopenNeverArmAnything`.
The new `travel-cue-cleanup` scope excludes TravelPlanFlowTest and the other Cue
method. Both optional-output flags were false; API36 disposable x86_64 emulator,
1080×2316 override, density420. Existing scope manifests/selectors were retained.
No personal device or actual provider/output operation was performed.

## All 18 originals individually inspected

Compared with [Board09 right](../../../design-concepts/2026-10-22-phone-app/09-rhythm-and-naps.png).
The main retains target/home zones, departure date, actual synthetic-clock time
difference, experimental cues, light-source route, tracking-reminder choice,
About and fixed Save. Supplementary pages expose settings and explicit review;
their explanation separates reminders from recording/flashlight arming. No new
core clipping was observed in this configuration. This is not pixel equality or
all-screen acceptance. These images show app UI, not an Android notification.

| Original | Visible state |
| --- | --- |
| [Settings off](travel-cues-settings-off.png) | Full main hierarchy, London/Riyadh, departure27Oct, cues/reminders Off and full Save. Current2h difference and departure-day3h difference are distinguished. |
| [Tracking settings](travel-cues-tracking-settings.png) | Reminder Off, active-manual-only explanation, not-enabled cue window and full Done. |
| [Tracking recreated](travel-cues-tracking-settings-recreated.png) | Same off state and explanation after framework recreation. |
| [Main About recreated](travel-cues-about-main-recreated.png) | Restored owned explanation dialog over main, complete text and Got it. |
| [Window](travel-cues-window.png) | 23Oct,10PM,60min,1night; full explanatory copy, Use/Cancel. |
| [Date](travel-cues-date.png) | Actual October2026 calendar,23 selected, home-zone explanation. |
| [Time](travel-cues-time.png) | Actual owned Cue window start dialog,10:00PM, arrows, AM/PM, Use time/Cancel. |
| [Window recreated](travel-cues-window-recreated.png) | Changed30min draft survives recreation; other window values retained. |
| [Settings enabled](travel-cues-settings-enabled.png) | Cue choice On in main draft, reminders Off, full Save. |
| [Settings reopened](travel-cues-settings-reopened.png) | Saved cue choice On after explicit Save/reopen. |
| [Light settings](travel-cues-light-settings.png) | Source, cue window, reminders Off, actual Review & arm route, About and Done. |
| [Light settings recreated](travel-cues-light-settings-recreated.png) | Same routes and values after recreation. |
| [About recreated](travel-cues-about-recreated.png) | Restored owned explanation over Light-cue settings; full Got it. |
| [Invalid window](travel-cues-invalid-window.png) | Departure-day cue choice rejected; complete error remains above Use and Cancel. |
| [Review from menu](travel-cues-review-from-menu.png) | Actual Review Activity, Not armed, full10–10:30PM window, disabled Arm without manual recording, Refresh status. |
| [Reminder draft](travel-cues-tracking-reminder-draft.png) | Reminder On after recreation while saved choice remains Off; full main/Save. |
| [Reminder saved](travel-cues-tracking-reminder-saved.png) | Reminder On after explicit Save/reopen. |
| [Reminder details](travel-cues-tracking-reminder-details.png) | Restored On choice, next saved reminder/home zone, separate review action and Done. Cue time wraps onto two readable lines. |

## Assertions that completed

The entire method, including its final continuation, passed. The new subsection
models a retained notification item in a **private fake port**, with no prior
local show. `recordingEnded` must cancel it exactly once without changing private
preferences. A synthetic cancellation exception must be contained, retain the
fake item and expose ERROR/NOTICE. A later cleanup retry removes it; exact prior
status values/key absence and the entire private preference map are restored.
No fake notification was newly shown and no recording/light admission occurred
during this cleanup subsection. This verifies the app cleanup API, not Android
process destruction, NotificationManager persistence or actual cancellation.

The retained method configures a real UI draft, cancels time selection, changes
window length, recreates, explicitly saves/reopens and checks invalid dates and
the actual unarmed review route. Cue settings alone must never arm/open output.
Legacy Travel JSON missing the optional reminder boolean reads Off without a
migration write. The reminder draft survives recreation while the saved choice
stays Off; explicit Save persists On. The fake clock is23Oct2026 and manual owner
is synthetic; displayed zones/dates are private fixture choices, not travel data.

Direct calls to the app's reminder delivery API exercise receipt-before-show,
duplicate rejection, consumed-window exclusion after a backwards clock change,
absence of active manual ownership, generation invalidation before publication,
late delivery, revision replacement, immediate fake permission recheck and saved
opt-out. Exactly one fake show remains; no fake torch arm/open occurs. Receiver
intent decoding, real OS permission enforcement and notification rendering are
static-review boundaries, not demonstrated by these API calls.

The private factory/clock/manual-control/notification ports remain installed;
the real hardware adapter fails if constructed. Owned Activities close and
PlanOperations drains before restoration. Exact original app preferences and
inactive services are asserted; private fixture preferences are deleted. The
method does not run the other Cue method's fake light-engine scenario.

## Two real failures retained

[40633953280](https://github.com/omeyerrato/SleepWell/actions/runs/40633953280)
at `dd201da3` failed19.224s during `capture time`: an underlying Activity frame
was registered while the time dialog was taking focus. Six required images
existed. [Raw failure evidence](failed-40633953280/) includes the original
transcript, summary, environment, error, PNG and XML. The new cleanup assertions
had not run. Native-only `747de525` waits the actual owned time-dialog/title
before its committed capture; it changes no timeout or input.

[40635063367](https://github.com/omeyerrato/SleepWell/actions/runs/40635063367)
at `747de525` failed21.325s at `tap 30 min`: the shared tap helper committed a
frame before awaiting its next target, catching another picker focus handoff.
Seven required images existed; cleanup still had not run. Its
[raw evidence](failed-40635063367/) is retained separately. `32d02465` removes
that premature pre-target frame. The helper now resolves the actual focused
target, commits its frame, checks full bounds and injects once. Same-owner,
post-dialog-detach and review-Activity assertions remain; inputs are not retried.
All production sources are identical across the three runs.

[source-audit.json](source-audit.json) pins17 unchanged exported declarations,
17 follow-up source/workflow blobs and the production-tree equivalence.
[Audit findings](../../security/TRAVEL-REMINDER-ENTRYPOINT-REVIEW.md) explain the
bounded stale-notification defect, retained guards and platform-contract inference.
[candidate-commit-order.json](candidate-commit-order.json) records separate
production/native/editor-copy/scope/synchronization commits.
[original-sha256.json](original-sha256.json) pins33 unaltered artifact files;
full guest/renderer logs and redundant failed captures remain private. Original
CRLF failure XML is retained with a local whitespace attribute. SHA256SUMS
covers all retained archive files except itself.

The separate dynamic editor footer copy is **not captured by this method**.
This pass establishes neither full Travel/Plan acceptance nor actual alarm,
recording, microphone, flashlight, haptic/provider behavior, process recovery,
cross-UID authorization, physical-device behavior or whole-app security.
