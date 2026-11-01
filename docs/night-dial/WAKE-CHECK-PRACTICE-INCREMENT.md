# Wake-check Practice — source increment, native acceptance pending

2026-11-01. Design07D explicitly requires a non-alarm Preview. Based on
`ce5ffeb0`, the two Wake-up check delay settings in `AlarmEditorActivity` now
provide Preview wake-up check. They pass only the draft's delay to a private,
nonexported `WakeCheckPracticeActivity`; no saved alarm or runtime nonce is used.

The Practice owner clearly labels its static two-minute example, the chosen draft
delay (including Off), and the absence of a running check or alarm restart. The
acknowledgement changes only its own recreation Bundle. Return and Android Back
finish this owner and leave the invoking settings draft unchanged. Preview does
not save settings, enable an Off check, confirm an actual check or start a timer.
The complete draft and Snooze route already use the editor's saved-state owner.
Future-default editing uses the same draft-only preview and retains the existing
explicit defaults review/save boundary.

The ring was extracted unchanged from `WakeCheckActivity` into the pure
`WakeCheckRing` View and reused here. The real Activity still owns its original
clock, engine, countdown and acknowledgement. The Practice Activity never opens
`WakeChecks`, calls `AlarmService`, changes ringing preferences, schedules,
claims, cancels, posts a notification or produces an output. It uses the shared
Night page/insets and fixed footer; acknowledgement requires its focused resumed
window. Invalid draft delay or URI/ClipData/selector inputs close the private page.

The [finite native plan](WAKE-CHECK-PRACTICE-NATIVE-PLAN.json) names one new,
meaningful method in the existing `WakeCheckFlowTest` and requires six actual
original frames plus a completion proof. It uses that fixture's private database,
prefs and inert scheduling boundaries. Actual picker, Preview, acknowledgement,
Return and recreation interactions cover an unsaved seven-minute draft and Off.
An engine/clock factory spy throws if opened. Saved records/alarms/preferences
must remain unchanged. The existing actual-runtime tests and their assertions
are preserved; this method does not execute them or claim their acceptance.

Local validation is limited to diff, manifest XML, finite plan and source
consistency. No build, native run, hosted cohort, unit campaign, phone action,
alarm, audio output or model evaluation ran here. Functional/Night acceptance is
pending integration. The historical source66 acceptance register is unchanged.
