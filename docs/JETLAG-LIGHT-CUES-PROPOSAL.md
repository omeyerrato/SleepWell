# Experimental travel light cues: source audit and proposed next vertical flow

Historical proposal status: research/specification only, based on f356f6838620d11c1c008d7e13a6613c811da7d7. See [the separately authorized implementation and evidence](TRAVEL-PHONE-LIGHT-CUES.md) for subsequent source changes. No production source, manifest, build or device behavior changed. No hardware/output actions or tests ran.

## Requirement and current gap

The supplied board09 third screen was visually inspected. Its cream travel layout uses home/destination cards, departure date, paired clocks, an experimental master switch initially off, phone flashlight, a during-tracking setting and About. The local BUILD-PROMPT.md explicitly requires researching feasibility rather than treating a toggle as a finished algorithm; PROMPTS.md says not to present a flashing prescription or medical guarantee.

Current TravelActivity/TravelPlan/TravelPlans implement genuine saved IANA zones, departure-date/DST-aware differences and explicit user-selected daylight reminders. TravelReminders schedules a private immutable inexact request and posts a silent notification. Its generation, late-delivery and retry behavior are real, but neither class emits light or estimates a circadian treatment window. F128 remains partial. Existing AlarmTorch provides a steady alarm-owned torch through TorchLease; it is not a cue engine and cannot be reused as though it supported pulses. TrackingService already has a user-visible specialUse foreground lifetime, active/pause/stop phases, a bounded wake lock and separate automatic/recurring admission.

## What the primary evidence does and does not establish

