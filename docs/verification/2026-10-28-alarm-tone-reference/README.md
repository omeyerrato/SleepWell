# Alarm sound reference evidence

[CI 40605830605](https://github.com/omeyerrato/SleepWell/actions/runs/40605830605) at `5bf664434ca303d26069e59287cc0e3c3a55a497`: `AlarmToneFlowTest#toneSelectionQuietPreviewImportCancelRecreateAndSavePersistWithoutOutput` **PASS, 31.887 seconds, one existing method**. The combined `label-navigation` job passed **two methods / 24 required originals**; `full_app_review` remains false. This folder owns the 16 Tone originals only; the eight Guard frames are archived separately.

All 16 PNGs were opened individually against [Board 03 left](../../../design-concepts/2026-10-22-phone-app/03-sound-and-snooze.png). The exact method log, environment and unaltered combined pass summary accompany them. [original-sha256.json](original-sha256.json) records byte sizes and SHA-256 for all 19 original artifacts, copied without conversion from `.android-local/ci-reviews/40605830605/silent-android-review/`. Local attributes preserve their original bytes. [source-equivalence.json](source-equivalence.json) pins eight Tone/host Java files, the existing native method and three bundled WAV assets; root main resolves to the same captured source commit. This archive changes no production or test code.

## Actual label-input correction

The existing method waits for a committed focused Tone frame, checks the whole enabled selection parent is visible and at least 48 dp, checks that Chime's name has no consuming tooltip and the parent owns its full-name tooltip, then issues a native coordinate tap at the name's measured bounds. It asserts Select Chime becomes selected. The `selected` original shows that radio state, the Chime preview panel and Use Chime action. This proves normal name-label input reaches the production selection callback, beyond direct callback invocation.

## Individually inspected originals

| Original PNG suffix (`alarm-tone-`) | Observation |
| --- | --- |
| builtins | Three named tones with real decoded waveforms; Daybreak selected, separate 80% alarm / 25% preview controls and complete preview/save actions. |
| playing | Daybreak pause controls and Stop appear; 7% preview is separate from the 80% alarm draft. |
| paused | Play preview returns while Stop remains; radio selection is unchanged. |
| preview-error | Complete inline preview failure, 7% draft and fixed Use Daybreak remain visible. |
| options | Sound playback, Extra loud ON, Gentle wake-up and Done are legible in the actual dialog. |
| selected | Native name tap selects Chime; actual alarm value is 65%, preview remains 7%. |
| import-checking | Checking audio state appears during held injected validation; Chime remains the active draft. |
| recreated | Chime, both slider drafts and complete interrupted-import message survive; preview does not restart. |
| import-cancelled | Interrupted-import recovery remains explicit; selection and footer are preserved. |
| decoder-busy | Held old decoder refuses a new fake session; complete failure line appears near the tight lower card edge, with Use Chime visible. |
| decoder-recovered | Explicit retry after releasing the old session restores Chime pause/Stop controls. |
| import-empty | My audio shows Device alarm plus the real Import audio file action, without a fabricated waveform. |
| import-error | Full injected document-access failure is visible; the existing Chime draft remains selected. |
| imported | Injected Selected recording.wav is selected, with no invented waveform and Use My audio. |
| reopened | Saved imported name, 65% alarm value and My audio selection reopen; preview resets to its separate default 25%. |
| cancelled | Cancelling a later Pulse draft leaves the saved imported choice and name intact. |

The alarm slider's native raw progress 55 maps to displayed/saved 65% because its range starts at 10%; the fixture asserts that exact saved value. Preview uses a separate 0-25% scale, so 25% places its thumb at that scale's right edge.

## Verified scope and remaining limits

The emulator uses actual AlarmEditor/Tone widgets, named private SQLite records, real recreation, fake preview sessions and an injected import source. The method verifies gain 0.25 then 0.07, pause/error/decoder ownership, interrupted import cancellation, retained drafts, the typed OPENABLE audio request cancelled by an inert monitor, rejected file URI and injected denied content URI, successful injected metadata, explicit Save/reopen and discarded later edits. Alarm scheduling and wake-check coordination are inert ports; enabled alarms, AlarmActivity and native playback are not used. Original preferences are checked unchanged and private fixtures/owners are cleaned up.

The three waveforms are actual bundled PCM16 WAV data decoded into 32 peak bins, normalized by ClipWaveView for display; zero samples stay on the center line. Decode is bounded to 4 MiB, mono/stereo PCM16 and two minutes. The real loader is retained in this method, which waits for all three wave views. The shapes therefore differ from the board's illustrative envelopes. They are static normalized amplitude previews, not measured loudness, an audition or advancing playback-position evidence. Imported audio intentionally has no waveform in this journey.

The cream hierarchy, tabs, tone rows, import row, independent volume controls, preview card and fixed brown footer follow Board 03. The fixture-only provenance label adds vertical space; the import glyph is a document instead of the board's folder. Error states use additional truthful copy and tighter spacing; the lower card edge is cropped in the decoder-busy initial capture, so that state is not claimed as exact full-card reference acceptance. Supporting options remain in the header menu. Default/playing/paused preview slider, action and footer pass full-control 48 dp visibility assertions.

Environment: disposable API 36 `sdk_gphone64_x86_64`, 1080 x 2316 override, density 420, host output disabled, `audio_imports: false`, no native playback allowlist. Actual speaker/stream volume, sound quality, MediaPlayer decoding, Files/provider grants or persistence, process death, accessibility/device/font variants and physical-phone behavior remain unverified. No user audio/provider, all-36-screen or whole-app acceptance claim follows. Creating this archive performed no tests, build, CI dispatch or device/output action.
