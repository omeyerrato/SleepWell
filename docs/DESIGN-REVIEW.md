# Strict design and behavior acceptance

All 12 numbered boards in `design-concepts/2026-10-22-phone-app` were visually
opened before implementation. ZIP entries were checked against the extracted
files. BUILD-PROMPT.md and SCREEN-MAP.md were read in full. The user reconfirmed
the same 12 references and rejected the first plain-row/button implementation.

## Reference observations

| Board | Visual contract inspected |
| --- | --- |
| 01 | Plum Tonight: large but controlled time, lilac plan arc, integrated illustrated bedtime panel; cream Report with measured chart hierarchy; amber challenge with cream answer sheet. |
| 02 | Cream alarm list with expandable rows, compact icon/value lines; full-screen time/day editor with bottom Save; smart window and sensitivity controls. |
| 03 | Radio selection and waveform sound rows, quiet preview panel; actual ramp curve and independent timing; compact snooze/backup sections. |
| 04 | Ordered heterogeneous routine, enrollment camera/tag state, amber timed wake check. |
| 05 | Distinct dark wind-down/guard and light goal screens; bedside art, selected apps, controls and real goal chart. |
| 06 | Dark placement/setup, real contactless calibration and focused live tracking with pause/stop. |
| 07 | Illustrated dark sound mixer, actual event player/favorites, light snore cue configuration. |
| 08 | Compact record history, lilac duration/trend plots, edit bounds/awakes/tags/notes. |
| 09 | Horizontal timing ranges and rhythm metrics, large nap time/presets, travel time-zone/light planning. |
| 10 | Compact automatic-start choices; motion diagnostics with live bars; dark room-light/noise signals. |
| 11 | Consistent icon tiles and descriptions in Settings; actual sleep-only health connection and automation rules. |
| 12 | Readiness permission rows, restrained data backup controls, theme/accessibility and smart-light states. |

## Independent review gate

The requested strict subagent independently viewed all 12 boards. Initial result:
**FAIL; zero of 36 screens final accepted.** Screens 01–05 and 31 have earlier
captures; the remaining screens have missing layouts, functionality or evidence.
No completion claim is permitted from compilation or appearance alone.

Concrete first-pass findings: dock labels left-aligned inside their capsules;
white three-button system-bar scrim on plum Tonight; repeated alarm-off text;
full-size AM; oversized competing elements; generic export-heavy Report; missing
icon identities; developer prose in Settings; editor Save below viewport;
generic sliders instead of required supporting settings. Existing modal line
graphs also failed the reference's bar-chart composition.

Current corrective work introduces centered grouped navigation, transparent
system-bar continuity, actual gear/privacy/GitHub icons, one alarm-state label,
separate illustrated plan panel, Report-first real epoch bars and Data & privacy
exports. Fresh device captures and another independent review are required.

The new bar chart must keep gaps, identify true zeros, label time/relative range,
and select actual epochs. Do not manufacture an all-night dataset or a stage
strip from four minutes of sensor data. A clear unavailable estimate state is
required until its algorithm is real and evaluated.

## Historical behavior gate at first rejection

The alarm test demonstrated editor persistence and real quiet full-screen delivery
over keyguard, but failed to locate Check answer after selection. It is **not a
passing alarm/challenge flow**. User prohibited further alarms while sleeping;
do not rerun it until that restriction is lifted. No overnight recording or new
microphone use was started during this visual correction.

At that first review, missing core features included multi-task routines, QR/walking, pause/resume, sonar, classifier, mixer/favorites/export, manual record/awake edits, goals/rhythm, integrations and appearance behavior. Later bounded milestones below supersede the pause, appearance and record-edit findings; the feature matrix tracks current gaps.

## Fresh root comparison: 22:31 and 22:38 device captures

Actual files remain local: `captures/roots/tonight.png`, `report.png`,
`report-selected.png`, `settings.png`. The independent reviewer reopened the
22:31 images and confirmed these **narrow corrections passed**: dock alignment,
plum system-bar continuity, flat smaller weekday discs, flatter reference-compatible
bedside art with blended boundary, Report numeral/unit hierarchy and time icons/
divider, compact unavailable-stage disclosure, larger Settings glyphs and the
external-link affordance. These are regression corrections, not screen acceptance.

The 22:38 follow-up mutes the disabled plan arc and labels the daytime test
recording as Recorded movement. The silent device test selected a real epoch,
verified its exposed timestamp/value state and captured the warm-brown selected
bar/timestamp capsule. This was visually compared with board 01. It did not
create a new session, invent values or operate the alarm.

Overall verdict remains **FAIL**. Appearance/Integrations routes are absent;
other screens/algorithms/integrations and persistence/accessibility evidence are
incomplete. Full alarm/challenge behavior remains unpassed and cannot be retested
under the user's current no-alarm instruction.

