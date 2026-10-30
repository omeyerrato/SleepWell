# Resumed reference comparisons —30October2026

The user explicitly resumed silent emulator comparisons and safe main-action checks. This cohort uses isolated GitHub Actions Android emulators, host audio disabled, private fixture databases and fake scheduling/playback ports. It does not exercise the personal phone, alarm delivery, real sound or hardware outputs. Fixture records are labelled in captures and are not application seed data.

## Before corrections

Source `acefec731103bf4d7568d087b191a3beaee53547`:

- [Report41008894116](https://github.com/omeyerrato/SleepWell/actions/runs/41008894116) failed: the complete Read details touch target did not fit above the dock. The captured Trends page shows extra navigation and date-control rows consuming the reference's primary content area. [Original Android capture](report-before.png).
- [Tonight41008900916](https://github.com/omeyerrato/SleepWell/actions/runs/41008900916) failed: Monday's visible target was115px wide at2.625density, below48dp. The fixed leading gutter squeezed seven weekday cells. [Weekday failure](tonight-weekdays-before.png).
- The same Tonight run captured clock progress at start/middle/end before reaching that failure. [Actual middle-of-window capture](tonight-middle-before.png) shows the moving Now marker and half-filled curve. This is elapsed plan time, not sensor-derived sleep progress.

## Corrections under comparison

- `5dfcb453`: Tonight's weekday gutter yields space to seven48dp targets when the panel is narrow. Circle visuals, stored weekday behavior and output guards are retained.
- `13630bfc`: remove the extra Report navigation rail, retain History/Trends navigation in header actions, move range stepping into Options, and remove artificial gaps between contiguous measured ribbon bins. Actual measurement gaps remain blank. The existing Add tag interaction follows its current label; fit and persistence assertions remain intact.

## Bounded current results

| Flow | Source / emulator run | Actual result | Visual comparison |
|---|---|---|---|
| Report/calendar/edit | `70bca1ec` / [41017261604](https://github.com/omeyerrato/SleepWell/actions/runs/41017261604) | PASS 69.636s; 18 required original captures; calendar, chart selection, trimming, cancel/recreate and Save/reopen | All 18 originals inspected against boards 01/08. Current captures include `eeb0f88c`'s 8dp before Tags and shortened empty-state caption. [Current archive and limits](current-report-41017261604/README.md) retain all originals and run metadata. [History](current-report-41017261604/report-reference-history.png) shows all seven nights; [Trends](current-report-41017261604/report-reference-week.png) retains Read details; [Editor](current-report-41017261604/report-reference-editor.png) retains Save/Delete. Inline saved-audio and provider-stage variants remain open. |
| Tonight | `6143f568` / [41011802754](https://github.com/omeyerrato/SleepWell/actions/runs/41011802754) | PASS58.995s;18 originals; actual Off/weekday save/recreation and first-alarm route with fake scheduling | All18 originals inspected. [Weekdays](tonight-weekdays-after-6143f568.png) retain full circular visuals and targets; [middle window](tonight-middle-after-6143f568.png) shows clock progress. A first-frame SystemUI contrast transition still appears and remains open, as in the earlier transition evidence. Start tracking itself is outside this flow. |

An intermediate Tonight run41010429024 passed its interaction assertions but visually clipped the first circle after an after-layout padding change. `6143f568` moves spacing calculation into measurement; the subsequent originals above show the correction. This intermediate passing check did not establish visual acceptance.

Alarm/settings and bedtime/tracking comparisons continue in separate narrow emulator cohorts. No reference screen is declared fully finished by these bounded results; missing provider-stage/recorded-clip variants, the SystemUI transition and uncaptured actions remain explicit. The user's separate Alarms tab and compact alarm-list organization intentionally supersede the original three-tab/expanded-list designs.
