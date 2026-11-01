# Challenge Emergency options — source increment, native acceptance pending

2026-11-01. Flow16C, based on `8eb33432b98fc9b1905257a91d81faab8c6e8713`.
The active `AlarmActivity` and ordinary routine Practice headers now have a
named 48dp Emergency options target. The owned Night sheet has a scrolling
explanation and fixed actions: Open system dialer, the current task's configured
Math fallback when available, and Return to current task. Opening, closing or
recreating the sheet never finishes a round, stops/snoozes an alarm, changes a
saved routine or disables a protection preference.

The dialer action rereads the current default-dialer package and exported enabled
activity, then creates an explicit ACTION_DIAL intent with no number/data, extras,
selector, ClipData, URI grant or flags. No call is placed. A changed or missing
dialer is rejected. Screen pinning is not disabled automatically; if Android
pinning prevents this handoff, the sheet explains that Android's own unpin
controls are required while the alarm continues. No new phone/call permission
is requested; the manifest adds only visibility for resolving ACTION_DIAL.

The existing bedtime/alarm guard's system, dialer and emergency exceptions stay
in place. An additional legitimate default-dialer package can be admitted only
for the explicit current alarm run/unit and at most two elapsed-time minutes.
The process-local exception is cleared on return, launch failure or a changed
occurrence/unit, expires without renewal, and grants no access to other app
packages. The alarm service, power-menu policy, challenge proof/advance and
release-build dismissal authority are retained. Shake/NFC/Walk inputs now also
require the active alarm window's focus while this new modal owns input.

Only an actual configured MorningCode Math fallback is offered by this shared
sheet. Production calls its existing guarded `qrFallback` owner. Practice uses
the existing `practiceFallback` checkpoint and the saved code's Math settings;
it has no production occurrence authority. Other tasks keep their existing
specific alternatives; the sheet does not invent a universal task bypass.
The current round still requires an admitted correct Math answer. Practice
focus loss/recreation preserves the draft and reopens only the options sheet,
without an automatic handoff or fallback selection.

The [pending native plan](CHALLENGE-EMERGENCY-NATIVE-PLAN.json) extends an existing
meaningful native method using `routineSlice=emergency`, not a new unit campaign
or the full routine cohort. Six original frames and a proof receipt are required.
Actual Practice gestures cover options/recreation, changed-dialer rejection,
return and configured Math with a real answer. The external handoff is captured
by an injected native port: no phone app or call is executed. Exact lease policy
boundaries are asserted, and Practice cannot issue a real-alarm exception.
Active alarm delivery, actual accessibility/OS handoff and pinning need their
separate authorized runtime acceptance; this slice does not claim those results.

Local validation covers the diff, manifest XML and finite plan/capture/source
consistency only. No build, new native run, hosted cohort, personal phone, alarm,
audio output or model evaluation ran here. Functional/Night acceptance remains
pending integration. The historical source66 state register is unchanged.
