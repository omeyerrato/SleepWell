# Priority: feedback and findable settings

User-requested priorities,26October2026. Keep every feature and the supplied Changing
light/Inter visual language. This is an implementation increment, not36screen acceptance.

## Priority tasks

- [x] Add shared input feedback and verify the saved opt-out; physical feel remains unchecked.
- [x] Exercise grouped navigation, settings/global search and actual parent returns.
- [x] Inspect ten actual Android captures and exercise draft/save/reopen persistence.

## Current navigation — 30 October

Usability is the current phase. New verification, security review and a separate bug-fixing pass are deferred by the user. The 0.1.9–0.1.19 source passes keep features reachable through these eight root entries:

| Root entry | Main job |
| --- | --- |
| Alarms | Manage alarms, future defaults and supporting alarm tools |
| Sleep tracking | Tracking setup, automatic tracking and grouped supporting tools |
| Sound analysis | Recording choices and supporting sound tools |
| Bedtime guard | Schedule and installed-app choices |
| Appearance | Theme, feedback, Tonight layout and home-screen tools |
| Reliability | Alarm readiness and Android access |
| Integrations | Health Connect, automation, lights and app access |
| Data & privacy | Storage, recording tools, backup and portable settings |

Report exposes Night / History / Trends. History names active date filters; Trends has previous/next ranges. Alarm settings keep common choices first, with named supporting menus. Command menus use action rows; saved choices use selection controls. More detailed explanations remain in About instead of occupying every main page.

Future-alarm defaults now have their own behavior-only edit/review/save flow. Backup keeps Export and Import primary. Automation and provider setup group their actual connection, consent and activation controls. These are source changes, not new interaction or visual acceptance.

Later source passes make alarm dropdowns and goal pickers explicit, keep Appearance Save visible, expose Nap options, clarify challenge selection and make guard app rows respond across their full area. Detailed scope remains in [the usability history](USABILITY-PASS-2026-10-29.md).

## Earlier grouping — 26 October

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

The existing organized-settings journey subsequently passed in34.25seconds with
11 individually inspected native captures, including sound category/duration
Save/reopen, retention/snoring options, automatic/recurring conditions and mixer
timer/fade persistence. It does not accept every tracking route or whole board.
See [bounded evidence](verification/2026-10-27-organized-menus/README.md),
[sound settings](SOUND-SETTINGS-ORGANIZATION.md) and
[sleep mixer](SLEEP-MIXER-ORGANIZATION.md).

## Finding settings within one alarm

Edit alarm now has a48dp header Search action. Its empty search browses9 actual
pages; a query searches42 named settings with their real parent breadcrumbs.
Results open the existing draft controls without toggling them. Conditional
restrictions remain visible; Extra loud points to Sound then More, and Alarm
protection explicitly opens all-alarm options. Searching never writes the alarm.
Final Save alarm remains the only commit action.

The query, nonzero scroll position and originating search context survive
recreation and returning from a result. Nested pages retain their natural parent;
returning from the searched page restores the query. The search input scrolls with
its page rather than remaining pinned. No saved record or audio is indexed.

The existing organization flow at7781d342, CI40442348795, passes in34.969seconds
with14 individually inspected Android captures. It verifies search/restoration,
actual child controls, unchanged draft data before final Save and saved options
after reopening. Settings search is now in the header; whole initial Home screen
and Schedule & dates row checks pass without scrolling. A later correction uses the shared modal setup for Smart wake-up dialogs and
restores bars after dismissal. CI40445147772 passes33.114seconds/14 inspected
frames, with actual light-status/navigation flag checks before every capture;
About restoration and legible glyphs are verified. This is included in33cb5693. Large fonts, physical haptics,
all menus and all36-screen fidelity remain open.

## 27 October installed update

Latest published and installed **32f064e2** adds Tonight arc/feedback/help
restoration and the organized challenge routine. arm64 APK166,649,432bytes,
original signer, build PASS11m19s; exact tag and both uploaded hashes verified.
[Release/evidence](verification/2026-10-27-tonight-report-routine/README.md).

Tonight45.786s/9 and challenge48.33s/6 inspected captures pass their named
actions/restoration/persistence. **New Report calendar verification failed and
remains incomplete.** These passes do not accept all36 designs or physical outputs.
Phone update installed with original database rows, preferences and user files
preserved; no app launch or physical feature/output test ran.
