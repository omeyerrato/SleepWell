# Interaction feedback

Shared native controls acknowledge foreground input with short platform haptics
and pressed/selected visual states in the existing Changing light palette. This
does not change callbacks, saving semantics, navigation destinations or layout.

## 30 October custom-control increment

Version0.1.19 adds missing hooks for sound-level changes and cancellation, trim-handle selection, custom alarm controls and uncovered Appearance/Guard/Automation inputs. These use the shared helpers below; no new output policy or preference is introduced. The implementation has APK packaging only. New tests and physical feel acceptance are deferred by the user. [Source scope](USABILITY-PASS-2026-10-29.md#custom-control-feedback--0119).

## Integration contract

- Preference: `interactionHapticsEnabled`, Boolean, default **true**.
- Read: `InteractionFeedback.enabled(Context)`; unreadable preferences return false.
- Save: `InteractionFeedback.setEnabled(Context, boolean)` returns the synchronous
  preference commit result. A settings row must show failure if it returns false.
  Reading/saving this preference does not itself vibrate or preview an effect.
- Saved strength: `interactionHapticsStrength`, integer Light0 / Standard1 / Firm2,
  default Standard. The current platform requests are `CLOCK_TICK`, `VIRTUAL_KEY`
  and `LONG_PRESS` respectively. The device controls the resulting sensation.
- Explicit user selection and action activation: `InteractionFeedback.selection(View)`
  and `InteractionFeedback.action(View)` use that saved strength.
  It means the input was acknowledged, **not** that asynchronous
  Save, export, permission or network work succeeded. There is no success effect.
- For a custom quiet screen, add Boolean intent extra `quietInteractionFeedback`.
  Existing `visualReview` also suppresses feedback.
- `InteractionFeedback.motion(Context)` allows native ripple motion only while
  app Reduce motion is off, system animators are enabled and touch exploration
  is inactive. Otherwise shared touch/outline surfaces use a static pressed
  tint; outlined controls retain their border.

`Design.page` acknowledges a different navigation tab once; tapping the selected
tab keeps its existing action but produces no selection pulse. `Design.button`,
`iconButton` and actionable `item` acknowledge their click before invoking the
unchanged callback. They do not mark a save as complete or add delayed effects.

`Design.toggle` uses a small native Switch subclass. Only a checked-state change
during its real click/key/drag dispatch acknowledges selection. Initial values,
`setChecked`, restoration and rendering do not emit haptics. The owning changed
listener dispatches before a callback can rebuild its View. Native Switch haptic
requests are suppressed in that subclass so the owned request is not doubled.
The existing changed callback still runs once. An 80 ms shared acknowledgement
gate prevents duplicate nested control/gesture pulses; rapid separate actions
inside that interval may intentionally share one tactile acknowledgement.

Other custom controls/selectors still need explicit integration at their actual
user-change boundary. Do not put these hooks in constructors, renderers, timer
ticks, preference observers, background callbacks or asynchronous save completion.
Do not add an extra action hook around a control already created by Design.

## Quiet and accessibility behavior

Requests use `View.performHapticFeedback(effect, 0)` only. There is no direct
Vibrator/VibrationEffect, permission, forced stream volume or settings override.
Android's touch-feedback preference, the View's own haptic-enabled setting and
hardware support retain final control. Tactile strength varies by device.

For the requested slight strength increase, the
[AOSP platform mapping](https://android.googlesource.com/platform/frameworks/base/+/c03ab432b762d16b20fd5f9366ebb9207be76820/services/core/java/com/android/server/vibrator/HapticFeedbackVibrationProvider.java)
resolves `VIRTUAL_KEY` to `EFFECT_CLICK`, the previous `CONTEXT_CLICK` to
`EFFECT_TICK`, and `CLOCK_TICK` to `EFFECT_TEXTURE_TICK`. These names are not
an intensity scale. The chosen click requests a clearer single acknowledgement;
OEM customization and Android's touch intensity still determine physical feel.
No heavy/long pulse, confirmation-of-success effect or repeated pulse is added.
The existing saved OFF, 80 ms gate and all quiet/provenance guards are unchanged.
This source adjustment has not been physically felt or accepted on a device.

Only an attached, enabled, visible View in a focused live Activity on the main
thread is eligible. Effects are suppressed while an alarm is ringing/active,
tracking is running or the microphone lease is occupied, and throughout alarm,
tracking, motion-sensor and microphone-challenge/preview Activities. They are
also suppressed in visual review, explicitly quiet screens and touch exploration
to avoid duplicating accessibility feedback. No effect is queued for later.
Unavailable hardware or a failed optional acknowledgement never prevents the
underlying user action. Visual pressed/selected state remains available.

The [Android Views guidance](https://developer.android.com/develop/ui/views/haptics/haptic-feedback)
recommends semantic View feedback, which needs no VIBRATE permission. The
[platform constants](https://developer.android.com/reference/android/view/HapticFeedbackConstants)
define the selected native effects. Supplied SleepWell references retain
precedence over generic UI skill palette or layout suggestions.

The shared hooks compile in sourcef0c1a274. One focused silent native flow
verifies the opt-out persists across recreation/reopen, with actual captured
pressed/selected/off states. Review suppresses haptic output deliberately.
Actual touch/drag/accessibility behavior, device strength and every custom
control remain unverified. [Scoped evidence](verification/2026-10-26-settings-organization/README.md).

## 27 October installed update — historical

Latest published and installed **32f064e2** adds Tonight arc/feedback/help
restoration and the organized challenge routine. arm64 APK166,649,432bytes,
original signer, build PASS11m19s; exact tag and both uploaded hashes verified.
[Release/evidence](verification/2026-10-27-tonight-report-routine/README.md).

Tonight45.786s/9 and challenge48.33s/6 inspected captures pass their named
actions/restoration/persistence. **New Report calendar verification failed and
remains incomplete.** These passes do not accept all36 designs or physical outputs.
Phone update installed with original database rows, preferences and user files
preserved; no app launch or physical feature/output test ran.
