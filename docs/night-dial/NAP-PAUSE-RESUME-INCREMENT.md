# Pause and resume the same timer — 1 November 2026

Source implementation; native acceptance remains pending.

Design 30C now has Pause timer and Resume timer for the saved timer. Pause first stores PAUSED with enabled=false and its exact remaining milliseconds; only then does it cancel the system job. Stale broadcasts cannot claim a disabled/non-PENDING timer. A failed cleanup stays visibly paused with Retry cleanup; successful retry clears that warning without arming anything.

Resume rechecks the exact reviewed alarm JSON and permissions, preserves the alarm ID, nonce, sound, delete-after choice, exact-second or minimum-rest kind, and resets a clock anchor before the existing scheduling owner reconciles it. A denied resume leaves the frozen timer unchanged. Another timer cannot replace a paused one without explicit cancellation. Original scheduled-start delay and minimum-rest position are preserved, rather than counting paused time as rest.

Three bounded integer clock fields use optional nested NapPlan JSON version 3. Unchanged timers keep their original v1/v2 encoding. There is no database schema migration or destructive data rewrite. Pause survives activity/process recreation and reboot. After resume, elapsed time governs the same boot; wall time is the existing fallback across a later reboot. Backup restoration still explicitly disables and cancels timers. Older app versions cannot interpret v3 timers, so this is not a promise of backward timer compatibility.

Independent source review caught and fixed anchor-only refresh omissions, successful-cleanup warnings that never cleared, and noncanonical zero-anchor v3 input. These are source conclusions, not physical delivery evidence.

The existing NapFlowTest presetsToneQuietPreviewStartRecreateReopenAndCancelPersist now also performs actual Pause/Resume controls with its private database and injected clock/scheduling/audio ports: immutable timer identity/configuration, rejected stale actions, no stale delivery while paused, frozen time across reopen and simulated reboot, permission-denied resume, same timer rescheduling, same-boot wall-clock changes, and final Cancel. Five additional originals are named in LOCAL-PLAN-NATIVE-PLAN.json. No new unit campaign or physical output is authorized or run.
