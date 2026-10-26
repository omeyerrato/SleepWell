# Priority: feedback and findable settings

User-requested priorities,26October2026. Keep every feature and the supplied Changing
light/Inter visual language. This is an implementation increment, not36screen acceptance.

## Priority tasks

- [x] Add shared input feedback and verify the saved opt-out; physical feel remains unchecked.
- [x] Exercise grouped navigation, settings/global search and actual parent returns.
- [x] Inspect ten actual Android captures and exercise draft/save/reopen persistence.

## Settings structure

The previous17root destinations remain available in8groups, plus settings search.
Search everything remains available from settings search. An empty settings query shows8categories rather than an undifferentiated list; selecting a category or entering a term reveals matching destinations. The current catalog has88 destinations, including direct links to the named supporting pages below.

| Root category | Destinations |
| --- | --- |
| Alarms | Manage alarms, defaults, protection, calendar rules, naps/timers, NFC setup |
| Sleep & bedtime | Tracking setup, automatic tracking, sleep plan, guard, awake estimates, rhythm, diary, travel |
| Sound & recordings | Analysis, snoring cues, sleep sounds, recording retention, favorites, explicit recorded laughter/instrument estimates |
| Appearance & feedback | Theme/text/motion, touch feedback, Tonight layout |
| Reliability | Existing readiness/permissions/battery screen |
| Connected apps | Health Connect, automations, lights, paired movement, external PIR, tracking requests, record/alarm access |
| Data & privacy | Existing storage/backups/exports/erase and a connected-access link |
| Home screen | Existing widgets/launcher settings |

Back from a settings group returns to Settings; embedded details return to their
parent group. Group and scroll state survive Activity recreation and ordinary
child-screen returns. Opening categories/search does not start outputs or change
permissions. Existing actual feature screens still perform their own explicit actions.

## Alarm editor

| Existing controls | Current location |
| --- | --- |
| Time, weekday/repeat, name, smart wake, challenges, sound, snooze, enabled and finalSave | Edit alarm |
| Selected date skips, next-only override, run/clear date, relative/speech time, sleep-plan time, sunrise basis, bedtime override | Schedule & dates |
| Protection, new-alarm defaults, delete | More alarm settings |
| Orientation lock, flip, volume keys | Buttons & orientation, reachable fromMore orSnooze with the actual parent remembered |
| Snooze duration/count/total/shortening and choose-each-time | Snooze |
| Backup enabled/delay/duration/bursts/gaps, wakecheck and ringing timeout | Backup & wake-up check |

These remain the same draft/validation/save paths. Grouping does not reset existing
alarm behavior; only finalSave alarm writes the draft. Delete keeps its explicit
confirmation. Errors and unavailable controls remain represented. Clear date is an action button rather than a misleading navigation chevron. Existing source
coverage is not proof of recreation/persistence or reference fidelity.

Shared feedback uses native setting-respecting haptics for tabs, buttons, menu rows, genuine switches, chips, segments, weekday selection and choice dialogs; no raw vibration. It
acknowledges an input rather than claiming asynchronous success. Sleep/alarm/capture,
quiet review and touch-exploration contexts are suppressed. SeeINTERACTION-FEEDBACK.md.

Current evidence: [one focused organization flow](verification/2026-10-26-settings-organization/README.md), CI40356841938. Remaining all-app/custom-control/physical acceptance is separate.

## Further sound and tracking organization

Three parallel implementation worktrees supplied this additional bounded pass.
Existing preference keys, playback/capture admission and explicit saving paths
remain; opening a menu does not start recording, sound or an armed wait.

| Main screen | Supporting pages |
| --- | --- |
| Automatic tracking: four genuine start-method radio choices, Save / Arm tonight | Start conditions; Sound recording; Sleep confirmation |
| Repeat nightly: enabled, time and start method | Start conditions; Start reliability; Recording limits |
| Sound sensitivity: category and duration summaries | Detected categories; Event duration |
| Audio retention: days and favorite preservation | Retention details |
| Snoring cues: response, type, intensity, repeats, sensitivity and explicit preview | Contactless & output; existing route settings and help |
| Sleep sounds: listening, levels, timer, fade and library | More sound settings → Sources & playlists / Playback & tracking; silent timer/fade drafts with Use / Cancel |

Chevrons open details, radio/check states select options, and down arrows open
choice lists. Android gesture Back and toolbar Back use the actual parent routes;
drafts and applicable route/scroll/picker state are retained over recreation.
Timer/fade writes retain revision checks rather than overwriting a newer mix.
Settings search can open these allowlisted detail routes directly, with visible
location breadcrumbs. Original search destinations retain their order.

Source organization is not visual acceptance. These new frames have not yet been
captured or exercised on Android; the earlier ten-capture Settings flow does not
cover them. See [sound settings](SOUND-SETTINGS-ORGANIZATION.md) and
[sleep mixer](SLEEP-MIXER-ORGANIZATION.md) for feature-preservation maps.
