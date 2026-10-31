# Recording-only UI: first bounded attempt

[Run41184901351](https://github.com/omeyerrato/SleepWell/actions/runs/41184901351), source `d1b954deed7c1bad8cb6d46dad9063082b21f7ea`, scope `privacy-audio-reference`, **FAILED after28.882s**. One existing native method selected its explicit `ciAudioClearReference=true` branch; it never entered the history-erasure branch. [Original summary](focused-review-summary.json), [environment](environment.json), [native log](PrivacyHistoryFlowTest%23confirmedEraseIsRetainedDbFirstAndPendingAudioRetriesWithoutDeletingNewHistory.txt) and [SHA provenance](originals.json) retain that failure without upgrading it to acceptance.

Six required original frames were captured before the failure. Each was individually opened as a complete native window and compared with [board12, Privacy](../../../../design-concepts/2026-10-22-phone-app/12-privacy-and-appearance.png). Supporting confirmation/progress/error states have no separate mockup; they retain the warm cream/brown typography and lilac selected switch used by the main reference.

- [Confirmation](privacy-recordings-confirmation.png): real reviewed selection, favorites On, full Delete/Keep actions and explicit audio-only scope.
- [Favorites included](privacy-recordings-favorites-included.png): actual draft Off and two selected files; cancellation still keeps both files.
- [Review error](privacy-recordings-review-error.png): injected fixture-storage error disables deletion while Keep remains available.
- [Removing](privacy-recordings-removing.png) and [recreated removal](privacy-recordings-removing-recreated.png): actual held worker, disabled main action, visible busy marker and status text survive Activity recreation.
- [Pending](privacy-recordings-pending.png): actual failed unlink of the fixture's nonempty owned directory leaves a visible retry message; the ordinary selected file has been removed.

The failed assertion expected no clip rows during pending cleanup. Existing `SoundClips.finish` removes a clip row only after its file unlink succeeds, so the deliberately blocked directory correctly leaves one clip row and one queued intent. This is a stale fixture expectation, not evidence of a production deletion bug. The missing retry/done/reopened frames are not accepted by this attempt. The retained [failure PNG](privacy-history-failure.png) and [XML](privacy-history-failure.xml) are diagnostics only.

Fixture-only follow-up `83dacc72` corrects that exact pending identity expectation and adds three nonempty persisted sound-event fixtures. Its comparison preserves every event field except the clip pointer, whose deliberate nulling is separately asserted at actual unlink. It does not change the production worker, and it requires its own later run.

The first attempt's event table was empty; unchanged empty JSON does not prove populated estimate retention. Files were private synthetic deletion fixtures, not playable/audio-analysis inputs. No microphone, playback, phone, alarm, vibration or flashlight output ran. These six layouts and the failed assertion do not establish all nine states, all36 designs, combined history cleanup or real-device behavior.
