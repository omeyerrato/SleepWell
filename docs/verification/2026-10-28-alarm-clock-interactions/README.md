# Alarm clock controls: bounded native evidence

[CI 40630084064](https://github.com/omeyerrato/SleepWell/actions/runs/40630084064),
source `4e9366d9ce4e31b3db55b4341a0325c21ffd1839`, passed
`SettingsOrganizationFlowTest#groupedSettingsSearchAndAlarmDraftPersistWithoutOutputs`
in **41.023 seconds: one existing method and all 18 required original PNGs**.
The [raw transcript](SettingsOrganizationFlowTest.txt) reports status 0 and
`OK (1 test)`. The unchanged [summary](focused-review-summary.json) has no
failures and explicitly records `full_app_review: false`.
[Run metadata](run.json) pins the authoritative successful head.

The production UI change adds direct draft-only hour/minute controls while
retaining the full-time picker, and uses common feedback/fitted time text in
the compact Alarms list. Production source is identical across both runs and
to production commit `36592e6f`; no scheduling/storage implementation changed.
This is separate from release packaging, publication or installation.

## All 18 originals individually inspected

Compared with [Board 02](../../../design-concepts/2026-10-22-phone-app/02-alarms.png)
and the [earlier editor original](../2026-10-27-alarm-hierarchy/organization-alarm-editor.png).
The later requested compact list in its own Alarms tab takes precedence over
the board's expanded list. The editor now has the board's direct time-control
hierarchy, weekdays, Name, four primary menus and fixed footer. This is a
hierarchy/action comparison, not a pixel-equality claim.

| Original | Visible inspection |
| --- | --- |
| [List](organization-alarm-list.png) | Complete 6:30 AM and 7:00 AM, two disabled private alarms, full identity/weekday rows, Take a nap/Add alarm and selected Alarms tab. |
| [Large app text list](organization-alarm-list-large.png) | Both full times and labels remain readable; actions and four-item dock fit. Only app text-size choice 2 is staged. |
| [Editor](organization-alarm-editor.png) | Full 6:30 AM, four arrows aligned to the hour/minute spans, weekdays, Name, Smart wake-up/Challenges/Sound/Snooze, Alarm on and Save. Schedule & dates reaches the footer edge; further options use scrolling. |
| [Stepped](organization-alarm-time-stepped.png) | 7:31 AM after native hour/minute actions; the same four primary menus and footer remain visible. |
| [Recreated clock](organization-alarm-time-recreated.png) | Same 7:31 AM draft and hierarchy after framework recreation; bytes equal the stepped frame. |
| [Settings root](organization-settings-root.png) | Eight grouped destinations, search action and Settings dock selection; complete Home screen row. |
| [Sleep group](organization-settings-sleep.png) | Sleep tracking, Room conditions including dimming copy, Automatic tracking, plan/guard and other destinations. Travel mode's supporting text continues below the viewport. |
| [Feedback off](organization-feedback-off.png) | Actual disabled Touch feedback switch and readable explanation/Theme & motion route. No physical haptic claim. |
| [Settings search](organization-search-flashlight.png) | One Sunrise & flashlight result with alarm-selection guidance. The horizontal category strip has a partially visible trailing label. |
| [Alarm search](organization-alarm-search.png) | This-draft-only guidance, flashlight query and one explicit Gentle wake-up route. |
| [Smart following](organization-smart-following.png) | Actual 7:01–7:31 AM window, inherited Medium/goal Off, no manual sensitivity selection and full Save settings. |
| [Smart configured](organization-smart-configured.png) | Explicit High and goal On; full window controls and footer after recreation. |
| [Smart reopened](organization-smart-reopened.png) | High and goal On after explicit alarm Save/reopen; bytes equal the configured frame. |
| [Schedule](organization-alarm-schedule.png) | Repeat, skips, next-only, date/relative/plan/time-basis/reminder routes and fixed Done remain accessible. |
| [More settings](organization-alarm-options.png) | Buttons & orientation, protection, defaults, Delete alarm and Done. Delete is not pressed. |
| [Controls recreated](organization-controls-recreated.png) | Snooze breadcrumb, orientation On, flip On, volume keys No action and Done after recreation. |
| [Backup](organization-alarm-backup.png) | Existing backup sound/volume/duration, wake-up check and stop-timeout settings remain visible; no output action is invoked. |
| [Reopened controls](organization-alarm-reopened.png) | More settings breadcrumb, saved orientation On, retained flip/key choices and Done after reopening the saved alarm. |

Images and raw artifact files are unaltered. [original-sha256.json](original-sha256.json)
records 27 original files: 18 passing PNGs, passing method log/environment/summary,
and six diagnostically relevant first-failure files. Full guest/renderer logs
and the other first-run originals remain private. Local attributes preserve
bytes, including the original CRLF failure XML; its local whitespace attribute
recognizes those line endings. `SHA256SUMS` covers every retained archive file
except itself.

## Actions and preservation that actually ran

API 36 disposable x86_64 emulator, 1080×2316 override, density 420. The exact
`organization` scope selects the one-method class. Both optional-output flags
were false. The original 14 capture literals are retained plus four new clock/
list literals; other scope inventories were preserved and were not rerun.

The actual list time tap opens the editor and Back closes it without storing
changes; the actual identity-label tap opens it again. The list is checked at
normal and large app text sizes, with no missing/ellipsized time characters or
overflow beyond its own width/height. The original text-size value or absence
is restored after that subsection.

Each arrow and the full-time picker must be enabled, fully visible and at least
48dp in both dimensions. Arrow rectangles must not overlap each other or the
clock button. The four primary rows must be fully visible above the fixed
Alarm on/Save footer. Inputs use native coordinates from actual owned focused
frames, without retrying delivered taps. A retained detached old hour-arrow
callback is invoked separately and must be inert.

Native hour/minute inputs change 6:30 to 7:31; the actual saved Activity bundle
and framework recreation retain that draft. The retained custom picker is
opened, changed and cancelled without changing 7:31. Through that picker and
the four direct arrows, the method checks 23:59 → 00:00 → 23:59 → 00:59 → 23:59,
then restores 7:31. Full draft JSON must equal the original with only hour and
minute changed. Stored private alarm JSON and scheduling count remain unchanged
through all these operations and later settings navigation.

The method also traverses the existing alarm search, Smart wake-up dialogs and
configuration, Schedule, More settings and Snooze controls. The corrected
Flashlight path opens the actual Gentle options dialog, asserts its real Switch
is off, framework-recreates and requires the same owned dialog/off state, then
uses Done and Back. The original flashlight query and unsaved alarm must remain.
The production owned dismiss listener clears `settingsDialog`; picker Cancel
and later arrow changes also complete successfully in this run.

Only explicit Save alarm writes the fixture alarm. Full JSON then includes
7:31, the requested orientation lock and explicit High/goal On Smart settings,
with the routine and all other fields preserved. Exactly one **fake scheduling
port callback** occurs for this disabled alarm. Reopen restores 7:31; another
native minute increase followed by Back is discarded, and a further reopen
still has 7:31. Saved choices and one callback remain required at method end.

The existing fixture uses a private database, asserts its second alarm unchanged
and sessions/epochs empty, and rejects recurring runtime work. Every opened
Activity must carry quiet visual-review admission. Owned windows close, search
readers finish and HomeSurfaces drains before factory restoration. Exact original
app preferences are restored and compared, the private database is deleted, and
ringing/tracking/service flags remain inactive. UI feedback routing and Switch
type are covered; physical haptic output is deliberately suppressed.

## First failure and correction

[CI 40629112361](https://github.com/omeyerrato/SleepWell/actions/runs/40629112361)
at `240e4649` failed in **30.658 seconds** on the old assertion that Flashlight
wake-up was a Switch directly on the Gentle page. The unchanged
[log](failed-40629112361/SettingsOrganizationFlowTest.txt),
[summary](failed-40629112361/focused-review-summary.json),
[environment](failed-40629112361/environment.json),
[failure original](failed-40629112361/organization-failure.png) and
[hierarchy](failed-40629112361/organization-failure.xml) retain that evidence.
Ten required originals existed; eight later frames were missing. The new list/
clock subsection had completed, but the overall method had not passed.

The actual failure frame shows the Gentle options header action and flashlight
Off summary. Existing production `gentleOptions` owns the Switch in its dialog.
Native-only correction `4e9366d9` follows that real route, asserts the same off
state inside the focused owned dialog and retains recreation, Back, query and
data-preservation checks. No production change or weakened Switch assertion
was used to obtain the passing result.

[source-equivalence.json](source-equivalence.json) pins 23 source/workflow blobs
and the identical `app/src/main` tree from the production commit through both
runs. [candidate-commit-order.json](candidate-commit-order.json) records the
source/native/docs/scope/correction order. This does not assert equality with a
later root integration or release APK.

The captured configuration uses 12-hour time. The picker adapts to current OS
12/24-hour mode, but both modes, other locales, system font scales, narrow/
tablet displays, sunrise calculations and large-text editor geometry were not
runtime-verified here. No Android alarm admission, actual scheduler delivery,
sound/flashlight/vibration, recording/provider behavior, physical-device
behavior, all-36-screen fidelity or whole-app security acceptance is claimed.
