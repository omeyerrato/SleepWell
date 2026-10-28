# Alarm list and draft clock controls

Source: `36592e6fadc23e4d4778333bc05c6642a07af72d`. Native extension: `ea3bdf46`.
Base: `f073504f`. These changes are separate from the frozen releases.

The [Board 02 reference](../design-concepts/2026-10-22-phone-app/02-alarms.png)
shows direct hour/minute controls in the editor. The earlier
[native editor original](verification/2026-10-27-alarm-hierarchy/organization-alarm-editor.png)
has the full-time picker action but no direct arrows. The compact list remains
in its own Alarms tab, following the later user instruction.

The list now uses the existing `InteractionFeedback.Toggle`; time and identity
taps use the common acknowledgement. Existing permission checks, nap handling,
storage and scheduling callbacks remain in place. Full time text fits within
its available width, allowing two lines without ellipsis. The full formatted
time is also exposed as its accessibility state description.

For ordinary clock alarms, four independent 48dp arrow actions change only
the editor's hour/minute draft, wrapping within the day. Numeric alignment uses
the fitted TextView layout rather than an assumed AM/PM width. Hit regions do
not overlap each other or the full-time picker action. An obsolete detached
arrow cannot mutate the current draft. The existing picker and its selector
are retained. Sunrise alarms continue to display their calculated next time
and use the picker for the fallback clock; direct arrows are omitted there.
The four main menus and fixed Alarm on/Save footer retain their actions;
supporting settings retain the scroll path.

## Verification prepared, not yet run

The existing inert method
`SettingsOrganizationFlowTest#groupedSettingsSearchAndAlarmDraftPersistWithoutOutputs`
is extended; no test method was added. Its private record and scheduling ports
remain installed. The alarm is disabled, and quiet visual-review admission
remains enabled. It checks:

- Full arrow/picker bounds, pairwise nonoverlap, and the four main menus above
  the fixed footer at ordinary app text size.
- Actual native list time and identity navigation, normal/large app-text list
  fit, and restoration of the original text-size preference.
- Native hour/minute changes, detached callback rejection, framework recreation,
  picker Cancel, midnight wraparound and restoration through the existing picker.
- Full draft equality except the requested hour/minute, no stored changes before
  Save, one private scheduling callback on explicit Save, saved reopen and Back
  discarding a later unsaved minute change.

Expected literals (18 total; the original 14 are retained):

```text
organization-settings-root.png
organization-settings-sleep.png
organization-feedback-off.png
organization-search-flashlight.png
organization-alarm-list.png
organization-alarm-list-large.png
organization-alarm-editor.png
organization-alarm-time-stepped.png
organization-alarm-time-recreated.png
organization-alarm-search.png
organization-smart-following.png
organization-smart-configured.png
organization-alarm-schedule.png
organization-alarm-options.png
organization-controls-recreated.png
organization-alarm-backup.png
organization-smart-reopened.png
organization-alarm-reopened.png
```

Exact changed-source and changed-native-file `javac` checks passed with JDK 21,
`--release 17`, Android API 36 and existing cached project/dependency classes.
The source check emitted five missing annotation-enum warnings from the minimal
classpath; both checks emitted only the expected deprecation notes otherwise.
`git diff --check` passed. No Gradle build, native run or CI dispatch was made
for this increment. The existing workflow inventories were not changed.

Runtime layout and the 18 originals still require focused native verification.
The test adapts to the current OS 12/24-hour mode; it does not force both modes,
change locale or system font scale, exercise sunrise runtime calculation, or
claim coverage of every display size. The larger list case changes only the
app's text-size setting in the disposable fixture. No actual alarm admission,
sound, vibration, physical device behavior or full-app acceptance is claimed.
