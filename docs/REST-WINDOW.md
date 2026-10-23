# Tonight rest window

The previous curve filled a fixed 80% of its path. That had no behavioral
meaning and was removed after the user's report.

The curve now maps the planned interval from bedtime to wake-up. Before bedtime
it stays neutral and shows a countdown. Within the interval it fills according
to elapsed clock time, with a moving Now marker and remaining-time caption.
Tapping it explains this meaning. This is a schedule clock, not measured sleep,
a sleep score or an indication that tracking is running.

An enabled alarm supplies its next rule-based occurrence; otherwise the saved
plan supplies wake-up. Cached zero/past deadlines are not treated as future
alarms. The hero, endpoint, duration and fill use the same instant/zone snapshot.
Weekly alarms show a day/date and use the intended bedtime immediately before
that occurrence, rather than making a multi-day sleep window. Clock refresh
does not save preferences, schedule alarms or change the device clock.

NightWindow chooses the intended local calendar dates before resolving DST.
Actual elapsed duration is used for the fill. A spring gap can collapse or
reverse a short window; that shows an unavailable plan rather than silently
adding a day. The exact wake instant is included at 100%; the next refresh after
it chooses the next planned window. Alarm scheduling itself is unchanged.

MainActivity's existing 30-second foreground Handler updates the retained view,
including with manual appearance. Resume and registered time/zone changes
refresh immediately. An expired alarm occurrence, zone or automatic appearance
phase rebuild preserves scroll. Work stops on pause; no background service is
added. Marker and tick geometry avoid the planned-duration text.

Validation: NightWindowTest has ten pure boundary, overnight/daytime, weekly,
DST and invalid-window tests. CI-only NightArcFlowTest uses an isolated plan and
database plus an injected clock. It never changes the OS clock or schedules an
alarm. It observes the actual foreground Handler tick in manual theme and same
View identity, then captures upcoming/help/start/middle/end/next/large/weekly
states. Source/build success alone does not certify those native results;
dated actual evidence and the independent verdict belong in VERIFICATION.md
and DESIGN-REVIEW.md. This remains a bounded component flow, not full app parity.
