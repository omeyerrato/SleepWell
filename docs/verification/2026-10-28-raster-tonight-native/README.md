# Raster icons and Tonight: focused native proof

[Run 40759551606](https://github.com/omeyerrato/SleepWell/actions/runs/40759551606) **passed in 50.332 seconds**, running only the existing `NightArcFlowTest#clockProgressAndExplanationWithoutAnyAlarmDelivery` method. All **18 required original PNGs** and `arc-icon-metrics.tsv` were produced. Both optional inputs were explicitly false: `native_sounds=false`, `audio_imports=false`. The raw summary says one class, one method, no failures, and `full_app_review=false`.

**Visual acceptance is bounded to the 73 glyph/native launcher checks and reviewed app controls. The empty-Alarms UI04 SystemUI contrast state remains pending; the 18 captures are not all declared fully visually accepted.**

The successful native head is `ecdea35da8956adc120cbf96a50fc08449a374d0`. Its production tree is byte-identical to frozen source `ef69408258e900a50274d6f2488e859647138714`; only the native fixture changed. Production source, artwork, layout, packaging/version metadata, and the 18-image scope did not change for the correction. This proof therefore covers the frozen source's raster resources without requiring a new production binary. No phone, release, or install action was performed here.

## Actual result

All 18 successful originals were individually opened. The 14 actual Tonight images were compared with the left screen of [board 01](../../../design-concepts/2026-10-22-phone-app/01-everyday.png); the three diagnostic galleries and launcher preview were individually checked for semantics, transparency, tint, fit, and legibility. No new core clipping, opaque icon canvas, lost icon, Start/dock overlap, or white footer strip was found. Generated glyphs have some optical weight variation across different symbols; their functional meanings remain readable. This is not a claim of exact Phosphor geometry or pixel equivalence to the reference.

The native artifact records all **73 unique semantic resources as `BitmapDrawable`**, intrinsic 63×63 pixels at 420 dpi: exactly 24dp. All have transparent pixels and alpha minimum zero; the fixture also checks every decoded corner is transparent. Each of the 73 real, fully visible 48dp icon controls passed an actual-window PixelCopy check after two successive `Drawable.setTint` changes. Espresso/teal are deliberately alternating diagnostic tints, not new app appearance settings. The screenshots themselves are unmodified `UiDevice.takeScreenshot` originals, taken only after the focused owned dialog commits a hardware frame. No icons or screenshots were drawn onto a substitute canvas.

The launcher preview loads the real package application icon via `PackageManager`. Both its color artwork and actual API33 monochrome layer render through native `AdaptiveIconDrawable` masks at 120dp and 48dp. All four previews are fully visible, with complete wave/S silhouettes and sun disks. Actual matrix metrics show the 237px intrinsic drawable mapped to 315px at 120dp and approximately 126px at 48dp. Raw and transformed masks fit their respective drawable/view bounds. This is a native preview of the package layers under this emulator's mask; it does not activate the OS launcher theme or prove OEM/home-screen cache behavior.

## Individual successful originals

| Original | Observation |
| --- | --- |
| [upcoming](passed-40759551606/arc-upcoming.png) | Truthful Wake-up plan, empty pre-bed arc, one Alarm off label, seven selected saved weekdays. New white/plum glyphs, illustration, Start and four-tab dock fit. |
| [help](passed-40759551606/arc-help.png) | Recreated real explanation remains readable with full Got it action; distinguishes clock progress from measured sleep. |
| [start](passed-40759551606/arc-start.png) | Actual foreground tick reaches zero elapsed, Now at the left endpoint and eight hours remaining. |
| [middle](passed-40759551606/arc-middle.png) | Actual clock midpoint, half arc and four hours remaining; no duplicated Off label. |
| [end](passed-40759551606/arc-end.png) | Full arc, endpoint Now and Wake-up time; footer/dock remain visible. |
| [next](passed-40759551606/arc-next.png) | Next window resets to empty, Bedtime in 15 h 59 min. |
| [large](passed-40759551606/arc-large.png) | Large in-app text stays readable; optional lower content continues in the scroll viewport while the complete Start/dock stay fixed. |
| [large-action](passed-40759551606/arc-large-action.png) | Real scrolling reveals the full Wind-down detail and art; top clock naturally leaves the viewport, Start/dock stay full. |
| [weekly](passed-40759551606/arc-weekly.png) | Enabled Sunday-only alarm, next Sunday date and actual 48 h 30 min wait; only Sunday selected. |
| [off](passed-40759551606/arc-off.png) | Real coordinate switch disables the saved alarm, retaining Sunday and one Off label. |
| [off recreated](passed-40759551606/arc-off-recreated.png) | Off persists through Activity recreation. |
| [weekday recreated](passed-40759551606/arc-weekday-recreated.png) | Re-enable and Monday selection persist; Monday/Sunday selection remains clear. |
| [empty](passed-40759551606/arc-empty.png) | No saved alarm: real Set first alarm route replaces switch/weekdays; readable new alarm/bed/caret/dock glyphs. |
| [empty action](passed-40759551606/arc-empty-action.png) | Real empty Alarms destination, readable more/add/bed glyphs and complete Add alarm action. Back returns without creating an alarm. SystemUI contrast in this captured UI04 state remains pending. |
| [icons 1](passed-40759551606/arc-icon-page-1.png) | 25 separate actual resources, alarm through download; open silhouettes, correct arrows/carets and contrast half, no painted backgrounds. |
| [icons 2](passed-40759551606/arc-icon-page-2.png) | 25 resources, exclamation through moon; outline/filled pairs remain distinct and detailed keyboard/list glyphs readable. |
| [icons 3](passed-40759551606/arc-icon-page-3.png) | Final 23 resources, music through close; play/pause/stop, QR, shield, sound, tag, trash, upload, vibration and waves remain distinguishable. |
| [launcher](passed-40759551606/arc-launcher-icons.png) | Actual color and themed native previews fit at both sizes; full generated S/wave mark and sun remain visible. |

The three galleries are temporary, labeled diagnostic dialogs, not production feature screens. Each exact resource name and its actual native measurements are retained in the [metrics TSV](passed-40759551606/arc-icon-metrics.tsv).

## Failed attempt and correction

[Run 40756639924](https://github.com/omeyerrato/SleepWell/actions/runs/40756639924) remains **failed**, at frozen source `ef694082`, method **58.396 seconds**. It produced all 18 originals plus a failure PNG/XML. Those 19 PNGs were also individually opened. The final launcher fixture incorrectly compared raw intrinsic drawable width 237px with ImageView width 315px, ignoring the native FIT_CENTER image matrix. The original screenshots show the complete previews; the failure is not relabelled as a production pass.

Native-only commit `ecdea35da8956adc120cbf96a50fc08449a374d0` maps the drawable and mask through the actual ImageView matrix, requires full rendered size and containment, and preserves the raw mask/full-48dp gates. It also writes measured glyph rows before the launcher assertion, so future failures retain those facts. Its native compilation passed in 1m 1s; the exact log is compressed byte-for-byte in `matrix-native-compile.log.gz`. No gate was replaced with an unconditional success and no interaction or resource was skipped. Full failed artifacts, guest logs, stack, environment, summary and originals remain separately in [failed-40756639924](failed-40756639924/OBSERVATION.md).

## Scope and limits

The same private clock/plan/database journey still checks the original 30-second production foreground tick, help recreation, switch Off persistence, weekday persistence, real empty route/Back, exact personal preference restoration, fixed footer order/background, full Start/dock/close/Add 48dp bounds, scroll ownership, non-overlap and button accessibility role. Only a UUID-private saved alarm changes; scheduling is injected and never reaches the OS scheduler. Lifecycle checks admit only owned Main Activities. **Start is never clicked**. Gallery icon controls have no feature callbacks, sound effects or haptic feedback enabled.

The requested four-tab dock intentionally differs from the reference's three tabs. Disabled/no-alarm Wake-up plan and empty-before-bed arc states remain truthful; decorative progress or an enabled alarm was not fabricated to match the board. The bedside artwork and the 14 Tonight layouts are preserved. The older [52.457s/14-image proof](../2026-10-28-tonight-composition/README.md) remains explicitly pre-raster and is not reused as proof for this migration.

The successful empty-Alarms capture has some pale SystemUI status/navigation icons on its light ground; the earlier failed attempt with identical production bytes has darker status icons. System-bar transition timing was not diagnosed or changed here, and is not accepted by the resource checks.

This run used one API36 x86_64 emulator, 1080×2316 at 420 dpi. It does not prove physical tracking, alarm delivery, microphone/sensors, sound/haptics/flashlight output, TalkBack operation, every density/OEM mask, other locales/RTL/system font scales, light-phase Tonight variants, all app screens, or all 36 reference requirements. See [source comparison](SOURCE-COMPARISON.md), [measured inventory](review-inventory.json), and [byte hashes](SHA256SUMS). All raw PNG/log/summary/environment bytes are preserved; no native test batch was added.
