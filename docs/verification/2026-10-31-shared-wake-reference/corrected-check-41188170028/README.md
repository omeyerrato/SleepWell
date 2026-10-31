# Corrected wake-up check presentation

[Run41188170028](https://github.com/omeyerrato/SleepWell/actions/runs/41188170028), scope `wake-check-reference`, exact source `dd8c3748cc836acef44bd0338eeb7df792cabe4a`. The same existing `WakeCheckFlowTest#silentPromptCountsDownRecreatesConfirmsAndReopensDurably` passed in **30.05s**, with all **seven required original Android screenshots**.

Every original was opened individually against [board04/right](../../../../design-concepts/2026-10-22-phone-app/04-challenges.png) and the [earlier25-frame source archive](../README.md). The copied1080×2316 PNGs are unmodified; [environment](environment.json), [focused summary](focused-review-summary.json), [native log](WakeCheckFlowTest%23silentPromptCountsDownRecreatesConfirmsAndReopensDurably.txt) and [source/SHA manifest](originals.json) retain their exact identity.

## Before and after

- The old explanatory caption touched the button visually. [Waiting](wake-check-waiting.png), [prompt](wake-check-prompt.png), [countdown](wake-check-countdown.png) and [recreated countdown](wake-check-recreated.png) now show a clear10dp gap. The same native capture helper checks that exact gap and that the complete caption remains visible.
- The amber footer now spans the full inset-safe column, with rounded top corners, square bottom corners and the reference-style button inset. The native helper checks full panel width/visibility and complete48dp-or-larger footer actions. The I'm awake target retains its larger74dp height. System-bar insets and scrolling remain owned by the existing layout.
- [Confirmed](wake-check-confirmed.png) and [reopened confirmation](wake-check-reopened.png) show the sharp new high-resolution raster check, sampled and tinted by the native ImageView. [Unavailable saved state](wake-check-storage-error.png) uses the existing crisp native DawnSun renderer instead of enlarging a small control glyph. The error and Done action remain clearly visible.
- The fake-clock ring still reflects actual fixture deadlines: full at2:00 and reduced at1:15, with the same1:15 retained across recreation. Confirming persists the result; reopening retains it. The storage-error state is explicit, and the method's original personal-preference/database/no-real-output assertions remain intact.

This closes the two specific visible residuals recorded in the earlier archive for the captured default geometry. It does not establish every size/font or all36 references. The source still uses private fixture state with injected clock, scheduler, notification and re-ring ports; no phone, real alarm delivery, native sound, vibration or flashlight output ran. It does not launch or accept real `AlarmActivity` admission, ringing, snooze/completion authority, pinning or sunrise behavior.

No new source edit, build, CI execution or device action was performed while preparing this archive. These results belong only to the recorded source and selected existing method; the earlier Math/routine25-frame evidence remains separately scoped.
