# Bedtime, tracking, rhythm and travel comparisons — 30 October 2026

This records bounded visual comparisons of actual Android emulator originals against boards 05, 06, 07, 09 and 10. It is not acceptance of all 36 screens or proof of real sleep detection. The user's separate Alarms tab supersedes the original three-tab navigation.

Selected PNGs are byte-for-byte copies of run artifacts, without crops, recoloring or compositing. `originals.json` records source paths, run IDs and SHA-256 hashes. Per-run environment and focused-summary JSON retain source revisions, exact methods and result boundaries. Captures use an API 36 emulator at 1080 × 2316, density 420; fixture labels remain visible.

## Runs and safe actions

| Cohort | Source and run | Result and boundaries |
|---|---|---|
| Bedtime / automatic / tracking setup / motion / room | `14fb6ff11045dba8562285abd4ef8eb67167ee99`, [41029421665](https://github.com/omeyerrato/SleepWell/actions/runs/41029421665) | PASS three existing methods; 45 required originals. Guard draft/cancel/save/reopen and pause; automatic source/conditions/access and saved draft routes; tracking sensor selection, light-setting recreation, sensitivity cancel and power menus; fake motion samples/settings lifecycle; fake room light/microphone states and dimming persistence. The setup journey does not start a live tracking service. |
| Live night | Same `14fb6ff1`, [41034030220](https://github.com/omeyerrato/SleepWell/actions/runs/41034030220) | PASS one existing method; all 10 originals inspected individually. Start, Pause, Options/recreation, Resume and explicit Stop use injected commands with the real coordinator and private SQLite. Actual stored pause/missing intervals remain gaps; saved duration excludes pauses. No physical speaker, microphone or sensor outputs. |
| Rhythm / nap / travel / snoring cues | `371c5b9e9b53655cb7d32d85ae9d6fc6f27fda79`, [41021595110](https://github.com/omeyerrato/SleepWell/actions/runs/41021595110) | PASS four existing methods; all 29 originals inspected individually. Workday save/recreate/reopen and empty state; nap presets/tone/pending/reopen/cancel; travel city/date/reminder save/recreate/reopen/cancel and injected scheduler failure; snore settings save/recreate/reopen and fake cue behavior. Scheduling, playback, notifications and cue outputs are fake. |
| Contactless setup | Same `371c5b9e`, [41023021745](https://github.com/omeyerrato/SleepWell/actions/runs/41023021745) | PASS one existing method; all 11 originals inspected individually. Real DSP processes injected PCM; gain/route changes invalidate readiness, permission routes do not auto-start, retest enables Use sonar, and reopen requires a fresh test. Hardware ports are fake; no AudioRecord/AudioTrack output. |
| Sleep goal, prior evidence | `70bca1ecdf34fecb2a9bacded2d86e106ea434c8`, [41017277836](https://github.com/omeyerrato/SleepWell/actions/runs/41017277836) | Prior passing goal-plan cohort, not a rerun at `14fb6ff1`. Goal save/reopen and nonzero goal-gap layout were inspected previously. Its own metadata is retained separately. |

The eight newly reached Room tracking setup/more/sensitivity/power/recreated/automatic/manual/phone-selection originals were inspected individually. Current Wind-down, Guard, Automatic and Room primaries were also compared; unchanged supporting states had already been inspected earlier. This does not claim every Room artifact image was newly inspected twice.

## Reference-specific findings

| Reference | Original | Comparison and retained differences |
|---|---|---|
| 13 Wind-down | [Wind-down](guard-wind-down.png) | Countdown, target bedtime, bedroom art, grouped three-row controls and Start/Pause follow board 05. Missing alarm/access states show actual values. Captured black SystemUI glyphs over plum remain a known transition concern. |
| 14 Bedtime guard | [Saved apps](guard-saved.png) | Schedule, selected switches, calls notice and Save/Pause fit. Apps are real installed emulator packages, rather than mockup examples; Choose apps opens the selector. |
| 15 Sleep goals | [Plan](goal-plan.png), [nonzero gap](goal-trends.png) | Prior captures retain goal duration, linked bedtime/wake-up, measured chart and Save. Number/unit wrapping is readable; absent history does not produce sample columns. |
| 16 Tracking setup | [Main](organized-settings-tracking-main.png), [recreated light setting](organized-settings-tracking-recreated.png) | Sensor cards, placement art, optional sound/light, Automatic tracking route and Start fit. Automatic is a Set up navigation row rather than a misleading selection. Start is exercised through fake commands in the live-night flow. |
| 17 Contactless setup | [Qualified](sonar-setup-ready.png), [permission denied](sonar-setup-access-denied.png) | Art, truthful signal chart/status, permission rows and footer targets follow board 06. Echo change is relative, not sleep stages; hardware accuracy remains unverified. |
| 18 Tracking live | [Whole night](sonar-night-whole-night.png), [Options](sonar-night-options.png), [missing signal](sonar-night-missing.png) | Duration/status/chart/Pause/Stop hierarchy follows board 06. Named Options replaces anonymous overflow while retaining the centered orb. Uniform fixture bars reflect injected values; missing intervals stay blank. |
| 21 Snoring cues | [Saved controls](snore-settings-selected.png) | Response, alert type, intensity, repeats, sensitivity, Preview and Save remain visible; selections survive recreation/reopen. This does not establish classification accuracy. |
| 25 Rhythm | [Seven nights](rhythm-seven-nights.png) | Real range, dated intervals, target legend, work/free midpoint and chronotype requirement follow board 09. Workday editing remains available; the empty state has no fake chart. |
| 26 Nap | [Default](nap-default.png) | Four presets, end time, smart wake-up, tone and delete preference precede Start. Multiple naps and Add schedule remain available; pending/reopen/cancel use fake scheduling. |
| 27 Travel | [Plan](travel-plan.png) | City/country zones, date, paired clocks and cue controls follow board 09. Actual London DST produces a current two-hour difference and a separately stated departure-day three-hour difference. |
| 28 Automatic tracking | [Main](organized-settings-automatic-main.png) | Single-line headline, Options beside the intro, four choices, source/conditions/access and Save/Arm are readable. Saving and arming tonight are distinct actions. |
| 29 Motion sensor | [Injected signal](motion-settings-ready.png) | Illustration, active status, axes/bars, signal status, sensitivity and Test/Use follow board 10. Calibration remains available; the fixture identifies injected samples. |
| 30 Room conditions | [Room](room-conditions-default.png) | Grouped cards, measured gold light bars, sound meter, microphone/dimming and primary action follow board 10. The chart covers actual open-screen samples, not invented evening history. |

[Tracking settings](organized-settings-tracking-more.png) groups Start & schedule, Sensors & battery and While recording. [Sampling & battery](organized-settings-tracking-power.png) retains compatibility and reserve controls behind that named route. No features were removed.

## Corrections and limits

`744a3d97` supplied the live Options label and updated existing journey selectors to Set up / Automatic tracking. It mistakenly added a duplicate focus callback; `14fb6ff1` removed it before these successful runs. **No system-bar behavior correction is claimed**: the activity already had the callback. Wind-down's captured contrast transition remains covered by [separate evidence](../2026-10-29-systemui-transitions/README.md).

Earlier Room run `41021587905` stopped at the stale Manual selector and did not establish the missing tracking controls. Newer originals now cover those controls. Passing assertions and screenshots establish only these bounded layouts and safe actions at the captured geometry. They do not establish every font scale/device, power-menu prevention, actual alarms, overnight battery performance, microphone/sonar reliability, clinical stages, apnea detection, anti-snoring effectiveness or external-service availability. No physical phone actions or real alarm/audio/flashlight outputs occurred in these comparisons.
