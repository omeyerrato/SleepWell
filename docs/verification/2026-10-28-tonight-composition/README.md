# Tonight composition: focused native verification

[Run 40735685243](https://github.com/omeyerrato/SleepWell/actions/runs/40735685243) **passed in 52.457 seconds**, at `dea845226d6086794737ad69a338fa32ccd473c6`. It ran only the existing `NightArcFlowTest#clockProgressAndExplanationWithoutAnyAlarmDelivery` method and produced all **14 original PNGs**. Both optional flags were false: `native_sounds=false`, `audio_imports=false`. This is scoped Tonight verification, not full app or all-36-screen acceptance.

All 14 images were individually opened and compared with the left screen of [board 01](../../../design-concepts/2026-10-22-phone-app/01-everyday.png). The prior nine originals in [the earlier Tonight archive](../2026-10-27-tonight-report-routine/tonight/) were also individually inspected before implementation. Their `TonightScreen.java` and `NightArc.java` bytes at `14bc693e` matched the published `4cf8a86f` baseline.

## Concrete result

The Start action is now outside the optional-content scroll and immediately above the existing dock. Its footer inherits the current phase background. On these plum-phase originals there is no white strip, dock overlap or hidden Start action, including the initial large-text frame. Stacked large-text settings have separation from their dividers; the remaining content and illustration can still be scrolled. The no-alarm state now offers the real **Set first alarm** route instead of an inoperative switch. Existing saved-alarm switches and weekdays remain functional.

The first run exposed a pre-existing shared dock rounding discrepancy at 420 dpi. Its 163-pixel child plus two 16-pixel padding edges needed 195 pixels, while the dock requested 194. The separate production correction sums the already rounded dimensions; the exact native full-width/full-height assertions were kept unchanged. [The failed attempt](failed-40729220884/OBSERVATION.md) retains its original diagnostic image, XML, method log, environment and failed summary. That 4.58-second failure produced no acceptance frames. The successful attempt is in `passed-40735685243/`; none of the failure files were relabelled as success.

## Individual image inspection

| Original | Actual observation |
|---|---|
| [upcoming](passed-40735685243/arc-upcoming.png) | Saved alarm is off, with one Off label and all seven saved weekdays selected. The truthful plan heading and empty pre-bed arc remain. Full bedside illustration, Start and four-tab dock fit together. |
| [help](passed-40735685243/arc-help.png) | The real rest-window explanation survives Activity recreation. Its close action fits; copy explains clock progress and distinguishes it from measured sleep. |
| [start](passed-40735685243/arc-start.png) | Actual foreground clock tick updates the existing arc to the window start: Now at the left, zero elapsed, eight hours remaining. Footer and dock remain fixed. |
| [middle](passed-40735685243/arc-middle.png) | Now and the real elapsed-clock fill reach the midpoint; four hours remain. No duplicate Off label or footer gap regression. |
| [end](passed-40735685243/arc-end.png) | Full clock fill reaches the wake endpoint and says Wake-up time. Start and dock remain fully visible. |
| [next](passed-40735685243/arc-next.png) | The following window is empty and says Bedtime in 15 h 59 min. It does not retain yesterday's fill. |
| [large](passed-40735685243/arc-large.png) | Enlarged clock, arc and stacked settings remain readable. The lower content continues below the scroll viewport; the full Start action and all four dock targets remain visible without scrolling. |
| [large-action](passed-40735685243/arc-large-action.png) | After real scrolling, complete Wind-down detail and bedside artwork are visible above the same fixed Start action. The clock at the top is naturally outside the scrolled viewport. |
| [weekly](passed-40735685243/arc-weekly.png) | Enabled Sunday-only alarm shows NEXT ALARM · SUN 25 OCT and the actual 48 h 30 min wait. Only Sunday is selected. |
| [off](passed-40735685243/arc-off.png) | Coordinate switch tap disables that saved alarm; the heading returns to the wake-up plan while the saved Sunday selection remains. Start stays above the dock. |
| [off-recreated](passed-40735685243/arc-off-recreated.png) | After real Activity recreation, the switch remains off and exactly one Off label remains. |
| [weekday-recreated](passed-40735685243/arc-weekday-recreated.png) | Coordinate re-enable and Monday selection persist through recreation. Monday and Sunday are visibly selected; the next Sunday occurrence remains correct. |
| [empty](passed-40735685243/arc-empty.png) | With the private alarm deleted, Set first alarm / No alarms saved replaces the switch and weekday controls. The plan/illustration remain, and the Start footer is still anchored above the dock. |
| [empty-action](passed-40735685243/arc-empty-action.png) | Tapping Set first alarm opens the actual empty Alarms tab, including its full Add alarm action. Back returns to the empty Tonight state without creating an alarm. |

## What the method actually proves

The method launches real `MainActivity` in `visualReview` mode with a UUID-private SQLite store and sleep-plan preferences. Only its clock suppliers and the existing scheduling port are injected. The scheduling port accepts configuration and records three calls; it never delegates to the OS scheduler. The no-output guards require emulator/CI mode, no ringing, no tracking owner and no sound service. Lifecycle checks admit only owned Main Activities; every owner closes before factories are restored. Saved plan contents and all original personal preference entries must compare equal at cleanup. The private alarm deliberately changes for the switch and weekday assertions, then is deleted with its private database.

The existing 30-second foreground Handler tick is observed on the same arc instance. The flow checks real help/recreation/close, Off persistence, weekday persistence, the empty route and Back. Before every main-state capture, it checks the exact full visible dimensions and minimum 48 dp bounds of Start and every dock target, footer order/background, scroll ownership, non-overlap and Start's button accessibility role. The help close and Add alarm destination also have full 48 dp bounds checks. Captures wait for the actual focused owned window and a committed hardware frame.

## Boundaries and remaining differences

This intentionally retains the requested **four-tab dock** while board 01 has three tabs. The WAKE-UP PLAN heading when no enabled alarm exists and the empty arc before bedtime are truthful states; the reference's enabled alarm and decorative partial fill are not fabricated. The existing bedside artwork/crop and arc geometry were preserved. This increment does not certify exact board raster/typography equivalence or all alternate row configurations.

The native fixture uses controlled clock values and private saved configuration; it does not measure sleep, change the OS clock or exercise hardware. **Start is not clicked**, so this archive does not prove tracking startup, permissions, sensor capture, service ownership, actual alarm scheduling/delivery, sound, haptics or flashlight output. The default and enlarged in-app text sizes are covered on one API 36 x86_64 emulator at 1080×2316 and 420 dpi; other devices, system font scales, RTL/locales, light appearance phases and optional-row combinations remain outside this run. TalkBack is not driven. These images also predate the separately planned launcher and SVG-icon replacement work; they do not accept that future asset migration.

See [source equivalence and commit order](SOURCE-EQUIVALENCE.md) and [SHA-256 inventory](SHA256SUMS.txt). PNGs and raw artifacts are copied without editing or line-ending normalization. No phone, release, install or Root-checkout mutation was performed.
