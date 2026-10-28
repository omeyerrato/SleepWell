# Calendar draft and consent recovery: bounded native evidence

[CI40607144683](https://github.com/omeyerrato/SleepWell/actions/runs/40607144683), exact source `7ee0b7de9a716c9b4273dd6166dc533421737292`: **CalendarRulesFlowTest#permissionDenialSettingsReturnAndRevocationKeepTheUnsavedDraft passed in 29.271 seconds, one existing method, all six required original PNGs.** The transcript reports method status 0 and `OK (1 test)`; the unchanged focused summary has no failures and `full_app_review: false`. The class's second method was not selected.

The disposable API36 `sdk_gphone64_x86_64` emulator used a 1080×2316 override, density420 and disabled host output, without optional audio/import tests. All six images were opened individually. PNGs, instrumentation transcript, environment and summary are copied byte for byte; [original-sha256.json](original-sha256.json) records all nine originals. Full guest/renderer logs remain private. SHA256SUMS covers every archived file except itself, and local attributes preserve original bytes.

## Individually inspected originals

| Original | Visible result |
| --- | --- |
| [Permission settings](calendar-rules-permission-settings.png) | Calendar rules draft on, denied-state copy and complete Open app permission settings action; preparation/advance rows and full Save remain visible. Two similar access-denied messages appear together; this redundancy is retained in the original. |
| [Accessible draft](calendar-rules-permission-granted-draft.png) | Fake Private calendar list with Skip all-day dates and Earlier for meetings on; full Reload and Save controls. This is injected access, not an Android grant. |
| [Access-lost draft](calendar-rules-permission-revoked-draft.png) | List is hidden and recovery action shown; rules draft remains on and full Save remains visible. Hidden calendar selections are verified by the saved-instance assertions, not inferred from this picture. |
| [Automatic draft recreated](auto-calendar-draft-recreated.png) | Save new recorded nights on and Fixture beta destination retained; shared-field/privacy explanation, Reload, full Save and Cancel fit. Private-fixture/no-provider status is visible. |
| [Consent conflict](auto-calendar-consent-conflict.png) | Newer saved state off / Fixture alpha, full explicit changed-consent warning, Reload, Save and Cancel. The old confirmation is absent. |
| [Private save complete](auto-calendar-saved-private.png) | On / Fixture beta after the successful private CAS. This resembles the unsaved draft visually; committed revision3 and one successful write are established by the method assertions. |

These are native Android frames with focus/frame-commit checks, not reconstructed images. The AutoCalendar inputs also require enabled, fully visible 48dp action bounds. The six captures show the represented normal-text layouts; they do not establish every dialog, font/device variant or reference screen.

## Actions and state assertions that ran

Calendar Rules uses private preferences, a fake source list, fake permission actions and a counting apply port. A throwing request leaves the asked marker unset. An empty result returns from the disabled Waiting state without falsely marking denial. A denied callback with rationale opens the owned rationale; Not now makes no additional request. Continue explicitly requests again. A second injected denial without rationale shows app-settings recovery; that action increments the fake settings counter once without another permission request.

Instrumentation invokes pause/resume on the actual Activity while changing fake access. Available access reveals the private list; the user toggles both calendar rules. Losing access performs no further source-list read, and the saved-instance draft retains enabled plus calendar7 in both holiday/meeting selections. No rules are saved or applied. Restoring fake access reveals both checked controls without another request. These are synthetic lifecycle/access-return cycles, **not an actual background task switch, Android permission controller, settings visit, grant or revocation**.

The same method then closes Calendar Rules and exercises the real Automatic calendar Activity with an Activity-pinned fake Access:

1. Native toggle/destination taps select enabled / Fixture beta. Pause/resume, fake access loss/return and real framework Activity recreation retain this unsaved draft. Access loss shows Reload to verify selected calendar. No save attempt occurs. Actual Back destroys the owner and leaves private config disabled at revision0.
2. A new explicit Save opens review. During a synthetic pause/resume, external private config changes revision0→1. The old dialog detaches; the UI adopts disabled / Fixture alpha with the conflict warning. Deliberately invoking the detached button callback programmatically still dispatches zero saves. This is a stale-callback check, not an ordinary user tap.
3. A separate foreground race changes private config revision1→2 after review. The native Enable input reaches the private save port with **expected revision1**, which rejects it. There are zero commits; config remains revision2.
4. Reload and edit restore the enabled / Fixture beta draft. Confirmation Cancel retains the draft and adds no save attempt. A fresh reviewed Save passes **expected revision2**, performs one successful AtomicReference compare-and-set and commits enabled / calendar8 / revision3. Final page Cancel closes without another commit. Recorded attempted revisions are exactly `[1, 2]`.

The fixture closes owned Activities and drains their readers plus accepted PlanOperations before restoring Access. It verifies no private operation token remains, restores the Calendar Rules factories, deletes its private preferences, requires exact original app-preference equality and leaves alarm/tracking inactive. The passing transcript includes these cleanup assertions.

## Source and evidence boundary

[source-equivalence.json](source-equivalence.json) pins 16 source/workflow blobs and their committed-byte SHA-256 values to the captured verification branch. The implementation order from frozen0.1.2 source `5bf66443` is `a54a6798` (draft/consent fix), `e8ea2c93` (pinned Access seam), `0fd315d2` (same existing native method extension), then `7ee0b7de` (one-method/six-image workflow scope). This archive adds docs only; it does not change or qualify the frozen0.1.2 APK.

The production default Access delegates the existing AutoCalendar config/items/status/resume/save operations and CalendarEventWrites access/list operations. Its Save continues to call the existing production revision/permission/destination validation. The native run replaces those operations with private Config objects, fake calendars/access and a real **private AtomicReference CAS**. It does not execute production preference/storage CAS, calendar provider writes, export queue processing, existing-history export, real OS permission/settings recovery or process-death restoration. There is no standalone Config validator reused here; the fixture checks its fixed destinations and revisions explicitly. This is bounded UI ownership/draft/consent evidence, not provider durability, full-app security, all36-screen or physical-output acceptance.
