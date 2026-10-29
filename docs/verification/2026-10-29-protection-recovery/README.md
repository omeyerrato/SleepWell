# Alarm protection: saved choices and access recovery

[CI40790536650](https://github.com/omeyerrato/SleepWell/actions/runs/40790536650)
passed at `1fdaf3544a3f68e36c7887bce6fd5d46d5e569be`: **35.916 seconds,
one existing method / all16 required original PNGs**. The unchanged
[transcript](AlarmProtectionFlowTest%23compactListAndGlobalProtectionsPersistWithoutAlarmExecution.txt)
reports status0 and `OK (1 test)`. The [raw summary](focused-review-summary.json)
has no failures and `full_app_review: false`; [run.json](run.json) records both
successful APK builds and authoritative head.

Exact selector: `AlarmProtectionFlowTest#compactListAndGlobalProtectionsPersistWithoutAlarmExecution`.
Scope `protection-recovery`; `native_sounds=false`, `audio_imports=false`.
Disposable API36 x86_64 renderer,1080×2316 override,density420, script-set font
scale1.0/12-hour clock. The [environment](environment.json) reports
`system_broadcast_idle: false`; three real startup focus samples and the final
native ownership/preferences/runtime checks completed. No other test method ran.

## Individual reference inspection

Every original below was opened individually. Alarm readiness was compared with
[Board12 left](../../../design-concepts/2026-10-22-phone-app/12-privacy-and-appearance.png),
the list/editor with [Board02](../../../design-concepts/2026-10-22-phone-app/02-alarms.png).
Protection itself is supporting UI without a separate direct mockup. It follows
Board12's distinction between current prerequisites and saved choices, with one
clear setup action. Both choices On now coexist with explicit **Setup needed**,
not a claim that the guard is live. Main text is short, Uninstall protection has
a readable stacked explanation and real route, and both footer actions fit.
About/disclosure preserve the full original privacy and device-limit information.

| Original | Individually observed state |
| --- | --- |
| [Alarms](protection-alarms.png) | Two private display-only alarm fixtures; clear time/name/days, edit routes and complete nap/add footer in the user's later compact Alarms tab. No scheduling/toggle admission. |
| [Editor](protection-editor.png) | Actual6:30AM/weekday draft, direct arrows and main routes, truthful may-ring window copy, complete Alarm on/Save. Editor Save is not pressed. |
| [Off](protection-off.png) | Both choices Off; Off plus actual missing accessibility state; complete Setup/Done and unsqueezed Uninstall route. |
| [About recreated](protection-about-recreated.png) | Restored owned About, complete privacy/calls/emergency/forced-restart/manufacturer/phone-off text and fixed Done. |
| [Disclosure](protection-disclosure.png) | Mandatory review before OS settings, all original disclosure and limits, complete Open settings/Later. |
| [Disclosure recreated](protection-disclosure-recreated.png) | Same owned review after recreation; no automatic Settings action. |
| [On](protection-on.png) | Both private saved choices On, explicit Setup needed and saved-choice/access explanation; full footer. |
| [Recreated](protection-recreated.png) | Both choices survive recreation; Setup needed remains and no new disclosure opens. |
| [Global](protection-global.png) | Global Alarm options entry observes both saved choices and the same missing-access status. |
| [Power only](protection-power-only.png) | Leaving-app Off/power-menu On; Setup needed persists. |
| [Read error](protection-read-error.png) | Complete read failure, no partly active switches, full Retry/Done and no reset claim. |
| [Readiness](protection-readiness.png) | Five actual API state rows and full battery action: exact alarms/notifications/battery need attention, full-screen allowed, microphone optional. |
| [Rationale](protection-readiness-rationale.png) | Existing owned optional-microphone rationale with full Continue/Not now. |
| [Rationale cancelled](protection-readiness-rationale-cancel.png) | Main readiness returns with microphone still optional. |
| [Settings recovery](protection-readiness-settings-recovery.png) | Permanently-denied recovery copy points to app settings; no grant inferred. |
| [Returned](protection-readiness-returned.png) | Same recovery state after actual CI App info/Back and framework recreation. |

The later compact list/four-tab user requirement takes precedence over Board02's
expanded rows/three-tab dock. Readiness reflects Android's actual states, not the
board's illustrative Allowed examples. Its unchanged Samsung guidance and duration
goal card continue below the main hierarchy; the goal card extends below the
viewport. The quiet-alarm action is not present in these frames and is not exercised.
This is bounded hierarchy/action acceptance, not pixel identity, all screen sizes,
full36-screen acceptance or proof that an alarm can ring.

Both new information dialogs fit their complete content at this viewport. The
native fixture verifies full content/footer bounds and zero offset without
injecting a scroll gesture, then checks the same offset after recreation.
**Nonzero-scroll restoration remains unverified at runtime.** The source stores
and restores the real offset, and the conditional native branch retains one
actual end gesture only when a measured scroll range exists. No paragraphs,
zoom, cropped viewport or invented content were added to force that branch.

## Completed action and data assertions

The existing single method retains all13 earlier captures/actions and adds only
About recreation, disclosure recreation and read error. It uses an existing
UUID private database and existing AlarmProtections Prefs Factory; no new
production access port exists. Android accessibility stays actually denied.

The passing method completes these assertions:

- Open a private alarm editor→More alarm settings→Alarm protection. Both choices
  start Off and private preferences are empty. About/Done/recreation preserves
  those exact values/key absence.
- Explicitly select leaving-app protection. Its saved choice opens mandatory
  disclosure. Recreate the owned review; a retained destroyed Open-settings
  callback produces zero route hits. One native Open settings tap produces
  exactly one matching action intercepted before its destination opens. The
  selected preference remains saved and access remains denied.
- Explicitly select power-menu protection, use Later, retain both choices across
  recreation and reopen from global Alarm options. Turn leaving-app Off while
  power stays On; status stays Setup needed throughout.
- Inject failure on the second preference read, after the first succeeds.
  The recreated Activity exposes no partly active switch. Retry/Done have full
  enabled48dp bounds. Exact private preferences are unchanged. Restore the
  existing Factory and use actual Retry; both saved values/key absence return.
- One native Uninstall-protection action reaches the actual explicit Activity
  route, intercepted before the admin screen opens. Exactly one hit is required;
  no administrator activation/removal runs.
- The original readiness subsection compares all five rendered states to current
  Android APIs. Actual CI microphone Deny→internal rationale Not now→Continue→
  permanent Deny→App info→Back→recreation executes. It uses exact controller
  resource IDs with the resolved system package, focused window identity and
  complete bounds; no Allow, grant, revoke or settings toggle is scripted.
- Restore the exact original request marker value/key absence and all global
  preference values. Private alarm rows remain equal to their original queried values;
  sessions and epochs remain empty. AlarmService/TrackingService/ringing remain
  inactive and accessibility/microphone remain denied. Finish all owned
  Activities and drain HomeSurfaces before restoring factories/deleting fixtures.

Native protection inputs prefer one exact clickable content description before
text-to-clickable-parent lookup. They preserve enabled/full bounds and one input.
The monitor stays installed through main-thread input completion and observable
route acceptance. Protection restore/capture waits bind to the exact monitored
Activity decor or its owned dialog; About text in the main header cannot satisfy
a dialog wait. Waits return on accepted true. Frames commit in the actual focused
window; no fixed sleep serves as a visual pass.

This is private-choice/fake-read-error UI proof. It does not enable accessibility,
exercise a live guard, admit a real alarm, trigger power-menu actions, validate
uninstall administration or prove calls/emergency behavior on a physical device.
**Access-enabled/live guard/real-alarm behavior remain unverified.** The retained
microphone subsection changes denial history only on the disposable emulator;
its original application request marker is restored and no microphone capture runs.

## Preserved failed attempts and fixture-only corrections

Every failed folder retains the original method/env/summary/failure PNG/XML plus
authoritative run metadata. Full guest/renderer diagnostics and earlier captures
remain private. No failed run counts as completion of later actions.

| Failed run | Exact limit and correction |
| --- | --- |
| [40786188235](failed-40786188235/run.json),26.87s | Incorrect fixture requirement that fitting About content must scroll. Its XML reports scrollable=false. `853e9064` measures content/range and verifies zero/full content without a gesture when it fits; the nonzero branch is not claimed. |
| [40787686648](failed-40787686648/run.json),16.847s | Non-clickable label was selected instead of its separately described Switch; full-bounds/action assertion rejected it before input. `562554c7` selects the exact clickable accessibility action. `699254af` also routes retained Protection captures through the shared owned-window gate. |
| [40789352903](failed-40789352903/run.json),16.979s | Immediate route-count assertion ran before posted click completion and removed the monitor early. Actual disposable Accessibility Settings opened; [selected original guest lines](failed-40789352903/settings-launch-diagnostic.txt) retain this fact. This run does not prove destination blocking or later assertions. `1fdaf354` drains actual input and awaits exact route acceptance while keeping both monitors installed. No settings toggle/grant action was scripted. |

These corrections preserve every action/assertion, all16 names and the original
8-second predicate timeout. They add no input replay, alias, new @Test, production
behavior or scope expansion. Only the final run proves both destinations blocked.

## Source and archive integrity

[source-equivalence.json](source-equivalence.json) records14 exact Git blobs at
the tested head. From `b5d3b2b6bb558b7d0038351770e38d0c32e07988`, only
AlarmProtectionActivity changes in production. AlarmProtections keys/persistence,
BedtimeGuard, PowerMenuGuard, UninstallProtectionActivity, SleepPicker, MainActivity,
Design/RecordUi, manifest and complete guard_description resource remain unchanged.
No equality to a later root checkout or installed/released APK is implied.

Ordered commits: `37f4b231` Activity; `97fc50e5` existing method extension;
`1b0e4775` exact ownership; `58cc1c33` narrow scope; `853e9064` measured range;
`562554c7` exact action; `699254af` retained captures; `1fdaf354` input completion.
All production changes precede the first run; later fixes are native-only.
Both changed files compiled API36/JDK21 release17 and both APKs compiled in CI.
[scope-inventory.json](scope-inventory.json) records preservation of all43 prior
scope mappings/selectors, plus existing constants/defaults and the exact one-method/16-name gate.

The16 successful PNGs, method/env/summary and all failed raw artifacts are
byte-for-byte copies. No image painting, crop, compositing or replacement was used.
[SHA256SUMS](SHA256SUMS) covers every other public archive file. No personal
phone, root checkout, engine/admin policy, user data, release or frozen APK changed.
Broad security and physical reliability remain outside this run.