| Source | Relevant finding and limit |
|---|---|
| [Sleep as Android jet-lag manual](https://www.sleep.urbandroid.org/docs/sleep/jetlag.html) | Describes brief2ms flashes every15s, pre-trip use and phone LED or supported lights. These are vendor feature claims, not validation of SleepWell, arbitrary Android torch timing or individual benefit. |
| [2016 Najjar/Zeitzer JCI study](https://www.jci.org/articles/view/82306) | A39-person laboratory comparison used calibrated xenon flashes around1800lux, with participants awakened and instructed to keep their eyes open during exposure. It studied circadian phase responses, not clinical jet-lag prevention with a phone. Its methods must not be represented as a directly validated asleep-phone protocol. |
| [2014 sleep-flash study](https://pubmed.ncbi.nlm.nih.gov/25227334/) | A13-person parallel experiment compared darkness with2ms flashes every30s during sleep, finding phase delay under laboratory conditions. It does not validate a phone LED, the app's sleep estimate, every direction of travel or the vendor's general preparation formula. |
| [2023 Moving time zones in a flash](https://pubmed.ncbi.nlm.nih.gov/37660233/) | A10-person placebo-controlled crossover used goggles and2ms flashes every15s for one hour during sleep. This is closer to the vendor interval, but is still small controlled-equipment evidence for circadian delay, not a phone-output trial or a guaranteed clinical outcome. |
| [2022 intensity/duration study](https://pubmed.ncbi.nlm.nih.gov/35259981/) | Laboratory responses depended on flash intensity, with less dependence on duration. Unmeasured phone illuminance, distance and eyelid transmission therefore cannot be treated as a known delivered dose. |
| [CDC Yellow Book jet-lag chapter](https://www.cdc.gov/yellow-book/hcp/travel-air-sea/jet-lag-disorder.html) | Appropriate light timing depends on circadian phase, route and zones crossed. The sign of a UTC-offset difference is not sufficient evidence to prescribe an individual advance/delay regimen, especially across the date line. No phase estimator or clinical schedule is proposed here. |

This supports a clearly labeled experimental user-selected cue tool. It does not justify labeling reminders, successful commands, phone-idle estimates or a completed cue session as measured jet-lag prevention.

## Android feasibility and existing ownership gap

[CameraManager](https://developer.android.com/reference/android/hardware/camera2/CameraManager) exposes torch mode and availability callbacks, but no exclusive lease or guaranteed optical pulse duration. Other applications can change the torch; camera-resource contention can revoke it. Therefore a request for2ms must be labeled requested/best-effort timing, never a measured2ms flash. Framework callback timestamps are not photodiode measurements or retinal exposure.

[AOSP torch control](https://source.android.com/docs/core/camera/torch-strength-control) explicitly documents the public APIs as requiring no camera-content permission. Optional strength control begins at API33 and depends on reported hardware support. Existing CAMERA permission is needed for QR enrollment, not a reason to add a new camera-content requirement for this tool. Unsupported strength must say default device strength, not display a fake low-intensity selection.

[Handler timing](https://developer.android.com/reference/android/os/Handler) can be delayed by deep sleep and queue scheduling. A timer loop alone cannot claim precise delivery. Use absolute elapsed deadlines while active, skip missed indices instead of backfilling bursts, and retain a bounded active wake lease. No permanent wake lock, exact alarm, service started from boot, microphone or new camera session is needed.

[Foreground-service types](https://developer.android.com/develop/background-work/services/fgs/service-types) require truthful declared use. The proposal uses only an explicitly user-started manual TrackingService session and amends its existing specialUse explanation if that scope is approved. It does not pretend torch mode requires a camera capture FGS. Future standalone/background automatic cue sessions would need a separate eligibility/design review.

The installed android-permissions-security skill was read in full; least privilege, private components, uncached access checks and caught platform failures apply. The existing android-intent-security/testing-setup guidance and injected native test stack remain applicable. No new permissions/dependencies or component exposure are proposed in this specification.

## Proposed complete flow

1. Keep existing daylight reminders as a separate functioning option. Restore board09's experimental section with master off, Phone flashlight, During manual tracking, a reviewed cue-window row and About. Use the existing cream/Inter grouped rows and modern picker controls. Do not add sample graphs or long clinical paragraphs.
2. User chooses a finite home-IANA calendar window and optional bounded repeat dates before departure. The app previews actual home/destination times and DST resolution. Times are authored by the user; no shift-per-day formula or recommended medical timing is generated. Source profile requests brief pulses at the vendor interval, without a front-panel prescription or any promise of optical timing.
3. Save trip and cue configuration atomically under TravelPlans' revision fence. Save emits no light. A visible Tracking setup action separately arms this reviewed revision for one manual session. Automatic/stillness/recurring starts are explicitly ineligible, even when saved opt-in remains on.
4. Once that manual session is ACTIVE and inside its selected window, the real output adapter requests pulses. Waiting, requested-count, skipped-count, finished and error states derive from actual engine state; a counter denotes requests, never measured flashes or therapeutic success. A separate Stop cues action cancels only its owned session, leaving recording intact.
5. Pause/stop, expired window, changed saved revision, lost tracking ownership, alarm admission, camera busy/unavailable, unexpected external torch changes or output failure revoke the cue generation and release only owned output. Resume/retry requires an explicit remaining-window action and never extends the original deadline.
6. Persist config independently of the current run. Rotation/reopen displays the actual retained run status. Process death/reboot marks a claimed run interrupted, never replays pulses automatically. Saved configuration survives for a later explicit arm. Clock jumps or zone changes during an armed session stop it for review rather than silently interpreting a new treatment schedule.

Supported phone-only behavior after implementation would be a bounded experimental torch-command sequence with honest limits. Smart lamps, sleep masks/wearables, measured dose, REM/dream targeting, closed-eyelid efficacy and a personalized prevention algorithm remain outside this first flow.

## Proposed interfaces and scope allocation

These names/contracts are design only; no empty classes or callable placeholders were added.

| Component | Contract |
|---|---|
| TravelCuePlan + strict codec | Immutable version/revision, reviewed trip snapshot, home IANA zone, finite local window(s), requested profile and enabled flag. Unknown fields/types, invalid dates/zones or stale revisions fail before mutation. |
| TravelCueWindow | Pure resolve/preview of actual calendar intervals, explicit DST policy, finite repeats and no guessed flight direction. Absolute original deadline is never restarted by retry. |
| TravelCueSession | Pure state machine with injected clock, scheduler, liveness, journal and output port. OFF/ARMED/RUNNING/FINISHED/STOPPED/ERROR/INTERRUPTED; nonce+tracking-session+plan-revision fences on every callback. No backfill bursts or output in configuration paths. |
| TravelCueStore | Atomic private-preference config and claimed run state; no database schema or history synthesis. Preserve all existing trip/reminder keys. No output from restore/read/reopen. |
| TravelCueUi | Board09 controls and supporting window/arm/error views. Counts and status are real; capability/unsupported/error/stop states have genuine actions. |
| PhoneTorchCoordinator | A serial process-owned command broker with priority ALARM over EXPERIMENTAL and generation-bound leases. Alarm takeover cancels the experimental generation before its first command; lower-priority cleanup cannot turn off the alarm's new lease. Already-lit external torches are not borrowed, and unexplained external changes terminate experimental ownership. Android remains nonexclusive, so source cannot claim exclusive physical control. |

Necessary narrow existing hooks: TravelActivity/TravelPlans combined draft save; TrackingActivity explicit manual-arm payload; TrackingService accepted-manual admission, phase/lifetime changes, notification status/cancel action; existing manifest specialUse description; AlarmTorch adapter to the shared coordinator. AlarmService may need a minimal priority-takeover hook, to be assigned with the parent. No Main, alarm schedule/protection/challenge, report, noise recorder, backup worker or SQLite schema edits are needed for this flow.

The shared coordinator is necessary: independently copying AlarmTorch would let an experimental OFF callback extinguish an alarm torch, or leave the alarm treating an already-lit experimental source as borrowed. It requires explicit integration ownership and strict source review before production changes.

## Verification plan and acceptance boundary

- Pure fake-clock/output tests: strict config/window/DST/date-line behavior; repeated arm/cancel; finite deadline/no retry extension; stale callback, plan change, tracking pause/stop/death; no automatic/recurring eligibility; delayed ticks skip rather than burst; failed ON/OFF and count semantics.
- Shared torch broker tests: existing external ON is untouched; alarm preempts cues; old cue OFF cannot affect new alarm owner; duplicate/unregister/unavailable callbacks; no resurrection after close; strength capability fallback; output dispatch occurs off the UI thread.
- Private native configuration flow with injected capabilities, clock, journal and output factory that fails if real hardware is constructed. Real save/cancel/arm/recreate/reopen/stop/error controls and all original data/preferences preserved. It must never start actual tracking, AlarmActivity, torch, microphone or an OS scheduled alarm.
- Parent CI captures real native originals and compares them against board09. Compilation/config fake tests alone do not validate optical timing, physical ownership across other apps, sleep preservation, overnight battery/thermal behavior or clinical effect.
- Real waveform/photodiode and controlled hardware timing are a genuine later prerequisite to any precise-duration claim; human/clinical validation is a separate prerequisite to prevention claims. Current restrictions authorize neither.

Await parent scope/ownership agreement before production edits. Research/spec worktree is separate and no Gradle job is running.
