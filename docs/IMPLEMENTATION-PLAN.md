# Persistent build plan

The full BUILD-PROMPT remains the goal. There are 36 intended phone screens;
wearables/watch companions and hardware vitals are excluded. No screen is final
accepted yet. The first UI pass was rejected by the user for visual fidelity.

1. Repair the root compositions against the boards: Tonight, Report and Settings.
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
`scripts/device_verification.py` runs only the silent root test and retains data.
