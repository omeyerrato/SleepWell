# Favorites, missing nights and chart evidence

These41 PNGs are original synthetic-emulator captures at bb582cf from
CI40037791812. SHA256.json records original bytes; no file is retouched.
All41 were individually opened against references; detailed per-file findings
are in ../../STRICT-VISUAL-GATE-2026-10-23.md.

Native result:33/34 classes pass,50 passing methods,175 PNGs produced overall.
FavoriteSoundsStorageTest3 (0.549s), FavoriteSoundsFlowTest1 (20.487s),
MissedNightFlowTest2 (21.739s) and ChartClockFlowTest1 (4.366s) pass actual
private SQLite/WAV, fake playback/Usage Access, save/recreation/reopen checks.
No microphone, step sensor, alarm, native playback or public usage query ran.

Fourteen Favorites images pass the narrow supporting-list gate. MissingNight's
10 non-denied states pass narrowly, but denied's gray default focus slab is
REJECTED. All four chart images have readable measured clock labels and proper
missing intervals, but are REJECTED for default keyboard-focus slabs and the
long fixture title. The latest five calendar captures are incomplete: its test
fails after the recreated nested calendar, with no selected/reopened image or
failure hierarchy. New owned-window synchronization/diagnostics require rerun.

Week markers, the inset preview glyph and all five purple checked privacy
switches pass the bounded style correction. No full36-screen or feature-wide
acceptance is claimed. Later playback trim guards, Walk error cleanup, chart
focus/input isolation, calendar test fixes, graded math and sound-category
candidates are outside this evidence head.
