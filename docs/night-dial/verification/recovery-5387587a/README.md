# Night Dial recovery and recorded-analysis increment — 31 October 2026

Current application source: `5387587a33408ffa583ec1b383811beef712ba4c`.
`app/src/main` tree: `ca3ea36fa3b1efa15f57629c8b6072011600e044`.
Native Java Views, accepted Night Dial design and existing feature owners remain.

## Built candidate, not a public release or phone installation

**0.1.27 / code 28**, API 30+, **arm64-v8a only**. APK size 189,997,079 bytes;
SHA256 `5b9dac6b7b49e9a1ccdccd8dca35afd96807c017f37e621c7590dc78ba9208cf`.
Root independently confirmed the original signer certificate SHA256
`49eeeb7f74344335321800e3f78b9bb9ab25c5fff059332c458133dca0da0e1c`.
The artifact's size, SHA256 and sole packaged native ABI were read-only checked
again during this archive. Root owns the APK copy to Downloads.

Both `assembleDebug` and `assembleDebugAndroidTest` passed. [Unchanged packaging
receipt](build-receipt.json) records 75.109 seconds; the [original build log](build.txt)
reports `BUILD SUCCESSFUL in 1m 12s`, 60 actionable tasks. No unit suite or security
sweep ran. The complete assets tree is byte-identical to `e17bcc70`:
`647ec5411f60daf8919fe969ffb3935397020413`. No model weights or frontend changed.

Packaging happened before native review, so the original receipt still contains
`native_review_run:null`. The later [verification receipt](verification-receipt.json)
links the passing run without rewriting that historical metadata.

This 0.1.27 candidate is **not installed or published**. Latest public release
remains 0.1.25. The separate [0.1.26 in-place installation receipt](../integrated-351b78bf/phone-installation.md)
still records literal ADB `Success` and unavailable post-install readback after
USB disconnection. No new personal-phone action occurred here.

## Exact focused native scope

[Run41232644704](https://github.com/omeyerrato/SleepWell/actions/runs/41232644704)
passes all three explicitly selected existing methods on API36 x86_64 at the
exact current source. [Environment](environment.json) and [original focused summary](focused-review-summary.json)
retain the scope: three methods,30 required original PNGs, `failures: []`,
`full_app_review:false`. The CI test APK and local arm64 delivery APK share source;
this is not a claim that the arm64 APK itself ran on that x86_64 emulator.

| Existing native method | Actual outcome | Original frames |
| --- | --- | ---: |
| [Privacy full history review/erase/retry](PrivacyHistoryFlowTest%23confirmedEraseIsRetainedDbFirstAndPendingAudioRetriesWithoutDeletingNewHistory.txt) | `OK (1 test)`,69.159s |16|
| [Backup reviewed replacement/recreation/reopen](BackupDocumentFlowTest%23explicitReviewedReplacementPersistsAcrossRotationAndReopen.txt) | `OK (1 test)`,12.872s |9|
| [Recorded saved/imported analysis/save/reopen/delete](RecordedAnalysisJourneyTest%23savedClipRunsBundledModelsPersistsAndDeletesSummary.txt) | `OK (1 test)`,19.172s |5|

Privacy runs the full history branch with `ciAudioClearReference:false`, not the
recording-only shortcut. It exercises restored confirmations, stale/dismissed
action rejection, genuine storage error, database-first erase, retained busy
work, queued-audio retry and preservation of a newly added night. Private fixture
alarms/preferences/files remain separately asserted. Backup retains the exact
parsed review across recreation, rejects old/cancelled confirmation controls,
replaces only after explicit confirmation, keeps imported alarms off and reopens
the result. Its typed profile fixture covers currentv3 and genuine legacyv2/v1
semantics, local permission-history exclusion, stale review and rollback. Only
the proven AndroidX `profileInstalled` compilation-status cache is excluded from
outside-file snapshots; application/user-file hashes stay asserted.

Recorded analysis uses its existing licensed lifecycle fixture and AAC copy,
real bundled CPU inference, private source hashes and timing, progress,
save/reopen/exact-summary deletion, and owned-worker cleanup. [Compact proof subset](recorded-analysis-proof-summary.json)
identifies `recorded-experimental-v2-f9dd5647-7bb42270-pann-instrument` and
`accuracyAcceptanceRequired:false`; it pins the SHA256 values of the complete
original proof and AAC timing files retained in the ignored run-artifact directory.
Those originals remain separate provenance. This journey is not frozen FSD50K or old
supplementary-holdout evaluation. No model selection or threshold tuning occurred.

## Original images and visual inspection

[All30 original hashes](original-captures.json) were computed from actual PNG
bytes, with dimensions and sizes. Six representative originals are copied here
byte-for-byte: Privacy restored review, busy, pending and done; Backup restored
confirmation; recorded result details. The complete originals remain in the
root checkout's ignored `.android-local/ci-reviews/41232644704/silent-android-review/`
and the source-specific GitHub artifact. Contact boards are inspection aids,
not replacements for original captures.

Representative originals: [Privacy restored review](privacy-history-review-restored.png),
[busy](privacy-history-busy.png), [pending](privacy-history-pending.png),
[done](privacy-history-done.png), [Backup recreated confirmation](backup-document-confirmation-recreated.png),
[recorded details](recorded-analysis-details.png).

Root inspected all 16 Privacy originals through three actual-PNG contact boards
and individual original keyframes. The recovery agent opened all 9 Backup and
all 5 recorded-analysis originals at original resolution. Observed text, cards,
confirmation actions and completion states fit Night Dial tokens without
material overlap; restored/reopened states retain their composition. The model
identifier and raw scores wrap within their cards, and the details header is
below the status bar. Lower content outside the captured viewport still requires
scrolling; captures alone do not accept all fonts/devices or assistive navigation.

**Preview correction:** an earlier resized-preview report claimed status-bar
overlap. Original-resolution inspection of both the old 41230319052 details PNG
and the current PNG shows correct insets. That report was false. The stable
recorded viewport/revision guard is a source refinement with passing lifecycle
evidence; it is **not credited with fixing a demonstrated visual regression**.

## Earlier failures remain failures

- [41230319052 summary](earlier-failures/41230319052/focused-review-summary.json):
  full Privacy and Backup failed; recorded analysis passed. Original environment
  and both failed instrumentation logs are retained beside that summary.
- [41231393787 summary](earlier-failures/41231393787/focused-review-summary.json):
  Privacy and Backup still failed; recorded analysis passed and all 9 Backup PNGs
  existed. Backup had progressed to a stale settings-profile v2 expectation; its
  separate AndroidX compilation-cache difference remains in the failed log.
- [29fbbe00 local build failure](earlier-failures/build-29fbbe00.txt): test
  compilation rejected a duplicate local `prior` in the profile fixture. The
  current 5387587a rename resolves that compilation failure without changing the
  assertions. That failed build is not the current packaged artifact.

These earlier failed runs and partial captures are not relabelled as passing or
used to inflate the current 30-original count. No rerun was started for this archive.

Full-app 43-flow/all-state acceptance, broad regression/security review,
physical alarm/audio/capture/haptics, external providers, overnight reliability,
stage/breathing accuracy and reliable automatic bedroom sound classes remain
open. The small [FSD50K policy study](../../../FSD50K-RECOGNITION-CALIBRATION.md)
still records instrument 1/8→6/8 and laughter 3/8 unchanged, with its misses and
limits. Stage-data public access is resolved and the 650 MB-capped original-model
pilot remains research, not a useful phone Deep/REM accuracy claim.
