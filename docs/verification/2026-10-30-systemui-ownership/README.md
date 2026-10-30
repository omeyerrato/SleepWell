# System-bar ownership: bounded source finding

Source inspection at `371c5b9e` on 30 October 2026. **No production change, new capture, build, test, CI run or device/output action was made for this finding.**

The [original failed captures and rejected experiment](../2026-10-29-systemui-transitions/README.md) remain unchanged. They show incorrect first-frame glyph contrast despite the correct requested appearance mask, followed by correct later and recreated frames. Those later frames do not replace or excuse the original mismatch. This remains an explicit visual limitation, separate from the application content/reference work.

## Current application ownership

`AppTheme` declares light status/navigation appearance false before Activity content, but has a cream launch background. `Design.page` then gives the Window and content the actual plum/cream background, disables system contrast scrims and requests the corresponding status/navigation appearance. MainActivity changes Tonight and Alarms inside that same Window. Its focus request reads the current destination; the existing posted/200 ms requests are already present, not proposed fixes from this pass.

A matching launch background could reduce a launch-surface color mismatch, but cannot by itself synchronize rendered icon polarity when this same Window switches between dark Tonight and light Alarms. Separate Activities/themes would be a navigation architecture change with unproven benefit, not a justified small correction here.

## Platform boundary

The [WindowInsetsController API](https://developer.android.com/reference/android/view/WindowInsetsController) describes `getSystemBarsAppearance()` as the **requested** appearance. Its returned bits are not an acknowledgement that SystemUI has drawn matching pixels. Controllable-insets callbacks concern control availability, not completion of icon tint rendering.

Android's [system-bars design guidance](https://developer.android.com/design/ui/mobile/guides/foundations/system-bars) documents dynamic gesture-handle color adaptation to the content behind it on Android 15 and later. In the [Android 16 QPR2 AOSP LightBarTransitionsController](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android16-qpr2-release/packages/SystemUI/src/com/android/systemui/statusbar/phone/LightBarTransitionsController.java), SystemUI owns icon-tint animation and can defer it for an app transition; its default tint-animation duration is 120 ms. This is a plausible explanation for requested-state/rendered-pixel timing differences, **not proof that this exact path caused the pinned emulator failure**.

## Decision

No additional delayed requests, polling for acceptable pixels, changed capture timing, fixed opaque bar strip, hidden API or speculative window rewrite was introduced. The earlier combined-request experiment already failed and remains reverted. The source evidence does not justify another equivalent patch or a claim that the transition is fixed. Keep first-frame contrast open while continuing the actual content flows; stable correct bars are a bounded observation, not all-frame acceptance.
