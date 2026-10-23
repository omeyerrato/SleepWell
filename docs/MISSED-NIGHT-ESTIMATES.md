# Reviewed missing-night phone-idle estimates (F045)

This supporting screen uses board 08's cream background, Inter typography, brown actions, lavender interval chart, real time controls and fixed action footer. It is not a dedicated reference-board screen. Its chart shows actual screen-off/on event endpoints and the user's reviewed interval; it never fabricates sleep stages, movement, sound events or audio for an unrecorded night.

## Actual flow

Open `MissedNightActivity` explicitly. It checks Android Usage Access and reads screen events only for the selected recent night: the previous local noon to the selected day's noon, capped at the current instant. The last three night-ending dates are available because Android's usage-event retention is short and device dependent. Local-zone instants handle DST; a day need not contain 24 hours.

Only a recorded noninteractive-to-interactive event pair is eligible. An unmatched endpoint, zero-length pair or reboot/shutdown crossing produces no interval; regressing timestamps fail closed. A repeated noninteractive event replaces the pending endpoint conservatively. Existing effective record periods, including edited/manual periods and open recordings, exclude overlapping suggestions. The 24 longest remaining intervals are available for review; this bounds UI and saved-state size without synthesizing or merging intervals.

The user can change the Start/End dates and times within the selected night using the existing SleepWell-owned pickers. DST gaps are rejected, and repeated local times require an explicit offset. Opening, checking, editing, cancelling and recreating the screen do not write history. Usage Access is opened only on a deliberate button press, never requested in a loop or from boot. No background job, sensor, microphone, service or alarm is started.

Save reviewed night creates an ordinary existing-contract manual record with the `Phone-idle estimate` tag and an explicit provenance note. Before insertion it rechecks the usage grant and the original real event pair. It checks effective history overlap and request-token idempotence in the same SQLite transaction as `Records.create`. An externally added or moved record therefore blocks a conflicting save. A retained save operation captures the app context, private storage factory and draft, survives Activity recreation, and uses a persisted request token to avoid duplicate creation after process recreation. A request whose record was later deleted fails rather than recreating it. Alarms/preferences are untouched; there is no schema change.

## Limits and primary sources

Phone inactivity does not establish that its owner was asleep. Android's noninteractive state also includes ambient display; a phone left unused can create false estimates. This flow requires user review and is not an automatic sleep classifier or a clinical estimate. The saved record's source is `manual` and the report retains the existing no-measurements behavior. It does not supply personalized sleep stages or replace a recorded night.

[Android UsageStatsManager](https://developer.android.com/reference/android/app/usage/UsageStatsManager) requires explicit PACKAGE_USAGE_STATS access for global events; queryEvents retains events for only a few days and returns null while user storage is locked on Android R+. [UsageEvents.Event](https://developer.android.com/reference/android/app/usage/UsageEvents.Event) defines SCREEN_NON_INTERACTIVE, SCREEN_INTERACTIVE, DEVICE_STARTUP and DEVICE_SHUTDOWN. The platform adapter copies only event type and timestamp, never package names, app activity names or the user's app history. It keeps no persistent event log.

[Sleep as Android automatic tracking documentation](https://sleep.urbandroid.org/docs/sleep/automatic_sleep_tracking.html) describes its broader missed-night estimate using activity/Sleep APIs. SleepWell's screen-event review is narrower and makes no equivalent accuracy claim. Missing events, an unavailable/locked query, denied/revoked Usage Access, existing history and a changed source pair have explicit UI/error states. Availability on a particular phone still requires later permitted validation; no phone query or output was used to develop this feature.

## Tests and integration

Ten pure `MissedNightTest` cases cover exact endpoint pairing, missing boundaries, reboot resets, duplicates, zero-length and out-of-order events, whole-period overlap exclusion, adjacent/open records, edited review bounds, DST length and recent-date selection. Direct javac/JUnit execution passed all ten before Android compilation.

`org.sleepwell.app.MissedNightFlowTest` has two emulator-CI-only methods:

- `reviewEditSaveRecreateAndMissingEvidenceStatesUseOnlyPrivateData`: injected Usage Access/events, private SQLite database, actual owned Start/End controls and cancel, no write before Save, edited-draft recreation, paused save/recreation with pinned factory, Save/reopen, unchanged alarm/preferences, no epochs/clips/sound events, missing endpoint, empty/query-error states and external-overlap rejection.
- `manualSaveIsIdempotentAndRechecksDeletedRequestsOpenRecordsAndChangedEvidence`: real private-database token recovery, deleted-request tombstone, changed event pair and active-record overlap guards.

Expected eleven screenshots: `missed-night-denied`, `missed-night-review`, `missed-night-edited`, `missed-night-recreated`, `missed-night-busy`, `missed-night-saved`, `missed-night-covered`, `missed-night-incomplete`, `missed-night-empty`, `missed-night-error`, `missed-night-conflict`. Native execution and original-image visual acceptance remain pending; compilation alone is not native verification.

Primary navigation hook: `startActivity(RecordUi.intent(this, MissedNightActivity.class))`, ideally from the automatic-tracking setting's Missing-night estimate row and/or history's explicit recovery action. Main/CI hook edits are intentionally left to the integrating agent. Manifest adds only the private Activity and PACKAGE_USAGE_STATS declaration; no exported IPC, boot receiver or new service is added.

Local verification: final scoped Gradle debug APK, Android-test APK, targeted unit test and lint build passed in 33 seconds. MissedNightTest: 10 tests, zero failures/errors (Gradle XML 0.075 seconds); lint: zero errors, 79 existing warnings. The initial PACKAGE_USAGE_STATS ProtectedPermissions diagnostic is suppressed only on that manifest declaration, with the official UsageStatsManager app-op/settings rationale inline; the special access remains denied until the user grants it, and the runtime boundary checks each query. No global lint baseline or permission bypass was added. Native methods are compiled, not executed or visually accepted yet.

Picker-lifecycle follow-up: night selection, inactive-period selection, Start/End date/time and repeated DST-offset sheets each retain their own route across recreation. The native flow explicitly edits minute 45, recreates the still-open End-time sheet, verifies the pending value, and only then applies it. Final incremental debug/native APK plus lint passed in 58 seconds (zero errors, 79 warnings); no model change required repeating the already-passed ten pure cases. Native execution remains pending.

Source-review corrections: a recovered no-epoch session can legitimately have equal start/end. Empty or reversed historical periods no longer poison all suggestions; they contribute no occupied interval and remain untouched. The private storage method now creates a genuine no-epoch recovered row and a reversed legacy row, proves they remain unchanged, and saves an otherwise valid estimate. The UI method also holds an injected query, revokes access while the same Activity resumes, returns the old query payload, and verifies denied state cannot be replaced. Permission loss revokes the query generation before clearing the draft. These additional native regressions still require CI execution.

The two source-review corrections passed incremental debug APK, native APK and lint in 47 seconds (zero lint errors, 79 warnings). The tests that cover Android storage/lifecycle are compiled and still require CI execution; no unchanged model tests were repeated.
