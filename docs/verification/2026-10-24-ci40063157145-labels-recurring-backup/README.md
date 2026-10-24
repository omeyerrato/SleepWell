# Full-run native milestones and remaining failures — CI40063157145

Frozen source: `244e4bcfd195250886a90d2f10aafeffb0166d7b`.
[Original run](https://github.com/omeyerrato/SleepWell/actions/runs/40063157145).
This scoped archive retains all50 exact class logs, environment/full summary,
all available label/nap/recurring/backup original PNGs. Other full-run images are
not copied into this folder. SHA256SUMS covers every raw file retained.

**Overall FAIL:**44/50 classes wholly pass,93/100 methods pass,7 fail;
275/299 required named originals exist (277 total PNGs including diagnostics).

New real native evidence, with private persistence and fake effect boundaries:
- SoundLabelStorageTest10 PASS1.106s; linked identity/reset/ABA/trim/delete,
  schema6-to7 migration, unchanged WAVs, portable metadata and original evidence.
- SoundLabelFlowTest3 PASS26.038s; all18 originals available for strict review.
- RecurringTrackingTest12 PASS0.258s; real private preferences and fake delivery/
  scheduling/stop, including ownership, admission and normalization.
- BackupStorageTest6 PASS0.433s; private SQLite and fake transport/effects.
- NapFlowTest3 PASS20.594s; actual native configuration/storage, fake timer.
- AlarmMenusFlowTest1 and MissedNightFlowTest2 pass after lifecycle/action waits.

Remaining failed classes: SoundCategoryFixtureEvaluationTest (unchanged laughter
positivity), AlarmToneFlowTest (typed picker monitor miss), WakeCheckFlowTest
(1/2, later Editor launch after escaped DocumentsUI), NapIntegrationFlowTest
(Remove nap confirmation wrongly gated on Activity focus while Dialog focused),
RecurringTrackingFlowTest (1/2, stale click in recreated/reopened draft), and
BackupDocumentFlowTest (1/3, posted picker callback not awaited and confirmation
frame gated on background Activity focus). The typed picker containment and
owned-window/chooser readiness changes require fresh native execution. No
production behavior regression is inferred solely from those harness failures.

Native UI failures remain failures; missing final frames are not synthesized.
No actual alarm, microphone, tracking, flashlight, notification delivery or
external document provider is proven. Only isolated emulator fixture data is
included. Full app/design-board acceptance remains incomplete.
