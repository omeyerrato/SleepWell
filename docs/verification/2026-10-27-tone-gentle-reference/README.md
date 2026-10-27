# Tone and Gentle: bounded reference evidence

Source: `170b0e44785c52429f7b4c089e234ca9d18ce102`.
[Run 40557496255](https://github.com/omeyerrato/SleepWell/actions/runs/40557496255) completed on 2026-10-27. **The overall job FAILED.** Privacy History failed after its busy state at `PrivacyHistoryFlowTest.java:72`; its raw log is retained. The unchanged combined summary records Cloud Backup and QR Configuration as passing. Those separate flows are not visually accepted by this folder.

The two scoped native methods pass:

- `AlarmWakeSettingsTest#actualDraftRecreationSaveReopenAndCancelWithoutOutputs`: **61.028 seconds, OK (1 test), 17 originals**.
- `AlarmToneFlowTest#toneSelectionQuietPreviewImportCancelRecreateAndSavePersistWithoutOutput`: **26.344 seconds, OK (1 test), 16 originals**.

All 33 original PNGs were opened individually and compared with board 03 left/middle at `design-concepts/2026-10-22-phone-app/03-sound-and-snooze.png`. Copies retain their CI bytes, as do the scoped logs, Privacy failure log, environment and combined summary. `run.json` is a read-only GitHub metadata export and independently identifies the same head and failed job. SHA256SUMS covers all retained files except itself.

## Actual reference comparison

Tone now shows a warm peach selected tab, taller separated tone/import rows, real brown progress rails and decorative speaker endpoints. The built-in screen retains all three choices, import, both volume controls, preview and complete Use action above the dock. Chime/65% and preview 7% render consistently. The imported file's actual fixture name, Selected recording.wav, appears in the selected row and preview heading; My audio selection and filename persist after Save/reopen and after cancelling another draft. More retains Sound playback, Extra loud and Gentle wake-up.

The actual PCM waveforms differ from the reference's illustrative envelopes; they remain data-derived. The first `wake-settings-sound` capture precedes asynchronous waveform completion, so it has empty waveform lanes; the dedicated Tone builtins capture awaits and shows all three. The preview slider's real range is 0..25%, so 25% correctly fills its rail rather than copying the reference's quarter-position thumb. The synthetic Tone fixture label is CI-only. The shared document/speaker glyphs, exact typography/row proportions and grey system-bar marks still differ from the reference.

Gentle now has the inline ramp selector, measured chart lanes, readable axes/ticks/endpoints and a true quadratic policy curve. Its normal 80%, extra-loud 100%, one-minute and Off states remain distinct; the chart is a plan, not a measured acoustic graph. Sound delay, vibration/timing, sunrise, flashlight summary, low-volume Preview and complete Save action fit in the represented main states. The real More dialog retains flashlight/About and restores its open state/checked value after recreation. The button labels disclose 8-second ramp sampling or 3-second steady sampling when Off, with delay skipped; loading, playing, stopped-after-recreation, error and completion messages remain readable above Save. The sample has no play glyph, and the flashlight summary adds a line absent from the board; this is bounded fidelity, not exact raster parity.

No new primary-control overlap or clipped main action was found. One conditional Tone limit remains: `alarm-tone-decoder-busy` combines the import-interrupted message and preview-unavailable message, putting the lower preview message partly below the scroll viewport. Play/Stop and Use remain complete, but the frame does not prove a full simultaneous error-message fit. The source's real scroll container retains access; no error text was removed. This archive does not establish every larger-text, RTL, device or multi-error state.

## Originals inspected

| Original | Observation |
| --- | --- |
| `alarm-tone-builtins.png` | Warm Built-in tab; Daybreak selected; all three PCM shapes and complete volume/preview/Use controls. |
| `alarm-tone-selected.png` | Chime selected, 65% primary and 7% preview rails, matching Use Chime. |
| `alarm-tone-playing.png` | Daybreak row pause and preview Pause/Stop agree; complete footer. |
| `alarm-tone-paused.png` | Play/Stop reflect paused preview, with the selected source retained. |
| `alarm-tone-preview-error.png` | Failure text remains readable beneath preview controls; Use remains complete. |
| `alarm-tone-options.png` | Named Sound playback, Extra loud, Gentle wake-up and Done fit in the real dialog. |
| `alarm-tone-import-checking.png` | Checking audio state coexists with the existing Chime draft and preview. |
| `alarm-tone-recreated.png` | Chime/65%/7% survive; interrupted-import message visible. |
| `alarm-tone-import-cancelled.png` | Same preserved draft and actionable interrupted-import guidance. |
| `alarm-tone-decoder-busy.png` | Busy decoder leaves Play/Stop/Use present; lower error partly outside body viewport as noted above. |
| `alarm-tone-decoder-recovered.png` | Chime row pause and preview Pause/Stop return after the old decoder lease closes. |
| `alarm-tone-import-empty.png` | Empty My audio state offers Device alarm and Import; existing Chime preview/draft is not falsely discarded. |
| `alarm-tone-import-error.png` | Unavailable audio message with real retry/import route and retained Chime draft. |
| `alarm-tone-imported.png` | Actual filename appears in selected row/preview; My audio and Use My audio agree. |
| `alarm-tone-reopened.png` | Saved filename/My audio/65% reopen with complete Use. |
| `alarm-tone-cancelled.png` | Cancelling the later Pulse draft leaves saved My audio/65% intact. |
| `wake-settings-sound.png` | Sound main controls fit; asynchronous waveform lanes are still empty in this first capture. |
| `wake-settings-loud-curve.png` | Two-minute curve ends100%; delay Off, vibration from start, sunrise off. |
| `wake-settings-preview-loading.png` | Cancel preview and opening message fit above Save. |
| `wake-settings-preview-playing.png` | Stop preview and disclosed8-second sample fit; main controls remain visible. |
| `wake-settings-preview-recreated.png` | Preview returns to idle with an explicit stopped-after-recreation message. |
| `wake-settings-preview-error.png` | Retained private failure offers retry; no footer overlap. |
| `wake-settings-preview-complete.png` | Completion message and idle preview action fit above Save. |
| `wake-settings-vibration-options.png` | Open timing selector survives recreation; From start selected and full Cancel. |
| `wake-settings-delay-options.png` | Sound-delay selector retains its selected Off value and full Cancel. |
| `wake-settings-options.png` | Real flashlight switch/About dialog and complete Done. |
| `wake-settings-options-recreated.png` | Dialog parent/flashlight checked state retained. |
| `wake-settings-lights.png` | 100% curve, 3-minute delay, vibration off/disabled timing, sunrise and flashlight on. |
| `wake-settings-recreated.png` | Same draft controls and flashlight summary after recreation. |
| `wake-settings-reopened.png` | Saved gentle settings reopen with complete Preview/Save. |
| `wake-settings-normal-curve.png` | Turning Extra loud off gives a real 80% curve while preserving other choices. |
| `wake-settings-edited-curve.png` | One-minute curve/ticks and After 2 min vibration timing remain readable. |
| `wake-settings-immediate.png` | Off draws a steady 80% plan and accurately labels a 3-second steady sample. |

## What the native methods prove

Both methods use a private database, disabled alarm fixtures, injected scheduling and fake preview ports. Tone exercises actual selection/ranges/More, paused/resumed/error preview state, an injected import held/cancelled across recreation, decoder busy/recovery, inert document cancellation, invalid/denied import, Save/reopen and cancelled-draft preservation. The actual document/media decoder output is not established by its fake import source.

Gentle exercises actual editor draft/recreation/Save/reopen/cancel, delay/timing selectors and More restoration. Its real preview controller uses the selected Daybreak source through a looping fake session: starts at zero, reaches 0.0625 gain at fake-clock 4s in the extra-loud quadratic 8s sample, and reaches the strict 0.25 cap before completion. Every fake gain asserts finite 0..0.25. It verifies loading cancel, stale callbacks, focus/route cleanup, old decoder closure, recreation without new allocation or auto-resume, retained error/retry and completion cleanup. The same method checks 80% target gives 0.20 endpoint/0.05 midpoint, configured delay is skipped, Off is 3s steady, labelled Daybreak radio fallback/first-listed playlist source, silent-mode refusal, timeout and synchronous callback cleanup. These are controller/port assertions, not sound measurements.

Environment: API 36 emulator, sdk_gphone64_x86_64, 1080x2316 override at 420dpi; host output disabled and no native playback output allowlisted. No MediaPlayer/AudioManager output, hardware alarm-stream write, AlarmService delivery, haptic/sunrise/flashlight output or personal-phone action is accepted here. Requested percentages/digital gain are not acoustic loudness. The complete 36-screen goal and all reference states remain open; this folder accepts only the represented Tone/Gentle layout and their two passing fake-output journeys.
