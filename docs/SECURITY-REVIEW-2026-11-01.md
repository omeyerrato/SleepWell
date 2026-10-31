# Component and destructive-consent review — 1 November 2026

Base reviewed: `9843587f` (Night Dial feature integration). This is a scoped source
review and remediation record, not a claim that every app, provider, device or
network security property has been accepted. Native Java Views, existing data,
permissions, storage formats and scheduling authority are preserved.

## Concrete finding and remediation

**Destructive confirmation windows lacked the app's overlay touch boundary.**
`AlarmAccessActivity`, `RecordAccessActivity`, `IncomingAutomationActivity` and
`SleepSummaryActivity` already use `SensitiveReviewTouches`. In contrast,
Backup's replacement confirmation, full-history erase/retry and recording-only
deletion created ordinary `Dialog` windows. The portable-settings apply/recovery
Activity also lacked the boundary.

A third-party app with overlay capability could obscure the explanation while
leaving the destructive button exposed. Android's [official tapjacking
guidance](https://developer.android.com/privacy-and-security/risks/tapjacking)
states that partial occlusion has no default protection; Android 12's default
full-occlusion protection is not sufficient for that case. This is a local
consent risk with an overlay prerequisite, not a remotely demonstrated attack.

`SensitiveReviewDialog` now applies the existing gesture guard at each native
dialog window's dispatch boundary. The three named confirmations use it, and
`SettingsProfileActivity` applies the same guard to its actual Activity dispatch.
Both full and partial obscuration cancel an in-progress gesture. A later clean
UP cannot become consent; the user must begin a fresh unobscured gesture.

The confirmation identity, snapshot/revision checks, asynchronous worker ownership,
Cancel behavior and final commit callbacks are unchanged. No screenshot blocking,
overlay-hiding permission, new request, styling change or sensor/output action is
introduced. Unobscured accessibility actions remain available. This does not claim
to distinguish legitimate and hostile privileged accessibility services.

## Source boundaries inspected

| Boundary | Observed implementation and disposition |
| --- | --- |
| Manifest exports | Eight Activities, one Activity alias, two services, four receivers and two providers are explicitly exported. Remaining declared Activities/services are private. Public rationale/configuration/navigation entry points are not made private simply to reduce this count. |
| Alarm/timer requests | Standard `SET_ALARM` permission; scalar validation reconstructs explicit internal drafts, rejects incoming data/ClipData and custom ringtone input, ignores skip-UI requests, and requires the normal later Save/Start. No foreign nested Intent is launched. |
| Home navigation | `HomeLaunch` allowlists screen names and creates new explicit internal targets. Its owned background draft is generation-checked; deferred routing requires a resumed, focused window. `onNewIntent` reparses a new request rather than reusing a prior target. |
| Incoming automation | Parsed requests are reviewed, expire, pin the recording identity and recheck visible/unlocked state. A reused request window is rejected. It does not forward an incoming service Intent. |
| Alarm/record providers | Signature read/write permissions and scoped `/shared/v1/...` URI grants coexist with private UID/current-signer/boot/revision admission. Calling UID/PID are captured before clearing Binder identity. Unsupported raw SQL, RPC, files and batch mutations are rejected. No permission or caller check is relaxed. |
| Summary sharing | Public picker requires explicit Share, temporary read access and a bounded reviewed summary; private file provider checks scope, age, size and canonical location and rejects writes. Original records, notes and audio are not exposed by this summary route. |
| Pending intents | Scheduling/control paths use explicit immutable PendingIntents. The intentional Google Sleep callback is mutable because GMS supplies classification extras; it targets the private explicit receiver with package/action/token identity and freshness checks. No implicit mutable token was found in this scoped source search. |
| System services/receivers | Job, accessibility and admin entry points declare their platform binding permissions. Boot receivers allowlist their expected system event names; private command receivers remain nonexported. This review did not attempt actual hostile-app broadcast delivery on every Android version. |
| Document/import boundaries | Backup input/output requires Android content URI selection, bounded bytes/depth and schema validation. Settings profiles use content URIs, a bounded typed whitelist, review/fingerprint/generation admission and recovery journal. Audio archives use bounded counts/bytes, canonical clip paths and staged snapshots. The review did not exercise a malicious external DocumentsProvider. |
| Permission recovery | Contactless setup rechecks capabilities on return/focus and before Save, cancels capture qualification when focus is lost, and exposes microphone recovery. Calendar denial/settings-return/revocation already has a named native journey. This source inspection is not fresh hardware permission acceptance for every feature. |

`onCreate` handles first-instance input. `onNewIntent` can deliver another input
to an existing instance (for example with singleTop-style reuse), so its parsing,
stale-operation retirement and admission rules matter as much as first creation.
Private alarm presentation retains its own singleTask runtime authority; none of
this work changes that service/alarm contract.

## Bounded verification to run on the merged revision

The existing
`InteractionSecurityNativeTest#obscuredNativeGesturesCancelAndRequireAFreshCleanDown`
now also creates both actual `HistoryEraseConfirm` production windows with inert
callbacks. For full and partial obscuration it dispatches native MotionEvents at
the real button: obscured DOWN/clean UP and clean DOWN/obscured UP/clean UP must
not confirm; a fresh clean gesture must confirm exactly once. The actual guarded
Dialog class is required. It does not delete records, start capture or play output.

This is synthesized occlusion-flag coverage in real native windows, not a claim
that a malicious overlay app was installed or that platform gesture injection was
tested. It extends an existing native method; no unit-test campaign or new model
evaluation is introduced.

The existing `interaction-security` CI scope selects the two native security
methods and the calendar permission recovery method. Current Backup/full-history
and recording-only journeys remain appropriate follow-up checks for normal clean
gestures and data preservation. Root owns builds and emulator runs; this commit's
local checks are source/diff checks only. Passing screenshots or compilation alone
must not be recorded as the security result.

## Remaining limits

Actual hostile-app/provider exercises, all-version permission handoffs, network
transport/credential analysis and complete accessibility combinations are separate
acceptance work. Physical alarm/capture/flashlight/haptic tests and overnight
accuracy are unchanged and are not authorized by this source review. The full
checklist remains subject to those separately recorded limits.
