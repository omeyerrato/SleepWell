# Recording-only cleanup: bounded native action evidence

[Run41186059054](https://github.com/omeyerrato/SleepWell/actions/runs/41186059054) at captured source **`e9077085b8a06a4bdfeddb28fa43d687f8527258`** passed **one selected native method in33.539s, with all nine required original PNGs**. The [summary](current-41186059054/focused-review-summary.json) explicitly says `full_app_review: false`.

`PrivacyHistoryFlowTest#confirmedEraseIsRetainedDbFirstAndPendingAudioRetriesWithoutDeletingNewHistory` ran only its explicit `ciAudioClearReference=true` recording-only branch. Despite the existing method name, this scope **does not run or accept the original history-erasure branch**. No new test method, unit-test campaign, provider operation or real output was introduced.

Each of the nine current originals below was individually opened as a full native window and compared with [board12's Privacy screen](../../../design-concepts/2026-10-22-phone-app/12-privacy-and-appearance.png). The board has no dedicated deletion confirmation/progress/error mockups. These supporting states retain its cream/brown surfaces and Inter-style text, lilac selected favorites switch, explicit scope and full primary/secondary actions. No clipping of the active modal's actions was found at the captured geometry.

[Environment](current-41186059054/environment.json): isolated API36 x86_64 emulator, 1080×2316 at420dpi, host audio disabled, no playback allowlisted. [Native log](current-41186059054/PrivacyHistoryFlowTest%23confirmedEraseIsRetainedDbFirstAndPendingAudioRetriesWithoutDeletingNewHistory.txt) and [SHA/source provenance](current-41186059054/originals.json) retain the original bytes. The Privacy UI/worker files are unchanged from0.1.24 package source `d1b954de`; this does not relabel the captured revision or accept unrelated later changes.

## Main action, failure and persistence

The fixture owns a UUID-private SQLite database, one synthetic12-byte retained-file fixture and an owned nonempty directory used to force an actual unlink failure. It seeds one private retained night/note, one disabled alarm and **three persisted sound-event fixtures**: two original snoring categories and one speech category. Two events link to the two owned clip rows; the third has no saved recording. One retained event has a favorite, a user-corrected speech label and revision7. These are stored fixture metadata, not classifier/audio-analysis results or playable WAV recordings.

- With Keep favorites On, the actual review excludes the starred audio; Off includes both files. Cancelling, including invoking a retained dismissed confirmation, leaves them untouched.
- An injected private-storage review error keeps deletion disabled and the explicit Keep action available.
- A held actual asynchronous cleanup worker disables the action and preserves busy progress/status through Activity recreation. The worker then really removes the ordinary owned file and retains the blocked directory's exact clip identity and queued intent.
- Closing and reopening Privacy produces a retry review for only that queued selection. After removing the fixture's owned child blocker, the real retry completes and the final reopen retains the completed status with no queued deletion or saved clip rows.
- Exact session/note JSON and the fixture alarm remain. All three sound-event rows retain exact IDs, timestamps, durations, original categories/scores/model, favorites, user labels and revision. The expected SQLite clip pointer changes are asserted separately: the first event detaches after actual unlink, the blocked event retains its pointer while pending, then detaches after retry; the originally unlinked event stays unlinked. The exact count/category distribution remains three/two-snoring/one-speech.
- Ordinary application database backup JSON, global preferences and all pre-existing file hashes remain unchanged. Cleanup drains the cleanup jobs, finishes owned Activities, restores factories and removes only owned fixture resources.

This closes the narrow recording-only confirmation/favorites/error/recreation/retry/reopen evidence gap. It does not establish sound-recognition accuracy, audio playback, retention expiry, process death, the combined history-erasure flow, Android document-provider durability or personal-phone data preservation.

## Current original inventory

| Original | Actual state and comparison |
|---|---|
| [Confirmation](current-41186059054/privacy-recordings-confirmation.png) | Cream rounded native dialog, explicit audio-only scope, lilac favorites On and both complete actions. Actual selection is one recording. |
| [Favorites included](current-41186059054/privacy-recordings-favorites-included.png) | Grey Off switch, actual two-recording count and full Delete/Keep actions; the draft is then cancelled. |
| [Review error](current-41186059054/privacy-recordings-review-error.png) | Actual injected fixture error, visibly disabled brown deletion action and enabled Keep action. |
| [Removing](current-41186059054/privacy-recordings-removing.png) | Disabled main delete row, brown busy marker and readable removing status. |
| [Recreated removal](current-41186059054/privacy-recordings-removing-recreated.png) | Same busy status after Activity recreation; submission stays disabled. |
| [Pending](current-41186059054/privacy-recordings-pending.png) | Visible actual one-recording retry notice; retained night count stays one. |
| [Retry](current-41186059054/privacy-recordings-retry.png) | Actual queued-only review; favorites draft is omitted, Retry/Keep remain complete. |
| [Done](current-41186059054/privacy-recordings-done.png) | Completion status is visible; the asynchronous storage summary still carries the previous unavailable-recording caption in this immediate frame. This is not claimed as a fully settled summary snapshot. |
| [Reopened](current-41186059054/privacy-recordings-reopened.png) | Retained completion, one night/zero audio bytes, and the unavailable caption has cleared on the refreshed summary. |

Default geometry and ordinary app text are the captured scope. Enlarged-text confirmation fit and the reduced-motion spinner branch were not exercised by this selected method. The source respects the saved reduced-motion choice, but this archive is not its native-state acceptance. No phone, microphone, alarm, sound, vibration, torch or security review ran. All36 design states and broader feature/device acceptance remain separate.

## Preserved failed predecessor

[Run41184901351 before evidence](before-41184901351/README.md) failed after28.882s on a stale assertion that no clip row should remain while unlinking the owned directory was still pending. Its six valid earlier UI captures and failure diagnostics remain unchanged, with their original failing summary. Fixture-only commit `83dacc72` corrected the expected pending identity and added the nonempty event-retention assertions above. **No production worker change or rebuild was needed to correct that assertion.**
