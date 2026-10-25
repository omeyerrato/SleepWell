# Standard Android alarm requests

Compatible assistant/apps can use Android's documented
[SET_ALARM /SHOW_ALARMS actions](https://developer.android.com/reference/android/provider/AlarmClock).
The exported receiver Activity requires `com.android.alarm.permission.SET_ALARM`.
It builds a fresh ordinary editor draft from bounded hour, minute, message,
Calendar weekdays and optional vibration fields. An absent time opens the editable
default time; absent days means one-time. Existing alarm identity, occurrence,
skip/nap/date authority and nested Intent values cannot be supplied by the caller.

Save in the ordinary editor is required; `EXTRA_SKIP_UI` cannot silently arm an
alarm. The request handler itself never writes/schedules an alarm. Custom/silent
ringtone requests currently show an explicit unsupported-request state; users can
choose sounds in SleepWell. It does not advertise dismiss/snooze/timer actions.
Actual assistant routing/choice of alarm app is external and remains unverified.
No voice account, cloud model or network request is introduced.

Four pure normalization/bounds tests pass within the combined900-test1275be1 build.
Atf5e80ab/CI40150902142 three native scalar/manifest tests and the actual
request/editor/recreate/explicit-Save/reopen flow pass with all three originals.
The scheduler is injected and records are private. That focused run failed on
separate DND/goal UI synchronization assertions, so it is not an overall green gate.
No phone, assistant, alarm or audio action was tested.
