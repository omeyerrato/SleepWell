# Smart wake-up and Travel update

[Development APK and checksum](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-smart-travel-20261027-c6f2fcaa) target **c6f2fcaaad0f83f08130399d76611020a2b39ea7**. assembleDebug passed in 18s; original signer and both uploaded asset sizes/digests verified. APK 334,944,069 bytes; offline models/runtime remain bundled. The phone remains **2cbfe043**, and this update is not installed.

## Changed behavior

Smart wake-up now has actual per-alarm sensitivity and recorded-duration goal overrides. Missing values follow the tracking profile/calibration and global goal; More offers explicit resets. Strict metadata survives defaults/backup/API copies and malformed metadata remains reviewable. Runtime keeps independent counters for each alarm/configuration/deadline, rejects stale/partial/gapped evidence and retains the independently scheduled fixed alarm. Raw saved measurements and separate nap policy remain unchanged. See [model and runtime contract](../../ALARM-SMART-WAKE.md).

The main screen follows board02's window, schedule illustration, length, sensitivity, goal and Save hierarchy. The dawn landscape shows actual configured times, not measured stages. Secondary resets/help no longer clip behind Save. The visible and accessible goal names both say Respect sleep goal; About explains active recorded duration. Child Save settings retains a draft; final Save alarm writes it.

Travel mode groups Daylight reminders and pre-travel sleep schedules under Schedules & reminders, and cue windows/review under Light source. Save stays visible on the main screen. All features remain. The cue date/time uses the subtitle to avoid an isolated AM/PM line. Modal About, draft routes, Back and recreation retain their context. Detail Done returns a draft; final Save owns persistence and reminder reconciliation.

## Bounded native evidence

- [Organization CI40423510216](https://github.com/omeyerrato/SleepWell/actions/runs/40423510216): existing single journey **26.671s**, all13 originals individually inspected against boards02/11/12. Full alarm JSON comparison, exactly one fixture scheduling call, inactive alarm and original-data restoration are retained. High sensitivity and goal On survive recreation and final Save/reopen. More Cancel and About rotate/close are exercised; no About modal screenshot was captured, so its visual comparison is not claimed.
- [Travel CI40426761660](https://github.com/omeyerrato/SleepWell/actions/runs/40426761660): two existing classes/three methods, plan **43.569s**, cue flow **37.968s**, all31 originals individually inspected against board09. Actual reminder/cue edits, draft recreation, Save/reopen, error/retry/disable and cancellation remain exercised. The light implementation is injected; arming review alone produces zero output. This does not establish physical flashlight or Android notification delivery.

Organization source is9429af18; only TravelActivity changed afterward. Travel evidence is at the release source, with unchanged smart-wake/model/shared UI sources. [Candidate identity](candidate.json), original logs/environment/summaries and [PNG-only hashes](screenshots.sha256.json) accompany both scopes. No full-app review, unit/lint batch, personal-phone query/install/launch/output or overnight run occurred.

The earlier Travel run40423517171 passed both cue methods (37.117s) but the plan journey failed on a stale accessibility node immediately after a page redraw. The test now clicks once then waits for native idle; assertions, timeouts and fake-output fences were retained. It was not counted as a complete Travel pass. Earlier organization40416602618 passed configuration but exposed clipped reset/About content; current layout corrects it.

## Remaining limits

This is not all36-screen or whole-board acceptance. Settings' Home-screen row and the alarm editor's Schedule row still partly appear at the first viewport's fixed navigation/footer edge, although the journey scrolls and opens them. Travel retains actual IANA zone labels and a simple arrow instead of the reference's country illustration/plane connector. Large fonts, all supporting routes and full reference fidelity remain open.

Per-alarm early-wake sensor runtime/accuracy, actual alarm protection/loudness/ramp/sunrise/flashlight/challenges, physical haptics, overnight capture/battery/anti-snore and external integrations remain unverified. Reliable additional sound recognition, phone sleep-stage/REM/breathing/apnea and external vendor prerequisites remain unfinished. No stage trace, measured sleep or jet-lag-prevention efficacy is inferred from these controls. Sleep schema17/backup13/private automation2/result database1 remain.
