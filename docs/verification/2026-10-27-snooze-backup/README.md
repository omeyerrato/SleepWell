# Snooze & backup: scoped native evidence

Source: `f7a6337b37fabfa7aef15a4035487159f83d3447`.
[GitHub Actions run 40541487743](https://github.com/omeyerrato/SleepWell/actions/runs/40541487743) completed on 2026-10-27. **The overall job FAILED** because the separate QR configuration method failed. This folder records the **passing Snooze scope only**: `org.sleepwell.app.AdvancedSnoozeFlowTest#longMaximumAndPerPressChoicePersistCancelAndRecreateWithoutRuntime`, 25.721 seconds, `OK (1 test)`, all 10 required Snooze originals. The unmodified combined summary retains the QR failure instead of presenting a successful whole job.

The emulator was API 36, `sdk_gphone64_x86_64`, 1080 x 2316 override at 420 dpi. Environment metadata states host output disabled and no allowlisted native playback output. Fixtures use private data, an injected scheduler and fake playback outputs. No phone or physical sound verification is represented here.

## Original-image inspection

All 10 PNGs were opened individually and compared with `design-concepts/2026-10-22-phone-app/03-sound-and-snooze.png`, right-hand Screen 09. Image bytes, native class log, environment and combined summary are copied unchanged from the retained CI artifact. `run.json` is the read-only GitHub metadata export captured while assembling this folder. SHA256SUMS covers all retained files except itself.

| Original | Observed state |
| --- | --- |
| `advanced-snooze-initial.png` | Duration 5 min/count 1, shortening off; backup enabled 5 min/Pulse 100%, wake check 1 min. Complete primary rows fit above Save. |
| `advanced-snooze-options.png` | Real supporting options expose per-press choice,90 min cap, Buttons & orientation and the legacy backup page; per-press is initially off. |
| `advanced-snooze-options-recreated.png` | Same options parent after recreation, now retaining per-press on and90 min cap. |
| `advanced-snooze-sound-picker.png` | Actual Daybreak/Pulse/Chime selector with Pulse selected; existing draft remains visible behind it. |
| `advanced-snooze-sound-picker-recreated.png` | Open selector and Pulse selection survive Activity recreation, with actual Cancel. |
| `advanced-snooze-draft.png` | Up to 60 min/count 2/shortening on, backup 7 min/Chime 35%, wake check 10 min; complete main controls remain above Save. |
| `advanced-snooze-recreated.png` | The same changed values survive draft recreation. |
| `advanced-snooze-reopened.png` | Actual Save/reopen retains Chime 35% and the other changed settings. |
| `advanced-snooze-invalid.png` | Unreadable sound/volume are marked Needs review; review/fallback explanation and explicit Reset action appear. Conditional repair content needs scrolling rather than being discarded. |
| `advanced-snooze-repaired-draft.png` | Explicit confirmed repair restores Pulse 100% in the draft while retaining other choices; the test verifies stored Chime 35% is unchanged until Save. |

The native method also checks unchanged full stored JSON before Save, cancellation and exact picker ownership, retained time cap/burst/timeout fields, Save/default-copy behavior and the hidden block surviving a partial caller-field patch. These are native assertions in this one passing method, not new separate unit batches.

## Fake-output and compatibility bounds

The method instantiates the actual `AlarmPlayback`, `AlarmWakePolicy` and `AlarmVolumeLease` with fake Sink/Port/Journal implementations. Assertions cover custom Chime 35% on both backup entry and start-in-backup, immediate backup envelope, primary return, retained-primary replacement failure, bounded source fallback, late-callback fencing, lower-level boost admission, baseline release, user stream changes, denied/unwritable/fixed-volume fallbacks, quiet routing and legacy Pulse 100%. This proves policy/orchestration decisions with fake outputs. It does **not** exercise MediaPlayer, AudioManager writes, LoudnessEnhancer, speaker gain, physical restoration, background delivery or real playback failures. Percentage is a requested alarm level, not acoustic loudness; device volume steps may round it.

The optional `backupSound` object uses bundled `daybreak`/`pulse`/`chime` and integral volume 10..100. Legacy absent/default Pulse 100% remains omitted from alarm serialization; SQLite remains version 17. The same native method verifies new format 14 export/restore, legacy format 13 import with absent settings defaulting to Pulse 100%, default-template transfer and malformed-block rejection before replacement. Malformed raw data is retained for explicit repair; primary delivery is not rejected merely because this optional block is unreadable. Its in-memory backup fallback is Pulse 100%, with review reporting. No content-URI grant surface or caller columns are added. The format 14 gate makes older applications reject newer portable exports instead of silently dropping custom backup settings; it is not a downgrade roundtrip guarantee.

## Remaining visual and coverage limits

The reference hierarchy and bounded main-control fit are supported by these native images and assertions. This is not a pixel-perfect or whole-app acceptance claim. The visible slider rail remains pale at both 100% and 35%, whereas board03 has a brown progress fill; its thumb/value and actual state changes pass. Backup sound uses a plain value/chevron and speaker glyph instead of the reference's value pill/music glyph; other icon details differ. Error/repair content uses the existing scroll path. Those visual residuals are retained honestly in the originals.

Only the represented normal text size, isolated settings/error states and fake-output policies are accepted here. No hardware/audio quality, real alarm delivery, personal-device data migration, every accessibility size or separate QR result is established. These files do not validate later source changes or the newer Gentle preview.
