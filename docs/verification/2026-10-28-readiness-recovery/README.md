# Alarm protection and microphone recovery: bounded native evidence

[CI 40587737701](https://github.com/omeyerrato/SleepWell/actions/runs/40587737701), source `9f22f2b3f83d9b22625e2fb6e8bd3f40591a64e3`: **AlarmProtectionFlowTest#compactListAndGlobalProtectionsPersistWithoutAlarmExecution passed in 35.776s, one method, 13 original PNGs.** The raw method transcript reports method status 0 and `OK (1 test)`; the unchanged focused summary reports no failures. This archive covers that method only.

API 36 disposable x86_64 emulator, 1080×2316 override, 420 dpi; host output disabled. PNGs, method log, environment and summary are byte-for-byte copies of the downloaded artifact. SHA256SUMS covers every retained file except itself. `source-equivalence.json` records exact Git blobs and SHA-256 of 12 scoped source files, equality with archive base `accd9b37` and root at archive time, plus hashes of both compared reference boards. It establishes listed-source equivalence, not APK or whole-app equivalence.

## Every original inspected

All 13 originals were opened individually. Alarm list/editor were compared with [board 02](../../../design-concepts/2026-10-22-phone-app/02-alarms.png); readiness with [board 12 left](../../../design-concepts/2026-10-22-phone-app/12-privacy-and-appearance.png). The later user requirement for a compact list in its own Alarms tab takes precedence over board 02's expanded rows and three-tab dock.

| Original | Observed result |
| --- | --- |
| [alarms](protection-alarms.png) | Two private enabled fixtures, clear time/name/days and edit chevrons, Skip rows, full nap/add actions and selected Alarms tab. Smart-wake/challenge details stay in the editor. |
| [editor](protection-editor.png) | Main time/day/name/settings hierarchy, Schedule & dates and More alarm settings routes; enabled Alarm on and full Save visible. Time picker differs from the board's arrow controls. |
| [off](protection-off.png) | Both protection choices off, global scope and access prerequisite explicit; complete Done. Uninstall protection wraps beside its long summary. |
| [disclosure](protection-disclosure.png) | Readable optional-guard explanation with complete Open settings/Later actions; runtime uses Later. |
| [on](protection-on.png) | Both private choices on; accessibility access still requires Set up. |
| [recreated](protection-recreated.png) | Both choices retained after recreation, without another disclosure; Done remains visible. |
| [global](protection-global.png) | Global Alarm options entry observes both previously selected choices. |
| [power-only](protection-power-only.png) | Leaving-app choice off, power-menu choice on; access still ungranted. |
| [readiness](protection-readiness.png) | All five state rows and full battery action fit. Exact alarms, notifications and battery need attention; full-screen allowed; microphone optional. |
| [rationale](protection-readiness-rationale.png) | Actual app-owned microphone rationale, readable explanation and complete Continue/Not now actions. |
| [rationale-cancel](protection-readiness-rationale-cancel.png) | Rationale dismissed; readiness remains and microphone stays optional. |
| [settings-recovery](protection-readiness-settings-recovery.png) | Permanently-denied recovery copy points to app settings; other state rows unchanged. |
| [returned](protection-readiness-returned.png) | Recovery copy and state hierarchy remain after App info Back and Activity recreation. Original bytes happen to equal the preceding recovery image. |

Readiness follows board 12's heading, explanatory copy, ordered icon/state rows and main battery action. Its statuses reflect Android rather than copying the board's Allowed examples. Icons/proportions differ; additional Samsung guidance and a duration-goal card extend below the main hierarchy. The goal card continues below the viewport. The board's quiet-alarm action is absent from these captured readiness frames and is not verified here. Protection pages are supporting UI beyond those two boards. This is not pixel-exact or 36-screen acceptance.

## What actually ran

The pinned test source has no skipped permission branch. Its passing transcript supports the following executed assertions; OS denial/App info screens themselves were not separately captured:

1. Open the private alarm editor, then **More alarm settings → Alarm protection**. Select each private protection preference, dismiss each disclosure with Later, recreate and verify retention. Return through both Done actions, cancel the unsaved editor with Back, then reach the same choices via **Alarms → Alarm options**. Accessibility access remains off.
2. Open **Settings → Reliability**. Compare all five displayed states with current Android APIs; require each initial row and battery action to have complete 48dp bounds. Captures wait for the exact focused native window's committed frame.
3. Starting with microphone denied and no readiness request marker, tap Microphone and the exact OS `com.android.permissioncontroller:id/permission_deny_button`, constrained to the resolved controller package. Require its focused window ID, enabled/clickable full bounds and one tap. Verify denied access and Android rationale=true.
4. Reopen Microphone, capture the distinct focused app-owned rationale, tap **Not now**, and verify denied access/rationale=true with no controller permission message reopened.
5. Reopen, **Continue**, then exact OS `permission_deny_and_dont_ask_again_button` under the same package/focus/bounds constraints. Verify denied access/rationale=false and the Settings recovery hint.
6. Tap Microphone, assert the resolved Android Settings package and app label, verify access remains denied, press **Back once**, and verify the original focused owner. Recreate MainActivity, require the old owner destroyed, verify the same denied/recovery state, then use in-app Back to Settings.

Both OS Deny taps are real disposable-emulator actions, not synthetic results. No Allow, grant/revoke command or permission toggle runs. The emulator's denial/user-fixed history changes and is intentionally not reset; microphone stays denied. The application request marker's original presence/value is restored exactly; the complete original shared-preference map is checked unchanged.

Records and protection settings use unique private fixtures. Enabled alarm rows are display-only: no alarm toggle, editor Save, schedule or alarm runtime is invoked. Exact alarm rows remain unchanged, sessions/epochs remain empty, and alarm/tracking/ringing/access stay inactive. All owned Activities must close and HomeSurfaces drain before factories are restored and private fixtures removed.

No actual alarm, accessibility enforcement, power menu, microphone capture, sound, haptic, sensor, provider, personal-phone or hardware reliability behavior is established. Other permission grants/revocations, other Android/OEM controllers, large text/RTL, other menus and broad IPC/security acceptance remain outside this single method. Archiving ran no build, test, CI dispatch, device or output action.