The independent reviewer also inspected the 22:38 disabled-arc and selected-epoch
captures: all three narrow corrections passed, with no new clipping/dock/system-bar
regression. Full-goal verdict remains FAIL. Its remaining wording correction is
implemented as “Recording started” rather than inferring sleep onset from capture start.

## Appearance preferences: 23:17–23:22 device pass

Board 12's Appearance detail is now a native cream page: separate Back affordance,
Inter heading, flat rows, violet switches, equal illustrated theme choices with
radio states, text-size value/chevron and a fixed brown Save action. The unavailable
smart-light connection is stated plainly; no fake Connect callback was added.

The first actual captures failed for wrapped Aa, washed-out switches, long helper
copy and stretched alternate art. Corrective captures under local
`captures/appearance/` show solid custom switch tracks, a single-line Aa, concise
helper with schedule information, uniform thumbnail geometry and separately
authored bedside variants preserving the composition in all three palettes.
The last thumbnail polish uses a crescent path difference so its cutout cannot
paint a dark blob outside the moon. No app screen is flattened into an image.

AppearanceFlowTest passed the full silent settings journey: Back cancellation,
each palette Save/reopen, actual rendered root colors, activity recreation,
1.3× native title size, reduced-motion press state, selected contrast pairs and
large-text scrolling to the primary action while the dock stays visible. The
original preference key presence/values were restored in finally. The independent
reviewer inspected all ten captures and source and **accepted the narrow
appearance-preferences flow**, with the crescent polish above requested afterward.

**Screen 36 remains partial** because the light adapter is missing. No whole-screen
accessibility certification is claimed: numeric checks cover selected text/button
pairs, not every pixel. Extreme system font settings, RTL and TalkBack still need
verification. Theme policy uses local-clock phases, not astronomical sunrise.
Full 36-screen/feature verdict remains **FAIL/incomplete**.

## Dense graph comparison: 23:36–23:45 device captures

The independent reviewer compared actual Android `captures/charts/dense.png`,
`selected.png` and `empty.png` against board 01's Report composition. These
images are explicitly labeled synthetic rendering fixtures. They are created
only in test memory through the native Report renderer and never enter the
production SQLite records. The actual short recorded session stays unchanged.

The narrow graph correction **passes**: spaced rounded lilac columns, clear
relative/time axes, genuine gaps inside grouped data, visible measured zeros,
and a warm selected column with its averaged time range. Display averages use
only contiguous actual epochs in production; stored readings are untouched.
High-contrast bars use a stronger solid color and labels respect text scaling.

The reviewer found two correctness gaps before acceptance: finite imported
extremes could overflow a sum/coordinate, and detail plots inferred bounds from
their samples. Both were fixed and tested: incremental means and division before
pixel scaling remain finite, and every detail plot now receives the session's
actual start/end so leading/trailing gaps and out-of-session filtering work.

This is **not** a passing overnight recording, estimator or sound-analysis flow.
Screen 02 and the full goal remain **FAIL/incomplete**. Extreme system-font
tooltip layouts and the remaining report actions/features still need evidence.

## Manual movement journey: 2026-10-23, final 01:03–01:04 captures

The strict independent reviewer rechecked all seven actual Android tracking
captures against board 06 and the saved/reopened Report against board 01.
Its final verdict: **narrow manual movement flow passes**; full screen 16/18,
Report 02 and the full app remain **partial/incomplete**.

The accepted scope is native setup with genuine availability/draft controls,
measured movement, same-session pause/recreation/resume, active time excluding
pauses, saved gaps and reopened Report. Actual Collecting starts with an empty
graph; measured one-second previews appear only when sensor data arrives. No
sleep stage or classified sound is invented to imitate the example board.

Corrections before acceptance: clipped graph labels and lower copy, missing
Pause/Stop glyphs, incorrect sonar icon, setup overflow, moon replaced with the
reference-style dotted signal indicator, clock/interval provenance, Off microphone
remaining Off while paused, and compact disclosure of live preview versus saved
minute averages. Live updates now retain the same chart View, selected time and
focus. The device test checks keyboard focus/selection across a refresh; this is
not a complete TalkBack or large-font certification. A redundant paused note that
clipped against the fixed footer was replaced by the compact chart context line.

The strict source review also required transactional epoch/progress writes,
recovery using closed-pause endpoints, a session-wide clip budget/cooldown,
calendar-time versus recording-axis separation and clock-change disclosure.
Targeted unit/isolated database tests cover those corrections. Microphone hardware
restart and real device clock changes were deliberately not tested during sleep.

Setup states plainly that sonar and automatic starts are unavailable. Those are
remaining implementation work, not an accepted substitute for screens 17/28.
Missing stage/sound classification and integrations keep full Report and feature
parity open. Overnight/reboot/Doze evidence and broader device/font/permission
states are still required. The final root captures retain grouped icon-over-label
navigation and a plum system navigation surface on Tonight.

## Native History → editor → Trends — 2026-10-23

