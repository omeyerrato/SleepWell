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

## Behavior gate remains open

The alarm test demonstrated editor persistence and real quiet full-screen delivery
over keyguard, but failed to locate Check answer after selection. It is **not a
passing alarm/challenge flow**. User prohibited further alarms while sleeping;
do not rerun it until that restriction is lifted. No overnight recording or new
microphone use was started during this visual correction.

Missing core features remain explicit in FEATURE-MATRIX.md, including multi-task
routines, QR/walking, pause/resume, sonar, classifier, mixer/favorites/export,
manual record/awake edits, goals/rhythm, integrations and appearance behavior.

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
