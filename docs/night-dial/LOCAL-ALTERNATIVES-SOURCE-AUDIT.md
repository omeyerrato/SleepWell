# Finite local alternatives audit — 1 November 2026

This review starts from `8ffe1c1c` and records the four bounded changes through `2db7bbce5992fe2cf808257a9b9d14127251abb4`. It does not change the historical [state register](STATE-ACCEPTANCE.json), whose 63 external/physical prerequisite states and three unsupported states map to 34 implementation owners. The [source inventory](LOCAL-ALTERNATIVES-SOURCE-AUDIT.json) pins all 66 IDs and current owner blobs/hashes. Source review is not successful device/provider/model execution.

## Concrete gaps repaired

| Registry | Inspected missing local action | Implemented owner boundary |
| --- | --- | --- |
| 19C | No installed voice → system voice settings had no action. | `NarrationVoiceActivity.openVoiceSettings`: explicit Android TTS settings handoff to an exported system Activity; no speech/download starts. Missing system pane is actionable error, retained over recreation. Return refreshes installed inventory; choosing/previewing a voice remains explicit. |
| 22C | Weak/unavailable contactless setup had no explicit phone movement choice. | `ContactlessSetupActivity.chooseMovement/completeMovement`: cancel own probe, wait for confirmed worker/lease release, then return a typed movement selection to the existing Tracking setup. BLOCKED cleanup cannot navigate. Existing start/save boundary, optional sound draft and saved sonar profile remain separate. Standalone entry opens Tracking setup only. |
| 34D | Missing calendar app offered no manual export alternative. | `CalendarEventExportActivity.showManualExport`: review retained original CSV only when the actual source exists; otherwise explicit all-record CSV overview disclosure → existing Backup owner. No automatic picker/export. The all-record route is **not** a selected effective-record export and includes all records/notes/tags/ratings/source details as disclosed. Calendar note choice and record identity remain retained. |
| 31C (parent's additional finding) | Pre-travel sleep schedule could create only a new dated alarm, with no Apply once to an existing occurrence. | `TripSleepActivity.prepareShift/chooseShift` → `TripAlarmShift` → ordinary `AlarmEditorActivity` → existing `NextAlarmOverrideActivity`. Use edits draft only. Keep current returns to Trip. **Save alarm** remains the sole scheduling boundary. The new dated draft feature stays available. |

Implementation commits: `d0a8792d`, `c8ad4ac6`, `2ffe5e14`, `ecc4fa39`, `71ec591e`, `b3c30823`, `2db7bbce`. The Calendar fixture follow-up matches its actual complete Switch content description; it does not relax the input-role/checked-state assertions.

## Travel identity and persistence

Each offered recurring occurrence is tied to the whole saved alarm JSON, its current source occurrence and consumed-occurrence marker, the phone time zone, the exact saved Trip sleep JSON and current Trip revision. Unsaved sleep-plan edits are not silently applied. Existing skip/override, one-time/naps, invalid schedules, expired/ambiguous targets and targets at or after the following ordinary occurrence are not admitted. The existing review's 366-day limit also applies.

Chooser recreation rereads actual saved alarms and restores review scroll; it does not choose an alarm. The occurrence review retains its request nonce and draft identity across recreation. Trip changes, alarm changes, zone/clock/DST changes and occurrence advancement are rechecked at selection, review, Use and Save. The final read-only admission repeats inside the existing `AlarmSkips` SQLite transaction before its unchanged prepare/commit/scheduling sequence; other callers use the original overload. Scheduling failure continues through the existing saved-error reconciliation path. No Trip operation directly rewrites/schedules an alarm.

## Remaining finite owners

The remaining source review checked the existing local controls, not the external success cases:

- Guard/protection/uninstall/widget/shortcut owners expose their Android handoff or launcher availability states; return paths read current status and opening a preview does not add an object or claim access.
- QR/NFC, camera/audio/motion practice and active alarm owners keep their existing admitted input, explicit retry, Math/cancel and cleanup boundaries. The separate 16C emergency-options repair belongs to the sound agent; this audit does not certify physical recognition, alarm output or completion.
- Tracking/audio/automatic/calibration/pairing/room/snoring/dream owners keep explicit setup/preview/Start/Stop boundaries and retained configuration. The new movement alternative does not label echo periodicity as breathing, apnea or stages.
- Calendar, Health Connect, Smart Light, incoming automation, Backup and folder backup owners retain their current permission/consent/review/Cancel/retry and separate read/write scopes. A system/provider/account success is not inferred from a local view. The manual Calendar alternative does not acquire new serialization authority.

No further concrete **unowned** local repair was demonstrated in those inspected controls. This is a finite source finding, not a blanket functionality PASS. Root/other agents own already identified Report, security/radio, Nap, emergency and rhythm work.

## Three unsupported states remain distinct

- **19D External-player media fade:** the actual source already discloses API33 support and eligible recording limits, allows Save Off on unsupported versions, keeps consent, revision/reload/error and explicit current-fade Stop. It does not guarantee an unobservable audio-focus transition. No fake successful external-player control was added.
- **23C Physiological experiment:** current Room conditions implements actual light/RMS/dimming only. No supported trained/capture contract for the physiological experiment was established. A fabricated Start, periodicity-as-breathing relabel, stage ribbon or diagnostic outcome would not satisfy this state. This portion remains missing/unsupported; no new model work or external proof demand was introduced.
- **28D Timing evidence:** the inspected base preserves unknown chronotype/insufficient history and honest self-report separation. Its required evidence → contributing observed nights route was reported to root and explicitly handed to the sound agent, who owns newer SleepRhythm source. Unknown copy alone is not accepted as that navigation action. No chronotype fiction is proposed.

## Exact native plan and limits

[LOCAL-ALTERNATIVES-NATIVE-PLAN.json](LOCAL-ALTERNATIVES-NATIVE-PLAN.json) names the actual classes/methods and all 81 emitted success originals: existing `NightDialBreadthTest` (47), Calendar (8), Contactless (11), and the one specifically authorized new `TripOccurrenceFlowTest` method (15). There is no `NightDialBreadthFlowTest` class. Twenty-six frames cover the additions; the other required originals preserve the selected methods' existing capture obligations.

The Trip method uses tomorrow derived from a pinned actual System clock/current zone, private SQLite and private Trip/TripSleep/Calendar preferences; no stale-date guard is weakened. It exercises actual chooser/Keep/review/Use/Save actions, three recreation boundaries, inactive retired Use, stale saved-plan rejection at final Save, stale whole-alarm rejection, DB-before-injected-schedule ordering and exact ordinary recurrence/configuration preservation. A narrow TripSleepStore factory defaults to its unchanged production preference filename; native teardown waits for owned readers before deleting private stores.

The narration method injects only an unavailable system-pane launcher; real Android settings and installation/voice preview are not proved. Calendar uses a fake launcher and opens only the existing local CSV overview; no document/provider write. Contactless uses injected PCM/capability/permission ports and confirms actual runtime teardown, not a physical sonar result. Trip injects scheduling and an inert wake-check owner; it never emits an alarm.

No build, native run, screenshots, unit campaign, phone action, scientific inference or download was performed for these commits. Root owns compilation and the focused native run, followed by inspection of the original images. Current status is **source implemented, native pending**, not complete app/state/physical acceptance.
