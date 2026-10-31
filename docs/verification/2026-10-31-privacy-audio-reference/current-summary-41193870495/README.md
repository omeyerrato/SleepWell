# Privacy: current storage totals after recording cleanup

[Run41193870495](https://github.com/omeyerrato/SleepWell/actions/runs/41193870495), source **`cc4871eefa5833266f79d6f1b319d5561c1bf285`**, narrow `privacy-audio-reference` scope: **PASS36.286s, one existing native method and all nine required original PNGs**. [Summary](focused-review-summary.json), [environment](environment.json), [native log](PrivacyHistoryFlowTest%23confirmedEraseIsRetainedDbFirstAndPendingAudioRetriesWithoutDeletingNewHistory.txt) and [SHA/source manifest](originals.json) retain exact source and original bytes.

Every frame below was individually opened as a complete native window and compared with [board12's Privacy screen](../../../../design-concepts/2026-10-22-phone-app/12-privacy-and-appearance.png). The confirmation/progress/error/retry pages are supporting states without dedicated mockups. They retain the reference's cream/brown typography and surfaces, lilac selected favorites switch and full app-owned actions. The active modal controls and updated summary text fit at the captured1080×2316 /420dpi geometry.

The existing method's explicit `ciAudioClearReference=true` branch uses the UUID-private SQLite/owned-file fixture described in the [parent archive](../README.md). This is actual local worker behavior against fixture files, not a simulated success callback. It preserves three persisted sound-event fixtures, user-corrected label/favorite/revision, session/note and disabled alarm while correctly detaching deleted clip links. Ordinary database backup JSON, preferences and pre-existing file hashes remain unchanged. It does **not** enter the original history-erasure branch or perform microphone, playback, alarm, vibration, torch, provider or personal-phone operations.

## Current states and comparison

| Original | Captured state |
|---|---|
| [Confirmation](privacy-recordings-confirmation.png) | Actual reviewed one-recording selection with favorites On; complete warm Delete/Keep controls. |
| [Favorites included](privacy-recordings-favorites-included.png) | Off includes both owned files; the count and switch state follow actual selection. Cancelling still keeps the files. |
| [Review error](privacy-recordings-review-error.png) | Injected fixture-storage error leaves the explicit deletion action disabled and Keep available. |
| [Removing](privacy-recordings-removing.png) | Held worker, disabled delete row, busy indicator and both totals **Loading…** with **Updating local storage…**. The prior missing-count snapshot is not displayed as current. |
| [Recreated removal](privacy-recordings-removing-recreated.png) | Same held cleanup after Activity recreation; the new owner displays loading placeholders rather than the prior missing count. |
| [Pending](privacy-recordings-pending.png) | Current one-record/zero-byte snapshot still honestly displays **1 recording unavailable** for the deliberately blocked owned directory, with the real queued-cleanup notice. The missing-file state was not hidden to obtain a pass. |
| [Retry](privacy-recordings-retry.png) | Reopened queued-only review, complete Retry/Keep controls and no favorites draft for the previously frozen selection. |
| [Done](privacy-recordings-done.png) | Settled one-record/zero-byte totals and completion message; **no stale unavailable caption**. |
| [Reopened](privacy-recordings-reopened.png) | Same settled totals and retained completion after closing/reopening; **no unavailable caption**. |

Compared with the [older done frame](../current-41186059054/privacy-recordings-done.png), the completion frame no longer combines zero audio bytes with the previous one-recording-unavailable caption. During real pending failure the caption still appears, and the stored clip identity/journal remains until retry succeeds.

The updated same-method assertions require loading/updating while held and recreated, genuine missing state while pending, then actual settled totals and an absent summary-warning view before both final captures. The native pass is therefore evidence of the refreshed states, rather than merely a different screenshot timing. Receipt refresh, data mutation/admission and deletion worker code remain separate and unchanged.

This is one API36 x86_64 isolated-emulator/default-text cohort with host audio disabled. It does not establish every font scale/device, reduced-motion state, process death, representative phone/provider storage failures, the combined history-erasure flow, all36 designs or full feature parity. No new test method, broad/unit-test campaign, build, security review or device/output action was performed to prepare this archive.
