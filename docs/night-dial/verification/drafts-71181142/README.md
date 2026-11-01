# Draft and challenge evidence at 71181142

[Run 41262365058](https://github.com/omeyerrato/SleepWell/actions/runs/41262365058)
passed on **2026-11-01** at source
`71181142772adb551904519382bb0828408cac36`:
**all three native methods passed, with all 23 required original screenshots and
all three required proof receipts present.** The exact application-main Git tree
is `c96213193398a7b3defc1f34edef5fa5ef25fc9d`.
The cohort is `draft-challenge-reference`; its scope is practice/draft behavior.

## Exact evidence

[Environment](environment.json), [summary](focused-review-summary.json), all
three [method logs](method-logs/) and the three proof JSON files are unchanged
artifact byte copies. [Method results](method-results.json) independently parse
the actual successful instrumentation results and pin each log hash.
[The original manifest](originals-manifest.json) records all 23 PNG names, byte
sizes, SHA-256 hashes and dimensions: every original is **1080×2316**.

Four [representative original PNGs](representative-originals/) are copied without
re-encoding, totalling 536,601 bytes. [The evidence file manifest](evidence-files-manifest.json)
pins all exact copied evidence; local attributes preserve its line endings.
All originals remain in the already downloaded
`.android-local/ci-reviews/41262365058/silent-android-review/` artifact. No replay
or generated image was used to make this archive. [The run receipt](run-receipt.json)
records GitHub's artifact ID and ZIP digest separately; that ZIP was not downloaded
again or independently rehashed during this archive.

## Verified scope

| Actual native invocation | Observed scope |
| --- | --- |
| `ChallengeRoutineFlowTest#orderedTasksConfigureReorderCancelRecreateAndPersistWithoutOutputs` | The Emergency practice slice: owned options, recreated sheet, changed-dialer rejection, configured QR Math fallback requiring an answer, and Tilt modal focus/checkpoint protection. This is not the entire ordered-routine cohort. |
| `WakeCheckFlowTest#settingsPracticePreviewAcknowledgesAndReturnsUnchangedDraftWithoutOpeningCheck` | Inert wake-check preview, static example ring, private acknowledgement/recreation, Off state and unchanged settings draft on return. |
| `RandomChallengeDraftFlowTest#dirtyBackKeepDiscardAndUseReturnOnlyParentDraftWithoutOutputs` | Real private parent editor/child draft, task edit and nested Back, outer Discard/Keep, rotation and retired-button rejection, clean Back, explicit Use and parent recreation without library writes. |

The [Emergency proof](routine-preview-emergency-proof.json) records that the
dialer launch was **injected**, no real dialer or alarm executed, the configured
fallback did not itself complete a unit, and Practice issued no active-alarm leave
lease. Exact run/unit/package/expiry lease matching was checked as a policy
boundary. Tilt focus loss froze input and retained its checkpoint; returning
required explicit calibration.

The [wake-check proof](wake-check-practice-proof.json) records zero real check
engine opens, runtime-clock opens or scheduling calls; no running check state was
created. Its `2:00` ring is explicitly a static example, not a live countdown.

The [Random proof](random-pool-draft-proof.json) records an unchanged private
library, zero scheduling and no runtime outputs. Use returned only the parent
draft; this run did not apply or enable an alarm.

## Visual review and limits

All 23 originals were inspected through the two existing private contact-board
aids, followed by individual inspection of the four copied originals. The images
show coherent native Night sheets, distinct Keep/Discard actions, recreated draft
state, the same reviewed emergency sheet on recipient rejection, configured Math
practice, the Tilt focus modal and clearly labelled wake-check Practice. Contact
aids are derived inspection tools, not replacement evidence, and are not copied
into this archive.

This was an API 36 x86_64 disposable emulator at density 420. Host audio was
disabled. There was **no active alarm, real dialer/call, personal-phone operation,
sound playback or real wake-check execution**. This proves these bounded native
flows at the pinned source, not full-app/reference completion, physical alarm
protection, accessibility-service return behavior, actual screen pinning, sensor
hardware acceptance or clinical/model performance.

The earlier
[41260557207](https://github.com/omeyerrato/SleepWell/actions/runs/41260557207)
failures remain historical failures at their own source. That run's Random
dialog hit-coordinate defect and Emergency fixture failure were repaired before
this successful rerun. This result does not turn unrelated or newer failing
cohorts into passes. No build, unit campaign, CI dispatch or app-source change was
performed while archiving it.
