# Focused settings organization UI flow

CI entry: `org.sleepwell.app.SettingsOrganizationFlowTest#groupedSettingsSearchAndAlarmDraftPersistWithoutOutputs`.

This is one instrumented native UI flow, guarded by explicit `ciReview=true`, `ro.kernel.qemu=1`, no active alarm/tracking and no pending wake check. It uses a uniquely named private Records.Factory database and the existing AlarmSkipFixture fake AlarmScheduling.Port. Every opened Activity must be an allowlisted Main/Search/Feedback/AlarmEditor window with `visualReview=true`, which suppresses haptics. No AlarmActivity, recording, sensor/camera/microphone admission, sound preview or real alarm scheduling is invoked.

The actual UI actions cover:

- Settings shows all eight groups; Sleep, Sound and Connected apps open their existing subgroup content and return with the visible Back action.
- Appearance & feedback opens Touch feedback. A real Switch tap turns it off; the saved value survives Activity recreation and explicit reopen.
- Settings search finds flashlight settings with a truthful Choose an alarm breadcrumb. The visible Search everything scope finds the saved private fixture alarm, then Settings only returns to the settings query. The selected Alarms category, scope and query survive recreation.
- Selecting the flashlight result actually opens the Alarms list. The fixture alarm’s own 48dp settings action opens its real editor.
- Schedule & dates, More alarm settings, Buttons & orientation and Backup & wake-up check are reached through real controls. Buttons opened from Snooze preserve that parent across recreation and Done returns to Snooze.
- Orientation changes remain a draft, with exact saved alarm JSON and zero fake scheduling calls asserted before Save alarm. Save then persists only the requested orientation field, preserves the entire routine and other configuration, invokes the fake schedule boundary once, and survives reopen.

All delivered actions are single native coordinate taps after the actual owned action is fully visible and at least 48dp in both dimensions. Helpers may scroll the real container to reveal an action, but never repeat an already delivered click or call a product click callback. Search text uses the actual accessibility input field. Screenshots require an attached, laid-out, hardware-accelerated focused Activity window to commit its own frame. Fixtures are explicitly labelled CI/private; no personal nights or sample graphs are fabricated.

Expected PNGs in the application’s `externalFiles/verification` directory:

1. `organization-settings-root.png`
2. `organization-settings-sleep.png`
3. `organization-search-flashlight.png`
4. `organization-feedback-off.png`
5. `organization-alarm-editor.png`
6. `organization-alarm-schedule.png`
7. `organization-alarm-options.png`
8. `organization-controls-recreated.png`
9. `organization-alarm-backup.png`
10. `organization-alarm-reopened.png`

Failure artifacts are `organization-failure.png`, `organization-failure.xml` and `organization-failure.txt` (original step/error). Cleanup finishes every owned Activity, waits for Search readers and home-surface work, restores the feedback preference and all injected globals, checks unchanged unrelated preferences/sibling alarm and removes the private database/preferences.

This is a focused route and persistence check, not a pixel-match verdict, hardware haptic/output test, broad unit suite or proof that every destination was exercised. The source author did not build or run it; root owns the combined compile and CI execution.
