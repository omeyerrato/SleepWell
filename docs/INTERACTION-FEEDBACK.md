# Interaction feedback

Shared native controls acknowledge foreground input with short platform haptics
and pressed/selected visual states in the existing Changing light palette. This
does not change callbacks, saving semantics, navigation destinations or layout.

## Integration contract

- Preference: `interactionHapticsEnabled`, Boolean, default **true**.
- Read: `InteractionFeedback.enabled(Context)`; unreadable preferences return false.
- Save: `InteractionFeedback.setEnabled(Context, boolean)` returns the synchronous
  preference commit result. A settings row must show failure if it returns false.
  Reading/saving this preference does not itself vibrate or preview an effect.
- Explicit user selection: `InteractionFeedback.selection(View)` uses the native
  `CLOCK_TICK` semantic effect.
- Explicit action activation: `InteractionFeedback.action(View)` uses native
  `CONTEXT_CLICK`. It means the input was acknowledged, **not** that asynchronous
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

This is a source milestone. No phone/emulator, haptic/alarm/audio output, unit
batch or build ran here. Actual touch/drag/accessibility behavior and subjective
strength require a later consented device check; source hooks do not prove that
all custom controls across the app already participate.
