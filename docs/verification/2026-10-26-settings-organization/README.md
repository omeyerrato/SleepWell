# Settings organization and action feedback — 26 October 2026

[CI40356841938](https://github.com/omeyerrato/SleepWell/actions/runs/40356841938)
passes one focused native flow with ten actual Android screenshots at957f3380.
Its only differences from production/APK sourcef0c1a274 are test observation/CI setup;
production source is identical. The locally built app and UI-check APKs pass in18s,
with the original signer, sleep schema17 and backup metadata13.

## Actual behavior exercised

Eight Settings groups preserve the former17 root destinations. The flow opens
Sleep, Sound, Connected apps and Appearance, returns through actual controls,
finds flashlight through settings search, switches to saved-data search, and
restores the settings query/category/scope after Activity recreation. A genuine
Touch feedback switch change survives recreation and reopen. Alarm schedule,
input and backup subpages open; input settings return to the correct Snooze
parent after recreation. An orientation change stays a draft until Save, then
persists across reopen. All other alarm fields and its complete routine match;
only one fake scheduling boundary is invoked. Unrelated preferences/sibling data
are retained and the original feedback preference is restored during cleanup.

This is an explicitly labelled disposable fixture on a host-muted API36 emulator.
No personal phone data, real scheduling, haptic actuator, alarm/audio, microphone,
sensor or flashlight action is involved. No unit/lint or broad test batch ran.
Physical tactile strength, every custom control, process-death/large-font cases,
all74 search destinations and the complete36-screen design remain unverified.

## Visual comparison

All ten original captures were visually opened, with boards02/03/11/12 as the
style references. The user's later menu-organization request changes the information
structure; the cream/chocolate Inter palette, row rhythm, icons, controls and
floating grouped navigation remain. Four navigation destinations keep the user's
explicit dedicated Alarms-tab request. This is not a claim of pixel-perfect parity.

| Actual capture | Observation and scope |
| --- | --- |
| settings-root | Eight groups and search replace the long root list. Titles/subtitles/chevrons align, selected Settings sits within its dock segment; the last row needs ordinary vertical scrolling. Connected apps uses the existing plug icon. |
| settings-sleep | Related tracking, plan, guard, diary, rhythm and travel destinations share a single list with a visible Back and parent label. |
| search-flashlight | Scope, query, selected category and the truthful Choose an alarm location are visible; categories scroll horizontally. The CI/private banner is fixture-only. |
| feedback-off | Persisted off state, parent path and theme link are visible. No physical feedback strength is established by a screenshot. |
| alarm-editor | Large time/day selectors and ordinary options retain board02's hierarchy. Supporting settings remain scrollable above the fixed enabled/Save footer rather than crowding one viewport. |
| alarm-schedule | Descriptions sit beneath labels, preserving readable title width; Time basis remains a short trailing value. |
| alarm-options | Buttons & orientation and New alarm defaults now have full readable labels with supporting text beneath; the prior narrow-column wrapping is corrected. |
| controls-recreated | On state and Snooze parent survive recreation, with stable switch and value alignment. |
| alarm-backup | Backup, delay, duration, wake check and timeout are grouped, with full-width Done. It preserves existing behavior rather than inventing reference data. |
| alarm-reopened | The saved orientation setting and More settings parent are visible after reopening. |

[Earlier passing and failed attempts](earlier-attempts.md) remain separate; they
are not substituted for this final run. [Organization map](../../UI-ORGANIZATION.md)
and [feedback contract](../../INTERACTION-FEEDBACK.md) document the full source scope.

## Published candidate

[Development prerelease](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-ui-20261026-f0c1a27): SleepWell-f0c1a27.apk,215737636bytes,
SHA256 `129a877a8d393b42d662fbaf933bc3022b2536ef2346309d7267a786f4983386`.
GitHub full source target, draft=false/prerelease=true, uploaded states, APK size/digest
and checksum-sidecar digest match the frozen original-signed local files. This
candidate is not installed; last phone source remains2cbfe043. CI-only absent
inactive boot defaults are established before its exact preference baseline;
existing values and all baseline key/value preservation assertions remain intact.
