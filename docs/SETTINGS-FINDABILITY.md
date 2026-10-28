# Settings findability

`SearchActivity.settingsIntent(Activity)` opens the settings-specific route. Equivalent explicit extras are `SearchActivity.EXTRA_MODE` (`searchMode`) set to `SearchActivity.MODE_SETTINGS` (`settings`). Root Settings uses this route. The ordinary zero-extra SearchActivity remains the existing global search across saved alarms, sleep records and settings. Both modes expose visible **Search everything** / **Settings only** scope controls, so global search remains discoverable from Settings. Switching scopes preserves the query and remembered settings category; only the chosen scope’s results are shown.

Settings mode loads only `SearchData.settings()` / the immutable local `SearchSettings` catalog: it does not open Store, enumerate saved alarms, read record notes/tags or contact a service. A blank query browses the real catalog. Filters correspond to the Settings groups: Alarms; Sleep & bedtime; Sound & recordings; Appearance & feedback; Reliability; Connected apps; Data & privacy; Home screen. Query, mode and selected filter survive Activity recreation. Query matching remains local, accent-insensitive, case-insensitive and bounded to 160 characters / 100 results; it is word matching with curated synonyms, not semantic/fuzzy search.

The catalog expands the existing 30 destinations to89, retaining their original ordering. The explicit experimental recorded-audio entry does not start recording or inference. It includes the existing automatic/nightly tracking, motion/contactless calibration, anti-snoring and headphone cues, recording retention/favorites, backup/import/export, travel, offline narration, naps/profiles, permissions/protections, real integrations, app-access grants, appearance, home layout and widget settings. Entries carry a concise category/location breadcrumb and explain the actual destination. Examples: `snoring` finds analysis/cue settings, `prevent power off` finds alarm protection, `flashlight` finds the per-alarm wake-up route, `backup` finds actual backup flows, and `automatic` finds the existing tracking/reminder/calendar options.

The additional supporting-page links use only static allowlisted route extras:
`trackingOptionsPage`, `soundSettingsPage` and `sleepSoundPage`. They open actual
charging/screen conditions, recording choices, sleep confirmation, nightly
reliability, detected categories/durations, contactless cues, mixer sources,
playback, timer or fade pages. Unknown route values fall back to the Activity's
main page. Selecting a search result never invokes Save, Arm, Play, a permission
request or microphone capture. Room conditions retains its existing foreground
light preview while visible; optional sound needs an explicit action. Timer/fade
results open unsaved choices with explicit Use
and Cancel. These new routes have source/build evidence separately from the
earlier Settings-only runtime flow; that flow does not exercise all89 destinations.

## Room conditions discovery

Settings → Sleep & bedtime → Room conditions now names screen dimming alongside
live light and optional sound. The appended search entry uses that actual category
and breadcrumb. `screen dimming`, `ambient light` and `room sound level` lead to
the same explicit, nonexported RoomConditionsActivity; no route extras toggle a
setting or request access. All prior search entries retain their positions.
The saved Room/Wind-down dimming choice and all existing controls are unchanged.

The existing `SearchFlowTest#queryResultsRecreationAndSavedRecordOpenUseRealControls`
has a bounded continuation for the Settings-only queries, actual native result
tap, unsaved Screen dimming draft, framework recreation and actual Back to the
same search query without saving. Unavailable light and throwing audio/permission
ports prevent sensor/microphone/permission operations. Only the disposable CI
dimming value is temporarily staged off to prevent an existing opt-in from
affecting the Room window; its exact original value or absence is restored before
the full preference equality check. Ports are restored after Room owners close.
This does not verify the copy-only Main settings row by native input or measure
real light/sound/dimming behavior.

The selected method still has one `@Test` and retains its private-record navigation
checks. Its five screenshot literals are `search-empty.png`, `search-room.png`,
`search-no-results.png`, `search-record.png` and `search-recreated.png`. All five
frames require their expected visible status and an actual focused committed
native frame. The exact method passed in12.917s in focusedCI40623526256, with all
five originals individually inspected. [Scoped evidence](verification/2026-10-28-room-settings-search/README.md)
retains the first failed run and its ambiguous record query/loading capture,
the corrected unique-record assertions and the fake-port/preservation limits.
No phone/provider/output action or app-data migration was performed.

Record- or alarm-owned editors are not opened with fabricated context. Flashlight, extra loud, gentle volume, challenge tasks, snooze and repeat results clearly say **Choose an alarm** and open the actual Alarms list. New alarm defaults also route through the alarm draft that supplies their required template. Per-record SoundArchiveActivity is not exposed as a zero-extra destination; the actual whole-library export remains available. Trends opens the real HistoryActivity with `trends=true`; its row explains using Trend options for chart settings. Selecting a result opens a screen only; it does not invoke a test, grant, enrollment, output, recording or save callback.

Required root routes in MainActivity are the existing private `settingsPage` allowlist: `alarms`, `alarm_options`, `reliability`, `sound_analysis`, `wind_down`, `privacy`. Other destinations use explicit existing private Activity classes. `FeedbackSettingsActivity` is supplied by root in the same integration batch; no new manifest entry is added by this search increment. The catalog is explicit source, not discovery of arbitrary installed/exported components. Missing routes show a local opening error instead of performing a substitute action.

Global search continues reading saved local alarms/records as before and retains their real editors/reports. Settings-only mode remains useful if saved-data reading fails, because it has no saved-data dependency. No user preferences, alarms or records are mutated by searching/filtering. Sourcef0c1a274 builds. One focused silent API36 flow verifies settings/global scope switching, flashlight routing and query/category/scope recreation; this does not exercise all74 destinations. See [scoped evidence](verification/2026-10-26-settings-organization/README.md).
