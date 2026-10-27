# Rhythm and Nap: bounded native verification

Two existing focused methods **passed** at
`bfc27151a5e3c0e191589ca0b9fd296aff0fcc56` in
[CI 40546119570](https://github.com/omeyerrato/SleepWell/actions/runs/40546119570):

| Focused method | Native result |
| --- | --- |
| [SleepRhythmFlowTest#actualSevenNightsWorkdaysPersistRecreateAndEmptyWithoutOutputs](SleepRhythmFlowTest%23actualSevenNightsWorkdaysPersistRecreateAndEmptyWithoutOutputs.txt) | One method passed, **19.697 seconds**, five original frames. |
| [NapFlowTest#presetsToneQuietPreviewStartRecreateReopenAndCancelPersist](NapFlowTest%23presetsToneQuietPreviewStartRecreateReopenAndCancelPersist.txt) | One method passed, **16.758 seconds**, eight original frames. |

**The overall CI job failed.** Gentle wake settings, Health Connect and Privacy
history failed separately. The unmodified [combined focused summary](focused-review-summary.json)
retains those failures and missing frames. This archive establishes the two
bounded results above, not a passing overall job or complete app review.

The tested presentation changes are `8dae55bd` (Rhythm; original source `99ba8464`)
and `af367beb` (Nap; original source `06e8c964`). No source or test was changed for
this evidence archive. [The original environment](environment.json) pins the
disposable API 36 x86_64 emulator, 1080×2316 override and density 420. Captures use
default text size. Host output is disabled, native playback is not allowlisted,
and audio imports are disabled. No physical phone action was used.

All thirteen original PNGs were opened individually and compared with board 09's
left Rhythm and middle Nap frames in `design-concepts/2026-10-22-phone-app/`:

| Original | Observed result |
| --- | --- |
| [Rhythm, seven nights](rhythm-seven-nights.png) | Dated real fixture bedtimes, dashed goal guide and matching capsule legend remain legible. The chart is compact; neutral group labels and indented clock values align. Chronotype progress follows Social jetlag directly, with secondary navigation below. All core sections appear on the first frame. |
| [Workdays](rhythm-workdays.png) | Monday off and Saturday on are visible draft choices; all seven day rows and the complete Save workdays button fit. |
| [Workdays recreated](rhythm-recreated.png) | The same unsaved day choices survive recreation; Save workdays remains complete. |
| [Rhythm reopened](rhythm-reopened.png) | Saved workday classification produces the actual changed midpoint comparison (`9 min`, `2:54 AM` and `3:03 AM`). Reopening and discarding a later draft preserve that saved result. |
| [Rhythm empty](rhythm-empty.png) | No main nights, missing range and midpoint dashes, zero group counts and `0 of 30 nights` remain honest. No invented bedtime markers or chronotype label appear. |
| [Nap default](nap-default.png) | Duration and smaller preset units lead into Ends at, Smart wake-up, Sound and Delete after dismissal. Music glyph, indented waveform, smaller visual play circle and centered Start glyph follow the reference. Power nap and Custom timer remain direct visible actions. The whole Start/Multiple naps/Add schedule entry footer fits. |
| [Nap preset](nap-preset.png) | Physical selection changes the real draft to 30 minutes, selected preset and computed end time to `1:00 PM`; the core sequence and footer stay clear. |
| [Tracking required](nap-tracking-required.png) | Smart wake-up stays off and the real missing-tracking error is visible. The complete deletion row and switch sit above the fixed error/action footer; automatic body scrolling exposes that row. |
| [Nap sound](nap-sound.png) | Pulse selection is reflected in the label and actual asset waveform. The fake preview's active state uses Pause in the indented play target; no physical audio output is implied. |
| [Nap pending](nap-pending.png) | The saved 30-minute deadline and remaining time are shown. Editing actions are disabled and the complete Cancel nap footer replaces Start. |
| [Nap recreated](nap-recreated.png) | Deadline, selected duration, Pulse and delete-off state survive recreation; Cancel nap remains complete. |
| [Nap reopened](nap-reopened.png) | Reopening the saved nap preserves the same real deadline, duration and settings. |
| [Nap cancelled](nap-cancelled.png) | Start, Multiple naps, Add schedule entry and Remove saved nap are available after durable cancellation. The additional fixed footer row reduces body height: lower settings require scrolling, and this frame does not establish that the deletion row is fully visible at the same time. |

The relocated Nap actions are exercised before the default screenshot in the
passing existing method: the physical Power nap action reaches the actual
`Minimum-rest mode` settings, Back returns to Nap, Custom timer opens `Timer
duration`, and Cancel returns to Nap. Assertions confirm **zero scheduling calls
and zero preview opens** during those navigation actions. These modal titles are
asserted by the native journey; no extra modal screenshot is claimed.

The remaining Nap journey selects Pulse, opens one fake preview, changes deletion
to off, recreates the screen, starts exactly one fake-scheduled 30-minute timer,
checks the persisted tone, deletion setting, challenge and nonce/deadline,
recreates/reopens, then cancels durably with **zero alarm fires**. It does not
execute real alarm delivery. Multiple/schedule actions remain visible here; their
other dedicated methods were not selected in this run.

The Rhythm journey checks the seven-night chart and history target, private
workday draft recreation, saved mask `62`, unchanged source records, durable
reopening and cancellation of a later draft, then the empty-record state.
Its labelled timing fixture is synthetic, not personal data or sensor-accuracy
evidence. Thirty nights remains a history target, not a validated diagnostic
cutoff or an inferred clinical chronotype.

No introduced clipping or overlap requiring a source correction was observed in
the core default/pending layouts at this runtime. Secondary Rhythm rows and the
taller cancelled-Nap state retain ordinary body scrolling. This closes the
bounded presentation/callback regression checks only; large text, other locales,
exact-duration application, Power nap interval persistence, other schedule/list
flows and physical-phone delivery are outside these two selected methods. It
does not close all 36 reference screens.

All thirteen PNGs, both exact instrumentation logs, environment and combined
summary were copied directly from this run's original artifact and compared by
SHA-256. [The hash manifest](original-sha256.json) records all seventeen originals;
folder-local Git attributes preserve their original bytes.
