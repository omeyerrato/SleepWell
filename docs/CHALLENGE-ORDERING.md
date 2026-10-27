# Ordering challenge routines

Open an alarm, then **Challenges**. Hold a six-dot handle and drag its card onto
another card to move the task. Cards near the viewport edge can scroll the list.
Tap the same handle for **Move up**, **Move down** or **Remove task**; these remain
available without a drag gesture. Each handle has a 48dp target and an accessible
description. Its dots use the app's vendored, licensed Phosphor glyph.

A drop changes the editor's fixed-sequence draft only. Dropping on the header or
blank space, cancelling, or dropping on the same card leaves its order unchanged.
The local drag carries a private controller and exact alarm/routine snapshot;
stale, detached, recreated, different-Activity and modal-covered targets cannot
apply it. No user data enters global ClipData and no URI grant is involved.
Random pools retain their existing configuration flow.

**Save routine** returns the sequence to the alarm draft. Only final **Save alarm**
writes the alarm and schedules it. Gesture acknowledgements use the existing
setting-respecting feedback policy; visual-review and quiet contexts suppress
haptics. Source highlighting and destination outlines clear on completion or
cancellation. Physical haptic feel remains unverified.

The existing single configuration journey passed on the host-muted API36 emulator
at source **0043aaa4**, [CI40442729962](https://github.com/omeyerrato/SleepWell/actions/runs/40442729962),
in **40.237 seconds**. It performs a real cancelled drag, a real Retype card drag,
the tap-menu fallback, recreation/cancellation and final Save/reopen. Private
fixture rows remain unchanged before final Save; a separate alarm, original
preferences and malformed metadata are preserved. The scheduler is injected and
the alarm stays disabled. No alarm, sensor, microphone, pinning or sound runtime
is launched.

All six original Android captures were individually compared with board04.
Numbered cards, handles, order and restored task details are readable, with no
stuck highlight or clipped footer. This accepts the changed ordering flow, not
the entire reference screen: header positioning, the additional routine-mode
control, available-task composition and a full-routine preview still differ from
that board. Whole-board fidelity and physical challenge acceptance remain open.

## 27 October installed update

Latest published and installed **32f064e2** adds Tonight arc/feedback/help
restoration and the organized challenge routine. arm64 APK166,649,432bytes,
original signer, build PASS11m19s; exact tag and both uploaded hashes verified.
[Release/evidence](verification/2026-10-27-tonight-report-routine/README.md).

Tonight45.786s/9 and challenge48.33s/6 inspected captures pass their named
actions/restoration/persistence. **New Report calendar verification failed and
remains incomplete.** These passes do not accept all36 designs or physical outputs.
Phone update installed with original database rows, preferences and user files
preserved; no app launch or physical feature/output test ran.
