# Optional protection for all alarms

The user explicitly accepts best-effort protection like Challenges Alarm. Its
[official listing](https://play.google.com/store/apps/details?id=com.garageapp.alarmchallenges)
describes accessibility-based protection for leaving the app and turning off the
device. SleepWell implements its own metadata-only guard; no vendor code is used.

The compact Alarms root contains each time, enable switch, name and repeat days.
Tapping the time/name opens the editor. Repeated Smart wake-up and Challenges
rows were removed from the list. Alarm protection is linked from every editor
and from global Alarm options. Both switches apply to all real ringing alarms:

- Prevent leaving app returns to the challenge after another ordinary app opens.
- Prevent power off sends accessibility BACK for a narrowly recognised system
  power-menu class, then returns to the challenge if BACK succeeded.

The existing alarmGuard key is preserved. The new alarmPowerGuard key defaults
off without writing a default. Each switch is independent; neither enables
the accessibility service automatically. The disclosure leads to Android's
accessibility settings only after the user chooses it. Calls, Settings,
permission controllers, emergency screens and unrelated system dialogs remain
exempt. Known nested emergency/SOS menu classes are explicitly excluded.

Both protections require the current process's foreground AlarmService to be
active, ringing=true, and a positive active alarm id. Quiet developer tests and
stale ringing preferences cannot trigger them. Service destruction clears its
liveness flag before cleanup. Back/return callbacks recheck eligibility.

PowerMenuGuard recognises exact AOSP and Samsung global-actions class names,
never generic android.app.Dialog or menu text. It waits 150 ms for a newly
reported menu, limits repeated attempts, and cancels on a new window, a different
observed focus package/window, interrupt, unbind or destruction. Package/class/window-ID
metadata only: canRetrieveWindowContent remains false. No text, view-tree,
password, key or screenshot retrieval; no root/device-owner provisioning.

This does **not** verify the current input focus at callback time. Android's
interactive-window APIs require screen-content capability, which this service
does not request. AOSP clears the source window ID when content retrieval is
disabled, so an unknown ID must not disable otherwise recognised menu events.
Window IDs add a fence only when present; package/class event-generation
fencing remains the metadata-only basis. See the source delivery in
[AbstractAccessibilityServiceConnection](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/services/accessibility/java/com/android/server/accessibility/AbstractAccessibilityServiceConnection.java)
and [AccessibilityRecord.setSource](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/core/java/android/view/accessibility/AccessibilityRecord.java).
Event-generation fencing reduces stale actions but cannot
prove a menu is still present when no fresh event arrives. Fast taps, unknown
OEM classes, delayed/missing events and system restrictions can defeat it.
Actual Samsung menu detection/BACK delivery remain unverified because the user
prohibited power-menu/alarm tests. No absolute shutdown guarantee is claimed.

Holding hardware buttons to force a restart, switching off before an alarm,
force-stop, uninstall and disabling accessibility remain possible. Alarms need a
powered-on phone; BootReceiver reschedules after boot, not while powered off.
Managed-device lock task is a different capability and is not provisioned here.
See [Android lock task](https://developer.android.com/work/dpc/dedicated-devices/lock-task-mode),
[alarm lifecycle](https://developer.android.com/develop/background-work/services/alarms),
and [interactive-window capability requirements](https://developer.android.com/reference/android/accessibilityservice/AccessibilityServiceInfo#FLAG_RETRIEVE_INTERACTIVE_WINDOWS).

Validation at `ab01ba59337e1e767b573a2e61ad40037eb6ed3c`: eleven pure policy tests pass for known/unknown/nested emergency
classes, liveness, independent eligibility, delay/generation/focus cancellation,
closure and failed BACK cooldown with fake callbacks. AlarmProtectionFlowTest
uses isolated configuration and display-only alarm rows, saves no alarm and
never launches AlarmActivity/Service, opens a power menu or changes access.
Its native configuration/recreation test passes in 15.794 s on API36, including independent switches, reopening through both entry points, a visible 48dp Alarm-on control above Save, unchanged personal preferences and unchanged isolated alarm rows. The disclosure is a rounded cream/Inter dialog with explicit Later. All four silent configuration/navigation tests pass in [CI39954661465](https://github.com/omeyerrato/SleepWell/actions/runs/39954661465); [original images, raw logs and scope](verification/2026-10-23-alarm-protection/README.md) are retained. The physical S23 read-only root test passes in 19.215 s with exact personal preferences/database rows preserved. These tests do not certify accessibility enforcement, Samsung menu recognition or actual BACK delivery during an alarm.
