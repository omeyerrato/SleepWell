# Report period gestures: actual native evidence

[CI 40647443477](https://github.com/omeyerrato/SleepWell/actions/runs/40647443477) at `ef2ded6b7073b94188dcdd56245d3e557c88262e` **PASSED 68.984s: one existing method / all 17 required original PNGs**. [Original method log](passing/ReportReferenceFlowTest%23referenceHistoryTrendsEditAndNightReportPersistWithoutEffects.txt) reports status 0/`OK (1 test)`; [unaltered summary](passing/focused-review-summary.json) records no failures and `full_app_review:false`. [Run metadata](run.json) and [environment](passing/environment.json) pin the exact successful head, Android 36 disposable emulator, host audio disabled and both optional output flags false.

Selector: `org.sleepwell.app.ReportReferenceFlowTest#referenceHistoryTrendsEditAndNightReportPersistWithoutEffects`, existing `report-flow` scope. No new @Test/unit batch, physical phone/output, release or installation. Published 0.1.4 source 08/package remains unchanged by this isolated branch.

## Concrete change and observed actions

The original PeriodTimeline already drew functional two-ended handles. Source 4cf8a86f accepted any DOWN as the nearest handle, blocked parent scrolling immediately, changed bounds on MOVE/UP from empty space, retained stale handle state after CANCEL and owned no stable pointer. Production `e93a94d8` keeps the existing renderer and constraining arithmetic, admitting only a 48dp near-handle candidate and a deliberate horizontal drag after touch slop. Taps/vertical page gestures do not edit; a single pointer owns it. Cancellation, multitouch, focus/pause/snapshot/detach restore exact original bounds and awake annotations. Previews always derive from the complete gesture annotation baseline, so dragging past an annotation and back cannot erase it. Only a completed final trim keeps intentional clipping.

Four genuine custom accessibility actions and keyboard Up/Down selection plus Left/Right adjustment address both endpoints. Start/End time buttons and their DST choice behavior remain. A completed changed drag gets one existing guarded feedback request; MOVE/render/cancel/restore and boundary no-ops get none. Saved OFF/quiet/Android/foreground/recording/accessibility guards stay in unchanged InteractionFeedback. No new output API or per-move buzz.

The passing existing method uses actual native pointer events, the real accessibility node actions and real key input in private fixtures. It verifies off/near-handle scrolling; tap without jump; annotation preview/backtracking; CANCEL; second pointer with IDs 7/11; actual dialog focus loss; unfinished-gesture recreation/lift; both deliberate drags; recorded-bound clamps; both endpoints' accessibility/keyboard actions; completed unsaved recreation; explicit Save/reopen; and subsequent boundary/note Back-discard. Reflection reads owned draft/axis/window state and never invokes the period mutation callback. Exact original session/epoch/pause/sound/clip rows and personal preferences remain unchanged; the intended saved edit overlay alone changes. All owned readers/Activities close before fixture/factory cleanup.

## All17 passing originals individually opened

Compared with [Board08 left/middle/right](../../../design-concepts/2026-10-22-phone-app/08-history-and-trends.png) and [Board01 middle](../../../design-concepts/2026-10-22-phone-app/01-everyday.png). This is a bounded hierarchy/action/regression comparison, not raster equality. Source labels remain recorded/reported coverage and actual movement; missing sensor stages/audio are disclosed.

| Original | Actual visible comparison |
| --- | --- |
| [history](passing/report-reference-history.png) | Seven private nights, right-aligned durations and real paused gap; complete four-tab dock. Four tabs follow later user instructions. |
| [week](passing/report-reference-week.png) | Selected Week, dated seven-day range, actual7h42 average, absent dated goal, duration and bedtime charts, full Read details above dock. |
| [week-calendar](passing/report-reference-week-calendar.png) | Actual26October pending selection, month controls and complete Use date/Cancel; dates follow the real disposable fixture day. |
| [week-selected](passing/report-reference-week-selected.png) | Selected fourth day reads7h10 from its saved pause-aware duration; cursor/label and both chart axes fit. |
| [month](passing/report-reference-month.png) | Seven known fixture dates only; other days stay blank, month selector and footer fit. |
| [year](passing/report-reference-year.png) | Only October has data; current year/month average remains honest, other months blank. |
| [night](passing/report-reference-night.png) | 7h42 recorded duration,22:48–06:30 timing, actual0.0708 relative selected average, stages Not estimated, analysis-only counts/no fabricated Play or waveform. |
| [chart-gap](passing/report-reference-chart-gap.png) | Actual1:15AM selection reads No readings in the recorded pause; blank span remains visible; no distant measurement substituted. |
| [editor](passing/report-reference-editor.png) | Initial22:48–06:30 /7h42, two original handles/time labels, date, time buttons, Notes and full Save/Delete fit. |
| [period-cancelled](passing/report-reference-period-cancelled.png) | After scroll/cancel/multitouch/dialog/recreation checks: original22:48–06:30 restored, Awake(1) retained, effective7h32. Screenshot supplements the exact bound/annotation assertions. |
| [period-drag-start](passing/report-reference-period-drag-start.png) | Deliberate Start drag reaches23:18 while End stays06:30;7h12 and intentionally clipped outside annotation agree with both time buttons. |
| [period-drag-end](passing/report-reference-period-drag-end.png) | Deliberate End drag reaches06:00 while Start stays23:18;6h42 and coverage edges agree; main/footer fit. |
| [period-accessibility](passing/report-reference-period-accessibility.png) | Both boundary semantic/key actions have returned to23:18–06:00 /6h42. Keyboard-selected Start has a clear brown outline; no opaque focus slab or clipping. |
| [period-draft-recreated](passing/report-reference-period-draft-recreated.png) | Unsaved23:18–06:00 /6h42 survives framework recreation; no incidental keyboard outline in touch mode; original source row still unedited by assertion. |
| [tags-notes](passing/report-reference-tags-notes.png) | Actual Late meal/Exercise chips and complete edited note above fixed Save;6h42 still agrees before the new in-range awake interval is added. |
| [reopened](passing/report-reference-reopened.png) | Saved23:18–06:00, Awake(1),6h32, both tags and note reopen intact; all primary controls remain visible. |
| [period-persisted](passing/report-reference-period-persisted.png) | Same saved boundaries/annotation/tags/note at the actual timeline before a further End action; subsequent Back-discard is verified against the private stored row. |

No introduced core clipping or lost main action was found in these 17 originals. Remaining raster/art/proportion/other-size states are still open; actual measurement shading is intentionally not a decorative stage ribbon. Added Record options/Details/awake controls preserve existing features. Manual 36h/current-time/short-second/DST validation is unchanged source, not newly exercised by this tracked fixture. Hardware touch/haptic feel, real sensors/services/alarms, overnight behavior and complete 36-screen/whole-app acceptance are not established.

## Both failed attempts remain failed

- [First raw set](failed-originals.json): CI 40644483192 at73a0ea65 failed 52.566s,10 required frames plus one diagnostic PNG. After active-gesture Activity recreation, the fixture omitted lifting pointer7, then sent another DOWN. [Unmodified guest log](failed-40644483192/guest-logcat.txt), line2814, explicitly rejects `Invalid DOWN event - pointers already down`. Bounds/awake rollback had passed. Native-only4361dd85 adds one terminalUP and another exact-state assertion before a distinct new gesture; no blind retap or production change.
- [Second raw set](failed-originals-40645865688.json): CI 40645865688 at4361dd85 failed 46.98s,9 required frames plus diagnostic PNG. The new helper immediately asserted editor focus after Awake ends/Use time dismissal. Guest log records the actual tap09:51:05.208 and focus return09:51:05.439; later diagnostic shows the restored main/awake1. Native-onlyef2ded6b waits for the SAME current resumed/focused/attached loaded timeline before geometry/actions, retaining strict checks and adding phase diagnostics. No action replay or production-gate weakening.

All 11 first-attempt and 10 second-attempt PNGs were also individually opened. They are partial failed evidence, not additional passing coverage. All 23+22 failed original files, including guest input/window/error logs, XML, PNGs, environments and failed summaries, are retained byte-identically. [Passing 27 originals](passing-originals.json) and [unified 72-original hash index](original-sha256.json) preserve every downloaded artifact without editing/normalization. [SHA256SUMS](SHA256SUMS) covers archive files; metadata/head records are separate from native-produced artifacts.

## Source and scope identity

Order preserved: production `e93a94d8` → initial native `ccb10153` → source-contract docs `0cd67189` → existing-scope inventory `73a0ea65` → native pointer release `4361dd85` → native owner readiness `ef2ded6b`. [Source comparisons](source-equivalence.json) hash the three production paths against the successful head; production app/src/main is identical across both native corrections. RecordDraft/RecordIntervals/Records/Store/RecordRibbon/RecordEditorSheets/ChartSeries/InteractionFeedback match the published base. [Pointer patch](native-protocol-correction.patch) and [readiness patch](native-readiness-correction.patch) keep the fixture corrections explicit.

The workflow is byte-unchanged. Reversing only the exact Report selector and six added frame-name edits restores the entire prior script AST, preserving every other scope/default. This audit parsed metadata statically; no full script import, broad native suite, unit campaign or device action was run. This passes the stated future Report branch, not the frozen published 0.1.4 APK.
