# Persistent build plan

The full BUILD-PROMPT remains the goal. There are 36 intended phone screens;
wearables/watch companions and hardware vitals are excluded. No claim of all-screen final acceptance is made. The first UI pass was rejected by the user for visual fidelity.

## Current execution override — 25 October 2026

The latest user direction is features first, using parallel implementation agents
in separate worktrees. The dedicated design reviewer is stopped. Broad design,
bug and security work is deferred, as are new unit/native/lint/screenshot batches.
Use essential production builds for integration. Do not act on the personal phone
(including installation, ADB queries, capture, microphone, alarm or other outputs).
The quiet-device procedures below are historical, not current authorization.

Current application candidate8399109 is published, signed with the installed debug
key, and has build-only evidence; the personal phone remains on571018b/schema7.
FEATURE-MATRIX.md and REMAINING-WORK.md carry current implementation/acceptance
bounds. The numbered sequence below describes the original full plan and does
not reinstate its old ordering or stopped reviewer.

1. Repair the root compositions against the boards: Tonight, Alarms, Report and Settings (latest user override).
   Correct proportion, dock alignment, icon vocabulary and system-bar continuity.
   Use actual saved records; a short record is not an overnight fixture.
2. Finish the alarm vertical flow and its supporting settings. Preserve SQLite
   data and drafts; require real persisted scheduling/challenge evidence and
   screenshot comparisons. Pending: multi-task routine, QR, walking, configured
   fallbacks, full wake-check sequence, smart sensitivity/goal controls, sound
   and gentle/snooze layouts, skip occurrence/defaults.
3. Finish tracking setup → live capture/pause/resume → report, with genuine gaps,
   explicit provenance and permissions. Then history/edit/trends/goals/rhythm.
4. Implement real sound library, mixing, retention/favorites/export; evaluate a
   licensed on-device classifier before offering snore-triggered cues.
5. Implement and validate phone sonar calibration/pipeline, automatic tracking,
   travel/nap/light flows. Document actual device constraints separately.
6. Implement sleep-only Health Connect, calendar, user-controlled backup, optional
   smart lights and opted-in automation transports with their prerequisites.
7. Complete readiness/privacy/appearance and supporting empty/error/permission
   states, accessible targets, font scaling, contrast and reduced motion.

Use FEATURE-MATRIX.md and feature-matrix.json for implementation status, and
DESIGN-REVIEW.md for the separate strict visual acceptance gate. Passing a build
does not satisfy either gate. The independent reviewer must recheck fresh images.

## Current device constraint

The user is going to sleep and explicitly prohibited running alarms. **No alarm,
screen-off, reboot, vibration, loud sound, or microphone tests until the user
authorizes them again.** Quiet root navigation/captures are permitted. A
`visualReview=true` launcher extra keeps only the app window awake; no lock/PIN
controls or global timeout changes are used. Losing the unlocked state is a
constraint, not permission to bypass the secure keyguard.

Never run Gradle `connectedDebugAndroidTest` on this personal installation: the
observed runner uninstalled it and removed its private state. The pre-update
SQLite backup was restored (1 session, 4 epochs, 1 disabled alarm). Preferences
not backed up before that uninstallation could not be recovered. Subsequent
tests must use direct instrumentation and a full private-app-data backup first.
`scripts/device_verification.py` allows silent roots/appearance/chart tests and an explicit controlled movement-only
tracking flow. Tracking asserts all existing alarms already disabled, forces mic,
clips and audio off, removes only its own rows and restores test preferences. Appearance draft/save, palette/text/contrast/motion behavior now has
passing device evidence and a narrow independent visual pass; smart lights and
the rest of screen 36 remain incomplete. The narrow manual movement flow now passes device and visual review after source
and screenshot corrections. Sonar, automatic tracking, microphone hardware
lifecycle and overnight reliability remain pending. Other priorities stay active.


The bounded board-08 records flow now passes default-size S23 behavior and strict
visual review: manual records, effective tracked edits, search/filter, calendar
trends, draft cancellation, same-save recreation and selected deletion. See
VERIFICATION.md for exact evidence. Full screens/configurations, advanced stats,
rhythm and advanced statistics remain open. Continue the remaining vertical flows; do not mark
the overall goal complete from this milestone.


The bounded Sleep goals → free14/30-day commitment → progress → reopen/Stop flow
now passes model, storage, native-device and strict visual review. Dark Guard
configuration draft/save/cancel/reopen/pause also passes; actual enforcement and
reminder delivery remain open. API36 CI now verifies final Privacy/Wind-down rendering and system Back; final
S23 installation and broader configurations remain pending. Do not ask the sleeping user to reconnect or run an
alarm. Do not mark those screens or the overall goal complete from source fixes.
Developer stop is debug-only in source; the user's no-alarm-test restriction
continues. See the latest VERIFICATION entry for the USB/emulator limitation.

Screen 19 has native mixer/service/UI source, 24 passing silent model tests,
API36 configuration persistence and actual Rain/Ocean visual evidence. Original
PNGs/results are tracked in verification/2026-10-23-api36. The strict reviewer
passes those bounded compositions and final Wind-down polish, while rejecting
full app completion. The short real native sound journey passes at ce7f64c on the host-audio-disabled
API36 emulator; exact scope and the 17 original captures are in VERIFICATION.md.
Continue the remaining sound lifecycle/import/listening and other matrix flows.
No alarm test or phone update is authorized by these emulator checks.
