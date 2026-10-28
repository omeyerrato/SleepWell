# Room settings discovery: bounded native evidence

[CI40623526256](https://github.com/omeyerrato/SleepWell/actions/runs/40623526256), exact source `52b6dbb56ff77175ef1116fabb3a12a8596d24ec`: **SearchFlowTest#queryResultsRecreationAndSavedRecordOpenUseRealControls passed in 12.917 seconds, one existing method and all five required original PNGs.** The raw method log reports status0 and `OK (1 test)`; the unchanged summary has no failures and `full_app_review: false`. [run.json](run.json) records the successful authoritative head.

The isolated branch adds one explicit private Room destination to local search and names dimming in the existing Settings → Sleep & bedtime → Room conditions subtitle. All88 earlier catalog entries retain their positions; the new total is89. The nonexported Room Activity, permission flow, saved options and data formats are unchanged. Frozen0.1.3 source/APK remains separate; no integration, publication or installation claim is made.

## Five originals individually inspected

| Original | Actual visible state |
| --- | --- |
| [Empty](search-empty.png) | Global Search SleepWell, both scope controls, empty input and clear search guidance. |
| [Room result](search-room.png) | Settings-only mode, `screen dimming`, one Room conditions result with the actual Sleep & bedtime breadcrumb, full explanatory text and edit chevron. The filter strip scrolls horizontally; its rightmost visible label is partly outside the viewport. |
| [No results](search-no-results.png) | Global mode, `nothing-matches` and settled No matching results, without the earlier loading message. |
| [Private record](search-record.png) | Exactly one dated, eight-hour User-reported record with a full actionable row. The UUID query is longer than the visible single-line input; its complete value is asserted separately. |
| [Record recreated](search-recreated.png) | Same query/count/record after real framework recreation; bytes equal the preceding image. Full query restoration and target record identity are assertions, not inferred only from identical pictures. |

PNG/log/environment/summary artifact bytes are unchanged. [original-sha256.json](original-sha256.json) records eight passing originals and four diagnostically relevant originals from the first failed run. Local attributes preserve bytes; SHA256SUMS covers every retained file except itself. Full guest/renderer logs and the other failed PNGs remain private.

## Inputs, ownership and preservation that ran

API36 disposable x86_64 emulator, 1080×2316 override, density420; `room-settings-search` selected only this method. Both optional-output flags were false and host output was disabled. Previous successful scope selectors/frame manifests were preserved unchanged; they were not rerun.

The method switches the actual Search scope to Settings only and searches `ambient light`, `room sound level` and `screen dimming`. Each query starts from an explicitly awaited no-match result, preventing an earlier matching row from satisfying the next query. The represented result is captured from an owned committed native frame.

A native row tap launches the actual RoomConditionsActivity with its explicit component and inherited quiet-review flag. Unavailable fake light and throwing audio/permission ports prevent physical sensor registration, microphone capture or permission requests. The actual Microphone analysis switch stays off. A native Screen dimming switch input changes only its unsaved draft. Framework recreation retains that draft while the saved choice stays off; actual Back destroys Room, discards the draft and returns to the same `screen dimming` query.

Only the disposable fixture's saved dimming value is temporarily staged off to prevent a prior opt-in from affecting the Room window. Exact original value or key absence is restored after all owned Room windows close, before the full app-preference equality assertion. Ports are restored after those owners close; no microphone owner or lease remains. There is no Room Save, microphone action or OS grant/revocation. These Room states are assertion-backed; this five-image scope does not contain a Room-screen screenshot.

The method returns to global search. A UUID marker in its private record note yields exactly one real RECORD entry with the expected ID. The actual date row is required before capture and tapped into RecordReportActivity; the delivered `record` extra must equal the private ID. The query survives recreation. Reader cleanup, unchanged database fingerprint and exact original preferences remain required; the private database is deleted and factories restored. No count, destination or preservation assertion was removed.

## First failure and precise correction

[CI40622527399](https://github.com/omeyerrato/SleepWell/actions/runs/40622527399), source `2e78c303`, **failed in 15.63s** after the new Room continuation and the next original capture. Its [raw log](failed-40622527399/SearchFlowTest%23queryResultsRecreationAndSavedRecordOpenUseRealControls.txt), [summary](failed-40622527399/focused-review-summary.json) and [environment](failed-40622527399/environment.json) remain unchanged. Record/recreated images were missing. Its original [no-results PNG](failed-40622527399/search-no-results.png) visibly contains Opening local search and is not accepted as a settled no-result frame.

The old UI query `exercise` assumed one global result even though the catalog already included that alias for Tags and quantities in addition to the private record. The failed run did not capture that final result list. The corrected passing method explicitly checks the real index contains that settings alias, uses an unambiguous private record marker, and retains the exact one-record/ID/row/report assertions. Every capture now requires its actual expected status before and after the focused committed-frame screenshot, avoiding acceptance of the observed loading frame. Only the existing native method changed between runs; production source is byte-identical across them.

[source-equivalence.json](source-equivalence.json) pins 23 listed source/workflow blobs, marks deliberate differences from frozen0.1.3, and records production equality between runs. [candidate-commit-order.json](candidate-commit-order.json) preserves source, native, docs, scope and native-correction order. The Main settings-row edit is copy-only and not exercised by this method. Actual hardware light/sound/window dimming, Android permission recovery, every settings destination, all36-screen/reference fidelity and whole-app security remain outside this result. No phone or real provider/output operation was performed.
