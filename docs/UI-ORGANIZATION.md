# Priority: feedback and findable settings

User-requested priorities,26October2026. Keep every feature and the supplied Changing
light/Inter visual language. This is an implementation increment, not36screen acceptance.

## Priority tasks

- [x] Add shared input feedback and verify the saved opt-out; physical feel remains unchecked.
- [x] Exercise grouped navigation, settings/global search and actual parent returns.
- [x] Inspect ten actual Android captures and exercise draft/save/reopen persistence.

## Settings structure

The previous17root destinations remain available in8groups, plus settings search.
Search everything remains available from settings search. An empty settings query shows8categories rather than an undifferentiated74-item list; selecting a category or entering a term reveals matching destinations.

| Root category | Destinations |
| --- | --- |
| Alarms | Manage alarms, defaults, protection, calendar rules, naps/timers, NFC setup |
| Sleep & bedtime | Tracking setup, automatic tracking, sleep plan, guard, awake estimates, rhythm, diary, travel |
| Sound & recordings | Analysis, snoring cues, sleep sounds, recording retention, favorites |
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
