# Alarm search, challenge ordering and audio import

[Development APK and checksum](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-search-audio-20261027-33cb5693) target **33cb5693208f137119df68365a91c9679fcce5e1**. The **arm64-v8a**
APK is166,642,573bytes, about50% smaller than the previous universal package;
offline models/assets remain bundled. Production build passed in22seconds.
Original signer, exact Git tag and both uploaded asset sizes/SHA-256 digests
match the local files. [Candidate identity](candidate.json).

The phone remains **2cbfe043**; this release is not installed. No phone query,
launch, unsuspend, alarm, sound, flashlight or haptic output occurred.

## Changed behavior

Edit alarm searches42 real settings with breadcrumbs, conditional availability
and draft-only navigation. Query, nonzero scroll position and parent context
survive recreation and returning from details. Settings search fits in its
header; the main Settings and alarm rows clear the dock/fixed Save footer.
Smart wake-up Options/About use the shared modal setup, retaining dark system
glyphs on cream after dismissal/recreation.

Fixed-sequence challenge cards have genuine six-dot drag handles and bounded
edge scrolling. Tap Move up/down/Remove remains an accessible alternative.
Cancelled/stale/header/blank drops do not change the draft. Only final Save
alarm writes. [Complete ordering contract and six captures](../2026-10-27-challenge-ordering/README.md).

Explicit selected-audio preparation supports bounded installed Android decoding,
stereo downmix and rational resampling with exact source metadata. Actual AAC
export/import exposed a38ms AOSP encoder tail discontinuity; export now clocks
only that verified AAC-LC encoder's access units from the continuous input sample
count. Imported timestamps are never repaired, and the2ms gap guard remains.
The fixture's154ms codec padding stays disclosed in its actual13.824second
decoded duration. Both analysis windows remain Uncertain.
[Audio contract and limitations](../../RECORDING-AUDIO-IMPORT.md).

The package also includes the previously built optional guarded smart-wake API
scalars. Default13 columns, existing grant/ownership/revision guards and full
alarm data remain; actual external callers are still unverified.

## Bounded native evidence

- [Organization40445147772](https://github.com/omeyerrato/SleepWell/actions/runs/40445147772)
  at eb3a7da8: existing1method33.114s, all14 originals individually inspected
  against boards02/11/12. Whole initial Home screen and Schedule rows, actual
  status/navigation contrast flags before every capture, About restoration,
  search/draft state, final Save/reopen, full JSON/original preferences and one
  disabled fixture scheduling call pass. Organization-related sources are
  unchanged between this run and the release; only AAC encoder source changed.
- [Recorded analysis40444367323](https://github.com/omeyerrato/SleepWell/actions/runs/40444367323)
  at3359881e: existing1method40.27s, all5 originals inspected against board07's
  supporting style. Actual platform AAC encoding/decoding, bundled CPU models,
  progress, Save/reopen/delete and original-data/worker cleanup pass. Audio-source
  implementation files match the release. Selection is a read-granted fixture
  content URI, not DocumentsUI or real external-provider acceptance. Original
 13.670seconds become216 actual AAC packets/221,184 decoded samples; packet and
  source/result proof JSON are preserved in the recorded-analysis folder.
- [Challenge ordering40442729962](https://github.com/omeyerrato/SleepWell/actions/runs/40442729962)
  at0043aaa4: existing1method40.237s, all6 originals inspected against board04;
  real drag/cancel/fallback and Save/reopen pass. The later editor change restores
  system bars after an owned modal; ordering code remains unchanged.

These are three separately scoped journeys on a host-muted API36 x86_64 emulator,
not a combined full-app or physical-arm64 run. No new unit/lint/broad campaign
ran. Original environments, method logs, summaries,19 new PNGs and a PNG-only hash
manifest accompany this note; the other6 PNGs are in the linked ordering folder.

Earlier import attempts are retained as failed evidence:40436899037 stopped at
test-provider acquisition;40438735539 reached the real decoder and rejected the
tail gap;40440341089 diagnosed it;40442037855 proved final-buffer EOS alone did
not fix it. No import tolerance, model threshold or holdout gate was weakened.

## Remaining limits

All36-screen/full-board fidelity remains open. Challenge header/routine-mode/
available-task composition and full-routine preview still differ from board04.
Recording-analysis supporting cards are not the reference Sound events player.
Large fonts, all supporting routes, physical haptic feel, real alarm protection/
loudness/ramp/sunrise/flashlight/challenges and overnight tracking/battery/anti-snore
remain unverified. Other codec/rate/channel/provider combinations and external
integrations need their own acceptance. Reliable additional recognition and
phone-input sleep-stage/REM/breathing/apnea remain unfinished.

Sleep schema17/backup13/private automation2/result database1 remain unchanged.
No source file, user record, model threshold or paid feature restriction was
removed. Other-ABI and universal source builds remain available; this downloadable
APK packages arm64 native libraries only.
