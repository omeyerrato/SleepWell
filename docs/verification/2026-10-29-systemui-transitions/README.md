# SystemUI transitions: diagnostic evidence

[CI40801401054](https://github.com/omeyerrato/SleepWell/actions/runs/40801401054) **failed in 26.855 seconds**, at `0a5f3fa31d37479a9863fc8108406ea270802643`. One subsection of the existing `NightArcFlowTest#clockProgressAndExplanationWithoutAnyAlarmDelivery` ran with `ciSystemBarsTransition=true`, eight original PNGs and both optional-output inputs false. The original 18-frame Tonight/raster scope and all 47 existing frame inventories were preserved. This diagnostic skipped the unrelated 73-resource gallery and 30-second clock wait.

**The first light Alarms frame reproduces white status glyphs on cream. Its later fixed sample and recreated screen are correct; the first-frame contrast issue remains open.** All eight original PNGs were individually opened. No production bytes changed; [source identities](diagnostic-source.json) pin exact production tree equality to Root `6cd00676`.

## Individual originals

| Original | Observation |
| --- | --- |
| [Tonight](failed-40801401054/arc-bars-tonight.png) | White status glyphs and light-gray gesture handle are readable on plum. Start and four-tab dock remain complete; Start was never pressed. |
| [Explanation](failed-40801401054/arc-bars-help.png) | Real owned explanation, full Got it action; light glyphs contrast with its dark dimmed backdrop. |
| [Explanation dismissed](failed-40801401054/arc-bars-help-return.png) | Actual dark Tonight returns with readable system glyphs and complete controls. |
| [First light Alarms](failed-40801401054/arc-bars-alarms-first.png) | **White clock/notification/Wi-Fi/signal/battery on cream**, despite requested light-surface appearance bits 24. The gesture handle is correctly dark in this run. |
| [Subsequent Alarms](failed-40801401054/arc-bars-alarms-observed.png) | Correct dark status glyphs and gesture handle on cream. This is an honestly later original, not a replacement for the failed first frame. |
| [Returned Tonight](failed-40801401054/arc-bars-tonight-return.png) | Native Back restores readable dark Tonight and the empty private alarm state. |
| [Alarms recreated](failed-40801401054/arc-bars-alarms-recreated.png) | Actual Alarms destination persists, with correct dark glyphs and complete Add alarm action. Add was never pressed. |
| [Explanation recreated](failed-40801401054/arc-bars-help-recreated.png) | Real explanation restores after recreation without another tap; full Got it and readable light glyphs. |

The [unaltered TSV](failed-40801401054/arc-bars-observations.tsv) records the actual owner, native appearance, legacy visibility, Window flags, insets and original-pixel counts. Each PNG was saved first, followed by **exactly twelve hardware-frame commits** and one additional screenshot sample without another saved PNG. The count is fixed independently of colors: no polling for acceptable pixels, forced bar appearance/focus, added input, image painting or delay chosen to make a capture pass.

The first Alarms PNG has native appearance 536 (masked status/navigation bits 24), legacy visibility 8208, 2,292 pure-white status pixels and no dark glyph pixels. Its fixed sample 649 ms later has the same requested masks, zero pure-white pixels and 2,350 dark pixels. The subsequent and recreated images are correct. This establishes a **transient first-frame mismatch between requested policy and rendered SystemUI**, not a persistent stale callback or proven framework root cause. The prior [UI04 white-handle observation](../2026-10-28-raster-tonight-native/README.md) remains historical; persistently wrong gesture tint did not reproduce here.

## Predicate correction

The failed run also contains false dark-scene failures from this new diagnostic: its light counter required all channels above 250. The real readable gesture is RGB(238,237,239), with 2,798 pixels of that color; the Alarms gesture is correctly RGB(101,99,96). The failed method is not relabeled as passed. Separate native commit `025ecef8` accepts light-gray pixels above 180, as the existing Sonar predicate does, while retaining a separate above 250 white counter to reject white-on-cream. This does not dismiss the actual first Alarms contrast failure.

The raw dim-behind explanation has Window flags 25165826 and darkens its backdrop to RGB(14,12,19). Its glyphs visibly contrast in this dark scene. **No causal claim links that dialog to UI04**, and no dialog production change was made on this evidence.

## Actions and limits

Actual full-48dp coordinate actions use the current NightArc description, Close rest window explanation, Set first alarm and native Back. Captures bind the exact Main decor or its focused owned explanation, then commit a hardware frame. Recreation waits for the actual restored owner. No Start, Add alarm, sound, microphone, haptic, torch, alarm-delivery or external-provider action ran.

The fixture uses UUID-private SQLite/plan preferences, a private clock and an injected scheduling port. It asserts the private alarm list remains empty and output services remain inactive before the final contrast assertion. That assertion fails, so the following outer expected-zero scheduling-count and unchanged-plan assertions are **not reached**. The finally block closes/drains owned Activities before restoring factories, restores original appearance preferences exactly and deletes private fixtures; the transcript contains no additional cleanup failure. This is not a pass for every action invariant or real OS scheduling.

[Original method log](failed-40801401054/NightArcFlowTest%23clockProgressAndExplanationWithoutAnyAlarmDelivery.txt), [raw failed summary](failed-40801401054/focused-review-summary.json), [environment](failed-40801401054/environment.json), [final failure PNG](failed-40801401054/arc-failure.png) and [failure hierarchy](failed-40801401054/arc-failure.xml) remain unchanged. Full guest/renderer and local compilation logs remain private. API 36 x86_64 emulator: 1080×2316 at 420 dpi. This does not establish physical/OEM/three-button behavior, every theme/locale/screen, all 36 references or frozen 0.1.6 acceptance. Release/install metadata is untouched.

## Combined-request experiment: rejected

[CI40803344526](https://github.com/omeyerrato/SleepWell/actions/runs/40803344526) **failed in 29.088 seconds** at `f78593ff256cc377ffd3a2cb83d95361b043a187`, with the same method, eight exact originals and optional-output inputs false. This head includes the corrected light-gray native predicate. Its sole production experiment changed `Design.java`: API 35+ requested status/navigation appearance together before changing the Window background/content; API30–34 retained AndroidX compatibility, and existing lifecycle/retry guards remained unchanged. Compilation passed in 3m 51s, 25 tasks: 3 executed/22 up to date; original local log remains private.

**This hypothesis failed and was reverted by `07e50c280e46f210f54aae7c5df0011d3008b961`. It must not be merged or described as a fix.** [Exact source/revert identities](rejected-experiment-source.json) establish that final production source equals Root `6cd00676` again. No further hypothesis was dispatched.

All eight experiment originals were individually opened:

| Original | Observation |
| --- | --- |
| [Tonight](rejected-40803344526/arc-bars-tonight.png) | Readable light status glyphs and light-gray gesture on plum; complete controls. |
| [Explanation](rejected-40803344526/arc-bars-help.png) | Real dark explanation and full Got it, readable light system glyphs. |
| [Explanation dismissed](rejected-40803344526/arc-bars-help-return.png) | Readable dark Tonight returns normally. |
| [First light Alarms](rejected-40803344526/arc-bars-alarms-first.png) | **White status icons and white gesture on cream**: the same first-frame defect persists. |
| [Subsequent Alarms](rejected-40803344526/arc-bars-alarms-observed.png) | Correct dark status/gesture glyphs on cream; this later frame does not replace the bad original. |
| [Returned Tonight](rejected-40803344526/arc-bars-tonight-return.png) | Complete dark main surface, readable light glyphs. |
| [Alarms recreated](rejected-40803344526/arc-bars-alarms-recreated.png) | Real Alarms destination remains, dark glyphs contrast on cream. |
| [Explanation recreated](rejected-40803344526/arc-bars-help-recreated.png) | Actual restored explanation retains readable light glyphs and full close action. |

The [raw experiment observations](rejected-40803344526/arc-bars-observations.tsv) show only `alarms-first/original` fails: requested mask 24 (native 536, legacy 0), 2,397 pure-white status pixels and 2,808 pure-white navigation pixels, both on cream. Its fixed twelve-commit sample 820 ms later is correct; all other original and fixed samples meet the corrected predicate. Thus the old near-white-only predicate error is resolved while the genuine first-frame contrast failure remains visible. The experiment also reproduces the historical **transient** white gesture; it does not establish a persistent wrong tint.

[Raw failed method](rejected-40803344526/NightArcFlowTest%23clockProgressAndExplanationWithoutAnyAlarmDelivery.txt), [summary](rejected-40803344526/focused-review-summary.json), [environment](rejected-40803344526/environment.json), [final failure PNG](rejected-40803344526/arc-failure.png) and [hierarchy](rejected-40803344526/arc-failure.xml) are unaltered. The same action/cleanup/assertion-reachability limits above apply. Full guest/renderer logs remain private. Neither run establishes a specific Android/framework defect, a DIM cause, or accepted transition behavior.

**Current outcome: reproducible first-frame contrast bug, corrected diagnostic, rejected production hypothesis, no net production change.** [SHA256SUMS](SHA256SUMS) covers every other public archive file. Native/scope/doc commits can be reviewed independently of the rejected source experiment and its revert.
