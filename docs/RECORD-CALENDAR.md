# Record date selection

History's date filter and trend anchor, plus the record editor's date field,
use a SleepWell-owned warm cream/Inter calendar. Weekday order and month/day
labels follow the current UI locale. The selected date is brown; month browsing
does not change it. Only **Use date** applies the pending selection. Cancel,
outside dismissal and Back leave the calling value unchanged. **All dates** is
an explicit history-filter action.

An open calendar keeps both its pending date and browsed month through Activity
recreation. In the editor, the surrounding time form also retains local date,
time, seconds text and selected UTC offset. Choosing a calendar date updates
only that time-form draft; its separate **Use time** validation still resolves
the instant. The record's final **Save changes** remains the only database
write. Invalid DST gaps and ambiguous folds retain the existing
`RecordDraft.instant` rules; an unchanged timestamp keeps its original exact
millisecond value. An awake-period chooser retains the first endpoint while
recreating its second endpoint.

There are no schema, preference, scheduler or output changes. Production alarm,
tracking, microphone and hardware-output code is untouched.

`CalendarMonthTest` covers locale weekday ordering, leap months, six-week
months and immutable geometry. Existing `RecordModelTest` covers DST resolution
and interval rules. `RecordCalendarFlowTest` is an isolated disposable-emulator
configuration journey with seven planned native captures:

- `calendar-history.png`
- `calendar-history-recreated.png`
- `calendar-trend.png`
- `calendar-editor.png`
- `calendar-editor-recreated.png`
- `calendar-editor-selected.png`
- `calendar-reopened.png`

It checks pending/cancel/apply/recreation, unchanged database state before Save,
an unchanged later-fold instant, and a saved date change with different offset
while preserving seconds and milliseconds. It observes the actual focused
dialog frame before screenshots. It never launches an alarm, starts recording
or invokes audio/vibration/light effects. Compilation is not native execution;
all seven fresh original screenshots and a successful journey are required
before visual or behavior acceptance.