The strict reviewer independently reopened all 13 final actual Android captures
under private `captures/history/` (02:35–02:36) and compared them with board 08,
screens 22–24. **PASS for the bounded native records flow at the tested S23
Ultra/default-size configuration. Full app goal remains FAIL.** This does not
certify every subfeature/configuration or all 36 designs.

Accepted compositions: cream history with compact date/coverage/duration rows,
adjacent moon/start and sun/end pairs, selected filters and grouped Report dock;
calendar Week/Month/Year trends with numeral/unit hierarchy, visible goal gap,
lilac rounded bars/dashed goal, thin bedtime line and real unknown-date gaps;
editor with adjustable bounds, duration, Start/End selectors, awake edits, lilac
outlined vector tags, Notes, fixed Save and red Delete. Supporting empty, search,
filtered and manual-no-measurement states are coherent. Extra awake rows scroll
Notes above the fixed footer. Short intervals show seconds.

Corrections required before acceptance: Notes clipping; missing tag/selector/
trash glyphs; Trends double-parent crash; offscreen/wrapped goal gap; collided
goal label; clipped bedtime axis; missing Year aggregation explanation; detached
sun/time pair; and white status icons during dialog transitions. Focus restoration
and stable post-dialog captures now assert the light status-bar appearance.

Function evidence is separate: HistoryFlowTest passes 101.657 s, new/existing
pending saves survive recreation and Back, and 17 isolated storage/replay/clock
checks pass. Raw captured rows remain immutable under trims/awake/note edits.
Manual records explicitly lack sensor graphs. Synthetic fixtures are labeled,
isolated and removed; these captures are not an overnight measurement.

Large-font, RTL, smaller-screen records layouts, real document-picker restoration,
advanced statistics/correlations, stage/sound classification and full Report
remain unverified or incomplete. Alarm/challenge still lacks a passing full flow;
routine/QR/walking, sonar/automatic starts, audio mixer/library, rhythm/travel,
integrations and smart lights remain open. No alarm/audio/microphone test ran.


## User-requested visible revision and bounded goal/guard gate — 2026-10-23

The user explicitly added **Alarms as a fourth root tab**, overriding the original
three-tab dock. Fresh S23 images were reviewed independently against boards01,
02,05,11 and12. The full36-screen/phone-feature gate remains **FAIL**.

Bounded visual passes at default S23 size: shared-path Tonight arc with no kink;
centered icon/label/selected capsules for four roots; purple system-bar continuity;
compact expandable Alarms list with styled switches, scaled AM/PM and anchored
Nap/Add actions; clearer Settings list with a smaller GitHub footer and external
launch glyph; readiness hero and check/attention/minus states; Sleep goals with
full Wind-down row above Save, linked time pair and real bedtime bars; all ten
isolated goal/create/stopped/lower-chart states; dark Guard drafts, real app
icons/switches, full Calls card, fixed actions and persistent Paused until/Resume.

Goal save/reopen/stop and Guard configuration persistence have separate passing
behavior evidence in VERIFICATION. Actual reminder notification delivery,
accessibility enforcement, alarm/challenge/fallback/developer-stop behavior,
large fonts, RTL and broader device configurations are not certified.

**Pending final images** after USB disconnected: Privacy summary heading was
missing in the last actual image and has an explicit width/weight source fix;
Wind-down row weights/countdown/past-bedtime state were rebuilt in source but
lack a final screenshot; final system-Back/destination recreation fixes lack a
connected-device result. These are pending gates, not visual passes inferred from
compilation. The last attempted final runner failed before installation because
ADB had no phone. The silent software-emulator fallback crashed before boot.

The phone's real Report remains a short captured period with sparse measurements.
No synthetic overnight curve, stage strip or sound classification was substituted
to make it resemble the illustrative all-night board. Sonar, automatic starts,
classification, routines/QR/walking, audio mixer/library, rhythm/travel,
integrations/smart lights and other matrix gaps keep full acceptance open.

## Screen 19 source revision — 2026-10-23

The native Sleep sounds detail now follows board07's hierarchy: dark plum detail
header without the root dock, rain-window hero, title/description, round playback
control and actual timer, primary/Brown gain rows, Timer/Fade and four illustrated
library tiles. Supporting import and auto-tracking controls follow the library.
Errors appear beside Play. Sliders have 48dp touch rows. All controls connect to
the actual mixer/service or persisted choices; opening the screen remains silent.

The strict reviewer inspected all four new original images and the native source.
Rain has the correct bedside/window/lamp composition and palette, but the assets
are more textured than the flat references. This is **not a visual pass**: actual
Android captures, default fold fit and supporting error states remain open.
Adding the Sleep sounds row also changes Settings height and invalidates a final
claim based solely on its older screenshot. The full36-screen/feature gate remains
**FAIL**. A manual-only silent GitHub emulator workflow is being added to obtain
actual Android captures without using the disconnected phone; it has no result yet.
